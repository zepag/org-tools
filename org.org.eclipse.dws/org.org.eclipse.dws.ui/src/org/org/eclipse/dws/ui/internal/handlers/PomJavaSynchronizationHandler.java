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

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
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
import org.org.eclipse.dws.core.internal.dialogs.ClasspathOrFolderPromptDialog;
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions;
import org.org.eclipse.dws.ui.internal.actions.ActionsMessages;
import org.org.eclipse.dws.ui.internal.wizards.PomJavaClasspathSynchronizationWizard;
import org.org.eclipse.dws.ui.internal.wizards.PomJavaFolderSynchronizationWizard;
import org.org.eclipse.dws.ui.internal.wizards.WizardInitException;
import org.org.eclipse.dws.ui.internal.wizards.WizardsMessages;
import org.org.eclipse.dws.ui.internal.wizards.WizardInitException.Status;
import org.org.model.RootModelItem;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * The Class PomJavaSynchronizationHandler.
 */
public class PomJavaSynchronizationHandler extends AbstractExtendedHandler<Object> {

	/** The Constant HANDLER_LABEL. */
	private final static String HANDLER_LABEL = "Parse pom action";

	/** The logger. */
	private static Logger logger = Logger.getLogger(PomJavaSynchronizationHandler.class);

	public Object doExecute(ExecutionEvent event, IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException {
		IStructuredSelection structuredSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		String chosenAction = ActionsMessages.PomJavaSynchronizationActionDelegate_undetermined;
		try {
			if (!(RootModelItem.<CrawledRepository> getInstance(ModelConstants.REPOSITORIES_ROOT).hasChildren())) {
				WarningDialog warningDialog = new WarningDialog(HANDLER_LABEL, ActionsMessages.PomJavaSynchronizationActionDelegate_noRepositoryDefined);
				warningDialog.open();
			} else {
				IProject project = ((IFile) structuredSelection.getFirstElement()).getProject();
				if (JavaProjectHelper.isJavaProject(project)) {
					ClasspathOrFolderPromptDialog comboInputDialog = new ClasspathOrFolderPromptDialog(PluginToolBox.getWorkbench().getActiveWorkbenchWindow().getShell());
					if (comboInputDialog.open() == Window.OK) {
						if (comboInputDialog.getChoice().equals(ClasspathOrFolderPromptDialog.ADD_TO_CLASSPATH)) {
							chosenAction = ClasspathOrFolderPromptDialog.ADD_TO_CLASSPATH;
							addToClasspath(structuredSelection);
						} else if (comboInputDialog.getChoice().equals(ClasspathOrFolderPromptDialog.ADD_TO_FOLDER)) {
							chosenAction = ClasspathOrFolderPromptDialog.ADD_TO_FOLDER;
							addToFolder(structuredSelection);
						}
					}
				} else {
					addToFolder(structuredSelection);
				}
			}
		} catch (WizardInitException e) {
			manageWizardInitException(chosenAction, e);
		} catch (Throwable e) {
			logger.error(ActionsMessages.AbstractParsePomActionDelegate_unattendedException, e);
			ErrorDialog errorDialog = new ErrorDialog(HANDLER_LABEL, MessageFormat.format(ActionsMessages.AbstractParsePomActionDelegate_unattendedExceptionWithDescription, new Object[] { e.getClass().getName(), e.getMessage(), e }));
			errorDialog.open();
		}
		return null;
	}

	/**
	 * Manage wizard init exception.
	 * 
	 * @param chosenAction
	 *            the chosen action
	 * @param e
	 *            the e
	 */
	public static void manageWizardInitException(String chosenAction, WizardInitException e) {
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
	}

	// FIXME use a Job
	/**
	 * Adds the to folder.
	 * 
	 * @param structuredSelection
	 *            the structured selection
	 * 
	 * @throws WizardInitException
	 *             the wizard init exception
	 */
	private void addToFolder(IStructuredSelection structuredSelection) throws WizardInitException {
		IFile selectedFile = (IFile) structuredSelection.getFirstElement();
		IProject project = selectedFile.getProject();
		try {
			ProjectHelper.checkProjectStatus(project, Job.getJobManager().createProgressGroup());

		} catch (CoreException e) {
			throw new WizardInitException(WizardInitException.Status.ERROR, e);
		}
		PomDependenciesFilteringOptions options = PomInteractionHelper.preparePomDependenciesFilteringOptions(project);
		Pom pom = null;
		try {
			pom = PomInteractionHelper.getParsedPomDescription(selectedFile);
			pom.filterDependencies(options);
		} catch (PomInteractionException e) {
			throw new WizardInitException(Status.ERROR, e);
		}
		PomJavaFolderSynchronizationWizard wizard = new PomJavaFolderSynchronizationWizard(options, pom);
		WizardDialog dialog = new WizardDialog(PluginToolBox.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		dialog.open();
	}

	// FIXME use a Job
	/**
	 * Adds the to classpath.
	 * 
	 * @param structuredSelection
	 *            the structured selection
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private void addToClasspath(IStructuredSelection structuredSelection) throws Exception {
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
			PomDependenciesFilteringOptions options = PomInteractionHelper.preparePomDependenciesFilteringOptions(project);
			Pom pom = null;
			try {
				pom = PomInteractionHelper.getParsedPomDescription(selectedFile);
				pom.filterDependencies(options);
			} catch (PomInteractionException e) {
				throw new WizardInitException(Status.ERROR, e);
			}
			PomJavaClasspathSynchronizationWizard wizard = new PomJavaClasspathSynchronizationWizard(options, pom);
			WizardDialog dialog = new WizardDialog(PluginToolBox.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
			dialog.open();
		} else {
			throw new WizardInitException(WizardInitException.Status.WARNING, MessageFormat.format(WizardsMessages.PomJavaClasspathSynchronizationWizard_project_not_java, new Object[] { project.getName() }));
		}
	}

	// /**
	// * Gets the parsed pom description.
	// *
	// * @param selectedFile
	// * the selected file
	// *
	// * @return the parsed pom description
	// *
	// * @throws WizardInitException
	// * the wizard init exception
	// */
	// private Pom getParsedPomDescription(IFile selectedFile) throws WizardInitException {
	// Pom pom;
	// try {
	// pom = PomInteractionHelper.parsePom(selectedFile.getContents());
	// } catch (CoreException e) {
	// throw new WizardInitException(WizardInitException.Status.ERROR, e);
	// }
	// return pom;
	// }
	//
	// /**
	// * Prepare options.
	// *
	// * @param project
	// * the project
	// *
	// * @return the filtering options
	// */
	// private PomDependenciesFilteringOptions prepareOptions(IProject project) {
	// PomDependenciesFilteringOptions.Builder optionsBuilder = new PomDependenciesFilteringOptions.Builder();
	// optionsBuilder.projectClasspathEntries(ProjectInteractionHelper.getClasspathEntries(JavaCore.create(project)));
	// optionsBuilder.scopeFilter(ScopeFilter.NONE);
	// optionsBuilder.filter(Filter.NONE);
	// optionsBuilder.dealWithTransitive(AggregatedProperties.getDealWithTransitive(project));
	// optionsBuilder.dealWithOptional(AggregatedProperties.getDealWithOptional(project));
	// optionsBuilder.dealWithNarrow(AggregatedProperties.getDealWithNarrow(project));
	// optionsBuilder.skippedDependencies(AggregatedProperties.getSkippedDependencies(project));
	// optionsBuilder.artifactExtensions(AggregatedProperties.getArtifactExtensions());
	// PomDependenciesFilteringOptions filteringOptions = optionsBuilder.build();
	// return filteringOptions;
	// }

}