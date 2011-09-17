package org.org.eclipse.dws.ui;

import org.org.eclipse.dws.core.IModelUpdateListener;
import org.org.eclipse.dws.ui.internal.views.MavenRepositoriesView;
import org.org.model.IModelItem;

public class ModelUpdateListener implements IModelUpdateListener {

	public ModelUpdateListener() {
	}

	@SuppressWarnings("unchecked")
	public void notifyModelUpdate(IModelItem modelItem) {
		MavenRepositoriesView.refreshViewer();
		if (modelItem != null) {
//			MavenRepositoriesView.showViewAndFocusOnElement(modelItem);
		}
	}

}
