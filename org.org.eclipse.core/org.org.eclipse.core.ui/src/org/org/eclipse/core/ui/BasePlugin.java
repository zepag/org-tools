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
package org.org.eclipse.core.ui;

import java.io.File;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.ILog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.org.eclipse.core.ui.images.PluginImages;
import org.org.eclipse.core.utils.platform.images.Images;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;
import org.org.eclipse.core.utils.platform.tools.logging.PluginLogAppender;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class BasePlugin extends AbstractUIPlugin implements IStartup{
    private static Logger logger = Logger.getLogger(BasePlugin.class);

    //The shared instance.
    private static BasePlugin plugin;

    //Resource bundle.
    private ResourceBundle resourceBundle;

    private Images images;

    /**
     * The constructor.
     */
    public BasePlugin() {
        super();
        plugin = this;
        try {
            resourceBundle = ResourceBundle.getBundle("org.org.eclipse.core.ui.CorePluginResources");
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        ILog log = this.getLog();
        PluginLogAppender.setLog(log);
//        DOMConfigurator.configure(PlatformUtilsPlugin.getFile("log4j.xml").toURI().toURL());
        images = new Images();
        images.addImage(this, "icons/org_error_64.gif", PluginImages.LOGO_ORG_ERROR_64);
        images.addImage(this, "icons/org_info_64.gif", PluginImages.LOGO_ORG_INFO_64);
        images.addImage(this, "icons/org_warning_64.gif", PluginImages.LOGO_ORG_WARNING_64);
        images.addImage(this, "icons/org_16.gif", PluginImages.LOGO_ORG_16);
        images.addImage(this, "icons/org_64.gif", PluginImages.LOGO_ORG_64);
        images.addImage(this, "icons/remove_off.gif", PluginImages.ICON_REMOVE_OFF_16);
        images.addImage(this, "icons/remove_on.gif", PluginImages.ICON_REMOVE_ON_16);
        images.addImage(this, "icons/run_off.gif", PluginImages.ICON_RUN_OFF_16);
        images.addImage(this, "icons/run_on.gif", PluginImages.ICON_RUN_ON_16);
        images.addImage(this, "icons/run_multiple_off.gif", PluginImages.ICON_RUN_MULTIPLE_OFF_16);
        images.addImage(this, "icons/run_multiple_on.gif", PluginImages.ICON_RUN_MULTIPLE_ON_16);
        images.addImage(this, "icons/org_banner.gif", PluginImages.BANNER_ORG);
        logger.debug("starting plugin :" + this.getClass().getName());
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     */
    public static BasePlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = BasePlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public static File getFile(String relativePath) {
        return PluginToolBox.getFileFromBundle(getDefault().getBundle(), relativePath);
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
