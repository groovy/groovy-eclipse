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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.SourceUnit;

/**
 * This is a 'stub' class inserted into Groovy 1.7 bundle so that it is 'compilation and classload
 * compatible' with Groovy 1.8 bundle, with respect to external code referencing ImportCustomizer.
 * <p>
 * This is intended to not actually be used in Groovy 1.7 so any attempt to actually instantiate
 * an instance of this class or call methods in it will cause a IllegalStateException. 
 * <p>
 * This class was created by copying the ImportCustomizer from Groovy 1.8 and replacing all public
 * API with a stub method that throws an Exception.
 * 
 * @author Kris De Volder
 */
public class ImportCustomizer extends CompilationCustomizer {

    public ImportCustomizer() {
    	super(null);
    }

    @Override
    public void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) throws CompilationFailedException {
    	throw new IllegalStateException("ImportCustomizer not supported in Groovy 1.7");
    }

    public ImportCustomizer addImport(final String alias, final String className) {
    	throw new IllegalStateException("ImportCustomizer not supported in Groovy 1.7");
    }

    public ImportCustomizer addStaticImport(final String className, final String fieldName) {
    	throw new IllegalStateException("ImportCustomizer not supported in Groovy 1.7");
    }

    public ImportCustomizer addStaticStars(final String... classNames) {
    	throw new IllegalStateException("ImportCustomizer not supported in Groovy 1.7");
    }

    public ImportCustomizer addStaticImport(final String alias, final String className, final String fieldName) {
    	throw new IllegalStateException("ImportCustomizer not supported in Groovy 1.7");
    }

    public ImportCustomizer addImports(final String... imports) {
    	throw new IllegalStateException("ImportCustomizer not supported in Groovy 1.7");
    }

    public ImportCustomizer addStarImports(final String... packageNames) {
    	throw new IllegalStateException("ImportCustomizer not supported in Groovy 1.7");
    }

}
