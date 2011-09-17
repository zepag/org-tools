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

import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.IWizardPage;
import org.org.eclipse.core.ui.BasePlugin;
import org.org.eclipse.core.ui.images.PluginImages;
import org.org.eclipse.core.utils.platform.wizards.AbstractWizard;
import org.org.eclipse.dws.core.DWSCorePlugin;
import org.org.eclipse.dws.core.internal.jobs.AddRepositoryJob;
import org.org.eclipse.dws.core.internal.jobs.completion.CompletionPopupJobChangeListener;
import org.org.eclipse.dws.ui.internal.wizards.pages.AbstractRepositoryInfosWizardPage;
import org.org.eclipse.dws.ui.internal.wizards.pages.FileSystemRepositoryInfosWizardPage;
import org.org.eclipse.dws.ui.internal.wizards.pages.HttpRepositoryInfosWizardPage;
import org.org.eclipse.dws.ui.internal.wizards.pages.RepositoryChoiceWizardPage;
import org.org.eclipse.dws.ui.internal.wizards.pages.RepositoryWizardType;
import org.org.repository.crawler.items.ICrawledRepositorySetup;
import org.org.repository.crawler.items.IFileSystemCrawledRepositorySetup;
import org.org.repository.crawler.items.IHttpCrawledRepositorySetup;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * The Class NewRepositoryWizard.
 */
public class NewRepositoryWizard extends AbstractWizard {

	/** The logger. */
	private static Logger logger = Logger.getLogger(NewRepositoryWizard.class);

	private RepositoryChoiceWizardPage page1;
	/** The page2. */
	private AbstractRepositoryInfosWizardPage<?> page2;

	/** The repository setup class. */
	private Class<?> repositorySetupClass;

	/**
	 * Instantiates a new new repository wizard.
	 * 
	 * @param repositorySetupClass
	 *            the repository setup class
	 */
	public NewRepositoryWizard(Class<?> repositorySetupClass) {
		super();
		setWindowTitle(WizardsMessages.NewRepositoryWizard_title);
		setNeedsProgressMonitor(false);
		this.repositorySetupClass = repositorySetupClass;
		logger.debug("started wizard :" + this.getClass().getName()); //$NON-NLS-1$
	}

	/**
	 * Adds the pages.
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		if (IHttpCrawledRepositorySetup.class.isAssignableFrom(repositorySetupClass)) {
			if (DWSCorePlugin.getDefault().getHttpRepositoryExtensionsLabels().size() > 0) {
				page1 = new RepositoryChoiceWizardPage("Available repository definitions", "Available repository definitions", "You may choose between contributed repositories definitions.", RepositoryWizardType.HTTP);
				addPage(page1);
			}
			page2 = new HttpRepositoryInfosWizardPage();
		} else if (IFileSystemCrawledRepositorySetup.class.isAssignableFrom(repositorySetupClass)) {
			if (DWSCorePlugin.getDefault().getFileSystemRepositoryExtensionsLabels().size() > 0) {
				page1 = new RepositoryChoiceWizardPage("Available repository definitions", "Available repository definitions", "You may choose between contributed repositories definitions.", RepositoryWizardType.FS);
				addPage(page1);
			}
			page2 = new FileSystemRepositoryInfosWizardPage();
		}
		page2.setTitle(WizardsMessages.NewRepositoryWizard_subtitle);
		page2.setImageDescriptor(BasePlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_ORG_64));
		addPage(page2);
	}

	@Override
	public boolean canFinish() {
		return page2.isPageComplete();
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page.equals(page1)) {
			page2.refresh();
		}
		return super.getNextPage(page);
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
		final CrawledRepository crawledRepository = new CrawledRepository(page2.getLabel(), page2.getRepositorySetup());
		Job job = new AddRepositoryJob(crawledRepository);
		job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG DWS Notification", MessageFormat.format(WizardsMessages.NewRepositoryWizard_scanended, new Object[] { crawledRepository.getUID() })));
		job.schedule();
		return true;
	}

	public ICrawledRepositorySetup getChosenRepositorySetup() {
		ICrawledRepositorySetup chosenSetup = null;
		chosenSetup = DWSCorePlugin.getDefault().getRepositoryExtension(page1.getChosenRepositoryLabel());
		return chosenSetup;
	}
}