/*******************************************************************************
 * Copyright (c) 2008 Pierre-Antoine Grégoire.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pierre-Antoine Grégoire - initial API and implementation
 *******************************************************************************/
package org.org.eclipse.core.utils.platform.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.impl.StandardFileSystemManager;

/**
 * @author pagregoire
 */
public final class ArchivesToolBox {
	public static interface IWriteHinter {
		public WriteMode getFileWriteMode(File file);

		public String alterFileName(File targetFolder, String fileName);

		public String alterFolderName(File targetFolder, String folderName);
	}

	public enum WriteMode {
		SKIP, REPLACE
	}

	private static class NoopFileWriteHinter implements IWriteHinter {

		public WriteMode getFileWriteMode(File targetFile) {
			return WriteMode.REPLACE;
		}

		public String alterFileName(File targetFolder, String fileName) {
			return fileName;
		}

		public String alterFolderName(File targetFolder, String folderName) {
			return folderName;
		}

	}

	public static void decompressArchiveSubPartTo(File archiveFile, File targetFolder, String pathInArchive) throws IOException {
		decompressArchiveSubPartTo(archiveFile, targetFolder, pathInArchive, new NoopFileWriteHinter());
	}

	public static void decompressArchiveSubPartTo(File archiveFile, File targetFolder, String pathInArchive, IWriteHinter writeHinter) throws IOException {
		if (!targetFolder.exists()) {
			throw new FileNotFoundException("Folder does not exist:" + targetFolder);
		}
		if (isZip(archiveFile) || isJar(archiveFile)) {
			decompressZipTo(archiveFile, targetFolder, pathInArchive, writeHinter);
		} else if (isTar(archiveFile)) {
			if (isTgz(archiveFile)) {
				decompressTgzTo(archiveFile, targetFolder, pathInArchive, writeHinter);
			} else if (isTbz2(archiveFile)) {
				decompressTbz2To(archiveFile, targetFolder, pathInArchive, writeHinter);
			} else {
				decompressTarTo(archiveFile, targetFolder, pathInArchive, writeHinter);
			}
		} else if (isGz(archiveFile)) {
			decompressGzTo(archiveFile, targetFolder, pathInArchive, writeHinter);
		} else if (isBz2(archiveFile)) {
			decompressBz2To(archiveFile, targetFolder, pathInArchive, writeHinter);
		}
	}

	public static void decompressArchiveTo(File archiveFile, File targetFolder) throws IOException {
		decompressArchiveSubPartTo(archiveFile, targetFolder, "", new NoopFileWriteHinter());
	}

	public static void decompressArchiveTo(File archiveFile, File targetFolder, IWriteHinter writeHinter) throws IOException {
		decompressArchiveSubPartTo(archiveFile, targetFolder, "", writeHinter);
	}

	public static void decompressBz2To(File archiveFile, File targetFolder, String pathInArchive) throws IOException {
		decompressArchiveTo(archiveFile, targetFolder, "bz2:", "/" + pathInArchive);
	}

	public static void decompressBz2To(File archiveFile, File targetFolder, String pathInArchive, IWriteHinter writeHinter) throws IOException {
		decompressArchiveTo(archiveFile, targetFolder, "bz2:", "/" + pathInArchive, writeHinter);
	}

	public static void decompressGzTo(File archiveFile, File targetFolder, String pathInArchive) throws IOException {
		decompressArchiveTo(archiveFile, targetFolder, "gz:", "/" + pathInArchive);
	}

	public static void decompressGzTo(File archiveFile, File targetFolder, String pathInArchive, IWriteHinter writeHinter) throws IOException {
		decompressArchiveTo(archiveFile, targetFolder, "gz:", "/" + pathInArchive, writeHinter);
	}

	public static void decompressJarTo(File archiveFile, File targetFolder, String pathInArchive) throws IOException {
		decompressArchiveTo(archiveFile, targetFolder, "jar:", "!" + pathInArchive);
	}

	public static void decompressJarTo(File archiveFile, File targetFolder, String pathInArchive, IWriteHinter writeHinter) throws IOException {
		decompressArchiveTo(archiveFile, targetFolder, "jar:", "!" + pathInArchive, writeHinter);
	}

	public static void decompressTarTo(File archiveFile, File targetFolder, String pathInArchive) throws IOException {
		decompressArchiveTo(archiveFile, targetFolder, "tar:", "!" + pathInArchive);
	}

	public static void decompressTarTo(File archiveFile, File targetFolder, String pathInArchive, IWriteHinter writeHinter) throws IOException {
		decompressArchiveTo(archiveFile, targetFolder, "tar:", "!" + pathInArchive, writeHinter);
	}

	public static void decompressTbz2To(File archiveFile, File targetFolder, String pathInArchive) throws IOException {
		decompressArchiveTo(archiveFile, targetFolder, "tbz2:", "!" + pathInArchive);
	}

	public static void decompressTbz2To(File archiveFile, File targetFolder, String pathInArchive, IWriteHinter writeHinter) throws IOException {
		decompressArchiveTo(archiveFile, targetFolder, "tbz2:", "!" + pathInArchive, writeHinter);
	}

	public static void decompressTgzTo(File archiveFile, File targetFolder, String pathInArchive) throws IOException {
		decompressArchiveTo(archiveFile, targetFolder, "tgz:", "!" + pathInArchive);
	}

	public static void decompressTgzTo(File archiveFile, File targetFolder, String pathInArchive, IWriteHinter writeHinter) throws IOException {
		decompressArchiveTo(archiveFile, targetFolder, "tgz:", "!" + pathInArchive, writeHinter);
	}

	public static void decompressZipTo(File archiveFile, File targetFolder, String pathInArchive) throws IOException {
		decompressArchiveTo(archiveFile, targetFolder, "zip:", "!" + pathInArchive);
	}

	public static void decompressZipTo(File archiveFile, File targetFolder, String pathInArchive, IWriteHinter writeHinter) throws IOException {
		decompressArchiveTo(archiveFile, targetFolder, "zip:", "!" + pathInArchive, writeHinter);
	}

	public static boolean isArchive(File file) {
		return isZip(file) || isTar(file) || isGz(file) || isBz2(file) || isJar(file) || isTgz(file) || isTbz2(file);
	}

	public static boolean isBz2(File file) {
		return isFileOpeneableAs(file, "bz2:", "");
	}

	public static boolean isGz(File file) {
		return isFileOpeneableAs(file, "gz:", "");
	}

	public static boolean isJar(File file) {
		return isFileOpeneableAs(file, "jar:", "");
	}

	public static boolean isTar(File file) {
		return isFileOpeneableAs(file, "tar:", "");
	}

	public static boolean isTbz2(File file) {
		return isFileOpeneableAs(file, "tbz2:", "");
	}

	public static boolean isTgz(File file) {
		return isFileOpeneableAs(file, "tgz:", "");
	}

	public static boolean isZip(File file) {
		return isFileOpeneableAs(file, "zip:", "");
	}

	private static FileObject createFileObject(FileSystemManager fileSystemManager, File file, String archiveProtocol, String pathInArchive) throws FileSystemException {
		return fileSystemManager.resolveFile(archiveProtocol + "/" + file.getAbsolutePath() + pathInArchive);
	}

	private static DefaultFileSystemManager createFileSystemManager() throws FileSystemException {
		DefaultFileSystemManager fileSystemManager = new StandardFileSystemManager();
		fileSystemManager.init();
		return fileSystemManager;
	}

	private static void decompressArchiveTo(File archiveFile, File targetFolder, String archiveProtocol, String pathInArchive) throws IOException {
		DefaultFileSystemManager fileSystemManager = createFileSystemManager();
		FileObject fileObject = createFileObject(fileSystemManager, archiveFile, archiveProtocol, pathInArchive);
		decompressFileObjectTo(fileObject, targetFolder);
		fileSystemManager.close();
	}

	private static void decompressArchiveTo(File archiveFile, File targetFolder, String archiveProtocol, String pathInArchive, IWriteHinter writeHinter) throws IOException {
		DefaultFileSystemManager fileSystemManager = createFileSystemManager();
		FileObject fileObject = createFileObject(fileSystemManager, archiveFile, archiveProtocol, pathInArchive);
		decompressFileObjectTo(fileObject, targetFolder, writeHinter);
		fileSystemManager.close();
	}

	private static void decompressFileObjectTo(FileObject fileObject, File targetFolder) throws IOException {
		decompressFileObjectTo(fileObject, targetFolder, new NoopFileWriteHinter());
	}

	private static void decompressFileObjectTo(FileObject fileObject, File targetFolder, IWriteHinter writeHinter) throws IOException {
		for (FileObject child : fileObject.getChildren()) {
			String childName = child.getName().getBaseName();
			FileType type = child.getType();
			if (type.equals(FileType.FOLDER)) {
				String folderName = writeHinter.alterFolderName(targetFolder, childName);
				File folder = new File(targetFolder, folderName);
				if (!folder.exists()) {
					folder.mkdirs();
				}
				decompressFileObjectTo(child, folder,writeHinter);
			}
			if (type.equals(FileType.FILE)) {
				String fileName = writeHinter.alterFileName(targetFolder, childName);
				File newFile = new File(targetFolder, fileName);
				newFile.createNewFile();
				WriteMode writeMode = writeHinter.getFileWriteMode(newFile);
				if (writeMode != WriteMode.SKIP) {
					FileOutputStream fileOutputStream = new FileOutputStream(newFile);
					InputStream inputStream = child.getContent().getInputStream();
					try {
						IOToolBox.inToOut(inputStream, fileOutputStream);
					} finally {
						inputStream.close();
						fileOutputStream.close();
					}
				}
			}
		}
	}

	private static boolean isFileOpeneableAs(File file, String archiveProtocol, String pathInArchive) {
		boolean result = true;
		try {
			createFileObject(createFileSystemManager(), file, archiveProtocol, pathInArchive);
		} catch (FileSystemException e) {
			result = false;
		}
		return result;
	}

	private ArchivesToolBox() {
	}
}