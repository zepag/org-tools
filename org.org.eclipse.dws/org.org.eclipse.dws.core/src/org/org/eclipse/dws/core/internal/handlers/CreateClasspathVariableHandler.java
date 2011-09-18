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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;
import org.org.eclipse.dws.core.internal.jobs.CreateClasspathVariableJob;
import org.org.eclipse.dws.core.internal.jobs.completion.CompletionPopupJobChangeListener;

/**
 * The Class CreateClasspathVariableHandler.
 */
public class CreateClasspathVariableHandler extends AbstractExtendedHandler<Object> {

	public Object doExecute(ExecutionEvent event, IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException {
		Job job = new CreateClasspathVariableJob();
		job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG DWS Notification", "Creating a variable"));
		job.schedule();
		return null;
	}

}
