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

import groovy.transform.NotYetImplemented

import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer
import org.eclipse.jface.text.Document
import org.junit.Test

final class ContentAssistLocationTests extends CompletionTestSuite {

    private void assertLocation(String contents, int offset, ContentAssistLocation location) {
        def unit = addGroovySource(contents, nextUnitName())
        def context = new GroovyCompletionProposalComputer().createContentAssistContext(unit, offset, new Document(unit.buffer.contents))

        assert context.location == location : "Invalid location at index $offset in text:\n$contents"
    }

    @Test
    void testStatement1() {
        assertLocation('', 0, ContentAssistLocation.SCRIPT)
    }

    @Test
    void testStatement2() {
        // This is technically a bug, but I actually want this to be
        // the expected behaviour since having the extra completions
        // available from script can be annoying
        assertLocation('a', 1, ContentAssistLocation.STATEMENT/*SCRIPT*/)
    }

    @Test
    void testStatement3() {
        String contents = 'a.g()'
        assertLocation(contents, contents.indexOf(')') + 1, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement4() {
        String contents = 'def x = { a.g() }'
        assertLocation(contents, contents.indexOf(')') + 1, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement5() {
        assertLocation('a\n', 2, ContentAssistLocation.SCRIPT)
    }

    @Test
    void testStatement6() {
        // This is technically a bug, but I actually want this to be
        // the expected behaviour since having the extra completions
        // available from script can be annoying
        assertLocation('a\na', 3, ContentAssistLocation.STATEMENT/*SCRIPT*/)
    }

    @Test
    void testStatement7() {
        String contents = 'def x = { }'
        assertLocation(contents, contents.indexOf('{') + 1, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement8() {
        String contents = 'def x() { }'
        assertLocation(contents, contents.indexOf('{') + 1, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement9() {
        String contents = 'class Blar { def x() { } }'
        assertLocation(contents, contents.indexOf('}'), ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement10() {
        String contents = 'class Blar { def x = { } }'
        assertLocation(contents, contents.indexOf('}'), ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement11() {
        String contents = 'def x = { a.g(    c,b) }'
        assertLocation(contents, contents.indexOf('c') + 1, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement12() {
        String contents = 'def x = { a.g a, b }'
        assertLocation(contents, contents.indexOf('b') + 1, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement13() {
        String contents = 'a.g a, b'
        assertLocation(contents, contents.indexOf('b') + 1, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement14() {
        String contents = 'a()'
        assertLocation(contents, contents.indexOf('a') + 1, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement15() {
        String contents = 'b a()'
        assertLocation(contents, contents.indexOf('a') + 1, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testStatement16() {
        String contents = 'new ArrayList(a,b)'
        assertLocation(contents, contents.indexOf(')') + 1, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testExpression() {
        String contents = 'a.g a, a.b'
        assertLocation(contents, contents.indexOf('b') + 1, ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testMethodContext1() {
        String contents = 'a.g()'
        assertLocation(contents, contents.indexOf('(') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext2() {
        String contents = 'def x = { a.g() }'
        assertLocation(contents, contents.indexOf('(') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext3() {
        String contents = 'def x = { a.g(a,b) }'
        assertLocation(contents, contents.indexOf('(') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext5() {
        String contents = 'def x = { a.g(    c,b) }'
        assertLocation(contents, contents.indexOf(',') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext6() {
        String contents = 'def x = { a.g(    c,\nb) }'
        assertLocation(contents, contents.indexOf(',') + 2, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext7() {
        String contents = 'a.g a, a.b'
        assertLocation(contents, contents.indexOf('g') + 2, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext8() {
        String contents = 'a.g a, a.b'
        assertLocation(contents, contents.indexOf(',') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext9() {
        String contents = 'a.g a, a.b'
        assertLocation(contents, contents.indexOf(',') + 2, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext10() {
        String contents = 'new ArrayList()'
        assertLocation(contents, contents.indexOf('(') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext11() {
        String contents = 'new ArrayList(a)'
        assertLocation(contents, contents.indexOf('(') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext12() {
        String contents = 'new ArrayList(a,b)'
        assertLocation(contents, contents.indexOf(',') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext13() {
        String contents = 'new ArrayList(a,b)'
        // see https://github.com/groovy/groovy-eclipse/issues/331
        assertLocation(contents, contents.indexOf('b') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext14() {
        String contents = 'new ArrayList<String>()'
        assertLocation(contents, contents.indexOf('(') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext15() {
        String contents = 'new ArrayList<String>(a)'
        assertLocation(contents, contents.indexOf('(') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext16() {
        String contents = 'new ArrayList<String>(a,b)'
        assertLocation(contents, contents.indexOf(',') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext17() {
        String contents = 'foo \nh'
        assertLocation(contents, contents.indexOf('foo ') + 4, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext18() {
        String contents = 'foo a, \nh'
        assertLocation(contents, contents.indexOf(', ') + 2, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext19() {
        String contents = 'foo a, b \nh'
        assertLocation(contents, contents.indexOf('b ') + 2, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext20() {
        String contents = 'foo (a, b )\nh'
        assertLocation(contents, contents.indexOf('b ') + 2, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testMethodContext21() {
        String contents = 'foo (a, )\nh'
        assertLocation(contents, contents.indexOf(', ') + 1, ContentAssistLocation.METHOD_CONTEXT)
    }

    @Test
    void testExpression1() {
        assertLocation('a.a', 3, ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression2() {
        assertLocation('a.', 2, ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression3() {
        assertLocation('a.\n', 3, ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression4() {
        String contents = 'a.// \n'
        assertLocation(contents, contents.length(), ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression5() {
        String contents = 'a.g(b.)// \n'
        assertLocation(contents, contents.indexOf('b.') + 2, ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression6() {
        String contents = 'def x = { a.g(    z.c,\nb) }'
        assertLocation(contents, contents.indexOf('c') + 1, ContentAssistLocation.EXPRESSION)
    }

    @Test
    void testExpression7() {
        String contents = 'def x = { a.g(    c,\nz.b) }'
        assertLocation(contents, contents.indexOf('b') + 1, ContentAssistLocation.EXPRESSION)
    }

    @Test @NotYetImplemented
    void testImport1() {
        String contents = 'import '
        int loc = contents.indexOf('import ') + 'import '.length()
        assertLocation(contents, loc, ContentAssistLocation.IMPORT)
    }

    @Test
    void testImport2() {
        String contents = 'import T'
        int loc = contents.indexOf('import T') + 'import T'.length()
        assertLocation(contents, loc, ContentAssistLocation.IMPORT)
    }

    @Test @NotYetImplemented
    void testImport3() {
        String contents = 'package '
        int loc = contents.indexOf('package ') + 'package '.length()
        assertLocation(contents, loc, ContentAssistLocation.IMPORT)
    }

    @Test
    void testImport4() {
        String contents = 'package T'
        int loc = contents.indexOf('package T') + 'package T'.length()
        assertLocation(contents, loc, ContentAssistLocation.PACKAGE)
    }

    @Test
    void testClassBody1() {
        String contents = 'class A { }'
        int loc = contents.indexOf('{') + 1
        assertLocation(contents, loc, ContentAssistLocation.CLASS_BODY)
    }

    @Test
    void testClassBody2() {
        String contents = 'class A { t }'
        int loc = contents.indexOf('t') + 1
        assertLocation(contents, loc, ContentAssistLocation.CLASS_BODY)
    }

    @Test
    void testClassBody3() {
        String contents = 'class A { void t }'
        int loc = contents.indexOf('t') + 1
        assertLocation(contents, loc, ContentAssistLocation.CLASS_BODY)
    }

    @Test
    void testClassBody4() {
        String contents = 'class A extends T { void t }'
        int loc = contents.indexOf('ds') + 4
        assertLocation(contents, loc, ContentAssistLocation.EXTENDS)
    }

    @Test
    void testClassBody5() {
        String contents = 'class A extends ArrayList { void t }'
        int loc = contents.indexOf('Arr') + 3
        assertLocation(contents, loc, ContentAssistLocation.EXTENDS)
    }

    @Test
    void testClassBody6() {
        String contents = 'class A extends ArrayList implements T { void t }'
        int loc = contents.indexOf('ents ') + 6
        assertLocation(contents, loc, ContentAssistLocation.IMPLEMENTS)
    }

    @Test
    void testClassBody7() {
        String contents = 'class A extends ArrayList implements Li { void t }'
        int loc = contents.indexOf(' Li') + 3
        assertLocation(contents, loc, ContentAssistLocation.IMPLEMENTS)
    }

    @Test
    void testClassBody8() {
        String contents = 'class A extends ArrayList implements Foo, Li { void t }'
        int loc = contents.indexOf(' Li') + 3
        assertLocation(contents, loc, ContentAssistLocation.IMPLEMENTS)
    }

    @Test
    void testClassBody9() {
        String contents = 'class A { void t \n }'
        int loc = contents.indexOf('\n') + 1
        assertLocation(contents, loc, ContentAssistLocation.CLASS_BODY)
    }

    @Test
    void testParameters1() {
        String contents = 'class A { void t() {} }'
        int loc = contents.indexOf('(') + 1
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER)
    }

    @Test
    void testParameters2() {
        String contents = 'class A { void t(v) {} }'
        int loc = contents.indexOf('(v') + 2
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER)
    }

    @Test // this one should not propose anything
    void testParameters3() {
        String contents = 'class A { void t(v y) {} }'
        int loc = contents.indexOf('(v y') + 4
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER)
    }

    @Test
    void testParameters4() {
        String contents = 'class A { void t(v y = hh) {} }'
        int loc = contents.indexOf('=') + 1
        assertLocation(contents, loc, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testParameters5() {
        String contents = 'class A { void t(v y = hh) {} }'
        int loc = contents.indexOf('hh') + 1
        assertLocation(contents, loc, ContentAssistLocation.STATEMENT)
    }

    @Test
    void testParameters6() {
        String contents = 'class A { def t = {v -> hh } }'
        int loc = contents.indexOf('v') + 1
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER)
    }

    @Test
    void testParameters7() {
        String contents = 'class A { def t = {v y -> hh } }'
        int loc = contents.indexOf('v') + 1
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER)
    }

    @Test
    void testParameters8() {
        String contents = 'class A { def t = {v y -> hh } }'
        int loc = contents.indexOf('y') + 1
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER)
    }

    @Test
    void testExceptions1() {
        String contents = 'class A { void t(v y = hh) throws Ex {} }'
        int loc = contents.indexOf('Ex') + 1
        assertLocation(contents, loc, ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions2() {
        String contents = 'class A { void t(v y = hh) throws T {} }'
        int loc = contents.indexOf('ws ') + 4
        assertLocation(contents, loc, ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions3() {
        String contents = 'class A { void t(v y = hh) throws Ex, T {} }'
        int loc = contents.indexOf(', ') + 3
        assertLocation(contents, loc, ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testExceptions4() {
        String contents = 'class A { void t(v y = hh) throws Ex, Th {} }'
        int loc = contents.indexOf('Th') + 2
        assertLocation(contents, loc, ContentAssistLocation.EXCEPTIONS)
    }

    @Test
    void testAnnotation1() {
        String contents = '@ class A { void t(v y = hh) {} }'
        int loc = contents.indexOf('@') + 1
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation2() {
        String contents = '@A class A { void t(v y = hh) {} }'
        int loc = contents.indexOf('@A') + 2
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation2a() {
        String contents = '@B @A class A { void t(v y = hh) {} }'
        int loc = contents.indexOf('@A') + 2
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation2b() {
        String contents = '@B @A @C class A { void t(v y = hh) {} }'
        int loc = contents.indexOf('@A') + 2
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation2d() {
        String contents = '@B @ @C class A { void t(v y = hh) {} }'
        int loc = contents.indexOf('@') + 1
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test @NotYetImplemented
    void testAnnotation3() {
        String contents = ' class A { @ void t(v y = hh) {} }'
        int loc = contents.indexOf('@') + 1
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation4() {
        String contents = ' class A { @A void t(v y = hh) {} }'
        int loc = contents.indexOf('@A') + 2
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test @NotYetImplemented
    void testAnnotation5() {
        String contents = ' class A { @ def t }'
        int loc = contents.indexOf('@') + 1
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation6() {
        String contents = ' class A { @A void t }'
        int loc = contents.indexOf('@A') + 2
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }

    @Test
    void testAnnotation7() {
        String contents = '@A import java.util.List\n class A { }'
        int loc = contents.indexOf('@A') + 2
        assertLocation(contents, loc, ContentAssistLocation.ANNOTATION)
    }
}
