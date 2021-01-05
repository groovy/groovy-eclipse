/*
 * Copyright 2009-2021 the original author or authors.
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

import org.junit.Ignore;
import org.junit.Test;

public final class DGMInferencingTests extends InferencingTestSuite {

    private void assertDeclType(String source, String target, String type) {
        assertDeclaringType(source, target, type);
    }

    private void assertExprType(String source, String target, String type) {
        assertType(source, target, type);
    }

    //--------------------------------------------------------------------------

    @Test
    public void testDGM1() {
        //@formatter:off
        String contents =
            "1.with { it.intValue() }";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.Integer");
        assertDeclType(contents, "with", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test
    public void testDGM1a() {
        //@formatter:off
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  1.with { it.intValue() }" +
            "}";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.Integer");
        assertDeclType(contents, "with", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test
    public void testDGM2() { // with has a delegate-first resolve strategy (default is owner-first)
        //@formatter:off
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
        //@formatter:on
        assertExprType(contents, "value", "java.lang.String");
    }

    @Test
    public void testDGM2a() {
        //@formatter:off
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
        //@formatter:on
        assertExprType(contents, "value", "java.lang.String");
    }

    @Test
    public void testDGM3() {
        //@formatter:off
        String contents =
            "[1, [2, 3]].collectNested { it }";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.Object");
    }

    @Test
    public void testDGM4() {
        //@formatter:off
        String contents =
            "[1, [2, 3], null].collectNested { it }";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.Object");
    }

    @Test
    public void testDGM5() {
        //@formatter:off
        String contents =
            "[key:1].every { key, value -> key.toUpperCase() + value.intValue() }";
        //@formatter:on
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM6() {
        //@formatter:off
        String contents =
            "[key:1].any { key, value -> key.toUpperCase() + value.intValue() }";
        //@formatter:on
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM7() {
        //@formatter:off
        String contents =
            "[key:1].every { key, value -> key.toUpperCase() + value.intValue() }";
        //@formatter:on
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM8() {
        //@formatter:off
        String contents =
            "[key:1].any { key, value -> key.toUpperCase() + value.intValue() }";
        //@formatter:on
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM9() {
        //@formatter:off
        String contents =
            "[1].collectMany { [it.intValue()] }";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.Integer");
    }

    @Test
    public void testDGM10() {
        //@formatter:off
        String contents =
            "Integer.metaClass { this }"; // static MetaClass metaClass(Class self, Closure closure)
        //@formatter:on
        assertExprType(contents, "this", DEFAULT_UNIT_NAME);
        // TODO: When 'closure' has @ClosureParams and/or @DelegatesTo, check param(s) and delegate
    }

    @Test
    public void testDGM11() {
        //@formatter:off
        String contents =
            "([1]).collectEntries { index -> index.intValue() }";
        //@formatter:on
        assertExprType(contents, "index", "java.lang.Integer");
    }

    @Test
    public void testDGM12() {
        //@formatter:off
        String contents =
            "[key:1].findResult(1) { key, value -> key.toUpperCase() + value.intValue() }";
        //@formatter:on
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM13() {
        //@formatter:off
        String contents =
            "[key:1].findResult(1) { key, value -> key.toUpperCase() + value.intValue() }";
        //@formatter:on
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM14() {
        //@formatter:off
        String contents =
            "[1].findResults { it.intValue() }";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.Integer");
    }

    @Test
    public void testDGM15() {
        //@formatter:off
        String contents =
            "[key:1].findResults { it.getKey().toUpperCase() + it.getValue().intValue() }";
        //@formatter:on
        assertExprType(contents, "it", "java.util.Map$Entry<java.lang.String,java.lang.Integer>");
    }

    @Test
    public void testDGM16() {
        //@formatter:off
        String contents =
            "[key:1].findResults { key, value -> key.toUpperCase() + value.intValue() }";
        //@formatter:on
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM17() {
        //@formatter:off
        String contents =
            "[key:1].findResults { key, value -> key.toUpperCase() + value.intValue() }";
        //@formatter:on
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM18() {
        //@formatter:off
        String contents =
            "[key:1].findAll { key, value -> key.toUpperCase() + value.intValue() }";
        //@formatter:on
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM19() {
        //@formatter:off
        String contents =
            "[key:1].findAll { key, value -> key.toUpperCase() + value.intValue() }";
        //@formatter:on
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM20() {
        //@formatter:off
        String contents =
            "[key:1].groupBy { key, value -> key.toUpperCase() + value.intValue() }";
        //@formatter:on
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM21() {
        //@formatter:off
        String contents =
            "[key:1].groupBy { key, value -> key.toUpperCase() + value.intValue() }";
        //@formatter:on
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM22() {
        //@formatter:off
        String contents =
            "([1]).countBy { it.intValue() }";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.Integer");
    }

    @Test
    public void testDGM23() {
        //@formatter:off
        String contents =
            "[key:1].groupEntriesBy { key, value -> key.toUpperCase() + value.intValue() }";
        //@formatter:on
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM24() {
        //@formatter:off
        String contents =
            "[key:1].groupEntriesBy { key, value -> key.toUpperCase() + value.intValue() }";
        //@formatter:on
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM25() {
        //@formatter:off
        String contents =
            "[key:1].inject(1.0) { seed, entry -> null }";
        //@formatter:on
        assertExprType(contents, "seed", "java.math.BigDecimal");
    }

    @Test
    public void testDGM26() {
        //@formatter:off
        String contents =
            "[key:1].inject(1.0) { seed, entry -> entry.key.toUpperCase() + entry.value.intValue() }";
        //@formatter:on
        assertExprType(contents, "entry", "java.util.Map$Entry<java.lang.String,java.lang.Integer>");
    }

    @Test
    public void testDGM26a() {
        //@formatter:off
        String contents =
            "[key:1].inject(1.0) { seed, key, value -> key.toUpperCase() + value.intValue() }";
        //@formatter:on
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM26b() {
        //@formatter:off
        String contents =
            "[key:1].inject(1.0) { seed, key, value -> key.toUpperCase() + value.intValue() }";
        //@formatter:on
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM27() {
        //@formatter:off
        String contents =
            "[key:1].withDefault { key -> key.toUpperCase() }";
        //@formatter:on
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM28() { // withDefault expects one-param Closure
        //@formatter:off
        String contents =
            "[key:1].withDefault { key, val -> key.toUpperCase() + val.intValue() }";
        //@formatter:on
        assertExprType(contents, "val", "java.lang.Object");
    }

    @Test
    public void testDGM29() {
        //@formatter:off
        String contents =
            "new FileOutputStream().withStream { it }";
        //@formatter:on
        assertExprType(contents, "it", "java.io.FileOutputStream");
    }

    @Test
    public void testDGM30() {
        //@formatter:off
        String contents =
            "import groovy.io.FileType\n" +
            "new File('test').eachFileMatch(FileType.FILES, 1) { it.name }";
        //@formatter:on
        assertExprType(contents, "it", "java.io.File");
    }

    @Test
    public void testDGM31() {
        //@formatter:off
        String contents =
            "new File('test').eachDirMatch(1) { it.name }";
        //@formatter:on
        assertExprType(contents, "it", "java.io.File");
    }

    @Test
    public void testDGM32() {
        //@formatter:off
        String contents =
            "new File('test').withReader { it.reset() }";
        //@formatter:on
        assertExprType(contents, "it", "java.io.BufferedReader");
    }

    @Test
    public void testDGM33() {
        //@formatter:off
        String contents =
            "new FileReader(new File('test')).filterLine(new FileWriter(new File('test'))) { it.toUpperCase() }";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.String");
    }

    @Test
    public void testDGM34() {
        //@formatter:off
        String contents =
            "new File('test').withOutputStream { it.flush() }";
        //@formatter:on
        assertExprType(contents, "it", "java.io.OutputStream");
    }

    @Test
    public void testDGM35() {
        //@formatter:off
        String contents =
            "new File('test').withInputStream { it.flush() }";
        //@formatter:on
        assertExprType(contents, "it", "java.io.InputStream");
    }

    @Test
    public void testDGM36() {
        //@formatter:off
        String contents =
            "new File('test').withDataOutputStream { it.flush() }";
        //@formatter:on
        assertExprType(contents, "it", "java.io.DataOutputStream");
    }

    @Test
    public void testDGM37() {
        //@formatter:off
        String contents =
            "new File('test').withDataInputStream { it.flush() }";
        //@formatter:on
        assertExprType(contents, "it", "java.io.DataInputStream");
    }

    @Test
    public void testDGM38() {
        //@formatter:off
        String contents =
            "new File('test').withWriter { it.flush() }";
        //@formatter:on
        assertExprType(contents, "it", "java.io.BufferedWriter");
    }

    @Test
    public void testDGM39() {
        //@formatter:off
        String contents =
            "new File('test').withWriterAppend { it.flush() }";
        //@formatter:on
        assertExprType(contents, "it", "java.io.BufferedWriter");
    }

    @Test
    public void testDGM40() {
        //@formatter:off
        String contents =
            "new File('test').withPrintWriter { it.flush() }";
        //@formatter:on
        assertExprType(contents, "it", "java.io.PrintWriter");
    }

    @Test
    public void testDGM41() {
        //@formatter:off
        String contents =
            "new FileReader(new File('test')).transformChar(new FileWriter(new File('test'))) { it.toUpperCase() }";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.String");
    }

    @Test
    public void testDGM42() {
        //@formatter:off
        String contents =
            "new FileReader(new File('test')).transformLine(new FileWriter(new File('test'))) { it.toUpperCase() }";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.String");
    }

    @Test @Ignore("ClosureParams states 'List<String>' or 'String[]', but " +
        "runtime allows for destructuring if number of elements fits into params")
    public void testDGM43() {
        //@formatter:off
        String contents =
            "''.eachMatch('') { it.toLowerCase() }";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.String");
    }

    @Test // GRECLIPSE-1695
    public void testDGM44() {
        //@formatter:off
        String contents =
            "List<String> list = []\n" +
            "list.toSorted { a, b ->\n" +
            "  a.trim() <=> b.trim()\n" +
            "}.each {\n" +
            "  it\n" +
            "}\n";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.String");
    }

    @Test
    public void testDGM45() {
        //@formatter:off
        String contents =
            "List<String> list = []\n" +
            "list.sort { a, b ->\n" +
            "  a.trim() <=> b.trim()\n" +
            "}.each {\n" +
            "  it\n" +
            "}\n";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.String");
        // Java 8 adds default method sort(Comparator) to the List interface
        assertDeclType(contents, "sort", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test
    public void testDGM45a() {
        // Java 8 adds default method sort(Comparator) to the List interface
        boolean jdkListSort;
        try {
            java.util.List.class.getDeclaredMethod("sort", java.util.Comparator.class);
            jdkListSort = true;
        } catch (Exception e) {
            jdkListSort = false;
        }
        //@formatter:off
        String contents =
            "List<String> list = []\n" +
            "list.sort({ a, b ->\n" +
            "  a.trim() <=> b.trim()\n" +
            "} as Comparator).each {\n" +
            "  it\n" +
            "}\n";
        //@formatter:on
        if (!jdkListSort) {
            assertExprType(contents, "it", "java.lang.String");
        } else {
            assertExprType(contents, "sort", "java.lang.Void");
            assertUnknownConfidence(contents, contents.indexOf("each"), contents.indexOf("each") + 4);
        }
    }

    @Test
    public void testDGM45b() {
        //@formatter:off
        String contents =
            "String[] array = []\n" +
            "array.sort { a, b ->\n" +
            "  a.trim() <=> b.trim()\n" +
            "}\n";
        //@formatter:on
        assertDeclType(contents, "sort", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1077
    public void testDGM45c() {
        //@formatter:off
        String contents =
            "char[] array = []\n" +
            "array.sort()\n";
        //@formatter:on
        int offset = contents.indexOf("sort");
        assertUnknownConfidence(contents, offset, offset + 4);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1214
    public void testDGM45d() {
        //@formatter:off
        String contents =
            "String[] array = ['1','22','333']\n" +
            "array.sort(true, Comparator.<String,Integer>comparing({it.length()}).reversed())\n";
        //@formatter:on
        assertDeclType(contents, "sort", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test
    public void testDGM46() {
        //@formatter:off
        String contents =
            "java.util.regex.Pattern[] pats = [~/one/, ~/two/]\n" +
            "pats.eachWithIndex { pat, idx ->\n" + // T <T> eachWithIndex(T self, Closure task)
            "  \n" +
            "}\n";
        //@formatter:on
        assertExprType(contents, "eachWithIndex", "java.util.regex.Pattern[]");
    }

    @Test
    public void testDGM47() {
        //@formatter:off
        String contents =
            "java.util.regex.Pattern[] pats = [~/one/, ~/two/]\n" +
            "pats.eachWithIndex { pat, idx ->\n" +
            "  \n" +
            "}.collect {\n" + // <T> List<T> collect(Object self, Closure<T> xform)
            "  it\n" +
            "}\n";
        //@formatter:on
        assertExprType(contents, "collect", "java.util.List<java.lang.Object>");
    }

    @Test
    public void testDGM48() {
        //@formatter:off
        String contents =
            "int[] ints = [1, 2, 3]\n" +
            "String dgm(Object[] arr) {}\n" +
            "Object dgm(Object obj) {}\n" +
            "def result = dgm(ints)\n";
        //@formatter:on
        assertExprType(contents, "result", "java.lang.Object");
    }

    @Test
    public void testDGM48a() {
        //@formatter:off
        String contents =
            "int[] ints = [1, 2, 3]\n" +
            "Object dgm(Object obj) {}\n" +
            "String dgm(Object[] arr) {}\n" +
            "def result = dgm(ints)\n";
        //@formatter:on
        assertExprType(contents, "result", "java.lang.Object");
    }

    @Test // primitive array is not compatible with boxed-type array
    public void testDGM49() {
        //@formatter:off
        String contents =
            "int[] ints = [1, 2, 3]\n" +
            "Integer dgm(Integer[] arr) { null }\n" +
            "Object dgm(Object obj) { null }\n" +
            "def result = dgm(ints)\n";
        //@formatter:on
        assertExprType(contents, "result", "java.lang.Object");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/804
    public void testDGM50() {
        //@formatter:off
        String contents =
            "def answer = (-42L).&abs\n";
        //@formatter:on
        assertExprType(contents, "abs", "java.lang.Long");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1027
    public void testDGM51() {
        //@formatter:off
        String contents =
            "def result = '42'.number\n";
        //@formatter:on
        assertExprType(contents, "number", "java.lang.Boolean");
        assertDeclType(contents, "number", "org.codehaus.groovy.runtime.StringGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1027
    public void testDGM52() {
        //@formatter:off
        String contents =
            "def result = ' '.allWhitespace\n";
        //@formatter:on
        assertExprType(contents, "allWhitespace", "java.lang.Boolean");
        assertDeclType(contents, "allWhitespace", "org.codehaus.groovy.runtime.StringGroovyMethods");
    }

    @Test
    public void testDGM53() {
        //@formatter:off
        String contents =
            "def result = ' '.&allWhitespace\n";
        //@formatter:on
        int offset = contents.indexOf("allWhitespace");
        assertUnknownConfidence(contents, offset, offset + "allWhitespace".length());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1076
    public void testDGM54() {
        //@formatter:off
        String contents =
            "void test(String[] strings) {\n" +
            "  strings.toString()\n" +
            "}\n";
        //@formatter:on
        assertExprType(contents, "toString", "java.lang.String");
        assertDeclType(contents, "toString", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1076
    public void testDGM54a() {
        //@formatter:off
        String contents =
            "void test(String[] strings) {\n" +
            "  strings.equals([])\n" +
            "}\n";
        //@formatter:on
        assertExprType(contents, "equals", "java.lang.Boolean");
        assertDeclType(contents, "equals", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1076
    public void testDGM54b() {
        //@formatter:off
        String contents =
            "void test(String[] strings) {\n" +
            "  [].equals(strings)\n" +
            "}\n";
        //@formatter:on
        assertExprType(contents, "equals", "java.lang.Boolean");
        assertDeclType(contents, "equals", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test
    public void testDGM55() {
        //@formatter:off
        String contents =
            "def str = '''\\\n" +
            "  blah blah\n" +
            "'''.stripIndent()\n";
        //@formatter:on
        assertExprType(contents, "stripIndent", "java.lang.String");
        float version = Float.parseFloat(System.getProperty("java.specification.version"));
        // Java 13+: @jdk.internal.PreviewFeature(feature=jdk.internal.PreviewFeature.Feature.TEXT_BLOCKS,essentialAPI=true)
        assertDeclType(contents, "stripIndent", version > 12 ? "java.lang.String" : "org.codehaus.groovy.runtime.StringGroovyMethods");
    }

    @Test // GRECLIPSE-1131
    public void testDGMClosure1() {
        //@formatter:off
        String contents =
            "1.each { it }\n";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.Object"); // not Integer because no @ClosureParams on this each
    }

    @Test // GRECLIPSE-1131
    public void testDGMClosure2() {
        //@formatter:off
        String contents =
            "each { it }\n";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.Object"); // not Search because no @ClosureParams on this each
    }

    @Test
    public void testDGMClosure3() {
        //@formatter:off
        String contents =
            "[''].each { it }\n";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.String");
    }

    @Test
    public void testDGMClosure4() {
        //@formatter:off
        String contents =
            "[''].reverseEach { val -> val }\n";
        //@formatter:on
        assertExprType(contents, "val", "java.lang.String");
    }

    @Test
    public void testDGMClosure5() {
        //@formatter:off
        String contents =
            "(1..4).find { it }\n";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure6() {
        //@formatter:off
        String contents =
            "['a':1].collect { it.key }\n";
        //@formatter:on
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGMClosure7() {
        //@formatter:off
        String contents =
            "['a':1].collect { it.value }\n";
        //@formatter:on
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test // Integer is explicit, so should use that as a type
    public void testDGMClosure8() {
        //@formatter:off
        String contents =
            "[''].reverseEach { Integer val -> val }\n";
        //@formatter:on
        assertExprType(contents, "val", "java.lang.Integer");
    }

    @Test // Integer is explicit, so should use that as a type
    public void testDGMClosure9() {
        //@formatter:off
        String contents =
            "[''].reverseEach { Integer it -> it }\n";
        //@formatter:on
        assertExprType(contents, "it", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure10() {
        //@formatter:off
        String contents =
            "[new Date()].eachWithIndex { val, i -> val }\n";
        //@formatter:on
        assertExprType(contents, "val", "java.util.Date");
    }

    @Test
    public void testDGMClosure11() {
        //@formatter:off
        String contents =
            "[''].eachWithIndex { val, i -> i }\n";
        //@formatter:on
        assertExprType(contents, "i", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure12() {
        //@formatter:off
        String contents =
            "[1:new Date()].eachWithIndex { key, val, i -> val }\n";
        //@formatter:on
        assertExprType(contents, "val", "java.util.Date");
    }

    @Test
    public void testDGMClosure13() {
        //@formatter:off
        String contents =
            "[1:new Date()].eachWithIndex { key, val, i -> key }\n";
        //@formatter:on
        assertExprType(contents, "key", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure14() {
        //@formatter:off
        String contents =
            "[1:new Date()].eachWithIndex { key, val, i -> i }\n";
        //@formatter:on
        assertExprType(contents, "i", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure15() {
        //@formatter:off
        String contents =
            "[1:new Date()].each { key, val -> key }\n";
        //@formatter:on
        assertExprType(contents, "key", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure16() {
        //@formatter:off
        String contents =
            "[1:new Date()].each { key, val -> val }\n";
        //@formatter:on
        assertExprType(contents, "val", "java.util.Date");
    }

    @Test
    public void testDGMClosure17() {
        //@formatter:off
        String contents =
            "[1:new Date()].collect { key, val -> key }\n";
        //@formatter:on
        assertExprType(contents, "key", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure18() {
        //@formatter:off
        String contents =
            "[1:new Date()].collect { key, val -> val }\n";
        //@formatter:on
        assertExprType(contents, "val", "java.util.Date");
    }

    @Test
    public void testDGMClosure19() {
        //@formatter:off
        String contents =
            "[1].unique { a, b -> b }\n";
        //@formatter:on
        assertExprType(contents, "b", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure20() {
        //@formatter:off
        String contents =
            "[1].unique { a, b -> a }\n";
        //@formatter:on
        assertExprType(contents, "a", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure21() {
        //@formatter:off
        String contents =
            "[1f: 1d].collectEntries { key, value -> [value, key] }\n";
        //@formatter:on
        assertExprType(contents, "value", "java.lang.Double");
    }

    @Test
    public void testDGMClosure22() {
        //@formatter:off
        String contents =
            "[1f: 1d].collectEntries { key, value -> [value, key] }\n";
        //@formatter:on
        assertExprType(contents, "key", "java.lang.Float");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/810
    public void testDGMClosure23() {
        //@formatter:off
        String contents =
            "class Bar { Number number }\n" +
            "class Foo implements Iterable<Bar> {\n" +
            "  List<Bar> bars\n" +
            "  Iterator<Bar> iterator() {\n" +
            "    return bars.iterator()\n" +
            "  }\n" +
            "  void test() {\n" +
            "    this.any { bar -> bar.number > 0 }\n" + // any(Object,Closure) vs. any(Iterable,Closure)
            "  }\n" +
            "}\n";
        //@formatter:on
        assertExprType(contents, "bar", "Bar"); // not "java.lang.Object"
    }

    @Test
    public void testDGMDeclaring1() {
        //@formatter:off
        String contents =
            "''.eachLine { line -> }\n";
        //@formatter:on
        assertDeclType(contents, "eachLine", "org.codehaus.groovy.runtime.StringGroovyMethods");
    }

    @Test
    public void testDGMDeclaring2() {
        //@formatter:off
        String contents =
            "new File().eachLine { line -> }\n";
        //@formatter:on
        assertDeclType(contents, "eachLine", "org.codehaus.groovy.runtime.ResourceGroovyMethods");
    }

    @Test
    public void testDGMDeclaring3() {
        //@formatter:off
        String contents =
            "Writer w\n" +
            "w.leftShift(null)\n";
        //@formatter:on
        assertDeclType(contents, "leftShift", "org.codehaus.groovy.runtime.IOGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1002
    public void testDGMDeclaring4() {
        String contents = "1.minus(0)";
        assertDeclType(contents, "minus", "org.codehaus.groovy.runtime.dgmimpl.NumberNumberMinus");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1002
    public void testDGMDeclaring5() {
        String contents = "new StringBuilder().size()";
        assertDeclType(contents, "size", isAtLeastGroovy(30)
            ? "org.codehaus.groovy.runtime.StringGroovyMethods"
            : "org.codehaus.groovy.vmplugin.v5.PluginDefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1192
    public void testDGMDeclaring6() {
        String contents = "String.&size";
        if (!isAtLeastGroovy(30)) {
            int offset = contents.indexOf("size");
            assertUnknownConfidence(contents, offset, offset + "size".length());
        } else {
            assertDeclType(contents, "size", "org.codehaus.groovy.runtime.StringGroovyMethods");
        }
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1002
    public void testDGMDeclaring7() {
        String contents = "['x','y','z'].stream().toList()";
        assertDeclType(contents, "toList", "org.codehaus.groovy.vmplugin.v8.PluginDefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1002
    public void testDGMDeclaring8() {
        String contents = "Thread.State.NEW.next().name()";
        String vmplugin = isAtLeastGroovy(30) ? "8" : "5";
        assertDeclType(contents, "next", "org.codehaus.groovy.vmplugin.v" + vmplugin + ".PluginDefaultGroovyMethods");
    }

    @Test
    public void testDGSMDeclaring1() {
        String contents = "sleep(42L)";
        assertDeclType(contents, "sleep", "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods");
    }

    @Test
    public void testDGSMDeclaring2() { // int argument vs. long parameter
        String contents = "void test(boolean flag) {\n  sleep(flag ? 42 : 1000)\n}";
        assertDeclType(contents, "sleep", "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods");
    }

    @Test
    public void testDGSMDeclaring3() {
        String contents = "Object.sleep(42)";
        assertDeclType(contents, "sleep", "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods");
    }

    @Test
    public void testDGSMDeclaring4() {
        String contents = "Date.sleep(42)"; // odd but works
        assertDeclType(contents, "sleep", "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods");
    }

    @Test
    public void testDGSMDeclaring5() {
        String contents = "Date.getLastMatcher()";
        int offset = contents.lastIndexOf("getLastMatcher");
        assertUnknownConfidence(contents, offset, offset + "getLastMatcher".length());
    }

    @Test
    public void testDGSMDeclaring6() {
        String contents = "java.util.regex.Matcher.getLastMatcher()";
        assertDeclType(contents, "getLastMatcher", "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods");
    }

    @Test
    public void testDGSMDeclaring7() {
        String contents = "java.util.regex.Matcher.lastMatcher";
        assertDeclType(contents, "lastMatcher", "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1192
    public void testDGSMDeclaring8() {
        String contents = "String.&sleep"; // odd but works
        if (!isAtLeastGroovy(30)) {
            int offset = contents.indexOf("sleep");
            assertUnknownConfidence(contents, offset, offset + "sleep".length());
        } else {
            assertDeclType(contents, "sleep", "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods");
        }
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1192
    public void testDGSMDeclaring9() {
        String contents = "Thread.&sleep"; // Thread#sleep(long) supersedes sleep(Object,long) but not sleep(Object,long,Closure)
        assertDeclType(contents, "sleep", !isAtLeastGroovy(30) ? "java.lang.Thread" : "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods");
    }

    @Test
    public void testDGSMDeclaring10() {
        String contents = "Thread.&mixin"; // throws MissingMethodException
        int offset = contents.indexOf("mixin");
        assertUnknownConfidence(contents, offset, offset + "mixin".length());
    }

    @Test
    public void testStaticMixinDGM() {
        //@formatter:off
        String contents =
            "class Parrot {\n" +
            "  static void echo(String self) {\n" +
            "    println \"Parrot says: $self\"\n" +
            "  }\n" +
            "}\n" +
            "String.mixin(Parrot)\n" +
            "'sqwak'.echo()";
        //@formatter:on
        assertDeclType(contents, "mixin", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
        //assertDeclType(contents, "echo", "Parrot"); // added to String using DGM.mixin(Class)
    }

    @Test
    public void testStaticWithDGM() {
        //@formatter:off
        String contents =
            "Date.with {\n" +
            "  delegate\n" + // type of delegate checked in ClosureInferencingTests
            "}\n";
        //@formatter:on
        assertDeclType(contents, "with", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/512
    public void testConflictWithDGM() {
        //@formatter:off
        createUnit("Reflections",
            "import java.lang.reflect.*\n" +
            "class Reflections {\n" +
            "  static Method findMethod(String methodName, Class<?> targetClass, Class<?>... paramTypes) {\n" +
            "  }\n" +
            "  static Object invokeMethod(Method method, Object target, Object... params) {\n" +
            "  }\n" +
            "}\n");

        String contents =
            "static void setThreadLocalProperty(String key, Object val) { Class target = null // redacted\n" +
            "  def setter = Reflections.findMethod('setThreadLocalProperty', target, String, Object)\n" +
            "  Reflections.invokeMethod(setter, target, key, val)\n" +
            "}\n";
        //@formatter:on
        assertDeclType(contents, "invokeMethod", "Reflections"); // not DefaultGroovyMethods
    }
}
