/*
 * Copyright 2009-2016 the original author or authors.
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

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode

final class CodeSelectImportsTests extends BrowsingTestCase {

    static junit.framework.Test suite() {
        newTestSuite(CodeSelectImportsTests)
    }

    void testCodeSelectOnImportType() {
        String source = '''\
            import java.util.regex.Pattern
            Pattern p = ~/123/
            '''.stripIndent()

        def elem = assertCodeSelect([source], 'Pattern')
        assert elem.inferredElement instanceof ClassNode
    }

    void testCodeSelectOnImportType1a() {
        String source = '''\
            import java.util.regex.Matcher
            import java.util.regex.Pattern
            Pattern p = ~/123/
            '''.stripIndent()

        def elem = assertCodeSelect([source], 'Pattern')
        assert elem.inferredElement instanceof ClassNode
    }

    void testCodeSelectOnImportType2() {
        String source = '''\
            import java.lang.Thread.State
            Pattern p = ~/123/
            '''.stripIndent()

        def elem = assertCodeSelect([source], 'State')
        assert elem.inferredElement instanceof ClassNode
    }

    void testCodeSelectOnImportType2a() {
        String source = '''\
            import java.lang.Thread.State
            def p = ~/123/
            '''.stripIndent()

        assertCodeSelect([source], 'Thread')
    }

    void testCodeSelectOnImportPackage1() {
        String source = '''\
            import java.util.regex.Pattern
            Pattern p = ~/123/
            '''.stripIndent()

        assertCodeSelect([source], 'regex', 'java.util.regex')
    }

    void testCodeSelectOnImportPackage1a() {
        String source = '''\
            import java.lang.Thread.State
            def p = ~/123/
            '''.stripIndent()

        assertCodeSelect([source], 'lang', 'java.lang')
    }

    void testCodeSelectOnImportPackage2() {
        String source = '''\
            import java.util.regex.*
            Pattern p = ~/123/
            '''.stripIndent()

        assertCodeSelect([source], 'regex', 'java.util.regex')
    }

    void testCodeSelectOnImportPackage2a() {
        String source = '''\
            import java.lang.Thread.*
            def p = ~/123/
            '''.stripIndent()

        assertCodeSelect([source], 'lang', 'java.lang')
    }

    void testCodeSelectOnImportWildcard() {
        String source = '''\
            import java.util.regex.*
            Pattern p = ~/123/
            '''.stripIndent()

        assertCodeSelect([source], '*', '')
    }
}
