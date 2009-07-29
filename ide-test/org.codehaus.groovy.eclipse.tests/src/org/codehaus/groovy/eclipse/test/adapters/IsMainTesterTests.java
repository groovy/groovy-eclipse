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

package org.codehaus.groovy.eclipse.test.adapters;

import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.codehaus.groovy.eclipse.ui.GroovyResourcePropertyTester;
import org.eclipse.core.resources.IFile;

/**
 * @author Andrew Eisenberg
 * @created Jul 10, 2009
 *
 * This class tests GroovyResourcePropertyTester
 * 
 * Tests to see if a groovy file has a runnable main method
 */
public class IsMainTesterTests extends EclipseTestCase {

    
    public void testHasMain1() throws Exception {
        doTest("class MainClass { static void main(String[] args){}}", true);
    }
    public void testHasMain2() throws Exception {
        doTest("class MainClass { static main(args){}}", true);
    }
    public void testHasMain2a() throws Exception {
        doTest("class MainClass { static def main(args){}}", true);
    }
    public void testHasMain3() throws Exception {
        // not static
        doTest("class MainClass { void main(String[] args){}}", false);
    }
    public void testHasMain3a() throws Exception {
        // no args
        doTest("class MainClass { static void main(){}}", false);
    }
    public void testHasMain4() throws Exception {
        // no script defined in this file
        doTest("class OtherClass { def s() { } }", false);
    }
    public void testHasMain5() throws Exception {
        // has a script
        doTest("thisIsPartOfAScript()\nclass OtherClass { def s() { } }", true);
    }
    public void testHasMain5a() throws Exception {
        // has a script
        doTest("class OtherClass { def s() { } }\nthisIsPartOfAScript()", true);
    }
    public void testHasMain6() throws Exception {
        doTest("thisIsPartOfAScript()", true);
    }
    public void testHasMain7() throws Exception {
        doTest("def x() { } \nx()", true);
    }
    

    
    private void doTest(String text, boolean expected) throws Exception {
        
        IFile file = testProject.createGroovyTypeAndPackage( "pack1",
                "MainClass.groovy",
                text);
        
        GroovyResourcePropertyTester tester = new GroovyResourcePropertyTester();
        boolean result = tester.test(file, "hasMain", null, null);
        if (result != expected) {
            fail("Should have " + (expected ? "" : "*not*") + " found a main method in class:\n" + text);
        }
    }
}
