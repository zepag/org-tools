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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Construct a version range from a specification.
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id: VersionRange.java,v 1.1 2006/08/28 14:59:12 cvspispt Exp $
 */
public class VersionRange {
    
    /** The RELEASE. */
    private final DefaultArtifactVersion RELEASE = new DefaultArtifactVersion("RELEASE");

    /** The recommended version. */
    private final DefaultArtifactVersion recommendedVersion;

    /** The restrictions. */
    private final List<Restriction> restrictions;

    /**
     * Instantiates a new version range.
     * 
     * @param recommendedVersion the recommended version
     * @param restrictions the restrictions
     */
    private VersionRange(DefaultArtifactVersion recommendedVersion, List<Restriction> restrictions) {
        this.recommendedVersion = recommendedVersion;
        this.restrictions = restrictions;
    }

    /**
     * Gets the recommended version.
     * 
     * @return the recommended version
     */
    public DefaultArtifactVersion getRecommendedVersion() {
        return recommendedVersion;
    }

    /**
     * Gets the restrictions.
     * 
     * @return the restrictions
     */
    public List<Restriction> getRestrictions() {
        return restrictions;
    }

    /**
     * Clone of.
     * 
     * @return the version range
     */
    public VersionRange cloneOf() {
        List<Restriction> copiedRestrictions = null;

        if (restrictions != null) {
            copiedRestrictions = new ArrayList<Restriction>();

            if (!restrictions.isEmpty()) {
                copiedRestrictions.addAll(restrictions);
            }
        }

        return new VersionRange(recommendedVersion, copiedRestrictions);
    }

    /**
     * Creates the from version spec.
     * 
     * @param spec the spec
     * 
     * @return the version range
     * 
     * @throws InvalidVersionSpecificationException the invalid version specification exception
     */
    public static VersionRange createFromVersionSpec(String spec) throws InvalidVersionSpecificationException {
        if (spec == null) {
            return null;
        }

        List<Restriction> restrictions = new ArrayList<Restriction>();
        String process = spec;
        DefaultArtifactVersion version = null;
        DefaultArtifactVersion upperBound = null;
        DefaultArtifactVersion lowerBound = null;

        while (process.startsWith("[") || process.startsWith("(")) {
            int index1 = process.indexOf(")");
            int index2 = process.indexOf("]");

            int index = index2;
            if (index2 < 0 || index1 < index2) {
                if (index1 >= 0) {
                    index = index1;
                }
            }

            if (index < 0) {
                throw new InvalidVersionSpecificationException("Unbounded range: " + spec);
            }

            Restriction restriction = parseRestriction(process.substring(0, index + 1));
            if (lowerBound == null) {
                lowerBound = restriction.getLowerBound();
            }
            if (upperBound != null) {
                if (restriction.getLowerBound() == null || restriction.getLowerBound().compareTo(upperBound) < 0) {
                    throw new InvalidVersionSpecificationException("Ranges overlap: " + spec);
                }
            }
            restrictions.add(restriction);
            upperBound = restriction.getUpperBound();

            process = process.substring(index + 1).trim();

            if (process.length() > 0 && process.startsWith(",")) {
                process = process.substring(1).trim();
            }
        }

        if (process.length() > 0) {
            if (restrictions.size() > 0) {
                throw new InvalidVersionSpecificationException("Only fully-qualified sets allowed in multiple set scenario: " + spec);
            } else {
                version = new DefaultArtifactVersion(process);
                restrictions.add(Restriction.EVERYTHING);
            }
        }

        return new VersionRange(version, restrictions);
    }

    /**
     * Parses the restriction.
     * 
     * @param spec the spec
     * 
     * @return the restriction
     * 
     * @throws InvalidVersionSpecificationException the invalid version specification exception
     */
    private static Restriction parseRestriction(String spec) throws InvalidVersionSpecificationException {
        boolean lowerBoundInclusive = spec.startsWith("[");
        boolean upperBoundInclusive = spec.endsWith("]");

        String process = spec.substring(1, spec.length() - 1).trim();

        Restriction restriction;

        int index = process.indexOf(",");

        if (index < 0) {
            if (!lowerBoundInclusive || !upperBoundInclusive) {
                throw new InvalidVersionSpecificationException("Single version must be surrounded by []: " + spec);
            }

            DefaultArtifactVersion version = new DefaultArtifactVersion(process);

            restriction = new Restriction(version, lowerBoundInclusive, version, upperBoundInclusive);
        } else {
            String lowerBound = process.substring(0, index).trim();
            String upperBound = process.substring(index + 1).trim();
            if (lowerBound.equals(upperBound)) {
                throw new InvalidVersionSpecificationException("Range cannot have identical boundaries: " + spec);
            }

            DefaultArtifactVersion lowerVersion = null;
            if (lowerBound.length() > 0) {
                lowerVersion = new DefaultArtifactVersion(lowerBound);
            }
            DefaultArtifactVersion upperVersion = null;
            if (upperBound.length() > 0) {
                upperVersion = new DefaultArtifactVersion(upperBound);
            }

            if (upperVersion != null && lowerVersion != null && upperVersion.compareTo(lowerVersion) < 0) {
                throw new InvalidVersionSpecificationException("Range defies version ordering: " + spec);
            }

            restriction = new Restriction(lowerVersion, lowerBoundInclusive, upperVersion, upperBoundInclusive);
        }

        return restriction;
    }

    /**
     * Creates the from version.
     * 
     * @param version the version
     * 
     * @return the version range
     */
    public static VersionRange createFromVersion(String version) {
        return new VersionRange(new DefaultArtifactVersion(version), new ArrayList<Restriction>());
    }

    /**
     * Restrict.
     * 
     * @param restriction the restriction
     * 
     * @return the version range
     */
    public VersionRange restrict(VersionRange restriction) {
        List<Restriction> r1 = this.restrictions;
        List<Restriction> r2 = restriction.restrictions;
        List<Restriction> restrictions;
        if (r1.isEmpty() || r2.isEmpty()) {
            restrictions = new ArrayList<Restriction>();
        } else {
            restrictions = intersection(r1, r2);
        }

        DefaultArtifactVersion version = null;
        if (restrictions.size() > 0) {
            boolean found = false;
            for (Iterator<Restriction> i = restrictions.iterator(); i.hasNext() && !found;) {
                Restriction r = i.next();

                if (recommendedVersion != null && r.containsVersion(recommendedVersion)) {
                    // if we find the original, use that
                    version = recommendedVersion;
                    found = true;
                } else if (version == null && restriction.getRecommendedVersion() != null && r.containsVersion(restriction.getRecommendedVersion())) {
                    // use this if we can, but prefer the original if possible
                    version = restriction.getRecommendedVersion();
                }
            }
        } else if (recommendedVersion != null) {
            // no range, so the recommended version is valid
            version = recommendedVersion;
        } else {
            throw new OverConstrainedVersionException("Restricting incompatible version ranges");
        }

        return new VersionRange(version, restrictions);
    }

    /**
     * Intersection.
     * 
     * @param r1 the r1
     * @param r2 the r2
     * 
     * @return the list< restriction>
     */
    private List<Restriction> intersection(List<Restriction> r1, List<Restriction> r2) {
        List<Restriction> restrictions = new ArrayList<Restriction>(r1.size() + r2.size());
        Iterator<Restriction> i1 = r1.iterator();
        Iterator<Restriction> i2 = r2.iterator();
        Restriction res1 = i1.next();
        Restriction res2 = i2.next();

        boolean done = false;
        while (!done) {
            if (res1.getLowerBound() == null || res2.getUpperBound() == null || res1.getLowerBound().compareTo(res2.getUpperBound()) <= 0) {
                if (res1.getUpperBound() == null || res2.getLowerBound() == null || res1.getUpperBound().compareTo(res2.getLowerBound()) >= 0) {
                    DefaultArtifactVersion lower;
                    DefaultArtifactVersion upper;
                    boolean lowerInclusive;
                    boolean upperInclusive;

                    // overlaps
                    if (res1.getLowerBound() == null) {
                        lower = res2.getLowerBound();
                        lowerInclusive = res2.isLowerBoundInclusive();
                    } else if (res2.getLowerBound() == null) {
                        lower = res1.getLowerBound();
                        lowerInclusive = res1.isLowerBoundInclusive();
                    } else {
                        int comparison = res1.getLowerBound().compareTo(res2.getLowerBound());
                        if (comparison < 0) {
                            lower = res2.getLowerBound();
                            lowerInclusive = res2.isLowerBoundInclusive();
                        } else if (comparison == 0) {
                            lower = res1.getLowerBound();
                            lowerInclusive = res1.isLowerBoundInclusive() && res2.isLowerBoundInclusive();
                        } else {
                            lower = res1.getLowerBound();
                            lowerInclusive = res1.isLowerBoundInclusive();
                        }
                    }

                    if (res1.getUpperBound() == null) {
                        upper = res2.getUpperBound();
                        upperInclusive = res2.isUpperBoundInclusive();
                    } else if (res2.getUpperBound() == null) {
                        upper = res1.getUpperBound();
                        upperInclusive = res1.isUpperBoundInclusive();
                    } else {
                        int comparison = res1.getUpperBound().compareTo(res2.getUpperBound());
                        if (comparison < 0) {
                            upper = res1.getUpperBound();
                            upperInclusive = res1.isUpperBoundInclusive();
                        } else if (comparison == 0) {
                            upper = res1.getUpperBound();
                            upperInclusive = res1.isUpperBoundInclusive() && res2.isUpperBoundInclusive();
                        } else {
                            upper = res2.getUpperBound();
                            upperInclusive = res2.isUpperBoundInclusive();
                        }
                    }

                    // don't add if they are equal and one is not inclusive
                    if (lower == null || upper == null || lower.compareTo(upper) != 0) {
                        restrictions.add(new Restriction(lower, lowerInclusive, upper, upperInclusive));
                    } else if (lowerInclusive && upperInclusive) {
                        restrictions.add(new Restriction(lower, lowerInclusive, upper, upperInclusive));
                    }

                    // noinspection ObjectEquality
                    if (upper == res2.getUpperBound()) {
                        // advance res2
                        if (i2.hasNext()) {
                            res2 = i2.next();
                        } else {
                            done = true;
                        }
                    } else {
                        // advance res1
                        if (i1.hasNext()) {
                            res1 = i1.next();
                        } else {
                            done = true;
                        }
                    }
                } else {
                    // move on to next in r1
                    if (i1.hasNext()) {
                        res1 = i1.next();
                    } else {
                        done = true;
                    }
                }
            } else {
                // move on to next in r2
                if (i2.hasNext()) {
                    res2 = i2.next();
                } else {
                    done = true;
                }
            }
        }

        return restrictions;
    }

    /**
     * Gets the selected version.
     * 
     * @return the selected version
     * 
     * @throws OverConstrainedVersionException the over constrained version exception
     */
    public DefaultArtifactVersion getSelectedVersion() throws OverConstrainedVersionException {
        DefaultArtifactVersion version;
        if (recommendedVersion != null) {
            version = recommendedVersion;
        } else {
            if (restrictions.size() == 0) {
                throw new OverConstrainedVersionException("The artifact has no valid ranges");
            } else {
                Restriction restriction = restrictions.get(restrictions.size() - 1);

                version = restriction.getUpperBound();
                if (version == null) {
                    version = RELEASE;
                }
            }
        }
        return version;
    }

    /**
     * Checks if is selected version known.
     * 
     * @return true, if is selected version known
     * 
     * @throws OverConstrainedVersionException the over constrained version exception
     */
    public boolean isSelectedVersionKnown() throws OverConstrainedVersionException {
        boolean value = false;
        if (recommendedVersion != null) {
            value = true;
        } else {
            if (restrictions.size() == 0) {
                throw new OverConstrainedVersionException("The artifact has no valid ranges");
            } else {
                Restriction restriction = restrictions.get(restrictions.size() - 1);
                if (restriction.getUpperBound() != null) {
                    value = restriction.isUpperBoundInclusive();
                }
            }
        }
        return value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    /**
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        if (recommendedVersion != null) {
            return recommendedVersion.toString();
        } else {
            StringBuffer buf = new StringBuffer();
            for (Iterator<Restriction> i = restrictions.iterator(); i.hasNext();) {
                Restriction r = i.next();

                buf.append(r.isLowerBoundInclusive() ? "[" : "(");
                if (r.getLowerBound() != null) {
                    buf.append(r.getLowerBound().toString());
                }
                buf.append(",");
                if (r.getUpperBound() != null) {
                    buf.append(r.getUpperBound().toString());
                }
                buf.append(r.isUpperBoundInclusive() ? "]" : ")");

                if (i.hasNext()) {
                    buf.append(",");
                }
            }
            return buf.toString();
        }
    }

    /**
     * Match version.
     * 
     * @param versions the versions
     * 
     * @return the default artifact version
     */
    public DefaultArtifactVersion matchVersion(List<DefaultArtifactVersion> versions) {
        // TO-DO: could be more efficient by sorting the list and then moving along the restrictions in order?

        DefaultArtifactVersion matched = null;
        for (Iterator<DefaultArtifactVersion> i = versions.iterator(); i.hasNext();) {
            DefaultArtifactVersion version = i.next();
            if (containsVersion(version)) {
                // valid - check if it is greater than the currently matched version
                if (matched == null || version.compareTo(matched) > 0) {
                    matched = version;
                }
            }
        }
        return matched;
    }

    /**
     * Contains version.
     * 
     * @param version the version
     * 
     * @return true, if successful
     */
    public boolean containsVersion(DefaultArtifactVersion version) {
        boolean matched = false;
        for (Iterator<Restriction> i = restrictions.iterator(); i.hasNext() && !matched;) {
            Restriction restriction = i.next();
            if (restriction.containsVersion(version)) {
                matched = true;
            }
        }
        return matched;
    }

    /**
     * Checks for restrictions.
     * 
     * @return true, if successful
     */
    public boolean hasRestrictions() {
        return !restrictions.isEmpty() && recommendedVersion == null;
    }
}
