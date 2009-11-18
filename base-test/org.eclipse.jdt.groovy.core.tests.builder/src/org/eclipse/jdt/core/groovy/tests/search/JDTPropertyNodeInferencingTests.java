 /*
 * Copyright 2003-2009 the original author or authors.
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

import junit.framework.Test;

/**
 * Tests for PropertyNodes of JDTClassNodes
 * @author Andrew Eisenberg
 * @created Nov 13, 2009
 *
 */
public class JDTPropertyNodeInferencingTests extends AbstractInferencingTest {
 
    /**
     * 
     */
    private static final String INTEGER = "java.lang.Integer";

    public static Test suite() {
        return buildTestSuite(JDTPropertyNodeInferencingTests.class);
    }

    public JDTPropertyNodeInferencingTests(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
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

    // this one is currently passing because it does not make any use of property nodes
    public void testNodeFromJava1() throws Exception {
        String contents = "def x = new JavaUnit()\nx";
        assertType(contents, contents.lastIndexOf('x'), contents.lastIndexOf('x')+1, "JavaUnit");
    }
    
    public void testPropertyNodeFromJava1() throws Exception {
    	String contents = "new JavaUnit().foo";
        assertType(contents,contents.lastIndexOf("foo"),contents.length(), "int");
    }
    
    public void testPropertyNodeFromJava2() throws Exception {
        String contents = "def x = new JavaUnit().foo\nx";
        assertType(contents, contents.lastIndexOf('x'), contents.lastIndexOf('x')+1, "int");
    }
    public void testPropertyNodeFromJava3() throws Exception {
        String contents = "def x = new JavaUnit().javaUnit.foo\nx";
        assertType(contents, contents.lastIndexOf('x'), contents.lastIndexOf('x')+1, "int");
    }
    public void testPropertyNodeFromJava4() throws Exception {
        String contents = "def x = new JavaUnit()\nx.javaUnit.foo";
        assertType(contents, contents.lastIndexOf("foo"), contents.lastIndexOf("foo")+"foo".length(), "int");
    }
    
}
