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

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.org.eclipse.core.utils.platform.jobs.completion.IJobCompletionNotification;

public class CompletionPopupJobChangeListener extends org.org.eclipse.core.utils.platform.jobs.completion.CompletionPopupJobChangeListener {

	public CompletionPopupJobChangeListener(String title, String label) {
		super(title, label);

	}

	@Override
	public IJobCompletionNotification createJobEventNotificationPopup(String label, IJobChangeEvent event) {
		return new JobEventBasedCompletionNotification(label, event);
	}
}