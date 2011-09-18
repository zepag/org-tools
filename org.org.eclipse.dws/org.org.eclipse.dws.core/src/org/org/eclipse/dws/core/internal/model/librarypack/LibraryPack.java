package org.org.eclipse.dws.core.internal.model.librarypack;

import org.org.repository.crawler.maven2.model.GroupsHolder;

public class LibraryPack extends GroupsHolder {
	private final String label;
	private final String description;

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}

	public LibraryPack(String label, String description) {
		this.label = label;
		this.description = description;
	}

	@Override
	public String getUID() {
		return label;
	}

	@Override
	public StringBuilder toStringBuilderDescription() {
		return new StringBuilder("LibraryPack [label:" + label + ",description:" + description + "]");
	}

	public int compareTo(LibraryPack o) {
		return label.compareTo(o.label);
	}
}