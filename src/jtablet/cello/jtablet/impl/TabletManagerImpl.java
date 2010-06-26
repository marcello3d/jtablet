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

import java.awt.Component;
import java.awt.Font;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;

import cello.jtablet.DriverStatus;
import cello.jtablet.TabletManager;
import cello.jtablet.event.TabletListener;
import cello.jtablet.impl.JTabletUpdateChecker.UpdateListener;
import cello.jtablet.impl.jpen.CocoaTabletManager;
import cello.jtablet.impl.jpen.WinTabTabletManager;
import cello.jtablet.impl.jpen.XInputTabletManager;
import cello.jtablet.installer.BrowserLauncher;
import cello.jtablet.installer.JTabletExtension;

/**
 * {@code TabletManagerImpl} is a {@link cello.jtablet.TabletManager}
 * which is capable of automatically determining the best concrete
 * manager to be used.
 * 
 * @author marcello
 */
public class TabletManagerImpl extends TabletManager {

	private final TabletManager tabletManager;
	private final DriverStatus tabletStatus; 
	
	/**
	 */
	public TabletManagerImpl() {
		if (PluginConstant.IS_PLUGIN) {
			
			showAlphaMessage();
			
			String os = System.getProperty("os.name").toLowerCase();
			DriverStatus tabletStatus = new DriverStatus(DriverStatus.State.UNSUPPORTED_OS);
			TabletManager chosenManager = null;
			final NativeLoader loader = new NativeLoader();
			
			Class<?> interfaces[] = {
				WinTabTabletManager.class,
				CocoaTabletManager.class,
				XInputTabletManager.class,
				ScreenMouseTabletManager.class, // supports screen listeners but requires extra security permissions
				MouseTabletManager.class
			};
			for (Class<?> cdClazz : interfaces) {
				try {
					TabletManager manager = (TabletManager)cdClazz.newInstance();
					if (manager instanceof NativeTabletManager) {
						final NativeTabletManager nsd = (NativeTabletManager)manager;
						if (nsd.isSystemSupported(os)) {
							try {
								final Architecture arch = nsd.getArchitecture();
	
								NativeLoaderException e = AccessController.doPrivileged(new PrivilegedAction<NativeLoaderException>() {
						            public NativeLoaderException run() {
						            	try {
						            		loader.load(arch);
										} catch (NativeLoaderException e) {
											return e;
										}
						            	return null;
						            }
						        });
								if (e != null) {
									throw e;
								}
			            		nsd.load();
								chosenManager = manager;
								tabletStatus = new DriverStatus(DriverStatus.State.LOADED);
								break;
							} catch (SecurityException e) {
								tabletStatus = new DriverStatus(DriverStatus.State.UNEXPECTED_EXCEPTION, e);
							} catch (UnsatisfiedLinkError e) {
								tabletStatus = new DriverStatus(DriverStatus.State.NATIVE_EXCEPTION, e);
							} catch (NativeLoaderException e) {
								tabletStatus = new DriverStatus(DriverStatus.State.NATIVE_EXCEPTION, e);
							}
						}
					} else {
						chosenManager = manager;
						break;
					}
				} catch (Throwable t) {
					tabletStatus = new DriverStatus(DriverStatus.State.UNEXPECTED_EXCEPTION, t);
				}
			}
			this.tabletStatus = tabletStatus;
			this.tabletManager = chosenManager;
		} else {
			this.tabletStatus = new DriverStatus(DriverStatus.State.NOT_INSTALLED);
			this.tabletManager = new MouseTabletManager();
		}
	}

	private void showAlphaMessage() {

		final String installedVersion = JTabletExtension.getInstalledVersion();
		String installedVersionTitle;
		if (installedVersion == null) {
			installedVersionTitle = "JTablet (development build)";
		} else {
			installedVersionTitle = "JTablet "+installedVersion;
		}
		
		final String messageHtml = 
			"Loading "+installedVersionTitle+"...<br>\n<br>\n"+
			"This is an <b>EXPERIMENTAL</b> alpha release that <i>may crash!</i><br>\n"+
			"Please use at your own risk!<br>\n<br>\n"+
			"Visit our website to get new versions and report problems: <br>\n"+ 
			"<a href=\"http://jtablet.cellosoft.com/\">http://jtablet.cellosoft.com/</a><br>\n<br>\n";
		
		// All this work to display a link in the message... might as well throw some other stylings in there, too.
		final JEditorPane richText = new JEditorPane("text/html", messageHtml + "Checking for updates...");
		
		// Don't edit, let the color show throw, don't allow text selection
		richText.setEditable(false);
		richText.setOpaque(false);
		richText.setHighlighter(null);
		
		JTabletUpdateChecker checker = new JTabletUpdateChecker(installedVersion, new UpdateListener() {
			public void newGoodStatus(String message) {
				richText.setText(messageHtml+message);
			}
			public void newBadStatus(String message) {
				newGoodStatus("<font color=\"#ff0000\">"+message+"</font>");
			}
		});
		checker.start();
		
        // Set default font...
        Font font = UIManager.getFont("Label.font");
        String bodyRule = "body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument)richText.getDocument()).getStyleSheet().addRule(bodyRule);

        // Make links clickable...
		richText.addHyperlinkListener(new HyperlinkListener() {  
			public void hyperlinkUpdate(HyperlinkEvent event) {  
				if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())) {  
					try {
						BrowserLauncher.browse(event.getURL().toURI());
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}  
				}  
			}  
		});  
		
		// Show the message...
		JOptionPane.showMessageDialog(
				null, 
				richText,
				"JTablet 2",
				JOptionPane.INFORMATION_MESSAGE);
		
		// Attempt to stop update thread if it hasn't already...
		checker.stop();
	}

	@Override
	public DriverStatus getDriverStatus() {
		return tabletStatus;
	}
	
	public void addScreenTabletListener(TabletListener l) {
		tabletManager.addScreenTabletListener(l);
	}
	public void removeScreenTabletListener(TabletListener l) {
		tabletManager.addScreenTabletListener(l);
	}

	public void addTabletListener(Component c, TabletListener l) {
		tabletManager.addTabletListener(c,l);
	}
	public void removeTabletListener(Component c, TabletListener l) {
		tabletManager.removeTabletListener(c,l);
	}
	
}
