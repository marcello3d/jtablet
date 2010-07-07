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

package cello.jtablet.impl;

import cello.jtablet.TabletDevice;

/**
 * This class describes a mouse-like tablet device. Actually, to be
 * more precise, it describes the mouse device exposed through Java.
 *
 * @author marcello
 * @since 1.2.5
 */
public class MouseDevice extends TabletDevice {
	
	/**
	 * Default system mouse input device
	 */
	public static final TabletDevice INSTANCE = new MouseDevice();
	
	
	@Override
	public Type getType() {
		return Type.MOUSE;
	}
	
	@Override 
	public Support getPressureSupport() {
		return Support.NO;
	}
	
	@Override
	public Support getTiltSupport() {
		return Support.NO;
	}
	
	@Override
	public Support getSidePressureSupport() {
		return Support.NO;
	}
	
	@Override
	public Support getRotationSupport() {
		return Support.NO;
	}
	
	@Override
	public Support getButtonSupport() {
		return Support.YES;
	}
	
	@Override
	public Support getUniqueIdSupport() {
		return Support.NO;
	}
	
	@Override
	public Support getFloatSupport() {
		return Support.NO;
	}
}

