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

import org.eclipse.jface.text.contentassist.ICompletionProposal;



/**
 * @author Andrew Eisenberg
 * @created Nov 13, 2011
 *
 * Tests that default methods are properly handled in content assist
 */
public class DefaultMethodContentAssistTests extends CompletionTestCase {

    public DefaultMethodContentAssistTests(String name) {
        super(name);
    }

    public void testDefaultMethods1() throws Exception {
        create(
                "Default", "class Default {\n" +
        		"  def meth(int a, b = 9, c = 10) { }\n" +
        		"}");
    	String contents = "new Default().me";
    	ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.lastIndexOf('e'));
    	proposalExists(proposals, "meth", 3);
    }
    public void testDefaultMethods2() throws Exception {
        create(
                "Default", "class Default {\n" +
                        "  def meth(int a, b = 9, c = 10) { }\n" +
        		"  def meth(String other) { }\n" +
        		"}");
        String contents = "new Default().me";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.lastIndexOf('e'));
        proposalExists(proposals, "meth", 4);
    }
}