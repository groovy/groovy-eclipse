/*
 * Copyright 2009-2017 the original author or authors.
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

import org.junit.Test

final class CodeSelectPropertiesTests extends BrowsingTestCase {

    @Test
    void testGettersAndField1() {
        addGroovySource(
            "class Other {\n" +
            "  String xxx\n" +
            "  public getXxx() { xxx }\n" +
            "}",
        "Other")

        String contents = "new Other().xxx"
        String toFind = "xxx"
        String elementName = "getXxx"
        assertCodeSelect([contents], toFind, elementName)

    }

    @Test
    void testGettersAndField2() {
        addGroovySource(
            "class Other {\n" +
            "  String xxx\n" +
            "  public getXxx() { xxx }\n" +
            "}",
        "Other")

        String contents = "new Other().getXxx()"
        String toFind = "getXxx"
        String elementName = toFind
        assertCodeSelect([contents], toFind, elementName)
    }

    @Test
    void testGettersAndField3() {
        addGroovySource(
            "class Other {\n" +
            "  String xxx\n" +
            "}",
        "Other")

        String contents = "new Other().getXxx()"
        String toFind = "getXxx"
        String elementName = "xxx"
        assertCodeSelect([contents], toFind, elementName)
    }

    @Test
    void testGettersAndField4() {
        addGroovySource(
            "class Other {\n" +
            "  public getXxx() { xxx }\n" +
            "}",
        "Other")

        String contents = "new Other().xxx"
        String toFind = "xxx"
        String elementName = "getXxx"
        assertCodeSelect([contents], toFind, elementName)
    }

    @Test
    void testGettersAndField5() {
        String contents =
            "class Other {\n" +
            "  String xxx\n" +
            "  public getXxx() { xxx }\n" +
            "}\n" +
            "new Other().xxx"
        String toFind = "xxx"
        String elementName = "getXxx"
        assertCodeSelect([contents], toFind, elementName)
    }

    @Test
    void testGettersAndField6() {
        String contents =
            "class Other {\n" +
            "  String xxx\n" +
            "  public getXxx() { xxx }\n" +
            "}\n" +
            "new Other().getXxx"
        String toFind = "getXxx"
        String elementName = toFind
        assertCodeSelect([contents], toFind, elementName)
    }

    @Test
    void testGettersAndField7() {
        String contents =
            "class Other {\n" +
            "  public getXxx() { xxx }\n" +
            "}\n" +
            "new Other().xxx"
        String toFind = "xxx"
        String elementName = "getXxx"
        assertCodeSelect([contents], toFind, elementName)
    }

    @Test
    void testGettersAndField8() {
        String contents =
            "class Other {\n" +
            "  String xxx\n" +
            "}\n" +
            "new Other().getXxx"
        String toFind = "getXxx"
        String elementName = "xxx"
        assertCodeSelect([contents], toFind, elementName)
    }

    @Test // GRECLIPSE-1162
    void testIsGetter1() {
        String contents =
            "class Other {\n" +
            "  boolean xxx\n" +
            "}\n" +
            "new Other().isXxx"
        String toFind = "isXxx"
        String elementName = "xxx"
        assertCodeSelect([contents], toFind, elementName)
    }
}
