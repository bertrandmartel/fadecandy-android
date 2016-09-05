# example Android Native Library makefile
# contributed by Gregory Junker <ggjunker@gmail.com>

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := libwebsockets
LOCAL_CFLAGS    := -DLWS_LIBRARY_VERSION= -DLWS_BUILD_HASH=  -DLWS_NO_CLIENT -DLWS_NO_WSAPOLL -DLWS_NO_DAEMONIZE
LWS_LIB_PATH	:= libwebsockets/lib

LOCAL_C_INCLUDES:= $(LOCAL_PATH)/libwebsockets/lib
LOCAL_C_INCLUDES+= $(LOCAL_PATH)/libwebsockets
LOCAL_C_INCLUDES+= $(LOCAL_PATH)

LOCAL_SRC_FILES := \
	$(LWS_LIB_PATH)/handshake.c \
	$(LWS_LIB_PATH)/libwebsockets.c \
	$(LWS_LIB_PATH)/parsers.c \
	$(LWS_LIB_PATH)/server-handshake.c \
	$(LWS_LIB_PATH)/server.c \
	$(LWS_LIB_PATH)/output.c \
	$(LWS_LIB_PATH)/sha-1.c \
	$(LWS_LIB_PATH)/base64-decode.c \
	$(LWS_LIB_PATH)/getifaddrs.c


LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
