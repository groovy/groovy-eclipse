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

import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Sept 29, 2009
 * 
 * Tests specific bug reports
 */
public class OtherCompletionTests extends CompletionTestCase {

    public OtherCompletionTests(String name) {
        super(name);
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
        ICompilationUnit unit = create(contents);
        fullBuild();
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
            
        ICompilationUnit groovyUnit = create(groovyClass);
        env.addClass(groovyUnit.getParent().getResource().getFullPath(), "StringExtension", javaClass);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "foo.ba"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "bar", 1);
        assertEquals (proposals[0].getDisplayString(), "bar() : String - StringExtension (Groovy)");
            
        proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "this.collect"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "collect", 2);
        assertTrue (printProposals(proposals), ((proposals[0].getDisplayString().equals("collect(Closure closure) : List - DefaultGroovyMethods (Groovy)")) ||
                     (proposals[1].getDisplayString().equals("collect(Closure closure) : List - DefaultGroovyMethods (Groovy)"))));
        assertTrue (printProposals(proposals), ((proposals[0].getDisplayString().equals("collect(Collection arg1, Closure arg2) : Collection - DefaultGroovyMethods (Groovy)")) ||
                     (proposals[1].getDisplayString().equals("collect(Collection arg1, Closure arg2) : Collection - DefaultGroovyMethods (Groovy)"))));
    }
    
    public void testVisibility() throws Exception {
        String groovyClass = 
"class B { }\n" +
"class C {\n" +
    "B theB\n" +
"}\n" +
"new C().th\n";
        ICompilationUnit groovyUnit = create(groovyClass);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "().th"), GroovyCompletionProposalComputer.class);
        
        proposalExists(proposals, "theB", 1);
        assertEquals("theB : B - C (Groovy)", proposals[0].getDisplayString());
            
    }
    
    public void testGString1() throws Exception {
        String groovyClass = 
            "\"${new String().c}\"";
        ICompilationUnit groovyUnit = create(groovyClass);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, ".c"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "center", 2);
    }
    
    // GRECLIPSE-706
    public void testContentAssistInInitializers1() throws Exception {
        String groovyClass = 
            "class A { { aa }\n def aaaa }";
        ICompilationUnit groovyUnit = create(groovyClass);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "aa"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "aaaa", 1);
    }
    // GRECLIPSE-706
    public void testContentAssistInInitializers2() throws Exception {
        String groovyClass = 
            "class A { {  }\n def aaaa }";
        ICompilationUnit groovyUnit = create(groovyClass);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "{ { "), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "aaaa", 1);
    }
    // GRECLIPSE-706
    public void testContentAssistInStaticInitializers1() throws Exception {
        String groovyClass = 
            "class A { static { aa }\n static aaaa }";
        ICompilationUnit groovyUnit = create(groovyClass);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "aa"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "aaaa", 1);
    }
    // GRECLIPSE-706
    public void testContentAssistInStaticInitializers2() throws Exception {
        String groovyClass = 
            "class A { static {  }\n static aaaa }";
        ICompilationUnit groovyUnit = create(groovyClass);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "static { "), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "aaaa", 1);
    }
    
    // GRECLIPSE-692
    public void testMethodWithSpaces() throws Exception {
        String groovyClass = 
            "class A { def \"ff f\"()  { ff } }";
        ICompilationUnit groovyUnit = create(groovyClass);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "{ ff"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "\"ff f\"()", 1);
    }
    // GRECLIPSE-692
    public void testMethodWithSpaces2() throws Exception {
        String groovyClass = 
            "class A { def \"fff\"()  { fff } }";
        ICompilationUnit groovyUnit = create(groovyClass);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "{ fff"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "fff()", 1);
    }
    
    // STS-1165 content assist after a static method call was broken
    public void testAfterStaticCall() throws Exception {
        String groovyClass = 
            "class A { static xxx(x) { }\n def something() {\nxxx oth }\ndef other}";
        ICompilationUnit groovyUnit = create(groovyClass);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "oth"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "other", 1);
    }
    

    
    // not working in multiline strings yet
//    public void testGString2() throws Exception {
//        String groovyClass = 
//            "\"\"\"${new String().c}\"\"\"";
//        ICompilationUnit groovyUnit = create(groovyClass);
//        fullBuild();
//        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, ".c"), GroovyCompletionProposalComputer.class);
//        proposalExists(proposals, "center", 2);
//    }
    
}
