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
package org.codehaus.groovy.eclipse.dsl.tests

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy
import static org.junit.Assume.assumeFalse
import static org.junit.Assume.assumeTrue

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist
import org.codehaus.groovy.eclipse.codeassist.tests.CompletionTestSuite
import org.codehaus.groovy.eclipse.dsl.DSLPreferencesInitializer
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator
import org.eclipse.core.resources.IFile
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

final class DSLContentAssistTests extends CompletionTestSuite {

    @BeforeClass
    static void setUpTests() {
        GroovyDSLCoreActivator.default.preferenceStore.setValue(DSLPreferencesInitializer.AUTO_ADD_DSL_SUPPORT, false)
    }

    @Before
    void setUp() {
        assumeTrue(!GroovyDSLCoreActivator.default.isDSLDDisabled())
        addClasspathContainer(GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID)
        withProject { project ->
            GroovyDSLCoreActivator.default.contextStoreManager.initialize(project, true)
        }

        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'true')
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'true')
    }

    private void createDsld(CharSequence dsld) {
        IFile file = addPlainText(dsld, "${nextUnitName()}.dsld")
        assert file.exists() : "File $file just created, but doesn't exist"
    }

    //--------------------------------------------------------------------------

    @Test
    void testAssignedVariable1() {
        createDsld '''\
            |contribute(bind(exprs: assignedVariable())) {
            |  property name: 'var_' + exprs[0].leftExpression.name
            |}
            |'''.stripMargin()

        String contents = '''\
            |def foo = v
            |'''.stripMargin()
        checkUniqueProposal(contents, 'v', 'var_foo')
    }

    @Test
    void testAssignedVariable2() {
        createDsld '''\
            |contribute(bind(exprs: assignedVariable('foo'))) {
            |  property name: 'var_' + exprs[0].leftExpression.name
            |}
            |'''.stripMargin()

        String contents = '''\
            |def foo = v
            |'''.stripMargin()
        checkUniqueProposal(contents, 'v', 'var_foo')
    }

    @Test
    void testAssignedVariable2a() {
        createDsld '''\
            |contribute(bind(exprs: assignedVariable('boo'))) {
            |  property name: 'var_' + exprs[0].leftExpression.name
            |}
            |'''.stripMargin()

        String contents = '''\
            |def foo = v
            |'''.stripMargin()
        proposalExists(createProposalsAtOffset(contents, getIndexOf(contents, 'v')), 'var_foo', 0)
    }

    @Test
    void testAssignedVariable3() {
        createDsld '''\
            |contribute(bind(exprs: assignedVariable(~/f.*/))) {
            |  property name: 'var_' + exprs[0].leftExpression.name
            |}
            |'''.stripMargin()

        String contents = '''\
            |def foo = v
            |'''.stripMargin()
        checkUniqueProposal(contents, 'v', 'var_foo')
    }

    @Test
    void testAssignedVariable3a() {
        createDsld '''\
            |contribute(bind(exprs: assignedVariable(~/b.*/))) {
            |  property name: 'var_' + exprs[0].leftExpression.name
            |}
            |'''.stripMargin()

        String contents = '''\
            |def foo = v
            |'''.stripMargin()
        proposalExists(createProposalsAtOffset(contents, getIndexOf(contents, 'v')), 'var_foo', 0)
    }

    @Test
    void testAssignedVariable4() {
        createDsld '''\
            |contribute(bind(exprs: assignedVariable(name('foo')))) {
            |  property name: 'var_' + exprs[0].leftExpression.name
            |}
            |'''.stripMargin()

        String contents = '''\
            |def foo = v
            |'''.stripMargin()
        checkUniqueProposal(contents, 'v', 'var_foo')
    }

    @Test
    void testAssignedVariable4a() {
        createDsld '''\
            |contribute(bind(exprs: assignedVariable(name('boo')))) {
            |  property name: 'var_' + exprs[0].leftExpression.name
            |}
            |'''.stripMargin()

        String contents = '''\
            |def foo = v
            |'''.stripMargin()
        proposalExists(createProposalsAtOffset(contents, getIndexOf(contents, 'v')), 'var_foo', 0)
    }

    @Test
    void testAssignedVariable5() {
        createDsld '''\
            |contribute(bind(exprs: assignedVariable(type(BigInteger)))) {
            |  property name: 'var_' + exprs[0].leftExpression.name
            |}
            |'''.stripMargin()

        String contents = '''\
            |BigInteger foo = v
            |'''.stripMargin()
        checkUniqueProposal(contents, 'v', 'var_foo')
    }

    @Test
    void testAssignedVariable5a() {
        createDsld '''\
            |contribute(bind(exprs: assignedVariable(type(BigInteger)))) {
            |  property name: 'var_' + exprs[0].leftExpression.name
            |}
            |'''.stripMargin()

        String contents = '''\
            |def foo = v
            |'''.stripMargin()
        proposalExists(createProposalsAtOffset(contents, getIndexOf(contents, 'v')), 'var_foo', 0)
    }

    @Test
    void testAssignedVariable6() {
        createDsld '''\
            |contribute(bind(exprs: assignedVariable())) {
            |  property name: 'var_' + exprs[0].leftExpression.name
            |}
            |'''.stripMargin()

        String contents = 'def foo = '
        checkUniqueProposal(contents, '= ', 'var_foo')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/600
    void testAssignedVariable6a() {
        createDsld '''\
            |contribute(bind(exprs: assignedVariable())) {
            |  property name: 'var_' + exprs[0].leftExpression.name
            |}
            |'''.stripMargin()

        String contents = 'foo = '
        checkUniqueProposal(contents, '= ', 'var_foo')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/598
    void testAssignedVariable7() {
        createDsld '''\
            |contribute(bind(exprs: assignedVariable())) {
            |  property name: 'var_' + exprs[0].leftExpression.name
            |}
            |'''.stripMargin()

        String contents = '''\
            |def foo = { }
            |'''.stripMargin()
        checkUniqueProposal(contents, '{ ', 'var_foo')
    }

    @Test
    void testAssignedVariable7a() {
        createDsld '''\
            |contribute(bind(exprs: assignedVariable())) {
            |  property name: 'var_' + exprs[0].leftExpression.name
            |}
            |'''.stripMargin()

        String contents = '''\
            |foo = { }
            |'''.stripMargin()
        checkUniqueProposal(contents, '{ ', 'var_foo')
    }

    @Test
    void testAssignedVariable8() {
        createDsld '''\
            |contribute(bind(exprs: assignedVariable())) {
            |  property name: 'var_' + exprs[0].leftExpression.name
            |}
            |'''.stripMargin()

        String contents = '''\
            |def foo = {
            |  bar {
            |    baz {
            |    }
            |  }
            |}
            |'''.stripMargin()
        checkUniqueProposal(contents, 'baz {', 'var_foo')
    }

    @Test
    void testAssignedVariable8a() {
        createDsld '''\
            |contribute(bind(exprs: assignedVariable())) {
            |  property name: 'var_' + exprs[0].leftExpression.name
            |}
            |'''.stripMargin()

        String contents = '''\
            |foo = {
            |  bar {
            |    baz {
            |    }
            |  }
            |}
            |'''.stripMargin()
        checkUniqueProposal(contents, 'baz {', 'var_foo')
    }

    @Test
    void testDelegatesToNoParens1() {
        createDsld '''\
            |contribute(currentType('Inner')) {
            |  delegatesTo type: 'Other', noParens: true
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Other {
            |  def blart(a, b, c) { }
            |  def flart(a) { }
            |}
            |class Inner { }
            |def val = new Inner()
            |val.bl
            |'''.stripMargin()
        ICompletionProposal proposal = checkUniqueProposal(contents, 'val.bl', 'blart', 'blart val, val, val')
        applyProposalAndCheck(proposal, contents.replace('val.bl', 'val.blart val, val, val'))
    }

    @Test
    void testDelegatesToNoParens2() {
        createDsld '''\
            |contribute(currentType('Inner')) {
            |  delegatesTo type: 'Other', noParens: true
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Other {
            |  def blart(a, b, c) { }
            |  def flart(a) { }
            |}
            |class Inner { }
            |def val = new Inner()
            |val.fl
            |'''.stripMargin()
        ICompletionProposal proposal = checkUniqueProposal(contents, 'val.fl', 'flart', 'flart val')
        applyProposalAndCheck(proposal, contents.replace('val.fl', 'val.flart val'))
    }

    @Test
    void testCommandChain1() {
        createDsld '''\
            |contribute(currentType('Inner')) {
            |  method name: 'flart', type: 'Inner', noParens: true
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Inner { }
            |def val = new Inner()
            |val.fla
            |'''.stripMargin()
        ICompletionProposal proposal = checkUniqueProposal(contents, '.fla', 'flart', 'flart()')
        applyProposalAndCheck(proposal, contents.replace('val.fla', 'val.flart()'))
    }

    @Test
    void testCommandChain2() {
        createDsld '''\
            |contribute(currentType('Inner')) {
            |  method name: 'flart', type: 'Inner', noParens: true
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Inner { }
            |def val = new Inner()
            |val.flart foo fl
            |'''.stripMargin()
        ICompletionProposal proposal = checkUniqueProposal(contents, ' fl', 'flart', 'flart()')
        applyProposalAndCheck(proposal, contents.replace(' fl', ' flart()'))
    }

    @Test
    void testCommandChain3() {
        createDsld '''\
            |contribute(currentType('Inner')) {
            |  method name: 'flart', type: 'Inner', noParens: true
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Inner { }
            |def val = new Inner()
            |val.flart foo, baz fl
            |'''.stripMargin()
        ICompletionProposal proposal = checkUniqueProposal(contents, ' fl', 'flart', 'flart()')
        applyProposalAndCheck(proposal, contents.replace(' fl', ' flart()'))
    }

    @Test
    void testCommandChain4() {
        createDsld '''\
            |contribute(currentType('Inner')) {
            |  method name: 'flart', type: 'Inner', params: [a: Integer], noParens: true
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Inner { }
            |def val = new Inner()
            |val.flart foo, baz fl
            |'''.stripMargin()

        ICompletionProposal proposal = checkUniqueProposal(contents, ' fl', 'flart', 'flart 0')
        applyProposalAndCheck(proposal, contents.replace(' fl', ' flart 0'))
    }

    @Test
    void testCommandChain5() {
        createDsld '''\
            |contribute(currentType('Inner')) {
            |  method name: 'flart', type: 'Inner', params: [a: Integer, b: String], noParens: true
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Inner { }
            |def val = new Inner()
            |val.flart foo, baz fl
            |'''.stripMargin()
        ICompletionProposal proposal = checkUniqueProposal(contents, ' fl', 'flart', 'flart 0, ""')
        applyProposalAndCheck(proposal, contents.replace(' fl', ' flart 0, ""'))
    }

    @Test
    void testConfigScript1() {
        createDsld '''\
            |contribute(isScript() & enclosingCall(name('withConfig') & hasArgument('configuration')) & inClosure() & isThisType()) {
            |  method(name: 'imports', type: void, params: [block: Closure])
            |}
            |'''.stripMargin()

        String contents = '''\
            |withConfig(configuration) {
            |  // here
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.indexOf('// here'))
        proposalExists(proposals, 'imports(Closure block)', 1)
    }

    @Test
    void testConfigScript1a() {
        createDsld '''\
            |contribute(isScript() & enclosingCall(name('withConfig') & hasArgument('configuration')) & inClosure() & isThisType()) {
            |  method(name: 'imports', type: void, params: [block: Closure])
            |}
            |'''.stripMargin()

        String contents = '''\
            |withConfig(configuration) {
            |  x.i
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'x.i'))
        proposalExists(proposals, 'imports(Closure block)', 0)
    }

    @Test
    void testConfigScript2() {
        createDsld '''\
            |def configBlock = { -> isScript() & enclosingCall(name('withConfig') & hasArgument('configuration')) & inClosure() & isThisType() }
            |
            |contribute(configBlock()) {
            |  method(name: 'imports', type: void, params: [block: Closure])
            |}
            |
            |contribute(configBlock() & enclosingCallName('imports')) {
            |  setDelegateType('org.codehaus.groovy.control.customizers.builder.ImportCustomizerFactory.ImportHelper')
            |}
            |'''.stripMargin()

        String contents = '''\
            |withConfig(configuration) {
            |  imports {
            |    star 'groovy.transform'
            |    norm
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'norm'))
        proposalExists(proposals, 'normal', 2)
    }

    @Test
    void testConfigScript3() {
        createDsld '''\
            |def configBlock = { -> isScript() & enclosingCall(name('withConfig') & hasArgument('configuration')) & inClosure() & isThisType() }
            |
            |contribute(configBlock()) {
            |  method(name: 'imports', type: void, params: [block: Closure])
            |}
            |
            |contribute(configBlock() & enclosingCallName('imports')) {
            |  setDelegateType('org.codehaus.groovy.control.customizers.builder.ImportCustomizerFactory.ImportHelper')
            |}
            |'''.stripMargin()

        String contents = '''\
            |withConfig(configuration) {
            |  source(basenameValidator: { !!(it =~ /.src.test./) }) {
            |    imports {
            |      normal 'org.junit.Test'
            |      st
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'st'))
        proposalExists(proposals, 'staticMember', 2)
        proposalExists(proposals, 'staticStar', 2)
        proposalExists(proposals, 'star', 1)
    }

    @Test // GRECLIPSE-1324
    void testStatementPosition1() {
        createDsld '''\
            |contribute(currentType(Integer) & enclosingCallName('foo')) {
            |  setDelegateType(String)
            |}
            |'''.stripMargin()

        String contents = '''\
            |def foo(@DelegatesTo(Integer) Closure cl) {
            |}
            |foo {
            |  #
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('#', ''), contents.indexOf('#'))
        // should see proposals from String, not Integer
        proposalExists(proposals, 'substring', 2)
        proposalExists(proposals, 'bytes', 1)
        proposalExists(proposals, 'abs', 0)
        proposalExists(proposals, 'capitalize', 1)
        proposalExists(proposals, 'digits', 0)
    }

    @Test // GRECLIPSE-1324
    void testStatementPosition2() {
        createDsld '''\
            |contribute(currentType(Integer) & enclosingCallName('foo')) {
            |  setDelegateType(String)
            |}
            |'''.stripMargin()

        String contents = '''\
            |def foo(@DelegatesTo(Integer) Closure cl) {
            |}
            |foo {
            |  something
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'something') + 1)
        // should see proposals from String, not Integer
        proposalExists(proposals, 'toUpperCase()', 1)
        proposalExists(proposals, 'toHexString()', 0)
    }

    @Test
    void testStatementPosition3() {
        // 1 and 2 hit on return statement; make sure block statement gives same result
        createDsld '''\
            |contribute(isScript() & enclosingCallName('foo') & inClosure() & isThisType()) {
            |  setDelegateType(String)
            |}
            |'''.stripMargin()

        String contents = '''\
            |def foo(Closure block) {
            |}
            |foo {
            |  #
            |  something
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('#', ''), contents.indexOf('#'))
        // should see proposals from String
        proposalExists(proposals, 'bytes', 1)
        proposalExists(proposals, 'capitalize', 1)
        proposalExists(proposals, 'toUpperCase()', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/786
    void testStatementPosition4() {
        createDsld '''\
            |contribute(isScript() & isThisType()) {
            |  property name: 'xyz'
            |}
            |'''.stripMargin()

        String contents = '''\
            |/*
            | * blah blah blah
            | */
            |
            |import java.util.regex.Pattern
            |
            |def abc = 123
            |
            |#
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('#', ''), contents.indexOf('#'))
        proposalExists(proposals, 'xyz', 1)
    }

    @Test // ensures currentNode contains BlockStatement reference
    void testStatementPosition5() {
        createDsld '''\
            |contribute(isThisType()) {
            |  if (currentNode instanceof org.codehaus.groovy.ast.stmt.BlockStatement) {
            |    property name: 'xyz'
            |  }
            |}
            |'''.stripMargin()

        String contents = '''\
            |void meth() {
            |  #
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('#', ''), contents.indexOf('#'))
        proposalExists(proposals, 'xyz', 1)
    }

    @Test
    void testTrailingClosure1() {
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, true)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, true)

        createDsld '''\
            |contribute(currentType()) {
            |  method name: 'bar', type: void, params: [block: Closure]
            |}
            |'''.stripMargin()

        String contents = 'foo {  }'
        String expected = 'foo { bar {  } }'
        ICompletionProposal proposal = checkUniqueProposal(contents, 'foo { ', 'bar', 'bar {  }')
        applyProposalAndCheckCursor(proposal, expected, getIndexOf(expected, 'bar { '))
    }

    @Test // closure brackets should have no effect  on a trailing closure when noparens is set
    void testTrailingClosure1a() {
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, false)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, true)

        createDsld '''\
            |contribute(currentType()) {
            |  method name: 'bar', type: void, params: [block: Closure]
            |}
            |'''.stripMargin()

        String contents = 'foo {  }'
        String expected = 'foo { bar {  } }'
        ICompletionProposal proposal = checkUniqueProposal(contents, 'foo { ', 'bar', 'bar {  }')
        applyProposalAndCheckCursor(proposal, expected, getIndexOf(expected, 'bar { '))
    }

    @Test // closure brackets should have effect on a trailing closure when noparens is not set
    void testTrailingClosure1b() {
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, false)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)

        createDsld '''\
            |contribute(currentType()) {
            |  method name: 'bar', type: void, params: [block: Closure]
            |}
            |'''.stripMargin()

        String contents = 'foo {  }'
        String expected = 'foo { bar(block) }'
        ICompletionProposal proposal = checkUniqueProposal(contents, 'foo { ', 'bar', 'bar(block)')
        applyProposalAndCheckCursor(proposal, expected, getIndexOf(expected, 'bar('), 5, getIndexOf(expected, 'bar(block)'))
    }

    @Test // closure brackets should have effect on a non-trailing closure
    void testTrailingClosure2() {
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, false)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, true)

        createDsld '''\
            |contribute(currentType()) {
            |  method name: 'bar', type: void, params: [closure: Closure, block: Closure]
            |}
            |'''.stripMargin()

        String contents = 'foo {  }'
        String expected = 'foo { bar(closure) {  } }'
        ICompletionProposal proposal = checkUniqueProposal(contents, 'foo { ', 'bar', 'bar(closure) {  }')
        applyProposalAndCheckCursor(proposal, expected, getIndexOf(expected, 'bar('), 7, getIndexOf(expected, 'bar(closure) { '))
    }

    @Test // closure brackets should have effect on a non-trailing closure
    void testTrailingClosure2a() {
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, true)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, true)

        createDsld '''\
            |contribute(currentType()) {
            |  method name: 'bar', type: void, params: [closure: Closure, block: Closure]
            |}
            |'''.stripMargin()

        String contents = 'foo {  }'
        String expected = 'foo { bar({  }) {  } }'
        ICompletionProposal proposal = checkUniqueProposal(contents, 'foo { ', 'bar', 'bar({  }) {  }')
        applyProposalAndCheckCursor(proposal, expected, getIndexOf(expected, 'bar('), 4, getIndexOf(expected, 'bar({  }) { '))
    }

    @Test
    void testTrailingClosure3() {
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, true)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, true)

        createDsld '''\
            |contribute(currentType()) {
            |  method name: 'bar', type: void, params: [block: Closure], namedParams: [name: String]
            |}
            |'''.stripMargin()

        String contents = 'foo {  }'
        String expected = 'foo { bar(name: name) {  } }'
        ICompletionProposal proposal = checkUniqueProposal(contents, 'foo { ', 'bar', 'bar(name: name) {  }')
        applyProposalAndCheckCursor(proposal, expected, getIndexOf(expected, 'name: '), 4, getIndexOf(expected, 'bar(name: name) { '))
    }

    @Test
    void testTrailingClosure3a() {
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, true)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)

        createDsld '''\
            |contribute(currentType()) {
            |  method name: 'bar', type: void, params: [block: Closure], namedParams: [name: String]
            |}
            |'''.stripMargin()

        String contents = 'foo {  }'
        String expected = 'foo { bar(name: name, {  }) }'
        ICompletionProposal proposal = checkUniqueProposal(contents, 'foo { ', 'bar', 'bar(name: name, {  })')
        applyProposalAndCheckCursor(proposal, expected, getIndexOf(expected, 'name: '), 4, getIndexOf(expected, '{  })'))
    }

    @Test
    void testTrailingClosure3b() {
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, false)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)

        createDsld '''\
            |contribute(currentType()) {
            |  method name: 'bar', type: void, params: [block: Closure], namedParams: [name: String]
            |}
            |'''.stripMargin()

        String contents = 'foo {  }'
        String expected = 'foo { bar(name: name, block) }'
        ICompletionProposal proposal = checkUniqueProposal(contents, 'foo { ', 'bar', 'bar(name: name, block)')
        applyProposalAndCheckCursor(proposal, expected, getIndexOf(expected, 'name: '), 4, getIndexOf(expected, 'block)'))
    }

    @Test
    void testTrailingClosure4() {
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, false)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, true)

        createDsld '''\
            |contribute(currentType()) {
            |  method name: 'bar', type: void, params: [block: Closure], namedParams: [name: String], noParens: true
            |}
            |'''.stripMargin()

        String contents = 'foo {  }'
        String expected = 'foo { bar name: name, block }'
        ICompletionProposal proposal = checkUniqueProposal(contents, 'foo { ', 'bar', 'bar name: name, block')
        applyProposalAndCheckCursor(proposal, expected, getIndexOf(expected, 'name: '), 4, getIndexOf(expected, 'block'))
    }

    @Test
    void testTrailingClosure4a() {
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, true)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, true)

        createDsld '''\
            |contribute(currentType()) {
            |  method name: 'bar', type: void, params: [block: Closure], namedParams: [name: String], noParens: true
            |}
            |'''.stripMargin()

        String contents = 'foo {  }'
        String expected = 'foo { bar name: name, {  } }'
        ICompletionProposal proposal = checkUniqueProposal(contents, 'foo { ', 'bar', 'bar name: name, {  }')
        applyProposalAndCheckCursor(proposal, expected, getIndexOf(expected, 'name: '), 4, getIndexOf(expected, '{  }'))
    }

    @Test
    void testTrailingClosure4b() {
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, true)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, true)

        createDsld '''\
            |contribute(currentType()) {
            |  method name: 'bar', type: void, params: [block: Closure], noParens: true
            |}
            |'''.stripMargin()

        String contents = 'foo {  }'
        String expected = 'foo { bar {  } }'
        ICompletionProposal proposal = checkUniqueProposal(contents, 'foo { ', 'bar', 'bar {  }')
        applyProposalAndCheckCursor(proposal, expected, getIndexOf(expected, 'bar '), '{  }'.length(), getIndexOf(expected, '{  }'))
    }

    //--------------------------------------------------------------------------
    // Built-in contributions:

    @Test
    void testNamedParamsAnnotation1() {
        assumeTrue(isAtLeastGroovy(25)) // @NamedParams added in Groovy 2.5

        String contents = '''\
            |import groovy.transform.*
            |
            |def meth(@NamedParams([@NamedParam('name'), @NamedParam(value='type', type=String)]) Map args) { }
            |
            |meth()
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'meth', 1)
        proposalExists(proposals, 'name : __ - java.lang.Object', 1)
        proposalExists(proposals, 'type : __ - java.lang.String', 1)
    }

    @Test
    void testNamedParamsAnnotation2() {
        assumeTrue(isAtLeastGroovy(25)) // @NamedParams added in Groovy 2.5

        String contents = '''\
            |import groovy.transform.*
            |
            |class Pogo {
            |  Pogo(@NamedParams([@NamedParam('name'), @NamedParam(value='type', type=String)]) Map args) { }
            |}
            |def pogo = new Pogo()
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'Pogo', 1)
        proposalExists(proposals, 'name : __ - java.lang.Object', 1)
        proposalExists(proposals, 'type : __ - java.lang.String', 1)
    }

    @Test
    void testNamedParamsAnnotation2a() {
        assumeTrue(isAtLeastGroovy(25)) // @NamedParams added in Groovy 2.5

        String contents = '''\
            |import groovy.transform.*
            |
            |class Pogo {
            |  Pogo(@NamedParams([@NamedParam('name'), @NamedParam(value='type', type=String)]) Map args) { }
            |}
            |def pogo = new Pogo(name: null, )
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, ', '))
        proposalExists(proposals, 'Pogo', 1)
        proposalExists(proposals, 'name : __ - java.lang.Object', 0)
        proposalExists(proposals, 'type : __ - java.lang.String', 1)
    }

    @Test
    void testNamedVariantTransform1() {
        assumeTrue(isAtLeastGroovy(25)) // @NamedVariant added in Groovy 2.5

        String contents = '''\
            |import groovy.transform.*
            |
            |class Pogo {
            |  String name, type
            |}
            |
            |@NamedVariant
            |def meth(Pogo pogo) { }
            |
            |meth()
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'meth', 2)
        proposalExists(proposals, 'pogo : __', 1)
        proposalExists(proposals, 'name : __', 0)
        proposalExists(proposals, 'type : __', 0)
    }

    @Test
    void testNamedVariantTransform2() {
        assumeTrue(isAtLeastGroovy(25)) // @NamedVariant added in Groovy 2.5

        String contents = '''\
            |import groovy.transform.*
            |
            |class Pogo {
            |  String name, type
            |}
            |
            |@NamedVariant
            |def meth(@NamedDelegate Pogo pogo) { }
            |
            |meth()
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'meth', 2)
        proposalExists(proposals, 'name : __', 1)
        proposalExists(proposals, 'type : __', 1)
    }

    @Test
    void testNamedVariantTransform2a() {
        assumeTrue(isAtLeastGroovy(25)) // @NamedVariant added in Groovy 2.5

        String contents = '''\
            |import groovy.transform.*
            |
            |class Pogo {
            |  String name, type
            |}
            |
            |@NamedVariant
            |def meth(@NamedDelegate Pogo pogo, int what) { }
            |
            |meth()
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'meth', 2)
        proposalExists(proposals, 'name : __', 1)
        proposalExists(proposals, 'type : __', 1)
        proposalExists(proposals, 'what : __', 0)
    }

    @Test
    void testNamedVariantTransform3() {
        assumeTrue(isAtLeastGroovy(25)) // @NamedVariant added in Groovy 2.5

        String contents = '''\
            |import groovy.transform.*
            |
            |class Pogo {
            |  boolean isSome() {}
            |  Object getThing() {}
            |  void setName(String value) {}
            |  void setType(String value) {}
            |}
            |
            |@NamedVariant
            |def meth(@NamedDelegate Pogo pogo) { }
            |
            |meth()
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'meth', 2)
        proposalExists(proposals, 'name : __', 1)
        proposalExists(proposals, 'type : __', 1)
        proposalExists(proposals, 'some : __', 0)
        proposalExists(proposals, 'thing : __', 0)
    }

    @Test
    void testNamedVariantTransform4() {
        assumeTrue(isAtLeastGroovy(25)) // @NamedVariant added in Groovy 2.5

        String contents = '''\
            |import groovy.transform.*
            |
            |@NamedVariant
            |def meth(@NamedParam('dob') Date date) { }
            |
            |meth()
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'meth', 2)
        proposalExists(proposals, 'dob : __', 1)
        proposalExists(proposals, 'date : __', 0)
    }

    @Test
    void testNamedVariantTransform5() {
        assumeTrue(isAtLeastGroovy(25)) // @NamedVariant added in Groovy 2.5

        String contents = '''\
            |import groovy.transform.*
            |
            |class Name {
            |  String first, middle, last
            |}
            |
            |@NamedVariant
            |def meth(@NamedDelegate Name name, @NamedParam('dob') Date date) { }
            |
            |meth()
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'first : __', 1)
        proposalExists(proposals, 'last : __', 1)
        proposalExists(proposals, 'name : __', 0)
        proposalExists(proposals, 'date : __', 0)
        proposalExists(proposals, 'dob : __', 1)
    }

    @Test
    void testNamedVariantTransform6() {
        assumeTrue(isAtLeastGroovy(25)) // @NamedVariant added in Groovy 2.5

        String contents = '''\
            |import groovy.transform.*
            |
            |class Color {
            |  int r, g, b
            |  @NamedVariant
            |  Color(int r, int g, int b) {
            |    this.r = r
            |    this.g = g
            |    this.b = b
            |  }
            |}
            |
            |def color = new Color(r: 0, )
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, ', '))
        proposalExists(proposals, 'r : __', 0)
        proposalExists(proposals, 'g : __', 1)
        proposalExists(proposals, 'b : __', 1)
    }

    @Test
    void testNewifyTransform1() {
        String contents = '''\
            |@Newify class Foo {
            |  List list = ArrayList.n
            |  Map map = HashM
            |}
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.n'))
        proposalExists(proposals, 'new', 3) // one for each constructor in ArrayList

        proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'HashM')))
        proposalExists(proposals, 'HashMap', 0)
    }

    @Test
    void testNewifyTransform2() {
        String contents = '''\
            |@Newify(HashMap) class Foo {
            |  List list = ArrayList.n
            |  Map map = HashM
            |}
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.n'))
        proposalExists(proposals, 'new', 3) // one for each constructor in ArrayList

        proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'HashM')))
        proposalExists(proposals, 'HashMap', 4) // one for each constructor in HashMap
    }

    @Test
    void testNewifyTransform3() {
        String contents = '''\
            |@Newify(auto=false, value=HashMap) class Foo {
            |  List list = ArrayList.n
            |  Map map = HashM
            |}
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.n'))
        proposalExists(proposals, 'new', 0)

        proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'HashM')))
        proposalExists(proposals, 'HashMap', 4) // one for each constructor in HashMap
    }

    @Test
    void testNewifyTransform4() {
        String contents = '''\
            |@Newify
            |List list = ArrayList.n
            |@Newify(HashMap)
            |Map map = HashM
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.n'))
        proposalExists(proposals, 'new', 3) // one for each constructor in ArrayList

        proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'HashM')))
        proposalExists(proposals, 'HashMap', 4) // one for each constructor in HashMap
    }

    @Test
    void testNewifyTransform5() {
        assumeTrue(isAtLeastGroovy(25)) // @Newify(pattern=...) added in Groovy 2.5

        String contents = '''\
            |@Newify(auto=false, pattern=/(Linked)?Hash.*/) class Foo {
            |  List list = ArrayList.n
            |  Map map = HashM
            |}
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.n'))
        proposalExists(proposals, 'new', 0)

        proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'HashM')))
        proposalExists(proposals, 'HashMap', 4) // one for each constructor in HashMap
    }

    @Test
    void testNewifyTransform5a() {
        assumeTrue(isAtLeastGroovy(25)) // @Newify(pattern=...) added in Groovy 2.5

        String contents = '''\
            |@Newify(auto=false, pattern=/(Linked)?Hash.*/) class Foo {
            |  Map map = LinkedH
            |}
            |'''.stripMargin()

        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'LinkedH')))
        proposalExists(proposals, 'LinkedHashMap', 5) // one for each constructor in LinkedHashMap
    }

    @Test
    void testNewifyTransform5b() {
        assumeTrue(isAtLeastGroovy(25)) // @Newify(pattern=...) added in Groovy 2.5

        String contents = '''\
            |@Newify(auto=false, pattern=/(Linked)?Hash.*/) class Foo {
            |  Map map = LinkedHashMap()
            |}
            |'''.stripMargin()

        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'LinkedHashMap')))
        proposalExists(proposals, 'LinkedHashMap', 5) // one for each constructor in LinkedHashMap
    }

    @Test
    void testSelfTypeTransform1() {
        String contents = '''\
            |import groovy.transform.*
            |
            |class Foo { String string }
            |
            |@CompileStatic
            |@SelfType(Foo)
            |trait Bar {
            |  void baz() {
            |    def s1 = str
            |    def s2 = getStr
            |  }
            |}
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'str'))
        proposalExists(proposals, 'string', 1)

        proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'getStr')))
        proposalExists(proposals, 'getString()', 1)
    }

    @Test
    void testSelfTypeTransform2() {
        String contents = '''\
            |import groovy.transform.*
            |
            |class Foo { String string }
            |
            |@CompileStatic
            |@SelfType([Foo, GroovyObject])
            |trait Bar {
            |  void baz() {
            |    def s1 = str
            |    def s2 = getStr
            |  }
            |}
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'str'))
        proposalExists(proposals, 'string', 1)

        proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'getStr')))
        proposalExists(proposals, 'getString()', 1)
    }

    @Test
    void testSingletonTransform1() {
        String contents = '''\
            |@Singleton class Foo { static Object ijk }
            |Foo.i
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, '.i')))
        // contributed by built-in DLSD for @Singleton AST transform
        assertProposalOrdering(proposals, 'instance', 'ijk')
    }

    @Test
    void testSingletonTransform2() {
        String contents = '''\
            |@Singleton class Foo { static Object getIjk() { } }
            |Foo.g
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, '.g')))
        // contributed by built-in DLSD for @Singleton AST transform
        assertProposalOrdering(proposals, 'getInstance', 'getIjk')
    }

    @Test
    void testSortableTransform1() {
        String contents = '''\
            |import groovy.transform.*
            |@Sortable class Foo {}
            |new Foo().com
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'com')))
        // contributed by built-in DLSD for @Sortable AST transform
        proposalExists(proposals, 'compareTo(Foo other) : int', 1)
    }

    @Test
    void testSwingBuilder1() {
        assumeFalse(isAtLeastGroovy(25)) // groovy-swing not included by default since 2.5

        String contents = '''\
            |import groovy.swing.SwingBuilder
            |new SwingBuilder().edt {
            |  delegate.f
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'delegate.f')))
        // contributed by built-in DSLD for SwingBuilder
        assertProposalOrdering(proposals, 'frame', 'find')
    }

    @Test
    void testSwingBuilder2() {
        assumeFalse(isAtLeastGroovy(25)) // groovy-swing not included by default since 2.5

        String contents = '''\
            |import groovy.swing.SwingBuilder
            |new SwingBuilder().edt {
            |  fr
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'fr')))
        // contributed by built-in DSLD for SwingBuilder
        assertProposalOrdering(proposals, 'frame', 'FrameFactory - groovy.swing.factory')
    }

    @Test
    void testSwingBuilder3() {
        assumeFalse(isAtLeastGroovy(25)) // groovy-swing not included by default since 2.5

        String contents = '''\
            |import groovy.swing.SwingBuilder
            |new SwingBuilder().edt {
            |  this.x
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this.'))
        // proposals should not exist since not applied to 'this'
        proposalExists(proposals, 'frame', 0)
        proposalExists(proposals, 'registerBinding', 0)
    }
}
