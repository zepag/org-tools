package org.org.eclipse.dws.core.internal.model;

import org.org.model.AbstractModelItem;
import org.org.model.IModelItem;

@SuppressWarnings("rawtypes")
public class Maven2Settings extends AbstractModelItem<IModelItem, IModelItem> {

	private final String filePath;

	private String localRepository;

	private final PomProfilesSet pomProfilesSet = new PomProfilesSet();

	@Override
	public String getUID() {
		return filePath;
	}

	@Override
	public StringBuilder toStringBuilderDescription() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Settings:" + filePath);
		return buffer;
	}

	public int compareTo(IModelItem o) {
		return o.getUID().compareTo(this.getUID());
	}

	public PomProfilesSet getProfiles() {
		return pomProfilesSet;
	}

	public Maven2Settings(String filePath) {
		super();
		this.filePath = filePath;
	}

	public String getLocalRepository() {
		return localRepository;
	}

	public void setLocalRepository(String localRepository) {
		this.localRepository = localRepository;
	}

}
