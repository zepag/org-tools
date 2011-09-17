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
 * The Interface IAlternativeConfiguration.
 */
public interface IAlternativeConfiguration {

	/**
	 * Gets the label.
	 * 
	 * @return the label
	 */
	public String getLabel();

	/**
	 * Gets the behaviour.
	 * 
	 * @return the behaviour
	 */
	public Behaviour getBehaviour();

	/**
	 * Gets the priority.
	 * 
	 * @return the priority
	 */
	public Integer getPriority();

	/**
	 * Gets the configuration property or null if not available.
	 * 
	 * @param name the name
	 * 
	 * @return the configuration property or null if not available
	 */
	public ConfigurationProperty getConfigurationPropertyOrNullIfNotAvailable(String name);
}
