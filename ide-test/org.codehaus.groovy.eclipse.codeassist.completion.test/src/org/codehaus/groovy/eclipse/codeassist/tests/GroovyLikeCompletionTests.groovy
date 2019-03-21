/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.tests

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 * Tests that completion proposals are sufficiently groovy-like in their output.
 */
final class GroovyLikeCompletionTests extends CompletionTestSuite {

    private static final String SCRIPTCONTENTS = '''\
        any
        clone
        findIndexOf
        inject
        class Foo {
          Foo(first, second) { }
          Foo(int third) { }
          def method1(arg) { }
          def method2(arg, Closure c1) { }
          def method3(arg, Closure c1, Closure c2) { }
        }
        new Foo()
        '''.stripIndent()
    private final static String CLOSURE_CONTENTS =
        "class Other {\n" +
        "    def first\n" +
        "    def second2() { } \n" +
        "}\n" +
        " \n" +
        "class MyOtherClass extends Other {\n" +
        "    def meth() {\n" +
        "        ''.with {\n" +
        "            substring(0)\n" +  // should find
        "            first\n" +  // should find
        "            second2()\n" +  // should find
        "            delegate.substring(0)\n" +  // should find
        "            delegate.first(0)\n" + // should not find
        "            delegate.second2(0)\n" + // should not find
        "            this.substring(0)\n" + // should not find
        "            this.first(0)\n" + // should find
        "            this.second2(0)\n" +  // should find
        "            wait\n" +  // should find 2 only
        "        }\n" +
        "    }\n" +
        "}"
    private final static String CLOSURE_CONTENTS2 =
        "class Other {\n" +
        "    def first\n" +
        "    def second2() { } \n" +
        "}\n" +
        "class Other2 extends Other { }\n" +
        "class MyOtherClass extends Other {\n" +
        "    def meth() {\n" +
        "        new Other2().foo {\n" +
        "            first\n" +  // should find 2 only
        "        }\n" +
        "    }\n" +
        "}"

    private final IPreferenceStore groovyPrefs = GroovyContentAssist.default.preferenceStore

    @Before
    void setUp() {
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_BRACKETS, true)
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_NOPARENS, true)
        groovyPrefs.setValue(GroovyContentAssist.NAMED_ARGUMENTS, false)
        groovyPrefs.setValue(GroovyContentAssist.PARAMETER_GUESSING, false)
        setJavaPreference(PreferenceConstants.TYPEFILTER_ENABLED, 'com.sun.*;sun.*')
    }

    @Test
    void testMethodWithClosure() {
        ICompletionProposal[] proposals = createProposalsAtOffset(SCRIPTCONTENTS, getIndexOf(SCRIPTCONTENTS, 'any'))
        checkReplacementString(proposals, 'any { it }', 1)
    }

    @Test
    void testMethodWithNoArgs() {
        ICompletionProposal[] proposals = createProposalsAtOffset(SCRIPTCONTENTS, getIndexOf(SCRIPTCONTENTS, 'clone'))
        checkReplacementString(proposals, 'clone()', 1)
    }

    @Test
    void testMethodWith2Args() {
        ICompletionProposal[] proposals = createProposalsAtOffset(SCRIPTCONTENTS, getIndexOf(SCRIPTCONTENTS, 'findIndexOf'))
        checkReplacementRegexp(proposals, /findIndexOf\(\p{javaJavaIdentifierStart}\p{javaJavaIdentifierPart}*\) \{ it \}/, 1)
    }

    @Test
    void testMethodWithClosureNotGroovyLike() {
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_BRACKETS, false)
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)

        ICompletionProposal[] proposals = createProposalsAtOffset(SCRIPTCONTENTS, getIndexOf(SCRIPTCONTENTS, 'any'))
        checkReplacementRegexp(proposals, /any\(\p{javaJavaIdentifierStart}\p{javaJavaIdentifierPart}*\)/, 1)
    }

    @Test
    void testMethodWith2ArgsNotGroovyLike() {
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_BRACKETS, false)
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)

        ICompletionProposal[] proposals = createProposalsAtOffset(SCRIPTCONTENTS, getIndexOf(SCRIPTCONTENTS, 'findIndexOf'))
        checkReplacementRegexp(proposals, /findIndexOf\(\p{javaJavaIdentifierStart}\p{javaJavaIdentifierPart}*, \p{javaJavaIdentifierStart}\p{javaJavaIdentifierPart}*\)/, 1)
    }

    @Test
    void testClosureApplication1a() {
        addGroovySource(SCRIPTCONTENTS, nextUnitName())

        String contents = 'new Foo().method1'
        String expected = 'new Foo().method1(arg)'
        checkProposalApplicationNonType(contents, expected, contents.length(), 'method1')
    }

    @Test
    void testClosureApplication1b() {
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)
        addGroovySource(SCRIPTCONTENTS, nextUnitName())

        String contents = 'new Foo().method1'
        String expected = 'new Foo().method1(arg)'
        checkProposalApplicationNonType(contents, expected, contents.length(), 'method1')
    }

    @Test
    void testClosureApplication1c() {
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_BRACKETS, false)
        addGroovySource(SCRIPTCONTENTS, nextUnitName())

        String contents = 'new Foo().method1'
        String expected = 'new Foo().method1(arg)'
        checkProposalApplicationNonType(contents, expected, contents.length(), 'method1')
    }

    @Test
    void testClosureApplication1d() {
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_BRACKETS, false)
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)
        addGroovySource(SCRIPTCONTENTS, nextUnitName())

        String contents = 'new Foo().method1'
        String expected = 'new Foo().method1(arg)'
        checkProposalApplicationNonType(contents, expected, contents.length(), 'method1')
    }

    @Test
    void testClosureApplication2a() {
        addGroovySource(SCRIPTCONTENTS, nextUnitName())

        String contents = 'new Foo().method2'
        String expected = 'new Foo().method2(arg) { it }'
        checkProposalApplicationNonType(contents, expected, contents.length(), 'method2')
    }

    @Test
    void testClosureApplication2b() {
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)
        addGroovySource(SCRIPTCONTENTS, nextUnitName())

        String contents = 'new Foo().method2'
        String expected = 'new Foo().method2(arg, { it })'
        checkProposalApplicationNonType(contents, expected, contents.length(), 'method2')
    }

    @Test
    void testClosureApplication2c() {
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_BRACKETS, false)
        addGroovySource(SCRIPTCONTENTS, nextUnitName())

        String contents = 'new Foo().method2'
        String expected = 'new Foo().method2(arg) c1'
        checkProposalApplicationNonType(contents, expected, contents.length(), 'method2')
    }

    @Test
    void testClosureApplication2d() {
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_BRACKETS, false)
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)
        addGroovySource(SCRIPTCONTENTS, nextUnitName())

        String contents = 'new Foo().method2'
        String expected = 'new Foo().method2(arg, c1)'
        checkProposalApplicationNonType(contents, expected, contents.length(), 'method2')
    }

    @Test
    void testClosureApplication3a() {
        addGroovySource(SCRIPTCONTENTS, nextUnitName())

        String contents = 'new Foo().method3'
        String expected = 'new Foo().method3(arg, { it }) { it }'
        checkProposalApplicationNonType(contents, expected, contents.length(), 'method3')
    }

    @Test
    void testClosureApplication3b() {
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)
        addGroovySource(SCRIPTCONTENTS, nextUnitName())

        String contents = 'new Foo().method3'
        String expected = 'new Foo().method3(arg, { it }, { it })'
        checkProposalApplicationNonType(contents, expected, contents.length(), 'method3')
    }

    @Test
    void testClosureApplication3c() {
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_BRACKETS, false)
        addGroovySource(SCRIPTCONTENTS, nextUnitName())

        String contents = 'new Foo().method3'
        String expected = 'new Foo().method3(arg, c1) c2'
        checkProposalApplicationNonType(contents, expected, contents.length(), 'method3')
    }

    @Test
    void testClosureApplication3d() {
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_BRACKETS, false)
        groovyPrefs.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)
        addGroovySource(SCRIPTCONTENTS, nextUnitName())

        String contents = 'new Foo().method3'
        String expected = 'new Foo().method3(arg, c1, c2)'
        checkProposalApplicationNonType(contents, expected, contents.length(), 'method3')
    }

    @Test // accessing members of super types in closures
    void testClosureCompletion1() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CLOSURE_CONTENTS, getLastIndexOf(CLOSURE_CONTENTS, ' substring'))
        checkReplacementString(proposals, 'substring(beginIndex)', 1)
    }

    @Test // accessing members of super types in closures
    void testClosureCompletion2() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CLOSURE_CONTENTS, getLastIndexOf(CLOSURE_CONTENTS, ' first'))
        checkReplacementString(proposals, 'first', 1)
    }

    @Test // accessing members of super types in closures
    void testClosureCompletion3() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CLOSURE_CONTENTS, getLastIndexOf(CLOSURE_CONTENTS, ' second2'))
        checkReplacementString(proposals, 'second2()', 1)
    }

    @Test // accessing members of super types in closures
    void testClosureCompletion4() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CLOSURE_CONTENTS, getLastIndexOf(CLOSURE_CONTENTS, 'delegate.substring'))
        checkReplacementString(proposals, 'substring(beginIndex)', 1)
    }

    @Test // accessing members of super types in closures
    void testClosureCompletion5() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CLOSURE_CONTENTS, getLastIndexOf(CLOSURE_CONTENTS, 'delegate.first'))
        checkReplacementString(proposals, 'first', 0)
    }

    @Test // accessing members of super types in closures
    void testClosureCompletion6() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CLOSURE_CONTENTS, getLastIndexOf(CLOSURE_CONTENTS, 'delegate.second2'))
        checkReplacementString(proposals, 'second2', 0)
    }

    @Test // accessing members of super types in closures
    void testClosureCompletion7() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CLOSURE_CONTENTS, getLastIndexOf(CLOSURE_CONTENTS, 'this.substring'))
        checkReplacementString(proposals, 'substring', 0)
    }

    @Test // accessing members of super types in closures
    void testClosureCompletion8() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CLOSURE_CONTENTS, getLastIndexOf(CLOSURE_CONTENTS, 'this.first'))
        checkReplacementString(proposals, 'first', 1)
    }

    @Test // accessing members of super types in closures
    void testClosureCompletion9() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CLOSURE_CONTENTS, getLastIndexOf(CLOSURE_CONTENTS, 'this.second2'))
        checkReplacementString(proposals, 'second2()', 1)
    }

    @Test // accessing members of super types in closures
    void testClosureCompletion10() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CLOSURE_CONTENTS, getLastIndexOf(CLOSURE_CONTENTS, 'wait'))
        checkReplacementString(proposals, 'wait()', 1)
    }

    @Test // accessing members of super types in closures
    void testClosureCompletion11() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CLOSURE_CONTENTS2, getLastIndexOf(CLOSURE_CONTENTS2, 'first'))
        checkReplacementString(proposals, 'first', 1)
    }

    @Test
    void testNamedArguments0() {
        groovyPrefs.setValue(GroovyContentAssist.NAMED_ARGUMENTS, true)

        ICompletionProposal[] proposals = createProposalsAtOffset(SCRIPTCONTENTS, getIndexOf(SCRIPTCONTENTS, 'clone'))
        checkReplacementString(proposals, 'clone()', 1)
    }

    @Ignore @Test
    void testNamedArguments1() {
        groovyPrefs.setValue(GroovyContentAssist.NAMED_ARGUMENTS, true)

        ICompletionProposal[] proposals = createProposalsAtOffset(SCRIPTCONTENTS, getIndexOf(SCRIPTCONTENTS, 'new Foo'))
        checkReplacementString(proposals, '(first:first, second:second)', 1)
    }

    @Ignore @Test
    void testNamedArguments2() {
        groovyPrefs.setValue(GroovyContentAssist.NAMED_ARGUMENTS, true)

        ICompletionProposal[] proposals = createProposalsAtOffset(SCRIPTCONTENTS, getIndexOf(SCRIPTCONTENTS, 'new Foo'))
        checkReplacementString(proposals, '(third:third)', 1)
    }

    @Ignore @Test // GRECLIPSE-268
    void testGString1() {
        ICompletionProposal[] proposals = createProposalsAtOffset('""""""', 3)
        assert proposals.length == 0 : 'Should not have found any proposals, but found:\n' + printProposals(proposals)
    }

    @Test // GRECLIPSE-268
    void testGString2() {
        ICompletionProposal[] proposals = createProposalsAtOffset('"""${this}"""', 3)
        assert proposals.length == 0 : 'Should not have found any proposals, but found:\n' + printProposals(proposals)
    }

    @Test // GRECLIPSE-268
    void testGString3() {
        ICompletionProposal[] proposals = createProposalsAtOffset('"""this"""', '"""this'.length())
        assert proposals.length == 0 : 'Should not have found any proposals, but found:\n' + printProposals(proposals)
    }

    @Test // GRECLIPSE-268
    void testGString4() {
        String contents = 'def flarb;\n"""${flarb}"""'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '${flarb'))
        checkReplacementString(proposals, 'flarb', 1)
    }
}
