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
package org.org.eclipse.core.utils.platform.actions;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

/**
 * @author pagregoire
 */
public class AbstractSimpleAction extends Action {
	public static final boolean ASYNC_EXEC = false;
	public static final boolean SYNC_EXEC = true;
	/**
	 * Logger for this class
	 */
	private static Logger logger = Logger.getLogger(AbstractSimpleAction.class);

	/**
	 * Convenience method for running an operation with progress and error feedback.
	 * 
	 * @param runnable
	 *            the runnable which executes the operation
	 * @param problemMessage
	 *            the message to display in the case of errors
	 * @param progressKind
	 *            one of PROGRESS_BUSYCURSOR or PROGRESS_DIALOG
	 */
	final protected void run(final IRunnableWithProgress runnableWithProgress, final String problemMessage, final IRunnableContext progressMonitorDialog, boolean syncExec) {
//		final Exception[] exceptions = new Exception[] { null };
		Display d = Display.findDisplay(Thread.currentThread());
		try {
			Runnable runnable = new Runnable() {
				public void run() {
					try {
						progressMonitorDialog.run(false, true, runnableWithProgress);
					} catch (InvocationTargetException e) {
						logger.error("run()", e);
//						exceptions[0] = e;
					} catch (InterruptedException e) {
						logger.error("run()", e);
//						exceptions[0] = null;
					}
				}
			};
			if (syncExec) {
				d.syncExec(runnable);
			} else {
				d.asyncExec(runnable);
			}
		} catch (Exception e) {
			logger.error("run(IRunnableWithProgress, String, int)", e);
		}
	}

	/**
	 * Convenience method for running an operation without progress nor error feedback.
	 * 
	 * @param runnable
	 *            the runnable which executes the operation
	 * @param problemMessage
	 *            the message to display in the case of errors
	 * @param progressKind
	 *            one of PROGRESS_BUSYCURSOR or PROGRESS_DIALOG
	 */
	final protected void run(Runnable runnable,boolean syncExec) {
		Display d = Display.findDisplay(Thread.currentThread());
		try {
			if (syncExec) {
				d.syncExec(runnable);
			} else {
				d.asyncExec(runnable);
			}
		} catch (Exception e) {
			logger.error("run(IRunnableWithProgress, String, int)", e);
		}
	}
}
