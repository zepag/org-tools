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
package org.org.eclipse.cheatsheet.catalog.internal.views;

import java.util.regex.Pattern;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetReference;
import org.org.eclipse.core.utils.platform.tools.PatternUtils;
import org.org.model.IModelItem;
import org.org.model.IModelItemVisitor;

/**
 * @author pagregoire
 */
public class CheatSheetCatalogViewTagBasedFilter extends ViewerFilter {

	public class ValidationVisitor implements IModelItemVisitor {
		boolean result = false;

		@SuppressWarnings("unchecked")
		public boolean visit(IModelItem modelItem) {
			if (!result && modelItem instanceof CheatSheetReference) {
				result = doesMatch(modelItem, PatternUtils.createPattern(".*" + userFilter.replaceAll("\\*", ".*") + ".*", true, true));
			}
			return !result;
		}

		public boolean getResult() {

			return result;
		}

	}

	private String userFilter = "";

	/**
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		boolean result = true;
		if (element instanceof IModelItem) {
			if (!userFilter.trim().equals("")) {
				IModelItem modelItem = (IModelItem) element;
				ValidationVisitor visitor = new ValidationVisitor();
				modelItem.accept(visitor);
				result = visitor.getResult();
			}
		} else {
			result = false;
		}
		return result;
	}

	public String getUserFilter() {
		return userFilter;
	}

	public void setUserFilter(String userFilter) {
		this.userFilter = userFilter;
	}

	@SuppressWarnings("unchecked")
	protected boolean doesMatch(IModelItem element, Pattern pattern) {

		boolean result = false;
		if (element instanceof CheatSheetReference) {
			CheatSheetReference reference = (CheatSheetReference) element;
			// Compare bean name first
			for (String toMatch : reference.getTags().getTagsArray()) {
				if (pattern.matcher(toMatch).matches()) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
}
