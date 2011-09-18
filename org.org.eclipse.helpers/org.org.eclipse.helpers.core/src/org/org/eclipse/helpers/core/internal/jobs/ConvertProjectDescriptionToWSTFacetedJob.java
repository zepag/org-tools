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
package org.org.eclipse.helpers.core.internal.jobs;

import java.io.File;
import java.util.Collection;
import java.util.regex.Pattern;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.ui.ModifyFacetedProjectWizard;
import org.org.eclipse.core.utils.jdt.tools.JavaProjectClasspathHelper;
import org.org.eclipse.core.utils.platform.dialogs.message.InfoDialog;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.helpers.core.internal.xml.IProjectFileModifier;
import org.org.eclipse.helpers.core.internal.xml.StaxProjectFileModifier;

public class ConvertProjectDescriptionToWSTFacetedJob extends Job {

	private static String JOB_ID = "ORG Helper: Convert to WST Faceted project";
	private final Collection<IProject> projects;

	public ConvertProjectDescriptionToWSTFacetedJob(Collection<IProject> projects) {
		super(JOB_ID);
		this.projects = projects;
		this.setPriority(Job.LONG);
		this.setUser(true);
		this.setRule(new BatchSimilarRule(HelpersJobs.JOB_FAMILY));
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus result = new StatusInfo(IStatus.OK, "Project(s) successfully converted.");
		try {
			StringBuilder buffer = new StringBuilder();
			for (final IProject project : projects) {
				File dotprojectfile = project.findMember(".project").getRawLocation().toFile();
				project.close(new SubProgressMonitor(monitor, 1));
				System.out.println(dotprojectfile.getAbsolutePath());
				IProjectFileModifier projectFileModifier = new StaxProjectFileModifier();
				try {
					projectFileModifier.modifyProjectFile(dotprojectfile);
				} catch (Throwable e) {
					throw new ExecutionException("Impossible to modify .project file for project " + project.getName(), e);
				}
				project.open(new SubProgressMonitor(monitor, 1));
				if (project.hasNature(JavaCore.NATURE_ID)) {
					// let's remove web entries from classpath
					// as they will be automatically detected by the IDE
					// and added to the classpath in the proper Library Container.
					removeWebClasspathEntriesFromClasspath(project, monitor);
				}
				try {
					project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				} catch (CoreException e) {
					throw new ExecutionException("Impossible to refresh project " + project.getName());
				}

				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						try {
							InfoDialog infoDialog = new InfoDialog("Project " + project.getName(), "Opening facets wizard for project:" + project.getName());
							infoDialog.open();
							IWizard wizard = new ModifyFacetedProjectWizard(ProjectFacetsManager.create(project));
							WizardDialog wizardDialog = new WizardDialog(PluginToolBox.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
							wizardDialog.open();
						} catch (CoreException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				});

				buffer.append("\n- converted project " + project.getName());
			}
			result = new StatusInfo(IStatus.OK, "Project(s) successfully converted." + buffer.toString());
		} catch (Throwable e) {
			monitor.setCanceled(true);
			result = new StatusInfo(IStatus.ERROR, "A problem occured while converting project:\n" + e.getMessage());
		}
		monitor.done();
		return result;
	}

	private void removeWebClasspathEntriesFromClasspath(IProject project, IProgressMonitor monitor) {
		Pattern pattern = Pattern.compile(".*WEB-INF.*");
		IJavaProject javaProject = JavaCore.create(project);
		if (JavaProjectClasspathHelper.listClasspathEntriesWithPathMatching(javaProject, pattern, monitor).length > 0) {
			JavaProjectClasspathHelper.removeClasspathEntriesWithPathMatching(javaProject, pattern, new SubProgressMonitor(monitor, 1));
		}
	}
}