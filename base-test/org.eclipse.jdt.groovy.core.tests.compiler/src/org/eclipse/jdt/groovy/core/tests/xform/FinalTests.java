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

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.lang.reflect.Modifier;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.Final}.
 */
public final class FinalTests extends GroovyCompilerTestSuite {

    @Before
    public void setUp() {
        assumeTrue(isAtLeastGroovy(40));
    }

    @Test
    public void testFinal1() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "@groovy.transform.Final\n" +
            "class C {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        TypeDeclaration type = getCUDeclFor("C.groovy").types[0];
        assertTrue("Expected final but was: " + Modifier.toString(type.modifiers), Modifier.isFinal(type.modifiers));
    }

    @Test
    public void testFinal2() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "@groovy.transform.Final(enabled=true)\n" +
            "class C {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        TypeDeclaration type = getCUDeclFor("C.groovy").types[0];
        assertTrue("Expected final but was: " + Modifier.toString(type.modifiers), Modifier.isFinal(type.modifiers));
    }

    @Test
    public void testFinal3() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "@groovy.transform.Final(enabled=false)\n" +
            "class C {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        TypeDeclaration type = getCUDeclFor("C.groovy").types[0];
        assertFalse("Expected non-final but was: " + Modifier.toString(type.modifiers), Modifier.isFinal(type.modifiers));
    }

    @Test
    public void testFinal4() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  @groovy.transform.Final\n" +
            "  class D {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        TypeDeclaration type = getCUDeclFor("C.groovy").types[0].memberTypes[0];
        assertTrue("Expected final but was: " + Modifier.toString(type.modifiers), Modifier.isFinal(type.modifiers));
    }

    @Test
    public void testFinal5() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  @groovy.transform.Final(enabled=true)\n" +
            "  class D {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        TypeDeclaration type = getCUDeclFor("C.groovy").types[0].memberTypes[0];
        assertTrue("Expected final but was: " + Modifier.toString(type.modifiers), Modifier.isFinal(type.modifiers));
    }

    @Test
    public void testFinal6() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  @groovy.transform.Final(enabled=false)\n" +
            "  class D {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        TypeDeclaration type = getCUDeclFor("C.groovy").types[0].memberTypes[0];
        assertFalse("Expected non-final but was: " + Modifier.toString(type.modifiers), Modifier.isFinal(type.modifiers));
    }

    @Test
    public void testFinal7() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  @groovy.transform.Final\n" +
            "  Object field\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        FieldDeclaration field = findField(getCUDeclFor("C.groovy"), "field");
        assertTrue("Expected final but was: " + Modifier.toString(field.modifiers), Modifier.isFinal(field.modifiers));
    }

    @Test
    public void testFinal8() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  @groovy.transform.Final(enabled=true)\n" +
            "  Object field\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        FieldDeclaration field = findField(getCUDeclFor("C.groovy"), "field");
        assertTrue("Expected final but was: " + Modifier.toString(field.modifiers), Modifier.isFinal(field.modifiers));
    }

    @Test
    public void testFinal9() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  @groovy.transform.Final(enabled=false)\n" +
            "  Object field\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        FieldDeclaration field = findField(getCUDeclFor("C.groovy"), "field");
        assertFalse("Expected non-final but was: " + Modifier.toString(field.modifiers), Modifier.isFinal(field.modifiers));
    }

    @Test
    public void testFinal10() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  @groovy.transform.Final\n" +
            "  def method() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        MethodDeclaration method = findMethod(getCUDeclFor("C.groovy"), "method");
        assertTrue("Expected final but was: " + Modifier.toString(method.modifiers), Modifier.isFinal(method.modifiers));
    }

    @Test
    public void testFinal11() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  @groovy.transform.Final(enabled=true)\n" +
            "  def method() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        MethodDeclaration method = findMethod(getCUDeclFor("C.groovy"), "method");
        assertTrue("Expected final but was: " + Modifier.toString(method.modifiers), Modifier.isFinal(method.modifiers));
    }

    @Test
    public void testFinal12() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  @groovy.transform.Final(enabled=false)\n" +
            "  def method() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        MethodDeclaration method = findMethod(getCUDeclFor("C.groovy"), "method");
        assertFalse("Expected non-final but was: " + Modifier.toString(method.modifiers), Modifier.isFinal(method.modifiers));
    }

    @Ignore @Test // GROOVY-11860
    public void testFinal13() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  @groovy.transform.Final\n" +
            "  C() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "ctor cannot be final");

        var constructor = (ConstructorDeclaration) getCUDeclFor("C.groovy").types[0].methods[0];
        assertFalse("Expected non-final but was: " + Modifier.toString(constructor.modifiers), Modifier.isFinal(constructor.modifiers));
    }

    @Ignore @Test // GROOVY-11860
    public void testFinal14() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  @groovy.transform.Final(enabled=true)\n" +
            "  C() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "ctor cannot be final");

        var constructor = (ConstructorDeclaration) getCUDeclFor("C.groovy").types[0].methods[0];
        assertFalse("Expected non-final but was: " + Modifier.toString(constructor.modifiers), Modifier.isFinal(constructor.modifiers));
    }

    @Test
    public void testFinal15() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  @groovy.transform.Final(enabled=false)\n" +
            "  C() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        var constructor = (ConstructorDeclaration) getCUDeclFor("C.groovy").types[0].methods[0];
        assertFalse("Expected non-final but was: " + Modifier.toString(constructor.modifiers), Modifier.isFinal(constructor.modifiers));
    }

    @Ignore @Test
    public void testFinal16() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "@groovy.transform.AnnotationCollector\n" +
            "@groovy.transform.Final\n" +
            "@interface A {\n" +
            "}\n",

            "C.groovy",
            "@A\n" +
            "class C {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        TypeDeclaration type = getCUDeclFor("C.groovy").types[0];
        assertTrue("Expected final but was: " + Modifier.toString(type.modifiers), Modifier.isFinal(type.modifiers));
    }

    @Ignore @Test
    public void testFinal17() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "@groovy.transform.AnnotationCollector(groovy.transform.Final)\n" +
            "@interface A {\n" +
            "}\n",

            "C.groovy",
            "@A\n" +
            "class C {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        TypeDeclaration type = getCUDeclFor("C.groovy").types[0];
        assertTrue("Expected final but was: " + Modifier.toString(type.modifiers), Modifier.isFinal(type.modifiers));
    }
}
