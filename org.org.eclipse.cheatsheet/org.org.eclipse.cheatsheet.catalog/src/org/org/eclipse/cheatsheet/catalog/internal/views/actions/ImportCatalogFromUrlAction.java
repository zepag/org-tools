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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.eclipse.jface.window.Window;
import org.org.eclipse.cheatsheet.catalog.internal.dialogs.UrlInputDialog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalogReference;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalogReferenceType;
import org.org.eclipse.cheatsheet.catalog.internal.model.ModelConstants;
import org.org.eclipse.cheatsheet.catalog.internal.model.ModelPersistence;
import org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.enabling.AlwaysEnabledAction;
import org.org.eclipse.cheatsheet.catalog.internal.xml.IXmlCatalogBinder;
import org.org.eclipse.cheatsheet.catalog.internal.xml.StaxXmlCatalogBinder;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldsValidator;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.message.ErrorDialog;
import org.org.eclipse.core.utils.platform.dialogs.message.InfoDialog;
import org.org.eclipse.core.utils.platform.tools.IOToolBox;
import org.org.model.RootModelItem;

public class ImportCatalogFromUrlAction extends AlwaysEnabledAction {
	/**
	 * 
	 */
	private final CheatSheetCatalogView cheatSheetCatalogView;

	public ImportCatalogFromUrlAction(CheatSheetCatalogView cheatSheetCatalogView, CheatSheetCatalogView actionHost) {
		super(actionHost);
		this.cheatSheetCatalogView = cheatSheetCatalogView;
	}

	public void run() {
		RootModelItem<CheatSheetCatalog> root = RootModelItem.<CheatSheetCatalog> getInstance(ModelConstants.ROOT_MODEL_ITEM_ID);
		UrlInputDialog inputDialog = new UrlInputDialog(this.cheatSheetCatalogView.getViewSite().getShell());
		inputDialog.setValidator(new IFieldsValidator() {

			
			@SuppressWarnings("rawtypes")
			public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
				StringBuilderValidationResult validationResult = new StringBuilderValidationResult();
				String url = (String) fieldValueHolders.get(UrlInputDialog.URL_FIELD).getValue();
				if (url.trim().length() == 0) {
					validationResult.append("Url should not be empty\n");
				} else {
					try {
						URL urlObj = new URL(url);
						Proxy proxy = IOToolBox.determineProxy(urlObj);
						urlObj.openConnection(proxy).getInputStream();
					} catch (MalformedURLException e) {
						validationResult.append("Malformed Url:\n\t" + e.getMessage());
					} catch (IOException e) {
						validationResult.append("Impossible to reach Url:\n\t" + e.getMessage());
					} catch (URISyntaxException e) {
						validationResult.append("URI Syntax issue:\n\t" + e.getMessage());
					}
				}
				return validationResult;
			}

		});
		if (inputDialog.open() == Window.OK) {
			IXmlCatalogBinder catalogBinder = new StaxXmlCatalogBinder();
			InputStream inputStream = null;
			try {
				String uri = inputDialog.getUrl();
				URL url = new URL(uri);
				Proxy proxy = IOToolBox.determineProxy(url);
				inputStream = url.openConnection(proxy).getInputStream();
				CheatSheetCatalog catalog = catalogBinder.parseXmlCatalog(inputStream);
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
					catalog.setReference(new CheatSheetCatalogReference(CheatSheetCatalogReferenceType.HTTP, uri));
					catalog.setReadOnly(true);
					root.addChild(catalog);
					this.cheatSheetCatalogView.refreshViewerKeepFolding();
					ModelPersistence.saveCheatSheetCatalogs();
					this.cheatSheetCatalogView.touchTags();
				} else {
					throw new Exception("File is probably not a catalog file");
				}
				this.cheatSheetCatalogView.refreshViewerKeepFolding();
			} catch (Exception e) {
				ErrorDialog errorDialog = new ErrorDialog("Error", "An error occured while importing catalog.", e);
				errorDialog.open();

			} finally {
				try {
					inputStream.close();
				} catch (Exception ex) {
					// TRAP THIS...
				}
			}
		}
	}
}