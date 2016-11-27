/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.tests;

import java.util.Arrays;
import java.util.Comparator;

import junit.framework.ComparisonFailure;
import junit.framework.Test;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Tests specific bug reports
 *
 * @author Andrew Eisenberg
 * @created Sept 29, 2009
 */
public final class OtherCompletionTests extends CompletionTestCase {

    public static Test suite() {
        return newTestSuite(OtherCompletionTests.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_PARAMETER_GUESSING, false);
    }

    public void testGreclipse414() throws Exception {
        String contents =
"public class Test {\n" +
    "int i\n" +
    "Test() {\n" +
        "this.i = 42\n" +
    "}\n" +
"Test(Test other) {\n" +
        "this.i = other.i\n" +
    "}\n" +
"}";
        ICompilationUnit unit = addGroovySource(contents, "File", "");
        // ensure that there is no ArrayIndexOutOfBoundsException thrown.
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "this."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "i", 1);
    }

    // type signatures were popping up in various places in the display string
    // ensure this doesn't happen
    public void testGreclipse422() throws Exception {
        String javaClass =
         "public class StringExtension {\n" +
         "public static String bar(String self) {\n" +
                     "return self;\n" +
                 "}\n" +
             "}\n";
        addJavaSource(javaClass, "StringExtension", "");

        String groovyClass =
             "public class MyClass {\n" +
                 "public void foo() {\n" +
                     "String foo = 'foo';\n" +
                     "use (StringExtension) {\n" +
                         "foo.bar()\n" +
                     "}\n" +
                     "this.collect\n" +
                 "}\n" +
             "}";

        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "foo.ba"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "bar", 1);
        assertEquals ("bar() : String - StringExtension (Category: StringExtension)", proposals[0].getDisplayString());

        proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "this.collect"), GroovyCompletionProposalComputer.class);
        Arrays.sort(proposals, new Comparator<ICompletionProposal>() {
            public int compare(ICompletionProposal o1, ICompletionProposal o2) {
                return - o1.getDisplayString().compareTo(o2.getDisplayString());
            }
        });
        proposalExists(proposals, "collect", 3);
        assertEquals(printProposals(proposals), "collect(Collection collector, Closure transform) : Collection - DefaultGroovyMethods (Category: DefaultGroovyMethods)", proposals[0].getDisplayString().toString());
        try {
        } catch (ComparisonFailure e) {
        }
        if (GroovyUtils.GROOVY_LEVEL >= 18) {
            assertEquals(printProposals(proposals), "collect(Closure transform) : List - DefaultGroovyMethods (Category: DefaultGroovyMethods)", proposals[1].getDisplayString().toString());
            assertEquals(printProposals(proposals), "collect() : Collection - DefaultGroovyMethods (Category: DefaultGroovyMethods)", proposals[2].getDisplayString().toString());
        } else {
            assertEquals(printProposals(proposals), "collect(Closure closure) : List - DefaultGroovyMethods (Category: DefaultGroovyMethods)", proposals[1].getDisplayString().toString());
        }
    }

    public void testVisibility() throws Exception {
        String groovyClass =
"class B { }\n" +
"class C {\n" +
    "B theB\n" +
"}\n" +
"new C().th\n";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "().th"), GroovyCompletionProposalComputer.class);

        proposalExists(proposals, "theB", 1);
        assertEquals("theB : B - C (Groovy)", proposals[0].getDisplayString());
    }

    public void testGString1() throws Exception {
        String groovyClass = "\"${new String().c}\"";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, ".c"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "center", 2);
    }

    public void testGString2() throws Exception {
        String groovyClass = "\"\"\"${new String().c}\"\"\"";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, ".c"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "center", 2);
    }

    // GRECLIPSE-706
    public void testContentAssistInInitializers1() throws Exception {
        String groovyClass =
            "class A { { aa }\n def aaaa }";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "aa"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "aaaa", 1);
    }

    // GRECLIPSE-706
    public void testContentAssistInInitializers2() throws Exception {
        String groovyClass =
            "class A { {  }\n def aaaa }";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "{ { "), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "aaaa", 1);
    }

    // GRECLIPSE-706
    public void testContentAssistInStaticInitializers1() throws Exception {
        String groovyClass =
            "class A { static { aa }\n static aaaa }";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "aa"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "aaaa", 1);
    }

    // GRECLIPSE-706
    public void testContentAssistInStaticInitializers2() throws Exception {
        String groovyClass =
            "class A { static {  }\n static aaaa }";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "static { "), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "aaaa", 1);
    }

    // GRECLIPSE-692
    public void testMethodWithSpaces() throws Exception {
        String groovyClass =
            "class A { def \"ff f\"()  { ff } }";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "{ ff"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "\"ff f\"()", 1);
    }

    // GRECLIPSE-692
    public void testMethodWithSpaces2() throws Exception {
        String groovyClass =
            "class A { def \"fff\"()  { fff } }";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "{ fff"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "fff()", 1);
    }

    // STS-1165 content assist after a static method call was broken
    public void testAfterStaticCall() throws Exception {
        String groovyClass =
            "class A { static xxx(x) { }\n def something() {\nxxx oth }\ndef other}";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "oth"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "other", 1);
    }

    public void testArrayCompletion1() throws Exception {
        String groovyClass = "class XX { \nXX[] xx\nXX yy }\nnew XX().xx[0].x";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "x"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "xx", 1);
    }

    public void testArrayCompletion2() throws Exception {
        String groovyClass = "class XX { \nXX[] xx\nXX yy }\nnew XX().xx[0].getX";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "getX"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "getXx()", 1);
    }

    public void testArrayCompletion3() throws Exception {
        String groovyClass = "class XX { \nXX[] xx\nXX yy }\nnew XX().xx[0].setX";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "setX"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "setXx(value)", 1);
    }

    public void testArrayCompletion4() throws Exception {
        String groovyClass = "class XX { \nXX[] xx\nXX yy }\nnew XX().getXx()[0].x";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "x"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "xx", 1);
    }

    public void testListCompletion1() throws Exception {
        String groovyClass = "[].";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "."), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, new String[]{"removeAll(arg0)","removeAll(c)"}, 1);
    }

    public void testListCompletion2() throws Exception {
        String groovyClass = "[].re";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, ".re"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, new String[]{"removeAll(arg0)","removeAll(c)"}, 1);
    }

    // GRECLIPSE-1165
    public void testSpreadCompletion1() throws Exception {
        String groovyClass = "[1,2,3]*.intValue()[0].value";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "."), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "value", 1);
    }

    // GRECLIPSE-1165
    public void testSpreadCompletion2() throws Exception {
        String groovyClass = "[1,2,3]*.intValue()[0].value";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, ".va"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "value", 1);
    }

    // GRECLIPSE-1165
    public void testSpreadCompletion3() throws Exception {
        String groovyClass = "[x:1,y:2,z:3]*.getKey()";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "."), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "getKey()", 1);
    }

    // GRECLIPSE-1165
    public void testSpreadCompletion4() throws Exception {
        String groovyClass = "[x:1,y:2,z:3]*.getKey()";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, ".get"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "getKey()", 1);
    }

    // GRECLIPSE-1165
    public void testSpreadCompletion5() throws Exception {
        String groovyClass = "[x:1,y:2,z:3]*.key[0].toLowerCase()";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "."), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "toLowerCase()", 1);
    }

    // GRECLIPSE-1165
    public void testSpreadCompletion6() throws Exception {
        String groovyClass = "[x:1,y:2,z:3]*.key[0].toLowerCase()";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, ".to"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "toLowerCase()", 1);
    }

    // GRECLIPSE-1165
    public void testSpreadCompletion7() throws Exception {
        String groovyClass = "[x:1,y:2,z:3]*.value[0].intValue()";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "."), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "intValue()", 1);
    }

    // GRECLIPSE-1165
    public void testSpreadCompletion8() throws Exception {
        String groovyClass = "[x:1,y:2,z:3]*.value[0].intValue()";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, ".int"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "intValue()", 1);
    }

    // GRECLIPSE-1165
    public void testSpreadCompletion9() throws Exception {
        String groovyClass = "[1,2,3]*.value[0].value";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "."), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "value", 1);
    }

    // GRECLIPSE-1165
    public void testSpreadCompletion10() throws Exception {
        String groovyClass = "[1,2,3]*.value[0].value";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, ".val"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "value", 1);
    }

    // GRECLIPSE-1165
    public void testSpreadCompletion11() throws Exception {
        String groovyClass = "[1,2,3]*.value";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, "."), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "value", 1);
    }

    // GRECLIPSE-1165
    public void testSpreadCompletion12() throws Exception {
        String groovyClass = "[1,2,3]*.value";
        ICompilationUnit groovyUnit = addGroovySource(groovyClass, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(groovyClass, ".val"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "value", 1);
    }

    // GRECLIPSE-1388
    public void testBeforeScript() throws Exception {
        String script = "\n\ndef x = 9";
        ICompilationUnit groovyUnit = addGroovySource(script, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(script, "\n"), GroovyCompletionProposalComputer.class);
        assertProposalOrdering(proposals, "binding");
    }
}
