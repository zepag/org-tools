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
package org.org.eclipse.dws.ui.internal.views;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;

public abstract class JobDoneListener implements IJobChangeListener {
	public void sleeping(IJobChangeEvent event) {

	}

	public void scheduled(IJobChangeEvent event) {

	}

	public void running(IJobChangeEvent event) {

	}

	public void awake(IJobChangeEvent event) {

	}

	public void aboutToRun(IJobChangeEvent event) {

	}

}