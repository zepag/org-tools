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
public class InterruptionFlag {
    public static final int CONTINUE = 1 << 0;

    public static final int STOP = 1 << 1;

    public static final int PAUSE = 1 << 2;

    private volatile int currentStatus = CONTINUE;

    /**
     * @return Returns the currentStatus.
     */
    public int getCurrentStatus() {
        return this.currentStatus;
    }

    /**
     * @param currentStatus
     *            The currentStatus to set.
     */
    public void setCurrentStatus(int currentStatus) {
        this.currentStatus = currentStatus;
    }

    public boolean isCurrentStatus(int testedStatus) {
        return (this.currentStatus == testedStatus);
    }
}