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
 * Tests specific bug reports
 */
public class OtherCompletionTests extends CompletionTestCase {

    public OtherCompletionTests(String name) {
        super(name);
    }
    
    public void testGreclipse414() throws Exception {
        System.out.println("Disabled test case " + this.getName());
//        String contents = "public class Test {\n    int i\n    Test() {\nthis.i = 42\n}\nTest(Test other) {\n\nthis.i = other.i}\n};";
//        ICompilationUnit unit = create(contents);
//        // ensure that there is no ArrayIndexOutOfBoundsException thrown.
//        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "this."), GeneralGroovyCompletionProcessor.class);
//        proposalExists(proposals, "i", 1);
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
