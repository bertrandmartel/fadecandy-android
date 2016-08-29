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

#include "fcdevice.h"
#include "rapidjson/stringbuffer.h"
#include "rapidjson/writer.h"
#include "opc.h"
#include <math.h>
#include <iostream>
#include <sstream>
#include <algorithm>
#include <stdio.h>
#include "android/log.h"
#include "tinythread.h"
#define APP_NAME "fadecandy-server"
#include "jni.h"
#include "fcserver.h"

FCDevice::FCDevice(bool verbose, std::string serialNumber, int fileDescriptor)
  : USBDevice("fadecandy", verbose, fileDescriptor),
    mConfigMap(0),
    mSerialNumber(serialNumber)
{
  mSerialBuffer[0] = '\0';
  mSerialString = mSerialBuffer;

  memset(&mFirmwareConfig, 0, sizeof mFirmwareConfig);
  mFirmwareConfig.control = TYPE_CONFIG;

  // Framebuffer headers
  memset(mFramebuffer, 0, sizeof mFramebuffer);
  for (unsigned i = 0; i < FRAMEBUFFER_PACKETS; ++i) {
    mFramebuffer[i].control = TYPE_FRAMEBUFFER | i;
  }
  mFramebuffer[FRAMEBUFFER_PACKETS - 1].control |= FINAL;

  // Color LUT headers
  memset(mColorLUT, 0, sizeof mColorLUT);
  for (unsigned i = 0; i < LUT_PACKETS; ++i) {
    mColorLUT[i].control = TYPE_LUT | i;
  }
  mColorLUT[LUT_PACKETS - 1].control |= FINAL;


  snprintf(mVersionString, sizeof mVersionString, "%x.%02x", 0, 0);
  
}

FCDevice::~FCDevice()
{
}

bool FCDevice::probe(int vendorId, int productId)
{
  return vendorId == 0x1d50 && productId == 0x607a;
}

int FCDevice::open()
{
  return 0;
}

void FCDevice::loadConfiguration(const Value &config)
{
  mConfigMap = findConfigMap(config);

  // Initial firmware configuration from our device options
  writeFirmwareConfiguration(config);
}

void FCDevice::writeFirmwareConfiguration(const Value &config)
{
  /*
   * Send a device configuration settings packet, using values based on a JSON
   * configuration.
   */

  if (!config.IsObject()) {
    std::clog << "Firmware configuration is not a JSON object\n";
    return;
  }

  const Value &led = config["led"];
  const Value &dither = config["dither"];
  const Value &interpolate = config["interpolate"];

  if (!(led.IsTrue() || led.IsFalse() || led.IsNull())) {
    std::clog << "LED configuration must be true (always on), false (always off), or null (default).\n";
  }

  mFirmwareConfig.data[0] =
    (led.IsNull() ? 0 : CFLAG_NO_ACTIVITY_LED)             |
    (led.IsTrue() ? CFLAG_LED_CONTROL : 0)                 |
    (dither.IsFalse() ? CFLAG_NO_DITHERING : 0)            |
    (interpolate.IsFalse() ? CFLAG_NO_INTERPOLATION : 0)   ;

  writeFirmwareConfiguration();
}

bool FCDevice::submitTransfer(FCDevice *device, void *buffer, int length)
{
  uint8_t *data = (uint8_t*) buffer;

  __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "in Transfer\n");

  if (FCServer::jvm != 0) {

    int getEnvStat = FCServer::jvm->GetEnv((void **)&FCServer::env, JNI_VERSION_1_6);

    if (getEnvStat == JNI_EDETACHED) {

      __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "jvm not attached\n");

      if (FCServer::jvm->AttachCurrentThread(&FCServer::env, NULL) != 0) {

        __android_log_print(ANDROID_LOG_ERROR, APP_NAME, "failed to attach\n");
      }
    } else if (getEnvStat == JNI_EVERSION) {

      __android_log_print(ANDROID_LOG_ERROR, APP_NAME, "jni: version not supported\n");
    }
  }
  else {
    __android_log_print(ANDROID_LOG_ERROR, APP_NAME, "jvm not defined\n");
  }

  jint fd = device->getFileDescriptor();
  jbyteArray data_ba = FCServer::env->NewByteArray(length);
  FCServer::env->SetByteArrayRegion( data_ba, 0, length, (const jbyte*)data );

  jmethodID methodId = FCServer::env->GetMethodID(FCServer::someClass, "bulkTransfer", "(II[B)I");
  jint result = FCServer::env->CallIntMethod(FCServer::thisClass, methodId, fd, 2000, data_ba);

  __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "bulk_transfer : %d\n", result);

  FCServer::env->DeleteLocalRef(data_ba);

  return true;
}

void FCDevice::flush()
{
}

void FCDevice::writeColorCorrection(const Value &color)
{
  /*
   * Populate the color correction table based on a JSON configuration object,
   * and send the new color LUT out over USB.
   *
   * 'color' may be 'null' to load an identity-mapped LUT, or it may be
   * a dictionary of options including 'gamma' and 'whitepoint'.
   *
   * This calculates a compound curve with a linear section and a nonlinear
   * section. The linear section, near zero, avoids creating very low output
   * values that will cause distracting flicker when dithered. This isn't a problem
   * when the LEDs are viewed indirectly such that the flicker is below the threshold
   * of perception, but in cases where the flicker is a problem this linear section can
   * eliminate it entierly at the cost of some dynamic range.
   *
   * By default, the linear section is disabled (linearCutoff is zero). To enable the
   * linear section, set linearCutoff to some nonzero value. A good starting point is
   * 1/256.0, correspnding to the lowest 8-bit PWM level.
   */

  // Default color LUT parameters
  double gamma = 1.0;                         // Power for nonlinear portion of curve
  double whitepoint[3] = {1.0, 1.0, 1.0};     // White-point RGB value (also, global brightness)
  double linearSlope = 1.0;                   // Slope (output / input) of linear section of the curve, near zero
  double linearCutoff = 0.0;                  // Y (output) coordinate of intersection of linear and nonlinear curves

  /*
   * Parse the JSON object
   */

  if (color.IsObject()) {
    const Value &vGamma = color["gamma"];
    const Value &vWhitepoint = color["whitepoint"];
    const Value &vLinearSlope = color["linearSlope"];
    const Value &vLinearCutoff = color["linearCutoff"];

    if (vGamma.IsNumber()) {
      gamma = vGamma.GetDouble();
    } else if (!vGamma.IsNull() && mVerbose) {
      std::clog << "Gamma value must be a number.\n";
    }

    if (vLinearSlope.IsNumber()) {
      linearSlope = vLinearSlope.GetDouble();
    } else if (!vLinearSlope.IsNull() && mVerbose) {
      std::clog << "Linear slope value must be a number.\n";
    }

    if (vLinearCutoff.IsNumber()) {
      linearCutoff = vLinearCutoff.GetDouble();
    } else if (!vLinearCutoff.IsNull() && mVerbose) {
      std::clog << "Linear slope value must be a number.\n";
    }

    if (vWhitepoint.IsArray() &&
        vWhitepoint.Size() == 3 &&
        vWhitepoint[0u].IsNumber() &&
        vWhitepoint[1].IsNumber() &&
        vWhitepoint[2].IsNumber()) {
      whitepoint[0] = vWhitepoint[0u].GetDouble();
      whitepoint[1] = vWhitepoint[1].GetDouble();
      whitepoint[2] = vWhitepoint[2].GetDouble();
    } else if (!vWhitepoint.IsNull() && mVerbose) {
      std::clog << "Whitepoint value must be a list of 3 numbers.\n";
    }

  } else if (!color.IsNull() && mVerbose) {
    std::clog << "Color correction value must be a JSON dictionary object.\n";
  }

  /*
   * Calculate the color LUT, stowing the result in an array of USB packets.
   */

  Packet *packet = mColorLUT;
  const unsigned firstByteOffset = 1;  // Skip padding byte
  unsigned byteOffset = firstByteOffset;

  for (unsigned channel = 0; channel < 3; channel++) {
    for (unsigned entry = 0; entry < LUT_ENTRIES; entry++) {
      double output;

      /*
       * Normalized input value corresponding to this LUT entry.
       * Ranges from 0 to slightly higher than 1. (The last LUT entry
       * can't quite be reached.)
       */
      double input = (entry << 8) / 65535.0;

      // Scale by whitepoint before anything else
      input *= whitepoint[channel];

      // Is this entry part of the linear section still?
      if (input * linearSlope <= linearCutoff) {

        // Output value is below linearCutoff. We're still in the linear portion of the curve
        output = input * linearSlope;

      } else {

        // Nonlinear portion of the curve. This starts right where the linear portion leaves
        // off. We need to avoid any discontinuity.

        double nonlinearInput = input - (linearSlope * linearCutoff);
        double scale = 1.0 - linearCutoff;
        output = linearCutoff + pow(nonlinearInput / scale, gamma) * scale;
      }

      // Round to the nearest integer, and clamp. Overflow-safe.
      int64_t longValue = (output * 0xFFFF) + 0.5;
      int intValue = std::max<int64_t>(0, std::min<int64_t>(0xFFFF, longValue));

      // Store LUT entry, little-endian order.
      packet->data[byteOffset++] = uint8_t(intValue);
      packet->data[byteOffset++] = uint8_t(intValue >> 8);
      if (byteOffset >= sizeof packet->data) {
        byteOffset = firstByteOffset;
        packet++;
      }
    }
  }

  // Start asynchronously sending the LUT.
  submitTransfer(this, &mColorLUT, sizeof mColorLUT);
}

void FCDevice::writeFramebuffer()
{
  submitTransfer(this, &mFramebuffer, sizeof mFramebuffer);
}

void FCDevice::writeMessage(Document &msg)
{
  /*
   * Dispatch a device-specific JSON command.
   *
   * This can be used to send frames or settings directly to one device,
   * bypassing the mapping we use for Open Pixel Control clients. This isn't
   * intended to be the fast path for regular applications, but it can be used
   * by configuration tools that need to operate regardless of the mapping setup.
   */

  __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "FCDevice::writeMessage\n");

  const char *type = msg["type"].GetString();

  if (!strcmp(type, "device_options")) {
    /*
     * TODO: Eventually this should turn into the same thing as
     *       loadConfiguration() and it shouldn't be device-specific,
     *       but for now most of fcserver assumes the configuration is static.
     */
    writeFirmwareConfiguration(msg["options"]);
    return;
  }

  if (!strcmp(type, "device_pixels")) {
    // Write raw pixels, without any mapping
    writeDevicePixels(msg);
    return;
  }

  // Chain to default handler
  USBDevice::writeMessage(msg);
}

void FCDevice::writeDevicePixels(Document &msg)
{
  /*
   * Write pixels without mapping, from a JSON integer
   * array in msg["pixels"]. The pixel array is removed from
   * the reply to save network bandwidth.
   *
   * Pixel values are clamped to [0, 255], for convenience.
   */

  __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "writeDevicePixels\n");

  const Value &pixels = msg["pixels"];
  if (!pixels.IsArray()) {
    msg.AddMember("error", "Pixel array is missing", msg.GetAllocator());
  } else {

    // Truncate to the framebuffer size, and only deal in whole pixels.
    int numPixels = pixels.Size() / 3;
    if ((unsigned)numPixels > NUM_PIXELS)
      numPixels = NUM_PIXELS;

    for (int i = 0; i < numPixels; i++) {
      uint8_t *out = fbPixel(i);

      const Value &r = pixels[i * 3 + 0];
      const Value &g = pixels[i * 3 + 1];
      const Value &b = pixels[i * 3 + 2];

      out[0] = std::max(0, std::min(255, r.IsInt() ? r.GetInt() : 0));
      out[1] = std::max(0, std::min(255, g.IsInt() ? g.GetInt() : 0));
      out[2] = std::max(0, std::min(255, b.IsInt() ? b.GetInt() : 0));
    }

    writeFramebuffer();
  }
}

void FCDevice::writeMessage(const OPC::Message &msg)
{
  /*
   * Dispatch an incoming OPC command
   */

  __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "FCDevice::writeMessage\n");

  switch (msg.command) {

  case OPC::SetPixelColors:
    opcSetPixelColors(msg);
    __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "before FCDevice::writeFramebuffer\n");
    writeFramebuffer();
    return;

  case OPC::SystemExclusive:
    opcSysEx(msg);
    return;
  }

  if (mVerbose) {
    std::clog << "Unsupported OPC command: " << unsigned(msg.command) << "\n";
  }
}

void FCDevice::opcSysEx(const OPC::Message &msg)
{
  if (msg.length() < 4) {
    if (mVerbose) {
      std::clog << "SysEx message too short!\n";
    }
    return;
  }

  unsigned id = (unsigned(msg.data[0]) << 24) |
                (unsigned(msg.data[1]) << 16) |
                (unsigned(msg.data[2]) << 8)  |
                unsigned(msg.data[3])        ;

  switch (id) {

  case OPC::FCSetGlobalColorCorrection:
    return opcSetGlobalColorCorrection(msg);

  case OPC::FCSetFirmwareConfiguration:
    return opcSetFirmwareConfiguration(msg);

  }

  // Quietly ignore unhandled SysEx messages.
}

void FCDevice::opcSetPixelColors(const OPC::Message &msg)
{
  __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "FCDevice::opcSetPixelColors\n");
  /*
   * Parse through our device's mapping, and store any relevant portions of 'msg'
   * in the framebuffer.
   */

  if (!mConfigMap) {
    // No mapping defined yet. This device is inactive.
    return;
  }

  const Value &map = *mConfigMap;
  for (unsigned i = 0, e = map.Size(); i != e; i++) {
    opcMapPixelColors(msg, map[i]);
  }
}

void FCDevice::opcMapPixelColors(const OPC::Message &msg, const Value &inst)
{
  /*
   * Parse one JSON mapping instruction, and copy any relevant parts of 'msg'
   * into our framebuffer. This looks for any mapping instructions that we
   * recognize:
   *
   *   [ OPC Channel, First OPC Pixel, First output pixel, Pixel count ]
   *   [ OPC Channel, First OPC Pixel, First output pixel, Color channels ]
   */

  unsigned msgPixelCount = msg.length() / 3;

  if (inst.IsArray() && inst.Size() == 4) {
    // Map a range from an OPC channel to our framebuffer

    const Value &vChannel = inst[0u];
    const Value &vFirstOPC = inst[1];
    const Value &vFirstOut = inst[2];
    const Value &vCount = inst[3];

    if (vChannel.IsUint() && vFirstOPC.IsUint() && vFirstOut.IsUint() && vCount.IsUint()) {
      unsigned channel = vChannel.GetUint();
      unsigned firstOPC = vFirstOPC.GetUint();
      unsigned firstOut = vFirstOut.GetUint();
      unsigned count = vCount.GetUint();

      if (channel != msg.channel) {
        return;
      }

      // Clamping, overflow-safe
      firstOPC = std::min<unsigned>(firstOPC, msgPixelCount);
      firstOut = std::min<unsigned>(firstOut, unsigned(NUM_PIXELS));
      count = std::min<unsigned>(count, msgPixelCount - firstOPC);
      count = std::min<unsigned>(count, NUM_PIXELS - firstOut);

      // Copy pixels
      const uint8_t *inPtr = msg.data + (firstOPC * 3);
      unsigned outIndex = firstOut;

      while (count--) {
        uint8_t *outPtr = fbPixel(outIndex++);
        outPtr[0] = inPtr[0];
        outPtr[1] = inPtr[1];
        outPtr[2] = inPtr[2];
        inPtr += 3;
      }

      return;
    }
  }

  if (inst.IsArray() && inst.Size() == 5) {
    // Map a range from an OPC channel to our framebuffer, with color channel swizzling

    const Value &vChannel = inst[0u];
    const Value &vFirstOPC = inst[1];
    const Value &vFirstOut = inst[2];
    const Value &vCount = inst[3];
    const Value &vColorChannels = inst[4];

    if (vChannel.IsUint() && vFirstOPC.IsUint() && vFirstOut.IsUint() && vCount.IsUint()
        && vColorChannels.IsString() && vColorChannels.GetStringLength() == 3) {

      unsigned channel = vChannel.GetUint();
      unsigned firstOPC = vFirstOPC.GetUint();
      unsigned firstOut = vFirstOut.GetUint();
      unsigned count = vCount.GetUint();
      const char *colorChannels = vColorChannels.GetString();

      if (channel != msg.channel) {
        return;
      }

      // Clamping, overflow-safe
      firstOPC = std::min<unsigned>(firstOPC, msgPixelCount);
      firstOut = std::min<unsigned>(firstOut, unsigned(NUM_PIXELS));
      count = std::min<unsigned>(count, msgPixelCount - firstOPC);
      count = std::min<unsigned>(count, NUM_PIXELS - firstOut);

      // Copy pixels
      const uint8_t *inPtr = msg.data + (firstOPC * 3);
      unsigned outIndex = firstOut;
      bool success = true;

      while (count--) {
        uint8_t *outPtr = fbPixel(outIndex++);

        for (int channel = 0; channel < 3; channel++) {
          if (!OPC::pickColorChannel(outPtr[channel], colorChannels[channel], inPtr)) {
            success = false;
            break;
          }
        }

        inPtr += 3;
      }

      if (success) {
        return;
      }
    }
  }

  // Still haven't found a match?
  if (mVerbose) {
    rapidjson::GenericStringBuffer<rapidjson::UTF8<> > buffer;
    rapidjson::Writer<rapidjson::GenericStringBuffer<rapidjson::UTF8<> > > writer(buffer);
    inst.Accept(writer);
    std::clog << "Unsupported JSON mapping instruction: " << buffer.GetString() << "\n";
  }
}

void FCDevice::opcSetGlobalColorCorrection(const OPC::Message &msg)
{
  /*
   * Parse the message as JSON text, and if successful, write new
   * color correction data to the device.
   */

  // Mutable NUL-terminated copy of the message string
  std::string text((char*)msg.data + 4, msg.length() - 4);

  // Parse it in-place
  rapidjson::Document doc;
  doc.ParseInsitu<0>(&text[0]);

  if (doc.HasParseError()) {
    if (mVerbose) {
      std::clog << "Parse error in color correction JSON at character "
                << doc.GetErrorOffset() << ": " << doc.GetParseError() << "\n";
    }
    return;
  }

  /*
   * Successfully parsed the JSON. From here, it's handled identically to
   * objects that come through the config file.
   */
  writeColorCorrection(doc);
}

void FCDevice::opcSetFirmwareConfiguration(const OPC::Message &msg)
{
  /*
   * Raw firmware configuration packet
   */

  memcpy(mFirmwareConfig.data, msg.data + 4, std::min<size_t>(sizeof mFirmwareConfig.data, msg.length() - 4));
  writeFirmwareConfiguration();
}

void FCDevice::writeFirmwareConfiguration()
{
  // Write mFirmwareConfig to the device
  submitTransfer(this, &mFirmwareConfig, sizeof mFirmwareConfig);
}

std::string FCDevice::getName()
{
  std::ostringstream s;
  s << "Fadecandy";
  if (mSerialString[0]) {
    s << " (Serial# " << mSerialString << ", Version " << mVersionString << ")";
  }
  return s.str();
}

void FCDevice::describe(rapidjson::Value &object, Allocator &alloc)
{
  USBDevice::describe(object, alloc);
  object.AddMember("version", mVersionString, alloc);
  object.AddMember("bcd_version", mSerialNumber.c_str(), alloc);
}
