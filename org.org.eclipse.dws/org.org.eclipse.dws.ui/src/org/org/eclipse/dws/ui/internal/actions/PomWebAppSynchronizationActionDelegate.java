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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.IHandlerService;
import org.org.eclipse.core.utils.jdt.tools.JavaProjectHelper;
import org.org.eclipse.dws.ui.internal.handlers.PomWebAppSynchronizationHandler;


/**
 * This Action launches the Pom /Web application Synchronization Wizard.
 * 
 * @author pagregoire
 */
public class PomWebAppSynchronizationActionDelegate extends AbstractParsePomActionDelegate {

	/**
	 * @see org.org.eclipse.dws.ui.internal.actions.AbstractParsePomActionDelegate#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		boolean result = super.isEnabled();
		final IResource resource = ((IResource) getSelection().getFirstElement());
		if (resource != null) {
			IProject project = resource.getProject();
			try {
				if (!JavaProjectHelper.isJavaProject(project)) {
					result = false;
				}
			} catch (CoreException e) {
				result = false;
			}
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * @see org.org.eclipse.dws.ui.internal.actions.AbstractParsePomActionDelegate#doRun()
	 */
	@Override
	public void doRun() throws Exception {
		IWorkbenchSite site = getTargetPart().getSite();
		IHandlerService handlerService = (IHandlerService) site.getService(IHandlerService.class);
		PomWebAppSynchronizationHandler pomWebAppSynchronizationHandler = new PomWebAppSynchronizationHandler();
		pomWebAppSynchronizationHandler.execute(new ExecutionEvent(null, Collections.EMPTY_MAP, null, handlerService.getCurrentState()));
	}
}