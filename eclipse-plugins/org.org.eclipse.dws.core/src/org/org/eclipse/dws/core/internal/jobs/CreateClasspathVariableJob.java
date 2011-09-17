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

import java.text.MessageFormat;
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
 * The Class CreateClasspathVariableJob.
 */
public class CreateClasspathVariableJob extends Job {

	/** The Constant JOB_ID. */
	private static final String JOB_ID = "DWS: Change Classpath Variable";

	/**
	 * Instantiates a new creates the classpath variable job.
	 */
	public CreateClasspathVariableJob() {
		super(JOB_ID);
		this.setPriority(Job.SHORT);
		this.setUser(true);
		this.setRule(new BatchSimilarRule(Maven2Jobs.MAVEN2_OTHER_JOB_FAMILY));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	/**
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus result = new StatusInfo(IStatus.OK, "Variable creation:+\n.");
		try {
			final List<String> modifiedVariables = new ArrayList<String>();
			if (!ProjectInteractionHelper.canBindDWSClasspathVariable(null, new NullProgressMonitor())) {
				modifiedVariables.add(ProjectInteractionHelper.createDWSClasspathVariableIfNotExists(null, new NullProgressMonitor()));
			}
			for (IJavaProject project : getAvailableProjects()) {
				if (!ProjectInteractionHelper.canBindDWSClasspathVariable(project, new NullProgressMonitor())) {
					modifiedVariables.add(ProjectInteractionHelper.createDWSClasspathVariableIfNotExists(project, new NullProgressMonitor()));
				}
			}
			StringBuilder buffer = new StringBuilder();
			if (modifiedVariables.size() > 0) {
				buffer.append("Added/Modified the following variables:");
				for (String modifiedVariable : modifiedVariables) {
					buffer.append("\n- " + modifiedVariable);
				}
			} else {
				buffer.append("No variable to modify.");
			}
			result = new StatusInfo(IStatus.OK, "Variable creation:+\n." + buffer);
		} catch (Throwable e) {
			monitor.setCanceled(true);
			result = new StatusInfo(IStatus.ERROR, "A problem occured while adding libraries to a project:\n\n" + MessageFormat.format("Unattended Exception occured : [{0} :{1}]", new Object[] { e.getClass().getName(), e.getMessage(), e }));
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