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
package org.org.eclipse.cheatsheet.catalog.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class OpenCatalogsViewHandler extends AbstractExtendedHandler<Object> {

	/**
	 * The constructor.
	 */
	public OpenCatalogsViewHandler() {
	}
	public Object doExecute(ExecutionEvent event, IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException {
		return CheatSheetCatalogView.showView();
	}

}
