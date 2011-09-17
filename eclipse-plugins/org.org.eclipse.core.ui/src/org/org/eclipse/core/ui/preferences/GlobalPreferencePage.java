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
package org.org.eclipse.core.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.org.eclipse.core.ui.BasePlugin;
import org.org.eclipse.core.ui.Messages;
import org.org.eclipse.core.ui.images.PluginImages;


/**
 * This preference page is the ORG plugins' main preferences page
 */
public class GlobalPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    /**
     * default constructor
     */
    public GlobalPreferencePage() {
        super();
        setImageDescriptor(BasePlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_ORG_64));
        setPreferenceStore(BasePlugin.getDefault().getPreferenceStore());
        setDescription(Messages.getString("ORGPreferencePage_preferencesDescription")); //$NON-NLS-1$
        initializeDefaults();
    }

    /**
     * Sets the default values of the preferences.
     */
    private void initializeDefaults() {
//        IPreferenceStore store = getPreferenceStore();
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various types of preferences. Each field editor knows how to save and restore itself.
     */

    public void createFieldEditors() {
    }

    protected Control createContents(Composite parent) {
        return new Composite(parent, SWT.NULL);
    }

    public void init(IWorkbench workbench) {
    }
}