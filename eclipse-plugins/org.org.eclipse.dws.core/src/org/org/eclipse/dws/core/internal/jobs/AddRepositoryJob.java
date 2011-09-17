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
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.model.IModelItemListener;
import org.org.model.ModelItemEvent;
import org.org.model.RootModelItem;
import org.org.repository.crawler.IExternalInterruptionFlagSetter;
import org.org.repository.crawler.InterruptionFlag;
import org.org.repository.crawler.items.IFileSystemCrawledRepositorySetup;
import org.org.repository.crawler.items.IHttpCrawledRepositorySetup;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * The Class AddRepositoryJob.
 */
public final class AddRepositoryJob extends Job {

	/**
	 * The Class JobUIlInterruptionFlagSetter.
	 */
	private static class JobUIlInterruptionFlagSetter implements IExternalInterruptionFlagSetter {

		/** The monitor. */
		final IProgressMonitor monitor;

		/**
		 * Instantiates a new job u il interruption flag setter.
		 * 
		 * @param monitor
		 *            the monitor
		 */
		private JobUIlInterruptionFlagSetter(IProgressMonitor monitor) {
			super();
			this.monitor = monitor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.repository.crawler.IExternalInterruptionFlagSetter#processStatus()
		 */
		/**
		 * @see org.org.repository.crawler.IExternalInterruptionFlagSetter#processStatus()
		 */
		public InterruptionFlag processStatus() {
			InterruptionFlag flag = new InterruptionFlag();
			if (monitor.isCanceled()) {
				flag.setCurrentStatus(InterruptionFlag.STOP);
			}
			return flag;
		}
	}

	/**
	 * The listener interface for receiving newRepositoryMonitoring events. The class that is interested in processing a newRepositoryMonitoring event implements this interface, and the object created with that class is registered with a component using the component's <code>addNewRepositoryMonitoringListener<code> method. When
	 * the newRepositoryMonitoring event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see NewRepositoryMonitoringEvent
	 */
	private static class NewRepositoryMonitoringListener implements IModelItemListener {

		/** The monitor. */
		final IProgressMonitor monitor;

		/**
		 * Instantiates a new new crawledRepository monitoring listener.
		 * 
		 * @param monitor
		 *            the monitor
		 */
		private NewRepositoryMonitoringListener(IProgressMonitor monitor) {
			super();
			this.monitor = monitor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.model.IModelItemListener#changeOccured(org.org.model.ModelItemEvent)
		 */
		/**
		 * @see org.org.model.IModelItemListener#changeOccured(org.org.model.ModelItemEvent)
		 */
		public void changeOccured(ModelItemEvent modelItemEvent) {
			if (modelItemEvent.getEventType() == ModelItemEvent.EventType.PRE_ADD_CHILD) {
				monitor.subTask("Adding " + modelItemEvent.getTargetItem().getUID() + " to " + modelItemEvent.getSourceItem().getUID());
				monitor.worked(1);
				DWSCorePlugin.getDefault().notifyRepositoryModelUpdate(modelItemEvent.getTargetItem());
			}
		}
	}

	/** The logger. */
	private Logger logger = Logger.getLogger(AddRepositoryJob.class);

	/** The crawledRepository. */
	private final CrawledRepository crawledRepository;

	/** The Constant JOB_ID. */
	private static final String JOB_ID = "DWS : Adding Maven 2 crawledRepository ";

	/**
	 * Instantiates a new adds the crawledRepository job.
	 * 
	 * @param crawledRepository
	 *            the crawledRepository
	 */
	public AddRepositoryJob(CrawledRepository crawledRepository) {
		super(JOB_ID + crawledRepository.getUID());
		this.crawledRepository = crawledRepository;
		this.setPriority(Job.LONG);
		this.setUser(true);
		this.setRule(new BatchSimilarRule(Maven2Jobs.MAVEN2_REFRESH_JOB_FAMILY));
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
	public IStatus run(IProgressMonitor monitor) {
		StatusInfo result = new StatusInfo(IStatus.OK, "New crawledRepository browsed successfully.");
		try {
			if (this.crawledRepository.getRepositorySetup() instanceof IHttpCrawledRepositorySetup) {
				IHttpCrawledRepositorySetup httpCrawledRepositorySetup = (IHttpCrawledRepositorySetup) crawledRepository.getRepositorySetup();
				RepositoryModelPersistence.addRepositoryNameAutocompleteProposal(this.crawledRepository.getLabel());
				RepositoryModelPersistence.addHttpBrowsedRepositoryAutocompleteProposal(httpCrawledRepositorySetup.getBaseUrl());
				if (httpCrawledRepositorySetup.getPatternSet().getEntryPattern() != null) {
					RepositoryModelPersistence.addEntryPatternAutocompleteProposal(httpCrawledRepositorySetup.getPatternSet().getEntryPattern());
				}
				if (httpCrawledRepositorySetup.getPatternSet().getParentDirectoryPattern() != null) {
					RepositoryModelPersistence.addParentPatternAutocompleteProposal(httpCrawledRepositorySetup.getPatternSet().getParentDirectoryPattern());
				}
				if (httpCrawledRepositorySetup.getProxyHost() != null) {
					RepositoryModelPersistence.addProxyHostAutocompleteProposal(httpCrawledRepositorySetup.getProxyHost());
				}
				if (httpCrawledRepositorySetup.getProxyPort() != null && httpCrawledRepositorySetup.getProxyPort() != 0) {
					RepositoryModelPersistence.addProxyPortAutocompleteProposal("" + httpCrawledRepositorySetup.getProxyPort());
				}
			} else if (this.crawledRepository.getRepositorySetup() instanceof IFileSystemCrawledRepositorySetup) {
				IFileSystemCrawledRepositorySetup fileSystemCrawledRepositorySetup = (IFileSystemCrawledRepositorySetup) crawledRepository.getRepositorySetup();
				RepositoryModelPersistence.addRepositoryNameAutocompleteProposal(fileSystemCrawledRepositorySetup.getId());
				RepositoryModelPersistence.addFSRepositoryAutocompleteProposal(fileSystemCrawledRepositorySetup.getBasePath());
			}
			monitor.beginTask("Retrieving ...", 1000);
			IModelItemListener modelItemListener = new NewRepositoryMonitoringListener(monitor);
			RootModelItem<CrawledRepository> rootModelItem = RootModelItem.<CrawledRepository> getInstance(ModelConstants.REPOSITORIES_ROOT);
			rootModelItem.addChild(this.crawledRepository);
			IExternalInterruptionFlagSetter externalInterruptionFlagSetter = new JobUIlInterruptionFlagSetter(monitor);
			RepositoryModelPersistence.refreshRepository(this.crawledRepository, externalInterruptionFlagSetter, modelItemListener);
			RepositoryModelPersistence.saveRepositoryInfo();
			DWSCorePlugin.getDefault().notifyRepositoryModelUpdate(rootModelItem);
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

	/*
	 * (non-Javadoc)
	 * 
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