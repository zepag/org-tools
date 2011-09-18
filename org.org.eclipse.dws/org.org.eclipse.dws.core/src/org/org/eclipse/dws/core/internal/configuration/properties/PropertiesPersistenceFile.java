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
package org.org.eclipse.dws.core.internal.configuration.properties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.org.eclipse.core.ui.BaseConstants;
import org.org.eclipse.core.utils.platform.preferences.HiddenFolderHandler;
import org.org.eclipse.core.utils.platform.tools.FileToolBox;
import org.org.eclipse.core.utils.platform.tools.XmlToolBox;
import org.org.eclipse.dws.core.DWSCorePlugin;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The Class PropertiesPersistenceFile.
 * 
 * @author pagregoire
 */
public class PropertiesPersistenceFile {

	/** The Constant FILE_NAME. */
	public final static String FILE_NAME = DWSCorePlugin.class.getName() + ".xml";

	/** The ROO t_ tag. */
	private static String ROOT_TAG = "maven2";

	/** The PROPERT y_ tag. */
	private static String PROPERTY_TAG = "property";

	/** The PROPERT y_ ta g_ nam e_ attribute. */
	private static String PROPERTY_TAG_NAME_ATTRIBUTE = "name";

	/** The PROPERT y_ ta g_ valu e_ attribute. */
	private static String PROPERTY_TAG_VALUE_ATTRIBUTE = "value";

	/**
	 * Save properties.
	 * 
	 * @param properties
	 *            the properties
	 * @param project
	 *            the project
	 */
	public static void saveProperties(Map<String,String> properties, IProject project) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XmlToolBox.XMLWriter writer = XmlToolBox.getXmlWriter(baos);
		writer.startTag(ROOT_TAG, null, true);
		for (Iterator<?> it = properties.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			String value = (String) properties.get(key);
			Map<String, Object> attr = new HashMap<String, Object>();
			attr.put(PROPERTY_TAG_NAME_ATTRIBUTE, key);
			attr.put(PROPERTY_TAG_VALUE_ATTRIBUTE, value);
			writer.printTag(PROPERTY_TAG, attr, true, true);
			writer.endTag(PROPERTY_TAG);
		}
		writer.endTag(ROOT_TAG);
		writer.flush();
		writer.close();
		try {
			baos.close();
		} catch (IOException e) {
		}
		HiddenFolderHandler.checkGlobalHiddenFolder(BaseConstants.HIDDEN_FOLDER_NAME, project, null);
		InputStream contents = new ByteArrayInputStream(baos.toByteArray());
		FileToolBox.createOrUpdateFile(project, BaseConstants.HIDDEN_FOLDER_NAME + "/" + FILE_NAME, contents);
		try {
			contents.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Load properties.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the map
	 */
	public static Map<String,String> loadProperties(IProject project) {
		Map<String,String> properties = new HashMap<String,String>();
		IFile file = FileToolBox.getFile(project, BaseConstants.HIDDEN_FOLDER_NAME + "/" + FILE_NAME);
		InputStream contents = null;
		if (file != null) {
			try {
				contents = file.getContents();
				SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
				PropertiesPersistenceFileHandler persistentFileHandler = new PropertiesPersistenceFileHandler();
				parser.parse(contents, persistentFileHandler);
				properties = persistentFileHandler.getProperties();
				try {
					contents.close();
				} catch (IOException e) {
				}
			} catch (Exception e) {
				properties = null;
			}
		}
		return properties;
	}

	/**
	 * The Class PropertiesPersistenceFileHandler.
	 */
	static class PropertiesPersistenceFileHandler extends DefaultHandler {

		/** The properties. */
		private Map<String, String> properties = new HashMap<String, String>();

		/**
		 * Gets the properties.
		 * 
		 * @return the properties
		 */
		public Map<String, String> getProperties() {
			return properties;
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (qName.equals(PROPERTY_TAG)) {
				properties.put(attributes.getValue(PROPERTY_TAG_NAME_ATTRIBUTE), attributes.getValue(PROPERTY_TAG_VALUE_ATTRIBUTE));
			}
		}
	}
}