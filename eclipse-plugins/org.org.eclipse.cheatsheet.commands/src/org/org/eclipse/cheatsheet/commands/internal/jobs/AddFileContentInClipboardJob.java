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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.tools.IOToolBox;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;

public class AddFileContentInClipboardJob extends Job {

	private static String JOB_ID = "CheatSheet helper: adding file's content to clipboard.";
	private final URL fileUrl;
	private final String encoding;

	public AddFileContentInClipboardJob(URL fileUrl, String encoding) {
		super(JOB_ID);
		this.fileUrl = fileUrl;
		this.encoding = encoding;
		this.setPriority(Job.SHORT);
		this.setUser(true);
		this.setRule(new BatchSimilarRule(CheatSheetJobs.JOB_FAMILY));
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus result = new StatusInfo(IStatus.OK, "File's content was successfully added to clipboard.");
		try {
			final StringBuilder fileContent = retrieveFileContent(monitor);
			final Display display = Display.getDefault();
			display.asyncExec(new Runnable() {

				public void run() {
					Clipboard clipboard = new Clipboard(display);
					clipboard.setContents(new Object[] { fileContent.toString() }, new Transfer[] { TextTransfer.getInstance() });
				}

			});

		} catch (Throwable e) {
			monitor.setCanceled(true);
			result = new StatusInfo(IStatus.ERROR, "A problem occured while adding file's content to clipboard:\n" + e.getMessage());
		}
		monitor.done();
		return result;
	}

	private StringBuilder retrieveFileContent(IProgressMonitor monitor) throws CoreException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOToolBox.downloadToOutputStreamAndCloseIt(baos, fileUrl, IOToolBox.determineProxy(fileUrl), new SubProgressMonitor(monitor, 1));
		return new StringBuilder(baos.toString(encoding));
	}

}