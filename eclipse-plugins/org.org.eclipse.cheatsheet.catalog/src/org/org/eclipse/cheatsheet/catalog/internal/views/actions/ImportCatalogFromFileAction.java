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
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalogReference;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalogReferenceType;
import org.org.eclipse.cheatsheet.catalog.internal.model.ModelConstants;
import org.org.eclipse.cheatsheet.catalog.internal.model.ModelPersistence;
import org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.enabling.AlwaysEnabledAction;
import org.org.eclipse.cheatsheet.catalog.internal.xml.IXmlCatalogBinder;
import org.org.eclipse.cheatsheet.catalog.internal.xml.StaxXmlCatalogBinder;
import org.org.eclipse.core.utils.platform.dialogs.message.ErrorDialog;
import org.org.eclipse.core.utils.platform.dialogs.message.InfoDialog;
import org.org.model.RootModelItem;

public class ImportCatalogFromFileAction extends AlwaysEnabledAction {
	/**
	 * 
	 */
	private final CheatSheetCatalogView cheatSheetCatalogView;

	public ImportCatalogFromFileAction(CheatSheetCatalogView cheatSheetCatalogView, CheatSheetCatalogView actionHost) {
		super(actionHost);
		this.cheatSheetCatalogView = cheatSheetCatalogView;
	}

	public void run() {
		RootModelItem<CheatSheetCatalog> root = RootModelItem.<CheatSheetCatalog> getInstance(ModelConstants.ROOT_MODEL_ITEM_ID);
		FileDialog inputDialog = new FileDialog(this.cheatSheetCatalogView.getViewSite().getShell(), SWT.OPEN);
		inputDialog.setFilterExtensions(new String[] { "*.xml" });
		if (inputDialog.open() != null) {
			IXmlCatalogBinder catalogBinder = new StaxXmlCatalogBinder();
			FileInputStream fileInputStream = null;
			try {
				String fileUri = inputDialog.getFilterPath() + "/" + inputDialog.getFileName();
				fileInputStream = new FileInputStream(new File(fileUri));
				CheatSheetCatalog catalog = catalogBinder.parseXmlCatalog(fileInputStream);
				if (catalog != null) {
					if (root.hasChild(catalog.getUID())) {
						InfoDialog infoDialog = new InfoDialog("Catalog already exists", "Catalog already exists locally. Another one will be created with a numeric suffix.");
						infoDialog.open();
						int i = 1;
						String name = catalog.getName() + "(" + i + ")";
						while (root.hasChild(catalog.getName() + "(" + (i++) + ")")) {
							name = catalog.getName() + "(" + (i) + ")";
						}
						catalog = catalog.duplicate(name, catalog.getProvider());
					}
					catalog.setReference(new CheatSheetCatalogReference(CheatSheetCatalogReferenceType.FILE_SYSTEM, fileUri));
					catalog.setReadOnly(true);
					root.addChild(catalog);
					this.cheatSheetCatalogView.refreshViewerKeepFolding();
					ModelPersistence.saveCheatSheetCatalogs();
					this.cheatSheetCatalogView.touchTags();
				} else {
					throw new Exception("File is probably not a catalog file");
				}
			} catch (FileNotFoundException e) {
				ErrorDialog errorDialog = new ErrorDialog("Impossible to open file", "Impossible to import catalog from file.", e);
				errorDialog.open();
			} catch (Exception e) {
				ErrorDialog errorDialog = new ErrorDialog("Error", "An error occured while importing catalog.", e);
				errorDialog.open();
			} finally {
				try {
					fileInputStream.close();
				} catch (Exception ex) {
					// TRAP THIS...
				}
			}
		}
	}
}