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
package org.org.eclipse.dws.core.internal.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.dws.core.DWSCorePlugin;
import org.org.eclipse.dws.core.internal.PomInteractionHelper;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelUtils;
import org.org.repository.crawler.maven2.model.ArtifactVersion;

public final class ComputeArtifactVersionDetailsJob extends Job {
	private static final String JOB_ID = "DWS: computing artifact version details";
	private final ArtifactVersion artifactVersion;
	private String artifactVersionDetails;
	private String dependencyXML;
	private String transitiveDependenciesExclusions;

	public ComputeArtifactVersionDetailsJob(final ArtifactVersion artifactVersion) {
		super(JOB_ID);
		this.setSystem(true);
		this.artifactVersion = artifactVersion;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		StringBuilder formattedArtifactVersion = new StringBuilder("<form><p>");
		formattedArtifactVersion.append("Type:<b> " + artifactVersion.getType().toString() + "</b><br />");
		formattedArtifactVersion.append("Group:<b> " + artifactVersion.getParent().getParent().getName() + "</b><br />");
		formattedArtifactVersion.append("Artifact:<b>" + artifactVersion.getParent().getId() + "</b><br />");
		formattedArtifactVersion.append("Version:<b>" + artifactVersion.getVersion() + "</b><br />");
		formattedArtifactVersion.append("Classifier:<b>" + (artifactVersion.getClassifier() == null ? "N/A" : artifactVersion.getClassifier()) + "</b><br />");
		formattedArtifactVersion.append("Javadoc:<b>" + (artifactVersion.getJavadocUrl() == null ? "N/A" : "Available") + "</b><br />");
		formattedArtifactVersion.append("Sources:<b>" + (artifactVersion.getSourcesUrl() == null ? "N/A" : "Available") + "</b><br />");
		formattedArtifactVersion.append("File:<b> " + artifactVersion.getId() + "</b><br />");
		formattedArtifactVersion.append("</p></form>");
		artifactVersionDetails = formattedArtifactVersion.toString();
		dependencyXML = PomInteractionHelper.toDependencyXML(artifactVersion);
		try {
			transitiveDependenciesExclusions = RepositoryModelUtils.getTransitiveDependenciesExclusions(artifactVersion);
		} catch (Exception e) {
			// if impossible to create exclusions, forget it
			transitiveDependenciesExclusions = "[Impossible to retrieve dependencies exclusions:" + e.getMessage() + "]";
		}
		return new Status(IStatus.OK, DWSCorePlugin.PI_MAVEN2, "DWS: details view refreshed");
	}

	public String getFormattedArtifactVersion() {
		return artifactVersionDetails;
	}

	public String getDependencyXML() {
		return dependencyXML;
	}

	public String getTransitiveDependenciesExclusions() {
		return transitiveDependenciesExclusions;
	}
}