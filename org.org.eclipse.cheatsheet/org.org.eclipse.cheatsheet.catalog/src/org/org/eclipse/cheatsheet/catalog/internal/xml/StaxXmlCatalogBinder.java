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
package org.org.eclipse.cheatsheet.catalog.internal.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalogReference;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalogReferenceType;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCategory;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetReference;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetReferenceType;
import org.org.eclipse.cheatsheet.catalog.internal.model.Tags;
import org.org.model.IModelItem;
import org.org.model.IModelItemVisitor;

public class StaxXmlCatalogBinder implements IXmlCatalogBinder {

	public class CheatSheetReferenceCreationVisitor implements IModelItemVisitor {

		private XMLEventWriter parser;
		private XMLEventFactory eventFactory;
		private XMLStreamException occuredException;

		public CheatSheetReferenceCreationVisitor(XMLEventWriter parser, XMLEventFactory eventFactory) {
			this.parser = parser;
			this.eventFactory = eventFactory;
		}

		@SuppressWarnings("rawtypes")
		public boolean visit(IModelItem modelItem) {
			boolean keepOnVisiting = true;
			if (modelItem instanceof CheatSheetReference) {
				CheatSheetReference cheatSheetReference = (CheatSheetReference) modelItem;
				Set<Attribute> attributes = new LinkedHashSet<Attribute>();
				attributes.add(eventFactory.createAttribute(new QName(ID), cheatSheetReference.getId()));
				attributes.add(eventFactory.createAttribute(new QName(NAME), cheatSheetReference.getName()));
				if (cheatSheetReference.getDescription() != null) {
					attributes.add(eventFactory.createAttribute(new QName(DESCRIPTION), cheatSheetReference.getDescription()));
				}
				attributes.add(eventFactory.createAttribute(new QName(TYPE), cheatSheetReference.getType().name()));
				attributes.add(eventFactory.createAttribute(new QName(URL), cheatSheetReference.getUrl()));
				attributes.add(eventFactory.createAttribute(new QName(CATEGORY), cheatSheetReference.getParent().getName()));
				attributes.add(eventFactory.createAttribute(new QName(TAGS), cheatSheetReference.getTags().getTagsString()));
				try {
					parser.add(eventFactory.createStartElement(new QName(CHEATSHEET_REFERENCE), attributes.iterator(), null));
					parser.add(eventFactory.createEndElement(new QName(CHEATSHEET_REFERENCE), null));
				} catch (XMLStreamException e) {
					this.occuredException = e;
				}

			}
			return keepOnVisiting;
		}

		public XMLStreamException getOccuredException() {
			return occuredException;
		}
	}

	public CheatSheetCatalog parseXmlCatalog(InputStream inputStream) {
		CheatSheetCatalog result = null;
		XMLInputFactory factory = XMLInputFactory.newInstance();
		try {
			XMLEventReader parser = factory.createXMLEventReader(inputStream);
			Map<String, CheatSheetCategory> registeredCategories = new HashMap<String, CheatSheetCategory>();
			while (true) {
				XMLEvent event = parser.nextEvent();
				if (event.isEndDocument()) {
					parser.close();
					break;
				}
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					if (startElement.getName().getLocalPart().equals(CHEATSHEET_CATALOG)) {
						String name = null;
						String provider = null;
						String description = null;
						String type = null;
						String url = null;
						for (Iterator<?> it = startElement.getAttributes(); it.hasNext();) {
							Attribute attribute = (Attribute) it.next();
							name = name == null ? getAttributeValue(NAME, attribute) : name;
							provider = provider == null ? getAttributeValue(PROVIDER, attribute) : provider;
							description = description == null ? getAttributeValue(DESCRIPTION, attribute) : description;
							type = type == null ? getAttributeValue(TYPE, attribute) : type;
							url = url == null ? getAttributeValue(URL, attribute) : url;
						}
						result = new CheatSheetCatalog(name, provider);
						CheatSheetCatalogReferenceType cheatSheetCatalogReferenceType = type == null ? null : CheatSheetCatalogReferenceType.valueOf(type);
						if (cheatSheetCatalogReferenceType != null && url != null) {
							result.setReference(new CheatSheetCatalogReference(cheatSheetCatalogReferenceType, url));
						}
						result.setDescription(description);
						continue;
					}
					if (startElement.getName().getLocalPart().equals(CHEATSHEET_REFERENCE)) {
						String id = null;
						String name = null;
						String url = null;
						String category = null;
						String[] tags = null;
						String description = null;
						String type = null;
						for (Iterator<?> it = startElement.getAttributes(); it.hasNext();) {
							Attribute attribute = (Attribute) it.next();
							id = id == null ? getAttributeValue(ID, attribute) : id;
							name = name == null ? getAttributeValue(NAME, attribute) : name;
							description = description == null ? getAttributeValue(DESCRIPTION, attribute) : description;
							url = url == null ? getAttributeValue(URL, attribute) : url;
							type = type == null ? getAttributeValue(TYPE, attribute) : type;
							category = category == null ? getAttributeValue(CATEGORY, attribute) : category;
							if (tags == null) {
								String tagsString = getAttributeValue(TAGS, attribute);
								tags = tagsString == null ? null : tagsString.split(",");
							}
						}
						CheatSheetCategory cheatSheetCategory = null;
						if (registeredCategories.containsKey(category)) {
							cheatSheetCategory = registeredCategories.get(category);
						} else {
							cheatSheetCategory = new CheatSheetCategory(category);
							registeredCategories.put(cheatSheetCategory.getName(), cheatSheetCategory);
						}
						CheatSheetReferenceType cheatSheetReferenceType = type == null ? null : CheatSheetReferenceType.valueOf(type);
						CheatSheetReference cheatSheetReference = null;
						if (cheatSheetReferenceType != null && url != null) {
							cheatSheetReference = new CheatSheetReference(cheatSheetReferenceType, id, name, url, new Tags(tags));
						} else {
							if (url.startsWith("http")) {
								cheatSheetReference = new CheatSheetReference(CheatSheetReferenceType.HTTP, id, name, url, new Tags(tags));
							} else if (url.startsWith("platform")) {
								cheatSheetReference = new CheatSheetReference(CheatSheetReferenceType.PLATFORM, id, name, url, new Tags(tags));
							} else {
								cheatSheetReference = new CheatSheetReference(CheatSheetReferenceType.FILE_SYSTEM, id, name, url, new Tags(tags));
							}
						}
						cheatSheetReference.setDescription(description);
						if (!result.hasChild(cheatSheetCategory.getUID())) {
							cheatSheetCategory.addChild(cheatSheetReference);
							result.addChild(cheatSheetCategory);
						} else {
							result.getChild(cheatSheetCategory.getUID()).addChild(cheatSheetReference);
							cheatSheetCategory = null;
						}
						continue;
					}
				}
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	private String getAttributeValue(String attributeName, Attribute attribute) {
		String result = null;
		if (attribute.getName().getLocalPart().equals(attributeName)) {
			result = attribute.getValue();
		}
		return result;
	}

	public CheatSheetCatalog parseXmlCatalog(String inputString) {
		InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
		return parseXmlCatalog(inputStream);
	}

	public String toXmlCatalog(CheatSheetCatalog cheatSheetCatalog) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		toXmlCatalog(cheatSheetCatalog, outputStream);
		return new String(outputStream.toByteArray());
	}

	public void toXmlCatalog(CheatSheetCatalog cheatSheetCatalog, OutputStream outputStream) {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		try {
			XMLEventWriter parser = factory.createXMLEventWriter(outputStream);
			parser.add(eventFactory.createStartDocument(UTF_8, XML_VERSION_1_0));
			parser.add(eventFactory.createIgnorableSpace("\n"));
			createCatalog(cheatSheetCatalog, eventFactory, parser);
			parser.add(eventFactory.createEndDocument());
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	private void createCatalog(CheatSheetCatalog cheatSheetCatalog, XMLEventFactory eventFactory, XMLEventWriter parser) throws XMLStreamException {
		Set<Attribute> attributes = new LinkedHashSet<Attribute>();
		attributes.add(eventFactory.createAttribute(new QName(NAME), cheatSheetCatalog.getName()));
		attributes.add(eventFactory.createAttribute(new QName(PROVIDER), cheatSheetCatalog.getProvider()));
		if (cheatSheetCatalog.getReference() != null) {
			attributes.add(eventFactory.createAttribute(new QName(TYPE), cheatSheetCatalog.getReference().getReferenceType().name()));
			attributes.add(eventFactory.createAttribute(new QName(URL), cheatSheetCatalog.getReference().getUri()));
		}
		parser.add(eventFactory.createStartElement(new QName(CHEATSHEET_CATALOG), attributes.iterator(), null));
		IModelItemVisitor cheatsheetReferenceCreationVisitor = new CheatSheetReferenceCreationVisitor(parser, eventFactory);
		cheatSheetCatalog.accept(cheatsheetReferenceCreationVisitor);
		parser.add(eventFactory.createEndElement(new QName(CHEATSHEET_CATALOG), null));
	}

	public List<CheatSheetCatalog> parseXmlCatalogs(InputStream inputStream) {
		List<CheatSheetCatalog> result = new ArrayList<CheatSheetCatalog>();
		XMLInputFactory factory = XMLInputFactory.newInstance();
		CheatSheetCatalog tmpCatalog = null;
		try {
			XMLEventReader parser = factory.createXMLEventReader(inputStream);
			Map<String, CheatSheetCategory> registeredCategories = new HashMap<String, CheatSheetCategory>();
			while (true) {
				XMLEvent event = parser.nextEvent();
				if (event.isEndDocument()) {
					parser.close();
					break;
				}
				if (event.isEndElement()) {
					EndElement startElement = event.asEndElement();
					if (startElement.getName().getLocalPart().equals(CHEATSHEET_CATALOG)) {
						result.add(tmpCatalog);
					}
				}
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					if (startElement.getName().getLocalPart().equals(CHEATSHEET_CATALOG)) {
						String name = null;
						String provider = null;
						String description = null;
						String type = null;
						String url = null;
						for (Iterator<?> it = startElement.getAttributes(); it.hasNext();) {
							Attribute attribute = (Attribute) it.next();
							name = name == null ? getAttributeValue(NAME, attribute) : name;
							provider = provider == null ? getAttributeValue(PROVIDER, attribute) : provider;
							description = description == null ? getAttributeValue(DESCRIPTION, attribute) : description;
							type = type == null ? getAttributeValue(TYPE, attribute) : type;
							url = url == null ? getAttributeValue(URL, attribute) : url;
						}
						tmpCatalog = new CheatSheetCatalog(name, provider);
						CheatSheetCatalogReferenceType cheatSheetCatalogReferenceType = type == null ? null : CheatSheetCatalogReferenceType.valueOf(type);
						if (cheatSheetCatalogReferenceType != null && url != null) {
							tmpCatalog.setReference(new CheatSheetCatalogReference(cheatSheetCatalogReferenceType, url));
						}
						tmpCatalog.setDescription(description);

						continue;
					}
					if (startElement.getName().getLocalPart().equals(CHEATSHEET_REFERENCE)) {
						String id = null;
						String name = null;
						String url = null;
						String category = null;
						String[] tags = null;
						String description = null;
						String type = null;
						for (Iterator<?> it = startElement.getAttributes(); it.hasNext();) {
							Attribute attribute = (Attribute) it.next();
							id = id == null ? getAttributeValue(ID, attribute) : id;
							name = name == null ? getAttributeValue(NAME, attribute) : name;
							description = description == null ? getAttributeValue(DESCRIPTION, attribute) : description;
							url = url == null ? getAttributeValue(URL, attribute) : url;
							type = type == null ? getAttributeValue(TYPE, attribute) : type;
							category = category == null ? getAttributeValue(CATEGORY, attribute) : category;
							if (tags == null) {
								String tagsString = getAttributeValue(TAGS, attribute);
								tags = tagsString == null ? null : tagsString.split(",");
							}
						}
						CheatSheetCategory cheatSheetCategory = null;
						if (registeredCategories.containsKey(category)) {
							cheatSheetCategory = registeredCategories.get(category);
						} else {
							cheatSheetCategory = new CheatSheetCategory(category);
							registeredCategories.put(cheatSheetCategory.getName(), cheatSheetCategory);
						}
						CheatSheetReferenceType cheatSheetReferenceType = type == null ? null : CheatSheetReferenceType.valueOf(type);
						CheatSheetReference cheatSheetReference = null;
						if (cheatSheetReferenceType != null && url != null) {
							cheatSheetReference = new CheatSheetReference(cheatSheetReferenceType, id, name, url, new Tags(tags));
						} else {
							if (url.startsWith("http")) {
								cheatSheetReference = new CheatSheetReference(CheatSheetReferenceType.HTTP, id, name, url, new Tags(tags));
							} else if (url.startsWith("platform")) {
								cheatSheetReference = new CheatSheetReference(CheatSheetReferenceType.PLATFORM, id, name, url, new Tags(tags));
							} else {
								cheatSheetReference = new CheatSheetReference(CheatSheetReferenceType.FILE_SYSTEM, id, name, url, new Tags(tags));
							}
						}
						cheatSheetReference.setDescription(description);
						if (!tmpCatalog.hasChild(cheatSheetCategory.getUID())) {
							cheatSheetCategory.addChild(cheatSheetReference);
							tmpCatalog.addChild(cheatSheetCategory);
						} else {
							tmpCatalog.getChild(cheatSheetCategory.getUID()).addChild(cheatSheetReference);
							cheatSheetCategory = null;
						}
						continue;
					}
				}
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			tmpCatalog = null;
		}
		return result;
	}

	public List<CheatSheetCatalog> parseXmlCatalogs(String inputString) {
		InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
		return parseXmlCatalogs(inputStream);
	}

	public String toXmlCatalogs(List<CheatSheetCatalog> cheatSheetCatalogs) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		toXmlCatalogs(cheatSheetCatalogs, outputStream);
		return new String(outputStream.toByteArray());
	}

	public void toXmlCatalogs(List<CheatSheetCatalog> cheatSheetCatalogs, OutputStream outputStream) {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		try {
			XMLEventWriter parser = factory.createXMLEventWriter(outputStream);
			parser.add(eventFactory.createStartDocument(UTF_8, XML_VERSION_1_0));
			parser.add(eventFactory.createIgnorableSpace("\n"));
			parser.add(eventFactory.createStartElement(new QName(CHEATSHEET_CATALOGS), null, null));
			for (CheatSheetCatalog cheatSheetCatalog : cheatSheetCatalogs) {
				createCatalog(cheatSheetCatalog, eventFactory, parser);
			}
			parser.add(eventFactory.createEndElement(new QName(CHEATSHEET_CATALOGS), null));
			parser.add(eventFactory.createEndDocument());
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

	}
}
