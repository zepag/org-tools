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
 * Occurs when ranges exclude each other and no valid value remains.
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id: OverConstrainedVersionException.java,v 1.1 2006/08/28 14:59:12 cvspispt Exp $
 */
public class OverConstrainedVersionException extends RuntimeException {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8660022997199032958L;

    /**
     * Instantiates a new over constrained version exception.
     * 
     * @param msg the msg
     */
    public OverConstrainedVersionException(String msg) {
        super(msg);
    }

}