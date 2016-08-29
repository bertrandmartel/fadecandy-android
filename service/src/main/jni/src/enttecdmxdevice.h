/*
 * Fadecandy driver for the Enttec DMX USB Pro.
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

#pragma once
#include "usbdevice.h"
#include "opc.h"
#include <set>


class EnttecDMXDevice : public USBDevice
{
public:
    EnttecDMXDevice(libusb_device *device, bool verbose);
    virtual ~EnttecDMXDevice();

    static bool probe(libusb_device *device);

    virtual int open();
    virtual bool probeAfterOpening();
    virtual void loadConfiguration(const Value &config);
    virtual void writeMessage(const OPC::Message &msg);
    virtual std::string getName();
    virtual void flush();

    void writeDMXPacket();
    void setChannel(unsigned n, uint8_t value);

private:
    static const unsigned OUT_ENDPOINT = 2;
    static const unsigned START_OF_MESSAGE = 0x7e;
    static const unsigned END_OF_MESSAGE = 0xe7;
    static const unsigned SEND_DMX_PACKET = 0x06;
    static const unsigned START_CODE = 0x00;

    struct Packet {
        uint8_t start;
        uint8_t label;
        uint16_t length;
        uint8_t data[514];
    };

    struct Transfer {
        Transfer(EnttecDMXDevice *device, void *buffer, int length);
        ~Transfer();
        libusb_transfer *transfer;
        bool finished;
    };

    char mSerialBuffer[256];
    bool mFoundEnttecStrings;
    const Value *mConfigMap;
    Packet mChannelBuffer;
    std::set<Transfer*> mPending;

    void submitTransfer(Transfer *fct);
    static LIBUSB_CALL void completeTransfer(struct libusb_transfer *transfer);

    void opcSetPixelColors(const OPC::Message &msg);
    void opcMapPixelColors(const OPC::Message &msg, const Value &inst);
};
