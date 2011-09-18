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
package org.org.eclipse.dws.core.internal.model.visitors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.org.eclipse.dws.core.internal.PomInteractionHelper;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelUtils;
import org.org.eclipse.dws.core.internal.model.DWSClasspathEntryDescriptor;
import org.org.eclipse.dws.core.internal.model.PomDependency;
import org.org.model.IModelItem;
import org.org.model.IModelItemVisitor;
import org.org.repository.crawler.maven2.model.ArtifactVersion;


/**
 * The Class PomDependenciesFilteredDependenciesRemover.
 */
public final class PomDependenciesFilteredDependenciesRemover implements IModelItemVisitor {

	/** The filtering options. */
	private PomDependenciesFilteringOptions filteringOptions;

	/**
	 * Instantiates a new pom dependencies filtered dependencies remover.
	 * 
	 * @param filteringOptions the filtering options
	 */
	public PomDependenciesFilteredDependenciesRemover(PomDependenciesFilteringOptions filteringOptions) {
		this.filteringOptions = filteringOptions;
	}

	/* (non-Javadoc)
	 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
	 */
	/**
	 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
	 */
	@SuppressWarnings("rawtypes")
	public boolean visit(IModelItem modelItem) {
		if (modelItem instanceof PomDependency) {
			if (filteringOptions.getProjectClasspathEntries() != null) {
				for (DWSClasspathEntryDescriptor projectClasspathEntry : filteringOptions.getProjectClasspathEntries()) {
					if (projectClasspathEntry.isValid()) {
						final PomDependency pomDependency = (PomDependency) modelItem;
						Set<DWSClasspathEntryDescriptor> conflictingClasspathEntries = new HashSet<DWSClasspathEntryDescriptor>();
						if (PomInteractionHelper.isClasspathEntryFromSameArtifact(projectClasspathEntry, pomDependency)) {
							String versionInClasspath = PomInteractionHelper.extractVersionIfAny(projectClasspathEntry, pomDependency);
							List<ArtifactVersion> artifactVersions = RepositoryModelUtils.findArtifactVersionsMatchingPomDependencyInRepositories(pomDependency, filteringOptions.getArtifactExtensions());
							for (ArtifactVersion artifactVersion : artifactVersions) {
								String versionFromRepo = PomInteractionHelper.formatToSnapshotIfNecessary(artifactVersion.getVersion());
								int comparisonResult = versionFromRepo.compareTo(versionInClasspath);
								if (comparisonResult > 0) {
									// version in the classpath is older than the one in dependencies... remove it
									conflictingClasspathEntries.add(projectClasspathEntry);
								} else if (comparisonResult == 0) {
									// version is already in the classpath... remove the dependency
									pomDependency.getParent().removeChild(pomDependency.getUID());
								} else if (comparisonResult < 0) {
									// version in the dependencies is older than the one in the classpath... do nothing...
									// the user will handle it...
								}
							}
						}
						if (!conflictingClasspathEntries.isEmpty()) {
							pomDependency.setConflictingClasspathEntries(conflictingClasspathEntries);
						}
					}
				}
			}
		}
		return true;
	}

}