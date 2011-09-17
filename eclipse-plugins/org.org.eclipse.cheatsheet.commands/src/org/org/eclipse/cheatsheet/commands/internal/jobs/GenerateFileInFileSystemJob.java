package org.org.eclipse.cheatsheet.commands.internal.jobs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.core.utils.platform.filegen.FreemarkerFileGenerator;
import org.org.eclipse.core.utils.platform.filegen.FreemarkerFileGeneratorInput;
import org.org.eclipse.core.utils.platform.filegen.IFileGenerator;
import org.org.eclipse.core.utils.platform.filegen.TemplateEngine;
import org.org.eclipse.core.utils.platform.filegen.VelocityFileGenerator;
import org.org.eclipse.core.utils.platform.filegen.VelocityFileGeneratorInput;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.tools.IOToolBox;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;

public class GenerateFileInFileSystemJob extends Job {
	private static String JOB_ID = "CheatSheet helper: generate file in filesystem";
	private final TemplateEngine templateEngine;
	private final URL templateUrl;
	private final Map<String, String> resolvedFields;
	private final String targetFile;

	public GenerateFileInFileSystemJob(TemplateEngine templateEngine, URL templateUrl, Map<String, String> resolvedFields, String targetFile) {
		super(JOB_ID);
		this.templateEngine = templateEngine;
		this.templateUrl = templateUrl;
		this.resolvedFields = Collections.unmodifiableMap(resolvedFields);
		this.targetFile = targetFile;
		this.setPriority(Job.SHORT);
		this.setUser(true);
		this.setRule(new BatchSimilarRule(CheatSheetJobs.JOB_FAMILY));
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus result = new StatusInfo(IStatus.OK, "File created successfully.");
		try {
			generateFile(monitor);
		} catch (Throwable e) {
			monitor.setCanceled(true);
			result = new StatusInfo(IStatus.ERROR, "A problem occured while generating file:\n" + e.getMessage());
		}
		monitor.done();
		return result;
	}

	private void generateFile(IProgressMonitor monitor) throws IOException {
		InputStream templateInputStream = null;
		OutputStream fileOutputStream = null;
		try {
			templateInputStream = templateUrl.openConnection(IOToolBox.determineProxy(templateUrl)).getInputStream();
			fileOutputStream = new FileOutputStream(targetFile);
			if (templateEngine == TemplateEngine.VELOCITY) {
				VelocityFileGeneratorInput generatorInput = new VelocityFileGeneratorInput(resolvedFields, templateInputStream);
				IFileGenerator<VelocityFileGeneratorInput> fileGenerator = new VelocityFileGenerator();
				fileGenerator.generate(generatorInput, fileOutputStream);
			} else if (templateEngine == TemplateEngine.FREEMARKER) {
				FreemarkerFileGeneratorInput generatorInput = new FreemarkerFileGeneratorInput(resolvedFields, templateInputStream);
				IFileGenerator<FreemarkerFileGeneratorInput> fileGenerator = new FreemarkerFileGenerator();
				fileGenerator.generate(generatorInput, "UTF-8");
			}
		} finally {
			try {
				templateInputStream.close();
				fileOutputStream.flush();
				fileOutputStream.close();
			} catch (Throwable e) {
			}
		}
	}
}