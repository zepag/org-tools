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
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPack;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPackArtifactVersion;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPackArtifactVersion.Target;
import org.org.model.IModelItem;
import org.org.model.IModelItemAdvancedVisitor;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.Group;
import org.org.repository.crawler.maven2.model.ArtifactVersion.Type;

/**
 * The Class StaxRepositoriesBinder.
 */
public class StaxLibraryPacksBinder implements IXmlLibraryPacksBinder {

	/**
	 * The Class LibraryPackCreationVisitor.
	 */
	public class LibraryPackCreationVisitor implements IModelItemAdvancedVisitor {

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
		public LibraryPackCreationVisitor(XMLEventWriter parser, XMLEventFactory eventFactory) {
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
				if (modelItem instanceof LibraryPack) {
					LibraryPack libraryPack = (LibraryPack) modelItem;
					List<Attribute> attributes = new LinkedList<Attribute>();
					createAttribute(attributes, LIBRARY_PACK_LABEL_ATTRIBUTE, libraryPack.getLabel());
					parser.add(eventFactory.createStartElement(new QName(LIBRARY_PACK_TAG), attributes.iterator(), null));
					parser.add(eventFactory.createStartElement(new QName(DESCRIPTION_TAG), null, null));
					parser.add(eventFactory.createCharacters(libraryPack.getDescription()));
					parser.add(eventFactory.createEndElement(new QName(DESCRIPTION_TAG), null));
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
				if (modelItem instanceof LibraryPackArtifactVersion) {
					LibraryPackArtifactVersion artifactVersion = (LibraryPackArtifactVersion) modelItem;
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
					if (artifactVersion.getTargets() != null) {
						StringBuilder builder = new StringBuilder();
						for (Target target : artifactVersion.getTargets()) {
							builder.append(target.name() + ",");
						}
						if (builder.length() > 0) {
							builder.deleteCharAt(builder.length() - 1);
						}
						createAttribute(attributes, ARTIFACTVERSION_TAG_TARGETS_ATTRIBUTE, builder.toString());
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
				if (modelItem instanceof LibraryPack) {
					parser.add(eventFactory.createEndElement(new QName(LIBRARY_PACK_TAG), null));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlRepositoriesBinder#parseXmlRepositories(java.io.InputStream)
	 */
	/**
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlRepositoriesBinder#parseXmlRepositories(java.io.InputStream)
	 */
	@SuppressWarnings("rawtypes")
	public List<LibraryPack> parseXmlLibraryPacks(InputStream inputStream) throws BindingException {
		List<LibraryPack> result = new ArrayList<LibraryPack>();
		XMLInputFactory factory = XMLInputFactory.newInstance();
		try {
			XMLEventReader parser = factory.createXMLEventReader(inputStream);
			String tmpLibraryPackLabel = null;
			LibraryPack tmpLibraryPack = null;
			Group tmpGroup = null;
			Artifact tmpArtifact = null;
			LibraryPackArtifactVersion tmpArtifactVersion = null;
			while (true) {
				XMLEvent event = parser.nextEvent();
				if (event.isEndDocument()) {
					parser.close();
					break;
				}
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					String elementNameLocalPart = startElement.getName().getLocalPart();
					if (elementNameLocalPart.equals(LIBRARY_PACK_TAG)) {
						String label = null;
						for (Iterator it = startElement.getAttributes(); it.hasNext();) {
							Attribute attribute = (Attribute) it.next();
							label = getAttributeValue(label, LIBRARY_PACK_LABEL_ATTRIBUTE, attribute);
						}
						tmpLibraryPackLabel = label;
						continue;
					}
					if (elementNameLocalPart.equals(DESCRIPTION_TAG)) {
						XMLEvent nextEvent = parser.nextEvent();
						if (nextEvent.isCharacters()) {
							tmpLibraryPack = new LibraryPack(tmpLibraryPackLabel, nextEvent.asCharacters().getData());
							tmpLibraryPackLabel = null;
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
						String targets = null;
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
							targets = getAttributeValue(targets, ARTIFACTVERSION_TAG_TARGETS_ATTRIBUTE, attribute);
						}
						tmpArtifactVersion = new LibraryPackArtifactVersion();
						tmpArtifactVersion.setId(id);
						try {
							tmpArtifactVersion.setType(LibraryPackArtifactVersion.Type.valueOf(type));
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
						try {
							String[] targetsStr = targets.contains(",") ? targets.split(",") : new String[] { targets };
							Target[] targetsEnum = new Target[targetsStr.length];
							int i = 0;
							for (String target : targetsStr) {
								targetsEnum[i] = Target.valueOf(target);
							}
							tmpArtifactVersion.setTargets(targetsEnum);
						} catch (Throwable exception) {
							System.err.println(exception);
						}
					}
				}
				if (event.isEndElement()) {
					EndElement endElement = event.asEndElement();
					String elementNameLocalPart = endElement.getName().getLocalPart();
					if (elementNameLocalPart.equals(LIBRARY_PACK_TAG)) {
						if (tmpLibraryPack != null) {
							result.add(tmpLibraryPack);
						}
						tmpLibraryPack = null;
						continue;
					}
					if (elementNameLocalPart.equals(GROUP_TAG)) {
						if (tmpGroup != null) {
							tmpGroup.setParent(tmpLibraryPack);
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
	public List<LibraryPack> parseXmlLibraryPacks(String inputString) throws BindingException {
		InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
		return parseXmlLibraryPacks(inputStream);
	}

	/**
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlRepositoriesBinder#toXmlRepositories(java.util.List)
	 */
	public String toXmlLibraryPacks(List<LibraryPack> crawledRepositories) throws BindingException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		toXmlLibraryPacks(crawledRepositories, outputStream);
		return new String(outputStream.toByteArray());
	}

	/**
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlRepositoriesBinder#toXmlRepositories(java.util.List, java.io.OutputStream)
	 */
	public void toXmlLibraryPacks(List<LibraryPack> libraryPacks, OutputStream outputStream) throws BindingException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		try {
			XMLEventWriter parser = factory.createXMLEventWriter(outputStream);
			parser.add(eventFactory.createStartDocument(UTF_8, XML_VERSION_1_0));
			Set<Attribute> attributes = new HashSet<Attribute>();
			parser.add(eventFactory.createStartElement(new QName(LIBRARY_PACKS_TAG), attributes.iterator(), null));
			LibraryPackCreationVisitor libraryPackCreationVisitor = new LibraryPackCreationVisitor(parser, eventFactory);
			for (LibraryPack libraryPack : libraryPacks) {
				libraryPack.accept(libraryPackCreationVisitor);
				if (libraryPackCreationVisitor.getOccuredException() != null) {
					throw libraryPackCreationVisitor.getOccuredException();
				}
			}
			parser.add(eventFactory.createEndElement(new QName(LIBRARY_PACKS_TAG), null));
			parser.add(eventFactory.createEndDocument());
		} catch (XMLStreamException e) {
			throw new BindingException(e);
		}
	}

}
