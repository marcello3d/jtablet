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




/**
 * Classes implementing {@link NativeTabletManager} make use of native
 * tablet drivers or libraries. This allows the full use of devices
 * which are unsupported or only partially supported by Java.
 * 
 * @author marcello
 */
public interface NativeTabletManager {

	/**
	 * Loads the actual native driver
	 * @throws NativeLoaderException 
	 */
	public void load() throws NativeLoaderException;
	
	/**
	 * @param os the os string
	 * @return true if the given os is supported by this native device
	 */
	public boolean isSystemSupported(String os);

	/**
	 * @return the architecture type desired (used by Windows 64bit)
	 */
	public Architecture getArchitecture();
}
