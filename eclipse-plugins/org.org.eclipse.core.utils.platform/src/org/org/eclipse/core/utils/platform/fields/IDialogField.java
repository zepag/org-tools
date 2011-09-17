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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;


public interface IDialogField {

	/**
	 * Sets the label of the dialog field.
	 */
	public abstract void setLabelText(String labeltext);

	/**
	 * Defines the listener for this dialog field.
	 */
	public abstract void setDialogFieldListener(IDialogFieldListener listener);

	/**
	 * Programatical invocation of a dialog field change.
	 */
	public abstract void dialogFieldChanged();

	/**
	 * Tries to set the focus to the dialog field. Returns <code>true</code> if the dialog field can take focus. To be reimplemented by dialog field implementors.
	 */
	public abstract boolean setFocus();

	/**
	 * Posts <code>setFocus</code> to the display event queue.
	 */
	public abstract void postSetFocusOnDialogField(Display display);

	/**
	 * Returns the number of columns of the dialog field. To be reimplemented by dialog field implementors.
	 */
	public abstract int getNumberOfControls();

	/**
	 * Creates or returns the created label widget.
	 * 
	 * @param parent The parent composite or <code>null</code> if the widget has already been created.
	 */
	public abstract Label getLabelControl(Composite parent);

	/**
	 * Sets the enable state of the dialog field.
	 */
	public abstract void setEnabled(boolean enabled);

	/**
	 * Brings the UI in sync with the model. Only needed when model was changed in different thread whil UI was lready created.
	 */
	public abstract void refresh();

	/**
	 * Gets the enable state of the dialog field.
	 */
	public abstract boolean isEnabled();

}