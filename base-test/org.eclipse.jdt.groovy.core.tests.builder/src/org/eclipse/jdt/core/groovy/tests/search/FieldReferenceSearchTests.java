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
import org.eclipse.jdt.core.search.IJavaSearchConstants;

import junit.framework.Test;

/**
 * @author Andrew Eisenberg
 * @created Sep 2, 2009
 *
 */
public class FieldReferenceSearchTests extends AbstractGroovySearchTest {
    public FieldReferenceSearchTests(String name) {
        super(name);
    }
    
    public static Test suite() {
        return buildTestSuite(FieldReferenceSearchTests.class);
    }

    
    public void testFieldReferencesInScript1() throws Exception {
        doTestForTwoFieldReferencesInScript("new First().xxx\nnew First()\n.\nxxx");
    }
    public void testFieldReferencesInScript2() throws Exception {
        doTestForTwoFieldReferencesInScript("First f = new First()\n f.xxx = f.xxx");
    }
    public void testFieldReferencesInScript3() throws Exception {
        doTestForTwoFieldReferencesInScript("First f = new First()\n \"$f.xxx\"\n\"$f.xxx\"");
    }
    public void testFieldReferencesInScript4() throws Exception {
        doTestForTwoFieldReferencesInScriptWithQuotes("First f = new First()\n f.'xxx'\nf.'xxx'");
    }
    public void testFieldReferencesInScript5() throws Exception {
        doTestForTwoFieldReferencesInScript("First f = new First()\n f.xxx\ndef xxx = 0\nxxx++\nf.xxx");
    }
    public void testFieldReferencesInScript6() throws Exception {
        doTestForTwoFieldReferencesInScript("class SubClass extends First { } \n SubClass f = new SubClass()\n f.xxx\ndef xxx = 0\nxxx++\nf.xxx");
    }
    public void testFieldReferencesInScript7() throws Exception {
        createUnit("Other.groovy", "class Other { def xxx }");
        doTestForTwoFieldReferencesInScript("class SubClass extends First { } \n SubClass f = new SubClass()\n f.xxx\nnew Other().xxx = 0\nf.xxx");
    }
    public void testFieldReferencesInScript8() throws Exception {
        doTestForTwoFieldReferencesInScript(
                "class SubClass extends First { } \n " +
        		"def f = new SubClass()\n " +
        		"f.xxx\n" + // here
        		"f = 9\n" +
        		"f.xxx\n" +  // invalid reference
        		"f = new SubClass()\n" +
        		"f.xxx");  // here
    }
    public void testFieldReferencesInClass1() throws Exception {
        doTestForTwoFieldReferencesInClass("class Second extends First { \ndef method() { this.xxx }\ndef xxx() { }\n def method2() { super.xxx }}");
    }

    public void testFieldReferencesInClass2() throws Exception {
        doTestForTwoFieldReferencesInClass("class Second extends First { \ndef method() { xxx }\ndef xxxDONT_SHADOW_SUPER_FIELD() { }\n def method2(xxx) { xxx = super.xxx }}");
    }

    public void testFieldReferencesInClass3() throws Exception {
        doTestForTwoFieldReferencesInClass(
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
    public void testFieldReferencesInClass4() throws Exception {
        createUnit("Third",  
                "class Third {\n" +
                "  def xxx\n" + // no
        "}\n");
        doTestForTwoFieldReferencesInClass(
                "class Second extends First {\n" +
                "  def method() {\n" +
                "    this.xxx = 'nothing'\n" + // yes
                "  }\n" +
                "  def xxx() { }\n" +  // no
                "  def method3(xxx) {\n" +  // no
                "    new Third().xxx\n" + // no
                "    xxx()\n" + // no 
                "    xxx = xxx\n" +  // no, no
                "    def nothing = super.xxx()\n" +  // yes...field reference used as a closure
                "  }\n" +
        "}");
    }
    
    public void testFieldReferenceInGString1() throws Exception {
        doTestForTwoFieldReferencesInGString("class Second extends First {\ndef x() { \"${xxx}\"\n\"${xxx.toString()}\" }");
    }
    public void testFieldReferenceInGString2() throws Exception {
        doTestForTwoFieldReferencesInGString("class Second extends First {\ndef x() { \"${ xxx }\"\n\"${ xxx .toString()}\" }");
    }
    public void testFieldReferenceInGString3() throws Exception {
        doTestForTwoFieldReferencesInGString("class Second extends First {\ndef x() { \"${xxx} ${xxx.toString()}\" }");
    }
    public void testFieldReferenceInGString4() throws Exception {
        doTestForTwoFieldReferencesInGString("class Second extends First {\ndef x() { \"${foo(xxx)} ${super.xxx}\" }");
    }
    
    protected void doTestForTwoFieldReferencesInGString(
            String secondContents) throws JavaModelException {
        super.doTestForTwoFieldReferencesInGString(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents,
                "xxx");
    }

    
    public void testFieldWritesInScript1() throws Exception {
        doTestForTwoFieldWritesInScript(
                "new First().xxx = 1\n" +
                "new First().xxx\n" +
                "def f = new First()\n" +
                "def y = f.xxx\n" +
        "f.xxx = 8");
    }

    public void testFieldReadsInScript1() throws Exception {
        doTestForTwoFieldReadsInScript(
                "new First().xxx\n" +
                "new First().xxx = 1\n" +
                "def f = new First()\n" +
        "f.xxx = f.xxx");
    }
    
    
    private void doTestForTwoFieldWritesInScript(String secondContents) throws JavaModelException {
        doTestForTwoFieldReferences(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, true, 3, "xxx", IJavaSearchConstants.WRITE_ACCESSES);
    }
    private void doTestForTwoFieldReadsInScript(String secondContents) throws JavaModelException {
        doTestForTwoFieldReferences(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, true, 3, "xxx", IJavaSearchConstants.READ_ACCESSES);
    }

    private void doTestForTwoFieldReferencesInScript(String secondContents) throws JavaModelException {
        doTestForTwoFieldReferences(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, true, 3, "xxx");
    }
    private void doTestForTwoFieldReferencesInScriptWithQuotes(String secondContents) throws JavaModelException {
        doTestForTwoFieldReferences(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, true, 3, "'xxx'");
    }
    private void doTestForTwoFieldReferencesInClass(String secondContents) throws JavaModelException {
        doTestForTwoFieldReferences(FIRST_CONTENTS_CLASS_FOR_FIELDS, secondContents, false, 0, "xxx");
    }
}
