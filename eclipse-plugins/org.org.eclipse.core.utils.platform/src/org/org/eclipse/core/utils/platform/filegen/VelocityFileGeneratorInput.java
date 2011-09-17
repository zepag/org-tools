package org.org.eclipse.core.utils.platform.filegen;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class VelocityFileGeneratorInput implements IFileGeneratorInput {
	private final Map<String, String> parameters;
	private final InputStream templateInputStream;

	public VelocityFileGeneratorInput(Map<String, String> parameters, InputStream templateInputStream) {
		super();
		this.parameters = parameters;
		this.templateInputStream = templateInputStream;
	}

	public VelocityFileGeneratorInput(Map<String, String> parameters, String templateString, String encoding) throws UnsupportedEncodingException {
		super();
		this.parameters = parameters;
		this.templateInputStream = new ByteArrayInputStream(templateString.getBytes(encoding));
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public InputStream getTemplateInputStream() {
		return templateInputStream;
	}

}