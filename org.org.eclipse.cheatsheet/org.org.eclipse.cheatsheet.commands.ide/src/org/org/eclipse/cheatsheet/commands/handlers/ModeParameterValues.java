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

public class ModeParameterValues implements IParameterValues {

	public static final String REPLACE = "REPLACE";
	public static final String PROMPT = "PROMPT";
	public static final String SUFFIX = "SUFFIX";
	public static final String SKIP = "SKIP";

	
	public Map<String,String> getParameterValues() {
		Map<String,String> map = new HashMap<String,String>();
		map.put("Replace if exists", REPLACE);
		map.put("Create with an additional suffix if exists", SUFFIX);
		map.put("Prompt user for replacement if exists.", PROMPT);
		map.put("Skip if exists.", SKIP);
		return map;
	}
}
