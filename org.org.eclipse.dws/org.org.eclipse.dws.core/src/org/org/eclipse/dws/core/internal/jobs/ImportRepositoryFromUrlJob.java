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

import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.tools.IOToolBox;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.DWSCorePlugin;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelPersistence;


/**
 * The Class ImportRepositoryFromUrlJob.
 */
public class ImportRepositoryFromUrlJob extends Job {

	/** The url string. */
	private final String urlString;

	/** The Constant JOB_ID. */
	private static final String JOB_ID = "DWS: importing repositories definition from Url: ";

	/**
	 * Instantiates a new import repository from url job.
	 * 
	 * @param urlString the url string
	 */
	public ImportRepositoryFromUrlJob(String urlString) {
		super(JOB_ID + urlString);
		this.urlString = urlString;
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
			URL url = new URL(urlString);
			Proxy proxy = IOToolBox.determineProxy(url);
			InputStream inputStream = url.openConnection(proxy).getInputStream();
			RepositoryModelPersistence.importRepositoryInfo(inputStream);
			if (RepositoryModelPersistence.getWorkspacePersistencesStatus().equals(RepositoryModelPersistence.OUT_OF_SYNC)) {
				DWSCorePlugin.getDefault().notifyRepositoryModelUpdate(null);
			}
		} catch (Exception e) {
			result = new StatusInfo(IStatus.ERROR, "Error while importing repositories definition : \"" + urlString + "\"  failed: " + e.getMessage());
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
