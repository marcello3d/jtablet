/*!
 * Copyright (c) 2009 Marcello Bastéa-Forte (marcello@cellosoft.com)
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

import java.awt.event.MouseEvent;

import cello.repackaged.jpen.event.PenAdapter;
import cello.repackaged.jpen.owner.ScreenPenOwner;
import cello.jtablet.TabletDevice;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletEvent.Type;
import cello.jtablet.impl.ScreenTabletManager;
import cello.jtablet.impl.SystemDevice;
import cello.repackaged.jpen.PButtonEvent;
import cello.repackaged.jpen.PKindEvent;
import cello.repackaged.jpen.PLevel;
import cello.repackaged.jpen.PLevelEvent;
import cello.repackaged.jpen.PenManager;

/**
 * 
 * @author marcello
 */

public class JPenTranslationTabletManager extends ScreenTabletManager {

	private PenManager penManager;
	private JPenListener listener = new JPenListener();
	

	/**
	 */
	public JPenTranslationTabletManager() {
	}
	
	@Override
	protected synchronized void start() {
		if (penManager == null) {
			penManager = new PenManager(ScreenPenOwner.getInstance());
		}
		penManager.pen.addListener(listener);
	}

	@Override
	protected synchronized void stop() {
		if (penManager != null) {
			penManager.pen.removeListener(listener);
		}
	}


	/**
	 * A wrapper that for JPen into our TabletEvent structure.  Makes it much
	 * more digestable and adds support for consuming events.
	 * 
	 * @author Marcello
	 */
	protected class JPenListener extends PenAdapter {
		private float x=0,y=0,pressure=0,sidePressure=0,tiltX=0,tiltY=0,rotation=0;
		private TabletDevice device = SystemDevice.INSTANCE;
		private int buttonMask = 0;

		@Override
		public void penButtonEvent(PButtonEvent ev) {
			// Translate the button
			int mask = 0;
			int button = MouseEvent.NOBUTTON;
			switch (ev.button.getType()) {
			case LEFT:
				button = MouseEvent.BUTTON1;
				mask = MouseEvent.BUTTON1_DOWN_MASK;
				break;
			case CENTER:
				button = MouseEvent.BUTTON2;
				mask = MouseEvent.BUTTON2_DOWN_MASK;
				break;
			case RIGHT:
				button = MouseEvent.BUTTON3;
				mask = MouseEvent.BUTTON3_DOWN_MASK;
				break;
			case CUSTOM:
			default:
				break;
			}
			boolean pressed = ev.button.value;
			if (pressed) {
				// flip mask bit on
				buttonMask |= mask;
			} else {
				// flip mask bit off
				buttonMask &= ~mask;
			}
			
			Type type = pressed ? TabletEvent.Type.PRESSED : TabletEvent.Type.RELEASED;
			fireScreenTabletEvent(new TabletEvent(SCREEN_COMPONENT, type, ev.getTime(), device, buttonMask,
											x,y,
											button));
		}
		@Override
		public void penKindEvent(PKindEvent ev) {
			// Translate the device
			final TabletDevice.Type type;
			switch (ev.pen.getKind().getType()) {
				case CURSOR:
					type = TabletDevice.Type.MOUSE;
					break;
				case ERASER:
					type = TabletDevice.Type.ERASER;
					break;
				case STYLUS:
					type = TabletDevice.Type.STYLUS;
					break;
				default:
					type = TabletDevice.Type.UNKNOWN;
					break;
			}
			

			device = new TabletDevice() {

				@Override
				public TabletDevice.Type getType() {
					return type;
				}

				@Override
				public Support getButtonSupport() {
					return Support.YES;
				}

				@Override
				public Support getUniqueIdSupport() {
					return Support.UNKNOWN;
				}

				@Override
				public Support getPressureSupport() {
					return Support.UNKNOWN;
				}

				@Override
				public Support getRotationSupport() {
					return Support.UNKNOWN;
				}

				@Override
				public Support getSidePressureSupport() {
					return Support.UNKNOWN;
				}

				@Override
				public Support getTiltSupport() {
					return Support.UNKNOWN;
				}
				
			};
			
			fireScreenTabletEvent(new TabletEvent(SCREEN_COMPONENT, TabletEvent.Type.ENTERED, ev.getTime(), buttonMask, buttonMask, device, x, y));
		}
		@Override
		public void penLevelEvent(PLevelEvent ev) {
			boolean moved = false;
			boolean levelChanged = false;
			for (PLevel level : ev.levels) {
				switch (level.getType()) {
				case X:
					if (x != level.value) {
						x = level.value;
						moved = true;
					}
					break;
				case Y:
					if (y != level.value) {
						y = level.value;
						moved = true;
					}
					break;
				case PRESSURE:
					if (pressure != level.value) {
						pressure = level.value;
						levelChanged = true;
					}
					break;
				case SIDE_PRESSURE:
					if (sidePressure != level.value) {
						sidePressure = level.value;
						levelChanged = true;
					}
					break;
				case ROTATION:
					if (rotation != level.value) {
						rotation = level.value;
						levelChanged = true;
					}
					break;
				case TILT_X:
					if (tiltX != level.value) {
						tiltX = level.value;
						levelChanged = true;
					}
					break;
				case TILT_Y:
					if (tiltY != level.value) {
						tiltY = level.value;
						levelChanged = true;
					}
					break;
				}
			}

//			if (device == TabletDevice.MOUSE) {
//				if (!pressured && buttonMask != 0) {
//					pressure = 1;
//				} 
//				if (buttonMask == 0){
//					pressure = 0;
//				}
//			}
			if (moved) {
				// Dragging?
				if (pressure > 0) {
					fireScreenTabletEvent(new TabletEvent(SCREEN_COMPONENT, TabletEvent.Type.DRAGGED, ev.getTime(), buttonMask, buttonMask,  device, 
									x,y, pressure, tiltX,tiltY, sidePressure, rotation, TabletEvent.NOBUTTON));
				} else {
					fireScreenTabletEvent(new TabletEvent(SCREEN_COMPONENT, TabletEvent.Type.MOVED, ev.getTime(), buttonMask, buttonMask,  device,
							x,y, pressure, tiltX,tiltY, sidePressure, rotation, TabletEvent.NOBUTTON));
				}
			} else if (levelChanged) {
				fireScreenTabletEvent(new TabletEvent(SCREEN_COMPONENT, TabletEvent.Type.LEVEL_CHANGED, ev.getTime(), buttonMask,buttonMask,  device,
						x,y,pressure, tiltX,tiltY, sidePressure, rotation, TabletEvent.NOBUTTON));
			}
		}
	}
}
