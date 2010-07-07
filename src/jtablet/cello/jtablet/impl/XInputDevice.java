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

import cello.jtablet.TabletDevice;
import cello.jtablet.TabletManager;
import cello.jtablet.ScreenComponent;
import cello.jtablet.event.TabletEvent;
import cello.repackaged.jpen.PLevel;
import cello.repackaged.jpen.provider.xinput.XiDevice;

/**
 * An {@link XInputDevice} is a device which is exposed through
 * the XInput API. We use JPen's native code to accomplish this,
 * though the necessary method calls to custom native code could
 * work just as well.
 *
 * While most {@code XInputDevice} objects will likely represent
 * tablets, XInput is *NOT* a tablet-specific API. It is possible
 * the user may have some other "extended" input device connected.
 * Provided the valuator mappings line up well, such non-tablet
 * devices should work fine with JTablet.
 *
 * @author Jason Gerecke
 */
public class XInputDevice extends TabletDevice {

	XiDevice device;

	float oldX = -100, oldY = -100;
	int   oldButtonMask = 0;
	boolean lastProximity = false;

	String name;
	String uniqueId;

	TabletDevice.Type type = Type.UNKNOWN;

	TabletDevice.Support uniqueIdSupport     = TabletDevice.Support.NO;
	TabletDevice.Support floatSupport        = TabletDevice.Support.YES;

	TabletDevice.Support buttonSupport       = TabletDevice.Support.UNKNOWN;
	TabletDevice.Support pressureSupport     = TabletDevice.Support.UNKNOWN;
	TabletDevice.Support rotationSupport     = TabletDevice.Support.UNKNOWN;
	TabletDevice.Support sidePressureSupport = TabletDevice.Support.UNKNOWN;
	TabletDevice.Support tiltSupport         = TabletDevice.Support.UNKNOWN;

	/**
	 * Uses the information available inside of an {@code XiDevice}
	 * to construct a new {@code XInputDevice}. A static method
	 * is used instead of a constructor since we can't determine
	 * enough information quickly enough (remember that the call
	 * to a superclass constructor *must* be the first line).
	 *
	 * @param device the XInput device to wrap
	 */
	protected static XInputDevice makeXInputDevice(XiDevice device) {
		String name     = device.getName();
		String uniqueId = name;
		TabletDevice.Type type = Type.UNKNOWN;

		//IDEA: We could move all this out to a properties
		//      file that would be not only be easier for
		//      users to extend with their own tablet data,
		//      but also could contain info that can't be
		//      found from the driver (e.g. physical tablet
		//      size, resolution)
		if (name.contains("Intuos") ||
		    name.contains("Cintiq")) {

			if      (name.contains("eraser")) { type = Type.ERASER;  }
			else if (name.contains("cursor")) { type = Type.MOUSE;   }
			else if (name.contains("pad"))    { type = Type.UNKNOWN; }
			else                              { type = Type.STYLUS;  }

			return new XInputDevice(device, type, name, uniqueId,
				TabletDevice.Support.YES, TabletDevice.Support.YES,
				TabletDevice.Support.NO, TabletDevice.Support.YES,
				TabletDevice.Support.YES, TabletDevice.Support.YES,
				TabletDevice.Support.YES);
		}
		else if (name.contains("Graphire") ||
			 name.contains("Bamboo")) {

			if      (name.contains("eraser")) { type = Type.ERASER;  }
			else if (name.contains("cursor")) { type = Type.MOUSE;   }
			else if (name.contains("pad"))    { type = Type.UNKNOWN; }
			else                              { type = Type.STYLUS;  }

			return new XInputDevice(device, type, name, uniqueId,
				TabletDevice.Support.YES, TabletDevice.Support.YES,
				TabletDevice.Support.NO, TabletDevice.Support.YES,
				TabletDevice.Support.NO, TabletDevice.Support.NO,
				TabletDevice.Support.NO);
		}

		return new XInputDevice(device, type, name, uniqueId);
	}

	protected XInputDevice(XiDevice device, TabletDevice.Type type,
		String name, String uniqueId) {

		this.device = device;
		this.type   = type;
		this.name   = name;
		this.uniqueId = uniqueId;
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

		this(device, type, name, uniqueId);

		this.floatSupport = floatSupport;
		this.buttonSupport = buttonSupport;
		this.uniqueIdSupport = uniqueIdSupport;
		this.pressureSupport = pressureSupport;
		this.rotationSupport = rotationSupport;
		this.sidePressureSupport = sidePressureSupport;
		this.tiltSupport = tiltSupport;
	}

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

	public void readPackets() {
		int width  = ScreenComponent.INSTANCE.bounds().width;
		int height = ScreenComponent.INSTANCE.bounds().height;
		
		while (device.nextEvent()) {
			XiDevice.EventType nativeType = device.getLastEventType();
			
			//Get valuator information
			float x            = getRangedValue(PLevel.Type.X) * width;
			float y            = getRangedValue(PLevel.Type.Y) * height;
			float pressure     = getRangedValue(PLevel.Type.PRESSURE);
			float tiltX        = getRangedValue(PLevel.Type.TILT_X);
			float tiltY        = getRangedValue(PLevel.Type.TILT_Y);
			float sidePressure = getRangedValue(PLevel.Type.SIDE_PRESSURE);
			float rotation     = getRangedValue(PLevel.Type.ROTATION);

			//Get button information
			int lastButton, buttonMask = oldButtonMask;

			if (nativeType == XiDevice.EventType.BUTTON_PRESS ||
			    nativeType == XiDevice.EventType.BUTTON_RELEASE)
				lastButton =  1 << (device.getLastEventButton() -1);
			else
				lastButton = 0;

			//TODO: Does XInput do this, or just the
			//      Linux Wacom drivers? For some
			//      reason button 0x0001 maps to
			//      0x0100; 0x0002 to 0x0200; etc.
			if (nativeType == XiDevice.EventType.BUTTON_PRESS)
				buttonMask = buttonMask | lastButton;
			else if (nativeType == XiDevice.EventType.BUTTON_RELEASE)
				buttonMask = buttonMask & ~lastButton;

			int button = 0;
			if ((lastButton & 0x01) > 0)
				button = java.awt.event.MouseEvent.BUTTON1;
			else if ((lastButton & 0x02) > 0)
				button = java.awt.event.MouseEvent.BUTTON2;
			else if ((lastButton & 0x04) > 0)
				button = java.awt.event.MouseEvent.BUTTON3;

			//TODO: Get keyModifier code up and working
			int keyModifiers = 0;

			//Get type information
			TabletEvent.Type eventType = TabletEvent.Type.MOVED;

			if (lastProximity == false)
				eventType = TabletEvent.Type.ENTERED;
			else if (lastProximity == true && device.getLastEventProximity() == false)
				eventType = TabletEvent.Type.EXITED;
			else if (x != oldX || y != oldY)
				if (buttonMask == 0)
					eventType = TabletEvent.Type.MOVED;
				else
					eventType = TabletEvent.Type.DRAGGED;
			else
				if (nativeType == XiDevice.EventType.BUTTON_PRESS)
						eventType = TabletEvent.Type.PRESSED;
				else if (nativeType == XiDevice.EventType.BUTTON_RELEASE)
					eventType = TabletEvent.Type.RELEASED;
				else
					eventType = TabletEvent.Type.LEVEL_CHANGED;
			

			//Create and send a new tablet event
			TabletEvent e = new TabletEvent(
				ScreenComponent.INSTANCE,
				eventType,
				device.getLastEventTime(),
				0,
				this,
				x,
				y,
				pressure,
				tiltX,
				tiltY,
				sidePressure,
				rotation,
				0,
				0,
				0,
				button,
				buttonMask
			);

			TabletManager.postTabletEvent(e);

			//Cleanup on aisle 4...
			oldX = x;
			oldY = y;
			oldButtonMask = buttonMask;
			lastProximity = device.getLastEventProximity();
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()+"["+getName()+" ("+getType()+")]";
	}

	@Override
	public Type getType() { return type; }

	@Override
	public Support getFloatSupport() { return floatSupport; }

	@Override
	public Support getButtonSupport() { return buttonSupport; }

	@Override
	public Support getUniqueIdSupport() { return uniqueIdSupport; }

	@Override
	public Support getPressureSupport() { return pressureSupport; }

	@Override
	public Support getTiltSupport() { return tiltSupport; }

	@Override
	public Support getSidePressureSupport() { return sidePressureSupport; }

	@Override
	public Support getRotationSupport() { return rotationSupport; }

	@Override
	public String getName() { return name; }
}
