 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.adapters;

import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.codehaus.groovy.eclipse.ui.GroovyResourcePropertyTester;
import org.codehaus.jdt.groovy.model.GroovyNature;
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
        testProject.addNature(GroovyNature.GROOVY_NATURE);
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
