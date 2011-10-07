/*
 * Copyright 2003-2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.search;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.internal.core.search.JavaSearchParticipant;

/**
 * All of these tests here should produce {@link ModuleNode}s with
 * encounteredUnrecoverableError set to true
 *
 * @author andrew
 * @created Oct 6, 2011
 */
public class SyntheticMemberSearchTests extends EclipseTestCase {

    private IType gType;

    class TestSearchRequestor implements ISearchRequestor {
        List<SearchMatch> matches = new ArrayList<SearchMatch>();
        public void acceptMatch(SearchMatch match) {
            matches.add(match);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GroovyRuntime.addGroovyNature(testProject.getProject());
        GroovyRuntime.addGroovyClasspathContainer(testProject.getJavaProject());
        gType = testProject.createUnit("p", "G.groovy",
                "package p\n" +
                        "class G {\n" +
                        "  def prop\n" +
                        "  public def notProp\n" +
                        "  def getExplicitGetter() { }\n" +
                        "  void getExplicitSetter(def toSet) { }\n" +
                        "  def getExplicitIsser() { }\n" +
                "}").getType("G");
    }

    public void testSearchInGroovy1() throws Exception {
        String contents =
                "new p.G().prop\n" +
                        "new p.G().setProp()\n" +
                        "new p.G().isProp()\n" +
                        "new p.G().getProp()\n";
        testProject.createUnit("", "Script.groovy", contents);
        // expecting 3 matches since the explicit property reference will not be found since it is not synthetic
        List<SearchMatch> matches = performSearch("prop");
        assertNumMatch(3, matches);
        assertMatch("run", "getProp", contents, matches);
        assertMatch("run", "setProp", contents, matches);
        assertMatch("run", "isProp", contents, matches);
        assertNoMatch("run", "prop", contents, matches);
    }

    public void testSearchInGroovy2() throws Exception {
        String contents =
                "new p.G().explicitGetter\n" +
                        "new p.G().getExplicitGetter()\n" +
                        "new p.G().isExplicitGetter()\n" +
                        "new p.G().setExplicitGetter()\n";
        testProject.createUnit("", "Script.groovy", contents);
        List<SearchMatch> matches = performSearch("getExplicitGetter");
        assertNumMatch(1, matches);
        assertMatch("run", "explicitGetter", contents, matches);
        assertNoMatch("run", "getExplicitGetter", contents, matches);
        assertNoMatch("run", "setExplicitGetter", contents, matches);
        assertNoMatch("run", "isExplicitGetter", contents, matches);
    }

    public void testSearchInGroovy3() throws Exception {
        String contents =
                "new p.G().explicitSetter\n" +
                        "new p.G().getExplicitSetter()\n" +
                        "new p.G().isExplicitSetter()\n" +
                        "new p.G().setExplicitSetter()\n";
        testProject.createUnit("", "Script.groovy", contents);
        List<SearchMatch> matches = performSearch("getExplicitSetter");
        assertNumMatch(1, matches);
        assertMatch("run", "explicitSetter", contents, matches);
        assertNoMatch("run", "getExplicitSetter", contents, matches);
        assertNoMatch("run", "setExplicitSetter", contents, matches);
        assertNoMatch("run", "isExplicitSetter", contents, matches);
    }

    public void testSearchInGroovy4() throws Exception {
        String contents =
                "new p.G().explicitIsser\n" +
                        "new p.G().getExplicitIsser()\n" +
                        "new p.G().isExplicitIsser()\n" +
                        "new p.G().setExplicitIsser()\n";
        testProject.createUnit("", "Script.groovy", contents);
        List<SearchMatch> matches = performSearch("getExplicitIsser");
        assertNumMatch(1, matches);
        assertMatch("run", "explicitIsser", contents, matches);
        assertNoMatch("run", "getExplicitIsser", contents, matches);
        assertNoMatch("run", "setExplicitIsser", contents, matches);
        assertNoMatch("run", "isExplicitIsser", contents, matches);
    }

    public void testSearchInJava1() throws Exception {
        String contents =
                "class AClass {\n" +
                        "  void run() {\n" +
                        "    new p.G().prop = null;\n" +
                        "    new p.G().setProp(null);\n" +
                        "    new p.G().isProp();\n" +
                        "    new p.G().getProp();\n" +
                        "} }\n";
        testProject.createUnit("", "AClass.java", contents);
        // expecting 3 matches since the explicit property reference will not be found since it is not synthetic
        List<SearchMatch> matches = performSearch("prop");
        assertNumMatch(3, matches);
        assertMatch("run", "getProp()", contents, matches);
        assertMatch("run", "setProp(null)", contents, matches);
        assertMatch("run", "isProp()", contents, matches);
        assertNoMatch("run", "prop", contents, matches);
    }

    // has a compile error, but still informative for searching
    public void testSearchInJava2() throws Exception {
        String contents =
                "class AClass {\n" +
                        "  void run() {\n" +
                        "    new p.G().explicitGetter = null;\n" +
                        "    new p.G().setExplicitGetter(null);\n" +
                        "    new p.G().getExplicitGetter();\n" +
                        "    new p.G().isExplicitGetter();\n" +
                        "} }\n";
        testProject.createUnit("", "AClass.java", contents);
        List<SearchMatch> matches = performSearch("getExplicitGetter");
        assertNumMatch(1, matches);
        assertNoMatch("run", "getExplicitGetter", contents, matches);
        assertNoMatch("run", "setExplicitGetter", contents, matches);
        assertNoMatch("run", "isExplicitGetter", contents, matches);
        assertMatch("run", "explicitGetter", contents, matches);
    }

    // has a compile error, but still informative for searching
    public void testSearchInJava3() throws Exception {
        String contents =
                "class AClass {\n" +
                        "  void run() {\n" +
                        "    new p.G().explicitSetter = null;\n" +
                        "    new p.G().setExplicitSetter(null);\n" +
                        "    new p.G().getExplicitSetter();\n" +
                        "    new p.G().isExplicitSetter();\n" +
                        "} }\n";
        testProject.createUnit("", "AClass.java", contents);
        List<SearchMatch> matches = performSearch("getExplicitSetter");
        assertNumMatch(1, matches);
        assertNoMatch("run", "getExplicitSetter", contents, matches);
        assertNoMatch("run", "setExplicitSetter", contents, matches);
        assertNoMatch("run", "isExplicitSetter", contents, matches);
        assertMatch("run", "explicitSetter", contents, matches);
    }

    // has a compile error, but still informative for searching
    public void testSearchInJava4() throws Exception {
        String contents =
                "class AClass {\n" +
                        "  void run() {\n" +
                        "    new p.G().explicitIsser = null;\n" +
                        "    new p.G().setExplicitIsser(null);\n" +
                        "    new p.G().getExplicitIsser();\n" +
                        "    new p.G().isExplicitIsser();\n" +
                        "} }\n";
        testProject.createUnit("", "AClass.java", contents);
        List<SearchMatch> matches = performSearch("getExplicitIsser");
        assertNumMatch(1, matches);
        assertNoMatch("run", "getExplicitIsser", contents, matches);
        assertNoMatch("run", "setExplicitIsser", contents, matches);
        assertNoMatch("run", "isExplicitIsser", contents, matches);
        assertMatch("run", "explicitIsser", contents, matches);
    }

    private IJavaElement findSearchTarget(String name) throws JavaModelException {
        for (IJavaElement child : gType.getChildren()) {
            if (child.getElementName().equals(name)) {
                return child;
            }
        }
        fail("child not found: " + name);
        return null;
    }

    private List<SearchMatch> performSearch(String searchName) throws CoreException {
        IJavaElement toSearch = findSearchTarget(searchName);
        SyntheticAccessorSearchRequestor synthRequestor = new SyntheticAccessorSearchRequestor();
        TestSearchRequestor requestor = new TestSearchRequestor();
        synthRequestor.findSyntheticMatches(toSearch, IJavaSearchConstants.REFERENCES,
                new SearchParticipant[] { new JavaSearchParticipant() }, SearchEngine.createWorkspaceScope(), requestor, null);
        return requestor.matches;
    }

    private String printMatches(List<SearchMatch> matches) {
        StringBuffer sb = new StringBuffer();
        for (Iterator<SearchMatch> matchIter = matches.iterator(); matchIter.hasNext();) {
            SearchMatch match = (SearchMatch) matchIter.next();
            sb.append("\n\n" + match);

        }
        return sb.toString();
    }

    /**
     * asserts that the given match exists at least once in the list
     *
     * @param matchName
     * @param contents
     * @param matches
     */
    private void assertMatch(String enclosingName, String matchName, String contents, List<SearchMatch> matches) {

        int matchStart = 0;

        int matchIndex = 0;
        boolean matchFound = false;
        for (SearchMatch match : matches) {
            if (((IJavaElement) match.getElement()).getElementName().equals(enclosingName) &&
                    contents.indexOf(matchName, matchStart) == match.getOffset() &&
                    matchName.length() == match.getLength()) {
                matchFound = true;
                break;
            }
            matchIndex++;
        }

        if (!matchFound) {
            fail("Match name " + matchName + " not found in\n" + printMatches(matches));
        }

        SearchMatch match = matches.remove(matchIndex);
        assertTrue("Match enclosing element does not exist", ((IJavaElement) match.getElement()).exists());
    }

    private void assertNumMatch(int expected, List<SearchMatch> matches) {
        assertEquals("Wrong number of matches found in:\n" + printMatches(matches), expected, matches.size());
    }

    private void assertNoMatch(String enclosingName, String matchName, String contents, List<SearchMatch> matches) {
        boolean matchFound = false;
        for (SearchMatch match : matches) {
            if (((IJavaElement) match.getElement()).getElementName().equals(enclosingName) &&
                    contents.indexOf(matchName) == match.getOffset() &&
                    matchName.length() == match.getLength()) {
                matchFound = true;
                break;
            }
        }

        if (matchFound) {
            fail("Match name " + matchName + " was found, but should not have been.\n" + printMatches(matches));
        }
    }

}
