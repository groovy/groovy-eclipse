/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.core.compiler;

import static java.util.UUID.randomUUID;

import static org.eclipse.jdt.groovy.core.util.GroovyUtils.isSynthetic;

import java.util.Iterator;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.CompilationUnitResolver;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.core.BasicCompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;

/**
 * Compiles a snippet of Groovy source code into a ModuleNode.  The client is
 * responsible for calling {@link #cleanup()} when all {@link ClassNode}s are
 * no longer needed.
 */
public class GroovySnippetCompiler {

    private CompilerOptions compilerOptions;
    private INameEnvironment nameEnvironment;

    public GroovySnippetCompiler(final JavaProject project) {
        try {
            compilerOptions = new CompilerOptions(project.getOptions(true));
            nameEnvironment = new SearchableEnvironment(project, (WorkingCopyOwner) null, false);
        } catch (JavaModelException e) {
            GroovyCore.logException("Problem initializing snippet compiler for project " + project.getElementName(), e);
        }
    }

    /**
     * Compiles source code into a ModuleNode when there is no file name.
     */
    public ModuleNode compile(final String source) {
        return compile(source, null);
    }

    /**
     * Compiles source code into a ModuleNode.  Source code
     * must be a complete file including package declaration
     * and import statements.
     *
     * @param source the groovy source code to compile
     * @param sourcePath the path including file name to compile; may be {@code null}
     */
    public ModuleNode compile(final String source, final String sourcePath) {
        GroovyCompilationUnitDeclaration decl = internalCompile(source, sourcePath);
        ModuleNode node = decl.getModuleNode();

        // remove any remaining synthetic methods
        for (ClassNode classNode : node.getClasses()) {
            for (Iterator<MethodNode> methodIter = classNode.getMethods().iterator(); methodIter.hasNext();) {
                MethodNode method = methodIter.next();
                if (isSynthetic(method)) {
                    methodIter.remove();
                }
            }
        }
        return node;
    }

    public CompilationResult compileForErrors(final String source, final String sourcePath) {
        GroovyCompilationUnitDeclaration unit = internalCompile(source, sourcePath);
        return unit.compilationResult();
    }

    private GroovyCompilationUnitDeclaration internalCompile(final String source, String sourcePath) {
        if (sourcePath == null) {
            sourcePath = "Snippet" + randomUUID().toString().replaceAll("-", "") + ".groovy";
        } else if (!ContentTypeUtils.isGroovyLikeFileName(sourcePath)) {
            sourcePath = sourcePath.concat(".groovy");
        }

        Compiler compiler = new CompilationUnitResolver(nameEnvironment, DefaultErrorHandlingPolicies.proceedWithAllProblems(), compilerOptions, result -> {}, new DefaultProblemFactory(), null, true);
        ICompilationUnit unit = new BasicCompilationUnit(source.toCharArray(), CharOperation.NO_CHAR_CHAR, sourcePath, (IJavaElement) null);
        GroovyCompilationUnitDeclaration decl = (GroovyCompilationUnitDeclaration) compiler.resolve(unit, true, false, false);
        return decl;
    }

    public void cleanup() {
        nameEnvironment.cleanup();
    }
}
