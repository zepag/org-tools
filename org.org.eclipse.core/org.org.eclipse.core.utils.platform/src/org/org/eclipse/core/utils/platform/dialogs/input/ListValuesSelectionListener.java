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
package org.org.eclipse.core.utils.platform.dialogs.input;

import java.util.Collections;
import java.util.Map;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.List;

public class ListValuesSelectionListener implements SelectionListener {
	private IFieldValueHolder<String[]> name;
	private Map<String, String> valuesMapping;

	public ListValuesSelectionListener(IFieldValueHolder<String[]> name) {
		this.name = name;
	}

	public ListValuesSelectionListener(IFieldValueHolder<String[]> name, Map<String, String> valuesMapping) {
		this.name = name;
		this.valuesMapping = Collections.<String, String> unmodifiableMap(valuesMapping);
	}

	public void widgetDefaultSelected(SelectionEvent e) {

	}

	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() instanceof List) {
			List list = ((List) e.getSource());
			String[] textContent = list.getSelection();
			if (valuesMapping != null) {
				int i = 0;
				for (String text : textContent) {
					textContent[i++] = valuesMapping.get(text);
				}
			}
			synchronized (this) {
				name.setValue(textContent);
			}
		}
	}
}
