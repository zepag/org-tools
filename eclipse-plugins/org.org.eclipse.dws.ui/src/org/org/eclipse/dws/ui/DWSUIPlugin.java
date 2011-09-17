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
package org.org.eclipse.dws.ui;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IFilter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.org.eclipse.core.utils.platform.images.Images;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;
import org.org.eclipse.core.utils.platform.tools.logging.PluginLogAppender;
import org.org.eclipse.dws.ui.internal.images.PluginImages;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class DWSUIPlugin extends AbstractUIPlugin implements IExtensionChangeHandler {

	/** The logger. */
	private static Logger logger = Logger.getLogger(DWSUIPlugin.class);

	/** The Constant PI_MAVEN2. */
	public static final String PI_MAVEN2 = "org.org.eclipse.dws.ui.DWSUIPlugin";

	/** Logger for this class. */

	// The shared instance.
	private static DWSUIPlugin plugin;

	/** The images. */
	private Images images;

	private ExtensionTracker extensionTracker;

	/**
	 * The constructor.
	 */
	public DWSUIPlugin() {
		super();
		if (plugin == null) {
			plugin = this;
		}
	}

	/**
	 * This method is called upon plug-in activation.
	 * 
	 * @param context
	 *            the context
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void start(BundleContext context) throws Exception {
		super.start(context);
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IFilter filter = createExtensionPointFilter();
		extensionTracker = new ExtensionTracker(reg);
		extensionTracker.registerHandler(this, filter);

		ILog log = this.getLog();
		PluginLogAppender.setLog(log);
		// DOMConfigurator.configure(PlatformUtilsPlugin.getFile("log4j.xml").toURI().toURL());
		images = new Images();
		images.addImage(this, "icons/librarypack.gif", PluginImages.LOGO_LIBRARY_PACKAGE_16);
		images.addImage(this, "icons/http_repository.gif", PluginImages.LOGO_MAVEN_HTTP_REPOSITORY_16);
		images.addImage(this, "icons/filesystem_repository.gif", PluginImages.LOGO_MAVEN_FILESYSTEM_REPOSITORY_16);
		images.addImage(this, "icons/group.gif", PluginImages.LOGO_MAVEN_GROUP_16);
		images.addImage(this, "icons/artifact.gif", PluginImages.LOGO_MAVEN_ARTIFACT_16);
		images.addImage(this, "icons/artifactversion.gif", PluginImages.LOGO_MAVEN_ARTIFACTVERSION_16);
		images.addImage(this, "icons/artifactversionerror.gif", PluginImages.LOGO_MAVEN_ARTIFACTVERSION_ERROR_16);
		images.addImage(this, "icons/artifactversionconflict.gif", PluginImages.LOGO_MAVEN_ARTIFACTVERSION_CONFLICT_16);
		images.addImage(this, "icons/download.gif", PluginImages.LOGO_MAVEN_DOWNLOAD_16);
		images.addImage(this, "icons/download_to_local.gif", PluginImages.LOGO_MAVEN_DOWNLOAD_TO_LOCAL_16);
		images.addImage(this, "icons/synchronize.gif", PluginImages.LOGO_MAVEN_SYNCHRONIZE_16);
		images.addImage(this, "icons/import.gif", PluginImages.LOGO_MAVEN_IMPORT_16);
		images.addImage(this, "icons/importfromurl.gif", PluginImages.LOGO_MAVEN_IMPORT_URL_16);
		images.addImage(this, "icons/export.gif", PluginImages.LOGO_MAVEN_EXPORT_16);
		images.addImage(this, "icons/refresh.gif", PluginImages.LOGO_MAVEN_REFRESH_16);
		images.addImage(this, "icons/add_http.gif", PluginImages.LOGO_MAVEN_ADD_HTTP_16);
		images.addImage(this, "icons/add_filesystem.gif", PluginImages.LOGO_MAVEN_ADD_FILESYSTEM_16);
		images.addImage(this, "icons/remove.gif", PluginImages.LOGO_MAVEN_REMOVE_16);
		images.addImage(this, "icons/edit.gif", PluginImages.LOGO_MAVEN_EDIT_16);
		images.addImage(this, "icons/jar_src_obj.gif", PluginImages.LOGO_MAVEN_ARTIFACT_VERSION_WITH_SOURCES);
		images.addImage(this, "icons/jar_obj.gif", PluginImages.LOGO_MAVEN_ARTIFACT_VERSION_LIBRARY_TYPE);
		images.addImage(this, PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT).createImage(), PluginImages.LOGO_MAVEN_ARTIFACT_VERSION_ARCHIVE_TYPE);

		logger.debug("starting plugin :" + this.getClass().getName());
	}

	private IFilter createExtensionPointFilter(IExtensionPoint... extensionPoints) {
		IFilter filter = ExtensionTracker.createExtensionPointFilter(extensionPoints);
		return filter;
	}

	/**
	 * This method is called when the plug-in is stopped.
	 * 
	 * @param context
	 *            the context
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the default
	 */
	public static DWSUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns a file from a path relative to the plugin's installation path.
	 * 
	 * @param relativePath
	 *            the relative path from plugin's installation dir's root.
	 * 
	 * @return a java.io.File object.
	 */
	public static File getFile(String relativePath) {
		return PluginToolBox.getFileFromBundle(DWSUIPlugin.getDefault().getBundle(), relativePath);
	}

	/**
	 * Gets the images.
	 * 
	 * @return the images
	 */
	public Images getImages() {
		return images;
	}

	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		// String extensionPointUniqueId = extension.getExtensionPointUniqueIdentifier();
	}

	// private String getUniqueId(IExtension extension) {
	// return extension.getUniqueIdentifier() == null ? extension.getSimpleIdentifier() : extension.getUniqueIdentifier();
	// }

	public void removeExtension(IExtension extension, Object[] objects) {
		// String extensionPointUniqueId = extension.getExtensionPointUniqueIdentifier();
	}
}
