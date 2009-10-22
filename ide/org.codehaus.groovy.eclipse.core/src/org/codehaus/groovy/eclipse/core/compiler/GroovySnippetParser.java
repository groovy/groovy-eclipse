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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.antlr.AntlrParserPlugin;
import org.codehaus.groovy.antlr.ErrorRecoveredCSTParserPluginFactory;
import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.ICSTReporter;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.ParserPlugin;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.CSTNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.objectweb.asm.Opcodes;


/**
 * @author Andrew Eisenberg
 * @created Aug 11, 2009
 *
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
        
    }
    
    /**
     * 
     * @author Andrew Eisenberg
     * @created Aug 11, 2009
     * Provide an empty requestor, no compilation results required
     */
    private static class Requestor implements ICompilerRequestor {
        public void acceptResult(CompilationResult result) {
        }
    }
    

    
    
    /**
     * Compiles source code into a ModuleNode.  Source code 
     * must be a complete file including package declaration
     * and import statements.
     * 
     * @param source the groovy source code to compile
     */
    public ModuleNode parse(String source) {
        
        Hashtable table = JavaCore.getOptions();
        table.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.ENABLED);
        CompilerOptions options = new CompilerOptions(table);
        ProblemReporter reporter = new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(), options,
                new DefaultProblemFactory());

        GroovyParser parser = new GroovyParser(options, reporter);
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
        return node;
    }
    
    
    
    public GroovySourceAST parseForCST(String source) {
        Hashtable table = JavaCore.getOptions();
        table.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.ENABLED);
        CompilerOptions options = new CompilerOptions(table);
        ProblemReporter reporter = new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(), options,
                new DefaultProblemFactory());

        GroovyParser parser = new GroovyParser(null, reporter);
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
