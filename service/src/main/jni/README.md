Fadecandy Server
================

The Fadecandy Server is a background process that handles the USB communications with one or more Fadecandy Controller boards.

You can send pixel data to a Fadecandy Server over the Open Pixel Control protocol, or from a web app via WebSockets. See the 'doc' directory for details on all protocols supported.

The Fadecandy Server optionally takes configuration options in the form of a JSON config file. Configuration files allow you to do things like:

* Support multiple Fadecandy boards
* Mix Fadecandy and DMX lighting devices
* Listen on an alternate TCP port
* Listen for connections from the network, not just from local programs

The configuration file format is documented in the **doc** directory.

When you run the Fadecandy Server, it will provide a simple web interface. By default, the Fadecandy server runs at [http://localhost:7890](http://localhost:7890).

Build
-----

Pre-built binaries are included in the **bin** directory, but you can also build it yourself. All required libraries are included as git submodules.

It can build on Windows, Mac OS, or Linux using Make and other command line tools. On Windows, the build uses MinGW and gcc.


Getting Started
-----

In order to build the binary from source you need to run the following commands inside of the **server** directory:

`$ make submodules`

`$ make`

The compiled binary will be created in the same **server** directory

If you want to remove the compiled binary and source files run:

`$ make clean`
