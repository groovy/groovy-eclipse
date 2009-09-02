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

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * @author Andrew Eisenberg
 * @created Sep 1, 2009
 *
 * Tests the groovy-specific type referencing search support
 */
public class TypeReferenceSearchTests extends AbstractGroovySearchTest {
    
	
    public TypeReferenceSearchTests(String name) {
        super(name);
    }
    
    public static Test suite() {
        return buildTestSuite(TypeReferenceSearchTests.class);
    }
	
    public void testSearchForTypesScript1() throws Exception {
    	doTestForTwoInScript("First f = new First()");
    }

    public void testSearchForTypesScript2() throws Exception {
        doTestForTwoInScript("First.class\nFirst.class");
    }

    public void testSearchForTypesScript3() throws Exception {
        doTestForTwoInScript("[First, First]");
    }
    
    public void testSearchForTypesScript4() throws Exception {
        // the key of a map is interpreted as a string
        // so don't put 'First' there
        doTestForTwoInScript("def x = [a : First]\nx[First.class]");
    }

    public void testSearchForTypesScript5() throws Exception {
        doTestForTwoInScript("x(First, First.class)");
    }
    
    public void testSearchForTypesScript6() throws Exception {
        // note that in "new First[ new First() ]", the first 'new First' is removed 
        // by the AntlrPluginParser and so there is no way to search for it
//        doTestForTwoInScript("new First[ new First() ]");
        doTestForTwoInScript("[ new First(), First ]");
    }
    
    public void testSearchForTypesClosure1() throws Exception {
        doTestForTwoInScript("{ First first, First second -> print first; print second;}");
    }
    
    public void testSearchForTypesClosure2() throws Exception {
        doTestForTwoInScript("def x = { First first = new First() }");
    }
    
    public void testSearchForTypesClosure3() throws Exception {
        doTestForTwoInScript("def x = { First.class\n First.class }");
    }
    
    public void testSearchForTypesClass1() throws Exception {
        doTestForTwoInClass("class Second extends First { First x }");
    }
    
    public void testSearchForTypesClass2() throws Exception {
        doTestForTwoInClass("class Second extends First { First x() { } }");
    }
    
    public void testSearchForTypesClass3() throws Exception {
        doTestForTwoInClass("class Second extends First { def x(First y) { } }");
    }
    
    public void testSearchForTypesClass4() throws Exception {
        doTestForTwoInClass("class Second extends First { def x(First ... y) { } }");
    }
    
    public void testSearchForTypesClass5() throws Exception {
        doTestForTwoInClassUseWithDefaultMethod("class Second extends First { def x(y = new First()) { } }");
    }
    public void testSearchForTypesClass6() throws Exception {
        doTestForTwoInClassWithImplements("class Second implements First { def x(First y) { } }");
    }
    
    
    // also need to test interfaces
    private void doTestForTwoInScript(String secondContents) throws JavaModelException {
        doTestForTwoTypeReferences(FIRST_CONTENTS_CLASS, secondContents, true, 3);
    }
    private void doTestForTwoInClass(String secondContents) throws JavaModelException {
        doTestForTwoTypeReferences(FIRST_CONTENTS_CLASS, secondContents, false, 0);
    }
    private void doTestForTwoInClassUseWithDefaultMethod(String secondContents) throws JavaModelException {
        // capture the default method that is created instead of the original method
        doTestForTwoTypeReferences(FIRST_CONTENTS_CLASS, secondContents, false, 1);
    }
    private void doTestForTwoInClassWithImplements(String secondContents) throws JavaModelException {
        doTestForTwoTypeReferences(FIRST_CONTENTS_INTERFACE, secondContents, false, 0);
    }
}
