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
package org.org.eclipse.dws.ui.internal.views;

import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.org.eclipse.core.utils.platform.tools.PatternUtils;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelUtils;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.model.IModelItem;
import org.org.model.IModelItemVisitor;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * The Class MavenRepositoriesViewFilter.
 * 
 * @author pagregoire
 */
public class MavenRepositoriesViewFilter extends ViewerFilter {

	private final class MatchingModelItemVisitor implements IModelItemVisitor {
		private boolean result;

		private MatchingModelItemVisitor() {
		}

		@SuppressWarnings("unchecked")
		public boolean visit(IModelItem modelItem) {
			boolean keepOnVisiting = true;
			if (doesMatch(modelItem, PatternUtils.createPattern(userFilter.replaceAll("\\*", ".*") + ".*", false, true))) {
				result = true;
				keepOnVisiting = false;
			}
			return keepOnVisiting;
		}

		public boolean getResult() {
			return result;
		}
	}

	private final class ValidArtifactVersionSeeker implements IModelItemVisitor {
		private boolean result;

		public boolean getResult() {
			return result;
		}

		private ValidArtifactVersionSeeker() {
		}

		@SuppressWarnings("unchecked")
		public boolean visit(IModelItem modelItem) {
			boolean keepOnVisiting = true;
			if (modelItem instanceof ArtifactVersion) {
				ArtifactVersion artifactVersion = (ArtifactVersion) modelItem;
				if (RepositoryModelUtils.isExtensionOk(artifactExtensions, artifactVersion)) {
					result = true;
					keepOnVisiting = false;
				}
			}
			return keepOnVisiting;
		}
	}

	/** The artifact extensions. */
	private Set<String> artifactExtensions = AggregatedProperties.getArtifactExtensions();

	/** The user filter. */
	private String userFilter = "";

	/**
	 * Select.
	 * 
	 * @param viewer
	 *            the viewer
	 * @param parentElement
	 *            the parent element
	 * @param element
	 *            the element
	 * 
	 * @return true, if select
	 * 
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		boolean result = true;
		if (element instanceof IModelItem) {
			result = firstPathFilter((IModelItem) element);
			if (result) {
				if (element instanceof ArtifactVersion) {
					result = filterArtifactVersion(element);
				} else {
					result = filterAnyModelItem((IModelItem) element);
				}
			} else if (element instanceof CrawledRepository) {
				result = true;
			}
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * Filter artifact version.
	 * 
	 * @param element
	 *            the element
	 * 
	 * @return true, if successful
	 */
	private boolean filterArtifactVersion(Object element) {
		ArtifactVersion artifactVersion = (ArtifactVersion) element;
		boolean result = false;
		if (doesMatch(artifactVersion, PatternUtils.createPattern(userFilter.replaceAll("\\*", ".*") + ".*", false, true))) {
			result = true;
		}
		return result;
	}

	/**
	 * Filter any model item.
	 * 
	 * @param element
	 *            the element
	 * 
	 * @return true, if successful
	 */
	@SuppressWarnings("unchecked")
	private boolean filterAnyModelItem(IModelItem element) {
		MatchingModelItemVisitor visitor = new MatchingModelItemVisitor();
		element.accept(visitor);
		return visitor.getResult();
	}

	/**
	 * First path filter.
	 * 
	 * @param element
	 *            the element
	 * 
	 * @return true, if successful
	 */
	@SuppressWarnings("unchecked")
	private boolean firstPathFilter(IModelItem element) {
		boolean result = false;
		if (element instanceof ArtifactVersion) {
			ArtifactVersion artifactVersion = (ArtifactVersion) element;
			if (RepositoryModelUtils.isExtensionOk(artifactExtensions, artifactVersion)) {
				result = true;
			}
		} else {
			ValidArtifactVersionSeeker visitor = new ValidArtifactVersionSeeker();
			element.accept(visitor);
			result = visitor.getResult();
		}
		return result;
	}

	/**
	 * Gets the user filter.
	 * 
	 * @return the user filter
	 */
	public String getUserFilter() {
		return userFilter;
	}

	/**
	 * Sets the user filter.
	 * 
	 * @param userFilter
	 *            the new user filter
	 */
	public void setUserFilter(String userFilter) {
		this.userFilter = userFilter;
	}

	/**
	 * Does match.
	 * 
	 * @param element
	 *            the element
	 * @param pattern
	 *            the pattern
	 * 
	 * @return true, if successful
	 */
	@SuppressWarnings("unchecked")
	protected boolean doesMatch(IModelItem element, Pattern pattern) {
		if (element instanceof ArtifactVersion) {
			ArtifactVersion artifactVersion = (ArtifactVersion) element;
			// Compare bean name first
			String toMatch = artifactVersion.getId().substring(0, artifactVersion.getId().lastIndexOf("."));
			if (pattern.matcher(toMatch).matches()) {
				return true;
			}
		}
		return false;
	}
}
