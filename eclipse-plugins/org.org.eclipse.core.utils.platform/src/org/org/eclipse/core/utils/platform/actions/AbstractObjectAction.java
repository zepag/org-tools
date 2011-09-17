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

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * The abstract superclass of all Team actions. This class contains some convenience methods for getting selected objects and mapping selected objects to their providers.
 * 
 * Team providers may subclass this class when creating their actions. Team providers may also instantiate or subclass any of the subclasses of TeamAction provided in this package.
 */
public abstract class AbstractObjectAction extends AbstractAction implements IObjectActionDelegate {
	private IWorkbenchPart targetPart;

	/*
	 * Method declared on IObjectActionDelegate.
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		setShell(targetPart.getSite().getShell());
		this.targetPart = targetPart;
	}

	public IWorkbenchPart getTargetPart() {
		return targetPart;
	}
}