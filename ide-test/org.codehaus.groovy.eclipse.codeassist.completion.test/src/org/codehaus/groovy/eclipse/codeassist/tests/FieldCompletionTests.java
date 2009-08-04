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
 * @created Jun 5, 2009
 * 
 * Tests that Field completions are working properly
 */
public class FieldCompletionTests extends CompletionTestCase {

    public FieldCompletionTests(String name) {
        super(name);
    }
    
    // test that safe dereferencing works
    // should find that someProperty is of type integer
    public void testSafeDeferencing() throws Exception {
        String contents = "public class SomeClass {\nint someProperty\nvoid someMethod() { someProperty?.}}";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "?."), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "getInteger", 3);
    }

    private ICompilationUnit create(String contents) throws Exception {
        IPath projectPath = createGenericProject();
        IPath src = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(src, "GroovyClass", contents);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        return unit;
    }
}
