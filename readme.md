About
=====

This project combined with
<a href="http://linuxonandroid.org/">Linux-on-Android</a> can enable a
complete Arduino development environment on Android. This app solves
a problem that most Android devices do not have USB ports to enable
normal programming of Arduino devices. To further complicate the
the issue the Bluetooth serial port is not shared from Android to
Linux-on-Android. To work around these issues this app runs as a
service which connects to Arduino over Bluetooth and to the avrdude
programmer over a TCP socket. The avrdude programming commands are
forwarded to Arduino over Bluetooth by the spp-mirror app.

Linux-on-Android can support the Arduino IDE however I've found that
the UI is a bit awkward to use due to the touch screen mouse and
smaller display. As a result, I recommend using the
<a href="http://inotool.org/">ino command line toolkit</a> for
working with Android hardware. As I will show, the ino command line
toolkit combined with vim and avrdude can replace the Arduino IDE.

For more information on uploading sketches wirelessly via Bluetooth see this
<a href="http://ariverpad.wordpress.com/2012/02/26/uploading-arduino-sketches-wirelessly-using-a-bluetooth-breakout-board/">link</a>.
Note that I found that many bootloaders implement the "Adaboot no-wait mod"
which bypasses the bootloader when the Arduino is reset by the watchdog
timer. This prevents the DTR pin workaround using the watchdog timer from
soft reseting the Arduino into the bootloader.  It is also required
for Arduino and Bluesmirf to agree on the baud rate. I recommend using the
Optiboot bootloader which sets the baud rate to 115200 matching the default
used by Bluesmirf. The baud rate for the default bootloader shipped with
many Arduinos is 57600.

The Bluesmirf Bluetooth module is available from Sparkfun.

* <a href="https://www.sparkfun.com/products/10268">Bluesmirf Gold</a>
* <a href="https://www.sparkfun.com/products/10269">Bluesmirf Silver</a>

Send questions or comments to Jeff Boody - jeffboody@gmail.com

Installing Android SDK
======================

	# download the SDK to $ANDROID
	# i.e. ~/android
	http://developer.android.com/sdk/index.html
	
	# unzip the packages
	cd $ANDROID
	tar -xzf android-sdk_<verison>.tgz
	
	# install ant (if necessary)
	sudo apt-get install ant
	
	# configuring the Android SDK
	# add "SDK Platform Android 2.1, API 7"
	android &
	# "Available Packages"
	# select check box for "SDK Platform 2.1, API 7"
	# install selected
	
	# Eclipse is not required

Clone Project
=============

	# download the source with git
	cd $SRC
	git clone git://github.com/jeffboody/spp-mirror.git
	cd spp-mirror
	git submodule update --init
	
	# or download the source(s) as a zip or tarball
	https://github.com/jeffboody/spp-mirror/archives/master
	https://github.com/jeffboody/spp-mirror/archives/master
	
	# configure the profile
	cd spp-mirror
	vim profile
	# edit SDK to point to $ANDROID

Building and installing the spp-mirror app
==========================================

	cd $SRC/spp-mirror
	source profile
	
	# start the adb server as root
	sudo adb devices
	
	./build-java.sh
	./install.sh

Configure Linux-on-Android for Arduino Development
==================================================

	# note these directions assume Ubuntu
	
	# vim editor (recommended)
	sudo apt-get install vim-gnome
	
	# Arduino SDK
	sudo apt-get install arduino
	
	# install ino command line toolkit
	# https://github.com/amperka/ino
	sudo apt-get install git
	sudo apt-get install make
	sudo apt-get install python-setuptools
	sudo apt-get install python-configobj
	sudo apt-get install python-jinja2
	sudo apt-get install python-serial
	cd $SRC
	git clone git://github.com/amperka/ino.git
	cd ino
	sudo make install

Example blink sketch
====================

Initialize a blink project
--------------------------

	mkdir blink
	cd blink
	ino init
	rm src/sketch.ino
	
	# add the famous blink sketch
	cp Blink.ino blink/src/Blink.ino

Build the blink sketch
-----------------------

	cd blink
	ino build

Connect the Blink circuit to Bluetooth
--------------------------------------

1. build standard blink circuit
2. connect Bluesmirf power and ground
3. connect Bluesmirf TX to Arduino RX
4. connect Bluesmirf RX to Arduino TX
5. connect Bluesmirf CTS to Bluesmirf RTS

Upload procedure
----------------

Since the Bluesmirf is not capable of automatically reseting the Arduino we
must manually reset the Arduino to enter the bootloader.

1. start the spp mirror app
2. connect to the Bluetooth spp device and ensure net is listening
3. switch to terminal
4. press Arduino reset button
5. wait ~100ms then start upload

avrdude upload command:

	avrdude -v -patmega328p -carduino -Pnet:127.0.0.1:6800 -D -Uflash:w:.build/uno/firmware.hex:i

License
=======

	Copyright (c) 2012 Jeff Boody

	Permission is hereby granted, free of charge, to any person obtaining a
	copy of this software and associated documentation files (the "Software"),
	to deal in the Software without restriction, including without limitation
	the rights to use, copy, modify, merge, publish, distribute, sublicense,
	and/or sell copies of the Software, and to permit persons to whom the
	Software is furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included
	in all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.
