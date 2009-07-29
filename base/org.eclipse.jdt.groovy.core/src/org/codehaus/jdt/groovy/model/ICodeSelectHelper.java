/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.jdt.groovy.model;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.text.IRegion;

/**
 * @author Andrew Eisenberg
 * @created May 26, 2009
 * 
 *          Performs code select in a Groovy-aware way.
 * 
 *          TODO Determine if this should be converted into an extension point
 *          Right now, it is very fragile. This interface is implemented in
 *          another plugin
 */
public interface ICodeSelectHelper {

    public IJavaElement[] select(GroovyCompilationUnit unit, IRegion r);

}
