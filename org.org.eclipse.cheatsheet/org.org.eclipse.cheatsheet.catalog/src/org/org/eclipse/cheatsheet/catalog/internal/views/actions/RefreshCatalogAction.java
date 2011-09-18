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
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalogReference;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalogReferenceType;
import org.org.eclipse.cheatsheet.catalog.internal.model.ModelPersistence;
import org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.enabling.EnabledForCheatSheetCatalogWithReferenceAction;
import org.org.eclipse.cheatsheet.catalog.internal.xml.IXmlCatalogBinder;
import org.org.eclipse.cheatsheet.catalog.internal.xml.StaxXmlCatalogBinder;
import org.org.eclipse.core.utils.platform.dialogs.message.ErrorDialog;
import org.org.eclipse.core.utils.platform.tools.IOToolBox;
import org.org.model.RootModelItem;

public class RefreshCatalogAction extends EnabledForCheatSheetCatalogWithReferenceAction {
	/**
	 * 
	 */
	private final CheatSheetCatalogView cheatSheetCatalogView;

	public RefreshCatalogAction(CheatSheetCatalogView cheatSheetCatalogView, CheatSheetCatalogView actionHost) {
		super(actionHost);
		this.cheatSheetCatalogView = cheatSheetCatalogView;
	}

	public void run() {
		IStructuredSelection structuredSelection = (IStructuredSelection) this.cheatSheetCatalogView.getViewer().getSelection();
		for (Object selectedElement : structuredSelection.toList()) {
			CheatSheetCatalog catalog = (CheatSheetCatalog) selectedElement;
			RootModelItem<CheatSheetCatalog> root = catalog.getParent();
			IXmlCatalogBinder catalogBinder = new StaxXmlCatalogBinder();
			CheatSheetCatalogReference reference = catalog.getReference();
			if (reference.getReferenceType() == CheatSheetCatalogReferenceType.HTTP) {
				InputStream inputStream = null;
				try {
					URL url = new URL(reference.getUri());
					Proxy proxy = IOToolBox.determineProxy(url);
					inputStream = url.openConnection(proxy).getInputStream();
					catalog = catalogBinder.parseXmlCatalog(inputStream);
					catalog.setReference(reference);
				} catch (Exception e) {
					ErrorDialog errorDialog = new ErrorDialog("Error", "An error occured while refreshing catalog.", e);
					errorDialog.open();
				} finally {
					try {
						inputStream.close();
					} catch (Throwable ex) {
						// TRAP THIS...
					}
				}
			} else if (reference.getReferenceType() == CheatSheetCatalogReferenceType.FILE_SYSTEM) {
				InputStream inputStream = null;
				try {
					inputStream = new FileInputStream(new File(reference.getUri()));
					catalog = catalogBinder.parseXmlCatalog(inputStream);
					catalog.setReference(reference);
				} catch (FileNotFoundException e) {
					ErrorDialog errorDialog = new ErrorDialog("Impossible to open file", "Impossible to refresh catalog from file.", e);
					errorDialog.open();
				} catch (Exception e) {
					ErrorDialog errorDialog = new ErrorDialog("Error", "An error occured while refreshing catalog.", e);
					errorDialog.open();
				} finally {
					try {
						inputStream.close();
					} catch (Throwable ex) {
						// TRAP THIS...
					}
				}
			} else if (reference.getReferenceType() == CheatSheetCatalogReferenceType.BUNDLE) {
				InputStream inputStream = null;
				try {
					URL url = new URL(reference.getUri());
					url = FileLocator.toFileURL(url);
					inputStream = url.openStream();
					catalog = catalogBinder.parseXmlCatalog(inputStream);
					catalog.setReference(reference);
				} catch (FileNotFoundException e) {
					ErrorDialog errorDialog = new ErrorDialog("Impossible to open file", "Impossible to refresh catalog from file.", e);
					errorDialog.open();
				} catch (Exception e) {
					ErrorDialog errorDialog = new ErrorDialog("Error", "An error occured while refreshing catalog.", e);
					errorDialog.open();
				} finally {
					try {
						inputStream.close();
					} catch (Throwable ex) {
						// TRAP THIS...
					}
				}
			}
			root.addChild(catalog);
			this.cheatSheetCatalogView.refreshViewerKeepFolding();
			ModelPersistence.saveCheatSheetCatalogs();
			this.cheatSheetCatalogView.touchTags();
		}
	}
}