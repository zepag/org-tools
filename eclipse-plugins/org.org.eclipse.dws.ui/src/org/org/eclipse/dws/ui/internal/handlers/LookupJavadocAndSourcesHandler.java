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
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
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
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldsValidator;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;
import org.org.eclipse.dws.core.internal.dialogs.ProjectPromptDialog;
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.eclipse.dws.ui.internal.wizards.JavadocSourcesLookupWizard;
import org.org.eclipse.dws.ui.internal.wizards.WizardInitException;
import org.org.model.RootModelItem;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * This Action launches the Pom update application Synchronization Wizard.
 * 
 * @author pagregoire
 */
public class LookupJavadocAndSourcesHandler extends AbstractExtendedHandler<Object> {

	/** The Constant HANDLER_LABEL. */
	public static final String HANDLER_LABEL = "Javadoc and Sources Lookup action"; //$NON-NLS-1$

	/** Logger for this class. */
	private static Logger logger = Logger.getLogger(LookupJavadocAndSourcesHandler.class);

	// FIXME use a JOB
	public Object doExecute(ExecutionEvent event, IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException {
		try {
			if (!(RootModelItem.<CrawledRepository> getInstance(ModelConstants.REPOSITORIES_ROOT).hasChildren())) {
				WarningDialog warningDialog = new WarningDialog(HANDLER_LABEL, "You have not defined any maven 2 repository!");
				warningDialog.open();
			} else {
				IProject project = determineProjectFromSelection(event);
				if (project != null && JavaProjectHelper.isJavaProject(project)) {
					JavadocSourcesLookupWizard wizard = new JavadocSourcesLookupWizard(JavaCore.create(project));
					WizardDialog dialog = new WizardDialog(PluginToolBox.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
					dialog.open();
				} else {
					WarningDialog warningDialog = new WarningDialog(HANDLER_LABEL, "Project is not a java project so looking up javadoc and/or sources for its referenced libraries is meaningless");
					warningDialog.open();
				}
			}
		} catch (WizardInitException e) {
			manageWizardInitException(e);
		} catch (Throwable e) {
			logger.error("Unattended Exception occured", e);
			ErrorDialog errorDialog = new ErrorDialog(HANDLER_LABEL, MessageFormat.format("Unattended Exception occured : [{0} :{1}]", new Object[] { e.getClass().getName(), e.getMessage(), e }));
			errorDialog.open();
		}
		return null;
	}

	/**
	 * Determine project from selection.
	 * 
	 * @param event
	 *            the event
	 * 
	 * @return the i project
	 */
	private IProject determineProjectFromSelection(ExecutionEvent event) {
		IProject project = null;
		try {
			ISelection currentSelection = HandlerUtil.getCurrentSelection(event);

			Object firstElement = ((IStructuredSelection) currentSelection).getFirstElement();

			if (firstElement != null) {
				if (firstElement instanceof IResource) {
					IResource resource = ((IResource) firstElement);
					project = resource.getProject();
				} else if (firstElement instanceof IJavaElement) {
					IJavaElement javaElement = ((IJavaElement) firstElement);
					IJavaProject javaProject = javaElement.getJavaProject();
					if (javaProject != null) {
						project = javaProject.getProject();
					}
				}
			}
		} catch (Throwable e) {
			// trap this
		}
		if (project == null) {
			java.util.List<IJavaProject> projects = JavaProjectHelper.getJavaProjects();
			if (projects.size() > 0) {
				ProjectPromptDialog userPromptDialog = new ProjectPromptDialog(HandlerUtil.getActiveShell(event), projects);
				userPromptDialog.setValidator(new IFieldsValidator() {

					@SuppressWarnings("unchecked")
					public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
						StringBuilderValidationResult validationResult = new StringBuilderValidationResult();
						String chosenProject = (String) fieldValueHolders.get(ProjectPromptDialog.CHOSEN_PROJECT_FIELD).getValue();
						if (chosenProject == null || chosenProject.trim().equals("")) {
							validationResult.append("Please choose a project.");
						}
						return validationResult;
					}

				});
				if (userPromptDialog.open() == Window.OK) {
					IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
					project = workspaceRoot.getProject(userPromptDialog.getChosenProject());
				}
			}
		}
		return project;
	}

	/**
	 * Manage wizard init exception.
	 * 
	 * @param e
	 *            the e
	 */
	public static void manageWizardInitException(WizardInitException e) {
		switch (e.getStatus()) {
		case ERROR:
			ErrorDialog errorDialog = new ErrorDialog(WizardInitException.Status.ERROR.name(), e.getMessage());
			errorDialog.open();
			break;
		case INFO:
			InfoDialog infoDialog = new InfoDialog(WizardInitException.Status.INFO.name(), e.getMessage());
			infoDialog.open();
			break;
		case WARNING:
			WarningDialog warningDialog = new WarningDialog(WizardInitException.Status.WARNING.name(), e.getMessage());
			warningDialog.open();
			break;
		default:
			break;
		}
	}

}