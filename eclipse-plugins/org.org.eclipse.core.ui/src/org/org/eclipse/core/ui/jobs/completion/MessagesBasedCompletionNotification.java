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
package org.org.eclipse.core.ui.jobs.completion;

import org.org.eclipse.core.ui.BasePlugin;
import org.org.eclipse.core.ui.images.PluginImages;


public class MessagesBasedCompletionNotification extends org.org.eclipse.core.utils.platform.jobs.completion.MessagesBasedCompletionNotification  {
	
	public MessagesBasedCompletionNotification(MessageType messageType, String label, String message) {
		super(messageType,label,message,BasePlugin.getDefault().getImages().getImage(PluginImages.LOGO_ORG_16));
	}

	public void openTargetElement() {
		
	}
}