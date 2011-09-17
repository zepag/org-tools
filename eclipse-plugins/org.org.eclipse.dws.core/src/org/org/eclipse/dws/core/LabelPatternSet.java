package org.org.eclipse.dws.core;

import org.org.repository.crawler.items.IPatternSet;
import org.org.repository.crawler.items.immutable.ImmutablePatternSet;
import org.org.repository.crawler.items.mutable.PatternSet;

public class LabelPatternSet implements IPatternSet {

	private IPatternSet patternSet;
	private final String extensionLabel;

	public LabelPatternSet(String patternExtensionLabel) {
		this.extensionLabel = patternExtensionLabel;
		this.patternSet = DWSCorePlugin.getDefault().getPatternSetWithLabel(patternExtensionLabel);
	}

	private void checkPatternSet() {
		if (patternSet == null) {
			synchronized (this) {
				this.patternSet = DWSCorePlugin.getDefault().getPatternSetWithLabel(extensionLabel);
				if (patternSet == null) {
					throw new NullPointerException("Pattern set with extension id: \"" + extensionLabel + "\" is not available.");
				}
			}
		}
	}

	public String getDirectoryEntryPattern() {
		checkPatternSet();
		return patternSet.getDirectoryEntryPattern();
	}

	public String getEntryPattern() {
		checkPatternSet();
		return patternSet.getEntryPattern();
	}

	public String getFileEntryPattern() {
		checkPatternSet();
		return patternSet.getFileEntryPattern();
	}

	public ImmutablePatternSet getImmutable() {
		checkPatternSet();
		return patternSet.getImmutable();
	}

	public String getLabel() {
		checkPatternSet();
		return patternSet.getLabel();
	}

	public PatternSet getMutable() {
		checkPatternSet();
		return patternSet.getMutable();
	}

	public String getParentDirectoryPattern() {
		checkPatternSet();
		return patternSet.getParentDirectoryPattern();
	}
}