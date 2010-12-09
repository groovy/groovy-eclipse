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
package org.codehaus.groovy.eclipse.codeassist.tests;

import org.codehaus.groovy.eclipse.codeassist.processors.GroovyImportRewriteFactory;
import org.codehaus.groovy.eclipse.codeassist.processors.GroovyProposalTypeSearchRequestor;

import groovy.util.GroovyTestCase;

/**
 * 
 * @author Andrew Eisenberg
 * @created Jul 29, 2010
 */
class FindImportsRegionTests extends GroovyTestCase {
    public void testFindImportsRegion0() throws Exception {
        checkRegion("""""","""""")
    }
    public void testFindImportsRegion1() throws Exception {
        checkRegion(
                """
package p
import a
import b
class I { }            
            """,
                """
package p
import a
import b
"""
                )
    }
    
    public void testFindImportsRegion2() throws Exception {
        checkRegion(
                """
import a
import b
class I { }            
                """,
                """
import a
import b
"""
                )
    }
    
    // we made the decision only to look at import statements that start at the 
    // beginning of the line.  An argument can be made otherwise and this can
    // be changed in the future
    public void testFindImportsRegion3() throws Exception {
        checkRegion(
                """
import a
 import b
class I { }            
                """,
                """
import a
"""
                )
    }
    
    public void testFindImportsRegion4() throws Exception {
        checkRegion(
                """
 import a
import b
class I { }
                """,
                """
 import a
import b
"""
        )
    }

    
    public void testFindImportsRegion5() throws Exception {
        checkRegion(
            """
package p
class I { }
            """,
            """
package p
"""
                )
    }

    public void testFindImportsRegion6() throws Exception {
        checkRegion(
            """package p
class I { }
            """,
            """package p
"""
                )
    }

    public void testFindImportsRegion7() throws Exception {
        checkRegion(
            """/**
            *
            * 
            */
package p
class I { }
            """,
            """/**
            *
            * 
            */
package p
"""
                )
    }

        public void testFindImportsRegion8() throws Exception {
        checkRegion(
            """/**
            *
            * 
            */
package p
import a.b.c // fdsaffdsa
class I { }
            """,
            """/**
            *
            * 
            */
package p
import a.b.c // fdsaffdsa
"""
                )
    }
    void checkRegion(String initial, String expected) {
        assertEquals expected, GroovyImportRewriteFactory.findImportsRegion(initial).toString();
    }
}
