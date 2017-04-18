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
package org.codehaus.groovy.eclipse.codeassist.tests

import org.codehaus.groovy.eclipse.codeassist.processors.GroovyImportRewriteFactory
import org.junit.Test

final class FindImportsRegionTests {

    private void checkRegion(String initial, String expected) {
        assert GroovyImportRewriteFactory.findImportsRegion(initial).toString() == expected
    }

    @Test
    void testFindImportsRegion0() {
        checkRegion('', '')
    }

    @Test
    void testFindImportsRegion1() {
        checkRegion('''
            package p
            import a
            import b
            class I { }
            '''.stripIndent(), '''
            package p
            import a
            import b
            '''.stripIndent())
    }

    @Test
    void testFindImportsRegion2() {
        checkRegion('''
            import a
            import b
            class I { }
            '''.stripIndent(), '''
            import a
            import b
            '''.stripIndent())
    }

    // we made the decision only to look at import statements that start at the
    // beginning of the line.  An argument can be made otherwise and this can
    // be changed in the future
    @Test
    void testFindImportsRegion3() {
        checkRegion('''
            import a
             import b
            class I { }
            '''.stripIndent(), '''
            import a
            '''.stripIndent())
    }

    @Test
    void testFindImportsRegion4() {
        checkRegion('''
             import a
            import b
            class I { }
            '''.stripIndent(), '''
             import a
            import b
            '''.stripIndent())
    }

    @Test
    void testFindImportsRegion5() {
        checkRegion('''
            package p
            class I { }
            '''.stripIndent(), '''
            package p
            '''.stripIndent())
    }

    @Test
    void testFindImportsRegion6() {
        checkRegion('''\
            package p
            class I { }
            '''.stripIndent(), '''\
            package p
            '''.stripIndent())
    }

    @Test
    void testFindImportsRegion7() {
        checkRegion('''\
            /**
             *
             *
             */
            package p
            class I { }
            '''.stripIndent(), '''\
            /**
             *
             *
             */
            package p
            '''.stripIndent())
    }

    @Test
    void testFindImportsRegion8() {
        checkRegion('''\
            /**
             *
             *
             */
            package p
            import a.b.c // fdsaffdsa
            class I { }
            '''.stripIndent(), '''\
            /**
             *
             *
             */
            package p
            import a.b.c // fdsaffdsa
            '''.stripIndent())
    }
}
