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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.DWSCorePlugin;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelPersistence;
import org.org.model.IModelItemListener;
import org.org.model.ModelItemEvent;
import org.org.repository.crawler.IExternalInterruptionFlagSetter;
import org.org.repository.crawler.InterruptionFlag;
import org.org.repository.crawler.maven2.model.CrawledRepository;


/**
 * The Class AddPreciseArtifactToRepositoryJob.
 */
public class AddPreciseArtifactToRepositoryJob extends Job {

	/**
	 * The Class ArtifactDescription.
	 */
	public static class ArtifactDescription {
		
		/** The group id. */
		private final String groupId;

		/** The artifact id. */
		private final String artifactId;

		/**
		 * Gets the artifact id.
		 * 
		 * @return the artifact id
		 */
		public String getArtifactId() {
			return artifactId;
		}

		/**
		 * Gets the group id.
		 * 
		 * @return the group id
		 */
		public String getGroupId() {
			return groupId;
		}

		/**
		 * Instantiates a new artifact description.
		 * 
		 * @param groupId the group id
		 * @param artifactId the artifact id
		 */
		public ArtifactDescription(String groupId, String artifactId) {
			super();
			this.groupId = groupId;
			this.artifactId = artifactId;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return groupId+"."+artifactId;
		}
	}

	/** The logger. */
	private Logger logger = Logger.getLogger(AddPreciseArtifactToRepositoryJob.class);

	/** The crawledRepository. */
	private final CrawledRepository crawledRepository;

	/** The group id. */
	private final String groupId;

	/** The artifact id. */
	private final String artifactId;

	/** The Constant JOB_ID. */
	private static final String JOB_ID = "DWS: adding artifact ";

	/**
	 * Instantiates a new adds the precise artifact to crawledRepository job.
	 * 
	 * @param crawledRepository the crawledRepository
	 * @param groupId the group id
	 * @param artifactId the artifact id
	 */
	public AddPreciseArtifactToRepositoryJob(CrawledRepository crawledRepository, String groupId, String artifactId) {
		super(JOB_ID + groupId + "." + artifactId + " to " + crawledRepository.getUID());
		this.crawledRepository = crawledRepository;
		this.groupId = groupId;
		this.artifactId = artifactId;
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
		IStatus result = new StatusInfo(IStatus.OK, "Artifact addition successful");
		try {
			monitor.beginTask("Adding artifact", 1000);
			final IProgressMonitor fMonitor = monitor;
			IModelItemListener modelItemListener = new IModelItemListener() {
				public void changeOccured(ModelItemEvent modelItemEvent) {
					if (modelItemEvent.getEventType() == ModelItemEvent.EventType.PRE_ADD_CHILD) {
						if (modelItemEvent.getTargetItem() != null && modelItemEvent.getSourceItem() != null) {
							fMonitor.subTask("Adding " + modelItemEvent.getTargetItem().getUID() + " to " + modelItemEvent.getSourceItem().getUID());
							fMonitor.worked(1);
						}
						DWSCorePlugin.getDefault().notifyRepositoryModelUpdate(modelItemEvent.getTargetItem());
					}
				}
			};
			IExternalInterruptionFlagSetter externalInterruptionFlagSetter = new IExternalInterruptionFlagSetter() {

				public InterruptionFlag processStatus() {
					InterruptionFlag flag = new InterruptionFlag();
					if (fMonitor.isCanceled()) {
						flag.setCurrentStatus(InterruptionFlag.STOP);
					}
					return flag;
				}

			};
			RepositoryModelPersistence.addPreciseArtifactToRepository(crawledRepository, groupId, artifactId, externalInterruptionFlagSetter, modelItemListener,true);
			if (monitor.isCanceled()) {
				result = new StatusInfo(IStatus.WARNING, "CrawledRepository browsing was canceled, it may not be complete.");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			monitor.setCanceled(true);
			result = new StatusInfo(IStatus.ERROR, "CrawledRepository browsing error: " + e.getMessage());
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