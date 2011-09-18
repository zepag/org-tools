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
package org.org.eclipse.core.utils.platform.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class PatternUtils {

    private PatternUtils() {
        // Not for instantiation
    }

    public static List<String> tokenize(String stringToParse, String delimiterString) {
        boolean loop = true;
        List<String> result = new ArrayList<String>();
        int startIndex = 0;
        int endIndex = 0;
        int length = stringToParse.length();
        while (loop) {
            endIndex = stringToParse.indexOf(delimiterString, startIndex);
            if (endIndex == -1 && startIndex == length) {
                loop = false;
            } else {
                if (endIndex == -1) {
                    endIndex = length;
                }
                result.add(stringToParse.substring(startIndex, endIndex));
                if (endIndex == length) {
                    startIndex = endIndex;
                } else {
                    startIndex = endIndex + delimiterString.length();
                }
            }
        }
        return result;
    }

    /**
     * Creates a pattern element from the pattern string which is either a reg-ex expression or of wildcard format ('*' matches any character and '?' matches one character).
     * 
     * @param pattern
     *            the search pattern
     * @param isCaseSensitive
     *            set to <code>true</code> to create a case insensitve pattern
     * @param isRegexSearch
     *            <code>true</code> if the passed string is a reg-ex pattern
     * @throws PatternSyntaxException
     */
    public static Pattern createPattern(String pattern, boolean isCaseSensitive, boolean isRegexSearch) throws PatternSyntaxException {
        if (!isRegexSearch) {
            pattern = toRegExFormat(pattern);
        }
        if (!isCaseSensitive) {
            return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
        }
        return Pattern.compile(pattern, Pattern.MULTILINE);
    }

    /**
     * Converts wildcard format ('*' and '?') to reg-ex format.
     */
    private static String toRegExFormat(String pattern) {
        StringBuffer regex = new StringBuffer(pattern.length());
        boolean escaped = false;
        boolean quoting = false;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '*' && !escaped) {
                if (quoting) {
                    regex.append("\\E");
                    quoting = false;
                }
                regex.append(".*");
                escaped = false;
                continue;
            } else if (c == '?' && !escaped) {
                if (quoting) {
                    regex.append("\\E");
                    quoting = false;
                }
                regex.append(".");
                escaped = false;
                continue;
            } else if (c == '\\' && !escaped) {
                escaped = true;
                continue;
            } else if (c == '\\' && escaped) {
                escaped = false;
                if (quoting) {
                    regex.append("\\E");
                    quoting = false;
                }
                regex.append("\\\\");
                continue;
            }
            if (!quoting) {
                regex.append("\\Q");
                quoting = true;
            }
            if (escaped && c != '*' && c != '?' && c != '\\') {
                regex.append('\\');
            }
            regex.append(c);
            escaped = c == '\\';
        }
        if (quoting) {
            regex.append("\\E");
        }
        return regex.toString();
    }
}