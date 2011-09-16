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
package org.codehaus.groovy.eclipse.dsl.tests;

import org.codehaus.groovy.eclipse.codeassist.proposals.NamedParameterProposal;
import org.codehaus.groovy.eclipse.codeassist.tests.CompletionTestCase;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.RefreshDSLDJob;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * 
 * @author Andrew Eisenberg
 * @created Jul 27, 2011
 */
public class DSLNamedArgContentAssistTests extends CompletionTestCase {

    public DSLNamedArgContentAssistTests(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createGenericProject();
        IProject project = env.getProject("Project");
        GroovyRuntime.addLibraryToClasspath(JavaCore.create(project), GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID);
        env.fullBuild();
        new RefreshDSLDJob(project).run(null);
        GroovyDSLCoreActivator.getDefault().getContainerListener().ignoreProject(project);
    }
    
    
    public void testNamedArgs1() throws Exception {
        createDSL("currentType().accept {\n" +
        		"method name:\"flar\", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true\n" +
        		"}");
        String contents = "flar()";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "("));
        proposalExists(proposals, "aaa : __", 1);
        proposalExists(proposals, "bbb : __", 1);
        proposalExists(proposals, "ccc : __", 1);
        proposalExists(proposals, "flar", 1);
    }

    public void testNoNamedArgs1() throws Exception {
        createDSL("currentType().accept {\n" +
                "method name:\"flar\", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:false\n" +
                "}");
        String contents = "flar()";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "("));
        proposalExists(proposals, "aaa : __", 0);
        proposalExists(proposals, "bbb : __", 0);
        proposalExists(proposals, "ccc : __", 0);
        proposalExists(proposals, "flar", 1);
    }
    
    public void testNamedArgs2() throws Exception {
        createDSL("currentType().accept {\n" +
                "method name:\"flar\", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true\n" +
                "}");
        String contents = "flar(aaa:__, )";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "("));
        proposalExists(proposals, "aaa : __", 0);
        proposalExists(proposals, "bbb : __", 1);
        proposalExists(proposals, "ccc : __", 1);
        proposalExists(proposals, "flar", 1);
    }

    public void testNamedArgs3() throws Exception {
        createDSL("currentType().accept {\n" +
                "method name:\"flar\", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true\n" +
                "}");
        String contents = "flar(aaa:__, )";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ","));
        proposalExists(proposals, "aaa : __", 0);
        proposalExists(proposals, "bbb : __", 1);
        proposalExists(proposals, "ccc : __", 1);
        proposalExists(proposals, "flar", 1);
    }
    
    public void testNamedArgs4() throws Exception {
        createDSL("currentType().accept {\n" +
                "method name:\"flar\", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true\n" +
                "}");
        String contents = "flar(aaa:__, bbb:__, )";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ","));
        proposalExists(proposals, "aaa : __", 0);
        proposalExists(proposals, "bbb : __", 0);
        proposalExists(proposals, "ccc : __", 1);
        proposalExists(proposals, "flar", 1);
    }
    
    public void testNamedArgs5() throws Exception {
        createDSL("currentType().accept {\n" +
                "method name:\"flar\", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true\n" +
                "}");
        String contents = "flar(aaa:__, bbb:__, ccc:__)";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ","));
        proposalExists(proposals, "aaa : __", 0);
        proposalExists(proposals, "bbb : __", 0);
        proposalExists(proposals, "ccc : __", 0);
        proposalExists(proposals, "flar", 1);
    }
    
    public void testNoNamedArgs6() throws Exception {
        createDSL("currentType().accept {\n" +
                "method name:\"flar\", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:false\n" +
                "}");
        String contents = "flar ";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, " "));
        proposalExists(proposals, "aaa : __", 0);
        proposalExists(proposals, "bbb : __", 0);
        proposalExists(proposals, "ccc : __", 0);
        proposalExists(proposals, "flar", 1);
    }
    
    
    public void testOptionalArgs1() throws Exception {
        createDSL("currentType().accept {\n" +
                "method name:\"flar\", optionalParams:[aaa:Integer, bbb:Boolean, ccc:String]\n" +
                "}");
        String contents = "flar( )";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "( "));
        proposalExists(proposals, "aaa : __", 1);
        proposalExists(proposals, "bbb : __", 1);
        proposalExists(proposals, "ccc : __", 1);
        proposalExists(proposals, "flar", 1);
    }
    
    public void testOptionalArgs2() throws Exception {
        createDSL("currentType().accept {\n" +
                "method name:\"flar\", namedParams:[aaa:Integer, bbb:Boolean, ccc:String]\n" +
                "}");
        String contents = "flar( )";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "( "));
        proposalExists(proposals, "aaa : __", 1);
        proposalExists(proposals, "bbb : __", 1);
        proposalExists(proposals, "ccc : __", 1);
        proposalExists(proposals, "flar", 1);
    }
    
    public void testOptionalArgs3() throws Exception {
        createDSL("currentType().accept {\n" +
                "method name:\"flar\", namedParams:[aaa:Integer], optionalParams: [bbb:Boolean, ccc:String]\n" +
                "}");
        String contents = "flar( )";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "( "));
        proposalExists(proposals, "aaa : __", 1);
        proposalExists(proposals, "bbb : __", 1);
        proposalExists(proposals, "ccc : __", 1);
        proposalExists(proposals, "flar", 1);
    }
    
    public void testNamedArgs7() throws Exception {
        createDSL("currentType().accept {\n" +
                "method name:\"flar\", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true\n" +
                "}");
        String contents = "flar aaa:__, \n" +
        		"need_this_here_so_parser_wont_break";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "r "));
        proposalExists(proposals, "aaa : __", 0);
        proposalExists(proposals, "bbb : __", 1);
        proposalExists(proposals, "ccc : __", 1);
        proposalExists(proposals, "flar", 1);
    }

    public void testNamedArgs8() throws Exception {
        createDSL("currentType().accept {\n" +
                "method name:\"flar\", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true\n" +
                "}");
        String contents = "flar aaa:__, \n" +
                "need_this_here_so_parser_wont_break";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ","));
        proposalExists(proposals, "aaa : __", 0);
        proposalExists(proposals, "bbb : __", 1);
        proposalExists(proposals, "ccc : __", 1);
        proposalExists(proposals, "flar", 1);
    }
    

    public void testParamGuessing1() throws Exception {
        createDSL("currentType().accept {\n" +
                "method name:\"flar\", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true\n" +
                "}");
        String contents = 
                "String xxx\n" +
                        "int yyy\n" +
                        "boolean zzz\n" +
                        "flar()";
        String[] expectedChoices = new String[] { "yyy", "0" };
        checkProposalChoices(contents, "flar(", "aaa", "aaa: __, ", expectedChoices);
    }

    public void testParamGuessing3() throws Exception {
        createDSL("currentType().accept {\n" +
                "method name:\"flar\", params:[aaa:Integer, bbb:Boolean, ccc:String], useNamedArgs:true\n" +
                "}");
        String contents = 
                "String xxx\n" +
                        "boolean zzz\n" +
                        "def uuu(int iii)\n {" +
                        "  int yyy\n" +
                        "  flar()\n" +
                        "}";
        String[] expectedChoices = new String[] { "iii", "yyy", "0" };
        checkProposalChoices(contents, "flar(", "aaa", "aaa: __, ", expectedChoices);
    }
    
    private void createDSL(String dsldContents) throws Exception {
        defaultFileExtension = "dsld";
        create(dsldContents, "MyDsld");
        defaultFileExtension = "groovy";
        env.fullBuild();
        expectingNoProblems();
    }
    
    private void checkProposalChoices(String contents, String toFind, String lookFor, String replacementString,
            String[] expectedChoices) throws Exception {
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, toFind));
        checkReplacementString(proposals, replacementString, 1);
        ICompletionProposal proposal = findFirstProposal(proposals, lookFor, false);
        NamedParameterProposal guessingProposal = (NamedParameterProposal) proposal;
        ICompletionProposal[] choices = guessingProposal.getChoices();
        assertEquals(expectedChoices.length, choices.length);
        for (int i = 0; i < expectedChoices.length; i++) {
            assertEquals("unexpected choice", expectedChoices[i], choices[i].getDisplayString());
        }
    }


}
