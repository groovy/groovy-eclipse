/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.groovy.tests.search;

import org.junit.Test;

public final class LocalVariableReferenceSearchTests extends SearchTestSuite {

    @Test
    public void testVarReference1() throws Exception {
        String contents = "def xxx";
        int nameStart = contents.indexOf("xxx");
        doTestForReferencesInScript(contents, createRegions(nameStart));
    }

    @Test
    public void testVarReference2() throws Exception {
        String contents = "def xxx\nxxx";
        int nameStart = contents.indexOf("xxx");
        int nameStart2 = contents.indexOf("xxx", nameStart + 1);
        doTestForReferencesInScript(contents, createRegions(nameStart, nameStart2));
    }

    @Test
    public void testVarReference3() throws Exception {
        String contents = "def xxx() {\n def xxx\n xxx\n xxx()\n}\n";
        int nameStart = contents.indexOf("xxx", contents.indexOf('('));
        int nameStart2 = contents.indexOf("xxx", nameStart + 1);
        int nameStart3 = contents.indexOf("xxx", nameStart2 + 1);
        doTestForReferences(contents, 4, createRegions(nameStart, nameStart2, nameStart3));
    }

    @Test
    public void testVarReference4() throws Exception {
        String contents = "def xxx(xxx) {\n xxx\n xxx()\n}\n";
        int nameStart = contents.indexOf("xxx", contents.indexOf('('));
        int nameStart2 = contents.indexOf("xxx", nameStart + 1);
        int nameStart3 = contents.indexOf("xxx", nameStart2 + 1);
        doTestForReferences(contents, 4, createRegions(nameStart, nameStart2, nameStart3));
    }

    @Test
    public void testVarReference5() throws Exception {
        String contents = "def xxx(int xxx) {\n xxx\n xxx()\n}\n";
        int nameStart = contents.indexOf("xxx", contents.indexOf('('));
        int nameStart2 = contents.indexOf("xxx", nameStart + 1);
        int nameStart3 = contents.indexOf("xxx", nameStart2 + 1);
        doTestForReferences(contents, 4, createRegions(nameStart, nameStart2, nameStart3));
    }

    @Test
    public void testVarReference6() throws Exception {
        String contents = "def xxx = { int xxx ->\n xxx\n xxx()\n}\n";
        int nameStart = contents.indexOf("xxx", contents.indexOf('{'));
        int nameStart2 = contents.indexOf("xxx", nameStart + 1);
        int nameStart3 = contents.indexOf("xxx", nameStart2 + 1);
        doTestForReferencesInScript(contents, createRegions(nameStart, nameStart2, nameStart3));
    }

    @Test
    public void testVarReference7() throws Exception {
        String contents = "def xxx = {\n int xxx\n xxx\n xxx()\n}\n";
        int nameStart = contents.indexOf("xxx", contents.indexOf('{'));
        int nameStart2 = contents.indexOf("xxx", nameStart + 1);
        int nameStart3 = contents.indexOf("xxx", nameStart2 + 1);
        doTestForReferencesInScript(contents, createRegions(nameStart, nameStart2, nameStart3));
    }

    @Test
    public void testVarReference8() throws Exception {
        String contents = "for (xxx in 0..7)\n {\n xxx\n}\n";
        int nameStart = contents.indexOf("xxx", contents.indexOf('('));
        int nameStart2 = contents.indexOf("xxx", nameStart + 1);
        doTestForReferencesInScript(contents, createRegions(nameStart, nameStart2));
    }

    @Test
    public void testVarReference9() throws Exception {
        String contents = "def x1(xxx) {\n xxx\n}\ndef x2(xxx) {\n xxx\n}\n";
        int nameStart = contents.indexOf("xxx", contents.indexOf('}'));
        int nameStart2 = contents.indexOf("xxx", nameStart + 1);
        doTestForReferences(contents, 5, createRegions(nameStart, nameStart2));
    }

    @Test
    public void testVarReference10() throws Exception {
        String contents = "class First {\n def xxx\n def x2(xxx) {\n xxx\n}\n}\n";
        int nameStart = contents.indexOf("xxx", contents.indexOf('('));
        int nameStart2 = contents.indexOf("xxx", nameStart + 1);
        doTestForReferences(contents, 1, createRegions(nameStart, nameStart2));
    }

    @Test
    public void testVarReferenceInGString1() throws Exception {
        String contents = "def xxx\n\"${xxx}\"";
        int nameStart = contents.indexOf("xxx");
        int nameStart2 = contents.indexOf("xxx", nameStart + 1);
        doTestForReferences(contents, 3, createRegions(nameStart, nameStart2));
    }

    @Test
    public void testVarReferenceInGString2() throws Exception {
        String contents = "def xxx\n\"${xxx.toString()}\"";
        int nameStart = contents.indexOf("xxx");
        int nameStart2 = contents.indexOf("xxx", nameStart + 1);
        doTestForReferences(contents, 3, createRegions(nameStart, nameStart2));
    }

    @Test
    public void testVarReferenceInGString3() throws Exception {
        String contents = "def xxx\n\"${blah(xxx)}\"";
        int nameStart = contents.indexOf("xxx");
        int nameStart2 = contents.indexOf("xxx", nameStart + 1);
        doTestForReferences(contents, 3, createRegions(nameStart, nameStart2));
    }

    @Test
    public void testVarReferenceInGString4() throws Exception {
        String contents = "def xxx\n\"${xxx} \"";
        int nameStart = contents.indexOf("xxx");
        int nameStart2 = contents.indexOf("xxx", nameStart + 1);
        doTestForReferences(contents, 3, createRegions(nameStart, nameStart2));
    }

    @Test
    public void testVarReferenceInGString5() throws Exception {
        String contents = "def xxx\n\"${xxx\t}\"";
        int nameStart = contents.indexOf("xxx");
        int nameStart2 = contents.indexOf("xxx", nameStart + 1);
        doTestForReferences(contents, 3, createRegions(nameStart, nameStart2));
    }

    //--------------------------------------------------------------------------

    private MatchRegion[] createRegions(int... nameStarts) {
        MatchRegion[] regions = new MatchRegion[nameStarts.length];
        for (int i = 0, n = nameStarts.length; i < n; i += 1) {
            regions[i] = new MatchRegion(nameStarts[i], "xxx".length());
        }
        return regions;
    }

    private void doTestForReferencesInScript(String contents, MatchRegion[] matchLocations) throws Exception {
        doTestForVarReferences(contents, 3, matchLocations[0].offset, matchLocations);
    }

    private void doTestForReferences(String contents, int locationInParent, MatchRegion[] matchLocations) throws Exception {
        doTestForVarReferences(contents, locationInParent, matchLocations[0].offset, matchLocations);
    }
}
