package org.org.eclipse.dws.core;

import org.org.model.IModelItem;

public interface IModelUpdateListener {

	@SuppressWarnings("rawtypes")
	void notifyModelUpdate(IModelItem modelItem);

}
