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

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

public class FieldValueModifiedListener implements ModifyListener {
	private IFieldValueHolder<String> name;
	private Map<String, String> valuesMapping;

	public FieldValueModifiedListener(IFieldValueHolder<String> name) {
		this.name = name;
	}

	public FieldValueModifiedListener(IFieldValueHolder<String> name, Map<String, String> valuesMapping) {
		this.name = name;
		this.valuesMapping = Collections.<String, String> unmodifiableMap(valuesMapping);
	}

	public void modifyText(ModifyEvent e) {
		if (e.getSource() instanceof Text) {
			Text text = ((Text) e.getSource());
			String textContent = text.getText();
			synchronized (this) {
				name.setValue(textContent);
			}
		}
		if (e.getSource() instanceof Combo) {
			Combo combo = ((Combo) e.getSource());
			String textContent = combo.getText();
			if (valuesMapping != null) {
				textContent = valuesMapping.get(textContent);
			}
			synchronized (this) {
				name.setValue(textContent);
			}
		}
	}

}
