/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.grails.ide.eclipse.groovy.debug.core.evaluation;

import groovy.lang.GroovyClassLoader;

import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;


/**
 * A classloader for the script to be evaluated that has a different parent
 * than the currently running STS instance or the debugged.  This way
 * classes in the script do not conflict with classes in running STS instance.
 * @author Andrew Eisenberg
 * @since 2.5.1
 */
public class JDIGroovyClassLoader extends GroovyClassLoader {

    private ClassNode theClass;
    
    public JDIGroovyClassLoader() {
        super();
    }

    public JDIGroovyClassLoader(ClassLoader loader) {
        super(loader);
    }

    class JDIClassCollector extends ClassCollector {

        protected JDIClassCollector(InnerLoader cl, CompilationUnit unit, SourceUnit su) {
            super(cl, unit, su);
        }
        
        @SuppressWarnings("rawtypes")
        protected Class createClass(byte[] code, ClassNode classNode) {
            theClass = classNode;
            return super.createClass(code, classNode);
        }
        
    }
    
    protected ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
        InnerLoader loader = AccessController.doPrivileged(new PrivilegedAction<InnerLoader>() {
            public InnerLoader run() {
                return new InnerLoader(JDIGroovyClassLoader.this);
            }
        });
        return new JDIClassCollector(loader, unit, su);
    }
    
    public ClassNode getTheClass() {
        return theClass;
    }
    
    /**
     * Override this method to add a new phase to the compilation
     * We need to convert comparator binary expressions to using the JDIComparator instead
     * of ScriptByteCodeAdapter.
     */
    @Override
    protected CompilationUnit createCompilationUnit(
            CompilerConfiguration config, CodeSource source) {
        CompilationUnit compilationUnit = super.createCompilationUnit(config, source);
        compilationUnit.addPhaseOperation(new CompilationUnit.PrimaryClassNodeOperation() {
            
            public void call(SourceUnit source, GeneratorContext context,
                    ClassNode classNode) throws CompilationFailedException {
                new JDIComparator.ComparatorVisitor().visitBlockStatement(classNode.getModule().getStatementBlock());
            }
        }, Phases.CANONICALIZATION);
        return compilationUnit;
    }
}
