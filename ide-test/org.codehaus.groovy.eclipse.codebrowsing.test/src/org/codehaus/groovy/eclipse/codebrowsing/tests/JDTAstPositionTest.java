/*
 * Copyright 2014 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.tests;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * @author Kris De Volder
 */
public class JDTAstPositionTest extends BrowsingTestCase {

    private int astLevel;
    private WorkingCopyOwner workingCopyOwner;
    private IProblemRequestor problemRequestor;

    public JDTAstPositionTest() {
        super(JDTAstPositionTest.class.getName());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        astLevel = AST.JLS3;
        try {
            AST.class.getDeclaredField("JLS8");
            astLevel = 8;
        } catch (NoSuchFieldException nsfe) {
            // pre-java8
        }
        
        //Need an active problem requestor to make reconcile turn on binding resolution.
        // This approximates better what is happening in reconciling for an actual editor in workspace.
        this.problemRequestor = new IProblemRequestor() {
            public boolean isActive() {
                return true;
            }
            
            public void endReporting() {
            }
            
            public void beginReporting() {
            }
            
            public void acceptProblem(IProblem problem) {
            	System.out.println("problem: "+problem);
            }
        };
        this.workingCopyOwner = new WorkingCopyOwner() {
        	@Override
        	public IProblemRequestor getProblemRequestor(ICompilationUnit workingCopy) {
        		return problemRequestor;
        	}
        };
    }
    
    public void testStringArrayArgs() throws Exception {
        final String contents = 
                  "class MyMain {\n"
                + "    static void main(String[] args) {\n"
                + "    }\n"
                + "}\n";
        CompilationUnit ast = getAST(contents);
        //I wished to check the String[] node has correct source location info
        // but it does not appear in the final AST. Instead it seems to be 
        // represented as a String vararg parameter. There's no 'ArrayType' node in the AST at all.
        
        traverseAst(contents, ast);
    }

    public void testStringVarArg() throws Exception {
        final String contents = 
                  "class MyMain {\n"
                + "    static void munching(String... args) {\n"
                + "    }\n"
                + "}\n";
        CompilationUnit ast = getAST(contents);
        traverseAst(contents, ast);
    }

    private void traverseAst(final String contents, CompilationUnit ast) {
        ast.accept(new ASTVisitor() {
        	@Override
        	public void preVisit(ASTNode node) {
        		System.out.println("--- "+node.getClass());
        		System.out.println(getText(node, contents));
        		System.out.println("------------------------------");
        	}
        });
    }
    

    private String getText(ASTNode node, String text) {
        int start = node.getStartPosition();
        int len = node.getLength();
        if (start==-1 && len==0) {
            return "<UNKNOWN>";
        } else {
            return text.substring(start, start+len);
        }
    }
    
    private CompilationUnit getAST(String contents) throws Exception {
        GroovyCompilationUnit unit = getCompilationUnitFor(contents);
        CompilationUnit ast = unit.reconcile(astLevel, true, workingCopyOwner, new NullProgressMonitor());
        return ast;
    }


}
