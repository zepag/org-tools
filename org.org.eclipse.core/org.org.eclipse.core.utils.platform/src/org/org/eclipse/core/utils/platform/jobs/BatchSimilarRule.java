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
package org.org.eclipse.core.utils.platform.jobs;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class BatchSimilarRule implements ISchedulingRule {

	public String id;

	public BatchSimilarRule(String id) {
		this.id = id;
	}

	/*
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	public boolean isConflicting(ISchedulingRule rule) {
		if (rule instanceof BatchSimilarRule) {
			return ((BatchSimilarRule) rule).id.equals(id);
		}
		return false;
	}

	public boolean contains(ISchedulingRule rule) {
		// added a FIX to allow the execution of jobs with an IResource scheduling rule.
        // This means that MutexRule does claim to contain all IResource rules...
        //FIXME this is a workaround that has to be checked for side-effects (possible side-effects would be the execution of other jobs with IResource as a rule resulting in failures in these jobs... maybe... hard to test :P)
        // It would be better to use the modified resources (Project, files, folders...etc) as Scheduling Rules or to use the RuleFactory to determine it.
        return rule == this || rule instanceof IResource;
	}
}
