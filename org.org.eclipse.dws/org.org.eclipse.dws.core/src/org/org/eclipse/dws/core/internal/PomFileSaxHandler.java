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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.org.eclipse.dws.core.internal.model.ParentPom;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.PomDependency;
import org.org.eclipse.dws.core.internal.model.PomRepository;
import org.org.eclipse.dws.core.internal.model.PomDependency.Scope;
import org.org.eclipse.dws.core.internal.xml.StaxPomFileBinder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The Class PomFileSaxHandler.
 * @deprecated prefer the use of the StaxPomFileBinder.
 * @see StaxPomFileBinder
 */
@Deprecated
public class PomFileSaxHandler extends DefaultHandler {

	/** The pom dependencies. */
	private List<PomDependency> pomDependencies = new ArrayList<PomDependency>();

	/** The last dependency. */
	private PomDependency lastDependency;

	/** The x path stack. */
	private Stack<String> xPathStack = new Stack<String>();

	/** The parent element. */
	private String parentElement = null;

	/** The group id. */
	private StringBuilder groupId;

	/** The artifact id. */
	private StringBuilder artifactId;

	/** The version. */
	private StringBuilder version;

	/** The description. */
	private StringBuilder description;

	/** The url. */
	private StringBuilder url;

	/** The parsed repositories descriptions. */
	private Set<PomRepository> parsedRepositoriesDescriptions = new LinkedHashSet<PomRepository>();

	/** The dependency group id. */
	private StringBuilder dependencyGroupId;

	/** The dependency artifact id. */
	private StringBuilder dependencyArtifactId;

	/** The dependency version. */
	private StringBuilder dependencyVersion;

	/** The dependency scope. */
	private StringBuilder dependencyScope;

	/** The dependency optional. */
	private StringBuilder dependencyOptional;

	/** The dependency classifier. */
	private StringBuilder dependencyClassifier;

	/** The dependency system path. */
	private StringBuilder dependencySystemPath;

	/** The parent pom description. */
	private ParentPom parentPom;

	/** The parent artifact id. */
	private StringBuilder parentArtifactId;

	/** The parent group id. */
	private StringBuilder parentGroupId;

	/** The parent version. */
	private StringBuilder parentVersion;

	/** The parent relative path. */
	private StringBuilder parentRelativePath;

	/** The last parsed repository description. */
	private PomRepository lastParsedRepositoryDescription;

	/** The repository id. */
	private StringBuilder repositoryId;

	/** The repository name. */
	private StringBuilder repositoryName;

	/** The repository url. */
	private StringBuilder repositoryUrl;

	/** The name. */
	private StringBuilder name;

	/** The properties. */
	private Map<String, String> properties;

	/** The property key. */
	private StringBuilder propertyKey;

	/** The property value. */
	private StringBuilder propertyValue;

	/** The parsed pom description. */
	private Pom pom;

	/** The packaging. */
	private StringBuilder packaging;

	/**
	 * Instantiates a new pom file sax handler.
	 */
	public PomFileSaxHandler() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		parentElement = xPathStack.empty() ? null : xPathStack.peek();
		xPathStack.push(qName);
		if (qName.equals(PomFileConstants.PROJECT_TAG)) {
			pom = new Pom();
		}
		if (qName.equals(PomFileConstants.GROUPID_TAG) && parentIs(PomFileConstants.PROJECT_TAG)) {
			groupId = new StringBuilder();
		}
		if (qName.equals(PomFileConstants.ARTIFACTID_TAG) && parentIs(PomFileConstants.PROJECT_TAG)) {
			artifactId = new StringBuilder();
		}
		if (qName.equals(PomFileConstants.VERSION_TAG) && parentIs(PomFileConstants.PROJECT_TAG)) {
			version = new StringBuilder();
		}
		if (qName.equals(PomFileConstants.DESCRIPTION_TAG) && parentIs(PomFileConstants.PROJECT_TAG)) {
			description = new StringBuilder();
		}
		if (qName.equals(PomFileConstants.PACKAGING_TAG) && parentIs(PomFileConstants.PROJECT_TAG)) {
			packaging = new StringBuilder();
		}
		if (qName.equals(PomFileConstants.URL_TAG) && parentIs(PomFileConstants.PROJECT_TAG)) {
			url = new StringBuilder();
		}
		if (qName.equals(PomFileConstants.REPOSITORY_TAG) || qName.equals(PomFileConstants.SNAPSHOT_REPOSITORY_TAG)) {
			lastParsedRepositoryDescription = new PomRepository();
		}
		if (qName.equals(PomFileConstants.ID_TAG) && (parentIs(PomFileConstants.REPOSITORY_TAG) || parentIs(PomFileConstants.SNAPSHOT_REPOSITORY_TAG))) {
			repositoryId = new StringBuilder();
		}
		if (qName.equals(PomFileConstants.NAME_TAG) && (parentIs(PomFileConstants.REPOSITORY_TAG) || parentIs(PomFileConstants.SNAPSHOT_REPOSITORY_TAG))) {
			repositoryName = new StringBuilder();
		}
		if (qName.equals(PomFileConstants.URL_TAG) && (parentIs(PomFileConstants.REPOSITORY_TAG) || parentIs(PomFileConstants.SNAPSHOT_REPOSITORY_TAG))) {
			repositoryUrl = new StringBuilder();
		}
		if (qName.equals(PomFileConstants.PARENT_TAG) && parentIs(PomFileConstants.PROJECT_TAG)) {
			parentPom = new ParentPom();
		}
		if (qName.equals(PomFileConstants.GROUPID_TAG) && parentIs(PomFileConstants.PARENT_TAG)) {
			parentGroupId = new StringBuilder();
		}
		if (qName.equals(PomFileConstants.ARTIFACTID_TAG) && parentIs(PomFileConstants.PARENT_TAG)) {
			parentArtifactId = new StringBuilder();
		}
		if (qName.equals(PomFileConstants.VERSION_TAG) && parentIs(PomFileConstants.PARENT_TAG)) {
			parentVersion = new StringBuilder();
		}
		if (qName.equals(PomFileConstants.RELATIVEPATH_TAG) && parentIs(PomFileConstants.PARENT_TAG)) {
			parentRelativePath = new StringBuilder();
		}
		if (qName.equals(PomFileConstants.DEPENDENCIES_TAG) && parentIs(PomFileConstants.PROJECT_TAG)) {
			pomDependencies = new ArrayList<PomDependency>();
		}
		if (qName.equals(PomFileConstants.DEPENDENCY_TAG) && parentIs(PomFileConstants.DEPENDENCIES_TAG)) {
			lastDependency = new PomDependency();
		}
		if (qName.equals(PomFileConstants.GROUPID_TAG) && parentIs(PomFileConstants.DEPENDENCY_TAG) && lastDependency != null) {
			dependencyGroupId = new StringBuilder("");
		}
		if (qName.equals(PomFileConstants.ARTIFACTID_TAG) && parentIs(PomFileConstants.DEPENDENCY_TAG) && lastDependency != null) {
			dependencyArtifactId = new StringBuilder("");
		}
		if (qName.equals(PomFileConstants.VERSION_TAG) && parentIs(PomFileConstants.DEPENDENCY_TAG) && lastDependency != null) {
			dependencyVersion = new StringBuilder("");
		}
		if (qName.equals(PomFileConstants.CLASSIFIER_TAG) && parentIs(PomFileConstants.DEPENDENCY_TAG) && lastDependency != null) {
			dependencyClassifier = new StringBuilder("");
		}
		if (qName.equals(PomFileConstants.OPTIONAL_TAG) && parentIs(PomFileConstants.DEPENDENCY_TAG) && lastDependency != null) {
			dependencyOptional = new StringBuilder("");
		}
		if (qName.equals(PomFileConstants.SCOPE_TAG) && parentIs(PomFileConstants.DEPENDENCY_TAG) && lastDependency != null) {
			dependencyScope = new StringBuilder("");
		}
		if (qName.equals(PomFileConstants.SYSTEMPATH_TAG) && parentIs(PomFileConstants.DEPENDENCY_TAG) && lastDependency != null) {
			dependencySystemPath = new StringBuilder("");
		}
		if (qName.equals(PomFileConstants.PROPERTIES_TAG)) {
			if (properties == null) {
				properties = new HashMap<String, String>();
			}
		}
		if (parentIs(PomFileConstants.PROPERTIES_TAG)) {
			propertyKey = new StringBuilder(new String(qName.getBytes()));
			propertyValue = new StringBuilder("");
		}
	}

	/**
	 * Gets the pom parsing description.
	 * 
	 * @return the pom parsing description
	 */
	public Pom getPomParsingDescription() {
		pom.setParentPom(parentPom);
		for (PomDependency pomDependency : pomDependencies) {
			pom.addChild(pomDependency);
		}
		if (parsedRepositoriesDescriptions != null) {
			pom.getRepositories().addRepositories(parsedRepositoriesDescriptions);
		}
		pom.getProperties().addProperties(properties);
		return pom;
	}

	/**
	 * Parent is.
	 * 
	 * @param tag
	 *            the tag
	 * 
	 * @return true, if successful
	 */
	private boolean parentIs(String tag) {
		return (parentElement != null) && parentElement.equals(tag);
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals(PomFileConstants.DEPENDENCY_TAG)) {
			validateDependencyData();
			lastDependency.setGroupId(dependencyGroupId.toString());
			lastDependency.setArtifactId(dependencyArtifactId.toString());
			lastDependency.setVersion(dependencyVersion.toString());
			if (dependencyClassifier != null) {
				lastDependency.setClassifier(dependencyClassifier.toString());
			}
			if (dependencyScope != null) {
				try {
					lastDependency.setScope(Scope.valueOf(dependencyScope.toString().toUpperCase()));
				} catch (IllegalArgumentException e) {
					lastDependency.setScope(Scope.OTHER);
				}
			} else {
				lastDependency.setScope(Scope.COMPILE);
			}
			if (dependencySystemPath != null) {
				lastDependency.setSystemPath(dependencySystemPath.toString());
			}
			if (dependencyOptional != null) {
				lastDependency.setOptional(Boolean.valueOf(dependencyOptional.toString()));
			}
			// findDependencyInRepositories(lastDependency);
			pomDependencies.add(lastDependency);
			lastDependency = null;
			dependencyGroupId = null;
			dependencyArtifactId = null;
			dependencyVersion = null;
			dependencyScope = null;
			dependencyClassifier = null;
			dependencyOptional = null;
		}
		if (qName.equals(PomFileConstants.PARENT_TAG)) {
			if (parentGroupId != null) {
				parentPom.setGroupId(parentGroupId.toString());
			}
			if (parentArtifactId != null) {
				parentPom.setArtifactId(parentArtifactId.toString());
			}
			if (parentVersion != null) {
				parentPom.setVersion(parentVersion.toString());
			}
			if (parentRelativePath != null) {
				parentPom.setRelativePath(parentRelativePath.toString());
			}
		}
		if (qName.equals(PomFileConstants.REPOSITORY_TAG) || qName.equals(PomFileConstants.SNAPSHOT_REPOSITORY_TAG)) {
			if (repositoryId != null) {
				lastParsedRepositoryDescription.setId(repositoryId.toString());
			}
			if (repositoryName != null) {
				lastParsedRepositoryDescription.setName(repositoryName.toString());
			}
			if (repositoryUrl != null) {
				lastParsedRepositoryDescription.setUrl(repositoryUrl.toString());
			}
			if (validateRepositoryData()) {
				parsedRepositoriesDescriptions.add(lastParsedRepositoryDescription);
			}
			lastParsedRepositoryDescription = null;
			repositoryId = null;
			repositoryName = null;
			repositoryUrl = null;
		}
		if (parentIs(PomFileConstants.PROPERTIES_TAG) && localName.equals(propertyKey)) {
			properties.put(propertyKey.toString(), propertyValue.toString());
			propertyKey = null;
			propertyValue = null;
		}
		if (parentIs(PomFileConstants.PROJECT_TAG)) {
			if (pom != null) {
				if (groupId != null) {
					pom.setGroupId(groupId.toString());
				}
				if (artifactId != null) {
					pom.setArtifactId(artifactId.toString());
				}
				if (version != null) {
					pom.setVersion(version.toString());
				}
				if (packaging != null) {
					pom.setPackaging(packaging.toString());
				}
				groupId = null;
				artifactId = null;
				version = null;
				packaging = null;
			}
		}
		xPathStack.pop();
	}

	/**
	 * Validate repository data.
	 * 
	 * @return true, if successful
	 */
	private boolean validateRepositoryData() {
		boolean result = true;
		if (lastParsedRepositoryDescription.getId() == null && lastParsedRepositoryDescription.getName() == null) {
			result = false;
		}
		if (lastParsedRepositoryDescription.getUrl() == null) {
			result = false;
		}
		return result;
	}

	/**
	 * Validate dependency data.
	 */
	private void validateDependencyData() {
		if (dependencyGroupId == null || dependencyGroupId.toString().equals("")) {
			throw new NullPointerException("groupId tag should be defined and not emptyfor every artifact (" + dependencyGroupId + ":" + dependencyArtifactId + ":" + dependencyVersion);
		}
		if (dependencyArtifactId == null || dependencyArtifactId.toString().equals("")) {
			throw new NullPointerException("artifactId tag should be defined and not empty for every artifact(" + dependencyGroupId + ":" + dependencyArtifactId + ":" + dependencyVersion);
		}
		if (dependencyVersion == null || dependencyVersion.toString().equals("")) {
			if (dependencyVersion == null) {
				dependencyVersion = new StringBuilder();
			}
			dependencyVersion.append("[0.0.0,)");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	/**
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (!xPathStack.empty()) {
			String tagContent = new String(ch, start, length).trim();
			String tagName = xPathStack.peek();
			if (tagName.equals(PomFileConstants.ID_TAG) && (parentIs(PomFileConstants.REPOSITORY_TAG) || parentIs(PomFileConstants.SNAPSHOT_REPOSITORY_TAG)) && lastParsedRepositoryDescription != null) {
				repositoryId.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.NAME_TAG) && (parentIs(PomFileConstants.REPOSITORY_TAG) || parentIs(PomFileConstants.SNAPSHOT_REPOSITORY_TAG)) && lastParsedRepositoryDescription != null) {
				repositoryName.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.URL_TAG) && (parentIs(PomFileConstants.REPOSITORY_TAG) || parentIs(PomFileConstants.SNAPSHOT_REPOSITORY_TAG)) && lastParsedRepositoryDescription != null) {
				repositoryUrl.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.GROUPID_TAG) && parentIs(PomFileConstants.PROJECT_TAG) && groupId != null) {
				groupId.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.ARTIFACTID_TAG) && parentIs(PomFileConstants.PROJECT_TAG) && artifactId != null) {
				artifactId.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.VERSION_TAG) && parentIs(PomFileConstants.PROJECT_TAG) && version != null) {
				version.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.DESCRIPTION_TAG) && parentIs(PomFileConstants.PROJECT_TAG) && description != null) {
				description.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.PACKAGING_TAG) && parentIs(PomFileConstants.PROJECT_TAG) && packaging != null) {
				packaging.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.URL_TAG) && parentIs(PomFileConstants.PROJECT_TAG) && url != null) {
				url.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.NAME_TAG) && parentIs(PomFileConstants.PROJECT_TAG) && name != null) {
				name.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.GROUPID_TAG) && parentIs(PomFileConstants.PARENT_TAG) && parentPom != null) {
				parentGroupId.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.ARTIFACTID_TAG) && parentIs(PomFileConstants.PARENT_TAG) && parentPom != null) {
				parentArtifactId.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.VERSION_TAG) && parentIs(PomFileConstants.PARENT_TAG) && parentPom != null) {
				parentVersion.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.RELATIVEPATH_TAG) && parentIs(PomFileConstants.PARENT_TAG) && parentPom != null) {
				parentRelativePath.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.GROUPID_TAG) && parentIs(PomFileConstants.DEPENDENCY_TAG) && lastDependency != null && lastDependency.getGroupId() == null) {
				dependencyGroupId.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.ARTIFACTID_TAG) && parentIs(PomFileConstants.DEPENDENCY_TAG) && lastDependency != null && lastDependency.getArtifactId() == null) {
				dependencyArtifactId.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.VERSION_TAG) && parentIs(PomFileConstants.DEPENDENCY_TAG) && lastDependency != null && lastDependency.getVersion() == null) {
				dependencyVersion.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.OPTIONAL_TAG) && parentIs(PomFileConstants.DEPENDENCY_TAG) && lastDependency != null) {
				dependencyOptional.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.CLASSIFIER_TAG) && parentIs(PomFileConstants.DEPENDENCY_TAG) && lastDependency != null) {
				dependencyClassifier.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.SCOPE_TAG) && parentIs(PomFileConstants.DEPENDENCY_TAG) && lastDependency != null && lastDependency.getScope() == null) {
				dependencyScope.append(tagContent);
			}
			if (tagName.equals(PomFileConstants.SYSTEMPATH_TAG) && parentIs(PomFileConstants.DEPENDENCY_TAG) && lastDependency != null && lastDependency.getSystemPath() == null) {
				dependencySystemPath.append(tagContent);
			}
			if (parentIs(PomFileConstants.PROPERTIES_TAG) && propertyKey != null) {
				propertyValue.append(tagContent);
			}
		}
	}

}