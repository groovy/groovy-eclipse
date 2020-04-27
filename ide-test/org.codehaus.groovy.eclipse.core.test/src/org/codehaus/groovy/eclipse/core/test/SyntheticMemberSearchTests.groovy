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
package org.codehaus.groovy.eclipse.core.test

import org.codehaus.groovy.eclipse.core.search.SyntheticAccessorSearchRequestor
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.search.IJavaSearchConstants
import org.eclipse.jdt.core.search.SearchEngine
import org.eclipse.jdt.core.search.SearchMatch
import org.eclipse.jdt.core.search.SearchParticipant
import org.eclipse.jdt.internal.core.search.JavaSearchParticipant
import org.junit.Assert
import org.junit.Before
import org.junit.Test

final class SyntheticMemberSearchTests extends GroovyEclipseTestSuite {

    private IType gType

    @Before
    void setUp() {
        def gUnit = addGroovySource '''\
            |class G {
            |  boolean proper
            |  public def field
            |  def getExplicit() { }
            |  boolean isExplicit() { }
            |  void setExplicit(def value) { }
            |}
            |'''.stripMargin(), 'G', 'p'

        gType = gUnit.getType('G')
    }

    @Test // references to property itself and synthetic accessors
    void testSearchInGroovy1() {
        String contents = '''\
            |new p.G().proper
            |new p.G().isProper()
            |new p.G().getProper()
            |new p.G().setProper(true)
            |'''.stripMargin()
        addGroovySource(contents, nextUnitName())
        List<SearchMatch> matches = performSearch('proper')

        assertCount(3, matches)
        assertNoMatch('run', 'proper', contents, matches)
        assertMatch('run', 'isProper', contents, matches)
        assertMatch('run', 'getProper', contents, matches)
        assertMatch('run', 'setProper', contents, matches)
    }

    @Test // references to pseudo-property and non-synthetic accessors
    void testSearchInGroovy2() {
        String contents = '''\
            |new p.G().explicit
            |new p.G().isExplicit()
            |new p.G().getExplicit()
            |new p.G().setExplicit()
            |'''.stripMargin()
        addGroovySource(contents, nextUnitName())
        List<SearchMatch> matches = performSearch('getExplicit')

        assertCount(0, matches)
        assertNoMatch('run', 'explicit', contents, matches)
        assertNoMatch('run', 'isExplicit', contents, matches)
        assertNoMatch('run', 'getExplicit', contents, matches)
        assertNoMatch('run', 'setExplicit', contents, matches)
    }

    @Test // references to pseudo-property and non-synthetic accessors
    void testSearchInGroovy3() {
        String contents = '''\
            |new p.G().explicit
            |new p.G().isExplicit()
            |new p.G().getExplicit()
            |new p.G().setExplicit()
            |'''.stripMargin()
        addGroovySource(contents, nextUnitName())
        List<SearchMatch> matches = performSearch('setExplicit')

        assertCount(0, matches)
        assertNoMatch('run', 'explicit', contents, matches)
        assertNoMatch('run', 'isExplicit', contents, matches)
        assertNoMatch('run', 'getExplicit', contents, matches)
        assertNoMatch('run', 'setExplicit', contents, matches)
    }

    @Test // references to pseudo-property and non-synthetic accessors
    void testSearchInGroovy4() {
        String contents = '''\
            |new p.G().explicit
            |new p.G().isExplicit()
            |new p.G().getExplicit()
            |new p.G().setExplicit()
            |'''.stripMargin()
        addGroovySource(contents, nextUnitName())
        List<SearchMatch> matches = performSearch('isExplicit')

        assertCount(0, matches)
        assertNoMatch('run', 'explicit', contents, matches)
        assertNoMatch('run', 'isExplicit', contents, matches)
        assertNoMatch('run', 'getExplicit', contents, matches)
        assertNoMatch('run', 'setExplicit', contents, matches)
    }

    @Test // GRECLIPSE-1369
    void testSearchInJava0() {
        String contents = '''\
            |class ClassA {
            |  int hhh;
            |  public int getHhh() {
            |    return hhh;
            |  }
            |  public void setHhh(int other) {
            |    this.hhh = other;
            |    this.getHhh();
            |    this.setHhh(0);
            |  }
            |}
            |'''.stripMargin()
        IType jType = addJavaSource(contents, 'ClassA').getType('ClassA')
        List<SearchMatch> matches = performSearch('hhh', jType)

        // should not match the reference to the getter or the setter
        // the actual references are found by the real search engine
        assertCount(0, matches)
    }

    @Test
    void testSearchInJava1() {
        String contents = '''\
            |class ClassB {
            |  void run() {
            |    new p.G().proper = null;
            |    new p.G().isProper();
            |    new p.G().getProper();
            |    new p.G().setProper(null);
            |  }
            |}
            |'''.stripMargin()
        addJavaSource(contents, 'ClassB')
        List<SearchMatch> matches = performSearch('proper')

        assertCount(3, matches)
        assertNoMatch('run', 'proper', contents, matches)
        assertMatch('run', 'isProper()', contents, matches)
        assertMatch('run', 'getProper()', contents, matches)
        assertMatch('run', 'setProper(null)', contents, matches)
    }

    @Test // has a compile error, but still informative for searching
    void testSearchInJava2() {
        String contents = '''\
            |class ClassC {
            |  void run() {
            |    new p.G().explicit = null;
            |    new p.G().isExplicit();
            |    new p.G().getExplicit();
            |    new p.G().setExplicit(null);
            |  }
            |}
            |'''.stripMargin()
        addJavaSource(contents, 'ClassC')
        List<SearchMatch> matches = performSearch('getExplicit')

        assertCount(1, matches)
        assertMatch('run', 'explicit', contents, matches)
        assertNoMatch('run', 'isExplicit', contents, matches)
        assertNoMatch('run', 'getExplicit', contents, matches)
        assertNoMatch('run', 'setExplicit', contents, matches)
    }

    @Test // has a compile error, but still informative for searching
    void testSearchInJava3() {
        String contents = '''\
            |class ClassD {
            |  void run() {
            |    new p.G().explicit = null;
            |    new p.G().isExplicit();
            |    new p.G().getExplicit();
            |    new p.G().setExplicit(null);
            |  }
            |}
            |'''.stripMargin()
        addJavaSource(contents, 'ClassD')
        List<SearchMatch> matches = performSearch('setExplicit')

        assertCount(1, matches)
        assertMatch('run', 'explicit', contents, matches)
        assertNoMatch('run', 'isExplicit', contents, matches)
        assertNoMatch('run', 'getExplicit', contents, matches)
        assertNoMatch('run', 'setExplicit', contents, matches)
    }

    @Test // has a compile error, but still informative for searching
    void testSearchInJava4() {
        String contents = '''\
            |class ClassE {
            |  void run() {
            |    new p.G().explicit = true;
            |    new p.G().isExplicit();
            |    new p.G().getExplicit();
            |    new p.G().setExplicit(null);
            |  }
            |}
            |'''.stripMargin()
        addJavaSource(contents, 'ClassE')
        List<SearchMatch> matches = performSearch('isExplicit')

        assertCount(1, matches)
        assertMatch('run', 'explicit', contents, matches)
        assertNoMatch('run', 'isExplicit', contents, matches)
        assertNoMatch('run', 'getExplicit', contents, matches)
        assertNoMatch('run', 'setExplicit', contents, matches)
    }

    @Test
    void testSearchInJava5() {
        addJavaSource '''\
            |class J {
            |  public boolean isProper() {
            |    return false;
            |  }
            |  void test(G g) {
            |    g.proper = null;
            |    g.isProper();
            |    g.getProper();
            |    g.setProper(null);
            |  }
            |}
            |'''.stripMargin(), 'C', 'p'

        List<SearchMatch> matches = []
        new SyntheticAccessorSearchRequestor().findSyntheticMatches(gType.children.find { it.elementName == 'proper' },
            IJavaSearchConstants.DECLARATIONS | IJavaSearchConstants.IGNORE_DECLARING_TYPE | IJavaSearchConstants.IGNORE_RETURN_TYPE,
            [new JavaSearchParticipant()] as SearchParticipant[],
            SearchEngine.createWorkspaceScope(),
            matches.&add,
            null)

        assertCount(0, matches)
    }

    //--------------------------------------------------------------------------

    private List<SearchMatch> performSearch(String searchName, IType type = gType) {
        IJavaElement target = type.children.find { it.elementName == searchName }
        Assert.assertNotNull("child not found: $searchName", target)

        List<SearchMatch> matches = []
        new SyntheticAccessorSearchRequestor().findSyntheticMatches(
            target, { match -> if (match.offset < 200) matches << match }, null)
        return matches
    }

    private void assertCount(int expected, List<SearchMatch> matches) {
        Assert.assertEquals("Wrong number of matches found in:\n${ -> matches.join('\n')}", expected, matches.size())
    }

    /**
     * asserts that the given match exists at least once in the list
     */
    private void assertMatch(String enclosingName, String matchName, String contents, List<SearchMatch> matches) {
        int matchStart = 0
        int matchIndex = 0
        boolean matchFound = false
        for (match in matches) {
            if ((match.element as IJavaElement).elementName == enclosingName &&
                    contents.indexOf(matchName, matchStart) == match.offset &&
                    matchName.length() == match.length) {
                matchFound = true
                break
            }
            matchIndex += 1
        }
        if (!matchFound) {
            Assert.fail("Match name $matchName not found in\n${matches.join('\n')}")
        }
        SearchMatch match = matches.remove(matchIndex)
        Assert.assertTrue('Match enclosing element does not exist', (match.element as IJavaElement).exists())
    }

    private void assertNoMatch(String enclosingName, String matchName, String contents, List<SearchMatch> matches) {
        boolean matchFound = matches.any { SearchMatch match ->
            (match.element as IJavaElement).elementName == enclosingName &&
            contents.indexOf(matchName) == match.offset &&
            matchName.length() == match.length
        }
        if (matchFound) {
            Assert.fail("Match name $matchName was found, but should not have been.\n${matches.join('\n')}")
        }
    }
}
