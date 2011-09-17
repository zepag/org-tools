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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.internal.bridges.ProjectInteractionHelper;


/**
 * The Class UpdateDWSClasspathVariableJob.
 */
public class UpdateDWSClasspathVariableJob extends Job {
	
	/** The Constant JOB_ID. */
	private static final String JOB_ID = "DWS: Change Classpath Variable";

	/**
	 * Instantiates a new update dws classpath variable job.
	 */
	public UpdateDWSClasspathVariableJob() {
		super(JOB_ID);
		this.setPriority(Job.LONG);
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
	protected IStatus run(IProgressMonitor monitor) {
		IStatus result = new StatusInfo(IStatus.OK, "No variable to modify.");
		StringBuilder errorMessage = new StringBuilder();
		StringBuilder successMessage = new StringBuilder("Updated the following variables:");
		try {
			if (ProjectInteractionHelper.canBindDWSClasspathVariable(null, new NullProgressMonitor())) {
				successMessage.append("\n\t- " + ProjectInteractionHelper.createDWSClasspathVariableIfNotExists(null, new NullProgressMonitor()));
			}
		} catch (Throwable e) {
			errorMessage.append("\t- " + e.getMessage() + "\n");
		}
		for (IJavaProject project : getAvailableProjects()) {
			try {
				if (ProjectInteractionHelper.canBindDWSClasspathVariable(project, new NullProgressMonitor())) {
					successMessage.append("\n\t- " + ProjectInteractionHelper.createDWSClasspathVariableIfNotExists(project, new NullProgressMonitor()));
				}
			} catch (Throwable e) {
				errorMessage.append("\t- " + e.getMessage() + "\n");
			}
		}

		if (errorMessage.length() > 0) {
			monitor.setCanceled(true);
			result = new StatusInfo(IStatus.ERROR, "A problem occured while adding libraries to a project:\n\n" + errorMessage);
		} else {
			if (successMessage.length() > 0) {
				result = new StatusInfo(IStatus.OK, successMessage.toString());
			}
		}
		monitor.done();
		return result;
	}

	/**
	 * Gets the available projects.
	 * 
	 * @return the available projects
	 */
	private List<IJavaProject> getAvailableProjects() {
		List<IJavaProject> result = new ArrayList<IJavaProject>();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = workspaceRoot.getProjects();
		for (IProject project : projects) {
			try {
				if (project.isAccessible() && !project.isPhantom() && project.hasNature(JavaCore.NATURE_ID)) {
					result.add(JavaCore.create(project));
				}
			} catch (CoreException e) {
				// Do something deeply meaningful here ;)
			}
		}
		return result;
	}
}