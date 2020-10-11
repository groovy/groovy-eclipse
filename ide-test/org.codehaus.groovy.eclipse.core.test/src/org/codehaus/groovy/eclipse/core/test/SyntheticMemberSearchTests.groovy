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

import groovy.transform.CompileStatic

import org.codehaus.groovy.eclipse.core.search.SyntheticAccessorSearchRequestor
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.search.IJavaSearchConstants
import org.eclipse.jdt.core.search.SearchEngine
import org.eclipse.jdt.core.search.SearchMatch
import org.eclipse.jdt.core.search.SearchParticipant
import org.junit.Before
import org.junit.Test

@CompileStatic
final class SyntheticMemberSearchTests extends GroovyEclipseTestSuite {

    private IType gType

    @Before
    void setUp() {
        def gUnit = addGroovySource '''\
            |class G {
            |  boolean xxxx
            |  public boolean yyyy
            |  boolean isZzzz() {}
            |  boolean getZzzz() {}
            |  void setZzzz(boolean value) {}
            |}
            |'''.stripMargin(), 'G', 'p'

        gType = gUnit.getType('G')
        assert gType?.exists()
    }

    @Test // references to property itself and synthetic accessors
    void testSearchInGroovy1() {
        String contents = '''\
            |new p.G().xxxx
            |new p.G().isXxxx()
            |new p.G().getXxxx()
            |new p.G().setXxxx(true)
            |'''.stripMargin()
        addGroovySource(contents, nextUnitName())
        List<SearchMatch> matches = performSearch('xxxx')

        assertCount(3, matches)
        assertNoMatch('run', 'xxxx', contents, matches)
        assertMatch('run', 'isXxxx', contents, matches)
        assertMatch('run', 'getXxxx', contents, matches)
        assertMatch('run', 'setXxxx', contents, matches)
    }

    @Test // references to pseudo-property and non-synthetic accessors
    void testSearchInGroovy2() {
        String contents = '''\
            |new p.G().zzzz
            |new p.G().isZzzz()
            |new p.G().getZzzz()
            |new p.G().setZzzz()
            |'''.stripMargin()
        addGroovySource(contents, nextUnitName())
        List<SearchMatch> matches = performSearch('getZzzz')

        assertCount(0, matches)
        assertNoMatch('run', 'zzzz', contents, matches)
        assertNoMatch('run', 'isZzzz', contents, matches)
        assertNoMatch('run', 'getZzzz', contents, matches)
        assertNoMatch('run', 'setZzzz', contents, matches)
    }

    @Test // references to pseudo-property and non-synthetic accessors
    void testSearchInGroovy3() {
        String contents = '''\
            |new p.G().zzzz
            |new p.G().isZzzz()
            |new p.G().getZzzz()
            |new p.G().setZzzz()
            |'''.stripMargin()
        addGroovySource(contents, nextUnitName())
        List<SearchMatch> matches = performSearch('setZzzz')

        assertCount(0, matches)
        assertNoMatch('run', 'zzzz', contents, matches)
        assertNoMatch('run', 'isZzzz', contents, matches)
        assertNoMatch('run', 'getZzzz', contents, matches)
        assertNoMatch('run', 'setZzzz', contents, matches)
    }

    @Test // references to pseudo-property and non-synthetic accessors
    void testSearchInGroovy4() {
        String contents = '''\
            |new p.G().zzzz
            |new p.G().isZzzz()
            |new p.G().getZzzz()
            |new p.G().setZzzz()
            |'''.stripMargin()
        addGroovySource(contents, nextUnitName())
        List<SearchMatch> matches = performSearch('isZzzz')

        assertCount(0, matches)
        assertNoMatch('run', 'zzzz', contents, matches)
        assertNoMatch('run', 'isZzzz', contents, matches)
        assertNoMatch('run', 'getZzzz', contents, matches)
        assertNoMatch('run', 'setZzzz', contents, matches)
    }

    @Test // GRECLIPSE-1369
    void testSearchInJava0() {
        String contents = '''\
            |class J {
            |  int nnn;
            |  public int getNnn() {
            |    return this.nnn;
            |  }
            |  public void setNnn(int nnn) {
            |    this.nnn = nnn;
            |    this.getNnn();
            |    this.setNnn(0);
            |  }
            |}
            |'''.stripMargin()
        IType jType = addJavaSource(contents, 'J').getType('J')
        List<SearchMatch> matches = performSearch('nnn', jType)

        // should not match the reference to the getter or the setter
        // the actual references are found by the usual search engine
        assertCount(0, matches)
    }

    @Test
    void testSearchInJava1() {
        String contents = '''\
            |class C {
            |  void run() {
            |    new p.G().xxxx = false;
            |    new p.G().isXxxx();
            |    new p.G().getXxxx();
            |    new p.G().setXxxx(true);
            |  }
            |}
            |'''.stripMargin()
        addJavaSource(contents, 'C')
        List<SearchMatch> matches = performSearch('xxxx')

        assertCount(3, matches)
        assertNoMatch('run', 'xxxx', contents, matches)
        assertMatch('run', 'isXxxx()', contents, matches)
        assertMatch('run', 'getXxxx()', contents, matches)
        assertMatch('run', 'setXxxx(true)', contents, matches)
    }

    @Test // has a compile error, but still informative for searching
    void testSearchInJava2() {
        String contents = '''\
            |class C {
            |  void run() {
            |    new p.G().zzzz = false;
            |    new p.G().isZzzz();
            |    new p.G().getZzzz();
            |    new p.G().setZzzz(null);
            |  }
            |}
            |'''.stripMargin()
        addJavaSource(contents, 'C')
        List<SearchMatch> matches = performSearch('getZzzz')

        assertCount(1, matches)
        assertMatch('run', 'zzzz', contents, matches)
        assertNoMatch('run', 'isZzzz', contents, matches)
        assertNoMatch('run', 'getZzzz', contents, matches)
        assertNoMatch('run', 'setZzzz', contents, matches)
    }

    @Test // has a compile error, but still informative for searching
    void testSearchInJava3() {
        String contents = '''\
            |class C {
            |  void run() {
            |    new p.G().zzzz = false;
            |    new p.G().isZzzz();
            |    new p.G().getZzzz();
            |    new p.G().setZzzz(null);
            |  }
            |}
            |'''.stripMargin()
        addJavaSource(contents, 'C')
        List<SearchMatch> matches = performSearch('setZzzz')

        assertCount(1, matches)
        assertMatch('run', 'zzzz', contents, matches)
        assertNoMatch('run', 'isZzzz', contents, matches)
        assertNoMatch('run', 'getZzzz', contents, matches)
        assertNoMatch('run', 'setZzzz', contents, matches)
    }

    @Test // has a compile error, but still informative for searching
    void testSearchInJava4() {
        String contents = '''\
            |class C {
            |  void run() {
            |    new p.G().zzzz = false;
            |    new p.G().isZzzz();
            |    new p.G().getZzzz();
            |    new p.G().setZzzz(null);
            |  }
            |}
            |'''.stripMargin()
        addJavaSource(contents, 'C')
        List<SearchMatch> matches = performSearch('isZzzz')

        assertCount(1, matches)
        assertMatch('run', 'zzzz', contents, matches)
        assertNoMatch('run', 'isZzzz', contents, matches)
        assertNoMatch('run', 'getZzzz', contents, matches)
        assertNoMatch('run', 'setZzzz', contents, matches)
    }

    @Test
    void testSearchInJava5() {
        addJavaSource '''\
            |package p;
            |class C {
            |  public boolean isXxxx() {
            |    return false;
            |  }
            |  void test(G g) {
            |    g.xxxx = false;
            |    g.isXxxx();
            |    g.getXxxx();
            |    g.setXxxx(true);
            |  }
            |}
            |'''.stripMargin(), 'C', 'p'

        List<SearchMatch> matches = []
        new SyntheticAccessorSearchRequestor().findSyntheticMatches(gType.children.find { it.elementName == 'xxxx' },
            IJavaSearchConstants.DECLARATIONS | IJavaSearchConstants.IGNORE_DECLARING_TYPE | IJavaSearchConstants.IGNORE_RETURN_TYPE,
            [SearchEngine.defaultSearchParticipant] as SearchParticipant[],
            SearchEngine.createWorkspaceScope(),
            matches.&add,
            null)

        assertCount(0, matches)
    }

    //--------------------------------------------------------------------------

    private List<SearchMatch> performSearch(String searchName, IType type = gType) {
        IJavaElement target = type.children.find { it.elementName == searchName }
        assert target != null : "child not found: $searchName"

        new LinkedList<SearchMatch>().tap {
            new SyntheticAccessorSearchRequestor().findSyntheticMatches(target, it.&add, null)
        }
    }

    private void assertCount(int expected, List<SearchMatch> matches) {
        assert matches.size() == expected : "Wrong number of matches found in:\n${ -> matches.join('\n')}"
    }

    /**
     * Ensures that the given match exists at least once in the list.
     */
    private void assertMatch(String enclosingName, String matchName, String contents, List<SearchMatch> matches) {
        int matchIndex = 0
        int matchStart = 0
        boolean matchFound = false
        for (match in matches) {
            if (isMatchOf(enclosingName, matchName, matchStart, contents, match)) {
                matchFound = true
                break
            }
            matchIndex += 1
        }
        assert matchFound : "Match name $matchName not found in\n${ -> matches.join('\n')}"

        SearchMatch match = matches.remove(matchIndex)
        assert (match.element as IJavaElement).exists()
    }

    private void assertNoMatch(String enclosingName, String matchName, String contents, List<SearchMatch> matches) {
        boolean matchFound = matches.any(this.&isMatchOf.curry(enclosingName, matchName, contents))
        assert !matchFound : "Match name $matchName was found, but should not have been.\n${ -> matches.join('\n')}"
    }

    private static boolean isMatchOf(String enclosingName, String matchName, int matchStart = 0, String contents, SearchMatch match) {
        (match.element as IJavaElement).elementName == enclosingName &&
        contents.indexOf(matchName, matchStart) == match.offset &&
        matchName.length() == match.length
    }
}
