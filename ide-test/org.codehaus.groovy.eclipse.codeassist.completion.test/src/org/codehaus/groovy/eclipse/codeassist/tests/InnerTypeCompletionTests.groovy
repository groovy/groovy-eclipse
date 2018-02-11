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
package org.codehaus.groovy.eclipse.codeassist.tests

import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Test

/**
 * Tests that content assist works as expected for inner classes.
 */
final class InnerTypeCompletionTests extends CompletionTestSuite {

    private ICompletionProposal assertProposalCreated(String contents, String expression, String expectedProposal) {
        def proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, expression))
        proposalExists(proposals, expectedProposal, 1)
        findFirstProposal(proposals, expectedProposal)
    }

    @Test
    void testInInnerClass1() {
        String contents = '''\
            class Outer {
              class Inner {
                HTML
              }
            }
            '''.stripIndent()
        assertProposalCreated(contents, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testInInnerClass2() {
        String contents = '''\
            class Outer {
              class Inner {
                def x(HTML) {
                  ;
                }
              }
            }
            '''.stripIndent()
        assertProposalCreated(contents, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testInInnerClass3() {
        String contents = '''\
            class Outer {
              class Inner {
                def x() {
                  HTML
                }
              }
            }
            '''.stripIndent()
        assertProposalCreated(contents, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testInInnerClass4() {
        String contents = '''\
            class Outer {
              class Inner extends HTML {
              }
            }
            '''.stripIndent()
        assertProposalCreated(contents, 'HTML', 'HTML - javax.swing.text.html')
    }

    //

    @Test
    void testInnerClass1() {
        String contents = '''\
            class Outer {
              class Inner {
                Inner f
              }
            }
            '''.stripIndent()
        assertProposalCreated(contents, 'Inner', 'Inner - Outer')
    }

    @Test
    void testInnerClass2() {
        String contents = '''\
            class Outer {
              class Inner {
                private Inner f
              }
            }
            '''.stripIndent()
        assertProposalCreated(contents, 'Inner', 'Inner - Outer')
    }

    @Test
    void testInnerClass3() {
        String contents = '''\
            class Outer {
              class Inner {
              }
            }
            Outer.Inn
            '''.stripIndent()
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - Outer'), contents.replaceFirst(/Inn\b/, 'Inner'))
    }

    @Test
    void testInnerClass4() {
        String contents = '''\
            Map.Ent
            '''.stripIndent()
        applyProposalAndCheck(assertProposalCreated(contents, 'Ent', 'Entry - java.util.Map'), contents.replace('Ent', 'Entry'))
    }

    @Test
    void testInnerClass5() {
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        testInnerClass4() // no difference; no qualifier should be inserted
    }

    @Test
    void testInnerClass6() {
        addGroovySource '''\
            class Outer {
              class Inner {
              }
            }
            ''', 'Outer', 'p'

        String contents = '''\
            import p.Outer
            Outer.Inn
            '''.stripIndent()
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - p.Outer'), contents.replace('Inn', 'Inner'))
    }

    @Test
    void testInnerClass7() {
        addGroovySource '''\
            class Outer {
              class Inner {
              }
            }
            ''', 'Outer', 'q'

        String contents = '''\
            q.Outer.Inn
            '''.stripIndent()
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - q.Outer'), contents.replace('Inn', 'Inner'))
    }

    @Test
    void testInnerClass8() {
        addGroovySource '''\
            class Outer {
              class Inner {
              }
            }
            ''', 'Outer', 'r'

        String contents = '''\
            import r.Outer
            class Foo extends Outer.Inn
            '''.stripIndent()
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - r.Outer'), contents.replace('Inn', 'Inner'))
    }

    @Test
    void testInnerClass9() {
        addGroovySource '''\
            class Outer {
              class Inner {
              }
            }
            ''', 'Outer', 's'

        String contents = '''\
            import s.Outer
            class Foo implements Outer.Inn
            '''.stripIndent()
        def proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'Inn'))
        assert !proposals
    }

    @Test
    void testInnerClass10() {
        addGroovySource '''\
            class Outer {
              interface Inner {
              }
            }
            ''', 'Outer', 't'

        String contents = '''\
            import t.Outer
            class Foo extends Outer.Inn
            '''.stripIndent()
        def proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'Inn'))
        assert !proposals
    }

    @Test
    void testInnerClass11() {
        addGroovySource '''\
            class Outer {
              interface Inner {
              }
            }
            ''', 'Outer', 'u'

        String contents = '''\
            import u.Outer
            class Foo implements Outer.Inn
            '''.stripIndent()
            applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - u.Outer'), contents.replace('Inn', 'Inner'))
    }

    @Test
    void testInnerClass12() {
        addGroovySource '''\
            class Outer {
              class Inner {
              }
            }
            ''', 'Outer', 'v'

        String contents = '''\
            Outer.Inn
            '''.stripIndent()
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - v.Outer'), '''\
            |import v.Outer
            |
            |Outer.Inner
            |'''.stripMargin())
    }

    @Test
    void testInnerClass13() {
        addGroovySource '''\
            class Outer {
              class Inner {
              }
            }
            ''', 'Outer', 'w'

        String contents = '''\
            Outer.Inn
            '''.stripIndent()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - w.Outer'), '''\
            |w.Outer.Inner
            |'''.stripMargin())
    }

    @Test
    void testInnerClass14() {
        addGroovySource '''\
            class Outer {
              class Inner {
                class Nucleus {
                }
              }
            }
            ''', 'Outer', 'x'

        String contents = '''\
            x.Outer.Inner.N
            '''.stripIndent()
        applyProposalAndCheck(assertProposalCreated(contents, 'N', 'Nucleus - x.Outer.Inner'), contents.replace('N', 'Nucleus'))
    }

    @Test
    void testInnerClass15() {
        addGroovySource '''\
            class Outer {
              class Inner {
              }
            }
            ''', 'Outer', 'y'

        String contents = '''\
            Inn
            '''.stripIndent()
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - y.Outer'), '''\
            |import y.Outer.Inner
            |
            |Inner
            |'''.stripMargin())
    }

    @Test
    void testInnerClass16() {
        addGroovySource '''\
            class Outer {
              class Inner {
              }
            }
            ''', 'Outer', 'z'

        String contents = '''\
            Inn
            '''.stripIndent()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - z.Outer'), '''\
            |z.Outer.Inner
            |'''.stripMargin())
    }

    //

    @Test
    void testInnerMember1() {
        String contents = '''\
            class Outer {
              class Inner {
                def xxx
                def y() {
                  xxx
                }
              }
            }
            '''.stripIndent()
        assertProposalCreated(contents, 'xxx', 'xxx')
    }

    @Test
    void testInnerMember2() {
        String contents = '''\
            class Outer {
              class Inner {
                def xxx
              }
              Inner i
              def y() {
                i.xxx
              }
            }
            '''.stripIndent()
        assertProposalCreated(contents, 'xxx', 'xxx')
    }

    @Test
    void testInnerMember3() {
        String contents = '''\
            class Outer {
              class Inner {
                def xxx
              }
            }
            def y(Outer.Inner i) {
              i.xxx
            }
            '''.stripIndent()
        assertProposalCreated(contents, 'xxx', 'xxx')
    }

    @Test
    void testInnerMember4() {
        String contents = '''\
            class Outer {
              class Inner {
                def getXxx() {}
              }
            }
            def y(Outer.Inner i) {
              i.xxx
            }
            '''.stripIndent()
        assertProposalCreated(contents, 'xxx', 'xxx')
    }

    @Test
    void testInnerMember5() {
        String contents = '''\
            class Outer {
              class Inner {
                def xxx
              }
            }
            Outer.Inner i
            i.xxx
            '''.stripIndent()
        assertProposalCreated(contents, 'xxx', 'xxx')
    }

    @Test
    void testInnerMember6() {
        String contents = '''\
            class Outer {
              class Inner {
                def getXxx() {}
              }
            }
            Outer.Inner i
            i.xxx
            '''.stripIndent()
        assertProposalCreated(contents, 'xxx', 'xxx')
    }
}
