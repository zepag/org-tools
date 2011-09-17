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
package org.org.eclipse.core.utils.platform.properties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * @author pagregoire
 */
public interface ITabItemDefinition {

    public void createTabItem(TabFolder folder, IAdaptable selectedElement);

    public TabItem getTabItem();
    
    public boolean performOk();

    public void performDefaults();
}