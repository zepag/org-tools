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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.cheatsheet.commands.internal.jobs.InsertNodeInXmlFileFromFileSystemJob;
import org.org.eclipse.core.ui.jobs.completion.CompletionPopupJobChangeListener;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class InsertNodeInXmlFileFromFileSystemHandler extends AbstractExtendedHandler<Object> {
	private static final String COMMAND_ID = "org.org.eclipse.cheatsheet.commands.insertNodeInXmlFileFromFileSystemCommand";
	private static final String TARGET_XPATH_PARAMETER = COMMAND_ID + ".targetXPath";
	private static final String NODE_DEFINITION_URL_PARAMETER = COMMAND_ID + ".nodeDefinitionUrl";
	private static final String TARGET_FILE_PARAMETER = COMMAND_ID + ".targetFile";

	/**
	 * The constructor.
	 */
	public InsertNodeInXmlFileFromFileSystemHandler() {
	}

	public Object doExecute(ExecutionEvent event, IExecutionSavedContext savedContext) throws ExtendedHandlerExecutionException {
		String targetXPath = event.getParameter(TARGET_XPATH_PARAMETER);
		if (targetXPath == null) {
			throw new ExtendedHandlerExecutionException("Invalid CheatSheet command, target XPath should be defined.");
		}
		URL nodeDefinitionUrl = null;
		try {
			nodeDefinitionUrl = new URL(event.getParameter(NODE_DEFINITION_URL_PARAMETER));
		} catch (MalformedURLException e) {
			throw new ExtendedHandlerExecutionException("Impossible to parse node definition's URL", e);
		}
		String targetFile = event.getParameter(TARGET_FILE_PARAMETER);
		if (targetFile == null) {
			throw new ExtendedHandlerExecutionException("Invalid CheatSheet command, target File should be defined.");
		}

		launchJob(targetXPath, nodeDefinitionUrl, targetFile);

		return null;
	}

	private void launchJob(String targetXPath, URL nodeDefinitionUrl, String targetFile) {
		Job job = new InsertNodeInXmlFileFromFileSystemJob(targetXPath, nodeDefinitionUrl, targetFile);
		job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG CheatSheet Helpers Notification", "Inserted node in xml file in file system:\n"));
		job.schedule();
	}
}
