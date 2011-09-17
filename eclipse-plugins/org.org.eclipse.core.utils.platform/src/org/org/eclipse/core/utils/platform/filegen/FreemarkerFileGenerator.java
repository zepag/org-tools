package org.org.eclipse.core.utils.platform.filegen;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerFileGenerator implements IFileGenerator<FreemarkerFileGeneratorInput> {

	private final class InputStreamTemplateLoader implements TemplateLoader {
		private InputStream templateInputStream;

		public InputStreamTemplateLoader(InputStream templateInputStream) {
			this.templateInputStream = templateInputStream;
		}

		public Reader getReader(Object templateSource, String encoding) throws IOException {
			return new BufferedReader(new InputStreamReader(templateInputStream, "UTF-8"));
		}

		public long getLastModified(Object templateSource) {
			return -1;
		}

		public Object findTemplateSource(String name) throws IOException {
			return templateInputStream;
		}

		public void closeTemplateSource(Object templateSource) throws IOException {

		}
	}

	public void generate(FreemarkerFileGeneratorInput generatorInput, OutputStream outputStream) throws FileGenerationException {
		Configuration cfg = new Configuration();
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		TemplateLoader templateLoader = new InputStreamTemplateLoader(generatorInput.getTemplateInputStream());
		cfg.setTemplateLoader(templateLoader);
		cfg.setDefaultEncoding("UTF-8");
		Template template = null;
		try {
			template = cfg.getTemplate("");
		} catch (IOException e) {
			throw new FileGenerationException("Error while reading template", e);
		}
		Writer writer;
		try {
			writer = new OutputStreamWriter(outputStream, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			throw new FileGenerationException("Error while starting to write result", e1);
		}
		try {
			template.process(generatorInput.getParameters(), writer);
		} catch (TemplateException e) {
			throw new FileGenerationException("Error while processing template", e);

		} catch (IOException e) {
			throw new FileGenerationException("Error while writing generated content", e);
		}
	}

	public String generate(FreemarkerFileGeneratorInput generatorInput, String encoding) throws FileGenerationException {
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

}
