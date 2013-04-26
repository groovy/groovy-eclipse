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

package org.codehaus.groovy.eclipse.core.compiler;

import groovyjarjarasm.asm.Opcodes;

import java.util.Iterator;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.CompilationUnitResolver;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;

/**
 * @author Andrew Eisenberg
 * @created Aug 6, 2009
 *
 *
 *          This class is used to compile a snippet of groovy source code into a
 *          module node.
 *          The client is responsible for calling {@link #cleanup()} when all
 *          {@link ClassNode}s
 *          created by this compiler are no longer needed
 */
public class GroovySnippetCompiler {

    /**
     * @author Andrew Eisenberg
     * @created Aug 6, 2009
     * Provide an empty requestor, no compilation results required
     */
    private static class Requestor implements ICompilerRequestor {
        public void acceptResult(CompilationResult result) {
        }
    }

    private INameEnvironment nameEnvironment;

    public GroovySnippetCompiler(GroovyProjectFacade project) {
        try {
            nameEnvironment = new SearchableEnvironment((JavaProject) project.getProject(), (WorkingCopyOwner) null);
        } catch (JavaModelException e) {
            GroovyCore
                    .logException("Problem initializing snippet compiler for project " + project.getProject().getElementName(), e);
        }
    }

    /**
     * Compiles source code into a ModuleNode.  Source code
     * must be a complete file including package declaration
     * and import statements.
     *
     * @param source the groovy source code to compile
     * @param sourcePath the path including file name to compile.  Can be null
     */
    public ModuleNode compile(String source, String sourcePath) {
        GroovyCompilationUnitDeclaration decl = internalCompile(source,
                sourcePath);
        ModuleNode node = decl.getModuleNode();

        // Remove any remaining synthetic methods
        for (ClassNode classNode : (Iterable<ClassNode>) node.getClasses()) {
            for (Iterator<MethodNode> methodIter = classNode.getMethods().iterator(); methodIter.hasNext();) {
                MethodNode method = methodIter.next();
                if ((method.getModifiers() & Opcodes.ACC_SYNTHETIC) != 0) {
                    methodIter.remove();
                }
            }
        }
        return node;
    }

    public CompilationResult compileForErrors(String source, String sourcePath) {
        GroovyCompilationUnitDeclaration unit = internalCompile(source, sourcePath);
        return unit.compilationResult();
    }

    @SuppressWarnings({ "unchecked" })
    private GroovyCompilationUnitDeclaration internalCompile(String source,
            String sourcePath) {
        if (sourcePath == null) {
            sourcePath = "Nothing.groovy";
        } else if (! ContentTypeUtils.isGroovyLikeFileName(sourcePath)) {
            sourcePath = sourcePath.concat(".groovy");
        }

        Map options = JavaCore.getOptions();
        options.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.ENABLED);
        Compiler compiler = new CompilationUnitResolver(nameEnvironment, DefaultErrorHandlingPolicies.proceedWithAllProblems(),
                new CompilerOptions(options), new Requestor(), new DefaultProblemFactory(), null, true);
        GroovyCompilationUnitDeclaration decl =
            (GroovyCompilationUnitDeclaration)
            compiler.resolve(new MockCompilationUnit(source.toCharArray(), sourcePath.toCharArray()), true, false, false);
        return decl;
    }


    public void cleanup() {
        nameEnvironment.cleanup();
    }

    /**
     * Compile source code into a module node when
     * there is no file name
     */
    public ModuleNode compile(String source) {
        return compile(source, null);
    }
}
