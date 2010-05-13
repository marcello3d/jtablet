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
import cello.jtablet.impl.MouseTabletManager;
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
 *
 * IDEA: Make XInputTabletManager *manage* the attached tablets,
 *       nothing more. Essentially, it should periodically check
 *       for (dis-)connected devices and update the list as needed.
 *
 * IDEA: Make XInputDevice be in charge of the device it manages.
 *       This means that each device would have its own thread running
 *       readPackets(). Ideally each thread would also stop automatically
 *       when the device is disconnected (and wait for the manager
 *       to nuke it completely), but I'm not sure that's possible
 *       in XInput1...
 *
 * TODO: Add mouse support in to the tablet manager. As things stand
 *       now, the mouse isn't an XInput device that JPen works with,
 *       so JTablet-enabled apps can't draw with it (the mouse still
 *       works fine for interacting with the UI, however).
 *
 * Javadoc:
 * --------------------------------------------------------------
 * 
 * {@code XInputTabletManager} is responsible for keeping track of all
 * of the tablets exposed via the X11 XInput interface. This class
 * offloads the dirty native work off to JPen for convenience -- why
 * reinvent the wheel? :)
 *
 * @author Jason Gerecke
 */
public class XInputTabletManager extends ScreenTabletManager implements NativeTabletManager {
	
	/**
	 * An {@code XInputDevice} is a device which is exposed through
	 * the XInput API. We use JPen's native code to acomplish this,
	 * though the necessary method calls to custom native code could
	 * work just as well.
	 *
	 * While most {@code XInputDevice} objects will likely represent
	 * tablets, XInput is *NOT* a tablet-specific API. It is possible
	 * the user may have some other "extended" input device connected.
	 * Provided the valuator mappings line up well, such non-tablet
	 * devices should work fine with JTablet.
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
			
			TabletDevice.Support uniqueIdSupport     = TabletDevice.Support.NO;
			TabletDevice.Support floatSupport        = TabletDevice.Support.YES;
			
			TabletDevice.Support buttonSupport       = TabletDevice.Support.UNKNOWN;
			TabletDevice.Support pressureSupport     = TabletDevice.Support.UNKNOWN;
			TabletDevice.Support rotationSupport     = TabletDevice.Support.UNKNOWN;
			TabletDevice.Support sidePressureSupport = TabletDevice.Support.UNKNOWN;
			TabletDevice.Support tiltSupport         = TabletDevice.Support.UNKNOWN;
			
			TabletDevice.Type type = Type.UNKNOWN;
			
			//IDEA: We could move all this out to a properties
			//      file that would be not only be easier for
			//      users to extend with their own tablet data,
			//      but also could contain info that can't be
			//      found from the driver (e.g. physical tablet
			//      size, resolution)
			if (name.contains("Wacom Graphire4")) {
				buttonSupport       = TabletDevice.Support.YES;
				pressureSupport     = TabletDevice.Support.YES;
				rotationSupport     = TabletDevice.Support.NO;
				sidePressureSupport = TabletDevice.Support.NO;
				tiltSupport         = TabletDevice.Support.NO;
				
				if      (name.contains("eraser")) { type = Type.ERASER;  }
				else if (name.contains("cursor")) { type = Type.MOUSE;   }
				else if (name.contains("pad"))    { type = Type.UNKNOWN; }
				else                              { type = Type.STYLUS;  }
			}
			
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
		int lastButton = 0;
		int buttonMask = 0;
		
		public XiDevice getXiDevice() { return device; }
		
		public int getValue(PLevel.Type type) {
			return device.getValue(type);
		}
		
		public float getRangedValue(PLevel.Type type) {
			cello.repackaged.jpen.internal.Range range = device.getLevelRange(type);
			int value = getValue(type);
			float ranged = range.getRangedValue(value);
			
			if (Float.isInfinite(ranged) || Float.isNaN(ranged))
				return value;
			else
				return ranged;
		}
		
		public int getButtonMask() {
			return buttonMask;
		}
		
		public int getLastEventButton() {
			return lastButton;
		}
		
		public boolean nextEvent() {
			if (device.nextEvent()) {
				XiDevice.EventType eventType = device.getLastEventType();
				
				if (eventType == XiDevice.EventType.BUTTON_PRESS ||
				    eventType == XiDevice.EventType.BUTTON_RELEASE)
					lastButton = device.getLastEventButton();
				else
					lastButton = 0;
				
				if (eventType == XiDevice.EventType.BUTTON_PRESS)
					buttonMask = buttonMask | (1 << (lastButton - 1));
				else if (eventType == XiDevice.EventType.BUTTON_RELEASE)
					buttonMask = buttonMask & ~(1 << (lastButton - 1));
				
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	private Collection<XInputDevice> devices = new LinkedList<XInputDevice>();
	private XiBus xiBus;
	private boolean running;
	private java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	private MouseTabletManager mouseListener = new MouseTabletManager();
	
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
			mouseListener.setFiringEvents(false);
			thread.start();
		}
		mouseListener.setFiringEvents(true);
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
			
			while (d.nextEvent()) {
				long time = device.getLastEventTime();
				
				float x            = d.getRangedValue(PLevel.Type.X) * screenSize.width;				
				float y            = d.getRangedValue(PLevel.Type.Y) * screenSize.height;
				float pressure     = d.getRangedValue(PLevel.Type.PRESSURE);
				float tiltX        = d.getRangedValue(PLevel.Type.TILT_X);
				float tiltY        = d.getRangedValue(PLevel.Type.TILT_Y);
				float sidePressure = d.getRangedValue(PLevel.Type.SIDE_PRESSURE);
				float rotation     = d.getRangedValue(PLevel.Type.ROTATION);
				
				//TODO: Does XInput do this, or just the
				//      Linux Wacom drivers? For some
				//      reason button 0x0001 maps to
				//      0x0100; 0x0002 to 0x0200; etc.
				int rawTabletButtonMask = d.getButtonMask();
				int buttonChanges = d.getLastEventButton() > 0 ? 1 << (d.getLastEventButton() - 1) : 0;
				
				//HACK: Why is it that we set both a "button" value
				//(which, BTW, isn't an ORing of components...) AND
				//a rawTabletButtonMask? Is it because we can't be
				//sure button 1 should be MouseEvent.BUTTON1? *shrug*
				int button = 0;
				if ((buttonChanges & 0x01) > 0)
					button = java.awt.event.MouseEvent.BUTTON1;
				else if ((buttonChanges & 0x02) > 0)
					button = java.awt.event.MouseEvent.BUTTON2;
				else if ((buttonChanges & 0x04) > 0)
					button = java.awt.event.MouseEvent.BUTTON3;
				
				boolean buttonJustPressed = (buttonChanges & rawTabletButtonMask) != 0;
				boolean buttonJustReleased = (buttonChanges & ~rawTabletButtonMask) != 0;

				//TODO: Get keyModifier code up and working

				//int lastModifiers = mouseListener.getLastModifiersEx();
				//int buttonModifiers = lastModifiers & BUTTON_MODIFIERS;
				//int keyModifiers = lastModifiers & KEY_MODIFIERS;
				int keyModifiers = 0;
			
			
				//HACK: ScreenTabletManager needs an overhaul:
				//      it keeps way more state information
				//      than it should. Using two tablets under
				//      the same manager is difficult at best,
				//      and outright buggy at worst.
				//
				//      These two method calls are just one
				//      instance of the crazieness. The first
				//      sets "lastDevice" which is used by the
				//      latter. Unless we always call the
				//      former before the latter, ScreenTabletManager
				//      is bound to get confused.

				generateDeviceEvents(d, time, keyModifiers, device.getLastEventProximity());

				generateDeviceEvents(d, time, keyModifiers, true);
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
}
