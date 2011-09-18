/*
 org.org.lib.repository.crawler is a java library/OSGI Bundle
 Providing Crawling capabilities for Maven 2 HTTP exposed repositories
 Copyright (C) 2007  Pierre-Antoine Grégoire
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.org.repository.crawler.maven2.model.protocolplugins;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Queue;

import org.org.repository.crawler.items.ICrawledRepositorySetup;
import org.org.repository.crawler.mapping.Entry;
import org.org.repository.crawler.maven2.model.ArtifactVersion;

public interface IRepositoryBrowserPlugin<T extends ICrawledRepositorySetup> {
	public abstract void init(T repositorySetup);

	public abstract void checkRepositorySetup(T repositorySetup);

	public abstract Queue<Entry> getEntryList(String requestedUrl) throws IOException;

	public abstract String buildUrl(T repositorySetup, String upperGroupName, String folderName);

	public abstract String cleanFolderName(String folderName);

	public abstract void setUrlForArtifactVersion(ArtifactVersion artifactVersion, String requestedUrl, String version) throws MalformedURLException;
}