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
package org.org.eclipse.cheatsheet.catalog.internal.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCategory;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetReference;
import org.org.model.IModelItem;

/**
 */
@SuppressWarnings("unchecked")
public class CheatCheatCatalogViewToolTip extends ToolTip {

	private static final String LINE_FEED = "\n";

	private final static int X_SHIFT;

	static {
		if ("gtk".equals(SWT.getPlatform()) || "carbon".equals(SWT.getPlatform())) {
			X_SHIFT = -26;
		} else {
			X_SHIFT = -23;
		}
	}

	private final static int Y_SHIFT = 1;

	private IModelItem currentTipElement;

	private final List<CheatSheetCatalogViewToolTipListener> listeners = new ArrayList<CheatSheetCatalogViewToolTipListener>();

	private boolean visible;

	private boolean triggeredByMouse;

	private final Control control;

	public CheatCheatCatalogViewToolTip(Control control) {
		super(control);

		this.control = control;
		setShift(new Point(1, 1));
	}

	public void dispose() {
		hide();
	}

	@Override
	protected void afterHideToolTip(Event event) {
		triggeredByMouse = true;
		visible = false;
		for (CheatSheetCatalogViewToolTipListener listener : listeners.toArray(new CheatSheetCatalogViewToolTipListener[0])) {
			listener.toolTipHidden(event);
		}
	}

	public void addTaskListToolTipListener(CheatSheetCatalogViewToolTipListener listener) {
		listeners.add(listener);
	}

	public void removeTaskListToolTipListener(CheatSheetCatalogViewToolTipListener listener) {
		listeners.remove(listener);
	}

	private IModelItem getViewElement(Object hoverObject) {
		if (hoverObject instanceof Widget) {
			Object data = ((Widget) hoverObject).getData();
			if (data != null) {
				if (data instanceof IModelItem) {
					return (IModelItem) data;
				} else if (data instanceof IAdaptable) {
					return (IModelItem) ((IAdaptable) data).getAdapter(IModelItem.class);
				}
			}
		}
		return null;
	}

	private String getTitleText(IModelItem element) {
		StringBuilder sb = new StringBuilder();
		if (element instanceof CheatSheetCatalog) {
			CheatSheetCatalog catalog = (CheatSheetCatalog) element;
			sb.append(catalog.getName());
		} else if (element instanceof CheatSheetCategory) {
			CheatSheetCategory category = (CheatSheetCategory) element;
			sb.append(category.getName());
		} else if (element instanceof CheatSheetReference) {
			CheatSheetReference reference = (CheatSheetReference) element;
			sb.append(reference.getName());
		}
		return sb.toString();

	}

	private String getDetailsText(IModelItem element) {
		StringBuilder sb = new StringBuilder();
		if (element instanceof CheatSheetCatalog) {
			CheatSheetCatalog catalog = (CheatSheetCatalog) element;
			sb.append("<b>Provider:</b> " + catalog.getName() + LINE_FEED);
			if (catalog.getDescription() != null && !catalog.getDescription().trim().equals("")) {
				sb.append("<b>Description:</b> " + catalog.getDescription() + LINE_FEED);
			}
			if (catalog.getReference() != null) {
				sb.append("<b>Reference CheatSheetReferenceType:</b> " + catalog.getReference().getReferenceType() + LINE_FEED);
				sb.append("<b>Reference Url:</b> " + LINE_FEED + catalog.getReference().getUri() + LINE_FEED);
			}
			sb.append("<b>Locked:</b> " + catalog.getReadOnly() + LINE_FEED);
		} else if (element instanceof CheatSheetCategory) {
			CheatSheetCategory category = (CheatSheetCategory) element;
			sb.append("<b>UID:</b> " + category.getUID());
		} else if (element instanceof CheatSheetReference) {
			CheatSheetReference reference = (CheatSheetReference) element;
			if (reference.getDescription() != null && !reference.getDescription().trim().equals("")) {
				sb.append("<b>Description:</b> " + reference.getDescription() + LINE_FEED);
			}
			sb.append("<b>Url:</b> " + LINE_FEED + reference.getUrl() + LINE_FEED);
			sb.append("<b>Tags:</b> " + reference.getTags() + LINE_FEED);
		}
		return sb.toString();
	}

	@Override
	public Point getLocation(Point tipSize, Event event) {
		Widget widget = getTipWidget(event);
		if (widget != null) {
			Rectangle bounds = getBounds(widget);
			if (bounds != null) {
				return control.toDisplay(bounds.x + X_SHIFT, bounds.y + bounds.height + Y_SHIFT);
			}
		}
		return super.getLocation(tipSize, event);// control.toDisplay(event.x + xShift, event.y + yShift);
	}

	@SuppressWarnings("unchecked")
	private Image getImage(IModelItem element) {
		return new ViewLabelProvider().getImage(element);
	}

	protected Widget getTipWidget(Event event) {
		Point widgetPosition = new Point(event.x, event.y);
		Widget widget = event.widget;
		if (widget instanceof ToolBar) {
			ToolBar w = (ToolBar) widget;
			return w.getItem(widgetPosition);
		}
		if (widget instanceof Table) {
			Table w = (Table) widget;
			return w.getItem(widgetPosition);
		}
		if (widget instanceof Tree) {
			Tree w = (Tree) widget;
			return w.getItem(widgetPosition);
		}

		return widget;
	}

	private Rectangle getBounds(Widget widget) {
		if (widget instanceof ToolItem) {
			ToolItem w = (ToolItem) widget;
			return w.getBounds();
		}
		if (widget instanceof TableItem) {
			TableItem w = (TableItem) widget;
			return w.getBounds();
		}
		if (widget instanceof TreeItem) {
			TreeItem w = (TreeItem) widget;
			return w.getBounds();
		}
		return null;
	}

	@Override
	protected boolean shouldCreateToolTip(Event event) {
		currentTipElement = null;

		if (super.shouldCreateToolTip(event)) {
			Widget tipWidget = getTipWidget(event);
			if (tipWidget != null) {
				currentTipElement = getViewElement(tipWidget);
			}
		}
		if (currentTipElement == null) {
			hide();
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected Composite createToolTipContentArea(Event event, Composite parent) {
		assert currentTipElement != null;

		FormToolkit toolkit = createToolTipContentAreaToolkit(parent);

		Form form = addIconAndLabel(parent, toolkit, getImage(currentTipElement), getTitleText(currentTipElement));

		String detailsText = getDetailsText(currentTipElement);
		if (detailsText != null) {
			addDetails(parent, toolkit, form, detailsText);
		}

		visible = true;

		return form;
	}

	private void addDetails(Composite parent, FormToolkit toolkit, Form form, String detailsText) {
		// create the text for user information
		FormText text = toolkit.createFormText(form.getBody(), true);
		GridData td = new GridData();
		td.horizontalSpan = 2;
		td.heightHint = 200;
		td.widthHint = 400;
		text.setLayoutData(td);
		detailsText = removeTrailingNewline(detailsText);
		detailsText = escapeLabelText(detailsText);
		detailsText = replaceLineFeedWithFormTags(detailsText);
		text.setText("<form><p>" + detailsText + "</p></form>", true, false);

	}

	private String replaceLineFeedWithFormTags(String detailsText) {
		return detailsText.replaceAll("\n", "</p><p>");
	}

	protected FormToolkit createToolTipContentAreaToolkit(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		return toolkit;

	}

	private String removeTrailingNewline(String text) {
		if (text.endsWith(LINE_FEED)) {
			return text.substring(0, text.length() - 1);
		}
		return text;
	}

	protected Form addIconAndLabel(Composite parent, FormToolkit toolkit, Image image, String titleText) {
		FormColors colors = toolkit.getColors();
		Color top = colors.getColor(IFormColors.H_GRADIENT_END);
		Color bot = colors.getColor(IFormColors.H_GRADIENT_START);
		// create the base form
		Form form = toolkit.createForm(parent);
		form.setText(titleText);
		form.setImage(image);
		form.setTextBackground(new Color[] { top, bot }, new int[] { 100 }, true);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		form.getBody().setLayout(layout);

		return form;
	}

	private String escapeLabelText(String text) {
		return (text != null) ? text.replace("&", "&&") : null; // mask & from SWT
	}

	public static interface CheatSheetCatalogViewToolTipListener {

		void toolTipHidden(Event event);

	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isTriggeredByMouse() {
		return triggeredByMouse;
	}

	@Override
	public void show(Point location) {
		super.show(location);
		triggeredByMouse = false;
	}
}