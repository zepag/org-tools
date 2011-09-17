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
package org.org.eclipse.core.utils.platform.wizards.page;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;

public final class WizardContentsHelper {
	private WizardContentsHelper() {
	}

	public static ExpandBar createExpandBar(Composite parent) {
		return createExpandBar(parent, SWT.NONE | SWT.FLAT);
	}

	public static ExpandBar createExpandBar(Composite parent, int style) {
		Composite composite = new Composite(parent, style);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(1, false));
		composite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		ExpandBar expandBar = new ExpandBar(composite, SWT.V_SCROLL);
		expandBar.setBackground(composite.getBackground());
		return expandBar;
	}

	public static Composite createClientComposite(Composite parent) {
		return createClientComposite(parent, SWT.NONE);
	}

	public static Composite createClientComposite(Composite parent, int style) {
		Composite composite = new Composite(parent, style);
		return composite;
	}

	public static ExpandItem createExpandItem(ExpandBar expandBar, Composite clientControl, String label) {
		return createExpandItem(expandBar, clientControl, label, null);
	}

	public static ExpandItem createExpandItem(ExpandBar expandBar, Composite clientControl, String label, Image image) {
		ExpandItem expandItem = new ExpandItem(expandBar, SWT.NONE);
		expandItem.setText(label);
		expandItem.setControl(clientControl);
		expandItem.setHeight(clientControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		if (image != null) {
			expandItem.setImage(image);
		}
		expandItem.setExpanded(true);
		return expandItem;
	}

	public static Label createDescriptionLabel(Composite composite, String message) {
		Label label = new Label(composite, SWT.FLAT);
		label.setText(message);
		return label;
	}
}
