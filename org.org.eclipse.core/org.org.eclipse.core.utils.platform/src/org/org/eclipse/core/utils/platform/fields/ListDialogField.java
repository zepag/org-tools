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
package org.org.eclipse.core.utils.platform.fields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.org.eclipse.core.utils.platform.dialogs.IListAdapter;
import org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField;
import org.org.eclipse.core.utils.platform.dialogs.selection.TableLayoutComposite;
import org.org.eclipse.core.utils.platform.tools.PixelConverter;

/**
 * A list with a button bar. Typical buttons are 'Add', 'Remove', 'Up' and 'Down'. List model is independend of widget creation. DialogFields controls are: Label, List and Composite containing buttons.
 */
public class ListDialogField extends DialogField implements IListDialogField {

	public static class ColumnsDescription {
		private ColumnLayoutData[] columns;

		private String[] headers;

		private boolean drawLines;

		public ColumnsDescription(ColumnLayoutData[] columns, String[] headers, boolean drawLines) {
			this.columns = columns;
			this.headers = headers;
			this.drawLines = drawLines;
		}

		public ColumnsDescription(String[] headers, boolean drawLines) {
			this(createColumnWeightData(headers.length), headers, drawLines);
		}

		public ColumnsDescription(int nColumns, boolean drawLines) {
			this(createColumnWeightData(nColumns), null, drawLines);
		}

		private static ColumnLayoutData[] createColumnWeightData(int nColumns) {
			ColumnLayoutData[] data = new ColumnLayoutData[nColumns];
			for (int i = 0; i < nColumns; i++) {
				data[i] = new ColumnWeightData(1);
			}
			return data;
		}
	}

	protected TableViewer fTable;

	protected Control fTableControl;

	protected ILabelProvider fLabelProvider;

	protected ListViewerAdapter fListViewerAdapter;

	protected List<Object> fElements;

	protected ViewerSorter fViewerSorter;

	protected String[] fButtonLabels;

	private Button[] fButtonControls;

	private boolean[] fButtonsEnabled;

	private int fRemoveButtonIndex;

	private int fUpButtonIndex;

	private int fDownButtonIndex;

	@SuppressWarnings("unused")
	private Label fLastSeparator;

	private Composite fButtonsControl;

	private ISelection fSelectionWhenEnabled;

	private IListAdapter fListAdapter;

	private Object fParentElement;

	private ColumnsDescription fTableColumns;

	/**
	 * Creates the <code>ListDialogField</code>.
	 * 
	 * @param adapter
	 *            A listener for button invocation, selection changes. Can be <code>null</code>.
	 * @param buttonLabels
	 *            The labels of all buttons: <code>null</code> is a valid array entry and marks a separator.
	 * @param lprovider
	 *            The label provider to render the table entries
	 */
	public ListDialogField(IListAdapter adapter, String[] buttonLabels, ILabelProvider lprovider) {
		fListAdapter = adapter;

		fLabelProvider = lprovider;
		fListViewerAdapter = new ListViewerAdapter();
		fParentElement = this;

		fElements = new ArrayList<Object>(10);

		fButtonLabels = buttonLabels;
		if (fButtonLabels != null) {
			int nButtons = fButtonLabels.length;
			fButtonsEnabled = new boolean[nButtons];
			for (int i = 0; i < nButtons; i++) {
				fButtonsEnabled[i] = true;
			}
		}

		fTable = null;
		fTableControl = null;
		fButtonsControl = null;
		fTableColumns = null;

		fRemoveButtonIndex = -1;
		fUpButtonIndex = -1;
		fDownButtonIndex = -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#setRemoveButtonIndex(int)
	 */
	public void setRemoveButtonIndex(int removeButtonIndex) {
		Assert.isTrue(removeButtonIndex < fButtonLabels.length);
		fRemoveButtonIndex = removeButtonIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#setUpButtonIndex(int)
	 */
	public void setUpButtonIndex(int upButtonIndex) {
		Assert.isTrue(upButtonIndex < fButtonLabels.length);
		fUpButtonIndex = upButtonIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#setDownButtonIndex(int)
	 */
	public void setDownButtonIndex(int downButtonIndex) {
		Assert.isTrue(downButtonIndex < fButtonLabels.length);
		fDownButtonIndex = downButtonIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#setViewerSorter(org.eclipse.jface.viewers.ViewerSorter)
	 */
	public void setViewerSorter(ViewerSorter viewerSorter) {
		fViewerSorter = viewerSorter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#setTableColumns(org.org.eclipse.core.utils.platform.dialogs.formfields.ListDialogField.ColumnsDescription)
	 */
	public void setTableColumns(ColumnsDescription column) {
		fTableColumns = column;
	}

	// ------ adapter communication

	private void buttonPressed(int index) {
		if (!managedButtonPressed(index) && fListAdapter != null) {
			fListAdapter.customButtonPressed(this, index);
		}
	}

	/**
	 * Checks if the button pressed is handled internally
	 * 
	 * @return Returns true if button has been handled.
	 */
	protected boolean managedButtonPressed(int index) {
		if (index == fRemoveButtonIndex) {
			remove();
		} else if (index == fUpButtonIndex) {
			up();
			if (!fButtonControls[index].isEnabled() && fDownButtonIndex != -1) {
				fButtonControls[fDownButtonIndex].setFocus();
			}
		} else if (index == fDownButtonIndex) {
			down();
			if (!fButtonControls[index].isEnabled() && fUpButtonIndex != -1) {
				fButtonControls[fUpButtonIndex].setFocus();
			}
		} else {
			return false;
		}
		return true;
	}

	// ------ layout helpers

	/*
	 * @see DialogField#doFillIntoGrid
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
	 */
	public Control[] doFillIntoTable(Composite parent, int nColumns) {
		PixelConverter converter = new PixelConverter(parent);

		GridData gd = new GridData(SWT.FILL);
		parent.setLayoutData(gd);

		Label label = getLabelControl(parent);
		gd = gridDataForLabel(1);
		gd.verticalAlignment = SWT.RIGHT;
		gd.horizontalSpan = nColumns;
		label.setLayoutData(gd);

		Control list = getListControl(parent);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = nColumns - 1;
		gd.heightHint = converter.convertHeightInCharsToPixels(6);
		list.setLayoutData(gd);

		Composite buttons = getButtonBox(parent);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 1;
		buttons.setLayoutData(gd);

		return new Control[] { label, list, buttons };
	}

	/*
	 * @see DialogField#getNumberOfControls
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#getNumberOfControls()
	 */
	public int getNumberOfControls() {
		return 3;
	}

	// ------ ui creation

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#getListControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control getListControl(Composite parent) {
		if (fTableControl == null) {
			assertCompositeNotNull(parent);

			if (fTableColumns == null) {
				fTable = createTableViewer(parent);
				fTableControl = fTable.getTable();
				((Table) fTableControl).setLayout(new TableLayout());
			} else {
				fTableControl = new TableLayoutComposite(parent, SWT.NONE);
				fTable = createTableViewer((TableLayoutComposite) fTableControl);

				Table tableControl = fTable.getTable();

				tableControl.setHeaderVisible(fTableColumns.headers != null);
				tableControl.setLinesVisible(fTableColumns.drawLines);
				ColumnLayoutData[] columns = fTableColumns.columns;
				for (int i = 0; i < columns.length; i++) {
					((TableLayoutComposite) fTableControl).addColumnData(columns[i]);
					TableColumn column = new TableColumn(tableControl, SWT.NONE);
					// tableLayout.addColumnData(columns[i]);
					if (fTableColumns.headers != null) {
						column.setText(fTableColumns.headers[i]);
					}
				}
			}

			fTable.getTable().addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					handleKeyPressed(e);
				}
			});

			// fTableControl.setLayout(tableLayout);

			fTable.setContentProvider(fListViewerAdapter);
			fTable.setLabelProvider(fLabelProvider);
			fTable.addSelectionChangedListener(fListViewerAdapter);
			fTable.addDoubleClickListener(fListViewerAdapter);

			fTable.setInput(fParentElement);

			if (fViewerSorter != null) {
				fTable.setSorter(fViewerSorter);
			}

			fTableControl.setEnabled(isEnabled());
			if (fSelectionWhenEnabled != null) {
				postSetSelection(fSelectionWhenEnabled);
			}
		}
		return fTableControl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#getTableViewer()
	 */
	public TableViewer getTableViewer() {
		return fTable;
	}

	/*
	 * Subclasses may override to specify a different treeStyle.
	 */
	protected int getListStyle() {
		int style = SWT.WRAP | SWT.BORDER;
		return style;
	}

	protected TableViewer createTableViewer(Composite parent) {
		Table table = new Table(parent, getListStyle());
		return new TableViewer(table);
	}

	protected Button createButton(Composite parent, String label, SelectionListener listener) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.addSelectionListener(listener);
		GridData td = new GridData(GridData.FILL);
		button.setLayoutData(td);
		return button;
	}

	private Label createSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR);
		separator.setVisible(false);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = SWT.LEFT;
		gd.heightHint = 4;
		separator.setLayoutData(gd);
		return separator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#getButtonBox(org.eclipse.swt.widgets.Composite)
	 */
	public Composite getButtonBox(Composite parent) {
		if (fButtonsControl == null) {
			assertCompositeNotNull(parent);

			SelectionListener listener = new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					doButtonSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {
					doButtonSelected(e);
				}
			};

			Composite contents = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			contents.setLayout(layout);

			if (fButtonLabels != null) {
				fButtonControls = new Button[fButtonLabels.length];
				for (int i = 0; i < fButtonLabels.length; i++) {
					String currLabel = fButtonLabels[i];
					if (currLabel != null) {
						fButtonControls[i] = createButton(contents, currLabel, listener);
						fButtonControls[i].setEnabled(isEnabled() && fButtonsEnabled[i]);
					} else {
						fButtonControls[i] = null;
						createSeparator(contents);
					}
				}
			}

			fLastSeparator = createSeparator(contents);

			updateButtonState();
			fButtonsControl = contents;
		}

		return fButtonsControl;
	}

	private void doButtonSelected(SelectionEvent e) {
		if (fButtonControls != null) {
			for (int i = 0; i < fButtonControls.length; i++) {
				if (e.widget == fButtonControls[i]) {
					buttonPressed(i);
					return;
				}
			}
		}
	}

	/**
	 * Handles key events in the table viewer. Specifically when the delete key is pressed.
	 */
	protected void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0) {
			if (fRemoveButtonIndex != -1 && isButtonEnabled(fTable.getSelection(), fRemoveButtonIndex)) {
				managedButtonPressed(fRemoveButtonIndex);
			}
		}
	}

	// ------ enable / disable management

	/*
	 * @see DialogField#dialogFieldChanged
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#dialogFieldChanged()
	 */
	public void dialogFieldChanged() {
		super.dialogFieldChanged();
		updateButtonState();
	}

	/*
	 * Updates the enable state of the all buttons
	 */
	protected void updateButtonState() {
		if (fButtonControls != null && isOkToUse(fTableControl)) {
			ISelection sel = fTable.getSelection();
			for (int i = 0; i < fButtonControls.length; i++) {
				Button button = fButtonControls[i];
				if (isOkToUse(button)) {
					button.setEnabled(isButtonEnabled(sel, i));
				}
			}
		}
	}

	protected boolean getManagedButtonState(ISelection sel, int index) {
		if (index == fRemoveButtonIndex) {
			return !sel.isEmpty();
		} else if (index == fUpButtonIndex) {
			return !sel.isEmpty() && canMoveUp();
		} else if (index == fDownButtonIndex) {
			return !sel.isEmpty() && canMoveDown();
		}
		return true;
	}

	/*
	 * @see DialogField#updateEnableState
	 */
	protected void updateEnableState() {
		super.updateEnableState();

		boolean enabled = isEnabled();
		if (isOkToUse(fTableControl)) {
			if (!enabled) {
				fSelectionWhenEnabled = fTable.getSelection();
				selectElements(null);
			} else {
				selectElements(fSelectionWhenEnabled);
				fSelectionWhenEnabled = null;
			}
			fTableControl.setEnabled(enabled);
		}
		updateButtonState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#enableButton(int, boolean)
	 */
	public void enableButton(int index, boolean enable) {
		if (fButtonsEnabled != null && index < fButtonsEnabled.length) {
			fButtonsEnabled[index] = enable;
			updateButtonState();
		}
	}

	private boolean isButtonEnabled(ISelection sel, int index) {
		boolean extraState = getManagedButtonState(sel, index);
		return isEnabled() && extraState && fButtonsEnabled[index];
	}

	// ------ model access

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#setElements(java.util.Collection)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setElements(Collection elements) {
		fElements = new ArrayList(elements);
		if (isOkToUse(fTableControl)) {
			fTable.refresh();
		}
		dialogFieldChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#getElements()
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getElements() {
		return new ArrayList(fElements);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#getElement(int)
	 */
	public Object getElement(int index) {
		return fElements.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#getIndexOfElement(java.lang.Object)
	 */
	public int getIndexOfElement(Object elem) {
		return fElements.indexOf(elem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#replaceElement(java.lang.Object, java.lang.Object)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void replaceElement(Object oldElement, Object newElement) throws IllegalArgumentException {
		int idx = fElements.indexOf(oldElement);
		if (idx != -1) {
			fElements.set(idx, newElement);
			if (isOkToUse(fTableControl)) {
				List selected = getSelectedElements();
				if (selected.remove(oldElement)) {
					selected.add(newElement);
				}
				fTable.refresh();
				selectElements(new StructuredSelection(selected));
			}
			dialogFieldChanged();
		} else {
			throw new IllegalArgumentException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#addElement(java.lang.Object)
	 */
	public void addElement(Object element) {
		addElement(element, fElements.size());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#addElement(java.lang.Object, int)
	 */

	public void addElement(Object element, int index) {
		if (fElements.contains(element)) {
			return;
		}
		fElements.add(index, element);
		if (isOkToUse(fTableControl)) {
			fTable.add(element);
			fTable.setSelection(new StructuredSelection(element));
		}

		dialogFieldChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#addElements(java.util.List)
	 */
	@SuppressWarnings({ "rawtypes" })
	public void addElements(List elements) {
		int nElements = elements.size();

		if (nElements > 0) {
			// filter duplicated
			ArrayList<Object> elementsToAdd = new ArrayList<Object>(nElements);

			for (int i = 0; i < nElements; i++) {
				Object elem = elements.get(i);
				if (!fElements.contains(elem)) {
					elementsToAdd.add(elem);
				}
			}
			fElements.addAll(elementsToAdd);
			if (isOkToUse(fTableControl)) {
				fTable.add(elementsToAdd.toArray());
				fTable.setSelection(new StructuredSelection(elementsToAdd));
			}
			dialogFieldChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#removeAllElements()
	 */
	public void removeAllElements() {
		if (fElements.size() > 0) {
			fElements.clear();
			if (isOkToUse(fTableControl)) {
				fTable.refresh();
			}
			dialogFieldChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#removeElement(java.lang.Object)
	 */
	public void removeElement(Object element) throws IllegalArgumentException {
		if (fElements.remove(element)) {
			if (isOkToUse(fTableControl)) {
				fTable.remove(element);
			}
			dialogFieldChanged();
		} else {
			throw new IllegalArgumentException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#removeElements(java.util.List)
	 */
	@SuppressWarnings({ "rawtypes" })
	public void removeElements(List elements) {
		if (elements.size() > 0) {
			fElements.removeAll(elements);
			if (isOkToUse(fTableControl)) {
				fTable.remove(elements.toArray());
			}
			dialogFieldChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#getSize()
	 */
	public int getSize() {
		return fElements.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#selectElements(org.eclipse.jface.viewers.ISelection)
	 */
	public void selectElements(ISelection selection) {
		fSelectionWhenEnabled = selection;
		if (isOkToUse(fTableControl)) {
			fTable.setSelection(selection, true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#selectFirstElement()
	 */
	public void selectFirstElement() {
		Object element = null;
		if (fViewerSorter != null) {
			Object[] arr = fElements.toArray();
			fViewerSorter.sort(fTable, arr);
			if (arr.length > 0) {
				element = arr[0];
			}
		} else {
			if (fElements.size() > 0) {
				element = fElements.get(0);
			}
		}
		if (element != null) {
			selectElements(new StructuredSelection(element));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#postSetSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void postSetSelection(final ISelection selection) {
		if (isOkToUse(fTableControl)) {
			Display d = fTableControl.getDisplay();
			d.asyncExec(new Runnable() {
				public void run() {
					if (isOkToUse(fTableControl)) {
						selectElements(selection);
					}
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#refresh()
	 */
	public void refresh() {
		super.refresh();
		if (isOkToUse(fTableControl)) {
			fTable.refresh();
		}
	}

	// ------- list maintenance

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List moveUp(List elements, List move) {
		int nElements = elements.size();
		List res = new ArrayList(nElements);
		Object floating = null;
		for (int i = 0; i < nElements; i++) {
			Object curr = elements.get(i);
			if (move.contains(curr)) {
				res.add(curr);
			} else {
				if (floating != null) {
					res.add(floating);
				}
				floating = curr;
			}
		}
		if (floating != null) {
			res.add(floating);
		}
		return res;
	}

	private void moveUp(List<?> toMoveUp) {
		if (toMoveUp.size() > 0) {
			setElements(moveUp(fElements, toMoveUp));
			fTable.reveal(toMoveUp.get(0));
		}
	}

	private void moveDown(List<?> toMoveDown) {
		if (toMoveDown.size() > 0) {
			setElements(reverse(moveUp(reverse(fElements), toMoveDown)));
			fTable.reveal(toMoveDown.get(toMoveDown.size() - 1));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List reverse(List p) {
		List reverse = new ArrayList(p.size());
		for (int i = p.size() - 1; i >= 0; i--) {
			reverse.add(p.get(i));
		}
		return reverse;
	}

	private void remove() {
		removeElements(getSelectedElements());
	}

	private void up() {
		moveUp(getSelectedElements());
	}

	private void down() {
		moveDown(getSelectedElements());
	}

	private boolean canMoveUp() {
		if (isOkToUse(fTableControl)) {
			int[] indc = fTable.getTable().getSelectionIndices();
			for (int i = 0; i < indc.length; i++) {
				if (indc[i] != i) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean canMoveDown() {
		if (isOkToUse(fTableControl)) {
			int[] indc = fTable.getTable().getSelectionIndices();
			int k = fElements.size() - 1;
			for (int i = indc.length - 1; i >= 0; i--, k--) {
				if (indc[i] != k) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.formfields.IListDialogField#getSelectedElements()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List getSelectedElements() {
		List result = new ArrayList();
		if (isOkToUse(fTableControl)) {
			ISelection selection = fTable.getSelection();
			if (selection instanceof IStructuredSelection) {
				Iterator iter = ((IStructuredSelection) selection).iterator();
				while (iter.hasNext()) {
					result.add(iter.next());
				}
			}
		}
		return result;
	}

	// ------- ListViewerAdapter

	private class ListViewerAdapter implements IStructuredContentProvider, ISelectionChangedListener, IDoubleClickListener {

		// ------- ITableContentProvider Interface ------------

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// will never happen
		}

		@SuppressWarnings("unused")
		public boolean isDeleted(Object element) {
			return false;
		}

		public void dispose() {
		}

		public Object[] getElements(Object obj) {
			return fElements.toArray();
		}

		// ------- ISelectionChangedListener Interface ------------

		public void selectionChanged(SelectionChangedEvent event) {
			doListSelected(event);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
		 */
		public void doubleClick(DoubleClickEvent event) {
			doDoubleClick(event);
		}

	}

	protected void doListSelected(SelectionChangedEvent event) {
		updateButtonState();
		if (fListAdapter != null) {
			fListAdapter.selectionChanged(this);
		}
	}

	protected void doDoubleClick(DoubleClickEvent event) {
		if (fListAdapter != null) {
			fListAdapter.doubleClicked(this);
		}
	}

}
