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
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.org.eclipse.core.utils.platform.binding.BindingException;
import org.org.eclipse.dws.core.internal.model.ParentPom;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.PomDependency;
import org.org.eclipse.dws.core.internal.model.PomProperty;
import org.org.eclipse.dws.core.internal.model.PomRepository;
import org.org.eclipse.dws.core.internal.model.PomDependency.Scope;

/**
 * The Class StaxPomFileBinder.
 */
public class StaxPomFileBinder implements IXmlPomFileBinder {

	/**
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlPomFileBinder#parsePomFile(java.io.InputStream)
	 */
	public Pom parsePomFile(InputStream inputStream) throws BindingException {
		Pom result = null;
		XMLInputFactory factory = XMLInputFactory.newInstance();
		try {
			XMLEventReader parser = factory.createXMLEventReader(inputStream);
			eventloop: while (true) {
				XMLEvent event = parser.nextEvent();
				if (event.isEndDocument()) {
					parser.close();
					break eventloop;
				}
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					String elementNameLocalPart = startElement.getName().getLocalPart();
					if (elementNameLocalPart.equals(PROJECT_TAG)) {
						result = new Pom();
						parseProject(parser, result);
						continue;
					}
				}
			}
		} catch (XMLStreamException e) {
			throw new BindingException(e);
		}
		return result;
	}

	private void parseProject(XMLEventReader parser, Pom result) throws XMLStreamException {
		eventloop: while (true) {
			XMLEvent event = parser.nextEvent();
			if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				String elementNameLocalPart = endElement.getName().getLocalPart();
				if (elementNameLocalPart.equals(PROJECT_TAG)) {
					break eventloop;
				}
			}
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String elementNameLocalPart = startElement.getName().getLocalPart();
				if (elementNameLocalPart.equals(GROUPID_TAG)) {
					result.setGroupId(parseTagTextContent(parser));
					continue;
				}
				if (elementNameLocalPart.equals(ARTIFACTID_TAG)) {
					result.setArtifactId(parseTagTextContent(parser));
					continue;
				}
				if (elementNameLocalPart.equals(VERSION_TAG)) {
					result.setVersion(parseTagTextContent(parser));
					continue;
				}
				if (elementNameLocalPart.equals(PACKAGING_TAG)) {
					result.setPackaging(parseTagTextContent(parser));
					continue;
				}
				if (elementNameLocalPart.equals(DESCRIPTION_TAG)) {
					result.setDescription(parseTagTextContent(parser));
					continue;
				}
				if (elementNameLocalPart.equals(NAME_TAG)) {
					result.setName(parseTagTextContent(parser));
					continue;
				}
				if (elementNameLocalPart.equals(DEPENDENCIES_TAG)) {
					parseDependencies(parser, result);
					continue;
				}
				if (elementNameLocalPart.equals(PARENT_TAG)) {
					parseParent(parser, result);
					continue;
				}
				if (elementNameLocalPart.equals(REPOSITORY_TAG)) {
					parseRepository(REPOSITORY_TAG, parser, result);
					continue;
				}
				if (elementNameLocalPart.equals(SNAPSHOT_REPOSITORY_TAG)) {
					parseRepository(SNAPSHOT_REPOSITORY_TAG, parser, result);
					continue;
				}
				if (elementNameLocalPart.equals(PROPERTIES_TAG)) {
					parseProperties(parser, result);
					continue;
				}
			}

		}

	}

	private void parseProperties(XMLEventReader parser, Pom result) throws XMLStreamException {
		eventloop: while (true) {
			XMLEvent event = parser.nextEvent();
			if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				String elementNameLocalPart = endElement.getName().getLocalPart();
				if (elementNameLocalPart.equals(PROPERTIES_TAG)) {
					break eventloop;
				}
			}
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String key = startElement.getName().getLocalPart();
				String value = parseTagTextContent(parser);
				result.getProperties().addProperty(new PomProperty(key, value));
				continue;
			}
		}
	}

	private void parseRepository(String tagName, XMLEventReader parser, Pom result) throws XMLStreamException {
		PomRepository pomRepository = new PomRepository();
		eventloop: while (true) {
			XMLEvent event = parser.nextEvent();
			if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				String elementNameLocalPart = endElement.getName().getLocalPart();
				if (elementNameLocalPart.equals(tagName)) {
					result.getRepositories().addRepository(pomRepository);
					break eventloop;
				}
			}
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String elementNameLocalPart = startElement.getName().getLocalPart();
				if (elementNameLocalPart.equals(ID_TAG)) {
					pomRepository.setId(parseTagTextContent(parser));
					continue;
				}
				if (elementNameLocalPart.equals(NAME_TAG)) {
					pomRepository.setName(parseTagTextContent(parser));
					continue;
				}
				if (elementNameLocalPart.equals(URL_TAG)) {
					pomRepository.setUrl(parseTagTextContent(parser));
					continue;
				}
			}
		}
	}

	private void parseParent(XMLEventReader parser, Pom result) throws XMLStreamException {
		ParentPom parentPom = new ParentPom();
		eventloop: while (true) {
			XMLEvent event = parser.nextEvent();
			if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				String elementNameLocalPart = endElement.getName().getLocalPart();
				if (elementNameLocalPart.equals(PARENT_TAG)) {
					result.setParentPom(parentPom);
					break eventloop;
				}
			}
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String elementNameLocalPart = startElement.getName().getLocalPart();
				if (elementNameLocalPart.equals(GROUPID_TAG)) {
					parentPom.setGroupId(parseTagTextContent(parser));
					continue;
				}
				if (elementNameLocalPart.equals(ARTIFACTID_TAG)) {
					parentPom.setArtifactId(parseTagTextContent(parser));
					continue;
				}
				if (elementNameLocalPart.equals(VERSION_TAG)) {
					parentPom.setVersion(parseTagTextContent(parser));
					continue;
				}
				if (elementNameLocalPart.equals(RELATIVEPATH_TAG)) {
					parentPom.setRelativePath(parseTagTextContent(parser));
					continue;
				}
			}
		}
	}

	private void parseDependencies(XMLEventReader parser, Pom result) throws XMLStreamException {
		eventloop: while (true) {
			XMLEvent event = parser.nextEvent();
			if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				String elementNameLocalPart = endElement.getName().getLocalPart();
				if (elementNameLocalPart.equals(DEPENDENCIES_TAG)) {
					break eventloop;
				}
			}
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String elementNameLocalPart = startElement.getName().getLocalPart();
				if (elementNameLocalPart.equals(DEPENDENCY_TAG)) {
					parseDependency(parser, result);
				}
			}
		}
	}

	private void parseDependency(XMLEventReader parser, Pom result) throws XMLStreamException {
		PomDependency pomDependency = new PomDependency();
		eventloop: while (true) {
			XMLEvent event = parser.nextEvent();
			if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				String elementNameLocalPart = endElement.getName().getLocalPart();
				if (elementNameLocalPart.equals(DEPENDENCY_TAG)) {
					result.addChild(pomDependency);
					break eventloop;
				}
			}
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String elementNameLocalPart = startElement.getName().getLocalPart();
				if (elementNameLocalPart.equals(GROUPID_TAG)) {
					pomDependency.setGroupId(parseTagTextContent(parser));
					continue;
				}
				if (elementNameLocalPart.equals(ARTIFACTID_TAG)) {
					pomDependency.setArtifactId(parseTagTextContent(parser));
					continue;
				}
				if (elementNameLocalPart.equals(VERSION_TAG)) {
					pomDependency.setVersion(parseTagTextContent(parser));
					continue;
				}
				if (elementNameLocalPart.equals(SCOPE_TAG)) {
					String tagContent = parseTagTextContent(parser);
					tagContent = tagContent == null ? "compile" : tagContent;
					pomDependency.setScope(Scope.valueOf(tagContent.toUpperCase()));
					continue;
				}
				if (elementNameLocalPart.equals(CLASSIFIER_TAG)) {
					pomDependency.setClassifier(parseTagTextContent(parser));
					continue;
				}
			}
		}
	}

	private String parseTagTextContent(XMLEventReader parser) throws XMLStreamException {
		String result = null;
		XMLEvent event = parser.nextEvent();
		if (event.isCharacters()) {
			Characters characters = event.asCharacters();
			result = characters.getData();
		}
		return result;
	}

	/**
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlPomFileBinder#parsePomFile(java.lang.String)
	 */
	public Pom parsePomFile(String input) throws BindingException {
		InputStream inputStream = new ByteArrayInputStream(input.getBytes());
		return parsePomFile(inputStream);
	}

	public String updatePom(InputStream pomStream, Set<PomDependency> pomDependencies, String pomEncoding) throws BindingException {
		String result = null;
		ByteArrayOutputStream outputStream = null;
		XMLEventReader reader = null;
		XMLEventWriter writer = null;
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		try {
			outputStream = new ByteArrayOutputStream();
			reader = inputFactory.createXMLEventReader(pomStream);
			writer = outputFactory.createXMLEventWriter(outputStream);
			boolean containsDependenciesTag = false;
			while (true) {
				XMLEvent event = reader.nextEvent();
				if (event.getEventType() == XMLStreamConstants.START_ELEMENT && event.asStartElement().getName().getLocalPart().equals(new QName(DEPENDENCIES_TAG).getLocalPart())) {
					containsDependenciesTag = true;
					writer.add(event);
					addPomDependenciesToExistingDependenciesTag(reader, writer, event, eventFactory, pomDependencies);
				} else {
					if (!containsDependenciesTag) {
						if (event.getEventType() == XMLStreamConstants.END_ELEMENT) {
							QName elementName = event.asEndElement().getName();
							if (elementName.getLocalPart().equals(new QName(PROJECT_TAG).getLocalPart())) {
								createNewDependenciesTagAndAddPomDependencies(reader, writer, event, eventFactory, pomDependencies);
							}
						}
					}
					writer.add(event);
					writer.flush();
				}
				if (!reader.hasNext())
					break;
			}
			reader.close();
			reader = null;
			writer.close();
			writer = null;
			pomStream.close();
			pomStream = null;
			outputStream.close();
			result = outputStream.toString(pomEncoding);
			outputStream = null;
		} catch (Exception e) {
			throw new BindingException();
		} finally {
			try {
				if (pomStream != null) {
					pomStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
				if (reader != null) {
					reader.close();
				}
				if (writer != null) {
					writer.close();
				}

			} catch (Exception e) {
				// trapping exceptions there
			} finally {
			}

		}
		return result;
	}

	/**
	 * @see org.org.eclipse.dws.core.internal.xml.IXmlPomFileBinder#updatePom(java.io.InputStream, java.util.Set, java.lang.String)
	 */
	public String updatePom(String input, Set<PomDependency> pomDependencies, String pomEncoding) throws BindingException {
		return null;
	}

	/**
	 * Creates the new dependencies tag and add pom dependencies.
	 * 
	 * @param reader
	 *            the reader
	 * @param writer
	 *            the writer
	 * @param event
	 *            the event
	 * @param eventFactory
	 *            the event factory
	 * @param pomDependencies
	 *            the pom dependencies
	 * 
	 * @throws XMLStreamException
	 *             the XML stream exception
	 */
	private static void createNewDependenciesTagAndAddPomDependencies(XMLEventReader reader, XMLEventWriter writer, XMLEvent event, XMLEventFactory eventFactory, Set<PomDependency> pomDependencies) throws XMLStreamException {
		writer.add(eventFactory.createStartElement(new QName(DEPENDENCIES_TAG), null, null));
		addPomDependenciesToExistingDependenciesTag(reader, writer, event, eventFactory, pomDependencies);
		writer.add(eventFactory.createIgnorableSpace("\n"));
		writer.add(eventFactory.createEndElement(new QName(DEPENDENCIES_TAG), null));
		writer.add(eventFactory.createIgnorableSpace("\n"));
	}

	/**
	 * Adds the pom dependencies to existing dependencies tag.
	 * 
	 * @param reader
	 *            the reader
	 * @param writer
	 *            the writer
	 * @param event
	 *            the event
	 * @param eventFactory
	 *            the event factory
	 * @param pomDependencies
	 *            the pom dependencies
	 * 
	 * @throws XMLStreamException
	 *             the XML stream exception
	 */
	private static void addPomDependenciesToExistingDependenciesTag(XMLEventReader reader, XMLEventWriter writer, XMLEvent event, XMLEventFactory eventFactory, Set<PomDependency> pomDependencies) throws XMLStreamException {
		for (PomDependency pomDependency : pomDependencies) {
			writer.add(eventFactory.createIgnorableSpace("\n\t"));
			writer.add(eventFactory.createStartElement(new QName(DEPENDENCY_TAG), null, null));
			addTagWithContent(writer, eventFactory, "\n\t\t", new QName(GROUPID_TAG), pomDependency.getGroupId());
			addTagWithContent(writer, eventFactory, "\n\t\t", new QName(ARTIFACTID_TAG), pomDependency.getArtifactId());
			addTagWithContent(writer, eventFactory, "\n\t\t", new QName(VERSION_TAG), pomDependency.getVersion());
			addTagWithContent(writer, eventFactory, "\n\t\t", new QName(CLASSIFIER_TAG), pomDependency.getClassifier());
			addTagWithContent(writer, eventFactory, "\n\t\t", new QName(SCOPE_TAG), pomDependency.getScope().name().toLowerCase());
			addTagWithContent(writer, eventFactory, "\n\t\t", new QName(OPTIONAL_TAG), pomDependency.isOptional().toString());
			writer.add(eventFactory.createIgnorableSpace("\n\t"));
			writer.add(eventFactory.createEndElement(new QName(DEPENDENCY_TAG), null));

		}
	}

	/**
	 * Adds the tag with content.
	 * 
	 * @param writer
	 *            the writer
	 * @param eventFactory
	 *            the event factory
	 * @param ignorableSpace
	 *            the ignorable space
	 * @param qName
	 *            the q name
	 * @param content
	 *            the content
	 * 
	 * @throws XMLStreamException
	 *             the XML stream exception
	 */
	private static void addTagWithContent(XMLEventWriter writer, XMLEventFactory eventFactory, String ignorableSpace, QName qName, String content) throws XMLStreamException {
		if (content != null) {
			writer.add(eventFactory.createIgnorableSpace(ignorableSpace));
			writer.add(eventFactory.createStartElement(qName, null, null));
			writer.add(eventFactory.createCharacters(content));
			writer.add(eventFactory.createEndElement(qName, null));
		}
	}

}