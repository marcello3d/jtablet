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

package cello.jtablet.impl.jpen.platform;

import jpen.provider.NativeLibraryLoader;
import jpen.utils.BuildInfo;
import cello.jtablet.impl.platform.NativeScreenTabletManager;


/**
 * Currently unimplemented shell intended to implement a Linux version of JTablet.
 * @author marcello
 */
public class NativeXInputInterface extends NativeScreenTabletManager {

	private static final NativeLibraryLoader LIB_LOADER=new NativeLibraryLoader(new String[]{""},
			new String[]{"x86_64", "ia64"},
			Integer.valueOf(BuildInfo.getProperties().getString("jpen.provider.xinput.nativeVersion")));

	public boolean isSystemSupported(String os) {
		return os.contains("linux");
	}

	@Override
	protected void start() {
	}

	@Override
	protected void stop() {
	}

	@Override
	protected NativeLibraryLoader getLoader() {
		return LIB_LOADER;
	}
}
