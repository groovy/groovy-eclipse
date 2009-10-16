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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Sept 29, 2009
 * 
 * Tests specific bug reports
 */
public class OtherCompletionTests extends CompletionTestCase {

    OtherCompletionTests(String name) {
        super(name);
    }
    
    void testGreclipse414() throws Exception {
        System.err.println("This test is disabled");
//        String contents = """
//public class Test {
//    int i
//    Test() {
//        this.i = 42
//    }
//    Test(Test other) {
//        this.i = other.i
//    }
//}""";
//        ICompilationUnit unit = create(contents);
//        // ensure that there is no ArrayIndexOutOfBoundsException thrown.
//        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "this."), GeneralGroovyCompletionProcessor.class);
//        proposalExists(proposals, "i", 1);
    }
    
    // type signatures were popping up in various places in the display string
    // ensure this doesn't happen
    void testGreclipse422() throws Exception {
        String javaClass = """
         public class StringExtension {
         public static String bar(String self) {
                     return self;
                 }
             }
             """     
            
        String groovyClass = """
             public class MyClass {
                 public void foo() {
                     String foo = 'foo';
                     use (StringExtension) {
                         foo.bar()
                     }
                     this.collect
                 }
             }
             """
            
        ICompilationUnit groovyUnit = create(groovyClass)
        env.addClass groovyUnit.getParent().getResource().getFullPath(), "StringExtension", javaClass
        incrementalBuild()
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "foo.ba"), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "bar", 1);
        assertEquals proposals[0].getDisplayString(), "bar() : String - StringExtension (Groovy)"
            
        proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "this.collect"), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "collect", 2);
        assertTrue   ((proposals[0].getDisplayString().equals("collect(Closure closure) : List - DefaultGroovyMethods (Groovy)")) ||
                     (proposals[1].getDisplayString().equals("collect(Closure closure) : List - DefaultGroovyMethods (Groovy)")))
        assertTrue   ((proposals[0].getDisplayString().equals("collect(Collection arg1, Closure arg2) : Collection - DefaultGroovyMethods (Groovy)")) ||
                     (proposals[1].getDisplayString().equals("collect(Collection arg1, Closure arg2) : Collection - DefaultGroovyMethods (Groovy)")))
    }
    
    public void testVisibility() throws Exception {
        String groovyClass = 
"""
class B { }
class C {
    B theB
}
new C().th
"""
        ICompilationUnit groovyUnit = create(groovyClass)
        incrementalBuild()
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "().th"), GeneralGroovyCompletionProcessor.class);
        proposalExists proposals, "theB", 1
        assertEquals "theB B - C (Groovy)", proposals[0].getDisplayString()
            
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
