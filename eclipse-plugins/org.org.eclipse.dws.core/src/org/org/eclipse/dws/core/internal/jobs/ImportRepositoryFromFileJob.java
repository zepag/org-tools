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

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.DWSCorePlugin;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelPersistence;


/**
 * The Class ImportRepositoryFromFileJob.
 */
public class ImportRepositoryFromFileJob extends Job {

	/** The file name. */
	private final String fileName;

	/** The Constant JOB_ID. */
	private static final String JOB_ID = "DWS: importing repositories definition from file: ";

	/**
	 * Instantiates a new import repository from file job.
	 * 
	 * @param fileName the file name
	 */
	public ImportRepositoryFromFileJob(String fileName) {
		super(JOB_ID + fileName);
		this.fileName = fileName;
		this.setPriority(Job.INTERACTIVE);
		this.setUser(true);
		this.setRule(new BatchSimilarRule(Maven2Jobs.MAVEN2_REFRESH_JOB_FAMILY));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	/**
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus result = new StatusInfo(IStatus.OK, "Imported repositories definition successfully.");
		try {
			File tmpFile = new File(fileName);
			RepositoryModelPersistence.importRepositoryInfo(tmpFile);
			if (RepositoryModelPersistence.getWorkspacePersistencesStatus().equals(RepositoryModelPersistence.OUT_OF_SYNC)) {
				DWSCorePlugin.getDefault().notifyRepositoryModelUpdate(null);
			}
		} catch (Exception e) {
			result = new StatusInfo(IStatus.ERROR, "Error while importing repositories definition : \"" + fileName + "\"  failed: " + e.getMessage());
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
