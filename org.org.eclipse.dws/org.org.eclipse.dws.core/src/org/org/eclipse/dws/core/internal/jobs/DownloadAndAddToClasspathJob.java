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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.org.eclipse.core.utils.jdt.tools.ClasspathEntryDefinition;
import org.org.eclipse.core.utils.jdt.tools.ClasspathEntryDefinition.EntryType;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.internal.bridges.ProjectInteractionHelper;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelUtils;
import org.org.eclipse.dws.core.internal.bridges.ProjectInteractionHelper.ProjectInteractionException;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.eclipse.dws.core.internal.model.ResolvedArtifact;
import org.org.eclipse.dws.core.internal.model.PomDependency.Scope;
import org.org.maven2.DownloadedFilesWrapper;
import org.org.maven2.IEventCallback;
import org.org.maven2.MavenRepositoryInteractionHelper;
import org.org.maven2.MavenRepositoryInteractionHelper.MavenRepositoryInteractionEvent;
import org.org.maven2.MavenRepositoryInteractionHelper.MavenRepositoryInteractionException;
import org.org.repository.crawler.maven2.model.ArtifactVersion;

/**
 * The Class DownloadAndAddToClasspathJob.
 */
public class DownloadAndAddToClasspathJob extends Job {

	/** The java project. */
	private IJavaProject javaProject;

	/** The libraries. */
	private Set<ResolvedArtifact> libraries;

	/** The Constant JOB_ID. */
	private final static String JOB_ID = "DWS: Adding libraries to classpath of javaProject ";

	/**
	 * Instantiates a new download and add to classpath job.
	 * 
	 * @param javaProject
	 *            the java project
	 * @param libraries
	 *            the libraries
	 */
	public DownloadAndAddToClasspathJob(IJavaProject javaProject, Set<ResolvedArtifact> libraries) {
		super(JOB_ID + javaProject.getElementName());
		this.setPriority(Job.LONG);
		this.setUser(true);
		this.javaProject = javaProject;
		this.libraries = libraries;
		this.setRule(new BatchSimilarRule(Maven2Jobs.MAVEN2_DOWNLOAD_JOB_FAMILY));
	}

	/**
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus run(IProgressMonitor monitor) {
		StatusInfo result = new StatusInfo(IStatus.OK, "Maven library retrieved");
		try {
			downloadAndAddToClasspath(libraries, javaProject, monitor);
		} catch (ProjectInteractionException e) {
			result = new StatusInfo(IStatus.ERROR, "Problem while interacting with the javaProject :" + e.getMessage());
		} catch (MavenRepositoryInteractionException e) {
			result = new StatusInfo(IStatus.ERROR, "Problem while interacting with the repository :" + e.getMessage());
		} finally {
			monitor.done();
		}
		return result;
	}

	/**
	 * Download and add to classpath.
	 * 
	 * @param resolvedArtifacts
	 *            the resolved artifacts
	 * @param javaProject
	 *            the java project
	 * @param monitor
	 *            the monitor
	 */
	private void downloadAndAddToClasspath(Set<ResolvedArtifact> resolvedArtifacts, IJavaProject javaProject, final IProgressMonitor monitor) {
		List<ClasspathEntryDefinition> classpathEntryWrappers = new ArrayList<ClasspathEntryDefinition>();
		monitor.beginTask("Downloading libraries from the repositories.", resolvedArtifacts.size());
		final String localRepositoryPreference = AggregatedProperties.getLocalRepository(javaProject.getProject());
		final File toFile = new Path(localRepositoryPreference).toFile();
		for (ResolvedArtifact resolvedArtifact : resolvedArtifacts) {
			final ArtifactVersion artifactVersion = resolvedArtifact.getArtifactVersion();
			String systemPath = resolvedArtifact.getSystemPath();
			File systemPathFile = systemPath == null ? null : new File(systemPath);
			if (resolvedArtifact.getScope() == Scope.SYSTEM) {
				if (systemPathFile != null && systemPathFile.exists()) {
					ClasspathEntryDefinition classpathEntryWrapper = new ClasspathEntryDefinition(Path.fromOSString(resolvedArtifact.getSystemPath()), null, null, EntryType.PATH);
					classpathEntryWrappers.add(classpathEntryWrapper);
					continue;
				} else {
					continue;
				}
			} else {
				if (toFile != null) {
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
					if (downloadedFilesWrapper.getArtifactVersionFile() != null) {
						Path path = new Path(downloadedFilesWrapper.getArtifactVersionFile().getAbsolutePath());
						Path sourcesPath = (downloadedFilesWrapper.getSourcesFile() != null ? new Path(downloadedFilesWrapper.getSourcesFile().getAbsolutePath()) : null);
						Path javadocPath = (downloadedFilesWrapper.getJavadocFile() != null ? new Path(downloadedFilesWrapper.getJavadocFile().getAbsolutePath()) : null);
						EntryType entryType = resolvedArtifact.getScope() == Scope.SYSTEM ? EntryType.PATH : EntryType.VARIABLE;
						ClasspathEntryDefinition classpathEntryWrapper = new ClasspathEntryDefinition(path, sourcesPath, javadocPath, entryType);
						classpathEntryWrappers.add(classpathEntryWrapper);
					}
				} else {
					throw new ProjectInteractionException("the target file :[" + localRepositoryPreference + "] could not be created.");
				}
			}
		}

		classpathEntryWrappers = ProjectInteractionHelper.addToClasspath(classpathEntryWrappers, javaProject, monitor);
	}

	/**
	 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
	 */
	@Override
	public boolean belongsTo(Object family) {
		return Maven2Jobs.MAVEN2_DOWNLOAD_JOB_FAMILY.equals(family);
	}
}