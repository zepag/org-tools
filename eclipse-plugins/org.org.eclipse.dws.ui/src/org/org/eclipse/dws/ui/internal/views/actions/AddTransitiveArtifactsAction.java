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
package org.org.eclipse.dws.ui.internal.views.actions;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelUtils;
import org.org.eclipse.dws.core.internal.jobs.AddMultipleArtifactsToRepositoryJob;
import org.org.eclipse.dws.core.internal.jobs.AddPreciseArtifactToRepositoryJob.ArtifactDescription;
import org.org.eclipse.dws.core.internal.model.PomDependency;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * The Class AddTransitiveArtifactsAction.
 */
public class AddTransitiveArtifactsAction extends AbstractDWSViewAction {

	/**
	 * Instantiates a new adds the transitive artifacts action.
	 * 
	 * @param actionHost
	 *            the action host
	 */
	public AddTransitiveArtifactsAction(IActionHost actionHost) {
		super(actionHost);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) getActionHost().getActionTrigger();
		Object obj = (selection).getFirstElement();
		if (obj instanceof ArtifactVersion) {
			ArtifactVersion artifactVersion = (ArtifactVersion) selection.getFirstElement();

			Set<PomDependency> pomDependencies = RepositoryModelUtils.getTransitiveDependenciesFlat(artifactVersion);
			Set<ArtifactDescription> artifactDescriptions = new TreeSet<ArtifactDescription>(new Comparator<ArtifactDescription>() {

				public int compare(ArtifactDescription o1, ArtifactDescription o2) {
					String toCompare1 = o1.getGroupId() + "." + o1.getArtifactId();
					String toCompare2 = o2.getGroupId() + "." + o2.getArtifactId();
					return toCompare1.compareTo(toCompare2);
				}

			});
			for (PomDependency pomDependency : pomDependencies) {
				artifactDescriptions.add(new ArtifactDescription(pomDependency.getGroupId(), pomDependency.getArtifactId()));
			}
			CrawledRepository repository = (CrawledRepository) artifactVersion.getParent().getParent().getParent();
			AddMultipleArtifactsToRepositoryJob addPreciseArtifactToRepositoryJob = new AddMultipleArtifactsToRepositoryJob(repository, artifactDescriptions);
			addPreciseArtifactToRepositoryJob.schedule();
		}
	}
}
