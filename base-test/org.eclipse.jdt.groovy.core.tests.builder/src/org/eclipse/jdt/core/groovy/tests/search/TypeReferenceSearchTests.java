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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.junit.Test;

/**
 * Tests the groovy-specific type referencing search support.
 */
public final class TypeReferenceSearchTests extends SearchTestSuite {

    @Test
    public void testSearchForTypesScript1() throws Exception {
        doTestForTwoInScript("First f = new First()");
    }

    @Test
    public void testSearchForTypesScript2() throws Exception {
        doTestForTwoInScript("First.class\nFirst.class");
    }

    @Test
    public void testSearchForTypesScript3() throws Exception {
        doTestForTwoInScript("[First, First]");
    }

    @Test
    public void testSearchForTypesScript4() throws Exception {
        // the key of a map is interpreted as a string
        // so don't put 'First' there
        doTestForTwoInScript("def x = [a : First]\nx[First.class]");
    }

    @Test
    public void testSearchForTypesScript5() throws Exception {
        doTestForTwoInScript("x(First, First.class)");
    }

    @Test
    public void testSearchForTypesScript6() throws Exception {
        // note that in "new First[ new First() ]", the first 'new First' is removed
        // by the AntlrPluginParser and so there is no way to search for it
        //doTestForTwoInScript("new First[ new First() ]");
        doTestForTwoInScript("[ new First(), First ]");
    }

    @Test
    public void testSearchForTypesClosure1() throws Exception {
        doTestForTwoInScript("{ First first, First second -> print first; print second;}");
    }

    @Test
    public void testSearchForTypesClosure2() throws Exception {
        doTestForTwoInScript("def x = {\n First first = new First()\n}");
    }

    @Test
    public void testSearchForTypesClosure3() throws Exception {
        doTestForTwoInScript("def x = {\n First.class\n First.class\n}");
    }

    @Test
    public void testSearchForTypesClass1() throws Exception {
        doTestForTwoInClass("class Second extends First {\n First x\n}");
    }

    @Test
    public void testSearchForTypesClass2() throws Exception {
        doTestForTwoInClass("class Second extends First { First x() {}\n}");
    }

    @Test
    public void testSearchForTypesClass3() throws Exception {
        doTestForTwoInClass("class Second extends First { def x(First y) {}\n}");
    }

    @Test
    public void testSearchForTypesClass4() throws Exception {
        doTestForTwoInClass("class Second extends First { def x(First ... y) {}\n}");
    }

    @Test
    public void testSearchForTypesClass5() throws Exception {
        doTestForTwoTypeReferences(FIRST_CONTENTS_CLASS, "class Second extends First { def x(y = new First()) {}\n}", false, 0);
    }

    @Test
    public void testSearchForTypesClass6() throws Exception {
        doTestForTwoTypeReferences(FIRST_CONTENTS_INTERFACE, "class Second implements First { def x(First y) {}\n}", false, 0);
    }

    @Test
    public void testSearchForTypesClass7() throws Exception {
        createUnit("other", "First", "class First {}");
        doTestForTwoInClass("class Second extends First {\n" + // yes
            "  def x() {\n" +
            "    y = new other.First()\n" + // no
            "    y = new First()\n" + // yes
            "  }\n" +
            "}\n");
    }

    @Test
    public void testSearchForTypesArray1() throws Exception {
        doTestForTwoInScript("First[] f = {\n First[] h -> h\n}");
    }

    @Test // GRECLIPSE-650
    public void testFindClassDeclaration() throws Exception {
        String firstContents = "class First {\n First x\n}";
        String secondContents = "class Second extends First {}";
        List<SearchMatch> matches = getAllMatches(firstContents, secondContents);
        assertEquals("Should find First 2 times", 2, matches.size());
        SearchMatch match = matches.get(0);
        int start = match.getOffset();
        int end = start + match.getLength();
        assertEquals("Invalid location", "First", firstContents.substring(start, end));

        match = matches.get(1);
        start = match.getOffset();
        end = start + match.getLength();
        assertEquals("Invalid location", "First", secondContents.substring(start, end));
    }

    /**
     * Tests whether queries looking for some type declaration with a name pattern like '*Tests' works
     * correctly.
     */
    @Test
    public void testFindClassDeclarationWithPattern() throws Exception {
        //Code in here directly inspired and mostly copied from
        //com.springsource.sts.grails.core.junit.Grails20AwareTestFinder
        //Specifically exercises the exact kind of searching behavior needed to find
        //Grails 2.0 tests that do *not* have @TestFor annotations.

        int matchRule = SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE;
        SearchPattern testPattern = SearchPattern.createPattern("*Tests", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, matchRule);
        GroovyCompilationUnit songTests = createUnit("gtunes", "SongTests",
                "package gtunes\n" +
                "\n" +
                "final class SongTests {" +
                "    def testSomething() {\n" +
                "       println 'testing'\n" +
                "    }\n" +
                "}");
        GroovyCompilationUnit weirdTests = createUnit("gtunes", "Song2tests",
                "package gtunes\n" +
                "\n" +
                "class Song2tests {" +
                "    SongTests theOtherTests\n" + //Shouldn't find
                "    def testSomethingElse() {\n" +
                "       println 'testing'\n" +
                "    }\n" +
                "}");
        GroovyCompilationUnit artistTests = createUnit("gtunes", "ArtistTests",
                "package gtunes\n" +
                "\n" +
                "final class ArtistTests {" +
                "    def testSomething() {\n" +
                "       println 'testing'\n" +
                "    }\n" +
                "}");
        IJavaProject javaProject = JavaCore.create(project);
        IType songTestsType = javaProject.findType("gtunes.SongTests");
        assertNotNull(songTestsType);
        IType artistTestsType = javaProject.findType("gtunes.ArtistTests");
        assertNotNull(artistTestsType);

        SearchParticipant[] searchParticipants = new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};

        final List<Object> result = new ArrayList<>();
        IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {songTests, weirdTests, artistTests}, IJavaSearchScope.SOURCES);

        SearchRequestor requestor = new SearchRequestor() {
            @Override
            public void acceptSearchMatch(SearchMatch match) throws CoreException {
                Object element = match.getElement();
                result.add(element);
            }
        };
        new SearchEngine().search(testPattern, searchParticipants, scope, requestor, new NullProgressMonitor());

        assertEquals("Number of results found", 2, result.size());

        assertElements(new HashSet<>(result), songTestsType, artistTestsType);
    }

    @Test // GRECLIPSE-628
    public void testShouldntFindClassDeclarationInScript() throws Exception {
        String firstContents = "print 'me'";
        String secondContents = "print 'me'";
        List<SearchMatch> matches = getAllMatches(firstContents, secondContents);
        assertEquals("Should find no matches", 0, matches.size());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/468
    public void testCoercion1() throws Exception {
        String firstContents =
            "package a\n" +
            "interface First {\n" +
            "  void meth();\n" +
            "}\n";

        String secondContents =
            "package a\n" +
            "class Second {\n" +
            "  def m() {\n" +
            "    return {->\n" +
            "    } as First\n" +
            "  }\n" +
            "}\n";

        List<SearchMatch> matches = getAllMatches(firstContents, secondContents, "a", "a", false);
        assertEquals("Wrong count", 1, matches.size());

        SearchMatch match = matches.get(0);
        assertEquals("Wrong length", "First".length(), match.getLength());
        assertEquals("Wrong offset", secondContents.indexOf("First"), match.getOffset());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/442
    public void testGenerics1() throws Exception {
        String firstContents =
            "package a\n" +
            "class First {\n" +
            "}\n";

        String secondContents =
            "package a\n" +
            "class Second {\n" +
            "  List<First> firsts\n" +
            "}\n";

        List<SearchMatch> matches = getAllMatches(firstContents, secondContents, "a", "a", false);
        assertEquals("Wrong count", 1, matches.size());

        SearchMatch match = matches.get(0);
        assertEquals("Wrong length", "First".length(), match.getLength());
        assertEquals("Wrong offset", secondContents.indexOf("First"), match.getOffset());
    }

    @Test
    public void testInnerTypes1() throws Exception {
        String firstContents =
            "class Other {\n" +
            "  class First {}\n" +
            "}";

        String secondContents =
            "import Other.First\n" +
            "Map<First, ? extends First> h\n" +
            "Other.First j\n" +
            "First i";

        String name = "First";
        int len = name.length();

        List<SearchMatch> matches = getAllMatches(firstContents, secondContents, true);
        assertEquals("Wrong number of matches found:\n" + matches, 5, matches.size());

        int start = secondContents.indexOf("First");
        SearchMatch match = matches.get(0);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());

        start = secondContents.indexOf("First", start + 1);
        match = matches.get(1);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());

        start = secondContents.indexOf("First", start + 1);
        match = matches.get(2);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());

        start = secondContents.indexOf("First", start + 1);
        match = matches.get(3);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());

        start = secondContents.indexOf("First", start + 1);
        match = matches.get(4);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());
    }

    @Test
    public void testInnerTypes2() throws Exception {
        String firstContents =
            "package p\n" +
            "class Other {\n" +
            "  class First {}\n" +
            "}";

        String secondContents =
            "package q\n" +
            "import p.Other.First\n" +
            "Map<First, ? extends First> h\n" +
            "p.Other.First j\n" +
            "First i";

        String name = "First";
        int len = name.length();

        List<SearchMatch> matches = getAllMatches(firstContents, secondContents, "p", "q", true);
        assertEquals("Wrong number of matches found:\n" + matches, 5, matches.size());

        int start = secondContents.indexOf("First");
        SearchMatch match = matches.get(0);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());

        start = secondContents.indexOf("First", start + 1);
        match = matches.get(1);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());

        start = secondContents.indexOf("First", start + 1);
        match = matches.get(2);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());

        start = secondContents.indexOf("First", start + 1);
        match = matches.get(3);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());

        start = secondContents.indexOf("First", start + 1);
        match = matches.get(4);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());
    }

    @Test
    public void testInnerTypes3() throws Exception {
        String firstContents =
            "package p\n" +
            "class Other {\n" +
            "  class First {}\n" +
            "}";

        String secondContents =
            "package q\n" +
            "import p.Other\n" +
            "Map<Other.First, ? extends Other.First> h\n" +
            "p.Other.First j\n" +
            "Other.First i";

        String name = "First";
        int len = name.length();

        List<SearchMatch> matches = getAllMatches(firstContents, secondContents, "p", "q", true);
        assertEquals("Wrong number of matches found:\n" + matches, 4, matches.size());

        int start = secondContents.indexOf("First");
        SearchMatch match = matches.get(0);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());

        start = secondContents.indexOf("First", start + 1);
        match = matches.get(1);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());

        start = secondContents.indexOf("First", start + 1);
        match = matches.get(2);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());

        start = secondContents.indexOf("First", start + 1);
        match = matches.get(3);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());
    }

    @Test
    public void testConstructorWithDefaultArgsInCompileStatic() throws Exception {
        String firstContents =
                "package p\n" +
                "class First {\n" +
                "    Class name\n" +
                "}\n";
        String secondContents =
                "package q\n" +
                "import p.*\n" +
                "import groovy.transform.CompileStatic\n" +
                "\n" +
                "@CompileStatic\n" +
                "class Foo {\n" +
                "  void apply() {\n" +
                "      new First([name: First])\n" +
                "  }\n" +
                "}";
        List<SearchMatch> matches = getAllMatches(firstContents, secondContents, "p", "q", true);
        int lastMatch = 0;
        for (SearchMatch searchMatch : matches) {
            int start = secondContents.indexOf("First", lastMatch);
            assertEquals("Wrong offset " + searchMatch, start, searchMatch.getOffset());
            assertEquals("Wrong length " + searchMatch, "First".length(), searchMatch.getLength());
            lastMatch = start + 1;
        }
        assertEquals("Wrong number of matches found\n" + matches, 2, matches.size());
    }

    //--------------------------------------------------------------------------

    private static void assertElements(Set<Object> actualSet, Object... expecteds) {
        Set<Object> expectedSet = new HashSet<>(Arrays.asList(expecteds));
        StringBuilder msg = new StringBuilder();
        for (Object expected : expectedSet) {
            if (!actualSet.contains(expected)) {
                msg.append("Expected but not found: " + expected + "\n");
            }
        }
        for (Object actual : actualSet) {
            if (!expectedSet.contains(actual)) {
                msg.append("Found but not expected: " + actual + "\n");
            }
        }
        if (!"".equals(msg.toString())) {
            fail(msg.toString());
        }
    }

    private void doTestForTwoInScript(String secondContents) throws Exception {
        doTestForTwoTypeReferences(FIRST_CONTENTS_CLASS, secondContents, true, 3);
    }

    private void doTestForTwoInClass(String secondContents) throws Exception {
        doTestForTwoTypeReferences(FIRST_CONTENTS_CLASS, secondContents, false, 0);
    }
}
