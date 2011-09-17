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
import org.org.model.IModelItem;

@SuppressWarnings("unchecked")
public class CheatSheetReference extends AbstractModelItem<CheatSheetCategory, IModelItem> {
	private final CheatSheetReferenceType cheatSheetReferenceType;
	private final String id;
	private final String name;
	private final String url;
	private final Tags tags;
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public CheatSheetReference(CheatSheetReferenceType cheatSheetReferenceType, String id, String name, String url, Tags tags) {
		super();
		this.cheatSheetReferenceType = cheatSheetReferenceType;
		this.id = id;
		this.name = name;
		this.url = url;
		this.tags = tags;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public Tags getTags() {
		return tags;
	}

	@Override
	public String getUID() {
		return id;
	}

	@Override
	public StringBuilder toStringBuilderDescription() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("id:" + id + ";");
		stringBuilder.append("name:" + name + ";");
		stringBuilder.append("url:" + url + ";");
		stringBuilder.append("tags:" + tags.getTagsString());
		return stringBuilder;
	}

	public CheatSheetReferenceType getType() {
		return cheatSheetReferenceType;
	}
}