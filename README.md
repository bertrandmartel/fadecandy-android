# Fadecandy Android

[![CircleCI](https://img.shields.io/circleci/project/bertrandmartel/fadecandy-android.svg?maxAge=2592000?style=plastic)](https://circleci.com/gh/bertrandmartel/fadecandy-android) 
[ ![Download](https://api.bintray.com/packages/bertrandmartel/maven/fadecandy-server-android/images/download.svg) ](https://bintray.com/bertrandmartel/maven/fadecandy-server-android/_latestVersion) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.bmartel/fadecandy-service/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.bmartel/fadecandy-service)
[![Javadoc](http://javadoc-badge.appspot.com/fr.bmartel/fadecandy-service.svg?label=javadoc)](http://javadoc-badge.appspot.com/fr.bmartel/fadecandy-service)
[![License](http://img.shields.io/:license-mit-blue.svg)](LICENSE.md)

The [Fadecandy](https://github.com/scanlime/fadecandy) server library for Android devices

Control your Fadecandy USB LED controller plugged into your Android device

Try Fadecandy server / client with the [sample app](https://play.google.com/store/apps/details?id=fr.bmartel.fadecandy) available on the Playstore.

**No root required**

[![Download Fadecandy from Google Play](http://www.android.com/images/brand/android_app_on_play_large.png)](https://play.google.com/store/apps/details?id=fr.bmartel.fadecandy)

![fadecandy](https://github.com/bertrandmartel/fadecandy-android/raw/master/img/fadecandy.gif)

You can control your Fadecandy device from :
* Android smartphone
* Android tablet
* Android TV (the sample app is not available on Android TV Playstore but you can upload the apk anyway)

## What is Fadecandy ?

Fadecandy is a USB controlled LED driver with on-board dithering. One Fadecandy device support up to 8 strips of 64 Leds that gives you a maximum of 512 Leds/Fadecandy device.

![notif](https://github.com/bertrandmartel/fadecandy-android/raw/master/img/fadecandy.jpg)

Check [official Fadecandy repository](https://github.com/scanlime/fadecandy) for more information about Fadecandy device

## What is Fadecandy Server ?

Fadecandy server is a TCP server embedded in Fadecandy project which is used to remotely control Fadecandy USB devices through [Open Pixel Control protocol](http://openpixelcontrol.org/), a custom TCP protocol tailored to control LEDs

Check [official Fadecandy repository](https://github.com/scanlime/fadecandy#open-pixel-control-server) for more information about Open Pixel Control Server

## How does it work ? 

Fadecandy Android fork is available at https://github.com/bertrandmartel/fadecandy

Originally, Fadecandy server uses libusbx to interface with Fadecandy USB devices. In Android, a regular user has to grant permission for the application to open an USB device.

Using [Fadecandy Android app](https://play.google.com/store/apps/details?id=fr.bmartel.fadecandy), when you plug a Fadecandy in your Android device, you will see this pop-up :

![notif](https://github.com/bertrandmartel/fadecandy-android/raw/master/screen/permission_pop.png)

If user click on `Use by default for this USB device`, it wont be asked again for this USB device when the device is re-plugged again.

What differs between libusbx Fadecandy server & Android Fadecandy server is that All USB operations including USB attached/detached events are catch using Java API :

* start server flow :

| order | description  | language |
|---|--------------------------------------------------------------------------|-------|
| 1 | register a USB event receiver (for a specific product/vendor ID)         | Java  |                                                 
| 2 | start Fadecandy server                                                   | C++   | 

* USB attached flow :

| order | description  | language |
|---|--------------------------------------------------------------------------|-------|
| 1 | catch a USB device attached event                                        | Java  |          
| 2 | check if this Fadecandy USB is allowed                                   | Java  |               
| 3 | ask permission if device is not allowed                                  | Java  |                
| 4 | open the device if permission is granted                                 | Java  |                 
| 5 | notify Fadecandy server that a new device is attached                    | C++   |                             

* USB detached flow :

| order | description | language |
|---|-------------------|--------|
| 1 | catch a USB device detached event | Java |
| 2 | notify Fadecandy server that a device is detached | C++ |


* USB write flow :

| order | description | language |
|-------|-------------|----------|
| 1     | prepare data to be written | C++ |
| 2     | perform a bulk transfer on `UsbDeviceConnection` | Java |

For writing to USB device, Fadecandy server is calling from C++ a Java method to perform a bulk transfer

## How to include it in your Android project ?

* with Gradle, from jcenter :

```
compile 'bmartel.fr:fadecandy-service:1.4'
```

## How to use it ?

* Use `FadecandyClient` service wrapper : 


```
mFadecandyClient = new FadecandyClient(mContext, 

		new IFcServerEventListener() {

            @Override
            public void onServerStart() {

                // server is started 

            }

            @Override
            public void onServerClose() {

                // server is closed

            }

            @Override
            public void onServerError(ServerError error) {

            	// a server error occured

            }

        }, new IUsbEventListener() {

            @Override
            public void onUsbDeviceAttached(UsbItem usbItem) {
                
                // a Fadecandy device has been attached

            }

            @Override
            public void onUsbDeviceDetached(UsbItem usbItem) {
                
                // a Fadecandy device has been detached

            }
        }, 
        "com.your.package/.activity.MainActivity"
);
```

`FadecandyClient` will give you an easy-to-use interface between Fadecandy Service and your application


### Start Fadecandy server 

```
mFadecandyClient.startServer();
```

`startServer()` will internally stop the server if already running before starting

### Stop Fadecandy server

```
mFadecandyClient.closeServer();
```

### Check if server is running 

```
boolean isRunning = mFadecandyClient.isServerRunning();
```

### Get last server IP/host & last server port

```
String serverAdress = mFadecandyClient.getIpAddress();

int serverPort = mFadecandyClient.getServerPort();
```

### Set server IP/host & server port 

```
mFadecandyClient.setServerAddress("127.0.0.1");

mFadecandyClient.setServerPort(7890);
```

You will need to call `startServer()` to restart the server after modifying these parameters

### Get list of Fadecandy USB devices attached

```
HashMap<Integer, UsbItem> usbDevices = mFadecandyClient.getUsbDeviceMap();
```

The key is the USB device file descriptor, The value is an `UsbItem` object encapsulating :

| Class       | description    |
|-------------|----------------|
| `UsbDevice` | features attached USB device     |
| `UsbConnection` | send/receive data from an UBS device |
| `UsbEndpoint` | channel used for sending/receiving data   |


### Get Fadecandy server configuration 

```
FadecandyConfig config = mFadecandyClient.getConfig();
```

Fadecandy configuration is composed of the Top level object defined in [Fadecandy Server configuration documentation](https://github.com/scanlime/fadecandy/blob/master/doc/fc_server_config.md#top-level-object)

### Set Fadecandy service type 

 * Set the Fadecandy service as `PERSISTENT` (default value) which means the service will stay in background, a notification will be present in notification view. The user can kill the service by clicking on "close background service" on the notification :

```
mFadecandyClient.setServiceType(ServiceType.PERSISTENT_SERVICE);
```

![notif](https://github.com/bertrandmartel/fadecandy-android/raw/master/screen/notif.png)

 * Set the Fadencandy service as `NON_PERSISTENT`. The service will be killed as soon as no application is bound to it

```
mFadecandyClient.setServiceType(ServiceType.NON_PERSISTENT_SERVICE);
```

### Bind Fadecandy service without starting server

```
mFadecandyClient.connect();
```

### Unbind Fadecandy service

```
mFadecandyClient.disconnect();
```

Assure you call `disconnect()` to close service & unregister client receiver when you are done with Fadecandy Service (eg exit your application)

### Proguard

If you are using proguard add this to your `proguard-rules.pro` : 

```
-keep class fr.bmartel.android.fadecandy.service.FadecandyService { *; }

-keepclassmembers,allowobfuscation class fr.bmartel.android.fadecandy.service.FadecandyService.** {
    <methods>;
}
```

This will keep methods in `FadecandyService` to preserve calls from native code to this class

## Build Library

### Get source code

```
git clone git@github.com:bertrandmartel/fadecandy-android.git
cd fadecandy-android
git submodule update --init --recursive
```

### Build

```
./gradlew build
```

## Open Source components

### Fadecandy Service

* Fadecandy : https://github.com/scanlime/fadecandy
* rapidjson : https://github.com/scanlime/rapidjson
* libwebsockets : https://github.com/bertrandmartel/libwebsockets
* Android support-v4

### Fadecandy Application

* DiscreteSeekBar : https://github.com/AnderWeb/discreteSeekBar
* Android Holo ColorPicker : https://github.com/LarsWerkman/HoloColorPicker
* Open Pixel Control Library : https://github.com/bertrandmartel/opc-java
* AndroidAsync : https://github.com/koush/AndroidAsync
* Led Icon by Kenneth Appiah, CA (Pulic Domain) : https://thenounproject.com/search/?q=led&i=3156
* appcompat-v7, design & recyclerview-v7

## License

```
The MIT License (MIT) Copyright (c) 2016 Bertrand Martel
```
