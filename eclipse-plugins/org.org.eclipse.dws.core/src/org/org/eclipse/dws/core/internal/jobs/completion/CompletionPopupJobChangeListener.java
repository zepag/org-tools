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
package org.org.eclipse.dws.core.internal.jobs.completion;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.org.eclipse.core.utils.platform.jobs.completion.IJobCompletionNotification;
import org.org.eclipse.dws.core.DWSCorePlugin;

/**
 * The listener interface for receiving completionPopupJobChange events. The class that is interested in processing a completionPopupJobChange event implements this interface, and the object created with that class is registered with a component using the component's <code>addCompletionPopupJobChangeListener<code> method. When
 * the completionPopupJobChange event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see CompletionPopupJobChangeEvent
 */
public class CompletionPopupJobChangeListener extends org.org.eclipse.core.utils.platform.jobs.completion.CompletionPopupJobChangeListener {

	/**
	 * Instantiates a new completion popup job change listener.
	 * 
	 * @param title
	 *            the title
	 * @param label
	 *            the label
	 */
	public CompletionPopupJobChangeListener(String title, String label) {
		super(title, label, new HyperlinkAdapter() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				DWSCorePlugin.getDefault().notifyRepositoryModelUpdate(null);
				// MavenRepositoriesView.showViewAndFocusOnElement(null);
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window != null) {

					Shell windowShell = window.getShell();
					if (windowShell != null) {
						windowShell.setMaximized(true);
						windowShell.open();
					}
				}
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.jobs.completion.CompletionPopupJobChangeListener#createJobEventNotificationPopup(java.lang.String, org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	/**
	 * @see org.org.eclipse.core.utils.platform.jobs.completion.CompletionPopupJobChangeListener#createJobEventNotificationPopup(java.lang.String, org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public IJobCompletionNotification createJobEventNotificationPopup(String label, IJobChangeEvent event) {
		return new JobEventBasedCompletionNotification(label, event);
	}
}