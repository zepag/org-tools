/*
 org.org.lib.repository.crawler is a java library/OSGI Bundle
 Providing Crawling capabilities for Maven 2 HTTP exposed repositories
 Copyright (C) 2007  Pierre-Antoine Grégoire
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.org.repository.crawler;

/**
 * @author pagregoire
 */
public class RepositoryCrawlingException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 3382668236189908227L;

	/**
     * 
     */
    public RepositoryCrawlingException() {
        super();
    }

    /**
     * @param message
     */
    public RepositoryCrawlingException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public RepositoryCrawlingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public RepositoryCrawlingException(Throwable cause) {
        super(cause);
    }
}
