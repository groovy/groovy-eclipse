/*
 * Copyright 2009-2024 the original author or authors.
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

import static org.junit.Assert.assertTrue;

import org.codehaus.groovy.ast.FieldNode;
import org.junit.Test;

public final class ArrayInferencingTests extends InferencingTestSuite {

    @Test
    public void testArray1() {
        String contents = "def x = new CharSequence[0]";
        assertType(contents, "x", "java.lang.CharSequence[]");
        assertType(contents, "CharSequence", "java.lang.CharSequence");
    }

    @Test
    public void testArray2() {
        String contents = "def x = (CharSequence[]) null";
        assertType(contents, "x", "java.lang.CharSequence[]");
        assertType(contents, "CharSequence", "java.lang.CharSequence");
    }

    @Test
    public void testArray3() {
        String contents = "def x = ['1', '2'] as CharSequence[]; x";
        assertType(contents, "x", "java.lang.CharSequence[]");
    }

    @Test
    public void testArray4() {
        String contents = "def x = ['1', '2'] as CharSequence[]; x[0]";
        assertType(contents, "x[0]", "java.lang.CharSequence");
    }

    @Test
    public void testArray5() {
        String contents = "def x = ['1', '2'] as CharSequence[]; x[0].length()";
        assertType(contents, "length", "java.lang.Integer");
    }

    @Test
    public void testArray6() {
        String contents = "int i = 0; def x = ['1', '2'] as CharSequence[]; x[i]";
        assertType(contents, "x[i]", "java.lang.CharSequence");
    }

    @Test
    public void testArray7() {
        String contents = "int i = 0; def x = ['1', '2'] as CharSequence[]; x[i].length()";
        assertType(contents, "length", "java.lang.Integer");
    }

    @Test
    public void testArrayClone1() {
        String contents = "def x = new int[0].clone()";
        assertType(contents, "x", "int[]");
        assertDeclaringType(contents, "clone", "int[]");
    }

    @Test
    public void testArrayClone2() {
        String contents = "def x = new Number[0].clone()";
        assertType(contents, "x", "java.lang.Number[]");
        assertDeclaringType(contents, "clone", "java.lang.Number[]");
    }

    @Test
    public void testArrayClone3() {
        String contents = "def x = ([] as Number[][]).clone()";
        assertType(contents, "x", "java.lang.Number[][]");
        assertDeclaringType(contents, "clone", "java.lang.Number[][]");
    }

    @Test
    public void testArrayClone4() {
        String contents = "def m(String... a) { def x = a.clone()}";
        assertType(contents, "x", "java.lang.String[]");
        assertDeclaringType(contents, "clone", "java.lang.String[]");
    }

    @Test
    public void testArrayLength1() {
        String contents = "int[] x = [1, 2]; x.length";
        assertType(contents, "length", "java.lang.Integer");

        int offset = contents.indexOf("length");
        var length = (FieldNode) assertDeclaration(contents, offset, offset + "length".length(), "int[]", "length", DeclarationKind.FIELD);
        assertTrue(length.isFinal() && length.isPublic() && length.isSynthetic());
    }

    @Test
    public void testArrayLength2() {
        String contents = "String[] x = ['1', '2']; x.length";
        assertType(contents, "length", "java.lang.Integer");

        int offset = contents.indexOf("length");
        assertDeclaringType(contents, offset, offset + "length".length(), "java.lang.String[]");
    }

    @Test
    public void testArrayLength3() {
        String contents = "String[][] x = ['1', '2']; x.length";
        assertType(contents, "length", "java.lang.Integer");

        int offset = contents.indexOf("length");
        assertDeclaringType(contents, offset, offset + "length".length(), "java.lang.String[][]");
    }

    @Test
    public void testArrayArgument1() {
        //@formatter:off
        String contents =
            "Number m(Number[] arr) { null }\n" +
            "int[] nums = [1, 2, 3]\n" +
            "def result = m(nums)\n";
        //@formatter:on

        int offset = contents.lastIndexOf("m(");
        assertUnknownConfidence(contents, offset, offset + 1);
    }

    @Test
    public void testArrayArgument2() {
        //@formatter:off
        String contents =
            "Number m(Number[] arr) { null }\n" +
            "Integer[] nums = [1, 2, 3]\n" +
            "def result = m(nums)\n";
        //@formatter:on

        assertType(contents, "result", "java.lang.Number");
    }

    @Test
    public void testArrayGenerics1() {
        String contents = "Class<? extends CharSequence>[] array";
        assertType(contents, "CharSequence", "java.lang.CharSequence");
        assertType(contents, "Class",  "java.lang.Class<? extends java.lang.CharSequence>");
    }

    @Test
    public void testArrayGenerics2() {
        String contents = "import java.util.regex.*; Map<String, Pattern>[] array";
        assertType(contents, "String", "java.lang.String");
        assertType(contents, "Pattern", "java.util.regex.Pattern");
        assertType(contents, "Map", "java.util.Map<java.lang.String,java.util.regex.Pattern>");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/763
    public void testArrayGenerics3() {
        String contents = "class C {\n int x\n}\n" + "Collection<List<C> >[] array = []; array*.x";
        assertType("@groovy.transform.TypeChecked " + contents, "x", "java.lang.Integer");
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1594
    public void testArrayGenerics4() {
        String contents = "class C {\n int x\n}\n" + "Collection<List<C> >[] array = []; array*.getX()";
        assertType("@groovy.transform.TypeChecked " + contents, "getX", "java.lang.Integer");
        assertType(contents, "getX", "java.lang.Integer");
    }

    @Test
    public void testArrayGenerics5() {
        String contents = "Map<String, ?>[] array = [[val:1]]; array*.val";
        assertType(contents, "val", "java.lang.Object");
    }

    @Test
    public void testArrayProperty1() {
        createUnit("XX", "class XX { XX[] xx; XX yy;}");
        String contents = "new XX().xx";
        assertType(contents, "xx", "XX[]");
    }

    @Test
    public void testArrayProperty2() {
        createUnit("XX", "class XX { XX[] xx; XX yy;}");
        String contents = "new XX().xx[0].yy";
        assertType(contents, "yy", "XX");
    }

    @Test
    public void testArrayProperty3() {
        createUnit("XX", "class XX { XX[] xx; XX yy;}");
        String contents = "new XX().xx[new XX()].yy";
        String toFind = "yy";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertUnknownConfidence(contents, start, end);
    }

    @Test
    public void testArrayProperty4() {
        createUnit("XX", "class XX { XX[] xx; XX yy;}");
        String contents = "new XX().xx[0].yy";
        assertType(contents, "yy", "XX");
    }

    @Test
    public void testArrayProperty5() {
        createUnit("XX", "class XX { XX[] xx; XX yy;}");
        String contents = "new XX().xx[0].xx[9].yy";
        assertType(contents, "yy", "XX");
    }

    @Test
    public void testArrayProperty6() {
        createUnit("XX", "class XX { XX[] xx; XX yy;}");
        String contents = "new XX().getXx()[0].xx[9].yy";
        assertType(contents, "yy", "XX");
    }

    @Test
    public void testArrayProperty7() {
        createUnit("XX", "class XX { XX[] xx; XX yy;}");
        String contents = "new XX().getXx()[0].getYy()";
        assertType(contents, "getYy", "XX");
    }

    @Test
    public void testArrayProperty8() {
        createUnit("XX", "class XX { XX[] xx; XX yy;}");
        String contents = "new XX().getXx()";
        assertType(contents, "getXx", "XX[]");
    }

    @Test
    public void testArrayProperty9() {
        createUnit("XX", "class XX { XX[] xx; XX yy;}");
        String contents = "new XX().getXx()[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx";
        assertType(contents, "xx", "XX[]");
    }

    @Test
    public void testArrayProperty10() {
        createUnit("XX", "class XX { XX[] xx; XX yy;}");
        String contents = "new XX().getYy().getYy().getYy().getYy().getYy().getYy().getYy().getYy()" +
                        ".getXx()[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx[0].xx";
        assertType(contents, "xx", "XX[]");
    }

    @Test
    public void testForLoop1() {
        String contents =
            "Integer[] a = [1, 2, 3]\n" +
            "for (int i = 0; i < a.length; i += 1) {\n" +
            "  def x = a[i]\n" +
            "}";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testForLoop2() {
        String contents =
            "Integer[] a = [1, 2, 3]\n" +
            "for (def x : a) {\n" +
            "  x\n" +
            "}";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testForLoop3() {
        String contents =
            "Integer[] a = [1, 2, 3]\n" +
            "for (x in a) {\n" +
            "  x\n" +
            "}";
        assertType(contents, "x", "java.lang.Integer");
    }

    @Test
    public void testForLoop3a() {
        String contents =
            "Integer[] a = [1, 2, 3]\n" +
            "for (x in a) {\n" +
            "}";
        assertType(contents, "x", "java.lang.Integer");
    }
}
