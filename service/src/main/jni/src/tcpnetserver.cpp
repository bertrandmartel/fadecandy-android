/*
 * Open Pixel Control and HTTP server for Fadecandy.
 *
 * This is a TCP server which accepts either Open Pixel Control, HTTP, or WebSockets
 * connections on a single port. We use a fork of libwebsockets which supports serving
 * external non-HTTP protocols via a low-level receive callback.
 *
 * Copyright (c) 2013 Micah Elizabeth Scott
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

#include "tcpnetserver.h"
#include "version.h"
#include "libwebsockets.h"
#include "rapidjson/stringbuffer.h"
#include "rapidjson/writer.h"
#include <iostream>
#include <algorithm>
#include <sys/socket.h>
#include "fcserver.h"
 
#ifdef __ANDROID__

#include "android/log.h"
#include "jni.h"
#define APP_NAME "fadecandy-server"

#endif // __ANDROID__

TcpNetServer::TcpNetServer(OPC::callback_t opcCallback, jsonCallback_t jsonCallback,
                           void *context, bool verbose)
    : mOpcCallback(opcCallback), mJsonCallback(jsonCallback),
      mUserContext(context), mThread(0), mVerbose(verbose), loop_control(true)
{
}

bool TcpNetServer::start(const char *host, int port)
{
    loop_control = true;

    const int llNormal = LLL_ERR | LLL_WARN;
    const int llVerbose = llNormal | LLL_NOTICE;

    static struct libwebsocket_protocols protocols[] = {
        // Only one protocol for now. Handles HTTP as well as our default WebSockets protocol.
        {
            "fcserver",             // Name
            lwsCallback,            // Callback
            sizeof(Client),         // Protocol-specific data size
            sizeof(OPC::Message),   // Max frame size / rx buffer
        },

        { NULL, NULL, 0, 0 }    // terminator
    };

    struct lws_context_creation_info info;
    memset(&info, 0, sizeof info);
    info.gid = -1;
    info.uid = -1;
    info.host = host;
    info.port = port;
    info.protocols = protocols;
    info.user = this;

    // Quieter during create_context, since it's kind of chatty.
    lws_set_log_level(llNormal, NULL);

    struct libwebsocket_context *context = libwebsocket_create_context(&info);
    if (!context) {
        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "libwebsocket init failed\n");
        lwsl_err("libwebsocket init failed\n");
        return false;
    }

    // Maybe set up a more verbose log level now.
    if (mVerbose) {
        lws_set_log_level(llVerbose, NULL);
    }
    __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "Server listening on %s:%d\n", host ? host : "*", port);
    lwsl_notice("Server listening on %s:%d\n", host ? host : "*", port);

    // Note that we pass ownership of all libwebsockets state to this new thread.
    // We shouldn't access it on the other threads afterwards.
    mThread = new tthread::thread(threadFunc, context);

    return true;
}

void TcpNetServer::threadFunc(void *arg)
{
    struct libwebsocket_context *context = (libwebsocket_context*) arg;
    TcpNetServer *self = (TcpNetServer*) libwebsocket_context_user(context);

    /*
     * Mostly we're just handling incoming events from libwebsocket's poll(),
     * but we do have some non-latency-critical broadcast events to flush out
     * periodically.
     *
     * These would be faster if we could wake up libwebsocket's poll() from another
     * thread, but there isn't an easy way to do that now, and we don't really care
     * that much. During normal operation we'll be receiving lots of data over the
     * network anyway.
     */
    while ((libwebsocket_service(context, 100) >= 0) && self->loop_control) {
        self->flushBroadcastList();
    }
    close_service_fd(context);
    libwebsocket_context_destroy(context);

    if (FCServer::jvm != 0) {
        FCServer::jvm->DetachCurrentThread();
    }
    else {
        __android_log_print(ANDROID_LOG_ERROR, "snoop decoder", "jvm not defined\n");
    }

    __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "Server close");
}

void TcpNetServer::close() {
    loop_control = false;
}

int TcpNetServer::lwsCallback(libwebsocket_context *context, libwebsocket *wsi,
                              enum libwebsocket_callback_reasons reason, void *user, void *in, size_t len)
{
    /*
     * Protocol callback for libwebsockets.
     *
     * Until we have a reason to support a non-default WebSockets protocol, this handles
     * everything: plain HTTP, Open Pixel Control, and WebSockets.
     *
     * For HTTP, this serves simple static HTML documents which are included at compile-time
     * from the 'http' directory. Our web UI interacts with the server via the public WebSockets API.
     */

    TcpNetServer *self = (TcpNetServer*) libwebsocket_context_user(context);
    Client *client = (Client*) user;

    switch (reason) {
    case LWS_CALLBACK_CLOSED:
    case LWS_CALLBACK_CLOSED_HTTP:
    case LWS_CALLBACK_DEL_POLL_FD:
        if (client && client->opcBuffer) {
            free(client->opcBuffer);
            client->opcBuffer = NULL;
        }
        self->mClients.erase(wsi);
        break;

    case LWS_CALLBACK_ESTABLISHED:
        self->mClients.insert(wsi);
        break;

    case LWS_CALLBACK_HTTP:
        return self->httpBegin(context, wsi, *client, (const char*) in);

    case LWS_CALLBACK_HTTP_FILE_COMPLETION:
        // Only serve one file per connect
        return -1;

    case LWS_CALLBACK_HTTP_WRITEABLE:
        return self->httpWrite(context, wsi, *client);

    case LWS_CALLBACK_SOCKET_READ:
        // Low-level socket read. We may trap these for OPC protocol handling.
        if (client->state != CLIENT_STATE_HTTP) {
            return self->opcRead(context, wsi, *client, (uint8_t*)in, len);
        }
        break;

    case LWS_CALLBACK_RECEIVE:
        // WebSockets data received
        return self->wsRead(context, wsi, *client, (uint8_t*)in, len);

    default:
        break;
    }

    return 0;
}

int TcpNetServer::opcRead(libwebsocket_context *context, libwebsocket *wsi,
                          Client &client, uint8_t *in, size_t len)
{
    /*
     * Open Pixel Control packet dispatch, and protocol detection.
     *
     * Store the new packet in our protocol buffer. This should be large enough to never overflow,
     * since our OPC buffer is large enough for two packets and the OPC max packet size is much larger
     * than the network receive buffer.
     *
     * If we have no buffered data yet, we can do this without copying.
     */

    OPCBuffer *opcb;
    uint8_t *buffer;
    unsigned bufferLength;

    // Allocate the buffer we use for OPC reassembly and protocol-detect.
    if (client.opcBuffer == NULL) {
        opcb = (OPCBuffer*) malloc(sizeof * client.opcBuffer);
        if (opcb == NULL) {
            __android_log_print(ANDROID_LOG_ERROR, APP_NAME, "ERROR: Out of memory allocating OPC reassembly buffer.\n");
            lwsl_err("ERROR: Out of memory allocating OPC reassembly buffer.\n");
            return -1;
        }
        opcb->bufferLength = 0;
        client.opcBuffer = opcb;
    } else {
        opcb = client.opcBuffer;
    }

    if (len + opcb->bufferLength > sizeof opcb->buffer) {
        return -1;
    }

    if (opcb->bufferLength) {
        memcpy(opcb->bufferLength + opcb->buffer, in, len);
        buffer = opcb->buffer;
        bufferLength = opcb->bufferLength + len;
    } else {
        buffer = in;
        bufferLength = len;
    }

    if (client.state == CLIENT_STATE_PROTOCOL_DETECT) {
        /*
         * It's a new connection, and we aren't sure yet whether it's native OPC
         * or HTTP / WebSockets. We examine the first four bytes received. If it's
         * "GET ", we assume this is HTTP. (Other HTTP methods are not needed).
         * If it's anything else, we interpret the connection as native OPC and these
         * are the first four bytes of the first OPC packet.
         */

        if (bufferLength < 4) {
            // Not enough data for protocol detect yet. Save this data for later.

            if (buffer != opcb->buffer) {
                memcpy(opcb->buffer, buffer, bufferLength);
                opcb->bufferLength = bufferLength;
            }

            // Do not pass this data on to libwebsocket yet
            return 1;
        }

        if (buffer[0] == 'G' && buffer[1] == 'E' && buffer[2] == 'T' && buffer[3] == ' ') {
            // Detected HTTP. Convert this to an HTTP client, and let libwebsockets handle
            // all data received so far. We can jettison the OPC buffer at this point.

            free(client.opcBuffer);
            client.opcBuffer = 0;
            client.state = CLIENT_STATE_HTTP;

            if (libwebsocket_read(context, wsi, buffer, bufferLength) < 0) {
                return -1;
            }
            return 1;
        }

        // Not HTTP. Handle this as an OPC socket.
        client.state = CLIENT_STATE_OPEN_PIXEL_CONTROL;
        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "New Open Pixel Control connection\n");
        lwsl_notice("New Open Pixel Control connection\n");
    }

    // Process any and all complete packets from our buffer
    while (1) {

        if (bufferLength < OPC::HEADER_BYTES) {
            // Still waiting for a header
            break;
        }

        OPC::Message *msg = (OPC::Message*) buffer;
        unsigned msgLength = OPC::HEADER_BYTES + msg->length();

        if (bufferLength < msgLength) {
            // Waiting for more data
            break;
        }

        // Complete packet.
        mOpcCallback(*msg, mUserContext);

        buffer += msgLength;
        bufferLength -= msgLength;
    }

    // If we have any residual data, save it for later.
    if (bufferLength && buffer != opcb->buffer) {
        memmove(opcb->buffer, buffer, bufferLength);
    }
    opcb->bufferLength = bufferLength;

    // Don't pass data on to libwebsockets
    return 1;
}

bool TcpNetServer::httpPathEqual(const char *a, const char *b)
{
    // HTTP path comparison. Stop at '?' or '#', to ignore query/fragment portions.
    for (;;) {
        char ca = *a;
        char cb = *b;
        if (ca == '?' || cb == '?' || ca == '#' || cb == '#')
            return true;
        if (ca != cb)
            return false;
        if (ca == '\0')
            return true;
        a++;
        b++;
    }
}

int TcpNetServer::httpBegin(libwebsocket_context *context, libwebsocket *wsi,
                            Client &client, const char *path)
{
    /*
     * We have a new plain HTTP request. Match it against our document list, and send
     * back headers for the response.
     *
     * Note: To keep the size of fcserver down, we compress our HTTP documents.
     *       We don't bother supporting decompressing them. Instead, we always send
     *       them back with deflate content-encoding.
     */

    HTTPDocument *doc = httpDocumentList;

    // Look for this path in the document list. If it isn't found, we'll serve the 404 doc.
    while (doc->path && !httpPathEqual(doc->path, path))
        doc++;

    if (!doc->path) {
        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "HTTP document not found, \"%s\"\n", path);
        lwsl_notice("HTTP document not found, \"%s\"\n", path);
    }

    char buffer[1024];
    int size = snprintf(buffer, sizeof buffer,
                        "HTTP/1.1 %d %s\r\n"
                        "Server: %s\r\n"
                        "Content-Type: %s\r\n"
                        "Content-Length: %u\r\n"
                        "Content-Encoding: deflate\r\n"
                        "Connection: close\r\n"
                        "\r\n",
                        doc->path ? 200 : 404,
                        doc->path ? "OK" : "Not Found",
                        kFCServerVersion,
                        doc->contentType,
                        doc->contentLength
                       );

    if (libwebsocket_write(wsi, (unsigned char*) buffer, size, LWS_WRITE_HTTP) < 0) {
        return -1;
    }

    // Write the body asynchronously
    client.httpBody = doc->body;
    client.httpLength = doc->contentLength;
    libwebsocket_callback_on_writable(context, wsi);

    return 0;
}

int TcpNetServer::httpWrite(libwebsocket_context *context, libwebsocket *wsi, Client &client)
{
    if (!client.httpBody) {
        return -1;
    }

    do {
        if (client.httpLength <= 0) {
            // End of document
            return -1;
        }
        int blockSize = std::min<int>(client.httpLength, 4096);
        int m = libwebsocket_write(wsi, (unsigned char *) client.httpBody, blockSize, LWS_WRITE_HTTP);
        if (m < 0) {
            // Write error, close connection
            return -1;
        }
        client.httpBody += m;
        client.httpLength -= m;
    } while (!lws_send_pipe_choked(wsi));

    libwebsocket_callback_on_writable(context, wsi);
    return 0;
}

int TcpNetServer::wsRead(libwebsocket_context *context, libwebsocket *wsi, Client &client, uint8_t *in, size_t len)
{
    // If this frame is binary, it's an OPC message. Does it parse?
    if (lws_frame_is_binary(wsi)) {
        OPC::Message *msg = (OPC::Message*) in;

        if (len < OPC::HEADER_BYTES) {
            __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "NOTICE: Received binary WebSockets packet, but it's too small for an OPC header.\n");
            lwsl_notice("NOTICE: Received binary WebSockets packet, but it's too small for an OPC header.\n");
            return 0;
        }

        if (msg->lenLow != 0 || msg->lenHigh != 0) {
            __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "NOTICE: Received OPC packet over WebSockets with nonzero reserved (length) fields.\n");
            lwsl_notice("NOTICE: Received OPC packet over WebSockets with nonzero reserved (length) fields.\n");
        }

        if (len > sizeof * msg) {
            __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "NOTICE: Received oversized OPC packet over WebSockets. Truncating.\n");
            lwsl_notice("NOTICE: Received oversized OPC packet over WebSockets. Truncating.\n");
            len = sizeof * msg;
        }

        msg->setLength(len - OPC::HEADER_BYTES);
        mOpcCallback(*msg, mUserContext);

        return 0;
    }

    // Text frames are JSON encoded. Does that parse?
    rapidjson::Document message;
    message.ParseInsitu<0>((char*) in);

    if (message.HasParseError()) {
        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "NOTICE: Parse error in received JSON, character\n");
        lwsl_notice("NOTICE: Parse error in received JSON, character %d: %s\n",
                    int(message.GetErrorOffset()), message.GetParseError());
        return 0;
    }

    if (!message.IsObject()) {
        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "NOTICE: Received JSON is not an object {}\n");
        lwsl_notice("NOTICE: Received JSON is not an object {}\n");
        return 0;
    }

    mJsonCallback(wsi, message, mUserContext);
    return 0;
}

int TcpNetServer::jsonReply(libwebsocket *wsi, rapidjson::Document &message)
{
    jsonBuffer_t buffer;
    jsonBufferPrepare(buffer, message);

    return jsonBufferSend(buffer, wsi);
}

void TcpNetServer::jsonBufferPrepare(jsonBuffer_t &buffer, rapidjson::Value &value)
{
    // Pre-packet padding
    rapidjson::PutN<>(buffer, 0, LWS_SEND_BUFFER_PRE_PADDING);

    // Write serialized message
    rapidjson::Writer<rapidjson::GenericStringBuffer<rapidjson::UTF8<> > > writer(buffer);
    value.Accept(writer);

    // Post-packet padding
    rapidjson::PutN<>(buffer, 0, LWS_SEND_BUFFER_POST_PADDING);
}

int TcpNetServer::jsonBufferSend(jsonBuffer_t &buffer, libwebsocket *wsi)
{
    const char *string = buffer.GetString() + LWS_SEND_BUFFER_PRE_PADDING;
    size_t len = buffer.Size() - LWS_SEND_BUFFER_PRE_PADDING - LWS_SEND_BUFFER_POST_PADDING;
    return libwebsocket_write(wsi, (unsigned char *) string, len, LWS_WRITE_TEXT);
}

void TcpNetServer::flushBroadcastList()
{
    // Send any pending broadcast packets. These are enqueued by other threads on a list
    // protected by mBroadcastMutex.

    mBroadcastMutex.lock();

    for (std::vector<jsonBuffer_t*>::iterator buf = mBroadcastList.begin(); buf != mBroadcastList.end(); ++buf) {

        for (std::set<libwebsocket*>::iterator cli = mClients.begin(); cli != mClients.end(); ++cli) {
            jsonBufferSend(**buf, *cli);
        }
        delete *buf;
    }
    mBroadcastList.clear();
    mBroadcastMutex.unlock();
}

void TcpNetServer::jsonBroadcast(rapidjson::Document &message)
{
    jsonBuffer_t *buffer = new jsonBuffer_t();
    jsonBufferPrepare(*buffer, message);

    mBroadcastMutex.lock();
    mBroadcastList.push_back(buffer);
    mBroadcastMutex.unlock();
}
