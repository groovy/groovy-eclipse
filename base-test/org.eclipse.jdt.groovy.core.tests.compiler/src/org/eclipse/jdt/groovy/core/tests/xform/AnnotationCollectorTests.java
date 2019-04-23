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
package org.eclipse.jdt.groovy.core.tests.xform;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.AnnotationCollector}.
 */
public final class AnnotationCollectorTests extends GroovyCompilerTestSuite {

    @Test
    public void testAnnotationCollector1() {
        //@formatter:off
        String[] sources = {
            "Type.groovy",
            "@Alias(includes='id')\n" +
            "class Type {\n" +
            "  String id\n" +
            "  String hidden = '456'\n" +
            "  \n" +
            "  static void main(String[] args) {\n" +
            "    print(new Type(id:'123'))\n" +
            "  }\n" +
            "}",

            "Alias.groovy",
            "import groovy.transform.*\n" +
            "@AnnotationCollector\n" +
            "@EqualsAndHashCode\n" +
            "@ToString\n" +
            "@interface Alias { }",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testAnnotationCollector2() {
        //@formatter:off
        String[] sources = {
            "Type.groovy",
            "@Alias(includes='id')\n" +
            "class Type {\n" +
            "  String id\n" +
            "  String hidden = '456'\n" +
            "  \n" +
            "  static void main(String[] args) {\n" +
            "    print(new Type(id:'123'))\n" +
            "  }\n" +
            "}",

            "Alias.groovy",
            "import groovy.transform.*\n" +
            "@AnnotationCollector([EqualsAndHashCode, ToString])\n" +
            "@interface Alias { }",
        };
        //@formatter:on

        runConformTest(sources, "Type(123)");
    }

    @Test
    public void testAnnotationCollector3() {
        //@formatter:off
        String[] sources = {
            "Book.groovy",
            "import java.lang.reflect.*\n" +
            "class Book {\n" +
            "  @ISBN String isbn\n" +
            "  public static void main(String[] argv) {\n" +
            "    Field f = Book.class.getDeclaredField('isbn')\n" +
            "    Object[] os = f.getDeclaredAnnotations()\n" +
            "    for (Object o: os) {\n" +
            "      println(o)\n" +
            "    }\n" +
            "  }\n" +
            "}\n",

            "NotNull.java",
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME) public @interface NotNull {\n" +
            "}\n",

            "Length.java",
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.RUNTIME) public @interface Length {\n" +
            "  int value() default 0;\n" +
            "}\n",

            "ISBN.groovy",
            "@NotNull @Length @groovy.transform.AnnotationCollector\n" +
            "public @interface ISBN {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "@NotNull()\n@Length(value=0)");
    }
}
