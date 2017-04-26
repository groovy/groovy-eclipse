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
package org.eclipse.jdt.core.groovy.tests.search;

public final class ArrayInferencingTests extends AbstractInferencingTest {

    public static junit.framework.Test suite() {
        return buildTestSuite(ArrayInferencingTests.class);
    }

    public ArrayInferencingTests(String name) {
        super(name);
    }

    private void assertExprType(String source, String target, String type) {
        final int offset = source.lastIndexOf(target);
        assertType(source, offset, offset + target.length(), type);
    }

    public void testArray1() {
        String contents = "def x = [\"1\", \"2\"] as CharSequence[]; x";
        assertExprType(contents, "x", "java.lang.CharSequence[]");
    }

    public void testArray2() {
        String contents = "def x = [\"1\", \"2\"] as CharSequence[]; x[0]";
        assertExprType(contents, "x[0]", "java.lang.CharSequence");
    }

    public void testArray3() {
        String contents = "def x = [\"1\", \"2\"] as CharSequence[]; x[0].length";
        assertExprType(contents, "length", "java.lang.Integer");
    }

    public void testArray4() {
        String contents = "int i = 0; def x = [\"1\", \"2\"] as CharSequence[]; x[i]";
        assertExprType(contents, "x[i]", "java.lang.CharSequence");
    }

    public void testArray5() {
        String contents = "int i = 0; def x = [\"1\", \"2\"] as CharSequence[]; x[i].length";
        assertExprType(contents, "length", "java.lang.Integer");
    }

    public void testArrayProperty1() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().xx";
        assertExprType(contents, "xx", "XX[]");
    }

    public void testArrayProperty2() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().xx[0].yy";
        assertExprType(contents, "yy", "XX");
    }

    public void testArrayProperty3() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().xx[new XX()].yy";
        String toFind = "yy";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertUnknownConfidence(contents, start, end, "XX", false);
    }

    public void testArrayProperty4() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().xx[0].yy";
        assertExprType(contents, "yy", "XX");
    }

    public void testArrayProperty5() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().xx[0].xx[9].yy";
        assertExprType(contents, "yy", "XX");
    }

    public void testArrayProperty6() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().getXx()[0].xx[9].yy";
        assertExprType(contents, "yy", "XX");
    }

    public void testArrayProperty7() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().getXx()[0].getYy()";
        assertExprType(contents, "getYy", "XX");
    }

    public void testArrayProperty8() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().getXx()";
        assertExprType(contents, "getXx", "XX[]");
    }

    public void testArrayProperty9() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().getXx()[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx";
        assertExprType(contents, "xx", "XX[]");
    }

    public void testArrayProperty10() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().getYy().getYy().getYy().getYy().getYy().getYy().getYy().getYy().getXx()[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx";
        assertExprType(contents, "xx", "XX[]");
    }

    public void testForLoop1() {
        String contents =
            "Integer[] a = [1, 2, 3]\n" +
            "for (int i = 0; i < a.length; i += 1) {\n" +
            "  def x = a[i]\n" +
            "}";
        assertExprType(contents, "x", "java.lang.Integer");
    }

    public void testForLoop2() {
        String contents =
            "Integer[] a = [1, 2, 3]\n" +
            "for (def x : a) {\n" +
            "  x\n" +
            "}";
        assertExprType(contents, "x", "java.lang.Integer");
    }

    public void testForLoop3() {
        String contents =
            "Integer[] a = [1, 2, 3]\n" +
            "for (x in a) {\n" +
            "  x\n" +
            "}";
        assertExprType(contents, "x", "java.lang.Integer");
    }
}
