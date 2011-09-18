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
package org.org.eclipse.dws.core.internal.configuration;


/**
 * The Class ConfigurationProperty.
 */
public class ConfigurationProperty {

	/** The name. */
	private final String name;

	/** The value. */
	private final String value;

	/**
	 * Instantiates a new configuration property.
	 * 
	 * @param name the name
	 * @param value the value
	 */
	public ConfigurationProperty(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name + "=" + value;
	}
}
