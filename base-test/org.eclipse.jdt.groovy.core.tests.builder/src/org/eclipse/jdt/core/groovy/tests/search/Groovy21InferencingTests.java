 /*
 * Copyright 2003-2013 the original author or authors.
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

import org.eclipse.jdt.core.tests.util.GroovyUtils;

/**
 * Tests for all Groovy 2.1 specific things
 * for example, {@link groovy.lang.DelegatesTo}
 * @author Andrew Eisenberg
 * @created Feb 5, 2013
 */
public class Groovy21InferencingTests extends AbstractInferencingTest {
 
    public static Test suite() {
        return buildTestSuite(Groovy21InferencingTests.class);
    }

    public Groovy21InferencingTests(String name) {
        super(name);
    }
    
    // tests CompareToNullExpression
    public void testDelegatesTo1() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 21 ) {
            return;
        }
        
        String contents = 
                "class Other { }\n" +
        		"def meth(@DelegatesTo(Other) Closure c) { }\n" +
        		"meth { delegate }";
        
        String toFind = "delegate";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "Other");
   }
    public void testDelegatesTo1a() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 21 ) {
            return;
        }
        
        String contents = 
                "class Other { }\n" +
                "def meth(@DelegatesTo(Other) c) { }\n" +
                "meth { delegate }";
        
        String toFind = "delegate";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "Other");
   }
   public void testDelegatesTo2() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 21 ) {
            return;
        }
        
        String contents = 
                "class Other { int xxx }\n" +
                "def meth(@DelegatesTo(Other) Closure c) { }\n" +
                "meth { xxx }";
        
        String toFind = "xxx";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
   }
   public void testDelegatesTo3() throws Exception {
       if (GroovyUtils.GROOVY_LEVEL < 21 ) {
           return;
       }
       
       String contents = 
               "def meth(@DelegatesTo(List) Closure c) { }\n" +
               "meth { delegate }";
       
       String toFind = "delegate";
       int start = contents.lastIndexOf(toFind);
       int end = start + toFind.length();
       assertType(contents, start, end, "java.util.List");
   }
   public void testDelegatesTo4() throws Exception {
       if (GroovyUtils.GROOVY_LEVEL < 21 ) {
           return;
       }
       
       String contents = 
               "def meth(int x, int y, @DelegatesTo(List) Closure c) { }\n" +
               "meth 1, 2, { delegate }";
       
       String toFind = "delegate";
       int start = contents.lastIndexOf(toFind);
       int end = start + toFind.length();
       assertType(contents, start, end, "java.util.List");
   }
   
   // expected to be broken
   public void testDelegatesTo5() throws Exception {
       if (GroovyUtils.GROOVY_LEVEL < 21 ) {
           return;
       }
       
       String contents = 
               "def meth(int x, int y, @DelegatesTo(List<String) Closure c) { }\n" +
               "meth { delegate }";
       
       String toFind = "delegate";
       int start = contents.lastIndexOf(toFind);
       int end = start + toFind.length();
       assertType(contents, start, end, "Search");
   }
}