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
package org.codehaus.groovy.eclipse.core.test

import org.codehaus.groovy.eclipse.core.search.ISearchRequestor
import org.codehaus.groovy.eclipse.core.search.SyntheticAccessorSearchRequestor
import org.codehaus.groovy.eclipse.test.EclipseTestCase
import org.eclipse.jdt.core.IField
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.search.SearchMatch
import org.junit.Assert
import org.junit.Before
import org.junit.Test

final class SyntheticMemberSearchTests extends EclipseTestCase {

    private IType gType

    private static class TestSearchRequestor implements ISearchRequestor {
        List<SearchMatch> matches = []
        public void acceptMatch(SearchMatch match) {
            matches.add(match)
        }
    }

    @Before
    void setUp() {
        gType = testProject.createGroovyTypeAndPackage("p", "G.groovy",
                "class G {\n" +
                "  def prop\n" +
                "  public def notProp\n" +
                "  def getExplicitGetter() { }\n" +
                "  void getExplicitSetter(def toSet) { }\n" +
                "  def getExplicitIsser() { }\n" +
                "}"
            ).getType("G")
    }

    @Test
    void testSearchInGroovy1() {
        String contents =
                "new p.G().prop\n" +
                "new p.G().setProp()\n" +
                "new p.G().isProp()\n" +
                "new p.G().getProp()\n"
        testProject.createGroovyTypeAndPackage("", "Script.groovy", contents)
        // expecting 3 matches since the explicit property reference will not be found since it is not synthetic
        List<SearchMatch> matches = performSearch("prop")
        assertNumMatch(3, matches)
        assertMatch("run", "getProp", contents, matches)
        assertMatch("run", "setProp", contents, matches)
        assertMatch("run", "isProp", contents, matches)
        assertNoMatch("run", "prop", contents, matches)
    }

    @Test
    void testSearchInGroovy2() {
        String contents =
                "new p.G().explicitGetter\n" +
                "new p.G().getExplicitGetter()\n" +
                "new p.G().isExplicitGetter()\n" +
                "new p.G().setExplicitGetter()\n"
        testProject.createGroovyTypeAndPackage("", "Script.groovy", contents)
        List<SearchMatch> matches = performSearch("getExplicitGetter")
        assertNumMatch(1, matches)
        assertMatch("run", "explicitGetter", contents, matches)
        assertNoMatch("run", "getExplicitGetter", contents, matches)
        assertNoMatch("run", "setExplicitGetter", contents, matches)
        assertNoMatch("run", "isExplicitGetter", contents, matches)
    }

    @Test
    void testSearchInGroovy3() {
        String contents =
                "new p.G().explicitSetter\n" +
                "new p.G().getExplicitSetter()\n" +
                "new p.G().isExplicitSetter()\n" +
                "new p.G().setExplicitSetter()\n"
        testProject.createGroovyTypeAndPackage("", "Script.groovy", contents)
        List<SearchMatch> matches = performSearch("getExplicitSetter")
        assertNumMatch(1, matches)
        assertMatch("run", "explicitSetter", contents, matches)
        assertNoMatch("run", "getExplicitSetter", contents, matches)
        assertNoMatch("run", "setExplicitSetter", contents, matches)
        assertNoMatch("run", "isExplicitSetter", contents, matches)
    }

    @Test
    void testSearchInGroovy4() {
        String contents =
                "new p.G().explicitIsser\n" +
                "new p.G().getExplicitIsser()\n" +
                "new p.G().isExplicitIsser()\n" +
                "new p.G().setExplicitIsser()\n"
        testProject.createGroovyTypeAndPackage("", "Script.groovy", contents)
        List<SearchMatch> matches = performSearch("getExplicitIsser")
        assertNumMatch(1, matches)
        assertMatch("run", "explicitIsser", contents, matches)
        assertNoMatch("run", "getExplicitIsser", contents, matches)
        assertNoMatch("run", "setExplicitIsser", contents, matches)
        assertNoMatch("run", "isExplicitIsser", contents, matches)
    }

    @Test
    void testSearchInJava1() {
        String contents =
                "class AClass {\n" +
                "  void run() {\n" +
                "    new p.G().prop = null;\n" +
                "    new p.G().setProp(null);\n" +
                "    new p.G().isProp();\n" +
                "    new p.G().getProp();\n" +
                "  }\n" +
                "}"
        testProject.createJavaTypeAndPackage("", "AClass.java", contents)
        // expecting 3 matches since the explicit property reference will not be found since it is not synthetic
        List<SearchMatch> matches = performSearch("prop")
        assertNumMatch(3, matches)
        assertMatch("run", "getProp()", contents, matches)
        assertMatch("run", "setProp(null)", contents, matches)
        assertMatch("run", "isProp()", contents, matches)
        assertNoMatch("run", "prop", contents, matches)
    }

    @Test // has a compile error, but still informative for searching
    void testSearchInJava2() {
        String contents =
                "class AClass {\n" +
                "  void run() {\n" +
                "    new p.G().explicitGetter = null;\n" +
                "    new p.G().setExplicitGetter(null);\n" +
                "    new p.G().getExplicitGetter();\n" +
                "    new p.G().isExplicitGetter();\n" +
                "  }\n" +
                "}"
        testProject.createJavaTypeAndPackage("", "AClass.java", contents)
        List<SearchMatch> matches = performSearch("getExplicitGetter")
        assertNumMatch(1, matches)
        assertNoMatch("run", "getExplicitGetter", contents, matches)
        assertNoMatch("run", "setExplicitGetter", contents, matches)
        assertNoMatch("run", "isExplicitGetter", contents, matches)
        assertMatch("run", "explicitGetter", contents, matches)
    }

    @Test // has a compile error, but still informative for searching
    void testSearchInJava3() {
        String contents =
                "class AClass {\n" +
                "  void run() {\n" +
                "    new p.G().explicitSetter = null;\n" +
                "    new p.G().setExplicitSetter(null);\n" +
                "    new p.G().getExplicitSetter();\n" +
                "    new p.G().isExplicitSetter();\n" +
                "  }\n" +
                "}"
        testProject.createJavaTypeAndPackage("", "AClass.java", contents)
        List<SearchMatch> matches = performSearch("getExplicitSetter")
        assertNumMatch(1, matches)
        assertNoMatch("run", "getExplicitSetter", contents, matches)
        assertNoMatch("run", "setExplicitSetter", contents, matches)
        assertNoMatch("run", "isExplicitSetter", contents, matches)
        assertMatch("run", "explicitSetter", contents, matches)
    }

    @Test // has a compile error, but still informative for searching
    void testSearchInJava4() {
        String contents =
                "class AClass {\n" +
                "  void run() {\n" +
                "    new p.G().explicitIsser = null;\n" +
                "    new p.G().setExplicitIsser(null);\n" +
                "    new p.G().getExplicitIsser();\n" +
                "    new p.G().isExplicitIsser();\n" +
                "  }\n" +
                "}"
        testProject.createJavaTypeAndPackage("", "AClass.java", contents)
        List<SearchMatch> matches = performSearch("getExplicitIsser")
        assertNumMatch(1, matches)
        assertNoMatch("run", "getExplicitIsser", contents, matches)
        assertNoMatch("run", "setExplicitIsser", contents, matches)
        assertNoMatch("run", "isExplicitIsser", contents, matches)
        assertMatch("run", "explicitIsser", contents, matches)
    }

    @Test // GRECLIPSE-1369
    void testSearchInJava5() {
        String contents =
                "class AClass {\n" +
                "  int hhh;\n" +
                "  public int getHhh() {" +
                "    return hhh;" +
                "  }\n" +
                "  public void setHhh(int other) {" +
                "    this.hhh = other;\n" +
                "    this.getHhh();\n" +
                "    this.setHhh(0);\n" +
                "  }\n" +
                "}"
        IType type = testProject.createJavaTypeAndPackage("", "AClass.java", contents).getType("AClass")
        IField toSearch = type.getField("hhh")
        SyntheticAccessorSearchRequestor synthRequestor = new SyntheticAccessorSearchRequestor()
        TestSearchRequestor requestor = new TestSearchRequestor()
        synthRequestor.findSyntheticMatches(toSearch, requestor, null)
        List<SearchMatch> matches = requestor.matches
        // should not match the reference to the getter or the setter.
        // the actual references are found by the real search engine
        assertNumMatch(0, matches)
    }

    private IJavaElement findSearchTarget(String name) {
        for (IJavaElement child : gType.getChildren()) {
            if (child.getElementName().equals(name)) {
                return child
            }
        }
        Assert.fail("child not found: " + name)
        return null
    }

    private List<SearchMatch> performSearch(String searchName) {
        IJavaElement toSearch = findSearchTarget(searchName)
        SyntheticAccessorSearchRequestor synthRequestor = new SyntheticAccessorSearchRequestor()
        TestSearchRequestor requestor = new TestSearchRequestor()
        synthRequestor.findSyntheticMatches(toSearch, requestor, null)
        return requestor.matches
    }

    private String printMatches(List<SearchMatch> matches) {
        StringBuffer sb = new StringBuffer()
        for (Iterator<SearchMatch> matchIter = matches.iterator(); matchIter.hasNext();) {
            SearchMatch match = matchIter.next()
            sb.append("\n\n" + match)
        }
        return sb.toString()
    }

    /**
     * asserts that the given match exists at least once in the list
     */
    private void assertMatch(String enclosingName, String matchName, String contents, List<SearchMatch> matches) {
        int matchStart = 0
        int matchIndex = 0
        boolean matchFound = false
        for (SearchMatch match : matches) {
            if (((IJavaElement) match.getElement()).getElementName().equals(enclosingName) &&
                    contents.indexOf(matchName, matchStart) == match.getOffset() &&
                    matchName.length() == match.getLength()) {
                matchFound = true
                break
            }
            matchIndex += 1
        }
        if (!matchFound) {
            Assert.fail("Match name " + matchName + " not found in\n" + printMatches(matches))
        }
        SearchMatch match = matches.remove(matchIndex)
        Assert.assertTrue("Match enclosing element does not exist", ((IJavaElement) match.getElement()).exists())
    }

    private void assertNumMatch(int expected, List<SearchMatch> matches) {
        Assert.assertEquals("Wrong number of matches found in:\n" + printMatches(matches), expected, matches.size())
    }

    private void assertNoMatch(String enclosingName, String matchName, String contents, List<SearchMatch> matches) {
        boolean matchFound = false
        for (SearchMatch match : matches) {
            if (((IJavaElement) match.getElement()).getElementName().equals(enclosingName) &&
                    contents.indexOf(matchName) == match.getOffset() &&
                    matchName.length() == match.getLength()) {
                matchFound = true
                break
            }
        }
        if (matchFound) {
            Assert.fail("Match name " + matchName + " was found, but should not have been.\n" + printMatches(matches))
        }
    }
}
