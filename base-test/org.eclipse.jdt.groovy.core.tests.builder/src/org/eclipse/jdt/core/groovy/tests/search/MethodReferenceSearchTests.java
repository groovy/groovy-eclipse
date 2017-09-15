/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.groovy.tests.MockPossibleMatch;
import org.eclipse.jdt.core.groovy.tests.MockSearchRequestor;
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
    public void testMethodWithPropertyOverride1() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { String getSomething(); }");

        String contents = "class Impl implements Face {\n" +
            "  String something\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.DECLARATIONS);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(1, searchRequestor.getMatches().size());
        assertLocation(searchRequestor.getMatch(0), contents.indexOf("something"), "something".length());
    }

    @Test
    public void testMethodWithPropertyOverride1a() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { String getSomething(String x); }");

        String contents = "class Impl implements Face {\n" +
            "  String something\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.DECLARATIONS);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(0, searchRequestor.getMatches().size());
    }

    @Test
    public void testMethodWithPropertyOverride2() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { void setSomething(String v); }");

        String contents = "class Impl implements Face {\n" +
            "  String something\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.DECLARATIONS);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(1, searchRequestor.getMatches().size());
        assertLocation(searchRequestor.getMatch(0), contents.indexOf("something"), "something".length());
    }

    @Test
    public void testMethodWithPropertyOverride2a() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { void setSomething(String v, Object x); }");

        String contents = "class Impl implements Face {\n" +
            "  String something\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.DECLARATIONS);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(0, searchRequestor.getMatches().size());
    }

    @Test
    public void testMethodWithPropertyOverride3() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { boolean isSomething(); }");

        String contents = "class Impl implements Face {\n" +
            "  String something\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.DECLARATIONS);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(1, searchRequestor.getMatches().size());
        assertLocation(searchRequestor.getMatch(0), contents.indexOf("something"), "something".length());
    }

    @Test
    public void testMethodWithPropertyOverride3a() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { String isSomething(String x); }");

        String contents = "class Impl implements Face {\n" +
            "  String something\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.DECLARATIONS);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(0, searchRequestor.getMatches().size());
    }

    @Test
    public void testMethodWithPropertyReference1() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { String getSomething(); }");

        String contents = "class Impl {\n" +
            "  static void main(String... args) {\n" +
            "    Face f = null\n" +
            "    f.something\n" +
            "  }\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(1, searchRequestor.getMatches().size());
        assertLocation(searchRequestor.getMatch(0), contents.indexOf("something"), "something".length());
    }

    @Test
    public void testMethodWithPropertyReference1a() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { String getSomething(String x); }");

        String contents = "class Impl {\n" +
            "  static void main(String... args) {\n" +
            "    Face f = null\n" +
            "    f.something\n" +
            "  }\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(0, searchRequestor.getMatches().size());
    }

    @Test
    public void testMethodWithPropertyReference2() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { void setSomething(String x); }");

        String contents = "class Impl {\n" +
            "  static void main(String... args) {\n" +
            "    Face f = null\n" +
            "    f.something = null\n" +
            "  }\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(1, searchRequestor.getMatches().size());
        assertLocation(searchRequestor.getMatch(0), contents.indexOf("something"), "something".length());
    }

    @Test
    public void testMethodWithPropertyReference2a() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { void setSomething(String x, String y); }");

        String contents = "class Impl {\n" +
            "  static void main(String... args) {\n" +
            "    Face f = null\n" +
            "    f.something = null\n" +
            "  }\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(0, searchRequestor.getMatches().size());
    }

    @Test
    public void testMethodWithPropertyReference2b() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { void setSomething(String x); }");

        String contents = "class Impl {\n" +
            "  static void main(String... args) {\n" +
            "    Face f = null\n" +
            "    def s = f.something\n" +
            "  }\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(0, searchRequestor.getMatches().size());
    }

    @Test
    public void testMethodWithPropertyReference3() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { boolean isSomething(); }");

        String contents = "class Impl {\n" +
            "  static void main(String... args) {\n" +
            "    Face f = null\n" +
            "    f.something\n" +
            "  }\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(1, searchRequestor.getMatches().size());
        assertLocation(searchRequestor.getMatch(0), contents.indexOf("something"), "something".length());
    }

    @Test
    public void testMethodWithPropertyReference3a() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { boolean isSomething(String x); }");

        String contents = "class Impl {\n" +
            "  static void main(String... args) {\n" +
            "    Face f = null\n" +
            "    f.something\n" +
            "  }\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(0, searchRequestor.getMatches().size());
    }

    @Test
    public void testMethodWithPropertyReference4() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { String getSomething(); }");

        String contents = "class Impl implements Face {\n" +
            "  String something\n" +
            "  static void main(String... args) {\n" +
            "    Impl i = null\n" +
            "    i.something\n" +
            "  }\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(1, searchRequestor.getMatches().size());
        assertLocation(searchRequestor.getMatch(0), contents.lastIndexOf("something"), "something".length());
    }

    @Test
    public void testMethodWithPropertyReference4a() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { String getSomething(String x); }");

        String contents = "class Impl implements Face {\n" +
            "  String something\n" +
            "  static void main(String... args) {\n" +
            "    Impl i = null\n" +
            "    i.something\n" +
            "  }\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(0, searchRequestor.getMatches().size());
    }

    @Test
    public void testMethodWithPropertyReference5() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { void setSomething(String x); }");

        String contents = "class Impl implements Face {\n" +
            "  String something\n" +
            "  static void main(String... args) {\n" +
            "    Impl i = null\n" +
            "    i.something = null\n" +
            "  }\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(1, searchRequestor.getMatches().size());
        assertLocation(searchRequestor.getMatch(0), contents.lastIndexOf("something"), "something".length());
    }

    @Test
    public void testMethodWithPropertyReference5a() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { void setSomething(String x, String y); }");

        String contents = "class Impl implements Face {\n" +
            "  String something\n" +
            "  static void main(String... args) {\n" +
            "    Impl i = null\n" +
            "    i.something = null\n" +
            "  }\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(0, searchRequestor.getMatches().size());
    }

    @Test
    public void testMethodWithPropertyReference5b() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { void setSomething(String x); }");

        String contents = "class Impl implements Face {\n" +
            "  String something\n" +
            "  static void main(String... args) {\n" +
            "    Impl i = null\n" +
            "    def s = i.something\n" +
            "  }\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(0, searchRequestor.getMatches().size());
    }

    @Test
    public void testMethodWithPropertyReference5c() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { void setSomething(String x); }");

        String contents = "class Impl implements Face {\n" +
            "  final String something\n" + // read-only property
            "  static void main(String... args) {\n" +
            "    Impl i = null\n" +
            "    i.something = ''\n" +
            "  }\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(0, searchRequestor.getMatches().size());
    }

    @Test
    public void testMethodWithPropertyReference6() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { boolean isSomething(); }");

        String contents = "class Impl implements Face {\n" +
            "  boolean something\n" +
            "  static void main(String... args) {\n" +
            "    Impl i = null\n" +
            "    i.something\n" +
            "  }\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(1, searchRequestor.getMatches().size());
        assertLocation(searchRequestor.getMatch(0), contents.lastIndexOf("something"), "something".length());
    }

    @Test
    public void testMethodWithPropertyReference6a() throws Exception {
        GroovyCompilationUnit faceUnit = createUnit("Face", "interface Face { boolean isSomething(String x); }");

        String contents = "class Impl implements Face {\n" +
            "  boolean something\n" +
            "  static void main(String... args) {\n" +
            "    Impl i = null\n" +
            "    i.something\n" +
            "  }\n" +
            "}\n";
        GroovyCompilationUnit implUnit = createUnit("Impl", contents);

        IMethod method = faceUnit.getType("Face").getMethods()[0];
        MockPossibleMatch match = new MockPossibleMatch(implUnit);
        SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        factory.createVisitor(match).visitCompilationUnit(new TypeRequestorFactory().createRequestor(match, pattern, searchRequestor));

        assertEquals(0, searchRequestor.getMatches().size());
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
        MockSearchRequestor requestor = new MockSearchRequestor();
        SearchEngine engine = new SearchEngine();
        engine.search(SearchPattern.createPattern(constructor, IJavaSearchConstants.REFERENCES),
            new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
            SearchEngine.createJavaSearchScope(new IJavaElement[] {first.getPackageFragmentRoot()}, false), requestor,
            new NullProgressMonitor());
        List<SearchMatch> matches = requestor.getMatches();
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
