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
package org.org.eclipse.core.utils.platform.dialogs.input;

public class SimpleFieldIdentifier implements IFieldIdentifier {

	private final String id;

	public SimpleFieldIdentifier(String id) {
		if (id == null || id.trim().equals("")) {
			throw new IllegalArgumentException("id should not be null nor empty");
		}
		this.id = id;
	}

	public String getId(String id) {
		return this.id;
	}
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		boolean result = true;
		if (obj != null && (obj instanceof SimpleFieldIdentifier)) {
			SimpleFieldIdentifier that = (SimpleFieldIdentifier) obj;
			result = this.id.equals(that.id);
		} else {
			result = false;
		}
		return result;
	}

	@Override
	public String toString() {
		return "id:" + id;
	}
}
