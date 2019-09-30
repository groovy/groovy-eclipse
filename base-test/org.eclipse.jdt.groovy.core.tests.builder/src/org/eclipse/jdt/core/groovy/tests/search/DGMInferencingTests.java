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

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assume.assumeFalse;

import java.util.Comparator;
import java.util.List;

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
        String contents = "1.with { it.intValue() }";
        assertExprType(contents, "it", "java.lang.Integer");
        assertDeclType(contents, "with", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test
    public void testDGM1a() {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  1.with { it.intValue() }" +
            "}";
        assertExprType(contents, "it", "java.lang.Integer");
        assertDeclType(contents, "with", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
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
        assertExprType(contents, "value", "java.lang.String");
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
        assertExprType(contents, "value", "java.lang.String");
    }

    @Test
    public void testDGM3() {
        String contents = "[1, [2, 3]].collectNested { it }";
        assertExprType(contents, "it", "java.lang.Object");
    }

    @Test
    public void testDGM4() {
        String contents = "[1, [2, 3], null].collectNested { it }";
        assertExprType(contents, "it", "java.lang.Object");
    }

    @Test
    public void testDGM5() {
        String contents = "[key:1].every { key, value -> key.toUpperCase() + value.intValue() }";
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM6() {
        String contents = "[key:1].any { key, value -> key.toUpperCase() + value.intValue() }";
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM7() {
        String contents = "[key:1].every { key, value -> key.toUpperCase() + value.intValue() }";
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM8() {
        String contents = "[key:1].any { key, value -> key.toUpperCase() + value.intValue() }";
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM9() {
        String contents = "[1].collectMany { [it.intValue()] }";
        assertExprType(contents, "it", "java.lang.Integer");
    }

    @Test @Ignore("Inferencing Engine gets tripped up with the different variants of 'metaClass'")
    public void testDGM10() {
        String contents = "Integer.metaClass { this }";
        assertExprType(contents, "this", "groovy.lang.MetaClass");
    }

    @Test
    public void testDGM11() {
        String contents = "([1] ).collectEntries { index -> index.intValue() }";
        assertExprType(contents, "index", "java.lang.Integer");
    }

    @Test
    public void testDGM12() {
        String contents = "[key:1].findResult(1) { key, value -> key.toUpperCase() + value.intValue() }";
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM13() {
        String contents = "[key:1].findResult(1) { key, value -> key.toUpperCase() + value.intValue() }";
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM14() {
        String contents = "[1].findResults { it.intValue() }";
        assertExprType(contents, "it", "java.lang.Integer");
    }

    @Test
    public void testDGM15() {
        String contents = "[key:1].findResults { it.getKey().toUpperCase() + it.getValue().intValue() }";
        assertExprType(contents, "it", "java.util.Map$Entry<java.lang.String,java.lang.Integer>");
    }

    @Test
    public void testDGM16() {
        String contents = "[key:1].findResults { key, value -> key.toUpperCase() + value.intValue() }";
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM17() {
        String contents = "[key:1].findResults { key, value -> key.toUpperCase() + value.intValue() }";
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM18() {
        String contents = "[key:1].findAll { key, value -> key.toUpperCase() + value.intValue() }";
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM19() {
        String contents = "[key:1].findAll { key, value -> key.toUpperCase() + value.intValue() }";
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM20() {
        String contents = "[key:1].groupBy { key, value -> key.toUpperCase() + value.intValue() }";
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM21() {
        String contents = "[key:1].groupBy { key, value -> key.toUpperCase() + value.intValue() }";
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM22() {
        String contents = "([1]).countBy { it.intValue() }";
        assertExprType(contents, "it", "java.lang.Integer");
    }

    @Test
    public void testDGM23() {
        String contents = "[key:1].groupEntriesBy { key, value -> key.toUpperCase() + value.intValue() }";
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM24() {
        String contents = "[key:1].groupEntriesBy { key, value -> key.toUpperCase() + value.intValue() }";
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM25() {
        String contents = "[key:1].inject(1.0) { seed, entry -> null }";
        assertExprType(contents, "seed", "java.math.BigDecimal");
    }

    @Test
    public void testDGM26() {
        String contents = "[key:1].inject(1.0) { seed, entry -> entry.key.toUpperCase() + entry.value.intValue() }";
        assertExprType(contents, "entry", "java.util.Map$Entry<java.lang.String,java.lang.Integer>");
    }

    @Test
    public void testDGM26a() {
        String contents = "[key:1].inject(1.0) { seed, key, value -> key.toUpperCase() + value.intValue() }";
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM26b() {
        String contents = "[key:1].inject(1.0) { seed, key, value -> key.toUpperCase() + value.intValue() }";
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testDGM27() {
        String contents = "[key:1].withDefault { key -> key.toUpperCase() }";
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGM28() { // withDefault expects one-param Closure
        String contents = "[key:1].withDefault { key, val -> key.toUpperCase() + val.intValue() }";
        assertExprType(contents, "val", "java.lang.Object");
    }

    @Test
    public void testDGM29() {
        String contents = "new FileOutputStream().withStream { it }";
        assertExprType(contents, "it", "java.io.FileOutputStream");
    }

    @Test
    public void testDGM30() {
        String contents = "new File('test').eachFileMatch(FileType.FILES, 1) { it.name }";
        assertExprType(contents, "it", "java.io.File");
    }

    @Test
    public void testDGM31() {
        String contents = "new File('test').eachDirMatch(1) { it.name }";
        assertExprType(contents, "it", "java.io.File");
    }

    @Test
    public void testDGM32() {
        String contents = "new File('test').withReader { it.reset() }";
        assertExprType(contents, "it", "java.io.BufferedReader");
    }

    @Test
    public void testDGM33() {
        String contents = "new FileReader(new File('test')).filterLine(new FileWriter(new File('test'))) { it.toUpperCase() }";
        assertExprType(contents, "it", "java.lang.String");
    }

    @Test
    public void testDGM34() {
        String contents = "new File('test').withOutputStream { it.flush() }";
        assertExprType(contents, "it", "java.io.OutputStream");
    }

    @Test
    public void testDGM35() {
        String contents = "new File('test').withInputStream { it.flush() }";
        assertExprType(contents, "it", "java.io.InputStream");
    }

    @Test
    public void testDGM36() {
        String contents = "new File('test').withDataOutputStream { it.flush() }";
        assertExprType(contents, "it", "java.io.DataOutputStream");
    }

    @Test
    public void testDGM37() {
        String contents = "new File('test').withDataInputStream { it.flush() }";
        assertExprType(contents, "it", "java.io.DataInputStream");
    }

    @Test
    public void testDGM38() {
        String contents = "new File('test').withWriter { it.flush() }";
        assertExprType(contents, "it", "java.io.BufferedWriter");
    }

    @Test
    public void testDGM39() {
        String contents = "new File('test').withWriterAppend { it.flush() }";
        assertExprType(contents, "it", "java.io.BufferedWriter");
    }

    @Test
    public void testDGM40() {
        String contents = "new File('test').withPrintWriter { it.flush() }";
        assertExprType(contents, "it", "java.io.PrintWriter");
    }

    @Test
    public void testDGM41() {
        String contents = "new FileReader(new File('test')).transformChar(new FileWriter(new File('test'))) { it.toUpperCase() }";
        assertExprType(contents, "it", "java.lang.String");
    }

    @Test
    public void testDGM42() {
        String contents = "new FileReader(new File('test')).transformLine(new FileWriter(new File('test'))) { it.toUpperCase() }";
        assertExprType(contents, "it", "java.lang.String");
    }

    @Test @Ignore("ClosureParams states 'List<String>' or 'String[]', but runtime allows for destructuring if number of elements fits into params")
    public void testDGM43() {
        String contents = "''.eachMatch('') { it.toLowerCase() }";
        assertExprType(contents, "it", "java.lang.String");
    }

    @Test // GRECLIPSE-1695
    public void testDGM44() {
        String contents = "List<String> list = []\n" +
            "list.toSorted { a, b ->\n" +
            "  a.trim() <=> b.trim()\n" +
            "}.each {\n" +
            "  it\n" +
            "}\n";
        assertExprType(contents, "it", "java.lang.String");
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
        assertExprType(contents, "it", "java.lang.String");
    }

    @Test @Ignore("each(T, Closure) lacks @ClosureParams metadata")
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
        assertExprType(contents, "it", jdkListSort ? "java.lang.Void" : "java.lang.String");
    }

    @Test
    public void testDGM46() {
        String contents = "java.util.regex.Pattern[] pats = [~/one/, ~/two/]\n" +
            "pats.eachWithIndex { pat, idx ->\n" + // T <T> eachWithIndex(T self, Closure task)
            "  \n" +
            "}\n";
        assertExprType(contents, "eachWithIndex", "java.util.regex.Pattern[]");
    }

    @Test
    public void testDGM47() throws Throwable {
        String contents = "java.util.regex.Pattern[] pats = [~/one/, ~/two/]\n" +
            "pats.eachWithIndex { pat, idx ->\n" +
            "  \n" +
            "}.collect {\n" + // <T> List<T> collect(Object self, Closure<T> xform)
            "  it\n" +
            "}\n";
        assertExprType(contents, "collect", "java.util.List<java.lang.Object>");
    }

    @Test
    public void testDGM48() {
        String contents = "int[] ints = [1, 2, 3]\n" +
            "String dgm(Object[] arr) {}\n" +
            "Object dgm(Object obj) {}\n" +
            "def result = dgm(ints)\n";
        assertExprType(contents, "result", "java.lang.Object");
    }

    @Test
    public void testDGM48a() {
        String contents = "int[] ints = [1, 2, 3]\n" +
            "Object dgm(Object obj) {}\n" +
            "String dgm(Object[] arr) {}\n" +
            "def result = dgm(ints)\n";
        assertExprType(contents, "result", "java.lang.Object");
    }

    @Test
    public void testDGM49() {
        // primitive array is not compatible with boxed-type array
        String contents = "int[] ints = [1, 2, 3]\n" +
            "Integer dgm(Integer[] arr) { null }\n" +
            "Object dgm(Object obj) { null }\n" +
            "def result = dgm(ints)\n";
        assertExprType(contents, "result", "java.lang.Object");
    }

    @Test
    public void testDGM50() {
        // SimpleTypeLookup returns first method in case of no type-compatible matches
        // TODO: primitive array is not compatible with derived-from-boxed-type array
        String contents = "int[] ints = [1, 2, 3]\n" +
            "Number dgm(Number[] arr) { null }\n" +
            "def result = dgm(ints)\n";
        assertExprType(contents, "result", "java.lang.Number");
        //assertUnknownConfidence(contents, start, end, "java.lang.Object", false);
    }

    @Test
    public void testDGM50a() {
        String contents = "Integer[] ints = [1, 2, 3]\n" +
            "Number dgm(Number[] arr) { null }\n" +
            "def result = dgm(ints)\n";
        assertExprType(contents, "result", "java.lang.Number");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/804
    public void testDGM51() {
        String contents = "def answer = (-42).&abs\n";
        assertExprType(contents, "abs", "java.lang.Integer");
    }

    @Test // GRECLIPSE-1131
    public void testDGMClosure1() {
        String contents = "1.each { it }";
        assertExprType(contents, "it", "java.lang.Object"); // not Integer because no @ClosureParams on this each
    }

    @Test // GRECLIPSE-1131
    public void testDGMClosure2() {
        String contents = "each { it }";
        assertExprType(contents, "it", "java.lang.Object"); // not Search because no @ClosureParams on this each
    }

    @Test
    public void testDGMClosure3() {
        String contents = "[''].each { it }";
        assertExprType(contents, "it", "java.lang.String");
    }

    @Test
    public void testDGMClosure4() {
        String contents = "[''].reverseEach { val -> val }";
        assertExprType(contents, "val", "java.lang.String");
    }

    @Test
    public void testDGMClosure5() {
        String contents = "(1..4).find { it }";
        assertExprType(contents, "it", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure6() {
        String contents = "['a':1].collect { it.key }";
        assertExprType(contents, "key", "java.lang.String");
    }

    @Test
    public void testDGMClosure7() {
        String contents = "['a':1].collect { it.value }";
        assertExprType(contents, "value", "java.lang.Integer");
    }

    @Test // Integer is explicit, so should use that as a type
    public void testDGMClosure8() {
        String contents = "[''].reverseEach { Integer val -> val }";
        assertExprType(contents, "val", "java.lang.Integer");
    }

    @Test // Integer is explicit, so should use that as a type
    public void testDGMClosure9() {
        String contents = "[''].reverseEach { Integer it -> it }";
        assertExprType(contents, "it", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure10() {
        String contents = "[new Date()].eachWithIndex { val, i -> val }";
        assertExprType(contents, "val", "java.util.Date");
    }

    @Test
    public void testDGMClosure11() {
        String contents = "[''].eachWithIndex { val, i -> i }";
        assertExprType(contents, "i", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure12() {
        String contents = "[1:new Date()].eachWithIndex { key, val, i -> val }";
        assertExprType(contents, "val", "java.util.Date");
    }

    @Test
    public void testDGMClosure13() {
        String contents = "[1:new Date()].eachWithIndex { key, val, i -> key }";
        assertExprType(contents, "key", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure14() {
        String contents = "[1:new Date()].eachWithIndex { key, val, i -> i }";
        assertExprType(contents, "i", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure15() {
        String contents = "[1:new Date()].each { key, val -> key }";
        assertExprType(contents, "key", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure16() {
        String contents = "[1:new Date()].each { key, val -> val }";
        assertExprType(contents, "val", "java.util.Date");
    }

    @Test
    public void testDGMClosure17() {
        String contents = "[1:new Date()].collect { key, val -> key }";
        assertExprType(contents, "key", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure18() {
        String contents = "[1:new Date()].collect { key, val -> val }";
        assertExprType(contents, "val", "java.util.Date");
    }

    @Test
    public void testDGMClosure19() {
        String contents = "[1].unique { a, b -> b }";
        assertExprType(contents, "b", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure20() {
        String contents = "[1].unique { a, b -> a }";
        assertExprType(contents, "a", "java.lang.Integer");
    }

    @Test
    public void testDGMClosure21() {
        String contents = "[1f: 1d].collectEntries { key, value -> [value, key] } ";
        assertExprType(contents, "value", "java.lang.Double");
    }

    @Test
    public void testDGMClosure22() {
        String contents = "[1f: 1d].collectEntries { key, value -> [value, key] } ";
        assertExprType(contents, "key", "java.lang.Float");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/810
    public void testDGMClosure23() {
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
        assertExprType(contents, "bar", "Bar"); // not "java.lang.Object"
    }

    @Test
    public void testDGMDeclaring1() {
        String contents = "''.eachLine { line -> }";
        assertDeclType(contents, "eachLine", "org.codehaus.groovy.runtime.StringGroovyMethods");
    }

    @Test
    public void testDGMDeclaring2() {
        String contents = "new File().eachLine { line -> }";
        assertDeclType(contents, "eachLine", "org.codehaus.groovy.runtime.ResourceGroovyMethods");
    }

    @Test
    public void testDGMDeclaring3() {
        String contents = "Writer w; w.leftShift(null)";
        assertDeclType(contents, "leftShift", "org.codehaus.groovy.runtime.IOGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/372
    public void testDGSMDeclaring1() {
        assumeFalse(isAtLeastGroovy(25));
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
        String contents = "Date.getLastMatcher()";
        int start = contents.lastIndexOf("getLastMatcher"), until = start + "getLastMatcher".length();
        assertUnknownConfidence(contents, start, until);
    }

    @Test
    public void testDGSMDeclaring5() {
        String contents = "java.util.regex.Matcher.getLastMatcher()";
        assertDeclType(contents, "getLastMatcher", "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods");
    }

    @Test
    public void testStaticMixinDGM() {
        String contents = "class Parrot { static void echo(String self) { println \"Parrot says: $self\" } }\nString.mixin(Parrot)\n'sqwak'.echo()";
        assertDeclType(contents, "mixin", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
      //assertDeclType(contents, "echo", "Parrot"); // added to String using DGM.mixin(Class)
    }

    @Test
    public void testStaticWithDGM() {
        String contents = "Date.with { delegate }"; // type of delegate checked in ClosureInferencingTests
        assertDeclType(contents, "with", "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/512
    public void testConflictWithDGM() {
        createUnit("Reflections", "import java.lang.reflect.*\n" +
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
        assertDeclType(contents, "invokeMethod", "Reflections"); // not DefaultGroovyMethods
    }
}
