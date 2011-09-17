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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;
import org.org.eclipse.core.utils.platform.dialogs.message.WarningDialog;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.internal.PomInteractionHelper;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.PomRepository;
import org.org.model.IModelItem;
import org.org.model.IModelItemVisitor;
import org.org.model.RootModelItem;
import org.org.repository.crawler.items.IFileSystemCrawledRepositorySetup;
import org.org.repository.crawler.items.IHttpCrawledRepositorySetup;
import org.org.repository.crawler.items.mutable.FileSystemCrawledRepositorySetup;
import org.org.repository.crawler.items.mutable.HttpCrawledRepositorySetup;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * The Class FindRepositoryJob.
 */
public class FindRepositoryJob extends Job {

	/**
	 * The Class RepositorySeeker.
	 */
	public class RepositorySeeker implements IModelItemVisitor {

		/** The parsed repository description. */
		private PomRepository pomRepository;

		/** The repository already declared. */
		private Boolean repositoryAlreadyDeclared = false;

		/**
		 * Instantiates a new repository seeker.
		 * 
		 * @param pomRepository
		 *            the parsed repository description
		 */
		public RepositorySeeker(PomRepository pomRepository) {
			this.pomRepository = pomRepository;
		}

		/**
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		@SuppressWarnings("unchecked")
		public boolean visit(IModelItem modelItem) {
			boolean keepOnScanning = true;
			if (modelItem instanceof CrawledRepository) {
				keepOnScanning = false;
				CrawledRepository crawledRepository = (CrawledRepository) modelItem;
				if (crawledRepository.getRepositorySetup() instanceof IHttpCrawledRepositorySetup) {
					if (pomRepository.getUrl().endsWith(((IHttpCrawledRepositorySetup) crawledRepository.getRepositorySetup()).getBaseUrl())) {
						repositoryAlreadyDeclared = true;
					}
				}
				if (crawledRepository.getRepositorySetup() instanceof IFileSystemCrawledRepositorySetup) {
					if (pomRepository.getUrl().endsWith(((IFileSystemCrawledRepositorySetup) crawledRepository.getRepositorySetup()).getBasePath())) {
						repositoryAlreadyDeclared = true;
					}
				}
			}
			return keepOnScanning;
		}

		/**
		 * Gets the repository already declared.
		 * 
		 * @return the repository already declared
		 */
		public Boolean getRepositoryAlreadyDeclared() {
			return repositoryAlreadyDeclared;
		}

	}

	/** The projects. */
	private final List<IProject> projects;

	/** The crawledRepositories. */
	private Set<CrawledRepository> crawledRepositories = new LinkedHashSet<CrawledRepository>();

	/** The Constant JOB_ID. */
	private static final String JOB_ID = "DWS: looking for defined crawledRepositories in projects ";

	/**
	 * Instantiates a new find repository job.
	 * 
	 * @param projects
	 *            the projects
	 */
	public FindRepositoryJob(List<IProject> projects) {
		super(JOB_ID);
		this.projects = projects;
		this.setPriority(Job.INTERACTIVE);
		this.setUser(true);
		this.setRule(new BatchSimilarRule(Maven2Jobs.MAVEN2_OTHER_JOB_FAMILY));
	}

	/**
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus result = new StatusInfo(IStatus.OK, "Looked up crawledRepositories without error.");
		String projectName = null;
		if (atLeastOneProjectOpened()) {
			try {
				for (IProject project : projects) {
					projectName = project.getName();
					scanProject(monitor, project);
				}

			} catch (Exception e) {
				result = new StatusInfo(IStatus.ERROR, "Look up of crawledRepositories in project : \"" + projectName + "\"  failed: " + e.getMessage());
			} finally {
				monitor.done();
			}
		} else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

				public void run() {
					WarningDialog errorDialog = new WarningDialog("Impossible to find crawledRepositories", "You have no opened projects in your workspace.\n This looks for opened projects with configured pom files.\n These files are scanned for repository definitions.");
					errorDialog.open();
				}

			});

		}
		return result;
	}

	/**
	 * At least one project opened.
	 * 
	 * @return true, if successful
	 */
	private boolean atLeastOneProjectOpened() {
		boolean result = false;
		if (projects.size() > 0) {
			for (IProject project : projects) {
				if (project.isOpen() && !project.isPhantom()) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Scan project.
	 * 
	 * @param monitor
	 *            the monitor
	 * @param project
	 *            the project
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws MalformedURLException
	 *             the malformed url exception
	 */
	private void scanProject(IProgressMonitor monitor, IProject project) throws IOException, MalformedURLException {
		if (project.isOpen() && !project.isPhantom()) {
			final String pomFileName = AggregatedProperties.getPomFileNames(project);
			final IResource pomFile = project.findMember(pomFileName);
			if (pomFile != null && pomFile.exists()) {
				monitor.beginTask("Parsing project " + project.getName() + "'s POM.", 1000);
				Pom pom = PomInteractionHelper.parsePom(pomFile.getRawLocationURI().toURL().openStream());
				for (PomRepository pomRepository : pom.getRepositories().getPomRepositories().values()) {
					if (notAlreadyDeclaredOrBlackListed(pomRepository)) {
						if (isHttpRepository(pomRepository)) {
							IHttpCrawledRepositorySetup httpCrawledRepositorySetup = new HttpCrawledRepositorySetup(pomRepository.getUrl());
							final CrawledRepository crawledRepository = new CrawledRepository(pomRepository.getName(), httpCrawledRepositorySetup);
							crawledRepositories.add(crawledRepository);
						}
						if (isFileSystemRepository(pomRepository)) {
							IFileSystemCrawledRepositorySetup fileSystemCrawledRepositorySetup = new FileSystemCrawledRepositorySetup(pomRepository.getUrl());
							final CrawledRepository crawledRepository = new CrawledRepository(pomRepository.getName(), fileSystemCrawledRepositorySetup);
							crawledRepositories.add(crawledRepository);
						}
					}
				}
			}
		}
	}

	/**
	 * Checks if is file system repository.
	 * 
	 * @param pomRepository
	 *            the parsed repository description
	 * 
	 * @return true, if is file system repository
	 */
	private boolean isFileSystemRepository(PomRepository pomRepository) {
		return pomRepository.getUrl().startsWith("file");
	}

	/**
	 * Checks if is http repository.
	 * 
	 * @param pomRepository
	 *            the parsed repository description
	 * 
	 * @return true, if is http repository
	 */
	private boolean isHttpRepository(PomRepository pomRepository) {
		return pomRepository.getUrl().startsWith("http");
	}

	/**
	 * Not already declared or black listed.
	 * 
	 * @param pomRepository
	 *            the parsed repository description
	 * 
	 * @return true, if successful
	 */
	private boolean notAlreadyDeclaredOrBlackListed(PomRepository pomRepository) {
		RepositorySeeker repositorySeeker = new RepositorySeeker(pomRepository);
		RootModelItem.<CrawledRepository> getInstance(ModelConstants.REPOSITORIES_ROOT).accept(repositorySeeker);
		return !repositorySeeker.getRepositoryAlreadyDeclared();
	}

	/**
	 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
	 */
	@Override
	public boolean belongsTo(Object family) {
		return Maven2Jobs.MAVEN2_OTHER_JOB_FAMILY.equals(family);
	}

	public Set<CrawledRepository> getFoundRepositories() {
		return crawledRepositories;
	}
}
