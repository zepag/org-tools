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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;
import org.org.eclipse.core.ui.BasePlugin;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.core.ui.images.PluginImages;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.eclipse.dws.core.internal.jobs.DownloadAndAddToClasspathJob;
import org.org.eclipse.dws.core.internal.jobs.DownloadAndAddToFolderJob;
import org.org.eclipse.dws.core.internal.jobs.RemoveConflictingFromClasspathJob;
import org.org.eclipse.dws.core.internal.model.AbstractChosenArtifactVersion;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions;
import org.org.eclipse.dws.ui.internal.wizards.pages.PomWebAppSynchronizationWizardPage;

/**
 * The Class PomWebAppSynchronizationWizard.
 */
public class PomWebAppSynchronizationWizard extends AbstractPomSynchronizationWizard {

	/** The page. */
	private PomWebAppSynchronizationWizardPage page;

	/**
	 * Instantiates a new pom web app synchronization wizard.
	 * 
	 * @param filteringOptions the filtering options
	 * @param pom the parsed pom description
	 */
	@SuppressWarnings("unchecked")
	public PomWebAppSynchronizationWizard(PomDependenciesFilteringOptions filteringOptions, Pom pom) {
		super(filteringOptions, pom, WizardsMessages.PomWebAppSynchronizationWizard_title, false, JavaCore.NATURE_ID);
	}

	/**
	 * Instantiates a new pom web app synchronization wizard.
	 * 
	 * @param artifactVersions the artifact versions
	 */
	@SuppressWarnings("unchecked")
	public PomWebAppSynchronizationWizard(Set<AbstractChosenArtifactVersion> artifactVersions) {
		super(artifactVersions, WizardsMessages.PomWebAppSynchronizationWizard_subtitle, false, JavaCore.NATURE_ID);
	}

	/**
	 * Gets the artifact versions.
	 * 
	 * @param filteringOptions the filtering options
	 * @param pom the parsed pom description
	 * 
	 * @return the artifact versions
	 * 
	 * @see org.org.eclipse.dws.ui.internal.wizards.AbstractPomSynchronizationWizard#getArtifactVersions(org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions, org.org.eclipse.dws.core.internal.model.Pom)
	 */
	@Override
	public Set<AbstractChosenArtifactVersion> getArtifactVersions(PomDependenciesFilteringOptions filteringOptions, Pom pom) {
		Set<AbstractChosenArtifactVersion> artifactVersions = new HashSet<AbstractChosenArtifactVersion>();
		boolean classpathComplete = false;
		boolean allOptional = false;
		Boolean dealWithNarrow = filteringOptions.dealWithNarrow();
		Boolean dealWithOptional = filteringOptions.dealWithOptional();
		if (pom.hasChildren()) {
			if (!dealWithOptional && pom.areAllDependenciesOptional()) {
				allOptional = true;
			} else if (!dealWithNarrow && pom.areAllDependenciesRisky()) {
				classpathComplete = true;
			} else {
				artifactVersions = pom.computeLibrariesFromPomDependencies(filteringOptions);
			}
		} else {
			classpathComplete = true;
		}
		if (classpathComplete) {
			throw new WizardInitException(WizardInitException.Status.INFO, WizardsMessages.PomWebAppSynchronizationWizard_classpath_already_complete);
		}
		if (allOptional) {
			throw new WizardInitException(WizardInitException.Status.INFO, WizardsMessages.PomWebAppSynchronizationWizard_classpath_already_complete_optional_skipped);
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
		page = new PomWebAppSynchronizationWizardPage(getPROJECT_NAMES(), getLIBRARIES(), getCHOSENPROJECT());
		page.setTitle(WizardsMessages.PomWebAppSynchronizationWizard_choose_project);
		page.setImageDescriptor(BasePlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_ORG_64));
		addPage(page);
	}

	/**
	 * This method is called when the user hits the "Finish" button.<br>
	 * It runs 3 jobs:<br>
	 * <ul>
	 * <li> A first one downloading the libraries targetted to the local classpath in the Maven 2 Repo and adding them as references
	 * <li> A second one downloading the libraries targetted to WEB-INF/lib directly to this folder
	 * <li> A third one removing the conflicting libraries from the classpath
	 * </ul>
	 * 
	 * @return true, if perform finish
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject selectedProject = workspaceRoot.getProject(page.getChosenProjectName());
		String webInfLibfolder = AggregatedProperties.getWebAppFolder(selectedProject);
		IFolder selectedFolder = selectedProject.getFolder(webInfLibfolder);
		IProgressMonitor monitor = Job.getJobManager().createProgressGroup();
		int totalTicks = page.getClasspathTargettedLibraries().size() + page.getLibTargettedLibraries().size() + page.getConflictingClasspathEntries().size();
		monitor.beginTask(WizardsMessages.PomWebAppSynchronizationWizard_progress_monitor_taskname, totalTicks);
		try {
			if (selectedProject.hasNature(JavaCore.NATURE_ID)) {
				Job job = new DownloadAndAddToClasspathJob(JavaCore.create(selectedProject), page.getClasspathTargettedLibraries());
				Job job2 = new DownloadAndAddToFolderJob(selectedProject, selectedFolder, page.getLibTargettedLibraries());
				Job job3 = new RemoveConflictingFromClasspathJob(selectedProject, page.getConflictingClasspathEntries());
				job.setProgressGroup(monitor, page.getClasspathTargettedLibraries().size());
				job.schedule();
				job2.setProgressGroup(monitor, page.getLibTargettedLibraries().size());
				job2.schedule();
				job3.setProgressGroup(monitor, page.getConflictingClasspathEntries().size());
				job3.schedule();
			} else {
				ErrorDialog errorDialog = new ErrorDialog("Chosen project is not a java project", "Chosen project should be a project with a java nature.");
				errorDialog.open();
			}
		} catch (CoreException e) {
			ErrorDialog errorDialog = new ErrorDialog("Chosen project in wrong state", "Chosen project should exist and be opened.", e);
			errorDialog.open();
		}
		return true;
	}

}
