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
package org.org.eclipse.core.utils.platform.jobs.completion;

import java.lang.reflect.Field;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

/**
 * Borrowed from Mylyn core.
 * 
 * @author Mik Kersten
 * @since 3.0
 */
public class CommonFonts {

	public static Font BOLD;

	public static Font ITALIC;

	public static Font STRIKETHROUGH = null;

	public static boolean HAS_STRIKETHROUGH;

	static {
		if (Display.getCurrent() != null) {
			init();
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					init();
				}
			});
		}
	}

	private static void init() {
		BOLD = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
		ITALIC = JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);

		Font defaultFont = JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT);
		FontData[] defaultData = defaultFont.getFontData();
		if (defaultData != null && defaultData.length == 1) {
			FontData data = new FontData(defaultData[0].getName(), defaultData[0].getHeight(), defaultData[0].getStyle());

			if ("win32".equals(SWT.getPlatform())) {
				// NOTE: Windows only, for: data.data.lfStrikeOut = 1;
				try {
					Field dataField = data.getClass().getDeclaredField("data");
					Object dataObject = dataField.get(data);
					Class<?> clazz = dataObject.getClass().getSuperclass();
					Field strikeOutFiled = clazz.getDeclaredField("lfStrikeOut");
					strikeOutFiled.set(dataObject, (byte) 1);
					CommonFonts.STRIKETHROUGH = new Font(Display.getCurrent(), data);
				} catch (Throwable t) {
					// ignore
				}
			}
		}
		if (CommonFonts.STRIKETHROUGH == null) {
			CommonFonts.HAS_STRIKETHROUGH = false;
			CommonFonts.STRIKETHROUGH = defaultFont;
		} else {
			CommonFonts.HAS_STRIKETHROUGH = true;
		}
	}

	/**
	 * NOTE: disposal of JFaceResources fonts handled by registry.
	 */
	public static void dispose() {
		if (CommonFonts.STRIKETHROUGH != null && !CommonFonts.STRIKETHROUGH.isDisposed()) {
			CommonFonts.STRIKETHROUGH.dispose();
		}
	}

}
