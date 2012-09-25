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
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Sep 25, 2012
 *
 * Tests for command chain style method invocation
 */
public class CommandChainCompletionTests extends CompletionTestCase {

    public CommandChainCompletionTests(String name) {
        super(name);
    }
    
    private final String INITIAL_CONTENTS = 
            "class Inner {\n" +
            "  Inner first(arg) { }\n" +
            "  Inner second(arg) { }\n" +
            "  Inner third(arg) { }\n" +
            "  Inner aField" +
            "}\n" +
            "def start = new Inner()\n";

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        env.setAutoBuilding(true);
    }

    public void testCommandChain1() throws Exception {
        // commands only available 18 and later
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = INITIAL_CONTENTS + "start.first 'foo' sec";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 1);
    }
    public void testCommandChain2() throws Exception {
        // commands only available 18 and later
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = INITIAL_CONTENTS + "start.first 'foo' third 'foo' sec";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 1);
    }
    public void testCommandChain3() throws Exception {
        // commands only available 18 and later
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = INITIAL_CONTENTS + "start.first 'foo' third 'foo' aFi";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "aFi"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "aField", 1);
    }
    public void testCommandChain4() throws Exception {
        // commands only available 18 and later
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = INITIAL_CONTENTS + "start.first 'foo' third 'foo','bar' sec";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 1);
    }
    public void testCommandChain5() throws Exception {
        // commands only available 18 and later
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = INITIAL_CONTENTS + "start.first 'foo' sec 'foo' third";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 1);
    }
    public void testCommandChain6() throws Exception {
        // commands only available 18 and later
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = INITIAL_CONTENTS + "start.first 'foo' third foo.bar(nuthin) sec";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 1);
    }
    
    public void testNoCommandChain1() throws Exception {
        // commands only available 18 and later
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = INITIAL_CONTENTS + "first 'foo' third 'foo' sec";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 0);
    }
    
    public void testNoCommandChain2() throws Exception {
        // commands only available 18 and later
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = INITIAL_CONTENTS + "first 'foo' third foo.bar(nuthin)\nsec";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 0);
    }
    public void testNoCommandChain3() throws Exception {
        // commands only available 18 and later
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = INITIAL_CONTENTS + "first 'foo' third foo.bar(nuthin).sec";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 0);
    }
    public void testNoCommandChain4() throws Exception {
        // commands only available 18 and later
        if (GroovyUtils.GROOVY_LEVEL < 18) {
            return;
        }
        String contents = INITIAL_CONTENTS + "first 'foo' third sec";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "sec"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "second", 0);
    }
}
