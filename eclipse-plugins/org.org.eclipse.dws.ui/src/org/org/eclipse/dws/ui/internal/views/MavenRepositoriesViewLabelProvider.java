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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.org.eclipse.dws.ui.DWSUIPlugin;
import org.org.eclipse.dws.ui.internal.images.PluginImages;
import org.org.repository.crawler.items.IFileSystemCrawledRepositorySetup;
import org.org.repository.crawler.items.IHttpCrawledRepositorySetup;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.CrawledRepository;
import org.org.repository.crawler.maven2.model.Group;

/**
 * The Class MavenRepositoriesViewLabelProvider.
 */
public class MavenRepositoriesViewLabelProvider extends LabelProvider {

	/**
	 * Gets the text.
	 * 
	 * @param obj
	 *            the obj
	 * 
	 * @return the text
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object obj) {
		String text = null;
		if (obj instanceof CrawledRepository) {
			CrawledRepository crawledRepository = (CrawledRepository) obj;
			if (crawledRepository.getRepositorySetup() instanceof IHttpCrawledRepositorySetup) {
				text = (crawledRepository.getLabel() + " (" + ((IHttpCrawledRepositorySetup) crawledRepository.getRepositorySetup()).getBaseUrl() + ")");
			} else if (((CrawledRepository) obj).getRepositorySetup() instanceof IFileSystemCrawledRepositorySetup) {
				text = (crawledRepository.getLabel() + " (" + ((IFileSystemCrawledRepositorySetup) crawledRepository.getRepositorySetup()).getBasePath() + ")");
			}
			crawledRepository = null;
		} else if (obj instanceof Group) {
			text = ((Group) obj).getName();
		} else if (obj instanceof Artifact) {
			text = ((Artifact) obj).getId();
		} else if (obj instanceof ArtifactVersion) {
			text = ((ArtifactVersion) obj).getId();
		} else {
			text = obj.toString();
		}
		return text;
	}

	/**
	 * Gets the image.
	 * 
	 * @param obj
	 *            the obj
	 * 
	 * @return the image
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object obj) {
		Image image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		if (obj instanceof CrawledRepository) {
			CrawledRepository crawledRepository = (CrawledRepository) obj;
			if (crawledRepository.getRepositorySetup() instanceof IHttpCrawledRepositorySetup) {
				image = DWSUIPlugin.getDefault().getImages().getImage(PluginImages.LOGO_MAVEN_HTTP_REPOSITORY_16);
			} else if (((CrawledRepository) obj).getRepositorySetup() instanceof IFileSystemCrawledRepositorySetup) {
				image = DWSUIPlugin.getDefault().getImages().getImage(PluginImages.LOGO_MAVEN_FILESYSTEM_REPOSITORY_16);
			}
			crawledRepository = null;
		} else if (obj instanceof Group) {
			image = DWSUIPlugin.getDefault().getImages().getImage(PluginImages.LOGO_MAVEN_GROUP_16);
		} else if (obj instanceof Artifact) {
			image = DWSUIPlugin.getDefault().getImages().getImage(PluginImages.LOGO_MAVEN_ARTIFACT_16);
		} else if (obj instanceof ArtifactVersion) {
			image = DWSUIPlugin.getDefault().getImages().getImage(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_16);
		}
		return image;
	}
}