/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.ui.search;

import java.util.Arrays;

import junit.framework.Test;

import org.codehaus.groovy.eclipse.search.GroovyOccurrencesFinder;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.groovy.tests.search.AbstractGroovySearchTest;
import org.eclipse.jdt.core.groovy.tests.search.CategorySearchTests;
import org.eclipse.jdt.internal.ui.search.IOccurrencesFinder.OccurrenceLocation;

/**
 * Tests for {@link GroovyOccurrencesFinder}
 *
 * @author andrew
 * @created Jan 2, 2011
 */
public class FindOccurrencesTests extends AbstractGroovySearchTest {

    public FindOccurrencesTests(String name) {
        super(name);
    }

    public static Test suite() {
        return buildTestSuite(FindOccurrencesTests.class);
    }

    public void testFindLocalOccurrences1() throws Exception {
        String contents = "def x\nx";
        doTest(contents, contents.lastIndexOf('x'), 1, contents.indexOf('x'), 1, contents.lastIndexOf('x'), 1);
    }        

    public void testFindLocalOccurrences2() throws Exception {
        String contents = "def x(x) {\nx}";
        doTest(contents, contents.lastIndexOf('x'), 1, contents.indexOf("(x")+1, 1, contents.lastIndexOf('x'), 1);
    }        
    
    public void testFindLocalOccurrences3() throws Exception {
        String contents = "nuthin\ndef x(int x) {\nx}";
        doTest(contents, contents.lastIndexOf('x'), 1, contents.indexOf("int x"), "int x".length(), contents.lastIndexOf('x'), 1);
    }        
    
    // looking for the method declaration, not the parameter
    public void testFindLocalOccurrences4() throws Exception {
        String contents = "nuthin\ndef x(int x) {\nx}";
        doTest(contents, contents.indexOf('x'), 1, contents.indexOf('x'), 1);
    }        
    
    public void testFindPrimitive() throws Exception {
        String contents = "int x(int y) {\nint z}\n int a";
        int length = "int".length(); 
        int first = contents.indexOf("int");
        int second = contents.indexOf("int", first+1);
        int third = contents.indexOf("int", second+1);
        int fourth = contents.indexOf("int", third+1);
        
        doTest(contents, first, length, first, length, second, length, third, length, fourth, length);
    }        
    
    public void testFindDGMOccurrences1() throws Exception {
        String contents = "print 'print'\n'$print'\n'${print}'";
        int length = "print".length();
        int length2 = "'$print'".length();
        int length3 = "'${print}'".length();
        doTest(contents, contents.indexOf("print"), 1, contents.indexOf("print"), length, contents.lastIndexOf("'$print'"), length2, contents.lastIndexOf("'${print}'"), length3);
    }        
    
    private void doTest(String contents, int start, int length, int ... expected) throws JavaModelException {
        GroovyCompilationUnit unit = createUnit("Occurrences", contents);
        try {
            unit.becomeWorkingCopy(null);
            OccurrenceLocation[] actual = find(unit, start, length);
            assertOccurrences(expected, actual);
        } finally {
            unit.discardWorkingCopy();
        }
    }
    
    private void assertOccurrences(int[] expected, OccurrenceLocation[] actual) {
        assertEquals("Wrong number of occurrences found. expecting:\n" + 
        		Arrays.toString(expected) + "\nbut found:\n" + 
        		printOccurrences(actual), expected.length/2, actual.length);
        for (int i = 0; i < actual.length; i++) {
            assertEquals("Problem in Occurrence " + i + " expecting:\n" + 
                    Arrays.toString(expected) + "\nbut found:\n" + 
                    printOccurrences(actual), expected[i*2], actual[i].getOffset());
            assertEquals("Problem in Occurrence " + i + " expecting:\n" + 
                    Arrays.toString(expected) + "\nbut found:\n" + 
                    printOccurrences(actual), expected[i*2+1], actual[i].getLength());
        }
    }

    private String printOccurrences(OccurrenceLocation[] os) {
        StringBuilder sb = new StringBuilder();
        for (OccurrenceLocation o : os) {
            sb.append(o + "\n");
        }
        return sb.toString();
    }
    
    private OccurrenceLocation[] find(GroovyCompilationUnit unit, int start, int length) {
        GroovyOccurrencesFinder finder = new GroovyOccurrencesFinder();
        finder.setGroovyCompilationUnit(unit);
        finder.initialize(null, start, length);
        return finder.getOccurrences();
    }

}
