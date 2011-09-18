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
package org.org.repository.crawler.maven2;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.org.repository.crawler.mapping.Entry;
import org.org.repository.crawler.mapping.Entry.MavenType;
import org.org.repository.crawler.mapping.Entry.RawType;
import org.org.repository.crawler.maven2.RepositoryCrawlerService.ArchivesSpecification;
import org.org.repository.crawler.maven2.RepositoryCrawlerService.LibrariesSpecification;
import org.org.repository.crawler.maven2.RepositoryCrawlerService.PomSpecification;
import org.org.repository.crawler.maven2.model.Group;

public final class RepositoryCrawlerHelper {
	private RepositoryCrawlerHelper() {
	}

	/**
	 * Pops the last segment from a Group Name.<br>
	 * A group name is supposed to be separated by dots.<br>
	 * It will remove everything after the last dot and the dot itself.<br>
	 * 1.2.3 will return 1.2<br>
	 * 1.2.3. will return 1.2.3<br>
	 * 1 will return an empty string<br>
	 * 
	 * @param groupName
	 *            The group name from which a trailing segment will be retrieved. Should not be null
	 * @return
	 */
	public static String popNameSegment(String groupName) throws IllegalArgumentException {
		if (groupName == null)
			throw new IllegalArgumentException("name to be popped should not be null");
		int indexOfLastSeparator = groupName.lastIndexOf(Group.SEPARATOR);
		if (indexOfLastSeparator == -1) {
			groupName = "";
		} else {
			groupName = groupName.substring(0, indexOfLastSeparator);
		}
		return groupName;
	}

	/**
	 * Determines if an Entry is a Javadoc artifact entry.<br>
	 * In maven repositories, a javadoc artifact has a -javadoc classifier at the end of its name.<br>
	 * 
	 * @param artifactVersionEntry
	 *            The Entry from which the javadoc status is resolved.
	 * @return
	 */
	public static boolean isJavadoc(Entry artifactVersionEntry) {
		if (artifactVersionEntry == null || artifactVersionEntry.getResolvedName() == null) {
			throw new IllegalArgumentException("artifactVersionEntry should not be null, nor its resolved name");
		}
		return RepositoryCrawlerService.JAVADOC_ARTIFACT_PATTERN.matcher(artifactVersionEntry.getResolvedName()).matches();
	}

	/**
	 * Determines if an Entry is a Sources artifact entry.<br>
	 * In maven repositories, a sources artifact has a -sources classifier at the end of its name.<br>
	 * 
	 * @param artifactVersionEntry
	 *            The Entry from which the sources status is resolved.
	 * @return
	 */
	public static boolean isSource(Entry artifactVersionEntry) {
		if (artifactVersionEntry == null || artifactVersionEntry.getResolvedName() == null) {
			throw new IllegalArgumentException("artifactVersionEntry should not be null, nor its resolved name");
		}
		return RepositoryCrawlerService.SOURCES_ARTIFACT_PATTERN.matcher(artifactVersionEntry.getResolvedName()).matches();
	}

	/**
	 * Determines if the Entry respects the parametered pom file specification.
	 * 
	 * @param artifactVersionEntry
	 *            the entry to scan.
	 * @param pomSpecification
	 *            the specification.
	 * @return
	 */
	public static boolean isPom(Entry artifactVersionEntry, PomSpecification pomSpecification) {
		if (artifactVersionEntry == null || artifactVersionEntry.getResolvedName() == null) {
			throw new IllegalArgumentException("artifactVersionEntry should not be null, nor its resolved name.");
		}
		if (pomSpecification == null) {
			throw new IllegalArgumentException("pomSpecification should not be null.");
		}
		boolean result = false;
		for (String pomExtension : pomSpecification.getExtensions()) {
			if (artifactVersionEntry.getResolvedName().endsWith(pomExtension)) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Determines if the Entry respects the parametered archive file specification.
	 * 
	 * @param artifactVersionEntry
	 *            the entry to scan.
	 * @param archivesSpecification
	 *            the specification.
	 * @return
	 */
	public static boolean isArchive(Entry artifactVersionEntry, ArchivesSpecification archivesSpecification) {
		if (artifactVersionEntry == null || artifactVersionEntry.getResolvedName() == null) {
			throw new IllegalArgumentException("artifactVersionEntry should not be null, nor its resolved name.");
		}
		if (archivesSpecification == null) {
			throw new IllegalArgumentException("archivesSpecification should not be null.");
		}
		boolean result = false;
		for (String artifactExtension : archivesSpecification.getExtensions()) {
			if (artifactVersionEntry.getResolvedName().endsWith(artifactExtension)) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Determines if the Entry respects the parametered library file specification.
	 * 
	 * @param artifactVersionEntry
	 *            the entry to scan.
	 * @param librariesSpecification
	 *            the specification.
	 * @return
	 */
	public static boolean isLibrary(Entry artifactVersionEntry, LibrariesSpecification librariesSpecification) {
		if (artifactVersionEntry == null || artifactVersionEntry.getResolvedName() == null) {
			throw new IllegalArgumentException("artifactVersionEntry should not be null, nor its resolved name.");
		}
		if (librariesSpecification == null) {
			throw new IllegalArgumentException("archivesSpecification should not be null.");
		}
		boolean result = false;
		for (String artifactExtension : librariesSpecification.getExtensions()) {
			if (artifactVersionEntry.getResolvedName().endsWith(artifactExtension)) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * This filters entries and only leaves artifact version folders.<br>
	 * In maven 2 repositories, a version folder is specified by the /[groupFolders].../[artifactId]/[version].
	 * 
	 * @param entryList
	 * @return
	 */
	public static Queue<Entry> getVersionsFolders(Queue<Entry> entryList) {
		if (entryList == null) {
			throw new IllegalArgumentException("entryList should not be null.");
		}
		Queue<Entry> versionsList = new LinkedBlockingQueue<Entry>();
		for (Entry nextEntry : entryList) {
			if (nextEntry.isRawType(RawType.DIRECTORY) && nextEntry.isMavenType(MavenType.ARTIFACT_VERSION_FOLDER)) {
				versionsList.add(nextEntry);
			}
		}
		return versionsList;
	}

	/**
	 * This filters entries and only leaves folders that are part of the group path.<br>
	 * In maven 2 repositories, folders can be separated in 3 main categories:<br>
	 * <ul>
	 * <li>group path folders
	 * <li>artifact folders (which contain metadata about an artifact and may sometimes also be group path folders for other libraries)
	 * <li>artifact version folders (which contain artifacts).
	 * </ul>
	 * Artifact Version folders will be removed from the result list.
	 * 
	 * @param entryList
	 * @return
	 */
	public static Queue<Entry> keepGroupPathOnly(Queue<Entry> entryList) {
		if (entryList == null) {
			throw new IllegalArgumentException("entryList should not be null.");
		}
		Queue<Entry> groupPathFolders = new LinkedBlockingQueue<Entry>();
		for (Entry evaluatedEntry : entryList) {
			if (evaluatedEntry.isRawType(RawType.DIRECTORY) && !(evaluatedEntry.isMavenType(MavenType.ARTIFACT_VERSION_FOLDER))) {
				groupPathFolders.add(evaluatedEntry);
			}
		}
		return groupPathFolders;
	}

	/**
	 * In maven 2 repositories, folders can be separated in 3 main categories:<br>
	 * <ul>
	 * <li>group path folders
	 * <li>artifact folders (which contain metadata about an artifact and may sometimes also be group path folders for other libraries)
	 * <li>artifact version folders (which contain artifacts).
	 * </ul>
	 * The second one is harder to specify than the others.<br>
	 * The only way is to provide a list of its entries. If it contains a an artifact version folder, then it is an artifact folder.<br>
	 * Another possible way would be in theory to scan for a maven-metadata*.xml entry, but their presence is not consistent in repositories.
	 * 
	 * 
	 * @param requestedUrl
	 * @param entryList
	 * @param isArtifact
	 * @return
	 * @throws IOException
	 */
	public static boolean isCurrentFolderAnArtifactFolder(Queue<Entry> entryList) {
		if (entryList == null) {
			throw new IllegalArgumentException("entryList should not be null.");
		}
		// boolean metaFileCondition = false;
		boolean versionFolderCondition = false;
		boolean result = false;
		for (Entry evaluatedEntry : entryList) {
			// metaFileCondition = metaFileCondition || evaluatedEntry.isMetaFile();
			versionFolderCondition = versionFolderCondition || (evaluatedEntry.isMavenType(MavenType.ARTIFACT_VERSION_FOLDER));
			if (versionFolderCondition) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * @param upperGroupName
	 * @return
	 */
	public static String getLastGroupNameSegment(String groupName) {
		if (groupName == null) {
			throw new IllegalArgumentException("groupName should not be null.");
		}
		if (groupName.endsWith("/")) {
			groupName = groupName.substring(0, groupName.length() - 1);
		}
		return groupName.substring(groupName.lastIndexOf("/") + 1, groupName.length());
	}

	/**
	 * @param upperGroupName
	 * @return
	 */
	public static boolean isRootFolder(String groupName) {
		if (groupName == null) {
			throw new IllegalArgumentException("groupName should not be null.");
		}
		return groupName.equals("");
	}

	public static String getClassifier(Entry artifactVersionEntry, String version) {
		if (artifactVersionEntry == null || artifactVersionEntry.getResolvedName() == null) {
			throw new IllegalArgumentException("artifactVersionEntry nor its resolved name should not be null.");
		}
		if(version== null){
			throw new IllegalArgumentException("version should not be null.");
		}
		Pattern pattern = Pattern.compile(".*" + version + "-(.*)");
		Matcher matcher = pattern.matcher(artifactVersionEntry.getResolvedName());
		String classifier = null;
		if (matcher.matches()) {
			classifier = matcher.group(1);
			if (classifier.contains(".")) {
				classifier = classifier.substring(0, classifier.lastIndexOf("."));
			}
			
		}
		return classifier;
	}

}
