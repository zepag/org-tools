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
package org.org.eclipse.cheatsheet.catalog;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IFilter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.org.eclipse.cheatsheet.catalog.internal.images.PluginImages;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalogReference;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalogReferenceType;
import org.org.eclipse.cheatsheet.catalog.internal.model.ModelConstants;
import org.org.eclipse.cheatsheet.catalog.internal.xml.IXmlCatalogBinder;
import org.org.eclipse.cheatsheet.catalog.internal.xml.StaxXmlCatalogBinder;
import org.org.eclipse.core.utils.platform.images.Images;
import org.org.eclipse.core.utils.platform.tools.IOToolBox;
import org.org.model.RootModelItem;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CheatSheetCatalogPlugin extends AbstractUIPlugin implements IExtensionChangeHandler {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.org.eclipse.cheatsheet.catalog";

	// The shared instance
	private static CheatSheetCatalogPlugin plugin;
	private Images images;


	private ExtensionTracker catalogExtensionTracker;

	public CheatSheetCatalogPlugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint ep = reg.getExtensionPoint("org.org.eclipse.cheatsheet.catalog.CheatSheetCatalogContribution");
		catalogExtensionTracker = new ExtensionTracker(reg);
		IFilter filter = ExtensionTracker.createExtensionPointFilter(ep);
		catalogExtensionTracker.registerHandler(this, filter);
		IExtension[] extensions = ep.getExtensions();
		for (int i = 0; i < extensions.length; ++i)
			addExtension(catalogExtensionTracker, extensions[i]);

		plugin = this;
		images = new Images();
		images.addImage(this, "icons/cheatsheetreference.gif", PluginImages.CHEATSHEET_REFERENCE);
		images.addImage(this, "icons/cheatsheetcategory.gif", PluginImages.CHEATSHEET_CATEGORY);
		images.addImage(this, "icons/cheatsheetcatalog.gif", PluginImages.CHEATSHEET_CATALOG);
		images.addImage(this, "icons/addcheatsheetreference.gif", PluginImages.ADD_CHEATSHEET_REFERENCE);
		images.addImage(this, "icons/addcheatsheetcategory.gif", PluginImages.ADD_CHEATSHEET_CATEGORY);
		images.addImage(this, "icons/addcheatsheetcatalog.gif", PluginImages.ADD_CHEATSHEET_CATALOG);
		images.addImage(this, "icons/duplicate.gif", PluginImages.DUPLICATE);
		images.addImage(this, "icons/export.gif", PluginImages.EXPORT);
		images.addImage(this, "icons/import.gif", PluginImages.IMPORT);
		images.addImage(this, "icons/delete.gif", PluginImages.DELETE);
		images.addImage(this, "icons/refresh.gif", PluginImages.REFRESH);
		images.addImage(this, "icons/importfromurl.gif", PluginImages.IMPORT_FROM_URL);
		images.addImage(this, "icons/readonlycheatsheetcatalog.gif", PluginImages.READONLY_CHEATSHEET_CATALOG);
		images.addImage(this, "icons/opencheatsheetreference.gif", PluginImages.OPEN_CHEATSHEET_REFERENCE);
		images.addImage(this, "icons/editcheatsheetcatalog.gif", PluginImages.EDIT_CHEATSHEET_CATALOG);
		images.addImage(this, "icons/editcheatsheetcategory.gif", PluginImages.EDIT_CHEATSHEET_CATEGORY);
		images.addImage(this, "icons/editcheatsheetreference.gif", PluginImages.EDIT_CHEATSHEET_REFERENCE);
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		if (catalogExtensionTracker != null) {
			catalogExtensionTracker.close();
			catalogExtensionTracker = null;
		}

	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static CheatSheetCatalogPlugin getDefault() {
		return plugin;
	}

	public static ImageDescriptor getPluginImageDescriptor(String key) {
		return getDefault().getImages().getImageDescriptor(key);
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public Images getImages() {
		return images;
	}

	public void addExtension(IExtensionTracker extensionTracker, IExtension extension) {
		IConfigurationElement[] configs = extension.getConfigurationElements();
		IXmlCatalogBinder catalogBinder = new StaxXmlCatalogBinder();
		for (int i = 0; i < configs.length; ++i) {
			IConfigurationElement member = configs[i];
			Bundle contributor = Platform.getBundle(member.getContributor().getName());
			// Get the label of the extender plugin and the ID of the extension.
			String pluginLabel = (String) contributor.getHeaders().get(org.osgi.framework.Constants.BUNDLE_NAME);
			if (pluginLabel == null) {
				pluginLabel = "[unnamed plugin]";
			}
			try {
				InputStream inputStream = null;
				CheatSheetCatalogReferenceType cheatSheetCatalogReferenceType = null;
				String uri = null;
				if (member.getName().equals("catalog-file")) {
					String file = member.getAttribute("xml-file");
					URL url = contributor.getResource(file);
					inputStream = url.openStream();
					cheatSheetCatalogReferenceType = CheatSheetCatalogReferenceType.BUNDLE;
					uri = url.toExternalForm();
				}
				if (member.getName().equals("catalog-url")) {
					String urlString = member.getAttribute("url");
					URL url = new URL(urlString);
					Proxy proxy = IOToolBox.determineProxy(url);
					inputStream = url.openConnection(proxy).getInputStream();
					cheatSheetCatalogReferenceType = CheatSheetCatalogReferenceType.HTTP;
					uri = urlString;
				}
				CheatSheetCatalog catalog = (CheatSheetCatalog) catalogBinder.parseXmlCatalog(inputStream);
				if (cheatSheetCatalogReferenceType != null) {
					catalog.setReference(new CheatSheetCatalogReference(cheatSheetCatalogReferenceType, uri));
				}
				RootModelItem.<CheatSheetCatalog> getInstance(ModelConstants.ROOT_MODEL_ITEM_ID).addChild(catalog);
				extensionTracker.registerObject(extension, catalog, IExtensionTracker.REF_WEAK);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}

	}

	public void removeExtension(IExtension extension, Object[] objects) {
		for (int i = 0; i < objects.length; ++i) {
			CheatSheetCatalog catalog = (CheatSheetCatalog) objects[i];
			if (RootModelItem.<CheatSheetCatalog> getInstance(ModelConstants.ROOT_MODEL_ITEM_ID).hasChild(catalog.getUID())) {
				RootModelItem.<CheatSheetCatalog> getInstance(ModelConstants.ROOT_MODEL_ITEM_ID).removeChild(catalog.getUID());
			}
		}
	}
}
