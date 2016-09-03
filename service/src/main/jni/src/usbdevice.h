/*
 * Abstract base class for USB-attached devices.
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

#pragma once

#include "rapidjson/document.h"
#include "opc.h"
#include <string>

/*
 * We find it important to know whether the libusbx backend ends up copying or mapping
 * our transaction data when it's submitted. On Linux, the kernel must already do a copy
 * to get our userspace data into kernel space. On Windows and Mac OS, the user buffer
 * is mapped. This matters because any changes to the buffer while a transfer is queued
 * will cause the transfer to change. This causes tearing for us.
 *
 * We can avoid this by copying the buffer ourselves, but we'd prefer to avoid the CPU
 * overhead of copying the buffer twice, so we only do this on platforms where the kernel
 * isn't already copying it.
 */
#if defined(OS_LINUX) || defined(__ANDROID__)
// No need to copy the buffer
#elif OS_WINDOWS
#define NEED_COPY_USB_TRANSFER_BUFFER 1
#elif OS_DARWIN
#define NEED_COPY_USB_TRANSFER_BUFFER 1
#else
#error Dont know whether we need to copy the USB transfer buffer
#endif


class USBDevice
{
public:
    typedef rapidjson::Value Value;
    typedef rapidjson::Document Document;
    typedef rapidjson::MemoryPoolAllocator<> Allocator;

    USBDevice(const char *type, bool verbose, int fileDescriptor);
    virtual ~USBDevice();

    // Must be opened before any other methods are called.
    virtual int open() = 0;

    // Some drivers can't determine whether this is a supported device prior to open()
    virtual bool probeAfterOpening();

    // Check a configuration. Does it describe this device?
    virtual bool matchConfiguration(const Value &config);

    // Load a matching configuration
    virtual void loadConfiguration(const Value &config) = 0;

    // Handle an incoming OPC message
    virtual void writeMessage(const OPC::Message &msg) = 0;

    // Handle a device-specific JSON message
    virtual void writeMessage(Document &msg);

    // Write color LUT from parsed JSON
    virtual void writeColorCorrection(const Value &color);

    // Deal with any I/O that results from completed transfers, outside the context of a completion callback
    virtual void flush() = 0;

    // Describe this device by adding keys to a JSON object
    virtual void describe(Value &object, Allocator &alloc);

    virtual std::string getName() = 0;

    //libusb_device *getDevice() { return mDevice; };
    const char *getSerial() { return mSerialString; }
    const char *getTypeString() { return mTypeString; }

    const int getFileDescriptor() { return mFD;}

protected:
    //libusb_device *mDevice;
    //libusb_device_handle *mHandle;
    struct timeval mTimestamp;
    const char *mTypeString;
    const char *mSerialString;
    bool mVerbose;
    const int mFD;
    // Utilities
    const Value *findConfigMap(const Value &config);
};
