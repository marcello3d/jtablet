/*!
 * Copyright (c) 2009 Jason Gerecke (killertofu@gmail.com)
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

package cello.jtablet.impl.jpen;

import java.util.Collection;
import java.util.LinkedList;

import cello.jtablet.TabletDevice;
import cello.jtablet.impl.AbstractTabletDevice;
import cello.jtablet.impl.Architecture;
import cello.jtablet.impl.NativeLoaderException;
import cello.jtablet.impl.NativeTabletManager;
import cello.jtablet.impl.ScreenTabletManager;
import cello.repackaged.jpen.provider.xinput.XiBus;
import cello.repackaged.jpen.provider.xinput.XiDevice;
import cello.repackaged.jpen.PLevel;
import cello.repackaged.jpen.internal.Range;


/**
 * Responsible for managaging tablets exposed through the X11 XInput
 * extension API. Based very heavily off of the WinTabTabletManager
 * and CocoaTabletManager code by Marcello. Also based very heavily
 * off of the whole JPen provider system (since I'm essentially
 * trying to emulate that part of their stack myself)
 * 
 * An outline of what needs to happen:
 *   1. XInputTabletManager is constructed
 *   2. We load the native driver if possible [throw exception if not]
 *   3. When started, we begin to monitor the tablet
 *   4. If anything interesting happens while monitoring we
 *      generate an event.
 *   5. Keep doing step 4 until we're told to stop.
 *
 * This outline is the same as what happens in the WinTabTabletManager,
 * despite it looking far more complex. Most of the overhead comes from
 * private classes to store individual cursors, code to see if the cursor
 * changed, code to compute values for events from raw data, etc.
 *
 * @author Jason Gerecke
 */
public class XInputTabletManager extends ScreenTabletManager implements NativeTabletManager {
	
	/**
	 * {@code XInputDevice} serves a dual purpose. In addition to being
	 * the concrete implementation of {@link AbstractTabletDevice} that
	 * will appear in JTablet's events, it also acts as a wrapper around
	 * JPen's {@code XiDevice} class. This saves us from completely
	 * re-implementing the wheel.
	 */
	private static class XInputDevice extends AbstractTabletDevice {	
		/**
		 * Uses the information available inside of an {@code XiDevice}
		 * to construct a new {@code XInputDevice}. A static method
		 * is used instead of a constructor since we can't determine
		 * enough information quickly enough (remember that the call
		 * to a superclass constructor *must* be the first line).
		 */
		protected static XInputDevice makeXInputDevice(XiDevice device) {
			String name     = device.getName();
			String uniqueId = name;
			
			TabletDevice.Type type = Type.UNKNOWN; //No clue at all :(
			
			TabletDevice.Support uniqueIdSupport     = TabletDevice.Support.NO;      //JPen provides no unique ID for us to use
			TabletDevice.Support floatSupport        = TabletDevice.Support.NO;      //XInput API limits us here
			
			TabletDevice.Support buttonSupport       = TabletDevice.Support.UNKNOWN; //We support buttons, but the tablet may not
			TabletDevice.Support pressureSupport     = TabletDevice.Support.UNKNOWN; //We support pressure, but the tablet may not
			TabletDevice.Support rotationSupport     = TabletDevice.Support.UNKNOWN; //We support rotation, but the tablet may not
			TabletDevice.Support sidePressureSupport = TabletDevice.Support.UNKNOWN; //We support side pressure, but the tablet may not
			TabletDevice.Support tiltSupport         = TabletDevice.Support.UNKNOWN; //We support tilt, but the tablet may not
			
			return new XInputDevice(device, type, name, uniqueId,
			   floatSupport, buttonSupport, uniqueIdSupport,
			   pressureSupport, rotationSupport,
			   sidePressureSupport, tiltSupport);
		}
		
		/**
		 * Constructs an {@code XInputDevice} for use by JTablet.
		 * Please use the static {@link XInputDevice}.{@link XInputDevice#makeXInputDevice(XiDevice)}
		 * instead of this directly.
		 */
		protected XInputDevice(XiDevice device, TabletDevice.Type type,
		   String name, String uniqueId, TabletDevice.Support floatSupport,
		   TabletDevice.Support buttonSupport, TabletDevice.Support uniqueIdSupport,
		   TabletDevice.Support pressureSupport, TabletDevice.Support rotationSupport,
		   TabletDevice.Support sidePressureSupport, TabletDevice.Support tiltSupport) {
			super(type, name, uniqueId, floatSupport, buttonSupport, uniqueIdSupport,
			   pressureSupport, rotationSupport, sidePressureSupport, tiltSupport);
			
			this.device = device;
		}
		
		XiDevice device;
		
		public XiDevice getXiDevice() { return device; }
	}
	
	private Collection<XInputDevice> devices = new LinkedList<XInputDevice>();
	private XiBus xiBus;
	private boolean running;
	
	/**
	 * This thread is responsible for the tablet polling loop.
	 * Once started, it just calls this class's readPackets()
	 * method over and over to be sure that we're getting new
	 * position/pressure/etc data.
	 */
	private Thread thread = new Thread("JTablet-XInput") {
		{
			setDaemon(true);
			setPriority(NORM_PRIORITY+1);
		}
		public void run() {
			while (running) {
				readPackets();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {}
			}
		}
	};
	
	/**
	 * This method attempts to load the library and find any
	 * attached devices. If either fails, we throw an exception
	 * immediatly.
	 */
	public void load() throws NativeLoaderException {
		try {
			xiBus = new XiBus();
		} catch (Exception e) {
			throw new NativeLoaderException("Could not obtain an XiBus for communication with the X11 XInput extension.", e);
		}
		
		try {
			getDevices(); //Check for tablets right up front
		} catch (Exception e) {
			System.err.println("Error on getdevices");
			throw new NativeLoaderException("Could not update the list of XInput devices.", e);
		}
	}

	/**
	 * With all the operating systems out there that can use X11,
	 * this class probably works fine in more than just Linux.
	 * I don't want to list explicit support for an OS until I
	 * know it works though...
	 */
	public boolean isSystemSupported(String os) {
		boolean support =
		     os.contains("linux")
		  //|| os.contains("bsd")
		  //|| os.contains("solaris")
		  ;
		
		return support;
	}

	@Override
	protected void start() {
		running = true;
		if (!thread.isAlive()) {
			thread.start();
		}
	}

	@Override
	protected void stop() {
		running = false;
	}

	public Architecture getArchitecture() {
		return Architecture.DEFAULT;
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
			if(!device.getIsAbsoluteMode() && !device.getName().toLowerCase().contains("pad")) {
				// A bug in the original JPen code, so likely
				// a bug here as well.
				System.err.println("devices using relative positioning mode are not supported, device skipped: "+device.getName()+
				                   "\n See bug description https://sourceforge.net/tracker/?func=detail&aid=2929548&group_id=209997&atid=1011964");
				continue;
			}
			
			device.setIsListening(true);
			if (!device.getIsListening()) {
				System.err.println("device was not able to be grabbed.");
			}
			
			devices.add(XInputDevice.makeXInputDevice(device));
		}
		
		if (devices.size() == 0)
			System.err.println("No supported devices found.");
	}
	
	/**
	 * Runs through the list of all XInputDevice objects that
	 * we've created, and sends out TabletEvent events for
	 * each event that we've detected.
	 */
	private void readPackets() {
		for (XInputDevice d : devices) {
			XiDevice device = d.getXiDevice();
			
			if (!device.nextEvent())
				continue;
			
			long time = device.getLastEventTime();
			
			float x = device.getLevelRange(PLevel.Type.X).getRangedValue(
				device.getValue(PLevel.Type.X)
			);
			
			float y = device.getLevelRange(PLevel.Type.Y).getRangedValue(
				device.getValue(PLevel.Type.Y)
			);
			
			float pressure = device.getLevelRange(PLevel.Type.PRESSURE).getRangedValue(
				device.getValue(PLevel.Type.PRESSURE)
			);
			
			float tiltX = device.getLevelRange(PLevel.Type.TILT_X).getRangedValue(
				device.getValue(PLevel.Type.TILT_X)
			);
			
			float tiltY = device.getLevelRange(PLevel.Type.TILT_Y).getRangedValue(
				device.getValue(PLevel.Type.TILT_Y)
			);
			
			float sidePressure = device.getLevelRange(PLevel.Type.SIDE_PRESSURE).getRangedValue(
				device.getValue(PLevel.Type.SIDE_PRESSURE)
			);
			
			float rotation = device.getLevelRange(PLevel.Type.ROTATION).getRangedValue(
				device.getValue(PLevel.Type.ROTATION)
			);
			
			int rawTabletButtonMask = device.getLastEventButton();
			
			int button = 0; //FIXME
			boolean buttonJustReleased = false; //FIXME
			boolean buttonJustPressed = false;  //FIXME

			//int lastModifiers = mouseListener.getLastModifiersEx();
			//int buttonModifiers = lastModifiers & BUTTON_MODIFIERS;
			//int keyModifiers = lastModifiers & KEY_MODIFIERS;
			int keyModifiers = 0;
			
			
			generatePointEvents(
			   time, 
			   keyModifiers, 
			   x,
			   y, 
			   pressure,
			   tiltX, 
			   tiltY, 
			   sidePressure,
			   rotation, 
			   rawTabletButtonMask,
			   button, 
			   buttonJustPressed, 
			   buttonJustReleased
			);
		}
	}
}
