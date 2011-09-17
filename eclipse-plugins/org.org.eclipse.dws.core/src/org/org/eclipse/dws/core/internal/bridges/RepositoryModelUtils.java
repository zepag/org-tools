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
package org.org.eclipse.dws.core.internal.bridges;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.org.eclipse.core.utils.platform.tools.IOToolBox;
import org.org.eclipse.dws.core.internal.PomInteractionHelper;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.eclipse.dws.core.internal.model.AbstractChosenArtifactVersion;
import org.org.eclipse.dws.core.internal.model.DWSClasspathEntryDescriptor;
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.eclipse.dws.core.internal.model.PomDependency;
import org.org.eclipse.dws.core.internal.model.ResolvedArtifact;
import org.org.eclipse.dws.core.internal.model.PomDependency.Scope;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions.Filter;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions.ScopeFilter;
import org.org.eclipse.dws.core.internal.versioning.DefaultArtifactVersion;
import org.org.eclipse.dws.core.internal.versioning.Restriction;
import org.org.eclipse.dws.core.internal.versioning.VersionRange;
import org.org.model.IModelItem;
import org.org.model.IModelItemVisitor;
import org.org.model.RootModelItem;
import org.org.repository.crawler.items.IHttpCrawledRepositorySetup;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.Group;
import org.org.repository.crawler.maven2.model.CrawledRepository;
import org.org.repository.crawler.maven2.model.ArtifactVersion.Type;

/**
 * The Class RepositoryModelUtils.
 */
public final class RepositoryModelUtils {

	/**
	 * The Class ArtifactVersionHarvesterVisitor.
	 */
	public static class ArtifactVersionHarvesterVisitor implements IModelItemVisitor {

		/** The artifact extensions. */
		private Set<String> artifactExtensions;

		/** The version range. */
		private VersionRange versionRange;

		/** The result. */
		private List<ArtifactVersion> result;

		/** The group id. */
		private String groupId;

		/** The artifact id. */
		private String artifactId;

		/** The classifier. */
		private String classifier;

		/**
		 * Instantiates a new artifact version harvester visitor.
		 * 
		 * @param groupId
		 *            the group id
		 * @param artifactId
		 *            the artifact id
		 * @param versionRange
		 *            the version range
		 * @param classifier
		 *            the classifier
		 * @param artifactExtensions
		 *            the artifact extensions
		 */
		public ArtifactVersionHarvesterVisitor(String groupId, String artifactId, VersionRange versionRange, String classifier, Set<String> artifactExtensions) {
			super();
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.versionRange = versionRange;
			this.classifier = classifier;
			this.artifactExtensions = artifactExtensions;
			result = new LinkedList<ArtifactVersion>();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		/**
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		@SuppressWarnings("unchecked")
		public boolean visit(IModelItem modelItem) {
			boolean keepOnVisiting = true;
			if (modelItem instanceof ArtifactVersion) {
				ArtifactVersion artifactVersion = (ArtifactVersion) modelItem;
				if (isExtensionOk(artifactExtensions, artifactVersion)) {
					String version = artifactVersion.getVersion();
					// IF VERSION RANGES ARE DEFINED
					if (versionRange.hasRestrictions()) {
						// CYCLE THROUGH THE RESTRICTIONS FOR VALIDITY
						boolean validVersion = true;
						for (Restriction restriction : versionRange.getRestrictions()) {
							validVersion = validVersion && restriction.containsVersion(new DefaultArtifactVersion(version));
						}
						if (validVersion) {
							// IF NO VERSION ADDED YET, ADD DIRECTLY
							if (result.isEmpty()) {
								result.add(artifactVersion);
							}
							// IF ANOTHER VERSION ALREADY ADDED, CHOOSE THE APPROPRIATE (E.G. MOST RECENT) ONE
							else {
								List<ArtifactVersion> olderVersions = new ArrayList<ArtifactVersion>();
								List<ArtifactVersion> recentVersions = new ArrayList<ArtifactVersion>();
								for (ArtifactVersion tmpArtifactVersion : result) {
									int comparisonResult = tmpArtifactVersion.getVersion().compareTo(artifactVersion.getVersion());
									if (comparisonResult > 0 || comparisonResult == 0) {
										// do not bother adding an older or equal version...
									} else if (comparisonResult < 0) {
										// remove from repositorySetup the older version...
										olderVersions.add(tmpArtifactVersion);
										// add to repositorySetup the older version...
										recentVersions.add(artifactVersion);
									}
								}
								result.removeAll(olderVersions);
								result.addAll(recentVersions);
							}
						}
					}
					// IF A SOFT REQUIREMENT IS DEFINED
					else {
						if (versionRange.getRecommendedVersion() == null || artifactVersion.getVersion().equals(versionRange.getRecommendedVersion().toString())) {
							if (classifier != null) {
								if (artifactVersion.getClassifier() != null && artifactVersion.getClassifier().equals(classifier)) {
									result.add(artifactVersion);
								}
							} else {
								result.add(artifactVersion);
							}
						}
					}
				}
			} else {
				boolean badBranch = false;
				if (modelItem instanceof Group) {
					Group group = (Group) modelItem;
					badBranch = !group.getName().equals(groupId);
				}
				if (modelItem instanceof Artifact) {
					Artifact artifact = (Artifact) modelItem;
					badBranch = !artifact.getId().equals(artifactId);
				}
				if (badBranch) {
					keepOnVisiting = false;
				}
			}
			return keepOnVisiting;
		}

		/**
		 * Gets the artifact versions.
		 * 
		 * @return the artifact versions
		 */
		public List<ArtifactVersion> getArtifactVersions() {
			return result;
		}

	}

	/**
	 * Instantiates a new repository model utils.
	 */
	private RepositoryModelUtils() {
	}

	/**
	 * Gets the child for element.
	 * 
	 * @param parent
	 *            the parent
	 * @param element
	 *            the element
	 * 
	 * @return the child for element
	 */
	@SuppressWarnings("unchecked")
	public static IModelItem getChildForElement(IModelItem parent, IModelItem element) {
		while (element != null) {
			IModelItem elementParent = element.getParent();
			if (parent.equals(elementParent)) {
				return element;
			}
			element = elementParent;
		}
		return null;
	}

	/**
	 * Look for artifact version.
	 * 
	 * @param parent
	 *            the parent
	 * @param pomDependency
	 *            the pom dependency
	 * @param artifactExtensions
	 *            the artifact extensions
	 * 
	 * @return the list< artifact version>
	 */
	@SuppressWarnings("unchecked")
	private static List<ArtifactVersion> lookForArtifactVersion(IModelItem parent, PomDependency pomDependency, Set<String> artifactExtensions) {
		ArtifactVersionHarvesterVisitor visitor = new ArtifactVersionHarvesterVisitor(pomDependency.getGroupId(), pomDependency.getArtifactId(), VersionRange.createFromVersionSpec(pomDependency.getVersion()), pomDependency.getClassifier(), artifactExtensions);
		parent.accept(visitor);
		return visitor.getArtifactVersions();

	}

	/**
	 * Look for artifact version in all repositories.
	 * 
	 * @param criterion
	 *            the criterion
	 * 
	 * @return the list< artifact version>
	 */
	public static List<ArtifactVersion> lookForArtifactVersionInAllRepositories(final String criterion) {
		final List<ArtifactVersion> result = new ArrayList<ArtifactVersion>();
		RootModelItem.<CrawledRepository> getInstance(ModelConstants.REPOSITORIES_ROOT).accept(new IModelItemVisitor() {

			@SuppressWarnings("unchecked")
			public boolean visit(IModelItem modelItem) {
				if (modelItem instanceof ArtifactVersion) {
					if (((ArtifactVersion) modelItem).getType() == Type.LIBRARY) {
						if (((ArtifactVersion) modelItem).getId().contains(criterion)) {
							result.add((ArtifactVersion) modelItem);
						}
					}
				}
				return true;
			}

		});
		return result;
	}

	/**
	 * Checks if is extension ok.
	 * 
	 * @param extensionList
	 *            the extension list
	 * @param artifactVersion
	 *            the artifact version
	 * 
	 * @return true, if is extension ok
	 */
	public static boolean isExtensionOk(Set<String> extensionList, ArtifactVersion artifactVersion) {
		boolean result = false;
		for (String extension : extensionList) {
			result = result || (artifactVersion.getId().endsWith(extension));
		}
		return result;
	}

	/**
	 * Compute libraries from artifact versions.
	 * 
	 * @param artifactVersions
	 *            the artifact versions
	 * 
	 * @return the set< abstract chosen artifact version>
	 */
	public static Set<AbstractChosenArtifactVersion> computeLibrariesFromArtifactVersions(List<ArtifactVersion> artifactVersions) {
		Set<AbstractChosenArtifactVersion> result = null;
		if (artifactVersions.size() > 0) {
			result = new TreeSet<AbstractChosenArtifactVersion>();
			for (ArtifactVersion artifactVersion : artifactVersions) {
				ResolvedArtifact listElement = new ResolvedArtifact();
				listElement.setArtifactVersion(artifactVersion);
				listElement.setScope(Scope.COMPILE);
				listElement.setOptional(Boolean.valueOf(false));
				result.add(listElement);
			}
		}
		return result;
	}

	/**
	 * Find artifact versions matching pom dependency in repositories.
	 * 
	 * @param pomDependency
	 *            the pom dependency
	 * @param artifactExtensions
	 *            the artifact extensions
	 * 
	 * @return the list< artifact version>
	 */
	public static List<ArtifactVersion> findArtifactVersionsMatchingPomDependencyInRepositories(PomDependency pomDependency, Set<String> artifactExtensions) {
		List<ArtifactVersion> resultList = RepositoryModelUtils.lookForArtifactVersion(RootModelItem.<CrawledRepository> getInstance(ModelConstants.REPOSITORIES_ROOT), pomDependency, artifactExtensions);
		return resultList;
	}

	/**
	 * Gets the transitive dependencies description.
	 * 
	 * @param artifactVersion
	 *            the artifact version
	 * 
	 * @return the transitive dependencies description
	 */
	public static StringBuilder getTransitiveDependenciesDescription(ArtifactVersion artifactVersion) {
		Artifact artifact = artifactVersion.getParent();
		Group group = artifact.getParent();

		PomDependency pomDependency = new PomDependency();
		pomDependency.setGroupId(group.getName());
		pomDependency.setArtifactId(artifact.getId());
		pomDependency.setVersion(artifactVersion.getVersion());
		PomDependenciesFilteringOptions.Builder optionsBuilder = new PomDependenciesFilteringOptions.Builder();
		optionsBuilder.projectClasspathEntries(Collections.<DWSClasspathEntryDescriptor> emptySet());
		optionsBuilder.scopeFilter(ScopeFilter.NONE);
		optionsBuilder.filter(Filter.NONE);
		optionsBuilder.dealWithTransitive(AggregatedProperties.getDealWithTransitive(null));
		optionsBuilder.skippedDependencies(AggregatedProperties.getSkippedDependencies(null));
		optionsBuilder.artifactExtensions(AggregatedProperties.getArtifactExtensions());
		pomDependency.retrieveTransitiveDependencies(optionsBuilder.build());
		final StringBuilder result = new StringBuilder("");
		PomInteractionHelper.getDescription(pomDependency, result, new StringBuilder(""));
		return result;
	}

	/**
	 * Gets the transitive groups.
	 * 
	 * @param artifactVersion
	 *            the artifact version
	 * 
	 * @return the transitive groups
	 */
	public static Set<String> getTransitiveGroups(ArtifactVersion artifactVersion) {
		Artifact artifact = artifactVersion.getParent();
		Group group = artifact.getParent();

		PomDependency pomDependency = new PomDependency();
		pomDependency.setGroupId(group.getName());
		pomDependency.setArtifactId(artifact.getId());
		pomDependency.setVersion(artifactVersion.getVersion());
		PomDependenciesFilteringOptions.Builder optionsBuilder = new PomDependenciesFilteringOptions.Builder();
		optionsBuilder.projectClasspathEntries(Collections.<DWSClasspathEntryDescriptor> emptySet());
		optionsBuilder.scopeFilter(ScopeFilter.NONE);
		optionsBuilder.filter(Filter.NONE);
		optionsBuilder.dealWithTransitive(AggregatedProperties.getDealWithTransitive(null));
		optionsBuilder.skippedDependencies(AggregatedProperties.getSkippedDependencies(null));
		optionsBuilder.artifactExtensions(AggregatedProperties.getArtifactExtensions());
		pomDependency.retrieveTransitiveDependencies(optionsBuilder.build());
		class GroupHarvesterVisitor implements IModelItemVisitor {

			private Set<String> result;

			public GroupHarvesterVisitor() {
				result = new HashSet<String>();
			}

			@SuppressWarnings("unchecked")
			public boolean visit(IModelItem modelItem) {
				if (modelItem instanceof PomDependency) {
					result.add(((PomDependency) modelItem).getGroupId());
				}
				return true;
			}

			public Set<String> getResult() {
				return result;
			}

		}
		GroupHarvesterVisitor visitor = new GroupHarvesterVisitor();
		pomDependency.accept(visitor);
		return visitor.getResult();
	}

	/**
	 * Gets the transitive dependencies exclusions.
	 * 
	 * @param artifactVersion
	 *            the artifact version
	 * 
	 * @return the transitive dependencies exclusions
	 */
	public static String getTransitiveDependenciesExclusions(ArtifactVersion artifactVersion) {
		final StringBuilder result = new StringBuilder("");
		Artifact artifact = artifactVersion.getParent();
		Group group = artifact.getParent();
		PomDependency pomDependency = new PomDependency();
		pomDependency.setGroupId(group.getName());
		pomDependency.setArtifactId(artifact.getId());
		pomDependency.setVersion(artifactVersion.getVersion());
		PomDependenciesFilteringOptions.Builder optionsBuilder = new PomDependenciesFilteringOptions.Builder();
		optionsBuilder.projectClasspathEntries(Collections.<DWSClasspathEntryDescriptor> emptySet());
		optionsBuilder.scopeFilter(ScopeFilter.NONE);
		optionsBuilder.filter(Filter.NONE);
		optionsBuilder.dealWithTransitive(AggregatedProperties.getDealWithTransitive(null));
		optionsBuilder.skippedDependencies(AggregatedProperties.getSkippedDependencies(null));
		optionsBuilder.artifactExtensions(AggregatedProperties.getArtifactExtensions());
		pomDependency.retrieveTransitiveDependencies(optionsBuilder.build());
		PomInteractionHelper.getExclusions(pomDependency, result);
		return result.toString();
	}

	/**
	 * Gets the transitive dependencies flat.
	 * 
	 * @param artifactVersion
	 *            the artifact version
	 * 
	 * @return the transitive dependencies flat
	 */
	public static Set<PomDependency> getTransitiveDependenciesFlat(ArtifactVersion artifactVersion) {
		Artifact artifact = artifactVersion.getParent();
		Group group = artifact.getParent();

		PomDependency pomDependency = new PomDependency();
		pomDependency.setGroupId(group.getName());
		pomDependency.setArtifactId(artifact.getId());
		pomDependency.setVersion(artifactVersion.getVersion());
		PomDependenciesFilteringOptions.Builder optionsBuilder = new PomDependenciesFilteringOptions.Builder();
		optionsBuilder.projectClasspathEntries(Collections.<DWSClasspathEntryDescriptor> emptySet());
		optionsBuilder.scopeFilter(ScopeFilter.NONE);
		optionsBuilder.filter(Filter.NONE);
		optionsBuilder.dealWithTransitive(AggregatedProperties.getDealWithTransitive(null));
		optionsBuilder.skippedDependencies(AggregatedProperties.getSkippedDependencies(null));
		optionsBuilder.artifactExtensions(AggregatedProperties.getArtifactExtensions());
		pomDependency.retrieveTransitiveDependencies(optionsBuilder.build());
		class GroupHarvesterVisitor implements IModelItemVisitor {

			private Set<PomDependency> result;

			public GroupHarvesterVisitor() {
				result = new HashSet<PomDependency>();
			}

			@SuppressWarnings("unchecked")
			public boolean visit(IModelItem modelItem) {
				if (modelItem instanceof PomDependency) {
					result.add((PomDependency) modelItem);
				}
				return true;
			}

			public Set<PomDependency> getResult() {
				return result;
			}

		}
		GroupHarvesterVisitor visitor = new GroupHarvesterVisitor();
		pomDependency.accept(visitor);
		return visitor.getResult();
	}

	/**
	 * Determine proxy.
	 * 
	 * @param crawledRepository
	 *            the repository
	 * 
	 * @return the proxy
	 */
	public static Proxy determineProxy(CrawledRepository crawledRepository) {
		Proxy proxy = null;
		if (crawledRepository.getRepositorySetup() instanceof IHttpCrawledRepositorySetup) {
			IHttpCrawledRepositorySetup httpBrowsedRepositorySetup = (IHttpCrawledRepositorySetup) crawledRepository.getRepositorySetup();
			if (httpBrowsedRepositorySetup.getProxyHost() != null) {
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpBrowsedRepositorySetup.getProxyHost(), httpBrowsedRepositorySetup.getProxyPort()));
			} else {
				try {
					IOToolBox.determineProxy(new URL(httpBrowsedRepositorySetup.getBaseUrl()));
				} catch (MalformedURLException e) {
					proxy = Proxy.NO_PROXY;
				}
			}
		} else {
			proxy = Proxy.NO_PROXY;
		}
		return proxy;
	}

	public static Proxy determineProxy(ArtifactVersion artifactVersion) {
		return IOToolBox.determineProxy(artifactVersion.getUrl());
	}

}
