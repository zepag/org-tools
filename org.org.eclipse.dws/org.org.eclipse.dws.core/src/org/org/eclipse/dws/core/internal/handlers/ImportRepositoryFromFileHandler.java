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
package org.org.eclipse.dws.core.internal.handlers;

import java.io.File;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;
import org.org.eclipse.dws.core.internal.jobs.ImportRepositoryFromFileJob;
import org.org.eclipse.dws.core.internal.jobs.completion.CompletionPopupJobChangeListener;


/**
 * The Class ImportRepositoryFromFileHandler.
 */
public class ImportRepositoryFromFileHandler extends AbstractExtendedHandler<Object> {
	
	/** The Constant COMMAND_ID. */
	private static final String COMMAND_ID = "org.org.eclipse.dws.core.importRepositoryFromFileCommand";
	
	/** The Constant FILE. */
	private static final String FILE = COMMAND_ID + ".file";

	public Object doExecute(ExecutionEvent event, IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException {
		
		String fileName = event.getParameter(FILE);
		if (fileName == null || !(new File(fileName).exists())) {
			FileDialog fileDialog = new FileDialog(Display.getCurrent().getActiveShell());
			fileDialog.setFileName("maven2-repo-export.xml");
			fileDialog.setFilterExtensions(new String[] { "*.xml" });
			fileName = fileDialog.open();
		}
		if (fileName != null) {
			Job job = new ImportRepositoryFromFileJob(fileName);
			job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG DWS Notification", "Import repositories definition from filesystem:\n"));
			job.schedule();
		}
		return null;
	}
}
