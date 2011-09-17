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
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelUtils;
import org.org.eclipse.dws.core.internal.bridges.ProjectInteractionHelper.ProjectInteractionException;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.maven2.DownloadedFilesWrapper;
import org.org.maven2.IEventCallback;
import org.org.maven2.MavenRepositoryInteractionHelper;
import org.org.maven2.MavenRepositoryInteractionHelper.MavenRepositoryInteractionEvent;
import org.org.maven2.MavenRepositoryInteractionHelper.MavenRepositoryInteractionException;
import org.org.model.IModelItem;
import org.org.repository.crawler.maven2.model.ArtifactVersion;

/**
 * The Class DownloadToLocalRepositoryJob.
 */
public class DownloadToLocalRepositoryJob extends Job {

	/** The artifact versions. */
	private Set<ArtifactVersion> artifactVersions;

	/** The model item. */
	@SuppressWarnings("unchecked")
	private IModelItem modelItem;

	/** The Constant JOB_ID. */
	private final static String JOB_ID = "DWS: Downloading to local repository ";

	/**
	 * Instantiates a new download to local repository job.
	 * 
	 * @param modelItem
	 *            the model item
	 * @param artifactVersions
	 *            the artifact versions
	 */
	@SuppressWarnings("unchecked")
	public DownloadToLocalRepositoryJob(IModelItem modelItem, Set<ArtifactVersion> artifactVersions) {
		super(JOB_ID);
		this.setPriority(Job.LONG);
		this.setUser(true);
		this.artifactVersions = artifactVersions;
		this.modelItem = modelItem;
		this.setRule(new BatchSimilarRule(Maven2Jobs.MAVEN2_DOWNLOAD_JOB_FAMILY));
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
		IStatus result = new StatusInfo(IStatus.OK, "Download of libraries found under item: \"" + modelItem.getUID() + "\" was successful.");
		try {
			download(artifactVersions, monitor);
			if (monitor.isCanceled()) {
				result = new StatusInfo(IStatus.WARNING, "Download of libraries found under item: \"" + modelItem.getUID() + "\" was canceled. It may not be complete.");
			}
		} catch (Exception e) {
			result = new StatusInfo(IStatus.ERROR, "Download of libraries found under item: \"" + modelItem.getUID() + "\" failed:" + e.getMessage());
		} finally {
			monitor.done();
		}
		return result;
	}

	/**
	 * Download.
	 * 
	 * @param artifactVersions
	 *            the artifact versions
	 * @param monitor
	 *            the monitor
	 */
	private void download(Set<ArtifactVersion> artifactVersions, final IProgressMonitor monitor) {
		List<IPath> librariesPath = new ArrayList<IPath>();
		monitor.beginTask("Downloading libraries from the repositories.", artifactVersions.size());
		Set<ArtifactVersion> notDownloaded = new HashSet<ArtifactVersion>();
		for (ArtifactVersion artifactVersion : artifactVersions) {
			if (!monitor.isCanceled()) {
				String localRepositoryPreference = AggregatedProperties.getLocalRepository(null);
				File toFile = new Path(localRepositoryPreference).toFile();
				if (toFile != null) {
					try {
						File repositoryFolder = toFile;
						monitor.subTask("Downloading " + artifactVersion.getUID() + " to " + repositoryFolder.toString());
						Proxy proxy = RepositoryModelUtils.determineProxy(artifactVersion);
						DownloadedFilesWrapper downloadedFilesWrapper = MavenRepositoryInteractionHelper.downloadArtifactToLocalRepository(artifactVersion, toFile, proxy, new IEventCallback() {
							public void onEvent(MavenRepositoryInteractionEvent event) {
								if (event.getEventType() == MavenRepositoryInteractionEvent.Type.START_TASK) {
									monitor.subTask(event.getMessage());
								} else if (event.getEventType() == MavenRepositoryInteractionEvent.Type.STOP_TASK) {
									monitor.worked(1);
								}
							}
						});
						monitor.worked(1);
						if (downloadedFilesWrapper.getArtifactVersionFile() != null) {
							librariesPath.add(new Path(downloadedFilesWrapper.getArtifactVersionFile().getAbsolutePath()));
						}
						if (downloadedFilesWrapper.getPomFile() != null) {
							librariesPath.add(new Path(downloadedFilesWrapper.getPomFile().getAbsolutePath()));
						}
						if (downloadedFilesWrapper.getJavadocFile() != null) {
							librariesPath.add(new Path(downloadedFilesWrapper.getJavadocFile().getAbsolutePath()));
						}
						if (downloadedFilesWrapper.getSourcesFile() != null) {
							librariesPath.add(new Path(downloadedFilesWrapper.getSourcesFile().getAbsolutePath()));
						}
					} catch (MavenRepositoryInteractionException e) {
						notDownloaded.add(artifactVersion);
					}
				} else {
					throw new ProjectInteractionException("the target file :[" + localRepositoryPreference + "] could not be created.");
				}
			}
		}
		if (notDownloaded.size() > 0) {
			StringBuilder builder = new StringBuilder();
			for (ArtifactVersion artifactVersion : notDownloaded) {
				builder.append(artifactVersion.getId() + ":" + artifactVersion.getUrl() + "\n");
			}
			throw new MavenRepositoryInteractionException("The following libraries could not be downloaded: \n" + builder);
		}
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
		return Maven2Jobs.MAVEN2_DOWNLOAD_JOB_FAMILY.equals(family);
	}
}