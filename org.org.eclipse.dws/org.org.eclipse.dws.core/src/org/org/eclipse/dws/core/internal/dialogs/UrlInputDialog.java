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
package org.org.eclipse.dws.core.internal.dialogs;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.SimpleFieldIdentifier;


/**
 * The Class UrlInputDialog.
 */
public class UrlInputDialog extends AbstractInputTrayDialog {
	
	/** The Constant URL_FIELD. */
	public static final IFieldIdentifier URL_FIELD = new SimpleFieldIdentifier("url");

	/**
	 * Gets the url.
	 * 
	 * @return the url
	 */
	public String getUrl() {
		return (String) getFieldValueHolderValue(URL_FIELD);
	}

	/**
	 * Instantiates a new url input dialog.
	 * 
	 * @param shell the shell
	 */
	public UrlInputDialog(Shell shell) {
		super(shell, "Input an URL");
	}

	/* (non-Javadoc)
	 * @see org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	/**
	 * @see org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		createTextField(composite, URL_FIELD, "Url");
		return composite;
	}

}