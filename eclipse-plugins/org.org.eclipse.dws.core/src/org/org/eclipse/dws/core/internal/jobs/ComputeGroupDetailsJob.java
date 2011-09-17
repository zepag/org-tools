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
import org.org.repository.crawler.maven2.model.Group;

public final class ComputeGroupDetailsJob extends Job {
	private static final String JOB_ID = "DWS: computing group details";
	private final Group group;
	private StringBuilder formattedGroup;

	public ComputeGroupDetailsJob(final Group group) {
		super(JOB_ID);
		this.setSystem(true);
		this.group = group;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		formattedGroup = new StringBuilder("<form><p>");
		formattedGroup.append("Group:<b> " + group.getName() + "</b><br />");
		formattedGroup.append("Available artifacts:</p>");
		for (Artifact artifact : group.getChildren()) {
			formattedGroup.append("<li>" + artifact.getId() + "</li>");
		}
		formattedGroup.append("</form>");
		return new Status(IStatus.OK, DWSCorePlugin.PI_MAVEN2, "DWS: details view refreshed");
	}

	public StringBuilder getFormattedGroup() {
		return formattedGroup;
	}
}