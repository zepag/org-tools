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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.org.eclipse.cheatsheet.commands.internal.jobs.OpenInEditorJob;
import org.org.eclipse.core.ui.jobs.completion.CompletionPopupJobChangeListener;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class OpenInEditorHandler extends AbstractExtendedHandler<Object> {
	private static final String FILE_PATH_PARAMETER = "org.org.eclipse.cheatsheet.commands.openInEditorCommand.filePath";
	private static final String LINE_NUMBER_PARAMETER = "org.org.eclipse.cheatsheet.commands.openInEditorCommand.lineNumber";

	/**
	 * The constructor.
	 */
	public OpenInEditorHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information from the application context.
	 */
	public Object doExecute(ExecutionEvent event, IExecutionSavedContext savedContext) throws ExtendedHandlerExecutionException {
		IWorkbenchPage workbenchPage = HandlerUtil.getActivePart(event).getSite().getPage();
		String filePath = event.getParameter(FILE_PATH_PARAMETER);
		Integer lineNumber = null;
		try {
			lineNumber = new Integer(event.getParameter(LINE_NUMBER_PARAMETER));
		} catch (Exception e) {
			// do something very clever here...
		}
		if (lineNumber == null)
			lineNumber = 1;

		launchJob(workbenchPage, lineNumber, filePath);
		return null;
	}

	protected void launchJob(IWorkbenchPage page, Integer lineNumber, String filePath) {
		Job job = new OpenInEditorJob(page, lineNumber, filePath);
		job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG CheatSheet Helpers Notification", "Opening file in editor:\n"));
		job.schedule();
	}
}
