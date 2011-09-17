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

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.org.eclipse.core.ui.BasePlugin;
import org.org.eclipse.core.ui.images.PluginImages;

public class JobEventBasedCompletionNotification extends org.org.eclipse.core.utils.platform.jobs.completion.JobEventBasedCompletionNotification{


	public JobEventBasedCompletionNotification(String label,IJobChangeEvent event) {
		super(label,event,BasePlugin.getDefault().getImages().getImage(PluginImages.LOGO_ORG_16));

	}

	public void openTargetElement() {
		
	}
}