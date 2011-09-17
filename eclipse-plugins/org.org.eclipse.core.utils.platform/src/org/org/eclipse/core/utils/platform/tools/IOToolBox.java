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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.Proxy.Type;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.org.eclipse.core.utils.platform.PlatformUtilsException;
import org.org.eclipse.core.utils.platform.PlatformUtilsPlugin;

/**
 * @author pagregoire
 */
public final class IOToolBox {
	private IOToolBox() {
	}

	public static StringBuffer streamToStringBuffer(InputStream stream) {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
		StringBuffer buffer = new StringBuffer();
		try {
			String line = bufferedReader.readLine();
			while (line != null) {
				buffer.append(line);
				line = bufferedReader.readLine();
			}
			stream.reset();
		} catch (IOException ioe) {
			throw new PlatformUtilsException(ioe);
		}
		return buffer;
	}

	public static void inToOut(InputStream inputStream, OutputStream outputStream) throws IOException {
		try {
			byte[] buffer = new byte[2048];
			int byteNumber = inputStream.read(buffer);
			while (byteNumber != -1) {
				outputStream.write(buffer, 0, byteNumber);
				byteNumber = inputStream.read(buffer);
			}
		} finally {
			inputStream.close();
			outputStream.close();
		}
	}

	public static Proxy determineProxy(URL url) {
		IProxyService proxyService = PlatformUtilsPlugin.getDefault().getProxyService();
		IProxyData proxyData = proxyService.getProxyDataForHost(url.getHost(), IProxyData.HTTP_PROXY_TYPE);
		Proxy proxy = Proxy.NO_PROXY;
		if (proxyData != null) {
			proxy = new Proxy(Type.HTTP, new InetSocketAddress(proxyData.getHost(), proxyData.getPort()));
		}
		return proxy;
	}

	public static void downloadToLocalFile(File targetFile, URL requestedURL, Proxy proxy, IProgressMonitor monitor) throws IOException {
		InputStream is = null;
		FileOutputStream out = null;
		try {
			monitor.beginTask("Downloading " + requestedURL.toExternalForm() + " to " + targetFile.toString(), 1);
			is = open(requestedURL, proxy);
			targetFile.createNewFile();
			out = new FileOutputStream(targetFile);
			byte[] buf = new byte[1024]; // 1K buffer
			int bytesRead;
			while ((bytesRead = is.read(buf)) != -1) {
				out.write(buf, 0, bytesRead);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			monitor.done();
			close(is);
			close(out);
		}
	}

	public static void downloadToOutputStreamAndCloseIt(OutputStream outputStream, URL requestedURL, Proxy proxy, IProgressMonitor monitor) throws IOException {
		InputStream is = null;
		try {
			monitor.beginTask("Downloading " + requestedURL.toExternalForm() + " to stream of type " + outputStream.getClass().getName(), 1);
			is = open(requestedURL, proxy);
			byte[] buf = new byte[1024]; // 1K buffer
			int bytesRead;
			while ((bytesRead = is.read(buf)) != -1) {
				outputStream.write(buf, 0, bytesRead);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			monitor.done();
			close(is);
			close(outputStream);
		}
	}

	private static void close(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e1) {
				// ignore.
			}
		}
	}

	private static void close(OutputStream out) {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e1) {
				// ignore.
			}
		}
	}

	private static InputStream open(URL url, Proxy proxy) throws IOException {
		InputStream inputStream = null;
		if (proxy != null) {
			inputStream = url.openConnection(proxy).getInputStream();
		} else {
			inputStream = url.openConnection().getInputStream();
		}
		return inputStream;
	}

	public static boolean fileExists(File file) {
		return file == null ? false : file.exists();
	}

	public static boolean fileWriteable(File file) {
		return file == null ? false : file.canWrite();
	}
}