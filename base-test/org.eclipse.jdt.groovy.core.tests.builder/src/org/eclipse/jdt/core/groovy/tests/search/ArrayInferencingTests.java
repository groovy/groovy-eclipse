/*
 * Copyright 2009-2018 the original author or authors.
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

public final class ArrayInferencingTests extends InferencingTestSuite {

    private void assertExprType(String source, String target, String type) {
        final int offset = source.lastIndexOf(target);
        assertType(source, offset, offset + target.length(), type);
    }

    @Test
    public void testArray1() {
        String contents = "def x = new CharSequence[0]";
        assertExprType(contents, "x", "java.lang.CharSequence[]");
        assertExprType(contents, "CharSequence", "java.lang.CharSequence");
    }

    @Test
    public void testArray2() {
        String contents = "def x = (CharSequence[]) null";
        assertExprType(contents, "x", "java.lang.CharSequence[]");
        assertExprType(contents, "CharSequence", "java.lang.CharSequence");
    }

    @Test
    public void testArray3() {
        String contents = "def x = ['1', '2'] as CharSequence[]; x";
        assertExprType(contents, "x", "java.lang.CharSequence[]");
    }

    @Test
    public void testArray4() {
        String contents = "def x = ['1', '2'] as CharSequence[]; x[0]";
        assertExprType(contents, "x[0]", "java.lang.CharSequence");
    }

    @Test
    public void testArray5() {
        String contents = "def x = ['1', '2'] as CharSequence[]; x[0].length";
        assertExprType(contents, "length", "java.lang.Integer");
    }

    @Test
    public void testArray6() {
        String contents = "int i = 0; def x = ['1', '2'] as CharSequence[]; x[i]";
        assertExprType(contents, "x[i]", "java.lang.CharSequence");
    }

    @Test
    public void testArray7() {
        String contents = "int i = 0; def x = ['1', '2'] as CharSequence[]; x[i].length";
        assertExprType(contents, "length", "java.lang.Integer");
    }

    @Test
    public void testArrayLength1() {
        String contents = "int[] x = [1, 2]; x.length";
        assertExprType(contents, "length", "java.lang.Integer");

        int offset = contents.indexOf("length");
        assertDeclaringType(contents, offset, offset + "length".length(), "int[]");
    }

    @Test
    public void testArrayLength2() {
        String contents = "String[] x = ['1', '2']; x.length";
        assertExprType(contents, "length", "java.lang.Integer");

        int offset = contents.indexOf("length");
        assertDeclaringType(contents, offset, offset + "length".length(), "java.lang.String[]");
    }

    @Test
    public void testArrayLength3() {
        String contents = "String[][] x = ['1', '2']; x.length";
        assertExprType(contents, "length", "java.lang.Integer");

        int offset = contents.indexOf("length");
        assertDeclaringType(contents, offset, offset + "length".length(), "java.lang.String[][]");
    }

    @Test
    public void testArrayGenerics1() {
        String contents = "Class<? extends CharSequence>[] array";
        assertExprType(contents, "CharSequence", "java.lang.CharSequence");
        assertExprType(contents, "Class",  "java.lang.Class<? extends java.lang.CharSequence>");
    }

    @Test
    public void testArrayGenerics2() {
        String contents = "import java.util.regex.*; Map<String, Pattern>[] array";
        assertExprType(contents, "String", "java.lang.String");
        assertExprType(contents, "Pattern", "java.util.regex.Pattern");
        assertExprType(contents, "Map", "java.util.Map<java.lang.String,java.util.regex.Pattern>");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/763
    public void testArrayGenerics3() {
        String contents = "Collection<List<String>>[] array = []; array*.trim()";
        assertExprType(contents, "trim", "java.lang.String");
    }

    @Test
    public void testArrayGenerics4() {
        String contents = "Map<String, ?>[] array = [[val:1]]; array*.val";
        assertExprType(contents, "val", "java.lang.Object");
    }

    @Test
    public void testArrayProperty1() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().xx";
        assertExprType(contents, "xx", "XX[]");
    }

    @Test
    public void testArrayProperty2() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().xx[0].yy";
        assertExprType(contents, "yy", "XX");
    }

    @Test
    public void testArrayProperty3() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().xx[new XX()].yy";
        String toFind = "yy";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertUnknownConfidence(contents, start, end, "XX", false);
    }

    @Test
    public void testArrayProperty4() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().xx[0].yy";
        assertExprType(contents, "yy", "XX");
    }

    @Test
    public void testArrayProperty5() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().xx[0].xx[9].yy";
        assertExprType(contents, "yy", "XX");
    }

    @Test
    public void testArrayProperty6() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().getXx()[0].xx[9].yy";
        assertExprType(contents, "yy", "XX");
    }

    @Test
    public void testArrayProperty7() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().getXx()[0].getYy()";
        assertExprType(contents, "getYy", "XX");
    }

    @Test
    public void testArrayProperty8() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().getXx()";
        assertExprType(contents, "getXx", "XX[]");
    }

    @Test
    public void testArrayProperty9() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().getXx()[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx";
        assertExprType(contents, "xx", "XX[]");
    }

    @Test
    public void testArrayProperty10() {
        createUnit("XX", "class XX { XX[] xx; XX yy; }");
        String contents = "new XX().getYy().getYy().getYy().getYy().getYy().getYy().getYy().getYy().getXx()[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx";
        assertExprType(contents, "xx", "XX[]");
    }

    @Test
    public void testForLoop1() {
        String contents =
            "Integer[] a = [1, 2, 3]\n" +
            "for (int i = 0; i < a.length; i += 1) {\n" +
            "  def x = a[i]\n" +
            "}";
        assertExprType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testForLoop2() {
        String contents =
            "Integer[] a = [1, 2, 3]\n" +
            "for (def x : a) {\n" +
            "  x\n" +
            "}";
        assertExprType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testForLoop3() {
        String contents =
            "Integer[] a = [1, 2, 3]\n" +
            "for (x in a) {\n" +
            "  x\n" +
            "}";
        assertExprType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testForLoop3a() {
        String contents =
            "Integer[] a = [1, 2, 3]\n" +
            "for (x in a) {\n" +
            "}";
        assertExprType(contents, "x", "java.lang.Integer");
    }
}
