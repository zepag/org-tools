/*
 org.org.lib.repository.crawler is a java library/OSGI Bundle
 Providing Crawling capabilities for Maven 2 HTTP exposed repositories
 Copyright (C) 2007  Pierre-Antoine Grégoire
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.org.repository.crawler.items.immutable;

import org.org.repository.crawler.items.IPatternSet;
import org.org.repository.crawler.items.mutable.PatternSet;

public class ImmutablePatternSet implements IPatternSet {

	private final String label;

	private final String entryPattern;

	private final String fileEntryPattern;

	private final String directoryEntryPattern;

	private final String parentDirectoryPattern;

	public ImmutablePatternSet(final String label, final String entryPattern, final String fileEntryPattern, final String directoryEntryPattern, final String parentDirectoryPattern) {
		super();
		this.label = label;
		this.entryPattern = entryPattern;
		this.fileEntryPattern = fileEntryPattern;
		this.directoryEntryPattern = directoryEntryPattern;
		this.parentDirectoryPattern = parentDirectoryPattern;
	}

	public ImmutablePatternSet(IPatternSet patternSet) {
		super();
		this.label = patternSet.getLabel();
		this.entryPattern = patternSet.getEntryPattern();
		this.fileEntryPattern = patternSet.getFileEntryPattern();
		this.directoryEntryPattern = patternSet.getDirectoryEntryPattern();
		this.parentDirectoryPattern = patternSet.getParentDirectoryPattern();
	}

	public ImmutablePatternSet getImmutable() {
		return this;
	}

	public PatternSet getMutable() {
		return new PatternSet(this);
	}

	public String getParentDirectoryPattern() {
		return parentDirectoryPattern;
	}

	public String getDirectoryEntryPattern() {
		return directoryEntryPattern;
	}

	public String getEntryPattern() {
		return entryPattern;
	}

	public String getFileEntryPattern() {
		return fileEntryPattern;
	}

	public String getLabel() {
		return label;
	}

}
