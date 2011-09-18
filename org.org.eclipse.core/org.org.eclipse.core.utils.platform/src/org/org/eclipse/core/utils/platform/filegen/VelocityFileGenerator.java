package org.org.eclipse.core.utils.platform.filegen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

public class VelocityFileGenerator implements IFileGenerator<VelocityFileGeneratorInput> {
	public String generate(VelocityFileGeneratorInput generatorInput, String encoding) throws FileGenerationException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		String result = null;
		try {
			generate(generatorInput, byteArrayOutputStream);
			try {
				result = byteArrayOutputStream.toString(encoding);
			} catch (UnsupportedEncodingException e) {
				throw new FileGenerationException("Error while writing template output", e);
			}
		} catch (FileGenerationException e) {
			throw e;
		} finally {
			try {
				byteArrayOutputStream.close();
			} catch (Throwable e) {
				// swallow error
			}
		}
		return result;
	}

	public void generate(VelocityFileGeneratorInput generatorInput, OutputStream outputStream) throws FileGenerationException {
		VelocityEngine velocityEngine = new VelocityEngine();
		// ensures that velocity fails in case of invalid property or directive
		velocityEngine.addProperty("runtime.references.strict", true);
		try {
			velocityEngine.init();
		} catch (Exception e) {
			throw new FileGenerationException("Error while initing Velocity Engine", e);
		}
		Context context = contextFromParameters(generatorInput.getParameters());
		Writer writer;
		Reader reader;
		try {
			writer = new OutputStreamWriter(outputStream, "UTF-8");
			reader = new InputStreamReader(generatorInput.getTemplateInputStream(), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			throw new FileGenerationException("Error while starting to write result", e1);
		}

		try {
			if (!velocityEngine.evaluate(context, writer, VelocityFileGenerator.class.getName(), reader)) {
				throw new FileGenerationException("Error while evaluating template");
			}
		} catch (Exception e) {
			throw new FileGenerationException("Error while evaluating template", e);
		} finally {
			try {
				writer.flush();
			} catch (IOException e) {
				// swallow error
			}

		}
	}

	private Context contextFromParameters(Map<String, String> parameters) {
		Context result = new VelocityContext(parameters);
		return result;
	}
}
