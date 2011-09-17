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

import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldsValidator;
import org.org.model.RootModelItem;

public  abstract class CatalogsAwareFieldsValidator implements IFieldsValidator {
	private RootModelItem<CheatSheetCatalog> root;

	public CatalogsAwareFieldsValidator(RootModelItem<CheatSheetCatalog> root) {
		super();
		this.root = root;
	}

	public RootModelItem<CheatSheetCatalog> getRoot() {
		return root;
	}

}