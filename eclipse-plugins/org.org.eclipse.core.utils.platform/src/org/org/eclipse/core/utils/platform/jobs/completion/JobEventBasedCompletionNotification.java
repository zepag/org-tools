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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.org.eclipse.core.utils.platform.PlatformUtilsPlugin;
import org.org.eclipse.core.utils.platform.images.PluginImages;

public abstract class JobEventBasedCompletionNotification implements IJobCompletionNotification {
	private final IJobChangeEvent event;
	private final String label;
	private final Image notificationIcon;
	public JobEventBasedCompletionNotification(String label,IJobChangeEvent event,Image notificationIcon) {
		this.label = label;
		this.event = event;
		this.notificationIcon=notificationIcon;
	}
	public JobEventBasedCompletionNotification(String label,IJobChangeEvent event) {
		this.label = label;
		this.event = event;
		this.notificationIcon=null;
	}
	public abstract void openTargetElement() ;

	public Image getOverlayIcon() {
		Image result = PlatformUtilsPlugin.getDefault().getImages().getImage(org.org.eclipse.core.utils.platform.images.PluginImages.SMALL_OK_16);
		if (event.getJob().getResult().getSeverity() == IStatus.ERROR) {
			result = PlatformUtilsPlugin.getDefault().getImages().getImage(org.org.eclipse.core.utils.platform.images.PluginImages.SMALL_ERROR_16);
		}
		if (event.getJob().getResult().getSeverity() == IStatus.WARNING || event.getJob().getResult().getSeverity() == IStatus.CANCEL) {
			result = PlatformUtilsPlugin.getDefault().getImages().getImage(org.org.eclipse.core.utils.platform.images.PluginImages.SMALL_WARNING_16);
		}
		return result;
	}

	public Image getNotificationIcon() {
		return notificationIcon!=null?notificationIcon:PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_OK_16);
	}

	public String getLabel() {
		return event.getJob().getName();
	}

	public String getDescription() {
		return label + event.getResult().getMessage();
	}
}