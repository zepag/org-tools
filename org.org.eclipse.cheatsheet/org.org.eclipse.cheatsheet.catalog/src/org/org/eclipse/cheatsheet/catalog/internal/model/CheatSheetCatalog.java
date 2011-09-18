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

import org.org.model.AbstractModelItem;
import org.org.model.RootModelItem;

public class CheatSheetCatalog extends AbstractModelItem<RootModelItem<CheatSheetCatalog>, CheatSheetCategory> {
	private String name;
	private String provider;
	private String description;
	private Boolean readOnly = true;
	private CheatSheetCatalogReference reference;

	public CheatSheetCatalog(String name, String provider) {
		super();
		this.name = name;
		this.provider = provider;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getUID() {
		return reference != null ? reference.toString() : name;
	}

	@Override
	public StringBuilder toStringBuilderDescription() {
		StringBuilder stringBuilder = new StringBuilder();
		if (reference != null) {
			stringBuilder.append("reference: " + reference + ";");
		}
		stringBuilder.append("name: " + name + ";");
		stringBuilder.append("provider: " + provider + ";");
		return stringBuilder;
	}

	public String getProvider() {
		return provider;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * This implementation of the duplicate method will result in a writable CheatSheetCatalog with a different id.
	 */
	public CheatSheetCatalog duplicate(Object... changedData) {
		if (changedData == null || changedData.length != 2 || !(changedData[0] instanceof String)) {
			throw new IllegalArgumentException("duplicate method takes a java.lang.String (new catalog's name) as a parameter.");
		}
		String name = changedData[0] == null ? this.name : (String) changedData[0];
		String provider = changedData[1] == null ? this.provider : (String) changedData[1];
		CheatSheetCatalog duplicatedCatalog = new CheatSheetCatalog(name, provider);
		if (this.getReference() != null) {
			duplicatedCatalog.setReference(new CheatSheetCatalogReference(CheatSheetCatalogReferenceType.LOCAL, "duplicated-" + this.getReference().getUri()));
		}
		duplicatedCatalog.setReadOnly(false);
		for (CheatSheetCategory childCategory : this.getChildren()) {
			CheatSheetCategory duplicatedCategory = new CheatSheetCategory(childCategory.getName());
			for (CheatSheetReference childReference : childCategory.getChildren()) {
				CheatSheetReference duplicatedReference = new CheatSheetReference(childReference.getType(), childReference.getId(), childReference.getName(), childReference.getUrl(), childReference.getTags());
				duplicatedCategory.addChild(duplicatedReference);
			}
			duplicatedCatalog.addChild(duplicatedCategory);
		}
		return duplicatedCatalog;
	}

	public CheatSheetCatalogReference getReference() {
		return reference;
	}

	public void setReference(CheatSheetCatalogReference reference) {
		this.reference = reference;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}