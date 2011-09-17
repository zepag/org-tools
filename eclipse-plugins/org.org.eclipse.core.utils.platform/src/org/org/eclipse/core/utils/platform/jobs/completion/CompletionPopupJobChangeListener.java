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
package org.org.eclipse.core.utils.platform.jobs.completion;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.events.HyperlinkAdapter;

public abstract class CompletionPopupJobChangeListener implements IJobChangeListener {
	private final String title;
	private final String label;
	private HyperlinkAdapter hyperlinkAdapter;

	public CompletionPopupJobChangeListener(String title, String label) {
		this.title = title;
		this.label = label;
	}

	public CompletionPopupJobChangeListener(String title, String label, HyperlinkAdapter hyperlinkAdapter) {
		this.title = title;
		this.label = label;
		this.hyperlinkAdapter = hyperlinkAdapter;
	}

	public void sleeping(IJobChangeEvent event) {
	}

	public void scheduled(IJobChangeEvent event) {
	}

	public void running(IJobChangeEvent event) {
	}

	public void done(final IJobChangeEvent event) {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				JobCompletionNotificationPopup popup = new JobCompletionNotificationPopup(Display.getDefault().getActiveShell(), title, hyperlinkAdapter);
				List<IJobCompletionNotification> notifications = new ArrayList<IJobCompletionNotification>();
				notifications.add(createJobEventNotificationPopup(label, event));

				popup.setContents(notifications);
				popup.open();
			}

		});
	}

	public void awake(IJobChangeEvent event) {
	}

	public void aboutToRun(IJobChangeEvent event) {
	}

	public abstract IJobCompletionNotification createJobEventNotificationPopup(String label2, IJobChangeEvent event);
}