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
package org.org.eclipse.core.utils.platform.dialogs;

import org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField;

/**
 * Change listener used by <code>ListDialogField</code> and <code>CheckedListDialogField</code>
 */
public interface IListAdapter {

	/**
	 * A button from the button bar has been pressed.
	 */
	void customButtonPressed(IListDialogField field, int index);

	/**
	 * The selection of the list has changed.
	 */
	void selectionChanged(IListDialogField field);

	/**
	 * En entry in the list has been double clicked
	 */
	void doubleClicked(IListDialogField field);

}
