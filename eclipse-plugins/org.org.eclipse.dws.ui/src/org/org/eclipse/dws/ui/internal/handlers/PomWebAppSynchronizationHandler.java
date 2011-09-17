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

import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.core.ui.dialogs.InfoDialog;
import org.org.eclipse.core.ui.dialogs.WarningDialog;
import org.org.eclipse.core.utils.jdt.tools.JavaProjectHelper;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;
import org.org.eclipse.core.utils.platform.tools.ProjectHelper;
import org.org.eclipse.dws.core.internal.PomInteractionHelper;
import org.org.eclipse.dws.core.internal.PomInteractionHelper.PomInteractionException;
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions;
import org.org.eclipse.dws.ui.internal.actions.ActionsMessages;
import org.org.eclipse.dws.ui.internal.wizards.PomWebAppSynchronizationWizard;
import org.org.eclipse.dws.ui.internal.wizards.WizardInitException;
import org.org.eclipse.dws.ui.internal.wizards.WizardsMessages;
import org.org.eclipse.dws.ui.internal.wizards.WizardInitException.Status;
import org.org.model.RootModelItem;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * This Action launches the Pom /Web application Synchronization Wizard.
 * 
 * @author pagregoire
 */
public class PomWebAppSynchronizationHandler extends AbstractExtendedHandler<Object> {

	/** The Constant HANDLER_LABEL. */
	private final static String HANDLER_LABEL = "Parse pom action";

	public Object doExecute(ExecutionEvent event, IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException {
		// FIXME PARAMETER/JOB
		IStructuredSelection structuredSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		String chosenAction = ActionsMessages.PomWebAppSynchronizationActionDelegate_name;
		try {
			if (!(RootModelItem.<CrawledRepository> getInstance(ModelConstants.REPOSITORIES_ROOT).hasChildren())) {
				WarningDialog warningDialog = new WarningDialog(HANDLER_LABEL, ActionsMessages.PomWebAppSynchronizationActionDelegate_noRepositoryDefined);
				warningDialog.open();
			} else {
				IFile selectedFile = (IFile) structuredSelection.getFirstElement();
				IProject project = selectedFile.getProject();
				boolean isJavaProject = false;
				try {
					isJavaProject = JavaProjectHelper.isJavaProject(project);
					ProjectHelper.checkProjectStatus(project, Job.getJobManager().createProgressGroup());

				} catch (CoreException e) {
					throw new WizardInitException(WizardInitException.Status.ERROR, e);
				}
				if (isJavaProject) {
					// FIXME use a Job
					PomDependenciesFilteringOptions options = PomInteractionHelper.preparePomDependenciesFilteringOptions(project);
					Pom pom = null;
					try {
						pom = PomInteractionHelper.getParsedPomDescription(selectedFile);
						pom.filterDependencies(options);
					} catch (PomInteractionException e) {
						throw new WizardInitException(Status.ERROR, e);
					}
					PomWebAppSynchronizationWizard wizard = new PomWebAppSynchronizationWizard(options, pom);
					wizard.setCHOSENPROJECT(project.getName());
					WizardDialog dialog = new WizardDialog(PluginToolBox.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
					dialog.open();
				} else {
					throw new WizardInitException(WizardInitException.Status.WARNING, MessageFormat.format(WizardsMessages.PomWebAppSynchronizationWizard_project_not_java, new Object[] { project.getName() }));
				}
			}
		} catch (WizardInitException e) {
			switch (e.getStatus()) {
			case ERROR:
				ErrorDialog errorDialog = new ErrorDialog(chosenAction + "-" + WizardInitException.Status.ERROR.name(), e.getMessage()); //$NON-NLS-1$
				errorDialog.open();
				break;
			case INFO:
				InfoDialog infoDialog = new InfoDialog(chosenAction + "-" + WizardInitException.Status.INFO.name(), e.getMessage()); //$NON-NLS-1$
				infoDialog.open();
				break;
			case WARNING:
				WarningDialog warningDialog = new WarningDialog(chosenAction + "-" + WizardInitException.Status.WARNING.name(), e.getMessage()); //$NON-NLS-1$
				warningDialog.open();
				break;
			default:
				break;
			}
		} catch (Throwable e) {
			ErrorDialog errorDialog = new ErrorDialog("Unexpected error", "Something went wrong...", e); //$NON-NLS-1$
			errorDialog.open();
		}
		return null;
	}
}