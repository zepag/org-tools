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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.dws.core.DWSCorePlugin;
import org.org.repository.crawler.items.IFileSystemCrawledRepositorySetup;
import org.org.repository.crawler.items.IHttpCrawledRepositorySetup;
import org.org.repository.crawler.items.ICrawledRepositorySetup;
import org.org.repository.crawler.maven2.model.CrawledRepository;

public final class ComputeRepositoryDetailsJob extends Job {
	private static final String JOB_ID = "DWS: computing crawledRepository details";
	private final CrawledRepository crawledRepository;
	private StringBuilder formattedRepository;
	private Class<? extends ICrawledRepositorySetup> repositorySetupType;
	private StringBuilder formattedPatterns;

	public ComputeRepositoryDetailsJob(final CrawledRepository crawledRepository) {
		super(JOB_ID);
		this.setSystem(true);
		this.crawledRepository = crawledRepository;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		formattedRepository = new StringBuilder("<form><p>");
		formattedRepository.append("Name:<b> " + crawledRepository.getLabel() + "</b><br />");
		repositorySetupType = crawledRepository.getRepositorySetup().getClass();
		formattedPatterns = new StringBuilder();
		if (IHttpCrawledRepositorySetup.class.isAssignableFrom(repositorySetupType)) {
			IHttpCrawledRepositorySetup httpCrawledRepositorySetup = (IHttpCrawledRepositorySetup) crawledRepository.getRepositorySetup();
			formattedRepository.append("Type:<b>Http crawledRepository</b><br />");
			formattedRepository.append("Base url:<b>" + httpCrawledRepositorySetup.getBaseUrl() + "</b><br />");
			if (httpCrawledRepositorySetup.getProxyHost() != null) {
				formattedRepository.append("Proxy host:<b>" + httpCrawledRepositorySetup.getProxyHost() + "</b><br />");
			}
			if (httpCrawledRepositorySetup.getProxyPort() != null) {
				formattedRepository.append("Proxy port:<b>" + httpCrawledRepositorySetup.getProxyPort() + "</b><br />");
			}
			formattedPatterns.append("Entry pattern: " + httpCrawledRepositorySetup.getPatternSet().getEntryPattern() + "\n");
			formattedPatterns.append("Directory Entry pattern: " + httpCrawledRepositorySetup.getPatternSet().getDirectoryEntryPattern() + "\n");
			formattedPatterns.append("File Entry pattern: " + httpCrawledRepositorySetup.getPatternSet().getFileEntryPattern() + "\n");
			formattedPatterns.append("Parent Directory pattern: " + httpCrawledRepositorySetup.getPatternSet().getParentDirectoryPattern() + "\n");
		}
		if (IFileSystemCrawledRepositorySetup.class.isAssignableFrom(repositorySetupType)) {
			IFileSystemCrawledRepositorySetup fileSystemCrawledRepositorySetup = (IFileSystemCrawledRepositorySetup) crawledRepository.getRepositorySetup();
			formattedRepository.append("Type:<b>File system crawledRepository</b><br />");
			formattedRepository.append("Base path:<b>" + fileSystemCrawledRepositorySetup.getBasePath() + "</b><br />");
		}
		formattedRepository.append("</p></form>");

		return new Status(IStatus.OK, DWSCorePlugin.PI_MAVEN2, "DWS: details view refreshed");
	}

	public StringBuilder getFormattedRepository() {
		return formattedRepository;
	}

	public Class<? extends ICrawledRepositorySetup> getRepositorySetupType() {
		return repositorySetupType;
	}

	public StringBuilder getFormattedPatterns() {
		return formattedPatterns;
	}
}