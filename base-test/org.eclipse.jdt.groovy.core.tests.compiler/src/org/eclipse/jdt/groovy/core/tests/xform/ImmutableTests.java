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
package org.eclipse.jdt.groovy.core.tests.xform;

import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.Immutable}, et al.
 */
public final class ImmutableTests extends GroovyCompilerTestSuite {

    @Test
    public void testImmutable1() {
        //@formatter:off
        String[] sources = {
            "c/Main.java",
            "package c;\n" +
            "public class Main {\n" +
            "  public static void main(String[] args) {" +
            "  }\n" +
            "}\n",

            "a/SomeId.groovy",
            "package a;\n" +
            "import groovy.transform.Immutable\n" +
            "@Immutable\n" +
            "class SomeId {\n" +
            "  UUID id\n" +
            "}\n",

            "b/SomeValueObject.groovy",
            "package b;\n" +
            "import groovy.transform.Immutable\n" +
            "import a.SomeId\n" +
            "@Immutable\n" +
            "class SomeValueObject {\n" +
            "  SomeId id\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);

        CompilationUnit unit = getCUDeclFor("SomeValueObject.groovy").getCompilationUnit();
        ClassNode fieldType = unit.getClassNode("b.SomeValueObject").getField("id").getType();
        Optional<AnnotationNode> anno = fieldType.getAnnotations().stream().filter(node -> {
            String name = node.getClassNode().getName();
            return name.matches("groovy.transform.(Known)?Immutable");
        }).findFirst();
        assertTrue(anno.isPresent());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/421
    public void testImmutable2() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Foo(\"one\", \"two\"));\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "@groovy.transform.Immutable\n" +
            "class Foo {\n" +
            "  String bar, baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one, two)");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/421
    public void testImmutable3() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Foo(\"one\", \"two\"));\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "@groovy.transform.Immutable(defaults=false)\n" +
            "class Foo {\n" +
            "  String bar, baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one, two)");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/421
    public void testImmutable4() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Foo(\"one\"));\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "@groovy.transform.Immutable(defaults=true,noArg=false)\n" + //TODO: GROOVY-10790
            "class Foo {\n" +
            "  String bar, baz = \"two\"\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo(one, two)");
    }
}
