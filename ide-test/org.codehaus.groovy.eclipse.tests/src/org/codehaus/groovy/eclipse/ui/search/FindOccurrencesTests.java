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
import org.eclipse.jdt.core.tests.util.GroovyUtils;
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
        int afterParen = contents.indexOf('(');
        doTest(contents, contents.lastIndexOf('x'), 1, contents.indexOf("x", afterParen), 1, contents.lastIndexOf('x'), 1);
    }        
    
    // looking for the method declaration, not the parameter
    public void testFindLocalOccurrences4() throws Exception {
        String contents = "nuthin\ndef x(int x) {\nx}";
        doTest(contents, contents.indexOf('x'), 1, contents.indexOf('x'), 1);
    }        
    
    public void testFindForLoopOccurrences() throws Exception {
        String contents = "for (x in []) {\n" +
        		"x }";
        doTest(contents, contents.indexOf('x'), 1, contents.indexOf('x'), 1, contents.lastIndexOf('x'), 1);
    }        
    
    /**
     * Not working now.  See GROOVY-4620 and GRECLIPSE-951
     */
    public void _testFindPrimitive() throws Exception {
        String contents = "int x(int y) {\nint z}\n int a";
        int length = "int".length(); 
        int first = contents.indexOf("int");
        int second = contents.indexOf("int", first+1);
        int third = contents.indexOf("int", second+1);
        int fourth = contents.indexOf("int", third+1);
        
        doTest(contents, second, length, first, length, second, length, third, length, fourth, length);
    }
    
    
    public void testFindProperty() throws Exception {
        String contents = "class X {\n" + 
        		"def foo\n" + 
        		"}\n" + 
        		"new X().foo\n" +
        		"new X().foo()\n";  // require a new line here or else Occurrence finding will crash.  See Bug 339614
        
        int length = "foo".length(); 
        int first = contents.indexOf("foo");
        int second = contents.indexOf("foo", first+1);
        int third = contents.indexOf("foo", second+1);
        doTest(contents, second, length, first, length, second, length, third, length);
    }
    
    public void testFindField() throws Exception {
        String contents = "class X {\n" + 
                "public def foo\n" + 
                "}\n" + 
                "new X().foo\n" +
                "new X().foo()\n";  // require a new line here or else Occurrence finding will crash.  See Bug 339614

        int length = "foo".length(); 
        int first = contents.indexOf("foo");
        int second = contents.indexOf("foo", first+1);
        int third = contents.indexOf("foo", second+1);
        doTest(contents, second, length, first, length, second, length, third, length);
    }
    
    public void testFindDGMOccurrences1() throws Exception {
        String contents = "print 'print'\n'$print'\n'${print}'";
        int length = "print".length();
        int length2 = "'$print'".length();
        int length3 = "'${print}'".length();
        doTest(contents, contents.indexOf("print"), 1, contents.indexOf("print"), length, contents.lastIndexOf("'$print'"), length2, contents.lastIndexOf("'${print}'"), length3);
    }
    
    // GRECLIPSE-1031
    public void testFindStaticMethods() throws Exception {
        String contents = 
            "class Static {\n" +
        	"  static staticMethod()  { staticMethod }\n" +
        	"  static { staticMethod }\n" +
        	"  { staticMethod }\n" +
        	"  def t = staticMethod()\n" +
        	"  def x() { " +
        	"    def a = staticMethod() \n" +
        	"    def b = staticMethod \n" +
        	"    Static.staticMethod 3, 4, 5\n" +
        	"    Static.staticMethod(3, 4, 5) \n" +
        	"  }\n" +
        	"}";
        
        String methName = "staticMethod";
        int len = methName.length();
        
        int start = contents.indexOf(methName);
        int start1 = contents.indexOf(methName);
        int start2 = contents.indexOf(methName, start1 + 1);
        int start3 = contents.indexOf(methName, start2 + 1);
        int start4 = contents.indexOf(methName, start3 + 1);
        int start5 = contents.indexOf(methName, start4 + 1);
        int start6 = contents.indexOf(methName, start5 + 1);
        int start7 = contents.indexOf(methName, start6 + 1);
        int start8 = contents.indexOf(methName, start7 + 1);
        int start9 = contents.indexOf(methName, start8 + 1);
        doTest(contents, start, len, start1, len, start2, len, start3, len, start4, len, start5, len, start6, len, start7, len, start8, len, start9, len);
    }

    
    // GRECLIPSE-1031
    // Groovy 1.8 specific test
    public void testFindStaticMethods18() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL > 17) {
            String contents = 
                "class Static {\n" +
                "  static staticMethod(nuthin)  { }\n" +
                "  def x() {\n" +
                "    def z = staticMethod \n" +
                "    def a = staticMethod 3, 4, 5\n" +
                "    def b = staticMethod(3, 4, 5) \n" +
                "    def c = Static.staticMethod 3, 4, 5\n" +
                "    def d = Static.staticMethod(3, 4, 5) \n" +
                "    // this one is commented out because of GRECLIPSE-4761" +
                "    // def z = staticMethod 3\n" +
                "  }\n" +
                "}";
            
            String methName = "staticMethod";
            int len = methName.length();
            
            int start = contents.indexOf(methName);
            int start1 = contents.indexOf(methName);
            int start2 = contents.indexOf(methName, start1 + 1);
            int start3 = contents.indexOf(methName, start2 + 1);
            int start4 = contents.indexOf(methName, start3 + 1);
            int start5 = contents.indexOf(methName, start4 + 1);
            int start6 = contents.indexOf(methName, start5 + 1);
            doTest(contents, start, len, start1, len, start2, len, start3, len, start4, len, start5, len, start6, len);
        } else {
            System.out.println("testFindStaticMethods18 is disabled when Groovy level is not 1.8 or higher");
        }
    }

    // GRECLIPSE-1023
    public void testInnerClass() throws Exception {
        String contents = 
            "class Other2 {\n" + 
            "        class Inner { }\n" + 
            "        Other2.Inner f\n" + 
            "        Inner g\n" + 
            "}";
        
        String className = "Inner";
        int len = className.length();
        int start = contents.indexOf(className);
        int start1 = contents.indexOf(className);
        int start2 = contents.indexOf(className, start1 + 1);
        int start3 = contents.indexOf(className, start2 + 1);
        doTest(contents, start, len, start1, len, start2, len, start3, len);
    }

    // GRECLIPSE-1023
    // try different starting point
    public void testInnerClass2() throws Exception {
        String contents = 
            "class Other2 {\n" + 
            "        class Inner { }\n" + 
            "        Other2.Inner f\n" + 
            "        Inner g\n" + 
            "}";
        
        String className = "Inner";
        int len = className.length();
        int start1 = contents.indexOf(className);
        int start2 = contents.indexOf(className, start1 + 1);
        int start3 = contents.indexOf(className, start2 + 1);
        int start = start2;
        doTest(contents, start, len, start1, len, start2, len, start3, len);
    }

    // GRECLIPSE-1023
    // try different starting point
    public void testInnerClass3() throws Exception {
        String contents = 
            "class Other2 {\n" + 
            "        class Inner { }\n" + 
            "        Other2.Inner f\n" + 
            "        Inner g\n" + 
            "}";
        
        String className = "Inner";
        int len = className.length();
        int start1 = contents.indexOf(className);
        int start2 = contents.indexOf(className, start1 + 1);
        int start3 = contents.indexOf(className, start2 + 1);
        int start = start3;
        doTest(contents, start, len, start1, len, start2, len, start3, len);
    }
    
    // GRECLIPSE-1023
    // inner class in other file
    public void testInnerClass4() throws Exception {
        createUnit("Other",  
                "class Other {\n" + 
                "  class Inner { }\n" + 
                "}");
        String contents = 
            "import Other.Inner\n" +
            "Other.Inner f\n" + 
            "Inner g";
        
        String className = "Inner";
        int len = className.length();
        int start1 = contents.indexOf(className);
        int start2 = contents.indexOf(className, start1 + 1);
        int start3 = contents.indexOf(className, start2 + 1);
        int start = start1;
        doTest(contents, start, len, start1, len, start2, len, start3, len);
    }
 
    
    public void testGenerics1() throws Exception {
        String contents = "import javax.swing.text.html.HTML\n" + 
        		"Map<HTML, ? extends HTML> h\n" + 
        		"HTML i";
        
        String name = "HTML";
        int len = name.length();
        
        int start1 = contents.indexOf(name);
        int start2 = contents.indexOf(name, start1 + 1);
        int start3 = contents.indexOf(name, start2 + 1);
        int start4 = contents.indexOf(name, start3 + 1);
        int start = start1;
        doTest(contents, start, len, start1, len, start2, len, start3, len, start4, len);
    }
    
    // As before but use a different starting point
    public void testGenerics2() throws Exception {
        String contents = "import javax.swing.text.html.HTML\n" + 
        "Map<HTML, ? extends HTML> h\n" + 
        "HTML i";
        
        String name = "HTML";
        int len = name.length();
        
        int start1 = contents.indexOf(name);
        int start2 = contents.indexOf(name, start1 + 1);
        int start3 = contents.indexOf(name, start2 + 1);
        int start4 = contents.indexOf(name, start3 + 1);
        int start = start2;
        doTest(contents, start, len, start1, len, start2, len, start3, len, start4, len);
    }
    
    // As before but use a different starting point
    public void testGenerics3() throws Exception {
        String contents = "import javax.swing.text.html.HTML\n" + 
        "Map<HTML, ? extends HTML> h\n" + 
        "HTML i";
        
        String name = "HTML";
        int len = name.length();
        
        int start1 = contents.indexOf(name);
        int start2 = contents.indexOf(name, start1 + 1);
        int start3 = contents.indexOf(name, start2 + 1);
        int start4 = contents.indexOf(name, start3 + 1);
        int start = start4;
        doTest(contents, start, len, start1, len, start2, len, start3, len, start4, len);
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
