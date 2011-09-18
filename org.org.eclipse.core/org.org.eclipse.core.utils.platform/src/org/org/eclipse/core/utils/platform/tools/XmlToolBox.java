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
package org.org.eclipse.core.utils.platform.tools;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.org.eclipse.core.utils.platform.PlatformUtilsException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This is just a Utility class for Xml operations.
 * 
 * @author pagregoire
 */
public final class XmlToolBox {
	private XmlToolBox() {
	}

	/**
	 * @param xmlSource
	 * @param xslInputStream
	 * @return
	 */
	public static ByteArrayOutputStream transform(InputStream xmlSource, InputStream xslInputStream) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			Result result = new StreamResult(baos);
			Source xsltSource = new StreamSource(xslInputStream);
			Transformer transformer = TransformerFactory.newInstance().newTransformer(xsltSource);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.transform(new StreamSource(xmlSource), result);
		} catch (TransformerConfigurationException tce) {
			throw new PlatformUtilsException(tce);
		} catch (TransformerException te) {
			throw new PlatformUtilsException(te);
		}

		return baos;
	}

	public static XMLWriter getXmlWriter(OutputStream os) {
		XMLWriter writer = null;
		try {
			writer = new XMLWriter(os);
		} catch (UnsupportedEncodingException e) {
			throw new PlatformUtilsException(e);
		}
		return writer;
	}

	private static void appendEscapedChar(StringBuffer buffer, char c) {
		String replacement = getReplacement(c);
		if (replacement != null) {
			buffer.append('&');
			buffer.append(replacement);
			buffer.append(';');
		} else {
			buffer.append(c);
		}
	}

	private static String getEscaped(String s) {
		StringBuffer result = new StringBuffer(s.length() + 10);
		for (int i = 0; i < s.length(); ++i)
			appendEscapedChar(result, s.charAt(i));
		return result.toString();
	}

	private static String getReplacement(char c) {
		// Encode special XML characters into the equivalent character references.
		// These five are defined by default for all XML documents.
		switch (c) {
		case '<':
			return "lt"; //$NON-NLS-1$
		case '>':
			return "gt"; //$NON-NLS-1$
		case '"':
			return "quot"; //$NON-NLS-1$
		case '\'':
			return "apos"; //$NON-NLS-1$
		case '&':
			return "amp"; //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * A simple XML writer.
	 */
	public static class XMLWriter extends PrintWriter {

		public static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

		protected int tab;

		public XMLWriter(OutputStream output) throws UnsupportedEncodingException {
			super(new OutputStreamWriter(output, "UTF8"));
			tab = 0;
			println(XML_VERSION);
		}

		public void endTag(String name) {
			tab--;
			printTag('/' + name, null);
		}

		public void printSimpleTag(String name, Object value) {
			if (value != null) {
				printTag(name, null, true, false);
				print(getEscaped(String.valueOf(value)));
				printTag('/' + name, null, false, true);
			}
		}

		public void printTabulation() {
			for (int i = 0; i < tab; i++)
				super.print('\t');
		}

		public void printTag(String name, Map<String, Object> parameters) {
			printTag(name, parameters, true, true);
		}

		public void printTag(String name, Map<String, Object> parameters, boolean tab, boolean newLine) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("<");
			buffer.append(name);
			if (parameters != null) {
				for (String key : parameters.keySet()) {
					buffer.append(" ");
					buffer.append(key);
					buffer.append("=\"");
					buffer.append(getEscaped(String.valueOf(parameters.get(key))));
					buffer.append("\"");
				}
			}
			buffer.append(">");
			if (tab) {
				printTabulation();
			}
			if (newLine) {
				println(buffer.toString());
			} else {
				print(buffer.toString());
			}
		}

		public void startTag(String name, Map<String, Object> parameters) {
			startTag(name, parameters, true);
		}

		public void startTag(String name, Map<String, Object> parameters, boolean newLine) {
			printTag(name, parameters, true, newLine);
			tab++;
		}
	}

	public static String xPathRetrieveString(String xPathExpression, InputStream inputStream) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		InputSource inputSource = new InputSource(inputStream);
		String result = (String) xPath.evaluate(xPathExpression, inputSource, XPathConstants.STRING);
		return result;
	}

	public static Boolean xPathRetrieveBoolean(String xPathExpression, InputStream inputStream) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		InputSource inputSource = new InputSource(inputStream);
		Boolean result = (Boolean) xPath.evaluate(xPathExpression, inputSource, XPathConstants.BOOLEAN);
		return result;
	}

	public static Double xPathRetrieveNumber(String xPathExpression, InputStream inputStream) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		InputSource inputSource = new InputSource(inputStream);
		Double result = (Double) xPath.evaluate(xPathExpression, inputSource, XPathConstants.NUMBER);
		return result;
	}

	public static Node xPathRetrieveNode(String xPathExpression, InputStream inputStream) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		InputSource inputSource = new InputSource(inputStream);
		Node result = (Node) xPath.evaluate(xPathExpression, inputSource, XPathConstants.NODE);
		return result;
	}

	public static NodeList xPathRetrieveNodeList(String xPathExpression, InputStream inputStream) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		InputSource inputSource = new InputSource(inputStream);
		NodeList result = (NodeList) xPath.evaluate(xPathExpression, inputSource, XPathConstants.NODESET);
		return result;
	}

	public static void xPathAlterNode(String xPathExpression, String alteredValue, InputStream inputStream, OutputStream outputStream) throws XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		InputSource inputSource = new InputSource(inputStream);
		Node result = (Node) xPath.evaluate(xPathExpression, inputSource, XPathConstants.NODE);
		result.setTextContent(alteredValue);
		Document document = result.getOwnerDocument();

		// Prepare the DOM document for writing
		Source source = new DOMSource(document);

		// Prepare the output file
		Result streamResult = new StreamResult(outputStream);

		// Write the DOM document to the file
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.transform(source, streamResult);

	}

	public static void xPathInsertNode(String xPathExpression, String nodeStringDefinition, InputStream inputStream, OutputStream outputStream) throws XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		InputSource inputSource = new InputSource(inputStream);
		Node result = (Node) xPath.evaluate(xPathExpression, inputSource, XPathConstants.NODE);
		Document document = result.getOwnerDocument();

		InputSource nodeInputSource = new InputSource(new StringReader(nodeStringDefinition));
		NodeList nodeList = (NodeList) xPath.evaluate("/", nodeInputSource, XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); i++) {

			Node item = nodeList.item(i);
			if (!(item instanceof Document)) {
				item = document.importNode(item, true);
				result.appendChild(item);
			} else {
				item = document.importNode(((Document) item).getFirstChild(), true);
				result.appendChild(item);
			}
		}

		// Prepare the DOM document for writing
		Source source = new DOMSource(document);

		// Prepare the output file
		Result streamResult = new StreamResult(outputStream);

		// Write the DOM document to the file
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.transform(source, streamResult);

	}

	public static void xPathInsertAttribute(String xPathExpression, String attributeName, String attributeValue, InputStream inputStream, OutputStream outputStream) throws XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		InputSource inputSource = new InputSource(inputStream);
		Node result = (Node) xPath.evaluate(xPathExpression, inputSource, XPathConstants.NODE);
		if (!(result instanceof Element))
			throw new IllegalArgumentException("XPath Expression should point to an Xml Element.");
		Document document = result.getOwnerDocument();
		((Element) result).setAttribute(attributeName, attributeValue);
		// Prepare the DOM document for writing
		Source source = new DOMSource(document);

		// Prepare the output file
		Result streamResult = new StreamResult(outputStream);

		// Write the DOM document to the file
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.transform(source, streamResult);

	}

	public static void xPathReplaceNode(String xPathExpression, String nodeStringDefinition, InputStream inputStream, OutputStream outputStream) throws XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		InputSource inputSource = new InputSource(inputStream);
		Node result = (Node) xPath.evaluate(xPathExpression, inputSource, XPathConstants.NODE);
		Node parent = result.getParentNode();
		parent.removeChild(result);
		Document document = result.getOwnerDocument();

		InputSource nodeInputSource = new InputSource(new StringReader(nodeStringDefinition));
		NodeList nodeList = (NodeList) xPath.evaluate("/", nodeInputSource, XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); i++) {

			Node item = nodeList.item(i);
			if (!(item instanceof Document)) {
				item = document.importNode(item, true);
				parent.appendChild(item);
			} else {
				item = document.importNode(((Document) item).getFirstChild(), true);
				parent.appendChild(item);
			}
		}

		// Prepare the DOM document for writing
		Source source = new DOMSource(document);

		// Prepare the output file
		Result streamResult = new StreamResult(outputStream);

		// Write the DOM document to the file
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.transform(source, streamResult);

	}

	public static void xPathRemoveNode(String xPathExpression, InputStream inputStream, OutputStream outputStream) throws XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		InputSource inputSource = new InputSource(inputStream);
		Node result = (Node) xPath.evaluate(xPathExpression, inputSource, XPathConstants.NODE);
		Node parent = result.getParentNode();
		parent.removeChild(result);
		Document document = result.getOwnerDocument();
		// Prepare the DOM document for writing
		Source source = new DOMSource(document);

		// Prepare the output file
		Result streamResult = new StreamResult(outputStream);

		// Write the DOM document to the file
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.transform(source, streamResult);

	}
}