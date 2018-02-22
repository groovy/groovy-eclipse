/*
 * Copyright 2009-2018 the original author or authors.
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
/*
 * Copyright 2009-2018 the original author or authors.
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
/*
 * Copyright 2009-2018 the original author or authors.
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
/*
 * Copyright 2009-2018 the original author or authors.
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
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.search.SearchMatch
import org.junit.Assert
import org.junit.Before
import org.junit.Test

final class SyntheticMemberSearchTests extends GroovyEclipseTestSuite {

    private IType gType

    @Before
    void setUp() {
        def gUnit = addGroovySource '''\
            class G {
              boolean prop
              public def field
              def getExplicit() { }
              boolean isExplicit() { }
              void setExplicit(def value) { }
            }
            '''.stripIndent(), 'G', 'p'

        gType = gUnit.getType('G')
    }

    @Test
    void testSearchInGroovy1() {
        String contents = '''\
            new p.G().prop
            new p.G().isProp()
            new p.G().getProp()
            new p.G().setProp()
            '''.stripIndent()
        addGroovySource(contents, nextUnitName())
        List<SearchMatch> matches = performSearch('prop')

        // expecting 3 matches since the explicit property reference will not be found since it is not synthetic
        assertNumMatch(3, matches)
        assertNoMatch('run', 'prop', contents, matches)
        assertMatch('run', 'isProp', contents, matches)
        assertMatch('run', 'getProp', contents, matches)
        assertMatch('run', 'setProp', contents, matches)
    }

    @Test
    void testSearchInGroovy2() {
        String contents = '''\
            new p.G().explicit
            new p.G().isExplicit()
            new p.G().getExplicit()
            new p.G().setExplicit()
            '''.stripIndent()
        addGroovySource(contents, nextUnitName())
        List<SearchMatch> matches = performSearch('getExplicit')

        assertNumMatch(1, matches)
        assertMatch('run', 'explicit', contents, matches)
        assertNoMatch('run', 'isExplicit', contents, matches)
        assertNoMatch('run', 'getExplicit', contents, matches)
        assertNoMatch('run', 'setExplicit', contents, matches)
    }

    @Test
    void testSearchInGroovy3() {
        String contents = '''\
            new p.G().explicit
            new p.G().isExplicit()
            new p.G().getExplicit()
            new p.G().setExplicit()
            '''.stripIndent()
        addGroovySource(contents, nextUnitName())
        List<SearchMatch> matches = performSearch('setExplicit')

        assertNumMatch(1, matches)
        assertMatch('run', 'explicit', contents, matches)
        assertNoMatch('run', 'isExplicit', contents, matches)
        assertNoMatch('run', 'getExplicit', contents, matches)
        assertNoMatch('run', 'setExplicit', contents, matches)
    }

    @Test
    void testSearchInGroovy4() {
        String contents = '''\
            new p.G().explicit
            new p.G().isExplicit()
            new p.G().getExplicit()
            new p.G().setExplicit()
            '''.stripIndent()
        addGroovySource(contents, nextUnitName())
        List<SearchMatch> matches = performSearch('isExplicit')

        assertNumMatch(1, matches)
        assertMatch('run', 'explicit', contents, matches)
        assertNoMatch('run', 'isExplicit', contents, matches)
        assertNoMatch('run', 'getExplicit', contents, matches)
        assertNoMatch('run', 'setExplicit', contents, matches)
    }

    @Test // GRECLIPSE-1369
    void testSearchInJava0() {
        String contents = '''\
            class ClassA {
              int hhh;
              public int getHhh() {
                return hhh;
              }
              public void setHhh(int other) {
                this.hhh = other;
                this.getHhh();
                this.setHhh(0);
              }
            }
            '''.stripIndent()
        IType jType = addJavaSource(contents, 'ClassA').getType('ClassA')
        List<SearchMatch> matches = performSearch('hhh', jType)

        // should not match the reference to the getter or the setter
        // the actual references are found by the real search engine
        assertNumMatch(0, matches)
    }

    @Test
    void testSearchInJava1() {
        String contents = '''\
            class ClassB {
              void run() {
                new p.G().prop = null;
                new p.G().isProp();
                new p.G().getProp();
                new p.G().setProp(null);
              }
            }
            '''.stripIndent()
        addJavaSource(contents, 'ClassB')
        List<SearchMatch> matches = performSearch('prop')

        // expecting 3 matches since the explicit property reference will not be found since it is not synthetic
        assertNumMatch(3, matches)
        assertNoMatch('run', 'prop', contents, matches)
        assertMatch('run', 'isProp()', contents, matches)
        assertMatch('run', 'getProp()', contents, matches)
        assertMatch('run', 'setProp(null)', contents, matches)
    }

    @Test // has a compile error, but still informative for searching
    void testSearchInJava2() {
        String contents = '''\
            class ClassC {
              void run() {
                new p.G().explicit = null;
                new p.G().isExplicit();
                new p.G().getExplicit();
                new p.G().setExplicit(null);
              }
            }
            '''.stripIndent()
        addJavaSource(contents, 'ClassC')
        List<SearchMatch> matches = performSearch('getExplicit')

        assertNumMatch(1, matches)
        assertMatch('run', 'explicit', contents, matches)
        assertNoMatch('run', 'isExplicit', contents, matches)
        assertNoMatch('run', 'getExplicit', contents, matches)
        assertNoMatch('run', 'setExplicit', contents, matches)
    }

    @Test // has a compile error, but still informative for searching
    void testSearchInJava3() {
        String contents = '''\
            class ClassD {
              void run() {
                new p.G().explicit = null;
                new p.G().isExplicit();
                new p.G().getExplicit();
                new p.G().setExplicit(null);
              }
            }
            '''.stripIndent()
        addJavaSource(contents, 'ClassD')
        List<SearchMatch> matches = performSearch('setExplicit')

        assertNumMatch(1, matches)
        assertMatch('run', 'explicit', contents, matches)
        assertNoMatch('run', 'isExplicit', contents, matches)
        assertNoMatch('run', 'getExplicit', contents, matches)
        assertNoMatch('run', 'setExplicit', contents, matches)
    }

    @Test // has a compile error, but still informative for searching
    void testSearchInJava4() {
        String contents = '''\
            class ClassE {
              void run() {
                new p.G().explicit = true;
                new p.G().isExplicit();
                new p.G().getExplicit();
                new p.G().setExplicit(null);
              }
            }
            '''.stripIndent()
        addJavaSource(contents, 'ClassE')
        List<SearchMatch> matches = performSearch('isExplicit')

        assertNumMatch(1, matches)
        assertMatch('run', 'explicit', contents, matches)
        assertNoMatch('run', 'isExplicit', contents, matches)
        assertNoMatch('run', 'getExplicit', contents, matches)
        assertNoMatch('run', 'setExplicit', contents, matches)
    }

    //--------------------------------------------------------------------------

    private static class TestSearchRequestor implements ISearchRequestor {
        List<SearchMatch> matches = []
        public void acceptMatch(SearchMatch match) {
            matches << match
        }
    }

    private IJavaElement findSearchTarget(String name, IType type) {
        for (IJavaElement child : type.children) {
            if (child.elementName == name) {
                return child
            }
        }
        Assert.fail("child not found: $name")
    }

    private List<SearchMatch> performSearch(String searchName, IType type = gType) {
        IJavaElement toSearch = findSearchTarget(searchName, type)
        TestSearchRequestor requestor = new TestSearchRequestor()
        new SyntheticAccessorSearchRequestor().findSyntheticMatches(toSearch, requestor, null)
        return requestor.matches
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

    private void assertNumMatch(int expected, List<SearchMatch> matches) {
        Assert.assertEquals("Wrong number of matches found in:\n${ -> matches.join('\n')}", expected, matches.size())
    }
}
