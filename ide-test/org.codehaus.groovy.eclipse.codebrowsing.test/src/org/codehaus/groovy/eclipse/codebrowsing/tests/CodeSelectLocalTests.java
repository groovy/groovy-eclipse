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
package org.codehaus.groovy.eclipse.codebrowsing.tests;

import static java.util.Arrays.asList;

/**
 * @author Andrew Eisenberg
 * @created Jul 26, 2011
 */
public final class CodeSelectLocalTests extends BrowsingTestCase {

    public static junit.framework.Test suite() {
        return newTestSuite(CodeSelectLocalTests.class);
    }

    public void testLocalVar1() throws Exception {
        assertCodeSelect(asList("def xxx(xxx) { xxx }"), "xxx");
    }

    public void testLocalVar2() throws Exception {
        assertCodeSelect(asList("def xxx(xxx) { \"${xxx}\" }"), "xxx");
    }

    public void testLocalVar3() throws Exception {
        assertCodeSelect(asList("def xxx = { xxx -> \"${xxx}\" }"), "xxx");
    }

    public void testLocalVar4() throws Exception {
        String contents = "def (xxx, yyy) = []\nxxx\nyyy";
        assertCodeSelect(asList(contents), "xxx");
        assertCodeSelect(asList(contents), "yyy");
    }

    // GRECLIPSE-1330
    public void testLocalVarInGString1() throws Exception {
        String contents = "def i\n\"$i\"";
        assertCodeSelect(asList(contents), "i");
    }

    // GRECLIPSE-1330
    public void testLocalVarInGString2() throws Exception {
        String contents = "def i\n\"$i\"";
        assertCodeSelect(asList(contents), "$i", "i");
    }

    // GRECLIPSE-1330
    public void testLocalVarInGString3() throws Exception {
        String contents = "def i\n\"${i}\"";
        assertCodeSelect(asList(contents), "i");
    }
}
