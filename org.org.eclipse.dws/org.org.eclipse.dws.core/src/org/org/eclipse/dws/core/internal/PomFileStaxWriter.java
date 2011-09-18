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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.org.eclipse.dws.core.internal.model.PomDependency;
import org.org.eclipse.dws.core.internal.model.PomDependency.Scope;
import org.org.eclipse.dws.core.internal.xml.StaxPomFileBinder;


/**
 * The Class PomFileStaxWriter.
 * @deprecated prefer the use of the StaxPomFileBinder.
 * @see StaxPomFileBinder
 */
@Deprecated
public class PomFileStaxWriter {

	/** The Constant OPTIONAL_TAG_QNAME. */
	private static final QName OPTIONAL_TAG_QNAME = new QName(null, PomFileConstants.OPTIONAL_TAG);

	/** The Constant SCOPE_TAG_QNAME. */
	private static final QName SCOPE_TAG_QNAME = new QName(null, PomFileConstants.SCOPE_TAG);

	/** The Constant CLASSIFIER_TAG_QNAME. */
	private static final QName CLASSIFIER_TAG_QNAME = new QName(null, PomFileConstants.CLASSIFIER_TAG);

	/** The Constant VERSION_TAG_QNAME. */
	private static final QName VERSION_TAG_QNAME = new QName(null, PomFileConstants.VERSION_TAG);

	/** The Constant ARTIFACTID_TAG_QNAME. */
	private static final QName ARTIFACTID_TAG_QNAME = new QName(null, PomFileConstants.ARTIFACTID_TAG);

	/** The Constant GROUPID_TAG_QNAME. */
	private static final QName GROUPID_TAG_QNAME = new QName(null, PomFileConstants.GROUPID_TAG);

	/** The Constant DEPENDENCY_TAG_QNAME. */
	private static final QName DEPENDENCY_TAG_QNAME = new QName(null, PomFileConstants.DEPENDENCY_TAG);

	/** The Constant PROJECT_TAG_QNAME. */
	private static final QName PROJECT_TAG_QNAME = new QName(null, PomFileConstants.PROJECT_TAG);

	/** The Constant DEPENDENCIES_TAG_QNAME. */
	private static final QName DEPENDENCIES_TAG_QNAME = new QName(null, PomFileConstants.DEPENDENCIES_TAG);

	/**
	 * Update pom.
	 * 
	 * @param pomStream the pom stream
	 * @param pomDependencies the pom dependencies
	 * @param pomEncoding the pom encoding
	 * 
	 * @return the string
	 * 
	 * @throws XMLStreamException the XML stream exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String updatePom(InputStream pomStream, Set<PomDependency> pomDependencies, String pomEncoding) throws XMLStreamException, IOException {
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
				if (event.getEventType() == XMLStreamConstants.START_ELEMENT && event.asStartElement().getName().getLocalPart().equals(DEPENDENCIES_TAG_QNAME.getLocalPart())) {
					containsDependenciesTag = true;
					writer.add(event);
					addPomDependenciesToExistingDependenciesTag(reader, writer, event, eventFactory, pomDependencies);
				} else {
					if (!containsDependenciesTag) {
						if (event.getEventType() == XMLStreamConstants.END_ELEMENT) {
							QName elementName = event.asEndElement().getName();
							if (elementName.getLocalPart().equals(PROJECT_TAG_QNAME.getLocalPart())) {
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
	 * Creates the new dependencies tag and add pom dependencies.
	 * 
	 * @param reader the reader
	 * @param writer the writer
	 * @param event the event
	 * @param eventFactory the event factory
	 * @param pomDependencies the pom dependencies
	 * 
	 * @throws XMLStreamException the XML stream exception
	 */
	private static void createNewDependenciesTagAndAddPomDependencies(XMLEventReader reader, XMLEventWriter writer, XMLEvent event, XMLEventFactory eventFactory, Set<PomDependency> pomDependencies) throws XMLStreamException {
		writer.add(eventFactory.createStartElement(DEPENDENCIES_TAG_QNAME, null, null));
		addPomDependenciesToExistingDependenciesTag(reader, writer, event, eventFactory, pomDependencies);
		writer.add(eventFactory.createIgnorableSpace("\n"));
		writer.add(eventFactory.createEndElement(DEPENDENCIES_TAG_QNAME, null));
		writer.add(eventFactory.createIgnorableSpace("\n"));
	}

	/**
	 * Adds the pom dependencies to existing dependencies tag.
	 * 
	 * @param reader the reader
	 * @param writer the writer
	 * @param event the event
	 * @param eventFactory the event factory
	 * @param pomDependencies the pom dependencies
	 * 
	 * @throws XMLStreamException the XML stream exception
	 */
	private static void addPomDependenciesToExistingDependenciesTag(XMLEventReader reader, XMLEventWriter writer, XMLEvent event, XMLEventFactory eventFactory, Set<PomDependency> pomDependencies) throws XMLStreamException {
		for (PomDependency pomDependency : pomDependencies) {
			writer.add(eventFactory.createIgnorableSpace("\n\t"));
			writer.add(eventFactory.createStartElement(DEPENDENCY_TAG_QNAME, null, null));
			addTagWithContent(writer, eventFactory, "\n\t\t", GROUPID_TAG_QNAME, pomDependency.getGroupId());
			addTagWithContent(writer, eventFactory, "\n\t\t", ARTIFACTID_TAG_QNAME, pomDependency.getArtifactId());
			addTagWithContent(writer, eventFactory, "\n\t\t", VERSION_TAG_QNAME, pomDependency.getVersion());
			addTagWithContent(writer, eventFactory, "\n\t\t", CLASSIFIER_TAG_QNAME, pomDependency.getClassifier());
			addTagWithContent(writer, eventFactory, "\n\t\t", SCOPE_TAG_QNAME, pomDependency.getScope().name().toLowerCase());
			addTagWithContent(writer, eventFactory, "\n\t\t", OPTIONAL_TAG_QNAME, pomDependency.isOptional().toString());
			writer.add(eventFactory.createIgnorableSpace("\n\t"));
			writer.add(eventFactory.createEndElement(DEPENDENCY_TAG_QNAME, null));

		}
	}

	/**
	 * Adds the tag with content.
	 * 
	 * @param writer the writer
	 * @param eventFactory the event factory
	 * @param ignorableSpace the ignorable space
	 * @param qName the q name
	 * @param content the content
	 * 
	 * @throws XMLStreamException the XML stream exception
	 */
	private static void addTagWithContent(XMLEventWriter writer, XMLEventFactory eventFactory, String ignorableSpace, QName qName, String content) throws XMLStreamException {
		if (content != null) {
			writer.add(eventFactory.createIgnorableSpace(ignorableSpace));
			writer.add(eventFactory.createStartElement(qName, null, null));
			writer.add(eventFactory.createCharacters(content));
			writer.add(eventFactory.createEndElement(qName, null));
		}
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * 
	 * @throws XMLStreamException the XML stream exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws XMLStreamException, IOException {
		 String file = "<project></project>";
		 InputStream inputStream = new ByteArrayInputStream(file.getBytes());
		 Set<PomDependency> pomDependencies = new LinkedHashSet<PomDependency>();
		 pomDependencies.add(createPomDependency("ec.ep.dit.isp", "myartifact", "1.0.0", "beta", Scope.COMPILE, false));
		 pomDependencies.add(createPomDependency("org.apache", "commons-lang", "2.0.6", null, Scope.COMPILE, true));
		 System.out.println(updatePom(inputStream, pomDependencies, "ISO-8859-1"));

		String file2 = "<project>" +
								"<dependencies>" +
									"<dependency>" +
										"<groupId>ec.ep.dit.isp.blabla</groupId>" +
										"<artifactId>myartifact2</artifactId>" +
										"<version>1.0.1</version>" +
										"<scope>compile</scope>" +
									"</dependency>" +
								"</dependencies>" +
							"</project>";
		InputStream inputStream2 = new ByteArrayInputStream(file2.getBytes());
		Set<PomDependency> pomDependencies2 = new LinkedHashSet<PomDependency>();
		pomDependencies2.add(createPomDependency("ec.ep.dit.isp", "myartifact", "1.0.0", "beta", Scope.COMPILE, false));
		pomDependencies2.add(createPomDependency("org.apache", "commons-lang", "2.0.6", null, Scope.COMPILE, true));
		System.out.println(updatePom(inputStream2, pomDependencies2, "UTF-8"));
	}

	/**
	 * Creates the pom dependency.
	 * 
	 * @param groupId the group id
	 * @param artifactId the artifact id
	 * @param version the version
	 * @param classifier the classifier
	 * @param scope the scope
	 * @param optional the optional
	 * 
	 * @return the pom dependency
	 */
	private static PomDependency createPomDependency(String groupId, String artifactId, String version, String classifier, Scope scope, Boolean optional) {
		PomDependency pomDependency = new PomDependency();
		pomDependency.setGroupId(groupId);
		pomDependency.setArtifactId(artifactId);
		pomDependency.setVersion(version);
		pomDependency.setClassifier(classifier);
		pomDependency.setScope(scope);
		pomDependency.setOptional(optional);
		return pomDependency;
	}
}