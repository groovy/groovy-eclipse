/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite

import groovy.transform.TypeChecked

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.core.runtime.CoreException
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.SourceRange
import org.eclipse.jdt.core.search.IJavaSearchConstants
import org.eclipse.jdt.core.search.IJavaSearchScope
import org.eclipse.jdt.core.search.SearchEngine
import org.eclipse.jdt.core.search.SearchPattern
import org.eclipse.jdt.core.search.TypeNameRequestor
import org.eclipse.jdt.internal.core.CompilationUnit

@TypeChecked
abstract class BrowsingTestCase extends TestCase {

    /**
     * Parent class should define:<pre>
     * public static Test suite() {
     *   return newTestSuite(Whatever.class);
     * }</pre>
     */
    protected static Test newTestSuite(Class test) {
        new BrowsingTestSetup(new TestSuite(test))
    }

    @Override
    protected void tearDown() throws Exception {
        BrowsingTestSetup.removeSources()
    }

    @Override
    protected void setUp() throws Exception {
        println '------------------------------'
        println "Starting: ${getName()}"
    }

    protected GroovyCompilationUnit addGroovySource(CharSequence contents, String name = nextFileName(), String pack = '') {
        BrowsingTestSetup.addGroovySource(contents, name, pack)
    }

    protected CompilationUnit addJavaSource(CharSequence contents, String name = nextFileName(), String pack = '') {
        BrowsingTestSetup.addJavaSource(contents, name, pack)
    }

    protected IJavaElement assertCodeSelect(Iterable<? extends CharSequence> sources, String target, String elementName = target) {
        def unit = null
        sources.each {
            unit = addGroovySource(it.toString(), nextFileName())
        }
        GroovyCompilationUnit gunit = unit as GroovyCompilationUnit

        waitUntilIndexesReady()
        gunit.becomeWorkingCopy(null)

        int offset = gunit.source.lastIndexOf(target), length = target.length()
        assert offset >= 0 && length > 0 && offset + length <= gunit.source.length()

        IJavaElement[] elems = gunit.codeSelect(offset, length)
        if (!elementName) {
            assertEquals(0, elems.length)
        } else {
            assertEquals("Should have found a selection", 1, elems.length)
            assertEquals("Should have found reference to: " + elementName, elementName, elems[0].elementName)
            assertTrue(elems[0].exists())
            return elems[0]
        }
    }

    protected IJavaElement assertCodeSelect(CharSequence source, SourceRange targetRange, String elementName) {
        GroovyCompilationUnit gunit = addGroovySource(source, nextFileName())
        gunit.becomeWorkingCopy(null)

        IJavaElement[] elems = gunit.codeSelect(targetRange.getOffset(), targetRange.getLength())
        if (!elementName) {
            assertEquals(0, elems.length)
        } else {
            assertEquals("Should have found a selection", 1, elems.length)
            assertEquals("Should have found reference to: " + elementName, elementName, elems[0].elementName)
            assertTrue(elems[0].exists())
            return elems[0]
        }
    }

    private static Random salt = new Random(System.currentTimeMillis())

    protected static String nextFileName() {
        "Pogo${salt.nextInt(999999)}"
    }

    protected static void waitUntilIndexesReady() {
        // dummy query for waiting until the indexes are ready
        SearchEngine engine = new SearchEngine();
        IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
        try {
            engine.searchAllTypeNames(
                null,
                SearchPattern.R_EXACT_MATCH,
                '!@$#!@'.toCharArray(),
                SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE,
                IJavaSearchConstants.CLASS,
                scope,
                new TypeNameRequestor() {
                    @Override
                    public void acceptType(
                        int modifiers,
                        char[] packageName,
                        char[] simpleTypeName,
                        char[][] enclosingTypeNames,
                        String path) {}
                },
                IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
                null);
        } catch (CoreException e) {
        }
    }
}
