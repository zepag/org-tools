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
package org.org.eclipse.dws.ui.internal.actions;

import java.util.Collections;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.IHandlerService;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.core.utils.platform.actions.AbstractWorkbenchWindowAction;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;
import org.org.eclipse.dws.ui.DWSUIPlugin;
import org.org.eclipse.dws.ui.internal.handlers.FindRepositoriesHandler;

/**
 * The Class FindRepositoriesAction.
 */
public class FindRepositoriesAction extends AbstractWorkbenchWindowAction {

	/** The Constant ACTION_LABEL_ID. */
	public static final String ACTION_LABEL_ID = ActionsMessages.FindRepositoriesAction_id;

	/** The window. */
	private IWorkbenchWindow window;

	/**
	 * Checks if is enabled.
	 * 
	 * @return true, if checks if is enabled
	 * 
	 * @see org.org.eclipse.dws.utils.platform.actions.AbstractObjectAction#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return true;
	}

	/**
	 * Gets the shell.
	 * 
	 * @return the shell
	 * 
	 * @see org.org.eclipse.dws.utils.platform.actions.AbstractObjectAction#getShell()
	 */
	@Override
	protected Shell getShell() {
		Shell result = null;
		if (this.getDefaultShell() != null) {
			result = this.getDefaultShell();
		} else {
			result = PluginToolBox.getActiveShell(DWSUIPlugin.getDefault());
		}
		return result;
	}

	/**
	 * Run.
	 * 
	 * @param action
	 *            the action
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		IWorkbenchSite site = window.getActivePage().getActivePart().getSite();
		IHandlerService handlerService = (IHandlerService) site.getService(IHandlerService.class);
		FindRepositoriesHandler findRepositoriesHandler = new FindRepositoriesHandler();
		try {
			findRepositoriesHandler.execute(new ExecutionEvent(null, Collections.EMPTY_MAP, null, handlerService.getCurrentState()));
		} catch (ExecutionException e) {
			ErrorDialog errorDialog = new ErrorDialog(ACTION_LABEL_ID, "Problem while trying to find repositories", e);
			errorDialog.open();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

}
