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

public class StringBuilderValidationResult implements IValidationResult {

	private StringBuilder messages = new StringBuilder();

	public StringBuilder append(String str) {
		return messages.append(str);
	}

	public String getMessage() {
		return messages.toString().length() > 0 ? messages.toString() : null;
	}

	public void appendMessage(String message) {
		this.messages.append(message);
		if (!message.endsWith("\n")) {
			this.messages.append("\n");
		}
	}

	public Boolean hasMessage(String message) {
		return (messages != null && messages.length() > 0);
	}
}
