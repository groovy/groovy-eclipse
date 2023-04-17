/*
 * Copyright 2009-2023 the original author or authors.
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

import static org.eclipse.jdt.internal.compiler.impl.CompilerOptions.OPTIONG_GroovyCompilerConfigScript;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.junit.Test;

/**
 * Tests for {@link org.eclipse.jdt.groovy.search.ConstructorReferenceSearchRequestor}
 */
public final class ConstructorReferenceSearchTests extends SearchTestSuite {

    @Test
    public void testConstructorReferences1() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo() {\n" + // search for this
            "    new Foo()\n" + // yes
            "  }\n" +
            "  Foo(a) {\n" +
            "    new Foo(a)\n" + // no
            "  }\n" +
            "}\n");
        createUnit("Other", "import p.Foo\n" +
            "new Foo()\n" + // yes
            "new Foo(a)\n" + // no
            "new p.Foo()\n" + // yes
            "new p.Foo(a)\n"); // no

        IMethod constructor = foo.getType("Foo").getMethods()[0];
        List<SearchMatch> matches = searchForReferences(constructor);
        assertEquals("Incorrect number of matches;", 3, matches.size());

        int fooCount = 0, otherCount = 0;
        for (SearchMatch match : matches) {
            if (match.getElement() instanceof IMethod) {
                if (((IMethod) match.getElement()).getResource().getName().equals("Foo.groovy")) {
                    fooCount += 1;
                } else if (((IMethod) match.getElement()).getResource().getName().equals("Other.groovy")) {
                    otherCount += 1;
                }
            }
        }
        assertEquals("Should have found 2 matches in Foo.groovy", 1, fooCount);
        assertEquals("Should have found 4 matches in Other.groovy", 2, otherCount);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/765
    public void testConstructorReferences2() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  static class Bar {\n" +
            "    static class Baz {\n" +
            "      Baz() {\n" + // search for this
            "        this(null)\n" + // no
            "      }\n" +
            "      Baz(def arg) {\n" +
            "        super()\n" + // no
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n");
        createUnit("Other", "import p.Foo.Bar.Baz\n" +
            "new Baz()\n" + // yes
            "new Baz(a)\n"); // no

        IMethod constructor = foo.getType("Foo").getType("Bar").getType("Baz").getMethods()[0];
        List<SearchMatch> matches = searchForReferences(constructor);
        assertEquals("Incorrect number of matches;", 1, matches.size());

        int fooCount = 0, otherCount = 0;
        for (SearchMatch match : matches) {
            if (match.getElement() instanceof IMethod) {
                if (((IMethod) match.getElement()).getResource().getName().equals("Foo.groovy")) {
                    fooCount += 1;
                } else if (((IMethod) match.getElement()).getResource().getName().equals("Other.groovy")) {
                    otherCount += 1;
                }
            }
        }
        assertEquals("Should have found 0 matches in Foo.groovy", 0, fooCount);
        assertEquals("Should have found 2 matches in Other.groovy", 1, otherCount);
    }

    @Test
    public void testConstructorReferences3() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(... args) {}\n" + // search for this
            "}\n");
        createUnit("Bar", "import p.Foo\n" +
            "new Foo()\n" + // yes
            "new Foo(a)\n" + // yes
            "new Foo(a,b)\n"); // yes

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(3, ctorRefs);
    }

    @Test
    public void testConstructorReferences4() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" + // search for this
            "  Foo(String s) {}\n" +
            "}\n");
        createUnit("Bar", "import p.Foo\n" +
            "new Foo()\n" + // no
            "new Foo(0)\n" + // yes
            "new Foo('')\n"); // no

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(1, ctorRefs);
    }

    @Test
    public void testConstructorReferences5() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" +
            "  Foo(String s) {}\n" + // search for this
            "}\n");
        createUnit("Bar", "import p.Foo\n" +
            "new Foo()\n" + // no -- associated with first declaration
            "new Foo(0)\n" + // no
            "new Foo('')\n"); // yes

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[1]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(1, ctorRefs);
    }

    @Test
    public void testConstructorReferences6() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo() {}\n" + // search for this
            "  Foo(a) {}\n" +
            "}\n");
        createUnit("Bar", "import p.Foo\n" +
            "new Foo()\n" + // yes
            "new Foo(a)\n" + // no
            "new Foo(a,b)\n"); // no

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(1, ctorRefs);
    }

    @Test
    public void testConstructorReferences7() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo() {}\n" +
            "  Foo(a) {}\n" + // search for this
            "}\n");
        createUnit("Bar", "import p.Foo\n" +
            "new Foo()\n" + // no
            "new Foo(a)\n" + // yes
            "new Foo(a,b)\n"); // no -- associated with first declaration

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[1]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(1, ctorRefs);
    }

    @Test
    public void testConstructorReferences8() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" + // search for this
            "  Foo(String s) {}\n" +
            "}\n");
        createUnit("Bar", "import p.Foo\n" +
            "class Bar extends Foo {\n" +
            "  Bar() {\n" +
            "    super(0)\n" + // yes
            "  }\n" +
            "}\n");

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(1, ctorRefs);
    }

    @Test
    public void testConstructorReferences9() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" +
            "  Foo(String s) {}\n" + // search for this
            "}\n");
        createUnit("Bar", "import p.Foo\n" +
            "class Bar extends Foo {\n" +
            "  Bar() {\n" +
            "    super(0)\n" + // no
            "  }\n" +
            "}\n");

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[1]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(0, ctorRefs);
    }

    @Test // non-static inner class constructors may have implicit parameter
    public void testConstructorReferences10() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  class FooFoo {\n" +
            "    FooFoo(int i) {}\n" + // search for this
            "    FooFoo(String s) {}\n" +
            "  }\n" +
            "}\n");
        createUnit("Bar", "import p.Foo\n" +
            "def foo = new Foo()\n" +
            "new Foo.FooFoo(foo, 0)\n" + // yes
            "new Foo.FooFoo(foo, '')\n"); // no

        long ctorRefs = searchForReferences(foo.getType("Foo").getType("FooFoo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(1, ctorRefs);
    }

    @Test // default value generates a synthetic constructor
    public void testConstructorReferences11() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i=42) {}\n" + // search for this
            "  Foo(String s) {}\n" +
            "}\n");
        createUnit("Bar", "import p.Foo\n" +
            "new Foo()\n" + // no
            "new Foo(0)\n" + // yes
            "new Foo('')\n"); // no

        List<SearchMatch> ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]);

        assertEquals(1, ctorRefs.size());
        assertEquals(27, ctorRefs.get(0).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, ctorRefs.get(0).getAccuracy());
    }

    @Test // default value generates a synthetic constructor
    public void testConstructorReferences12() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i=42) {}\n" + // search for this
            "  Foo(String s) {}\n" +
            "}\n");
        createUnit("Bar", "import p.Foo\n" +
            "new Foo()\n" + // yes
            "new Foo(0)\n" + // no
            "new Foo('')\n"); // no

        List<SearchMatch> ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[1]);

        assertEquals(1, ctorRefs.size());
        assertEquals(17, ctorRefs.get(0).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, ctorRefs.get(0).getAccuracy());
    }

    @Test // same-unit references exercise patch in Verifier.addDefaultParameterConstructors
    public void testConstructorReferences13() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i=42) {}\n" + // search for this
            "  Foo(String s) {this(666)}\n" + // yes
            "  def m() {\n" +
            "    new Foo()\n" + // no
            "    new Foo(0)\n" + // yes
            "    new Foo('')\n" + // no
            "  }\n" +
            "}\n");

        List<SearchMatch> ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]);

        assertEquals(2, ctorRefs.size());
        assertEquals(58, ctorRefs.get(0).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, ctorRefs.get(0).getAccuracy());
        assertEquals(103, ctorRefs.get(1).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, ctorRefs.get(1).getAccuracy());
    }

    @Test // default value generates a synthetic constructor
    public void testConstructorReferences14() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" +
            "  Foo(String s = '') {}\n" + // search for this
            "}\n");
        createUnit("Bar", "import p.Foo\n" +
            "new Foo()\n" + // no
            "new Foo(0)\n" + // no
            "new Foo('')\n"); // yes

        List<SearchMatch> ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[1]);

        assertEquals(1, ctorRefs.size());
        assertEquals(38, ctorRefs.get(0).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, ctorRefs.get(0).getAccuracy());
    }

    @Test // default value generates a synthetic constructor
    public void testConstructorReferences15() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" +
            "  Foo(String s = '') {}\n" + // search for this
            "}\n");
        createUnit("Bar", "import p.Foo\n" +
            "new Foo()\n" + // yes
            "new Foo(0)\n" + // no
            "new Foo('')\n"); // no

        List<SearchMatch> ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[2]);

        assertEquals(1, ctorRefs.size());
        assertEquals(17, ctorRefs.get(0).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, ctorRefs.get(0).getAccuracy());
    }

    @Test // same-unit references exercise patch in Verifier.addDefaultParameterConstructors
    public void testConstructorReferences16() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {this('xxx')}\n" + // yes
            "  Foo(String s = '') {}\n" + // search for this
            "  def m() {\n" +
            "    new Foo()\n" + // no
            "    new Foo(0)\n" + // no
            "    new Foo('')\n" + // yes
            "  }\n" +
            "}\n");

        List<SearchMatch> ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[1]);

        assertEquals(2, ctorRefs.size());
        assertEquals(36, ctorRefs.get(0).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, ctorRefs.get(0).getAccuracy());
        assertEquals(122, ctorRefs.get(1).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, ctorRefs.get(1).getAccuracy());
    }

    @Test
    public void testConstructorReferences17() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" + // search for this
            "  Foo(String s) {}\n" +
            "}\n");
        createUnit("Other", "import p.Foo\n" +
            "def foo = new Foo(0) {\n" + // yes
            "}\n");

        assertEquals(1, searchForReferences(foo.getType("Foo").getMethods()[0]).stream().count());
    }

    @Test
    public void testConstructorReferences18() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" +
            "  Foo(String s) {}\n" + // search for this
            "}\n");
        createUnit("Other", "import p.Foo\n" +
            "def foo = new Foo(0) {\n" + // no
            "}\n");

        assertEquals(0, searchForReferences(foo.getType("Foo").getMethods()[1]).stream().count());
    }

    @Test
    public void testConstructorReferences19() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(String s, Map m) {}\n" +
            "  Foo(String s, ... v) {}\n" + // search for this
            "}\n");
        createUnit("Other", "def foo = new p.Foo('')\n");

        assertEquals(1, searchForReferences(foo.getType("Foo").getMethods()[1]).stream().count());
    }

    @Test
    public void testConstructorReferences20() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(String s, Map m) {}\n" +
            "  Foo(String s, ... v) {}\n" + // search for this
            "}\n");
        createUnit("Other", "def one = new p.Foo('a', 'b'), two = new p.Foo('a', 'b', 'c')\n");

        assertEquals(2, searchForReferences(foo.getType("Foo").getMethods()[1]).stream().count());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1473
    public void testConstructorReferences21() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo() {} \n" + // search for this
            "  Foo(String s) {}\n" +
            "  String bar, baz;\n" +
            "}\n");
        createUnit("Other", "def one = new p.Foo(), two = new p.Foo(bar:'')\n");

        assertEquals(2, searchForReferences(foo.getType("Foo").getMethods()[0]).stream().count());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1473
    public void testConstructorReferences22() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo() {} \n" +
            "  Foo(String s) {}\n" + // search for this
            "  String bar, baz;\n" +
            "}\n");
        createUnit("Other", "def one = new p.Foo(), two = new p.Foo(bar:'')\n");

        assertEquals(0, searchForReferences(foo.getType("Foo").getMethods()[1]).stream().count());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1473
    public void testConstructorReferences23() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo() {} \n" + // search for this
            "  Foo(Map m) {}\n" +
            "  String bar, baz\n" +
            "}\n");
        createUnit("Other", "def one = new p.Foo(), two = new p.Foo(baz:'')\n");

        assertEquals(1, searchForReferences(foo.getType("Foo").getMethods()[0]).stream().count());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1473
    public void testConstructorReferences24() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo() {} \n" +
            "  Foo(Map m) {}\n" + // search for this
            "  String bar, baz\n" +
            "}\n");
        createUnit("Other", "def one = new p.Foo(), two = new p.Foo(baz:'')\n");

        assertEquals(1, searchForReferences(foo.getType("Foo").getMethods()[1]).stream().count());
    }

    @Test
    public void testConstructorReferences25() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo() {} \n" + // search for this
            "  Foo(String s) {}\n" +
            "  String bar, baz;\n" +
            "}\n");
        createUnit("Other", "def map = [bar:'x',baz:'y']; def obj = new p.Foo(map)\n");

        assertEquals(1, searchForReferences(foo.getType("Foo").getMethods()[0]).stream().count());
    }

    @Test
    public void testConstructorReferences26() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo() {} \n" +
            "  Foo(object) {}\n" + // search for this
            "  String bar,baz\n" +
            "}\n");
        createUnit("Other", "def map = [bar:'x',baz:'y']; def obj = new p.Foo(map)\n");

        assertEquals(1, searchForReferences(foo.getType("Foo").getMethods()[1]).stream().count());
    }

    @Test
    public void testConstructorReferences27() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo() {} \n" +
            "  Foo(... array) {}\n" + // search for this
            "  String bar,baz\n" +
            "}\n");
        createUnit("Other", "def map = [bar:'x',baz:'y']; def obj = new p.Foo(map)\n");

        assertEquals(1, searchForReferences(foo.getType("Foo").getMethods()[1]).stream().count());
    }

    @Test
    public void testAliasConstructorReferences1() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo( ) {}\n" + // search for this
            "  Foo(a) {}\n" +
            "}\n");
        createUnit("Other", "import p.Foo as Bar\n" +
            "new Bar()\n" + // yes
            "new Bar(a)\n" + // no
            "new p.Foo()\n" + // yes
            "new p.Foo(a)\n"); // no

        IMethod constructor = foo.getType("Foo").getMethods()[0];
        List<SearchMatch> matches = searchForReferences(constructor);
        assertEquals("Incorrect number of matches;", 2, matches.size());

        int fooCount = 0, otherCount = 0;
        for (SearchMatch match : matches) {
            if (match.getElement() instanceof IMethod) {
                if (((IMethod) match.getElement()).getResource().getName().equals("Foo.groovy")) {
                    fooCount += 1;
                } else if (((IMethod) match.getElement()).getResource().getName().equals("Other.groovy")) {
                    otherCount += 1;
                }
            }
        }
        assertEquals("Should have found 2 matches in Foo.groovy", 0, fooCount);
        assertEquals("Should have found 4 matches in Other.groovy", 2, otherCount);
    }

    @Test
    public void testAliasConstructorReferences2() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo( ) {}\n" + // search for this
            "  Foo(a) {}\n" +
            "}\n");
        createUnit("Other", "import p.Foo as Bar\n" +
            "new Bar()\n" + // yes
            "new Bar(a)\n"); // no

        IMethod constructor = foo.getType("Foo").getMethods()[0];
        List<SearchMatch> matches = searchForReferences(constructor);
        assertEquals("Incorrect number of matches;", 0, matches.size());
    }

    @Test
    public void testAliasConstructorReferences3() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo( ) {}\n" + // search for this
            "  Foo(a) {}\n" +
            "}\n");
        createUnit("Other", // import p.Foo as Bar
            "new Bar()\n" + // yes
            "new Bar(a)\n"); // no

        createConfigScript(
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    alias 'Bar', 'p.Foo'\n" +
            "  }\n" +
            "}\n");

        IMethod constructor = foo.getType("Foo").getMethods()[0];
        List<SearchMatch> matches = searchForReferences(constructor);
        assertEquals("Incorrect number of matches;", 0, matches.size());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/796
    public void testNewifyConstructorReferences1() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" + // search for this
            "  Foo(String s) {}\n" +
            "}\n");
        createUnit("Bar", "import p.Foo\n" +
            "@Newify\n" +
            "def m() {\n" +
            "  Foo.new()\n" + // no
            "  Foo.new(0)\n" + // yes
            "  Foo.new('')\n" + // no
            "}\n");

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(1, ctorRefs);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/796
    public void testNewifyConstructorReferences2() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" + // search for this
            "  Foo(String s) {}\n" +
            "}\n");
        createUnit("Bar", "import p.Foo\n" +
            "@Newify\n" +
            "def m() {\n" +
            "  Foo.new()\n" + // no
            "  Foo.new(0)\n" + // yes
            "  Foo.new('')\n" + // no
            "}\n");

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(1, ctorRefs);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/797
    public void testNewifyConstructorReferences3() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" + // search for this
            "  Foo(String s) {}\n" +
            "}\n");
        createUnit("Bar", "import p.Foo\n" +
            "@Newify(Foo)\n" +
            "def m() {\n" +
            "  Foo()\n" + // no
            "  Foo(0)\n" + // yes
            "  Foo('')\n" + // no
            "}\n");

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(1, ctorRefs);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/797
    public void testNewifyConstructorReferences4() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" + // search for this
            "  Foo(String s) {}\n" +
            "}\n");
        createUnit("Bar", "import p.Foo\n" +
            "@Newify(Foo)\n" +
            "def m() {\n" +
            "  Foo()\n" + // no
            "  Foo(0)\n" + // yes
            "  Foo('')\n" + // no
            "}\n");

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(1, ctorRefs);
    }

    //--------------------------------------------------------------------------

    private void createConfigScript(final String script) {
        env.addFile(project.getFullPath(), "config.groovy", script);
        env.getJavaProject(project.getFullPath()).setOption(OPTIONG_GroovyCompilerConfigScript, "config.groovy");
    }

    private List<SearchMatch> searchForReferences(final IMethod method) throws CoreException {
        return search(SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES),
            SearchEngine.createJavaSearchScope(new IJavaElement[] {JavaCore.create(project)}));
    }
}
