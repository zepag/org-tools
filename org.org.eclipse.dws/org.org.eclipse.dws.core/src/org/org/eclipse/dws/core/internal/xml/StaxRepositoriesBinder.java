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
package org.org.eclipse.dws.core.internal.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.org.eclipse.core.utils.platform.binding.BindingException;
import org.org.model.IModelItem;
import org.org.model.IModelItemAdvancedVisitor;
import org.org.repository.crawler.items.IFileSystemCrawledRepositorySetup;
import org.org.repository.crawler.items.IHttpCrawledRepositorySetup;
import org.org.repository.crawler.items.mutable.FileSystemCrawledRepositorySetup;
import org.org.repository.crawler.items.mutable.HttpCrawledRepositorySetup;
import org.org.repository.crawler.items.mutable.PatternSet;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.Group;
import org.org.repository.crawler.maven2.model.CrawledRepository;
import org.org.repository.crawler.maven2.model.ArtifactVersion.Type;

/**
 * The Class StaxRepositoriesBinder.
 */
public class StaxRepositoriesBinder implements IXmlRepositoriesBinder {

	/**
	 * The Class LibraryPackCreationVisitor.
	 */
	public class RepositoryCreationVisitor implements IModelItemAdvancedVisitor {

		/** The parser. */
		private XMLEventWriter parser;

		/** The event factory. */
		private XMLEventFactory eventFactory;

		/** The occured exception. */
		private XMLStreamException occuredException;

		/**
		 * Instantiates a new repository creation visitor.
		 * 
		 * @param parser
		 *            the parser
		 * @param eventFactory
		 *            the event factory
		 */
		public RepositoryCreationVisitor(XMLEventWriter parser, XMLEventFactory eventFactory) {
			this.parser = parser;
			this.eventFactory = eventFactory;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		/**
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		@SuppressWarnings("rawtypes")
		public boolean visit(IModelItem modelItem) {
			boolean keepOnVisiting = true;
			try {
				if (modelItem instanceof CrawledRepository) {
					CrawledRepository crawledRepository = (CrawledRepository) modelItem;
					List<Attribute> attributes = new LinkedList<Attribute>();
					createAttribute(attributes, REPOSITORY_TAG_ID_ATTRIBUTE, crawledRepository.getLabel());
					createAttribute(attributes, REPOSITORY_TAG_GROUPFILTERS_ATTRIBUTE, serializeGroupFilters(crawledRepository.getRepositorySetup().getGroupFilters()));
					if (crawledRepository.getRepositorySetup() instanceof IHttpCrawledRepositorySetup) {
						IHttpCrawledRepositorySetup httpCrawledRepositorySetup = (IHttpCrawledRepositorySetup) crawledRepository.getRepositorySetup();
						createAttribute(attributes, REPOSITORY_TAG_TYPE_ATTRIBUTE, REPOSITORY_TAG_TYPE_ATTRIBUTE_HTTP_VALUE);
						createAttribute(attributes, REPOSITORY_TAG_BASEURL_ATTRIBUTE, httpCrawledRepositorySetup.getBaseUrl());
						if (httpCrawledRepositorySetup.getProxyHost() != null) {
							createAttribute(attributes, REPOSITORY_TAG_PROXYHOST_ATTRIBUTE, httpCrawledRepositorySetup.getProxyHost());
							createAttribute(attributes, REPOSITORY_TAG_PROXYPORT_ATTRIBUTE, "" + httpCrawledRepositorySetup.getProxyPort());
						}
						if (httpCrawledRepositorySetup.getPatternSet().getEntryPattern() != null) {
							createAttribute(attributes, REPOSITORY_TAG_ENTRYPATTERN_ATTRIBUTE, httpCrawledRepositorySetup.getPatternSet().getEntryPattern());
						}
						if (httpCrawledRepositorySetup.getPatternSet().getParentDirectoryPattern() != null) {
							createAttribute(attributes, REPOSITORY_TAG_PARENTPATTERN_ATTRIBUTE, httpCrawledRepositorySetup.getPatternSet().getParentDirectoryPattern());
						}
						if (httpCrawledRepositorySetup.getPatternSet().getFileEntryPattern() != null) {
							createAttribute(attributes, REPOSITORY_TAG_FILEPATTERN_ATTRIBUTE, httpCrawledRepositorySetup.getPatternSet().getFileEntryPattern());
						}
						if (httpCrawledRepositorySetup.getPatternSet().getDirectoryEntryPattern() != null) {
							createAttribute(attributes, REPOSITORY_TAG_DIRECTORYPATTERN_ATTRIBUTE, httpCrawledRepositorySetup.getPatternSet().getDirectoryEntryPattern());
						}
					} else if (crawledRepository.getRepositorySetup() instanceof IFileSystemCrawledRepositorySetup) {
						IFileSystemCrawledRepositorySetup fileSystemCrawledRepositorySetup = (IFileSystemCrawledRepositorySetup) crawledRepository.getRepositorySetup();
						createAttribute(attributes, REPOSITORY_TAG_TYPE_ATTRIBUTE, REPOSITORY_TAG_TYPE_ATTRIBUTE_FILESYSTEM_VALUE);
						createAttribute(attributes, REPOSITORY_TAG_BASEURL_ATTRIBUTE, fileSystemCrawledRepositorySetup.getBasePath());
					}
					parser.add(eventFactory.createStartElement(new QName(REPOSITORY_TAG), attributes.iterator(), null));
				}
				if (modelItem instanceof Group) {
					Group group = (Group) modelItem;
					Set<Attribute> attributes = new HashSet<Attribute>();
					createAttribute(attributes, GROUP_TAG_NAME_ATTRIBUTE, group.getName());
					parser.add(eventFactory.createStartElement(new QName(GROUP_TAG), attributes.iterator(), null));
				}
				if (modelItem instanceof Artifact) {
					Artifact artifact = (Artifact) modelItem;
					Set<Attribute> attributes = new HashSet<Attribute>();
					createAttribute(attributes, ARTIFACT_TAG_ID_ATTRIBUTE, artifact.getId());
					parser.add(eventFactory.createStartElement(new QName(ARTIFACT_TAG), attributes.iterator(), null));
				}
				if (modelItem instanceof ArtifactVersion) {
					ArtifactVersion artifactVersion = (ArtifactVersion) modelItem;
					Set<Attribute> attributes = new HashSet<Attribute>();
					createAttribute(attributes, ARTIFACTVERSION_TAG_ID_ATTRIBUTE, artifactVersion.getId());
					if (artifactVersion.getType() != null) {
						createAttribute(attributes, ARTIFACTVERSION_TAG_TYPE_ATTRIBUTE, artifactVersion.getType().name());
					} else {
						createAttribute(attributes, ARTIFACTVERSION_TAG_TYPE_ATTRIBUTE, Type.LIBRARY.name());
					}
					createAttribute(attributes, ARTIFACTVERSION_TAG_VERSION_ATTRIBUTE, artifactVersion.getVersion());
					if (artifactVersion.getClassifier() != null) {
						createAttribute(attributes, ARTIFACTVERSION_TAG_CLASSIFIER_ATTRIBUTE, artifactVersion.getVersion());
					}
					createAttribute(attributes, ARTIFACTVERSION_TAG_URL_ATTRIBUTE, artifactVersion.getUrl().toExternalForm());
					if (artifactVersion.getPomUrl() != null) {
						createAttribute(attributes, ARTIFACTVERSION_TAG_POM_URL_ATTRIBUTE, artifactVersion.getPomUrl().toExternalForm());
					}
					if (artifactVersion.getSourcesUrl() != null) {
						createAttribute(attributes, ARTIFACTVERSION_TAG_SOURCES_URL_ATTRIBUTE, artifactVersion.getSourcesUrl().toExternalForm());
					}
					if (artifactVersion.getJavadocUrl() != null) {
						createAttribute(attributes, ARTIFACTVERSION_TAG_JAVADOC_URL_ATTRIBUTE, artifactVersion.getJavadocUrl().toExternalForm());
					}
					StartElement artifactVersionElement = eventFactory.createStartElement(new QName(ARTIFACTVERSION_TAG), attributes.iterator(), null);
					parser.add(artifactVersionElement);
				}
			} catch (XMLStreamException e) {
				this.occuredException = e;
				keepOnVisiting = false;
			}
			return keepOnVisiting;
		}

		/**
		 * Creates the attribute.
		 * 
		 * @param attributes
		 *            the attributes
		 * @param attributeName
		 *            the attribute name
		 * @param attributeValue
		 *            the attribute value
		 */
		private void createAttribute(Collection<Attribute> attributes, String attributeName, String attributeValue) {
			attributes.add(eventFactory.createAttribute(new QName(attributeName), attributeValue));
		}

		/**
		 * Gets the occured exception.
		 * 
		 * @return the occured exception
		 */
		public XMLStreamException getOccuredException() {
			return occuredException;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.model.IModelItemAdvancedVisitor#aftervisit(org.org.model.IModelItem, boolean)
		 */
		/**
		 * @see org.org.model.IModelItemAdvancedVisitor#aftervisit(org.org.model.IModelItem, boolean)
		 */
		@SuppressWarnings("rawtypes")
		public void aftervisit(IModelItem modelItem, boolean shouldContinue) {
			try {
				if (modelItem instanceof CrawledRepository) {
					parser.add(eventFactory.createEndElement(new QName(REPOSITORY_TAG), null));
				}
				if (modelItem instanceof Group) {
					parser.add(eventFactory.createEndElement(new QName(GROUP_TAG), null));
				}
				if (modelItem instanceof Artifact) {
					parser.add(eventFactory.createEndElement(new QName(ARTIFACT_TAG), null));
				}
				if (modelItem instanceof ArtifactVersion) {
					parser.add(eventFactory.createEndElement(new QName(ARTIFACTVERSION_TAG), null));
				}
			} catch (XMLStreamException e) {
				this.occuredException = e;
			}
		}
	}

	/**
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlRepositoriesBinder#parseXmlDefinition(java.io.InputStream)
	 */
	public CrawledRepository parseXmlDefinition(InputStream inputStream) throws BindingException {
		CrawledRepository result = null;
		XMLInputFactory factory = XMLInputFactory.newInstance();
		try {
			XMLEventReader parser = factory.createXMLEventReader(inputStream);
			CrawledRepository tmpRepository = null;
			Group tmpGroup = null;
			Artifact tmpArtifact = null;
			ArtifactVersion tmpArtifactVersion = null;
			while (true) {
				XMLEvent event = parser.nextEvent();
				if (event.isEndDocument()) {
					parser.close();
					break;
				}
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					String elementNameLocalPart = startElement.getName().getLocalPart();
					if (elementNameLocalPart.equals(REPOSITORY_TAG)) {
						String id = null;
						String type = null;
						String baseUrl = null;
						String groupFilters = null;
						String proxyHost = null;
						String proxyPort = null;
						String entryPattern = null;
						String parentDirectoryPattern = null;
						String fileEntryPattern = null;
						String directoryEntryPattern = null;
						for (Iterator<?> it = startElement.getAttributes(); it.hasNext();) {
							Attribute attribute = (Attribute) it.next();
							id = getAttributeValue(id, REPOSITORY_TAG_ID_ATTRIBUTE, attribute);
							type = getAttributeValue(type, REPOSITORY_TAG_TYPE_ATTRIBUTE, attribute);
							baseUrl = getAttributeValue(baseUrl, REPOSITORY_TAG_BASEURL_ATTRIBUTE, attribute);
							groupFilters = getAttributeValue(groupFilters, REPOSITORY_TAG_GROUPFILTERS_ATTRIBUTE, attribute);
							proxyHost = getAttributeValue(proxyHost, REPOSITORY_TAG_PROXYHOST_ATTRIBUTE, attribute);
							proxyPort = getAttributeValue(proxyPort, REPOSITORY_TAG_PROXYPORT_ATTRIBUTE, attribute);
							entryPattern = getAttributeValue(entryPattern, REPOSITORY_TAG_ENTRYPATTERN_ATTRIBUTE, attribute);
							parentDirectoryPattern = getAttributeValue(parentDirectoryPattern, REPOSITORY_TAG_PARENTPATTERN_ATTRIBUTE, attribute);
							fileEntryPattern = getAttributeValue(fileEntryPattern, REPOSITORY_TAG_FILEPATTERN_ATTRIBUTE, attribute);
							directoryEntryPattern = getAttributeValue(directoryEntryPattern, REPOSITORY_TAG_DIRECTORYPATTERN_ATTRIBUTE, attribute);
						}
						if (type != null && type.equals(REPOSITORY_TAG_TYPE_ATTRIBUTE_HTTP_VALUE)) {
							HttpCrawledRepositorySetup httpCrawledRepositorySetup = new HttpCrawledRepositorySetup(baseUrl, parseGroupFiltersAttribute(groupFilters));
							if (proxyHost != null && proxyPort != null && isInteger(proxyPort)) {
								httpCrawledRepositorySetup.setProxyHost(proxyHost);
								httpCrawledRepositorySetup.setProxyPort(Integer.parseInt(proxyPort));
							}
							PatternSet patternSet = httpCrawledRepositorySetup.getPatternSet().getMutable();
							if (entryPattern != null) {
								patternSet.setEntryPattern(entryPattern);
							}
							if (parentDirectoryPattern != null) {
								patternSet.setParentDirectoryPattern(parentDirectoryPattern);
							}
							if (fileEntryPattern != null) {
								patternSet.setFileEntryPattern(fileEntryPattern);
							}
							if (directoryEntryPattern != null) {
								patternSet.setDirectoryEntryPattern(directoryEntryPattern);
							}
							httpCrawledRepositorySetup.setPatternSet(patternSet);
							tmpRepository = new CrawledRepository(id, httpCrawledRepositorySetup);
						} else if (type != null && type.equals(REPOSITORY_TAG_TYPE_ATTRIBUTE_FILESYSTEM_VALUE)) {
							FileSystemCrawledRepositorySetup fileSystemCrawledRepositorySetup = new FileSystemCrawledRepositorySetup(baseUrl);
							fileSystemCrawledRepositorySetup.setGroupFilters(parseGroupFiltersAttribute(groupFilters));
							tmpRepository = new CrawledRepository(id, fileSystemCrawledRepositorySetup);
						}
						continue;
					}
					if (elementNameLocalPart.equals(GROUP_TAG)) {
						String name = null;
						for (Iterator<?> it = startElement.getAttributes(); it.hasNext();) {
							Attribute attribute = (Attribute) it.next();
							name = getAttributeValue(name, GROUP_TAG_NAME_ATTRIBUTE, attribute);
						}
						tmpGroup = new Group(name);
						continue;
					}
					if (elementNameLocalPart.equals(ARTIFACT_TAG)) {
						String id = null;
						for (Iterator<?> it = startElement.getAttributes(); it.hasNext();) {
							Attribute attribute = (Attribute) it.next();
							id = getAttributeValue(id, ARTIFACT_TAG_ID_ATTRIBUTE, attribute);
						}
						tmpArtifact = new Artifact(id);
					}
					if (elementNameLocalPart.equals(ARTIFACTVERSION_TAG)) {
						String id = null;
						String type = null;
						String url = null;
						String pomUrl = null;
						String sourcesUrl = null;
						String javadocUrl = null;
						String version = null;
						String classifier = null;
						for (Iterator<?> it = startElement.getAttributes(); it.hasNext();) {
							Attribute attribute = (Attribute) it.next();
							id = getAttributeValue(id, ARTIFACTVERSION_TAG_ID_ATTRIBUTE, attribute);
							type = getAttributeValue(type, ARTIFACTVERSION_TAG_TYPE_ATTRIBUTE, attribute);
							url = getAttributeValue(url, ARTIFACTVERSION_TAG_URL_ATTRIBUTE, attribute);
							pomUrl = getAttributeValue(pomUrl, ARTIFACTVERSION_TAG_POM_URL_ATTRIBUTE, attribute);
							sourcesUrl = getAttributeValue(sourcesUrl, ARTIFACTVERSION_TAG_SOURCES_URL_ATTRIBUTE, attribute);
							javadocUrl = getAttributeValue(javadocUrl, ARTIFACTVERSION_TAG_JAVADOC_URL_ATTRIBUTE, attribute);
							version = getAttributeValue(version, ARTIFACTVERSION_TAG_VERSION_ATTRIBUTE, attribute);
							classifier = getAttributeValue(classifier, ARTIFACTVERSION_TAG_CLASSIFIER_ATTRIBUTE, attribute);
						}
						tmpArtifactVersion = new ArtifactVersion();
						tmpArtifactVersion.setId(id);
						try {
							tmpArtifactVersion.setType(ArtifactVersion.Type.valueOf(type));
						} catch (Exception exception) {
							// FOR PREVIOUS VERSIONS' COMPATIBILITY, CONSIDER ANY PREVIOUS ENTRY IS OF LIBRARY TYPE.
							tmpArtifactVersion.setType(Type.LIBRARY);
						}
						try {
							tmpArtifactVersion.setUrl(new URL(url));
						} catch (MalformedURLException exception) {
							// just ignore...
						}
						try {
							tmpArtifactVersion.setPomUrl(new URL(pomUrl));
						} catch (MalformedURLException exception) {
							// just ignore...
						}
						try {
							tmpArtifactVersion.setSourcesUrl(new URL(sourcesUrl));
						} catch (MalformedURLException exception) {
							// just ignore...
						}
						try {
							tmpArtifactVersion.setJavadocUrl(new URL(javadocUrl));
						} catch (MalformedURLException exception) {
							// just ignore...
						}
						tmpArtifactVersion.setVersion(version);
						tmpArtifactVersion.setClassifier(classifier);
					}
				}
				if (event.isEndElement()) {
					EndElement endElement = event.asEndElement();
					String elementNameLocalPart = endElement.getName().getLocalPart();
					if (elementNameLocalPart.equals(REPOSITORY_TAG)) {
						if (tmpRepository != null) {
							result = tmpRepository;
						}
						tmpRepository = null;
						continue;
					}
					if (elementNameLocalPart.equals(GROUP_TAG)) {
						if (tmpGroup != null) {
							tmpGroup.setParent(tmpRepository);
						}
						tmpGroup = null;
						continue;
					}
					if (elementNameLocalPart.equals(ARTIFACT_TAG)) {
						if (tmpArtifact != null) {
							tmpArtifact.setParent(tmpGroup);
						}
						tmpArtifact = null;
						continue;
					}
					if (elementNameLocalPart.equals(ARTIFACTVERSION_TAG)) {
						if (tmpArtifactVersion != null) {
							tmpArtifactVersion.setParent(tmpArtifact);
						}
						tmpArtifactVersion = null;
						continue;
					}
				}
			}
		} catch (XMLStreamException e) {
			throw new BindingException(e);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlRepositoriesBinder#parseXmlDefinition(java.lang.String)
	 */
	/**
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlRepositoriesBinder#parseXmlDefinition(java.lang.String)
	 */
	public CrawledRepository parseXmlDefinition(String input) throws BindingException {
		InputStream inputStream = new ByteArrayInputStream(input.getBytes());
		return parseXmlDefinition(inputStream);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlRepositoriesBinder#toXmlDefinition(org.org.repository.crawler.maven2.model.CrawledRepository, java.io.OutputStream)
	 */
	/**
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlRepositoriesBinder#toXmlDefinition(org.org.repository.crawler.maven2.model.CrawledRepository, java.io.OutputStream)
	 */
	public void toXmlDefinition(CrawledRepository crawledRepository, OutputStream outputStream) throws BindingException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		try {
			XMLEventWriter parser = factory.createXMLEventWriter(outputStream);
			parser.add(eventFactory.createStartDocument(UTF_8, XML_VERSION_1_0));
			RepositoryCreationVisitor repositoryCreationVisitor = new RepositoryCreationVisitor(parser, eventFactory);
			crawledRepository.accept(repositoryCreationVisitor);
			if (repositoryCreationVisitor.getOccuredException() != null) {
				throw repositoryCreationVisitor.getOccuredException();
			}
			parser.add(eventFactory.createEndDocument());
		} catch (XMLStreamException e) {
			throw new BindingException(e);
		}
	}

	/**
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlRepositoriesBinder#toXmlDefinition(org.org.repository.crawler.maven2.model.CrawledRepository)
	 */
	public String toXmlDefinition(CrawledRepository crawledRepository) throws BindingException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		toXmlDefinition(crawledRepository, outputStream);
		return new String(outputStream.toByteArray());
	}

	/**
	 * Serialize group filters.
	 * 
	 * @param groupFilters
	 *            the group filters
	 * 
	 * @return the string
	 */
	private String serializeGroupFilters(Set<String> groupFilters) {
		StringBuilder result = new StringBuilder();
		for (String groupFilter : groupFilters) {
			result.append(groupFilter + "|");
		}
		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlRepositoriesBinder#parseXmlRepositories(java.io.InputStream)
	 */
	/**
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlRepositoriesBinder#parseXmlRepositories(java.io.InputStream)
	 */
	@SuppressWarnings("rawtypes")
	public List<CrawledRepository> parseXmlRepositories(InputStream inputStream) throws BindingException {
		List<CrawledRepository> result = new ArrayList<CrawledRepository>();
		XMLInputFactory factory = XMLInputFactory.newInstance();
		try {
			XMLEventReader parser = factory.createXMLEventReader(inputStream);
			CrawledRepository tmpRepository = null;
			Group tmpGroup = null;
			Artifact tmpArtifact = null;
			ArtifactVersion tmpArtifactVersion = null;
			while (true) {
				XMLEvent event = parser.nextEvent();
				if (event.isEndDocument()) {
					parser.close();
					break;
				}
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					String elementNameLocalPart = startElement.getName().getLocalPart();
					if (elementNameLocalPart.equals(REPOSITORY_TAG)) {
						String id = null;
						String type = null;
						String baseUrl = null;
						String groupFilters = null;
						String proxyHost = null;
						String proxyPort = null;
						String entryPattern = null;
						String parentDirectoryPattern = null;
						String fileEntryPattern = null;
						String directoryEntryPattern = null;
						for (Iterator it = startElement.getAttributes(); it.hasNext();) {
							Attribute attribute = (Attribute) it.next();
							id = getAttributeValue(id, REPOSITORY_TAG_ID_ATTRIBUTE, attribute);
							type = getAttributeValue(type, REPOSITORY_TAG_TYPE_ATTRIBUTE, attribute);
							baseUrl = getAttributeValue(baseUrl, REPOSITORY_TAG_BASEURL_ATTRIBUTE, attribute);
							groupFilters = getAttributeValue(groupFilters, REPOSITORY_TAG_GROUPFILTERS_ATTRIBUTE, attribute);
							proxyHost = getAttributeValue(proxyHost, REPOSITORY_TAG_PROXYHOST_ATTRIBUTE, attribute);
							proxyPort = getAttributeValue(proxyPort, REPOSITORY_TAG_PROXYPORT_ATTRIBUTE, attribute);
							entryPattern = getAttributeValue(entryPattern, REPOSITORY_TAG_ENTRYPATTERN_ATTRIBUTE, attribute);
							parentDirectoryPattern = getAttributeValue(parentDirectoryPattern, REPOSITORY_TAG_PARENTPATTERN_ATTRIBUTE, attribute);
							fileEntryPattern = getAttributeValue(fileEntryPattern, REPOSITORY_TAG_FILEPATTERN_ATTRIBUTE, attribute);
							directoryEntryPattern = getAttributeValue(directoryEntryPattern, REPOSITORY_TAG_DIRECTORYPATTERN_ATTRIBUTE, attribute);
						}
						if (type != null && type.equals(REPOSITORY_TAG_TYPE_ATTRIBUTE_HTTP_VALUE)) {
							HttpCrawledRepositorySetup httpCrawledRepositorySetup = new HttpCrawledRepositorySetup(baseUrl, parseGroupFiltersAttribute(groupFilters));
							if (proxyHost != null && proxyPort != null && isInteger(proxyPort)) {
								httpCrawledRepositorySetup.setProxyHost(proxyHost);
								httpCrawledRepositorySetup.setProxyPort(Integer.parseInt(proxyPort));
							}
							PatternSet patternSet = httpCrawledRepositorySetup.getPatternSet().getMutable();
							if (entryPattern != null) {
								patternSet.setEntryPattern(entryPattern);
							}
							if (parentDirectoryPattern != null) {
								patternSet.setParentDirectoryPattern(parentDirectoryPattern);
							}
							if (fileEntryPattern != null) {
								patternSet.setFileEntryPattern(fileEntryPattern);
							}
							if (directoryEntryPattern != null) {
								patternSet.setDirectoryEntryPattern(directoryEntryPattern);
							}
							httpCrawledRepositorySetup.setPatternSet(patternSet);
							tmpRepository = new CrawledRepository(id, httpCrawledRepositorySetup);
						} else if (type != null && type.equals(REPOSITORY_TAG_TYPE_ATTRIBUTE_FILESYSTEM_VALUE)) {
							FileSystemCrawledRepositorySetup fileSystemCrawledRepositorySetup = new FileSystemCrawledRepositorySetup(baseUrl);
							fileSystemCrawledRepositorySetup.setGroupFilters(parseGroupFiltersAttribute(groupFilters));
							tmpRepository = new CrawledRepository(id, fileSystemCrawledRepositorySetup);
						}
						continue;
					}
					if (elementNameLocalPart.equals(GROUP_TAG)) {
						String name = null;
						for (Iterator it = startElement.getAttributes(); it.hasNext();) {
							Attribute attribute = (Attribute) it.next();
							name = getAttributeValue(name, GROUP_TAG_NAME_ATTRIBUTE, attribute);
						}
						tmpGroup = new Group(name);
						continue;
					}
					if (elementNameLocalPart.equals(ARTIFACT_TAG)) {
						String id = null;
						for (Iterator it = startElement.getAttributes(); it.hasNext();) {
							Attribute attribute = (Attribute) it.next();
							id = getAttributeValue(id, ARTIFACT_TAG_ID_ATTRIBUTE, attribute);
						}
						tmpArtifact = new Artifact(id);
					}
					if (elementNameLocalPart.equals(ARTIFACTVERSION_TAG)) {
						String id = null;
						String type = null;
						String url = null;
						String pomUrl = null;
						String sourcesUrl = null;
						String javadocUrl = null;
						String version = null;
						String classifier = null;
						for (Iterator it = startElement.getAttributes(); it.hasNext();) {
							Attribute attribute = (Attribute) it.next();
							id = getAttributeValue(id, ARTIFACTVERSION_TAG_ID_ATTRIBUTE, attribute);
							type = getAttributeValue(type, ARTIFACTVERSION_TAG_TYPE_ATTRIBUTE, attribute);
							url = getAttributeValue(url, ARTIFACTVERSION_TAG_URL_ATTRIBUTE, attribute);
							pomUrl = getAttributeValue(pomUrl, ARTIFACTVERSION_TAG_POM_URL_ATTRIBUTE, attribute);
							sourcesUrl = getAttributeValue(sourcesUrl, ARTIFACTVERSION_TAG_SOURCES_URL_ATTRIBUTE, attribute);
							javadocUrl = getAttributeValue(javadocUrl, ARTIFACTVERSION_TAG_JAVADOC_URL_ATTRIBUTE, attribute);
							version = getAttributeValue(version, ARTIFACTVERSION_TAG_VERSION_ATTRIBUTE, attribute);
							classifier = getAttributeValue(classifier, ARTIFACTVERSION_TAG_CLASSIFIER_ATTRIBUTE, attribute);
						}
						tmpArtifactVersion = new ArtifactVersion();
						tmpArtifactVersion.setId(id);
						try {
							tmpArtifactVersion.setType(ArtifactVersion.Type.valueOf(type));
						} catch (Exception exception) {
							// FOR PREVIOUS VERSIONS' COMPATIBILITY, CONSIDER ANY PREVIOUS ENTRY IS OF LIBRARY TYPE.
							tmpArtifactVersion.setType(Type.LIBRARY);
						}
						try {
							tmpArtifactVersion.setUrl(new URL(url));
						} catch (MalformedURLException exception) {
							// just ignore...
						}
						try {
							tmpArtifactVersion.setPomUrl(new URL(pomUrl));
						} catch (MalformedURLException exception) {
							// just ignore...
						}
						try {
							tmpArtifactVersion.setSourcesUrl(new URL(sourcesUrl));
						} catch (MalformedURLException exception) {
							// just ignore...
						}
						try {
							tmpArtifactVersion.setJavadocUrl(new URL(javadocUrl));
						} catch (MalformedURLException exception) {
							// just ignore...
						}
						tmpArtifactVersion.setVersion(version);
						tmpArtifactVersion.setClassifier(classifier);
					}
				}
				if (event.isEndElement()) {
					EndElement endElement = event.asEndElement();
					String elementNameLocalPart = endElement.getName().getLocalPart();
					if (elementNameLocalPart.equals(REPOSITORY_TAG)) {
						if (tmpRepository != null) {
							result.add(tmpRepository);
						}
						tmpRepository = null;
						continue;
					}
					if (elementNameLocalPart.equals(GROUP_TAG)) {
						if (tmpGroup != null) {
							tmpGroup.setParent(tmpRepository);
						}
						tmpGroup = null;
						continue;
					}
					if (elementNameLocalPart.equals(ARTIFACT_TAG)) {
						if (tmpArtifact != null) {
							tmpArtifact.setParent(tmpGroup);
						}
						tmpArtifact = null;
						continue;
					}
					if (elementNameLocalPart.equals(ARTIFACTVERSION_TAG)) {
						if (tmpArtifactVersion != null) {
							tmpArtifactVersion.setParent(tmpArtifact);
						}
						tmpArtifactVersion = null;
						continue;
					}
				}
			}
		} catch (XMLStreamException e) {
			throw new BindingException(e);
		}
		return result;
	}

	/**
	 * Checks if is integer.
	 * 
	 * @param proxyPort
	 *            the proxy port
	 * 
	 * @return true, if is integer
	 */
	private boolean isInteger(String proxyPort) {
		return proxyPort.matches("[0-9]+");
	}

	/**
	 * Parses the group filters attribute.
	 * 
	 * @param attributeValue
	 *            the attribute value
	 * 
	 * @return the set< string>
	 */
	private Set<String> parseGroupFiltersAttribute(String attributeValue) {
		Set<String> groupFilters = new HashSet<String>();
		for (String groupFilter : attributeValue.split("\\|")) {
			if (groupFilter.trim().length() > 0) {
				groupFilters.add(groupFilter);
			}
		}
		return groupFilters;
	}

	/**
	 * Gets the attribute value.
	 * 
	 * @param attributeValue
	 *            the attribute value
	 * @param attributeName
	 *            the attribute name
	 * @param attribute
	 *            the attribute
	 * 
	 * @return the attribute value
	 */
	private String getAttributeValue(String attributeValue, String attributeName, Attribute attribute) {
		if (attributeValue == null) {
			if (attribute.getName().getLocalPart().equals(attributeName)) {
				attributeValue = attribute.getValue();
			}
		}
		return attributeValue;
	}

	/**
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlRepositoriesBinder#parseXmlRepositories(java.lang.String)
	 */
	public List<CrawledRepository> parseXmlRepositories(String inputString) throws BindingException {
		InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
		return parseXmlRepositories(inputStream);
	}

	/**
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlRepositoriesBinder#toXmlRepositories(java.util.List)
	 */
	public String toXmlRepositories(List<CrawledRepository> crawledRepositories) throws BindingException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		toXmlRepositories(crawledRepositories, outputStream);
		return new String(outputStream.toByteArray());
	}

	/**
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlRepositoriesBinder#toXmlRepositories(java.util.List, java.io.OutputStream)
	 */
	public void toXmlRepositories(List<CrawledRepository> crawledRepositories, OutputStream outputStream) throws BindingException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		try {
			XMLEventWriter parser = factory.createXMLEventWriter(outputStream);
			parser.add(eventFactory.createStartDocument(UTF_8, XML_VERSION_1_0));
			Set<Attribute> attributes = new HashSet<Attribute>();
			parser.add(eventFactory.createStartElement(new QName(REPOSITORIES_TAG), attributes.iterator(), null));
			RepositoryCreationVisitor repositoryCreationVisitor = new RepositoryCreationVisitor(parser, eventFactory);
			for (CrawledRepository crawledRepository : crawledRepositories) {
				crawledRepository.accept(repositoryCreationVisitor);
				if (repositoryCreationVisitor.getOccuredException() != null) {
					throw repositoryCreationVisitor.getOccuredException();
				}
			}
			parser.add(eventFactory.createEndElement(new QName(REPOSITORIES_TAG), null));
			parser.add(eventFactory.createEndDocument());
		} catch (XMLStreamException e) {
			throw new BindingException(e);
		}
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * 
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws BindingException
	 */
	public static void main(String[] args) throws FileNotFoundException, BindingException {
		IXmlRepositoriesBinder repositoriesBinder = new StaxRepositoriesBinder();
		List<CrawledRepository> result = repositoriesBinder.parseXmlRepositories(new FileInputStream(new File("R:/maven2-repo-export.xml")));
		System.out.println(result);

		repositoriesBinder.toXmlRepositories(result, System.out);

		CrawledRepository repo = repositoriesBinder.parseXmlDefinition(new FileInputStream(new File("R:/repo-definition.xml")));
		System.out.println();
		System.out.println(repo);

		repositoriesBinder.toXmlDefinition(repo, System.out);
	}
}
