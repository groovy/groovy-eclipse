/*
 * Copyright 2009-2023 the original author or authors.
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
import org.eclipse.jdt.core.groovy.tests.search.SearchTestSuite
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

    @Test // https://github.com/groovy/groovy-eclipse/issues/1458
    void testSearchInGroovy5() {
        String contents = '''\
            |class C {
            |  String string
            |}
            |def obj = new C(string: null)
            |def str = obj.string
            |obj.string = str
            |str = obj.@string
            |obj.@string = str
            |str += obj.string
            |obj.string += str
            |str = obj.getString() // yes
            |obj.setString( null )
            |str = obj.'getString'() // yes
            |obj.'setString'( null )
            |'''.stripMargin()
        def unit = addGroovySource(contents, nextUnitName())

        List<SearchMatch> matches = []
        new SyntheticAccessorSearchRequestor().findSyntheticMatches(unit.getType('C').getField('string'),
            IJavaSearchConstants.READ_ACCESSES | IJavaSearchConstants.IGNORE_RETURN_TYPE,
            [SearchEngine.getDefaultSearchParticipant()] as SearchParticipant[],
            SearchEngine.createWorkspaceScope(),
            matches.&add, null)

        assertCount(2, matches)
        assertMatch('run', 'getString', contents, matches)
        assertMatch('run', "'getString'", contents, matches)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1458
    void testSearchInGroovy6() {
        String contents = '''\
            |class C {
            |  String string
            |}
            |def obj = new C(string: null)
            |def str = obj.string
            |obj.string = str
            |str = obj.@string
            |obj.@string = str
            |str += obj.string
            |obj.string += str
            |str = obj.getString()
            |obj.setString( null ) // yes
            |str = obj.'getString'()
            |obj.'setString'( null ) // yes
            |'''.stripMargin()
        def unit = addGroovySource(contents, nextUnitName())

        List<SearchMatch> matches = []
        new SyntheticAccessorSearchRequestor().findSyntheticMatches(unit.getType('C').getField('string'),
            IJavaSearchConstants.WRITE_ACCESSES | IJavaSearchConstants.IGNORE_RETURN_TYPE,
            [SearchEngine.getDefaultSearchParticipant()] as SearchParticipant[],
            SearchEngine.createWorkspaceScope(),
            matches.&add, null)

        assertCount(2, matches)
        assertMatch('run', 'setString', contents, matches)
        assertMatch('run', "'setString'", contents, matches)
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
            [SearchEngine.getDefaultSearchParticipant()] as SearchParticipant[],
            SearchEngine.createWorkspaceScope(),
            matches.&add, null)

        assertCount(0, matches)
    }

    //--------------------------------------------------------------------------

    private List<SearchMatch> performSearch(String searchName, IType type = gType) {
        IJavaElement target = type.children.find { it.elementName == searchName }
        assert target?.exists() : "child not found: $searchName"
        SearchTestSuite.waitUntilReady(type.javaProject)

        new LinkedList<SearchMatch>().tap {
            new SyntheticAccessorSearchRequestor().findSyntheticMatches(target, it.&add, null)
        }
    }

    private void assertCount(int expected, List<SearchMatch> matches) {
        assert matches.size() == expected : "Wrong number of matches found in:\n${ -> matches.join('\n')}"
    }

    private void assertMatch(String enclosingName, String matchName, String contents, List<SearchMatch> matches) {
        SearchMatch match = matches.find(this.&isMatchOf.curry(enclosingName, matchName, contents))
        assert match : "Match for '$matchName' not found in:\n${ -> matches.join('\n')}"
        assert (match.element as IJavaElement).exists()
    }

    private void assertNoMatch(String enclosingName, String matchName, String contents, List<SearchMatch> matches) {
        boolean matchFound = matches.any(this.&isMatchOf.curry(enclosingName, matchName, contents))
        assert !matchFound : "Match for '$matchName' was found in:\n${ -> matches.join('\n')}"
    }

    private static boolean isMatchOf(String enclosingName, String matchName, String contents, SearchMatch match) {
        (match.element as IJavaElement).elementName == enclosingName &&
        contents.indexOf(matchName) == match.offset &&
        matchName.length() == match.length
    }
}
