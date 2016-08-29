/*
 * Fadecandy device interface
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
#include "jni.h"

class FCDevice : public USBDevice
{
public:
    FCDevice(bool verbose, std::string mSerialNumber, int fileDescriptor);
    virtual ~FCDevice();

    static bool probe(int vendorId, int productId);

    virtual int open();
    virtual void loadConfiguration(const Value &config);
    virtual void writeMessage(const OPC::Message &msg);
    virtual void writeMessage(Document &msg);
    virtual void writeColorCorrection(const Value &color);
    virtual std::string getName();
    virtual void flush();
    virtual void describe(rapidjson::Value &object, Allocator &alloc);

    static const unsigned NUM_PIXELS = 512;

    // Send current buffer contents
    void writeFramebuffer();

    // Framebuffer accessor
    uint8_t *fbPixel(unsigned num) {
        return &mFramebuffer[num / PIXELS_PER_PACKET].data[3 * (num % PIXELS_PER_PACKET)];
    }

private:

    static const unsigned PIXELS_PER_PACKET = 21;
    static const unsigned LUT_ENTRIES_PER_PACKET = 31;
    static const unsigned FRAMEBUFFER_PACKETS = 25;
    static const unsigned LUT_PACKETS = 25;
    static const unsigned LUT_ENTRIES = 257;
    static const unsigned OUT_ENDPOINT = 1;
    static const unsigned MAX_FRAMES_PENDING = 2;

    static const uint8_t TYPE_FRAMEBUFFER = 0x00;
    static const uint8_t TYPE_LUT = 0x40;
    static const uint8_t TYPE_CONFIG = 0x80;
    static const uint8_t FINAL = 0x20;

    static const uint8_t CFLAG_NO_DITHERING     = (1 << 0);
    static const uint8_t CFLAG_NO_INTERPOLATION = (1 << 1);
    static const uint8_t CFLAG_NO_ACTIVITY_LED  = (1 << 2);
    static const uint8_t CFLAG_LED_CONTROL      = (1 << 3);

    struct Packet {
        uint8_t control;
        uint8_t data[63];
    };

    enum PacketType {
        OTHER = 0,
        FRAME,
    };

    const Value *mConfigMap;

    char mSerialBuffer[256];
    char mVersionString[10];

    std::string mSerialNumber;

    Packet mFramebuffer[FRAMEBUFFER_PACKETS];
    Packet mColorLUT[LUT_PACKETS];
    Packet mFirmwareConfig;

    bool submitTransfer(FCDevice *device, void *buffer, int length);
    void writeFirmwareConfiguration();
    void writeFirmwareConfiguration(const Value &json);
    void writeDevicePixels(Document &msg);

    void opcSetPixelColors(const OPC::Message &msg);
    void opcSysEx(const OPC::Message &msg);
    void opcSetGlobalColorCorrection(const OPC::Message &msg);
    void opcSetFirmwareConfiguration(const OPC::Message &msg);
    void opcMapPixelColors(const OPC::Message &msg, const Value &inst);
};
