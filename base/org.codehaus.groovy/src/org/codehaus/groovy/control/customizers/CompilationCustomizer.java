/*******************************************************************************
 * Copyright (c) 2009-2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kris De Volder - Initial implementation.
 *******************************************************************************/
package org.codehaus.groovy.control.customizers;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;

/**
 * This is a 'stub' class inserted into Groovy 1.7 bundle so that it is 'compilation and classload
 * compatible' with Groovy 1.8 bundle, with respect to external code referencing ImportCustomizer.
 * <p>
 * This class is copied because it is the superclass of ImportCustomizer and some Greclipse code
 * references the getPhase method inherited from here.
 * <p>
 * This is intended to *not* actually be used in Groovy 1.7 so any attempt to actually instantiate
 * an instance of this class or call methods in it will cause a IllegalStateException. 
 * <p>
 * This class was created by copying CompilationCustomizer from Groovy 1.8 and replacing all public
 * API with a stub method that throws IllegalStateException.
 * 
 * @author Kris De Volder
 */
public abstract class CompilationCustomizer extends CompilationUnit.PrimaryClassNodeOperation {

	public CompilationCustomizer(CompilePhase phase) {
		throw new IllegalStateException("CompilationCustomizer not supported in Groovy 1.7");
    }

    public CompilePhase getPhase() {
		throw new IllegalStateException("CompilationCustomizer not supported in Groovy 1.7");
    }
}
