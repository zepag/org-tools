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
package org.org.eclipse.dws.core.internal.handlers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;

public class TargetTypeValues implements IParameterValues {

	public static final String PROJECT_CLASSPATH = "PROJECT_CLASSPATH";
	public static final String WEB_INF_LIB = "WEB_INF_LIB";
	public static final String TARGET_DIR = "TARGET_DIR";

	public Map<String,String> getParameterValues() {
		Map<String,String> map = new HashMap<String,String>();
		map.put("Add to project classpath", PROJECT_CLASSPATH);
		map.put("Add to WEB-INF/lib or classpath depending on scope", WEB_INF_LIB);
		map.put("Add to a target dir", TARGET_DIR);
		return map;
	}
}
