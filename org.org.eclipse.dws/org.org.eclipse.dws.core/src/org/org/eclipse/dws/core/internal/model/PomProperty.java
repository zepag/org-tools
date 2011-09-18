package org.org.eclipse.dws.core.internal.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.org.model.AbstractModelItem;
import org.org.model.IModelItem;

@SuppressWarnings("rawtypes")
public class PomProperty extends AbstractModelItem<IModelItem, IModelItem> {
	private final String key;
	private final Set<String> possibleValues = new HashSet<String>();

	public PomProperty(String key) {
		this.key = key;
	}

	public PomProperty(String key, String... values) {
		this.key = key;
		this.possibleValues.addAll(Arrays.asList(values));
	}

	public String getKey() {
		return this.key;
	}

	public Set<String> getPossibleValues() {
		return this.possibleValues;
	}

	public void addPossibleValue(String possibleValue) {
		this.possibleValues.add(possibleValue);
	}

	public void addPossibleValues(String... possibleValues) {
		for (String possibleValue : possibleValues) {
			this.possibleValues.add(possibleValue);
		}
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof IModelItem) {
			result=this.getUID().equals(((IModelItem) obj).getUID());
		}
		return result;
	}

	@Override
	public String getUID() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("key:" + key + ";possibleValues:[");
		for (String possibleValue : possibleValues) {
			stringBuilder.append(possibleValue + ",");
		}
		if (!possibleValues.isEmpty()) {
			stringBuilder.deleteCharAt(stringBuilder.length()-1);
		}
		stringBuilder.append("]");
		return stringBuilder.toString();
	}

	@Override
	public StringBuilder toStringBuilderDescription() {
		return new StringBuilder(getUID());
	}
}
