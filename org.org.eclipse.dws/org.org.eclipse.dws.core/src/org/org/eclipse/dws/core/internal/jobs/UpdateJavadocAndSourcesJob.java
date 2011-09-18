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
import java.util.Set;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathSupport;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelUtils;
import org.org.eclipse.dws.core.internal.bridges.ProjectInteractionHelper.ProjectInteractionException;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.eclipse.dws.core.internal.model.ArtifactVersionWrapper;
import org.org.eclipse.dws.core.internal.model.LibraryWithMissingJavadocOrSourcesWrapper;
import org.org.maven2.DownloadedFilesWrapper;
import org.org.maven2.IEventCallback;
import org.org.maven2.MavenRepositoryInteractionHelper;
import org.org.maven2.MavenRepositoryInteractionHelper.MavenRepositoryInteractionEvent;
import org.org.maven2.MavenRepositoryInteractionHelper.MavenRepositoryInteractionException;
import org.org.repository.crawler.maven2.model.ArtifactVersion;

/**
 * The Class UpdateJavadocAndSourcesJob.
 */
public class UpdateJavadocAndSourcesJob extends Job {

	/** The logger. */
	private Logger logger = Logger.getLogger(UpdateJavadocAndSourcesJob.class);

	// private final String projectName;

	/** The project. */
	private IJavaProject project;

	/** The libraries. */
	private Set<LibraryWithMissingJavadocOrSourcesWrapper> libraries;

	/** The Constant JOB_ID. */
	private static final String JOB_ID = "DWS : Updating project libraries with available sources and javadoc : s";

	/**
	 * Instantiates a new update javadoc and sources job.
	 * 
	 * @param project
	 *            the project
	 * @param libraries
	 *            the libraries
	 */
	public UpdateJavadocAndSourcesJob(IJavaProject project, Set<LibraryWithMissingJavadocOrSourcesWrapper> libraries) {
		super(JOB_ID + project.getElementName());
		// this.projectName = project.getElementName();
		this.project = project;
		this.libraries = libraries;
		this.setPriority(Job.INTERACTIVE);
		this.setUser(true);
		this.setRule(new BatchSimilarRule(Maven2Jobs.MAVEN2_OTHER_JOB_FAMILY));
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
	public IStatus run(final IProgressMonitor monitor) {
		StatusInfo result = new StatusInfo(IStatus.OK, "Added available sources and javadoc.");
		try {
			// create a sample file
			monitor.beginTask("Adding available sources and javadoc", libraries.size());
			String localRepositoryPreference = AggregatedProperties.getLocalRepository(null);
			File toFile = new Path(localRepositoryPreference).toFile();
			StringBuilder globalMessage = new StringBuilder();
			for (LibraryWithMissingJavadocOrSourcesWrapper library : libraries) {
				if (!monitor.isCanceled()) {
					if (toFile != null) {
						boolean javadocOk = false;
						boolean sourcesOk = false;
						for (ArtifactVersionWrapper artifactVersionWrapper : library.getArtifactVersionWrappers()) {
							if (artifactVersionWrapper.getExactMatch()) {
								File artifactVersionFile = null;
								File javadocFile = null;
								File sourcesFile = null;
								try {
									ArtifactVersion artifactVersion = artifactVersionWrapper.getArtifactVersion();
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
									if (downloadedFilesWrapper.getJavadocFile() != null) {
										updateJavadoc(project, library.getPackageFragmentRoot(), downloadedFilesWrapper.getJavadocFile(), monitor);
										javadocOk = true;
									}
									if (downloadedFilesWrapper.getSourcesFile() != null) {
										updateSources(project, library.getPackageFragmentRoot(), downloadedFilesWrapper.getSourcesFile(), monitor);
										sourcesOk = true;
									}
								} catch (Exception e) {
									try {
										testArchives(artifactVersionFile, javadocFile, sourcesFile);

									} catch (MavenRepositoryInteractionException e2) {
										globalMessage.append(e2.getMessage());
									}
									globalMessage.append(e.getMessage());
								}
								if (javadocOk && sourcesOk) {
									break;
								}
							}
						}
					} else {
						throw new ProjectInteractionException("Javadoc and Sources magic based on local repository:[" + localRepositoryPreference + "] could not end properly.");
					}
				}
				monitor.worked(1);
			}
			if (globalMessage.length() > 0) {
				throw new ProjectInteractionException(globalMessage.toString());
			}
		} catch (Throwable e) {
			logger.error(e.getMessage());
			monitor.setCanceled(true);
			result = new StatusInfo(IStatus.WARNING, "Something went wrong: " + e.getMessage());
		} finally {
			monitor.done();
		}
		return result;
	}

	/**
	 * Test archives.
	 * 
	 * @param artifactVersionFile
	 *            the artifact version file
	 * @param javadocFile
	 *            the javadoc file
	 * @param sourcesFile
	 *            the sources file
	 */
	private void testArchives(File artifactVersionFile, File javadocFile, File sourcesFile) {
		StringBuilder buffer = new StringBuilder("");
		try {
			new ZipFile(artifactVersionFile);
		} catch (Exception e) {
			if (e instanceof ZipException) {
				buffer.append(artifactVersionFile.getAbsolutePath() + " archive is not valid:" + e.getMessage() + ". You should remove it from the repository and retry.\n");
			}
		}
		if (buffer.length() == 0) {
			try {
				new ZipFile(javadocFile);
			} catch (Exception e) {
				if (e instanceof ZipException) {
					buffer.append(javadocFile.getAbsolutePath() + " archive is not valid:" + e.getMessage() + ". You should remove it from the repository and retry.\n");
				}
			}
			try {
				new ZipFile(sourcesFile);
			} catch (Exception e) {
				if (e instanceof ZipException) {
					buffer.append(sourcesFile.getAbsolutePath() + " archive is not valid:" + e.getMessage() + ". You should remove it from the repository and retry.\n");
				}
			}
		}
		if (buffer.length() > 0) {
			throw new MavenRepositoryInteractionException(buffer.toString());
		}
	}

	/**
	 * Update sources.
	 * 
	 * @param project
	 *            the project
	 * @param packageFragmentRoot
	 *            the package fragment root
	 * @param file
	 *            the file
	 * @param monitor
	 *            the monitor
	 */
	@SuppressWarnings("restriction")
	private void updateSources(IJavaProject project, IPackageFragmentRoot packageFragmentRoot, File file, IProgressMonitor monitor) {
		try {
			logger.info("updating source attachment of " + packageFragmentRoot.getElementName() + " with " + file);
			IClasspathEntry rawClasspathEntry = packageFragmentRoot.getRawClasspathEntry();
			IPath containerPath = null;
			if (rawClasspathEntry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				containerPath = rawClasspathEntry.getPath();
				rawClasspathEntry = handleContainerEntry(containerPath, project, packageFragmentRoot.getPath());
			}
			CPListElement cpElem = CPListElement.createFromExisting(rawClasspathEntry, project);
			String loc = file.getAbsolutePath();
			cpElem.setAttribute(CPListElement.SOURCEATTACHMENT, Path.fromOSString(loc).makeAbsolute());
			IClasspathEntry newEntry = cpElem.getClasspathEntry();
			String[] changedAttributes = { CPListElement.SOURCEATTACHMENT };
			BuildPathSupport.modifyClasspathEntry(null, newEntry, changedAttributes, project, containerPath,true, monitor);
		} catch (JavaModelException e) {
			throw new MavenRepositoryInteractionException("Impossible to attach source jar:" + file.getAbsolutePath(), e);
		} catch (CoreException e) {
			throw new MavenRepositoryInteractionException("Impossible to attach source jar:" + file.getAbsolutePath(), e);
		}
	}

	/**
	 * Update javadoc.
	 * 
	 * @param project
	 *            the project
	 * @param packageFragmentRoot
	 *            the package fragment root
	 * @param file
	 *            the file
	 * @param monitor
	 *            the monitor
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	@SuppressWarnings("restriction")
	private void updateJavadoc(IJavaProject project, IPackageFragmentRoot packageFragmentRoot, File file, IProgressMonitor monitor) throws CoreException {
		try {
			logger.info("updating javadoc location of " + packageFragmentRoot.getElementName() + " with " + file);
			IClasspathEntry rawClasspathEntry = packageFragmentRoot.getRawClasspathEntry();
			IPath containerPath = null;
			if (rawClasspathEntry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				containerPath = rawClasspathEntry.getPath();
				rawClasspathEntry = handleContainerEntry(containerPath, project, packageFragmentRoot.getPath());
			}
			CPListElement cpElem = CPListElement.createFromExisting(rawClasspathEntry, project);
			String loc = "jar:file:/" + file.getAbsolutePath() + "!/";
			cpElem.setAttribute(CPListElement.JAVADOC, loc);
			IClasspathEntry newEntry = cpElem.getClasspathEntry();
			String[] changedAttributes = { CPListElement.JAVADOC };
			BuildPathSupport.modifyClasspathEntry(null, newEntry, changedAttributes, project, containerPath,true, monitor);
		} catch (JavaModelException e) {
			throw new MavenRepositoryInteractionException("Impossible to set javadoc location:" + file.getAbsolutePath(), e);
		} catch (CoreException e) {
			throw new MavenRepositoryInteractionException("Impossible to set javadoc location:" + file.getAbsolutePath(), e);
		}

	}

	/**
	 * Handle container entry.
	 * 
	 * @param containerPath
	 *            the container path
	 * @param jproject
	 *            the jproject
	 * @param jarPath
	 *            the jar path
	 * 
	 * @return the i classpath entry
	 * 
	 * @throws JavaModelException
	 *             the java model exception
	 */
	@SuppressWarnings("restriction")
	private IClasspathEntry handleContainerEntry(IPath containerPath, IJavaProject jproject, IPath jarPath) throws JavaModelException {
		ClasspathContainerInitializer initializer = JavaCore.getClasspathContainerInitializer(containerPath.segment(0));
		IClasspathContainer container = JavaCore.getClasspathContainer(containerPath, jproject);
		if (initializer == null || container == null) {
			logger.error("Container is not valid:" + containerPath);
			return null;
		}
		IStatus status = initializer.getAttributeStatus(containerPath, jproject, IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME);
		if (status.getCode() == ClasspathContainerInitializer.ATTRIBUTE_NOT_SUPPORTED) {
			logger.error("Container does not support javadoc location attribute.");
			return null;
		}
		if (status.getCode() == ClasspathContainerInitializer.ATTRIBUTE_READ_ONLY) {
			logger.error("Container's javadoc location attribute is read only.");
			return null;
		}
		IClasspathEntry entry = JavaModelUtil.findEntryInContainer(container, jarPath);
		Assert.isNotNull(entry);
		return entry;
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
		return Maven2Jobs.MAVEN2_OTHER_JOB_FAMILY.equals(family);
	}

}
