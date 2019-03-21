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

import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Test

/**
 * Tests that Local variable completions are working properly.
 * They should only be active when inside a script or in a closure.
 */
final class LocalVariableCompletionTests extends CompletionTestSuite {

    private static final String CONTENTS = 'class LocalsClass { public LocalsClass() {\n }\n void doNothing(int x) { def xxx\n def xx\n def y = { t -> print t\n }\n } }'
    private static final String SCRIPTCONTENTS = 'def xx = 9\ndef xxx\ndef y = { t -> print t\n }\n'
    private static final String SCRIPTCONTENTS2 = 'def xx = 9\ndef xxx\ndef y = { t -> print t\n.toString() }\n'
    private static final String SELFREFERENCINGSCRIPT = 'def xx = 9\nxx = xx\nxx.abs()'

    private ICompilationUnit createJava() {
        addJavaSource(CONTENTS, nextUnitName())
    }

    private ICompilationUnit createGroovy() {
        addGroovySource(CONTENTS, nextUnitName())
    }

    private ICompilationUnit createGroovyForScript() {
        addGroovySource(SCRIPTCONTENTS, nextUnitName())
    }

    private ICompilationUnit createGroovyForScript2() {
        addGroovySource(SCRIPTCONTENTS2, nextUnitName())
    }

    private ICompilationUnit createGroovyForSelfReferencingScript() {
        addGroovySource(SELFREFERENCINGSCRIPT, nextUnitName())
    }

    //

    @Test // should not find local vars here
    void testLocalVarsInJavaFile() {
        ICompilationUnit unit = createJava()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(CONTENTS, 'y\n'))
        proposalExists(proposals, 'xxx', 0)
        proposalExists(proposals, 'xx', 0)
        proposalExists(proposals, 'y', 0)
    }

    @Test // should not find local vars here.. They are calculated by JDT
    void testLocalVarsInGroovyFile() {
        ICompilationUnit unit = createGroovy()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(CONTENTS, 'y\n'))
        proposalExists(proposals, 'xxx', 0)
        proposalExists(proposals, 'xx', 0)
        proposalExists(proposals, 'y', 0)
    }

    @Test // should find local vars here
    void testLocalVarsInScript() {
        ICompilationUnit unit = createGroovyForScript()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(SCRIPTCONTENTS, '}\n'))
        proposalExists(proposals, 'xxx', 1)
        proposalExists(proposals, 'xx', 1)
        proposalExists(proposals, 'y', 1)
    }

    @Test // should find local vars here
    void testLocalVarsInClosureInScript() {
        ICompilationUnit unit = createGroovyForScript()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(SCRIPTCONTENTS, 'print t\n'))
        proposalExists(proposals, 'xxx', 1)
        proposalExists(proposals, 'xx', 1)
        proposalExists(proposals, 'y', 1)
    }

    @Test // should not find local vars here
    void testLocalVarsInClosureInScript2() {
        ICompilationUnit unit = createGroovyForScript2()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(SCRIPTCONTENTS2, 'print t\n.toStr'))
        proposalExists(proposals, 'xxx', 0)
        proposalExists(proposals, 'xx', 0)
        proposalExists(proposals, 'y', 0)
    }

    @Test// should find local vars here
    void testLocalVarsInClosureInMethod() {
        ICompilationUnit unit = createGroovy()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(CONTENTS, 'print t\n'))
        proposalExists(proposals, 'xxx', 1)
        proposalExists(proposals, 'xx', 1)
        proposalExists(proposals, 'y', 1)
    }

    @Test // GRECLIPSE-369
    void testSelfReferencingLocalVar() {
        ICompilationUnit unit = createGroovyForSelfReferencingScript()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getLastIndexOf(SELFREFERENCINGSCRIPT, 'xx.'))
        proposalExists(proposals, 'abs', 1)
    }

    @Test // GRECLIPSE-1267
    void testClsoureVar1() {
        String contents = 'def x = { o }'
        String expected = 'def x = { owner }'
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, '{ o'), 'owner')
    }

    @Test // GRECLIPSE-1267
    void testClsoureVar2() {
        String contents = 'def x = { d }'
        String expected = 'def x = { delegate }'
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, '{ d'), 'delegate')
    }

    @Test // GRECLIPSE-1267
    void testClsoureVar3() {
        String contents = 'def x = { getO }'
        String expected = 'def x = { getOwner() }'
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, '{ getO'), 'getOwner')
    }

    @Test // GRECLIPSE-1267
    void testClsoureVar4() {
        String contents = 'def x = { getD }'
        String expected = 'def x = { getDelegate() }'
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, '{ getD'), 'getDelegate')
    }

    @Test // GRECLIPSE-1387
    void testClsoureVar4a() {
        String contents = 'def x = { thisO }'
        String expected = 'def x = { thisObject }'
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, '{ thisO'), 'thisObject')
    }

    @Test // GRECLIPSE-1267
    void testClsoureVar5() {
        String contents = 'o\nd\nge'
        ICompilationUnit unit = addGroovySource(contents, nextUnitName())
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(contents, 'o'))
        proposalExists(proposals, 'owner', 0)
        proposals = createProposalsAtOffset(unit, getIndexOf(contents, 'd'))
        proposalExists(proposals, 'delegate', 0)
        proposals = createProposalsAtOffset(unit, getIndexOf(contents, 'ge'))
        proposalExists(proposals, 'getDelegate', 0)
        proposalExists(proposals, 'getOwner', 0)
    }

    @Test
    void testDeclaredVar() {
        String contents = 'def xxx = new ArrayList(xx)'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'xx'))
        proposalExists(proposals, 'xxx', 0) // declared variable should not be proposed within its own initializer
    }
}
