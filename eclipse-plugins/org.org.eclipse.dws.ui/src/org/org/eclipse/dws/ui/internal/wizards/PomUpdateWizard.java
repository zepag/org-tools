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

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.org.eclipse.core.ui.BasePlugin;
import org.org.eclipse.core.ui.images.PluginImages;
import org.org.eclipse.core.utils.platform.wizards.AbstractWizard;
import org.org.eclipse.dws.core.internal.jobs.UpdatePomFileJob;
import org.org.eclipse.dws.core.internal.model.PomDependency;
import org.org.eclipse.dws.core.internal.model.PomDependency.Scope;
import org.org.eclipse.dws.ui.internal.wizards.pages.DependenciesFromClasspathPage;
import org.org.repository.crawler.maven2.model.ArtifactVersion;

/**
 * This is a sample new wizard. Its role is to create a new file resource in the provided container. If the container resource (a folder or a project) is selected in the workspace when the wizard is opened, it will accept it as the target container. The wizard creates one file with the extension "xml". If a sample multi-page1 editor (also available as a template) is registered for the same extension, it will be able to open it.
 */
public class PomUpdateWizard extends AbstractWizard implements INewWizard {

	/** The logger. */
	private static Logger logger = Logger.getLogger(PomUpdateWizard.class);

	/** The wizard page. */
	private DependenciesFromClasspathPage page;

	/** The selected file. */
	private IFile selectedFile;

	/**
	 * Constructor for NewPomFileWizard.
	 * 
	 * @param selectedFile the selected file
	 */
	public PomUpdateWizard(IFile selectedFile) {
		super();
		setWindowTitle(WizardsMessages.NewPomFileWizard_title);
		setNeedsProgressMonitor(false);
		logger.debug("started wizard :" + this.getClass().getName()); //$NON-NLS-1$
		this.selectedFile = selectedFile;
	}

	/**
	 * Adding the page to the wizard.
	 */
	@Override
	public void addPages() {
		page = new DependenciesFromClasspathPage(selectedFile.getProject());
		page.setImageDescriptor(BasePlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_ORG_64));
		page.setTitle(WizardsMessages.NewPomFileWizard_choose_possible_dependencies);
		addPage(page);

	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard.
	 * A job is then executed, updating the Pom file.
	 * 
	 * @return true, if perform finish
	 */
	@Override
	public boolean performFinish() {
		Set<PomDependency> pomDependencies = new LinkedHashSet<PomDependency>();
		for (ArtifactVersion chosenDependency : page.getChosenDependencies()) {
			PomDependency pomDependency = new PomDependency();
			pomDependency.setGroupId(chosenDependency.getParent().getParent().getName());
			pomDependency.setArtifactId(chosenDependency.getParent().getId());
			pomDependency.setVersion(chosenDependency.getVersion());
			pomDependency.setClassifier(chosenDependency.getClassifier());
			pomDependency.setOptional(false);
			pomDependency.setScope(Scope.COMPILE);
			pomDependencies.add(pomDependency);
		}

		Job job = new UpdatePomFileJob(selectedFile, pomDependencies);
		job.schedule();
		return true;
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize from it.
	 * 
	 * @param workbench the workbench
	 * @param selection the selection
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {

	}
}