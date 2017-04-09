/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.jdom;

import org.eclipse.jdt.internal.core.jdom.*;

/**
 * Standard implementation of <code>IDOMFactory</code>, and the only means
 * of creating JDOMs and document fragments.
 * <p>
 * This class may be instantiated.
 * </p>
 * @deprecated The JDOM was made obsolete by the addition in 2.0 of the more
 * powerful, fine-grained DOM/AST API found in the
 * org.eclipse.jdt.core.dom package.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DOMFactory implements IDOMFactory {
	String lineSeparator;
/**
 * Creates a new DOM factory.
 */
public DOMFactory() {
	// constructor is explicitly API
	this.lineSeparator = org.eclipse.jdt.internal.core.util.Util.getLineSeparator(null, null/*take the workspace line separator as no project is available*/);
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMCompilationUnit createCompilationUnit() {
	return (new DOMBuilder()).createCompilationUnit();
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMCompilationUnit createCompilationUnit(char[] sourceCode, String name)  {
	if(sourceCode == null) {
		return null;
	}
	return (new SimpleDOMBuilder()).createCompilationUnit(sourceCode, name.toCharArray());
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMCompilationUnit createCompilationUnit(String sourceCode, String name) {
	if(sourceCode == null) {
		return null;
	}
	return (new SimpleDOMBuilder()).createCompilationUnit(sourceCode.toCharArray(), name.toCharArray());
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMField createField() {
	return createField("Object aField;"+ this.lineSeparator); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMField createField(String sourceCode) {
	if(sourceCode == null) {
		return null;
	}
	return (new DOMBuilder()).createField(sourceCode.toCharArray());
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMImport createImport() {
	return (new DOMBuilder()).createImport();
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMImport createImport(String sourceCode) {
	if(sourceCode == null) {
		return null;
	}
	return (new DOMBuilder()).createImport(sourceCode.toCharArray());
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMInitializer createInitializer() {
	return createInitializer("static {}"+ this.lineSeparator); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMInitializer createInitializer(String sourceCode) {
	if(sourceCode == null) {
		return null;
	}
	return (new DOMBuilder()).createInitializer(sourceCode.toCharArray());
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMMethod createMethod() {
	return createMethod("public void newMethod() {"+ this.lineSeparator+"}"+ this.lineSeparator); //$NON-NLS-2$ //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMMethod createMethod(String sourceCode) {
	if(sourceCode == null) {
		return null;
	}
	return (new DOMBuilder()).createMethod(sourceCode.toCharArray());
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMPackage createPackage() {
	return (new DOMBuilder()).createPackage();
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMPackage createPackage(String sourceCode) {
	if(sourceCode == null) {
		return null;
	}
	return (new DOMBuilder()).createPackage(sourceCode.toCharArray());
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMType createType() {
	return createType("public class AClass {"+ this.lineSeparator +"}"+ this.lineSeparator); //$NON-NLS-2$ //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMType createClass() {
	return createType("public class AClass {"+ this.lineSeparator +"}"+ this.lineSeparator); //$NON-NLS-2$ //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMType createInterface() {
	return createType("public interface AnInterface {"+ this.lineSeparator +"}"+ this.lineSeparator); //$NON-NLS-2$ //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on IDOMFactory.
 */
public IDOMType createType(String sourceCode) {
	if(sourceCode == null) {
		return null;
	}
	return (new DOMBuilder()).createType(sourceCode.toCharArray());
}
}
