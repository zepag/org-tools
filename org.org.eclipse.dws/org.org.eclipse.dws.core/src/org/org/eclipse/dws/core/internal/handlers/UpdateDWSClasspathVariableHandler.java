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

import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;
import org.org.eclipse.dws.core.internal.jobs.UpdateDWSClasspathVariableJob;
import org.org.eclipse.dws.core.internal.jobs.completion.CompletionPopupJobChangeListener;

/**
 * The Class UpdateDWSClasspathVariableHandler.
 */
public class UpdateDWSClasspathVariableHandler extends AbstractExtendedHandler<Object> {

	/** The logger. */
	private static Logger logger = Logger.getLogger(UpdateDWSClasspathVariableHandler.class);

	public Object doExecute(ExecutionEvent event, IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException {
		try {
			launchJob();
		} catch (Throwable e) {
			logger.error("Unattended exception", e);
			ErrorDialog errorDialog = new ErrorDialog("Update classpath variable", MessageFormat.format("Unattended Exception occured : [{0} :{1}]", new Object[] { e.getClass().getName(), e.getMessage(), e }));
			errorDialog.open();
		}
		return null;
	}

	/**
	 * Launch job.
	 */
	private void launchJob() {
		Job job = new UpdateDWSClasspathVariableJob();
		job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG DWS Notification", "Variables change ended:\n"));
		job.schedule();
	}

}
