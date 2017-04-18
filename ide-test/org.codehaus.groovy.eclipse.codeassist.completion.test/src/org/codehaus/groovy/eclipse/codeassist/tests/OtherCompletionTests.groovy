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

import org.codehaus.groovy.eclipse.GroovyPlugin
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Before
import org.junit.Test

/**
 * Tests specific bug reports.
 */
final class OtherCompletionTests extends CompletionTestCase {

    @Before
    void setUp() {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false)
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_PARAMETER_GUESSING, false)
    }

    @Test
    void testGreclipse414() {
        String contents =
            "public class Test {\n" +
                "int i\n" +
                "Test() {\n" +
                    "this.i = 42\n" +
                "}\n" +
            "Test(Test other) {\n" +
                    "this.i = other.i\n" +
                "}\n" +
            "}"
        ICompilationUnit unit = addGroovySource(contents, "File", "")
        // ensure that there is no ArrayIndexOutOfBoundsException thrown.
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "this."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "i", 1)
    }

    // type signatures were popping up in various places in the display string
    // ensure this doesn't happen
    @Test
    void testGreclipse422() {
        String javaClass =
            "public class StringExtension {\n" +
                "public static String bar(String self) {\n" +
                    "return self;\n" +
                "}\n" +
            "}\n"
        addJavaSource(javaClass, "StringExtension", "")

        String groovyClass =
             "public class MyClass {\n" +
                 "public void foo() {\n" +
                     "String foo = 'foo';\n" +
                     "use (StringExtension) {\n" +
                         "foo.bar()\n" +
                     "}\n" +
                     "this.collect\n" +
                 "}\n" +
             "}"

        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "foo.ba"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "bar", 1)
        assert proposals[0].getDisplayString() == "bar() : String - StringExtension (Category: StringExtension)"

        proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "this.collect"), GroovyCompletionProposalComputer)
        Arrays.sort(proposals, { ICompletionProposal o1, ICompletionProposal o2 ->
            o2.getDisplayString() <=> o1.getDisplayString()
        } as Comparator<ICompletionProposal>)
        proposalExists(proposals, "collect", 3)
        assert proposals[0].getDisplayString().matches(
            "collect\\(Collection \\w+, Closure \\w+\\) : Collection - DefaultGroovyMethods \\(Category: DefaultGroovyMethods\\)") : printProposals(proposals)
        assert proposals[1].getDisplayString().matches(
            "collect\\(Closure \\w+\\) : List - DefaultGroovyMethods \\(Category: DefaultGroovyMethods\\)") : printProposals(proposals)
        assert proposals[2].getDisplayString().matches(
            "collect\\(\\) : Collection - DefaultGroovyMethods \\(Category: DefaultGroovyMethods\\)") : printProposals(proposals)
    }

    @Test
    void testVisibility() {
        String groovyClass =
            "class B { }\n" +
            "class C {\n" +
                "B theB\n" +
            "}\n" +
            "new C().th\n"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "().th"), GroovyCompletionProposalComputer)

        proposalExists(proposals, "theB", 1)
        assert proposals[0].getDisplayString() == "theB : B - C (Groovy)"
    }

    @Test
    void testGString1() {
        String groovyClass = '\"${new String().c}\"'
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, ".c"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "center", 2)
    }

    @Test
    void testGString2() {
        String groovyClass = '\"\"\"${new String().c}\"\"\"'
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, ".c"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "center", 2)
    }

    // GRECLIPSE-706
    @Test
    void testContentAssistInInitializers1() {
        String groovyClass =
            "class A { { aa }\n def aaaa }"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "aa"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "aaaa", 1)
    }

    // GRECLIPSE-706
    @Test
    void testContentAssistInInitializers2() {
        String groovyClass =
            "class A { {  }\n def aaaa }"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "{ { "), GroovyCompletionProposalComputer)
        proposalExists(proposals, "aaaa", 1)
    }

    // GRECLIPSE-706
    @Test
    void testContentAssistInStaticInitializers1() {
        String groovyClass =
            "class A { static { aa }\n static aaaa }"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "aa"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "aaaa", 1)
    }

    // GRECLIPSE-706
    @Test
    void testContentAssistInStaticInitializers2() {
        String groovyClass =
            "class A { static {  }\n static aaaa }"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "static { "), GroovyCompletionProposalComputer)
        proposalExists(proposals, "aaaa", 1)
    }

    // GRECLIPSE-692
    @Test
    void testMethodWithSpaces() {
        String groovyClass =
            "class A { def \"ff f\"()  { ff } }"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "{ ff"), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "\"ff f\"()", 1)
    }

    // GRECLIPSE-692
    @Test
    void testMethodWithSpaces2() {
        String groovyClass =
            "class A { def \"fff\"()  { fff } }"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "{ fff"), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "fff()", 1)
    }

    // STS-1165 content assist after a static method call was broken
    @Test
    void testAfterStaticCall() {
        String groovyClass =
            "class A { static xxx(x) { }\n def something() {\nxxx oth }\ndef other}"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "oth"), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "other", 1)
    }

    @Test
    void testArrayCompletion1() {
        String groovyClass = "class XX { \nXX[] xx\nXX yy }\nnew XX().xx[0].x"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "x"), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "xx", 1)
    }

    @Test
    void testArrayCompletion2() {
        String groovyClass = "class XX { \nXX[] xx\nXX yy }\nnew XX().xx[0].getX"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "getX"), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "getXx()", 1)
    }

    @Test
    void testArrayCompletion3() {
        String groovyClass = "class XX { \nXX[] xx\nXX yy }\nnew XX().xx[0].setX"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "setX"), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "setXx(value)", 1)
    }

    @Test
    void testArrayCompletion4() {
        String groovyClass = "class XX { \nXX[] xx\nXX yy }\nnew XX().getXx()[0].x"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "x"), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "xx", 1)
    }

    @Test
    void testListCompletion1() {
        String groovyClass = "[]."
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "."), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, ["removeAll(arg0)","removeAll(c)"] as String[], 1)
    }

    @Test
    void testListCompletion2() {
        String groovyClass = "[].re"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, ".re"), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, ["removeAll(arg0)","removeAll(c)"] as String[], 1)
    }

    // GRECLIPSE-1165
    @Test
    void testSpreadCompletion1() {
        String groovyClass = "[1,2,3]*.intValue()[0].value"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "."), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "value", 1)
    }

    // GRECLIPSE-1165
    @Test
    void testSpreadCompletion2() {
        String groovyClass = "[1,2,3]*.intValue()[0].value"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, ".va"), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "value", 1)
    }

    // GRECLIPSE-1165
    @Test
    void testSpreadCompletion3() {
        String groovyClass = "[x:1,y:2,z:3]*.getKey()"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "."), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "getKey()", 1)
    }

    // GRECLIPSE-1165
    @Test
    void testSpreadCompletion4() {
        String groovyClass = "[x:1,y:2,z:3]*.getKey()"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, ".get"), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "getKey()", 1)
    }

    // GRECLIPSE-1165
    @Test
    void testSpreadCompletion5() {
        String groovyClass = "[x:1,y:2,z:3]*.key[0].toLowerCase()"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "."), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "toLowerCase()", 1)
    }

    // GRECLIPSE-1165
    @Test
    void testSpreadCompletion6() {
        String groovyClass = "[x:1,y:2,z:3]*.key[0].toLowerCase()"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, ".to"), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "toLowerCase()", 1)
    }

    // GRECLIPSE-1165
    @Test
    void testSpreadCompletion7() {
        String groovyClass = "[x:1,y:2,z:3]*.value[0].intValue()"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "."), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "intValue()", 1)
    }

    // GRECLIPSE-1165
    @Test
    void testSpreadCompletion8() {
        String groovyClass = "[x:1,y:2,z:3]*.value[0].intValue()"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, ".int"), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "intValue()", 1)
    }

    // GRECLIPSE-1165
    @Test
    void testSpreadCompletion9() {
        String groovyClass = "[1,2,3]*.value[0].value"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "."), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "value", 1)
    }

    // GRECLIPSE-1165
    @Test
    void testSpreadCompletion10() {
        String groovyClass = "[1,2,3]*.value[0].value"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, ".val"), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "value", 1)
    }

    // GRECLIPSE-1165
    @Test
    void testSpreadCompletion11() {
        String groovyClass = "[1,2,3]*.value"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "."), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "value", 1)
    }

    // GRECLIPSE-1165
    @Test
    void testSpreadCompletion12() {
        String groovyClass = "[1,2,3]*.value"
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, ".val"), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, "value", 1)
    }

    // GRECLIPSE-1388
    @Test
    void testBeforeScript() {
        String script = "\n\ndef x = 9"
        ICompilationUnit groovyUnit = addGroovySource(script, "File", "")
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(script, "\n"), GroovyCompletionProposalComputer)
        assertProposalOrdering(proposals, "binding")
    }
}
