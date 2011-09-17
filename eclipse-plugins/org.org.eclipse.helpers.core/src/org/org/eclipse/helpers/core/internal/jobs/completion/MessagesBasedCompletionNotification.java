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
package org.org.eclipse.helpers.core.internal.jobs.completion;


public class MessagesBasedCompletionNotification extends org.org.eclipse.core.utils.platform.jobs.completion.MessagesBasedCompletionNotification {

	public MessagesBasedCompletionNotification(MessageType messageType, String label, String message) {
		super(messageType, label, message);
	}

	public void openTargetElement() {
	}
}