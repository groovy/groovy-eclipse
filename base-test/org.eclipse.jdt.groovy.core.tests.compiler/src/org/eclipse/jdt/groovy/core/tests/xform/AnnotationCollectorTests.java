/*
 * Copyright 2009-2026 the original author or authors.
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

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
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
            "  static main(args) {\n" +
            "    print(new Type(id:'123'))\n" +
            "  }\n" +
            "}\n",

            "Alias.groovy",
            "import groovy.transform.*\n" +
            "@AnnotationCollector([EqualsAndHashCode, ToString])\n" +
            "@interface Alias { }\n",
        };
        //@formatter:on

        runConformTest(sources, "Type(123)");
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
            "  static main(args) {\n" +
            "    print(new Type(id:'123'))\n" +
            "  }\n" +
            "}\n",

            "Alias.groovy",
            "import groovy.transform.*\n" +
            "@AnnotationCollector\n" +
            "@EqualsAndHashCode\n" +
            "@ToString\n" +
            "@interface Alias { }\n",
        };
        //@formatter:on

        runConformTest(sources, "Type(123)");
    }

    @Test
    public void testAnnotationCollector3() {
        //@formatter:off
        String[] sources = {
            "Type.groovy",
            "@Alias(includes='id')\n" +
            "class Type {\n" +
            "  String id\n" +
            "  String hidden = '456'\n" +
            "  @Override\n" +
            "  String toString() {\n" +
            "    \"Type($id)\"\n" +
            "  }\n" +
            "  \n" +
            "  static main(args) {\n" +
            "    print(new Type('123'))\n" +
            "  }\n" +
            "}\n",

            "Alias.groovy",
            "import groovy.transform.*\n" +
            "@AnnotationCollector\n" +
            "@TupleConstructor(defaults=false)\n" +
            "@interface Alias { }\n",
        };
        //@formatter:on

        runConformTest(sources, "Type(123)");
    }

    @Test // GROOVY-10121
    public void testAnnotationCollector4() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static main(args) {\n" +
            "    print(Alias.classes*.name)\n" +
            "  }\n" +
            "}\n",

            "Alias.groovy",
            "import groovy.transform.*\n" +
            "@AnnotationCollector()\n" +
            "@interface Alias { }\n",
        };
        //@formatter:on

        runConformTest(sources, "[Alias$CollectorHelper]");
    }

    @Test
    public void testAnnotationCollector5() {
        //@formatter:off
        String[] sources = {
            "Book.groovy",
            "import java.lang.reflect.*\n" +
            "class Book {\n" +
            "  @ISBN String isbn\n" +
            "  static void main(args) {\n" +
            "    Field f = this.getDeclaredField('isbn')\n" +
            "    for (a in f.getDeclaredAnnotations()) {\n" +
            "      print(a)\n" +
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

        runConformTest(sources, Runtime.version().feature() > 13 ? "@NotNull()@Length(0)" : "@NotNull()@Length(value=0)");
    }

    @Test
    public void testAnnotationCollector6() throws Exception {
        var bundleEntry = Platform.getBundle("org.eclipse.jdt.groovy.core.tests.compiler").getEntry("astTransformations/transforms237.jar");
        cpAdditions = new String[] {FileLocator.toFileURL(bundleEntry).getPath()};

        //@formatter:off
        String[] sources = {
            "Pogo.groovy",
            "@pack.Meta\n" + // prior to Groovy 2.5.3, serialized annotation data came from "public static Object[][] value()" of annotation
            "class Pogo {\n" +
            "  String foo, bar\n" +
            "  static void main(args) {\n" +
            "    print(new Pogo(foo:'foo', bar:'bar'))\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Pogo(foo, bar)");
    }

    @Test // GROOVY-10570: emulate transform
    public void testAnnotationCollector7() {
        //@formatter:off
        runNegativeTest(new String[] {
            "pack/Meta.java",
            "package pack;\n" +
            "import java.lang.annotation.*;\n" +
            "@Retention(RetentionPolicy.SOURCE)\n" +
            "@groovy.transform.AnnotationCollector(serializeClass = Meta.CollectorHelper.class)\n" +
            "public @interface Meta {\n" +
            "  class CollectorHelper {\n" +
            "    public static Object[][] value() {\n" +
            "      return new Object[][] {\n" +
            "        {groovy.transform.ToString.class, java.util.Map.of(\"excludes\", \"foo\")}\n" +
            "      };\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        }, "");

        String[] sources = {
            "Pogo.groovy",
            "@pack.Meta\n" +
            "class Pogo {\n" +
            "  String foo, bar\n" +
            "  static void main(args) {\n" +
            "    print(new Pogo(foo:'foo', bar:'bar'))\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        //runConformTest(sources, "Pogo(bar)"); // with shouldFlushOutputDirectory:false
        org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest testDriver =
            org.eclipse.jdt.groovy.core.util.ReflectionUtils.getPrivateField(GroovyCompilerTestSuite.class, "testDriver", this);
        testDriver.runTest(
            sources,
            false, // expectingCompilerErrors
            null,  // expectedCompilerLog
            "Pogo(bar)",
            null,  // expectedStderr
            false, // forceExecution
            null,  // classLibraries
            false, // shouldFlushOutputDirectory
            vmArguments,
            null,  // customOptions
            null,  // customRequestor
            false  // skipJavac
        );
    }
}
