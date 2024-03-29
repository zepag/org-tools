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

import java.util.StringTokenizer;


/**
 * Default implementation of artifact versioning.
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id: DefaultArtifactVersion.java,v 1.1 2006/08/28 14:59:12 cvspispt Exp $
 */
public class DefaultArtifactVersion {
    
    /** The major version. */
    private Integer majorVersion;

    /** The minor version. */
    private Integer minorVersion;

    /** The incremental version. */
    private Integer incrementalVersion;

    /** The build number. */
    private Integer buildNumber;

    /** The qualifier. */
    private String qualifier;

    /**
     * Instantiates a new default artifact version.
     * 
     * @param version the version
     */
    public DefaultArtifactVersion(String version) {
        parseVersion(version);
    }

    /**
     * Compare to.
     * 
     * @param o the o
     * 
     * @return the int
     */
    public int compareTo(Object o) {
        DefaultArtifactVersion otherVersion = (DefaultArtifactVersion) o;

        int result = compareIntegers(majorVersion, otherVersion.majorVersion);
        if (result == 0) {
            result = compareIntegers(minorVersion, otherVersion.minorVersion);
        }
        if (result == 0) {
            result = compareIntegers(incrementalVersion, otherVersion.incrementalVersion);
        }
        if (result == 0) {
            if (buildNumber != null || otherVersion.buildNumber != null) {
                result = compareIntegers(buildNumber, otherVersion.buildNumber);
            } else if (qualifier != null) {
                if (otherVersion.qualifier != null) {
                    if (qualifier.length() > otherVersion.qualifier.length() && qualifier.startsWith(otherVersion.qualifier)) {
                        // here, the longer one that otherwise match is considered older
                        result = -1;
                    } else if (qualifier.length() < otherVersion.qualifier.length() && otherVersion.qualifier.startsWith(qualifier)) {
                        // here, the longer one that otherwise match is considered older
                        result = 1;
                    } else {
                        result = qualifier.compareTo(otherVersion.qualifier);
                    }
                } else {
                    // otherVersion has no qualifier but we do - that's newer
                    result = -1;
                }
            } else if (otherVersion.qualifier != null) {
                // otherVersion has a qualifier but we don't, we're newer
                result = 1;
            }
        }
        return result;
    }

    /**
     * Compare integers.
     * 
     * @param i1 the i1
     * @param i2 the i2
     * 
     * @return the int
     */
    private int compareIntegers(Integer i1, Integer i2) {
        // treat null as 0 in comparison
        if (i1 == null ? i2 == null : i1.equals(i2)) {
            return 0;
        } else if (i1 == null) {
            return -i2.intValue();
        } else if (i2 == null) {
            return i1.intValue();
        } else {
            return i1.intValue() - i2.intValue();
        }
    }

    /**
     * Gets the major version.
     * 
     * @return the major version
     */
    public int getMajorVersion() {
        return majorVersion != null ? majorVersion.intValue() : 0;
    }

    /**
     * Gets the minor version.
     * 
     * @return the minor version
     */
    public int getMinorVersion() {
        return minorVersion != null ? minorVersion.intValue() : 0;
    }

    /**
     * Gets the incremental version.
     * 
     * @return the incremental version
     */
    public int getIncrementalVersion() {
        return incrementalVersion != null ? incrementalVersion.intValue() : 0;
    }

    /**
     * Gets the builds the number.
     * 
     * @return the builds the number
     */
    public int getBuildNumber() {
        return buildNumber != null ? buildNumber.intValue() : 0;
    }

    /**
     * Gets the qualifier.
     * 
     * @return the qualifier
     */
    public String getQualifier() {
        return qualifier;
    }

    /**
     * Parses the version.
     * 
     * @param version the version
     */
    public final void parseVersion(String version) {
        int index = version.indexOf("-");

        String part1;
        String part2 = null;

        if (index < 0) {
            part1 = version;
        } else {
            part1 = version.substring(0, index);
            part2 = version.substring(index + 1);
        }

        if (part2 != null) {
            try {
                if (part2.length() == 1 || !part2.startsWith("0")) {
                    buildNumber = Integer.valueOf(part2);
                } else {
                    qualifier = part2;
                }
            } catch (NumberFormatException e) {
                qualifier = part2;
            }
        }

        if (part1.indexOf(".") < 0 && !part1.startsWith("0")) {
            try {
                majorVersion = Integer.valueOf(part1);
            } catch (NumberFormatException e) {
                // qualifier is the whole version, including "-"
                qualifier = version;
                buildNumber = null;
            }
        } else {
            boolean fallback = false;
            StringTokenizer tok = new StringTokenizer(part1, ".");
            try {
                majorVersion = getNextIntegerToken(tok);
                if (tok.hasMoreTokens()) {
                    minorVersion = getNextIntegerToken(tok);
                }
                if (tok.hasMoreTokens()) {
                    incrementalVersion = getNextIntegerToken(tok);
                }
                if (tok.hasMoreTokens()) {
                    fallback = true;
                }
            } catch (NumberFormatException e) {
                fallback = true;
            }

            if (fallback) {
                // qualifier is the whole version, including "-"
                qualifier = version;
                majorVersion = null;
                minorVersion = null;
                incrementalVersion = null;
                buildNumber = null;
            }
        }
    }

    /**
     * Gets the next integer token.
     * 
     * @param tok the tok
     * 
     * @return the next integer token
     */
    private static Integer getNextIntegerToken(StringTokenizer tok) {
        String s = tok.nextToken();
        if (s.length() > 1 && s.startsWith("0")) {
            throw new NumberFormatException("Number part has a leading 0: '" + s + "'");
        }
        return Integer.valueOf(s);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    /**
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        StringBuffer buf = new StringBuffer();
        if (majorVersion != null) {
            buf.append(majorVersion);
        }
        if (minorVersion != null) {
            buf.append(".");
            buf.append(minorVersion);
        }
        if (incrementalVersion != null) {
            buf.append(".");
            buf.append(incrementalVersion);
        }
        if (buildNumber != null) {
            buf.append("-");
            buf.append(buildNumber);
        } else if (qualifier != null) {
            if (buf.length() > 0) {
                buf.append("-");
            }
            buf.append(qualifier);
        }
        return buf.toString();
    }
}