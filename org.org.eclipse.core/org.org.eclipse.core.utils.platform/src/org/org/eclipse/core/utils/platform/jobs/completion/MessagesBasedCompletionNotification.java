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

import org.eclipse.swt.graphics.Image;
import org.org.eclipse.core.utils.platform.PlatformUtilsPlugin;
import org.org.eclipse.core.utils.platform.images.PluginImages;

public abstract class MessagesBasedCompletionNotification implements IJobCompletionNotification {
	public static enum MessageType {
		OK, ERROR, WARNING
	}

	private final MessageType messageType;
	private final String label;
	private final String message;
	private final Image notificationImage;

	public MessagesBasedCompletionNotification(MessageType messageType, String label, String message) {
		this.messageType = messageType;
		this.label = label;
		this.message = message;
		this.notificationImage=null;
	}
	public MessagesBasedCompletionNotification(MessageType messageType, String label, String message,Image notificationImage) {
		this.messageType = messageType;
		this.label = label;
		this.message = message;
		this.notificationImage=notificationImage;
	}

	public abstract void openTargetElement() ;
	
	public Image getOverlayIcon() {
		Image result = PlatformUtilsPlugin.getDefault().getImages().getImage(org.org.eclipse.core.utils.platform.images.PluginImages.SMALL_OK_16);
		if (messageType == MessageType.ERROR) {
			result = PlatformUtilsPlugin.getDefault().getImages().getImage(org.org.eclipse.core.utils.platform.images.PluginImages.SMALL_ERROR_16);
		}
		if (messageType == MessageType.WARNING) {
			result = PlatformUtilsPlugin.getDefault().getImages().getImage(org.org.eclipse.core.utils.platform.images.PluginImages.SMALL_WARNING_16);
		}
		return result;
	}

	public Image getNotificationIcon() {
		return notificationImage!=null?notificationImage:PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_OK_16);
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return label + message;
	}
}