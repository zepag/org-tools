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
package org.org.eclipse.core.utils.jdt;

/**
 * @author pagregoire
 */
public class JDTUtilsException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7253362089884979876L;

	/**
     *  
     */
    public JDTUtilsException() {
        super();
    }

    /**
     * @param message
     */
    public JDTUtilsException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public JDTUtilsException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public JDTUtilsException(String message, Throwable cause) {
        super(message, cause);
    }

}