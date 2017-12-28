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
package org.eclipse.jdt.groovy.core.tests.basic;

import static org.junit.Assume.assumeTrue;

import org.junit.Before;
import org.junit.Test;

public final class GroovySimpleTests_Compliance_1_8 extends GroovyCompilerTestSuite {

    @Before
    public void setUp() {
        assumeTrue(isAtLeastJava(JDK8));
    }

    @Test
    public void testDefaultAndStaticMethodInInterface() {
        runConformTest(new String[] {
            "p/IExample.java",
            "package p;\n" +
            "public interface IExample {\n" +
            "  void testExample();\n" +
            "  static void callExample() {}\n" +
            "  default void callDefault() {}\n" +
            "}\n",

            "p/Example.groovy",
            "package p\n" +
            "class Example implements IExample {\n" +
            "  public void testExample() {}\n" +
            "}\n",
        });
    }

    @Test
    public void testFunctionalInterfaceInterfaceCoercion() {
        runConformTest(new String[] {
            "Foo.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Foo {\n" +
            "  String bar\n" +
            "  def baz() {\n" +
            "    Collection<Foo> coll\n" +
            "    coll.removeIf { it.bar == null }\n" + // Closure should coerce to SAM type java.util.function.Predicate
            "  }\n" +
            "}\n",
        });
    }
}
