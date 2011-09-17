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

import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCategory;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldsValidator;

public abstract class CategoryAwareFieldsValidator implements IFieldsValidator {

	private CheatSheetCategory category;

	public CategoryAwareFieldsValidator(CheatSheetCategory category) {
		super();
		this.category = category;
	}

	public CheatSheetCategory getCategory() {
		return category;
	}

}