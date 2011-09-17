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

import org.eclipse.jface.wizard.WizardDialog;
import org.org.eclipse.core.utils.platform.actions.AbstractSimpleAction;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.core.utils.platform.actions.IActionResolver;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;
import org.org.eclipse.dws.ui.internal.wizards.NewRepositoryWizard;
import org.org.repository.crawler.items.IFileSystemCrawledRepositorySetup;


/**
 * The Class AddFileSystemRepositoryAction.
 * 
 * @author pagregoire
 */
public class AddFileSystemRepositoryAction extends AbstractSimpleAction {

	/** The resolver. */
	private IActionResolver resolver;

	/**
	 * Instantiates a new adds the file system repository action.
	 * 
	 * @param actionHost the action host
	 */
	public AddFileSystemRepositoryAction(IActionHost actionHost) {
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
		NewRepositoryWizard newRepositoryWizard = new NewRepositoryWizard(IFileSystemCrawledRepositorySetup.class);
		WizardDialog dialog = new WizardDialog(PluginToolBox.getWorkbench().getActiveWorkbenchWindow().getShell(), newRepositoryWizard);
		dialog.open();
	}

}
