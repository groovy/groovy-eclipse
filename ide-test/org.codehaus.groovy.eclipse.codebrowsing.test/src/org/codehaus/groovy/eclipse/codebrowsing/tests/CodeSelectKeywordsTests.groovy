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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import org.junit.Ignore
import org.junit.Test

final class CodeSelectKeywordsTests extends BrowsingTestSuite {

    @Test
    void testCodeSelectKeywordPackage() {
        String contents = '''\
            package a.b.c.d
            '''.stripIndent()
        assertCodeSelect([contents], 'package', null)
    }

    @Test
    void testCodeSelectKeywordImport() {
        String contents = '''\
            import java.util.regex.Pattern
            Pattern p = ~/123/
            '''.stripIndent()
        assertCodeSelect([contents], 'import', null)
    }

    @Test
    void testCodeSelectKeywordStaticInImport() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            def p = compile('123')
            '''.stripIndent()
        assertCodeSelect([contents], 'static', null)
    }

    @Test
    void testCodeSelectKeywordAsInImport() {
        String contents = '''\
            import static java.util.regex.Pattern.compile as build
            def p = build('123')
            '''.stripIndent()
        assertCodeSelect([contents], 'as', null)
    }

    @Test @Ignore('Is this just shorthand for getClass()? Java Editor doesn\'t code select on "class" in literal')
    void testSelectKeywordClass1() {
        String contents = 'String.class'
        assertCodeSelect([contents], 'class', null)
    }

    @Test
    void testSelectKeywordClass2() {
        String contents = 'class C { }'
        assertCodeSelect([contents], 'class', null)
    }

    @Test
    void testSelectKeywordClass3() {
        String contents = '@groovy.transform.Canonical class C { }'
        assertCodeSelect([contents], 'class', null)
    }

    @Test
    void testSelectKeywordClass4() {
        String contents = '@groovy.transform.Immutable class C { }'
        assertCodeSelect([contents], 'class', null)
    }

    @Test
    void testSelectKeywordDef1() {
        String contents = 'class C { def x() { } }'
        assertCodeSelect([contents], 'def', null)
    }

    @Test
    void testSelectKeywordDef2() {
        String contents = 'class C { Object x() { def y } }'
        assertCodeSelect([contents], 'def', null)
    }

    @Test
    void testSelectKeywordNull() {
        String contents = 'Object o = null'
        assertCodeSelect([contents], 'null', null)
    }

    @Test
    void testSelectKeywordThis1() {
        // Java Editor doesn't code select on 'this' variable expression
        String contents = 'class C { def x() { this } }'
        assertCodeSelect([contents], 'this', null)
    }

    @Test
    void testSelectKeywordThis2() {
        // Java Editor doesn't code select on 'this' variable expression
        String contents = 'class C { def x() { this.toString() } }'
        assertCodeSelect([contents], 'this', null)
    }

    @Test // GRECLIPSE-548
    void testSelectKeywordSuper1() {
        String contents = '''\
            class Super { }
            class C extends Super {
              def x() { super }
            }
            '''.stripIndent()
        assertCodeSelect([contents], 'super', 'Super')
    }

    @Test // GRECLIPSE-548
    void testSelectKeywordSuper2() {
        String contents = '''\
            class Super { }
            class C extends Super {
              def x() { super.toString() }
            }
            '''.stripIndent()
        assertCodeSelect([contents], 'super', 'Super')
    }

    @Test
    void testSelectKeywordReturn() {
        String contents = 'def meth() { return null }'
        assertCodeSelect([contents], 'return', null)
    }
}
