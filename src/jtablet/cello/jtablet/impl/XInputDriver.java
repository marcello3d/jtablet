/*!
 * Copyright (c) 2010 Jason Gerecke (killertofu@gmail.com)
 *
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *     1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software
 *     in a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *     2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 *
 *     3. This notice may not be removed or altered from any source
 *     distribution.
 */

package cello.jtablet.impl;

import java.util.Collection;
import java.util.LinkedList;

import cello.jtablet.DriverStatus;
import cello.repackaged.jpen.provider.xinput.XiBus;
import cello.repackaged.jpen.provider.xinput.XiDevice;

/**
 * This {@link TabletDriver} interfaces with X11's XInput API to obtain
 * events from tablets. Actually, that's a little bit of a lie -- this
 * class actually interfaces with JPen's Java XInput provider...
 *
 * @author Jason Gerecke
 */
public class XInputDriver implements TabletDriver {
	
	private XiBus xiBus;
	private Collection<XInputDevice> devices = new LinkedList<XInputDevice>();
	private DriverStatus status;

	public void load() {
		String action = "";
		try {
			action = "loading native library";
			System.loadLibrary("jtablet2");

			action = "obtaining XiBus";
			xiBus = new XiBus();

			action = "obtaining list of XInput devices";
			getDevices();

			status = new DriverStatus(DriverStatus.State.LOADED);
		} catch (Throwable t) {
			String exceptionString = t.toString();
			String homeFolder = System.getProperty("user.home");

			if (exceptionString.contains(homeFolder)) {
				NativeLoaderException e = new NativeLoaderException(exceptionString.replace(homeFolder, "~"), t.getCause());
				e.setStackTrace(t.getStackTrace());
				t = e;
			}

			Exception ex = new NativeLoaderException("Loading XInputDriver failed at: " + action);
			status = new DriverStatus(DriverStatus.State.NATIVE_EXCEPTION, ex);
		}
	}


	public DriverStatus getStatus() {
		return status;
	}
	
	public void run() {
		try {
			while (true) {
				for (XInputDevice d : devices)
					d.readPackets();

				Thread.sleep(10);
			}
		} catch (InterruptedException e) {}
	}

	private void getDevices() {
		for (int i=xiBus.getXiDevicesSize()-1; i>=0; i--) {
			// This is used in Jpen, though I'm not 100% sure why.
			// My best current guess is that it is to allow
			// better multithreading. Since X isn't thread
			// safe we simply give each XiDevice its own
			// connection to the server, and let X deal with
			// simultaneous requests from different connections.
			XiBus xiBus2 = null;

			try { xiBus2 = new XiBus(); xiBus2.setXiDevice(i); }
			catch (Exception ex) { continue; }

			XiDevice device = xiBus2.getXiDevice();
			device.setIsListening(true);
			if (!device.getIsListening()) {
				System.err.println("device was not able to be grabbed.");
			}

			devices.add(XInputDevice.makeXInputDevice(device));
		}

		if (devices.isEmpty())
			System.err.println("No supported devices found.");
	}
}
