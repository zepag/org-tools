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
package org.org.eclipse.cheatsheet.commands.internal.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;

public class AddTextToClipboardJob extends Job {

	private static String JOB_ID = "CheatSheet helper: adding text to clipboard.";
	private final String line;

	public AddTextToClipboardJob(String line) {
		super(JOB_ID);
		this.line = line;
		this.setPriority(Job.SHORT);
		this.setUser(true);
		this.setRule(new BatchSimilarRule(CheatSheetJobs.JOB_FAMILY));
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus result = new StatusInfo(IStatus.OK, "text was successfully added to clipboard.");
		try {
			final Display display = Display.getDefault();
			display.asyncExec(new Runnable() {

				public void run() {
					Clipboard clipboard = new Clipboard(display);
					clipboard.setContents(new Object[] { line.toString() }, new Transfer[] { TextTransfer.getInstance() });
				}

			});
		} catch (Throwable e) {
			monitor.setCanceled(true);
			result = new StatusInfo(IStatus.ERROR, "A problem occured while adding text to clipboard:\n" + e.getMessage());
		}
		monitor.done();
		return result;
	}

}