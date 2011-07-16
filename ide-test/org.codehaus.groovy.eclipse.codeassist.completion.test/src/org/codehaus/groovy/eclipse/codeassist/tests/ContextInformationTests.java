/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
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

import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Tests that context information is appropriately available.
 * 
 * And also that completion proposals that come from the {@link ContentAssistLocation#METHOD_CONTEXT}
 * location do not modify source
 * @author Andrew Eisenberg
 * @created Jul 15, 2011
 */
public class ContextInformationTests extends CompletionTestCase {

    public ContextInformationTests(String name) {
        super(name);
    }
    
    public void testMethodContext1() throws Exception {
        create("class Other {\n" +
                "  //def meth() { }\n" +  // methods with 0 args do not have context info
        		"  def meth(a) { }\n" +
        		"  def meth(int a, int b) { }\n" +
        		"  def method(int a, int b) { }\n" +
        		"}", "Other");
        
        String contents = "new Other().meth()";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "meth("), GroovyCompletionProposalComputer.class);
        assertContextInformation("meth", 2, proposals, contents);
    }

    public void testMethodContext2() throws Exception {
        create("class Other extends Super {\n" +
                "  //def meth() { }\n" +  // methods with 0 args do not have context info
                "  def meth(a) { }\n" +
                "  def meth(int a, int b) { }\n" +
                "}\n" +
                "class Super {\n" +
                "  def meth(String d) { }\n" +
                "  def method(String d) { }\n" +
                "}", "Other");
        
        String contents = "new Other().meth()";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "meth("), GroovyCompletionProposalComputer.class);
        assertContextInformation("meth", 3, proposals, contents);
    }
    
    public void testMethodContext3() throws Exception {
        create("class Other extends Super {\n" +
                "  //def meth() { }\n" +  // methods with 0 args do not have context info
                "  def meth(a) { }\n" +
                "  def meth(int a, int b) { }\n" +
                "}\n" +
                "class Super {\n" +
                "  def meth(String d) { }\n" +
                "  def method(String d) { }\n" +
                "}", "Other");
        
        String contents = "new Other().meth(a)";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "meth("), GroovyCompletionProposalComputer.class);
        assertContextInformation("meth", 3, proposals, contents);
    }
    
    public void testMethodContext4() throws Exception {
        create("class Other extends Super {\n" +
                "  //def meth() { }\n" +  // methods with 0 args do not have context info
                "  def meth(a) { }\n" +
                "  def meth(int a, int b) { }\n" +
                "}\n" +
                "class Super {\n" +
                "  def meth(String d) { }\n" +
                "  def method(String d) { }\n" +
                "}", "Other");
        
        String contents = "new Other().meth(a,b)";
        ICompilationUnit unit = create(contents);
        unit.becomeWorkingCopy(null);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "meth(a,"), GroovyCompletionProposalComputer.class);
        assertContextInformation("meth", 3, proposals, contents);
    }
    
    public void testConstructorContext1() throws Exception {
        ICompilationUnit unit = create("class Other {\n" +
                "  Other(a) { }\n" +
                "  Other(int a, int b) { }\n" +
                "}", "Other");
        
        // forces indexes to be ready
        performDummySearch(unit);
        String contents = "new Other()";
        unit = create(contents); 
        performDummySearch(unit);
        unit.becomeWorkingCopy(null);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "Other("), GroovyCompletionProposalComputer.class);
        assertContextInformation("Other", 2, proposals, contents);
    }

    public void testConstructorContext2() throws Exception {
        create("class Other {\n" +
                "  Other(a) { }\n" +
                "  Other(int a, int b) { }\n" +
                "}", "Other");
        
        String contents = "new Other(a)";
        ICompilationUnit unit = create(contents);
        performDummySearch(unit);
        performDummySearch(unit);
        performDummySearch(unit);
        performDummySearch(unit);
        performDummySearch(unit);
        performDummySearch(unit);
        performDummySearch(unit);
        unit.becomeWorkingCopy(null);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "Other("), GroovyCompletionProposalComputer.class);
        assertContextInformation("Other", 2, proposals, contents);
    }
    
    public void testConstructorContext3() throws Exception {
        create("class Other {\n" +
                "  Other(a) { }\n" +
                "  Other(int a, int b) { }\n" +
                "}", "Other");
        
        String contents = "new Other(a,b)";
        ICompilationUnit unit = create(contents);
        performDummySearch(unit);
        performDummySearch(unit);
        performDummySearch(unit);
        performDummySearch(unit);
        performDummySearch(unit);
        performDummySearch(unit);
        performDummySearch(unit);
        unit.becomeWorkingCopy(null);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "Other(a,"), GroovyCompletionProposalComputer.class);
        assertContextInformation("Other", 2, proposals, contents);
    }
    
    public void testConstructorContext4() throws Exception {
        create("class Other {\n" +
                "  Other(a) { }\n" +
                "  Other(int a, int b) { }\n" +
                "}\n" +
                "class Super {\n" +
                "  Super(String d) { }\n" +
                "  Super(String d, String e) { }\n" +
                "}", "Other");
        
        String contents = "new Super()";
        ICompilationUnit unit = create(contents);
        performDummySearch(unit);
        performDummySearch(unit);
        performDummySearch(unit);
        performDummySearch(unit);
        performDummySearch(unit);
        performDummySearch(unit);
        performDummySearch(unit);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "Super("), GroovyCompletionProposalComputer.class);
        assertContextInformation("Super", 2, proposals, contents);
    }
    
    private void assertContextInformation(String proposalName, int cnt,
            ICompletionProposal[] proposals, String contents) {
        if (cnt != proposals.length) {
            fail("Expected " + cnt + " proposals, but found " + proposals.length + "\nin:\n" + printProposals(proposals));
        }
        
        IDocument doc = new Document(contents);
        
        for (int i = 0; i < proposals.length; i++) {
            if (!proposals[i].getDisplayString().startsWith(proposalName)) {
                fail("Unexpected disoplay string for proposal " + cnt + ".  All proposals:\n" + printProposals(proposals));
            }
            if (proposals[i].getContextInformation() == null) {
                fail("No context information for proposal " + cnt + ".  All proposals:\n" + printProposals(proposals));
            }
            proposals[i].apply(doc);
            assertEquals("Invalid proposal application.  Should have no changes.", 
                    contents, doc.get());
        }
    }
    
//    public void performDummySearch(IJavaElement element) throws Exception{
//        new SearchEngine().searchAllTypeNames(
//            null,
//            SearchPattern.R_EXACT_MATCH,
//            "XXXXXXXXX".toCharArray(), // make sure we search a concrete name. This is faster according to Kent
//            SearchPattern.R_EXACT_MATCH,
//            IJavaSearchConstants.CLASS,
//            SearchEngine.createJavaSearchScope(new IJavaElement[]{element}),
//            new Requestor(),
//            IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
//            null);
//    }
//
//    private static class Requestor extends TypeNameRequestor {
//    }
//

//    protected void performDummySearch() throws Exception {
//        performDummySearch(getPackageP());
//    }
//
//    protected IPackageFragment getPackageP() {
//        IProject project = env.getProject("Project");
//        env.addPackage(project.getFullPath().append("src"), "p");
//        return (IPackageFragment) JavaCore.create(project.getFolder("src/p"));
//    }
}
