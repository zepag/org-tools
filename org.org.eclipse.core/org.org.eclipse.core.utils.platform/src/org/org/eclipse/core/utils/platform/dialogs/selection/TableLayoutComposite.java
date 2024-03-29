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
package org.org.eclipse.core.utils.platform.dialogs.selection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;

/**
 * A special composite to layout columns inside a table. The composite is needed since we have to layout the columns "before" the actual table gets layouted. Hence we can't use a normal layout manager.
 */
public class TableLayoutComposite extends Composite {

	private List<ColumnLayoutData> columns = new ArrayList<ColumnLayoutData>();

	/**
	 * Creates a new <code>TableLayoutComposite</code>.
	 */
	public TableLayoutComposite(Composite parent, int style) {
		super(parent, style);
		final Composite fParent = parent;
		addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				// Rectangle area = ((TableLayoutComposite)e.widget).getParent().getParent().getBounds();
				Table table = (Table) getChildren()[0];
				// Point preferredSize = computeTableSize(table);
				// int width = area.width - 2 * table.getBorderWidth();
				// if (preferredSize.y > area.height) {
				// // Subtract the scrollbar width from the total column width
				// // if a vertical scrollbar will be required
				// Point vBarSize = table.getVerticalBar().getSize();
				// width -= vBarSize.x;
				// }
				layoutTable(table, fParent.getSize().x, fParent.getBounds(), table.getSize().x < fParent.getBounds().width);
			}
		});
	}

	/**
	 * Adds a new column of data to this table layout.
	 * 
	 * @param data
	 *            the column layout data
	 */
	public void addColumnData(ColumnLayoutData data) {
		columns.add(data);
	}

	// ---- Helpers -------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private Point computeTableSize(Table table) {
		Point result = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		int width = 0;
		for (ColumnLayoutData layoutData : columns) {
			if (layoutData instanceof ColumnPixelData) {
				ColumnPixelData col = (ColumnPixelData) layoutData;
				width += col.width;
			} else if (layoutData instanceof ColumnWeightData) {
				ColumnWeightData col = (ColumnWeightData) layoutData;
				width += col.minimumWidth;
			} else {
				Assert.isTrue(false, "Unknown column layout data"); //$NON-NLS-1$
			}
		}
		if (width > result.x)
			result.x = width;
		return result;
	}

	private void layoutTable(Table table, int width, Rectangle area, boolean increase) {
		// XXX: Layout is being called with an invalid value the first time
		// it is being called on Linux. This method resets the
		// Layout to null so we make sure we run it only when
		// the value is OK.
		if (width <= 1)
			return;

		TableColumn[] tableColumns = table.getColumns();
		int size = Math.min(columns.size(), tableColumns.length);
		int[] widths = new int[size];
		int fixedWidth = 0;
		int numberOfWeightColumns = 0;
		int totalWeight = 0;
		int i = 0;
		// First calc space occupied by fixed columns
		for (ColumnLayoutData layoutData : columns) {
			if (layoutData instanceof ColumnPixelData) {
				int pixels = ((ColumnPixelData) layoutData).width;
				widths[i++] = pixels;
				fixedWidth += pixels;
			} else if (layoutData instanceof ColumnWeightData) {
				ColumnWeightData cw = (ColumnWeightData) layoutData;
				numberOfWeightColumns++;
				// first time, use the weight specified by the column data, otherwise use the actual width as the weight
				// int weight = firstTime ? cw.weight : tableColumns[i].getWidth();
				int weight = cw.weight;
				totalWeight += weight;
			} else {
				Assert.isTrue(false, "Unknown column layout data"); //$NON-NLS-1$
			}
		}

		// Do we have columns that have a weight
		if (numberOfWeightColumns > 0) {
			// Now distribute the rest to the columns with weight.
			int rest = width - fixedWidth;
			int totalDistributed = 0;
			for (ColumnLayoutData layoutData : columns) {
				if (layoutData instanceof ColumnWeightData) {
					ColumnWeightData cw = (ColumnWeightData) layoutData;
					// calculate weight as above
					// int weight = firstTime ? cw.weight : tableColumns[i].getWidth();
					int weight = cw.weight;
					int pixels = totalWeight == 0 ? 0 : weight * rest / totalWeight;
					if (pixels < cw.minimumWidth)
						pixels = cw.minimumWidth;
					totalDistributed += pixels;
					widths[i] = pixels;
				}
			}

			// Distribute any remaining pixels to columns with weight.
			int diff = rest - totalDistributed;
			for (int j = 0; diff > 0; ++j) {
				if (j == size)
					j = 0;
				ColumnLayoutData col = (ColumnLayoutData) columns.get(j);
				if (col instanceof ColumnWeightData) {
					++widths[j];
					--diff;
				}
			}
		}

		if (increase) {
			table.setSize(area.width, area.height);
		}
		for (int j = 0; j < size; j++) {
			tableColumns[j].setWidth(widths[j]);
		}
		if (!increase) {
			table.setSize(area.width, area.height);
		}
	}
}