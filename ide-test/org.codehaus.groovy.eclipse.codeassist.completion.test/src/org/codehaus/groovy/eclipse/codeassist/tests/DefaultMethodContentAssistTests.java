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

import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Tests that default methods are properly handled in content assist
 *
 * @author Andrew Eisenberg
 * @created Nov 13, 2011
 */
public final class DefaultMethodContentAssistTests extends CompletionTestCase {

    public static Test suite() {
        return newTestSuite(DefaultMethodContentAssistTests.class);
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
