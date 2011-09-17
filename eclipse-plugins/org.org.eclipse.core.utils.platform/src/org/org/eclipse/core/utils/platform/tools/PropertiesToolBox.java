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
package org.org.eclipse.core.utils.platform.tools;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

/**
 * @author pagregoire
 */
public final class PropertiesToolBox {
	private PropertiesToolBox() {
	}

	public static Object getProjectProperty(Map<String, Object> defaultValues, IProject project, String propertyName, boolean failIfNull) {
		Object result;
		try {
			result = project.getPersistentProperty(new QualifiedName("", propertyName));
			if (failIfNull && result == null) {
				throw new NullPointerException("No such property as " + propertyName + " defined for project" + project);
			} else if (result == null) {
				initPropertiesForProjectWithDefaultValues(defaultValues, project);
				result = project.getPersistentProperty(new QualifiedName("", propertyName));
			}
		} catch (CoreException e) {
			if (failIfNull) {
				throw new NullPointerException("No such property as " + propertyName + " defined for project" + project);
			} else {
				result = defaultValues.get(propertyName);
			}
		}
		return result;
	}

	public static Object getDefaultPropertyValue(Map<String, Object> defaultValues, String propertyName) {
		return defaultValues.get(propertyName);
	}

	public static void setDefaultPropertyValue(Map<String, Object> defaultValues, IProject project, String propertyName, String value) {
		defaultValues.put(propertyName, value);
		initPropertiesForProjectWithDefaultValues(defaultValues, project);
	}

	public static void setProjectProperty(Map<String, Object> defaultValues, IProject project, String propertyName, String value) throws CoreException {
		project.setPersistentProperty(new QualifiedName("", propertyName), value);
	}

	public static void initPropertiesForProjectWithDefaultValues(Map<String, Object> defaultValues, IProject project) {
		int errorCounter = 0;
		for (String key:defaultValues.keySet()) {
			try {
				project.setPersistentProperty(new QualifiedName("", key), defaultValues.get(key).toString());
			} catch (CoreException e) {
				errorCounter++;
			}
		}
		if (errorCounter > 0) {
			throw new NullPointerException("Did not load " + errorCounter + " properties for project " + project);
		}
	}
}
