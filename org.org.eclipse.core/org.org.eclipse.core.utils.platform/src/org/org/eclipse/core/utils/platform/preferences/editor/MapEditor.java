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
package org.org.eclipse.core.utils.platform.preferences.editor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

/**
 * An abstract field editor that manages a table of input values. The editor displays a table containing the values, buttons for adding and removing values, and Up and Down buttons to adjust the order of elements in the table.
 * <p>
 * Subclasses must implement the <code>parseString</code>, <code>createList</code>, and <code>getNewInputObject</code> framework methods.
 * </p>
 */
public abstract class MapEditor extends FieldEditor {

	public static class Entry {
		private final String key;
		private final String value;

		public Entry(final String key, final String value) {
			this.key = key;
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}

		public String getKey() {
			return this.key;
		}

	}

	/**
	 * The table widget; <code>null</code> if none (before creation or after disposal).
	 */
	private Table table;

	/**
	 * The button box containing the Add, Remove, Up, and Down buttons; <code>null</code> if none (before creation or after disposal).
	 */
	private Composite buttonBox;

	/**
	 * The Add button.
	 */
	private Button addButton;

	/**
	 * The Remove button.
	 */
	private Button removeButton;

	/**
	 * The Up button.
	 */
	private Button upButton;

	/**
	 * The Down button.
	 */
	private Button downButton;

	/**
	 * The selection listener.
	 */
	private SelectionListener selectionListener;

	/**
	 * Creates a new table field editor
	 */
	protected MapEditor() {
	}

	/**
	 * Creates a map field editor.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 */
	protected MapEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		createControl(parent);
	}

	/**
	 * Notifies that the Add button has been pressed.
	 */
	private void addPressed() {
		setPresentsDefaultValue(false);
		Entry entry = getNewInputObject();

		if (entry != null) {
			int index = table.getSelectionIndex();
			if (index >= 0) {
				createTableItemFromEntry(entry, index + 1);
			} else {
				createTableItemFromEntry(entry, 0);
			}
			selectionChanged();
		}
	}

	private void createTableItemFromEntry(Entry entry, int index) {
		if (tableContains(entry)) {
			removePropertyTableItem(entry);
		}
		TableItem tableItem = new TableItem(table, SWT.NONE, index);
		tableItem.setText(new String[] { entry.getKey(), entry.getValue() });
	}

	private void removePropertyTableItem(Entry entry) {
		int index = -1;
		for (TableItem tableItem : table.getItems()) {
			index++;
			if (tableItem.getText(0).equals(entry.getKey())) {
				break;
			}
		}
		if (index >= 0) {
			table.remove(index);
		}
	}

	private boolean tableContains(Entry entry) {
		boolean result = false;
		for (TableItem tableItem : table.getItems()) {
			if (tableItem.getText(0).equals(entry.getKey())) {
				result = true;
				break;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void adjustForNumColumns(int numColumns) {
		Control control = getLabelControl();
		((GridData) control.getLayoutData()).horizontalSpan = numColumns;
		((GridData) table.getLayoutData()).horizontalSpan = numColumns - 1;
	}

	/**
	 * Creates the Add, Remove, Up, and Down button in the given button box.
	 * 
	 * @param box
	 *            the box for the buttons
	 */
	private void createButtons(Composite box) {
		addButton = createPushButton(box, "Add");//$NON-NLS-1$
		removeButton = createPushButton(box, "Remove");//$NON-NLS-1$
		upButton = createPushButton(box, "Up");//$NON-NLS-1$
		downButton = createPushButton(box, "Down");//$NON-NLS-1$
	}

	/**
	 * Combines the given table of items into a single string. This method is the converse of <code>parseString</code>.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @param entries
	 *            the table of items
	 * @return the combined string
	 * @see #parseString
	 */
	protected abstract String createListPreference(Set<Entry> entries);

	/**
	 * Helper method to create a push button.
	 * 
	 * @param parent
	 *            the parent control
	 * @param key
	 *            the resource name used to supply the button's label text
	 * @return Button
	 */
	private Button createPushButton(Composite parent, String key) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(JFaceResources.getString(key));
		button.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		int widthHint = convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.addSelectionListener(getSelectionListener());
		return button;
	}

	/**
	 * Creates a selection listener.
	 */
	public void createSelectionListener() {
		selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Widget widget = event.widget;
				if (widget == addButton) {
					addPressed();
				} else if (widget == removeButton) {
					removePressed();
				} else if (widget == upButton) {
					upPressed();
				} else if (widget == downButton) {
					downPressed();
				} else if (widget == table) {
					selectionChanged();
				}
			}
		};
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);

		table = getTableControl(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalSpan = numColumns - 1;
		gd.grabExcessHorizontalSpace = true;
		table.setLayoutData(gd);

		buttonBox = getButtonBoxControl(parent);
		gd = new GridData();
		gd.verticalAlignment = GridData.BEGINNING;
		buttonBox.setLayoutData(gd);
	}

	protected void doLoad() {
		if (table != null) {
			String s = getPreferenceStore().getString(getPreferenceName());
			Map<String, String> array = parseString(s);
			if (array != null) {
				for (String key : array.keySet()) {
					createTableItemFromEntry(new Entry(key, array.get(key)), table.getItemCount());
				}
			}
		}
	}

	protected void doLoadDefault() {
		if (table != null) {
			table.removeAll();
			String s = getPreferenceStore().getDefaultString(getPreferenceName());
			Map<String, String> array = parseString(s);
			if (array != null) {
				for (String key : array.keySet()) {
					createTableItemFromEntry(new Entry(key, array.get(key)), table.getItemCount());
				}
			}
		}
	}

	protected void doStore() {
		Set<Entry> entries = new HashSet<Entry>();
		for (TableItem tableItem : table.getItems()) {
			entries.add(new Entry(tableItem.getText(0), tableItem.getText(1)));
		}
		String s = createListPreference(entries);
		if (s != null) {
			getPreferenceStore().setValue(getPreferenceName(), s);
		}
	}

	/**
	 * Notifies that the Down button has been pressed.
	 */
	private void downPressed() {
		swap(false);
	}

	/**
	 * Returns this field editor's button box containing the Add, Remove, Up, and Down button.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the button box
	 */
	public Composite getButtonBoxControl(Composite parent) {
		if (buttonBox == null) {
			buttonBox = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			buttonBox.setLayout(layout);
			createButtons(buttonBox);
			buttonBox.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					addButton = null;
					removeButton = null;
					upButton = null;
					downButton = null;
					buttonBox = null;
				}
			});

		} else {
			checkParent(buttonBox, parent);
		}

		selectionChanged();
		return buttonBox;
	}

	/**
	 * Returns this field editor's table control.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the table control
	 */
	public Table getTableControl(Composite parent) {
		if (table == null) {
			table = new Table(parent, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			table.setFont(parent.getFont());
			table.addSelectionListener(getSelectionListener());
			table.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					table = null;
				}
			});
			TableColumn tc = new TableColumn(table, SWT.LEFT);
			tc.setText("key");
			tc.setResizable(true);
			tc.setWidth(100);
			TableColumn tc2 = new TableColumn(table, SWT.LEFT);
			tc2.setText("value");
			tc2.setResizable(true);
			tc2.setWidth(100);
		} else {
			checkParent(table, parent);
		}
		return table;
	}

	/**
	 * Creates and returns a new item for the table.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @return a new item
	 */
	protected abstract Entry getNewInputObject();

	public int getNumberOfControls() {
		return 2;
	}

	/**
	 * Returns this field editor's selection listener. The listener is created if nessessary.
	 * 
	 * @return the selection listener
	 */
	private SelectionListener getSelectionListener() {
		if (selectionListener == null) {
			createSelectionListener();
		}
		return selectionListener;
	}

	/**
	 * Returns this field editor's shell.
	 * <p>
	 * This method is internal to the framework; subclassers should not call this method.
	 * </p>
	 * 
	 * @return the shell
	 */
	protected Shell getShell() {
		if (addButton == null) {
			return null;
		}
		return addButton.getShell();
	}

	/**
	 * Splits the given string into a table of strings. This method is the converse of <code>createList</code>.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @param stringList
	 *            the string
	 * @return an array of <code>String</code>
	 * @see #createList
	 */
	protected abstract Map<String, String> parseString(String stringList);

	/**
	 * Notifies that the Remove button has been pressed.
	 */
	private void removePressed() {
		setPresentsDefaultValue(false);
		int index = table.getSelectionIndex();
		if (index >= 0) {
			table.remove(index);
			selectionChanged();
		}
	}

	/**
	 * Notifies that the table selection has changed.
	 */
	private void selectionChanged() {

		int index = table.getSelectionIndex();
		int size = table.getItemCount();

		removeButton.setEnabled(index >= 0);
		upButton.setEnabled(size > 1 && index > 0);
		downButton.setEnabled(size > 1 && index >= 0 && index < size - 1);
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	public void setFocus() {
		if (table != null) {
			table.setFocus();
		}
	}

	/**
	 * Moves the currently selected item up or down.
	 * 
	 * @param up
	 *            <code>true</code> if the item should move up, and <code>false</code> if it should move down
	 */
	private void swap(boolean up) {
		setPresentsDefaultValue(false);
		int index = table.getSelectionIndex();
		int target = up ? index - 1 : index + 1;

		if (index >= 0) {
			TableItem[] selection = table.getSelection();
			Assert.isTrue(selection.length == 1);
			table.remove(index);
			createTableItemFromEntry(new Entry(selection[0].getText(0), selection[0].getText(1)), index);
			table.setSelection(target);
		}
		selectionChanged();
	}

	/**
	 * Notifies that the Up button has been pressed.
	 */
	private void upPressed() {
		swap(true);
	}

	/*
	 * @see FieldEditor.setEnabled(boolean,Composite).
	 */
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		getTableControl(parent).setEnabled(enabled);
		addButton.setEnabled(enabled);
		removeButton.setEnabled(enabled);
		upButton.setEnabled(enabled);
		downButton.setEnabled(enabled);
	}
}
