/*
 * Open Pixel Control protocol definitions
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
#include <stdint.h>

namespace OPC {

    enum Command {
        SetPixelColors = 0x00,
        SystemExclusive = 0xFF,
    };

    // SysEx system and command IDs
    enum SysEx {
        FCSetGlobalColorCorrection = 0x00010001,
        FCSetFirmwareConfiguration = 0x00010002
    };

    struct Message
    {
        uint8_t channel;
        uint8_t command;
        uint8_t lenHigh;
        uint8_t lenLow;
        uint8_t data[0xFFFF];

        unsigned length() const {
            return lenLow | (unsigned(lenHigh) << 8);
        }

        void setLength(unsigned l) {
            lenLow = (uint8_t) l;
            lenHigh = (uint8_t) (l >> 8);
        }
    };

    static const unsigned HEADER_BYTES = 4;

    typedef void (*callback_t)(Message &msg, void *context);

    // Common idiom for choosing color channels based on a character string
    inline bool pickColorChannel(uint8_t &output, char selector, const uint8_t *rgb)
    {
        switch (selector) {

            case 'r':
            case 'R':
                output = rgb[0];
                return true;

            case 'g':
            case 'G':
                output = rgb[1];
                return true;

            case 'b':
            case 'B':
                output = rgb[2];
                return true;

            case 'l':
            case 'L':
                output = (unsigned(rgb[0]) + unsigned(rgb[1]) + unsigned(rgb[2])) / 3;
                return true;

            default:
                return false;
        }
    }

}
