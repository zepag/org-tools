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
package org.org.eclipse.dws.ui.internal.views.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.core.utils.platform.actions.AbstractSimpleAction;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.core.utils.platform.actions.IActionResolver;
import org.org.eclipse.dws.core.internal.handlers.ImportRepositoryFromFileHandler;


/**
 * The Class ImportRepositoryFromFileAction.
 * 
 * @author pagregoire
 */
public class ImportRepositoryFromFileAction extends AbstractSimpleAction {

	/** The resolver. */
	private IActionResolver resolver;

	/** The action host. */
	@SuppressWarnings("unused")
	private IActionHost actionHost;

	/**
	 * Instantiates a new import repository from file action.
	 * 
	 * @param actionHost the action host
	 */
	public ImportRepositoryFromFileAction(IActionHost actionHost) {
		this.actionHost = actionHost;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
	/**
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		boolean result = false;
		if (resolver == null) {
			result = false;
		} else {
			result = resolver.isEnabled();
		}
		return result;
	}

	/**
	 * Sets the resolver.
	 * 
	 * @param resolver the new resolver
	 */
	public void setResolver(IActionResolver resolver) {
		this.resolver = resolver;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {

		ImportRepositoryFromFileHandler importRepositoryFromFileHandler = new ImportRepositoryFromFileHandler();
		try {
			importRepositoryFromFileHandler.execute(new ExecutionEvent());
		} catch (ExecutionException e) {
			ErrorDialog errorDialog = new ErrorDialog("Problem while importing", "Impossible to import from file", e);
			errorDialog.open();
		}

	}
}