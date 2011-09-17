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
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.internal.bridges.ProjectInteractionHelper;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelUtils;
import org.org.eclipse.dws.core.internal.bridges.ProjectInteractionHelper.ProjectInteractionException;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.eclipse.dws.core.internal.model.ResolvedArtifact;
import org.org.maven2.DownloadedFilesWrapper;
import org.org.maven2.IEventCallback;
import org.org.maven2.MavenRepositoryInteractionHelper;
import org.org.maven2.MavenRepositoryInteractionHelper.MavenRepositoryInteractionEvent;
import org.org.maven2.MavenRepositoryInteractionHelper.MavenRepositoryInteractionException;
import org.org.repository.crawler.maven2.model.ArtifactVersion;


/**
 * The Class DownloadAndAddToFolderJob.
 */
public class DownloadAndAddToFolderJob extends Job {

	/** The project. */
	private IProject project;

	/** The folder. */
	private IFolder folder;

	/** The libraries. */
	private Set<ResolvedArtifact> libraries;

	/** The Constant JOB_ID. */
	private final static String JOB_ID = "DWS: Adding libraries to folder ";

	/**
	 * Instantiates a new download and add to folder job.
	 * 
	 * @param project the project
	 * @param folder the folder
	 * @param libraries the libraries
	 */
	public DownloadAndAddToFolderJob(IProject project, IFolder folder, Set<ResolvedArtifact> libraries) {
		super(JOB_ID + folder.getName());
		this.setPriority(Job.LONG);
		this.setUser(true);
		this.project = project;
		this.folder = folder;
		this.libraries = libraries;
		this.setRule(new BatchSimilarRule(Maven2Jobs.MAVEN2_DOWNLOAD_JOB_FAMILY));
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
			downloadAndAddToFolder(libraries, folder, project, monitor);
		} catch (ProjectInteractionException e) {
			result = new StatusInfo(IStatus.ERROR, "Problem while interacting with the project :" + e.getMessage());
		} catch (MavenRepositoryInteractionException e) {
			result = new StatusInfo(IStatus.ERROR, "Problem while interacting with the repository :" + e.getMessage());
		} finally {
			monitor.done();
		}
		return result;
	}

	/**
	 * Download and add to folder.
	 * 
	 * @param resolvedArtifacts the resolved artifacts
	 * @param folderPath the folder path
	 * @param project the project
	 * @param monitor the monitor
	 */
	private void downloadAndAddToFolder(Set<ResolvedArtifact> resolvedArtifacts, IFolder folderPath, IProject project, final IProgressMonitor monitor) {
		List<File> librariesFiles = new ArrayList<File>();
		for (ResolvedArtifact resolvedArtifact : resolvedArtifacts) {
			final ArtifactVersion artifactVersion = resolvedArtifact.getArtifactVersion();
			String localRepositoryPreference = AggregatedProperties.getLocalRepository(project);
			final File targettedFile = new Path(localRepositoryPreference).toFile();
			if (targettedFile != null) {
				File repositoryFolder = targettedFile;
				monitor.subTask("Downloading " + artifactVersion.getUID() + " to " + repositoryFolder.toString());
				Proxy proxy = RepositoryModelUtils.determineProxy(artifactVersion);
				DownloadedFilesWrapper libraryFile = MavenRepositoryInteractionHelper.downloadArtifactToLocalRepository(artifactVersion, targettedFile, proxy, new IEventCallback() {
					public void onEvent(MavenRepositoryInteractionEvent event) {
						if (event.getEventType() == MavenRepositoryInteractionEvent.Type.START_TASK) {
							monitor.subTask(event.getMessage());
						} else if (event.getEventType() == MavenRepositoryInteractionEvent.Type.STOP_TASK) {
							monitor.worked(1);
						}
					}
				});

				monitor.worked(1);
				if (libraryFile.getArtifactVersionFile() != null) {
					librariesFiles.add(libraryFile.getArtifactVersionFile());
				}

				// if (libraryFile.getJavadocFile() != null) {
				// librariesFiles.add(libraryFile.getJavadocFile());
				// }
				// if (libraryFile.getSourcesFile() != null) {
				// librariesFiles.add(libraryFile.getSourcesFile());
				// }
			} else {
				throw new ProjectInteractionException("the target file :[" + localRepositoryPreference + "] could not be created.");
			}
		}
		ProjectInteractionHelper.createFolderAndAddLibraries(folderPath, librariesFiles, project, monitor);
	}

	/* (non-Javadoc)
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