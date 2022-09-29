/*
 * Copyright 2009-2022 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IProblemRequestor
import org.eclipse.jdt.core.WorkingCopyOwner
import org.eclipse.jdt.core.compiler.IProblem
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.groovy.core.util.JavaConstants
import org.junit.Before
import org.junit.Test

final class ASTPositionTests extends BrowsingTestSuite {

    private IProblemRequestor problemRequestor
    private WorkingCopyOwner workingCopyOwner

    @Before
    void setUp() {
        // Need an active problem requestor to make reconcile turn on binding resolution.
        // This approximates better what is happening in reconciling for an actual editor in workspace.
        problemRequestor = new IProblemRequestor() {
            @Override
            boolean isActive() {
                true
            }
            @Override
            void endReporting() {
            }
            @Override
            void beginReporting() {
            }
            @Override
            void acceptProblem(IProblem problem) {
                println "problem: $problem"
            }
        }

        workingCopyOwner = new WorkingCopyOwner() {
            @Override
            IProblemRequestor getProblemRequestor(ICompilationUnit workingCopy) {
                problemRequestor
            }
        }
    }

    @Test // GROOVY-9206
    void testAnnotationPositions1() {
        addJavaSource '''\
            |import java.lang.annotation.*;
            |@Target(ElementType.FIELD)
            |@interface Separator {
            |  char value();
            |}
            |'''.stripMargin(), 'Separator'

        String contents = '''\
            |class C {
            |  @Separator((char)';')
            |  String tokens
            |}
            |'''.stripMargin()

        traverseAST(contents)
    }

    @Test // STS-3822
    void testAnnotationPositions2() {
        String contents = '''\
            |class main_test extends BaseTest {
            |  @Foo(s = '%1')
            |  static Object[][] P() {
            |    return [
            |      [
            |        "a",
            |        "b",
            |        "c"
            |      ]
            |    ]
            |  }
            |  @Bar(ddd = "P")
            |  final void test_some_things(def a, def b, def c) {
            |    method_1(a)
            |    method_2(b)
            |    method_3(c)
            |  }
            |  void setUp() {
            |    XXX = [
            |      "param_1": "1",
            |      "param_2": "2"
            |    ]
            |    setUpBase()
            |  }
            |}
            |'''.stripMargin()

        traverseAST(contents)
    }

    @Test // STS-3822: This variant doesn't have a well-formed array spec on P()
    void testAnnotationPositions3() {
        String contents = '''\
            |class main_test extends BaseTest {
            |  @Foo(s = '%1')
            |  static Object[][ P() {
            |    return [
            |      [
            |        "a",
            |        "b",
            |        "c"
            |      ]
            |    ]
            |  }
            |  @Bar(ddd = "P")
            |  final void test_some_things(def a, def b, def c) {
            |    method_1(a)
            |    method_2(b)
            |    method_3(c)
            |  }
            |  void setUp() {
            |    XXX = [
            |      "param_1": "1",
            |      "param_2": "2"
            |    ]
            |    setUpBase()
            |  }
            |}
            |'''.stripMargin()

        traverseAST(contents)
    }

    @Test // STS-3787
    void testStringArrayArgs() {
        String contents = '''\
            |class MyMain {
            |  static void main(String[] args) {
            |  }
            |}
            |'''.stripMargin()

        // I wished to check the String[] node has correct source location info
        // but it does not appear in the final AST. Instead it seems to be
        // represented as a String vararg parameter. There's no 'ArrayType' node in the AST at all.
        traverseAST(contents)
    }

    @Test // STS-3787
    void testStringVarArg() {
        String contents = '''\
            |class MyMain {
            |  static void munching(String... args) {
            |  }
            |}
            |'''.stripMargin()

        traverseAST(contents)
    }

    //--------------------------------------------------------------------------

    private String getText(ASTNode node, String text) {
        int offset = node.startPosition, length = node.length
        if (offset == -1 && length == 0) {
            return '<UNKNOWN>'
        }
        return text.substring(offset, offset + length)
    }

    private CompilationUnit getAST(String contents) {
        GroovyCompilationUnit unit = addGroovySource(contents, nextUnitName())
        unit.reconcile(JavaConstants.AST_LEVEL, true, workingCopyOwner, new NullProgressMonitor())
    }

    private void traverseAST(String contents) {
        CompilationUnit ast = getAST(contents)
        ast.accept([preVisit: { ASTNode node ->
            println "--- ${node.class}"
            println getText(node, contents)
            println '----------------------------------------'
        }] as ASTVisitor)
    }
}
