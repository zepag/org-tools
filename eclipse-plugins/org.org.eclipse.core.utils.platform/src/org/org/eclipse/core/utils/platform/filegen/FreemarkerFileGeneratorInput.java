package org.org.eclipse.core.utils.platform.filegen;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;


public class FreemarkerFileGeneratorInput implements IFileGeneratorInput {

	private final Map<String, String> parameters;
	private InputStream templateInputStream;

	public FreemarkerFileGeneratorInput(Map<String, String> parameters, String templateString, String encoding) throws UnsupportedEncodingException {
		this.parameters = parameters;
		this.templateInputStream = new ByteArrayInputStream(templateString.getBytes(encoding));
	}

	public FreemarkerFileGeneratorInput(Map<String, String> parameters, InputStream templateInputStream) {
		this.parameters = parameters;
		this.templateInputStream = templateInputStream;
	}

	public InputStream getTemplateInputStream() {
		return templateInputStream;
	}

	public void setTemplateInputStream(InputStream templateInputStream) {
		this.templateInputStream = templateInputStream;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}
}
