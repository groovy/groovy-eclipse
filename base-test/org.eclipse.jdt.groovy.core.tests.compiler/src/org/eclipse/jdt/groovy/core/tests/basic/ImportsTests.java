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
package org.eclipse.jdt.groovy.core.tests.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.AliasImportReference;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.junit.Test;

public final class ImportsTests extends GroovyCompilerTestSuite {

    @Test
    public void testImports() {
        //@formatter:off
        String[] sources = {
            "p/First.groovy",
            "package p\n" +
            "import java.util.regex.Pattern\n" +
            "class First {\n" +
            "  static void main(String[] argv) {\n" +
            "    Pattern p = Pattern.compile('.')\n" +
            "    print 'success'\n" +
            "  }\n" +
            "  Pattern getPattern() {}\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testDefaultImportsJavaIO() {
        //@formatter:off
        String[] sources = {
            "p/First.groovy",
            "package p\n" +
            "import java.util.regex.Pattern\n" +
            "class First {\n" +
            "  static void main(String[] argv) {\n" +
            "    print 'success'\n" +
            "  }\n" +
            "  File getFile() {}\n" + // java.io.File should be picked up magically
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testDefaultImportsBigDecimal1() {
        //@formatter:off
        String[] sources = {
            "p/Main.java",
            "package p;\n" +
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Big().getAmount().toString());\n" + // https://github.com/groovy/groovy-eclipse/issues/268
            "  }\n" +
            "}\n",

            "p/Big.groovy",
            "package p\n" +
            "class Big {\n" +
            "  BigDecimal amount = 3.14\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "3.14");
    }

    @Test
    public void testDefaultImportsBigDecimal2() {
        //@formatter:off
        String[] sources = {
            "p/Big.groovy",
            "package p\n" +
            "class Big {\n" +
            "  static void main(String[] args) {\n" +
            "    print 'success'\n" +
            "  }\n" +
            "  BigDecimal getAmount() { return 0 }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test // this version has an import; that can make a difference...
    public void testDefaultImportsBigDecimal3() {
        //@formatter:off
        String[] sources = {
            "p/Big.groovy",
            "package p\n" +
            "import java.util.regex.Pattern\n" +
            "class Big {\n" +
            "  static void main(String[] args) {\n" +
            "    print 'success'\n" +
            "  }\n" +
            "  BigDecimal getAmount() { return 0 }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testDefaultImportsBigDecimal4() {
        //@formatter:off
        String[] sources = {
            "p/Big.groovy",
            "package p\n" +
            "class Big {\n" +
            "  static void main(String[] args) {\n" +
            "    print 'success'\n" +
            "  }\n" +
            "  private static final BigDecimal FIXED_AMOUNT = BigDecimal.TEN\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testDefaultImportsBigInteger1() {
        //@formatter:off
        String[] sources = {
            "p/Main.java",
            "package p;\n" +
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Big().getAmount().toString());\n" +
            "  }\n" +
            "}\n",

            "p/Big.groovy",
            "package p\n" +
            "class Big {\n" +
            "  BigInteger amount = 10\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "10");
    }

    @Test
    public void testDefaultImportsBigInteger2() {
        //@formatter:off
        String[] sources = {
            "p/Big.groovy",
            "package p\n" +
            "class Big {\n" +
            "  static void main(String[] args) {\n" +
            "    print 'success'\n" +
            "  }\n" +
            "  BigInteger getAmount() { return 0 }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testDefaultImportsBigInteger3() {
        //@formatter:off
        String[] sources = {
            "p/Big.groovy",
            "package p\n" +
            "class Big {\n" +
            "  static void main(String[] args) {\n" +
            "    print 'success'\n" +
            "  }\n" +
            "  private static final BigInteger FIXED_AMOUNT = BigInteger.TEN\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testStarImports1() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import foo.*\n" +
            "println new Pojo().class.name\n",

            "foo/Pojo.java",
            "package foo;\n" +
            "public class Pojo {}\n",
        };
        //@formatter:on

        runConformTest(sources, "foo.Pojo");
    }

    @Test
    public void testStarImports2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import foo.bar.*\n" +
            "println new Pojo().class.name\n",

            "foo/bar/Pojo.java",
            "package foo.bar;\n" +
            "public class Pojo {}\n",
        };
        //@formatter:on

        runConformTest(sources, "foo.bar.Pojo");
    }

    @Test
    public void testStarImports3() {
        //@formatter:off
        String[] sources = {
            "Wibble.groovy",
            "import a.b.c.*\n" +
            "class Wibble {\n" +
            "  Process process = new Process()\n" +
            "  static void main(String[] argv) {\n" +
            "    print new Wibble().process.class\n" +
            "  }\n" +
            "}\n",

            "a/b/c/Process.java",
            "package a.b.c;\n" +
            "public class Process {}\n",
        };
        //@formatter:on

        runConformTest(sources, "class a.b.c.Process");
    }

    @Test // 'import static a.B.FOO'
    public void testStaticImport1() {
        //@formatter:off
        String[] sources = {
            "b/Run.groovy",
            "package b\n" +
            "import static a.B.FOO\n" +
            "class Run { public static void main(String[]argv) { print FOO;} }\n",

            "a/B.groovy",
            "package a\n" +
            "class B { public static String FOO='abc';}\n",
        };
        //@formatter:on

        runConformTest(sources, "abc");

        ImportReference ref = getCUDeclFor("Run.groovy").imports[0];
        assertTrue(ref.isStatic());
        assertEquals("a.B.FOO", ref.toString());
        assertFalse(ref instanceof AliasImportReference);
        assertEquals("FOO", String.valueOf(ref.getSimpleName()));
    }

    @Test // 'import static a.B.*'
    public void testStaticImport2() {
        //@formatter:off
        String[] sources = {
            "b/Run.groovy",
            "package b\n" +
            "import static a.B.*\n" +
            "class Run { public static void main(String[]argv) { print FOO;} }\n",

            "a/B.groovy",
            "package a\n" +
            "class B { public static String FOO='abc';}\n",
        };
        //@formatter:on

        runConformTest(sources, "abc");

        ImportReference ref = getCUDeclFor("Run.groovy").imports[0];
        assertTrue(ref.isStatic());
        assertEquals("a.B.*", ref.toString());
    }

    @Test // 'import static a.B.FOO as Wibble'
    public void testStaticImport3() {
        //@formatter:off
        String[] sources = {
            "b/Run.groovy",
            "package b\n" +
            "import static a.B.FOO as Wibble\n" +
            "class Run { public static void main(String[]argv) { print Wibble;} }\n",

            "a/B.groovy",
            "package a\n" +
            "class B { public static String FOO='abc';}\n",
        };
        //@formatter:on

        runConformTest(sources, "abc");

        ImportReference ref = getCUDeclFor("Run.groovy").imports[0];
        assertTrue(ref.isStatic());
        assertTrue(ref instanceof AliasImportReference);
        assertEquals("a.B.FOO as Wibble", ref.toString());
        assertEquals("Wibble", String.valueOf(ref.getSimpleName()));
    }

    @Test
    public void testStaticImport4() {
        //@formatter:off
        String[] sources = {
            "a/B.groovy",
            "package a\n" +
            "interface B {\n" +
            "  String C = 'nls'\n" +
            "}",

            "x/Y.groovy",
            "package x\n" +
            "import static a.B.C\n" +
            "class Y {\n" +
            "  @SuppressWarnings(C) def one() {}\n" +
            "  @SuppressWarnings(C) def two() {}\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testStaticImports_JtoG() {
        //@formatter:off
        String[] sources = {
            "p/Run.java",
            "package p;\n" +
            "import static p.q.r.Colour.*;\n" +
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.print(Red);\n" +
            "    System.out.print(Green);\n" +
            "    System.out.print(Blue);\n" +
            "  }\n" +
            "}\n",

            "p/q/r/Colour.groovy",
            "package p.q.r;\n" +
            "enum Colour { Red,Green,Blue; }\n",
        };
        //@formatter:on

        runConformTest(sources, "RedGreenBlue");
    }

    @Test
    public void testStaticImports_GtoJ() {
        //@formatter:off
        String[] sources = {
            "p/Run.groovy",
            "package p;\n" +
            "import static p.q.r.Colour.*;\n" +
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.print(Red);\n" +
            "    System.out.print(Green);\n" +
            "    System.out.print(Blue);\n" +
            "  }\n" +
            "}\n",

            "p/q/r/Colour.java",
            "package p.q.r;\n" +
            "enum Colour { Red,Green,Blue; }\n",
        };
        //@formatter:on

        runConformTest(sources, "RedGreenBlue");
    }

    @Test
    public void testStaticImports2_GtoJ() {
        //@formatter:off
        String[] sources = {
            "p/Run.java",
            "package p;\n" +
            "import static p.q.r.Colour.*;\n" +
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n" +
            "    Red.printme();\n" +
            "  }\n" +
            "}\n",

            "p/q/r/Colour.groovy",
            "package p.q.r;\n" +
            "enum Colour { Red,Green,Blue; \n" +
            "  void printme() {\n" +
            "    println \"${name()}\";\n" +
             "  }\n" +
             "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Red");
    }

    @Test
    public void testImportAliasing() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "import q.A as AA;\n" +
            "import r.A as AB;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    callitOne(new AA());\n" +
            "    callitTwo(new AB());\n" +
            "  }\n" +
            "  public static void callitOne(AA a) { a.run();}\n" +
            "  public static void callitTwo(AB a) { a.run();}\n" +
            "}\n",

            "q/A.java",
            "package q;\n" +
            "public class A {\n" +
            "  public static void run() { System.out.print(\"q.A.run \");}\n" +
            "}\n",

            "r/A.java",
            "package r;\n" +
            "public class A {\n" +
            "  public static void run() { System.out.print(\"r.A.run\");}\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "q.A.run r.A.run");
    }

    // Test that the alias is recognized when referenced as superclass
    // WMTW: the code Scope.getShortNameFor()
    @Test
    public void testImportAliasingGoober() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "import java.util.HashMap as Goober;\n" +
            "public class C extends Goober {\n" +
            "  public static void main(String[] argv) {\n" +
            "    print 'q.A.run'\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "q.A.run");
    }

    @Test
    public void testImportAliasingStatic() {
        //@formatter:off
        String[] sources = {
            "p/Run.groovy",
            "package p\n" +
            "import static java.lang.Math.PI as pi\n" +
            "import static java.lang.Math.sin as sine\n" +
            "import static java.lang.Math.cos as cosine\n" +
            "\n" +
            "print sine(pi / 6) + cosine(pi / 3)\n",
        };
        //@formatter:on

        runConformTest(sources, "1.0");
    }

    @Test
    public void testImportAliasingAndOldReference() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "import q.A as AA;\n" +
            "import r.A as AB;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    callitOne(new AA());\n" +
            "  }\n" +
            "  public static void callitOne(A a) {}\n" + // no A imported!
            "}\n",

            "q/A.java",
            "package q;\n" +
            "public class A {\n" +
            "  public static void run() { System.out.print(\"q.A.run \");}\n" +
            "}\n",

            "r/A.java",
            "package r;\n" +
            "public class A {\n" +
            "  public static void run() { System.out.print(\"r.A.run\");}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in p\\C.groovy (at line 8)\n" +
            "\tpublic static void callitOne(A a) {}\n" +
            "\t                             ^\n" +
            "Groovy:unable to resolve class A\n" +
            "----------\n");
    }

    @Test
    public void testAliasing_GRE473() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "import java.util.regex.Pattern as JavaPattern\n" +
            "class Pattern {JavaPattern javaPattern}\n" +
            "def p = new Pattern(javaPattern:~/\\d+/)\n" +
            "assert \"123\" ==~ p.javaPattern\n" +
            "print 'success '\n" +
            "print '['+p.class.name+']['+JavaPattern.class.name+']'\n",
        };
        //@formatter:on

        runConformTest(sources, "success [Pattern][java.util.regex.Pattern]");
    }

    @Test
    public void testAliasing_GRE473_2() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "import java.util.regex.Pattern\n" +
            "class Pattern {Pattern javaPattern}\n" +
            "def p = new Pattern(javaPattern:~/\\d+/)\n" +
            "assert \"123\" ==~ p.javaPattern\n" +
            "print 'success'\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in Foo.groovy (at line 1)\n" +
            "\timport java.util.regex.Pattern\n" +
            "\t       ^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "The import java.util.regex.Pattern conflicts with a type defined in the same file\n" +
            "----------\n");
    }

    @Test
    public void testImportInnerInner01() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "public class C {\n" +
            "  private static Wibble.Inner.Inner2 wibbleInner = new G();\n" +
            "  public static void main(String[] argv) {\n" +
            "    wibbleInner.run();\n" +
            "  }\n" +
            "}\n" +
            "class G extends Wibble.Inner.Inner2  {}",

            "p/Wibble.java",
            "package p;\n" +
            "public class Wibble {\n" +
            "  public static class Inner {\n" +
            "    public static class Inner2 {\n" +
            "      public static void run() { System.out.print(\"p.Wibble.Inner.Inner2.run \");}\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "p.Wibble.Inner.Inner2.run");
    }

    @Test
    public void testImportInnerClass01_JavaCase() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "import x.y.z.Wibble.Inner;\n" +
            "\n" +
            "public class C {\n" +
            "  private static Inner wibbleInner = new Inner();\n" +
            "  public static void main(String[] argv) {\n" +
            "    wibbleInner.run();\n" +
            "  }\n" +
            "}\n",

            "x/y/z/Wibble.java",
            "package x.y.z;\n" +
            "public class Wibble {\n" +
            "  public static class Inner {\n" +
            "    public static void run() { System.out.print(\"q.A.run \");}\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "q.A.run");
    }

    // FIXASC need to look at all other kinds of import - statics/double nested static classes/etc
    @Test
    public void testImportInnerClass01_GroovyCase() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "import x.y.z.Wibble.Inner\n" +
            "\n" +
            "public class C {\n" +
            "  private static Inner wibbleInner = new Inner();\n" +
            "  public static void main(String[] argv) {\n" +
            "    wibbleInner.run();\n" +
            "  }\n" +
            "}\n",

            "x/y/z/Wibble.java",
            "package x.y.z;\n" +
            "public class Wibble {\n" +
            "  public static class Inner {\n" +
            "    public static void run() { System.out.print(\"q.A.run \");}\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "q.A.run");
    }

    @Test
    public void testImportInnerClass() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "import x.y.z.Wibble.Inner /*as WibbleInner*/;\n" +
            "public class C {\n" +
            "  private static Inner wibbleInner = new Inner();\n" +
            "  public static void main(String[] argv) {\n" +
            "    wibbleInner.run();\n" +
            "  }\n" +
            //"  public static void callitOne(WibbleInner a) { a.run();}\n" +
            "}\n",

            "x/y/z/Wibble.java",
            "package x.y.z;\n" +
            "public class Wibble {\n" +
            "  public static class Inner {\n" +
            "    public void run() { System.out.print(\"run\");}\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "run");
    }

    @Test
    public void testImportAliasingInnerClass() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "import x.y.z.Wibble.Inner as WibbleInner;\n" +
            "public class C {\n" +
            "  private static WibbleInner wibbleInner = new WibbleInner();\n" +
            "  public static void main(String[] argv) {\n" +
            "   wibbleInner.run();\n" +
            "  }\n" +
            "}\n",

            "x/y/z/Wibble.java",
            "package x.y.z;\n" +
            "public class Wibble {\n" +
            "  public static class Inner {\n" +
            "    public void run() { System.out.print(\"run \");}\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "run");
    }

    @Test
    public void testExtraImports1() {
        Map<String, String> options = getCompilerOptions();
        // use the pre-2.1 verbose syntax for one test case to ensure it works as a fallback
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "import org.codehaus.groovy.control.customizers.ImportCustomizer\n" +
            "def ic = new ImportCustomizer()\n" +
            "ic.addStarImports 'com.foo'\n" +
            "configuration.addCompilationCustomizers(ic)\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" +
            "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "done", options);
    }

    @Test
    public void testExtraImports2() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    star 'com.foo'\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" +
            "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "done", options);
    }

    @Test
    public void testExtraImports3() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    normal 'com.foo.Type'\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" +
            "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "done", options);
    }

    @Test
    public void testExtraImports4() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    normal 'com.foo.Type'\n" +
            "    normal 'com.foo.Type2'\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    Type2.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" +
            "class Type {\n" +
                "  public static void m() {}\n" +
            "}\n",

            "com/foo/Type2.groovy",
            "package com.foo\n" +
            "class Type2 {\n" +
            "  public static void m() {}\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "done", options);
    }

    @Test
    public void testExtraImports_extensionFilter1() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  source(extension: 'groovy') {\n" +
            "    imports {\n" +
            "      star 'com.foo'\n" +
            "    }\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" + "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "done", options);
    }

    @Test
    public void testExtraImports_extensionFilter2() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  source(extension: 'groovy') {\n" +
            "    imports {\n" +
            "      normal 'com.foo.Type'\n" +
            "    }\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "       print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" + "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "done", options);
    }

    @Test
    public void testExtraImports_extensionFilter3() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  source(extension: 'groovy') {\n" +
            "    imports {\n" +
            "      normal 'com.foo.Type'\n" +
            "      normal 'com.foo.TypeB'\n" +
            "    }\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    TypeB.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" + "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",

            "com/foo/TypeB.groovy",
            "package com.foo\n" +
            "class TypeB {\n" +
            "  public static void m() {}\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "done", options);
    }

    @Test
    public void testExtraImports_nonMatchingSuffix() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  source(extension: 'gradle') {\n" +
            "    imports {\n" +
            "      normal 'com.foo.Type'\n" +
            "    }\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" + "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in com\\bar\\Runner.groovy (at line 4)\n" +
            "\tType.m()\n" +
            "\t^^^^\n" +
            "Groovy:Apparent variable 'Type' was found in a static scope but doesn't refer to a local variable, static field or class. Possible causes:\n" +
            "----------\n",
            options);
    }

    @Test
    public void testExtraImports_typeDoesNotExist() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    normal 'com.foo.Type2'\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" +
            "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in com\\bar\\Runner.groovy (at line 1)\n" +
            "\tpackage com.bar\n" +
            "\t^\n" +
            "Groovy:unable to resolve class com.foo.Type2\n" +
            "----------\n" +
            "2. ERROR in com\\bar\\Runner.groovy (at line 4)\n" +
            "\tType.m()\n" +
            "\t^^^^\n" +
            "Groovy:Apparent variable \'Type\' was found in a static scope but doesn\'t refer to a local variable, static field or class. Possible causes:\n" +
            "----------\n" +
            "----------\n" +
            "1. ERROR in com\\foo\\Type.groovy (at line 1)\n" +
            "\tpackage com.foo\n" +
            "\t^\n" +
            "Groovy:unable to resolve class com.foo.Type2\n" +
            "----------\n",
            options);
    }

    @Test
    public void testExtraImports_packageDoesNotExist() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    star 'com.whatever'\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" +
            "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in com\\bar\\Runner.groovy (at line 4)\n" +
            "\tType.m()\n" +
            "\t^^^^\n" +
            "Groovy:Apparent variable \'Type\' was found in a static scope but doesn\'t refer to a local variable, static field or class. Possible causes:\n" +
            "----------\n",
            options);
    }

    @Test
    public void testExtraImports_mixedAdditions() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    star 'com.whatever'\n" +
            "  }\n" +
            "  source(extension: 'groovy') {\n" +
            "    imports {\n" +
            "      normal 'com.foo.Type'\n" +
            "    }\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" +
            "class Type {\n" +
                "  public static void m() {}\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "done", options);
    }

    @Test
    public void testNonTerminalMissingImport() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "import a.b.c.D;\n"+
            "public class X {\n" +
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\timport a.b.c.D;\n" +
            "\t       ^^^^^^^\n" +
            "Groovy:unable to resolve class a.b.c.D\n" +
            "----------\n");
    }

    @Test
    public void testAmbiguous_GRE945_gu() {
        //@formatter:off
        String[] sources = {
            "Code.groovy",
            "import bug.factory.*\n" +
            "class Code {\n" +
            "  static Factory fact = new Factory()\n" +
            "  public static void main(String[]argv) {\n" +
            "    fact.foo()\n" +
            "  }\n" +
            "}\n",

            "Factory.groovy",
            "package bug.factory\n" +
            "class Factory { static foo() { print 'abc'}}\n",
        };
        //@formatter:on

        runConformTest(sources, "abc");
    }

    @Test
    public void testAmbiguous_GRE945_jl() {
        //@formatter:off
        String[] sources = {
            "Code.groovy",
            "import bug.factory.*\n" +
            "class Code {\n" +
            "  static StringBuffer fact = new StringBuffer()\n" +
            "  public static void main(String[]argv) {\n" +
            "    print fact.foo()\n" +
            "  }\n" +
            "}\n",

            "StringBuffer.groovy",
            "package bug.factory\n" +
            "class StringBuffer { static String foo() { return 'abc'}}\n",
        };
        //@formatter:on

        runConformTest(sources, "abc");
    }

    @Test
    public void testAmbiguous_GRE945_bothFromSource() {
        //@formatter:off
        String[] sources = {
            "Code.groovy",
            "import a.*\n" +
            "import b.*\n" +
            "class Code {\n" +
            "  static A fact = new A()\n" +
            "  public static void main(String[]argv) {\n" +
            "    print fact.foo()\n" +
            "  }\n" +
            "}\n",

            "a/A.groovy",
            "package a\n" +
            "class A { static String foo() { return 'abc'}}\n",

            "b/A.groovy",
            "package b\n" +
            "class A { static String foo() { return 'def'}}\n",
        };
        //@formatter:on

        runConformTest(sources, "abc");
    }

    @Test
    public void testAmbiguous_GRE945_bothFromSource_2() {
        //@formatter:off
        String[] sources = {
            "Code.groovy",
            "import b.*\n" +
            "import a.*\n" +
            "class Code {\n" +
            "  static A fact = new A()\n" +
            "  public static void main(String[]argv) {\n" +
            "    print fact.foo()\n" +
            "  }\n" +
            "}\n",

            "a/A.groovy",
            "package a\n" +
            "class A { static String foo() { return 'abc'}}\n",

            "b/A.groovy",
            "package b\n" +
            "class A { static String foo() { return 'def'}}\n",
        };
        //@formatter:on

        runConformTest(sources, "def");
    }

    @Test
    public void testAmbiguous_GRE945_bothFromSource_3() {
        //@formatter:off
        String[] sources = {
            "Code.groovy",
            "import b.*\n" +
            "import a.*\n" +
            "class Code {\n" +
            "  static Process fact = new Process()\n" +
            "  public static void main(String[]argv) {\n" +
            "    print fact.foo()\n" +
            "  }\n" +
            "}\n",

            "a/Process.groovy",
            "package a\n" +
            "class Process { static String foo() { return 'abc'}}\n",

            "b/Process.groovy",
            "package b\n" +
            "class Process { static String foo() { return 'def'}}\n",
        };
        //@formatter:on

        runConformTest(sources, "def");
    }

    @Test
    public void testAmbiguous_GRE945_ju() {
        //@formatter:off
        String[] sources = {
            "Code.groovy",
            "import bug.factory.*\n" +
            "class Code {\n" +
            "  static List fact = new List()\n" +
            "  public static void main(String[]argv) {\n" +
            "    fact.foo()\n" +
            "  }\n" +
            "}\n",

            "List.groovy",
            "package bug.factory\n" +
            "class List { static foo() { print 'abc'}}\n",
        };
        //@formatter:on

        runConformTest(sources, "abc");
    }

    @Test
    public void testAmbiguous_GRE945_jn() {
        //@formatter:off
        String[] sources = {
            "Code.groovy",
            "import bug.factory.*\n" +
            "class Code {\n" +
            "  static Socket fact = new Socket()\n" +
            "  public static void main(String[]argv) {\n" +
            "    fact.foo()\n" +
            "  }\n" +
            "}\n",

            "Socket.groovy",
            "package bug.factory\n" +
            "class Socket { static foo() { print 'abc'}}\n",
        };
        //@formatter:on

        runConformTest(sources, "abc");
    }

    @Test
    public void testAmbiguous_GRE945_gl() {
        //@formatter:off
        String[] sources = {
            "Code.groovy",
            "import bug.factory.*\n" +
            "class Code {\n" +
            "  static Tuple fact = new Tuple()\n" +
            "  public static void main(String[]argv) {\n" +
            "    fact.foo()\n" +
            "  }\n" +
            "}\n",

            "Tuple.groovy",
            "package bug.factory\n" +
            "class Tuple { static foo() { print 'abc'}}\n",
        };
        //@formatter:on

        runConformTest(sources, "abc");
    }

    @Test
    public void testAmbiguous_GRE945_ji() {
        //@formatter:off
        String[] sources = {
            "Code.groovy",
            "import bug.factory.*\n" +
            "class Code {\n" +
            "  static Serializable fact = new Serializable()\n" +
            "  public static void main(String[]argv) {\n" +
            "    fact.foo()\n" +
            "  }\n" +
            "}\n",

            "Serializable.groovy",
            "package bug.factory\n" +
            "class Serializable { static foo() { print 'abc'}}\n",
        };
        //@formatter:on

        runConformTest(sources, "abc");
    }

    @Test
    public void testParsingBlankPackage() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "package \n" +
            "class Name { }\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in Foo.groovy (at line 1)\n" +
            "\tpackage \n" +
            "\t^\n" +
            "Groovy:Invalid package specification @ line 1, column 0.\n" +
            "----------\n");
    }

    @Test
    public void testParsingBlankPackage2() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "package ;\n" +
            "class Name { }\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in Foo.groovy (at line 1)\n" +
            "\tpackage ;\n" +
            "\t^\n" +
            "Groovy:Invalid package specification @ line 1, column 0.\n" +
            "----------\n");
    }

    @Test // does the second error now get reported after the package problem
    public void testParsingBlankPackage3() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "package ;\n" +
            "class Name { \n" +
            "  asdf\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in Foo.groovy (at line 1)\n" +
            "\tpackage ;\n" +
            "\t^\n" +
            "Groovy:Invalid package specification @ line 1, column 0.\n" +
            "----------\n" +
            "2. ERROR in Foo.groovy (at line 3)\n" +
            "\tasdf\n" +
            "\t^\n" +
            "Groovy:unexpected token: asdf @ line 3, column 3.\n" +
            "----------\n");
    }

    @Test
    public void testParsingBlankImport_538() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "import ",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in A.groovy (at line 1)\n" +
            "\timport \n" +
            "\t^^^^^^^\n" +
            "Groovy:unable to resolve class ?\n" +
            "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List<ImportNode> imports = mn.getImports();
        ImportNode recoveredImport = imports.get(0);
        assertEquals(0, recoveredImport.getStart());
        assertEquals(7, recoveredImport.getEnd());
        assertEquals("?", recoveredImport.getType().getName());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("A"));
    }

    @Test
    public void testParsingBlankImport2_538() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "import ;",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in A.groovy (at line 1)\n" +
            "\timport ;\n" +
            "\t^^^^^^^\n" +
            "Groovy:unable to resolve class ?\n" +
            "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List<ImportNode> imports = mn.getImports();
        ImportNode recoveredImport = imports.get(0);
        assertEquals(0, recoveredImport.getStart());
        assertEquals(7, recoveredImport.getEnd());
        assertEquals("?", recoveredImport.getType().getName());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("A"));
    }

    @Test
    public void testParsingDotTerminatedImport_538() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "import foo.",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in A.groovy (at line 1)\n" +
            "\timport foo.\n" +
            "\t       ^\n" +
            "Groovy:Invalid import @ line 1, column 8.\n" +
            "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List<ImportNode> imports = mn.getStarImports();
        ImportNode recoveredImport = imports.get(0);
        assertEquals("foo.", recoveredImport.getPackageName());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("A"));
    }

    @Test
    public void testParsingBlankImportStatic_538() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "import static \n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in A.groovy (at line 1)\n" +
            "\timport static \n" +
            "\t^^^^^^^^^^^^^^\n" +
            "Groovy:unable to resolve class ?\n" +
            "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List<ImportNode> imports = mn.getImports();
        ImportNode recoveredImport = imports.get(0);
        assertEquals(0, recoveredImport.getStart());
        assertEquals(14, recoveredImport.getEnd());
        assertEquals("?", recoveredImport.getType().getName());
        assertTrue(mn.getStaticImports().isEmpty());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("A"));
    }

    @Test
    public void testParsingBlankImportStatic2_538() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "import static ;\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in A.groovy (at line 1)\n" +
            "\timport static ;\n" +
            "\t^^^^^^^^^^^^^^\n" +
            "Groovy:unable to resolve class ?\n" +
            "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List<ImportNode> imports = mn.getImports();
        ImportNode recoveredImport = imports.get(0);
        assertEquals(0, recoveredImport.getStart());
        assertEquals(14, recoveredImport.getEnd());
        assertEquals("?", recoveredImport.getType().getName());
        assertTrue(mn.getStaticImports().isEmpty());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("A"));
    }

    @Test
    public void testParsingDotTerminatedImportStatic_538() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "import static foo.Bar.",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in A.groovy (at line 1)\n" +
            "\timport static foo.Bar.\n" +
            "\t              ^\n" +
            "Groovy:Invalid import @ line 1, column 15.\n" +
            "----------\n" +
            "2. ERROR in A.groovy (at line 1)\n" +
            "\timport static foo.Bar.\n" +
            "\t              ^^^^^^^\n" +
            "Groovy:unable to resolve class foo.Bar\n" +
            "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        Map<String, ImportNode> imports = mn.getStaticStarImports();
        ImportNode recoveredImport = imports.get("foo.Bar");
        assertEquals("foo.Bar", recoveredImport.getType().getName());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertEquals("A", cn.getName());
    }

    @Test
    public void testParsingDotTerminatedImportFollowedByClassDeclaration_538() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "import foo.\n" +
            "\n" +
            "class Wibble {}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in A.groovy (at line 1)\n" +
            "\timport foo.\n" +
            "\t       ^\n" +
            "Groovy:Invalid import @ line 1, column 8.\n" +
            "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List<ImportNode> imports = mn.getStarImports();
        ImportNode recoveredImport = imports.get(0);
        assertEquals("foo.", recoveredImport.getPackageName());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertEquals("Wibble", cn.getName());
    }

    @Test
    public void testParsingDotTerminatedImportFollowedByModifierAndClassDeclaration_538() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "import foo.\n" +
            "\n" +
            "public class Wibble {}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in A.groovy (at line 1)\n" +
            "\timport foo.\n" +
            "\t       ^\n" +
            "Groovy:Invalid import @ line 1, column 8.\n" +
            "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List<ImportNode> imports = mn.getStarImports();
        ImportNode recoveredImport = imports.get(0);
        assertEquals("foo.", recoveredImport.getPackageName());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertEquals("Wibble", cn.getName());
    }

    @Test
    public void testParsingBlankImportFollowedByClassDeclaration_538() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "import\n" +
            "\n" +
            "public class Wibble {}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in A.groovy (at line 1)\n" +
            "\timport\n" +
            "\t^^^^^^\n" +
            "Groovy:unable to resolve class ?\n" +
            "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List<ImportNode> imports = mn.getImports();
        ImportNode recoveredImport = imports.get(0);
        assertEquals("?", recoveredImport.getType().getName());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertEquals("Wibble", cn.getName());
    }
}
