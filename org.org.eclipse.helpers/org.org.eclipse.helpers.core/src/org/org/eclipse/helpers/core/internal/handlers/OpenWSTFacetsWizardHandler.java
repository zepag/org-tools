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
package org.org.eclipse.helpers.core.internal.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.ui.ModifyFacetedProjectWizard;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldsValidator;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.message.ErrorDialog;
import org.org.eclipse.core.utils.platform.dialogs.message.InfoDialog;
import org.org.eclipse.core.utils.platform.dialogs.message.WarningDialog;
import org.org.eclipse.helpers.core.internal.dialog.ProjectChoicePromptDialog;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class OpenWSTFacetsWizardHandler extends AbstractExtendedHandler<Object> {
	private static final String COMMAND_ID = "org.org.eclipse.helpers.core.OpenWSTFacetsWizardCommand";
	private static final String TARGET_PROJECT_NAME_PARAMETER = COMMAND_ID + ".targetProjects";
	private static final String COMMA = ",";

	/**
	 * The constructor.
	 */
	public OpenWSTFacetsWizardHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information from the application context.
	 */
	public Object doExecute(ExecutionEvent event, org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		Set<IProject> projects = new HashSet<IProject>();
		final String projectNames = event.getParameter(TARGET_PROJECT_NAME_PARAMETER);
		validateProjectNames(workspaceRoot, projects, projectNames);
		if (projects.size() == 0) {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (selection != null && selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				for (Iterator<?> it = structuredSelection.iterator(); it.hasNext();) {
					Object selectedObject = it.next();
					if (IProject.class.isAssignableFrom(selectedObject.getClass())) {
						projects.add((IProject) selectedObject);
					} else if (IFacetedProject.class.isAssignableFrom(selectedObject.getClass())) {
						projects.add(((IFacetedProject) selectedObject).getProject());
					} else if (IJavaProject.class.isAssignableFrom(selectedObject.getClass())) {
						projects.add(((IJavaProject) selectedObject).getProject());
					} else if (IResource.class.isAssignableFrom(selectedObject.getClass())) {
						projects.add(((IResource) selectedObject).getProject());
					} else if (IJavaElement.class.isAssignableFrom(selectedObject.getClass())) {
						projects.add(((IJavaElement) selectedObject).getJavaProject().getProject());
					}
				}
			} else {
				List<IProject> workspaceProjectsWithFacets = getProjectsWithFacets(workspaceRoot);
				if (workspaceProjectsWithFacets.size() > 0) {
					ProjectChoicePromptDialog userPromptDialog = new ProjectChoicePromptDialog(HandlerUtil.getActiveShell(event), workspaceProjectsWithFacets);
					userPromptDialog.setValidator(new IFieldsValidator() {

						@SuppressWarnings("rawtypes")
						public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
							StringBuilderValidationResult validationResult = new StringBuilderValidationResult();
							String[] chosenProjects = (String[]) fieldValueHolders.get(ProjectChoicePromptDialog.CHOSEN_PROJECT_FIELD).getValue();
							if (chosenProjects == null || chosenProjects.length == 0) {
								validationResult.append("Please choose at least one project.");
							}
							return validationResult;
						}

					});
					if (userPromptDialog.open() == Window.OK) {
						String[] chosenProjects = userPromptDialog.getChosenProjects();
						for (String projectName : chosenProjects) {
							IProject project = workspaceRoot.getProject(projectName);
							projects.add(project);
						}
					}
				} else {
					WarningDialog warningDialog = new WarningDialog("No project", "No project without Faceted nature.");
					warningDialog.open();
				}
			}
		}
		if (projects.size() > 0) {
			for (IProject project : projects) {
				try {
					if (projects.size() > 1) {
						InfoDialog infoDialog = new InfoDialog("Project " + project.getName(), "Opening facets wizard for project:" + project.getName());
						infoDialog.open();
					}
					IWizard wizard = new ModifyFacetedProjectWizard(ProjectFacetsManager.create(project));
					WizardDialog wizardDialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
					wizardDialog.open();
				} catch (CoreException e) {
					ErrorDialog errorDialog = new ErrorDialog("Error", "Impossible to open facets wizard.");
					errorDialog.open();
				}
			}
		}
		return null;
	}

	private List<IProject> getProjectsWithFacets(IWorkspaceRoot workspaceRoot) {
		List<IProject> allProjects = Arrays.asList(workspaceRoot.getProjects());
		List<IProject> result = new ArrayList<IProject>();
		for (IProject project : allProjects) {
			try {
				if (project.hasNature("org.eclipse.wst.common.project.facet.core.nature")) {
					result.add(project);
				}
			} catch (CoreException e) {
				// Do something deeply meaningful
			}
		}
		return result;
	}

	private void validateProjectNames(IWorkspaceRoot workspaceRoot, Set<IProject> projects, String projectNames) {
		if (projectNames != null) {
			for (String projectName : projectNames.split(COMMA)) {
				IProject project = workspaceRoot.getProject(projectName);
				if (project.exists()) {
					projects.add(project);
				}
			}
		}
	}
}
