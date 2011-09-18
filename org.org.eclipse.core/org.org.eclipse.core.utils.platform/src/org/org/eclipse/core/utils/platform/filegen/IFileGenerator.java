package org.org.eclipse.core.utils.platform.filegen;

import java.io.OutputStream;

public interface IFileGenerator<T extends IFileGeneratorInput> {
	public static final String TARGET_PATH_PROPERTY="fileGeneratorTargetPath";
	public static final String TARGET_FILENAME_PROPERTY="fileGeneratorTargetFileName";
	void generate(T generatorInput, OutputStream outputStream) throws FileGenerationException;

	String generate(T generatorInput, String encoding) throws FileGenerationException;

}
