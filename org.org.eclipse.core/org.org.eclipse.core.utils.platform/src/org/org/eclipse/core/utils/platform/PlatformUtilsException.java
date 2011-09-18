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
package org.org.eclipse.core.utils.platform;

/**
 * @author pagregoire
 */
public class PlatformUtilsException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7253362089884979876L;

	/**
     *  
     */
    public PlatformUtilsException() {
        super();
    }

    /**
     * @param message
     */
    public PlatformUtilsException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public PlatformUtilsException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public PlatformUtilsException(String message, Throwable cause) {
        super(message, cause);
    }

}