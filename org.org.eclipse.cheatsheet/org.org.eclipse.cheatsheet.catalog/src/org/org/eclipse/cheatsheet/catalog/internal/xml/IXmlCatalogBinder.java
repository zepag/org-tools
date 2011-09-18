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
package org.org.eclipse.cheatsheet.catalog.internal.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;

public interface IXmlCatalogBinder {
	public static final String XML_VERSION_1_0 = "1.0";
	public static final String UTF_8 = "UTF-8";
	public static final String TAGS = "tags";
	public static final String CATEGORY = "category";
	public static final String URL = "url";
	public static final String TYPE = "type";
	public static final String ID = "id";
	public static final String CHEATSHEET_REFERENCE = "cheatsheet-reference";
	public static final String PROVIDER = "provider";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String CHEATSHEET_CATALOG = "cheatsheet-catalog";
	public static final String CHEATSHEET_CATALOGS = "cheatsheet-catalogs";

	public abstract CheatSheetCatalog parseXmlCatalog(InputStream inputStream);

	public abstract CheatSheetCatalog parseXmlCatalog(String inputStream);

	public abstract List<CheatSheetCatalog> parseXmlCatalogs(InputStream inputStream);

	public abstract List<CheatSheetCatalog> parseXmlCatalogs(String inputStream);

	public abstract String toXmlCatalog(CheatSheetCatalog cheatSheetCatalog);

	public abstract void toXmlCatalog(CheatSheetCatalog cheatSheetCatalog, OutputStream outputStream);
	
	public abstract String toXmlCatalogs(List<CheatSheetCatalog> cheatSheetCatalog);

	public abstract void toXmlCatalogs(List<CheatSheetCatalog> cheatSheetCatalog, OutputStream outputStream);

}
