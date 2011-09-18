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
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.DWSCorePlugin;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelPersistence;
import org.org.model.IModelItemListener;
import org.org.model.ModelItemEvent;
import org.org.repository.crawler.maven2.model.Artifact;


/**
 * The Class RefreshArtifactJob.
 */
public class RefreshArtifactJob extends Job {
	
	/** The artifact. */
	private final Artifact artifact;

	/** The Constant JOB_ID. */
	private static final String JOB_ID = "DWS: refreshing artifact ";

	/**
	 * Instantiates a new refresh artifact job.
	 * 
	 * @param artifact the artifact
	 */
	public RefreshArtifactJob(Artifact artifact) {
		super(JOB_ID + artifact.getId());
		this.artifact = artifact;
		this.setPriority(Job.SHORT);
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
		IStatus result = new StatusInfo(IStatus.OK, "Refresh of artifact: \"" + artifact.getUID() + "\" was successful.");
		try {
			monitor.beginTask("Refreshing artifact " + artifact.getUID(), 1000);
			final IProgressMonitor fMonitor = monitor;
			IModelItemListener modelItemListener = new IModelItemListener() {
				public void changeOccured(ModelItemEvent modelItemEvent) {
					if (modelItemEvent.getEventType() == ModelItemEvent.EventType.PRE_ADD_CHILD) {
						fMonitor.subTask("Adding " + modelItemEvent.getTargetItem().getUID() + " to " + modelItemEvent.getSourceItem().getUID());
						fMonitor.worked(1);
						DWSCorePlugin.getDefault().notifyRepositoryModelUpdate(modelItemEvent.getTargetItem());
					}
				}
			};
			// RootModelItem.<CrawledRepository>getInstance(ModelConstants.REPOSITORIES_ROOT).addListener(modelItemListener);
			RepositoryModelPersistence.refreshArtifact(artifact,modelItemListener);
			// RootModelItem.<CrawledRepository>getInstance(ModelConstants.REPOSITORIES_ROOT).removeListener(modelItemListener);
			if (monitor.isCanceled()) {
				result = new StatusInfo(IStatus.WARNING, "Refresh of artifact: \"" + artifact.getUID() + "\" was canceled. It may not be complete.");
			}
		} catch (Exception e) {
			result = new StatusInfo(IStatus.ERROR, "Refresh of artifact: \"" + artifact.getUID() + "\"  failed: " + e.getMessage());
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
		return Maven2Jobs.MAVEN2_REFRESH_JOB_FAMILY.equals(family);
	}
}