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
package org.org.eclipse.dws.ui.internal.views;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.org.eclipse.dws.ui.DWSUIPlugin;
import org.org.eclipse.dws.ui.internal.images.PluginImages;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.ArtifactVersion.Type;

/**
 * The Class MavenRepositoriesViewLabelDecorator.
 */
public class LibraryPacksViewLabelDecorator implements ILabelDecorator {

	/**
	 * Instantiates a new maven repositories view label decorator.
	 */
	public LibraryPacksViewLabelDecorator() {
		super();
	}

	/**
	 * Decorate image.
	 * 
	 * @param image
	 *            the image
	 * @param element
	 *            the element
	 * 
	 * @return the image
	 * 
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
	 */
	public Image decorateImage(Image image, Object element) {
		Image result = image;
		if (element instanceof ArtifactVersion) {
			ArtifactVersion artifactVersion = (ArtifactVersion) element;
			if (artifactVersion.getSourcesUrl() != null) {
				result = DWSUIPlugin.getDefault().getImages().getImage(PluginImages.LOGO_MAVEN_ARTIFACT_VERSION_WITH_SOURCES);
			} else {
				if (artifactVersion.getType() == Type.LIBRARY) {
					result = DWSUIPlugin.getDefault().getImages().getImage(PluginImages.LOGO_MAVEN_ARTIFACT_VERSION_LIBRARY_TYPE);
				} else if (artifactVersion.getType() == Type.ARCHIVE) {
					result = DWSUIPlugin.getDefault().getImages().getImage(PluginImages.LOGO_MAVEN_ARTIFACT_VERSION_ARCHIVE_TYPE);
				}
			}
		}
		return result;
	}

	/**
	 * Decorate text.
	 * 
	 * @param text
	 *            the text
	 * @param element
	 *            the element
	 * 
	 * @return the string
	 * 
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
	 */
	public String decorateText(String text, Object element) {
		return text;
	}

	/**
	 * Adds the listener.
	 * 
	 * @param listener
	 *            the listener
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/**
	 * Dispose.
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * Checks if is label property.
	 * 
	 * @param element
	 *            the element
	 * @param property
	 *            the property
	 * 
	 * @return true, if checks if is label property
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	/**
	 * Removes the listener.
	 * 
	 * @param listener
	 *            the listener
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

}
