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

import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.junit.Test;

public final class StaticInferencingTests extends InferencingTestSuite {

    @Test
    public void testClassReference1() {
        String contents = "String";
        assertType(contents, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference2() {
        String contents = "String.class";
        assertType(contents, "java.lang.Class<java.lang.String>");
    }

    @Test
    public void testClassReference3() {
        String contents = "String.getClass()";
        int start = contents.indexOf("getClass");
        int end = start + "getClass".length();
        assertType(contents, start, end, GroovyUtils.GROOVY_LEVEL > 23 ? "java.lang.Class<?>"
            : "java.lang.Class<? extends java.lang.Object>"); // should be <? extends java.lang.String>
    }

    @Test
    public void testClassReference4() {
        String contents = "String.class.getCanonicalName()";
        int start = contents.indexOf("getCanonicalName");
        int end = start + "getCanonicalName".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testClassReference5() {
        String contents = "String.class.canonicalName";
        int start = contents.indexOf("canonicalName");
        int end = start + "canonicalName".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // GRECLIPSE-1079 Accessing the methods/fields on class directly
    public void testClassReference6() {
        String contents = "String.getCanonicalName()";
        int start = contents.indexOf("getCanonicalName");
        int end = start + "getCanonicalName".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testClassReference7() {
        String contents = "String.canonicalName";
        int start = contents.indexOf("canonicalName");
        int end = start + "canonicalName".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testClassReference8() {
        String contents = "class S { static { getCanonicalName() } }";
        int start = contents.indexOf("getCanonicalName");
        int end = start + "getCanonicalName".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // GRECLIPSE-855: should be able to find the type, but with unknown confidence
    public void testNonStaticReference1() {
        String contents = "String.length()";
        int start = contents.indexOf("length");
        int end = start + "length".length();
        assertUnknownConfidence(contents, start, end, "java.lang.String", false);
    }

    @Test
    public void testNonStaticReference2() {
        String contents = "String.length";
        int start = contents.indexOf("length");
        int end = start + "length".length();
        assertUnknownConfidence(contents, start, end, "java.lang.String", false);
    }

    @Test
    public void testNonStaticReference3() {
        String contents = "class GGG { int length }\nGGG.length";
        int start = contents.lastIndexOf("length");
        int end = start + "length".length();
        assertUnknownConfidence(contents, start, end, "GGG", false);
    }

    @Test
    public void testNonStaticReference4() {
        String contents = "class GGG { int length }\nGGG.@length";
        int start = contents.lastIndexOf("length");
        int end = start + "length".length();
        assertUnknownConfidence(contents, start, end, "GGG", false);
    }

    @Test
    public void testNonStaticReference5() {
        String contents = "class GGG { int length() { } }\nGGG.length()";
        int start = contents.lastIndexOf("length");
        int end = start + "length".length();
        assertUnknownConfidence(contents, start, end, "GGG", false);
    }

    @Test
    public void testNonStaticReference6() {
        String contents = "class GGG { def length = { } }\nGGG.length()";
        int start = contents.lastIndexOf("length");
        int end = start + "length".length();
        assertUnknownConfidence(contents, start, end, "GGG", false);
    }

    @Test
    public void testNonStaticReference7() {
        String contents = "class GGG { int length() { } \nstatic {\nlength() } }";
        int start = contents.lastIndexOf("length");
        int end = start + "length".length();
        assertUnknownConfidence(contents, start, end, "GGG", false);
    }

    @Test
    public void testNonStaticReference8() {
        String contents = "class GGG { def length = { } \nstatic {\nlength() } }";
        int start = contents.lastIndexOf("length");
        int end = start + "length".length();
        assertUnknownConfidence(contents, start, end, "GGG", false);
    }

    @Test
    public void testStaticReference1() {
        String contents = "class GGG { static int length }\nGGG.length";
        int start = contents.lastIndexOf("length");
        int end = start + "length".length();
        assertType(contents, start, end, "java.lang.Integer", false);
    }

    @Test
    public void testStaticImport1() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");
        String contents = "import static p.Other.FOO";
        int start = contents.lastIndexOf("FOO");
        int end = start + "FOO".length();
        assertType(contents, start, end, "java.lang.Integer", false);
    }

    @Test
    public void testStaticImport2() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");
        String contents = "import static p.Other.FOO as BAR";
        int start = contents.lastIndexOf("FOO");
        int end = start + "FOO".length();
        assertType(contents, start, end, "java.lang.Integer", false);
    }

    @Test
    public void testStaticImport3() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");
        String contents = "import static p.Other.FOO\nFOO";
        int start = contents.lastIndexOf("FOO");
        int end = start + "FOO".length();
        assertType(contents, start, end, "java.lang.Integer", false);
    }

    @Test
    public void testStaticImport4() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");
        String contents = "import static p.Other.FOO as BAR\nFOO";
        int start = contents.lastIndexOf("FOO");
        int end = start + "FOO".length();
        assertUnknownConfidence(contents, start, end, "Search", false);
    }

    @Test
    public void testStaticImport5() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");
        String contents = "import static p.Other.FO";
        int start = contents.lastIndexOf("FO");
        int end = start + "FO".length();
        assertUnknownConfidence(contents, start, end, "Search", false);
    }

    @Test
    public void testStaticImport6() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");
        String contents = "import static p.Other.BAR\nBAR";
        int start = contents.indexOf("BAR");
        int end = start + "BAR".length();
        assertType(contents, start, end, "java.lang.Boolean", false);
        start = contents.lastIndexOf("BAR");
        end = start + "BAR".length();
        assertType(contents, start, end, "java.lang.Boolean", false);
    }

    @Test
    public void testStaticImport7() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");
        String contents = "import static p.Other.FOO\nFOO";
        int start = contents.lastIndexOf("p.Other");
        int end = start + "p.Other".length();
        assertType(contents, start, end, "p.Other", false);
    }

    @Test
    public void testStaticImport8() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");
        String contents = "import static p.Other.FOO as BAR\nFOO";
        int start = contents.lastIndexOf("p.Other");
        int end = start + "p.Other".length();
        assertType(contents, start, end, "p.Other", false);
    }

    @Test
    public void testStaticImport9() throws Exception {
        createUnit("p", "Other", "package p\nclass Other { static int FOO\n static boolean BAR() { } }");
        String contents = "import static p.Other.*\nFOO";
        int start = contents.lastIndexOf("p.Other");
        int end = start + "p.Other".length();
        assertType(contents, start, end, "p.Other", false);
    }

    @Test // GRECLIPSE-1544
    public void testSTCAndClassInstance() {
        String contents = "package pkg0\n" +
            "@groovy.transform.TypeChecked\n" +
            "public class BugClass {\n" +
            "    public void showBug() {\n" +
            "        BugClass.getInstance();  \n" +
            "    }\n" +
            "    static BugClass getInstance() { return null }\n" +
            "}";

        int start = contents.indexOf("getInstance");
        int end = start + "getInstance".length();
        assertType(contents, start, end, "pkg0.BugClass", false);
    }
}
