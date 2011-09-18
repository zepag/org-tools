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


public class CheatSheetCatalogReference {
	private final CheatSheetCatalogReferenceType cheatSheetCatalogReferenceType;

	private final String referenceUri;

	public String getUri() {
		return referenceUri;
	}

	public CheatSheetCatalogReferenceType getReferenceType() {
		return cheatSheetCatalogReferenceType;
	}

	public CheatSheetCatalogReference(CheatSheetCatalogReferenceType cheatSheetCatalogReferenceType, String referenceUri) {
		super();
		this.cheatSheetCatalogReferenceType = cheatSheetCatalogReferenceType;
		this.referenceUri = referenceUri;
	}

	@Override
	public String toString() {
		return cheatSheetCatalogReferenceType + "@" + referenceUri;
	}
}