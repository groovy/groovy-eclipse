/*
 * Copyright 2009-2019 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.search;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assume.assumeTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Waiting on GROOVY-9064")
public final class Groovy25InferencingTests extends InferencingTestSuite {

    @Before
    public void setUp() {
        assumeTrue(isAtLeastGroovy(25));
    }

    @Test
    public void testCompileStaticVariableAssignment1() {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "void meth() {\n" +
            "  List list = Collections.emptyList()\n" +
            "}";

        assertType(contents, "list", "java.util.List");
    }

    @Test
    public void testCompileStaticVariableAssignment2() {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "void meth() {\n" +
            "  List list = new ArrayList()\n" +
            "}";

        assertType(contents, "list", "java.util.List");
    }

    @Test
    public void testCompileStaticVariableAssignment3() {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "void meth() {\n" +
            "  List list = [1, 2]\n" +
            "}";

        assertType(contents, "list", "java.util.List");
    }

    @Test
    public void testCompileStaticVariableAssignment4() {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "void meth(boolean flag) {\n" +
            "  List list = [1, 2]\n" +
            "  if (flag) {\n" +
            "    list = [3, 'four']\n" +
            "  }\n" +
            "}";

        assertType(contents, "list", "java.util.List");
    }

    @Test
    public void testCompileStaticVariableAssignment5() {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "void meth() {\n" +
            "  Map map = Collections.emptyMap()\n" +
            "}";

        assertType(contents, "map", "java.util.Map");
    }

    @Test
    public void testCompileStaticVariableAssignment6() {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "void meth() {\n" +
            "  Map map = new HashMap()\n" +
            "}";

        assertType(contents, "map", "java.util.Map");
    }

    @Test
    public void testCompileStaticVariableAssignment7() {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "void meth() {\n" +
            "  Map map = [:]\n" +
            "}";

        assertType(contents, "map", "java.util.Map");
    }

    @Test
    public void testCompileStaticVariableAssignment8() {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "void meth(boolean flag) {\n" +
            "  Map map = [:]\n" +
            "  if (flag) {\n" +
            "    map = [a: 1, b: '2']\n" +
            "  }\n" +
            "}";

        assertType(contents, "map", "java.util.Map");
    }
}
