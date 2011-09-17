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
package org.org.eclipse.core.utils.platform.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchWizard;
import org.org.eclipse.core.utils.platform.Messages;


/**
 * This abstract wizard must be implemented by all wizards.
 * @author pagregoire
 */
public abstract class AbstractWorkbenchWizard extends Wizard implements IWorkbenchWizard{
    public void throwCoreException(String message) throws CoreException {
        IStatus status = new Status(IStatus.ERROR, Messages.AbstractWorkbenchWizard_problem, IStatus.OK, message, null); 
        throw new CoreException(status);
    }

    public void throwCoreException(String message, Exception cause) throws CoreException {
        IStatus status = new Status(IStatus.ERROR, Messages.AbstractWorkbenchWizard_problem, IStatus.OK, message, cause); 
        throw new CoreException(status);
    }
}
