/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.helper;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author Stefan Reinhard
 */
public class Checks {

	/**
	 * Returns true if it's possible to refactor the given Field
	 */
	public static boolean isRefactorable(IField field) {
		if (field == null) return false;
		try {
			return field.isStructureKnown()
					&& !field.isBinary();
		} catch (JavaModelException e) {
			return false;
		}
	}

	/**
	 * Returns true if it's possible to refactor the given Method
	 */
	public static boolean isRefactorable(IMethod method) {
		if (method == null) return false;
		try {
			return !Flags.isPrivate(method.getFlags())
					&& !method.isBinary()
						&& method.isStructureKnown();
		} catch (JavaModelException e) {
			return false;
		}
	}
	
	/**
	 * Return true if the given Method is static
	 */
	public static boolean isStatic(IMethod method) {
		try {
			return Flags.isStatic(method.getFlags());
		} catch (JavaModelException e) {
			return false;
		}
	}

}
