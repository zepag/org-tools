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
package org.org.eclipse.dws.ui.internal.wizards;

/**
 * The Class WizardInitException.
 */
public class WizardInitException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -9155177308366268059L;

	/**
	 * The Enum Status.
	 */
	public enum Status {

		/** The WARNING. */
		WARNING,
		
		/** The ERROR. */
		ERROR,
		
		/** The INFO. */
		INFO
	}

	/** The status. */
	private Status status;

	/**
	 * Instantiates a new wizard init exception.
	 * 
	 * @param status the status
	 */
	public WizardInitException(Status status) {
		super();
		this.status = status;
	}

	/**
	 * Instantiates a new wizard init exception.
	 * 
	 * @param status the status
	 * @param message the message
	 * @param cause the cause
	 */
	public WizardInitException(Status status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	/**
	 * Instantiates a new wizard init exception.
	 * 
	 * @param status the status
	 * @param message the message
	 */
	public WizardInitException(Status status, String message) {
		super(message);
		this.status = status;
	}

	/**
	 * Instantiates a new wizard init exception.
	 * 
	 * @param status the status
	 * @param cause the cause
	 */
	public WizardInitException(Status status, Throwable cause) {
		super(cause);
		this.status = status;
	}

	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

}
