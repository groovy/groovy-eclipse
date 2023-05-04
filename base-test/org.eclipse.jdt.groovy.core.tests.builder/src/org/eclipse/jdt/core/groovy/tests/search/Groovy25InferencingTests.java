/*
 * Copyright 2009-2023 the original author or authors.
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

import org.junit.Test;

public final class Groovy25InferencingTests extends InferencingTestSuite {

    @Test
    public void testCompileStaticVariableAssignment1() {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "void meth() {\n" +
            "  List list = Collections.emptyList()\n" +
            "}\n";

        assertType(contents, "list", "java.util.List<java.lang.Object>");
    }

    @Test
    public void testCompileStaticVariableAssignment2() {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "void meth() {\n" +
            "  List list = new ArrayList<>()\n" +
            "}\n";

        assertType(contents, "list", "java.util.ArrayList<java.lang.Object>");
    }

    @Test
    public void testCompileStaticVariableAssignment3() {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "void meth() {\n" +
            "  List list = [1, 2]\n" +
            "}\n";

        assertType(contents, "list", "java.util.ArrayList<java.lang.Integer>");
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
            "}\n";

        float version = Float.parseFloat(System.getProperty("java.specification.version"));
        assertType(contents, "list", "java.util.ArrayList<java.io.Serializable or java.lang.Comparable" +
            (version > 11 ? " or java.lang.constant.Constable or java.lang.constant.ConstantDesc" : "") + ">");
    }

    @Test
    public void testCompileStaticVariableAssignment5() {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "void meth() {\n" +
            "  Map map = Collections.emptyMap()\n" +
            "}\n";

        assertType(contents, "map", "java.util.Map<java.lang.Object,java.lang.Object>");
    }

    @Test
    public void testCompileStaticVariableAssignment6() {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "void meth() {\n" +
            "  Map map = new HashMap<>()\n" +
            "}\n";

        assertType(contents, "map", "java.util.HashMap<java.lang.Object,java.lang.Object>");
    }

    @Test
    public void testCompileStaticVariableAssignment7() {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "void meth() {\n" +
            "  Map map = [:]\n" +
            "}\n";

        assertType(contents, "map", "java.util.LinkedHashMap" + (!isAtLeastGroovy(40) ? "" : "<java.lang.Object,java.lang.Object>"));
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
            "}\n";

        float version = Float.parseFloat(System.getProperty("java.specification.version"));
        assertType(contents, "map", "java.util.LinkedHashMap<java.lang.String,java.io.Serializable or java.lang.Comparable" +
                            (version > 11 ? " or java.lang.constant.Constable or java.lang.constant.ConstantDesc" : "") + ">");
    }
}
