/*
 * Copyright 2009-2017 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.search;

import static org.eclipse.jdt.core.tests.util.GroovyUtils.isAtLeastGroovy;
import static org.junit.Assume.assumeTrue;

import org.junit.Before;
import org.junit.Test;

public final class Groovy20InferencingTests extends InferencingTestSuite {

    @Before
    public void setUp() {
        assumeTrue(isAtLeastGroovy(20));
    }

    @Test // tests CompareToNullExpression
    public void testCompileStatic1() throws Exception {
        String contents =
            "import groovy.transform.CompileStatic;\n" +
                "class CompilingStatic {\n" +
                "\n" +
                "    @CompileStatic\n" +
                "    def foo(String args) {\n" +
                "        args!= null\n" +
                "    }\n" +
                "}";
        int start = contents.lastIndexOf("args");
        int end = start + "args".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // tests CompareToNullExpression
    public void testCompileStatic2() throws Exception {
        String contents =
            "import groovy.transform.CompileStatic;\n" +
                "class CompilingStatic {\n" +
                "\n" +
                "    @CompileStatic\n" +
                "    def foo(String args) {\n" +
                "        args== null\n" +
                "    }\n" +
                "}";
        int start = contents.lastIndexOf("args");
        int end = start + "args".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // tests CompareIdentityExpression
    public void testCompileStatic3() throws Exception {
        String contents =
            "import groovy.transform.CompileStatic;\n" +
                "class CompilingStatic {\n" +
                "\n" +
                "    @CompileStatic\n" +
                "    def foo(String args) {\n" +
                "        args== 9\n" +
                "    }\n" +
                "}";
        int start = contents.lastIndexOf("args");
        int end = start + "args".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // tests CompareIdentityExpression
    public void testCompileStatic4() throws Exception {
        String contents =
            "import groovy.transform.CompileStatic;\n" +
                "class CompilingStatic {\n" +
                "\n" +
                "    @CompileStatic\n" +
                "    def foo(String args) {\n" +
                "        9 == args\n" +
                "    }\n" +
                "}";
        int start = contents.lastIndexOf("args");
        int end = start + "args".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // tests CompareToNullExpression
    public void testCompileStatic5() throws Exception {
        String contents =
            "import groovy.transform.CompileStatic;\n" +
                "class CompilingStatic {\n" +
                "\n" +
                "    @CompileStatic\n" +
                "    def foo(String args) {\n" +
                "        null== args\n" +
                "    }\n" +
                "}";
        int start = contents.lastIndexOf("args");
        int end = start + "args".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // tests CompareToNullExpression
    public void testTypeChecked1() throws Exception {
        String contents =
            "import groovy.transform.TypeChecked;\n" +
                "class CompilingStatic {\n" +
                "\n" +
                "    @TypeChecked\n" +
                "    def foo(String args) {\n" +
                "        args!= null\n" +
                "    }\n" +
                "}";
        int start = contents.lastIndexOf("args");
        int end = start + "args".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // tests CompareToNullExpression
    public void testTypeChecked2() throws Exception {
        String contents =
            "import groovy.transform.TypeChecked;\n" +
                "class CompilingStatic {\n" +
                "\n" +
                "    @TypeChecked\n" +
                "    def foo(String args) {\n" +
                "        args== null\n" +
                "    }\n" +
                "}";
        int start = contents.lastIndexOf("args");
        int end = start + "args".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // tests CompareIdentityExpression
    public void testTypeChecked3() throws Exception {
        String contents =
            "import groovy.transform.TypeChecked;\n" +
                "class CompilingStatic {\n" +
                "\n" +
                "    @TypeChecked\n" +
                "    def foo(String args) {\n" +
                "        args== 9\n" +
                "    }\n" +
                "}";
        int start = contents.lastIndexOf("args");
        int end = start + "args".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // tests CompareIdentityExpression
    public void testTypeChecked4() throws Exception {
        String contents =
            "import groovy.transform.TypeChecked;\n" +
                "class CompilingStatic {\n" +
                "\n" +
                "    @TypeChecked\n" +
                "    def foo(String args) {\n" +
                "        9 == args\n" +
                "    }\n" +
                "}";
        int start = contents.lastIndexOf("args");
        int end = start + "args".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // tests CompareToNullExpression
    public void testTypeChecked5() throws Exception {
        String contents =
            "import groovy.transform.TypeChecked;\n" +
                "class CompilingStatic {\n" +
                "\n" +
                "    @TypeChecked\n" +
                "    def foo(String args) {\n" +
                "        null== args\n" +
                "    }\n" +
                "}";
        int start = contents.lastIndexOf("args");
        int end = start + "args".length();
        assertType(contents, start, end, "java.lang.String");
    }
}
