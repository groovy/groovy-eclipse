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

import java.util.List;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;

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
    public void testSearchForTypesClass7() throws Exception {
        createUnit("other", "First", "class First { }");
        doTestForTwoInClass("class Second extends First {\n" +
        		" def x() {\n" + // yes
        		" y = new other.First()\n" + // no
        		" y = new First()} }"); // yes
    }
    
    public void testSearchForTypesArray1() throws Exception {
        doTestForTwoInScript("First[] f = { First[] h -> h }");
    }
    
    /**
     * GRECLIPSE-650
     * @throws Exception
     */
    public void testFindClassDeclaration() throws Exception {
        String firstContents = "class First { First x }";
        String secondContents = "class Second extends First {}";
        List<SearchMatch> matches = getAllMatches(firstContents, secondContents);
        assertEquals("Should find First 2 times", 2, matches.size());
        SearchMatch match = matches.get(0);
        int start = match.getOffset();
        int end = start + match.getLength();
        assertEquals("Invalid location", "First", firstContents.substring(start, end));

        match = matches.get(1);
        start = match.getOffset();
        end = start + match.getLength();
        assertEquals("Invalid location", "First", secondContents.substring(start, end));
    }
    
    /**
     * GRECLIPSE-628
     */
    public void testShouldntFindClassDeclarationInScript() throws Exception {
        String firstContents = "print 'me'";
        String secondContents = "print 'me'";
        List<SearchMatch> matches = getAllMatches(firstContents, secondContents);
        assertEquals("Should find no matches", 0, matches.size());
    }
    
    public void testInnerTypes1() throws Exception {
        String firstContents = 
            "class Other {\n" + 
            "        class First { }\n" + 
            "}";
        
        String secondContents = 
            "import Other.First\n" + 
            "Map<First, ? extends First> h\n" + 
            "Other.First j\n" + 
            "First i";
        
        String name = "First";
        int len = name.length();
        
        List<SearchMatch> matches = getAllMatches(firstContents, secondContents, true);
        assertEquals("Wrong number of matches found:\n" + matches, 5, matches.size());
        
        int start = secondContents.indexOf("First");
        SearchMatch match = matches.get(0);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());
        
        start = secondContents.indexOf("First", start+1);
        match = matches.get(1);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());
        
        start = secondContents.indexOf("First", start+1);
        match = matches.get(2);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());
        
        start = secondContents.indexOf("First", start+1);
        match = matches.get(3);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());
        
        start = secondContents.indexOf("First", start+1);
        match = matches.get(4);
        assertEquals("Wrong offset " + match, start, match.getOffset());
        assertEquals("Wrong length " + match, len, match.getLength());
    }
    
    
    private void doTestForTwoInScript(String secondContents) throws JavaModelException {
        doTestForTwoTypeReferences(FIRST_CONTENTS_CLASS, secondContents, true, 3);
    }
    private void doTestForTwoInClass(String secondContents) throws JavaModelException {
        doTestForTwoTypeReferences(FIRST_CONTENTS_CLASS, secondContents, false, 0);
    }
    private void doTestForTwoInClassUseWithDefaultMethod(String secondContents) throws JavaModelException {
        // capture the default method that is created instead of the original method
        // it seems that the order of the method variants is switched depending on whether or not 
        // concrete asts are requested.
        doTestForTwoTypeReferences(FIRST_CONTENTS_CLASS, secondContents, false, 1);
    }
    private void doTestForTwoInClassWithImplements(String secondContents) throws JavaModelException {
        doTestForTwoTypeReferences(FIRST_CONTENTS_INTERFACE, secondContents, false, 0);
    }
}
