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
package org.org.eclipse.dws.ui.internal.handlers;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.org.eclipse.core.ui.dialogs.InfoDialog;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;
import org.org.eclipse.dws.core.internal.jobs.FindRepositoryJob;
import org.org.eclipse.dws.ui.internal.wizards.EditRepositoryWizard;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * The Class FindRepositoriesHandler.
 */
public class FindRepositoriesHandler extends AbstractExtendedHandler<Object> {

	/** Logger for this class. */
	private static Logger logger = Logger.getLogger(FindRepositoriesHandler.class);

	public Object doExecute(ExecutionEvent event, IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException {
		logger.debug("Find repositories");
		final List<IProject> projects = computeProjects();
		Job job = new FindRepositoryJob(projects);
		job.addJobChangeListener(new IJobChangeListener() {

			public void sleeping(IJobChangeEvent event) {

			}

			public void scheduled(IJobChangeEvent event) {

			}

			public void running(IJobChangeEvent event) {

			}

			public void done(IJobChangeEvent event) {
				FindRepositoryJob job = (FindRepositoryJob) event.getJob();
				Set<CrawledRepository> crawledRepositories = job.getFoundRepositories();
				if (crawledRepositories.size() < 1) {
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

						public void run() {
							InfoDialog infoDialog = new InfoDialog("Impossible to find crawledRepositories", "No Maven repository definition in any pom.xml file in the workspace.");
							infoDialog.open();
						}

					});
				} else {
					for (final CrawledRepository crawledRepository : crawledRepositories) {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

							public void run() {
								EditRepositoryWizard editRepositoryWizard = new EditRepositoryWizard(crawledRepository);
								WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), editRepositoryWizard);
								dialog.open();
							}

						});
					}
				}
			}

			public void awake(IJobChangeEvent event) {

			}

			public void aboutToRun(IJobChangeEvent event) {

			}

		});
		job.schedule();
		return null;
	}

	/**
	 * Compute projects.
	 * 
	 * @return the list< i project>
	 */
	private List<IProject> computeProjects() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		return Arrays.asList(workspaceRoot.getProjects());
	}
}
