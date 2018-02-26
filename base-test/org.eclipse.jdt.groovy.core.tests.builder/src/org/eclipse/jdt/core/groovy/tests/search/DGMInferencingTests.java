/*
 * Copyright 2009-2018 the original author or authors.
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

import java.util.Comparator;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public final class DGMInferencingTests extends InferencingTestSuite {

    @Test
    public void testDGM1() {
        String contents = "1.with { it.intValue() }";
        int start = contents.lastIndexOf("it");
        assertType(contents, start, start + 2, "java.lang.Integer");
    }

    @Test
    public void testDGM2() { // with has a delegate-first resolve strategy (default is owner-first)
        String contents =
            "class Y {\n" +
            "  String value\n" +
            "}\n" +
            "class Z {\n" +
            "  Number value\n" +
            "  void meth(Y y) {\n" +
            "    y.with { value }\n" +
            "  }\n" +
            "}";
        int start = contents.lastIndexOf("value");
        assertType(contents, start, start + 5, "java.lang.String");
    }

    @Test
    public void testDGM2a() {
        String contents =
            "class Y {\n" +
            "  String value\n" +
            "}\n" +
            "class Z {\n" +
            "  Number value\n" +
            "  void meth(Y y) {\n" +
            "    y.with { println \"Value: $value\" }\n" + // another enclosing method call
            "  }\n" +
            "}";
        int start = contents.lastIndexOf("value");
        assertType(contents, start, start + 5, "java.lang.String");
    }

    @Test
    public void testDGM3() {
        String contents = "[1].collectNested { it }";
        int start = contents.lastIndexOf("it");
        assertType(contents, start, start + 2, "java.lang.Integer");
    }

    @Test
    public void testDGM4() {
        String contents = "[1].collectNested { it }";
        int start = contents.lastIndexOf("it");
        assertType(contents, start, start + 2, "java.lang.Integer");
    }

    @Test
    public void testDGM5() {
        String contents = "[key:1].every { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testDGM6() {
        String contents = "[key:1].any { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testDGM7() {
        String contents = "[key:1].every { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testDGM8() {
        String contents = "[key:1].any { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testDGM9() {
        String contents = "[1].collectMany { [it.intValue()] }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test @Ignore
    public void testDGM10() {
        // this one is not working since Inferencing Engine gets tripped up with the different variants of 'metaClass'
        String contents = "Integer.metaClass { this }";
        String str = "this";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "groovy.lang.MetaClass");
    }

    @Test
    public void testDGM11() {
        String contents = "([1] ).collectEntries { index -> index.intValue() }";
        String str = "index";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testDGM12() {
        String contents = "[key:1].findResult(1) { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testDGM13() {
        String contents = "[key:1].findResult(1) { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testDGM14() {
        String contents = "[1].findResults { it.intValue() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testDGM15() {
        String contents = "[key:1].findResults { it.getKey().toUpperCase() + it.getValue().intValue() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.util.Map$Entry<java.lang.String,java.lang.Integer>");
    }

    @Test
    public void testDGM16() {
        String contents = "[key:1].findResults { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testDGM17() {
        String contents = "[key:1].findResults { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testDGM18() {
        String contents = "[key:1].findAll { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testDGM19() {
        String contents = "[key:1].findAll { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testDGM20() {
        String contents = "[key:1].groupBy { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testDGM21() {
        String contents = "[key:1].groupBy { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testDGM22() {
        String contents = "([1]).countBy { it.intValue() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testDGM23() {
        String contents = "[key:1].groupEntriesBy { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testDGM24() {
        String contents = "[key:1].groupEntriesBy { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testDGM25() {
        String contents = "[key:1].inject(1) { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testDGM26() {
        String contents = "[key:1].inject(1) { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testDGM27() {
        String contents = "[key:1].withDefault { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testDGM28() {
        String contents = "[key:1].withDefault { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testDGM29() {
        String contents = "new FileOutputStream().withStream { it }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.OutputStream");
    }

    @Test
    public void testDGM30() {
        String contents = "new File(\"test\").eachFileMatch(FileType.FILES, 1) { it.getName() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.File");
    }

    @Test
    public void testDGM31() {
        String contents = "new File(\"test\").eachDirMatch(FileType.FILES, 1) { it.getName() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.File");
    }

    @Test
    public void testDGM32() {
        String contents = "new File(\"test\").withReader { it.reset() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.BufferedReader");
    }

    @Test
    public void testDGM33() {
        String contents = "new FileReader(new File(\"test\")).filterLine(new FileWriter(new File(\"test\"))) { it.toUpperCase() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testDGM34() {
        String contents = "new File(\"test\").withOutputStream { it.flush() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.OutputStream");
    }

    @Test
    public void testDGM35() {
        String contents = "new File(\"test\").withInputStream { it.flush() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.InputStream");
    }

    @Test
    public void testDGM36() {
        String contents = "new File(\"test\").withDataOutputStream { it.flush() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.DataOutputStream");
    }

    @Test
    public void testDGM37() {
        String contents = "new File(\"test\").withDataInputStream { it.flush() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.DataInputStream");
    }

    @Test
    public void testDGM38() {
        String contents = "new File(\"test\").withWriter { it.flush() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.BufferedWriter");
    }

    @Test
    public void testDGM39() {
        String contents = "new File(\"test\").withWriterAppend { it.flush() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.BufferedWriter");
    }

    @Test
    public void testDGM40() {
        String contents = "new File(\"test\").withPrintWriter { it.flush() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.PrintWriter");
    }

    @Test
    public void testDGM41() {
        String contents = "new FileReader(new File(\"test\")).transformChar(new FileWriter(new File(\"test\"))) { it.toUpperCase() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testDGM42() {
        String contents = "new FileReader(new File(\"test\")).transformLine(new FileWriter(new File(\"test\"))) { it.toUpperCase() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testDGM43() {
        String contents = "\"\".eachMatch(\"\") { it.toLowerCase() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // GRECLIPSE-1695
    public void testDGM44() {
        String contents = "List<String> list = []\n" +
            "list.toSorted { a, b ->\n" +
            "  a.trim() <=> b.trim()\n" +
            "}.each {\n" +
            "  it\n" +
            "}\n";
        int offset = contents.lastIndexOf("it");
        assertType(contents, offset, offset + 2, "java.lang.String");
    }

    @Test // GRECLIPSE-1695 redux
    public void testDGM45() {
        // Java 8 adds default method sort(Comparator) to the List interface
        String contents = "List<String> list = []\n" +
            "list.sort { a, b ->\n" +
            "  a.trim() <=> b.trim()\n" +
            "}.each {\n" +
            "  it\n" +
            "}\n";
        int offset = contents.lastIndexOf("it");
        assertType(contents, offset, offset + 2, "java.lang.String");
    }

    @Test
    public void testDGM45a() {
        // Java 8 adds default method sort(Comparator) to the List interface
        boolean jdkListSort;
        try {
            List.class.getDeclaredMethod("sort", Comparator.class);
            jdkListSort = true;
        } catch (Exception e) {
            jdkListSort = false;
        }

        String contents = "List<String> list = []\n" +
            "list.sort({ a, b ->\n" +
            "  a.trim() <=> b.trim()\n" +
            "} as Comparator).each {\n" +
            "  it\n" +
            "}\n";
        int offset = contents.lastIndexOf("it");
        assertType(contents, offset, offset + 2, jdkListSort ? "java.lang.Void" : "java.lang.String");
    }

    @Test
    public void testDGM46() {
        String contents = "java.util.regex.Pattern[] pats = [~/one/, ~/two/]\n" +
            "pats.eachWithIndex { pat, idx ->\n" + // T <T> eachWithIndex(T self, Closure task)
            "  \n" +
            "}\n";
        int start = contents.indexOf("eachWithIndex");
        int end = start + "eachWithIndex".length();
        assertType(contents, start, end, "java.util.regex.Pattern[]");
    }

    @Test
    public void testDGM47() throws Throwable {
        String contents = "java.util.regex.Pattern[] pats = [~/one/, ~/two/]\n" +
            "pats.eachWithIndex { pat, idx ->\n" +
            "  \n" +
            "}.collect {\n" + // <T> List<T> collect(Object self, Closure<T> xform)
            "  it\n" +
            "}\n";
        int start = contents.indexOf("collect");
        int end = start + "collect".length();
        assertTypeOneOf(contents, start, end, "java.util.List", "java.util.List<T>", "java.util.List<java.lang.Object<T>>");
    }

    @Test
    public void testDGM48() {
        String contents = "int[] ints = [1, 2, 3]\n" +
            "String dgm(Object[] arr) { null }\n" +
            "Object dgm(Object obj) { null }\n" +
            "def result = dgm(ints)\n";
        int start = contents.indexOf("result");
        int end = start + "result".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testDGM48a() {
        // TODO: runtime preference seems to be the Object method
        String contents = "int[] ints = [1, 2, 3]\n" +
            "Object dgm(Object obj) { null }\n" +
            "String dgm(Object[] arr) { null }\n" +
            "def result = dgm(ints)\n";
        int start = contents.indexOf("result");
        int end = start + "result".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testDGM49() {
        // primitive array is not compatible with boxed-type array
        String contents = "int[] ints = [1, 2, 3]\n" +
            "Integer dgm(Integer[] arr) { null }\n" +
            "Object dgm(Object obj) { null }\n" +
            "def result = dgm(ints)\n";
        int start = contents.indexOf("result");
        int end = start + "result".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testDGM50() {
        // SimpleTypeLookup returns first method in case of no type-compatible matches
        // TODO: primitive array is not compatible with derived-from-boxed-type array
        String contents = "int[] ints = [1, 2, 3]\n" +
            "Number dgm(Number[] arr) { null }\n" +
            "def result = dgm(ints)\n";
        int start = contents.indexOf("result");
        int end = start + "result".length();
        assertType(contents, start, end, "java.lang.Number");
        //assertUnknownConfidence(contents, start, end, "java.lang.Object", false);
    }

    @Test
    public void testDGM50a() {
        String contents = "Integer[] ints = [1, 2, 3]\n" +
            "Number dgm(Number[] arr) { null }\n" +
            "def result = dgm(ints)\n";
        int start = contents.indexOf("result");
        int end = start + "result".length();
        assertType(contents, start, end, "java.lang.Number");
    }

    @Test
    public void testDGMDeclaring() {
        // With groovy 2.0, there are some new DGM classes.  Need to ensure that we are using those classes as the declaring type, but only for 2.0 or later.
        String contents = "\"\".eachLine";
        String str = "eachLine";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertDeclaringType(contents, start, end, "org.codehaus.groovy.runtime.StringGroovyMethods");
    }

    @Test
    public void testDGMDeclaring2() {
        String contents = "new File().eachLine";
        String str = "eachLine";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertDeclaringType(contents, start, end, "org.codehaus.groovy.runtime.ResourceGroovyMethods");
    }

    @Test
    public void testDGMDeclaring3() {
        String contents = "Writer w\nw.leftShift";
        String str = "leftShift";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertDeclaringType(contents, start, end, "org.codehaus.groovy.runtime.IOGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/372
    public void testDGSMDeclaring() {
        String contents = "Date.parse('format', 'value')";
        String target = "parse";
        int start = contents.lastIndexOf(target), until = start + target.length();
        assertDeclaringType(contents, start, until, "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods");
    }

    @Test
    public void testDGSMDeclaring2() {
        String contents = "Date.sleep(42)";
        String target = "sleep";
        int start = contents.lastIndexOf(target), until = start + target.length();
        assertDeclaringType(contents, start, until, "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods");
    }

    @Test
    public void testDGSMDeclaring3() {
        String contents = "Date.getLastMatcher()";
        String target = "getLastMatcher";
        int start = contents.lastIndexOf(target), until = start + target.length();
        assertUnknownConfidence(contents, start, until, "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods", false);
    }

    @Test
    public void testStaticMixinDGM() {
        String contents = "class Parrot { static void echo(String self) { println \"Parrot says: $self\" } }\nString.mixin(Parrot)\n'sqwak'.echo()";
        String target = "mixin";
        int start = contents.lastIndexOf(target), until = start + target.length();
        assertDeclaringType(contents, start, until, "org.codehaus.groovy.runtime.DefaultGroovyMethods");
        target = "echo";
        start = contents.lastIndexOf(target); until = start + target.length();
        //assertDeclaringType(contents, start, until, "Parrot"); // added to String usinf DGM.mixin(Class)
    }

    @Test
    public void testStaticWithDGM() {
        String contents = "Date.with { delegate }"; // type of delegate checked in ClosureInferencingTests
        String target = "with";
        int start = contents.lastIndexOf(target), until = start + target.length();
        assertDeclaringType(contents, start, until, "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }
}
