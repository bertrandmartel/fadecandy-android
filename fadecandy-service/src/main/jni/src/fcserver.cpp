/*
 * Open Pixel Control server for Fadecandy
 *
 * Original work Copyright (c) 2013 Micah Elizabeth Scott
 * Modified work Copyright (c) 2016 Bertrand Martel
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

#include "fcserver.h"
#include "usbdevice.h"
#include "fcdevice.h"
#include "version.h"
#include <ctype.h>
#include <iostream>
#include "android/log.h"
#define APP_NAME "fadecandy-server"
#include "jni.h"

jclass  FCServer::thisClass;
jclass  FCServer::someClass;
JavaVM* FCServer::jvm = 0;
JNIEnv* FCServer::env = 0;

FCServer::FCServer()
    : mPollForDevicesOnce(false),
      mTcpNetServer(cbOpcMessage, cbJsonMessage, this, mVerbose),
      mUSBHotplugThread(0)
{
}

int FCServer::init(const char* config)
{
    __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "config : %s", config);

    mConfig.Parse<0>(config);

    if (mConfig.HasParseError()) {

        __android_log_print(ANDROID_LOG_ERROR, APP_NAME, "Parse error at character %d: %s\n", int(mConfig.GetErrorOffset()), mConfig.GetParseError());
        return -1;
    }
    else {

        this->mColor = mConfig["color"];
        this->mDevices = mConfig["devices"];
        this->mVerbose = mConfig["verbose"].IsTrue();
    }

    /*
     * Minimal validation on 'devices'
     */
    if (!mDevices.IsArray()) {
        __android_log_print(ANDROID_LOG_ERROR, APP_NAME, "The required 'devices' configuration key must be an array.\n");
        mError << "The required 'devices' configuration key must be an array.\n";
    }

    return 0;
}

bool FCServer::start()
{
    const Value &host = mConfig["listen"][0u];
    const Value &port = mConfig["listen"][1];
    const char *hostStr = host.IsString() ? host.GetString() : NULL;

    return mTcpNetServer.start(hostStr, port.GetUint());
}

void FCServer::cbOpcMessage(OPC::Message &msg, void *context)
{
    /*
     * Broadcast the OPC message to all configured devices.
     */

    FCServer *self = static_cast<FCServer*>(context);
    self->mEventMutex.lock();

    for (std::vector<USBDevice*>::iterator i = self->mUSBDevices.begin(), e = self->mUSBDevices.end(); i != e; ++i) {
        USBDevice *dev = *i;
        dev->writeMessage(msg);
    }

    self->mEventMutex.unlock();
}

void FCServer::usbDeviceArrived(int vendorId, int productId, std::string serialNumber, int fileDescriptor)
{
    /*
     * New USB device. Is this a device we recognize?
     */
    USBDevice *dev;

    if (FCDevice::probe(vendorId, productId)) {
        dev = new FCDevice(mVerbose, serialNumber, fileDescriptor);
    }
    /*
    else if (EnttecDMXDevice::probe(device)) {
        dev = new EnttecDMXDevice(device, mVerbose);

    }
    */
    else {
        return;
    }

    if (!dev->probeAfterOpening()) {
        // We were mistaken, this device isn't actually one we want.
        delete dev;
        return;
    }

    for (unsigned i = 0; i < mDevices.Size(); ++i) {

        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "device : %d\n", mDevices[i].IsObject());

        if (dev->matchConfiguration(mDevices[i])) {
            // Found a matching configuration for this device. We're keeping it!

            dev->loadConfiguration(mDevices[i]);
            dev->writeColorCorrection(mColor);
            mUSBDevices.push_back(dev);

            if (mVerbose) {
                __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "USB device attached \n");
                std::clog << "USB device " << dev->getName() << " attached.\n";
            }
            jsonConnectedDevicesChanged();
            return;
        }
    }

    if (mVerbose) {
        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "USB device has no matching configuration. Not using it. %d\n", mDevices.Size());
        std::clog << "USB device " << dev->getName() << " has no matching configuration. Not using it.\n";
    }
    delete dev;
}

void FCServer::usbDeviceLeft(int fileDescriptor)
{
    /*
     * Is this a device we recognize? If so, delete it.
     */
    for (std::vector<USBDevice*>::iterator i = mUSBDevices.begin(), e = mUSBDevices.end(); i != e; ++i) {
        USBDevice *dev = *i;
        if (dev->getFileDescriptor() == fileDescriptor) {
            usbDeviceLeft(i);
            break;
        }
    }

}

void FCServer::usbDeviceLeft(std::vector<USBDevice*>::iterator iter)
{
    USBDevice *dev = *iter;
    if (mVerbose) {
        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "USB device removed\n");
        std::clog << "USB device " << dev->getName() << " removed.\n";
    }
    mUSBDevices.erase(iter);
    delete dev;
    jsonConnectedDevicesChanged();
}

void FCServer::cbJsonMessage(libwebsocket *wsi, rapidjson::Document &message, void *context)
{
    // Received a JSON message from a WebSockets client.
    // Replies are formed by modifying the original message.

    FCServer *self = (FCServer*) context;

    const Value &vtype = message["type"];
    if (!vtype.IsString()) {
        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "NOTICE: Received JSON is missing mandatory \"type\" string\n");
        lwsl_notice("NOTICE: Received JSON is missing mandatory \"type\" string\n");
        return;
    }
    const char *type = vtype.GetString();

    // Hold the event lock while dispatching
    self->mEventMutex.lock();

    if (!strcmp(type, "list_connected_devices")) {
        self->jsonListConnectedDevices(message);
    } else if (!strcmp(type, "server_info")) {
        self->jsonServerInfo(message);
    } else if (message.HasMember("device")) {
        self->jsonDeviceMessage(message);
    } else {
        message.AddMember("error", "Unknown message type", message.GetAllocator());
    }

    self->mEventMutex.unlock();

    // Remove heavyweight members we should never reply with
    message.RemoveMember("pixels");

    // All messages get a reply, and we leave any extra parameters on the message
    // so that clients can keep track of asynchronous completions.
    self->mTcpNetServer.jsonReply(wsi, message);
}

void FCServer::close(){
    mUSBDevices.clear();
    mTcpNetServer.close();
}

void FCServer::jsonDeviceMessage(rapidjson::Document &message)
{
    /*
     * If this message has a "device" member and doesn't match any server-global
     * message types, give each matching device a chance to handle it.
     */

    const Value &device = message["device"];
    bool matched = false;

    if (device.IsObject()) {
        for (unsigned i = 0; i != mUSBDevices.size(); i++) {
            USBDevice *usbDev = mUSBDevices[i];

            if (usbDev->matchConfiguration(device)) {
                matched = true;
                usbDev->writeMessage(message);
                if (message.HasMember("error"))
                    break;
            }
        }
    }

    if (!matched) {
        message.AddMember("error", "No matching device found", message.GetAllocator());
    }
}

void FCServer::jsonListConnectedDevices(rapidjson::Document &message)
{
    message.AddMember("devices", rapidjson::kArrayType, message.GetAllocator());

    Value &list = message["devices"];

    for (unsigned i = 0; i != mUSBDevices.size(); i++) {
        list.PushBack(rapidjson::kObjectType, message.GetAllocator());
        mUSBDevices[i]->describe(list[i], message.GetAllocator());
    }
}

void FCServer::jsonServerInfo(rapidjson::Document &message)
{
    // Server version
    message.AddMember("version", kFCServerVersion, message.GetAllocator());

    // Server configuration
    message.AddMember("config", rapidjson::kObjectType, message.GetAllocator());

    message.DeepCopy(message["config"], mConfig);
}

void FCServer::jsonConnectedDevicesChanged()
{
    rapidjson::Document message;

    message.SetObject();
    message.AddMember("type", "connected_devices_changed", message.GetAllocator());

    jsonListConnectedDevices(message);

    mTcpNetServer.jsonBroadcast(message);
}
