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

package org.eclipse.jdt.core.groovy.tests.search;

import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

/**
 * @author Andrew Eisenberg
 * @created Oct 28, 2009
 *
 */
public class MethodReferenceSearchTests extends AbstractGroovySearchTest {
    public MethodReferenceSearchTests(String name) {
        super(name);
    }
    
    public static Test suite() {
        return buildTestSuite(MethodReferenceSearchTests.class);
    }

    
    public void testMethodReferencesInScript1() throws Exception {
        doTestForTwoMethodReferencesInScript("new First().xxx\nnew First()\n.\nxxx");
    }
    
    public void testMethodReferencesInScript1GRE_1180() throws Exception {
        doTestForTwoMethodReferencesInScript("new First().xxx\n'xxx'\n\"xxx\"\n\"\"\"xxx\"\"\"\nnew First()\n.\nxxx");
    }

    public void testMethodReferencesInScript2() throws Exception {
        doTestForTwoMethodReferencesInScript("First f = new First()\n f.xxx = f.xxx");
    }
    public void testMethodReferencesInScript3() throws Exception {
        doTestForTwoMethodReferencesInScript("First f = new First()\n \"$f.xxx\"\n\"$f.xxx\"");
    }
    public void testMethodReferencesInScript4() throws Exception {
        doTestForTwoMethodReferencesInScriptWithQuotes("First f = new First()\n f.'xxx'\nf.'xxx'");
    }
    public void testMethodReferencesInScript5() throws Exception {
        doTestForTwoMethodReferencesInScript("First f = new First()\n f.xxx\ndef xxx = 0\nxxx++\nf.xxx");
    }
    public void testMethodReferencesInScript6() throws Exception {
        doTestForTwoMethodReferencesInScript("class SubClass extends First { } \n SubClass f = new SubClass()\n f.xxx\ndef xxx = 0\nxxx++\nf.xxx");
    }
    public void testMethodReferencesInScript7() throws Exception {
        createUnit("Other.groovy", "class Other { def xxx }");
        doTestForTwoMethodReferencesInScript("class SubClass extends First { } \n SubClass f = new SubClass()\n f.xxx\nnew Other().xxx = 0\nf.xxx");
    }
    public void testMethodReferencesInScript8() throws Exception {
        doTestForTwoMethodReferencesInScript(
                "class SubClass extends First { } \n " +
        		"def f = new SubClass()\n " +
        		"f.xxx\n" + // here
        		"f = 9\n" +
        		"f.xxx\n" +  // invalid reference
        		"f = new SubClass()\n" +
        		"f.xxx");  // here
    }
    public void testMethodReferencesInClass1() throws Exception {
        doTestForTwoMethodReferencesInClass("class Second extends First { \ndef method() { this.xxx }\ndef xxx() { }\n def method2() { super.xxx }}");
    }

    public void testMethodReferencesInClass2() throws Exception {
        doTestForTwoMethodReferencesInClass("class Second extends First { \ndef method() { xxx }\ndef xxx() { }\n def method2(xxx) { xxx = super.xxx }}");
    }

    public void testMethodReferencesInClass3() throws Exception {
        doTestForTwoMethodReferencesInClass(
        		"class Second extends First {\n" +
        		"  def method() {\n" +
        		"    this.xxx = 'nothing'\n" + // yes
        		"  }\n" +
                "  def xxx() { }\n" +  // no
        		"  def method2() {\n" +  // no
        		"    def nothing = super.xxx()\n" +  // yes...field reference used as a closure
        		"  }\n" +
        		"}");
    }
    public void testMethodReferencesInClass4() throws Exception {
        createUnit("Third",  
                "class Third {\n" +
                "  def xxx() { }\n" + // no
        "}\n");
        doTestForTwoMethodReferencesInClass(
                "class Second extends First {\n" +
                "  def method() {\n" +
                "    this.xxx = 'nothing'\n" + // yes
                "  }\n" +
                "  def xxx() { }\n" +  // no
                "  def method3(xxx) {\n" +  // no
                "    new Third().xxx()\n" + // no
                "    xxx()\n" + // no...this will try to execute the xxx parameter, not the method 
                "    xxx = xxx\n" +  // no, no
                "    def nothing = super.xxx\n" +  // yes...method reference passed as a closure
                "  }\n" +
        "}");
    }
    
    public void testOverloadedMethodReferences1() throws Exception {
    	// should match on the method reference with precise # of args as well as method reference with unmatched number of args
    	doTestForTwoMethodReferences(
    			"interface First {\n" + 
    			"    void xxx()\n" + 
    			"    void xxx(a)\n" + 
    			"}",
    			"public class Second implements First {\n" + 
		        "    public void other() {\n" +
		        "        xxx()\n" +
		        "    }\n" +
    			"    public void xxx() {\n" +
    			"        xxx(a)\n" +
    			"    }\n" +
    			"    void xxx(a) {\n" +
    			"        xxx(a,b)\n" +
    			"    }\n" +
    			"}", false, 0, "xxx" );
    }
    
    public void testOverloadedMethodReferences2() throws Exception {
        // should match on the method reference with precise # of args as well as method reference with unmatched number of args
        doTestForTwoMethodReferences(
                "interface First {\n" + 
                "    void xxx(a)\n" + 
                "    void xxx()\n" + 
                "}",
                "public class Second implements First {\n" + 
                "    public void other() {\n" +
                "        xxx(a)\n" +
                "    }\n" +
                "    public void xxx() {\n" +
                "        xxx()\n" +
                "    }\n" +
                "    void xxx(a) {\n" +
                "        xxx(a,b)\n" +
                "    }\n" +
                "}", false, 0, "xxx" );
    }
    
    public void testOverloadedMethodReferences3() throws Exception {
        // should match on the method reference with precise # of args as well as method reference with unmatched number of args
    	createUnit("Sub", 
                "interface Sub extends First { void xxx(a) }");
        doTestForTwoMethodReferences(
        		"interface First {\n" + 
                        "    void xxx(a)\n" + 
                        "    void xxx()\n" + 
                        "}",
                "public class Second implements Sub {\n" + 
                "    public void other() {\n" +
                "        xxx(a)\n" +
                "    }\n" +
                "    public void xxx() {\n" +
                "        xxx()\n" +
                "    }\n" +
                "    void xxx(a) {\n" +
                "        xxx(a,b)\n" +
                "    }\n" +
                "}", false, 0, "xxx" );
    }
    
    public void testOverloadedMethodReferences4() throws Exception {
        // should match on the method reference with precise # of args as well as method reference with unmatched number of args
        createUnit("Sub", 
                "interface Sub extends First {\n" +
                "    void xxx(a)\n" +
                "    void xxx(a,b,c)\n" +
                "}");
        doTestForTwoMethodReferences(
                "interface First {\n" + 
                "    void xxx(a,b)\n" + 
                "    void xxx(a)\n" + 
                "}",
                "public class Second implements Sub {\n" + 
                "    public void other() {\n" +
                "        First f\n" +
                "        f.xxx(a,b,c)\n" +
                "    }\n" +
                "    public void xxx() {\n" +
                "        xxx(a)\n" +
                "        xxx(a,b,c)\n" +
                "        Sub s\n" +
                "        s.xxx(a)\n" +
                "        s.xxx(a,b,c)\n" +
                "    }\n" +
                "    void xxx(a) {\n" +
                "        Sub s\n" +
                "        s.xxx(a,b)\n" +
                "    }\n" +
                "}", false, 0, "xxx" );
    }
    
    private void doTestForTwoMethodReferencesInScript(String secondContents) throws JavaModelException {
        doTestForTwoMethodReferences(FIRST_CONTENTS_CLASS_FOR_METHODS, secondContents, true, 3, "xxx");
    }
    private void doTestForTwoMethodReferencesInScriptWithQuotes(String secondContents) throws JavaModelException {
        doTestForTwoMethodReferences(FIRST_CONTENTS_CLASS_FOR_METHODS, secondContents, true, 3, "'xxx'");
    }
    private void doTestForTwoMethodReferencesInClass(String secondContents) throws JavaModelException {
        doTestForTwoMethodReferences(FIRST_CONTENTS_CLASS_FOR_METHODS, secondContents, false, 0, "xxx");
    }
}
