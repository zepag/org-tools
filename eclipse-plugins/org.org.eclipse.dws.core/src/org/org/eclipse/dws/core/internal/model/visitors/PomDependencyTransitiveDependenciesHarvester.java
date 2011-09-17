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

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.org.eclipse.dws.core.internal.PomInteractionHelper;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelUtils;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.PomDependency;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions.Filter;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions.ScopeFilter;
import org.org.model.IModelItem;
import org.org.model.IModelItemVisitor;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * The Class PomDependencyTransitiveDependenciesHarvester.
 */
public class PomDependencyTransitiveDependenciesHarvester implements IModelItemVisitor {

	/** The filtering options. */
	private PomDependenciesFilteringOptions filteringOptions;

	/**
	 * Instantiates a new pom dependency transitive dependencies harvester.
	 * 
	 * @param filteringOptions
	 *            the filtering options
	 */
	public PomDependencyTransitiveDependenciesHarvester(PomDependenciesFilteringOptions filteringOptions) {
		this.filteringOptions = filteringOptions;
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
		if (modelItem instanceof PomDependency) {
			PomDependency pomDependency = (PomDependency) modelItem;
			if (!(filteringOptions.getScopeFilter() == ScopeFilter.FILTER_NARROW_SCOPES && !(pomDependency.getScope() == null) && !(pomDependency.getScope().equals(PomDependency.Scope.COMPILE)))) {
				List<ArtifactVersion> artifactVersions = RepositoryModelUtils.findArtifactVersionsMatchingPomDependencyInRepositories(pomDependency, filteringOptions.getArtifactExtensions());
				for (ArtifactVersion artifactVersion : artifactVersions) {
					if (artifactVersion.getPomUrl() != null) {
						Set<PomDependency> pomDependencies = null;
						CrawledRepository crawledRepository = (CrawledRepository) artifactVersion.getParent().getParent().getParent();
						InputStream pomStream = PomInteractionHelper.getPomStream(crawledRepository.getRepositorySetup(), artifactVersion.getPomUrl());
						Pom pom = PomInteractionHelper.parsePom(pomStream);
						pomDependencies = new LinkedHashSet<PomDependency>(pom.getChildren());
						if (filteringOptions.getFilter() == Filter.CONFLICTING) {
							PomDependenciesFilteredDependenciesRemover pomDependenciesFilteredDependenciesRemover = new PomDependenciesFilteredDependenciesRemover(filteringOptions);
							pom.accept(pomDependenciesFilteredDependenciesRemover);
						}
						for (PomDependency transitiveDependency : pomDependencies) {
							pomDependency.addChild(transitiveDependency);
						}
					}
				}
			}
		}
		return true;
	}
}