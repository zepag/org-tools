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
package org.org.eclipse.dws.core.internal.configuration.preferences.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.org.eclipse.dws.core.internal.configuration.ConfigurationConstants;
import org.org.eclipse.dws.core.internal.dialogs.LibraryPromptDialog;


/**
 * A field editor to edit directory paths.
 */
public class LibrariesFieldEditor extends ListEditor {

	/**
	 * Creates a new path field editor.
	 */
	protected LibrariesFieldEditor() {
	}

	/**
	 * Creates a path field editor.
	 * 
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent the parent of the field editor's control
	 * @param urlChooserLabelText the url chooser label text
	 */
	public LibrariesFieldEditor(String name, String labelText, String urlChooserLabelText, Composite parent) {
		init(name, labelText);
		createControl(parent);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.ListEditor#createList(java.lang.String[])
	 */
	/**
	 * @see org.eclipse.jface.preference.ListEditor#createList(java.lang.String[])
	 */
	@Override
	protected String createList(String[] items) {
		StringBuffer path = new StringBuffer("");//$NON-NLS-1$
		for (int i = 0; i < items.length; i++) {
			path.append(items[i]);
			path.append(ConfigurationConstants.PIPE_SEPARATOR); 
		}
		return path.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.ListEditor#getNewInputObject()
	 */
	/**
	 * @see org.eclipse.jface.preference.ListEditor#getNewInputObject()
	 */
	@Override
	protected String getNewInputObject() {
		LibraryPromptDialog libraryPromptDialog = new LibraryPromptDialog(getShell()); 
		if (libraryPromptDialog.open() == Window.CANCEL) {
			return null;
		}
		return libraryPromptDialog.getLibrary();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.ListEditor#parseString(java.lang.String)
	 */
	/**
	 * @see org.eclipse.jface.preference.ListEditor#parseString(java.lang.String)
	 */
	@Override
	protected String[] parseString(String stringList) {
		StringTokenizer st = new StringTokenizer(stringList, ConfigurationConstants.PIPE_SEPARATOR); 
		List<String> v = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			v.add(st.nextToken());
		}
		return v.toArray(new String[v.size()]);
	}
}