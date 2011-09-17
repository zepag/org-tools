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

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.org.eclipse.core.ui.BasePlugin;
import org.org.eclipse.core.ui.images.PluginImages;
import org.org.eclipse.core.utils.platform.wizards.AbstractWizard;
import org.org.eclipse.dws.core.internal.jobs.UpdateJavadocAndSourcesJob;
import org.org.eclipse.dws.core.internal.jobs.completion.CompletionPopupJobChangeListener;
import org.org.eclipse.dws.core.internal.model.LibraryWithMissingJavadocOrSourcesWrapper;
import org.org.eclipse.dws.ui.internal.wizards.pages.LookupJavadocAndSourcesForLibrariesInClasspathPage;

/**
 * This wizard allows finding Javadoc and Sources automatically for a given.
 */
public class JavadocSourcesLookupWizard extends AbstractWizard implements INewWizard {

	/** The logger. */
	private static Logger logger = Logger.getLogger(JavadocSourcesLookupWizard.class);

	/** The page2. */
	private LookupJavadocAndSourcesForLibrariesInClasspathPage page2;

	/** The selected project. */
	private IJavaProject selectedProject;

	/**
	 * Constructor for NewPomFileWizard.
	 * 
	 * @param selectedProject
	 *            the selected project
	 */
	public JavadocSourcesLookupWizard(IJavaProject selectedProject) {
		super();
		setWindowTitle(WizardsMessages.JavadocSourcesLookupWizard_title);
		setNeedsProgressMonitor(false);
		logger.debug("started wizard :" + this.getClass().getName()); //$NON-NLS-1$
		this.selectedProject = selectedProject;
	}

	/**
	 * Adding the page1 to the wizard.
	 */

	@Override
	public void addPages() {
		page2 = new LookupJavadocAndSourcesForLibrariesInClasspathPage(selectedProject);
		page2.setImageDescriptor(BasePlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_ORG_64));
		page2.setTitle(WizardsMessages.JavadocSourcesLookupWizard_choose_possible_dependencies);
		addPage(page2);

	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We will create an operation and run it using wizard as execution context.
	 * 
	 * @return true, if perform finish
	 */
	@Override
	public boolean performFinish() {
		Set<LibraryWithMissingJavadocOrSourcesWrapper> chosenDependencies = page2.getChosenDependencies();
		Job job = new UpdateJavadocAndSourcesJob(selectedProject, chosenDependencies);
		job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG DWS notification", "Javadoc and Sources magic ended: \n"));
		job.schedule();
		return true;
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize from it.
	 * 
	 * @param workbench
	 *            the workbench
	 * @param selection
	 *            the selection
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {

	}
}