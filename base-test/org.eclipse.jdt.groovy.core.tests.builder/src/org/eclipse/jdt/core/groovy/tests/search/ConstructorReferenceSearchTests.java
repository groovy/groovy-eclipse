/*
 * Copyright 2009-2020 the original author or authors.
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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
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
            "}");
        createUnit("", "Other", "import p.Foo\n" +
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
            "}");
        createUnit("", "Other", "import p.Foo.Bar.Baz\n" +
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
            "}");
        createUnit("", "Bar", "import p.Foo\n" +
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
            "}");
        createUnit("", "Bar", "import p.Foo\n" +
            "new Foo()\n" + // yes
            "new Foo(0)\n" + // yes
            "new Foo('')\n"); // no

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(2, ctorRefs);
    }

    @Test
    public void testConstructorReferences5() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" +
            "  Foo(String s) {}\n" + // search for this
            "}");
        createUnit("", "Bar", "import p.Foo\n" +
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
            "}");
        createUnit("", "Bar", "import p.Foo\n" +
            "new Foo()\n" + // yes
            "new Foo(a)\n" + // no
            "new Foo(a,b)\n"); // yes

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(2, ctorRefs);
    }

    @Test
    public void testConstructorReferences7() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo() {}\n" +
            "  Foo(a) {}\n" + // search for this
            "}");
        createUnit("", "Bar", "import p.Foo\n" +
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
            "}");
        createUnit("", "Bar", "import p.Foo\n" +
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
            "}");
        createUnit("", "Bar", "import p.Foo\n" +
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
            "}");
        createUnit("", "Bar", "import p.Foo\n" +
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
            "  Foo(int i = 0) {}\n" + // search for this
            "  Foo(String s) {}\n" +
            "}");
        createUnit("", "Bar", "import p.Foo\n" +
            "new Foo()\n" + // yes
            "new Foo(0)\n" + // yes
            "new Foo('')\n"); // no

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(2, ctorRefs);
    }

    @Test // same-unit references exercise patch in Verifier.addDefaultParameterConstructors
    public void testConstructorReferences11a() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i = 0) {}\n" + // search for this
            "  Foo(String s) {this()}\n" + // yes
            "  def m() {\n" +
            "    new Foo()\n" + // yes
            "    new Foo(0)\n" + // yes
            "    new Foo('')\n" + // no
            "  }\n" +
            "}");

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Foo.groovy"))
            .count();
        assertEquals(3, ctorRefs);
    }

    @Test // default value generates a synthetic constructor
    public void testConstructorReferences12() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" +
            "  Foo(String s = '') {}\n" + // search for this
            "}");
        createUnit("", "Bar", "import p.Foo\n" +
            "new Foo()\n" + // yes
            "new Foo(0)\n" + // no
            "new Foo('')\n"); // yes

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[1]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(2, ctorRefs);
    }

    @Test // same-unit references exercise patch in Verifier.addDefaultParameterConstructors
    public void testConstructorReferences12a() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {this()}\n" + // yes
            "  Foo(String s = '') {}\n" + // search for this
            "  def m() {\n" +
            "    new Foo()\n" + // yes
            "    new Foo(0)\n" + // no
            "    new Foo('')\n" + // yes
            "  }\n" +
            "}");

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[1]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Foo.groovy"))
            .count();
        assertEquals(3, ctorRefs);
    }

    @Test
    public void testConstructorReferences13() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" + // search for this
            "  Foo(String s) {}\n" +
            "}");
        createUnit("", "Bar", "import p.Foo\n" +
            "def bar = new Foo(0) {\n" + // yes
            "}\n");

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(1, ctorRefs);
    }

    @Test
    public void testConstructorReferences14() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" +
            "  Foo(String s) {}\n" + // search for this
            "}");
        createUnit("", "Bar", "import p.Foo\n" +
            "def bar = new Foo(0) {\n" + // no
            "}\n");

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[1]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(0, ctorRefs);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/796
    public void testNewifyConstructorReferences1() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" + // search for this
            "  Foo(String s) {}\n" +
            "}");
        createUnit("", "Bar", "import p.Foo\n" +
            "@Newify\n" +
            "def m() {\n" +
            "  Foo.new()\n" + // yes
            "  Foo.new(0)\n" + // yes
            "  Foo.new('')\n" + // no
            "}\n");

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(2, ctorRefs);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/796
    public void testNewifyConstructorReferences2() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" + // search for this
            "  Foo(String s) {}\n" +
            "}");
        createUnit("", "Bar", "import p.Foo\n" +
            "@Newify\n" +
            "def m() {\n" +
            "  Foo.new()\n" + // yes
            "  Foo.new(0)\n" + // yes
            "  Foo.new('')\n" + // no
            "}\n");

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(2, ctorRefs);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/797
    public void testNewifyConstructorReferences3() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" + // search for this
            "  Foo(String s) {}\n" +
            "}");
        createUnit("", "Bar", "import p.Foo\n" +
            "@Newify(Foo)\n" +
            "def m() {\n" +
            "  Foo()\n" + // yes
            "  Foo(0)\n" + // yes
            "  Foo('')\n" + // no
            "}\n");

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(2, ctorRefs);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/797
    public void testNewifyConstructorReferences4() throws Exception {
        GroovyCompilationUnit foo = createUnit("p", "Foo", "package p\n" +
            "class Foo {\n" +
            "  Foo(int i) {}\n" + // search for this
            "  Foo(String s) {}\n" +
            "}");
        createUnit("", "Bar", "import p.Foo\n" +
            "@Newify(Foo)\n" +
            "def m() {\n" +
            "  Foo()\n" + // yes
            "  Foo(0)\n" + // yes
            "  Foo('')\n" + // no
            "}\n");

        long ctorRefs = searchForReferences(foo.getType("Foo").getMethods()[0]).stream()
            .filter(match -> ((IMethod) match.getElement()).getResource().getName().equals("Bar.groovy"))
            .count();
        assertEquals(2, ctorRefs);
    }

    //--------------------------------------------------------------------------

    List<SearchMatch> searchForReferences(IMethod method) throws CoreException {
        new SearchEngine().search(
            SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES),
            new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
            SearchEngine.createJavaSearchScope(new IJavaElement[] {JavaCore.create(project)}, false),
            searchRequestor, new NullProgressMonitor());
        return searchRequestor.getMatches();
    }
}
