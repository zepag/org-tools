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
package org.org.eclipse.dws.core;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IFilter;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.org.eclipse.core.utils.platform.images.Images;
import org.org.eclipse.core.utils.platform.preferences.PreferencesFacade;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;
import org.org.eclipse.core.utils.platform.tools.logging.PluginLogAppender;
import org.org.eclipse.dws.core.internal.configuration.AlternativeConfigurations;
import org.org.eclipse.dws.core.internal.configuration.ConfigurationConstants;
import org.org.eclipse.dws.core.internal.configuration.ConfigurationPropertiesConstants;
import org.org.eclipse.dws.core.internal.configuration.ConfigurationsResolver;
import org.org.eclipse.dws.core.internal.configuration.DefaultConfiguration;
import org.org.eclipse.dws.core.internal.configuration.preferences.PreferencesNames;
import org.org.eclipse.dws.core.internal.images.PluginImages;
import org.org.model.IModelItem;
import org.org.repository.crawler.items.ICrawledRepositorySetup;
import org.org.repository.crawler.items.IFileSystemCrawledRepositorySetup;
import org.org.repository.crawler.items.IHttpCrawledRepositorySetup;
import org.org.repository.crawler.items.IPatternSet;
import org.org.repository.crawler.items.immutable.ImmutableFileSystemCrawledRepositorySetup;
import org.org.repository.crawler.items.immutable.ImmutableHttpCrawledRepositorySetup;
import org.org.repository.crawler.items.immutable.ImmutablePatternSet;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class DWSCorePlugin extends AbstractUIPlugin implements IExtensionChangeHandler {

	public static final String REPOSITORY_DEFINITION_EXTENSION_ID = "org.org.eclipse.dws.core.DWSMaven2RepositoryDefinition";

	public static final String PATTERN_SET_EXTENSION_ID = "org.org.eclipse.dws.core.DWSMaven2PluginPatternSet";

	public static final String MODEL_LISTENERS_EXTENSION_ID = "org.org.eclipse.dws.core.DWSRepositoryModelListeners";

	/** The logger. */
	private static Logger logger = Logger.getLogger(DWSCorePlugin.class);

	/** The Constant PI_MAVEN2. */
	public static final String PI_MAVEN2 = "org.org.eclipse.dws.core.DWSCorePlugin";

	/** Logger for this class. */

	// The shared instance.
	private static DWSCorePlugin plugin;

	/** The images. */
	private Images images;

	private ExtensionTracker extensionTracker;

	private Map<String, IFileSystemCrawledRepositorySetup> filesystemCrawledRepositoriesDefinitions = new ConcurrentHashMap<String, IFileSystemCrawledRepositorySetup>();
	private Map<String, ImmutablePatternSet> patternSets = new ConcurrentHashMap<String, ImmutablePatternSet>();

	private Map<String, IModelUpdateListener> repositoryModelUpdateListeners = new ConcurrentHashMap<String, IModelUpdateListener>();

	private Map<String, IHttpCrawledRepositorySetup> httpCrawledRepositoriesDefinitions = new ConcurrentHashMap<String, IHttpCrawledRepositorySetup>();

	public String[] getPatternSetLabels() {
		return patternSets.keySet().toArray(new String[] {});
	}

	public IPatternSet getPatternSetWithLabel(String label) {
		return label == null ? null : patternSets.get(label);
	}

	/**
	 * The constructor.
	 */
	public DWSCorePlugin() {
		super();
		if (plugin == null) {
			plugin = this;
		}
	}

	/**
	 * This method is called upon plug-in activation.
	 * 
	 * @param context
	 *            the context
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint patternSetEP = reg.getExtensionPoint(PATTERN_SET_EXTENSION_ID);
		IExtensionPoint repositoriesEP = reg.getExtensionPoint(REPOSITORY_DEFINITION_EXTENSION_ID);
		IExtensionPoint modelUpdateListenersEP = reg.getExtensionPoint(MODEL_LISTENERS_EXTENSION_ID);
		IFilter filter = createExtensionPointFilter(patternSetEP, repositoriesEP);
		extensionTracker = new ExtensionTracker(reg);
		extensionTracker.registerHandler(this, filter);
		IExtension[] extensions = patternSetEP.getExtensions();
		for (int i = 0; i < extensions.length; ++i)
			addExtension(extensionTracker, extensions[i]);

		extensions = repositoriesEP.getExtensions();
		for (int i = 0; i < extensions.length; ++i)
			addExtension(extensionTracker, extensions[i]);

		extensions = modelUpdateListenersEP.getExtensions();
		for (int i = 0; i < extensions.length; ++i)
			addExtension(extensionTracker, extensions[i]);

		ILog log = this.getLog();
		PluginLogAppender.setLog(log);
		// DOMConfigurator.configure(PlatformUtilsPlugin.getFile("log4j.xml").toURI().toURL());
		images = new Images();
		images.addImage(this, "icons/http_repository.gif", PluginImages.LOGO_MAVEN_HTTP_REPOSITORY_16);
		images.addImage(this, "icons/synchronize.gif", PluginImages.LOGO_MAVEN_SYNCHRONIZE_16);

		HashMap<String,Object> defaultPreferences = new HashMap<String,Object>();

		AlternativeConfigurations.process();
		ConfigurationsResolver configurationsResolver = new ConfigurationsResolver(new DefaultConfiguration(patternSets.get("Apache 2 parsing patterns")), AlternativeConfigurations.getAlternativeConfigurations());
		defaultPreferences.put(PreferencesNames.P_MAVEN_HTTP_REPOSITORIES_AUTOCOMPLETE, configurationsResolver.resolvePropertyWithMultipleValues(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_AUTOCOMPLETE_URL, ConfigurationConstants.PIPE_SEPARATOR));
		defaultPreferences.put(PreferencesNames.P_MAVEN_REPOSITORY_NAME_AUTOCOMPLETE, configurationsResolver.resolvePropertyWithMultipleValues(ConfigurationPropertiesConstants.REPOSITORY_DEFAULT_AUTOCOMPLETE_NAME, ConfigurationConstants.PIPE_SEPARATOR));
		defaultPreferences.put(PreferencesNames.P_MAVEN_FS_REPOSITORIES_AUTOCOMPLETE, configurationsResolver.resolvePropertyWithMultipleValues(ConfigurationPropertiesConstants.FS_REPOSITORY_DEFAULT_AUTOCOMPLETE_URL, ConfigurationConstants.PIPE_SEPARATOR));
		defaultPreferences.put(PreferencesNames.P_MAVEN_ENTRY_PATTERN_AUTOCOMPLETE, configurationsResolver.resolvePropertyWithMultipleValues(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_AUTOCOMPLETE_ENTRY_PATTERN, ConfigurationConstants.PIPE_SEPARATOR));
		defaultPreferences.put(PreferencesNames.P_MAVEN_PARENT_PATTERN_AUTOCOMPLETE, configurationsResolver.resolvePropertyWithMultipleValues(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_AUTOCOMPLETE_PARENT_PATTERN, ConfigurationConstants.PIPE_SEPARATOR));
		defaultPreferences.put(PreferencesNames.P_MAVEN_DIRECTORY_ENTRY_PATTERN_AUTOCOMPLETE, configurationsResolver.resolvePropertyWithMultipleValues(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_AUTOCOMPLETE_DIRECTORY_ENTRY_PATTERN, ConfigurationConstants.PIPE_SEPARATOR));
		defaultPreferences.put(PreferencesNames.P_MAVEN_FILE_ENTRY_PATTERN_AUTOCOMPLETE, configurationsResolver.resolvePropertyWithMultipleValues(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_AUTOCOMPLETE_FILE_ENTRY_PATTERN, ConfigurationConstants.PIPE_SEPARATOR));
		defaultPreferences.put(PreferencesNames.P_MAVEN_PROXY_HOST_AUTOCOMPLETE, configurationsResolver.resolvePropertyWithMultipleValues(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_AUTOCOMPLETE_PROXY_HOST, ConfigurationConstants.PIPE_SEPARATOR));
		defaultPreferences.put(PreferencesNames.P_MAVEN_PROXY_PORT_AUTOCOMPLETE, configurationsResolver.resolvePropertyWithMultipleValues(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_AUTOCOMPLETE_PROXY_PORT, ConfigurationConstants.PIPE_SEPARATOR));
		defaultPreferences.put(PreferencesNames.P_MAVEN_PATTERNSET_DEFAULT, configurationsResolver.resolveProperty(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_PATTERN_SET));
		defaultPreferences.put(PreferencesNames.P_MAVEN_REPOSITORY_LOCAL_PATH, configurationsResolver.resolveProperty(ConfigurationPropertiesConstants.HTTP_REPOSITORY_DEFAULT_LOCAL_PATH));
		defaultPreferences.put(PreferencesNames.P_MAVEN_POM_FILE_NAMES, configurationsResolver.resolvePropertyWithMultipleValues(ConfigurationPropertiesConstants.POM_FILE_NAMES, ConfigurationConstants.POM_FILES_SEPARATOR));
		defaultPreferences.put(PreferencesNames.P_MAVEN_POM_FILE_ENCODING, configurationsResolver.resolveProperty(ConfigurationPropertiesConstants.POM_FILE_ENCODING));
		defaultPreferences.put(PreferencesNames.P_MAVEN_DEFAULT_FOLDER, configurationsResolver.resolveProperty(ConfigurationPropertiesConstants.DOWNLOAD_TO_FOLDER));
		defaultPreferences.put(PreferencesNames.P_MAVEN_DEFAULT_WEBAPP_FOLDER, configurationsResolver.resolveProperty(ConfigurationPropertiesConstants.WEBAPP_LIBS_FOLDER));
		defaultPreferences.put(PreferencesNames.P_MAVEN_ARTIFACT_EXTENSIONS, configurationsResolver.resolvePropertyWithMultipleValues(ConfigurationPropertiesConstants.SCANNED_DEPENDENCIES, ConfigurationConstants.PIPE_SEPARATOR));
		defaultPreferences.put(PreferencesNames.P_MAVEN_DEAL_WITH_TRANSITIVE_DEPENDENCIES, new Boolean(configurationsResolver.resolveProperty(ConfigurationPropertiesConstants.DEAL_WITH_TRANSITIVE_DEPENDENCIES)));
		defaultPreferences.put(PreferencesNames.P_MAVEN_DEAL_WITH_DEPENDENCIES_OF_UNDETERMINED_OR_RESTRICTIVE_SCOPE, new Boolean(configurationsResolver.resolveProperty(ConfigurationPropertiesConstants.DEAL_WITH_UNDETERMINED_OR_RESTRICTIVE_SCOPE)));
		defaultPreferences.put(PreferencesNames.P_MAVEN_UNDETERMINED_OR_RESTRICTIVE_AUTOMATICALLY_ADD, new Boolean(configurationsResolver.resolveProperty(ConfigurationPropertiesConstants.UNDETERMINED_OR_RESTRICTIVE_SCOPE_AUTOMATICALLY_ADDED)));
		defaultPreferences.put(PreferencesNames.P_MAVEN_TRANSITIVE_DEPENDENCIES_AUTOMATICALLY_ADD, new Boolean(configurationsResolver.resolveProperty(ConfigurationPropertiesConstants.TRANSITIVE_AUTOMATICALLY_ADDED)));
		defaultPreferences.put(PreferencesNames.P_MAVEN_CONFLICTING_AUTOMATICALLY_REMOVE, new Boolean(configurationsResolver.resolveProperty(ConfigurationPropertiesConstants.AUTOMATICALLY_REMOVE_CONFLICTING)));
		defaultPreferences.put(PreferencesNames.P_MAVEN_CONSIDER_OPTIONAL_LIBRARIES, new Boolean(configurationsResolver.resolveProperty(ConfigurationPropertiesConstants.CONSIDER_OPTIONAL_LIBRARIES)));
		defaultPreferences.put(PreferencesNames.P_MAVEN_USE_LIBRARY_CONTAINER, new Boolean(configurationsResolver.resolveProperty(ConfigurationPropertiesConstants.USE_LIBRARY_CONTAINER)));
		defaultPreferences.put(PreferencesNames.P_MAVEN_VARIABLE_NAME, configurationsResolver.resolveProperty(ConfigurationPropertiesConstants.VARIABLE_NAME));
		defaultPreferences.put(PreferencesNames.P_MAVEN_FILTERED_LIBS, configurationsResolver.resolvePropertyWithMultipleValues(ConfigurationPropertiesConstants.WIZARDS_POM_FILTERED_LIBRARIES, ConfigurationConstants.PIPE_SEPARATOR));
		defaultPreferences.put(PreferencesNames.P_MAVEN_HIDE_APPROXIMATIVE_MATCH, configurationsResolver.resolveProperty(ConfigurationPropertiesConstants.WIZARDS_POM_HIDE_APPROXIMATIVE_MATCHES));
		defaultPreferences.put(PreferencesNames.P_MAVEN_NUMBER_OF_KEPT_MATCHES, configurationsResolver.resolveProperty(ConfigurationPropertiesConstants.WIZARDS_POM_NUMBER_OF_KEPT_MATCHES));

		defaultPreferences.put(PreferencesNames.P_MAVEN_REPOSITORIES_INFOS, "");// Do
		// Not
		// modify
		// this
		defaultPreferences.put(PreferencesNames.P_MAVEN_REPOSITORIES_MODEL, "");// Do
		// Not
		// modify
		// this

		PreferencesFacade.setDefaultValues(this, defaultPreferences);

		logger.debug("starting plugin :" + this.getClass().getName());
	}

	private IFilter createExtensionPointFilter(IExtensionPoint... extensionPoints) {
		IFilter filter = ExtensionTracker.createExtensionPointFilter(extensionPoints);
		return filter;
	}

	/**
	 * This method is called when the plug-in is stopped.
	 * 
	 * @param context
	 *            the context
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the default
	 */
	public static DWSCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns a file from a path relative to the plugin's installation path.
	 * 
	 * @param relativePath
	 *            the relative path from plugin's installation dir's root.
	 * 
	 * @return a java.io.File object.
	 */
	public static File getFile(String relativePath) {
		return PluginToolBox.getFileFromBundle(DWSCorePlugin.getDefault().getBundle(), relativePath);
	}

	/**
	 * Gets the images.
	 * 
	 * @return the images
	 */
	public Images getImages() {
		return images;
	}

	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		String extensionPointUniqueId = extension.getExtensionPointUniqueIdentifier();
		if (extensionPointUniqueId.equals(PATTERN_SET_EXTENSION_ID)) {
			IConfigurationElement[] configs = extension.getConfigurationElements();
			for (int i = 0; i < configs.length; ++i) {
				IConfigurationElement element = configs[i];
				if (element.getName().equals("pattern-set")) {
					String label = element.getAttribute("label");
					String entryPattern = "";
					String directoryPattern = "";
					String filePattern = "";
					String parentDirectoryPattern = "";
					IConfigurationElement[] children = element.getChildren();
					for (int j = 0; j < children.length; ++j) {
						IConfigurationElement child = children[j];
						if (child.getName().equals("entry-pattern")) {
							entryPattern = child.getValue();
						}
						if (child.getName().equals("directory-entry-pattern")) {
							directoryPattern = child.getValue();
						}
						if (child.getName().equals("file-entry-pattern")) {
							filePattern = child.getValue();
						}
						if (child.getName().equals("parent-directory-pattern")) {
							parentDirectoryPattern = child.getValue();
						}
					}
					ImmutablePatternSet patternSet = new ImmutablePatternSet(label, entryPattern, filePattern, directoryPattern, parentDirectoryPattern);
					patternSets.put(patternSet.getLabel(), patternSet);
					tracker.registerObject(extension, patternSet, IExtensionTracker.REF_WEAK);
				}
			}
		}
		if (extensionPointUniqueId.equals(REPOSITORY_DEFINITION_EXTENSION_ID)) {
			IConfigurationElement[] configs = extension.getConfigurationElements();
			for (int i = 0; i < configs.length; ++i) {
				IConfigurationElement element = configs[i];
				if (element.getName().equals("http-crawled-repository")) {
					String label = element.getAttribute("label");
					String patternExtensionPointId = element.getAttribute("pattern-extensionpoint-label");
					String baseUrl = element.getAttribute("base-url");
					String proxyHost = element.getAttribute("proxy-host");
					String proxyPortAttribute = element.getAttribute("proxy-port");
					Integer proxyPort = proxyPortAttribute == null ? null : Integer.valueOf(proxyPortAttribute);
					Set<String> groupFilters = new HashSet<String>();
					IConfigurationElement[] children = element.getChildren();
					if (children.length > 0) {
						IConfigurationElement groupFiltersElement = children[0];
						IConfigurationElement[] groupFilterElements = groupFiltersElement.getChildren();
						for (IConfigurationElement groupFilterElement : groupFilterElements) {
							groupFilters.add(groupFilterElement.getAttribute("pattern"));
						}
					}
					IPatternSet patternSet = new LabelPatternSet(patternExtensionPointId);
					IHttpCrawledRepositorySetup crawledRepositorySetup = new ImmutableHttpCrawledRepositorySetup(label, groupFilters, patternSet, baseUrl, proxyHost, proxyPort);
					this.httpCrawledRepositoriesDefinitions.put(label, crawledRepositorySetup);
					tracker.registerObject(extension, crawledRepositorySetup, IExtensionTracker.REF_WEAK);
				} else if (element.getName().equals("filesystem-crawled-repository")) {
					String label = element.getAttribute("label");
					String baseUrl = element.getAttribute("base-url");
					Set<String> groupFilters = new HashSet<String>();
					IConfigurationElement[] children = element.getChildren();
					if (children.length > 0) {
						IConfigurationElement groupFiltersElement = children[0];
						IConfigurationElement[] groupFilterElements = groupFiltersElement.getChildren();
						for (IConfigurationElement groupFilterElement : groupFilterElements) {
							groupFilters.add(groupFilterElement.getAttribute("pattern"));
						}
					}
					IFileSystemCrawledRepositorySetup crawledRepositorySetup = new ImmutableFileSystemCrawledRepositorySetup(label, groupFilters, baseUrl);
					this.filesystemCrawledRepositoriesDefinitions.put(label, crawledRepositorySetup);
					tracker.registerObject(extension, crawledRepositorySetup, IExtensionTracker.REF_WEAK);
				}
			}
		}
		if (extensionPointUniqueId.equals(MODEL_LISTENERS_EXTENSION_ID)) {
			IConfigurationElement[] configs = extension.getConfigurationElements();
			for (int i = 0; i < configs.length; ++i) {
				IConfigurationElement element = configs[i];
				if (element.getName().equals("model-update-listener")) {
					try {
						IModelUpdateListener modelUpdateListener = (IModelUpdateListener) element.createExecutableExtension("implementation");
						String id = element.getAttribute("id");
						this.repositoryModelUpdateListeners.put(id, modelUpdateListener);
						tracker.registerObject(extension, modelUpdateListener, IExtensionTracker.REF_WEAK);
					} catch (CoreException e) {
						e.printStackTrace();
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
	}

	private String getUniqueId(IExtension extension) {
		return extension.getUniqueIdentifier() == null ? extension.getSimpleIdentifier() : extension.getUniqueIdentifier();
	}

	public void removeExtension(IExtension extension, Object[] objects) {
		String extensionPointUniqueId = extension.getExtensionPointUniqueIdentifier();
		if (extensionPointUniqueId.equals(PATTERN_SET_EXTENSION_ID)) {
			for (int i = 0; i < objects.length; ++i) {
				ImmutablePatternSet patternSet = (ImmutablePatternSet) objects[i];
				if (patternSets != null && patternSets.containsKey(patternSet.getLabel())) {
					patternSets.remove(patternSet.getLabel());
				}
			}
		}
		if (extensionPointUniqueId.equals(REPOSITORY_DEFINITION_EXTENSION_ID)) {
			for (int i = 0; i < objects.length; ++i) {
				ICrawledRepositorySetup crawledRepositorySetup = (ICrawledRepositorySetup) objects[i];
				if (filesystemCrawledRepositoriesDefinitions != null && filesystemCrawledRepositoriesDefinitions.containsKey(crawledRepositorySetup.getId())) {
					filesystemCrawledRepositoriesDefinitions.remove(crawledRepositorySetup.getId());
				}
				if (httpCrawledRepositoriesDefinitions != null && httpCrawledRepositoriesDefinitions.containsKey(crawledRepositorySetup.getId())) {
					httpCrawledRepositoriesDefinitions.remove(crawledRepositorySetup.getId());
				}
			}
		}
		if (extensionPointUniqueId.equals(MODEL_LISTENERS_EXTENSION_ID)) {
			for (int i = 0; i < objects.length; ++i) {
				if (repositoryModelUpdateListeners != null && repositoryModelUpdateListeners.containsKey(getUniqueId(extension))) {
					repositoryModelUpdateListeners.remove(getUniqueId(extension));
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void notifyRepositoryModelUpdate(IModelItem modelItem) {
		for (IModelUpdateListener modelUpdateListener : repositoryModelUpdateListeners.values()) {
			modelUpdateListener.notifyModelUpdate(modelItem);
		}
	}

	public Set<String> getHttpRepositoryExtensionsLabels() {
		return httpCrawledRepositoriesDefinitions.keySet();
	}

	public Set<String> getFileSystemRepositoryExtensionsLabels() {
		return filesystemCrawledRepositoriesDefinitions.keySet();
	}

	public ICrawledRepositorySetup getRepositoryExtension(String label) {
		ICrawledRepositorySetup crawledRepositorySetup = filesystemCrawledRepositoriesDefinitions.get(label);
		crawledRepositorySetup = crawledRepositorySetup == null ? httpCrawledRepositoriesDefinitions.get(label) : crawledRepositorySetup;
		return crawledRepositorySetup;
	}
}
