package org.org.eclipse.dws.ui.maven2.internal.xml;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Assert;
import org.junit.Test;
import org.org.eclipse.dws.core.internal.PomFileSaxHandler;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.PomPropertiesSet;
import org.org.eclipse.dws.core.internal.model.PomProperty;
import org.org.eclipse.dws.core.internal.xml.IXmlPomFileBinder;
import org.org.eclipse.dws.core.internal.xml.StaxPomFileBinder;

public class IXmlPomFileBinderTest {

	/**
	 * This test is meant to be pruned when Sax handler is completely deprecated.
	 */
	@Test
	public void testPropertiesParsing() {
		try {
			// New Stax Binder parsing
			InputStream inputStream2 = Thread.currentThread().getContextClassLoader().getResourceAsStream("test-pom.xml");
			IXmlPomFileBinder xmlPomFileBinder = new StaxPomFileBinder();
			final Pom newResult = xmlPomFileBinder.parsePomFile(inputStream2);
			PomProperty property = new PomProperty("prop1", "prop1Value");
			PomProperty property2 = new PomProperty("prop2", "prop2Value", "prop4Value");
			PomProperty property3 = new PomProperty("prop3", "prop5Value");
			PomPropertiesSet expectedProperties = new PomPropertiesSet();
			expectedProperties.addProperty(property);
			expectedProperties.addProperty(property2);
			expectedProperties.addProperty(property3);

			// ASSERT THAT RESULT IS THE SAME
			Assert.assertEquals(expectedProperties, newResult.getProperties());
		} catch (Exception e) {
			Assert.fail("An exception should not occur there: " + e.getMessage());
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testStaxParserForNonRegression() {
		try {
			// Former Sax Handler parsing
			InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test-pom.xml");
			PomFileSaxHandler pomFileSaxHandler = new PomFileSaxHandler();
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(inputStream, pomFileSaxHandler);
			final Pom formerResult = pomFileSaxHandler.getPomParsingDescription();

			// New Stax Binder parsing
			InputStream inputStream2 = Thread.currentThread().getContextClassLoader().getResourceAsStream("test-pom.xml");
			IXmlPomFileBinder xmlPomFileBinder = new StaxPomFileBinder();
			final Pom newResult = xmlPomFileBinder.parsePomFile(inputStream2);

			// ASSERT THAT RESULT IS THE SAME
			Assert.assertEquals(formerResult, newResult);
			Assert.assertEquals(formerResult.getParentPom(), newResult.getParentPom());
			Assert.assertEquals(formerResult.toString(), newResult.toString());
			Assert.assertEquals(formerResult.getRepositories(), newResult.getRepositories());
		} catch (Exception e) {
			Assert.fail("An exception should not occur there: " + e.getMessage());
		}
	}
}