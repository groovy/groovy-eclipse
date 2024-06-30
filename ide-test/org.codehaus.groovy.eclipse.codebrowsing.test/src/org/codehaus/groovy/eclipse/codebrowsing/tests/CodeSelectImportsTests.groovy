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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import org.codehaus.groovy.ast.ClassNode
import org.junit.Test

final class CodeSelectImportsTests extends BrowsingTestSuite {

    @Test
    void testCodeSelectOnImportType1() {
        String source = '''\
            |import java.util.regex.Pattern
            |Pattern p = ~/123/
            |'''.stripMargin()

        def elem = assertCodeSelect([source], 'Pattern')
        assert elem.inferredElement instanceof ClassNode
    }

    @Test
    void testCodeSelectOnImportType2() {
        String source = '''\
            |import java.util.regex.Matcher
            |import java.util.regex.Pattern
            |Pattern p = ~/123/
            |'''.stripMargin()

        def elem = assertCodeSelect([source], 'Pattern')
        assert elem.inferredElement instanceof ClassNode
    }

    @Test
    void testCodeSelectOnImportType3() {
        String source = '''\
            |import java.lang.Thread.State
            |import java.util.regex.Pattern
            |Pattern p = ~/123/
            |'''.stripMargin()

        def elem = assertCodeSelect([source], 'State')
        assert elem.key == 'Ljava/lang/Thread$State;'
    }

    @Test
    void testCodeSelectOnImportType4() {
        String source = '''\
            |import java.lang.Thread.State
            |def p = ~/123/
            |'''.stripMargin()

        def elem = assertCodeSelect([source], 'Thread')
        assert elem.key == 'Ljava/lang/Thread;'
    }

    @Test
    void testCodeSelectOnImportType5() {
        String source = '''\
            |import java.lang.Thread.*
            |'''.stripMargin()

        def elem = assertCodeSelect([source], 'Thread')
        assert elem.key == 'Ljava/lang/Thread;'
    }

    @Test
    void testCodeSelectOnImportType6() {
        String source = '''\
            |import java.util.Map.Entry.*
            |'''.stripMargin()

        assertCodeSelect([source], 'Map')
        assertCodeSelect([source], 'Entry')
    }

    @Test
    void testCodeSelectOnImportPackage1() {
        String source = '''\
            |import java.util.regex.Pattern
            |Pattern p = ~/123/
            |'''.stripMargin()

        assertCodeSelect([source], 'regex', 'java.util.regex')
    }

    @Test
    void testCodeSelectOnImportPackage2() {
        String source = '''\
            |import java.lang.Thread.State
            |def p = ~/123/
            |'''.stripMargin()

        assertCodeSelect([source], 'lang', 'java.lang')
    }

    @Test
    void testCodeSelectOnImportPackage3() {
        String source = '''\
            |import java.util.regex.*
            |Pattern p = ~/123/
            |'''.stripMargin()

        assertCodeSelect([source], 'regex', 'java.util.regex')
    }

    @Test
    void testCodeSelectOnImportPackage4() {
        String source = '''\
            |import java.lang.Thread.*
            |def p = ~/123/
            |'''.stripMargin()

        assertCodeSelect([source], 'lang', 'java.lang')
    }

    @Test
    void testCodeSelectOnImportWildcard1() {
        String source = '''\
            |import java.util.regex.*
            |Pattern p = ~/123/
            |'''.stripMargin()

        assertCodeSelect([source], '*', null)
    }

    @Test
    void testCodeSelectOnImportWildcard2() {
        String source = '''\
            |import java.util.regex.*;
            |Pattern p = ~/123/
            |'''.stripMargin()

        assertCodeSelect([source], '*', null)
    }

    @Test
    void testCodeSelectOnImportWildcard3() {
        String source = '''\
            |import static java.util.regex.Pattern.*
            |def p = compile('123')
            |'''.stripMargin()

        assertCodeSelect([source], '*', null)
    }

    @Test
    void testCodeSelectOnImportWildcard4() {
        String source = '''\
            |import static java.util.regex.Pattern.*;
            |def p = compile('123')
            |'''.stripMargin()

        assertCodeSelect([source], '*', null)
    }
}
