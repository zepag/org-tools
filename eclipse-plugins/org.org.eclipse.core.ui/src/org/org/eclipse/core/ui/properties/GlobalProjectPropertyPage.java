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
package org.org.eclipse.core.ui.properties;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.WorkbenchException;
import org.org.eclipse.core.ui.BasePlugin;
import org.org.eclipse.core.ui.images.PluginImages;
import org.org.eclipse.core.utils.platform.extensions.ProcessTableItems;
import org.org.eclipse.core.utils.platform.properties.AbstractTabbedPropertyPage;
import org.org.eclipse.core.utils.platform.properties.ITabItemDefinition;
import org.org.eclipse.core.utils.platform.properties.PropertyPageTabItems;


public class GlobalProjectPropertyPage extends AbstractTabbedPropertyPage {
    private static Logger logger = Logger.getLogger(GlobalProjectPropertyPage.class);

    /**
     * Constructor for SamplePropertyPage.
     */
    public GlobalProjectPropertyPage() {
        this(null);
    }

    public GlobalProjectPropertyPage(IJavaProject project) {
        super();
        setElement(project);
        setImageDescriptor(BasePlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_ORG_64));
    }

    /**
     * @see org.org.eclipse.core.ui.properties.AbstractTabbedPropertyPage#initTabItemDefinitions()
     */
    public void initTabItemDefinitions() {
        try {
            ProcessTableItems.process();
            List<ITabItemDefinition> tabItemDefinitions = getTabItemsDefinitions();
            if (tabItemDefinitions == null) {
                tabItemDefinitions = PropertyPageTabItems.getTabItems();
                logger.debug(tabItemDefinitions.size() + " tabItemDefinitions configured");
            }
            setTabItemsDefinitions(tabItemDefinitions);
        } catch (WorkbenchException wbe) {
            logger.error("Can not process table items extension point :", wbe);
        } catch (CoreException ce) {
            logger.error("Can not process table items extension point :", ce);
        } catch (Exception e) {
            logger.error("Can not process table items extension point :", e);
        }
    }

}