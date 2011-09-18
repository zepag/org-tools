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
package org.org.eclipse.dws.ui.internal.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;
import org.org.eclipse.dws.ui.internal.views.MavenRepositoriesView;

/**
 * The Class OpenMavenRepositoryViewHandler.
 */
public class OpenMavenRepositoryViewHandler extends AbstractExtendedHandler<Object> {

	/** The logger. */
	private static Logger logger = Logger.getLogger(OpenMavenRepositoryViewHandler.class);

	public Object doExecute(ExecutionEvent event, IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException {
		logger.debug("Opening Maven 2 Repositories View");
		MavenRepositoriesView.showViewAndFocusOnElement(null);
		return null;
	}

	/**
	 * Inits the.
	 * 
	 * @param window
	 *            the window
	 */
	public void init(IWorkbenchWindow window) {

	}
}
