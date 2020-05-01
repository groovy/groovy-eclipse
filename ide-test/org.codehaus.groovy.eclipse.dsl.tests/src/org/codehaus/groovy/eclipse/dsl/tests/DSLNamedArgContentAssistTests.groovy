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

import static org.junit.Assume.assumeTrue

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist
import org.codehaus.groovy.eclipse.codeassist.tests.CompletionTestSuite
import org.codehaus.groovy.eclipse.dsl.DSLPreferencesInitializer
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

final class DSLNamedArgContentAssistTests extends CompletionTestSuite {

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
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.NAMED_ARGUMENTS, true)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, true)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, true)
    }

    private void createDSL(String contents) {
        addPlainText(contents, 'MyDsld.dsld')
    }

    //--------------------------------------------------------------------------

    @Test
    void testNamedParams() {
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'flar', namedParams: [aaa: Integer, bbb: Boolean, ccc: String]
            |}
            |'''.stripMargin()

        String contents = 'flar()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testOptionalParams() {
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'flar', optionalParams: [aaa: Integer, bbb: Boolean, ccc: String]
            |}
            |'''.stripMargin()

        String contents = 'flar()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testNamedAndOptionalParams() {
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'flar', namedParams: [aaa: Integer], optionalParams: [bbb: Boolean, ccc: String]
            |}
            |'''.stripMargin()

        String contents = 'flar()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/623
    void testNormalAndOptionalParams() {
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'flar', params: [aaa: Integer], optionalParams: [bbb: Boolean, ccc: String]
            |}
            |'''.stripMargin()

        checkUniqueProposal('fla', 'fla', 'flar(Integer aaa)', 'flar(0)')

        String contents = 'flar()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testUseNamedArgs1() {
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'flar', params: [aaa: Integer, bbb: Boolean, ccc: String], useNamedArgs: true
            |}
            |'''.stripMargin()
        String contents = 'flar()'

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testUseNamedArgs1a() {
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'flar', params: [aaa: Integer, bbb: Boolean, ccc: String], useNamedArgs: false
            |}
            |'''.stripMargin()
        String contents = 'flar()'

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 0)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testUseNamedArgs2() {
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'flar', params: [aaa: Integer, bbb: Boolean, ccc: String], useNamedArgs: true
            |}
            |'''.stripMargin()
        String contents = 'flar(aaa:__, )'

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testUseNamedArgs3() {
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'flar', params: [aaa: Integer, bbb: Boolean, ccc: String], useNamedArgs: true
            |}
            |'''.stripMargin()
        String contents = 'flar(aaa:__, )'

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ','))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testUseNamedArgs4() {
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'flar', params: [aaa: Integer, bbb: Boolean, ccc: String], useNamedArgs: true
            |}
            |'''.stripMargin()

        String contents = 'flar(aaa:__, bbb:__, )'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ','))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testUseNamedArgs5() {
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'flar', params: [aaa: Integer, bbb: Boolean, ccc: String], useNamedArgs: true
            |}
            |'''.stripMargin()

        String contents = 'flar(aaa:__, bbb:__, ccc:__)'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ','))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 0)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testUseNamedArgs6() {
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'flar', params: [aaa: Integer, bbb: Boolean, ccc: String], useNamedArgs: false
            |}
            |'''.stripMargin()

        String contents = 'flar '
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ' '))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 0)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testUseNamedArgs7() {
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'flar', params: [aaa: Integer, bbb: Boolean, ccc: String], useNamedArgs: true
            |}
            |'''.stripMargin()

        String contents = '''\
            |flar aaa:__,
            |need_this_here_so_parser_wont_break
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'r '))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testUseNamedArgs8() {
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'flar', params: [aaa: Integer, bbb: Boolean, ccc: String], useNamedArgs: true
            |}
            |'''.stripMargin()

        String contents = '''\
            |flar aaa:__,
            |need_this_here_so_parser_wont_break
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ','))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testUseNamedArgs9() {
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'flar', params: [one: Integer, two: Closure], useNamedArgs: true
            |}
            |'''.stripMargin()

        String contents = '''\
            |fla
            |'''.stripMargin()
        checkUniqueProposal(contents, 'fla', 'flar(Integer one, Closure two)', 'flar(one: 0) {  }')
    }

    @Test
    void testParamGuessing1() {
        createDSL '''\
            |contribute(currentType()) {
            |  method name:"flar", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true
            |}
            |'''.stripMargin()

        String contents = '''\
            |String xxx
            |int yyy
            |boolean zzz
            |flar()
            |'''.stripMargin()
        checkProposalChoices(contents, 'flar(', 'aaa', 'aaa: __', 'yyy', '0')
    }

    @Test
    void testParamGuessing3() {
        createDSL '''\
            |contribute(currentType()) {
            |  method name:"flar", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true
            |}
            |'''.stripMargin()

        String contents = '''\
            |String xxx
            |boolean zzz
            |def uuu(int iii) {
            |  int yyy
            |  flar()
            |}
            |'''.stripMargin()
        checkProposalChoices(contents, 'flar(', 'aaa', 'aaa: __', 'iii', 'yyy', '0')
    }

    @Test //https://github.com/groovy/groovy-eclipse/issues/613
    void testProposalSignature1() {
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, false)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'bar', type: 'void', namedParams:['named1':float,'named2':double], params: ['reg1':int,'reg2':long,'closure':Closure]
            |}
            |'''.stripMargin()

        String contents = 'foo {  }'
        ICompletionProposal proposal = checkUniqueProposal(contents, 'foo { ', 'bar', 'bar(named1: named1, named2: named2, reg1, reg2, closure)')
        assertProposalSignature(proposal, 'bar(float named1, double named2, int reg1, long reg2, Closure closure) : void')
    }

    @Test
    void testProposalSignature2() {
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, true)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'bar', type: 'void', namedParams:['named1':float,'named2':double], params: ['reg1':int,'reg2':long,'closure':Closure]
            |}
            |'''.stripMargin()

        String contents = 'foo {  }'
        ICompletionProposal proposal = checkUniqueProposal(contents, 'foo { ', 'bar', 'bar(named1: named1, named2: named2, reg1, reg2, {  })')
        assertProposalSignature(proposal, 'bar(float named1, double named2, int reg1, long reg2, Closure closure) : void')
    }

    @Test
    void testProposalSignature3() {
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, true)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, true)
        createDSL '''\
            |contribute(currentType()) {
            |  method name: 'bar', type: 'void', namedParams:['named1':float,'named2':double], params: ['reg1':int,'reg2':long,'closure':Closure]
            |}
            |'''.stripMargin()

        String contents = 'foo {  }'
        ICompletionProposal proposal = checkUniqueProposal(contents, 'foo { ', 'bar', 'bar(named1: named1, named2: named2, reg1, reg2) {  }')
        assertProposalSignature(proposal, 'bar(float named1, double named2, int reg1, long reg2, Closure closure) : void')
    }

    //--------------------------------------------------------------------------
    // test cases for application of closures with and without named parameters

    private static final String CLOSURE_DSLD = '''\
        |contribute(currentType('Type')) {
        |  method name: 'test1', params: [op: Closure]
        |  method name: 'test2', params: [first: String, op: Closure]
        |  method name: 'test3', namedParams: [op: Closure]
        |  method name: 'test4', namedParams: [first: String, op: Closure]
        |  method name: 'test5', params: [first: String], namedParams: [op: Closure]
        |  method name: 'test6', namedParams: [first: String], params: [op: Closure]
        |  method name: 'test7', namedParams: [first: String, other: String], params: [op: Closure]
        |  method name: 'test8', namedParams: [first: String], params: [other: String, op: Closure]
        |  method name: 'test9', namedParams: [first: String], params: [other: String, op: Closure, other2: String]
        |  method name: 'test0', namedParams: [first: String], params: [other: String, op: Closure, other2: String, op2: Closure]
        |}
        |'''.stripMargin()
    private static final String CLOSURE_TEST = 'class Type { }\n new Type().test'

    @Test
    void testClosureOp1() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(CLOSURE_TEST, CLOSURE_TEST + '1 {  }', CLOSURE_TEST.length(), 'test1')
    }

    @Test
    void testClosureOp2() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(CLOSURE_TEST, CLOSURE_TEST + '2("") {  }', CLOSURE_TEST.length(), 'test2')
    }

    @Test
    void testClosureOp3() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(CLOSURE_TEST, CLOSURE_TEST + '3(op: {  })', CLOSURE_TEST.length(), 'test3')
    }

    @Test
    void testClosureOp4() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(CLOSURE_TEST, CLOSURE_TEST + '4(first: "", op: {  })', CLOSURE_TEST.length(), 'test4')
    }

    @Test
    void testClosureOp5() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(CLOSURE_TEST, CLOSURE_TEST + '5(op: {  }, "")', CLOSURE_TEST.length(), 'test5')
    }

    @Test
    void testClosureOp6() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(CLOSURE_TEST, CLOSURE_TEST + '6(first: "") {  }', CLOSURE_TEST.length(), 'test6')
    }

    @Test
    void testClosureOp7() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(CLOSURE_TEST, CLOSURE_TEST + '7(first: "", other: "") {  }', CLOSURE_TEST.length(), 'test7')
    }

    @Test
    void testClosureOp8() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(CLOSURE_TEST, CLOSURE_TEST + '8(first: "", "") {  }', CLOSURE_TEST.length(), 'test8')
    }

    @Test
    void testClosureOp9() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(CLOSURE_TEST, CLOSURE_TEST + '9(first: "", "", {  }, "")', CLOSURE_TEST.length(), 'test9')
    }

    @Test
    void testClosureOp0() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(CLOSURE_TEST, CLOSURE_TEST + '0(first: "", "", {  }, "") {  }', CLOSURE_TEST.length(), 'test0')
    }
}
