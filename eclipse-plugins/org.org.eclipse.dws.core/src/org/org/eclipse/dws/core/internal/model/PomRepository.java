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
package org.org.eclipse.dws.core.internal.model;

import org.org.model.AbstractModelItem;
import org.org.model.IModelItem;


/**
 * The Class PomRepository.
 */
@SuppressWarnings("unchecked")
public class PomRepository extends AbstractModelItem<IModelItem,IModelItem> {

	/** The url. */
	private String url;

	/** The name. */
	private String name;

	/** The id. */
	private String id;

	/* (non-Javadoc)
	 * @see org.org.model.AbstractModelItem#getUID()
	 */
	/**
	 * @see org.org.model.AbstractModelItem#getUID()
	 */
	@Override
	public String getUID() {
		return this.toStringBuilderDescription().toString();
	}

	/* (non-Javadoc)
	 * @see org.org.model.AbstractModelItem#toStringBuilderDescription()
	 */
	/**
	 * @see org.org.model.AbstractModelItem#toStringBuilderDescription()
	 */
	@Override
	public StringBuilder toStringBuilderDescription() {
		return new StringBuilder(id + ":" + name + ":" + url);
	}

	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the url.
	 * 
	 * @param url the new url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the url.
	 * 
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

}
