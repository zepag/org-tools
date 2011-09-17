package org.org.eclipse.rcp.cheatsheet;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);
		layout.addView("org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView", IPageLayout.LEFT, 1.0f, editorArea);
	}
}
