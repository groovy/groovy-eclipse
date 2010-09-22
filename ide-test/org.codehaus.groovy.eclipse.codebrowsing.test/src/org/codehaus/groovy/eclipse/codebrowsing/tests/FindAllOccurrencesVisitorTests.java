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
package org.codehaus.groovy.eclipse.codebrowsing.tests;

import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindAllOccurrencesVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * Tests that the {@link FindAllOccurrencesVisitor} finder is working properly.
 *
 * Note that there is a limitation in how {@link FindAllOccurrencesVisitor}
 * works. This is described in the comments of the class under test.
 * 
 * Not entirely happy with the test coverage here. We really should be testing
 * more
 *
 * @author andrew
 * @created May 12, 2010
 */
public class FindAllOccurrencesVisitorTests extends AbstractCheckerTests {

    public FindAllOccurrencesVisitorTests() {
        super(FindAllOccurrencesVisitorTests.class.getName());
    }

    public void testFindAllOccurrences1() throws Exception {
        String moduleText = "FOO + BAR // FOO + BAR";
        String exprText = "FOO + BAR";
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText));
    }

    public void testFindAllOccurrences2() throws Exception {
        String moduleText = "def x = FOO + BAR // FOO + BAR \nFOO + BAR";
        String exprText = "FOO + BAR";
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText), moduleText.lastIndexOf(exprText));
    }

    public void testFindAllOccurrences3() throws Exception {
        String moduleText = "class Foo { \ndef x = FOO + BAR // FOO + BAR \n }";
        String exprText = "FOO + BAR";
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText));
    }

    public void testFindAllOccurrences4() throws Exception {
        String moduleText = "class Foo { \ndef x = FOO + BAR // FOO + BAR\n def y = FOO + BAR }";
        String exprText = "FOO + BAR";
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText), moduleText.lastIndexOf(exprText));
    }

    public void testFindAllOccurrences5() throws Exception {
        String moduleText = "class Foo { \ndef x() {\n FOO + BAR // FOO + BAR \n } }";
        String exprText = "FOO + BAR";
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText));
    }

    public void testFindAllOccurrences6() throws Exception {
        String moduleText = "class Foo { \ndef x() { FOO + BAR // FOO + BAR\n}\n def y() {\n FOO + BAR\n} }";
        String exprText = "FOO + BAR";
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText), moduleText.lastIndexOf(exprText));
    }


    public void testFindAllOccurrences7() throws Exception {
        String moduleText = "class Foo { { FOO + BAR // FOO + BAR\n}\n {\n FOO + BAR\n} }";
        String exprText = "FOO + BAR";
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText), moduleText.lastIndexOf(exprText));
    }

    public void testFindAllOccurrences8() throws Exception {
        String moduleText = "class Foo { static FOO = 9 \n static BAR = 10 \n static { FOO + BAR // FOO + BAR \n } }";
        String exprText = "FOO + BAR";
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText));
    }

    public void testFindAllOccurrences9() throws Exception {
        String moduleText = "class Foo { static FOO = 9 \n static BAR = 10 \n static x = FOO + BAR // FOO + BAR \n }";
        String exprText = "FOO + BAR";
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText));
    }

    public void testFindAllOccurrences10() throws Exception {
        String moduleText = "class Foo { static FOO = 9 \n static BAR = 10 \n static x() { FOO + BAR // FOO + BAR \n } }";
        String exprText = "FOO + BAR";
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText));
    }

    public void testFindAllOccurrences11() throws Exception {
        String moduleText = "class Foo { static FOO = 9 \n static BAR = 10 \n static x() { def x = { FOO + BAR // FOO + BAR \n } } }";
        String exprText = "FOO + BAR";
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText));
    }

    public void testFindAllOccurrences12() throws Exception {
        String moduleText = "class Foo { static FOO = 9 \n static BAR = 10 \n def x() { schlameal ( FOO + BAR // FOO + BAR \n ) } }";
        String exprText = "FOO + BAR";
        assertOccurrences(exprText, moduleText, moduleText.indexOf(exprText));
    }

    public void testFindAllOccurrences13() throws Exception {
        String moduleText = "FOO + BAR + a + FOO + BAR + b + c + FOO + BAR";
        String exprText = "FOO + BAR";
        int first = moduleText.indexOf(exprText);
        int second = moduleText.indexOf(exprText, first + 2);
        int third = moduleText.indexOf(exprText, second + 2);
        assertOccurrences(exprText, moduleText, first, second, third);
    }

    public void testFindAllOccurrences14() throws Exception {
        String moduleText = "FOO.BAR.FOO.BAR + FOO.BAR.FOO.BAR";
        String exprText = "FOO.BAR";
        int first = moduleText.indexOf(exprText);
        int second = moduleText.indexOf(exprText, moduleText.indexOf('+'));
        assertOccurrences(exprText, moduleText, first, second);
    }

    public void testFindAllOccurrences15() throws Exception {
        String moduleText = "FOO.BAR.baz(FOO.BAR)";
        String exprText = "FOO.BAR";
        int first = moduleText.indexOf(exprText);
        int second = moduleText.lastIndexOf(exprText);
        assertOccurrences(exprText, moduleText, first, second);
    }

    public void testFindAllOccurrences16() throws Exception {
        String moduleText = "FOO.BAR(FOO.BAR)";
        String exprText = "FOO.BAR";
        int first = moduleText.lastIndexOf(exprText);
        assertOccurrences(exprText, moduleText, first);
    }

	public void testFindAllOccurrences17() throws Exception {
		String moduleText = "def BAR\nBAR(FOO.BAZ, FOO.BAZ)";
		String exprText = "FOO.BAZ";
		int first = moduleText.indexOf(exprText);
		int second = moduleText.lastIndexOf(exprText);
		assertOccurrences(exprText, moduleText, first, second);
	}

    public void testFindAllOccurrences18() throws Exception {
        String moduleText = "FOO.BAR";
        String exprText = "FOO";
        int first = moduleText.lastIndexOf(exprText);
        assertOccurrences(exprText, moduleText, first);
    }

    public void testFindAllOccurrences19() throws Exception {
        String moduleText = "FOO.BAR()";
        String exprText = "FOO";
        int first = moduleText.lastIndexOf(exprText);
        assertOccurrences(exprText, moduleText, first);
    }

    public void testFindAllOccurrences20() throws Exception {
        String moduleText = "FOO.BAR()";
        String exprText = "FOO";
        int first = moduleText.lastIndexOf(exprText);
        assertOccurrences(exprText, moduleText, first);
    }

    public void testFindAllOccurrences21() throws Exception {
        String moduleText = "FOO.BAR.baz(FOO?.BAR)";
        String exprText = "FOO.BAR";
        int first = moduleText.indexOf(exprText);
        assertOccurrences(exprText, moduleText, first);
    }

    public void testFindAllOccurrences21a() throws Exception {
        String moduleText = "FOO?.BAR.baz(FOO.BAR)";
        String exprText = "FOO?.BAR";
        int first = moduleText.indexOf(exprText);
        assertOccurrences(exprText, moduleText, first);
    }

    public void testFindAllOccurrences22() throws Exception {
        String moduleText = "FOO?.BAR.baz(FOO?.BAR)";
        String exprText = "FOO?.BAR";
        int first = moduleText.indexOf(exprText);
        int second = moduleText.lastIndexOf(exprText);
        assertOccurrences(exprText, moduleText, first, second);
    }

    private void assertOccurrences(String exprToFindText, String moduleText, int... startLocations) throws CoreException {
        IASTFragment exprToFind = getLastFragment(createModuleFromText(exprToFindText));
        ModuleNode module = createModuleFromText(moduleText);
        FindAllOccurrencesVisitor visitor = new FindAllOccurrencesVisitor(module);
        List<IASTFragment> foundExprs = visitor.findOccurrences(exprToFind);
        assertEquals(createMsg(foundExprs, startLocations, exprToFindText, moduleText), startLocations.length, foundExprs.size());
        for (int i = 0; i < startLocations.length; i++) {
            assertEquals(createMsg(foundExprs, startLocations, exprToFindText, moduleText), foundExprs.get(i).getStart(),
                    startLocations[i]);
        }
    }

    /**
     * @param foundExprs
     * @param startLocations
     * @param moduleText
     * @return
     */
    private String createMsg(List<IASTFragment> foundExprs, int[] startLocations, String exprToFindText, String moduleText) {
        StringBuilder sb = new StringBuilder();
        sb.append("Incorrect expressions found in:\n" + moduleText + "\n-----\nLooking for:\n" + exprToFindText
                + "\n------\nExpecting to find expressions starting at: " + Arrays.toString(startLocations)
                + "\nbut instead found expressions starting at: [ " + getStarts(foundExprs) + " ]");
        return sb.toString();
    }

    /**
     * @param foundExprs
     * @return
     */
    private String getStarts(List<IASTFragment> foundExprs) {
        StringBuilder sb = new StringBuilder();
        for (IASTFragment foundExpr : foundExprs) {
            sb.append(foundExpr.getStart()).append(", ");
        }
        if (sb.length() > 0) {
            sb.replace(sb.length() - 2, sb.length(), "");
        }
        return sb.toString();
    }
}
