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
package org.codehaus.groovy.eclipse.dsl.tests

import static org.junit.Assume.assumeTrue

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist
import org.codehaus.groovy.eclipse.codeassist.tests.CompletionTestSuite
import org.codehaus.groovy.eclipse.dsl.DSLPreferencesInitializer
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator
import org.eclipse.core.resources.IProject
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
        withProject { IProject project ->
            GroovyDSLCoreActivator.default.contextStoreManager.initialize(project, true)
          //GroovyDSLCoreActivator.default.contextStoreManager.ignoreProject(project)
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

    //

    @Test
    void testNamedArgs1() {
        createDSL '''\
            contribute(currentType()) {
              method name:"flar", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true
            }
            '''.stripIndent()
        String contents = 'flar()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testNoNamedArgs1() {
        createDSL '''\
            contribute(currentType()) {
              method name:"flar", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:false
            }
            '''.stripIndent()
        String contents = 'flar()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 0)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testNamedArgs2() {
        createDSL '''\
            contribute(currentType()) {
              method name:"flar", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true
            }
            '''.stripIndent()
        String contents = 'flar(aaa:__, )'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testNamedArgs3() {
        createDSL '''\
            contribute(currentType()) {
              method name:"flar", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true
            }
            '''.stripIndent()
        String contents = 'flar(aaa:__, )'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ','))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testNamedArgs4() {
        createDSL '''\
            contribute(currentType()) {
              method name:"flar", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true
            }
            '''.stripIndent()
        String contents = 'flar(aaa:__, bbb:__, )'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ','))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testNamedArgs5() {
        createDSL '''\
            contribute(currentType()) {
              method name:"flar", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true
            }
            '''.stripIndent()
        String contents = 'flar(aaa:__, bbb:__, ccc:__)'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ','))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 0)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testNoNamedArgs6() {
        createDSL '''\
            contribute(currentType()) {
              method name:"flar", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:false
            }
            '''.stripIndent()
        String contents = 'flar '
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ' '))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 0)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testOptionalArgs1() {
        createDSL '''\
            contribute(currentType()) {
              method name:"flar", optionalParams:[aaa:Integer, bbb:Boolean, ccc:String]
            }
            '''.stripIndent()
        String contents = 'flar( )'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '( '))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testOptionalArgs2() {
        createDSL '''\
            contribute(currentType()) {
              method name:"flar", namedParams:[aaa:Integer, bbb:Boolean, ccc:String]
            }
            '''.stripIndent()
        String contents = 'flar( )'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '( '))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testOptionalArgs3() {
        createDSL '''\
            contribute(currentType()) {
              method name:"flar", namedParams:[aaa:Integer], optionalParams: [bbb:Boolean, ccc:String]
            }
            '''.stripIndent()
        String contents = 'flar( )'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '( '))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testNamedArgs7() {
        createDSL '''\
            contribute(currentType()) {
              method name:"flar", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true
            }
            '''.stripIndent()
        String contents = '''\
            flar aaa:__,
            need_this_here_so_parser_wont_break
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'r '))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testNamedArgs8() {
        createDSL '''\
            contribute(currentType()) {
              method name:"flar", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true
            }
            '''.stripIndent()
        String contents = '''\
            flar aaa:__,
            need_this_here_so_parser_wont_break
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ','))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'flar', 1)
    }

    @Test
    void testParamGuessing1() {
        createDSL '''\
            contribute(currentType()) {
              method name:"flar", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true
            }
            '''.stripIndent()
        String contents = '''\
            String xxx
            int yyy
            boolean zzz
            flar()
            '''.stripIndent()
        checkProposalChoices(contents, 'flar(', 'aaa', 'aaa: __, ', 'yyy', '0')
    }

    @Test
    void testParamGuessing3() {
        createDSL '''\
            contribute(currentType()) {
              method name:"flar", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true
            }
            '''.stripIndent()
        String contents = '''\
            String xxx
            boolean zzz
            def uuu(int iii)\n {
              int yyy
              flar()
            }
            '''.stripIndent()
        checkProposalChoices(contents, 'flar(', 'aaa', 'aaa: __, ', 'iii', 'yyy', '0')
    }

    // tests application of closures with and without named parameters
    private static final String CLOSURE_DSLD = '''\
        contribute(currentType('Clos')) {
          method name: 'test1', params: [op:Closure]
          method name: 'test2', params: [first: String, op:Closure]
          method name: 'test3', namedParams: [op:Closure]
          method name: 'test4', namedParams: [first: String, op:Closure]
          method name: 'test5', params: [first: String], namedParams: [op:Closure]
          method name: 'test6', namedParams: [first: String], params: [op:Closure]
          method name: 'test7', namedParams: [first: String, other:String], params: [op:Closure]
          method name: 'test8', namedParams: [first: String], params: [other:String, op:Closure]
          method name: 'test9', namedParams: [first: String], params: [other:String, op:Closure, other2:String]
          method name: 'test0', namedParams: [first: String], params: [other:String, op:Closure, other2:String, op2:Closure]
        }
        '''.stripIndent()
    private static final String closureContents = '''\
        class Clos { }
        new Clos().test'''.stripIndent()

    @Test
    void testClostureOp1() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(closureContents, closureContents + '1 {  }', closureContents.length(), 'test1')
    }

    @Test
    void testClostureOp2() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(closureContents, closureContents + '2("") {  }', closureContents.length(), 'test2')
    }

    @Test
    void testClostureOp3() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(closureContents, closureContents + '3(op: {  })', closureContents.length(), 'test3')
    }

    @Test
    void testClostureOp4() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(closureContents, closureContents + '4(first: "", op: {  })', closureContents.length(), 'test4')
    }

    @Test
    void testClostureOp5() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(closureContents, closureContents + '5(op: {  }, "")', closureContents.length(), 'test5')
    }

    @Test
    void testClostureOp6() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(closureContents, closureContents + '6(first: "") {  }', closureContents.length(), 'test6')
    }

    @Test
    void testClostureOp7() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(closureContents, closureContents + '7(first: "", other: "") {  }', closureContents.length(), 'test7')
    }

    @Test
    void testClostureOp8() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(closureContents, closureContents + '8(first: "", "") {  }', closureContents.length(), 'test8')
    }

    @Test
    void testClostureOp9() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(closureContents, closureContents + '9(first: "", "", {  }, "")', closureContents.length(), 'test9')
    }

    @Test
    void testClostureOp0() {
        createDSL(CLOSURE_DSLD)
        checkProposalApplicationNonType(closureContents, closureContents + '0(first: "", "", {  }, "") {  }', closureContents.length(), 'test0')
    }
}
