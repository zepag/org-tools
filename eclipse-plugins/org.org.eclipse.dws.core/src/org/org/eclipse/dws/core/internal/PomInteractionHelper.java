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
package org.org.eclipse.dws.core.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.org.eclipse.core.utils.platform.binding.BindingException;
import org.org.eclipse.core.utils.platform.tools.IOToolBox;
import org.org.eclipse.dws.core.internal.bridges.ProjectInteractionHelper;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.eclipse.dws.core.internal.model.DWSClasspathEntryDescriptor;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.PomCreationDescription;
import org.org.eclipse.dws.core.internal.model.PomDependency;
import org.org.eclipse.dws.core.internal.model.visitors.DependenciesHarvester;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions.Filter;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions.ScopeFilter;
import org.org.eclipse.dws.core.internal.xml.IXmlPomFileBinder;
import org.org.eclipse.dws.core.internal.xml.StaxPomFileBinder;
import org.org.repository.crawler.items.ICrawledRepositorySetup;
import org.org.repository.crawler.items.IHttpCrawledRepositorySetup;
import org.org.repository.crawler.maven2.model.ArtifactVersion;

/**
 * The Class PomInteractionHelper.
 */
public final class PomInteractionHelper {

	/**
	 * Instantiates a new pom interaction helper.
	 */
	private PomInteractionHelper() {
	}

	/**
	 * The Class PomInteractionException.
	 */
	public static class PomInteractionException extends RuntimeException {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -2793937229997215054L;

		/**
		 * Instantiates a new pom interaction exception.
		 */
		public PomInteractionException() {
			super();
		}

		/**
		 * Instantiates a new pom interaction exception.
		 * 
		 * @param message
		 *            the message
		 * @param cause
		 *            the cause
		 */
		public PomInteractionException(String message, Throwable cause) {
			super(message, cause);
		}

		/**
		 * Instantiates a new pom interaction exception.
		 * 
		 * @param message
		 *            the message
		 */
		public PomInteractionException(String message) {
			super(message);
		}

		/**
		 * Instantiates a new pom interaction exception.
		 * 
		 * @param cause
		 *            the cause
		 */
		public PomInteractionException(Throwable cause) {
			super(cause);
		}

	}

	/**
	 * This method parses the.
	 * 
	 * @param pomStream
	 *            the pom stream
	 * 
	 * @return the parsed pom description
	 */
	public static Pom parsePom(InputStream pomStream) {
		IXmlPomFileBinder xmlPomFileBinder = new StaxPomFileBinder();
		Pom pom = null;
		try {
			pom = xmlPomFileBinder.parsePomFile(pomStream);
		} catch (BindingException e) {
			throw new PomInteractionException("The inputStream for the POM could not be streamed with Stax.", e);
		}
		return pom;
	}

	/**
	 * Adds the pom dependencies to pom.
	 * 
	 * @param pomStream
	 *            the pom stream
	 * @param pomDependencies
	 *            the pom dependencies
	 * @param pomEncoding
	 *            the pom encoding
	 * 
	 * @return the string
	 */
	public static String addPomDependenciesToPom(InputStream pomStream, Set<PomDependency> pomDependencies, String pomEncoding) {
		String pomContent = null;
		IXmlPomFileBinder xmlPomFileBinder = new StaxPomFileBinder();
		try {
			pomContent = xmlPomFileBinder.updatePom(pomStream, pomDependencies, pomEncoding);
		} catch (BindingException e) {
			throw new PomInteractionException("The inputStream for the POM could not be streamed with Stax.", e);
		}
		try {
			pomStream.close();
		} catch (IOException e) {
		}
		return pomContent;
	}

	/**
	 * Gets the pom stream.
	 * 
	 * @param crawledRepositorySetup
	 *            the repository setup
	 * @param pomURL
	 *            the pom url
	 * 
	 * @return the pom stream
	 */
	public static InputStream getPomStream(ICrawledRepositorySetup crawledRepositorySetup, URL pomURL) {
		InputStream result = null;
		try {
			URLConnection connection = null;
			if (crawledRepositorySetup instanceof IHttpCrawledRepositorySetup) {
				IHttpCrawledRepositorySetup httpCrawledRepositorySetup = (IHttpCrawledRepositorySetup) crawledRepositorySetup;
				if (httpCrawledRepositorySetup.getProxyHost() != null && !(httpCrawledRepositorySetup.getProxyHost().equals(""))) {
					final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpCrawledRepositorySetup.getProxyHost(), httpCrawledRepositorySetup.getProxyPort()));
					connection = pomURL.openConnection(proxy);
				} else {
					connection = pomURL.openConnection(IOToolBox.determineProxy(pomURL));
				}
			} else {
				connection = pomURL.openConnection(Proxy.NO_PROXY);
			}
			result = connection.getInputStream();
		} catch (IOException e) {
			throw new PomInteractionException("Impossible to get POM Stream.", e);
		}
		return result;
	}

	/**
	 * Checks if is classpath entry from same artifact.
	 * 
	 * @param classpathEntry
	 *            the classpath entry
	 * @param pomDependency
	 *            the pom dependency
	 * 
	 * @return true, if checks if is classpath entry from same artifact
	 */
	public static boolean isClasspathEntryFromSameArtifact(DWSClasspathEntryDescriptor classpathEntry, PomDependency pomDependency) {
		String pathToParse = classpathEntry.getPath();
		String artifactId = pomDependency.getArtifactId();
		Pattern pattern = Pattern.compile(".+" + artifactId + "-(.+).jar");
		Matcher matcher = pattern.matcher(pathToParse);
		return matcher.matches();
	}

	/**
	 * Extract version if any.
	 * 
	 * @param classpathEntry
	 *            the classpath entry
	 * @param pomDependency
	 *            the pom dependency
	 * 
	 * @return the string
	 */
	public static String extractVersionIfAny(DWSClasspathEntryDescriptor classpathEntry, PomDependency pomDependency) {
		String result = "";
		String pathToParse = classpathEntry.getPath();
		String artifactId = pomDependency.getArtifactId();
		Pattern pattern = Pattern.compile(".+" + artifactId + "-(.+).jar");
		Matcher matcher = pattern.matcher(pathToParse);
		if (matcher.matches()) {
			result = matcher.group(1);
			result = formatToSnapshotIfNecessary(result);
		} else {
			result = null;
		}
		return result;
	}

	/**
	 * Format to snapshot if necessary.
	 * 
	 * @param version
	 *            the version
	 * 
	 * @return the string
	 */
	public static String formatToSnapshotIfNecessary(String version) {
		if (version.contains("-")) {
			StringTokenizer tkz = new StringTokenizer(version, "-", false);
			version = tkz.nextToken() + "-SNAPSHOT";
		}
		return version;
	}

	/**
	 * Format pom contents.
	 * 
	 * @param pomContents
	 *            the pom contents
	 * 
	 * @return the string
	 */
	public static String formatPomContents(String pomContents) {
		return pomContents.replace("\t", "    ");
	}

	/**
	 * Gets the pom contents.
	 * 
	 * @param groupId
	 *            the group id
	 * @param artifactId
	 *            the artifact id
	 * @param version
	 *            the version
	 * @param packaging
	 *            the packaging
	 * 
	 * @return the pom contents
	 */
	public static String getPomContents(String groupId, String artifactId, String version, String packaging) {
		StringBuilder contents = new StringBuilder();
		contents.append("<?xml version=\"1.0\"?>\n");
		contents.append("<project>\n");
		contents.append("\t<modelVersion>4.0.0</modelVersion>\n");
		contents.append("\t<groupId>" + (groupId == null ? "" : groupId) + "</groupId>\n");
		contents.append("\t<artifactId>" + (artifactId == null ? "" : artifactId) + "</artifactId>\n");
		contents.append("\t<version>" + (version == null ? "" : version) + "</version>\n");
		contents.append("\t<packaging>" + (packaging == null ? "" : packaging) + "</packaging>\n");
		contents.append("\t<dependencies>\n");
		contents.append("\t\t<!-- insert your project's dependencies here-->\n");
		contents.append("\t\t");
		contents.append("\t</dependencies>\n");
		contents.append("</project>");
		return contents.toString();
	}

	/**
	 * Gets the pom contents.
	 * 
	 * @param pomCreationDescription
	 *            the pom creation description
	 * 
	 * @return the pom contents
	 */
	public static String getPomContents(PomCreationDescription pomCreationDescription) {
		StringBuilder contents = new StringBuilder();
		contents.append("<?xml version=\"1.0\"?>\n");
		contents.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\"> \n");
		contents.append("\t<modelVersion>4.0.0</modelVersion>\n");
		contents.append("\t<groupId>" + (pomCreationDescription.getGroupId() == null ? "" : pomCreationDescription.getGroupId()) + "</groupId>\n");
		contents.append("\t<artifactId>" + (pomCreationDescription.getArtifactId() == null ? "" : pomCreationDescription.getArtifactId()) + "</artifactId>\n");
		contents.append("\t<version>" + (pomCreationDescription.getVersion() == null ? "" : pomCreationDescription.getVersion()) + "</version>\n");
		contents.append("\t<packaging>" + (pomCreationDescription.getPackaging() == null ? "" : pomCreationDescription.getPackaging()) + "</packaging>\n");
		contents.append("\t<dependencies>\n");
		for (ArtifactVersion chosenDependency : pomCreationDescription.getChildren()) {
			contents.append(PomInteractionHelper.toDependencyXML(chosenDependency));
		}
		contents.append("\t\t");
		contents.append("\t\t<!-- insert your project's dependencies here-->\n");
		contents.append("\t\t");
		contents.append("\t</dependencies>\n");
		contents.append("</project>");
		return contents.toString();
	}

	/**
	 * Gets the exclusions.
	 * 
	 * @param transitiveDependency
	 *            the transitive dependency
	 * @param builder
	 *            the builder
	 * 
	 * @return the exclusions
	 */
	public static void getExclusions(PomDependency transitiveDependency, StringBuilder builder) {
		builder.append("<dependency>\n");
		builder.append("\t<groupId>" + transitiveDependency.getGroupId() + "</groupId>\n");
		builder.append("\t<artifactId>" + transitiveDependency.getArtifactId() + "</artifactId>\n");
		builder.append("\t<version>" + transitiveDependency.getVersion() + "</version>\n");
		if (transitiveDependency.getClassifier() != null) {
			builder.append("\t<classifier>" + transitiveDependency.getClassifier() + "</classifier>\n");
		}
		builder.append("\t<scope>compile</scope><!-- compile, test, provided,runtime or system-->\n");
		if (transitiveDependency.hasChildren()) {
			builder.append("\t<exclusions>\n");
			DependenciesHarvester dependenciesHarvester = new DependenciesHarvester(transitiveDependency);
			transitiveDependency.accept(dependenciesHarvester);
			Set<PomDependency> allTransitiveDependencies = new TreeSet<PomDependency>(new Comparator<PomDependency>() {

				public int compare(PomDependency o1, PomDependency o2) {
					String o1Str = o1.getGroupId() + o1.getArtifactId();
					String o2Str = o2.getGroupId() + o2.getArtifactId();
					return o1Str.compareTo(o2Str);
				}

			});
			allTransitiveDependencies.addAll(dependenciesHarvester.getDependencies());
			for (PomDependency pomDependency : allTransitiveDependencies) {
				builder.append("\t\t<exclusion>\n");
				builder.append("\t\t\t<groupId>" + pomDependency.getGroupId() + "</groupId>\n");
				builder.append("\t\t\t<artifactId>" + pomDependency.getArtifactId() + "</artifactId>\n");
				builder.append("\t\t<exclusions>\n");
			}
			builder.append("\t</exclusions>\n");
		} else {
			builder.append("\t<!-- no transitive dependencies: no exclusions -->\n");
		}
		builder.append("</dependency>\n");

	}

	/**
	 * Gets the description.
	 * 
	 * @param transitiveDependency
	 *            the transitive dependency
	 * @param buffer
	 *            the buffer
	 * @param tabs
	 *            the tabs
	 * 
	 * @return the description
	 */
	public static void getDescription(PomDependency transitiveDependency, StringBuilder buffer, StringBuilder tabs) {
		buffer.append(tabs.toString() + getDescription(transitiveDependency) + "\n");
		if (transitiveDependency.hasChildren()) {
			tabs.append("----");
			Set<PomDependency> orderedDependencies = new TreeSet<PomDependency>(new Comparator<PomDependency>() {
				public int compare(PomDependency pomDependency1, PomDependency pomDependency2) {
					int result = 0;
					if (pomDependency1.getScope() != null && pomDependency2.getScope() != null) {
						result = pomDependency1.getScope().compareTo(pomDependency2.getScope());
						if (result == 0) {
							result = pomDependency1.compareTo(pomDependency2);
						}
					} else {
						result = pomDependency1.compareTo(pomDependency2);
					}
					return result;
				}
			});
			orderedDependencies.addAll(transitiveDependency.getChildren());
			for (PomDependency dependency : orderedDependencies) {
				getDescription(dependency, buffer, tabs);
			}
		}
	}

	/**
	 * Gets the description.
	 * 
	 * @param transitiveDependency
	 *            the transitive dependency
	 * 
	 * @return the description
	 */
	private static Object getDescription(PomDependency transitiveDependency) {
		final String groupId = transitiveDependency.getGroupId();
		final String artifactId = transitiveDependency.getArtifactId();
		final String version = transitiveDependency.getVersion();
		final String scope = transitiveDependency.getScope() == null ? "[NO SCOPE DEFINED]" : transitiveDependency.getScope().name().toLowerCase();
		return groupId + ":" + artifactId + ":" + version + ":" + scope;
	}

	/**
	 * To dependency xml.
	 * 
	 * @param artifactVersion
	 *            the artifact version
	 * 
	 * @return the string
	 */
	public static String toDependencyXML(ArtifactVersion artifactVersion) {
		StringBuilder builder = new StringBuilder();
		builder.append("<dependency>\n");
		builder.append("\t<groupId>" + artifactVersion.getParent().getParent().getName() + "</groupId>\n");
		builder.append("\t<artifactId>" + artifactVersion.getParent().getId() + "</artifactId>\n");
		builder.append("\t<version>" + artifactVersion.getVersion() + "</version>\n");
		if (artifactVersion.getClassifier() != null) {
			builder.append("\t<classifier>" + artifactVersion.getClassifier() + "</classifier>\n");
		}
		builder.append("\t<scope>compile</scope><!-- compile, test, provided,runtime or system-->\n");
		builder.append("</dependency>\n");
		return builder.toString();
	}

	/**
	 * Gets the parsed pom description.
	 * 
	 * @param selectedFile
	 *            the selected file
	 * 
	 * @return the parsed pom description
	 * 
	 * @throws WizardInitException
	 *             the wizard init exception
	 */
	public static Pom getParsedPomDescription(IFile selectedFile) throws PomInteractionException {
		Pom pom;
		try {
			pom = parsePom(selectedFile.getContents());
		} catch (CoreException e) {
			throw new PomInteractionException(e);
		}
		return pom;
	}

	/**
	 * Prepare options.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the filtering options
	 */
	public static PomDependenciesFilteringOptions preparePomDependenciesFilteringOptions(IProject project) {
		PomDependenciesFilteringOptions.Builder optionsBuilder = new PomDependenciesFilteringOptions.Builder();
		optionsBuilder.projectClasspathEntries(ProjectInteractionHelper.getClasspathEntries(JavaCore.create(project)));
		optionsBuilder.scopeFilter(ScopeFilter.NONE);
		optionsBuilder.filter(Filter.NONE);
		optionsBuilder.dealWithTransitive(AggregatedProperties.getDealWithTransitive(project));
		optionsBuilder.dealWithOptional(AggregatedProperties.getDealWithOptional(project));
		optionsBuilder.dealWithNarrow(AggregatedProperties.getDealWithNarrow(project));
		optionsBuilder.skippedDependencies(AggregatedProperties.getSkippedDependencies(project));
		optionsBuilder.artifactExtensions(AggregatedProperties.getArtifactExtensions());
		PomDependenciesFilteringOptions filteringOptions = optionsBuilder.build();
		return filteringOptions;
	}

}
