/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

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
        performDummySearch(unit);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "clone"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "clone()", 1);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_NAMED_ARGUMENTS, false);
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
