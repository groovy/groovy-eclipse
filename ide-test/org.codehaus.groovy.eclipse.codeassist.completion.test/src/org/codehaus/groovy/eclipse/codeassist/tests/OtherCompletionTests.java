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
 * @created Sept 29, 2009
 * 
 * Tests specific bug reports
 */
public class OtherCompletionTests extends CompletionTestCase {

    public OtherCompletionTests(String name) {
        super(name);
    }
    
    public void testGreclipse414() throws Exception {
        String contents = 
"public class Test {\n" +
    "int i\n" +
    "Test() {\n" +
        "this.i = 42\n" +
    "}\n" +
"Test(Test other) {\n" +
        "this.i = other.i\n" +
    "}\n" +
"}";
        ICompilationUnit unit = create(contents);
        fullBuild();
        // ensure that there is no ArrayIndexOutOfBoundsException thrown.
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "this."), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "i", 1);
    }
    
    // type signatures were popping up in various places in the display string
    // ensure this doesn't happen
    public void testGreclipse422() throws Exception {
        String javaClass = 
         "public class StringExtension {\n" +
         "public static String bar(String self) {\n" +
                     "return self;\n" +
                 "}\n" +
             "}\n";
            
        String groovyClass =
             "public class MyClass {\n" +
                 "public void foo() {\n" +
                     "String foo = 'foo';\n" +
                     "use (StringExtension) {\n" +
                         "foo.bar()\n" +
                     "}\n" +
                     "this.collect\n" +
                 "}\n" +
             "}";
            
        ICompilationUnit groovyUnit = create(groovyClass);
        env.addClass(groovyUnit.getParent().getResource().getFullPath(), "StringExtension", javaClass);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "foo.ba"), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "bar", 1);
        assertEquals (proposals[0].getDisplayString(), "bar() : String - StringExtension (Groovy)");
            
        proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "this.collect"), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "collect", 2);
        assertTrue ( ((proposals[0].getDisplayString().equals("collect(Closure closure) : List - DefaultGroovyMethods (Groovy)")) ||
                     (proposals[1].getDisplayString().equals("collect(Closure closure) : List - DefaultGroovyMethods (Groovy)"))));
        assertTrue ( ((proposals[0].getDisplayString().equals("collect(Collection arg1, Closure arg2) : Collection - DefaultGroovyMethods (Groovy)")) ||
                     (proposals[1].getDisplayString().equals("collect(Collection arg1, Closure arg2) : Collection - DefaultGroovyMethods (Groovy)"))));
    }
    
    public void testVisibility() throws Exception {
        String groovyClass = 
"class B { }\n" +
"class C {\n" +
    "B theB\n" +
"}\n" +
"new C().th\n";
        ICompilationUnit groovyUnit = create(groovyClass);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(groovyUnit, getIndexOf(groovyClass, "().th"), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "theB", 1);
        assertEquals("theB B - C (Groovy)", proposals[0].getDisplayString());
            
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
