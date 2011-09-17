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
package org.org.eclipse.dws.ui.internal.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.org.eclipse.dws.core.internal.jobs.ComputeArtifactDetailsJob;
import org.org.eclipse.dws.core.internal.jobs.ComputeArtifactVersionDetailsJob;
import org.org.eclipse.dws.core.internal.jobs.ComputeGroupDetailsJob;
import org.org.eclipse.dws.core.internal.jobs.ComputeRepositoryDetailsJob;
import org.org.model.IModelItem;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.CrawledRepository;
import org.org.repository.crawler.maven2.model.Group;

/**
 */
@SuppressWarnings("unchecked")
public class MavenRepositoriesViewToolTip extends ToolTip {

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

	private final List<MavenRepositoriesViewToolTipListener> listeners = new ArrayList<MavenRepositoriesViewToolTipListener>();

	private boolean visible;

	private boolean triggeredByMouse;

	private final Control control;

	public MavenRepositoriesViewToolTip(Control control) {
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
		for (MavenRepositoriesViewToolTipListener listener : listeners.toArray(new MavenRepositoriesViewToolTipListener[0])) {
			listener.toolTipHidden(event);
		}
	}

	public void addTaskListToolTipListener(MavenRepositoriesViewToolTipListener listener) {
		listeners.add(listener);
	}

	public void removeTaskListToolTipListener(MavenRepositoriesViewToolTipListener listener) {
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
		return new MavenRepositoriesViewLabelProvider().getImage(element);
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
		String titleText = "Unknown element";
		String formattedElement = "Unknown element";
		if (currentTipElement instanceof CrawledRepository) {
			ComputeRepositoryDetailsJob job = new ComputeRepositoryDetailsJob((CrawledRepository) currentTipElement);
			job.run(new NullProgressMonitor());
			formattedElement = job.getFormattedRepository().toString();
			titleText = "CrawledRepository";
		} else if (currentTipElement instanceof Group) {
			ComputeGroupDetailsJob job = new ComputeGroupDetailsJob((Group) currentTipElement);
			job.run(new NullProgressMonitor());
			formattedElement = job.getFormattedGroup().toString();
			titleText = "Group";
		} else if (currentTipElement instanceof Artifact) {
			ComputeArtifactDetailsJob job = new ComputeArtifactDetailsJob((Artifact) currentTipElement);
			job.run(new NullProgressMonitor());
			formattedElement = job.getFormattedArtifact().toString();
			titleText = "Artifact";
		} else if (currentTipElement instanceof ArtifactVersion) {
			ComputeArtifactVersionDetailsJob job = new ComputeArtifactVersionDetailsJob((ArtifactVersion) currentTipElement);
			job.run(new NullProgressMonitor());
			formattedElement = job.getFormattedArtifactVersion().toString();
			titleText = "Artifact Version";
		}
		FormColors colors = toolkit.getColors();
		Color top = colors.getColor(IFormColors.H_GRADIENT_END);
		Color bot = colors.getColor(IFormColors.H_GRADIENT_START);
		// create the base form
		Form form = toolkit.createForm(parent);
		form.setText(titleText);
		form.setMessage("Double-click on element for full details.");
		form.setImage(getImage(currentTipElement));
		form.setTextBackground(new Color[] { top, bot }, new int[] { 100 }, true);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		form.getBody().setLayout(layout);
		FormText text = toolkit.createFormText(form.getBody(), true);
		GridData td = new GridData();
		td.horizontalSpan = 2;
		td.heightHint = 200;
		td.widthHint = 400;
		text.setLayoutData(td);

		text.setText(formattedElement, true, false);
		visible = true;

		return form;
	}

	protected FormToolkit createToolTipContentAreaToolkit(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		return toolkit;

	}

	protected Form addIconAndLabel(Composite parent, FormToolkit toolkit, Image image, String titleText) {
		// create the base form
		Form form = toolkit.createForm(parent);
		form.setText(titleText);
		form.setImage(image);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		form.getBody().setLayout(layout);
		toolkit.decorateFormHeading(form);
		return form;
	}

	public static interface MavenRepositoriesViewToolTipListener {

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