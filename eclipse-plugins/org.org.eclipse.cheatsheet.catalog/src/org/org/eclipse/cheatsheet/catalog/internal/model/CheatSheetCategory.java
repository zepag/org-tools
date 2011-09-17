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

import java.util.UUID;

import org.org.model.AbstractModelItem;

public class CheatSheetCategory extends AbstractModelItem<CheatSheetCatalog, CheatSheetReference> {
	private String name;
	private String uid;

	public CheatSheetCategory(String name) {
		super();
		this.name = name;
		this.uid = UUID.randomUUID().toString();
	}

	public CheatSheetCategory(String uid, String name) {
		super();
		this.name = name;
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getUID() {
		return uid;
	}

	@Override
	public StringBuilder toStringBuilderDescription() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(CheatSheetCategory.class.getSimpleName() + "#");
		stringBuilder.append("uid:" + uid);
		stringBuilder.append("name:" + name);
		return stringBuilder;
	}
}
