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
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;

public final class ComputeArtifactDetailsJob extends Job {
	private static final String JOB_ID = "DWS: computing artifact details";
	private final Artifact artifact;
	private StringBuilder formattedArtifact;

	public ComputeArtifactDetailsJob(final Artifact artifact) {
		super(JOB_ID);
		this.setSystem(true);
		this.artifact = artifact;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		formattedArtifact = new StringBuilder("<form><p>");
		formattedArtifact.append("Group:<b> " + artifact.getParent().getName() + "</b><br />");
		formattedArtifact.append("Artifact:<b>" + artifact.getId() + "</b><br />");
		formattedArtifact.append("Available versions:</p>");
		for (ArtifactVersion artifactVersion : artifact.getChildren()) {
			formattedArtifact.append("<li>" + artifactVersion.getVersion() + (artifactVersion.getClassifier() == null ? "" : "-" + artifactVersion.getClassifier()) + "</li>");
		}
		formattedArtifact.append("</form>");
		return new Status(IStatus.OK, DWSCorePlugin.PI_MAVEN2, "DWS: details view refreshed");
	}

	public StringBuilder getFormattedArtifact() {
		return formattedArtifact;
	}
}