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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.org.eclipse.cheatsheet.catalog.CheatSheetCatalogPlugin;
import org.org.eclipse.cheatsheet.catalog.internal.images.PluginImages;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCategory;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetReference;

class ViewLabelProvider implements ILabelProvider {

	public String getText(Object obj) {
		String text = null;
		if (obj instanceof CheatSheetCatalog) {
			String lockedStatus = ((CheatSheetCatalog) obj).getReadOnly() ? "" : "[UNLOCKED]";
			text = ((CheatSheetCatalog) obj).getName() + lockedStatus;
		} else if (obj instanceof CheatSheetCategory) {
			text = ((CheatSheetCategory) obj).getName();
		} else if (obj instanceof CheatSheetReference) {
			text = ((CheatSheetReference) obj).getName();
		} else {
			text = obj.toString();
		}
		return text;
	}

	public Image getImage(Object obj) {
		String imageKey = PluginImages.CHEATSHEET_CATEGORY;
		if (obj instanceof CheatSheetCatalog)
			if (((CheatSheetCatalog) obj).getReadOnly()) {
				imageKey = PluginImages.READONLY_CHEATSHEET_CATALOG;
			} else {
				imageKey = PluginImages.CHEATSHEET_CATALOG;
			}
		if (obj instanceof CheatSheetReference)
			imageKey = PluginImages.CHEATSHEET_REFERENCE;
		return CheatSheetCatalogPlugin.getDefault().getImages().getImage(imageKey);
	}

	public void addListener(ILabelProviderListener listener) {
		// ignore
	}

	public void dispose() {
		// ignore
	}

	public boolean isLabelProperty(Object element, String property) {
		// ignore
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// ignore
	}
}