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
package org.codehaus.groovy.eclipse.codebrowsing.tests;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.groovy.core.util.JavaConstants;

/**
 * @author Kris De Volder
 * @author Andy Clement
 */
public final class JDTAstPositionTests extends BrowsingTestCase {

    public static junit.framework.Test suite() {
        return newTestSuite(JDTAstPositionTests.class);
    }

    private WorkingCopyOwner workingCopyOwner;
    private IProblemRequestor problemRequestor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Need an active problem requestor to make reconcile turn on binding
        // resolution.
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

    public void testAnnotationPositions_STS3822() throws Exception {
        //@formatter:off
        final String contents =
                "class main_test extends BaseTest{\n"+
                "  @Foo(s = '%1')\n"+
                "  static Object[][] P() {\n"+
                "    return null;\n"+
                "   return [\n"+
                "       [\n"+
                "           \"a\",\n"+
                "           \"b\",\n"+
                "           \"c\"\n"+
                "       ]\n"+
                "   ]\n"+
                "  }\n"+
                "\n"+
                "  @Bar(ddd = \"P\")\n"+
                "  final void test_some_things(def a, def b, def c) {\n"+
                "     method_1(a)\n"+
                "     method_2(b)\n"+
                "     method_3(c)\n"+
                "  }\n"+
                "\n"+
                "  void setUp() {\n"+
                "   XXX =\n"+
                "   [\n"+
                "     \"param_1\": \"1\",\n"+
                "     \"param_2\": \"2\"\n"+
                "   ]\n"+
                "   setUpBase()\n"+
                "  }\n"+
                "}\n";
        //@formatter:on
        CompilationUnit ast = getAST(contents);
        traverseAst(contents, ast);
    }

    // This variant doesn't have a well formed array spec on method P()
    public void testAnnotationPositions_STS3822_2() throws Exception {
        //@formatter:off
        final String contents =
                "class main_test extends BaseTest{\n"+
                "  @Foo(s = '%1')\n"+
                "  static Object[][ P() {\n"+
                "    return null;\n"+
                "   return [\n"+
                "       [\n"+
                "           \"a\",\n"+
                "           \"b\",\n"+
                "           \"c\"\n"+
                "       ]\n"+
                "   ]\n"+
                "  }\n"+
                "\n"+
                "  @Bar(ddd = \"P\")\n"+
                "  final void test_some_things(def a, def b, def c) {\n"+
                "     method_1(a)\n"+
                "     method_2(b)\n"+
                "     method_3(c)\n"+
                "  }\n"+
                "\n"+
                "  void setUp() {\n"+
                "   XXX =\n"+
                "   [\n"+
                "     \"param_1\": \"1\",\n"+
                "     \"param_2\": \"2\"\n"+
                "   ]\n"+
                "   setUpBase()\n"+
                "  }\n"+
                "}\n";
        //@formatter:on
        CompilationUnit ast = getAST(contents);
        traverseAst(contents, ast);
    }

    public void testStringArrayArgs_STS3787() throws Exception {
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

    public void testStringVarArg_STS3787() throws Exception {
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
        GroovyCompilationUnit unit = addGroovySource(contents);
        IProgressMonitor monitor = new NullProgressMonitor();
        unit.becomeWorkingCopy(monitor);
        CompilationUnit ast = unit.reconcile(JavaConstants.AST_LEVEL, true, workingCopyOwner, monitor);
        return ast;
    }
}
