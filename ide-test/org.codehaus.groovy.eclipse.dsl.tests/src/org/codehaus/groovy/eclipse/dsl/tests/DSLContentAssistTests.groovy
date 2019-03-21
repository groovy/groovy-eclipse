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
package org.codehaus.groovy.eclipse.dsl.tests

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy
import static org.junit.Assume.assumeTrue

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist
import org.codehaus.groovy.eclipse.codeassist.tests.CompletionTestSuite
import org.codehaus.groovy.eclipse.dsl.DSLPreferencesInitializer
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IProject
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

final class DSLContentAssistTests extends CompletionTestSuite {

    private static final String COMMAND_CHAIN_NO_ARGS = '''\
        contribute(currentType('Inner')) {
          method name:'flart', noParens:true, type:'Inner'
        }
        '''.stripIndent()
    private static final String COMMAND_CHAIN_ONE_ARG = '''\
        contribute(currentType('Inner')) {
          method name:'flart', noParens:true, type:'Inner', params:[a:Integer]
        }
        '''.stripIndent()
    private static final String COMMAND_CHAIN_TWO_ARGS = '''\
        contribute(currentType('Inner')) {
          method name:'flart', noParens:true, type:'Inner', params:[a:Integer, b:String]
        }
        '''.stripIndent()
    private static final String NO_PARENS_FOR_DELEGATE = '''\
        contribute(currentType('Inner')) {
          delegatesTo type: 'Other', noParens: true
        }
        '''.stripIndent()
    private static final String SET_DELEGATE_ON_INT = '''\
        contribute(currentType(Integer) & enclosingCallName('foo')) {
          setDelegateType(String)
        }
        '''.stripIndent()

    @BeforeClass
    static void setUpTests() {
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.PARAMETER_GUESSING, true)
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
    }

    private String[] createDsls(String... dsls) {
        System.out.println("Now creating $dsls.length DSLD files.")
        int i = 0
        for (dsl in dsls) {
            println "Creating:\n$dsl"
            IFile file = addPlainText(dsl, "dsl${i++}.dsld")
            assert file.exists() : "File $file just created, but doesn't exist"
        }
        return dsls
    }

    //--------------------------------------------------------------------------

    @Test // GRECLIPSE-1324
    void testEmptyClosure1() {
        createDsls(SET_DELEGATE_ON_INT)
        String contents = '''\
            def foo(@DelegatesTo(Integer) Closure cl) {
            }
            foo {
              // here
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '\n  '))
        // should see proposals from String, not Integer
        proposalExists(proposals, 'substring', 2)
        proposalExists(proposals, 'bytes', 1)
        proposalExists(proposals, 'abs', 0)
        proposalExists(proposals, 'capitalize', 1)
        proposalExists(proposals, 'digits', 0)
    }

    @Test // GRECLIPSE-1324
    void testEmptyClosure2() {
        createDsls(SET_DELEGATE_ON_INT)
        String contents = '''\
            def foo(@DelegatesTo(Integer) Closure cl) {
            }
            foo {
              to
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ' to') + 1)
        // should see proposals from String, not Integer
        proposalExists(proposals, 'toUpperCase()', 1)
        proposalExists(proposals, 'toHexString()', 0)
    }

    @Test
    void testCommandChain1() {
        createDsls(COMMAND_CHAIN_NO_ARGS)
        String contents = '''\
            class Inner { }
            def val = new Inner()
            val.fla
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.fla'))
        proposalExists(proposals, 'flart', 1)
        ICompletionProposal proposal = findFirstProposal(proposals, 'flart', false)
        applyProposalAndCheck(new Document(contents), proposal, contents.replace('val.fla', 'val.flart'))
    }

    @Test
    void testCommandChain2() {
        createDsls(COMMAND_CHAIN_NO_ARGS)
        String contents = '''\
            class Inner { }
            def val = new Inner()
            val.flart foo fl
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ' fl'))
        proposalExists(proposals, 'flart', 1)
        ICompletionProposal proposal = findFirstProposal(proposals, 'flart', false)
        applyProposalAndCheck(new Document(contents), proposal, contents.replace(' fl', ' flart'))
    }

    @Test
    void testCommandChain3() {
        createDsls(COMMAND_CHAIN_NO_ARGS)
        String contents = '''\
            class Inner { }
            def val = new Inner()
            val.flart foo, baz fl
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ' fl'))
        proposalExists(proposals, 'flart', 1)
        ICompletionProposal proposal = findFirstProposal(proposals, 'flart', false)
        applyProposalAndCheck(new Document(contents), proposal, contents.replace(' fl', ' flart'))
    }

    @Test
    void testCommandChain4() {
        createDsls(COMMAND_CHAIN_ONE_ARG)
        String contents = '''\
            class Inner { }
            def val = new Inner()
            val.flart foo, baz fl
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ' fl'))
        proposalExists(proposals, 'flart', 1)
        ICompletionProposal proposal = findFirstProposal(proposals, 'flart', false)
        applyProposalAndCheck(new Document(contents), proposal, contents.replace(' fl', ' flart 0 '))
    }

    @Test
    void testCommandChain5() {
        createDsls(COMMAND_CHAIN_TWO_ARGS)
        String contents = '''\
            class Inner { }
            def val = new Inner()
            val.flart foo, baz fl
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ' fl'))
        proposalExists(proposals, 'flart', 1)
        ICompletionProposal proposal = findFirstProposal(proposals, 'flart', false)
        applyProposalAndCheck(new Document(contents), proposal, contents.replace(' fl', ' flart 0, "" '))
    }

    @Test
    void testDelegatesToNoParens1() {
        createDsls(NO_PARENS_FOR_DELEGATE)
        String contents = '''\
            class Other {
              def blart(a, b, c) { }
              def flart(a) { }
            }
            class Inner { }
            def val = new Inner()
            val.bl
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'val.bl'))
        ICompletionProposal proposal = findFirstProposal(proposals, 'blart', false)
        applyProposalAndCheck(new Document(contents), proposal, contents.replace('val.bl', 'val.blart val, val, val '))
    }

    @Test
    void testDelegatesToNoParens2() {
        createDsls(NO_PARENS_FOR_DELEGATE)
        String contents = '''\
            class Other {
              def blart(a, b, c) { }
              def flart(a) { }
            }
            class Inner { }
            def val = new Inner()
            val.fl
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'val.fl'))
        ICompletionProposal proposal = findFirstProposal(proposals, 'flart', false)
        applyProposalAndCheck(new Document(contents), proposal, contents.replace('val.fl', 'val.flart val '))
    }

    @Test
    void testNewifyTransform1() {
        String contents = '''\
            @Newify class Foo {
              List list = ArrayList.n
              Map map = HashM
            }
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.n'))
        proposalExists(proposals, 'new', 3) // one for each constructor in ArrayList

        proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'HashM')))
        proposalExists(proposals, 'HashMap', 0)
    }

    @Test
    void testNewifyTransform2() {
        String contents = '''\
            @Newify(HashMap) class Foo {
              List list = ArrayList.n
              Map map = HashM
            }
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.n'))
        proposalExists(proposals, 'new', 3) // one for each constructor in ArrayList

        proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'HashM')))
        proposalExists(proposals, 'HashMap', 4) // one for each constructor in HashMap
    }

    @Test
    void testNewifyTransform3() {
        String contents = '''\
            @Newify(auto=false, value=HashMap) class Foo {
              List list = ArrayList.n
              Map map = HashM
            }
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.n'))
        proposalExists(proposals, 'new', 0)

        proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'HashM')))
        proposalExists(proposals, 'HashMap', 4) // one for each constructor in HashMap
    }

    @Test
    void testNewifyTransform4() {
        String contents = '''\
            @Newify
            List list = ArrayList.n
            @Newify(HashMap)
            Map map = HashM
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.n'))
        proposalExists(proposals, 'new', 3) // one for each constructor in ArrayList

        proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'HashM')))
        //proposalExists(proposals, 'HashMap', 4) // one for each constructor in HashMap
        // TODO: waiting for https://issues.apache.org/jira/browse/GROOVY-8249
    }

    @Test
    void testSelfTypeTransform1() {
        assumeTrue(isAtLeastGroovy(24)) // SelfType was added in Groovy 2.4

        String contents = '''\
            import groovy.transform.*

            class Foo { String string }

            @CompileStatic
            @SelfType(Foo)
            trait Bar {
              void baz() {
                def s1 = str
                def s2 = getStr
              }
            }
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'str'))
        proposalExists(proposals, 'string', 1)

        proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'getStr')))
        proposalExists(proposals, 'getString()', 1)
    }

    @Test
    void testSelfTypeTransform2() {
        assumeTrue(isAtLeastGroovy(24)) // SelfType was added in Groovy 2.4

        String contents = '''\
            import groovy.transform.*

            class Foo { String string }

            @CompileStatic
            @SelfType([Foo, GroovyObject])
            trait Bar {
              void baz() {
                def s1 = str
                def s2 = getStr
              }
            }
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'str'))
        proposalExists(proposals, 'string', 1)

        proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'getStr')))
        proposalExists(proposals, 'getString()', 1)
    }

    @Test
    void testSingletonTransform1() {
        String contents = '''\
            @Singleton class Foo { static Object ijk }
            Foo.i
            '''.stripIndent()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, '.i')))
        // contributed by built-in DLSD for @Singleton AST transform
        assertProposalOrdering(proposals, 'instance', 'ijk')
    }

    @Test
    void testSingletonTransform2() {
        String contents = '''\
            @Singleton class Foo { static Object getIjk() { } }
            Foo.g
            '''.stripIndent()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, '.g')))
        // contributed by built-in DLSD for @Singleton AST transform
        assertProposalOrdering(proposals, 'getInstance', 'getIjk')
    }

    @Test
    void testSwingBuilder1() {
        String contents = '''\
            import groovy.swing.SwingBuilder
            new SwingBuilder().edt {
              delegate.f
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'delegate.f')))
        // contributed by built-in DSLD for SwingBuilder
        assertProposalOrdering(proposals, 'frame', 'find')
    }

    @Test
    void testSwingBuilder2() {
        String contents = '''\
            import groovy.swing.SwingBuilder
            new SwingBuilder().edt {
              fr
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'fr')))
        // contributed by built-in DSLD for SwingBuilder
        assertProposalOrdering(proposals, 'frame', 'FrameFactory - groovy.swing.factory')
    }

    @Test // proposals should not exist since not applied to 'this'
    void testSwingBuilder3() {
        String contents = '''\
            import groovy.swing.SwingBuilder
            new SwingBuilder().edt {
              this.x
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this.'))
        proposalExists(proposals, 'frame', 0)
        proposalExists(proposals, 'registerBinding', 0)
    }
}
