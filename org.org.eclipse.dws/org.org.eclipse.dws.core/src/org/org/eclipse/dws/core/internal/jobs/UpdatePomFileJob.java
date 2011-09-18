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
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.internal.PomInteractionHelper;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.PomDependency;


/**
 * The Class UpdatePomFileJob.
 */
public final class UpdatePomFileJob extends Job {

	/** The logger. */
	private Logger logger = Logger.getLogger(UpdatePomFileJob.class);

	/** The project name. */
	private final String projectName;

	/** The pom dependencies. */
	private Set<PomDependency> pomDependencies;

	/** The updated pom file. */
	private IFile updatedPomFile;

	/** The Constant JOB_ID. */
	private static final String JOB_ID = "DWS : Updating POM file for project ";

	/**
	 * Instantiates a new update pom file job.
	 * 
	 * @param updatedPomFile the updated pom file
	 * @param pomDependencies the pom dependencies
	 */
	public UpdatePomFileJob(IFile updatedPomFile, Set<PomDependency> pomDependencies) {
		super(JOB_ID + updatedPomFile);
		this.projectName = updatedPomFile.getProject().getName();
		this.updatedPomFile = updatedPomFile;
		this.pomDependencies = pomDependencies;
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
		StatusInfo result = new StatusInfo(IStatus.OK, "Updated POM.");
		try {

			// create a sample file
			monitor.beginTask("Creating pom.xml", 2);
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject selectedProject = root.getProject(projectName);
			if (!selectedProject.exists()) {
				throw new Exception("Project \"" + projectName + "\" does not exist.");
			}

			try {
				InputStream stream = updatedPomFile.getContents();
				Pom pom = PomInteractionHelper.parsePom(stream);
				Set<PomDependency> dependenciesToRemove = new LinkedHashSet<PomDependency>();
				for (PomDependency pomDependency : pom.getChildren()) {
					final String comparisonString = pomDependency.getGroupId() + pomDependency.getArtifactId() + pomDependency.getVersion() + pomDependency.getClassifier();
					for (PomDependency dependency : pomDependencies) {
						if (comparisonString.equals(dependency.getGroupId() + dependency.getArtifactId() + dependency.getVersion() + dependency.getClassifier())) {
							dependenciesToRemove.add(dependency);
							break;
						}
					}
				}
				pomDependencies.removeAll(dependenciesToRemove);
				stream.close();
				stream = null;
				if (updatedPomFile.exists()) {
					stream = updatedPomFile.getContents();
					String resultingPomContents = PomInteractionHelper.addPomDependenciesToPom(stream, pomDependencies, AggregatedProperties.getPomEncoding(selectedProject));
					updatedPomFile.setContents(new ByteArrayInputStream(resultingPomContents.getBytes()), true, true, monitor);
					stream.close();
				} else {
					throw new IOException("Pom file Doesn't exist.");
				}

			} catch (IOException e) {
			}
			monitor.worked(1);
			monitor.setTaskName("Opening file for editing...");
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IDE.openEditor(page, updatedPomFile, true);
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