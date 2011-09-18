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

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelUtils;
import org.org.eclipse.dws.core.internal.model.AbstractChosenArtifactVersion;
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.eclipse.dws.core.internal.model.ResolvedArtifact;
import org.org.model.IModelItem;
import org.org.model.IModelItemVisitor;
import org.org.model.RootModelItem;
import org.org.repository.crawler.maven2.model.ArtifactVersion;

/**
 * The Class AddMavenLibraryToClasspathJob.
 */
public class AddMavenLibraryToClasspathJob extends Job {

	/** The logger. */
	private Logger logger = Logger.getLogger(AddMavenLibraryToClasspathJob.class);

	/** The Constant JOB_ID. */
	private static final String JOB_ID = "DWS: adding libraries to project ";

	/** The group. */
	public final String group;

	/** The artifact. */
	public final String artifact;

	/** The version. */
	public final String version;

	/** The classifier. */
	public final String classifier;

	/** The project name. */
	private String projectName;

	/**
	 * The Class FindMatchingArtifactVersionVisitor.
	 */
	private final class FindMatchingArtifactVersionVisitor implements IModelItemVisitor {

		/** The group. */
		private final String group;

		/** The artifact. */
		private final String artifact;

		/** The version. */
		private final String version;

		/** The classifier. */
		private final String classifier;

		/** The matching artifact version. */
		private ArtifactVersion matchingArtifactVersion;

		/**
		 * Instantiates a new find matching artifact version visitor.
		 * 
		 * @param group
		 *            the group
		 * @param artifact
		 *            the artifact
		 * @param version
		 *            the version
		 * @param classifier
		 *            the classifier
		 */
		public FindMatchingArtifactVersionVisitor(String group, String artifact, String version, String classifier) {
			this.group = group;
			this.artifact = artifact;
			this.version = version;
			this.classifier = classifier;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		/**
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		@SuppressWarnings("rawtypes")
		public boolean visit(IModelItem modelItem) {
			boolean result = true;
			if (modelItem instanceof ArtifactVersion) {
				ArtifactVersion artifactVersion = (ArtifactVersion) modelItem;
				boolean artifactVersionOK = artifactVersion.getVersion().equals(version);
				boolean artifactOK = artifactVersion.getParent().getId().equals(artifact);
				boolean groupOK = artifactVersion.getParent().getParent().getName().equals(group);
				boolean classifierOk = classifier == null ? true : artifactVersion.getClassifier().equals(classifier);
				if (groupOK && artifactOK && artifactVersionOK && classifierOk) {
					matchingArtifactVersion = artifactVersion;
					result = false;
				}
			}
			return result;
		}

		/**
		 * Gets the matching artifact version.
		 * 
		 * @return the matching artifact version
		 */
		public ArtifactVersion getMatchingArtifactVersion() {
			return matchingArtifactVersion;
		}
	}

	/**
	 * Instantiates a new adds the maven library to classpath job.
	 * 
	 * @param projectName
	 *            the project name
	 * @param group
	 *            the group
	 * @param artifact
	 *            the artifact
	 * @param version
	 *            the version
	 * @param classifier
	 *            the classifier
	 */
	public AddMavenLibraryToClasspathJob(String projectName, String group, String artifact, String version, String classifier) {
		super(JOB_ID + projectName + ":" + group + ":" + artifact + ":" + version + ":" + (classifier == null ? "" : classifier));
		this.projectName = projectName;
		this.group = group;
		this.artifact = artifact;
		this.version = version;
		this.classifier = classifier;
		this.setPriority(Job.SHORT);
		this.setUser(true);
		this.setRule(new BatchSimilarRule(Maven2Jobs.MAVEN2_DOWNLOAD_JOB_FAMILY));
	}

	/**
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus result = new StatusInfo(IStatus.OK, "Artifact addition successful");
		try {
			RootModelItem<?> root = RootModelItem.getInstance(ModelConstants.REPOSITORIES_ROOT);
			if (root.hasChildren()) {
				FindMatchingArtifactVersionVisitor findMatchingArtifactVersionVisitor = new FindMatchingArtifactVersionVisitor(group, artifact, version, classifier);
				root.accept(findMatchingArtifactVersionVisitor);
				ArtifactVersion artifactVersion = findMatchingArtifactVersionVisitor.getMatchingArtifactVersion();
				if (artifactVersion != null) {
					List<ArtifactVersion> artifactVersions = new ArrayList<ArtifactVersion>();
					artifactVersions.add(artifactVersion);
					Set<AbstractChosenArtifactVersion> chosenArtifactVersions = RepositoryModelUtils.computeLibrariesFromArtifactVersions(artifactVersions);
					Set<ResolvedArtifact> resolvedArtifacts = new HashSet<ResolvedArtifact>();
					for (AbstractChosenArtifactVersion chosenArtifactVersion : chosenArtifactVersions) {
						if (chosenArtifactVersion instanceof ResolvedArtifact) {
							resolvedArtifacts.add((ResolvedArtifact) chosenArtifactVersion);
						}
					}
					IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
					IProject project = workspaceRoot.getProject(projectName);
					if (project != null && project.hasNature(JavaCore.NATURE_ID)) {
						DownloadAndAddToClasspathJob downloadAndAddToClasspathJob = new DownloadAndAddToClasspathJob(JavaCore.create(project), resolvedArtifacts);
						downloadAndAddToClasspathJob.run(monitor);
					} else {
						throw new Exception("Project is not a java project.");
					}
				} else {
					throw new Exception("No matching artifactVersion for:\n\tgroup:" + group + "\n\tartifact:" + artifact + "\n\tversion:" + version);
				}
			} else {
				throw new Exception("No repository defined in DWS, define a repository and try again.\n CrawledRepository should contain:\n\tgroup:" + group + "\n\tartifact:" + artifact + "\n\tversion:" + version);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			monitor.setCanceled(true);
			result = new StatusInfo(IStatus.ERROR, "A problem occured while adding a library to a project:" + e.getMessage());
		} finally {
			monitor.done();
		}
		return result;
	}

}