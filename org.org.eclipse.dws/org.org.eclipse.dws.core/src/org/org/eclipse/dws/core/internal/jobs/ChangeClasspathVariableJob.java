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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;
import org.org.eclipse.core.utils.jdt.tools.JavaProjectClasspathHelper;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;


/**
 * The Class ChangeClasspathVariableJob.
 */
public class ChangeClasspathVariableJob extends Job {
	
	/** The Constant JOB_ID. */
	private static final String JOB_ID = "DWS: Change Classpath Variable";
	
	/** The original variable. */
	private final String originalVariable;
	
	/** The target variable. */
	private final String targetVariable;

	/**
	 * Instantiates a new change classpath variable job.
	 * 
	 * @param originalVariable the original variable
	 * @param targetVariable the target variable
	 */
	public ChangeClasspathVariableJob(String originalVariable, String targetVariable) {
		super(JOB_ID);
		this.originalVariable = originalVariable;
		this.targetVariable = targetVariable;
		this.setPriority(Job.SHORT);
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
		IStatus result = new StatusInfo(IStatus.OK, "Replaced variable " + originalVariable + " with " + targetVariable + ".");
		StringBuilder errorMessage = new StringBuilder();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : workspaceRoot.getProjects()) {
			try {
				if (project.isAccessible() && project.hasNature(JavaCore.NATURE_ID)) {
					monitor.beginTask("Changing variable " + originalVariable + " to " + targetVariable + " in project " + project.getName(), 1);
					JavaProjectClasspathHelper.changeClasspathVariable(originalVariable, targetVariable, JavaCore.create(project), monitor);
				}
			} catch (Throwable e) {
				errorMessage.append("\t- " + e.getMessage() + "\n");
			} finally {
				monitor.worked(1);
			}
		}
		if (errorMessage.length() > 0) {
			monitor.setCanceled(true);
			result = new StatusInfo(IStatus.ERROR, "A problem occured while adding libraries to a project:\n\n" + errorMessage);
		}
		monitor.done();
		return result;
	}
}