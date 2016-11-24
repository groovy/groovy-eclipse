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

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.internal.resources.PreferenceInitializer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Jun 5, 2009
 *
 * Tests that completion proposals are sufficiently groovy-like in their output
 */
public class GroovyLikeCompletionTests extends CompletionTestCase {

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

    public GroovyLikeCompletionTests(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // ensure that the correct properties are set
        new PreferenceInitializer().initializeDefaultPreferences();
        new org.codehaus.groovy.eclipse.preferences.PreferenceInitializer().initializeDefaultPreferences();
        // ensure that the correct properties are set
        new org.codehaus.groovy.eclipse.preferences.PreferenceInitializer().reset();
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_PARAMETER_GUESSING, false);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // ensure that the correct properties are set
        new org.codehaus.groovy.eclipse.preferences.PreferenceInitializer().reset();
    }

    public void testMethodWithClosure() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, true);
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "any"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "any {  }", 1);
    }

    public void testMethodWithNoArgs() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "clone"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "clone()", 1);
    }

    public void testMethodWith2Args() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, true);
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "findIndexOf"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "findIndexOf(arg1) {  }", 1);
    }

    public void testMethodWithClosureNotGroovyLike() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, false);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "any"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "any(arg1)", 1);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, true);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, true);
    }

    public void testMethodWith2ArgsNotGroovyLike() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, false);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "findIndexOf"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "findIndexOf(arg1, arg2)", 1);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, true);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, true);
    }

    public void testClosureApplication1a() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, true);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, true);
        createGroovy();
        String contents = "new Foo().method1";
        String expected = "new Foo().method1(arg)";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method1");
    }

    public void testClosureApplication1b() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, true);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        createGroovy();
        String contents = "new Foo().method1";
        String expected = "new Foo().method1(arg)";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method1");
    }

    public void testClosureApplication1c() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, false);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, true);
        createGroovy();
        String contents = "new Foo().method1";
        String expected = "new Foo().method1(arg)";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method1");
    }

    public void testClosureApplication1d() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, false);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        createGroovy();
        String contents = "new Foo().method1";
        String expected = "new Foo().method1(arg)";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method1");
    }

    public void testClosureApplication2a() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, true);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, true);
        createGroovy();
        String contents = "new Foo().method2";
        String expected = "new Foo().method2(arg) {  }";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method2");
    }

    public void testClosureApplication2b() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, true);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        createGroovy();
        String contents = "new Foo().method2";
        String expected = "new Foo().method2(arg, {  })";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method2");
    }

    public void testClosureApplication2c() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, false);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, true);
        createGroovy();
        String contents = "new Foo().method2";
        String expected = "new Foo().method2(arg) c1";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method2");
    }

    public void testClosureApplication2d() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, false);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        createGroovy();
        String contents = "new Foo().method2";
        String expected = "new Foo().method2(arg, c1)";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method2");
    }

    public void testClosureApplication3a() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, true);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, true);
        createGroovy();
        String contents = "new Foo().method3";
        String expected = "new Foo().method3(arg, {  }) {  }";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method3");
    }

    public void testClosureApplication3b() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, true);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        createGroovy();
        String contents = "new Foo().method3";
        String expected = "new Foo().method3(arg, {  }, {  })";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method3");
    }

    public void testClosureApplication3c() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, false);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, true);
        createGroovy();
        String contents = "new Foo().method3";
        String expected = "new Foo().method3(arg, c1) c2";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method3");
    }

    public void testClosureApplication3d() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, false);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
        createGroovy();
        String contents = "new Foo().method3";
        String expected = "new Foo().method3(arg, c1, c2)";
        checkProposalApplicationNonType(contents, expected, contents.length(), "method3");
    }

    /**
     * can't get it to pass on build server
     * @throws Exception
     */
    public void _testNamedArguments1() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_NAMED_ARGUMENTS, true);
        ICompilationUnit unit = createGroovy();
        doWait();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "new Foo"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "(first:first, second:second)", 1);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_NAMED_ARGUMENTS, false);
    }

    /**
     * can't get it to pass on build server
     * @throws Exception
     */
    public void _testNamedArguments2() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_NAMED_ARGUMENTS, true);
        ICompilationUnit unit = createGroovy();
        doWait();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "new Foo"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "(third:third)", 1);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_NAMED_ARGUMENTS, false);
    }

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
    // accessing members of super types in closures
    public void testClosureCompletion1() throws Exception {
        ICompilationUnit groovyUnit = create(CLOSURE_CONTENTS);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, " substring"), GroovyCompletionProposalComputer.class);
        checkReplacementRegexp(proposals, "substring\\(\\p{Alnum}*\\)", 1);
        //checkReplacementString(proposals, "substring(arg0)", 1);
    }
    // accessing members of super types in closures
    public void testClosureCompletion2() throws Exception {
        ICompilationUnit groovyUnit = create(CLOSURE_CONTENTS);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, " first"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "first", 1);
    }
    // accessing members of super types in closures
    public void testClosureCompletion3() throws Exception {
        ICompilationUnit groovyUnit = create(CLOSURE_CONTENTS);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, " second2"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "second2()", 1);
    }
    // accessing members of super types in closures
    public void testClosureCompletion4() throws Exception {
        ICompilationUnit groovyUnit = create(CLOSURE_CONTENTS);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, "delegate.substring"), GroovyCompletionProposalComputer.class);
        checkReplacementRegexp(proposals, "substring\\(\\p{Alnum}*\\)", 1);
        //checkReplacementString(proposals, "substring(arg0)", 1);
    }
    // accessing members of super types in closures
    public void testClosureCompletion5() throws Exception {
        ICompilationUnit groovyUnit = create(CLOSURE_CONTENTS);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, "delegate.first"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "first", 0);
    }
    // accessing members of super types in closures
    public void testClosureCompletion6() throws Exception {
        ICompilationUnit groovyUnit = create(CLOSURE_CONTENTS);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, "delegate.second2"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "second2", 0);
    }
    // accessing members of super types in closures
    public void testClosureCompletion7() throws Exception {
        ICompilationUnit groovyUnit = create(CLOSURE_CONTENTS);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, "this.substring"), GroovyCompletionProposalComputer.class);
        checkReplacementRegexp(proposals, "substring\\(\\p{Alnum}*\\)", 0);
        //checkReplacementString(proposals, "substring", 0);
    }
    // accessing members of super types in closures
    public void testClosureCompletion8() throws Exception {
        ICompilationUnit groovyUnit = create(CLOSURE_CONTENTS);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, "this.first"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "first", 1);
    }
    // accessing members of super types in closures
    public void testClosureCompletion9() throws Exception {
        ICompilationUnit groovyUnit = create(CLOSURE_CONTENTS);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, "this.second2"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "second2()", 1);
    }
    // accessing members of super types in closures
    public void testClosureCompletion10() throws Exception {
        ICompilationUnit groovyUnit = create(CLOSURE_CONTENTS);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS, "wait"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "wait()", 1);
    }
    // accessing members of super types in closures
    public void testClosureCompletion11() throws Exception {
        ICompilationUnit groovyUnit = create(CLOSURE_CONTENTS2);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getLastIndexOf(CLOSURE_CONTENTS2, "first"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "first", 1);
    }


    protected void doWait() {
        // it seems like
        int x = 0;
        while (! Job.getJobManager().isIdle() && x < 20) {
            synchronized (this) {
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                }
                x++;
            }
        }
    }

    public void testNamedArguments3() throws Exception {
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_NAMED_ARGUMENTS, true);
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "clone"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "clone()", 1);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_NAMED_ARGUMENTS, false);
    }

    // GRECLIPSE-268
    // disabled because groovy will parse the following as an empty constant expression
    public void _testGString1() throws Exception {
        ICompilationUnit unit = create("\"\"\"\"\"\"");
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(unit, "\"\"\"".length(), GroovyCompletionProposalComputer.class);
        assertEquals("Should not have found any proposals, but found:\n" + printProposals(proposals), 0, proposals.length);
    }

    // GRECLIPSE-268
    public void testGString2() throws Exception {
        ICompilationUnit unit = create("\"\"\"${this}\"\"\"");
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(unit, "\"\"\"".length(), GroovyCompletionProposalComputer.class);
        assertEquals("Should not have found any proposals, but found:\n" + printProposals(proposals), 0, proposals.length);
    }

    // GRECLIPSE-268
    public void testGString3() throws Exception {
        ICompilationUnit unit = create("\"\"\"this\"\"\"");
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(unit, "\"\"\"this".length(), GroovyCompletionProposalComputer.class);
        assertEquals("Should not have found any proposals, but found:\n" + printProposals(proposals), 0, proposals.length);
    }

    // GRECLIPSE-268
    public void testGString4() throws Exception {
        String contents = "def flarb;\n\"\"\"${flarb}\"\"\"";
        ICompilationUnit unit = create(contents);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "${flarb"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "flarb", 1);
    }

    private ICompilationUnit createGroovy() throws Exception {
        return createGroovy("GroovyLikeCompletions", SCRIPTCONTENTS);
    }

    private ICompilationUnit createGroovy(String cuName, String cuContents) throws Exception {
        IPath projectPath = createGenericProject();
        IPath src = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(src, cuName, cuContents);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        return unit;
    }
}
