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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

import org.org.model.IModelItem;
import org.org.model.IModelItemListener;
import org.org.repository.crawler.IExternalInterruptionFlagSetter;
import org.org.repository.crawler.InterruptionFlag;
import org.org.repository.crawler.RepositoryCrawlingException;
import org.org.repository.crawler.items.ICrawledRepositorySetup;
import org.org.repository.crawler.items.mutable.AbstractCrawledRepositorySetup;
import org.org.repository.crawler.mapping.Entry;
import org.org.repository.crawler.mapping.Entry.RawType;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.CrawledRepository;
import org.org.repository.crawler.maven2.model.Group;
import org.org.repository.crawler.maven2.model.protocolplugins.IRepositoryBrowserPlugin;

/**
 * @author pagregoire
 */
public class RepositoryCrawlerService<T extends ICrawledRepositorySetup> {
	public final static Pattern ARTIFACT_VERSION_FOLDER_PATTERN = Pattern
			.compile("[0-9.]*[0-9].+");

	public final static Pattern JAVADOC_ARTIFACT_PATTERN = Pattern
			.compile(".*-javadoc.jar");

	public final static Pattern SOURCES_ARTIFACT_PATTERN = Pattern
			.compile(".*-sources.jar");

	public static class ArchivesSpecification {
		private Set<String> archivesExtensions = new HashSet<String>();

		public boolean addExtension(String o) {
			return archivesExtensions.add(o);
		}

		public Set<String> getExtensions() {
			return archivesExtensions;
		}

		public void setExtensions(Set<String> extensions) {
			this.archivesExtensions = extensions;
		}
	}

	public static class LibrariesSpecification {
		private Set<String> libraryExtensions = new HashSet<String>();

		public boolean addExtension(String o) {
			return libraryExtensions.add(o);
		}

		public Set<String> getExtensions() {
			return libraryExtensions;
		}

		public void setExtensions(Set<String> extensions) {
			this.libraryExtensions = extensions;
		}
	}

	public static class PomSpecification {
		private Set<String> pomExtensions = new HashSet<String>();

		public boolean addExtension(String o) {
			return pomExtensions.add(o);
		}

		public Set<String> getExtensions() {
			return pomExtensions;
		}

		public void setExtensions(Set<String> extensions) {
			this.pomExtensions = extensions;
		}
	}

	private ArchivesSpecification archivesSpecification = new ArchivesSpecification();

	private LibrariesSpecification librariesSpecification = new LibrariesSpecification();

	private PomSpecification pomSpecification = new PomSpecification();

	private InterruptionFlag interruptionFlag = new InterruptionFlag();

	private List<IExternalInterruptionFlagSetter> externalInterruptionFlagSetters = new ArrayList<IExternalInterruptionFlagSetter>();

	private IRepositoryBrowserPlugin<T> repositoryBrowserPlugin;

	@SuppressWarnings({ "rawtypes", "unused" })
	private class ScanningContext<M extends IModelItem, S extends ICrawledRepositorySetup> {

		private S repositorySetup;

		private final M resultingItem;

		private final Map<String, Group> temporaryGroups;

		private String upperGroupName;

		private Queue<String> folderNamesStack;

		private String strictArtifactName;

		private String strictArtifactVersion;

		public ScanningContext(final M resultingItem, S repositorySetup) {
			super();
			this.resultingItem = resultingItem;
			this.temporaryGroups = new ConcurrentHashMap<String, Group>();
			this.folderNamesStack = new LinkedList<String>();
			this.upperGroupName = "";
			this.repositorySetup = repositorySetup;
		}

		public M getResultingItem() {
			return resultingItem;
		}

		public void addTemporaryGroup(Group group) {
			temporaryGroups.put(group.getName(), group);
		}

		public Boolean hasTemporaryGroup(String groupName) {
			return temporaryGroups.containsKey(groupName);
		}

		public void removeTemporaryGroup(Group group) {
			temporaryGroups.remove(group.getName());
		}

		public Group getTemporaryGroup(String groupName) {
			return temporaryGroups.get(groupName);
		}

		public String getUpperGroupName() {
			return upperGroupName;
		}

		public void setUpperGroupName(String upperGroupName) {
			this.upperGroupName = upperGroupName;
		}

		public boolean offerFolderName(String o) {
			return folderNamesStack.offer(o);
		}

		public String peekLatestFolderName() {
			return folderNamesStack.peek();
		}

		public String pollLatestFolderName() {
			return folderNamesStack.poll();
		}

		@Override
		public String toString() {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(resultingItem);
			stringBuilder.append(temporaryGroups);
			stringBuilder.append(upperGroupName);
			stringBuilder.append(folderNamesStack);
			return stringBuilder.toString();
		}

		public String getStrictArtifactName() {
			return strictArtifactName;
		}

		public Boolean hasStrictArtifactName() {
			return strictArtifactName != null;
		}

		public void setStrictArtifactName(String strictArtifactName) {
			this.strictArtifactName = strictArtifactName;
		}

		public String getStrictArtifactVersion() {
			return strictArtifactVersion;
		}

		public Boolean hasStrictArtifactVersion() {
			return strictArtifactVersion != null;
		}

		public void setStrictArtifactVersion(String strictArtifactVersion) {
			this.strictArtifactVersion = strictArtifactVersion;
		}

		public S getRepositorySetup() {
			return repositorySetup;
		}
	}

	/**
	 * 
	 */
	public RepositoryCrawlerService(
			IRepositoryBrowserPlugin<T> repositoryBrowserPlugin) {
		archivesSpecification.addExtension(".war");
		archivesSpecification.addExtension(".ear");
		librariesSpecification.addExtension("jar");
		pomSpecification.addExtension(".pom");
		this.repositoryBrowserPlugin = repositoryBrowserPlugin;
	}

	public void addExternalInterruptionFlagSetter(int index,
			IExternalInterruptionFlagSetter externalInterruptionFlagSetter) {
		externalInterruptionFlagSetters.add(index,
				externalInterruptionFlagSetter);
	}

	public Queue<Entry> scanForArtifacts(T repositorySetup,
			String upperGroupName, String folderName) throws IOException {
		String requestedUrl = repositoryBrowserPlugin.buildUrl(repositorySetup,
				makeGroupPathOutOfGroupName(upperGroupName), folderName);
		Queue<Entry> artifactsList = new LinkedBlockingQueue<Entry>();
		Queue<Entry> entryList = repositoryBrowserPlugin
				.getEntryList(requestedUrl);
		for (Entry nextEntry : entryList) {
			if (!nextEntry.isRawType(RawType.DIRECTORY)) {
				if (nextEntry.isRawType(RawType.FILE)) {
					artifactsList.add(nextEntry);
				}
			}
		}
		return artifactsList;
	}

	@SuppressWarnings("rawtypes")
	private <Y extends IModelItem> void scanRepository(
			ScanningContext<Y, T> scanningContext) throws IOException {
		processFlagSetters();
		if (getInterruptionFlag().isCurrentStatus(InterruptionFlag.CONTINUE)) {
			// create the requested URL
			String requestedUrl = repositoryBrowserPlugin.buildUrl(
					scanningContext.getRepositorySetup(),
					scanningContext.getUpperGroupName(), "");
			// get the entries from the listing page
			Queue<Entry> entryList = repositoryBrowserPlugin
					.getEntryList(requestedUrl);
			// test if the current folder is an artifact folder
			boolean isArtifact = RepositoryCrawlerHelper
					.isCurrentFolderAnArtifactFolder(entryList);
			// keeps the group pathes only (folders which
			// are part of a group id), removes version folders.
			Queue<Entry> groupPathFolders = RepositoryCrawlerHelper
					.keepGroupPathOnly(entryList);
			// if the current folder is recognized as an artifact folder,
			// retrieve the artifacts' info and add it to the upper Group.
			if (isArtifact) {
				scanForArtifact(scanningContext,
						RepositoryCrawlerHelper.getVersionsFolders(entryList),
						groupPathFolders, requestedUrl);
			}
			// handle recursively the other group folders.
			if (worthLookingFurtherInTree(scanningContext)) {
				handleGroupPathFolders(scanningContext,
						scanningContext.getUpperGroupName(), groupPathFolders);
			}
		}
		try {
			while (getInterruptionFlag()
					.isCurrentStatus(InterruptionFlag.PAUSE)) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			getInterruptionFlag().setCurrentStatus(InterruptionFlag.STOP);
		}
	}

	private Queue<Entry> removeNonMatchingFolders(T repositorySetup,
			String upperGroupName, Queue<Entry> groupPathFolders) {
		Queue<Entry> result = new LinkedBlockingQueue<Entry>();
		for (Entry entry : groupPathFolders) {
			boolean matchesAny = false;
			for (String filter : repositorySetup.getGroupFilters()) {
				filter = makeGroupPathOutOfGroupName(filter);
				final String separator = (upperGroupName.equals("") || upperGroupName
						.endsWith(".")) ? "" : "/";
				final String toTest = upperGroupName + separator
						+ entry.getResolvedName();
				if (toTest.matches(filter) || filter.startsWith(toTest)
						|| toTest.startsWith(filter)) {
					matchesAny = true;
					break;
				}
			}
			if (matchesAny) {
				result.add(entry);
			}
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	private boolean worthLookingFurtherInTree(ScanningContext scanningContext) {
		boolean result = false;
		for (String groupFilter : scanningContext.getRepositorySetup()
				.getGroupFilters()) {
			if (groupFilter != null) {
				groupFilter = makeGroupPathOutOfGroupName(groupFilter);
				String upperGroupName = scanningContext.getUpperGroupName();
				// if a strict group name is specified
				if (upperGroupName.matches(groupFilter)
						|| (groupFilter.startsWith(upperGroupName))) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	private void processFlagSetters() {
		for (IExternalInterruptionFlagSetter externalInterruptionFlagSetter : externalInterruptionFlagSetters) {
			interruptionFlag = externalInterruptionFlagSetter.processStatus();
		}
	}

	/**
	 * @param repositorySetup
	 * @param scanningContext
	 * @param upperGroupName
	 * @param folderName
	 * @param groupPathFolders
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	private <Y extends IModelItem> void handleGroupPathFolders(
			ScanningContext<Y, T> scanningContext, String folderName,
			Queue<Entry> groupPathFolders) throws IOException {
		String upperGroupName = scanningContext.getUpperGroupName();
		groupPathFolders = removeNonMatchingFolders(
				scanningContext.getRepositorySetup(), upperGroupName,
				groupPathFolders);
		for (Entry groupFolderEntry : groupPathFolders) {
			String groupName = "";
			String cleanFolderName = repositoryBrowserPlugin
					.cleanFolderName(groupFolderEntry.getResolvedName());
			if (!upperGroupName.equals("")) {
				groupName = upperGroupName + Group.SEPARATOR + cleanFolderName;
			} else {
				groupName = cleanFolderName;
			}
			String parentGroupName = upperGroupName;
			scanningContext.setUpperGroupName(groupName);
			scanningContext.offerFolderName(cleanFolderName);
			scanRepository(scanningContext);
			scanningContext.pollLatestFolderName();
			scanningContext.setUpperGroupName(parentGroupName);
		}
	}

	/**
	 * @param repository
	 * @param scanningContext
	 * @param upperGroupName
	 * @param entryList
	 * @param groupPathFolders
	 * @param requestedUrl
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	private <Y extends IModelItem> void scanForArtifact(
			ScanningContext<Y, T> scanningContext, Queue<Entry> versionsList,
			Queue<Entry> groupPathFolders, String requestedUrl)
			throws IOException {
		if (!RepositoryCrawlerHelper.isRootFolder(scanningContext
				.getUpperGroupName())) {
			if (isWorthScanningForArtifact(scanningContext)) {
				groupPathFolders.removeAll(versionsList);
				Set<ArtifactVersion> artifactVersions = generateArtifactVersions(
						scanningContext.getRepositorySetup(),
						scanningContext.getUpperGroupName(), requestedUrl,
						versionsList);
				addToResult(scanningContext, artifactVersions);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private <Y extends IModelItem> Boolean isWorthScanningForArtifact(
			ScanningContext<Y, T> scanningContext) {
		Boolean result = true;// default is that it is worth it.
		if (scanningContext.getRepositorySetup().hasGroupFilters()) {
			result = false;
			String group = scanningContext.getUpperGroupName().substring(0,
					scanningContext.getUpperGroupName().lastIndexOf("/"));
			for (String groupFilter : scanningContext.getRepositorySetup()
					.getGroupFilters()) {
				groupFilter = makeGroupPathOutOfGroupName(groupFilter);
				// if a strict group name is specified
				if (group.matches(groupFilter)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <Y extends IModelItem> void addToResult(
			ScanningContext<Y, T> scanningContext,
			Set<ArtifactVersion> artifactVersions) {
		if (scanningContext.getResultingItem() instanceof CrawledRepository) {
			addToRepository(
					(ScanningContext<CrawledRepository, T>) scanningContext,
					artifactVersions);
		} else if (scanningContext.getResultingItem() instanceof Group) {
			addToGroup((ScanningContext<Group, T>) scanningContext,
					artifactVersions);
		} else if (scanningContext.getResultingItem() instanceof Artifact) {
			addToArtifact((ScanningContext<Artifact, T>) scanningContext,
					artifactVersions);
		}
	}

	private void addToRepository(
			ScanningContext<CrawledRepository, T> scanningContext,
			Set<ArtifactVersion> artifactVersions) {
		Group group = null;
		String groupName = makeGroupNameOutOfGroupPath(RepositoryCrawlerHelper
				.popNameSegment(scanningContext.getUpperGroupName()));
		if (scanningContext.getResultingItem().hasChild(groupName)) {
			group = scanningContext.getResultingItem().getChild(groupName);
		} else {
			if (!scanningContext.hasTemporaryGroup(groupName)) {
				group = new Group(
						makeGroupNameOutOfGroupPath(RepositoryCrawlerHelper
								.popNameSegment(scanningContext
										.getUpperGroupName())));
				scanningContext.addTemporaryGroup(group);
			} else {
				group = scanningContext
						.getTemporaryGroup(makeGroupNameOutOfGroupPath(groupName));
			}
			scanningContext.getResultingItem().addChild(group);
		}
		Artifact artifact = new Artifact(
				RepositoryCrawlerHelper.getLastGroupNameSegment(scanningContext
						.getUpperGroupName()));
		if (group.hasChild(artifact.getUID())) {
			artifact = group.getChild(artifact.getUID());
		} else {
			group.addChild(artifact);
		}
		for (ArtifactVersion artifactVersion : artifactVersions) {
			artifact.addChild(artifactVersion);
		}

	}

	private void addToGroup(ScanningContext<Group, T> scanningContext,
			Set<ArtifactVersion> artifactVersions) {
		Artifact artifact = new Artifact(
				RepositoryCrawlerHelper.getLastGroupNameSegment(scanningContext
						.getUpperGroupName()));
		if (!scanningContext.getResultingItem().hasChild(artifact.getUID())) {
			scanningContext.getResultingItem().addChild(artifact);
		}
		for (ArtifactVersion artifactVersion : artifactVersions) {
			scanningContext.getResultingItem().getChild(artifact.getUID())
					.addChild(artifactVersion);
		}
	}

	private void addToArtifact(ScanningContext<Artifact, T> scanningContext,
			Set<ArtifactVersion> artifactVersions) {
		for (ArtifactVersion artifactVersion : artifactVersions) {
			scanningContext.getResultingItem().addChild(artifactVersion);
		}
	}

	/**
	 * @param repository
	 * @param upperGroupName
	 * @param requestedUrl
	 * @param versionsList
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private Set<ArtifactVersion> generateArtifactVersions(T repositorySetup,
			String upperGroupName, String requestedUrl,
			Queue<Entry> versionsList) throws IOException,
			MalformedURLException {
		Set<ArtifactVersion> artifactVersions = new HashSet<ArtifactVersion>();
		Map<String, ArtifactVersion> artifactVersionsMap = new HashMap<String, ArtifactVersion>();
		for (Entry versionFolder : versionsList) {
			String version = repositoryBrowserPlugin
					.cleanFolderName(versionFolder.getResolvedName());
			Map<String, ArtifactVersion> pomStack = new HashMap<String, ArtifactVersion>();
			Map<String, ArtifactVersion> sourcesStack = new HashMap<String, ArtifactVersion>();
			Map<String, ArtifactVersion> javadocStack = new HashMap<String, ArtifactVersion>();
			for (Entry artifactVersionEntry : scanForArtifacts(repositorySetup,
					upperGroupName, version)) {

				if (RepositoryCrawlerHelper.isSource(artifactVersionEntry)) {
					ArtifactVersion artifactVersion = new ArtifactVersion();
					artifactVersion.setId(artifactVersionEntry
							.getResolvedName());
					artifactVersion.setVersion(version);
					repositoryBrowserPlugin.setUrlForArtifactVersion(
							artifactVersion, requestedUrl, version);
					if (artifactVersionsMap.get(artifactVersion.getVersion()) != null) {
						artifactVersionsMap.get(artifactVersion.getVersion())
								.setSourcesUrl(artifactVersion.getUrl());
					} else {
						sourcesStack.put(artifactVersion.getVersion(),
								artifactVersion);
					}
				} else if (RepositoryCrawlerHelper
						.isJavadoc(artifactVersionEntry)) {
					ArtifactVersion artifactVersion = new ArtifactVersion();
					artifactVersion.setId(artifactVersionEntry
							.getResolvedName());
					artifactVersion.setVersion(version);
					repositoryBrowserPlugin.setUrlForArtifactVersion(
							artifactVersion, requestedUrl, version);
					if (artifactVersionsMap.get(artifactVersion.getVersion()) != null) {
						artifactVersionsMap.get(artifactVersion.getVersion())
								.setJavadocUrl(artifactVersion.getUrl());
					} else {
						javadocStack.put(artifactVersion.getVersion(),
								artifactVersion);
					}
				} else if (RepositoryCrawlerHelper.isArchive(
						artifactVersionEntry, archivesSpecification)
						|| RepositoryCrawlerHelper.isLibrary(
								artifactVersionEntry, librariesSpecification)) {
					ArtifactVersion artifactVersion = new ArtifactVersion();
					artifactVersion.setId(artifactVersionEntry
							.getResolvedName());
					artifactVersion.setVersion(version);
					final String classifier = RepositoryCrawlerHelper
							.getClassifier(artifactVersionEntry, version);
					if (classifier != null && !version.contains(classifier)) {
						artifactVersion.setClassifier(classifier);
					}
					if (RepositoryCrawlerHelper.isLibrary(artifactVersionEntry,
							librariesSpecification)) {
						artifactVersion.setType(ArtifactVersion.Type.LIBRARY);
					} else if (RepositoryCrawlerHelper.isArchive(
							artifactVersionEntry, archivesSpecification)) {
						artifactVersion.setType(ArtifactVersion.Type.ARCHIVE);
					}
					repositoryBrowserPlugin.setUrlForArtifactVersion(
							artifactVersion, requestedUrl, version);
					final String artifactVersionIdentifier = artifactVersion
							.getVersion()
							+ (artifactVersion.getClassifier() == null ? ""
									: "-" + artifactVersion.getClassifier());
					artifactVersionsMap.put(artifactVersionIdentifier,
							artifactVersion);
					if (pomStack.get(artifactVersionIdentifier) != null) {
						artifactVersion.setPomUrl(pomStack.get(
								artifactVersionIdentifier).getUrl());
						pomStack.remove(artifactVersionIdentifier);
					}
					if (javadocStack.get(artifactVersionIdentifier) != null) {
						artifactVersion.setJavadocUrl(javadocStack.get(
								artifactVersion.getVersion()).getUrl());
						javadocStack.remove(artifactVersion.getVersion());
					}
					if (sourcesStack.get(artifactVersionIdentifier) != null) {
						artifactVersion.setSourcesUrl(sourcesStack.get(
								artifactVersionIdentifier).getUrl());
						sourcesStack.remove(artifactVersionIdentifier);
					}
				} else if (RepositoryCrawlerHelper.isPom(artifactVersionEntry,
						pomSpecification)) {
					ArtifactVersion artifactVersion = new ArtifactVersion();
					artifactVersion.setId(artifactVersionEntry
							.getResolvedName());
					artifactVersion.setVersion(version);
					artifactVersion.setType(ArtifactVersion.Type.POM);
					repositoryBrowserPlugin.setUrlForArtifactVersion(
							artifactVersion, requestedUrl, version);
					artifactVersion.setPomUrl(artifactVersion.getUrl());
					final String artifactVersionIdentifier = artifactVersion
							.getVersion()
							+ (artifactVersion.getClassifier() == null ? ""
									: "-" + artifactVersion.getClassifier());
					if (artifactVersionsMap.get(artifactVersionIdentifier) != null) {
						artifactVersionsMap.get(artifactVersionIdentifier)
								.setPomUrl(artifactVersion.getUrl());
					} else {
						pomStack.put(artifactVersionIdentifier, artifactVersion);
					}
				}
			}
			if (!pomStack.isEmpty()) {
				for (String pomVersion : pomStack.keySet()) {
					artifactVersionsMap.put(pomVersion + "pom",
							pomStack.get(pomVersion));
				}
			}
		}
		artifactVersions = new HashSet<ArtifactVersion>(
				artifactVersionsMap.values());
		return artifactVersions;
	}

	public CrawledRepository retrieveRepository(T repositorySetup, String label) {
		return this.retrieveRepository(repositorySetup, label,
				new IModelItemListener[0]);
	}

	public CrawledRepository retrieveRepository(T repositorySetup,
			String label, IModelItemListener... modelItemListeners) {
		CrawledRepository crawledRepository = new CrawledRepository(label,
				repositorySetup);
		return refreshRepository(crawledRepository, label, modelItemListeners);
	}

	public CrawledRepository refreshRepository(
			CrawledRepository existingRepository, String label) {
		return this.refreshRepository(existingRepository, label,
				new IModelItemListener[0]);
	}

	
	@SuppressWarnings("unchecked")
	public CrawledRepository refreshRepository(
			CrawledRepository crawledRepository, String label,
			IModelItemListener... modelItemListeners) {
		// if a precise group definition is set, all existing groups should be
		// added, in order not to lose them.
		if (crawledRepository.getRepositorySetup().hasGroupFilters()
				&& crawledRepository.hasChildren()) {
			for (Group childGroup : crawledRepository.getChildren()) {
				boolean matchesNone = true;
				for (String filter : crawledRepository.getRepositorySetup()
						.getGroupFilters()) {
					if (childGroup.getName().matches(filter)) {
						matchesNone = false;
					}
				}
				// if group doesn't match any of the specified filters, add it
				// as a filter.
				if (matchesNone) {
					crawledRepository.addGroupFilter(childGroup.getName());
				}
			}
		}
		T repositorySetup = (T) crawledRepository.getRepositorySetup();
		repositoryBrowserPlugin.checkRepositorySetup(repositorySetup);
		if (modelItemListeners != null) {
			for (IModelItemListener modelItemListener : modelItemListeners) {
				crawledRepository.addListener(modelItemListener);
			}
		}

		ScanningContext<CrawledRepository, T> scanningContext = new ScanningContext<CrawledRepository, T>(
				crawledRepository, repositorySetup);
		try {
			repositoryBrowserPlugin.init(repositorySetup);
			scanRepository(scanningContext);
		} catch (Exception e) {
			throw new RepositoryCrawlingException(e);
		} finally {
			for (IModelItemListener modelItemListener : modelItemListeners) {
				crawledRepository.removeListener(modelItemListener);
			}
		}
		return scanningContext.getResultingItem();
	}

	public Group retrieveGroup(T repositorySetup, String groupName) {
		return this.retrieveGroup(repositorySetup, groupName,
				new IModelItemListener[0]);
	}

	
	@SuppressWarnings("unchecked")
	public Group retrieveGroup(T repositorySetup, String groupName,
			IModelItemListener... modelItemListeners) {
		Group group = new Group(makeGroupNameOutOfGroupPath(groupName));
		AbstractCrawledRepositorySetup setup = repositorySetup.getMutable();
		setup.getGroupFilters().remove(
				AbstractCrawledRepositorySetup.KEEP_ALL_PATTERN);
		setup.getGroupFilters().add(groupName);
		return refreshGroup(group, (T) setup.getImmutable(), groupName,
				modelItemListeners);
	}

	public Group refreshGroup(Group group, T repositorySetup, String groupName) {
		return this.refreshGroup(group, repositorySetup, groupName,
				new IModelItemListener[0]);
	}

	
	@SuppressWarnings("unchecked")
	public Group refreshGroup(Group group, T repositorySetup, String groupName,
			IModelItemListener... modelItemListeners) {
		repositoryBrowserPlugin.checkRepositorySetup(repositorySetup);
		if (modelItemListeners != null) {
			for (IModelItemListener modelItemListener : modelItemListeners) {
				group.addListener(modelItemListener);
			}
		}
		// if a precise group definition is set, all existing groups should be
		// added, in order not to lose them.
		if (repositorySetup.hasGroupFilters()) {
			boolean matchesNone = true;
			for (String filter : repositorySetup.getGroupFilters()) {
				if (group.getName().matches(filter)) {
					matchesNone = false;
				}
			}
			// if group doesn't match any of the specified filters, add it as a
			// filter.
			if (matchesNone) {
				AbstractCrawledRepositorySetup setup = repositorySetup
						.getMutable();
				setup.getGroupFilters().add(group.getName());
				repositorySetup = (T) setup.getImmutable();
			}
		}
		ScanningContext<Group, T> context = new ScanningContext<Group, T>(
				group, repositorySetup);
		context.setUpperGroupName(makeGroupPathOutOfGroupName(groupName));
		try {
			repositoryBrowserPlugin.init(repositorySetup);
			scanRepository(context);
		} catch (Exception e) {
			throw new RepositoryCrawlingException(e);
		} finally {
			for (IModelItemListener modelItemListener : modelItemListeners) {
				group.removeListener(modelItemListener);
			}
		}
		return context.getResultingItem();
	}

	public Artifact refreshArtifact(Artifact artifact, T repositorySetup,
			String groupName, String artifactName) {
		return this.refreshArtifact(artifact, repositorySetup, groupName,
				artifactName, new IModelItemListener[0]);
	}

	
	@SuppressWarnings("unchecked")
	public Artifact refreshArtifact(Artifact artifact, T repositorySetup,
			String groupName, String artifactName,
			IModelItemListener... modelItemListeners) {
		repositoryBrowserPlugin.checkRepositorySetup(repositorySetup);
		if (modelItemListeners != null) {
			for (IModelItemListener modelItemListener : modelItemListeners) {
				artifact.addListener(modelItemListener);
			}
		}
		// if a precise group definition is set, all existing groups should be
		// added, in order not to lose them.
		if (repositorySetup.hasGroupFilters()) {
			boolean matchesNone = true;
			for (String filter : repositorySetup.getGroupFilters()) {
				if (groupName.matches(filter)) {
					matchesNone = false;
				}
			}
			// if group doesn't match any of the specified filters, add it as a
			// filter.
			if (matchesNone) {
				AbstractCrawledRepositorySetup setup = repositorySetup
						.getMutable();
				setup.getGroupFilters().add(groupName);
				repositorySetup = (T) setup.getImmutable();
			}
		}
		ScanningContext<Artifact, T> context = new ScanningContext<Artifact, T>(
				artifact, repositorySetup);
		context.setUpperGroupName(makeGroupPathOutOfGroupName(groupName + "."
				+ artifactName));
		context.setStrictArtifactName(artifactName);
		try {
			repositoryBrowserPlugin.init(repositorySetup);
			scanRepository(context);
		} catch (Exception e) {
			throw new RepositoryCrawlingException(e);
		} finally {
			for (IModelItemListener modelItemListener : modelItemListeners) {
				artifact.removeListener(modelItemListener);
			}
		}
		return context.getResultingItem();
	}

	public Artifact retrieveArtifact(T repositorySetup, String groupName,
			String artifactName) {
		return this.retrieveArtifact(repositorySetup, groupName, artifactName,
				new IModelItemListener[0]);
	}

	
	@SuppressWarnings("unchecked")
	public Artifact retrieveArtifact(T repositorySetup, String groupName,
			String artifactName, IModelItemListener... modelItemListeners) {
		Artifact artifact = new Artifact(artifactName);
		AbstractCrawledRepositorySetup setup = repositorySetup.getMutable();
		setup.getGroupFilters().remove(
				AbstractCrawledRepositorySetup.KEEP_ALL_PATTERN);
		setup.getGroupFilters().add(groupName);
		return refreshArtifact(artifact, (T) setup, groupName, artifactName,
				modelItemListeners);
	}

	public Set<ArtifactVersion> retrieveArtifactVersions(T repositorySetup,
			String groupName, String artifactName, String artifactVersion) {
		Set<ArtifactVersion> result = new LinkedHashSet<ArtifactVersion>();
		repositoryBrowserPlugin.checkRepositorySetup(repositorySetup);
		repositoryBrowserPlugin.init(repositorySetup);

		// create the requested URL
		String requestedUrl = repositoryBrowserPlugin.buildUrl(repositorySetup,
				makeGroupPathOutOfGroupName(groupName), artifactName);
		// get the entries from the listing page
		try {
			Queue<Entry> entryList = repositoryBrowserPlugin
					.getEntryList(requestedUrl);
			entryList = RepositoryCrawlerHelper.getVersionsFolders(entryList);
			Queue<Entry> chosenVersionsList = new LinkedBlockingQueue<Entry>();
			for (Entry entry : entryList) {
				if (entry.getResolvedName().startsWith(artifactVersion)) {
					chosenVersionsList.add(entry);
				}
			}
			result = generateArtifactVersions(repositorySetup, groupName + "."
					+ artifactName, requestedUrl, chosenVersionsList);
		} catch (IOException e) {
			throw new RepositoryCrawlingException(e);
		}
		return result;
	}

	private String makeGroupPathOutOfGroupName(String groupName) {
		String groupPath = groupName;
		if (!groupName.equals("")) {
			if (!groupName.contains("/")) {// probably already a group path
				groupPath = groupName.replace(".*", "##notlikely##");
				groupPath = groupPath.replace('.', '/');
				groupPath = groupPath.replace("##notlikely##", ".*");
			}
		}
		return groupPath;
	}

	private String makeGroupNameOutOfGroupPath(String groupPath) {
		String groupName = groupPath;
		if (!groupPath.equals("")) {
			groupName = groupPath.replace('/', '.');
			if (groupName.startsWith(".")) {
				groupName = groupName.substring(1);
			}
			if (groupName.endsWith(".")) {
				groupName = groupName.substring(0, groupName.length() - 1);
			}
		}
		return groupName;
	}

	/**
	 * @return Returns the interruptionFlag.
	 */
	public InterruptionFlag getInterruptionFlag() {
		return this.interruptionFlag;
	}

	public ArchivesSpecification getArchivesSpecification() {
		return archivesSpecification;
	}

	public void setArchivesSpecification(
			ArchivesSpecification archivesSpecification) {
		this.archivesSpecification = archivesSpecification;
	}

	public LibrariesSpecification getLibrariesSpecification() {
		return librariesSpecification;
	}

	public void setLibrariesSpecification(
			LibrariesSpecification librariesSpecification) {
		this.librariesSpecification = librariesSpecification;
	}

	public PomSpecification getPomSpecification() {
		return pomSpecification;
	}

	public void setPomSpecification(PomSpecification pomSpecification) {
		this.pomSpecification = pomSpecification;
	}
}