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

import junit.framework.Test;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Tests that completion proposals are sufficiently groovy-like in their output
 *
 * @author Andrew Eisenberg
 * @created Jun 5, 2009
 */
public final class GroovyLikeCompletionTests extends CompletionTestCase {

    private static final String SCRIPTCONTENTS =
            "any\n" +
            "clone\n" +
            "findIndexOf\n" +
            "inject\n" +
            "class Foo {\n" +
            "  Foo(first, second) { }\n" +
            "  Foo(int third) { }\n" +
            "  def method1(arg) { }\n" +
            "  def method2(arg, Closure c1) { }\n" +
            "  def method3(arg, Closure c1, Closure c2) { }\n" +
            "}\n" +
            "new Foo()";
    private final static String CLOSURE_CONTENTS =
            "class Other {\n" +
            "    def first\n" +
            "    def second2() { } \n" +
            "}\n" +
            " \n" +
            "class MyOtherClass extends Other {\n" +
            "    def meth() {\n" +
            "        \"\".foo {\n" +
            "            substring(0)\n" +  // should find
            "            first\n" +  // should find
            "            second2()\n" +  // should find
            "            delegate.substring(0)\n" +  // should find
            "            delegate.first(0)\n" + // should not find
            "            delegate.second2(0)\n" + // should not find
            "            this.substring(0)\n" + // should not find
            "            this.first(0)\n" + // should find
            "            this.second2(0)\n" +  // should find
            "            wait\n" +  // should find 2 only
            "        }\n" +
            "    }\n" +
            "}";
    private final static String CLOSURE_CONTENTS2 =
            "class Other {\n" +
            "    def first\n" +
            "    def second2() { } \n" +
            "}\n" +
            "class Other2 extends Other { }\n" +
            "class MyOtherClass extends Other {\n" +
            "    def meth() {\n" +
            "        new Other2().foo {\n" +
            "            first\n" +  // should find 2 only
            "        }\n" +
            "    }\n" +
            "}";

    public static Test suite() {
        return newTestSuite(GroovyLikeCompletionTests.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IPreferenceStore prefs = GroovyPlugin.getDefault().getPreferenceStore();
        prefs.setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, true);
        prefs.setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, true);
        prefs.setValue(PreferenceConstants.GROOVY_CONTENT_NAMED_ARGUMENTS, false);
        prefs.setValue(PreferenceConstants.GROOVY_CONTENT_PARAMETER_GUESSING, false);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        IPreferenceStore prefs = GroovyPlugin.getDefault().getPreferenceStore();
        prefs.setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, true);
        prefs.setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, true);
        prefs.setValue(PreferenceConstants.GROOVY_CONTENT_NAMED_ARGUMENTS, false);
        prefs.setValue(PreferenceConstants.GROOVY_CONTENT_PARAMETER_GUESSING, true);
    }

    public void testMethodWithClosure() throws Exception {
        ICompilationUnit unit = addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "any"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "any {  }", 1);
    }

    public void testMethodWithNoArgs() throws Exception {
        ICompilationUnit unit = addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "clone"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "clone()", 1);
    }

    public void testMethodWith2Args() throws Exception {
        ICompilationUnit unit = addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "findIndexOf"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "findIndexOf(startIndex) {  }", 1);
    }

    public void testMethodWithClosureNotGroovyLike() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, false);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        ICompilationUnit unit = addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "any"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "any(closure)", 1);
    }

    public void testMethodWith2ArgsNotGroovyLike() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, false);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        ICompilationUnit unit = addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "findIndexOf"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "findIndexOf(startIndex, closure)", 1);
    }

    public void testClosureApplication1a() throws Exception {
        addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        String contents = "new Foo().method1";
        String expected = "new Foo().method1(arg)";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method1");
    }

    public void testClosureApplication1b() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        String contents = "new Foo().method1";
        String expected = "new Foo().method1(arg)";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method1");
    }

    public void testClosureApplication1c() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, false);
        addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        String contents = "new Foo().method1";
        String expected = "new Foo().method1(arg)";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method1");
    }

    public void testClosureApplication1d() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, false);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        String contents = "new Foo().method1";
        String expected = "new Foo().method1(arg)";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method1");
    }

    public void testClosureApplication2a() throws Exception {
        addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        String contents = "new Foo().method2";
        String expected = "new Foo().method2(arg) {  }";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method2");
    }

    public void testClosureApplication2b() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        String contents = "new Foo().method2";
        String expected = "new Foo().method2(arg, {  })";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method2");
    }

    public void testClosureApplication2c() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, false);
        addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        String contents = "new Foo().method2";
        String expected = "new Foo().method2(arg) c1";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method2");
    }

    public void testClosureApplication2d() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, false);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        String contents = "new Foo().method2";
        String expected = "new Foo().method2(arg, c1)";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method2");
    }

    public void testClosureApplication3a() throws Exception {
        addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        String contents = "new Foo().method3";
        String expected = "new Foo().method3(arg, {  }) {  }";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method3");
    }

    public void testClosureApplication3b() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        String contents = "new Foo().method3";
        String expected = "new Foo().method3(arg, {  }, {  })";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method3");
    }

    public void testClosureApplication3c() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, false);
        addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        String contents = "new Foo().method3";
        String expected = "new Foo().method3(arg, c1) c2";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method3");
    }

    public void testClosureApplication3d() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, false);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        String contents = "new Foo().method3";
        String expected = "new Foo().method3(arg, c1, c2)";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method3");
    }

    // accessing members of super types in closures
    public void testClosureCompletion1() throws Exception {
        ICompilationUnit groovyUnit = addGroovySource(CLOSURE_CONTENTS, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, " substring"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "substring(beginIndex)", 1);
    }

    // accessing members of super types in closures
    public void testClosureCompletion2() throws Exception {
        ICompilationUnit groovyUnit = addGroovySource(CLOSURE_CONTENTS, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, " first"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "first", 1);
    }

    // accessing members of super types in closures
    public void testClosureCompletion3() throws Exception {
        ICompilationUnit groovyUnit = addGroovySource(CLOSURE_CONTENTS, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, " second2"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "second2()", 1);
    }

    // accessing members of super types in closures
    public void testClosureCompletion4() throws Exception {
        ICompilationUnit groovyUnit = addGroovySource(CLOSURE_CONTENTS, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, "delegate.substring"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "substring(beginIndex)", 1);
    }

    // accessing members of super types in closures
    public void testClosureCompletion5() throws Exception {
        ICompilationUnit groovyUnit = addGroovySource(CLOSURE_CONTENTS, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, "delegate.first"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "first", 0);
    }

    // accessing members of super types in closures
    public void testClosureCompletion6() throws Exception {
        ICompilationUnit groovyUnit = addGroovySource(CLOSURE_CONTENTS, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, "delegate.second2"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "second2", 0);
    }

    // accessing members of super types in closures
    public void testClosureCompletion7() throws Exception {
        ICompilationUnit groovyUnit = addGroovySource(CLOSURE_CONTENTS, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, "this.substring"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "substring", 0);
    }

    // accessing members of super types in closures
    public void testClosureCompletion8() throws Exception {
        ICompilationUnit groovyUnit = addGroovySource(CLOSURE_CONTENTS, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, "this.first"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "first", 1);
    }

    // accessing members of super types in closures
    public void testClosureCompletion9() throws Exception {
        ICompilationUnit groovyUnit = addGroovySource(CLOSURE_CONTENTS, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, "this.second2"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "second2()", 1);
    }

    // accessing members of super types in closures
    public void testClosureCompletion10() throws Exception {
        ICompilationUnit groovyUnit = addGroovySource(CLOSURE_CONTENTS, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, "wait"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "wait()", 1);
    }

    // accessing members of super types in closures
    public void testClosureCompletion11() throws Exception {
        ICompilationUnit groovyUnit = addGroovySource(CLOSURE_CONTENTS2, "File", "");
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS2, "first"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "first", 1);
    }

    public void testNamedArguments0() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_NAMED_ARGUMENTS, true);
        ICompilationUnit unit = addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "clone"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "clone()", 1);
    }

    public void _testNamedArguments1() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_NAMED_ARGUMENTS, true);
        ICompilationUnit unit = addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "new Foo"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "(first:first, second:second)", 1);
    }

    public void _testNamedArguments2() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_NAMED_ARGUMENTS, true);
        ICompilationUnit unit = addGroovySource(SCRIPTCONTENTS, "GroovyLikeCompletions", "");
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "new Foo"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "(third:third)", 1);
    }

    // GRECLIPSE-268
    public void _testGString1() throws Exception {
        ICompilationUnit unit = addGroovySource("\"\"\"\"\"\"", "File", "");
        ICompletionProposal[] proposals = performContentAssist(unit, "\"\"\"".length(), GroovyCompletionProposalComputer.class);
        assertEquals("Should not have found any proposals, but found:\n" + printProposals(proposals), 0, proposals.length);
    }

    // GRECLIPSE-268
    public void testGString2() throws Exception {
        ICompilationUnit unit = addGroovySource("\"\"\"${this}\"\"\"", "File", "");
        ICompletionProposal[] proposals = performContentAssist(unit, "\"\"\"".length(), GroovyCompletionProposalComputer.class);
        assertEquals("Should not have found any proposals, but found:\n" + printProposals(proposals), 0, proposals.length);
    }

    // GRECLIPSE-268
    public void testGString3() throws Exception {
        ICompilationUnit unit = addGroovySource("\"\"\"this\"\"\"", "File", "");
        ICompletionProposal[] proposals = performContentAssist(unit, "\"\"\"this".length(), GroovyCompletionProposalComputer.class);
        assertEquals("Should not have found any proposals, but found:\n" + printProposals(proposals), 0, proposals.length);
    }

    // GRECLIPSE-268
    public void testGString4() throws Exception {
        String contents = "def flarb;\n\"\"\"${flarb}\"\"\"";
        ICompilationUnit unit = addGroovySource(contents, "File", "");
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "${flarb"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "flarb", 1);
    }
}
