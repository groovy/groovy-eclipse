/*
 * Copyright 2009-2016 the original author or authors.
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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import groovyjarjarasm.asm.Opcodes;
import org.codehaus.groovy.antlr.AntlrParserPlugin;
import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.ParserPlugin;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * @author Andrew Eisenberg
 * @created Aug 11, 2009
 *
 * This class is used to parse a snippet of groovy source code into a module node
 * The module node is not resolved
 */
public class GroovySnippetParser {

    private static class MockCompilationUnit implements ICompilationUnit {

        private char[] contents;
        private char[] fileName;

        MockCompilationUnit(char[] contents, char[] fileName) {
            this.contents = contents;
            this.fileName = fileName;
        }

        public char[] getContents() {
            return contents;
        }

        public char[] getMainTypeName() {
            return new char[0];
        }

        public char[][] getPackageName() {
            return new char[0][];
        }

        public char[] getFileName() {
            return fileName;
        }

        public boolean ignoreOptionalProblems() {
            return false;
        }

    }

    private CategorizedProblem[] problems;

    /**
     * Compiles source code into a ModuleNode.  Source code
     * must be a complete file including package declaration
     * and import statements.
     *
     * @param source the groovy source code to compile
     */
    public ModuleNode parse(String source) {
        Map<String, String> table = JavaCore.getOptions();
        table.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.ENABLED);
        CompilerOptions options = new CompilerOptions(table);
        ProblemReporter reporter = new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(), options,
                new DefaultProblemFactory());

        GroovyParser parser = new GroovyParser(options, reporter, false, true);
        ICompilationUnit unit = new MockCompilationUnit(source.toCharArray(), "Hello.groovy".toCharArray());
        CompilationResult compilationResult = new CompilationResult(unit, 0, 0, options.maxProblemsPerUnit);


        GroovyCompilationUnitDeclaration decl =
            (GroovyCompilationUnitDeclaration)
            parser.dietParse(unit, compilationResult);
        ModuleNode node = decl.getModuleNode();

        if (node == null) {
            return null;
        }
        // Remove any remaining synthetic methods
        for (ClassNode classNode : (Iterable<ClassNode>) node.getClasses()) {
            for (Iterator<MethodNode> methodIter = classNode.getMethods().iterator(); methodIter.hasNext();) {
                MethodNode method = methodIter.next();
                if ((method.getModifiers() & Opcodes.ACC_SYNTHETIC) != 0) {
                    methodIter.remove();
                }
            }
        }

        problems = compilationResult.getErrors();
        return node;
    }

    public CategorizedProblem[] getProblems() {
        return problems;
    }

    public GroovySourceAST parseForCST(String source) {
        Hashtable<String, String> table = JavaCore.getOptions();
        table.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.ENABLED);
        CompilerOptions options = new CompilerOptions(table);
        ProblemReporter reporter = new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(), options,
                new DefaultProblemFactory());

        GroovyParser parser = new GroovyParser(null, reporter, false, true);
        ICompilationUnit unit = new MockCompilationUnit(source.toCharArray(), "Hello.groovy".toCharArray());
        CompilationResult compilationResult = new CompilationResult(unit, 0, 0, options.maxProblemsPerUnit);


        GroovyCompilationUnitDeclaration decl =
            (GroovyCompilationUnitDeclaration)
            parser.dietParse(unit, compilationResult);
        SourceUnit sourceUnit = decl.getSourceUnit();
        ParserPlugin parserPlugin = (ParserPlugin) ReflectionUtils.getPrivateField(SourceUnit.class, "parserPlugin", sourceUnit);
        if (parserPlugin instanceof AntlrParserPlugin) {
            return (GroovySourceAST) ReflectionUtils.getPrivateField(AntlrParserPlugin.class, "ast", parserPlugin);
        } else {
            return null;
        }
    }
}
