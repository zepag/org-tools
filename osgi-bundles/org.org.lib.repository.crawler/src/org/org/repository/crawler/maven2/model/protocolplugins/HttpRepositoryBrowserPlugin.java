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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.org.repository.crawler.RepositoryCrawlingException;
import org.org.repository.crawler.items.IHttpCrawledRepositorySetup;
import org.org.repository.crawler.items.IPatternSet;
import org.org.repository.crawler.items.mutable.PatternSet;
import org.org.repository.crawler.mapping.Entry;
import org.org.repository.crawler.mapping.Entry.MavenType;
import org.org.repository.crawler.mapping.Entry.RawType;
import org.org.repository.crawler.maven2.RepositoryCrawlerService;
import org.org.repository.crawler.maven2.model.ArtifactVersion;

/**
 * @author pagregoire
 */
public class HttpRepositoryBrowserPlugin implements IRepositoryBrowserPlugin<IHttpCrawledRepositorySetup> {

	public final static IPatternSet TOMCAT6_PATTERNSET;

	public final static IPatternSet APACHE2_PATTERNSET;

	public final static IPatternSet ARTIFACTORY_PATTERNSET;

	public final static IPatternSet S3BROWSE_PATTERNSET;

	static {
		PatternSet tmpTomcat6PatternSet = new PatternSet();
		tmpTomcat6PatternSet.setLabel("Tomcat 6 parsing patterns");
		tmpTomcat6PatternSet.setEntryPattern(".*<a href=\".+\"><.*>(.+)<.*></a>.*");
		tmpTomcat6PatternSet.setParentDirectoryPattern(".*<a href=\".+\"><.*>Up To.*<.*></a>.*");
		tmpTomcat6PatternSet.setFileEntryPattern(".*<a href=\".*\"><.*>(.+)<.*></a>.*");
		tmpTomcat6PatternSet.setDirectoryEntryPattern(".*<a href=\".*\"><.*>(.+)/<.*></a>.*");
		PatternSet tmpApache2PatternSet = new PatternSet();
		tmpApache2PatternSet.setLabel("Apache 2 parsing patterns");
		tmpApache2PatternSet.setEntryPattern(".*<a href=\".+\">(.+)</a>.*");
		tmpApache2PatternSet.setParentDirectoryPattern(".*<a href=\".+\">Parent Directory</a>.*");
		tmpApache2PatternSet.setFileEntryPattern(".*<a href=\".*\">(.+)</a>.*");
		tmpApache2PatternSet.setDirectoryEntryPattern(".*<a href=\".*\">(.+)/</a>.*");
		PatternSet tmpArtifactoryPatternSet = new PatternSet();
		tmpArtifactoryPatternSet.setLabel("Artifactory parsing patterns");
		tmpArtifactoryPatternSet.setEntryPattern("[^>]*<a href=\"[^#?]+\">(.+)</a>[^&]*");
		tmpArtifactoryPatternSet.setParentDirectoryPattern(".*<a href=\"[^#]+\">..</a>.*");
		tmpArtifactoryPatternSet.setFileEntryPattern(".*<a href=\"[^#]*\">(.+)</a>.*");
		tmpArtifactoryPatternSet.setDirectoryEntryPattern(".*<a href=\"[^#]*/\">(.+)</a>.*");
		PatternSet tmpS3BrowsePatternSet = new PatternSet();
		tmpS3BrowsePatternSet.setLabel("S3 Browse parsing patterns");
		tmpS3BrowsePatternSet.setEntryPattern("[^>]*<(p|td)><a href=\"[^#?]+\">(.+)</a>.*");
		tmpS3BrowsePatternSet.setParentDirectoryPattern("[^>]*<p><a href=\"[^#]+\">ROOT</a>.*");
		tmpS3BrowsePatternSet.setFileEntryPattern("[^>]*<td><a href=\"[^#]*\">(.+)</a>.*");
		tmpS3BrowsePatternSet.setDirectoryEntryPattern("[^>]*<td><a href=\"[^#]*/\">(.+)</a>.*");

		TOMCAT6_PATTERNSET = tmpTomcat6PatternSet.getImmutable();
		APACHE2_PATTERNSET = tmpApache2PatternSet.getImmutable();
		ARTIFACTORY_PATTERNSET = tmpArtifactoryPatternSet.getImmutable();
		S3BROWSE_PATTERNSET = tmpS3BrowsePatternSet.getImmutable();
	}

	private IPatternSet patternSet = APACHE2_PATTERNSET;

	private Pattern parentPatternCompiled = Pattern.compile(patternSet.getParentDirectoryPattern(), Pattern.CASE_INSENSITIVE);

	private Pattern entryPatternCompiled = Pattern.compile(patternSet.getEntryPattern(), Pattern.CASE_INSENSITIVE);

	private Pattern directoryPatternCompiled = Pattern.compile(patternSet.getDirectoryEntryPattern(), Pattern.CASE_INSENSITIVE);

	private Pattern filePatternCompiled = Pattern.compile(patternSet.getFileEntryPattern(), Pattern.CASE_INSENSITIVE);

	public static final int DIRECTORY_PATTERN_GROUP_INDEX = 1;

	public static final int FILE_PATTERN_GROUP_INDEX = 1;

	private Proxy proxy;

	public IPatternSet getPatternSet() {
		return patternSet;
	}

	public Pattern getParentPatternCompiled() {
		return parentPatternCompiled;
	}

	public Pattern getEntryPatternCompiled() {
		return entryPatternCompiled;
	}

	public Pattern getDirectoryPatternCompiled() {
		return directoryPatternCompiled;
	}

	public Pattern getFilePatternCompiled() {
		return filePatternCompiled;
	}

	public Proxy getProxy() {
		return proxy;
	}

	/**
	 * init this class with the repository info
	 * 
	 * @param httpBrowsedRepository
	 */
	public void init(IHttpCrawledRepositorySetup repositorySetup) {
		if (repositorySetup.getProxyHost() != null) {
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(repositorySetup.getProxyHost(), repositorySetup.getProxyPort()));
		} else {
			proxy = Proxy.NO_PROXY;
		}
		setPatternSet(repositorySetup.getPatternSet());
	}

	/**
	 * Checks the repository's validity
	 * 
	 * @param repositorySetup
	 * @return
	 */
	public void checkRepositorySetup(IHttpCrawledRepositorySetup repositorySetup) {
		if (repositorySetup.getBaseUrl() == null) {
			throw new RepositoryCrawlingException("An url should be defined for the repository.");
		}
	}

	/**
	 * @param requestedUrl
	 * @return
	 * @throws IOException
	 */
	public Queue<Entry> getEntryList(String requestedUrl) throws IOException {
		Queue<Entry> entryList = new LinkedBlockingQueue<Entry>();
		BufferedReader response = null;
		StringBuilder buffer = new StringBuilder();
		try {
			response = open(requestedUrl);
			String line = null;

			while ((line = response.readLine()) != null) {
				buffer.append(line + "\n");
				Entry entry = new Entry();
				entry.setValue(line);
				Matcher entryMatcher = entryPatternCompiled.matcher(entry.getValue());
				Matcher parentMatcher = parentPatternCompiled.matcher(entry.getValue());
				boolean isEntry = entryMatcher.matches();
				boolean isParent = parentMatcher.matches();
				if (isEntry && !isParent) {
					Matcher directoryMatcher = directoryPatternCompiled.matcher(entry.getValue());
					Matcher fileMatcher = filePatternCompiled.matcher(entry.getValue());
					if (directoryMatcher.matches()) {
						entry.setRawType(RawType.DIRECTORY);
					}
					if (entry.isRawType(RawType.DIRECTORY)) {
						entry.setResolvedName(directoryMatcher.group(DIRECTORY_PATTERN_GROUP_INDEX));
						if (RepositoryCrawlerService.ARTIFACT_VERSION_FOLDER_PATTERN.matcher(entry.getResolvedName()).matches()) {
							entry.setMavenType(MavenType.ARTIFACT_VERSION_FOLDER);
						}
					} else {
						fileMatcher.matches();
						entry.setResolvedName(fileMatcher.group(FILE_PATTERN_GROUP_INDEX));
						if (fileMatcher.matches()) {
							entry.setRawType(RawType.FILE);
						}
						if (entry.isRawType(RawType.FILE)) {
							if (entry.getResolvedName().startsWith("maven-metadata")) {
								entry.setMavenType(MavenType.METADATA_FILE);
							}
						}
					}
					entryList.add(entry);
				}
			}
		} catch (FileNotFoundException e) {
			if (response != null) {
				close(response);
			}
		}
		return entryList;
	}

	private BufferedReader open(String url) throws IOException {
		if (!url.endsWith("/")) {
			url = url + "/";
		}
		InputStream inputStream = null;
		if (proxy == null) {
			inputStream = (new URL(url)).openConnection().getInputStream();
		} else {
			inputStream = (new URL(url)).openConnection(proxy).getInputStream();
		}
		return new BufferedReader(new InputStreamReader(inputStream));
	}

	private void close(BufferedReader in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e1) {
				// ignore.
			}
		}

	}

	/**
	 * @param repositorySetup
	 * @param upperGroupName
	 * @param folderName
	 * @return
	 */
	public String buildUrl(IHttpCrawledRepositorySetup repositorySetup, String upperGroupName, String folderName) {
		StringBuffer buffer = new StringBuffer(repositorySetup.getBaseUrl());
		buffer.append((repositorySetup.getBaseUrl().endsWith("/") ? "" : "/"));
		// if (!upperGroupName.equals("")) {
		// buffer.append(upperGroupName.replace('.', '/'));
		// }
		if (!upperGroupName.equals("")) {
			buffer.append(upperGroupName);
		}
		if (!folderName.equals("")) {
			buffer.append("/" + (folderName.endsWith("/") ? folderName : folderName + "/"));
		}
		return buffer.toString();
	}

	/**
	 * @param versionFolder
	 * @param version
	 * @return
	 */
	public String cleanFolderName(String folderName) {
		return folderName;
	}

	private void setPatternSet(IPatternSet patternSet) {
		this.patternSet = patternSet;
		entryPatternCompiled = Pattern.compile(patternSet.getEntryPattern());
		directoryPatternCompiled = Pattern.compile(patternSet.getDirectoryEntryPattern());
		filePatternCompiled = Pattern.compile(patternSet.getFileEntryPattern());
		parentPatternCompiled = Pattern.compile(patternSet.getParentDirectoryPattern());
	}

	/**
	 * @see org.org.repository.crawler.maven2.RepositoryCrawlerService#setUrlToArtifactVersion(org.org.maven2.crawler.items.ArtifactVersion)
	 */
	public void setUrlForArtifactVersion(ArtifactVersion artifactVersion, String requestedUrl, String version) throws MalformedURLException {
		artifactVersion.setUrl(new URL((requestedUrl.endsWith("/") ? requestedUrl : (requestedUrl + "/")) + version + "/" + artifactVersion.getId()));
	}

}