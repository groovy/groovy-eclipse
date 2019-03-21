/*
 * Copyright 2009-2018 the original author or authors.
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
import org.junit.Test;

public final class MethodReferenceSearchTests extends SearchTestSuite {

    @Test
    public void testMethodReferencesInScript1() throws Exception {
        doTestForTwoMethodReferencesInScript("new First().xxx\nnew First()\n.\nxxx");
    }

    @Test
    public void testMethodReferencesInScript1GRE_1180() throws Exception {
        doTestForTwoMethodReferencesInScript("new First().xxx\n'xxx'\n\"xxx\"\n\"\"\"xxx\"\"\"\nnew First()\n.\nxxx");
    }

    @Test
    public void testMethodReferencesInScript2() throws Exception {
        doTestForTwoMethodReferencesInScript("First f = new First()\n f.xxx = f.xxx");
    }

    @Test
    public void testMethodReferencesInScript3() throws Exception {
        doTestForTwoMethodReferencesInScript("First f = new First()\n \"$f.xxx\"\n\"$f.xxx\"");
    }

    @Test
    public void testMethodReferencesInScript4() throws Exception {
        doTestForTwoMethodReferencesInScriptWithQuotes("First f = new First()\n f.'xxx'\nf.'xxx'");
    }

    @Test
    public void testMethodReferencesInScript5() throws Exception {
        doTestForTwoMethodReferencesInScript("First f = new First()\n f.xxx\ndef xxx = 0\nxxx++\nf.xxx");
    }

    @Test
    public void testMethodReferencesInScript6() throws Exception {
        doTestForTwoMethodReferencesInScript("class SubClass extends First { } \n SubClass f = new SubClass()\n f.xxx\ndef xxx = 0\nxxx++\nf.xxx");
    }

    @Test
    public void testMethodReferencesInScript7() throws Exception {
        createUnit("Other.groovy", "class Other { def xxx }");
        doTestForTwoMethodReferencesInScript("class SubClass extends First { } \n SubClass f = new SubClass()\n f.xxx\nnew Other().xxx = 0\nf.xxx");
    }

    @Test
    public void testMethodReferencesInScript8() throws Exception {
        doTestForTwoMethodReferencesInScript(
            "class SubClass extends First { } \n " +
            "def f = new SubClass()\n " +
            "f.xxx\n" + // here
            "f = 9\n" +
            "f.xxx\n" +  // invalid reference
            "f = new SubClass()\n" +
            "f.xxx");  // here
    }

    @Test
    public void testMethodReferencesInClass1() throws Exception {
        doTestForTwoMethodReferencesInClass("class Second extends First { \ndef method() { this.xxx }\ndef xxx() { }\n def method2() { super.xxx }}");
    }

    @Test
    public void testMethodReferencesInClass2() throws Exception {
        doTestForTwoMethodReferencesInClass("class Second extends First { \ndef method() { xxx }\ndef xxx() { }\n def method2(xxx) { xxx = super.xxx }}");
    }

    @Test
    public void testMethodReferencesInClass3() throws Exception {
        doTestForTwoMethodReferencesInClass(
            "class Second extends First {\n" +
            "  def method() {\n" +
            "    this.xxx = 'nothing'\n" + // yes
            "  }\n" +
            "  def xxx() { }\n" +  // no
            "  def method2() {\n" +  // no
            "    def nothing = super.xxx()\n" +  // yes...field reference used as a closure
            "  }\n" +
            "}");
    }

    @Test
    public void testMethodReferencesInClass4() throws Exception {
        createUnit("Third",
            "class Third {\n" +
            "  def xxx() { }\n" + // no
            "}\n");
        doTestForTwoMethodReferencesInClass(
            "class Second extends First {\n" +
            "  def method() {\n" +
            "    this.xxx = 'nothing'\n" + // yes
            "  }\n" +
            "  def xxx() { }\n" +  // no
            "  def method3(xxx) {\n" +  // no
            "    new Third().xxx()\n" + // no
            "    xxx()\n" + // no...this will try to execute the xxx parameter, not the method
            "    xxx = xxx\n" +  // no, no
            "    def nothing = super.xxx\n" +  // yes...method reference passed as a closure
            "  }\n" +
            "}");
    }

    @Test
    public void testOverloadedMethodReferences1() throws Exception {
        // should match on the method reference with precise # of args as well as method reference with unmatched number of args
        doTestForTwoMethodReferences(
            "interface First {\n" +
            "    void xxx()\n" +
            "    void xxx(a)\n" +
            "}",
            "public class Second implements First {\n" +
            "    public void other() {\n" +
            "        xxx()\n" +
            "    }\n" +
            "    public void xxx() {\n" +
            "        xxx(a)\n" +
            "    }\n" +
            "    void xxx(a) {\n" +
            "        xxx(a,b)\n" +
            "    }\n" +
            "}",
            false, 0, "xxx");
    }

    @Test
    public void testOverloadedMethodReferences2() throws Exception {
        // should match on the method reference with precise # of args as well as method reference with unmatched number of args
        doTestForTwoMethodReferences(
            "interface First {\n" +
            "    void xxx(a)\n" +
            "    void xxx()\n" +
            "}",
            "public class Second implements First {\n" +
            "    public void other() {\n" +
            "        xxx(a)\n" +
            "    }\n" +
            "    public void xxx() {\n" +
            "        xxx()\n" +
            "    }\n" +
            "    void xxx(a) {\n" +
            "        xxx(a,b)\n" +
            "    }\n" +
            "}",
            false, 0, "xxx");
    }

    @Test
    public void testOverloadedMethodReferences3() throws Exception {
        // should match on the method reference with precise # of args as well as method reference with unmatched number of args
        createUnit("Sub",
            "interface Sub extends First { void xxx(a) }");
        doTestForTwoMethodReferences(
            "interface First {\n" +
            "    void xxx(a)\n" +
            "    void xxx()\n" +
            "}",
            "public class Second implements Sub {\n" +
            "    public void other() {\n" +
            "        xxx(a)\n" +
            "    }\n" +
            "    public void xxx() {\n" +
            "        xxx()\n" +
            "    }\n" +
            "    void xxx(a) {\n" +
            "        xxx(a,b)\n" +
            "    }\n" +
            "}",
            false, 0, "xxx");
    }

    @Test
    public void testOverloadedMethodReferences4() throws Exception {
        // should match on the method reference with precise # of args as well as method reference with unmatched number of args
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
            "        f.xxx(a,b,c)\n" +
            "    }\n" +
            "    public void xxx() {\n" +
            "        xxx(a)\n" +
            "        xxx(a,b,c)\n" +
            "        Sub s\n" +
            "        s.xxx(a)\n" +
            "        s.xxx(a,b,c)\n" +
            "    }\n" +
            "    void xxx(a) {\n" +
            "        Sub s\n" +
            "        s.xxx(a,b)\n" +
            "    }\n" +
            "}",
            false, 0, "xxx");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/373
    public void testOverloadedMethodReferences5() throws Exception {
        doTestForTwoMethodReferences(
            "class First {\n" +
            "  URL doSomething(String s, URL u) { }\n" + // search for references
            "  URL doSomething(Integer i, URL u) { }\n" +
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

    @Test // https://github.com/groovy/groovy-eclipse/issues/373
    public void testOverloadedMethodReferences6() throws Exception {
        doTestForTwoMethodReferences(
            "class First {\n" +
            "  URL doSomething(Integer i, URL u) { }\n" + // search for references
            "  URL doSomething(String s, URL u) { }\n" +
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
            true, 2, "doSomething");// "true, 2" says both matches are in xxx() (aka Second.children[2])
    }

    @Test
    public void testMethodWithDefaultParameters1() throws Exception {
        doTestForTwoMethodReferences(
            "class First {\n" +
            "    void xxx(a, b = 9) { }\n" +
            "    void xxx(a, b, c) { }\n" +
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
            "    void xxx(a, b = 9) { }\n" +
            "    void xxx(a, b, c) { }\n" +
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
            "        f.xxx\n" +
            "    }\n" +
            "}",
            false, 0, "xxx");
    }

    @Test
    public void testConstructorReferenceSearch() throws Exception {
        String groovyContents =
            "package p\n" +
            "class Foo {\n" +
            "  Foo() {\n" +
            "    new Foo()\n" +
            "  }\n" +
            "  Foo(a) {\n" +
            "    new Foo(a)\n" +
            "  }\n" +
            "}";
        String otherContents =
            "import p.Foo\n" +
            "new Foo()\n" +
            "new Foo(a)\n" +
            "new p.Foo()\n" +
            "new p.Foo(a)\n";

        GroovyCompilationUnit first = createUnit("p", "Foo", groovyContents);
        createUnit("", "Other", otherContents);

        IMethod constructor = first.getType("Foo").getMethods()[0];
        new SearchEngine().search(
            SearchPattern.createPattern(constructor, IJavaSearchConstants.REFERENCES),
            new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
            SearchEngine.createJavaSearchScope(new IJavaElement[] {first.getPackageFragmentRoot()}, false),
            searchRequestor, new NullProgressMonitor());

        List<SearchMatch> matches = searchRequestor.getMatches();
        assertEquals("Incorrect number of matches:\n" + matches, 6, matches.size());

        // two from Foo and two from other
        int fooCnt = 0, otherCnt = 0;
        for (SearchMatch match : matches) {
            if (match.getElement() instanceof IMethod) {
                if (((IMethod) match.getElement()).getResource().getName().equals("Foo.groovy")) {
                    fooCnt += 1;
                } else if (((IMethod) match.getElement()).getResource().getName().equals("Other.groovy")) {
                    otherCnt += 1;
                }
            }
        }
        assertEquals("Should have found 2 matches in Foo.groovy", 2, fooCnt);
        assertEquals("Should have found 4 matches in Other.groovy", 4, otherCnt);
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
        GroovyCompilationUnit unit = createUnit("Foo", contents);

        IMethod method = unit.getType("Foo").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(unit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(1, searchRequestor.getMatches().size());
        assertLocation(searchRequestor.getMatch(0), contents.lastIndexOf("bar"), "bar".length());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/489
    public void testExplicitPropertySetterSearch() throws Exception {
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
            "bar.string = null\n" + // potential
            "def str = bar.string" +
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
        assertEquals(SearchMatch.A_INACCURATE, matches.get(3).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).indexOf("string = "), matches.get(3).getOffset());
        assertEquals(SearchMatch.A_INACCURATE, matches.get(4).getAccuracy());
        assertEquals(String.valueOf(baz.getContents()).lastIndexOf("setString"), matches.get(4).getOffset());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/402
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
            "package foo\n" +
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
