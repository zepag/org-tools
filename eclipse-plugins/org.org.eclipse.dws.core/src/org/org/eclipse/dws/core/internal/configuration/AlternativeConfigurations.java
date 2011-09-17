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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.WorkbenchException;
import org.org.eclipse.core.utils.platform.Messages;


/**
 * This class processes the extensions defined in other plugins in order to add them to the common configurations.
 * 
 * @author pagregoire
 */
public final class AlternativeConfigurations {
	
	/** The logger. */
	private static Logger logger = Logger.getLogger(AlternativeConfigurations.class);

	/** The alternative configurations. */
	private static Map<String, IAlternativeConfiguration> alternativeConfigurations = new LinkedHashMap<String, IAlternativeConfiguration>();

	/**
	 * Instantiates a new alternative configurations.
	 */
	private AlternativeConfigurations() {
	}

	/**
	 * Adds the alternative configuration.
	 * 
	 * @param pluginLabel the plugin label
	 * @param alternativeConfiguration the alternative configuration
	 */
	public static void addAlternativeConfiguration(String pluginLabel, IAlternativeConfiguration alternativeConfiguration) {
		alternativeConfigurations.put(pluginLabel + "-" + alternativeConfiguration.getLabel(), alternativeConfiguration);
	}

	/**
	 * Gets the alternative configurations.
	 * 
	 * @return the alternative configurations
	 */
	public static Set<IAlternativeConfiguration> getAlternativeConfigurations() {
		return new LinkedHashSet<IAlternativeConfiguration>(alternativeConfigurations.values());
	}

	/** The fully-qualified name of the functions extension-point for this plug-in. */
	private static final String EXTENSION_POINT = "org.org.eclipse.dws.core.DWSMaven2PluginAlternativeConfiguration"; //$NON-NLS-1$

	/** Name of the XML attribute designating the fully-qualified name of the implementation class of a function. */
	private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

	/** The processed. */
	private static boolean processed = false;

	/**
	 * Process.
	 * 
	 * @throws WorkbenchException the workbench exception
	 * @throws CoreException the core exception
	 */
	public static void process() throws WorkbenchException, CoreException {
		if (!processed) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT);
			if (extensionPoint == null) {
				throw new WorkbenchException(Messages.ProcessTableItems_error_extensionpointresolution + EXTENSION_POINT);
			}
			IConfigurationElement[] members = extensionPoint.getConfigurationElements();

			// For each service:
			for (int m = 0; m < members.length; m++) {
				IConfigurationElement member = members[m];
				// Get the label of the extender plugin and the ID of the extension.
				String contributorPluginLabel = member.getContributor().getName();
				if (contributorPluginLabel == null) {
					contributorPluginLabel = "[unnamed plugin]"; //$NON-NLS-1$
				}
				// Get the name of the operation implemented by the service.
				// The operation name is a service attribute in the extension's XML specification.
				Object callback = null;
				callback = member.createExecutableExtension(CLASS_ATTRIBUTE);
				if (callback == null) {
					throw new WorkbenchException(Messages.ProcessTableItems_error_extensionloading + CLASS_ATTRIBUTE);
				}
				if (callback instanceof IAlternativeConfiguration) {
					IAlternativeConfiguration alternativeConfiguration = (IAlternativeConfiguration) callback;
					addAlternativeConfiguration(contributorPluginLabel, alternativeConfiguration);
					logger.info("Found alternative configuration: " + alternativeConfiguration.getLabel() + " from plugin " + contributorPluginLabel);
				}
			}
			processed = true;
		}
	}

}