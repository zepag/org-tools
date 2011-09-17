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
import java.util.StringTokenizer;
import java.util.zip.ZipFile;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.org.eclipse.cheatsheet.commands.internal.dialog.ProjectCreationPromptDialog;
import org.org.eclipse.cheatsheet.commands.internal.jobs.CreateProjectInWorkspaceJob;
import org.org.eclipse.core.ui.jobs.completion.CompletionPopupJobChangeListener;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class CreateProjectFromRemoteArchiveHandler extends AbstractExtendedHandler<Object>{
	private static final String FILE_URL_PARAMETER = "org.org.eclipse.cheatsheet.commands.createProjectFromRemoteArchiveCommand.fileUrl";
	private static final String PATH_IN_ARCHIVE_PARAMETER = "org.org.eclipse.cheatsheet.commands.createProjectFromRemoteArchiveCommand.pathInArchive";
	private static final String TARGET_PROJECT_NAME_PARAMETER = "org.org.eclipse.cheatsheet.commands.createProjectFromRemoteArchiveCommand.targetProjectName";
	private static final String MODE_PARAMETER = "org.org.eclipse.cheatsheet.commands.createProjectFromRemoteArchiveCommand.mode";
	private static final String CUSTOM_SUFFIX_PARAMETER = "org.org.eclipse.cheatsheet.commands.createProjectFromRemoteArchiveCommand.customSuffix";

	/**
	 * The constructor.
	 */
	public CreateProjectFromRemoteArchiveHandler() {
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
		String targetProjectName = event.getParameter(TARGET_PROJECT_NAME_PARAMETER);
		if (targetProjectName == null) {
			targetProjectName = determineProjectNameFrom(fileUrl, pathInArchive);
		}
		String mode = event.getParameter(MODE_PARAMETER);
		if (mode == null)
			mode = ModeParameterValues.REPLACE;
		String customSuffix = event.getParameter(CUSTOM_SUFFIX_PARAMETER);
		
		if (mode == null) {
			mode = ModeParameterValues.SUFFIX;
		}
		boolean prompt = mode.equals(ModeParameterValues.PROMPT);
		if (prompt) {
			ProjectCreationPromptDialog userPromptDialog = new ProjectCreationPromptDialog(HandlerUtil.getActiveShell(event));
			if (userPromptDialog.open() == Window.OK) {
				mode = userPromptDialog.getMode();
			} else {
				mode = ModeParameterValues.SUFFIX;
			}
		}
		Job job = new CreateProjectInWorkspaceJob( fileUrl, pathInArchive, targetProjectName, mode, customSuffix);
		job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG CheatSheet Helpers Notification","Project creation ended:\n"));
		job.schedule();
		return null;
	}

	private String determineProjectNameFrom(URL fileUrl, String pathInArchive) {
		StringTokenizer fileTkz = new StringTokenizer(fileUrl.toExternalForm(), "\\/", false);
		StringTokenizer pathInArchiveTkz = new StringTokenizer(pathInArchive, "\\/", false);
		String result = null;
		while (fileTkz.hasMoreTokens()) {
			result = fileTkz.nextToken();
		}
		if (result.contains(".")) {
			result = result.substring(0, result.lastIndexOf("."));
		}
		while (pathInArchiveTkz.hasMoreTokens()) {
			result = pathInArchiveTkz.nextToken();
		}
		if (result != null && !result.matches(".*")) {
			result = null;
		}
		return result;
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
