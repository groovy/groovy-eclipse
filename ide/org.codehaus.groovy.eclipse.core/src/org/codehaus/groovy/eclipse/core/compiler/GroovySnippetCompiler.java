/*
 * Copyright 2009-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.core.compiler;

import static org.eclipse.jdt.groovy.core.util.GroovyUtils.isSynthetic;

import java.util.Iterator;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.CompilationUnitResolver;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;

/**
 * Compiles a snippet of groovy source code into a module node.  The client is
 * responsible for calling {@link #cleanup()} when all {@link ClassNode}s are
 * no longer needed.
 */
public class GroovySnippetCompiler {

    private INameEnvironment nameEnvironment;

    public GroovySnippetCompiler(JavaProject project) {
        try {
            nameEnvironment = new SearchableEnvironment(project, (WorkingCopyOwner) null, false);
        } catch (JavaModelException e) {
            GroovyCore.logException("Problem initializing snippet compiler for project " + project.getElementName(), e);
        }
    }

    /**
     * Compiles source code into a ModuleNode.  Source code
     * must be a complete file including package declaration
     * and import statements.
     *
     * @param source the groovy source code to compile
     * @param sourcePath the path including file name to compile; may be {@code null}
     */
    public ModuleNode compile(String source, String sourcePath) {
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

    public CompilationResult compileForErrors(String source, String sourcePath) {
        GroovyCompilationUnitDeclaration unit = internalCompile(source, sourcePath);
        return unit.compilationResult();
    }

    private GroovyCompilationUnitDeclaration internalCompile(String source, String sourcePath) {
        if (sourcePath == null) {
            sourcePath = "Nothing.groovy";
        } else if (!ContentTypeUtils.isGroovyLikeFileName(sourcePath)) {
            sourcePath = sourcePath.concat(".groovy");
        }

        Map<String, String> options = JavaCore.getOptions();
        options.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.ENABLED);
        Compiler compiler = new CompilationUnitResolver(nameEnvironment, DefaultErrorHandlingPolicies.proceedWithAllProblems(), new CompilerOptions(options), result -> {}, new DefaultProblemFactory(), null, true);
        GroovyCompilationUnitDeclaration decl = (GroovyCompilationUnitDeclaration) compiler.resolve(new MockCompilationUnit(source.toCharArray(), sourcePath.toCharArray()), true, false, false);
        return decl;
    }

    public void cleanup() {
        nameEnvironment.cleanup();
    }

    /**
     * Compiles source code into a module node when there is no file name.
     */
    public ModuleNode compile(String source) {
        return compile(source, null);
    }
}
