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
package org.org.eclipse.dws.core.internal.versioning;



/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Describes a restriction in versioning.
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id: Restriction.java,v 1.1 2006/08/28 14:59:12 cvspispt Exp $
 */
public class Restriction {
    
    /** The lower bound. */
    private final DefaultArtifactVersion lowerBound;

    /** The lower bound inclusive. */
    private final boolean lowerBoundInclusive;

    /** The upper bound. */
    private final DefaultArtifactVersion upperBound;

    /** The upper bound inclusive. */
    private final boolean upperBoundInclusive;

    /** The Constant EVERYTHING. */
    static final Restriction EVERYTHING = new Restriction(null, false, null, false);

    /**
     * Instantiates a new restriction.
     * 
     * @param lowerBound the lower bound
     * @param lowerBoundInclusive the lower bound inclusive
     * @param upperBound the upper bound
     * @param upperBoundInclusive the upper bound inclusive
     */
    public Restriction(DefaultArtifactVersion lowerBound, boolean lowerBoundInclusive, DefaultArtifactVersion upperBound, boolean upperBoundInclusive) {
        this.lowerBound = lowerBound;
        this.lowerBoundInclusive = lowerBoundInclusive;
        this.upperBound = upperBound;
        this.upperBoundInclusive = upperBoundInclusive;
    }

    /**
     * Gets the lower bound.
     * 
     * @return the lower bound
     */
    public DefaultArtifactVersion getLowerBound() {
        return lowerBound;
    }

    /**
     * Checks if is lower bound inclusive.
     * 
     * @return true, if is lower bound inclusive
     */
    public boolean isLowerBoundInclusive() {
        return lowerBoundInclusive;
    }

    /**
     * Gets the upper bound.
     * 
     * @return the upper bound
     */
    public DefaultArtifactVersion getUpperBound() {
        return upperBound;
    }

    /**
     * Checks if is upper bound inclusive.
     * 
     * @return true, if is upper bound inclusive
     */
    public boolean isUpperBoundInclusive() {
        return upperBoundInclusive;
    }

    /**
     * Contains version.
     * 
     * @param version the version
     * 
     * @return true, if successful
     */
    public boolean containsVersion(DefaultArtifactVersion version) {
        if (lowerBound != null) {
            int comparison = lowerBound.compareTo(version);
            if (comparison == 0 && !lowerBoundInclusive) {
                return false;
            }
            if (comparison > 0) {
                return false;
            }
        }
        if (upperBound != null) {
            int comparison = upperBound.compareTo(version);
            if (comparison == 0 && !upperBoundInclusive) {
                return false;
            }
            if (comparison < 0) {
                return false;
            }
        }
        return true;
    }
}