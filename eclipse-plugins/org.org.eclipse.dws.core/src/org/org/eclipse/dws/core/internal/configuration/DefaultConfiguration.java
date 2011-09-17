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

import java.util.Properties;

import org.eclipse.core.resources.ResourcesPlugin;
import org.org.eclipse.dws.core.internal.configuration.preferences.PatternSetPreferencesHelper;
import org.org.repository.crawler.items.immutable.ImmutablePatternSet;


/**
 * The Class DefaultConfiguration.
 */
public class DefaultConfiguration implements IAlternativeConfiguration {

	/** The default properties. */
	private Properties defaultProperties;

	/**
	 * Instantiates a new default configuration.
	 * @param immutablePatternSet 
	 */
	public DefaultConfiguration(ImmutablePatternSet immutablePatternSet) {
		this.defaultProperties = new Properties();
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_AUTOCOMPLETE_URL, "http://repo1.maven.org/maven2");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.REPOSITORY_DEFAULT_AUTOCOMPLETE_NAME, "MAVEN2");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.FS_REPOSITORY_DEFAULT_AUTOCOMPLETE_URL, "");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_AUTOCOMPLETE_ENTRY_PATTERN, immutablePatternSet.getEntryPattern());
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_AUTOCOMPLETE_PARENT_PATTERN, immutablePatternSet.getParentDirectoryPattern());
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_AUTOCOMPLETE_DIRECTORY_ENTRY_PATTERN, immutablePatternSet.getDirectoryEntryPattern());
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_AUTOCOMPLETE_FILE_ENTRY_PATTERN, immutablePatternSet.getFileEntryPattern());
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_AUTOCOMPLETE_PROXY_HOST, "");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_AUTOCOMPLETE_PROXY_PORT, "");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_PATTERN_SET, PatternSetPreferencesHelper.serialize(immutablePatternSet));
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_LOCAL_PATH, ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toFile() + "/.maven2repo");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.POM_FILE_NAMES, "pom.xml");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.POM_FILE_ENCODING, "UTF-8");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.DOWNLOAD_TO_FOLDER, "libs");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.WEBAPP_LIBS_FOLDER, "WebContent/WEB-INF/lib");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.SCANNED_DEPENDENCIES, ".jar" + ConfigurationConstants.PIPE_SEPARATOR + ".war" + ConfigurationConstants.PIPE_SEPARATOR + ".pom" + ConfigurationConstants.PIPE_SEPARATOR + ".ear");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.DEAL_WITH_TRANSITIVE_DEPENDENCIES, "false");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.DEAL_WITH_UNDETERMINED_OR_RESTRICTIVE_SCOPE, "true");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.UNDETERMINED_OR_RESTRICTIVE_SCOPE_AUTOMATICALLY_ADDED, "false");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.TRANSITIVE_AUTOMATICALLY_ADDED, "false");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.AUTOMATICALLY_REMOVE_CONFLICTING, "true");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.CONSIDER_OPTIONAL_LIBRARIES, "false");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.USE_LIBRARY_CONTAINER, "false");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.VARIABLE_NAME, "M2_REPO");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.WIZARDS_POM_FILTERED_LIBRARIES, "rt.jar" + ConfigurationConstants.PIPE_SEPARATOR + "jsse.jar" + ConfigurationConstants.PIPE_SEPARATOR + "jce.jar" + ConfigurationConstants.PIPE_SEPARATOR + "charsets.jar" + ConfigurationConstants.PIPE_SEPARATOR + "dnsns.jar" + ConfigurationConstants.PIPE_SEPARATOR + "localedata.jar" + ConfigurationConstants.PIPE_SEPARATOR + "sunjce_provider.jar" + ConfigurationConstants.PIPE_SEPARATOR + "sunpkcs11.jar" + ConfigurationConstants.PIPE_SEPARATOR + "");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.WIZARDS_POM_HIDE_APPROXIMATIVE_MATCHES, "false");
		this.defaultProperties.setProperty(ConfigurationPropertiesConstants.WIZARDS_POM_NUMBER_OF_KEPT_MATCHES, "6");
	}

	/**
	 * @see org.org.eclipse.dws.core.internal.configuration.IAlternativeConfiguration#getConfigurationPropertyOrNullIfNotAvailable(java.lang.String)
	 */
	public ConfigurationProperty getConfigurationPropertyOrNullIfNotAvailable(String name) {
		ConfigurationProperty configurationProperty = null;
		if (defaultProperties.containsKey(name)) {
			configurationProperty = new ConfigurationProperty(name, defaultProperties.getProperty(name));
		}
		return configurationProperty;
	}

	/**
	 * @see org.org.eclipse.dws.core.internal.configuration.IAlternativeConfiguration#getBehaviour()
	 */
	public Behaviour getBehaviour() {
		return Behaviour.REPLACES_DEFAULT;
	}

	/**
	 * @see org.org.eclipse.dws.core.internal.configuration.IAlternativeConfiguration#getPriority()
	 */
	public Integer getPriority() {
		throw new UnsupportedOperationException("Default configuration doesn't use the regular priority mechanism");
	}

	/**
	 * @see org.org.eclipse.dws.core.internal.configuration.IAlternativeConfiguration#getLabel()
	 */
	public String getLabel() {
		return "Default configuration";
	}
}
