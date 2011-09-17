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

import java.text.MessageFormat;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.core.utils.platform.actions.AbstractAction;
import org.org.eclipse.core.utils.platform.actions.AbstractObjectAction;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.eclipse.dws.core.internal.configuration.ConfigurationConstants;
import org.org.eclipse.dws.ui.DWSUIPlugin;

/**
 * The Class AbstractParsePomActionDelegate.
 */
public abstract class AbstractParsePomActionDelegate extends AbstractObjectAction {

	/** The Constant ACTION_LABEL_ID. */
	public static final String ACTION_LABEL_ID = "Parse pom action"; //$NON-NLS-1$

	/** Logger for this class. */
	private static Logger logger = Logger.getLogger(AbstractParsePomActionDelegate.class);

	/**
	 * Checks if is enabled.
	 * 
	 * @return true, if checks if is enabled
	 * 
	 * @see org.org.eclipse.dws.utils.platform.actions.AbstractObjectAction#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		boolean result = false;
		if (getSelection().size() == 1) {
			if (getSelection().getFirstElement() instanceof IResource) {
				IResource resource = (IResource) getSelection().getFirstElement();
				String pomFileNames = AggregatedProperties.getPomFileNames(resource.getProject());
				StringTokenizer tkz = new StringTokenizer(pomFileNames, ConfigurationConstants.POM_FILES_SEPARATOR, false);
				while (tkz.hasMoreTokens()) {
					if (resource.getName().equalsIgnoreCase(tkz.nextToken())) {
						result = true;
					}
				}
			}
		}
		return result;
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
		run(new Runnable() {
			public void run() {
				try {
					doRun();
				} catch (Throwable e) {
					logger.error(ActionsMessages.AbstractParsePomActionDelegate_unattendedException, e);
					ErrorDialog errorDialog = new ErrorDialog(ACTION_LABEL_ID, MessageFormat.format(ActionsMessages.AbstractParsePomActionDelegate_unattendedExceptionWithDescription, new Object[] { e.getClass().getName(), e.getMessage(), e }));
					errorDialog.open();
				}
			}
		}, AbstractAction.ASYNC_EXEC);
	}

	/**
	 * Do run.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public abstract void doRun() throws Exception;

}
