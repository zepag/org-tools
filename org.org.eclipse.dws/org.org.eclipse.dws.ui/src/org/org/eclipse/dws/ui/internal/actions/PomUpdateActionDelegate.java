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
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.IHandlerService;
import org.org.eclipse.dws.ui.internal.handlers.PomUpdateHandler;


/**
 * This Action launches the Pom update application Synchronization Wizard.
 * 
 * @author pagregoire
 */
public class PomUpdateActionDelegate extends AbstractParsePomActionDelegate {

	/* (non-Javadoc)
	 * @see org.org.eclipse.dws.core.actions.AbstractParsePomActionDelegate#doRun()
	 */
	/**
	 * @see org.org.eclipse.dws.ui.internal.actions.AbstractParsePomActionDelegate#doRun()
	 */
	@Override
	public void doRun() throws Exception {
		IWorkbenchSite site = getTargetPart().getSite();
		IHandlerService handlerService = (IHandlerService) site.getService(IHandlerService.class);
		PomUpdateHandler pomUpdateHandler = new PomUpdateHandler();
		pomUpdateHandler.execute(new ExecutionEvent(null, Collections.EMPTY_MAP, null, handlerService.getCurrentState()));
	}
}