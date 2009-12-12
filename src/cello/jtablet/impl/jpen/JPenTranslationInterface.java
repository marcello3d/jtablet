package cello.jtablet.impl.jpen;

import java.awt.event.MouseEvent;

import jpen.PButtonEvent;
import jpen.PKindEvent;
import jpen.PLevel;
import jpen.PLevelEvent;
import jpen.PenManager;
import jpen.event.PenAdapter;
import jpen.owner.ScreenPenOwner;
import jpen.provider.NativeLibraryLoader;
import cello.jtablet.TabletDevice;
import cello.jtablet.events.TabletEvent;
import cello.jtablet.events.TabletEvent.Type;
import cello.jtablet.impl.ScreenInputInterface;
import cello.jtablet.impl.platform.NativeScreenInputInterface;

/**
 * 
 * @author marcello
 */

public class JPenTranslationInterface extends ScreenInputInterface {

	private PenManager penManager;
	private JPenListener listener = new JPenListener();
	

	/**
	 */
	public JPenTranslationInterface() {
	}
	
	@Override
	protected synchronized void start() {
		if (penManager == null) {
			penManager = new PenManager(new ScreenPenOwner());
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
		private TabletDevice device = TabletDevice.SYSTEM_MOUSE;
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
					type = TabletDevice.Type.STYLUS_TIP;
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
				public Support supportsButtons() {
					return Support.SUPPORTED;
				}

				@Override
				public Support supportsDeviceID() {
					return Support.UNKNOWN;
				}

				@Override
				public Support supportsPressure() {
					return Support.UNKNOWN;
				}

				@Override
				public Support supportsRotation() {
					return Support.UNKNOWN;
				}

				@Override
				public Support supportsSidePressure() {
					return Support.UNKNOWN;
				}

				@Override
				public Support supportsTilt() {
					return Support.UNKNOWN;
				}
				
			};
			
			fireScreenTabletEvent(new TabletEvent(SCREEN_COMPONENT, TabletEvent.Type.NEW_DEVICE, ev.getTime(), buttonMask, device, x, y));
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
					fireScreenTabletEvent(new TabletEvent(SCREEN_COMPONENT, TabletEvent.Type.DRAGGED, ev.getTime(), buttonMask, device, 
									x,y, pressure, tiltX,tiltY, sidePressure, rotation, TabletEvent.NOBUTTON));
				} else {
					fireScreenTabletEvent(new TabletEvent(SCREEN_COMPONENT, TabletEvent.Type.MOVED, ev.getTime(), buttonMask, device,
							x,y, pressure, tiltX,tiltY, sidePressure, rotation, TabletEvent.NOBUTTON));
				}
			} else if (levelChanged) {
				fireScreenTabletEvent(new TabletEvent(SCREEN_COMPONENT, TabletEvent.Type.LEVEL_CHANGED, ev.getTime(), buttonMask, device,
						x,y,pressure, tiltX,tiltY, sidePressure, rotation, TabletEvent.NOBUTTON));
			}
		}
	}


	public boolean isSystemSupported(String os) {
		return true;
	}
}
