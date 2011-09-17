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
package org.org.eclipse.core.utils.jdt.tools;

import java.util.List;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.org.eclipse.core.utils.jdt.JDTUtilsException;

/**
 * @author pagregoire
 */
public final class JavaTypesHelper {

	private JavaTypesHelper() {
	}

	public static String[] getInterfaceNamesFromTypes(List<IType> types) {
		String[] result = new String[types.size()];
		int i = 0;
		for (IType type : types) {
			result[i++] = type.getElementName();
		}

		return result;
	}

	public static IType getTypeFromHandleIdentifier(String handleIdentifier) {
		IJavaElement element = JavaCore.create(handleIdentifier);
		IType type = null;
		try {
			if (element instanceof ICompilationUnit) {
				ICompilationUnit cu = (ICompilationUnit) element;
				type = cu.getType(Signature.getQualifier(cu.getElementName()));
			} else if (element instanceof IClassFile) {
				type = ((IClassFile) element).getType();
			} else if (element instanceof IType) {
				type = (IType) element;
			} else if (element instanceof IMember) {
				type = ((IMember) element).getDeclaringType();
			}
		} catch (Exception e) {
			throw new JDTUtilsException(e);
		}
		return type;
	}
}