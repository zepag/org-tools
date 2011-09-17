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
package org.org.eclipse.cheatsheet.commands.handlers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.org.eclipse.cheatsheet.commands.internal.dialog.FileCreationPromptDialog;
import org.org.eclipse.cheatsheet.commands.internal.jobs.CreateFileInWorkspaceJob;
import org.org.eclipse.core.ui.jobs.completion.CompletionPopupJobChangeListener;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldsValidator;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class CreateFileFromUrlHandler extends AbstractExtendedHandler<Object> {
	private static final String FILE_URL_PARAMETER = "org.org.eclipse.cheatsheet.commands.createFileFromUrlCommand.fileUrl";
	private static final String TARGET_FOLDER_PARAMETER = "org.org.eclipse.cheatsheet.commands.createFileFromUrlCommand.targetFolder";
	private static final String MODE_PARAMETER = "org.org.eclipse.cheatsheet.commands.createFileFromUrlCommand.mode";
	private static final String TARGET_FILE_NAME_PARAMETER = "org.org.eclipse.cheatsheet.commands.createFileFromUrlCommand.targetFileName";
	private static final String CUSTOM_SUFFIX_PARAMETER = "org.org.eclipse.cheatsheet.commands.createFileFromUrlCommand.customSuffix";

	/**
	 * The constructor.
	 */
	public CreateFileFromUrlHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information from the application context.
	 */
	public Object doExecute(ExecutionEvent event, IExecutionSavedContext savedContext) throws ExtendedHandlerExecutionException {
		URL fileUrl = null;
		try {
			fileUrl = new URL(event.getParameter(FILE_URL_PARAMETER));
		} catch (MalformedURLException e) {
			throw new ExtendedHandlerExecutionException("Impossible to parse file's URL", e);
		}
		String mode = event.getParameter(MODE_PARAMETER);
		if (mode == null)
			mode = ModeParameterValues.REPLACE;
		String targetFolder = event.getParameter(TARGET_FOLDER_PARAMETER);
		if (targetFolder == null)
			targetFolder = "";
		String targetFileName = event.getParameter(TARGET_FILE_NAME_PARAMETER);
		if (targetFileName == null)
			targetFileName = determineFileNameFrom(fileUrl);
		if (targetFileName == null)
			throw new ExtendedHandlerExecutionException("Invalid URL, not pointing to a file.");
		String customSuffix = event.getParameter(CUSTOM_SUFFIX_PARAMETER);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		List<IProject> projects = Arrays.asList(workspace.getRoot().getProjects());
		if (mode == null) {
			mode = ModeParameterValues.REPLACE;
		}
		boolean prompt = mode.equals(ModeParameterValues.PROMPT);
		FileCreationPromptDialog userPromptDialog = new FileCreationPromptDialog(HandlerUtil.getActiveShell(event), projects, prompt);
		userPromptDialog.setValidator(new IFieldsValidator() {

			@SuppressWarnings("unchecked")
			public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
				StringBuilderValidationResult validationResult = new StringBuilderValidationResult();
				String chosenProject = (String) fieldValueHolders.get(FileCreationPromptDialog.CHOSEN_PROJECT_FIELD).getValue();
				if (chosenProject == null || chosenProject.trim().equals("")) {
					validationResult.append("Please choose a project.");
				}

				IFieldValueHolder modeFieldValueHolder = fieldValueHolders.get(FileCreationPromptDialog.MODE_PROJECT_FIELD);
				if (modeFieldValueHolder != null) {
					String mode = (String) modeFieldValueHolder.getValue();
					if (mode == null || mode.trim().equals("")) {
						modeFieldValueHolder.setValue(ModeParameterValues.SUFFIX);
					}
				}
				return validationResult;
			}

		});
		if (userPromptDialog.open() == Window.OK) {
			String chosenProject = userPromptDialog.getChosenProject();
			mode = mode.equals(ModeParameterValues.PROMPT) ? userPromptDialog.getMode() : mode;

			launchJob(fileUrl, mode, targetFolder, targetFileName, customSuffix, workspace, chosenProject);

		}
		return null;
	}

	protected void launchJob(URL fileUrl, String mode, String targetFolder, String targetFileName, String customSuffix, IWorkspace workspace, String chosenProject) {
		Job job = new CreateFileInWorkspaceJob(workspace.getRoot().getProject(chosenProject), fileUrl, targetFolder, targetFileName, mode, customSuffix);
		job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG CheatSheet Helpers Notification", "Creation of file in workspace:\n"));
		job.schedule();
	}

	private String determineFileNameFrom(URL fileUrl) {
		StringTokenizer tkz = new StringTokenizer(fileUrl.toExternalForm(), "\\/", true);
		String result = null;
		while (tkz.hasMoreTokens()) {
			result = tkz.nextToken();

		}
		if (result != null && !result.matches(".*\\..*")) {
			result = null;
		}
		return result;
	}
}
