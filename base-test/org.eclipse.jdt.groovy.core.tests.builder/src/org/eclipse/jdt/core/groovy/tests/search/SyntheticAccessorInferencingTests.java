/*
 * Copyright 2009-2019 the original author or authors.
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

/**
 * Tests for synthetic accessor type inferencing.
 */
public final class SyntheticAccessorInferencingTests extends InferencingTestSuite {

    @Test
    public void testSyntheticAccessors1() {
        String contents =
            "class Foo {\n" +
            "  boolean bar\n" +
            "  void method() {\n" +
            "    isBar()\n" +
            "    getBar()\n" +
            "    setBar(null)\n" +
            "  }\n" +
            "}\n";

        shouldBeKnown(contents, "isBar",  "Foo");
        shouldBeKnown(contents, "getBar", "Foo");
        shouldBeKnown(contents, "setBar", "Foo");
    }

    @Test
    public void testSyntheticAccessors2() {
        String contents =
            "class Foo {\n" +
            "  Boolean bar\n" +
            "  void method() {\n" +
            "    isBar()\n" +
            "    getBar()\n" +
            "    setBar(null)\n" +
            "  }\n" +
            "}\n";

        shouldBeUnknown(contents, "isBar");
        shouldBeKnown(contents, "getBar", "Foo");
        shouldBeKnown(contents, "setBar", "Foo");
    }

    @Test
    public void testSyntheticAccessors3() {
        String contents =
            "class Foo {\n" +
            "  def bar\n" +
            "  void method() {\n" +
            "    isBar()\n" +
            "    getBar()\n" +
            "    setBar(null)\n" +
            "  }\n" +
            "}\n";

        shouldBeUnknown(contents, "isBar");
        shouldBeKnown(contents, "getBar", "Foo");
        shouldBeKnown(contents, "setBar", "Foo");
    }

    @Test
    public void testSyntheticAccessors4() {
        String contents =
            "// yes underlines and no content assist\n" +
            "String getProperty1(param){}\n" +
            "void getProperty2(){}\n" +
            "boolean isProperty3(param){}\n" +
            "String isProperty4(){}\n" +
            "void isProperty5(){}\n" +
            "void setProperty6(){}\n" +
            "void setProperty7(param1, param2){}\n" +
            "String setProperty8(param){}\n" +
            "\n" +
            "// no underlines and yes content assist\n" +
            "def setProperty1a(param) {}\n" +
            "void setProperty2a(param) {}\n" +
            "def getProperty3a() {}\n" +
            "boolean isProperty4a() {}\n" +
            "\n" +
            "property1\n" +
            "property2\n" +
            "property3\n" +
            "property4\n" +
            "property5\n" +
            "property6\n" +
            "property7\n" +
            "property8\n" +
            "\n" +
            "property1a = 1\n" +
            "property2a = 2\n" +
            "property3a\n" +
            "property4a \n" +
            "\n" +
            "class Cat {\n" +
            "    // yes underlines and no content assist\n" +
            "    static String getPropertyCat1(Search self, param){}\n" +
            "    static void getPropertyCat2(Search self){}\n" +
            "    static boolean isPropertyCat3(Search self, param){}\n" +
            "    static String isPropertyCat4(Search self){}\n" +
            "    static void isPropertyCat5(Search self){}\n" +
            "    static void setPropertyCat6(Search self){}\n" +
            "    static void setPropertyCat7(Search self, param1, param2){}\n" +
            "    static String setPropertyCat8(Search self, param){}\n" +
            "    static def isPropertyCat9(Search self) {}\n" +
            "    def getPropertyCat10(File self) {}\n" +
            "    \n" +
            "    // no underlines and yes content assist\n" +
            "    static def setPropertyCat1a(Search self, param) {}\n" +
            "    static void setPropertyCat2a(Search self, param) {}\n" +
            "    static def getPropertyCat3a(Search self) {}\n" +
            "}\n" +
            "use (Cat) {\n" +
            "    propertyCat1\n" +
            "    propertyCat2\n" +
            "    propertyCat3 \n" +
            "    propertyCat4\n" +
            "    propertyCat5\n" +
            "    propertyCat6\n" +
            "    propertyCat7\n" +
            "    propertyCat8\n" +
            "    propertyCat9\n" +
            "    \n" +
            "    propertyCat1a\n" +
            "    propertyCat2a\n" +
            "    propertyCat3a\n" +
            "}";

        shouldBeUnknown(contents, "property1");
        shouldBeUnknown(contents, "property2");
        shouldBeUnknown(contents, "property3");
        shouldBeUnknown(contents, "property4");
        shouldBeUnknown(contents, "property5");
        shouldBeUnknown(contents, "property6");
        shouldBeUnknown(contents, "property7");
        shouldBeUnknown(contents, "property8");
        shouldBeUnknown(contents, "propertyCat1");
        shouldBeUnknown(contents, "propertyCat2");
        shouldBeUnknown(contents, "propertyCat3");
        shouldBeUnknown(contents, "propertyCat4");
        shouldBeUnknown(contents, "propertyCat5");
        shouldBeUnknown(contents, "propertyCat6");
        shouldBeUnknown(contents, "propertyCat7");
        shouldBeUnknown(contents, "propertyCat8");
        shouldBeUnknown(contents, "propertyCat9");

        shouldBeKnown(contents, "property1a", DEFAULT_UNIT_NAME);
        shouldBeKnown(contents, "property2a", DEFAULT_UNIT_NAME);
        shouldBeKnown(contents, "property3a", DEFAULT_UNIT_NAME);
        shouldBeKnown(contents, "property4a", DEFAULT_UNIT_NAME);

        shouldBeKnown(contents, "propertyCat1a", "Cat");
        shouldBeKnown(contents, "propertyCat2a", "Cat");
        shouldBeKnown(contents, "propertyCat3a", "Cat");
    }

    //--------------------------------------------------------------------------

    private void shouldBeUnknown(String contents, String var) {
        int start = contents.indexOf(var);
        int end = start + var.length();
        assertUnknownConfidence(contents, start, end);
    }

    private void shouldBeKnown(String contents, String var, String type) {
        int start = contents.lastIndexOf(var);
        int end = start + var.length();
        assertDeclaringType(contents, start, end, type);
    }
}
