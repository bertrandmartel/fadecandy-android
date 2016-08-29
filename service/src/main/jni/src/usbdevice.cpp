/*
 * Abstract base class for USB-attached devices.
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

#include "libwebsockets.h"   // Lazy portable way to get gettimeofday()
#include "usbdevice.h"
#include <iostream>
#include "android/log.h"
#define APP_NAME "fadecandy-server"

USBDevice::USBDevice(const char *type, bool verbose, int fileDescriptor)
    : mTypeString(type),
      mSerialString(0),
      mVerbose(verbose),
      mFD(fileDescriptor)
{
    gettimeofday(&mTimestamp, NULL);
}

USBDevice::~USBDevice()
{
    /*
    if (mHandle) {
        libusb_close(mHandle);
    }
    if (mDevice) {
        libusb_unref_device(mDevice);
    }
    */
}

bool USBDevice::probeAfterOpening()
{
    // By default, any device is supported by the time we get to opening it.
    return true;
}

void USBDevice::writeColorCorrection(const Value &color)
{
    // Optional. By default, ignore color correction messages.
}

bool USBDevice::matchConfiguration(const Value &config)
{
    if (!config.IsObject()) {
        return false;
    }
    
    const Value &vtype = config["type"];
    const Value &vserial = config["serial"];

    if (!vtype.IsNull() && (!vtype.IsString() || strcmp(vtype.GetString(), mTypeString))) {
        return false;
    }

    if (mSerialString && !vserial.IsNull() &&
            (!vserial.IsString() || strcmp(vserial.GetString(), mSerialString))) {
        return false;
    }

    return true;
}

const USBDevice::Value *USBDevice::findConfigMap(const Value &config)
{
    const Value &vmap = config["map"];

    if (vmap.IsArray()) {
        // The map is optional, but if it exists it needs to be an array.
        return &vmap;
    }

    if (!vmap.IsNull() && mVerbose) {
        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "Device configuration 'map' must be an array.\n");
        std::clog << "Device configuration 'map' must be an array.\n";
    }

    return 0;
}

void USBDevice::writeMessage(Document &msg)
{
    __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "USBDevice::writeMessage\n");

    const char *type = msg["type"].GetString();

    if (!strcmp(type, "device_color_correction")) {
        // Single-device color correction
        writeColorCorrection(msg["color"]);
        return;
    }

    msg.AddMember("error", "Unknown device-specific message type", msg.GetAllocator());
}

void USBDevice::describe(rapidjson::Value &object, Allocator &alloc)
{
    object.AddMember("type", mTypeString, alloc);
    if (mSerialString) {
        object.AddMember("serial", mSerialString, alloc);
    }

    /*
     * The connection timestamp lets a particular connection instance be identified
     * reliably, even if the same device connects and disconnects.
     *
     * We encode the timestamp as 64-bit millisecond count, so we don't have to worry about
     * the portability of string/float conversions. This also matches a common JS format.
     */

    uint64_t timestamp = (uint64_t)mTimestamp.tv_sec * 1000 + mTimestamp.tv_usec / 1000;
    object.AddMember("timestamp", timestamp, alloc);
}
