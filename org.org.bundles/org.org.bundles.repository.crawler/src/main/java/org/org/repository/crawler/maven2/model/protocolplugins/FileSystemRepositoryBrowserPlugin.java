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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.org.repository.crawler.RepositoryCrawlingException;
import org.org.repository.crawler.items.IFileSystemCrawledRepositorySetup;
import org.org.repository.crawler.mapping.Entry;
import org.org.repository.crawler.mapping.Entry.MavenType;
import org.org.repository.crawler.mapping.Entry.RawType;
import org.org.repository.crawler.maven2.RepositoryCrawlerService;
import org.org.repository.crawler.maven2.model.ArtifactVersion;

/**
 * @author pagregoire
 */
public class FileSystemRepositoryBrowserPlugin implements IRepositoryBrowserPlugin<IFileSystemCrawledRepositorySetup> {

	/**
	 * init this class with the repository info
	 * 
	 * @param httpBrowsedRepository
	 */
	public void init(IFileSystemCrawledRepositorySetup repository) {
		// does nothing for now...
	}

	/**
	 * Checks the repository's validity
	 * 
	 * @param repository
	 * @return
	 */
	public void checkRepositorySetup(IFileSystemCrawledRepositorySetup repositorySetup) {
		if (repositorySetup.getBasePath() == null) {
			throw new RepositoryCrawlingException("A base path should be defined for the repository.");
		}
	}

	/**
	 * @param repository
	 * @param upperGroupName
	 * @param folderName
	 * @return
	 */
	public String buildUrl(IFileSystemCrawledRepositorySetup repositorySetup, String upperGroupName, String folderName) {
		StringBuffer buffer = new StringBuffer(repositorySetup.getBasePath());
		buffer.append((repositorySetup.getBasePath().endsWith("/") ? "" : "/"));
		if (!upperGroupName.equals("")) {
			buffer.append(upperGroupName.replace('.', '/'));
		}
		if (!folderName.equals("")) {
			buffer.append("/" + (folderName.endsWith("/") ? folderName : folderName + "/"));
		}
		return buffer.toString();
	}

	/**
	 * @param requestedUrl
	 * @return
	 * @throws IOException
	 */
	public Queue<Entry> getEntryList(String requestedUrl) throws IOException {
		File file = new File(requestedUrl);
		Queue<Entry> entryList = new LinkedBlockingQueue<Entry>();
		if (file.exists()) {
			List<String> files = new ArrayList<String>();
			if (file.isDirectory()) {
				files = Arrays.asList(file.list());
			}
			File nextFile = null;
			for (String entryName : files) {
				Entry entry = new Entry();
				nextFile = new File(file, entryName);
				entry.setValue(nextFile.getAbsolutePath());
				entry.setResolvedName(entryName);
				if (nextFile.isDirectory()) {
					entry.setRawType(RawType.DIRECTORY);
				}
				if (entry.isRawType(RawType.DIRECTORY)) {
					if (RepositoryCrawlerService.ARTIFACT_VERSION_FOLDER_PATTERN.matcher(entry.getResolvedName()).matches()) {
						entry.setMavenType(MavenType.ARTIFACT_VERSION_FOLDER);
					}
				}
				if (nextFile.isFile()) {
					entry.setRawType(RawType.FILE);
				}
				if (entry.isRawType(RawType.FILE)) {
					if (entry.getResolvedName().startsWith("maven-metadata")) {
						entry.setMavenType(MavenType.METADATA_FILE);
					}
				}
				entryList.add(entry);
			}
		}
		return entryList;
	}

	/**
	 * @see org.org.repository.crawler.maven2.RepositoryCrawlerService#cleanFolderName(java.lang.String)
	 */
	public String cleanFolderName(String folderName) {
		return folderName;
	}

	/**
	 * @see org.org.repository.crawler.maven2.RepositoryCrawlerService#setUrlToArtifactVersion(org.org.maven2.crawler.items.ArtifactVersion)
	 */
	public void setUrlForArtifactVersion(ArtifactVersion artifactVersion, String requestedUrl, String version) throws MalformedURLException {
		artifactVersion.setUrl(new URL("file:" + (requestedUrl.endsWith("/") ? requestedUrl : (requestedUrl + "/")) + version + "/" + artifactVersion.getId()));
	}
}