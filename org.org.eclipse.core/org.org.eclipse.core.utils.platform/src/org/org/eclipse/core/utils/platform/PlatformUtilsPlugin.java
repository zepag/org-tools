/*******************************************************************************
 * Copyright (c) 2008 Pierre-Antoine Grégoire.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pierre-Antoine Grégoire - initial API and implementation
 *******************************************************************************/
package org.org.eclipse.core.utils.platform;

import java.io.File;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.ILog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.org.eclipse.core.utils.platform.images.Images;
import org.org.eclipse.core.utils.platform.images.PluginImages;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;
import org.org.eclipse.core.utils.platform.tools.logging.PluginLogAppender;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The main plugin class to be used in the desktop.
 */
public class PlatformUtilsPlugin extends AbstractUIPlugin implements IStartup {
	public static final String PLUGIN_ID = PlatformUtilsPlugin.class.getName();

	private static Logger logger = Logger.getLogger(PlatformUtilsPlugin.class);

	// The shared instance.
	private static PlatformUtilsPlugin plugin;

	// Resource bundle.
	private ResourceBundle resourceBundle;

	private Images images;

	private ServiceTracker<IProxyService, IProxyService> proxyServiceTracker;

	/**
	 * The constructor.
	 */
	public PlatformUtilsPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle
					.getBundle("org.org.eclipse.core.utils.platform.CorePluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		this.proxyServiceTracker = new ServiceTracker<IProxyService, IProxyService>(
				context, IProxyService.class.getName(), null);
		this.proxyServiceTracker.open();
		ILog log = this.getLog();
		PluginLogAppender.setLog(log);
		// DOMConfigurator.configure(PlatformUtilsPlugin.getFile("log4j.xml").toURI().toURL());
		images = new Images();
		images.addImage(this, "icons/basic_error_64.gif",
				PluginImages.LOGO_BASIC_ERROR_64);
		images.addImage(this, "icons/basic_info_64.gif",
				PluginImages.LOGO_BASIC_INFO_64);
		images.addImage(this, "icons/basic_warning_64.gif",
				PluginImages.LOGO_BASIC_WARNING_64);
		images.addImage(this, "icons/small_error_16.gif",
				PluginImages.SMALL_ERROR_16);
		images.addImage(this, "icons/small_warning_16.gif",
				PluginImages.SMALL_WARNING_16);
		images.addImage(this, "icons/small_ok_16.gif", PluginImages.SMALL_OK_16);
		images.addImage(this, "icons/remove_on.gif",
				PluginImages.ICON_REMOVE_ON_16);
		logger.debug("starting plugin :" + this.getClass().getName());
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		this.proxyServiceTracker.close();
	}

	/**
	 * Returns the shared instance.
	 */
	public static PlatformUtilsPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = PlatformUtilsPlugin.getDefault()
				.getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	public static File getFile(String relativePath) {
		return PluginToolBox.getFileFromBundle(getDefault().getBundle(),
				relativePath);
	}

	public IProxyService getProxyService() {
		return this.proxyServiceTracker.getService();
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public static Shell getActiveShell() {
		if (plugin == null)
			return null;
		IWorkbench workBench = plugin.getWorkbench();
		if (workBench == null)
			return null;
		IWorkbenchWindow workBenchWindow = workBench.getActiveWorkbenchWindow();
		if (workBenchWindow == null)
			return null;
		return workBenchWindow.getShell();
	}

	public Images getImages() {
		return images;
	}

	public void earlyStartup() {

	}
}
