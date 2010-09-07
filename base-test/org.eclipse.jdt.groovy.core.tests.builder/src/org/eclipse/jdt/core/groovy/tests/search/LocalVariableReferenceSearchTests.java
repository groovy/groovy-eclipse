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
import org.eclipse.jdt.core.groovy.tests.search.AbstractGroovySearchTest.MatchRegion;

import junit.framework.Test;

/**
 * @author Andrew Eisenberg
 * @created Sep 2, 2009
 *
 */
public class LocalVariableReferenceSearchTests extends AbstractGroovySearchTest {
    private static final String XXX = "xxx";
    public LocalVariableReferenceSearchTests(String name) {
        super(name);
    }
    
    public static Test suite() {
        return buildTestSuite(LocalVariableReferenceSearchTests.class);
    }

    public void testvarReference1() throws Exception {
        String contents = "def xxx";
        int nameStart = contents.indexOf(XXX);
        doTestForReferencesInScript(contents, createRegions(nameStart));
    }
    public void testvarReference2() throws Exception {
        String contents = "def xxx\nxxx";
        int nameStart = contents.indexOf(XXX);
        int nameStart2 = contents.indexOf(XXX, nameStart+1);
        doTestForReferencesInScript(contents, createRegions(nameStart, nameStart2));
    }
    public void testvarReference3() throws Exception {
        String contents = "def xxx() { \ndef xxx\n xxx\n xxx() }";
        int nameStart = contents.indexOf(XXX, contents.indexOf('('));
        int nameStart2 = contents.indexOf(XXX, nameStart+1);
        int nameStart3 = contents.indexOf(XXX, nameStart2+1);
        doTestForReferences(contents, 4, createRegions(nameStart, nameStart2, nameStart3));
    }
    public void testvarReference4() throws Exception {
        String contents = "def xxx(xxx) { \n xxx\n xxx() }";
        int nameStart = contents.indexOf(XXX, contents.indexOf('('));
        int nameStart2 = contents.indexOf(XXX, nameStart+1);
        int nameStart3 = contents.indexOf(XXX, nameStart2+1);
        doTestForReferences(contents, 4, createRegions(nameStart, nameStart2, nameStart3));
    }
    public void testvarReference5() throws Exception {
        String contents = "def xxx(int xxx) { \n xxx\n xxx() }";
        int nameStart = contents.indexOf(XXX, contents.indexOf('('));
        int nameStart2 = contents.indexOf(XXX, nameStart+1);
        int nameStart3 = contents.indexOf(XXX, nameStart2+1);
        doTestForReferences(contents, 4, createRegions(nameStart, nameStart2, nameStart3));
    }
    public void testvarReference6() throws Exception {
        String contents = "def xxx = {int xxx -> \n xxx\n xxx() }";
        int nameStart = contents.indexOf(XXX, contents.indexOf('{'));
        int nameStart2 = contents.indexOf(XXX, nameStart+1);
        int nameStart3 = contents.indexOf(XXX, nameStart2+1);
        doTestForReferencesInScript(contents, createRegions(nameStart, nameStart2, nameStart3));
    }
    public void testvarReference7() throws Exception {
        String contents = "def xxx = {int xxx \n xxx\n xxx() }";
        int nameStart = contents.indexOf(XXX, contents.indexOf('{'));
        int nameStart2 = contents.indexOf(XXX, nameStart+1);
        int nameStart3 = contents.indexOf(XXX, nameStart2+1);
        doTestForReferencesInScript(contents, createRegions(nameStart, nameStart2, nameStart3));
    }
    public void testvarReference8() throws Exception {
        String contents = "for (xxx in 0..7) \n { xxx }";
        int nameStart = contents.indexOf(XXX, contents.indexOf('('));
        int nameStart2 = contents.indexOf(XXX, nameStart+1);
        doTestForReferencesInScript(contents, createRegions(nameStart, nameStart2));
    }
    public void testvarReference9() throws Exception {
        String contents = "def x1(xxx) { xxx }\ndef x2(xxx) { xxx }";
        int nameStart = contents.indexOf(XXX, contents.indexOf('}'));
        int nameStart2 = contents.indexOf(XXX, nameStart+1);
        doTestForReferences(contents, 5, createRegions(nameStart, nameStart2));
    }
    public void testvarReference10() throws Exception {
        String contents = "class First {\n def xxx \ndef x2(xxx) { xxx } }";
        int nameStart = contents.indexOf(XXX, contents.indexOf('('));
        int nameStart2 = contents.indexOf(XXX, nameStart+1);
        doTestForReferences(contents, 1, createRegions(nameStart, nameStart2));
    }
    public void testvarReferenceInGString1() throws Exception {
        String contents = "def xxx\n\"${xxx}\"";
        int nameStart = contents.indexOf(XXX);
        int nameStart2 = contents.indexOf(XXX, nameStart+1);
        doTestForReferences(contents, 3, createRegions(nameStart, nameStart2));
    }
    public void testvarReferenceInGString2() throws Exception {
        String contents = "def xxx\n\"${xxx.toString()}\"";
        int nameStart = contents.indexOf(XXX);
        int nameStart2 = contents.indexOf(XXX, nameStart+1);
        doTestForReferences(contents, 3, createRegions(nameStart, nameStart2));
    }
    public void testvarReferenceInGString3() throws Exception {
        String contents = "def xxx\n\"${blah(xxx)}\"";
        int nameStart = contents.indexOf(XXX);
        int nameStart2 = contents.indexOf(XXX, nameStart+1);
        doTestForReferences(contents, 3, createRegions(nameStart, nameStart2));
    }
    public void testvarReferenceInGString4() throws Exception {
        String contents = "def xxx\n\"${xxx} \"";
        int nameStart = contents.indexOf(XXX);
        int nameStart2 = contents.indexOf(XXX, nameStart+1);
        doTestForReferences(contents, 3, createRegions(nameStart, nameStart2));
    }
    public void testvarReferenceInGString5() throws Exception {
        String contents = "def xxx\n\"${xxx }\"";
        int nameStart = contents.indexOf(XXX);
        int nameStart2 = contents.indexOf(XXX, nameStart+1);
        doTestForReferences(contents, 3, createRegions(nameStart, nameStart2));
    }
    
    private MatchRegion[] createRegions(int...nameStarts) {
        MatchRegion[] regions = new MatchRegion[nameStarts.length];
        for (int i = 0; i < nameStarts.length; i++) {
            regions[i] = new MatchRegion(nameStarts[i], XXX.length());
        }
        return regions;
    }
    
    private void doTestForReferencesInScript(String contents, MatchRegion[] matchLocations) throws JavaModelException {
        doTestForVarReferences(contents, 3, XXX, matchLocations[0].offset, matchLocations);
    }
    private void doTestForReferences(String contents, int locationInParent, MatchRegion[] matchLocations) throws JavaModelException {
        doTestForVarReferences(contents, locationInParent, XXX, matchLocations[0].offset, matchLocations);
    }
}
