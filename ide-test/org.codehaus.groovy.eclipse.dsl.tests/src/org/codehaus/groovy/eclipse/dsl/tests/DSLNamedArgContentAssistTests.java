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
package org.codehaus.groovy.eclipse.dsl.tests;

import groovy.lang.Closure;

import org.codehaus.groovy.eclipse.codeassist.tests.CompletionTestCase;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.test.EclipseTestSetup;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Before;

public final class DSLNamedArgContentAssistTests extends CompletionTestCase {

    @Before @SuppressWarnings({"serial", "unused"})
    public void setUp() throws Exception {
        EclipseTestSetup.addClasspathContainer(GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID);
        EclipseTestSetup.withProject(new Closure<IProject>(null) {
            public Void doCall(IProject project) {
                GroovyDSLCoreActivator.getDefault().getContextStoreManager().initialize(project, true);
                //GroovyDSLCoreActivator.getDefault().getContainerListener().ignoreProject(project);
                return null;
            }
        });
    }

    private void createDSL(String dsldContents) throws Exception {
        EclipseTestSetup.addPlainText(dsldContents, "MyDsld.dsld");
    }

    //

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


    // tests application of closures with and without named parameters
    private static final String closuredsld =
            "contribute (currentType('Clos')) {\n" +
            "    method name: 'test1', params : [op:Closure]\n" +
            "    method name: 'test2', params : [first: String, op:Closure]\n" +
            "    method name: 'test3', namedParams : [op:Closure]\n" +
            "    method name: 'test4', namedParams : [first: String, op:Closure]\n" +
            "    method name: 'test5', params : [first: String], namedParams: [op:Closure]\n" +
            "    method name: 'test6', namedParams : [first: String], params: [op:Closure]\n" +
            "    method name: 'test7', namedParams : [first: String, other:String], params: [op:Closure]\n" +
            "    method name: 'test8', namedParams : [first: String], params: [other:String, op:Closure]\n" +
            "    method name: 'test9', namedParams : [first: String], params: [other:String, op:Closure, other2:String]\n" +
            "    method name: 'test0', namedParams : [first: String], params: [other:String, op:Closure, other2:String, op2:Closure]\n" +
            "}";
    private static final String closureContents = "class Clos { }\nnew Clos().test";
    public void testClostureOp1() throws Exception {
        createDSL(closuredsld);
        checkProposalApplicationNonType(closureContents, closureContents +"1 {", closureContents.length(), "test1");
    }
    public void testClostureOp2() throws Exception {
        createDSL(closuredsld);
        checkProposalApplicationNonType(closureContents, closureContents + "2(\"\") {", closureContents.length(), "test2");
    }
    public void testClostureOp3() throws Exception {
        createDSL(closuredsld);
        checkProposalApplicationNonType(closureContents, closureContents + "3(op:{  })", closureContents.length(), "test3");
    }
    public void testClostureOp4() throws Exception {
        createDSL(closuredsld);
        checkProposalApplicationNonType(closureContents, closureContents + "4(first:\"\", op:{  })", closureContents.length(), "test4");
    }
    public void testClostureOp5() throws Exception {
        createDSL(closuredsld);
        checkProposalApplicationNonType(closureContents, closureContents + "5(\"\", op:{  })", closureContents.length(), "test5");
    }
    public void testClostureOp6() throws Exception {
        createDSL(closuredsld);
        checkProposalApplicationNonType(closureContents, closureContents + "6(first:\"\") {", closureContents.length(), "test6");
    }
    public void testClostureOp7() throws Exception {
        createDSL(closuredsld);
        checkProposalApplicationNonType(closureContents, closureContents + "7(first:\"\", other:\"\") {", closureContents.length(), "test7");
    }
    public void testClostureOp8() throws Exception {
        createDSL(closuredsld);
        checkProposalApplicationNonType(closureContents, closureContents + "8(\"\", first:\"\") {", closureContents.length(), "test8");
    }
    public void testClostureOp9() throws Exception {
        createDSL(closuredsld);
        checkProposalApplicationNonType(closureContents, closureContents + "9(\"\", {  }, \"\", first:\"\")", closureContents.length(), "test9");
    }
    public void testClostureOp0() throws Exception {
        createDSL(closuredsld);
        checkProposalApplicationNonType(closureContents, closureContents + "0(\"\", {  }, \"\", first:\"\") {", closureContents.length(), "test0");
    }
}
