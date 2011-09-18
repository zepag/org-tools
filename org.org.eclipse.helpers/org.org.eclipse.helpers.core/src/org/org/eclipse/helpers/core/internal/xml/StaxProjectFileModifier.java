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
package org.org.eclipse.helpers.core.internal.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class StaxProjectFileModifier implements IProjectFileModifier {

	@SuppressWarnings("unchecked")
	public void modifyProjectFile(File file) throws IOException {
		File backupFile = new File(file.getAbsolutePath() + ".backup." + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date(System.currentTimeMillis())));
		copy(file, backupFile);
		InputStream inputStream = new FileInputStream(backupFile);
		modifyProjectFile(inputStream, new FileOutputStream(file));
	}

	private void copy(File file, File targetFile) throws IOException {
		FileReader in = null;
		FileWriter out = null;
		try {
			in = new FileReader(file);
			out = new FileWriter(targetFile);
			int c;
			while ((c = in.read()) != -1)
				out.write(c);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// do something clever here
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// do something clever here
				}
			}
		}
	}

	public void modifyProjectFile(InputStream inputStream, OutputStream outputStream) {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		try {
			XMLEventReader eventReader = inputFactory.createXMLEventReader(inputStream);
			XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(outputStream);
			while (true) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isEndDocument()) {
					eventReader.close();
					eventWriter.close();
					break;
				} else {
					Boolean isPassThru = true;
					if (event.isStartElement()) {
						StartElement startElement = event.asStartElement();
						if (startElement.getName().getLocalPart().equals(BUILDSPEC)) {
							isPassThru = false;
							while (true) {
								event = eventReader.nextEvent();
								if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(BUILDSPEC)) {
									break;
								}
							}
							eventWriter.add(eventFactory.createStartElement(new QName(BUILDSPEC), null, null));
							addBuildCommand(eventWriter, eventFactory, "org.eclipse.jdt.core.javabuilder");
							addBuildCommand(eventWriter, eventFactory, "org.eclipse.wst.common.project.facet.core.builder");
							addBuildCommand(eventWriter, eventFactory, "org.eclipse.wst.validation.validationbuilder");
							eventWriter.add(eventFactory.createEndElement(new QName(BUILDSPEC), null));
						}
						if (startElement.getName().getLocalPart().equals(NATURES)) {
							isPassThru = false;
							while (true) {
								event = eventReader.nextEvent();
								if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(NATURES)) {
									break;
								}
							}
							eventWriter.add(eventFactory.createStartElement(new QName(NATURES), null, null));
							addNature(eventWriter, eventFactory, "org.eclipse.wst.common.project.facet.core.nature");
							addNature(eventWriter, eventFactory, "org.eclipse.jdt.core.javanature");
							addNature(eventWriter, eventFactory, "org.eclipse.wst.common.modulecore.ModuleCoreNature");
							addNature(eventWriter, eventFactory, "org.eclipse.jem.workbench.JavaEMFNature");
							eventWriter.add(eventFactory.createEndElement(new QName(NATURES), null));
						}
					}
					if (isPassThru) {
						eventWriter.add(event);
					}
				}
			}
		} catch (XMLStreamException e) {
			throw new RuntimeException("Impossible to handle XML file", e);
		}
	}

	private void addNature(XMLEventWriter eventWriter, XMLEventFactory eventFactory, String nature) throws XMLStreamException {
		eventWriter.add(eventFactory.createStartElement(new QName(NATURE), null, null));
		eventWriter.add(eventFactory.createCharacters(nature));
		eventWriter.add(eventFactory.createEndElement(new QName(NATURE), null));
	}

	private void addBuildCommand(XMLEventWriter eventWriter, XMLEventFactory eventFactory, String buildCommandName) throws XMLStreamException {
		eventWriter.add(eventFactory.createStartElement(new QName(BUILDCOMMAND), null, null));
		eventWriter.add(eventFactory.createStartElement(new QName(NAME), null, null));
		eventWriter.add(eventFactory.createCharacters(buildCommandName));
		eventWriter.add(eventFactory.createEndElement(new QName(NAME), null));
		eventWriter.add(eventFactory.createStartElement(new QName(ARGUMENTS), null, null));
		eventWriter.add(eventFactory.createCharacters(""));
		eventWriter.add(eventFactory.createEndElement(new QName(ARGUMENTS), null));
		eventWriter.add(eventFactory.createEndElement(new QName(BUILDCOMMAND), null));
	}

	public static void main(String[] args) {
		StaxProjectFileModifier staxProjectFileModifier = new StaxProjectFileModifier();
		InputStream inputStream = new ByteArrayInputStream(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<projectDescription>" + "<name>essai</name>" + "<comment></comment>" + "<projects>" + "</projects>" + "<buildSpec>" + "<buildCommand>" + "<name>com.genuitec.eclipse.j2eedt.core.WebClasspathBuilder</name>" + "<arguments>" + "</arguments>" + "</buildCommand>" + "<buildCommand>" + "<name>org.eclipse.jdt.core.javabuilder</name>" + "<arguments>" + "</arguments>" + "</buildCommand>" + "<buildCommand>" + "<name>com.genuitec.eclipse.j2eedt.core.J2EEProjectValidator</name>" + "<arguments>" + "</arguments>" + "</buildCommand>" + "<buildCommand>" + "<name>com.genuitec.eclipse.j2eedt.core.DeploymentDescriptorValidator</name>" + "<arguments>" + "</arguments>" + "</buildCommand>" + "<buildCommand>" + "<name>org.eclipse.wst.validation.validationbuilder</name>" + "<arguments>" + "</arguments>" + "</buildCommand>" + "</buildSpec>" + "<natures>" + "<nature>com.genuitec.eclipse.j2eedt.core.webnature</nature>" + "<nature>org.eclipse.jdt.core.javanature</nature>" + "</natures>" + "</projectDescription>").getBytes());
		staxProjectFileModifier.modifyProjectFile(inputStream, System.out);
	}
}
