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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.dialogs.PropertyPage;
import org.org.eclipse.core.utils.platform.dialogs.selection.TabFolderLayout;

/**
 * @author pagregoire
 */
public abstract class AbstractTabbedPropertyPage extends PropertyPage {
	private static Logger logger = Logger
			.getLogger(AbstractTabbedPropertyPage.class);

	private List<ITabItemDefinition> tabItemsDefinitions;

	private TabFolder folder;

	/**
	 * @return Returns the tabItemsContainers.
	 */
	protected List<ITabItemDefinition> getTabItemsDefinitions() {
		return tabItemsDefinitions;
	}

	/**
	 * @param tabItemsContainers
	 *            The tabItemsContainers to set.
	 */
	protected void setTabItemsDefinitions(
			List<ITabItemDefinition> tabItemsContainers) {
		this.tabItemsDefinitions = tabItemsContainers;
	}

	protected Control createContents(Composite parent) {

		folder = new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		initTabItemDefinitions();
		createTabItems(folder);
		return folder;
	}

	protected void createTabItems(TabFolder folder) {
		logger.debug("creating " + getTabItemsDefinitions().size()
				+ " tabItemDefinitions.");
		for (ITabItemDefinition tabItemDefinition : getTabItemsDefinitions()) {
			try {
				logger.debug("adding tab "
						+ tabItemDefinition.getClass().getName());
				tabItemDefinition.createTabItem(folder, getElement());
			} catch (Exception e) {
				logger.error("cannot add...", e);
			}
		}
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		for (ITabItemDefinition tabItemDefinition : getTabItemsDefinitions()) {
			tabItemDefinition.performDefaults();
		}
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		for (ITabItemDefinition tabItemDefinition : getTabItemsDefinitions()) {
			tabItemDefinition.performOk();
		}
		return true;
	}

	/**
	 * This method must be implemented and must set the tabItemContainers
	 * protected attribute.
	 * 
	 * @param tabItemContainers
	 * @see AbstractTabbedPropertyPage#tabItemsDefinitions
	 */
	public abstract void initTabItemDefinitions();
}
