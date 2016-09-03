LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := fadecandy-server

GLOBAL_PATH := ../
FADECANDY_SRC_PATH := ../src
LIBWEBSOCKETS_PATH := ../libws/libwebsockets/lib

LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(FADECANDY_SRC_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(GLOBAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(LIBWEBSOCKETS_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../libwebsockets

#VERSION := $(shell git describe --match "fcserver-*")
VERSION := fcserver-android-1.0

LOCAL_SRC_FILES := $(LOCAL_PATH)/android_main.cpp \
	$(LOCAL_PATH)/$(FADECANDY_SRC_PATH)/tcpnetserver.cpp \
	$(LOCAL_PATH)/$(FADECANDY_SRC_PATH)/usbdevice.cpp \
	$(LOCAL_PATH)/$(FADECANDY_SRC_PATH)/fcdevice.cpp \
	$(LOCAL_PATH)/$(FADECANDY_SRC_PATH)/fcserver.cpp \
	$(LOCAL_PATH)/$(FADECANDY_SRC_PATH)/version.cpp \
	$(LOCAL_PATH)/$(FADECANDY_SRC_PATH)/tinythread.cpp \
	$(LOCAL_PATH)/$(FADECANDY_SRC_PATH)/httpdocs.cpp \

LOCAL_CPP_FEATURES += exceptions

LOCAL_CFLAGS += -Wall -DLIBUSB_DESCRIBE="" -O3 -fno-builtin-printf -fno-builtin-fprintf -DFCSERVER_VERSION=$(VERSION)

LOCAL_SHARED_LIBRARIES := libwebsockets

LOCAL_LDLIBS := -lm -llog

include $(BUILD_SHARED_LIBRARY)
