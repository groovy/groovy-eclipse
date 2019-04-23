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

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Modifier;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.PackageScope}.
 */
public final class PackageScopeTests extends GroovyCompilerTestSuite {

    private static boolean isPackagePrivate(int modifiers) {
        return !(Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers) || Modifier.isPrivate(modifiers));
    }

    @Test
    public void testPackageScope1() {
        //@formatter:off
        String[] sources = {
            "Goo.groovy",
            "class Goo {\n" +
            "  public static void main(String[] argv) {\n" +
            "    q.Run.main(argv);\n" +
            "  }\n" +
            "}\n",

            "q/Run.groovy",
            "package q;\n" +
            "import q.Wibble;\n" +
            "public class Run {\n" +
            "  public static void main(String[] argv) throws Exception {\n" +
            "    Wibble w = new Wibble();\n" +
            "    System.out.print(Wibble.class.getDeclaredField(\"field\").getModifiers());\n" +
            "    System.out.print(Wibble.class.getDeclaredField(\"field2\").getModifiers());\n" +
            "  }\n" +
            "}\n",

            "q/Wibble.groovy",
            "package q\n" +
            "class Wibble {" +
            "  String field = 'abcd';\n" +
            "  @groovy.transform.PackageScope String field2 = 'abcd';\n" + // adjust the visibility of property
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "20"); // 0x2 = private, 0x0 = default (so field2 has had private vis removed by annotation)
    }

    @Test
    public void testPackageScope2() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.*\n" +
            "class Foo {\n" +
            "  @PackageScope Object field\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        FieldDeclaration field = findField(getCUDeclFor("Foo.groovy"), "field");
        assertTrue("Expected package-private but was: " + Modifier.toString(field.modifiers), isPackagePrivate(field.modifiers));
    }

    @Test
    public void testPackageScope3() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.*\n" +
            "@PackageScope(PackageScopeTarget.FIELDS)\n" +
            "class Foo {\n" +
            "  Object field\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        FieldDeclaration field = findField(getCUDeclFor("Foo.groovy"), "field");
        assertTrue("Expected package-private but was: " + Modifier.toString(field.modifiers), isPackagePrivate(field.modifiers));
    }

    @Test
    public void testPackageScope3a() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.*\n" +
            "import static groovy.transform.PackageScopeTarget.*\n" +
            "@PackageScope(FIELDS)\n" +
            "class Foo {\n" +
            "  Object field\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        FieldDeclaration field = findField(getCUDeclFor("Foo.groovy"), "field");
        assertTrue("Expected package-private but was: " + Modifier.toString(field.modifiers), isPackagePrivate(field.modifiers));
    }

    @Test
    public void testPackageScope3b() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.*\n" +
            "import static groovy.transform.PackageScopeTarget.FIELDS\n" +
            "@PackageScope(FIELDS)\n" +
            "class Foo {\n" +
            "  Object field\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        FieldDeclaration field = findField(getCUDeclFor("Foo.groovy"), "field");
        assertTrue("Expected package-private but was: " + Modifier.toString(field.modifiers), isPackagePrivate(field.modifiers));
    }

    @Test
    public void testPackageScope4() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.*\n" +
            "class Foo {\n" +
            "  @PackageScope Object method() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        MethodDeclaration method = findMethod(getCUDeclFor("Foo.groovy"), "method");
        assertTrue("Expected package-private but was: " + Modifier.toString(method.modifiers), isPackagePrivate(method.modifiers));
    }

    @Test
    public void testPackageScope5() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.*\n" +
            "@PackageScope(PackageScopeTarget.METHODS)\n" +
            "class Foo {\n" +
            "  Object method() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        MethodDeclaration method = findMethod(getCUDeclFor("Foo.groovy"), "method");
        assertTrue("Expected package-private but was: " + Modifier.toString(method.modifiers), isPackagePrivate(method.modifiers));
    }

    @Test
    public void testPackageScope5a() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.*\n" +
            "import static groovy.transform.PackageScopeTarget.*\n" +
            "@PackageScope(METHODS)\n" +
            "class Foo {\n" +
            "  Object method() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        MethodDeclaration method = findMethod(getCUDeclFor("Foo.groovy"), "method");
        assertTrue("Expected package-private but was: " + Modifier.toString(method.modifiers), isPackagePrivate(method.modifiers));
    }

    @Test
    public void testPackageScope5b() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.*\n" +
            "import static groovy.transform.PackageScopeTarget.METHODS\n" +
            "@PackageScope(METHODS)\n" +
            "class Foo {\n" +
            "  Object method() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        MethodDeclaration method = findMethod(getCUDeclFor("Foo.groovy"), "method");
        assertTrue("Expected package-private but was: " + Modifier.toString(method.modifiers), isPackagePrivate(method.modifiers));
    }

    @Test
    public void testPackageScope6() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.*\n" +
            "import static groovy.transform.PackageScopeTarget.*\n" +
            "@PackageScope([CLASS, FIELDS, METHODS])\n" +
            "class Foo {\n" +
            "  Object method() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        MethodDeclaration method = findMethod(getCUDeclFor("Foo.groovy"), "method");
        assertTrue("Expected package-private but was: " + Modifier.toString(method.modifiers), isPackagePrivate(method.modifiers));
    }

    @Test // @PackageScope only applies to synthetic public members
    public void testPackageScope7() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.PackageScope(groovy.transform.PackageScopeTarget.FIELDS)\n" +
            "class Foo {\n" +
            "  Object field1\n" +
            "  public Object field2\n" +
            "  private Object field3\n" +
            "  protected Object field4\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        FieldDeclaration field = findField(getCUDeclFor("Foo.groovy"), "field1");
        assertTrue("Expected package-private but was: " + Modifier.toString(field.modifiers), isPackagePrivate(field.modifiers));
        field = findField(getCUDeclFor("Foo.groovy"), "field2");
        assertTrue("Expected public but was: " + Modifier.toString(field.modifiers), Modifier.isPublic(field.modifiers));
        field = findField(getCUDeclFor("Foo.groovy"), "field3");
        assertTrue("Expected private but was: " + Modifier.toString(field.modifiers), Modifier.isPrivate(field.modifiers));
        field = findField(getCUDeclFor("Foo.groovy"), "field4");
        assertTrue("Expected protected but was: " + Modifier.toString(field.modifiers), Modifier.isProtected(field.modifiers));
    }

    @Test // @PackageScope only applies to synthetic public members
    public void testPackageScope8() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.PackageScope(groovy.transform.PackageScopeTarget.METHODS)\n" +
            "class Foo {\n" +
            "  Object method1() {}\n" +
            "  public Object method2() {}\n" +
            "  private Object method3() {}\n" +
            "  protected Object method4() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        MethodDeclaration method = findMethod(getCUDeclFor("Foo.groovy"), "method1");
        assertTrue("Expected package-private but was: " + Modifier.toString(method.modifiers), isPackagePrivate(method.modifiers));
        method = findMethod(getCUDeclFor("Foo.groovy"), "method2");
        assertTrue("Expected public but was: " + Modifier.toString(method.modifiers), Modifier.isPublic(method.modifiers));
        method = findMethod(getCUDeclFor("Foo.groovy"), "method3");
        assertTrue("Expected private but was: " + Modifier.toString(method.modifiers), Modifier.isPrivate(method.modifiers));
        method = findMethod(getCUDeclFor("Foo.groovy"), "method4");
        assertTrue("Expected protected but was: " + Modifier.toString(method.modifiers), Modifier.isProtected(method.modifiers));
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8940
    public void testPackageScope9() {
        //@formatter:off
        String[] sources = {
            "Tag.groovy",
            "@groovy.transform.PackageScope\n" +
            "@interface Tag {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }
}
