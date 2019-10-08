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
package org.eclipse.jdt.core.groovy.tests.search;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.groovy.tests.MockPossibleMatch;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.groovy.search.TypeRequestorFactory;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link org.eclipse.jdt.groovy.search.MethodReferenceSearchRequestor}
 */
public final class MethodReferenceSearchTests extends SearchTestSuite {

    @Test
    public void testMethodReferencesInScript1() throws Exception {
        doTestForTwoMethodReferencesInScript("new First().xxx()\nnew First()\n.\nxxx()");
    }

    @Test
    public void testMethodReferencesInScript2() throws Exception {
        doTestForTwoMethodReferencesInScript("First f = new First()\n f.xxx()\n f\n.\nxxx()");
    }

    @Test
    public void testMethodReferencesInScript3() throws Exception {
        doTestForTwoMethodReferencesInScript("First f = new First()\n \"${f.xxx()}\"\n\"${f.xxx()}\"");
    }

    @Test
    public void testMethodReferencesInScript4() throws Exception {
        doTestForTwoMethodReferencesInScriptWithQuotes("First f = new First()\n f.'xxx'()\nf.'xxx'()");
    }

    @Test // GRECLIPSE-1180
    public void testMethodReferencesInScript5() throws Exception {
        doTestForTwoMethodReferencesInScript("new First().xxx()\n'xxx'\n\"xxx\"\n\"\"\"xxx\"\"\"\nnew First()\n.\nxxx()");
    }

    @Test
    public void testMethodReferencesInScript6() throws Exception {
        doTestForTwoMethodReferencesInScript("First f = new First()\n f.xxx()\ndef xxx = 0\nxxx++\nf.xxx()");
    }

    @Test
    public void testMethodReferencesInScript7() throws Exception {
        doTestForTwoMethodReferencesInScript("class SubClass extends First {} \n SubClass f = new SubClass()\n f.xxx()\ndef xxx = 0\nxxx++\nf.xxx()");
    }

    @Test
    public void testMethodReferencesInScript8() throws Exception {
        createUnit("Other.groovy", "class Other {\ndef xxx\n}");
        doTestForTwoMethodReferencesInScript("class SubClass extends First {} \n SubClass f = new SubClass()\n f.xxx()\nnew Other().xxx = 0\nf.xxx()");
    }

    @Test
    public void testMethodReferencesInScript9() throws Exception {
        doTestForTwoMethodReferencesInScript(
            "class SubClass extends First {}\n" +
            "def f = new SubClass()\n" +
            "f.xxx()\n" + // here
            "f = 9\n" +
            "f.xxx\n" +  // invalid reference
            "f = new SubClass()\n" +
            "f.xxx()");  // here
    }

    @Test
    public void testMethodReferencesInClass1() throws Exception {
        // "class First { def xxx() { } }"
        doTestForTwoMethodReferencesInClass(
            "class Second extends First {\n" +
            "  def method() {\n" +
            "    this.xxx()\n" + // yes
            "    this.xxx\n" + // no
            "  }\n" +
            "  def xxx() {}\n" + // no; an overload is a declaration, not a reference
            "  def method2() {\n" +
            "    super.xxx\n" + // no
            "    super.xxx()\n" + // yes
            "  }\n" +
            "}\n");
    }

    @Test
    public void testMethodReferencesInClass2() throws Exception {
        // "class First { def xxx() { } }"
        doTestForTwoMethodReferencesInClass(
            "class Second extends First {\n" +
            "  def method() {\n" +
            "    def closure = this.&xxx\n" + // yes
            "    this.xxx = 'nothing'\n" + // no
            "  }\n" +
            "  def xxx() {}\n" +  // no; an overload is a declaration, not a reference
            "  def method2() {\n" +
            "    def nothing = this.xxx()\n" +  // yes
            "  }\n" +
            "}");
    }

    @Test
    public void testMethodReferencesInClass3() throws Exception {
        createUnit("Third",
            "class Third {\n" +
            "  def xxx() {}\n" +
            "}\n");
        // "class First { def xxx() { } }"
        doTestForTwoMethodReferencesInClass(
            "class Second extends First {\n" +
            "  def method() {\n" +
            "    def closure = super.&xxx\n" + // yes
            "    super.xxx = 'nothing'\n" + // no
            "  }\n" +
            "  def xxx() {}\n" +  // no
            "  def method2(xxx) {\n" +  // no
            "    new Third().xxx()\n" + // no
            "    xxx()\n" + // no; ref to parameter, not the method
            "    xxx = xxx\n" +  // no, no
            "    def nothing = super.xxx()\n" +  // yes
            "  }\n" +
            "}");
    }

    @Test
    public void testOverloadedMethodReferences1() throws Exception {
        // search for "First.xxx()" should match on the method reference with precise # of args as well as method reference with unmatched number of args
        doTestForTwoMethodReferences(
            "interface First {\n" +
            "    void xxx()\n" +
            "    void xxx(a)\n" +
            "}",
            "public class Second implements First {\n" +
            "    public void other() {\n" +
            "        xxx()\n" + //yes
            "    }\n" +
            "    public void xxx() {\n" +
            "        xxx(a)\n" + //no!
            "    }\n" +
            "    void xxx(a) {\n" +
            "        xxx(a,b)\n" + //yes
            "    }\n" +
            "}",
            false, 0, "xxx");
    }

    @Test
    public void testOverloadedMethodReferences2() throws Exception {
        // search for "First.xxx(a)" should match on the method reference with precise # of args as well as method reference with unmatched number of args
        doTestForTwoMethodReferences(
            "interface First {\n" +
            "    void xxx(a)\n" +
            "    void xxx()\n" +
            "}",
            "public class Second implements First {\n" +
            "    public void other() {\n" +
            "        xxx(a)\n" + //yes
            "    }\n" +
            "    public void xxx() {\n" +
            "        xxx()\n" + //no!
            "    }\n" +
            "    void xxx(a) {\n" +
            "        xxx(a,b)\n" + //yes
            "    }\n" +
            "}",
            false, 0, "xxx");
    }

    @Test
    public void testOverloadedMethodReferences3() throws Exception {
        // search for "First.xxx(a)" should match on the method reference with precise # of args as well as method reference with unmatched number of args
        createUnit("Sub",
            "interface Sub extends First {\n" +
            "    void xxx(a)\n" +
            "}");
        doTestForTwoMethodReferences(
            "interface First {\n" +
            "    void xxx(a)\n" +
            "    void xxx()\n" +
            "}",
            "public class Second implements Sub {\n" +
            "    public void other() {\n" +
            "        xxx(a)\n" + //yes
            "    }\n" +
            "    public void xxx() {\n" +
            "        xxx()\n" + //no!
            "    }\n" +
            "    void xxx(a) {\n" +
            "        xxx(a,b)\n" + //yes
            "    }\n" +
            "}",
            false, 0, "xxx");
    }

    @Test
    public void testOverloadedMethodReferences4() throws Exception {
        // search for "First.xxx(a,b)" should match on the method reference with precise # of args as well as method reference with unmatched number of args
        createUnit("Sub",
            "interface Sub extends First {\n" +
            "    void xxx(a)\n" +
            "    void xxx(a,b,c)\n" +
            "}");
        doTestForTwoMethodReferences(
            "interface First {\n" +
            "    void xxx(a,b)\n" +
            "    void xxx(a)\n" +
            "}",
            "public class Second implements Sub {\n" +
            "    public void other() {\n" +
            "        First f\n" +
            "        f.xxx(a,b,c)\n" + //yes
            "    }\n" +
            "    public void xxx() {\n" +
            "        xxx(a)\n" + //no!
            "        xxx(a,b,c)\n" + //no!
            "        Sub s\n" +
            "        s.xxx(a)\n" + //no!
            "        s.xxx(a,b,c)\n" + //no!
            "    }\n" +
            "    void xxx(a) {\n" +
            "        Sub s\n" +
            "        s.xxx(a,b)\n" + //yes
            "    }\n" +
            "}",
            false, 0, "xxx");
    }

    @Test
    public void testOverloadedMethodReferences5() throws Exception {
        doTestForTwoMethodReferences(
            "class First {\n" +
            "  URL doSomething(String s, URL u) {}\n" + // search for references
            "  URL doSomething(Integer i, URL u) {}\n" +
            "}",
            "class Second {\n" +
            "  First first\n" +
            "  def other\n" +
            "  void xxx() {\n" +
            "    URL u = new URL('www.example.com')\n" +
            "    first.doSomething('ciao', u)\n" + //yes
            "    first.doSomething(1, u)\n" + //no!
            "    first.&doSomething\n" + //yes
            "  }\n" +
            "}",
            true, 2, "doSomething"); // "true, 2" says both matches are in xxx() (aka Second.children[2])
    }

    @Test @Ignore("https://github.com/groovy/groovy-eclipse/issues/373")
    public void testOverloadedMethodReferences5a() throws Exception {
        doTestForTwoMethodReferences(
            "class First {\n" +
            "  URL doSomething(String s, URL u) {}\n" + // search for references
            "  URL doSomething(Integer i, URL u) {}\n" +
            "}",
            "class Second {\n" +
            "  First first\n" +
            "  def other\n" +
            "  void xxx() {\n" +
            "    URL u = new URL('www.example.com')\n" +
            "    first.doSomething('ciao', u)\n" + //yes
            "    first.doSomething(1L, u)\n" + //no!
            "    first.&doSomething\n" + //yes
            "  }\n" +
            "}",
            true, 2, "doSomething"); // "true, 2" says both matches are in xxx() (aka Second.children[2])
    }

    @Test
    public void testOverloadedMethodReferences5b() throws Exception {
        doTestForTwoMethodReferences(
            "class First {\n" +
            "  URL doSomething(Integer i, URL u) {}\n" + // search for references
            "  URL doSomething(String s, URL u) {}\n" +
            "}",
            "class Second {\n" +
            "  First first\n" +
            "  def other\n" +
            "  void xxx() {\n" +
            "    URL u = 'www.example.com'.toURL()\n" +
            "    first.doSomething(1, u)\n" + //yes
            "    first.doSomething('', u)\n" + //no!
            "    first.&doSomething\n" + //yes
            "  }\n" +
            "}",
            true, 2, "doSomething"); // "true, 2" says both matches are in xxx() (aka Second.children[2])
    }

    @Test
    public void testMethodWithDefaultParameters1() throws Exception {
        doTestForTwoMethodReferences(
            "class First {\n" +
            "    void xxx(a, b = 9) {}\n" +
            "    void xxx(a, b, c) {}\n" +
            "}",
            "class Second {\n" +
            "    void other0() {\n" +
            "        First f\n" +
            "        f.xxx(a)\n" +
            "    }\n" +
            "    void other1() {\n" +
            "        First f\n" +
            "        f.xxx(a,b,c)\n" +
            "    }\n" +
            "    void other2() {\n" +
            "        First f\n" +
            "        f.xxx(a,b)\n" +
            "    }\n" +
            "}",
            false, 0, "xxx");
    }

    @Test
    public void testMethodWithDefaultParameters2() throws Exception {
        doTestForTwoMethodReferences(
            "class First {\n" +
            "    void xxx(a, b = 9) {}\n" +
            "    void xxx(a, b, c) {}\n" +
            "}",
            "class Second {\n" +
            "    void other0() {\n" +
            "        First f\n" +
            "        f.xxx(a)\n" +
            "    }\n" +
            "    void other1() {\n" +
            "        First f\n" +
            "        f.xxx(a,b,c)\n" +
            "    }\n" +
            "    void other2() {\n" +
            "        First f\n" +
            "        f.&xxx\n" +
            "    }\n" +
            "}",
            false, 0, "xxx");
    }

    @Test
    public void testMethodWithDefaultParameters3() throws Exception {
        GroovyCompilationUnit groovyUnit = createUnit("foo", "Bar",
            "package foo\n" +
            "class Bar {\n" +
            "  void doSomething() {}\n" +
            "  void doSomething(String one, String two = 'x') {}\n" +
            "}");
        createUnit("foo", "Baz",
            "package foo;\n" +
            "public class Baz {\n" +
            "  void test(Bar bar) {\n" +
            "    bar.doSomething();\n" + //no!
            "    bar.doSomething(\"one\");\n" + //yes
            "    bar.doSomething(\"one\", \"two\");\n" + //yes
            "  }\n" +
            "}");

        IMethod method = groovyUnit.getType("Bar").getMethods()[1];
        new SearchEngine().search(
            SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES),
            new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
            SearchEngine.createJavaSearchScope(new IJavaElement[] {groovyUnit.getPackageFragmentRoot()}, false),
            searchRequestor, new NullProgressMonitor());

        List<SearchMatch> matches = searchRequestor.getMatches();

        assertEquals(2, matches.size());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(0).getAccuracy());
        assertEquals("Baz.groovy", ((IJavaElement) matches.get(0).getElement()).getResource().getName());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(1).getAccuracy());
        assertEquals("Baz.groovy", ((IJavaElement) matches.get(1).getElement()).getResource().getName());
    }

    @Test @Ignore("Only one method signature is searched and Java lacks link to original method")
    public void testMethodWithDefaultParameters4() throws Exception {
        GroovyCompilationUnit groovyUnit = createUnit("foo", "Bar",
            "package foo\n" +
            "class Bar {\n" +
            "  void doSomething() {}\n" +
            "  void doSomething(String one, String two = 'x') {}\n" +
            "}");
        createJavaUnit("foo", "Baz",
            "package foo;\n" +
            "public class Baz {\n" +
            "  void test(Bar bar) {\n" +
            "    bar.doSomething();\n" + //no!
            "    bar.doSomething(\"one\");\n" + //no! (want to be yes)
            "    bar.doSomething(\"one\", \"two\");\n" + //yes
            "  }\n" +
            "}");

        IMethod method = groovyUnit.getType("Bar").getMethods()[1];
        new SearchEngine().search(
            SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES),
            new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
            SearchEngine.createJavaSearchScope(new IJavaElement[] {groovyUnit.getPackageFragmentRoot()}, false),
            searchRequestor, new NullProgressMonitor());

        List<SearchMatch> matches = searchRequestor.getMatches();

        assertEquals(2, matches.size());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(0).getAccuracy());
        assertEquals("Baz.java", ((IJavaElement) matches.get(0).getElement()).getResource().getName());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(1).getAccuracy());
        assertEquals("Baz.java", ((IJavaElement) matches.get(1).getElement()).getResource().getName());
    }

    @Test
    public void testStaticMethodReferenceSearch() throws Exception {
        String contents =
            "class Foo {\n" +
            "  static def bar() {}\n" +
            "  static {\n" +
            "    bar()\n" +
            "  }\n" +
            "}\n";
        GroovyCompilationUnit foo = createUnit("Foo", contents);

        IMethod method = foo.getType("Foo").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(foo);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(1, searchRequestor.getMatches().size());
        assertLocation(searchRequestor.getMatch(0), contents.lastIndexOf("bar"), "bar".length());
    }

    @Test
    public void testExplicitPropertyGetterSearch1() throws Exception {
        GroovyCompilationUnit bar = createUnit("foo", "Bar",
            "package foo\n" +
            "class Bar {\n" +
            "  static String string\n" +
            "  static String getString() {\n" +
            "  }\n" +
            "}");
        GroovyCompilationUnit baz = createUnit("foo", "Baz",
            "package foo\n" +
            "import static foo.Bar.getString as blah\n" + // potential
            "import static foo.Bar.getString as getS\n" + // potential
            "Bar.getString()\n" + // exact
            "Bar.'getString'()\n" + // exact
            "str = Bar.string\n" + // potential
            "str = Bar.@string\n" +
            "fun = Bar.&getString\n" + // potential
            "str = blah()\n" + // potential
            "str = s\n" + // potential
            "");

        IMethod method = bar.getType("Bar").getMethods()[0];
        new SearchEngine().search(
            SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES),
            new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
            SearchEngine.createJavaSearchScope(new IJavaElement[] {bar.getPackageFragmentRoot()}, false),
            searchRequestor, new NullProgressMonitor());

        List<SearchMatch> matches = searchRequestor.getMatches();

        assertEquals(8, matches.size());
        assertEquals(SearchMatch.A_INACCURATE, matches.get(0).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("getString as blah"), matches.get(0).getOffset());
        assertEquals(SearchMatch.A_INACCURATE, matches.get(1).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("getString as getS"), matches.get(1).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(2).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("getString()"), matches.get(2).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(3).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("'getString'"), matches.get(3).getOffset());

        assertEquals(SearchMatch.A_INACCURATE, matches.get(4).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("Bar.string") + 4, matches.get(4).getOffset());
        assertEquals(SearchMatch.A_INACCURATE, matches.get(5).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("Bar.&getString") + 5, matches.get(5).getOffset());
        assertEquals(SearchMatch.A_INACCURATE, matches.get(6).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).lastIndexOf("blah"), matches.get(6).getOffset());
        assertEquals(SearchMatch.A_INACCURATE, matches.get(7).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).lastIndexOf("s"), matches.get(7).getOffset());
    }

    @Test
    public void testExplicitPropertyGetterSearch2() throws Exception {
        GroovyCompilationUnit bar = createUnit("foo", "Bar",
            "package foo\n" +
            "class Bar {\n" +
            "  String string\n" +
            "  String getString() {}\n" +
            "}");
        GroovyCompilationUnit baz = createUnit("foo", "Baz",
            "package foo\n" +
            "new Bar().with {\n" +
            "  def str = string\n" + // exact
            "  str = getString()\n" + // exact
            "  str = 'getString'()\n" + // exact
            "  def fun = delegate.&getString\n" + // potential
            "}\n" +
            "");

        IMethod method = bar.getType("Bar").getMethods()[0];
        new SearchEngine().search(
            SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES),
            new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
            SearchEngine.createJavaSearchScope(new IJavaElement[] {bar.getPackageFragmentRoot()}, false),
            searchRequestor, new NullProgressMonitor());

        List<SearchMatch> matches = searchRequestor.getMatches();

        assertEquals(4, matches.size());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(0).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("string"), matches.get(0).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(1).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("getString"), matches.get(1).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(2).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("'getString'"), matches.get(2).getOffset());
        assertEquals(SearchMatch.A_INACCURATE, matches.get(3).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).lastIndexOf("getString"), matches.get(3).getOffset());
    }

    @Test
    public void testExplicitPropertySetterSearch1() throws Exception {
        GroovyCompilationUnit bar = createUnit("foo", "Bar",
            "package foo\n" +
            "class Bar {\n" +
            "  String string\n" +
            "  void setString(String string) {\n" +
            "    this.string = (string ?: '')\n" +
            "  }\n" +
            "}");
        GroovyCompilationUnit baz = createUnit("foo", "Baz",
            "package foo\n" +
            "def bar = new Bar(string: null)\n" + // exact
            "bar.setString(null)\n" + // exact
            "bar.'setString'(null)\n" + // exact
            "bar.string = null\n" + // exact
            "def str = bar.string\n" +
            "bar.@string = null\n" +
            "bar.&setString\n" + // potential
            "");

        IMethod method = bar.getType("Bar").getMethods()[0];
        new SearchEngine().search(
            SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES),
            new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
            SearchEngine.createJavaSearchScope(new IJavaElement[] {bar.getPackageFragmentRoot()}, false),
            searchRequestor, new NullProgressMonitor());

        List<SearchMatch> matches = searchRequestor.getMatches();

        assertEquals(5, matches.size());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(0).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("string:"), matches.get(0).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(1).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("setString"), matches.get(1).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(2).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("'setString'"), matches.get(2).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(3).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("string = "), matches.get(3).getOffset());
        assertEquals(SearchMatch.A_INACCURATE, matches.get(4).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).lastIndexOf("setString"), matches.get(4).getOffset());
    }

    @Test
    public void testExplicitPropertySetterSearch2() throws Exception {
        GroovyCompilationUnit bar = createUnit("foo", "Bar",
            "package foo\n" +
            "class Bar {\n" +
            "  String string\n" +
            "  void setString(String string) {\n" +
            "    this.string = (string ?: '')\n" +
            "  }\n" +
            "}");
        GroovyCompilationUnit baz = createUnit("foo", "Baz",
            "package foo\n" +
            "new Bar().with {\n" +
            "  setString(null)\n" + // exact
            "  'setString'(null)\n" + // exact
            "  string = 'ab'\n" + // exact
            "  string += 'c'\n" + // exact
            "  def str = string\n" +
            "  delegate.@string = null\n" +
            "  delegate.&setString\n" + // potential
            "}\n" +
            "");

        IMethod method = bar.getType("Bar").getMethods()[0];
        new SearchEngine().search(
            SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES),
            new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
            SearchEngine.createJavaSearchScope(new IJavaElement[] {bar.getPackageFragmentRoot()}, false),
            searchRequestor, new NullProgressMonitor());

        List<SearchMatch> matches = searchRequestor.getMatches();

        assertEquals(5, matches.size());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(0).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("setString"), matches.get(0).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(1).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("'setString'"), matches.get(1).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(2).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("string = "), matches.get(2).getOffset());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(3).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("string +="), matches.get(3).getOffset());
        assertEquals(SearchMatch.A_INACCURATE, matches.get(4).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).lastIndexOf("setString"), matches.get(4).getOffset());
    }

    @Test
    public void testGenericsMethodReferenceSearch() throws Exception {
        GroovyCompilationUnit groovyUnit = createUnit("foo", "Bar",
            "package foo\n" +
            "abstract class Bar {\n" +
            "  String xxx(Number number, Date date) {\n" +
            "    'Hello'\n" +
            "  }\n" +
            "}");
        @SuppressWarnings("unused")
        ICompilationUnit javaUnit = createJavaUnit("foo", "Baz",
            "package foo;\n" +
            "import java.util.Date;\n" +
            "public class Baz<T extends Bar> {\n" +
            "  private T test;\n" +
            "  void testCase() {\n" +
            "    test.xxx(1, new Date());\n" +
            "  }\n" +
            "}");

        IMethod method = groovyUnit.getType("Bar").getMethods()[0];
        new SearchEngine().search(
            SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES),
            new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
            SearchEngine.createJavaSearchScope(new IJavaElement[] {groovyUnit.getPackageFragmentRoot()}, false),
            searchRequestor, new NullProgressMonitor());

        List<SearchMatch> matches = searchRequestor.getMatches();

        assertEquals(1, matches.size());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(0).getAccuracy());
        assertEquals("Baz.java", ((IJavaElement) matches.get(0).getElement()).getResource().getName());
    }

    //--------------------------------------------------------------------------

    private void doTestForTwoMethodReferencesInScript(String secondContents) throws Exception {
        doTestForTwoMethodReferences(FIRST_CONTENTS_CLASS_FOR_METHODS, secondContents, true, 3, "xxx");
    }

    private void doTestForTwoMethodReferencesInScriptWithQuotes(String secondContents) throws Exception {
        doTestForTwoMethodReferences(FIRST_CONTENTS_CLASS_FOR_METHODS, secondContents, true, 3, "'xxx'");
    }

    private void doTestForTwoMethodReferencesInClass(String secondContents) throws Exception {
        doTestForTwoMethodReferences(FIRST_CONTENTS_CLASS_FOR_METHODS, secondContents, false, 0, "xxx");
    }
}
