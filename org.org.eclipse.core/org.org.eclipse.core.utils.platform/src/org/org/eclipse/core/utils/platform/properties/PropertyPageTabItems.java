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

import java.util.ArrayList;
import java.util.List;

/**
 * @author pagregoire
 */
public final class PropertyPageTabItems {
    private static List<ITabItemDefinition> tabItems = new ArrayList<ITabItemDefinition>();

    /**
     * 
     */
    private PropertyPageTabItems() {
    }

    public static void addTabItem(ITabItemDefinition tabItem) {
        tabItems.add(tabItem);
    }

    public static List<ITabItemDefinition> getTabItems() {
        return tabItems;
    }
}
