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

import java.util.Set;

import org.apache.log4j.Logger;


/**
 * The Class ConfigurationsResolver.
 */
public class ConfigurationsResolver {
	
	/** The logger. */
	private static Logger logger = Logger.getLogger(ConfigurationsResolver.class);

	/** The default configuration. */
	private final DefaultConfiguration defaultConfiguration;

	/** The alternative configurations. */
	private final Set<IAlternativeConfiguration> alternativeConfigurations;

	/**
	 * Instantiates a new configurations resolver.
	 * 
	 * @param defaultConfiguration the default configuration
	 * @param alternativeConfigurations the alternative configurations
	 */
	public ConfigurationsResolver(final DefaultConfiguration defaultConfiguration, final Set<IAlternativeConfiguration> alternativeConfigurations) {
		super();
		this.defaultConfiguration = defaultConfiguration;
		this.alternativeConfigurations = alternativeConfigurations;
	}

	/**
	 * Resolve property.
	 * 
	 * @param propertyName the property name
	 * 
	 * @return the string
	 */
	public String resolveProperty(String propertyName) {
		return resolvePropertyWithMultipleValues(propertyName, null);
	}

	/**
	 * Resolve property with multiple values.
	 * 
	 * @param propertyName the property name
	 * @param separator the separator
	 * 
	 * @return the string
	 */
	public String resolvePropertyWithMultipleValues(String propertyName, String separator) {
		String result = "";
		ConfigurationProperty defaultProperty = defaultConfiguration.getConfigurationPropertyOrNullIfNotAvailable(propertyName);
		logger.debug("# Found one default Property: " + defaultProperty);
		result = defaultProperty.getValue();
		Integer latestPriority = 0;
		if (alternativeConfigurations.size() > 0) {
			for (IAlternativeConfiguration alternativeConfiguration : alternativeConfigurations) {
				ConfigurationProperty alternativeProperty = alternativeConfiguration.getConfigurationPropertyOrNullIfNotAvailable(propertyName);
				if (alternativeProperty != null) {
					logger.debug("-> Found one alternative Property in configuration " + alternativeConfiguration.getLabel() + ": " + alternativeProperty);
					if (alternativeConfiguration.getBehaviour() == Behaviour.COMPLEMENTS_DEFAULT && separator != null) {
						if (latestPriority < alternativeConfiguration.getPriority()) {
							logger.debug("\t-> " + alternativeConfiguration.getLabel() + " property complements this additive property.");
							result = result + separator + alternativeProperty.getValue();
						}
						if (latestPriority == alternativeConfiguration.getPriority()) {
							logger.debug("\t-> " + alternativeConfiguration.getLabel() + " property has same priority (" + latestPriority + ") as the latest evaluated alternative property. SKIPPED.");
						}
						if (latestPriority > alternativeConfiguration.getPriority()) {
							logger.debug("\t-> " + alternativeConfiguration.getLabel() + " a property of higher priority was chosen.");
						}
						latestPriority = alternativeConfiguration.getPriority();
					}
					if (alternativeConfiguration.getBehaviour() == Behaviour.REPLACES_DEFAULT) {
						if (latestPriority < alternativeConfiguration.getPriority()) {
							logger.debug("\t-> " + alternativeConfiguration.getLabel() + " property replaces default property.");
							result = alternativeProperty.getValue();
						}
						if (latestPriority == alternativeConfiguration.getPriority()) {
							logger.debug("\t-> " + alternativeConfiguration.getLabel() + " property has same priority (" + latestPriority + ") as the latest evaluated alternative property. SKIPPED.");
						}
						if (latestPriority > alternativeConfiguration.getPriority()) {
							logger.debug("\t-> " + alternativeConfiguration.getLabel() + " a property of higher priority was chosen.");
						}
					}
				} else {
					logger.debug("\t-> " + alternativeConfiguration.getLabel() + " property is not available.");
				}
			}
		}
		logger.info("Configuration property: \t" + propertyName + " = " + result);
		return result;
	}
}
