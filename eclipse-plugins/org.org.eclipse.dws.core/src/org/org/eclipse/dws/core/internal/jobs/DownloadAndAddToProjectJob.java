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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.org.eclipse.core.utils.jdt.tools.ClasspathEntryDefinition;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.internal.bridges.ProjectInteractionHelper;
import org.org.eclipse.dws.core.internal.bridges.ProjectInteractionHelper.ProjectInteractionException;
import org.org.eclipse.dws.core.internal.model.ResolvedArtifact;
import org.org.eclipse.dws.core.internal.model.PomDependency.Scope;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPackArtifactVersion;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPackArtifactVersion.Target;
import org.org.maven2.MavenRepositoryInteractionHelper.MavenRepositoryInteractionException;

/**
 * The Class DownloadAndAddToClasspathJob.
 */
public class DownloadAndAddToProjectJob extends Job {

	/** The libraries. */
	private Set<LibraryPackArtifactVersion> libraries;

	private IJavaProject javaProject;

	/** The Constant JOB_ID. */
	private final static String JOB_ID = "DWS: Adding libraries to java Project ";

	/**
	 * Instantiates a new download and add to classpath job.
	 * 
	 * @param javaProject
	 *            the java project
	 * @param libraries
	 *            the libraries
	 */
	public DownloadAndAddToProjectJob(ProjectWrapper projectWrapper, Set<LibraryPackArtifactVersion> libraries) {
		super(JOB_ID + projectWrapper.getName());
		this.setPriority(Job.LONG);
		this.setUser(true);
		this.javaProject = projectWrapper.getJavaProject();
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
	 * @param libraries
	 *            the resolved artifacts
	 * @param javaProject
	 *            the java project
	 * @param monitor
	 *            the monitor
	 */
	private static void downloadAndAddToClasspath(Set<LibraryPackArtifactVersion> libraries, IJavaProject javaProject, final IProgressMonitor monitor) {
		List<ClasspathEntryDefinition> classpathEntryWrappers = new ArrayList<ClasspathEntryDefinition>();
		monitor.beginTask("Downloading libraries from the repositories.", libraries.size());
		// final String localRepositoryPreference = AggregatedProperties.getLocalRepository(javaProject.getProject());
		Set<ResolvedArtifact> resolvedArtifacts = new HashSet<ResolvedArtifact>();
		for (LibraryPackArtifactVersion libraryPackArtifactVersion : libraries) {
			Set<Target> targets = libraryPackArtifactVersion.getTargets();
			if (targets.contains(Target.BUNDLED_FOR_RUNTIME)) {
				ResolvedArtifact resolvedArtifact = new ResolvedArtifact();
				resolvedArtifact.setArtifactVersion(libraryPackArtifactVersion);
				resolvedArtifact.setScope(Scope.COMPILE);
				resolvedArtifacts.add(resolvedArtifact);
			}
			if (targets.contains(Target.ADDED_TO_PROJECT_CLASSPATH)) {
				ResolvedArtifact resolvedArtifact = new ResolvedArtifact();
				resolvedArtifact.setArtifactVersion(libraryPackArtifactVersion);
				resolvedArtifact.setScope(Scope.COMPILE);
				resolvedArtifacts.add(resolvedArtifact);
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