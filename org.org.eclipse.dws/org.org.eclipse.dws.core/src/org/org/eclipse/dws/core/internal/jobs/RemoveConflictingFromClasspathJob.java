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

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.internal.bridges.ProjectInteractionHelper;
import org.org.eclipse.dws.core.internal.model.DWSClasspathEntryDescriptor;


/**
 * The Class RemoveConflictingFromClasspathJob.
 */
public class RemoveConflictingFromClasspathJob extends Job {
	
	/** The project. */
	private IProject project;

	/** The conflicting entries. */
	private Set<DWSClasspathEntryDescriptor> conflictingEntries;

	/** The Constant JOB_ID. */
	private final static String JOB_ID = "DWS: Removing conflicting libraries from classpath of project ";

	/**
	 * Instantiates a new removes the conflicting from classpath job.
	 * 
	 * @param project the project
	 * @param conflictingEntries the conflicting entries
	 */
	public RemoveConflictingFromClasspathJob(IProject project, Set<DWSClasspathEntryDescriptor> conflictingEntries) {
		super(JOB_ID + project.getName());
		this.setPriority(Job.LONG);
		this.setUser(true);
		this.project = project;
		this.conflictingEntries = conflictingEntries;
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
		StatusInfo result = new StatusInfo(IStatus.OK, "Maven library retrieved");
		try {
			ProjectInteractionHelper.removeFromClasspath(conflictingEntries, project, monitor);
		} catch (Exception e) {
			result = new StatusInfo(IStatus.WARNING, "A problem occured:" + e.getMessage());
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