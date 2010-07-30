/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.groovy.tests.locations;

import java.util.Hashtable;

import junit.framework.Test;

import org.codehaus.jdt.groovy.integration.LanguageSupportFactory;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;

/**
 * Tests that our changes to the ASTConverter do not 
 * break Java code
 * @author Andrew Eisenberg
 * @created Jul 30, 2010
 */
public class ASTConverterTests extends TestCase {
    public ASTConverterTests(String name) {
        super(name);
    }

    public static Test suite() {
        return buildTestSuite(ASTConverterTests.class);
    }
    
    public void testJavaASTConversionEnum() throws Exception {
        checkJavaName("enum MyNames {\n NAME1(0), NAME2(0);\n private MyNames(int val) { } }", "MyNames");
    }
    
    public void testJavaASTConversionClass() throws Exception {
        checkJavaName("class MyNames {\n \n private MyNames(int val) { } }", "MyNames");
    }
    
    public void testJavaASTConversionInterface() throws Exception {
        checkJavaName("interface MyNames {\n \n int myMethod(int val); }", "MyNames");
    }
    
    
    private void checkJavaName(String contents, String expectedName) {
        int start = contents.lastIndexOf(expectedName);
        int length = expectedName.length();
        SimpleName name = (SimpleName) findJavaNodeAt(contents, start, length);
        assertEquals(expectedName, name.getIdentifier());
        assertEquals("Invalid start position", start, name.getStartPosition());
        assertEquals("Invalid length position", length, name.getLength());
    }
    
    private ASTNode findJavaNodeAt(String contents, int start, int length) {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        Hashtable<String, String> options = JavaCore.getOptions();
        options.put(CompilerOptions.OPTION_Source, "1.5");
        parser.setCompilerOptions(options);
        parser.setSource(contents.toCharArray());
        parser.setStatementsRecovery(true);
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        assertEquals("Compilation unit should not have any problems:\n" + printProblems(unit), 
                0, unit.getProblems().length);
        
        return NodeFinder.perform(unit, start, length);
    }

    private String printProblems(CompilationUnit unit) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < unit.getProblems().length; i++) {
            sb.append(unit.getProblems()[i]);
            sb.append("\n");
        }
        return sb.toString();
    }
}