package org.org.eclipse.dws.core.internal.model;

import java.util.UUID;

import org.org.model.AbstractModelItem;
import org.org.model.IModelItem;

@SuppressWarnings("unchecked")
public class PomProfile extends AbstractModelItem<IModelItem, PomDependency> {
	private final String id;

	private final PomPropertiesSet properties = new PomPropertiesSet();

	public PomProfile(String id) {
		if (id == null || id.trim().equals("")) {
			id = UUID.randomUUID().toString();
		}
		this.id = id;
	}

	@Override
	public String getUID() {
		return id;
	}

	@Override
	public StringBuilder toStringBuilderDescription() {
		return new StringBuilder("id:" + id);
	}

	public PomPropertiesSet getProperties() {
		return properties;
	}
}
