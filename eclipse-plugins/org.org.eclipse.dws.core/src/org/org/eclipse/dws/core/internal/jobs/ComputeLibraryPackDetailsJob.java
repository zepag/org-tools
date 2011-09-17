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
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPack;

public final class ComputeLibraryPackDetailsJob extends Job {
	private static final String JOB_ID = "DWS: computing Library Pack details";
	private final LibraryPack libraryPack;
	private StringBuilder formattedLibraryPack;

	// private Class<? extends ICrawledRepositorySetup> repositorySetupType;
	// private StringBuilder formattedPatterns;

	public ComputeLibraryPackDetailsJob(final LibraryPack libraryPack) {
		super(JOB_ID);
		this.setSystem(true);
		this.libraryPack = libraryPack;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		formattedLibraryPack = new StringBuilder("<form><p>");
		formattedLibraryPack.append("Name:<b> " + libraryPack.getLabel() + "</b><br />");
		formattedLibraryPack.append("Description:<br /><b>" + libraryPack.getDescription() + "</b><br />");
		formattedLibraryPack.append("</p></form>");

		return new Status(IStatus.OK, DWSCorePlugin.PI_MAVEN2, "DWS: details view refreshed");
	}

	public StringBuilder getFormattedLibraryPack() {
		return formattedLibraryPack;
	}
}