/*
 * Copyright 2009-2017 the original author or authors.
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

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for PropertyNodes of JDTClassNodes.
 */
public final class JDTPropertyNodeInferencingTests extends InferencingTestSuite {

    @Before
    public void setUp() throws Exception {
        createJavaUnit("JavaUnit",
                "class JavaUnit {\n" +
                "  int getFoo() { " +
                "    return 0; " +
                "  } " +
                "  JavaUnit getJavaUnit() { " +
                "    return null; " +
                "  } " +
                "}");
    }

    @Test // this one is currently passing because it does not make any use of property nodes
    public void testNodeFromJava1() throws Exception {
        String contents = "def x = new JavaUnit()\nx";
        assertType(contents, contents.lastIndexOf('x'), contents.lastIndexOf('x')+1, "JavaUnit");
    }

    @Test
    public void testPropertyNodeFromJava1() throws Exception {
        String contents = "new JavaUnit().foo";
        assertType(contents,contents.lastIndexOf("foo"),contents.length(), "java.lang.Integer");
    }

    @Test
    public void testPropertyNodeFromJava2() throws Exception {
        String contents = "def x = new JavaUnit().foo\nx";
        assertType(contents, contents.lastIndexOf('x'), contents.lastIndexOf('x')+1, "java.lang.Integer");
    }

    @Test
    public void testPropertyNodeFromJava3() throws Exception {
        String contents = "def x = new JavaUnit().javaUnit.foo\nx";
        assertType(contents, contents.lastIndexOf('x'), contents.lastIndexOf('x')+1, "java.lang.Integer");
    }

    @Test
    public void testPropertyNodeFromJava4() throws Exception {
        String contents = "def x = new JavaUnit()\nx.javaUnit.foo";
        assertType(contents, contents.lastIndexOf("foo"), contents.lastIndexOf("foo")+"foo".length(), "java.lang.Integer");
    }

    @Test // a property coming from a binary groovy file can have its generated getter and setter seen
    public void testPropertyNodeReferenceInBinary1() throws Exception {
        String contents = "new AGroovyClass().getProp1()";
        env.addJar(project.getFullPath(), "lib/test-groovy-project.jar");
        assertType(contents, contents.lastIndexOf("getProp1"), contents.lastIndexOf("getProp1")+"getProp1".length(), "java.lang.Integer");
    }
}
