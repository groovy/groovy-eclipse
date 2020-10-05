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
import static org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies.proceedWithAllProblems;

import java.util.Iterator;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * This class is used to parse a snippet of groovy code into a module node.
 * The module node is not resolved.
 */
public class GroovySnippetParser {

    private CategorizedProblem[] problems;

    public CategorizedProblem[] getProblems() {
        return problems;
    }

    /**
     * Compiles source code into a ModuleNode.  Source code
     * must be a complete file including package declaration
     * and import statements.
     *
     * @param source the groovy source code to compile
     */
    public ModuleNode parse(final CharSequence source) {
        char[] contents = source.toString().toCharArray();
        String fileName = "Snippet" + randomUUID().toString().replaceAll("-", "") + ".groovy";

        Map<String, String> options = JavaCore.getOptions();
        options.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.ENABLED);
        options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);

        CompilerOptions compilerOptions = new CompilerOptions(options);
        ProblemReporter problemReporter = new ProblemReporter(proceedWithAllProblems(), compilerOptions, new DefaultProblemFactory());
        CompilationResult compilationResult = new CompilationResult(fileName.toCharArray(), 0, 0, compilerOptions.maxProblemsPerUnit);
        GroovyCompilationUnitDeclaration gcud = new GroovyParser(compilerOptions, problemReporter, false, true).dietParse(contents, fileName, compilationResult);

        problems = compilationResult.getProblems();
        ModuleNode node = gcud.getModuleNode();
        if (node == null) {
            return null;
        }

        // remove any remaining synthetic methods
        for (ClassNode classNode : node.getClasses()) {
            for (Iterator<MethodNode> it = classNode.getMethods().iterator(); it.hasNext();) {
                MethodNode method = it.next();
                if (isSynthetic(method)) {
                    it.remove();
                }
            }
        }

        return node;
    }
}
