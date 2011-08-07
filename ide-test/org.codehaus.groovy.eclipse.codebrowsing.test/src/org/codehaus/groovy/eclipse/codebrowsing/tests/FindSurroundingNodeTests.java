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

import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;

/**
 *
 * @author Andrew Eisenberg
 * @created May 10, 2010
 */
public class FindSurroundingNodeTests extends BrowsingTestCase {

    public FindSurroundingNodeTests() {
        super(FindSurroundingNodeTests.class.getName());
    }

    public void testFindSurrounding1() throws Exception {
        String contents = "import java.util.List\n class Clazz { }";
        Region initialRegion = new Region(contents.indexOf('C'), 0);
        Region expectedRegion = new Region(contents.indexOf('c'), "class Clazz { }".length());
        checkRegion(contents, initialRegion, expectedRegion);
    }
    public void testFindSurrounding2() throws Exception {
        String contents = "import org.codehaus.groovy.ast.ASTNode\n class Clazz { }";
        Region initialRegion = new Region(contents.indexOf('A'), 0);
        Region expectedRegion = new Region(0, "import org.codehaus.groovy.ast.ASTNode".length());
        checkRegion(contents, initialRegion, expectedRegion);
    }
    public void testFindSurrounding3() throws Exception {
        String contents = "import java.util.List\n class Clazz { def method() { def x\n}}";
        Region initialRegion = new Region(contents.indexOf('x'), 0);
        Region expectedRegion = new Region(contents.indexOf("x"), "x".length());
        GroovyCompilationUnit unit = checkRegion(contents, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(contents.indexOf("def x"), "def x".length());
        unit = checkRegion(contents, unit, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(contents.indexOf("{ def x\n}"), "{ def x\n}".length());
        unit = checkRegion(contents, unit, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(contents.indexOf("def method() { def x\n}"), "def method() { def x\n}".length());
        unit = checkRegion(contents, unit, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(contents.indexOf("class Clazz { def method() { def x\n}}"), "class Clazz { def method() { def x\n}}".length());
        unit = checkRegion(contents, unit, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(0, contents.length());
        checkRegion(contents, unit, initialRegion, expectedRegion);
    }


    public void testFindSurrounding4() throws Exception {
        String contents = "if (true) { \n def x\n }else { def y }";
        Region initialRegion = new Region(contents.indexOf('x'), 0);
        Region expectedRegion = new Region(contents.indexOf("x"), "x".length());
        GroovyCompilationUnit unit = checkRegion(contents, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(contents.indexOf("def x"), "def x".length());
        unit = checkRegion(contents, unit, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(contents.indexOf("{ \n def x\n }"), "{ \n def x\n }".length());
        unit = checkRegion(contents, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(0, contents.length());
        unit = checkRegion(contents, initialRegion, expectedRegion);
    }

    // Mysteriously failing on build server.
    public void _testFindSurrounding5() throws Exception {
        String contents = "foo() .foo()";
        Region initialRegion = new Region(contents.indexOf('f'), 1);
        Region expectedRegion = new Region(contents.indexOf("foo"), "foo".length());
        GroovyCompilationUnit unit = checkRegion(contents, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(contents.indexOf("foo() "), "foo() ".length());
        unit = checkRegion(contents, unit, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(0, contents.length());
        unit = checkRegion(contents, initialRegion, expectedRegion);
    }

    public void testFindSurrounding5a() throws Exception {
        String contents = "foo() .foo()";
        Region initialRegion = new Region(contents.lastIndexOf('f'), 1);
        Region expectedRegion = new Region(contents.lastIndexOf("foo"), "foo".length());
        GroovyCompilationUnit unit = checkRegion(contents, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(contents.lastIndexOf("foo()"), "foo()".length());
        unit = checkRegion(contents, unit, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(0, contents.length());
        unit = checkRegion(contents, initialRegion, expectedRegion);
    }

    public void testFindSurrounding6() throws Exception {
        String contents = "while(foo.bar) { }";
        Region initialRegion = new Region(contents.indexOf('f'), 0);
        Region expectedRegion = new Region(contents.indexOf("foo"), "foo".length());
        GroovyCompilationUnit unit = checkRegion(contents, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(contents.indexOf("foo.bar"), "foo.bar".length());
        unit = checkRegion(contents, unit, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(0, contents.length());
        unit = checkRegion(contents, initialRegion, expectedRegion);
    }

    public void testFindSurrounding7() throws Exception {
        String contents = "while(foo.bar) { }\nclass A { \ndef x = 7 + 9}";
        Region initialRegion = new Region(contents.indexOf('7'), 0);
        Region expectedRegion = new Region(contents.indexOf("7"), "7".length());
        GroovyCompilationUnit unit = checkRegion(contents, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(contents.indexOf("7 + 9"), "7 + 9".length());
        unit = checkRegion(contents, unit, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(contents.indexOf("def x = 7 + 9"), "def x = 7 + 9".length());
        unit = checkRegion(contents, unit, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(contents.indexOf("class A { \ndef x = 7 + 9}"), "class A { \ndef x = 7 + 9}".length());
        unit = checkRegion(contents, unit, initialRegion, expectedRegion);

        initialRegion = expectedRegion;
        expectedRegion = new Region(0, contents.length());
        unit = checkRegion(contents, initialRegion, expectedRegion);
    }

    private GroovyCompilationUnit checkRegion(String contents, Region initialRegion,
            Region expectedRegion) throws Exception {
        GroovyCompilationUnit unit = getCompilationUnitFor(contents);
        return checkRegion(contents, unit, initialRegion, expectedRegion);
    }

    private GroovyCompilationUnit checkRegion(String contents, GroovyCompilationUnit unit,
            Region initialRegion, Region expectedRegion) {
        FindSurroundingNode finder = new FindSurroundingNode(initialRegion);
        IASTFragment result = finder.doVisitSurroundingNode(unit.getModuleNode());
        Region actualRegion = new Region(result);
        assertEquals(
                "Expected text = |"+getRegionText(expectedRegion,contents)+"|\n" +
        		"Actual text = |"+getRegionText(actualRegion,contents)+"|\n",
                expectedRegion, actualRegion);
        return unit;
    }

    private String getRegionText(Region region, String sourceString) {
        return sourceString.substring(region.getOffset(),region.getEnd());
    }

}
