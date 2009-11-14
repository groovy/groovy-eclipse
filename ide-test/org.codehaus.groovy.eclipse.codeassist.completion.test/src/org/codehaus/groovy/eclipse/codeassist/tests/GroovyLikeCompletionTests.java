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
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Jun 5, 2009
 * 
 * Tests that completion proposals are sufficiently groovy-like in their output
 */
public class GroovyLikeCompletionTests extends CompletionTestCase {

    private static final String SCRIPTCONTENTS = "any\nclone\nfindIndexOf\n";

    public GroovyLikeCompletionTests(String name) {
        super(name);
    }
    
    
    public void testMethodWithClosure() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "any"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "any { }", 1);
    }

    public void testMethodWithNoArgs() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "clone"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "clone()", 1);
    }
    
    public void testMethodWith2Args() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "findIndexOf"), GroovyCompletionProposalComputer.class);
        checkReplacementString(proposals, "findIndexOf param1, { }", 1);
    }
    
    private ICompilationUnit createGroovy() throws Exception {
        IPath projectPath = createGenericProject();
        IPath src = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(src, "GroovyLikeCompletions", SCRIPTCONTENTS);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        return unit;
    }
}
