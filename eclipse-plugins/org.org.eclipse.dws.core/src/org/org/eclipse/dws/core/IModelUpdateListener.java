package org.org.eclipse.dws.core;

import org.org.model.IModelItem;

@SuppressWarnings("unchecked")
public interface IModelUpdateListener {

	void notifyModelUpdate(IModelItem modelItem);

}
