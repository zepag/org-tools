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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.internal.PomInteractionHelper;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelPersistence;
import org.org.eclipse.dws.core.internal.model.PomCreationDescription;


/**
 * The Class NewPomFileJob.
 */
public final class NewPomFileJob extends Job {

	/** The logger. */
	private Logger logger = Logger.getLogger(NewPomFileJob.class);

	/** The project name. */
	private final String projectName;

	/** The pom creation description. */
	private PomCreationDescription pomCreationDescription;

	/** The Constant JOB_ID. */
	private static final String JOB_ID = "DWS : New POM file for project ";

	/**
	 * Instantiates a new new pom file job.
	 * 
	 * @param projectName the project name
	 * @param pomCreationDescription the pom creation description
	 */
	public NewPomFileJob(String projectName, PomCreationDescription pomCreationDescription) {
		super(JOB_ID + projectName);

		this.projectName = projectName;
		this.pomCreationDescription = pomCreationDescription;
		this.setPriority(Job.INTERACTIVE);
		this.setUser(true);
		this.setRule(new BatchSimilarRule(Maven2Jobs.MAVEN2_OTHER_JOB_FAMILY));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	/**
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus run(IProgressMonitor monitor) {
		StatusInfo result = new StatusInfo(IStatus.OK, "New POM [" + pomCreationDescription.getGroupId() + ":" + pomCreationDescription.getArtifactId() + ":" + pomCreationDescription.getVersion() + ":" + pomCreationDescription.getPackaging() + "']");
		try {
			RepositoryModelPersistence.addGroupIdAutocompleteProposal(pomCreationDescription.getGroupId());
			RepositoryModelPersistence.addArtifactIdAutocompleteProposal(pomCreationDescription.getArtifactId());
			// create a sample file
			monitor.beginTask("Creating pom.xml", 2);
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject selectedProject = root.getProject(projectName);
			if (!selectedProject.exists()) {
				throw new Exception("Project \"" + pomCreationDescription.getArtifactId() + "\" does not exist.");
			}
			final IFile file = selectedProject.getFile(new Path("pom.xml"));
			try {
				InputStream stream = openContentStream(PomInteractionHelper.getPomContents(pomCreationDescription));
				if (file.exists()) {
					file.setContents(stream, true, true, monitor);
				} else {
					file.create(stream, true, monitor);
				}
				stream.close();
			} catch (IOException e) {
			}
			monitor.worked(1);
			monitor.setTaskName("Opening file for editing...");
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IDE.openEditor(page, file, true);
					} catch (PartInitException e) {
					}
				}
			});
			monitor.worked(1);
		} catch (Exception e) {
			logger.error(e.getMessage());
			monitor.setCanceled(true);
			result = new StatusInfo(IStatus.ERROR, "POM file creation error: " + e.getMessage());
		} finally {
			monitor.done();
		}
		return result;
	}

	/**
	 * We will initialize file contents with a sample text.
	 * 
	 * @param contents the contents
	 * 
	 * @return the input stream
	 */

	private InputStream openContentStream(String contents) {
		return new ByteArrayInputStream(contents.getBytes());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
	 */
	/**
	 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
	 */
	@Override
	public boolean belongsTo(Object family) {
		return Maven2Jobs.MAVEN2_OTHER_JOB_FAMILY.equals(family);
	}
}