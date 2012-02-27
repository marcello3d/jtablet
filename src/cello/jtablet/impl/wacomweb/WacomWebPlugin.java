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
import java.util.EnumMap;

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
    private final JSObject embedObject;
    private JSObject penAPI;
    private EnumMap<Property,String> lastData = new EnumMap<Property, String>(Property.class);

    private static JSObject getWacomPluginFromEmbed(Applet applet, String embedName) throws JSException {
        System.out.println("applet = "+applet);
		JSObject window = JSObject.getWindow(applet);
        System.out.println("window = "+window);
		JSObject document = (JSObject)window.getMember("document");
        System.out.println("document = "+document);
		JSObject embeds = (JSObject)document.getMember("embeds");
        System.out.println("embeds = "+embeds);
		return (JSObject)embeds.getMember(embedName);
	}
	
	/**
	 * Constructs a wacomwebplugin wrapper object by searching for an embed object in the same document as the specified 
	 * Applet.
	 * @param applet applet requesting this plugin
	 * @param embedName the html name associated with the wacom plugin object
	 * @throws JSException if something happened in JavaScript land
	 */
	public WacomWebPlugin(Applet applet, String embedName) throws JSException {
		this(getWacomPluginFromEmbed(applet, embedName));
	}
	
    public enum Property {
            pointerType,
//            isWacom,
//            isEraser,
            pressure,
//            tangentialPressure,
//            posX,
//            posY,
//            rotationRad,
//            tiltX,
//            tiltY,
//            sysX,
//            sysY,
//            tabletModel,
//            tabletModelID,
//            version
    }
    public static final Property[] PROPERTIES = Property.values();
    
	/**
	 * @param embedObject the JSObject
	 */
	public WacomWebPlugin(JSObject embedObject) {
        this.embedObject = embedObject;
        System.out.println("wacom plugin = " + embedObject);
        StringBuilder sb = new StringBuilder();
        sb.append("var api = this.penAPI; this.getAll = function() { return [");
        boolean first = true;
        
        for (Property key : Property.values()) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append("api.").append(key.name());
        }

        sb.append("].join(','); }");
        System.out.println("executed: "+sb);
        embedObject.eval(sb.toString());
                
        this.penAPI = (JSObject) embedObject.getMember("penAPI");
        System.out.println("wacom plugin.penAPI = "+penAPI);
	}

    public EnumMap<Property,String> poll() {
        Object result = this.embedObject.call("getAll", new Object[0]);
        System.out.println("result = "+result);
        if (result instanceof String) {
            String[] values = ((String) result).split(",");
            for (int i = 0; i < values.length; i++) {
                lastData.put(PROPERTIES[i], values[i]);    
            }
            return lastData;
        }
        return null;
    }
    
//	/**
//	 * @return if a Tablet device is attached
//	 */
//	public boolean isAttached() {
//		return getIntegerAsBoolean(getProperty(Property.isWacom));
//	}
//
//	/**
//	 * @return if the last packet came from an eraser device
//	 */
//	public boolean isEraser() {
//		return getIntegerAsBoolean(getProperty(Property.isEraser));
//	}

	/**
	 * @return last packet pressure [0.0 ~ 1.0] 
	 */
	public float getPressure() {
		return getValueAsFloat(getProperty(Property.pressure));
	}
//
//	/**
//	 * @return last packet tangential pressure [0.0 ~ 1.0]
//	 */
//	public float getTangentialPressure() {
//		return getValueAsFloat(getProperty(Property.tangentialPressure));
//	}
//	/**
//	 * @return last packet tablet x coordinate
//	 */
//	public long getPosX() {
//		return getValueAsLong(getProperty(Property.posX));
//	}
//	/**
//	 * @return last packet tablet y coordinate
//	 */
//	public long getPosY() {
//		return getValueAsLong(getProperty(Property.posY));
//	}
//
//	/**
//	 * @return last packet stylus rotation in radians
//	 */
//	public float getRotation() {
//		return getValueAsFloat(getProperty(Property.rotationRad));
//	}
//	/**
//	 * @return last packet horizontal tilt [-1.0 ~ 1.0]
//	 */
//	public float getTiltX() {
//		return getValueAsFloat(getProperty(Property.tiltX));
//	}
//	/**
//	 * @return last packet vertical tilt [-1.0 ~ 1.0]
//	 */
//	public float getTiltY() {
//		return getValueAsFloat(getProperty(Property.tiltY));
//	}
//	/**
//	 * @return last packet X position in pixel coordinates (to sub-pixel resolution)
//	 */
//	public float getSysX() {
//		return getValueAsFloat(getProperty(Property.sysX));
//	}
//	/**
//     * @return last packet Y position in pixel coordinates (to sub-pixel resolution)
//	 */
//	public float getSysY() {
//		return getValueAsFloat(getProperty(Property.sysY));
//	}
//
//    public String getTabletModel() {
//        return (String)getProperty(Property.tabletModel);
//    }
//    public String getTabletModelId() {
//        return (String)getProperty(Property.tabletModelID);
//    }
//
	/**
	 * @return last packet device type
	 */
	public TabletDevice.Type getPointerType() {
		switch (getValueAsInt(getProperty(Property.pointerType))) {
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
	
//	/**
//	 * @return The version of the plugin in decimal form (eg: 1.1.0.0 is reported as 1100).
//	 */
//	public long getPluginVersion() {
//		return getValueAsLong(getProperty(Property.version));
//	}
	
	private Object getProperty(Property property) {
        if (lastData.containsKey(property)) {
            return lastData.get(property);
        }
        return penAPI.getMember(property.name());
	}
	private long getValueAsLong(Object jsvalue) {
        if (jsvalue instanceof String) {
            return Long.parseLong((String) jsvalue);
        }
		if (jsvalue instanceof Number) {
			return ((Number)jsvalue).longValue();
		}
		return 0;
	}
	private int getValueAsInt(Object jsvalue) {
        if (jsvalue instanceof String) {
            return Integer.parseInt((String) jsvalue);
        }
		if (jsvalue instanceof Number) {
			return ((Number)jsvalue).intValue();
		}
		return 0;
	}
	private float getValueAsFloat(Object jsvalue) {
        if (jsvalue instanceof String) {
            return Float.parseFloat((String) jsvalue);
        }
		if (jsvalue instanceof Number) {
			return ((Number) jsvalue).floatValue();
		}
		return 0;
	}
	private boolean getIntegerAsBoolean(Object jsvalue) {
        if (jsvalue instanceof String) {
            return !jsvalue.equals("0");
        }
        if (jsvalue instanceof Number) {
            return ((Number)jsvalue).intValue() != 0;
        }
		return false;
	}
		
}
