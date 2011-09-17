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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.enabling.EnabledForCheatSheetCatalogAction;
import org.org.eclipse.cheatsheet.catalog.internal.xml.IXmlCatalogBinder;
import org.org.eclipse.cheatsheet.catalog.internal.xml.StaxXmlCatalogBinder;
import org.org.eclipse.core.utils.platform.dialogs.message.ErrorDialog;

public class ExportCatalogAction extends EnabledForCheatSheetCatalogAction {
	/**
	 * 
	 */
	private final CheatSheetCatalogView cheatSheetCatalogView;

	public ExportCatalogAction(CheatSheetCatalogView cheatSheetCatalogView, CheatSheetCatalogView actionHost) {
		super(actionHost);
		this.cheatSheetCatalogView = cheatSheetCatalogView;
	}

	public void run() {
		IStructuredSelection structuredSelection = (IStructuredSelection) this.cheatSheetCatalogView.getViewer().getSelection();
		CheatSheetCatalog cheatSheetCatalog = (CheatSheetCatalog) structuredSelection.getFirstElement();
		String nameProposal = null;
		try {
			IPath referencePath = new Path(cheatSheetCatalog.getReference().getUri());
			nameProposal = referencePath.lastSegment();
		} catch (Throwable e) {
			nameProposal = "cheatsheet-catalog-export.xml";
		}
		FileDialog fileDialog = new FileDialog(this.cheatSheetCatalogView.getViewSite().getShell(), SWT.SAVE);
		fileDialog.setFileName(nameProposal);
		String path = fileDialog.open();
		if (path != null) {
			File file = new File(path);
			IXmlCatalogBinder catalogBinder = new StaxXmlCatalogBinder();
			OutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(file);
				catalogBinder.toXmlCatalog(cheatSheetCatalog, outputStream);
			} catch (FileNotFoundException e) {
				ErrorDialog errorDialog = new ErrorDialog("Error", "An error occured while exporting catalog.");
				errorDialog.open();
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}