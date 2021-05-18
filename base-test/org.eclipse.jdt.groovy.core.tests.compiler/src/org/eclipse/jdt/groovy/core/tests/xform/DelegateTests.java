/*
 * Copyright 2009-2021 the original author or authors.
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
package org.eclipse.jdt.groovy.core.tests.xform;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Test;

/**
 * Test cases for {@link groovy.lang.Delegate}.
 */
public final class DelegateTests extends GroovyCompilerTestSuite {

    @Test
    public void testDelegate1() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C { @Delegate URL myUrl }\n" +
            "\n" +
            "print C.getDeclaredMethod('getContent', Class[].class)\n",
        };
        //@formatter:on

        runConformTest(sources, "public final java.lang.Object C.getContent(java.lang.Class[]) throws java.io.IOException");
    }

    @Test // GROOVY-4320
    public void testDelegate2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface I {\n" +
            "  void event(String name)\n" +
            "  void event(String name, List values)\n" +
            "}\n" +
            "class C implements I {\n" +
            "  void event(String name, List values = []) {\n" +
            "    print \"$name:$values\"\n" +
            "  }\n" +
            "}\n" +
            "class D {\n" +
            "  @Delegate private C c = new C()\n" + // The method with default parameters "..." defines a method "void event(java.lang.String)" that is already defined.
            "}\n" +
            "\n" +
            "new D().event('x')\n",
        };
        //@formatter:on

        runConformTest(sources, "x:[]");
    }

    @Test // GROOVY-4516
    public void testDelegate3() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C {\n" +
            "  def m(boolean flag = true) {\n" +
            "    flag\n" +
            "  }\n" +
            "}\n" +
            "class D {\n" +
            "  @Delegate private C c = new C()\n" +
            "}\n" +
            "print new D().m()\n" +
            "print new D().m(false)\n",
        };
        //@formatter:on

        runConformTest(sources, "truefalse");
    }

    @Test
    public void testDelegate4() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C {\n" +
            "  def m(x = defaultValue()) {\n" +
            "    x\n" +
            "  }\n" +
            "  private defaultValue() {\n" +
            "    'x'\n" +
            "  }\n" +
            "}\n" +
            "class D {\n" +
            "  @Delegate private C c = new C()\n" +
            "}\n" +
            "print new D().m()\n" +
            "print new D().m('y')\n",
        };
        //@formatter:on

        runConformTest(sources, "xy");
    }

    /**
     * Test code based on an <a href="http://www.infoq.com/articles/groovy-1.5-new">article</a>.
     * The groups of tests are loosely based on its contents, but what is really exercised
     * here is the accessibility of the described constructs across the Java/Groovy divide.
     */
    @Test
    public void testDelegate_1731() {
        //@formatter:off
        String[] sources = {
            "c/Main.java",
            "package c;\n" +
            "import java.lang.reflect.Method;\n" +
            "import a.SampleAnnotation;\n" +
            "import b.Sample;\n" +
            "public class Main {\n" +
            "  public static void main(String[] args) throws Exception {" +
            "    Method method = Sample.class.getMethod(\"doSomething\");\n" +
            "    SampleAnnotation annotation = method.getAnnotation(SampleAnnotation.class);\n" +
            "    System.out.print(annotation);\n" +
            "  }\n" +
            "}\n",

            "a/SampleAnnotation.java",
            "package a;\n" +
            "import java.lang.annotation.ElementType;\n" +
            "import java.lang.annotation.Retention;\n" +
            "import java.lang.annotation.RetentionPolicy;\n" +
            "import java.lang.annotation.Target;\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target({ElementType.METHOD})\n" +
            "public @interface SampleAnnotation {}\n",

            "a/DelegateInOtherProject.java",
            "package a;\n" +
            "public class DelegateInOtherProject {\n" +
            "  @SampleAnnotation\n" +
            "  public void doSomething() {}\n" +
            "}\n",

            "b/Sample.groovy",
            "package b\n" +
            "import a.DelegateInOtherProject;\n" +
            "class Sample {\n" +
            "  @Delegate(methodAnnotations=true)\n" +
            "  DelegateInOtherProject delegate\n" +
            "}\n",

            "b/Delegated.groovy",
            "package b\n" +
            "import a.SampleAnnotation;\n" +
            "class Delegated {\n" +
            "  @SampleAnnotation\n" +
            "  def something() {}\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "@a.SampleAnnotation()");
    }
}
