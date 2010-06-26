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

package cello.jtablet.impl.wacomweb;

import java.applet.Applet;

import cello.jtablet.TabletDevice;
import cello.jtablet.TabletDevice.Type;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 * This class provides a wrapper for accessing tablet information through Wacom's web tablet. Read more here:
 * http://www.wacomeng.com/web/release_notes.htm and http://www.wacomeng.com/web/
 * 
 * @author marcello
 */
public class WacomWebPlugin {
	private final JSObject wacomJavascriptObject;


	private static JSObject getWacomPluginFromEmbed(Applet applet, String embedName) throws JSException {
		JSObject window = JSObject.getWindow(applet);
		JSObject document = (JSObject)window.getMember("document");
		JSObject embeds = (JSObject)document.getMember("embeds");
		return (JSObject)embeds.getMember(embedName);
	}
	
	/**
	 * Constructs a wacomwebplugin wrapper object by searching for an embed object in the same document as the specified 
	 * Applet.
	 * @param applet
	 * @param embedName the html name associated with the wacom plugin object
	 * @throws JSException
	 */
	public WacomWebPlugin(Applet applet, String embedName) throws JSException {
		this(getWacomPluginFromEmbed(applet, embedName));
	}
	
	/**
	 * @param wacomJavascriptObject the JSObject  
	 */
	public WacomWebPlugin(JSObject wacomJavascriptObject) {
		this.wacomJavascriptObject = wacomJavascriptObject;
	}

	/**
	 * @return if a Tablet device is attached
	 */
	public boolean isAttached() {
		return getIntegerAsBoolean(getProperty("isWacom"));
	}
	
	/**
	 * @return if the last packet came from an eraser device
	 */
	public boolean isEraser() {
		return getIntegerAsBoolean(getProperty("isEraser"));
	}

	/**
	 * @return last packet pressure [0.0 ~ 1.0] 
	 */
	public float getPressure() {
		return getValueAsFloat(getProperty("pressure"));
	}

	/**
	 * @return last packet tangential pressure [0.0 ~ 1.0] 
	 */
	public float getTangentialPressure() {
		return getValueAsFloat(getProperty("tangentialPressure"));
	}
	/**
	 * @return last packet tablet x coordinate 
	 */
	public long getPosX() {
		return getValueAsLong(getProperty("posX"));
	}
	/**
	 * @return last packet tablet y coordinate 
	 */
	public long getPosY() {
		return getValueAsLong(getProperty("posY"));
	}

	/**
	 * @return last packet stylus rotation in radians
	 */
	public float getRotation() {
		return getValueAsFloat(getProperty("rotationRad"));
	}
	/**
	 * @return last packet horizontal tilt [-1.0 ~ 1.0] 
	 */
	public float getTiltX() {
		return getValueAsFloat(getProperty("tiltX"));
	}
	/**
	 * @return last packet vertical tilt [-1.0 ~ 1.0] 
	 */
	public float getTiltY() {
		return getValueAsFloat(getProperty("tiltY"));
	}
	
	/**
	 * @return last packet device type
	 */
	public TabletDevice.Type getPointerType() {
		switch (getValueAsInt(getProperty("pointerType"))) {
			case 1:
				return Type.STYLUS;
			case 2:
				return Type.MOUSE;
			case 3:
				return Type.ERASER;
			default:
				return Type.UNKNOWN;
		}
	}
	
	/**
	 * @return The version of the plugin in decimal form (eg: 1.1.0.0 is reported as 1100).
	 */
	public long getPluginVersion() {
		return getValueAsLong(getProperty("version"));
	}
	
	private Object getProperty(String propertyName) {
		return wacomJavascriptObject.getMember(propertyName);
	}
	private long getValueAsLong(Object jsvalue) {
		if (jsvalue instanceof Number) {
			return ((Number)jsvalue).longValue();
		}
		return 0;
	}
	private int getValueAsInt(Object jsvalue) {
		if (jsvalue instanceof Number) {
			return ((Number)jsvalue).intValue();
		}
		return 0;
	}
	private float getValueAsFloat(Object jsvalue) {
		if (jsvalue instanceof Number) {
			return ((Number) jsvalue).floatValue();
		}
		return 0;
	}
	private boolean getIntegerAsBoolean(Object jsvalue) {
		return jsvalue != null && jsvalue instanceof Number && ((Number)jsvalue).intValue() != 0;
	}
		
}
