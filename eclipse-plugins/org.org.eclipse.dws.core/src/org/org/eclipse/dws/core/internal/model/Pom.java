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
package org.org.eclipse.dws.core.internal.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.org.eclipse.dws.core.internal.bridges.RepositoryModelUtils;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteredDependenciesRemover;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions.Filter;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions.ScopeFilter;
import org.org.model.AbstractModelItem;
import org.org.model.IModelItem;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.Group;

@SuppressWarnings("unchecked")
public class Pom extends AbstractModelItem<IModelItem, PomDependency> {

	private ParentPom parentPom;

	private String groupId;

	private String artifactId;

	private String version;

	private String packaging;

	private String description;

	private String name;

	final private PomRepositoriesSet pomRepositoriesSet = new PomRepositoriesSet();

	final private PomProfilesSet pomProfilesSet = new PomProfilesSet();

	final private PomPropertiesSet properties = new PomPropertiesSet();

	/**
	 * @return
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * @param artifactId
	 */
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	/**
	 * @return
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @see java.lang.Comparable
	 */
	@Override
	public int compareTo(IModelItem o) {
		Pom pomCreationDescription = (Pom) o;
		return String.CASE_INSENSITIVE_ORDER.compare(groupId + artifactId + version, pomCreationDescription.groupId + pomCreationDescription.artifactId + pomCreationDescription.version);
	}

	@Override
	public String getUID() {
		return this.getGroupId() + ":" + this.getArtifactId() + ":" + this.getVersion() + ":" + this.getPackaging();
	}

	@Override
	public StringBuilder toStringBuilderDescription() {
		return new StringBuilder(this.getUID());
	}

	public String getPackaging() {
		return packaging;
	}

	public void setPackaging(String archiveType) {
		this.packaging = archiveType;
	}

	public ParentPom getParentPom() {
		return parentPom;
	}

	public void setParentPom(ParentPom parentPom) {
		this.parentPom = parentPom;
	}

	public void filterDependencies(PomDependenciesFilteringOptions filteringOptions) {
		PomDependenciesFilteredDependenciesRemover pomDependenciesFilteredDependenciesRemover = new PomDependenciesFilteredDependenciesRemover(filteringOptions);
		accept(pomDependenciesFilteredDependenciesRemover);
	}

	public boolean areAllDependenciesOptional() {
		boolean result = true;
		for (PomDependency pomDependency : getChildren()) {
			result = pomDependency.isOptional() && pomDependency.areTransitiveAllOptional();
		}
		return result;
	}

	public boolean areAllDependenciesRisky() {
		boolean result = true;
		for (PomDependency pomDependency : getChildren()) {
			result = pomDependency.isScopeNarrow() && pomDependency.areTransitiveAllNarrowScoped();
		}
		return result;
	}

	private static boolean testIfSkipped(String groupId, String artifactId, Set<SkippedDependency> skippedDependencies) {
		boolean result = false;
		for (SkippedDependency skippedDependency : skippedDependencies) {
			if (groupId.equals(skippedDependency.getGroupId()) && artifactId.equals(skippedDependency.getArtifactId())) {
				result = true;
				break;
			}
		}
		return result;
	}

	public Set<AbstractChosenArtifactVersion> computeLibrariesFromPomDependencies(PomDependenciesFilteringOptions filteringOptions) {
		return computeLibrariesFromPomDependencies(getChildren(), filteringOptions);
	}

	private Set<AbstractChosenArtifactVersion> computeLibrariesFromPomDependencies(Set<PomDependency> pomDependencies, PomDependenciesFilteringOptions filteringOptions) {
		Set<AbstractChosenArtifactVersion> result = null;
		if (hasChildren()) {
			result = new TreeSet<AbstractChosenArtifactVersion>();
			Set<ResolvedArtifact> resolvedArtifacts = new HashSet<ResolvedArtifact>();
			for (PomDependency pomDependency : pomDependencies) {
				List<ArtifactVersion> artifactVersionsFromRepositories = RepositoryModelUtils.findArtifactVersionsMatchingPomDependencyInRepositories(pomDependency, filteringOptions.getArtifactExtensions());
				if (artifactVersionsFromRepositories.size() > 0) {
					for (ArtifactVersion artifactVersion : artifactVersionsFromRepositories) {
						ResolvedArtifact listElement = new ResolvedArtifact();
						listElement.setArtifactVersion(artifactVersion);
						listElement.setScope(pomDependency.getScope());
						listElement.setOptional(pomDependency.isOptional());
						listElement.setSystemPath(pomDependency.getSystemPath());
						Artifact artifact = artifactVersion.getParent();
						Group group = artifact.getParent();
						listElement.setSkipped(testIfSkipped(group.getName(), artifact.getId(), filteringOptions.getSkippedDependencies()));
						if (pomDependency.isConflictingWithClasspathEntries()) {

							listElement.setConflictingClasspathEntries(pomDependency.getConflictingClasspathEntries());
						}
						resolvedArtifacts.add(listElement);
						if (artifactVersion.getPomUrl() != null && filteringOptions.dealWithTransitive()) {
							PomDependenciesFilteringOptions derivedOptions = new PomDependenciesFilteringOptions.Builder(filteringOptions).scopeFilter(ScopeFilter.FILTER_NARROW_SCOPES).filter(Filter.CONFLICTING).build();
							pomDependency.retrieveTransitiveDependencies(derivedOptions);
							Set<PomDependency> transitiveDependencies = pomDependency.getChildren();
							Set<AbstractChosenArtifactVersion> abstractChosenArtifactVersions = computeLibrariesFromPomDependencies(transitiveDependencies, filteringOptions);
							if (abstractChosenArtifactVersions != null) {
								for (AbstractChosenArtifactVersion abstractChosenArtifactVersion : abstractChosenArtifactVersions) {
									listElement.addChild(abstractChosenArtifactVersion);
								}
							}
						}
						// ADDING THE RESOLVED ARTIFACT TO THE LIST
						result.add(listElement);
					}
				} else {
					UnresolvedArtifact listElement = new UnresolvedArtifact();
					listElement.setUnresolvedPomDependency(pomDependency);
					listElement.setScope(pomDependency.getScope());
					listElement.setOptional(pomDependency.isOptional());
					listElement.setSkipped(testIfSkipped(pomDependency.getGroupId(), pomDependency.getArtifactId(), filteringOptions.getSkippedDependencies()));
					// ADDING THE UNRESOLVED ARTIFACT TO THE LIST
					result.add(listElement);
				}
			}
		}
		return result;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PomPropertiesSet getProperties() {
		return properties;
	}

	public PomRepositoriesSet getRepositories() {
		return pomRepositoriesSet;
	}

	public PomProfilesSet getProfiles() {
		return pomProfilesSet;
	}
}