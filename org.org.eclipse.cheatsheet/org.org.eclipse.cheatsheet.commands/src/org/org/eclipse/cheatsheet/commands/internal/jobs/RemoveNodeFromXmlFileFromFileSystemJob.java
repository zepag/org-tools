package org.org.eclipse.cheatsheet.commands.internal.jobs;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.tools.XmlToolBox;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;

public class RemoveNodeFromXmlFileFromFileSystemJob extends Job {
	private static String JOB_ID = "CheatSheet helper: remove node from xml file in filesystem";
	private final String targetXPath;
	private final String targetFile;

	public RemoveNodeFromXmlFileFromFileSystemJob(String targetXPath, String targetFile) {
		super(JOB_ID);
		this.targetXPath = targetXPath;
		this.targetFile = targetFile;
		this.setPriority(Job.SHORT);
		this.setUser(true);
		this.setRule(new BatchSimilarRule(CheatSheetJobs.JOB_FAMILY));
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus result = new StatusInfo(IStatus.OK, "XML File updated successfully.");
		try {
			generateFile(monitor);
		} catch (Throwable e) {
			monitor.setCanceled(true);
			result = new StatusInfo(IStatus.ERROR, "A problem occured while updating xml file:\n" + e.getMessage());
		}
		monitor.done();
		return result;
	}

	private void generateFile(IProgressMonitor monitor) throws IOException, XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
		StringBuilder originalFile = readOriginalFile();
		InputStream inputStream = new ByteArrayInputStream(originalFile.toString().getBytes());
		OutputStream outputStream = new FileOutputStream(targetFile);
		XmlToolBox.xPathRemoveNode(targetXPath, inputStream, outputStream);
	}

	private StringBuilder readOriginalFile() throws IOException {
		StringBuilder nodeDefinition = new StringBuilder();
		InputStream nodeDefinitionInputStream = null;
		try {
			nodeDefinitionInputStream = new FileInputStream(targetFile);
			InputStreamReader reader = new InputStreamReader(nodeDefinitionInputStream, "UTF-8");
			char[] buff = new char[256];
			int numberRead = 0;
			while ((numberRead = reader.read(buff)) != -1) {
				for (int i = 0; i < numberRead; i++) {
					nodeDefinition.append(buff[i]);
				}
			}
		} finally {
			try {
				nodeDefinitionInputStream.close();
			} catch (Throwable e) {
			}
		}
		return nodeDefinition;
	}
}
