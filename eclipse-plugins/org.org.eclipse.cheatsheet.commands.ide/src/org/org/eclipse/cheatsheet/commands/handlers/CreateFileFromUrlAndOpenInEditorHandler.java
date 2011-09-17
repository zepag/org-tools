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

import java.net.URL;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.org.eclipse.cheatsheet.commands.internal.jobs.CreateFileInWorkspaceAndOpenInEditorJob;
import org.org.eclipse.core.ui.jobs.completion.CompletionPopupJobChangeListener;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class CreateFileFromUrlAndOpenInEditorHandler extends CreateFileFromUrlHandler {
	private static final String LINE_NUMBER_PARAMETER = "org.org.eclipse.cheatsheet.commands.createFileFromUrlCommand.lineNumber";

	private IWorkbenchPage workbenchPage;

	private Integer lineNumber;

	/**
	 * The constructor.
	 */
	public CreateFileFromUrlAndOpenInEditorHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information from the application context.
	 */
	public Object doExecute(ExecutionEvent event, IExecutionSavedContext savedContext) throws ExtendedHandlerExecutionException {
		this.workbenchPage = HandlerUtil.getActivePart(event).getSite().getPage();
		try {
			lineNumber = new Integer(event.getParameter(LINE_NUMBER_PARAMETER));
		} catch (Exception e) {
			// do something very clever here...
		}
		if (lineNumber == null)
			lineNumber = 1;
		super.doExecute(event, savedContext);
		return null;
	}

	protected void launchJob(URL fileUrl, String mode, String targetFolder, String targetFileName, String customSuffix, IWorkspace workspace, String chosenProject) {
		Job job = new CreateFileInWorkspaceAndOpenInEditorJob(workbenchPage, lineNumber, workspace.getRoot().getProject(chosenProject), fileUrl, targetFolder, targetFileName, mode, customSuffix);
		job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG CheatSheet Helpers Notification", "Creation of file in workspace:\n"));
		job.schedule();
	}
}
