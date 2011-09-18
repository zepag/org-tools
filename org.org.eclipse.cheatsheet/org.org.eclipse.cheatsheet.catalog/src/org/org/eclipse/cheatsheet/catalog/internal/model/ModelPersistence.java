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
package org.org.eclipse.cheatsheet.catalog.internal.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.org.eclipse.cheatsheet.catalog.internal.xml.IXmlCatalogBinder;
import org.org.eclipse.cheatsheet.catalog.internal.xml.StaxXmlCatalogBinder;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;
import org.org.model.IModelItemListener;
import org.org.model.RootModelItem;

public class ModelPersistence {

	/** The logger. */
	private static Logger logger = Logger.getLogger(ModelPersistence.class);
	public static final Integer OUT_OF_SYNC = 0;
	public static final Integer SYNC = 1;
	private static Integer workspacePersistenceStatus;

	public static void loadCheatSheetCatalogs() {
		logger.info("Now loading from proper file storage.");
		IPath persistedCatalogs = getPersistedCatalogsPath();
		InputStream contents = null;
		try {
			contents = new FileInputStream(persistedCatalogs.toFile());
			loadCheatSheetCatalogs(contents);
			logger.info("loaded cheatsheet catalogs from file " + persistedCatalogs.toFile().toString() + ".");
		} catch (Exception e) {
			logger.error("unable to load cheatsheet catalogs from file " + persistedCatalogs.toFile().toString() + ".");
		} finally {
			try {
				contents.close();
			} catch (Throwable e) {
			}
		}

		setWorkspacePersistenceStatus(OUT_OF_SYNC);
	}

	private static void loadCheatSheetCatalogs(InputStream contents) {
		IXmlCatalogBinder xmlCatalogBinder = new StaxXmlCatalogBinder();
		List<CheatSheetCatalog> catalogModelItems = xmlCatalogBinder.parseXmlCatalogs(contents);
		RootModelItem<CheatSheetCatalog> root = RootModelItem.<CheatSheetCatalog> getInstance(ModelConstants.ROOT_MODEL_ITEM_ID);
		root.toggleListenersOn();
		for (CheatSheetCatalog catalog : catalogModelItems) {
			root.addChild(catalog);
		}
	}

	private static IPath getPersistedCatalogsPath() {
		IPath persistedRepositories = PluginToolBox.getCurrentWorkspace().getRoot().getRawLocation();
		persistedRepositories = persistedRepositories.append("/.org.org.eclipse.cheatsheet-catalogs.xml");
		return persistedRepositories;
	}

	private static void setWorkspacePersistenceStatus(Integer status) {
		workspacePersistenceStatus = status;
	}

	public static void saveCheatSheetCatalogs() {
		IPath persistedCatalogs = getPersistedCatalogsPath();
		IXmlCatalogBinder xmlRepositoriesBinder = new StaxXmlCatalogBinder();
		try {
			xmlRepositoriesBinder.toXmlCatalogs(new LinkedList<CheatSheetCatalog>(RootModelItem.<CheatSheetCatalog> getInstance(ModelConstants.ROOT_MODEL_ITEM_ID).getChildren()), new FileOutputStream(persistedCatalogs.toFile()));
			logger.info("saved cheatsheet catalogs to file " + persistedCatalogs.toFile().toString() + ".");
		} catch (FileNotFoundException e) {
			logger.error("impossible to save cheatsheet catalogs to file " + persistedCatalogs.toFile().toString() + ".", e);
		}
		setWorkspacePersistenceStatus(SYNC);
	}

	public static void checkStatus() {
		if (getWorkspacePersistencesStatus().equals(OUT_OF_SYNC)) {
			saveCheatSheetCatalogs();
		}
	}

	public static Integer getWorkspacePersistencesStatus() {
		return workspacePersistenceStatus;
	}

	public static void refreshModel(IModelItemListener modelItemListener) {
		// TODO implement a refresh for catalogs depending on their source.
	}

}
