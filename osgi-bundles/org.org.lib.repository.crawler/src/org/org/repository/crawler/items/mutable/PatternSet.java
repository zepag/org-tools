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
package org.org.repository.crawler.items.mutable;

import org.org.repository.crawler.items.IPatternSet;
import org.org.repository.crawler.items.immutable.ImmutablePatternSet;

public final class PatternSet implements IPatternSet {

	private String label;

	private String entryPattern;

	private String parentDirectoryPattern;

	private String directoryEntryPattern;

	private String fileEntryPattern;

	public PatternSet() {
		super();
	}

	public PatternSet(IPatternSet patternSet) {
		super();
		this.label = patternSet.getLabel();
		this.entryPattern = patternSet.getEntryPattern();
		this.fileEntryPattern = patternSet.getFileEntryPattern();
		this.directoryEntryPattern = patternSet.getDirectoryEntryPattern();
		this.parentDirectoryPattern = patternSet.getParentDirectoryPattern();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.maven2.crawler.items.mutable.IPatternSet#getDirectoryEntryPattern()
	 */
	public String getDirectoryEntryPattern() {
		return directoryEntryPattern;
	}

	public void setDirectoryEntryPattern(String directoryEntryPattern) {
		this.directoryEntryPattern = directoryEntryPattern;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.maven2.crawler.items.mutable.IPatternSet#getEntryPattern()
	 */
	public String getEntryPattern() {
		return entryPattern;
	}

	public void setEntryPattern(String entryPattern) {
		this.entryPattern = entryPattern;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.maven2.crawler.items.mutable.IPatternSet#getFileEntryPattern()
	 */
	public String getFileEntryPattern() {
		return fileEntryPattern;
	}

	public void setFileEntryPattern(String fileEntryPattern) {
		this.fileEntryPattern = fileEntryPattern;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.maven2.crawler.items.mutable.IPatternSet#getParentDirectoryPattern()
	 */
	public String getParentDirectoryPattern() {
		return parentDirectoryPattern;
	}

	public void setParentDirectoryPattern(String parentDirectoryPattern) {
		this.parentDirectoryPattern = parentDirectoryPattern;
	}

	public ImmutablePatternSet getImmutable() {
		return new ImmutablePatternSet(this);
	}

	public PatternSet getMutable() {
		return this;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}