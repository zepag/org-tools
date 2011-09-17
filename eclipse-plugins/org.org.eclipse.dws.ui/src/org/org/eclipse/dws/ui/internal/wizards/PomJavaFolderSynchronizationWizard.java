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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;
import org.org.eclipse.core.ui.BasePlugin;
import org.org.eclipse.core.ui.images.PluginImages;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.eclipse.dws.core.internal.jobs.DownloadAndAddToFolderJob;
import org.org.eclipse.dws.core.internal.model.AbstractChosenArtifactVersion;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions;
import org.org.eclipse.dws.ui.internal.wizards.pages.ChooseFolderWizardPage;
import org.org.eclipse.dws.ui.internal.wizards.pages.PomJavaSynchronizationWizardPage;

/**
 * The Class PomJavaFolderSynchronizationWizard.
 */
public class PomJavaFolderSynchronizationWizard extends AbstractPomSynchronizationWizard {

	/** The page 1: java sync. */
	private PomJavaSynchronizationWizardPage page1;

	/** The page 2: target folder. */
	private ChooseFolderWizardPage page2;

	/**
	 * Instantiates a new pom java folder synchronization wizard.
	 * 
	 * @param filteringOptions
	 *            the filtering options
	 * @param pom
	 *            the parsed pom description
	 */
	@SuppressWarnings("unchecked")
	public PomJavaFolderSynchronizationWizard(PomDependenciesFilteringOptions filteringOptions, Pom pom) {
		super(filteringOptions, pom, WizardsMessages.PomJavaFolderSynchronizationWizard_title, false, JavaCore.NATURE_ID);
	}

	/**
	 * Instantiates a new pom java folder synchronization wizard.
	 * 
	 * @param artifactVersions
	 *            the artifact versions
	 */
	@SuppressWarnings("unchecked")
	public PomJavaFolderSynchronizationWizard(Set<AbstractChosenArtifactVersion> artifactVersions) {
		super(artifactVersions, WizardsMessages.PomJavaFolderSynchronizationWizard_subtitle, false);
	}

	/**
	 * Gets the artifact versions.
	 * 
	 * @param filteringOptions
	 *            the filtering options
	 * @param pom
	 *            the parsed pom description
	 * 
	 * @return the artifact versions
	 * 
	 * @see org.org.eclipse.dws.ui.internal.wizards.AbstractPomSynchronizationWizard#getArtifactVersions(org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions, org.org.eclipse.dws.core.internal.model.Pom)
	 */
	@Override
	public Set<AbstractChosenArtifactVersion> getArtifactVersions(PomDependenciesFilteringOptions filteringOptions, Pom pom) {
		Set<AbstractChosenArtifactVersion> artifactVersions = new HashSet<AbstractChosenArtifactVersion>();
		// options retrieval
		Boolean dealWithOptional = filteringOptions.dealWithOptional();
		boolean allOptional = false;
		boolean classpathComplete = false;
		// RETRIEVE THE POM DEPENDENCIES FROM THE SELECTED POM FILE.

		if (pom.hasChildren()) {
			if (!dealWithOptional && pom.areAllDependenciesOptional()) {
				allOptional = true;
				classpathComplete = true;
			} else {
				// WHETHER WE'RE DEALING WITH OPTIONALS OR IF NOT, SOME LIBRARIES ARE NOT OPTIONAL!
				artifactVersions = pom.computeLibrariesFromPomDependencies(filteringOptions);
			}
		} else {
			classpathComplete = true;
		}
		if (classpathComplete) {
			if (allOptional) {
				StringBuilder message = new StringBuilder();
				if (allOptional) {
					message.append(WizardsMessages.PomJavaFolderSynchronizationWizard_libraries_marked_optional);
				}
				throw new WizardInitException(WizardInitException.Status.INFO, WizardsMessages.PomJavaFolderSynchronizationWizard_no_libraries_to_add + message.toString());
			} else {
				throw new WizardInitException(WizardInitException.Status.INFO, WizardsMessages.PomJavaFolderSynchronizationWizard_no_libraries_to_add);
			}
		}
		return artifactVersions;
	}

	/**
	 * Adds the pages.
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		page1 = new PomJavaSynchronizationWizardPage(getPROJECT_NAMES(), getLIBRARIES(), getCHOSENPROJECT());
		page1.setTitle(WizardsMessages.PomJavaFolderSynchronizationWizard_choose_project_and_libraries);
		page1.setImageDescriptor(BasePlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_ORG_64));
		addPage(page1);
		page2 = new ChooseFolderWizardPage();
		page2.setTitle(WizardsMessages.PomJavaFolderSynchronizationWizard_choose_folder);
		page2.setImageDescriptor(BasePlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_ORG_64));
		addPage(page2);
	}

	/**
	 * This method is called when the user hits the "Finish" button.<br>
	 * It runs a job downloading libraries to a given folder.
	 * 
	 * @return true, if perform finish
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject selectedProject = workspaceRoot.getProject(page1.getChosenProjectName());
		IFolder selectedFolder = selectedProject.getFolder(page2.getChosenFolder() == null ? AggregatedProperties.getDefaultLibFolder(selectedProject) : page2.getChosenFolder());
		IProgressMonitor pm = Job.getJobManager().createProgressGroup();
		pm.beginTask(WizardsMessages.PomJavaFolderSynchronizationWizard_monitor_task_name, 100);
		Job job = new DownloadAndAddToFolderJob(selectedProject, selectedFolder, page1.getSelectedLibraries());
		job.setProgressGroup(pm, 100);
		job.schedule();
		return true;
	}

}