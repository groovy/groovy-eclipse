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

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;

import org.junit.Test;

/**
 * Tests for synthetic accessor type inferencing.
 */
public final class SyntheticAccessorInferencingTests extends InferencingTestSuite {

    private void assertKnown(String source, String target, String type) {
        assertDeclaringType(source, target, type);
    }

    private void assertUnknown(String contents, String target) {
        int offset = contents.indexOf(target);
        assertUnknownConfidence(contents, offset, offset + target.length());
    }

    //--------------------------------------------------------------------------

    @Test
    public void testSyntheticAccessors1() {
        String contents =
            "class Foo {\n" +
            "  boolean bar\n" +
            "  void method() {\n" +
            "    isBar()\n" +
            "    getBar()\n" +
            "    setBar(true)\n" +
            "  }\n" +
            "}\n";

        assertKnown(contents, "isBar",  "Foo");
        assertKnown(contents, "getBar", "Foo");
        assertKnown(contents, "setBar", "Foo");
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

        if (!isAtLeastGroovy(40)) {
            assertUnknown(contents, "isBar");
        } else {
            assertKnown(contents, "isBar", "Foo");
        }
        assertKnown(contents, "getBar", "Foo");
        assertKnown(contents, "setBar", "Foo");
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

        assertUnknown(contents, "isBar");
        assertKnown(contents, "getBar", "Foo");
        assertKnown(contents, "setBar", "Foo");
    }

    @Test
    public void testSyntheticAccessors4() {
        String contents =
            "class Foo {\n" +
            "  // yes underlines and no content assist\n" +
            "  String  getProperty1(param) {}\n" +
            "  void    getProperty2() {}\n" +
            "  boolean isProperty3(param) {}\n" +
            "  String  isProperty4() {}\n" +
            "  void    isProperty5() {}\n" +
            "  void    setProperty6() {}\n" +
            "  void    setProperty7(param1, param2) {}\n" +
            "  \n" +
            "  // no underlines and yes content assist\n" +
            "  void    setProperty1a(... params) {}\n" +
            "  void    setProperty2a(param) {}\n" +
            "  String  setProperty3a(param) {}\n" +
            "  def     setProperty4a(param) {}\n" +
            "  def     getProperty5a() {}\n" +
            "  boolean isProperty6a() {}\n" +
            "  \n" +
            "  void method() {\n" +
            "    property1\n" +
            "    property2\n" +
            "    property3\n" +
            "    property4\n" +
            "    property5\n" +
            "    property6 = null\n" +
            "    property7 = null\n" +
            "    \n" +
            "    property1a = null\n" +
            "    property2a = null\n" +
            "    property3a = null\n" +
            "    property4a = null\n" +
            "    property5a\n" +
            "    property6a\n" +
            "  }\n" +
            "}\n";

        assertUnknown(contents, "property1");
        assertUnknown(contents, "property2");
        assertUnknown(contents, "property3");
        assertUnknown(contents, "property4");
        assertUnknown(contents, "property5");
        assertUnknown(contents, "property6");
        assertUnknown(contents, "property7");

        assertKnown(contents, "property1a", "Foo");
        assertKnown(contents, "property2a", "Foo");
        assertKnown(contents, "property3a", "Foo");
        assertKnown(contents, "property4a", "Foo");
        assertKnown(contents, "property5a", "Foo");
        assertKnown(contents, "property6a", "Foo");
    }

    @Test
    public void testSyntheticAccessors5() {
        String contents =
            "class Cat {\n" +
            "  // yes underlines and no content assist\n" +
            "  static String  getPropertyCat1(Search self, param) {}\n" +
            "  static void    getPropertyCat2(Search self) {}\n" +
            "  static void    setPropertyCat3(Search self) {}\n" +
            "  static void    setPropertyCat4(Search self, ... params) {}\n" +
            "  static void    setPropertyCat5(Search self, param1, param2) {}\n" +
            "  static boolean isPropertyCat6(Search self, param) {}\n" +
            "  static Boolean isPropertyCat7(Search self) {}\n" +
            "  static String  isPropertyCat8(Search self) {}\n" +
            "  static void    isPropertyCat9(Search self) {}\n" +
            "  static def     isPropertyCat10(Search self) {}\n" +
            "  \n" +
            "  // no underlines and yes content assist\n" +
            "  static def     setPropertyCat1a(Search self, param) {}\n" +
            "  static void    setPropertyCat2a(Search self, param) {}\n" +
            "  static String  setPropertyCat3a(Search self, param) {}\n" + // https://github.com/groovy/groovy-eclipse/issues/1025
            "  static def     getPropertyCat4a(Search self) {}\n" +
            "}\n" +
            "use (Cat) {\n" +
            "  propertyCat1\n" +
            "  propertyCat2\n" +
            "  propertyCat3\n" +
            "  propertyCat4\n" +
            "  propertyCat5\n" +
            "  propertyCat6\n" +
            "  propertyCat7\n" +
            "  propertyCat8\n" +
            "  propertyCat9\n" +
            "  propertyCat10\n" +
            "  \n" +
            "  propertyCat1a = null\n" +
            "  propertyCat2a = null\n" +
            "  propertyCat3a = null\n" +
            "  propertyCat4a\n" +
            "}";

        assertUnknown(contents, "propertyCat1");
        assertUnknown(contents, "propertyCat2");
        assertUnknown(contents, "propertyCat3");
        assertUnknown(contents, "propertyCat4");
        assertUnknown(contents, "propertyCat5");
        assertUnknown(contents, "propertyCat6");
        assertUnknown(contents, "propertyCat7");
        assertUnknown(contents, "propertyCat8");
        assertUnknown(contents, "propertyCat9");
        assertUnknown(contents, "propertyCat10");

        assertKnown(contents, "propertyCat1a", "Cat");
        assertKnown(contents, "propertyCat2a", "Cat");
        assertKnown(contents, "propertyCat3a", "Cat");
        assertKnown(contents, "propertyCat4a", "Cat");
    }
}
