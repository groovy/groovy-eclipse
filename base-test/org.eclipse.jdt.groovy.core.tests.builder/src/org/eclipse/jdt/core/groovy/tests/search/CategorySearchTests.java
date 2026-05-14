/*
 * Copyright 2009-2024 the original author or authors.
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

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.junit.Test;

/**
 * Tests searches that make use of categories.
 */
public final class CategorySearchTests extends SearchTestSuite {

    //@formatter:off
    private static String CATEGORY_CLASSES =
        "class CatTarget {\n" +
        "  CatTarget self\n" +
        "}\n" +
        "class Cat {\n" +
        "  static String printMessage(CatTarget e, msg) {\n" +
        "    print msg\n" +
        "  }\n" +
        "}\n" +
        "class Cat2 {\n" +
        "  static String printMessage2(CatTarget e, msg) {\n" +
        "    print msg\n" +
        "  }\n" +
        "}\n" +
        "class Cat3 extends Cat {\n" +
        "}";

    private static String SIMPLE_CATEGORY =
        "use (Cat) {\n" +
        "  new CatTarget().printMessage 'jello'\n" +
        "  def x = new CatTarget()\n" +
        "  x.printMessage 'jello'\n" +
        "  x.self = x\n" +
        "  x.printMessage 'jello'\n" +
        "  Cat.printMessage x, 'jello'\n" +
        "}";

    private static String CATEGORY_WITH_SUBTYPE =
        "class Sub extends CatTarget { }\n" +
        "use (Cat) {\n" +
        "  new Sub().printMessage 'jello'\n" +
        "  def x = new Sub()\n" +
        "  x.printMessage 'jello'\n" +
        "  x.self = x\n" +
        "  x.printMessage 'jello'\n" +
        "  Cat.printMessage x, 'jello'\n" +
        "}";

    private static String CATEGORY_ASSIGNED =
        "def y = Cat\n" +
        "use (y) {\n" +
        "  new CatTarget().printMessage 'jello'\n" +
        "  def x = new CatTarget()\n" +
        "  x.printMessage 'jello'\n" +
        "  x.self = x\n" +
        "  x.printMessage 'jello'\n" +
        "  y.printMessage x, 'jello'\n" +
        "}";

    private static String CATEGORY_MULTIPLE_OUTER =
        "use (Cat) { use (Cat2) {\n" +
        "  new CatTarget().printMessage 'jello'\n" +
        "  def x = new CatTarget()\n" +
        "  x.printMessage 'jello'\n" +
        "  x.self = x\n" +
        "  x.printMessage 'jello'\n" +
        "  Cat.printMessage x, 'jello'\n" +
        "} }";

    private static String CATEGORY_MULTIPLE_INNER =
        "use (Cat2) { use (Cat) {\n" +
        "  new CatTarget().printMessage 'jello'\n" +
        "  def x = new CatTarget()\n" +
        "  x.printMessage 'jello'\n" +
        "  x.self = x\n" +
        "  x.printMessage 'jello'\n" +
        "  Cat.printMessage x, 'jello'\n" +
        "} }";
    //@formatter:on

    @Test
    public void testCategorySearch1() throws Exception {
        doCategorySearchTest(SIMPLE_CATEGORY, 4);
    }

    @Test
    public void testCategorySearch2() throws Exception {
        doCategorySearchTest(CATEGORY_WITH_SUBTYPE, 4);
    }

    @Test
    public void testCategorySearch3() throws Exception {
        doCategorySearchTest(CATEGORY_ASSIGNED, 4);
    }

    @Test
    public void testCategorySearch4() throws Exception {
        doCategorySearchTest(CATEGORY_MULTIPLE_INNER, 4);
    }

    @Test
    public void testCategorySearch5() throws Exception {
        doCategorySearchTest(CATEGORY_MULTIPLE_OUTER, 4);
    }

    @Test
    public void testCategorySearch6() throws Exception {
        doCategorySearchTest("use (Cat) {}\n" +
            "new CatTarget().printMessage('')\n", 0);
    }

    @Test
    public void testCategorySearch7() throws Exception {
        doCategorySearchTest("use (Cat3) {\n" +
            "  new CatTarget().printMessage('')\n" +
            "}", 1);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1545
    public void testCategorySearch8() throws Exception {
        GroovyCompilationUnit unit = createUnit("stuff",
            "class Cat {\n" +
            "  static int getLength(Foo foo) {}\n" +
            "}\n" +
            "class Foo {\n" +
            "  int length() {}\n" +
            "}\n" +
            "use (Cat) {\n" +
            "  def foo = new Foo()\n" +
            "  foo.length()\n" + // no
            "  foo.length\n" + // yes
            "}\n");

        IMethod searchFor = unit.getType("Cat").getMethods()[0];
        assertEquals("getLength", searchFor.getElementName());
        List<SearchMatch> matches = search(
            SearchPattern.createPattern(searchFor, IJavaSearchConstants.REFERENCES),
            SearchEngine.createJavaSearchScope(new IJavaElement[] {unit.getPackageFragmentRoot()}));

        assertEquals(1, matches.size());
        assertEquals(SearchMatch.A_ACCURATE, matches.get(0).getAccuracy());
        assertEquals(String.valueOf(unit.getContents()).lastIndexOf("length"), matches.get(0).getOffset());
    }

    //--------------------------------------------------------------------------

    private void doCategorySearchTest(String contents, int numMatches) throws JavaModelException {
        checkMatches(findMatches(contents), numMatches, contents);
    }

    private List<SearchMatch> findMatches(String contents) throws JavaModelException {
        GroovyCompilationUnit catUnit = createUnit("Cat", CATEGORY_CLASSES);
        GroovyCompilationUnit unit = createUnit("script", contents);
        expectingNoProblems();

        IMethod searchFor = (IMethod) catUnit.getElementAt(CATEGORY_CLASSES.indexOf("printMessage"));
        assertEquals("Wrong IJavaElement found: " + searchFor, "printMessage", searchFor.getElementName());
        return search(SearchPattern.createPattern(searchFor, IJavaSearchConstants.REFERENCES), unit);
    }

    private void checkMatches(List<SearchMatch> matches, int nExpected, String contents) {
        assertEquals("Wrong number of matches found:\n" + toString(matches), nExpected, matches.size());
        if (nExpected > 0) {
            Iterator<SearchMatch> it = matches.iterator();
            Pattern p = Pattern.compile("printMessage");
            Matcher m = p.matcher(contents);
            while (m.find()) {
                SearchMatch match = it.next();
                assertEquals("Wrong starting location for " + toString(match), m.start(), match.getOffset());
                assertEquals("Wrong length for " + toString(match), "printMessage".length(), match.getLength());
            }
        }
    }
}
