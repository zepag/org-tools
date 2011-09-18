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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.org.eclipse.core.ui.BasePlugin;
import org.org.eclipse.core.ui.dialogs.WarningDialog;
import org.org.eclipse.core.ui.images.PluginImages;
import org.org.eclipse.core.utils.platform.wizards.AbstractWizard;
import org.org.eclipse.dws.core.internal.jobs.NewPomFileJob;
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.eclipse.dws.core.internal.model.PomCreationDescription;
import org.org.eclipse.dws.ui.internal.wizards.pages.DependenciesFromClasspathPage;
import org.org.eclipse.dws.ui.internal.wizards.pages.NewPomFilePage;
import org.org.model.RootModelItem;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.Group;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * This wizard's role is to create a new POM File.
 */
public class NewPomFileWizard extends AbstractWizard implements INewWizard {

	/** The logger. */
	private static Logger logger = Logger.getLogger(NewPomFileWizard.class);

	/** The page1. */
	private NewPomFilePage page1;

	/** The page2. */
	private DependenciesFromClasspathPage page2;

	/** The selection. */
	private IStructuredSelection selection;

	/**
	 * Constructor for NewPomFileWizard.
	 */
	public NewPomFileWizard() {
		super();
		setWindowTitle(WizardsMessages.NewPomFileWizard_title);
		setNeedsProgressMonitor(false);
		logger.debug("started wizard :" + this.getClass().getName()); //$NON-NLS-1$
	}

	/**
	 * Adding the page1 to the wizard.
	 */

	@Override
	public void addPages() {
		Object input = selection.getFirstElement();
		IResource firstElement = null;
		if (input instanceof IResource) {
			firstElement = (IResource) input;
		} else if (input instanceof IAdaptable) {
			IAdaptable a = (IAdaptable) input;
			firstElement = (IResource) a.getAdapter(IResource.class);
		}
		IProject project = null;
		if (firstElement != null) {
			project = firstElement.getProject();
		}
		if (project == null) {
			project = getFirstAvailableProject();
			WarningDialog warningDialog = new WarningDialog(WizardsMessages.NewPomFileWizard_warning_noproject_title, WizardsMessages.NewPomFileWizard_warning_noproject_message);
			warningDialog.open();
		}
		String[] projectNames = computeProjectNames(false);
		if (projectNames.length != 0) {
			page1 = new NewPomFilePage(project, projectNames, computeGroupIdsFromRepository(), computeArtifactIdsFromRepository());
			page1.setImageDescriptor(BasePlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_ORG_64));
			page1.setTitle(WizardsMessages.NewPomFileWizard_describe_pom_file);
			addPage(page1);
			page2 = new DependenciesFromClasspathPage(project);
			page2.setImageDescriptor(BasePlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_ORG_64));
			page2.setTitle(WizardsMessages.NewPomFileWizard_choose_possible_dependencies);
			addPage(page2);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#getStartingPage()
	 */
	/**
	 * @see org.eclipse.jface.wizard.Wizard#getStartingPage()
	 */
	@Override
	public IWizardPage getStartingPage() {
		if (computeProjectNames(false).length == 0) {
			WarningDialog warningDialog = new WarningDialog(WizardsMessages.NewPomFileWizard_warning_impossible_to_use, WizardsMessages.NewPomFileWizard_warning_all_projects_closed);
			warningDialog.open();
		}
		return super.getStartingPage();
	}

	/**
	 * Compute group ids from repository.
	 * 
	 * @return the string[]
	 */
	private String[] computeGroupIdsFromRepository() {
		Set<String> result = new TreeSet<String>();
		for (CrawledRepository crawledRepository : RootModelItem.<CrawledRepository> getInstance(ModelConstants.REPOSITORIES_ROOT).getChildren()) {
			for (Group group : crawledRepository.getChildren()) {
				result.add(group.getName());
			}
		}
		return result.toArray(new String[] {});
	}

	/**
	 * Compute artifact ids from repository.
	 * 
	 * @return the string[]
	 */
	private String[] computeArtifactIdsFromRepository() {
		Set<String> result = new TreeSet<String>();
		for (CrawledRepository crawledRepository : RootModelItem.<CrawledRepository> getInstance(ModelConstants.REPOSITORIES_ROOT).getChildren()) {
			for (Group group : crawledRepository.getChildren()) {
				for (Artifact artifact : group.getChildren()) {
					result.add(artifact.getId());
				}
			}
		}

		return result.toArray(new String[] {});
	}

	/**
	 * Compute project names.
	 * 
	 * @param checkJavaNature the check java nature
	 * 
	 * @return the string[]
	 */
	private String[] computeProjectNames(boolean checkJavaNature) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = workspaceRoot.getProjects();
		List<String> projectNamesList = new ArrayList<String>();
		for (IProject project : projects) {
			if (project.isAccessible() && !project.isPhantom())
				projectNamesList.add(project.getName());
		}
		return projectNamesList.toArray(new String[0]);
	}

	/**
	 * Gets the first available project.
	 * 
	 * @return the first available project
	 */
	private IProject getFirstAvailableProject() {
		IProject result = null;
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = workspaceRoot.getProjects();
		for (IProject project : projects) {
			if (project.isAccessible() && !project.isPhantom())
				result = project;
			break;
		}
		return result;

	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We will create an operation and run it using wizard as execution context.
	 * 
	 * @return true, if perform finish
	 */
	@Override
	public boolean performFinish() {
		PomCreationDescription pomCreationDescription = new PomCreationDescription();
		pomCreationDescription.setGroupId(page1.getGroupId());
		pomCreationDescription.setArtifactId(page1.getArtifactId());
		pomCreationDescription.setVersion(page1.getVersion());
		pomCreationDescription.setPackaging(page1.getPackaging());
		for (ArtifactVersion chosenDependency : page2.getChosenDependencies()) {
			pomCreationDescription.addChild(chosenDependency);
		}
		Job job = new NewPomFileJob(page1.getChosenProject(), pomCreationDescription);
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
		this.selection = selection;
	}
}