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
package org.org.eclipse.cheatsheet.catalog.internal.views.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.commands.ICommandService;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetReference;
import org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.enabling.EnabledForCheatSheetReferenceAction;
import org.org.eclipse.core.utils.platform.dialogs.message.ErrorDialog;

public class OpenReferenceAction extends EnabledForCheatSheetReferenceAction {
	/**
	 * 
	 */
	private final CheatSheetCatalogView cheatSheetCatalogView;

	public OpenReferenceAction(CheatSheetCatalogView cheatSheetCatalogView, CheatSheetCatalogView actionHost) {
		super(actionHost);
		this.cheatSheetCatalogView = cheatSheetCatalogView;
	}

	
	public void run() {
		ICommandService commandService = (ICommandService) this.cheatSheetCatalogView.getSite().getService(ICommandService.class);
		IStructuredSelection structuredSelection = (IStructuredSelection) this.cheatSheetCatalogView.getViewer().getSelection();
		CheatSheetReference cheatSheetReference = (CheatSheetReference) structuredSelection.getFirstElement();
		if (cheatSheetReference.getUrl().startsWith("platform:/")) {
			Command command = commandService.getCommand("org.eclipse.ui.cheatsheets.openCheatSheet");
			Map<String,String> map = new HashMap<String, String>();
			map.put("cheatSheetId", cheatSheetReference.getUrl().substring("platform:/".length()));
			try {
				command.executeWithChecks(new ExecutionEvent(command, map, null, null));
			} catch (Exception e) {
				ErrorDialog errorDialog = new ErrorDialog("Impossible to open Cheat Sheet", "An error occured while trying to open cheat sheet.", e);
				errorDialog.open();
			}
		} else {
			Command command = commandService.getCommand("org.eclipse.ui.cheatsheets.openCheatSheetURL");
			Map<String,String> map = new HashMap<String, String>();
			map.put("cheatSheetId", cheatSheetReference.getId());
			map.put("name", cheatSheetReference.getName());
			map.put("url", cheatSheetReference.getUrl());
			try {
				command.executeWithChecks(new ExecutionEvent(command, map, null, null));
			} catch (Exception e) {
				ErrorDialog errorDialog = new ErrorDialog("Impossible to open Cheat Sheet", "An error occured while trying to open cheat sheet.", e);
				errorDialog.open();
			}
		}

	}
}