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
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

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
        String contents =
            //@formatter:off
            "1.with { it.intValue() }";
            //@formatter:on

        assertExprType(contents, "it", "java.lang.Integer");
        assertDeclType(contents, "with", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test
    public void testDGM1a() {
        String contents =
            //@formatter:off
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
        String contents =
            //@formatter:off
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
        String contents =
            //@formatter:off
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
        String contents =
            //@formatter:off
            "[1, [2, 3]].collectNested { it }";
            //@formatter:on

        assertExprType(contents, "it", "java.lang.Object");
    }

    @Test
    public void testDGM4() {
        String contents =
            //@formatter:off
            "[1, [2, 3], null].collectNested { it }";
            //@formatter:on

        assertExprType(contents, "it", "java.lang.Object");
    }

    @Test
    public void testDGM5() {
        String contents =
            //@formatter:off
            "[key:1].every { key, value -> key.toUpperCase() + value.intValue() }";
            //@formatter:on

        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM6() {
        String contents =
            //@formatter:off
            "[key:1].any { key, value -> key.toUpperCase() + value.intValue() }";
            //@formatter:on

        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM7() {
        String contents =
            //@formatter:off
            "[key:1].every { key, value -> key.toUpperCase() + value.intValue() }";
            //@formatter:on

        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM8() {
        String contents =
            //@formatter:off
            "[key:1].any { key, value -> key.toUpperCase() + value.intValue() }";
            //@formatter:on

        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM9() {
        String contents =
            //@formatter:off
            "[1].collectMany { [it.intValue()] }";
            //@formatter:on

        assertExprType(contents, "it", "java.lang.Integer");
    }

    @Test
    public void testDGM10() {
        String contents =
            //@formatter:off
            "Integer.metaClass { this }"; // static MetaClass metaClass(Class self, Closure closure)
            //@formatter:on

        assertExprType(contents, "this", DEFAULT_UNIT_NAME);
        // TODO: When 'closure' has @ClosureParams and/or @DelegatesTo, check param(s) and delegate
    }

    @Test
    public void testDGM11() {
        String contents =
            //@formatter:off
            "([1]).collectEntries { index -> index.intValue() }";
            //@formatter:on

        assertExprType(contents, "index", "java.lang.Integer");
    }

    @Test
    public void testDGM12() {
        String contents =
            //@formatter:off
            "[key:1].findResult(1) { key, value -> key.toUpperCase() + value.intValue() }";
            //@formatter:on

        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM13() {
        String contents =
            //@formatter:off
            "[key:1].findResult(1) { key, value -> key.toUpperCase() + value.intValue() }";
            //@formatter:on

        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM14() {
        String contents =
            //@formatter:off
            "[1].findResults { it.intValue() }";
            //@formatter:on

        assertExprType(contents, "it", "java.lang.Integer");
    }

    @Test
    public void testDGM15() {
        String contents =
            //@formatter:off
            "[key:1].findResults { it.getKey().toUpperCase() + it.getValue().intValue() }";
            //@formatter:on

        assertExprType(contents, "it", "java.util.Map$Entry<java.lang.String,java.lang.Integer>");
    }

    @Test
    public void testDGM16() {
        String contents =
            //@formatter:off
            "[key:1].findResults { key, value -> key.toUpperCase() + value.intValue() }";
            //@formatter:on

        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM17() {
        String contents =
            //@formatter:off
            "[key:1].findResults { key, value -> key.toUpperCase() + value.intValue() }";
            //@formatter:on

        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM18() {
        String contents =
            //@formatter:off
            "[key:1].findAll { key, value -> key.toUpperCase() + value.intValue() }";
            //@formatter:on

        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM19() {
        String contents =
            //@formatter:off
            "[key:1].findAll { key, value -> key.toUpperCase() + value.intValue() }";
            //@formatter:on

        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM20() {
        String contents =
            //@formatter:off
            "[key:1].groupBy { key, value -> key.toUpperCase() + value.intValue() }";
            //@formatter:on

        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM21() {
        String contents =
            //@formatter:off
            "[key:1].groupBy { key, value -> key.toUpperCase() + value.intValue() }";
            //@formatter:on

        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM22() {
        String contents =
            //@formatter:off
            "([1]).countBy { it.intValue() }";
            //@formatter:on

        assertExprType(contents, "it", "java.lang.Integer");
    }

    @Test
    public void testDGM23() {
        String contents =
            //@formatter:off
            "[key:1].groupEntriesBy { key, value -> key.toUpperCase() + value.intValue() }";
            //@formatter:on

        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM24() {
        String contents =
            //@formatter:off
            "[key:1].groupEntriesBy { key, value -> key.toUpperCase() + value.intValue() }";
            //@formatter:on

        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM25() {
        String contents =
            //@formatter:off
            "[key:1].inject(1.0) { seed, entry -> null }";
            //@formatter:on

        assertExprType(contents, "seed", "java.math.BigDecimal");
    }

    @Test
    public void testDGM26() {
        String contents =
            //@formatter:off
            "[key:1].inject(1.0) { seed, entry -> entry.key.toUpperCase() + entry.value.intValue() }";
            //@formatter:on

        assertExprType(contents, "entry", "java.util.Map$Entry<java.lang.String,java.lang.Integer>");
    }

    @Test
    public void testDGM26a() {
        String contents =
            //@formatter:off
            "[key:1].inject(1.0) { seed, key, value -> key.toUpperCase() + value.intValue() }";
            //@formatter:on

        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM26b() {
        String contents =
            //@formatter:off
            "[key:1].inject(1.0) { seed, key, value -> key.toUpperCase() + value.intValue() }";
            //@formatter:on

        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM27() {
        String contents =
            //@formatter:off
            "[key:1].withDefault { key -> key.toUpperCase() }";
            //@formatter:on

        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM28() { // withDefault expects one-param Closure
        String contents =
            //@formatter:off
            "[key:1].withDefault { key, val -> key.toUpperCase() + val.intValue() }";
            //@formatter:on

        assertExprType(contents, "val", "java.lang.Object");
    }

    @Test
    public void testDGM29() {
        String contents =
            //@formatter:off
            "new FileOutputStream().withStream { it }";
            //@formatter:on

        assertExprType(contents, "it", "java.io.FileOutputStream");
    }

    @Test
    public void testDGM30() {
        String contents =
            //@formatter:off
            "import groovy.io.FileType\n" +
            "new File('test').eachFileMatch(FileType.FILES, 1) { it.name }";
            //@formatter:on

        assertExprType(contents, "it", "java.io.File");
    }

    @Test
    public void testDGM31() {
        String contents =
            //@formatter:off
            "new File('test').eachDirMatch(1) { it.name }";
            //@formatter:on

        assertExprType(contents, "it", "java.io.File");
    }

    @Test
    public void testDGM32() {
        String contents =
            //@formatter:off
            "new File('test').withReader { it.reset() }";
            //@formatter:on

        assertExprType(contents, "it", "java.io.BufferedReader");
    }

    @Test
    public void testDGM33() {
        String contents =
            //@formatter:off
            "new FileReader(new File('test')).filterLine(new FileWriter(new File('test'))) { it.toUpperCase() }";
            //@formatter:on

        assertExprType(contents, "it", "java.lang.String");
    }

    @Test
    public void testDGM34() {
        String contents =
            //@formatter:off
            "new File('test').withOutputStream { it.flush() }";
            //@formatter:on

        assertExprType(contents, "it", "java.io.OutputStream");
    }

    @Test
    public void testDGM35() {
        String contents =
            //@formatter:off
            "new File('test').withInputStream { it.flush() }";
            //@formatter:on

        assertExprType(contents, "it", "java.io.InputStream");
    }

    @Test
    public void testDGM36() {
        String contents =
            //@formatter:off
            "new File('test').withDataOutputStream { it.flush() }";
            //@formatter:on

        assertExprType(contents, "it", "java.io.DataOutputStream");
    }

    @Test
    public void testDGM37() {
        String contents =
            //@formatter:off
            "new File('test').withDataInputStream { it.flush() }";
            //@formatter:on

        assertExprType(contents, "it", "java.io.DataInputStream");
    }

    @Test
    public void testDGM38() {
        String contents =
            //@formatter:off
            "new File('test').withWriter { it.flush() }";
            //@formatter:on

        assertExprType(contents, "it", "java.io.BufferedWriter");
    }

    @Test
    public void testDGM39() {
        String contents =
            //@formatter:off
            "new File('test').withWriterAppend { it.flush() }";
            //@formatter:on

        assertExprType(contents, "it", "java.io.BufferedWriter");
    }

    @Test
    public void testDGM40() {
        String contents =
            //@formatter:off
            "new File('test').withPrintWriter { it.flush() }";
            //@formatter:on

        assertExprType(contents, "it", "java.io.PrintWriter");
    }

    @Test
    public void testDGM41() {
        String contents =
            //@formatter:off
            "new FileReader(new File('test')).transformChar(new FileWriter(new File('test'))) { it.toUpperCase() }";
            //@formatter:on

        assertExprType(contents, "it", "java.lang.String");
    }

    @Test
    public void testDGM42() {
        String contents =
            //@formatter:off
            "new FileReader(new File('test')).transformLine(new FileWriter(new File('test'))) { it.toUpperCase() }";
            //@formatter:on

        assertExprType(contents, "it", "java.lang.String");
    }

    @Test @Ignore("ClosureParams states 'List<String>' or 'String[]', but runtime allows for destructuring if number of elements fits into params")
    public void testDGM43() {
        String contents =
            //@formatter:off
            "''.eachMatch('') { it.toLowerCase() }";
            //@formatter:on

        assertExprType(contents, "it", "java.lang.String");
    }

    @Test // GRECLIPSE-1695
    public void testDGM44() {
        String contents =
            //@formatter:off
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
        String contents =
            //@formatter:off
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

        String contents =
            //@formatter:off
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
        String contents =
            //@formatter:off
            "String[] array = []\n" +
            "array.sort { a, b ->\n" +
            "  a.trim() <=> b.trim()\n" +
            "}\n";
            //@formatter:on

        assertDeclType(contents, "sort", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1077
    public void testDGM45c() {
        String contents =
            //@formatter:off
            "char[] array = []\n" +
            "array.sort()\n";
            //@formatter:on

        int offset = contents.indexOf("sort");
        assertUnknownConfidence(contents, offset, offset + 4);
    }

    @Test
    public void testDGM46() {
        String contents =
            //@formatter:off
            "java.util.regex.Pattern[] pats = [~/one/, ~/two/]\n" +
            "pats.eachWithIndex { pat, idx ->\n" + // T <T> eachWithIndex(T self, Closure task)
            "  \n" +
            "}\n";
            //@formatter:on

        assertExprType(contents, "eachWithIndex", "java.util.regex.Pattern[]");
    }

    @Test
    public void testDGM47() {
        String contents =
            //@formatter:off
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
        String contents =
            //@formatter:off
            "int[] ints = [1, 2, 3]\n" +
            "String dgm(Object[] arr) {}\n" +
            "Object dgm(Object obj) {}\n" +
            "def result = dgm(ints)\n";
            //@formatter:on

        assertExprType(contents, "result", "java.lang.Object");
    }

    @Test
    public void testDGM48a() {
        String contents =
            //@formatter:off
            "int[] ints = [1, 2, 3]\n" +
            "Object dgm(Object obj) {}\n" +
            "String dgm(Object[] arr) {}\n" +
            "def result = dgm(ints)\n";
            //@formatter:on

        assertExprType(contents, "result", "java.lang.Object");
    }

    @Test
    public void testDGM49() {
        // primitive array is not compatible with boxed-type array
        String contents =
            //@formatter:off
            "int[] ints = [1, 2, 3]\n" +
            "Integer dgm(Integer[] arr) { null }\n" +
            "Object dgm(Object obj) { null }\n" +
            "def result = dgm(ints)\n";
            //@formatter:on

        assertExprType(contents, "result", "java.lang.Object");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/804
    public void testDGM50() {
        String contents =
            //@formatter:off
            "def answer = (-42L).&abs\n";
            //@formatter:on

        assertExprType(contents, "abs", "java.lang.Long");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1027
    public void testDGM51() {
        String contents =
            //@formatter:off
            "def result = '42'.number\n";
            //@formatter:on

        assertExprType(contents, "number", "java.lang.Boolean");
        assertDeclType(contents, "number", "org.codehaus.groovy.runtime.StringGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1027
    public void testDGM52() {
        String contents =
            //@formatter:off
            "def result = ' '.allWhitespace\n";
            //@formatter:on

        assertExprType(contents, "allWhitespace", "java.lang.Boolean");
        assertDeclType(contents, "allWhitespace", "org.codehaus.groovy.runtime.StringGroovyMethods");
    }

    @Test
    public void testDGM53() {
        String contents =
            //@formatter:off
            "def result = ' '.&allWhitespace\n";
            //@formatter:on

        int offset = contents.indexOf("allWhitespace");
        assertUnknownConfidence(contents, offset, offset + "allWhitespace".length());
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1076
    public void testDGM54() {
        String contents =
            //@formatter:off
            "void test(String[] strings) {\n" +
            "  strings.toString()\n" +
            "}\n";
            //@formatter:on

        assertExprType(contents, "toString", "java.lang.String");
        assertDeclType(contents, "toString", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1076
    public void testDGM54a() {
        String contents =
            //@formatter:off
            "void test(String[] strings) {\n" +
            "  strings.equals([])\n" +
            "}\n";
            //@formatter:on

        assertExprType(contents, "equals", "java.lang.Boolean");
        assertDeclType(contents, "equals", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1076
    public void testDGM54b() {
        String contents =
            //@formatter:off
            "void test(String[] strings) {\n" +
            "  [].equals(strings)\n" +
            "}\n";
            //@formatter:on

        assertExprType(contents, "equals", "java.lang.Boolean");
        assertDeclType(contents, "equals", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test
    public void testDGM55() {
        String contents =
            //@formatter:off
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
        String contents =
            //@formatter:off
            "1.each { it }\n";
            //@formatter:on

        assertExprType(contents, "it", "java.lang.Object"); // not Integer because no @ClosureParams on this each
    }

    @Test // GRECLIPSE-1131
    public void testDGMClosure2() {
        String contents =
            //@formatter:off
            "each { it }\n";
            //@formatter:on

        assertExprType(contents, "it", "java.lang.Object"); // not Search because no @ClosureParams on this each
    }

    @Test
    public void testDGMClosure3() {
        String contents =
            //@formatter:off
            "[''].each { it }\n";
            //@formatter:on

        assertExprType(contents, "it", "java.lang.String");
    }

    @Test
    public void testDGMClosure4() {
        String contents =
            //@formatter:off
            "[''].reverseEach { val -> val }\n";
            //@formatter:on

        assertExprType(contents, "val", "java.lang.String");
    }

    @Test
    public void testDGMClosure5() {
        String contents =
            //@formatter:off
            "(1..4).find { it }\n";
            //@formatter:on

        assertExprType(contents, "it", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure6() {
        String contents =
            //@formatter:off
            "['a':1].collect { it.key }\n";
            //@formatter:on

        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGMClosure7() {
        String contents =
            //@formatter:off
            "['a':1].collect { it.value }\n";
            //@formatter:on

        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test // Integer is explicit, so should use that as a type
    public void testDGMClosure8() {
        String contents =
            //@formatter:off
            "[''].reverseEach { Integer val -> val }\n";
            //@formatter:on

        assertExprType(contents, "val", "java.lang.Integer");
    }

    @Test // Integer is explicit, so should use that as a type
    public void testDGMClosure9() {
        String contents =
            //@formatter:off
            "[''].reverseEach { Integer it -> it }\n";
            //@formatter:on

        assertExprType(contents, "it", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure10() {
        String contents =
            //@formatter:off
            "[new Date()].eachWithIndex { val, i -> val }\n";
            //@formatter:on

        assertExprType(contents, "val", "java.util.Date");
    }

    @Test
    public void testDGMClosure11() {
        String contents =
            //@formatter:off
            "[''].eachWithIndex { val, i -> i }\n";
            //@formatter:on

        assertExprType(contents, "i", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure12() {
        String contents =
            //@formatter:off
            "[1:new Date()].eachWithIndex { key, val, i -> val }\n";
            //@formatter:on

        assertExprType(contents, "val", "java.util.Date");
    }

    @Test
    public void testDGMClosure13() {
        String contents =
            //@formatter:off
            "[1:new Date()].eachWithIndex { key, val, i -> key }\n";
            //@formatter:on

        assertExprType(contents, "key", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure14() {
        String contents =
            //@formatter:off
            "[1:new Date()].eachWithIndex { key, val, i -> i }\n";
            //@formatter:on

        assertExprType(contents, "i", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure15() {
        String contents =
            //@formatter:off
            "[1:new Date()].each { key, val -> key }\n";
            //@formatter:on

        assertExprType(contents, "key", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure16() {
        String contents =
            //@formatter:off
            "[1:new Date()].each { key, val -> val }\n";
            //@formatter:on

        assertExprType(contents, "val", "java.util.Date");
    }

    @Test
    public void testDGMClosure17() {
        String contents =
            //@formatter:off
            "[1:new Date()].collect { key, val -> key }\n";
            //@formatter:on

        assertExprType(contents, "key", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure18() {
        String contents =
            //@formatter:off
            "[1:new Date()].collect { key, val -> val }\n";
            //@formatter:on

        assertExprType(contents, "val", "java.util.Date");
    }

    @Test
    public void testDGMClosure19() {
        String contents =
            //@formatter:off
            "[1].unique { a, b -> b }\n";
            //@formatter:on

        assertExprType(contents, "b", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure20() {
        String contents =
            //@formatter:off
            "[1].unique { a, b -> a }\n";
            //@formatter:on

        assertExprType(contents, "a", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure21() {
        String contents =
            //@formatter:off
            "[1f: 1d].collectEntries { key, value -> [value, key] }\n";
            //@formatter:on

        assertExprType(contents, "value", "java.lang.Double");
    }

    @Test
    public void testDGMClosure22() {
        String contents =
            //@formatter:off
            "[1f: 1d].collectEntries { key, value -> [value, key] }\n";
            //@formatter:on

        assertExprType(contents, "key", "java.lang.Float");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/810
    public void testDGMClosure23() {
        String contents =
            //@formatter:off
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
        String contents =
            //@formatter:off
            "''.eachLine { line -> }\n";
            //@formatter:on

        assertDeclType(contents, "eachLine", "org.codehaus.groovy.runtime.StringGroovyMethods");
    }

    @Test
    public void testDGMDeclaring2() {
        String contents =
            //@formatter:off
            "new File().eachLine { line -> }\n";
            //@formatter:on

        assertDeclType(contents, "eachLine", "org.codehaus.groovy.runtime.ResourceGroovyMethods");
    }

    @Test
    public void testDGMDeclaring3() {
        String contents =
            //@formatter:off
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
        assertDeclType(contents, "size", isAtLeastGroovy(30) ? "org.codehaus.groovy.runtime.StringGroovyMethods" : "org.codehaus.groovy.vmplugin.v5.PluginDefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1002
    public void testDGMDeclaring6() {
        assumeTrue(isAtLeastGroovy(25));

        String contents = "['x','y','z'].stream().toList()";
        assertDeclType(contents, "toList", "org.codehaus.groovy.vmplugin.v8.PluginDefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1002
    public void testDGMDeclaring7() {
        String contents = "Thread.State.NEW.next().name()";
        String vmplugin = isAtLeastGroovy(30) ? "8" : "5";
        assertDeclType(contents, "next", "org.codehaus.groovy.vmplugin.v" + vmplugin + ".PluginDefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/372
    public void testDGSMDeclaring1() {
        assumeFalse(isAtLeastGroovy(25)); // parse is deprecated

        String contents = "Date.parse('format', 'value')";
        assertDeclType(contents, "parse", "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods");
    }

    @Test
    public void testDGSMDeclaring2() {
        String contents = "Date.sleep(42)";
        assertDeclType(contents, "sleep", "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods");
    }

    @Test
    public void testDGSMDeclaring3() {
        String contents = "sleep 42";
        assertDeclType(contents, "sleep", "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods");
    }

    @Test
    public void testDGSMDeclaring4() {
        String contents = "void test(flag) {\n  sleep(flag ? 42 : 1000)\n}";
        assertDeclType(contents, "sleep", "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods");
    }

    @Test
    public void testDGSMDeclaring5() {
        String contents = "Date.getLastMatcher()";
        int start = contents.lastIndexOf("getLastMatcher"), until = start + "getLastMatcher".length();
        assertUnknownConfidence(contents, start, until);
    }

    @Test
    public void testDGSMDeclaring6() {
        String contents = "java.util.regex.Matcher.getLastMatcher()";
        assertDeclType(contents, "getLastMatcher", "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods");
    }

    @Test
    public void testStaticMixinDGM() {
        String contents =
            //@formatter:off
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
        String contents =
            //@formatter:off
            "Date.with {\n" +
            "  delegate\n" + // type of delegate checked in ClosureInferencingTests
            "}\n";
            //@formatter:on

        assertDeclType(contents, "with", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/512
    public void testConflictWithDGM() {
        createUnit("Reflections",
            //@formatter:off
            "import java.lang.reflect.*\n" +
            "class Reflections {\n" +
            "  static Method findMethod(String methodName, Class<?> targetClass, Class<?>... paramTypes) {\n" +
            "  }\n" +
            "  static Object invokeMethod(Method method, Object target, Object... params) {\n" +
            "  }\n" +
            "}\n");
            //@formatter:on

        String contents =
            //@formatter:off
            "static void setThreadLocalProperty(String key, Object val) { Class target = null // redacted\n" +
            "  def setter = Reflections.findMethod('setThreadLocalProperty', target, String, Object)\n" +
            "  Reflections.invokeMethod(setter, target, key, val)\n" +
            "}\n";
            //@formatter:on

        assertDeclType(contents, "invokeMethod", "Reflections"); // not DefaultGroovyMethods
    }
}
