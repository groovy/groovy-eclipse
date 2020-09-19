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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.BinaryMember;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public final class BinarySearchTests extends SearchTestSuite {

    //@formatter:off
    private String groovyClassContents =
        "package pack\n" +
        "class OtherClass { }\n" +
        "class AGroovyClass {\n" +
        "        String name_1\n" +
        "        int age_1\n" +
        "        def referencedInInitializer() { }\n" +
        "        def fieldInInitializer\n" +
        "        \n" +
        "        def doit() {\n" +
        "                println name_1 + age_1\n" +
        "                AGroovyClass\n" +
        "                OtherClass\n" +
        "                doit()\n" +
        "                def aClosure = {\n" +
        "                        println name_1 + age_1\n" +
        "                        AGroovyClass\n" +
        "                        OtherClass\n" +
        "                        doit()\n" +
        "                }\n" +
        "        }\n" +
        "        { \n" +
        "                referencedInInitializer() \n" +
        "                fieldInInitializer\n" +
        "        }\n" +
        "}\n";
    private String groovyClassContents2 =
        "package pack\n" +
        "class AnotherGroovyClass {\n" +
        "        def doit() {\n" +
        "                println new AGroovyClass().name_1 + new AGroovyClass().age_1\n" +
        "                AGroovyClass\n" +
        "                OtherClass\n" +
        "                new AGroovyClass().doit()\n" +
        "                def aClosure = {\n" +
        "                        println new AGroovyClass().name_1 + new AGroovyClass().age_1\n" +
        "                        AGroovyClass\n" +
        "                        OtherClass\n" +
        "                        new AGroovyClass().doit()\n" +
        "                }\n" +
        "        }\n" +
        "        { \n" +
        "                new AGroovyClass().referencedInInitializer() \n" +
        "                new AGroovyClass().fieldInInitializer\n" +
        "        }\n" +
        "}\n";
    //@formatter:on

    private IJavaProject javaProject;

    @Before
    public void setUp() throws Exception {
        Path libDir = new Path(FileLocator.resolve(Platform.getBundle("org.eclipse.jdt.groovy.core.tests.builder").getEntry("lib")).getFile());
        env.addEntry(project.getFullPath(), JavaCore.newLibraryEntry(libDir.append("binGroovySearch.jar"), libDir.append("binGroovySearchSrc.zip"), null));

        javaProject = env.getJavaProject(project.getName());
        // overwrite the contents vars with the actual contents
        groovyClassContents = javaProject.findType("pack.AGroovyClass").getTypeRoot().getBuffer().getContents();
        groovyClassContents2 = javaProject.findType("pack.AnotherGroovyClass").getTypeRoot().getBuffer().getContents();
    }

    @After
    public void tearDown() throws Exception {
        javaProject = null;
    }

    private List<SearchMatch> performSearch(final IJavaElement javaElement) throws Exception {
        assertTrue("Expected binary member, but got: " + javaElement == null ? null : javaElement.getClass().getName(), javaElement instanceof BinaryMember);
        SearchPattern pattern  = SearchPattern.createPattern(javaElement, IJavaSearchConstants.REFERENCES);
        IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {javaProject});
        return search(pattern, scope);
    }

    private void assertMatches(final String toFind, final List<SearchMatch> matches, final int allMatches, final int firstMatches) {
        String matchesString = toString(matches);
        if (matches.size() != allMatches) {
            fail("Expecting " + allMatches + " matches, but found " + matches.size() + "\n" + matchesString);
        }
        int currIndex = groovyClassContents.indexOf("def doit") + "def doit".length();
        for (int i = 0; i < firstMatches; i += 1) {
            SearchMatch match = matches.get(i);
            currIndex = groovyClassContents.indexOf(toFind, currIndex);
            assertEquals("Invalid start position in match " + i + "\n" + matchesString, currIndex, match.getOffset());
            assertEquals("Invalid length in match " + i + "\n" + matchesString, toFind.length(), match.getLength());
            currIndex += toFind.length();
        }
        currIndex = groovyClassContents2.indexOf("def doit") + "def doit".length();
        for (int i = firstMatches; i < allMatches; i += 1) {
            SearchMatch match = matches.get(i);
            currIndex = groovyClassContents2.indexOf(toFind, currIndex);
            assertEquals("Invalid start position in match " + i + "\n" + matchesString, currIndex, match.getOffset());
            assertEquals("Invalid length in match " + i + "\n" + matchesString, toFind.length(), match.getLength());
            currIndex += toFind.length();
        }
    }

    //

    @Test
    public void testClassDecl1() throws Exception {
        IType type = javaProject.findType("pack.AGroovyClass");
        assertMatches("AGroovyClass", performSearch(type), 12, 2);
    }

    @Test
    public void testClassDecl2() throws Exception {
        IType type = javaProject.findType("pack.OtherClass");
        assertMatches("OtherClass", performSearch(type), 4, 2);
    }

    @Test
    public void testFieldDecl1() throws Exception {
        IType type = javaProject.findType("pack.AGroovyClass");
        String toFind = "age_1";
        IField field = type.getField(toFind);
        assertMatches(toFind, performSearch(field), 2, 2); // all was 4, but in binary synthetic accessor is indistinguishable from source method
    }

    @Test
    public void testFieldDecl2() throws Exception {
        IType type = javaProject.findType("pack.AGroovyClass");
        String toFind = "name_1";
        IField field = type.getField(toFind);
        assertMatches(toFind, performSearch(field), 2, 2); // all was 4, but in binary synthetic accessor is indistinguishable from source method
    }

    @Test
    public void testMethodDecl() throws Exception {
        IType type = javaProject.findType("pack.AGroovyClass");
        String toFind = "doit";
        IMethod method = type.getMethod(toFind, new String[0]);
        assertMatches(toFind, performSearch(method), 4, 2);
    }

    @Test
    public void testFieldRefInInitializer() throws Exception {
        IType type = javaProject.findType("pack.AGroovyClass");
        String toFind = "fieldInInitializer";
        IField method = type.getField(toFind);
        assertMatches(toFind, performSearch(method), 1, 1); // all was 2, but in binary synthetic accessor is indistinguishable from source method
    }

    @Test
    public void testMethodRefInInitializer() throws Exception {
        IType type = javaProject.findType("pack.AGroovyClass");
        String toFind = "referencedInInitializer";
        IMethod method = type.getMethod(toFind, new String[0]);
        assertMatches(toFind, performSearch(method), 2, 1);
    }
}
