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
package org.codehaus.groovy.eclipse.codeassist.tests;

import groovy.transform.NotYetImplemented

import org.eclipse.jdt.internal.codeassist.impl.AssistOptions
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Test

/**
 * Tests that type completions are working properly.
 */
final class TypeCompletionTests extends CompletionTestSuite {

    @Test
    void testCompletionTypesInScript() {
        String contents = 'HTML'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTML'))
        proposalExists(proposals, 'HTML - javax.swing.text.html', 1)
    }

    @Test
    void testCompletionTypesInScript2() {
        String contents = 'new HTML()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTML'))
        proposalExists(proposals, 'HTML - javax.swing.text.html', 1)
    }

    @Test
    void testCompletionTypesInMethod() {
        String contents = 'def x() {\nHTML\n}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTML'))
        proposalExists(proposals, 'HTML - javax.swing.text.html', 1)
    }

    @Test
    void testCompletionTypesInMethod2() {
        String contents = 'class Foo {\ndef x() {\nHTML\n}}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTML'))
        proposalExists(proposals, 'HTML - javax.swing.text.html', 1)
    }

    @Test
    void testCompletionTypesInParameter() {
        String contents = 'def x(HTML h) { }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTML'))
        proposalExists(proposals, 'HTML - javax.swing.text.html', 1)
    }

    @Test
    void testCompletionTypesInParameter2() {
        String contents = 'def x(t, HTML h) { }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTML'))
        proposalExists(proposals, 'HTML - javax.swing.text.html', 1)
    }

    @Test
    void testCompletionTypesInParameter3() {
        String contents = 'def x(t, HTML ... h) { }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTML'))
        proposalExists(proposals, 'HTML - javax.swing.text.html', 1)
    }

    @Test
    void testCompletionTypesInParameter4() {
        String contents = 'def x(t, h = HTML) { }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTML'))
        proposalExists(proposals, 'HTML - javax.swing.text.html', 1)
    }

    @Test
    void testCompletionTypesInClassBody() {
        String contents = 'class Foo {\nHTML\n}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTML'))
        proposalExists(proposals, 'HTML - javax.swing.text.html', 1)
    }

    @Test
    void testCompletionTypesInExtends() {
        String contents = 'class Foo extends HTML { }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTML'))
        proposalExists(proposals, 'HTML - javax.swing.text.html', 1)
    }

    @Test
    void testCompletionTypesInImplements() {
        String contents = 'class Foo implements HTMLAnchElem { }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTMLAnchElem'))
        proposalExists(proposals, 'HTMLAnchorElement - org.w3c.dom.html', 1)
    }

    @Test
    void testCompleteFullyQualifiedTypeInScript() {
        String contents = 'javax.swing.text.html.HTMLDocume'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTMLDocume'))
        proposalExists(proposals, 'HTMLDocument', 1, true)
    }

    @Test
    void testCompleteFullyQualifiedTypeInClass() {
        String contents = 'class Foo { javax.swing.text.html.HTMLDocume }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTMLDocume'))
        proposalExists(proposals, 'HTMLDocument', 1, true)
    }

    @Test
    void testCompleteFullyQualifiedTypeInMethod() {
        String contents = 'class Foo { def x() { javax.swing.text.html.HTMLDocume } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTMLDocume'))
        proposalExists(proposals, 'HTMLDocument', 1, true)
    }

    @Test
    void testCompleteFullyQualifiedTypeInMethodParams() {
        String contents = 'class Foo { def x(javax.swing.text.html.HTMLDocume) { } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTMLDocume'))
        proposalExists(proposals, 'HTMLDocument', 1, true)
    }

    @Test
    void testCompleteFullyQualifiedTypeInImports() {
        String contents = 'import javax.swing.text.html.HTMLDocume'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTMLDocume'))
        proposalExists(proposals, 'HTMLDocument', 1, true)
    }

    @Test
    void testCompleteFullyQualifiedInnerType1() {
        String contents = 'java.util.Map.'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        proposalExists(proposals, 'Entry', 1, true)
    }

    @Test
    void testCompleteFullyQualifiedInnerType2() {
        String contents = 'java.util.Map.E'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        proposalExists(proposals, 'Entry', 1, true)
    }

    @Test
    void testCompletePartiallyQualifiedInnerType1() {
        String contents = 'Map.'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        proposalExists(proposals, 'Entry', 1, true)
    }

    @Test
    void testCompletePartiallyQualifiedInnerType2() {
        String contents = 'Map.E'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        proposalExists(proposals, 'Entry', 1, true)
    }

    @Test
    void testCompletePackageInClass() {
        String contents = 'class Foo { javax.swing.text.html.p }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.p'))
        proposalExists(proposals, 'javax.swing.text.html.parser', 1, true)
        // ensure no type proposals exist
        proposalExists(proposals, 'Icons', 0, true)
    }

    @Test
    void testCompletePackageInMethod() {
        String contents = 'class Foo { def x() { javax.swing.text.html.p } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.p'))
        proposalExists(proposals, 'javax.swing.text.html.parser', 1, true)
        // ensure no type proposals exist
        proposalExists(proposals, 'Icons', 0, true)
    }

    @Test
    void testCompletePackageInMethodParams() {
        String contents = 'class Foo { def x(javax.swing.text.html.p ) { } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.p'))
        proposalExists(proposals, 'javax.swing.text.html.parser', 1, true)
        // ensure no type proposals exist
        proposalExists(proposals, 'Icons', 0, true)
    }

    @Test
    void testCompletePackageInImports() {
        String contents = 'import javax.swing.text.html.p'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.p'))
        proposalExists(proposals, 'javax.swing.text.html.parser', 1, true)
        // ensure no type proposals exist
        proposalExists(proposals, 'Icons', 0, true)
    }

    @Test
    void testCompleteClass1() {
        String contents = 'class Foo { }\nFoo.cla'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        proposalExists(proposals, 'class : Class', 1, true)
    }

    @Test
    void testCompleteClass2() {
        String contents = 'class Foo { }\nFoo.com'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        int release = Integer.parseInt(System.getProperty('java.version').split(/\./)[0])
        proposalExists(proposals, 'componentType', release < 12 ? 1 : 2, true)
    }

    @Test
    void testCompleteClass3() {
        String contents = 'class Foo { }\nFoo.getCom'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        proposalExists(proposals, 'getComponentType', 1, true)
    }

    @Test
    void testCompleteClass4() {
        String contents = 'class Foo { }\nFoo.class.com'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        int release = Integer.parseInt(System.getProperty('java.version').split(/\./)[0])
        proposalExists(proposals, 'componentType', release < 12 ? 1 : 2)
    }

    @Test
    void testCompleteClass5() {
        String contents = 'class Foo { }\nFoo.class.getCom'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        proposalExists(proposals, 'getComponentType', 1)
    }

    @Test
    void testCompleteExceptionClass() {
        String contents = 'throw new MPE'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'MPE'))
        // found twice: once as a type proposal and once as a constructor proposal
        proposalExists(proposals, 'MissingPropertyExceptionNoStack', 2, true)
    }

    @Test
    void testCompleteGenericType1() {
        String contents = 'ArLi', expected = 'ArrayList' // not 'ArrayList<E>'
        checkProposalApplication(contents, expected, contents.length(), 'ArrayList - java.util', true)
    }

    @Test
    void testCompleteGenericType2() {
        String contents = 'def list = ArrLis', expected = 'def list = ArrayList'
        checkProposalApplication(contents, expected, contents.length(), 'ArrayList - java.util', true)
    }

    @Test
    void testField1() {
        String contents = 'class Foo {\n	JFr\n}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'JFr'))
        proposalExists(proposals, 'JFrame - javax.swing', 1)
    }

    @Test
    void testField2() {
        String contents = 'class Foo {\n	private JFr\n}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'JFr'))
        proposalExists(proposals, 'JFrame - javax.swing', 1)
    }

    @Test
    void testField3() {
        String contents = 'class Foo {\n	public JFr\n}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'JFr'))
        proposalExists(proposals, 'JFrame - javax.swing', 1)
    }

    @Test
    void testField4() {
        String contents = 'class Foo {\n	protected JFr\n}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'JFr'))
        proposalExists(proposals, 'JFrame - javax.swing', 1)
    }

    @Test
    void testField5() {
        String contents = 'class Foo {\n	public static JFr\n}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'JFr'))
        proposalExists(proposals, 'JFrame - javax.swing', 1)
    }

    @Test
    void testField6() {
        String contents = 'class Foo {\n	public final JFr\n}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'JFr'))
        proposalExists(proposals, 'JFrame - javax.swing', 1)
    }

    @Test
    void testField7() {
        String contents = 'class Foo {\n	public static final JFr\n}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'JFr'))
        proposalExists(proposals, 'JFrame - javax.swing', 1)
    }

    @Test
    void testField8() {
        String contents = '''\
            class Foo {
                String bar
                Lis
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'Lis'))
        proposalExists(proposals, 'List - java.util', 1)
        proposalExists(proposals, 'List - java.awt', 1)

        applyProposalAndCheck(findFirstProposal(proposals, 'List - java.util'), '''\
            class Foo {
                String bar
                List
            }
            '''.stripIndent())
    }

    @Test
    void testField9() {
        String contents = '''\
            class Foo {
                String bar
                Lis
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'Lis'))
        applyProposalAndCheck(findFirstProposal(proposals, 'List - java.awt'), '''\
            |import java.awt.List
            |
            |class Foo {
            |    String bar
            |    List
            |}
            |'''.stripMargin())
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/866
    void testField9a() {
        String contents = '''\
            class Foo {
                String bar
                List
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'List'))
        applyProposalAndCheck(findFirstProposal(proposals, 'List - java.awt'), '''\
            |import java.awt.List
            |
            |class Foo {
            |    String bar
            |    List
            |}
            |'''.stripMargin())
    }

    @Test
    void testField10() {
        String contents = '''\
            |import java.awt.*
            |
            |class Foo {
            |    String bar
            |    Lis
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'Lis'))
        applyProposalAndCheck(findFirstProposal(proposals, 'List - java.awt'), '''\
            |import java.awt.*
            |import java.awt.List
            |
            |class Foo {
            |    String bar
            |    List
            |}
            |'''.stripMargin())
    }

    @Test @NotYetImplemented
    void testField11() {
        String contents = '''\
            |import java.awt.*
            |
            |class Foo {
            |    String bar
            |    Lis
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'Lis'))
        applyProposalAndCheck(findFirstProposal(proposals, 'List - java.util'), '''\
            |import java.awt.*
            |import java.util.List
            |
            |class Foo {
            |    String bar
            |    List
            |}
            |'''.stripMargin())
    }

    @Test
    void testTypeFilter1() {
        setJavaPreference(PreferenceConstants.TYPEFILTER_ENABLED, 'javax.swing.JFrame')

        ICompletionProposal[] proposals = createProposalsAtOffset('JFr', 2)
        proposalExists(proposals, 'JFrame - javax.swing', 0)
    }

    @Test
    void testTypeFilter2() {
        setJavaPreference(PreferenceConstants.TYPEFILTER_ENABLED, 'javax.swing.*')

        ICompletionProposal[] proposals = createProposalsAtOffset('JFr', 2)
        proposalExists(proposals, 'JFrame - javax.swing', 0)
    }

    @Test
    void testDeprecationCheck0() {
        setJavaPreference(AssistOptions.OPTION_PerformDeprecationCheck, AssistOptions.DISABLED)
        addJUnit(4)

        ICompletionProposal[] proposals = createProposalsAtOffset('Assert', 2)
        proposalExists(proposals, 'Assert - junit.framework', 1)
        proposalExists(proposals, 'Assert - org.junit', 1)
    }

    @Test
    void testDeprecationCheck1() {
        setJavaPreference(AssistOptions.OPTION_PerformDeprecationCheck, AssistOptions.ENABLED)
        addJUnit(4)

        ICompletionProposal[] proposals = createProposalsAtOffset('Assert', 2)
        proposalExists(proposals, 'Assert - junit.framework', 0)
        proposalExists(proposals, 'Assert - org.junit', 1)
    }
}
