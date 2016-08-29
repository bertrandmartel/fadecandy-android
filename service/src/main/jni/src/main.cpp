/*
 * Open Pixel Control server for Fadecandy
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

#include "rapidjson/document.h"
#include "rapidjson/reader.h"
#include "rapidjson/filestream.h"
#include "fcserver.h"
#include "version.h"
#include <cstdio>
#include <iostream>

const char *kDefaultConfig =
    "    {\n"
    "        \"listen\": [\"127.0.0.1\", 7890],\n"
    "        \"verbose\": true,\n"
    "    \n"
    "        \"color\": {\n"
    "            \"gamma\": 2.5,\n"
    "            \"whitepoint\": [1.0, 1.0, 1.0]\n"
    "        },\n"
    "    \n"
    "        \"devices\": [\n"
    "            {\n"
    "                \"type\": \"fadecandy\",\n"
    "                \"map\": [[ 0, 0, 0, 512 ]]\n"
    "            }\n"
    "        ]\n"
    "    }\n";


int main(int argc, char **argv)
{
    rapidjson::Document config;

    libusb_context *usb;
    if (libusb_init(&usb)) {
        std::clog << "Error initializing USB library!\n";
        return 7;
    }

    if (argc == 2 && argv[1][0] != '-') {
        // Load config from file

        FILE *configFile = fopen(argv[1], "r");
        if (!configFile) {
            perror("Error opening config file");
            return 2;
        }

        rapidjson::FileStream istr(configFile);
        config.ParseStream<0>(istr);

    } else if (argc == 1) {
        // Load default configuration

        config.Parse<0>(kDefaultConfig);

    } else {
        // Unknown, show usage message.

        fprintf(stderr,
            "\n"
            "Fadecandy Open Pixel Control server\n"
            "%s\n"
            "\n"
            "usage: fcserver [<config.json>]\n"
            "\n"
            "To use multiple Fadecandy devices or to set up a custom\n"
            "mapping from OPC pixel to Fadecandy pixel, you can provide\n"
            "a JSON configuration file. By default, all detected Fadecandy\n"
            "boards map directly to OPC pixels using the following default\n"
            "configuration. For more information about the config file\n"
            "format, see the README.\n"
            "\n"
            "%s"
            "\n"
            "Copyright (c) 2013 Micah Elizabeth Scott <micah@scanlime.org>\n"
            "https://github.com/scanlime/fadecandy\n"
            "\n"
            "Portions of this software are licensed under the GNU General\n"
            "Public License. Full license information and source code are\n"
            "available at the URL above.\n"
            "\n",
            kFCServerVersion,
            kDefaultConfig);
        return 1;
    }

    if (config.HasParseError()) {
        fprintf(stderr, "Parse error at character %d: %s\n",
            int(config.GetErrorOffset()), config.GetParseError());
        return 3;
    }

    FCServer server(config);
    if (server.hasError()) {
        fprintf(stderr, "Configuration errors:\n%s", server.errorText());
        return 5;
    }
    if (!server.start(usb)) {
        return 9;
    }

    server.mainLoop();

    // If mainLoop() exits, it was an error
    return 8;
}
