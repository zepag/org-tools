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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * The abstract superclass of all actions.
 */
public abstract class AbstractAction extends ActionDelegate {
    /**
     * Logger for this class
     */
    private static Logger logger = Logger.getLogger(AbstractAction.class);

    public final static int PROGRESS_BUSYCURSOR = 2;

    // Constants for determining the type of progress. Subclasses may
    // pass one of these values to the run method.
    public final static int PROGRESS_DIALOG = 1;

    public final static boolean SYNC_EXEC = true;

    public final static boolean ASYNC_EXEC = false;

    // The current selection
    private IStructuredSelection selection;

    // The shell, required for the progress dialog
    private Shell defaultShell;

    public abstract boolean isEnabled();

    /**
     * @return Returns the defaultShell.
     */
    protected Shell getDefaultShell() {
        return defaultShell;
    }

    /**
     * @param shell
     *            The shell to set.
     */
    protected void setShell(Shell shell) {
        this.defaultShell = shell;
    }

    /**
     * @param selection
     *            The selection to set.
     */
    protected void setSelection(IStructuredSelection selection) {
        this.selection = selection;
    }

    /**
     * Returns the selected projects.
     * 
     * @return the selected projects
     */
    protected IProject[] getSelectedProjects() {
        // project list
        List<IProject> projects = null;
        // if selection not empty
        if (!selection.isEmpty()) {
            projects = new ArrayList<IProject>();
            Iterator<?> elements = ((IStructuredSelection) selection).iterator();
            while (elements.hasNext()) {
                Object next = elements.next();
                // if the selected element is an IProject
                if (next instanceof IProject) {
                    projects.add((IProject) next);
                    continue;
                }
                // if the selected element is an IAdaptable
                if (next instanceof IAdaptable) {
                    IAdaptable a = (IAdaptable) next;
                    Object adapter = a.getAdapter(IResource.class);
                    if (adapter instanceof IProject) {
                        projects.add((IProject) adapter);
                        continue;
                    }
                }
            }
        }
        // if projectList is not Empty
        IProject[] result = new IProject[0];
        if (projects != null && !projects.isEmpty()) {
            result = new IProject[projects.size()];
            projects.toArray(result);
            return result;
        }
        return result;
    }

    /**
     * Returns the selected resources.
     * 
     * @return the selected resources
     */
    protected IResource[] getSelectedResources() {
        // resource list
        List<IResource> resources = null;
        // if selection not empty
        if (!selection.isEmpty()) {
            resources = new ArrayList<IResource>();
            Iterator<?> elements = ((IStructuredSelection) selection).iterator();
            while (elements.hasNext()) {
                Object next = elements.next();
                if (next instanceof IResource) {
                    resources.add((IResource) next);
                    continue;
                }
                if (next instanceof IAdaptable) {
                    IAdaptable a = (IAdaptable) next;
                    Object adapter = a.getAdapter(IResource.class);
                    if (adapter instanceof IResource) {
                        resources.add((IResource) adapter);
                        continue;
                    }
                }
            }
        }
        IResource[] result = new IResource[0];
        if (resources != null && !resources.isEmpty()) {
            result = new IResource[resources.size()];
            resources.toArray(result);
            return result;
        }
        return result;
    }

    /**
     * Returns the selection.
     * 
     * @return IStructuredSelection
     */
    public IStructuredSelection getSelection() {
        return selection;
    }

    /**
     * Convenience method for getting the current shell.
     * 
     * @return the shell
     */
    abstract protected Shell getShell();

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
//        final Exception[] exceptions = new Exception[] { null };
        Display d = Display.findDisplay(Thread.currentThread());
        try {
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        progressMonitorDialog.run(false, true, runnableWithProgress);
                    } catch (InvocationTargetException e) {
                        logger.error("run()", e);
//                        exceptions[0] = e;
                    } catch (InterruptedException e) {
                        logger.error("run()", e);
//                        exceptions[0] = null;
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
    final protected void run(Runnable runnable, boolean syncExec) {
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

    /*
     * Method declared on IActionDelegate.
     */
    public void selectionChanged(IAction action, ISelection selection) {

        if (selection instanceof IStructuredSelection) {
            this.selection = (IStructuredSelection) selection;
            if (action != null) {
                try {
                    action.setEnabled(isEnabled());
                } catch (Exception e) {
                    logger.error("selectionChanged(IAction, ISelection)", e);
                    action.setEnabled(false);
                }
            }
        }
    }

}