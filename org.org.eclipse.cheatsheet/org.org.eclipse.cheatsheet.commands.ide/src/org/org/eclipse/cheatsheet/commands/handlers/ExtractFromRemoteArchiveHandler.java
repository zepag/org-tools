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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipFile;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.org.eclipse.cheatsheet.commands.internal.dialog.FileCreationPromptDialog;
import org.org.eclipse.cheatsheet.commands.internal.jobs.ExtractContentsInProjectJob;
import org.org.eclipse.core.ui.jobs.completion.CompletionPopupJobChangeListener;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;

public class ExtractFromRemoteArchiveHandler extends AbstractExtendedHandler<Object> {
	private static final String FILE_URL_PARAMETER = "org.org.eclipse.cheatsheet.commands.extractFromRemoteArchiveCommand.fileUrl";
	private static final String PATH_IN_ARCHIVE_PARAMETER = "org.org.eclipse.cheatsheet.commands.extractFromRemoteArchiveCommand.pathInArchive";
	private static final String PATH_IN_PROJECT_PARAMETER = "org.org.eclipse.cheatsheet.commands.extractFromRemoteArchiveCommand.pathInProject";
	private static final String MODE_PARAMETER = "org.org.eclipse.cheatsheet.commands.extractFromRemoteArchiveCommand.mode";
	private static final String CUSTOM_SUFFIX_PARAMETER = "org.org.eclipse.cheatsheet.commands.extractFromRemoteArchiveCommand.customSuffix";

	/**
	 * The constructor.
	 */
	public ExtractFromRemoteArchiveHandler() {
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
		String pathInArchive = event.getParameter(PATH_IN_ARCHIVE_PARAMETER);
		if (pathInArchive == null) {
			pathInArchive = "/";
		}
		String pathInTargetProject = event.getParameter(PATH_IN_PROJECT_PARAMETER);
		if (pathInTargetProject == null) {
			pathInTargetProject = "/";
		}
		String mode = event.getParameter(MODE_PARAMETER);
		if (mode == null)
			mode = ModeParameterValues.REPLACE;
		String customSuffix = event.getParameter(CUSTOM_SUFFIX_PARAMETER);

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		List<IProject> projects = Arrays.asList(workspace.getRoot().getProjects());
		if (mode == null) {
			mode = ModeParameterValues.SUFFIX;
		}
		boolean promptForMode = mode.equals(ModeParameterValues.PROMPT);
		FileCreationPromptDialog userPromptDialog = new FileCreationPromptDialog(HandlerUtil.getActiveShell(event), projects, promptForMode);
		if (userPromptDialog.open() == Window.OK) {
			String targetProjectName = userPromptDialog.getChosenProject();
			if (promptForMode) {
				mode = userPromptDialog.getMode();
			}
			Job job = new ExtractContentsInProjectJob(targetProjectName, fileUrl, pathInArchive, pathInTargetProject, mode, customSuffix);
			job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG CheatSheet Helpers Notification", "Content extraction ended:\n"));
			job.schedule();
		}

		return null;
	}

	public static boolean isZipFile(String fileName) {
		if (fileName.length() == 0) {
			return false;
		}

		try {
			new ZipFile(fileName);
		} catch (IOException ioException) {
			return false;
		}

		return true;
	}

}
