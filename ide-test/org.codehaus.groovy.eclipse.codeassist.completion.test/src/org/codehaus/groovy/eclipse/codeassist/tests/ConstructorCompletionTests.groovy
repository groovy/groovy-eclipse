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

import groovy.transform.NotYetImplemented

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Before
import org.junit.Test

/**
 * Tests that constructor completions are working properly.  Ensures that the
 * resulting document has the correct text in it.
 */
final class ConstructorCompletionTests extends CompletionTestSuite {

    @Before
    void setUp() {
        // filter some legacy packages
        setJavaPreference(PreferenceConstants.TYPEFILTER_ENABLED, 'sun.*;com.sun.*;org.omg.*')

        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'true')
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'true')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)
    }

    @Test
    void testConstructorCompletion1() {
        String contents = 'class YYY { YYY() { } }\nnew YY\nkkk'
        String expected = 'class YYY { YYY() { } }\nnew YYY()\nkkk'
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test @NotYetImplemented // do this with Ctrl triggering of constructor proposal
    void testConstructorCompletion2() {
        String contents = 'class YYY { YYY() { } }\nnew YY()\nkkk' // trailing parens
        String expected = 'class YYY { YYY() { } }\nnew YYY()\nkkk'
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test
    void testConstructorCompletion3() {
        String contents = 'class YYY { YYY(x) { } }\nnew YY\nkkk'
        String expected = 'class YYY { YYY(x) { } }\nnew YYY(x)\nkkk'
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test
    void testConstructorCompletion4() {
        String contents = 'class YYY { YYY(x, y) { } }\nnew YY\nkkk'
        String expected = 'class YYY { YYY(x, y) { } }\nnew YYY(x, y)\nkkk'
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test
    void testConstructorCompletionWithGenerics1() {
        String contents = 'List<String> list = new ArrayL'
        String expected = 'List<String> list = new ArrayList()'
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new ArrayL'), 'ArrayList()')
    }

    @Test
    void testContructorCompletionWithinEnumDeclaration1() {
        String contents = 'class YYY { YYY() { } }\nenum F {\n' +
            '	Aaa() {\n@Override int foo() {\nnew YY\n}\n}\nint foo() {\n	}\n}'
        String expected = 'class YYY { YYY() { } }\nenum F {\n' +
            '	Aaa() {\n@Override int foo() {\nnew YYY()\n}\n}\nint foo() {\n	}\n}'
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test
    void testContructorCompletionWithinEnumDeclaration2() {
        String contents = 'class YYY { YYY() { } }\nenum F {\n' +
            '	Aaa {\n@Override int foo() {\nnew YY\n}\n}\nint foo() {\n	}\n}'
        String expected = 'class YYY { YYY() { } }\nenum F {\n' +
            '	Aaa {\n@Override int foo() {\nnew YYY()\n}\n}\nint foo() {\n	}\n}'
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test
    void testContructorCompletionWithQualifier() {
        String contents = 'new java.text.Anno'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length());
        proposalExists(proposals, 'AnnotationVisitor', 0)
        proposalExists(proposals, 'Annotation', 1)
    }

    @Test
    void testConstructorCompletionInnerClass1() {
        addGroovySource '''\
            class Outer {
              static class Inner {
                Inner() {}
              }
            }
            '''.stripIndent(), 'Outer', 'a'

        String contents = '''\
            new a.Outer.Inn
            '''.stripIndent()
        applyProposalAndCheck(checkUniqueProposal(contents, 'Inn', 'Inner() - a.Outer.Inner', '()'), contents.replace('Inn', 'Inner()'))
    }

    @Test
    void testConstructorCompletionInnerClass2() {
        addGroovySource '''\
            class Outer {
              static class Inner {
                Inner() {}
              }
            }
            '''.stripIndent(), 'Outer', 'b'

        String contents = '''\
            new Outer.Inn
            '''.stripIndent()
        applyProposalAndCheck(checkUniqueProposal(contents, 'Inn', 'Inner() - b.Outer.Inner', '()'), '''\
            |import b.Outer
            |
            |new Outer.Inner()
            |'''.stripMargin())
    }

    @Test
    void testConstructorCompletionInnerClass3() {
        addGroovySource '''\
            class Outer {
              static class Inner {
                Inner() {}
              }
            }
            '''.stripIndent(), 'Outer', 'c'

        String contents = '''\
            new Outer.Inn
            '''.stripIndent()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        applyProposalAndCheck(checkUniqueProposal(contents, 'Inn', 'Inner() - c.Outer.Inner', '()'), '''\
            |new c.Outer.Inner()
            |'''.stripMargin())
    }

    @Test
    void testConstructorCompletionInnerClass4() {
        addGroovySource '''\
            class Outer {
              static class XyzInner {
                XyzInner() {}
              }
            }
            '''.stripIndent(), 'Outer', 'd'

        String contents = '''\
            new XyzInn
            '''.stripIndent()
        applyProposalAndCheck(checkUniqueProposal(contents, 'XyzInn', 'XyzInner() - d.Outer.XyzInner', '()'), '''\
            |import d.Outer.XyzInner
            |
            |new XyzInner()
            |'''.stripMargin())
    }

    @Test
    void testConstructorCompletionInnerClass5() {
        addGroovySource '''\
            class Outer {
              static class XyzInner {
                XyzInner() {}
              }
            }
            '''.stripIndent(), 'Outer', 'e'

        String contents = '''\
            new XyzInn
            '''.stripIndent()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        applyProposalAndCheck(checkUniqueProposal(contents, 'XyzInn', 'XyzInner() - e.Outer.XyzInner', '()'), '''\
            |new e.Outer.XyzInner()
            |'''.stripMargin())
    }

    @Test
    void testConstructorCompletionInnerClass6() {
        addGroovySource '''\
            class Outer {
              static class Inner {
                Inner() {}
              }
            }
            '''.stripIndent(), 'Outer', 'f'

        String contents = '''\
            import f.Outer
            new Outer.Inn
            '''.stripIndent()
        applyProposalAndCheck(checkUniqueProposal(contents, 'Inn', 'Inner() - f.Outer.Inner', '()'), '''\
            |import f.Outer
            |new Outer.Inner()
            |'''.stripMargin())
    }

    @Test
    void testConstructorCompletionInnerClass7() {
        addGroovySource '''\
            class Outer {
              static class Inner {
                Inner() {}
              }
            }
            '''.stripIndent(), 'Outer', 'g'

        String contents = '''\
            import g.Outer
            new Outer.Inn
            '''.stripIndent()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        applyProposalAndCheck(checkUniqueProposal(contents, 'Inn', 'Inner() - g.Outer.Inner', '()'), '''\
            |import g.Outer
            |new Outer.Inner()
            |'''.stripMargin())
    }

    @Test
    void testConstructorCompletionInnerClass8() {
        addGroovySource '''\
            class Outer {
              static class Inner {
                Inner() {}
              }
            }
            '''.stripIndent(), 'Outer', 'h'

        String contents = '''\
            import h.*
            new Outer.Inn
            '''.stripIndent()
        applyProposalAndCheck(checkUniqueProposal(contents, 'Inn', 'Inner() - h.Outer.Inner', '()'), '''\
            |import h.*
            |new Outer.Inner()
            |'''.stripMargin())
    }

    @Test
    void testConstructorCompletionInnerClass9() {
        addGroovySource '''\
            class Outer {
              static class Inner {
                Inner() {}
              }
            }
            '''.stripIndent(), 'Outer', 'i'

        String contents = '''\
            import i.*
            new Outer.Inn
            '''.stripIndent()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        applyProposalAndCheck(checkUniqueProposal(contents, 'Inn', 'Inner() - i.Outer.Inner', '()'), '''\
            |import i.*
            |new Outer.Inner()
            |'''.stripMargin())
    }

    @Test
    void testConstructorCompletionInnerClass10() {
        addGroovySource '''\
            class Outer {
              static class Inner {
                Inner(Number number, String string) {}
              }
            }
            '''.stripIndent(), 'Outer', 'j'

        String contents = '''\
            new j.Outer.Inn
            '''.stripIndent()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        applyProposalAndCheck(checkUniqueProposal(contents, 'Inn', 'Inner(Number number, String string) - j.Outer.Inner', '(number, string)'), contents.replace('Inn', 'Inner(number, string)'))
    }

    @Test
    void testConstructorCompletionInnerClass11() {
        addGroovySource '''\
            class Outer {
              static class Inner {
                Inner(Number number, String string) {}
              }
            }
            '''.stripIndent(), 'Outer', 'k'

        String contents = '''\
            new k.Outer.Inner()
            '''.stripIndent()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false') // TODO: Should not need to remove the qualifier
        applyProposalAndCheck(checkUniqueProposal(contents, '(', 'Inner(Number number, String string) - k.Outer.Inner' - ~/k.Outer./, ''), contents) // context display
    }

    @Test
    void testContructorCompletionImportHandling0() {
        String contents = '''\
            def a = new java.text.Anno
            '''.stripIndent()
        String expected = '''\
            def a = new java.text.Annotation(value)
            '''.stripIndent()
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'Anno'), 'Annotation')
    }

    @Test
    void testContructorCompletionImportHandling1() {
        String contents = '''\
            def a = new Anno
            '''.stripIndent()
        String expected = '''\
            |import java.text.Annotation
            |
            |def a = new Annotation(value)
            |'''.stripMargin()
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new Anno'), 'Annotation')
    }

    @Test
    void testNamedArgs1() {
        String contents = '''\
            class Foo {
              String aaa
              int bbb
              Date ccc
            }
            new Foo()
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testNamedArgs2() {
        addGroovySource('''\
            class Foo {
              String aaa
              int bbb
              Date ccc
            }
            '''.stripIndent(), 'Foo')

        String contents = 'new Foo()' // separate source

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testNamedArgs3() {
        addGroovySource('''\
            class Foo {
              String aaa
              int bbb
              Date ccc
            }
            '''.stripIndent(), 'Foo', 'p')

        String contents = 'new p.Foo()' // fully-qualified reference

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testNamedArgs4() {
        String contents = '''\
            class Foo {
              String aaa
              int bbb
              Date ccc
            }
            new Foo(aaa:'1', )
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, ', '))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testNamedArgs5() {
        String contents = '''\
            class Foo {
              String aaa
              int bbb
              Date ccc
            }
            new Foo(aaa:'1', bbb:2, )
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, ', '))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testNamedArgs6() {
        String contents = '''\
            class Foo {
              String aaa
              int bbb
              Date ccc
            }
            new Foo(aaa:'1', bbb:2, ccc:null)
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 0)
    }

    @Test // STS-2628: ensure no double adding of named properties for booleans
    void testNamedArgs7() {
        String contents = '''\
            class Foo {
              boolean aaa
              boolean bbb
              boolean ccc
            }
            new Foo()
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testNamedArgs8() {
        String contents = '''\
            class Foo {
              String bar
              private String baz
              def setBaz(String baz) { this.baz = baz }
            }
            new Foo()
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'bar : __', 1)
        proposalExists(proposals, 'baz : __', 1)
    }

    @Test // explicit no-arg constructor exists
    void testNamedArgs9() {
        String contents = '''\
            class Foo {
              Foo() { }
              String aaa
              int bbb
              Date ccc
            }
            new Foo()
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testNamedArgs10() {
        String contents = '''\
            class Foo {
              String aaa
              Number bbb
            }
            new Foo(a)
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'a'))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 0)
    }

    @Test
    void testNamedArgs11() {
        String contents = '''\
            class Foo {
              String aaa
              Number abc
            }
            new Foo(a)
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'a'))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'abc : __', 1)
    }

    @Test
    void testNamedArgs12() {
        String contents = '''\
            class Foo {
              String aaa
              void setAbc(Number abc) {}
            }
            new Foo(a)
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'a'))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'abc : __', 1)
    }

    @Test
    void testNamedArgs13() {
        String contents = '''\
            class Foo {
              String aaa
              void setXyz(Number xyz) {}
            }
            new Foo(a)
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'a'))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'xyz : __', 0)
    }

    @Test
    void testNamedArgs15() {
        addGroovySource '''\
            class Outer {
              static class Inner {
                Number number
                String string
                Inner() {}
              }
            }
            '''.stripIndent(), 'Outer', 'pack'

        String contents = '''\
            new pack.Outer.Inner()
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'number : __', 1)
        proposalExists(proposals, 'string : __', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/404
    void testNamedArgs16() {
        addGroovySource '''\
            class Outer {
              static class Inner {
                Number number
                String string
              }
            }
            '''.stripIndent(), 'Outer', 'qual'

        String contents = '''\
            import qual.Outer
            new Outer.Inner()
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'number : __', 1)
        proposalExists(proposals, 'string : __', 1)
    }

    @Test
    void testNamedArgs14() {
        String contents = '''\
            class Foo {
              String aaa
              String aaaBbbCccDdd
              void setAaaBbbCcc(Object value) {}
            }
            new Foo(aBC)
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'aBC'))
        proposalExists(proposals, 'aaaBbbCccDdd : __', 1)
        proposalExists(proposals, 'aaaBbbCcc : __', 1)
        proposalExists(proposals, 'aaa : __', 0)
    }

    @Test // explicit no-arg and tuple constructors exist
    void testNoNamedArgs() {
        String contents = '''\
            class Foo {
              Foo() { }
              Foo(a,b,c) { }
              String aaa
              int bbb
              Date ccc
            }
            new Foo()
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 0)
        proposalExists(proposals, 'Foo', 2)
    }

    @Test
    void testNamedArgumentTrigger1() {
        addGroovySource '''\
            class Foo {
              Number number
              String string
            }
            '''.stripIndent()

        String contents = 'new Foo()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        applyProposalAndCheck(findFirstProposal(proposals, 'number : __'), 'new Foo(number: __,)', ',' as char)
    }

    @Test @NotYetImplemented
    void testNamedArgumentTrigger2() {
        addGroovySource '''\
            class Foo {
              Number number
              String string
            }
            '''.stripIndent()

        String contents = 'new Foo()'
        setJavaPreference(PreferenceConstants.EDITOR_SMART_SEMICOLON, 'true')
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        applyProposalAndCheck(findFirstProposal(proposals, 'number : __'), 'new Foo(number: __);', ';' as char)
    }

    @Test
    void testParamGuessing1() {
        addGroovySource('''\
            class Flar {
              String aaa
              int bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar', 'p')

        String contents = '''\
            import p.Flar
            String xxx
            int yyy
            boolean zzz
            new Flar()
            '''.stripIndent()

        String[] expectedChoices = [ 'yyy', '0' ]
        checkProposalChoices(contents, 'Flar(', 'bbb', 'bbb: __', expectedChoices)
    }

    @Test
    void testParamGuessing2() {
        addGroovySource('''\
            class Flar {
              String aaa
              int bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar', 'p')

        String contents = '''\
            String xxx
            int yyy
            boolean zzz
            new p.Flar()
            '''.stripIndent()

        String[] expectedChoices = [ 'yyy', '0' ]
        checkProposalChoices(contents, 'Flar(', 'bbb', 'bbb: __', expectedChoices)
    }

    @Test
    void testParamGuessing3() {
        addGroovySource('''\
            class Flar {
              String aaa
              int bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar', 'p')

        String contents = '''\
            import p.Flar
            String xxx
            Integer yyy
            boolean zzz
            new Flar()
            '''.stripIndent()

        String[] expectedChoices = [ 'yyy', '0' ]
        checkProposalChoices(contents, 'Flar(', 'bbb', 'bbb: __', expectedChoices)
    }

    @Test
    void testParamGuessing4() {
        addGroovySource('''\
            class Flar {
              String aaa
              Integer bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar', 'p')

        String contents = '''\
            import p.Flar
            String xxx
            Integer yyy
            boolean zzz
            new Flar()
            '''.stripIndent()

        String[] expectedChoices = [ 'yyy', '0' ]
        checkProposalChoices(contents, 'Flar(', 'bbb', 'bbb: __', expectedChoices)
    }

    @Test
    void testParamGuessing5() {
        addGroovySource('''\
            class Flar {
              String aaa
              Integer bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar', 'p')

        String contents = '''\
            import p.Flar
            String xxx
            int yyy
            boolean zzz
            new Flar()
            '''.stripIndent()

        String[] expectedChoices = [ 'yyy', '0' ]
        checkProposalChoices(contents, 'Flar(', 'bbb', 'bbb: __', expectedChoices)
    }

    @Test
    void testParamGuessing6() {
        addGroovySource('''\
            class Flar {
              String aaa
              Integer bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar', 'p')

        String contents = '''\
            import p.Flar
            String xxx
            int yyy
            boolean zzz
            new Flar()
            '''.stripIndent()

        String[] expectedChoices = [ 'xxx', '""' ]
        checkProposalChoices(contents, 'Flar(', 'aaa', 'aaa: __', expectedChoices)
    }

    @Test
    void testParamGuessing7() {
        addGroovySource('''\
            class Flar {
              Closure aaa
              Integer bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar', 'p')

        String contents = '''\
            import p.Flar
            Closure xxx
            int yyy
            boolean zzz
            new Flar()
            '''.stripIndent()

        String[] expectedChoices = [ 'xxx', '{  }' ]
        checkProposalChoices(contents, 'Flar(', 'aaa', 'aaa: __', expectedChoices)
    }

    @Test
    void testParamGuessTrigger1() {
        addGroovySource '''\
            class Foo {
              Number number
              String string
            }
            '''.stripIndent()

        def proposal = checkUniqueProposal('new Foo()', 'new Foo(', 'number', 'number: __')

        applyProposalAndCheck(proposal, 'new Foo(number: __)')
        //TODO:applyProposalAndCheck(proposal.choices[0], 'new Foo(number: null,)', ',' as char)
        applyProposalAndCheck(new Document('new Foo(number: __)'), proposal.choices[0], 'new Foo(number: null)')
    }
}
