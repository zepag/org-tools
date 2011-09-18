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
package org.org.eclipse.core.utils.platform.preferences;

import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.org.eclipse.core.utils.platform.PlatformUtilsException;

/**
 * @author pagregoire
 */
public class PreferencesFacade {
	private static Map<String, Object> defaultValues = null;

	public static void setDefaultValues(AbstractUIPlugin plugin, Map<String, Object> map) {
		defaultValues = map;
		initDefaultValues(plugin, true);
	}

	private static boolean defaultsSet = false;

	public static String getPreference(AbstractUIPlugin plugin, String propertyName, Class<?> type) {
		if (defaultValues == null) {
			throw new PlatformUtilsException("Default values' map should have been set at least one time");
		}
		String result = null;
		if (type.equals(String.class)) {
			result = plugin.getPreferenceStore().getString(propertyName);
		}
		if (type.equals(Boolean.class)) {
			result = Boolean.valueOf(plugin.getPreferenceStore().getBoolean(propertyName)).toString();
		}
		if (type.equals(Integer.class)) {
			result = Integer.valueOf(plugin.getPreferenceStore().getInt(propertyName)).toString();
		}
		if (type.equals(Double.class)) {
			result = new Double(plugin.getPreferenceStore().getDouble(propertyName)).toString();
		}
		if (type.equals(Float.class)) {
			result = new Float(plugin.getPreferenceStore().getFloat(propertyName)).toString();
		}
		if (type.equals(Long.class)) {
			result = Long.valueOf(plugin.getPreferenceStore().getLong(propertyName)).toString();
		}
		return result;
	}

	public static void setPreference(AbstractUIPlugin plugin, String propertyName, String value) {
		if (defaultValues == null) {
			throw new PlatformUtilsException("Default values' map should have been set at least one time");
		}
		Class<?> type = value.getClass();
		if (type.equals(String.class)) {
			plugin.getPreferenceStore().setValue(propertyName, (String) value);
		}
	}

	public static Object getDefaultPreferenceValue(String propertyName) {
		if (defaultValues == null) {
			throw new PlatformUtilsException("Default values' map should have been set at least one time");
		}
		return defaultValues.get(propertyName);
	}

	public static void setToDefaultValues(AbstractUIPlugin plugin) {
		if (defaultValues == null) {
			throw new PlatformUtilsException("Default values' map should have been set at least one time");
		}
		initDefaultValues(plugin, false);
		IPreferenceStore preferenceStore = plugin.getPreferenceStore();
		for (String next : defaultValues.keySet()) {
			preferenceStore.setToDefault(next);
		}
	}

	public static void setToDefaultValue(AbstractUIPlugin plugin, String propertyName) {
		if (defaultValues == null) {
			throw new PlatformUtilsException("Default values' map should have been set at least one time");
		}
		initDefaultValues(plugin, false);
		IPreferenceStore preferenceStore = plugin.getPreferenceStore();
		preferenceStore.setToDefault(propertyName);
	}

	public static void initDefaultValues(AbstractUIPlugin plugin, boolean force) {
		if (defaultValues == null) {
			throw new PlatformUtilsException("Default values' map should have been set at least one time");
		}
		if ((!defaultsSet) || force) {
			IPreferenceStore preferenceStore = plugin.getPreferenceStore();
			Object value = null;
			for (String key : defaultValues.keySet()) {
				value = defaultValues.get(key);
				if (value.getClass().equals(String.class)) {
					preferenceStore.setDefault(key, (String) value);
				}
				if (value.getClass().equals(Boolean.class)) {
					preferenceStore.setDefault(key, ((Boolean) value).booleanValue());
				}
				if (value.getClass().equals(Integer.class)) {
					preferenceStore.setDefault(key, ((Integer) value).intValue());
				}
				if (value.getClass().equals(Double.class)) {
					preferenceStore.setDefault(key, ((Double) value).doubleValue());
				}
				if (value.getClass().equals(Float.class)) {
					preferenceStore.setDefault(key, ((Float) value).floatValue());
				}
				if (value.getClass().equals(Long.class)) {
					preferenceStore.setDefault(key, ((Long) value).longValue());
				}
			}
			defaultsSet = true;
		}
	}
}