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

import org.codehaus.groovy.eclipse.codeassist.completion.jdt.GeneralGroovyCompletionProcessor;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Jul 23, 2009
 *
 */
public class InferencingCompletionTests extends CompletionTestCase {

    
    public InferencingCompletionTests() {
        super("Inferencing Completion Test Cases");
    }

    private static final String CONTENTS = "class TransformerTest {\nvoid testTransformer() {\ndef s = \"string\"\ns.st\n}}";
    public void testInferenceOfLocalStringInMethod() throws Exception {
        IPath projectPath = createGenericProject();
        IPath pack = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(pack, "TransformerTest", CONTENTS);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        unit.becomeWorkingCopy(null);
        ICompletionProposal[] proposals = performContentAssist(unit, CONTENTS.indexOf("s.st") + "s.ts".length(), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "startsWith", 2);
    }

    private static final String CONTENTS_SCRIPT = 
        "def s = \"string\"\n" +
        "s.st\n" +
        "s.substring(0).sub\n" +
        "class AClass {\n " +
        "  def g() {\n" +
        "    def t\n" +
        "    t = \"\"\n" +
        "    t.st\n" +
        "  }" +
        "}";
    public void testInferenceOfLocalString() throws Exception {
        IPath projectPath = createGenericProject();
        IPath pack = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(pack, "TransformerTest2", CONTENTS_SCRIPT);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        unit.becomeWorkingCopy(null);
        ICompletionProposal[] proposals = performContentAssist(unit, CONTENTS_SCRIPT.indexOf("s.st") + "s.ts".length(), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "startsWith", 2);
    }
    public void testInferenceOfLocalString2() throws Exception {
        IPath projectPath = createGenericProject();
        IPath pack = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(pack, "TransformerTest2", CONTENTS_SCRIPT);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        unit.becomeWorkingCopy(null);
        ICompletionProposal[] proposals = performContentAssist(unit, CONTENTS_SCRIPT.indexOf("0).sub") + "0).sub".length(), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "substring", 2);
    }
    
    public void testInferenceOfStringInClass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath pack = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(pack, "TransformerTest2", CONTENTS_SCRIPT);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        unit.becomeWorkingCopy(null);
        ICompletionProposal[] proposals = performContentAssist(unit, CONTENTS_SCRIPT.indexOf("t.st") + "t.st".length(), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "startsWith", 2);
    }
    
    public void testInferenceInClosure() throws Exception {
        String contents = "def file = new File(\"/tmp/some-file.txt\")\ndef writer = file.newWriter()\nnew URL(url).eachLine { line ->\nwriter.close()\n}";
        IPath projectPath = createGenericProject();
        IPath pack = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(pack, "ClosureTest", contents);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        unit.becomeWorkingCopy(null);
        ICompletionProposal[] proposals = performContentAssist(unit, contents.indexOf("writer.clos") + "writer.clos".length(), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "close", 1);
    }
}
