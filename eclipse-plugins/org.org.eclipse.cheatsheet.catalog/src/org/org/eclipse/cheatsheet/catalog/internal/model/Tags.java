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
package org.org.eclipse.cheatsheet.catalog.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Tags {
	private String[] tags;
	private String tagsString;

	public Tags(String... tags) {
		this.tags = tags;
		this.tagsString = formatTags(tags);
	}

	private String formatTags(String... tags) {
		StringBuilder tagsString = new StringBuilder();
		for (String string : tags) {
			tagsString.append(string);
			tagsString.append(",");
		}
		return tagsString.substring(0, tagsString.length() - 1);
	}

	public Tags(String tagsString) {
		StringTokenizer tkz = new StringTokenizer(tagsString, ",", false);
		List<String> list = new ArrayList<String>();
		while (tkz.hasMoreTokens()) {
			list.add(tkz.nextToken());
		}
		tags = (String[]) list.toArray(new String[list.size()]);

		this.tagsString = formatTags(tags);
	}

	public String getTagsString() {
		return tagsString;
	}

	public String[] getTagsArray() {
		return tags;
	}

	@Override
	public String toString() {
		return tagsString;
	}
}
