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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;
import org.org.eclipse.core.ui.BasePlugin;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.core.ui.images.PluginImages;
import org.org.eclipse.dws.core.internal.jobs.DownloadAndAddToClasspathJob;
import org.org.eclipse.dws.core.internal.jobs.RemoveConflictingFromClasspathJob;
import org.org.eclipse.dws.core.internal.model.AbstractChosenArtifactVersion;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions;
import org.org.eclipse.dws.ui.internal.wizards.pages.PomJavaSynchronizationWizardPage;


/**
 * The Class PomJavaClasspathSynchronizationWizard.
 */
public class PomJavaClasspathSynchronizationWizard extends AbstractPomSynchronizationWizard {

	/** The page. */
	private PomJavaSynchronizationWizardPage page;

	/**
	 * Instantiates a new pom java classpath synchronization wizard.
	 * 
	 * @param artifactVersions the artifact versions
	 */
	@SuppressWarnings("unchecked")
	public PomJavaClasspathSynchronizationWizard(Set<AbstractChosenArtifactVersion> artifactVersions) {
		super(artifactVersions, WizardsMessages.PomJavaClasspathSynchronizationWizard_subtitle, false, JavaCore.NATURE_ID);
	}

	/**
	 * Instantiates a new pom java classpath synchronization wizard.
	 * 
	 * @param options the options
	 * @param pom the parsed pom description
	 */
	@SuppressWarnings("unchecked")
	public PomJavaClasspathSynchronizationWizard(PomDependenciesFilteringOptions options, Pom pom) {
		super(options, pom, WizardsMessages.PomJavaClasspathSynchronizationWizard_subtitle, false, JavaCore.NATURE_ID);
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
		boolean allSkipped = false;
		Boolean dealWithNarrow = filteringOptions.dealWithNarrow();
		Boolean dealWithOptional = filteringOptions.dealWithOptional();
		if (pom.hasChildren()) {
			if (!dealWithOptional && pom.areAllDependenciesOptional()) {
				allOptional = true;
			} else if (!dealWithNarrow && pom.areAllDependenciesRisky()) {
				classpathComplete = true;
			} else {
				artifactVersions = pom.computeLibrariesFromPomDependencies(filteringOptions);
				allSkipped = areAllSkipped(artifactVersions);
			}
		} else {
			classpathComplete = true;
		}
		if (classpathComplete) {
			throw new WizardInitException(WizardInitException.Status.INFO, WizardsMessages.PomJavaClasspathSynchronizationWizard_classpath_already_complete);
		}
		if (allOptional) {
			throw new WizardInitException(WizardInitException.Status.INFO, WizardsMessages.PomJavaClasspathSynchronizationWizard_classpath_already_complete_optional_skipped);
		}
		if (allSkipped) {
			throw new WizardInitException(WizardInitException.Status.INFO, WizardsMessages.PomJavaClasspathSynchronizationWizard_classpath_already_complete_libraries_skipped);
		}
		return artifactVersions;
	}

	/**
	 * Are all skipped.
	 * 
	 * @param artifactVersions the artifact versions
	 * 
	 * @return true, if successful
	 */
	private boolean areAllSkipped(Set<AbstractChosenArtifactVersion> artifactVersions) {
		boolean result = true;
		for (AbstractChosenArtifactVersion artifactVersion : artifactVersions) {
			if (!artifactVersion.isSkipped()) {
				result = false;
				break;
			}
		}
		return result;
	}

	/**
	 * Adds the pages.
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		page = new PomJavaSynchronizationWizardPage(getPROJECT_NAMES(), getLIBRARIES(), getCHOSENPROJECT());
		page.setTitle(WizardsMessages.PomJavaClasspathSynchronizationWizard_choose_project_and_libraries);
		page.setImageDescriptor(BasePlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_ORG_64));
		addPage(page);
	}

	/**
	 * This method is called when the user hits the "Finish" button.<br>
	 * It runs 2 jobs:<br>
	 * <ul>
	 * <li> A first one downloading the libraries targetted to the local classpath in the Maven 2 Repo and adding them as references
	 * <li> A second one removing the conflicting libraries from the classpath
	 * </ul>
	 * 
	 * @return true, if perform finish
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		final IProject selectedProject = workspaceRoot.getProject(page.getChosenProjectName());
		final IProgressMonitor monitor = Job.getJobManager().createProgressGroup();
		try {
			if (selectedProject.hasNature(JavaCore.NATURE_ID)) {
				Job job = new DownloadAndAddToClasspathJob(JavaCore.create(selectedProject), page.getSelectedLibraries());
				final Job job2 = new RemoveConflictingFromClasspathJob(selectedProject, page.getConflictingClasspathEntries());
				job.setProgressGroup(monitor, 50);
				job.addJobChangeListener(new IJobChangeListener() {

					public void sleeping(IJobChangeEvent event) {
					}

					public void scheduled(IJobChangeEvent event) {
					}

					public void running(IJobChangeEvent event) {
					}

					public void done(IJobChangeEvent event) {
						if (event.getResult().getSeverity() == IStatus.OK) {
							job2.setProgressGroup(monitor, 50);
							job2.schedule();
						}
					}

					public void awake(IJobChangeEvent event) {
					}

					public void aboutToRun(IJobChangeEvent event) {
					}

				});
				job.schedule();
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
