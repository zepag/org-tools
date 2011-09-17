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
package org.org.eclipse.core.utils.platform.fields;


/**
 * Change listener used by <code>DialogField</code>
 */
public interface IDialogFieldListener {

    /**
     * The dialog field has changed.
     */
    void dialogFieldChanged(IDialogField field);

}