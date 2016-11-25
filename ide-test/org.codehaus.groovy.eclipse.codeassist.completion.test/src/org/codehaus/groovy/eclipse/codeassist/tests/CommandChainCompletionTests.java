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

import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Sep 25, 2012
 *
 * Tests for command chain style method invocation
 */
public final class CommandChainCompletionTests extends CompletionTestCase {

    public static Test suite() {
        return newTestSuite(CommandChainCompletionTests.class);
    }

    private static final String INITIAL_CONTENTS =
            "class Inner {\n" +
            "  Inner first(arg) { }\n" +
            "  Inner second(arg) { }\n" +
            "  Inner third(arg) { }\n" +
            "  Inner aField" +
            "}\n" +
            "def start = new Inner()\n";

    public void testCommandChain1() throws Exception {
        String contents = INITIAL_CONTENTS + "start.first 'foo' sec";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 1);
    }

    public void testCommandChain2() throws Exception {
        String contents = INITIAL_CONTENTS + "start.first 'foo' third 'foo' sec";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 1);
    }

    public void testCommandChain3() throws Exception {
        String contents = INITIAL_CONTENTS + "start.first 'foo' third 'foo' aFi";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "aFi"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "aField", 1);
    }

    public void testCommandChain4() throws Exception {
        String contents = INITIAL_CONTENTS + "start.first 'foo' third 'foo','bar' sec";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 1);
    }

    public void testCommandChain5() throws Exception {
        String contents = INITIAL_CONTENTS + "start.first 'foo' sec 'foo' third";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 1);
    }

    public void testCommandChain6() throws Exception {
        String contents = INITIAL_CONTENTS + "start.first 'foo' third foo.bar(nuthin) sec";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 1);
    }

    public void testNoCommandChain1() throws Exception {
        String contents = INITIAL_CONTENTS + "first 'foo' third 'foo' sec";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 0);
    }

    public void testNoCommandChain2() throws Exception {
        String contents = INITIAL_CONTENTS + "first 'foo' third foo.bar(nuthin)\nsec";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 0);
    }

    public void testNoCommandChain3() throws Exception {
        String contents = INITIAL_CONTENTS + "first 'foo' third foo.bar(nuthin).sec";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 0);
    }

    public void testNoCommandChain4() throws Exception {
        String contents = INITIAL_CONTENTS + "first 'foo' third sec";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 0);
    }
}
