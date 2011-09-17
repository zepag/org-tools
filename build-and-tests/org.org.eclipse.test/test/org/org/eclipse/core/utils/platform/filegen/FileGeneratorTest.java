package org.org.eclipse.core.utils.platform.filegen;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class FileGeneratorTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testVelocityFileGenerator() throws Exception {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("person", "zepagé");
		parameters.put("activity", "Bowling");
		IFileGeneratorInput generatorInput = new VelocityFileGeneratorInput(parameters, "Hello $person,\n planned $activity lately?", "UTF-8");
		IFileGenerator fileGenerator = new VelocityFileGenerator();
		String result = fileGenerator.generate(generatorInput, "UTF-8");
		Assert.assertFalse(result.trim().equals(""));
		Assert.assertEquals("Hello zepagé,\n planned Bowling lately?", result);

		Map<String, String> parameters2 = paramsFromProperties("filegen-velocity.properties");
		IFileGeneratorInput generatorInput2 = new VelocityFileGeneratorInput(parameters2, streamFromFile("filegen-velocity.vm"));
		IFileGenerator fileGenerator2 = new VelocityFileGenerator();
		String result2 = fileGenerator2.generate(generatorInput2, "UTF-8");
		Assert.assertFalse(result2.trim().equals(""));
		Assert.assertEquals("Hello zepagé, planned Bowling lately?", result2);
	}

	private Map<String, String> paramsFromProperties(String string) throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(streamFromFile(string));
		Map<String, String> map = new HashMap<String, String>();
		for (Object key : properties.keySet()) {
			map.put((String) key, (String) properties.get(key));
		}
		return map;
	}

	private InputStream streamFromFile(String string) throws FileNotFoundException {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(string);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFreemarkerFileGenerator() throws Exception {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("person", "zepagé");
		parameters.put("activity", "Bowling");
		IFileGeneratorInput generatorInput = new FreemarkerFileGeneratorInput(parameters, "Hello ${person},\n planned ${activity} lately?", "UTF-8");
		IFileGenerator fileGenerator = new FreemarkerFileGenerator();
		String result = fileGenerator.generate(generatorInput, "UTF-8");
		Assert.assertFalse(result.trim().equals(""));
		Assert.assertEquals("Hello zepagé,\n planned Bowling lately?", result);

		Map<String, String> parameters2 = paramsFromProperties("filegen-freemarker.properties");
		IFileGeneratorInput generatorInput2 = new FreemarkerFileGeneratorInput(parameters2, streamFromFile("filegen-freemarker.fm"));
		IFileGenerator fileGenerator2 = new FreemarkerFileGenerator();
		String result2 = fileGenerator2.generate(generatorInput2, "UTF-8");
		Assert.assertFalse(result2.trim().equals(""));
		Assert.assertEquals("Hello zepagé, planned Bowling lately?", result2);
	}
}