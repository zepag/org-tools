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

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * This code was (almost) replicated from Mylar's popup.
 * 
 * @author pagregoire
 */
public class JobCompletionNotificationPopup extends AbstractNotificationPopup {

	private static final String NOTIFICATIONS_HIDDEN = " more changes...";

	private static final int NUM_NOTIFICATIONS_TO_DISPLAY = 3;

	private String title = "Job Completed";

	private List<IJobCompletionNotification> notifications;

	private HyperlinkAdapter hyperlinkAdapter;

	private Color background;

	public JobCompletionNotificationPopup(Shell parent, String title, HyperlinkAdapter hyperlinkAdapter) {
		super(parent.getDisplay());
		// toolkit = new FormToolkit(parent.getDisplay());
		this.title = title;
		this.hyperlinkAdapter = hyperlinkAdapter;
		this.setFadingEnabled(true);
	}

	public JobCompletionNotificationPopup(Shell parent, String title) {
		super(parent.getDisplay());
		// toolkit = new FormToolkit(parent.getDisplay());
		this.title = title;
	}

	public void setContents(List<IJobCompletionNotification> notifications) {
		this.notifications = notifications;
	}

	@Override
	protected final void createContentArea(final Composite parent) {
		this.background = parent.getBackground();
		getShell().setText(title);
		int count = 0;
		for (final IJobCompletionNotification notification : notifications) {
			Composite notificationComposite = new Composite(parent, SWT.NO_FOCUS);
			notificationComposite.setLayout(new GridLayout(2, false));
			setBackground(notificationComposite);
			if (count < NUM_NOTIFICATIONS_TO_DISPLAY) {
				Label notificationLabelIcon = new Label(notificationComposite, SWT.NO_FOCUS);
				notificationLabelIcon.setImage(notification.getOverlayIcon());
				setBackground(notificationLabelIcon);
				ImageHyperlink link = new ImageHyperlink(notificationComposite, SWT.BEGINNING | SWT.WRAP | SWT.NO_FOCUS);
				link.setText(notification.getLabel());
				link.setImage(notification.getNotificationIcon());
				link.addHyperlinkListener(new HyperlinkAdapter() {
					@Override
					public void linkActivated(HyperlinkEvent e) {
						notification.openTargetElement();
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
				setBackground(link);
				String descriptionText = null;
				if (notification.getDescription() != null) {
					descriptionText = notification.getDescription();
				}
				if (descriptionText != null) {
					Label descriptionLabel = new Label(notificationComposite, SWT.NO_FOCUS);
					descriptionLabel.setText(descriptionText);
					setBackground(descriptionLabel);
					GridDataFactory.fillDefaults().span(2, SWT.DEFAULT).applyTo(descriptionLabel);
				}
			} else {
				int numNotificationsRemain = notifications.size() - count;
				Hyperlink remainingHyperlink = new Hyperlink(notificationComposite, SWT.NO_FOCUS);
				remainingHyperlink.setText(numNotificationsRemain + NOTIFICATIONS_HIDDEN);
				GridDataFactory.fillDefaults().span(2, SWT.DEFAULT).applyTo(remainingHyperlink);
				if (hyperlinkAdapter != null) {
					remainingHyperlink.addHyperlinkListener(hyperlinkAdapter);
				}
				setBackground(remainingHyperlink);
				break;
			}
			count++;
		}
	}

	private void setBackground(Control control) {
		control.setBackground(background);
	}

	@Override
	protected Image getPopupShellImage(int maximumHeight) {
		return null;
	}

	@Override
	protected String getPopupShellTitle() {
		return title;
	}

}