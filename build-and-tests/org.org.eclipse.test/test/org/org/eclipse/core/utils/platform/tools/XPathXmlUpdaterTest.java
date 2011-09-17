package org.org.eclipse.core.utils.platform.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XPathXmlUpdaterTest {

	@Test
	public void testXPathXmlRetrieveString() throws Exception {
		String example = "<a><b>B</b></a>";
		InputStream inputStream = new ByteArrayInputStream(example.getBytes());
		// ////////////
		final String result = XmlToolBox.xPathRetrieveString("/a/b", inputStream);
		// ////////////
		Assert.assertEquals("B", result);
	}

	@Test
	public void testXPathXmlRetrieveBoolean() throws Exception {
		String example = "<a><b>true</b></a>";
		InputStream inputStream = new ByteArrayInputStream(example.getBytes());
		// ////////////
		final Boolean result = XmlToolBox.xPathRetrieveBoolean("/a/b", inputStream);
		// ////////////
		Assert.assertEquals(true, result);
	}

	@Test
	public void testXPathXmlRetrieveNumber() throws Exception {
		String example = "<a><b>1.0</b></a>";
		InputStream inputStream = new ByteArrayInputStream(example.getBytes());
		// ////////////
		final Double result = XmlToolBox.xPathRetrieveNumber("/a/b", inputStream);
		// ////////////
		Assert.assertEquals(1.0d, result);
	}

	@Test
	public void testXPathXmlRetrieveNode() throws Exception {
		String example = "<a><b>glagla</b></a>";
		InputStream inputStream = new ByteArrayInputStream(example.getBytes());
		// ////////////
		final Node result = XmlToolBox.xPathRetrieveNode("/a/b", inputStream);
		// ////////////
		Assert.assertNotNull(result);
		Assert.assertTrue(result instanceof Node);
		Assert.assertEquals("glagla", result.getTextContent());
	}

	@Test
	public void testXPathXmlRetrieveNodeList() throws Exception {
		String example = "<a><b><c>1</c><c>a</c><c>d1</c></b></a>";
		InputStream inputStream = new ByteArrayInputStream(example.getBytes());
		// ////////////
		final NodeList result = XmlToolBox.xPathRetrieveNodeList("/a/b/c", inputStream);
		// ////////////
		Assert.assertNotNull(result);
		Assert.assertTrue(result instanceof NodeList);
		Assert.assertEquals(3, result.getLength());
	}

	@Test
	public void testXPathXmlAlterNode() throws Exception {
		String example = "<a><b>glagla</b></a>";
		InputStream inputStream = new ByteArrayInputStream(example.getBytes());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// ////////////
		XmlToolBox.xPathAlterNode("/a/b", "gloglo", inputStream, outputStream);
		// ////////////
		inputStream.close();
		outputStream.flush();
		outputStream.close();
		final String result = outputStream.toString("UTF-8");
		Assert.assertTrue(result.endsWith("<a><b>gloglo</b></a>"));
	}

	@Test
	public void testXPathXmlAlterAttribute() throws Exception {
		String example = "<a><b attr=\"glagla\">glagla</b></a>";
		InputStream inputStream = new ByteArrayInputStream(example.getBytes());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// ////////////
		XmlToolBox.xPathAlterNode("/a/b/@attr", "gloglo", inputStream, outputStream);
		// ////////////
		inputStream.close();
		outputStream.flush();
		outputStream.close();
		final String result = outputStream.toString("UTF-8");
		Assert.assertTrue(result.endsWith("<a><b attr=\"gloglo\">glagla</b></a>"));
	}

	@Test
	public void testXPathXmlInsertNode() throws Exception {
		String example = "<a><b attr=\"glagla\"></b></a>";
		InputStream inputStream = new ByteArrayInputStream(example.getBytes());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// ////////////
		XmlToolBox.xPathInsertNode("/a/b", "<c>blabla</c>", inputStream, outputStream);
		// ////////////
		inputStream.close();
		outputStream.flush();
		outputStream.close();
		final String result = outputStream.toString("UTF-8");
		Assert.assertTrue(result.endsWith("<a><b attr=\"glagla\"><c>blabla</c></b></a>"));
	}

	@Test
	public void testXPathXmlInsertAttribute() throws Exception {
		String example = "<a><b attr=\"glagla\">blabla</b></a>";
		InputStream inputStream = new ByteArrayInputStream(example.getBytes());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// ////////////
		XmlToolBox.xPathInsertAttribute("/a/b", "name", "blabla", inputStream, outputStream);
		// ////////////
		inputStream.close();
		outputStream.flush();
		outputStream.close();
		final String result = outputStream.toString("UTF-8");
		Assert.assertTrue(result.endsWith("<a><b attr=\"glagla\" name=\"blabla\">blabla</b></a>"));
	}

	@Test
	public void testXPathXmlReplaceNode() throws Exception {
		String example = "<a><b attr=\"glagla\"></b></a>";
		InputStream inputStream = new ByteArrayInputStream(example.getBytes());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// ////////////
		XmlToolBox.xPathReplaceNode("/a/b", "<c>blabla</c>", inputStream, outputStream);
		// ////////////
		inputStream.close();
		outputStream.flush();
		outputStream.close();
		final String result = outputStream.toString("UTF-8");
		Assert.assertTrue(result.endsWith("<a><c>blabla</c></a>"));
	}

	@Test
	public void testXPathXmlRemoveNode() throws Exception {
		String example = "<a><b attr=\"glagla\"></b></a>";
		InputStream inputStream = new ByteArrayInputStream(example.getBytes());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// ////////////
		XmlToolBox.xPathRemoveNode("/a/b", inputStream, outputStream);
		// ////////////
		inputStream.close();
		outputStream.flush();
		outputStream.close();
		final String result = outputStream.toString("UTF-8");
		Assert.assertTrue(result.endsWith("<a/>"));
	}

}