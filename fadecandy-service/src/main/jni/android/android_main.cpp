/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Bertrand Martel
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

#include "rapidjson/document.h"
#include "rapidjson/reader.h"
#include "rapidjson/filestream.h"
#include "fcserver.h"
#include "version.h"
#include <cstdio>
#include <iostream>
#include <jni.h>
#include "android/log.h"

#define APP_NAME "fadecandy-server"

static FCServer server;

int launch_server(std::string configuration)
{
    server.init(configuration.c_str());

    if (server.hasError()) {
        __android_log_print(ANDROID_LOG_ERROR, APP_NAME, "Configuration errors:\n%s", server.errorText());
        return -1;
    }
    if (!server.start()) {
        __android_log_print(ANDROID_LOG_ERROR, APP_NAME, "Start error");
        return -1;
    }

    return 0;
}

extern "C" {

    JNIEXPORT jint Java_fr_bmartel_android_fadecandy_service_FadecandyService_startFcServer(JNIEnv* env, jclass cls, jstring config)
    {
        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "startServer()");

        FCServer::thisClass = (jclass)env->NewGlobalRef(cls);

        const jsize len = env->GetStringUTFLength(config);
        const char* configChars = env->GetStringUTFChars(config, 0);

        std::string configuration(configChars, len);

        env->ReleaseStringUTFChars(config, configChars);

        return launch_server(configuration);
    }

}


extern "C" {

    JNIEXPORT jint Java_fr_bmartel_android_fadecandy_service_FadecandyService_stopFcServer(JNIEnv* env, jclass cls)
    {
        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "stopServer()");

        server.close();

        return 0;
    }

}

extern "C" {

    JNIEXPORT void Java_fr_bmartel_android_fadecandy_service_FadecandyService_usbDeviceArrived(
        JNIEnv* env,
        jclass cls,
        jint vendorId,
        jint productId,
        jstring serialNumber,
        jint fileDescriptor)
    {
        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "usbDeviceArrived()");

        FCServer::thisClass = (jclass)env->NewGlobalRef(cls);

        const jsize len = env->GetStringUTFLength(serialNumber);
        const char* serialChars = env->GetStringUTFChars(serialNumber, 0);

        std::string serial(serialChars, len);

        env->ReleaseStringUTFChars(serialNumber, serialChars);

        server.usbDeviceArrived(vendorId, productId, serial, fileDescriptor);
    }

}

extern "C" {

    JNIEXPORT void Java_fr_bmartel_android_fadecandy_service_FadecandyService_usbDeviceLeft(
        JNIEnv* env,
        jclass cls,
        jint fileDescriptor)
    {
        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "usbDeviceLeft()");

        server.usbDeviceLeft(fileDescriptor);
    }

}

extern "C" {

    JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* aReserved)
    {
        FCServer::jvm = vm;

        JNIEnv *jni_env;
        vm->GetEnv((void **)&jni_env, JNI_VERSION_1_6);

        jclass someClass = jni_env->FindClass("fr/bmartel/android/fadecandy/service/FadecandyService");

        if (someClass == NULL) {
            return -1;
        }
        FCServer::someClass = (jclass)jni_env->NewGlobalRef(someClass);

        return JNI_VERSION_1_6;
    }
}