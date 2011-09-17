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
package org.org.eclipse.core.utils.platform.images;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;

public class Images {
	private static Logger logger = Logger.getLogger(Images.class);

	/** A table of all the <code>ImageDescriptor</code>s. */
	private Map<String, ImageDescriptor> imageDescriptors;

	/** The image registry containing <code>Image</code>s. */
	private ImageRegistry imageRegistry;

	/**
	 * Returns the <code>Image<code> identified by the given key,
	 * or <code>null</code> if it does not exist.
	 */
	public Image getImage(String key) {
		return getImageRegistry().get(key);
	}

	/**
	 * Returns the <code>Image<code> identified by the given key,
	 * or <code>null</code> if it does not exist.
	 */
	public ImageDescriptor getImageDescriptor(String key) {
		return imageDescriptors.get(key);
	}

	/*
	 * Helper method to access the image registry from the JavaPlugin class.
	 */
	private ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
			for (String key:imageDescriptors.keySet()) {
				imageRegistry.put(key, imageDescriptors.get(key).createImage());
			}
		}
		return imageRegistry;
	}

	public ImageDescriptor addImage(AbstractUIPlugin plugin, String path, String key) {
		try {
			URL baseURL = PluginToolBox.getPluginInstallationURL(plugin);
			ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(baseURL, path));
			if (imageDescriptors == null) {
				imageDescriptors = new HashMap<String, ImageDescriptor>();
			}
			imageDescriptors.put(key, result);
			if (imageRegistry != null) {
				logger.debug("Image registry already defined.");
			}
			return result;
		} catch (MalformedURLException e) {
			logger.error("URL is malformed for image : " + path, e);
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	public ImageDescriptor addImage(AbstractUIPlugin plugin, Image image, String key) {
		ImageDescriptor result = ImageDescriptor.createFromImage(image);
		if (imageDescriptors == null) {
			imageDescriptors = new HashMap<String, ImageDescriptor>();
		}
		imageDescriptors.put(key, result);
		if (imageRegistry != null) {
			logger.debug("Image registry already defined.");
		}
		return result;

	}

	private URL makeIconFileURL(URL baseURL, String name) throws MalformedURLException {
		return new URL(baseURL, name);
	}
}