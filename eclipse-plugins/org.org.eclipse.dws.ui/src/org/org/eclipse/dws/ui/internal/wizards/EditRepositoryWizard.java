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
package org.org.eclipse.dws.ui.internal.wizards;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.core.ui.BasePlugin;
import org.org.eclipse.core.ui.images.PluginImages;
import org.org.eclipse.core.utils.platform.wizards.AbstractWizard;
import org.org.eclipse.dws.core.internal.jobs.AddRepositoryJob;
import org.org.eclipse.dws.ui.internal.wizards.pages.AbstractRepositoryInfosWizardPage;
import org.org.eclipse.dws.ui.internal.wizards.pages.FileSystemRepositoryInfosWizardPage;
import org.org.eclipse.dws.ui.internal.wizards.pages.HttpRepositoryInfosWizardPage;
import org.org.repository.crawler.items.IFileSystemCrawledRepositorySetup;
import org.org.repository.crawler.items.IHttpCrawledRepositorySetup;
import org.org.repository.crawler.maven2.model.CrawledRepository;


/**
 * The Class EditRepositoryWizard.
 */
public class EditRepositoryWizard extends AbstractWizard {
	
	/** The logger. */
	private static Logger logger = Logger.getLogger(EditRepositoryWizard.class);

	/** The wizardPage. */
	private AbstractRepositoryInfosWizardPage<?> page;

	/** The crawledRepository. */
	private CrawledRepository crawledRepository;

	/**
	 * Instantiates a new edits the crawledRepository wizard.
	 * 
	 * @param crawledRepository the crawledRepository
	 */
	public EditRepositoryWizard(CrawledRepository crawledRepository) {
		super();
		setWindowTitle(WizardsMessages.EditRepositoryWizard_title);
		setNeedsProgressMonitor(false);
		this.crawledRepository = crawledRepository;
		logger.debug("started wizard :" + this.getClass().getName());
	}

	/**
	 * Adds the pages.
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		if (crawledRepository.getRepositorySetup() instanceof IHttpCrawledRepositorySetup) {
			page = new HttpRepositoryInfosWizardPage(crawledRepository.getLabel(), (IHttpCrawledRepositorySetup) this.crawledRepository.getRepositorySetup());
		} else if (crawledRepository.getRepositorySetup() instanceof IFileSystemCrawledRepositorySetup) {
			page = new FileSystemRepositoryInfosWizardPage(crawledRepository.getLabel(), (IFileSystemCrawledRepositorySetup) this.crawledRepository.getRepositorySetup());
		}
		page.setTitle(WizardsMessages.EditRepositoryWizard_page_title);
		page.setImageDescriptor(BasePlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_ORG_64));
		addPage(page);
	}

	/**
	 * Perform finish.
	 * 
	 * @return true, if perform finish
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		Job job = new AddRepositoryJob(new CrawledRepository(page.getLabel(), page.getRepositorySetup()));
		job.schedule();
		return true;
	}
}
