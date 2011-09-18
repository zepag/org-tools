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
import org.org.eclipse.dws.core.DWSCorePlugin;
import org.org.eclipse.dws.core.internal.images.PluginImages;

/**
 * The Class JobEventBasedCompletionNotification.
 */
public class JobEventBasedCompletionNotification extends org.org.eclipse.core.utils.platform.jobs.completion.JobEventBasedCompletionNotification {

	/**
	 * Instantiates a new job event based completion notification.
	 * 
	 * @param label
	 *            the label
	 * @param event
	 *            the event
	 */
	public JobEventBasedCompletionNotification(String label, IJobChangeEvent event) {
		super(label, event, DWSCorePlugin.getDefault().getImages().getImage(PluginImages.LOGO_MAVEN_HTTP_REPOSITORY_16));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.jobs.completion.JobEventBasedCompletionNotification#openTargetElement()
	 */
	/**
	 * @see org.org.eclipse.core.utils.platform.jobs.completion.JobEventBasedCompletionNotification#openTargetElement()
	 */
	@Override
	public void openTargetElement() {
		DWSCorePlugin.getDefault().notifyRepositoryModelUpdate(null);
	}
}