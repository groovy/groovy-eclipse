/*
 * Copyright 2009-2016 the original author or authors.
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

import junit.framework.Test;

/**
 * Tests of closures inside DGM methods.
 *
 * @author Andrew Eisenberg
 * @created Sep 29, 2011
 */
public class DGMInferencingTests extends AbstractInferencingTest {

    public static Test suite() {
        return buildTestSuite(DGMInferencingTests.class);
    }

    public DGMInferencingTests(String name) {
        super(name);
    }

    public void testDGM1() throws Exception {
        String contents = "[1].collectNested { it }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testDGM2() throws Exception {
        String contents = "[1].collectNested { it }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testDGM3() throws Exception {
        String contents = "1.with { it.intValue() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testDGM4() throws Exception {
        String contents = "1.addShutdownHook { it.intValue() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testDGM5() throws Exception {
        String contents = "[key:1].every { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testDGM6() throws Exception {
        String contents = "[key:1].any { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testDGM7() throws Exception {
        String contents = "[key:1].every { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testDGM8() throws Exception {
        String contents = "[key:1].any { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testDGM9() throws Exception {
        String contents = "[1].collectMany { [it.intValue()] }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void _testDGM10() throws Exception {
        // this one is not working since Inferencing Engine gets tripped up with the different variants of 'metaClass'
        String contents = "Integer.metaClass { this }";
        String str = "this";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "groovy.lang.MetaClass");
    }
    public void testDGM11() throws Exception {
        String contents = "([1] ).collectEntries { index -> index.intValue() }";
        String str = "index";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testDGM12() throws Exception {
        String contents = "[key:1].findResult(1) { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testDGM13() throws Exception {
        String contents = "[key:1].findResult(1) { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testDGM14() throws Exception {
        String contents = "[1].findResults { it.intValue() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testDGM15() throws Exception {
        String contents = "[key:1].findResults { it.getKey().toUpperCase() + it.getValue().intValue() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.util.Map$Entry<java.lang.String,java.lang.Integer>");
    }
    public void testDGM16() throws Exception {
        String contents = "[key:1].findResults { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testDGM17() throws Exception {
        String contents = "[key:1].findResults { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testDGM18() throws Exception {
        String contents = "[key:1].findAll { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testDGM19() throws Exception {
        String contents = "[key:1].findAll { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testDGM20() throws Exception {
        String contents = "[key:1].groupBy { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testDGM21() throws Exception {
        String contents = "[key:1].groupBy { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testDGM22() throws Exception {
        String contents = "([1]).countBy { it.intValue() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testDGM23() throws Exception {
        String contents = "[key:1].groupEntriesBy { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testDGM24() throws Exception {
        String contents = "[key:1].groupEntriesBy { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testDGM25() throws Exception {
        String contents = "[key:1].inject(1) { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testDGM26() throws Exception {
        String contents = "[key:1].inject(1) { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testDGM27() throws Exception {
        String contents = "[key:1].withDefault { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "key";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testDGM28() throws Exception {
        String contents = "[key:1].withDefault { key, value -> key.toUpperCase() + value.intValue() }";
        String str = "value";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testDGM29() throws Exception {
        String contents = "new FileOutputStream().withStream { it }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.OutputStream");
    }
    public void testDGM30() throws Exception {
        String contents = "new File(\"test\").eachFileMatch(FileType.FILES, 1) { it.getName() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.File");
    }
    public void testDGM31() throws Exception {
        String contents = "new File(\"test\").eachDirMatch(FileType.FILES, 1) { it.getName() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.File");
    }
    public void testDGM32() throws Exception {
        String contents = "new File(\"test\").withReader { it.reset() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.BufferedReader");
    }
    public void testDGM34() throws Exception {
        String contents = "new File(\"test\").withOutputStream { it.flush() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.OutputStream");
    }
    public void testDGM35() throws Exception {
        String contents = "new File(\"test\").withInputStream { it.flush() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.InputStream");
    }
    public void testDGM36() throws Exception {
        String contents = "new File(\"test\").withDataOutputStream { it.flush() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.DataOutputStream");
    }
    public void testDGM37() throws Exception {
        String contents = "new File(\"test\").withDataInputStream { it.flush() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.DataInputStream");
    }
    public void testDGM38() throws Exception {
        String contents = "new File(\"test\").withWriter { it.flush() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.BufferedWriter");
    }
    public void testDGM39() throws Exception {
        String contents = "new File(\"test\").withWriterAppend { it.flush() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.BufferedWriter");
    }
    public void testDGM40() throws Exception {
        String contents = "new File(\"test\").withPrintWriter { it.flush() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.io.PrintWriter");
    }
    public void testDGM41() throws Exception {
        String contents = "new FileReader(new File(\"test\")).transformChar(new FileWriter(new File(\"test\"))) { it.toUpperCase() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testDGM42() throws Exception {
        String contents = "new FileReader(new File(\"test\")).transformLine(new FileWriter(new File(\"test\"))) { it.toUpperCase() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testDGM33() throws Exception {
        String contents = "new FileReader(new File(\"test\")).filterLine(new FileWriter(new File(\"test\"))) { it.toUpperCase() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testDGM43() throws Exception {
        String contents = "\"\".eachMatch(\"\") { it.toLowerCase() }";
        String str = "it";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testDGM_GRECLIPSE1695() throws Exception {
        String contents = "List<String> myList = new ArrayList<String>()\n" +
            "myList.toSorted { a, b ->\n" +
            "  a.trim() <=> b.trim()\n" +
            "}.each {\n" +
            "  it\n" +
            "}\n";
        int start = contents.lastIndexOf("it");
        int end = start + "it".length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testDGM_GRECLIPSE1695_redux() throws Throwable {
        // Java 8 adds default method sort(Comparator) to List interface...
        // TypeInferencingVisitorWithRequestor.lookupExpressionType replaces DGM (from CategoryTypeLookup) with JDK (from SimpleTypeLookup)
        String contents = "List<String> myList = new ArrayList<String>()\n" +
            "myList.sort { a, b ->\n" +
            "  a.trim() <=> b.trim()\n" +
            "}.each {\n" +
            "  it\n" +
            "}\n";
        int start = contents.lastIndexOf("it");
        int end = start + "it".length();
        assertTypeOneOf(contents, start, end, "java.lang.Void", "java.lang.String");
    }
    public void testDGMDeclaring1() throws Exception {
        // With groovy 2.0, there are some new DGM classes.  Need to ensure that we are using those classes as the declaring type, but only for 2.0 or later.
        String contents = "\"\".eachLine";
        String str = "eachLine";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        if (GroovyUtils.GROOVY_LEVEL >= 20) {
            assertDeclaringType(contents, start, end, "org.codehaus.groovy.runtime.StringGroovyMethods");
        } else {
            assertDeclaringType(contents, start, end, "org.codehaus.groovy.runtime.DefaultGroovyMethods");
        }
    }
    public void testDGMDeclaring2() throws Exception {
        String contents = "new File().eachLine";
        String str = "eachLine";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        if (GroovyUtils.GROOVY_LEVEL >= 20) {
            assertDeclaringType(contents, start, end, "org.codehaus.groovy.runtime.ResourceGroovyMethods");
        } else {
            assertDeclaringType(contents, start, end, "org.codehaus.groovy.runtime.DefaultGroovyMethods");
        }
    }
    public void testDGMDeclaring3() throws Exception {
        String contents = "Writer w\nw.leftShift";
        String str = "leftShift";
        int start = contents.lastIndexOf(str);
        int end = start + str.length();
        if (GroovyUtils.GROOVY_LEVEL >= 20) {
            assertDeclaringType(contents, start, end, "org.codehaus.groovy.runtime.IOGroovyMethods");
        } else {
            assertDeclaringType(contents, start, end, "org.codehaus.groovy.runtime.DefaultGroovyMethods");
        }
    }
}
