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
package org.org.eclipse.cheatsheet.commands.handlers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;

public class FileGeneratorTypeParameterValues implements IParameterValues {

	public static final String VELOCITY = "VELOCITY";
	public static final String FREEMARKER = "FREEMARKER";

	public Map<String, String> getParameterValues() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("Velocity Engine", VELOCITY);
		map.put("Freemarker Engine", FREEMARKER);
		return map;
	}
}
