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
package org.org.eclipse.core.utils.platform.views.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;

public abstract class ResolvedAction<T> extends Action {
	private T actionHost;

	public ResolvedAction(T actionHost) {
		Assert.isNotNull(actionHost);
		this.actionHost = actionHost;
	}

	public T getActionHost() {
		return actionHost;
	}
}
