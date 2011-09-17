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

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * @author pagregoire
 */
public abstract class AbstractTabItemDefinition implements ITabItemDefinition {
	private static Logger logger = Logger.getLogger(AbstractTabItemDefinition.class);

	private IAdaptable element;

	private TabItem tabItem;

	private static FontMetrics fontMetrics;

	/**
	 * @return Returns the fontMetrics.
	 */
	protected static FontMetrics getFontMetrics() {
		return fontMetrics;
	}

	/**
	 * @param fontMetrics
	 *            The fontMetrics to set.
	 */
	protected static void setFontMetrics(FontMetrics fontMetrics) {
		AbstractTabItemDefinition.fontMetrics = fontMetrics;
	}

	/**
	 * @param tabItem
	 *            The tabItem to set.
	 */
	protected void setTabItem(TabItem tabItem) {
		this.tabItem = tabItem;
	}

	/**
	 * @param element
	 *            The element to set.
	 */
	protected void setElement(IAdaptable element) {
		this.element = element;
	}

	/**
	 * @param parent
	 * @param style
	 */
	public void createTabItem(TabFolder parent, IAdaptable element) {
		this.element = element;

		tabItem = new TabItem(parent, SWT.NONE);
		tabItem.setImage(createImage());
		tabItem.setText(createTitle());

		Composite composite = new Composite(parent, SWT.NONE);
		if (getFontMetrics() == null) {
			setFontMetrics(new GC(composite).getFontMetrics());
		}
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		createContents(composite);

		getProperties();
		tabItem.setControl(composite);
	}

	/**
	 * @param parent
	 * @return
	 */
	protected static Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		return composite;
	}

	protected Table createTable(Composite parent, String label, String toolTip, String[] columnsTitles) {
		Label labelWidget = new Label(parent, SWT.NONE);
		labelWidget.setText(label);
		labelWidget.setToolTipText(toolTip);
		Table table = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setHeaderVisible(false);
		table.setLinesVisible(true);
		GridData data = new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL);
		data.heightHint = 100;
		table.setLayoutData(data);
		for (int i = 0; i < columnsTitles.length; i++) {
			createTableColumn(i, 200, table, columnsTitles[i]);
		}
		return table;
	}

	protected TableColumn createTableColumn(int columnIndex, int width, Table table, String title) {
		TableColumn tc = new TableColumn(table, SWT.LEFT);
		tc.setText(title);
		tc.setResizable(true);
		tc.setWidth(width);
		return tc;
	}

	protected static class ColumnsValues {
		private final String[] values;

		private final int hashCode;

		public ColumnsValues(String... values) {
			this.values = values;
			this.hashCode = calculateHashcode();
		}

		private int calculateHashcode() {
			int result = 37;
			for (String value : values) {
				result = 17 * result + value.hashCode();
			}
			return result;
		}

		public String[] getValues() {
			return values;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			return obj != null && obj instanceof ColumnsValues && this.hashCode == ((ColumnsValues) obj).hashCode;
		}
	}

	protected void addTableContents(Table table, Set<ColumnsValues> items) {
		for (ColumnsValues columnsValues : items) {
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(columnsValues.getValues());
		}
	}

	protected static Text createLabelAndTextField(Composite parent, String label, String toolTip, int textFieldWidth) {
		Composite composite = createDefaultComposite(parent);
		Label labelWidget = new Label(composite, SWT.NONE);
		labelWidget.setText(label);
		labelWidget.setToolTipText(toolTip);
		Text textWidget = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = Dialog.convertWidthInCharsToPixels(getFontMetrics(), textFieldWidth);
		textWidget.setLayoutData(gd);
		return textWidget;
	}

	protected static Button createLabelAndCheckBoxField(Composite parent, String label, String toolTip, int textFieldWidth) {
		Composite composite = createDefaultComposite(parent);
		Button checkBoxWidget = new Button(composite, SWT.CHECK | SWT.LEFT);
		checkBoxWidget.setText(label);
		checkBoxWidget.setToolTipText(toolTip);
		return checkBoxWidget;
	}

	/**
	 * @see org.org.eclipse.core.utils.platform.properties.ITabItemContainer#performDefaults()
	 */
	public void performDefaults() {
		setDefaults();
		try {
			storeProperties();
		} catch (CoreException e) {
			logger.warn("using default properties for this project");
		}
	}

	/**
	 * @see org.org.eclipse.core.utils.platform.properties.ITabItemContainer#performOk()
	 */
	public boolean performOk() {
		try {
			storeProperties();
		} catch (CoreException e) {
			return false;
		}
		return true;
	}

	/**
	 * @return
	 */
	public IAdaptable getElement() {
		return element;
	}

	/**
	 * @see org.org.eclipse.core.utils.platform.properties.ITabItemContainer#getTabItem()
	 */
	public TabItem getTabItem() {
		return tabItem;
	}

	protected abstract Image createImage();

	protected abstract String createTitle();

	protected abstract void createContents(Composite composite);

	protected abstract void setDefaults();

	protected abstract void storeProperties() throws CoreException;

	protected abstract void getProperties();

	protected abstract void init();
}