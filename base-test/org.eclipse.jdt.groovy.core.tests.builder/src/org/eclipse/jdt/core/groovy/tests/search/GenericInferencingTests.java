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

import java.util.Set;

import org.codehaus.groovy.ast.MethodNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.groovy.tests.compiler.ReconcilerUtils;
import org.eclipse.jdt.core.tests.util.GroovyUtils;

public final class GenericInferencingTests extends AbstractInferencingTest {

    public static junit.framework.Test suite() {
        return buildTestSuite(GenericInferencingTests.class);
    }

    public GenericInferencingTests(String name) {
        super(name);
    }

    public void testEnum1() {
        String contents =
            "Blah<Some> farb\n" +
            "farb.something().AA.other\n" +
            "enum Some {\n" +
            "    AA(List)\n" +
            "    public final Class<List<String>> other\n" +
            "    public Some(Class<List<String>> other) {\n" +
            "        this.other = other\n" +
            "    }\n" +
            "}\n" +
            "class Blah<K> {\n" +
            "    K something() {\n" +
            "    }\n" +
            "}";

        int start = contents.indexOf("other");
        int end = start + "other".length();
        assertType(contents, start, end, "java.lang.Class<java.util.List<java.lang.String>>");

    }

    public void testList1() {
        assertType("new LinkedList<String>()", "java.util.LinkedList<java.lang.String>");
    }

    public void testList2() {
        String contents ="def x = new LinkedList<String>()\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.util.LinkedList<java.lang.String>");
    }

    public void testList3() {
        String contents ="def x = [ '' ]\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.util.List<java.lang.String>");
    }

    public void testList4() {
        String contents ="def x = [ 1 ]\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.util.List<java.lang.Integer>");
    }

    public void testList5() {
        String contents = "[ 1 ].get(0)";
        String toFind = "get";
        int start = contents.indexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testList6() {
        String contents = "[ 1 ].iterator()";
        String toFind = "iterator";
        int start = contents.indexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Iterator<java.lang.Integer>");
    }

    public void testList7() {
        String contents = "[ 1 ].iterator().next()";
        String toFind = "next";
        int start = contents.indexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testList8() {
        String contents ="def x = []\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.util.List<java.lang.Object>");
    }

    public void testList9() {
        String contents = "def x = [] << ''; x";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, GroovyUtils.GROOVY_LEVEL < 24 ? "java.util.Collection<java.lang.String>" : "java.util.List<java.lang.String>");
    }

    // GRECLIPSE-1040
    public void testList10() {
        String contents = "def x = new LinkedList()\nx";
        String toFind = "x";
        int start = contents.indexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.LinkedList");
    }

    // GRECLIPSE-1040
    public void testSet1() {
        String contents = "def x = new HashSet()\nx";
        String toFind = "x";
        int start = contents.indexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.HashSet");
    }

    public void testMap1() {
        String contents = "new HashMap<String,Integer>()";
        assertType(contents, "java.util.HashMap<java.lang.String,java.lang.Integer>");
    }

    public void testMap2() {
        String contents = "def x = new HashMap<String,Integer>()\nx";
        String toFind = "x";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.HashMap<java.lang.String,java.lang.Integer>");
    }

    public void testMap3() {
        String contents = "Map<String,Integer> x\nx.entrySet().iterator().next().value";
        String toFind = "entrySet";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.Integer>>");
    }

    public void testMap4() {
        String contents = "def x = new HashMap<String,Integer>()\nx.entrySet().iterator().next().key";
        String toFind = "key";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.String");
    }

    public void testMap5() {
        String contents = "def x = new HashMap<String,Integer>()\nx.entrySet().iterator().next().value";
        String toFind = "value";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testMap6() {
        String contents = "Map<String,Integer> x\nx.entrySet().iterator().next().value";
        String toFind = "value";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testMap7() {
        String contents = "[ 1:1 ]";
        assertType(contents, "java.util.Map<java.lang.Integer,java.lang.Integer>");
    }

    public void testMap8() {
        String contents = "[ 1:1 ].entrySet()";
        String toFind = "entrySet";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Set<java.util.Map$Entry<java.lang.Integer,java.lang.Integer>>");
    }

    public void testMap9() {
        String contents = "Map<Integer, Integer> x() { }\ndef f = x()\nf";
        String toFind = "f";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Map<java.lang.Integer,java.lang.Integer>");
    }

    // GRECLIPSE-1040
    public void testMap10() {
        String contents = "new HashMap()";
        assertType(contents, "java.util.HashMap");
    }

    public void testMapOfList() {
        String contents = "Map<String,List<Integer>> x\nx.entrySet().iterator().next().value";
        String toFind = "value";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.List<java.lang.Integer>");
    }

    public void testMapOfList2() {
        String contents = "Map<String,List<Integer>> x\nx.entrySet().iterator().next().value.iterator().next()";
        String toFind = "next";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testMapOfList3() {
        String contents = "def x = [1: [1]]\nx.entrySet().iterator().next().key";
        String toFind = "key";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    // not working yet since our approach to determining the type of a map only looks at the static types of the
    // first elements.  It does not try to infer the type of these elements.
    public void testMapOfList4() {
        String contents = "def x = [1: [1]]\nx.entrySet().iterator().next().value.iterator().next()";
        String toFind = "next";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testMapOfList5() {
        String contents = "def x = [1: [1]]\nx.entrySet().iterator().next().value.iterator().next()";
        String toFind = "next";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testMapOfList6() {
        String contents = "Map<String, Map<Integer, List<Date>>> map\n" +
            "def x = map.get('foo').get(5).get(2)\n" +
            "x";
        String toFind = "x";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Date");
    }

    // GRECLIPSE-941
    public void testMapOfList7() {
        String contents = "Map<String, Map<Integer, List<Date>>> map\n" +
            "def x = map['foo'][5][2]\n" +
            "x";
        String toFind = "x";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Date");
    }

    public void testForLoop1() {
        String contents = "def x = 1..4\nfor (a in x) { \na }";
        String toFind = "a";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testForLoop2() {
        String contents = "for (a in 1..4) { \na }";
        String toFind = "a";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testForLoop3() {
        String contents = "for (a in [1, 2].iterator()) { \na }";
        String toFind = "a";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testForLoop4() {
        String contents = "for (a in (1..4).iterator()) { \na }";
        String toFind = "a";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testForLoop5() {
        String contents = "for (a in [1 : 1]) { \na.key }";
        String toFind = "key";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testForLoop6() {
        String contents = "for (a in [1 : 1]) { \na.value }";
        String toFind = "value";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testForLoop7() {
        String contents = "InputStream x\nfor (a in x) { \na }";
        String toFind = "a";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Byte");
    }

    public void testForLoop8() {
        String contents = "DataInputStream x\nfor (a in x) { \na }";
        String toFind = "a";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Byte");
    }

    public void testForLoop9() {
        String contents = "class X {\n" +
            "  List<String> images\n" +
            "}\n" +
            "def sample = new X()\n" + "for (img in sample.images) {\n" +
            "  img\n" +
            "}";
        String toFind = "img";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.String");
    }

    public void testForLoop11() {
        String contents =
            "class X {\n" +
            " public void m() {\n" +
            "  List<String> ls = new ArrayList<String>();\n" +
            "  for (foo in ls) {\n" +
            "   foo\n" +
            "  }\n" +
            " }\n" +
            "}\n";
        String toFind = "foo";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.String");
    }

    public void testClosure1() {
        String contents = "def fn = { int a, int b -> a + b }";
        assertType(contents, 4, 6, "groovy.lang.Closure<java.lang.Integer>");
    }

    public void testClosure2() {
        String contents = "def fn = 'abc'.&length";
        assertType(contents, 4, 6, "groovy.lang.Closure<java.lang.Integer>");
    }

    public void testClosure3() {
        String contents = "def fn = Collections.&emptyList";
        assertType(contents, 4, 6, "groovy.lang.Closure<java.util.List<T>>");
    }

    public void testClosure4() {
        String contents = "def fn = (String.&trim) >> (Class.&forName)";
        assertType(contents, 4, 6, GroovyUtils.GROOVY_LEVEL > 23 ? "groovy.lang.Closure<java.lang.Class<?>>" : "groovy.lang.Closure<java.lang.Class<? extends java.lang.Object>>");
    }

    public void testDGM() {
        String contents = "String[] strings = ['1', '2'];  strings.iterator()";
        String toFind = "iterator";
        int start = contents.lastIndexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.util.Iterator<java.lang.String>");
        MethodNode dgm = assertDeclaration(contents, start, end, "org.codehaus.groovy.runtime.DefaultGroovyMethods", "iterator", DeclarationKind.METHOD);
        assertEquals("First parameter type should be resolved from object expression", "java.lang.String[]", printTypeName(dgm.getParameters()[0].getType()));
    }

    // all testing for GRECLIPSE-833
    public void testDGMClosure1() {
        String contents = "[''].each { it }";
        String toFind = "it";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.String");
    }

    public void testDGMClosure2() {
        String contents = "[''].reverseEach { val -> val }";
        String toFind = "val";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.String");
    }

    public void testDGMClosure3() {
        if (GroovyUtils.GROOVY_LEVEL < 21) {
            return;
        }
        String contents = "(1..4).find { it }";
        String toFind = "it";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testDGMClosure4() {
        String contents = "['a':1].unique { it.key }";
        String toFind = "key";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.String");
    }

    public void testDGMClosure5() {
        String contents = "['a':1].collect { it.value }";
        String toFind = "value";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    // Integer is explicit, so should use that as a type
    public void testDGMClosure7() {
        String contents = "[''].reverseEach { Integer val -> val }";
        String toFind = "val";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    // Integer is explicit, so should use that as a type
    public void testDGMClosure8() {
        String contents = "[''].reverseEach { Integer it -> it }";
        String toFind = "it";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testDGMClosure9() {
        String contents = "[new Date()].eachWithIndex { val, i -> val }";
        String toFind = "val";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Date");
    }

    public void testDGMClosure10() {
        String contents = "[''].eachWithIndex { val, i -> i }";
        String toFind = "i";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testDGMClosure11() {
        String contents = "[1:new Date()].eachWithIndex { key, val, i -> val }";
        String toFind = "val";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Date");
    }

    public void testDGMClosure12() {
        String contents = "[1:new Date()].eachWithIndex { key, val, i -> key }";
        String toFind = "key";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testDGMClosure13() {
        String contents = "[1:new Date()].eachWithIndex { key, val, i -> i }";
        String toFind = "i";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testDGMClosure14() {
        String contents = "[1:new Date()].each { key, val -> key }";
        String toFind = "key";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testDGMClosure15() {
        String contents = "[1:new Date()].each { key, val -> val }";
        String toFind = "val";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Date");
    }

    public void testDGMClosure16() {
        String contents = "[1:new Date()].collect { key, val -> key }";
        String toFind = "key";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testDGMClosure17() {
        String contents = "[1:new Date()].collect { key, val -> val }";
        String toFind = "val";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Date");
    }

    public void testDGMClosure18() {
        String contents = "[1].inject { a, b -> a }";
        String toFind = "a";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testDGMClosure19() {
        String contents = "[1].inject { a, b -> b }";
        String toFind = "b";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testDGMClosure20() {
        String contents = "[1].unique { a, b -> b }";
        String toFind = "b";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testDGMClosure21() {
        String contents = "[1].unique { a, b -> a }";
        String toFind = "a";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testDGMClosure22() {
        String contents = "[1f: 1d].collectEntries { key, value -> [value, key] } ";
        String toFind = "value";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Double");
    }

    public void testDGMClosure23() {
        String contents = "[1f: 1d].collectEntries { key, value -> [value, key] } ";
        String toFind = "key";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Float");
    }

    // GRECLIPSE-997
    public void testNestedGenerics1() {
        String contents =
            "class MyMap extends HashMap<String,Class> { }\n" +
            "MyMap m\n" +
            "m.get()";
        String toFind = "get";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Class");
    }

    // GRECLIPSE-997
    public void testNestedGenerics2() {
        String contents =
            "class MyMap extends HashMap<String,Class> { }\n" +
            "MyMap m\n" +
            "m.entrySet()";
        String toFind = "entrySet";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.Class>>");
    }

    // GRECLIPSE-997
    public void testNestedGenerics3() {
        String contents =
            "import java.lang.ref.WeakReference\n" +
            "class MyMap<K,V> extends HashMap<K,WeakReference<V>>{ }\n" +
            "MyMap<String,Class> m\n" +
            "m.entrySet()";
        String toFind = "entrySet";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.ref.WeakReference<java.lang.Class>>>");
    }

    // GRECLIPSE-997
    public void testNestedGenerics4() {
        String contents =
            "import java.lang.ref.WeakReference\n" +
            "class MyMap<K,V> extends HashMap<K,WeakReference<V>>{ }\n" +
            "class MySubMap extends MyMap<String,Class>{ }\n" +
            "MySubMap m\n" +
            "m.entrySet()";
        String toFind = "entrySet";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.ref.WeakReference<java.lang.Class>>>");
    }

    // GRECLIPSE-997
    public void testNestedGenerics5() {
        String contents =
            "import java.lang.ref.WeakReference\n" +
            "class MyMap<K,V> extends HashMap<K,WeakReference<V>>{ }\n" +
            "class MySubMap<L> extends MyMap<String,Class>{ \n" +
            "  Map<L,Class> val\n" +
            "}\n" +
            "MySubMap<Integer> m\n" +
            "m.val";
        String toFind = "val";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Map<java.lang.Integer,java.lang.Class>");
    }

    // GRECLIPSE-997
    public void testNestedGenerics6() {
        String contents =
            "import java.lang.ref.WeakReference\n" +
            "class MyMap<K,V> extends HashMap<K,WeakReference<List<K>>>{ }\n" +
            "class MySubMap extends MyMap<String,Class>{ }\n" +
            "MySubMap m\n" +
            "m.entrySet()";
        String toFind = "entrySet";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.ref.WeakReference<java.util.List<java.lang.String>>>>");
    }

    // GRECLIPSE-997
    public void testNestedGenerics7() {
        String contents =
            "class MyMap<K,V> extends HashMap<V,K>{ }\n" +
            "MyMap<Integer,Class> m\n" +
            "m.get";
        String toFind = "get";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    // GRECLIPSE-997
    public void testNestedGenerics8() {
        String contents =
            "class MyMap<K,V> extends HashMap<K,V>{\n" +
            "Map<V,Class<K>> val}\n" +
            "MyMap<Integer,Class> m\n" +
            "m.val";
        String toFind = "val";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Map<java.lang.Class,java.lang.Class<java.lang.Integer>>");
    }

    // GRECLIPSE-1131
    public void testEachOnNonIterables1() {
        String contents = "1.each { it }";
        String toFind = "it";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    // GRECLIPSE-1131
    public void testEachOnNonIterables2() {
        String contents = "each { it }";
        String toFind = "it";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "Search");
    }

    // GRECLIPSE-1131
    public void testEachOnNonIterables3() {
        String contents = "1.reverseEach { it }";
        String toFind = "it";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testInferringGetClass1() {
        String contents = "''.getClass()", toFind = "getClass";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Class<? extends java.lang.String>");
    }

    public void testInferringGetClass2() {
        String contents = "[''].getClass()", toFind = "getClass";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Class<? extends java.util.List<java.lang.String>>");
    }

    public void testInferringGetClass3() {
        String contents = "[a:''].getClass()", toFind = "getClass";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Class<? extends java.util.Map<java.lang.String,java.lang.String>>");
    }

    public void testInferringList1() {
        String contents = "def x = 9\ndef xxx = [x]\nxxx";
        String toFind = "xxx";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.List<java.lang.Integer>");
    }

    public void testInferringList2() {
        String contents = "def x = 9\ndef xxx = [x, '']\nxxx";
        String toFind = "xxx";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.List<java.lang.Integer>");
    }

    public void testInferringList3() {
        String contents = "def x = 9\ndef xxx = [x+9*8, '']\nxxx";
        String toFind = "xxx";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.List<java.lang.Integer>");
    }

    public void testInferringRange1() {
        String contents = "def x = 9\ndef xxx = x..x\nxxx";
        String toFind = "xxx";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "groovy.lang.Range<java.lang.Integer>");
    }

    public void testInferringRange2() {
        String contents = "def x = 9\ndef xxx = (x*1)..x\nxxx";
        String toFind = "xxx";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "groovy.lang.Range<java.lang.Integer>");
    }

    public void testInferringMap1() {
        String contents = "def x = 9\ndef y = false\ndef xxx = [(x):y]\nxxx";
        String toFind = "xxx";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Map<java.lang.Integer,java.lang.Boolean>");
    }

    public void testInferringMap2() {
        String contents = "def x = 9\ndef y = false\ndef xxx = [(x+x):!y]\nxxx";
        String toFind = "xxx";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Map<java.lang.Integer,java.lang.Boolean>");
    }

    public void testInferringMap3() {
        String contents = "def x = 9\ndef y = false\ndef xxx = [(x+x):!y, a:'a', b:'b']\nxxx";
        String toFind = "xxx";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Map<java.lang.Integer,java.lang.Boolean>");
    }

    public void testInferringMap4() {
        String contents = "def x = 9\ndef y = false\ndef xxx = [[(x+x):!y, a:'a', b:'b']]\nxxx";
        String toFind = "xxx";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.List<java.util.Map<java.lang.Integer,java.lang.Boolean>>");
    }

    public void testInferringMap5() {
        String contents = "def x = [ ['a':11, 'b':12] : ['a':21, 'b':22] ]\n" +
            "def xxx = x\n" +
            "xxx";
        String toFind = "xxx";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Map<java.util.Map<java.lang.String,java.lang.Integer>,java.util.Map<java.lang.String,java.lang.Integer>>");
    }

    public void testInferringMap6() {
        String contents = "def x = [ ['a':11, 'b':12], ['a':21, 'b':22] ]\n" +
            "def xxx = x*.a\n" +
            "xxx";
        String toFind = "xxx";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.List<java.lang.Integer>");
    }

    // GRECLIPSE-1696
    // Generic method type inference with @CompileStatic
    public void testMethod1() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;
        String contents =
            "import groovy.transform.CompileStatic\n" +
            "class A {\n" +
            "    public <T> T myMethod(Class<T> claz) {\n" +
            "        return null\n" +
            "    }\n" +
            "    @CompileStatic\n" +
            "    static void main(String[] args) {\n" +
            "        A a = new A()\n" +
            "        def val = a.myMethod(String.class)\n" +
            "        val.trim()\n" +
            "    }\n" +
            "}";
        int start = contents.lastIndexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");
    }

    // Generic method type inference without @CompileStatic
    public void testMethod2() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;
        String contents =
            "class A {\n" +
            "    public <T> T myMethod(Class<T> claz) {\n" +
            "        return null\n" +
            "    }\n" +
            "    static void main(String[] args) {\n" +
            "        A a = new A()\n" +
            "        def val = a.myMethod(String.class)\n" +
            "        val.trim()\n" +
            "    }\n" +
            "}";
        int start = contents.lastIndexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");
    }

    // Generic method without object type inference with @CompileStatic
    public void testMethod3() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;
        String contents =
            "import groovy.transform.CompileStatic\n" +
            "class A {\n" +
            "    public <T> T myMethod(Class<T> claz) {\n" +
            "        return null\n" +
            "    }\n" +
            "    @CompileStatic\n" +
            "    def m() {\n" +
            "        def val = myMethod(String.class)\n" +
            "        val.trim()\n" +
            "    }\n" +
            "}";
        int start = contents.lastIndexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");
    }

    // Generic method type without object inference without @CompileStatic
    public void testMethod4() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;
        String contents =
            "class A {\n" +
            "    public <T> T myMethod(Class<T> claz) {\n" +
            "        return null\n" +
            "    }\n" +
            "    def m() {\n" +
            "        def val = myMethod(String.class)\n" +
            "        val.trim()\n" +
            "    }\n" +
            "}";
        int start = contents.lastIndexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");
    }

    // GRECLIPSE-1129
    // Static generic method type inference with @CompileStatic
    public void testStaticMethod1() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;
        String contents =
            "class A {\n" +
            "    static <T> T myMethod(Class<T> claz) {\n" +
            "        return null\n" +
            "    }\n" +
            "    @groovy.transform.CompileStatic\n" +
            "    static void main(String[] args) {\n" +
            "        def val = A.myMethod(String.class)\n" +
            "        val.trim()\n" +
            "    }\n" +
            "}";
        int start = contents.lastIndexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");
    }

    // Static generic method type inference without @CompileStatic
    public void testStaticMethod2() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;
        String contents =
            "class A {\n" +
            "    static <T> T myMethod(Class<T> claz) {\n" +
            "        return null\n" +
            "    }\n" +
            "    static void main(String[] args) {\n" +
            "        def val = A.myMethod(String.class)\n" +
            "        val.trim()\n" +
            "    }\n" +
            "}";
        int start = contents.lastIndexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");
    }

    // Static generic method without class type inference with @CompileStatic
    public void testStaticMethod3() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;
        String contents =
            "class A {\n" +
            "    static <T> T myMethod(Class<T> claz) {\n" +
            "        return null\n" +
            "    }\n" +
            "    @groovy.transform.CompileStatic\n" +
            "    def m() {\n" +
            "        def val = myMethod(String.class)\n" +
            "        val.trim()\n" +
            "    }\n" +
            "}";
        int start = contents.lastIndexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");
    }

    // Static generic method without class type inference without @CompileStatic
    public void testStaticMethod4() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;
        String contents =
            "class A {\n" +
            "    static <T> T myMethod(Class<T> claz) {\n" +
            "        return null\n" +
            "    }\n" +
            "    def m() {\n" +
            "        def val = myMethod(String.class)\n" +
            "        val.trim()\n" +
            "    }\n" +
            "}";
        int start = contents.lastIndexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");
    }

    // Test according GRECLIPSE-1129 description
    public void testStaticMethod5() {
        if (GroovyUtils.GROOVY_LEVEL < 23) return;
        String contents =
            "class A {}\n" +
            "class B extends A {}\n" +
            "static <T extends A> T loadSomething(T t) {\n" +
            "  return t\n" +
            "}\n" +
            "def val = loadSomething(new B())";
        int start = contents.lastIndexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "B");
    }

    // Additional test according comment to PR #75
    // Actually type should not be inferred for fields with type def
    public void testStaticMethod6() {
        String contents =
            "class A {}\n" +
            "class B extends A {}\n" +
            "class C {\n" +
            "    static <T extends A> T loadSomething(T t) {\n" +
            "        return t\n" +
            "    }\n" +
            "    def col = loadSomething(new B())\n" +
            "    def m() { col }" +
            "}";
        int start = contents.lastIndexOf("col");
        int end = start + "col".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    public void testStaticMethod7() {
        // Collections: public static final <T> List<T> singletonList(T)
        String contents = "List<String> list = Collections.singletonList('')";
        String toFind = "singletonList";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.util.List<java.lang.String>");
        MethodNode m = assertDeclaration(contents, start, end, "java.util.Collections", toFind, DeclarationKind.METHOD);
        assertEquals("Parameter type should be resolved", "java.lang.String", printTypeName(m.getParameters()[0].getType()));
    }

    public void _testStaticMethod8() { // no help from parameters
        // Collections: public static final <T> List<T> emptyList()
        String contents = "List<String> list = Collections.emptyList()";
        String toFind = "emptyList";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.util.List<java.lang.String>");
    }

    public void testStaticMethod9() {
        // Collections: public static final <T> List<T> emptyList()
        String contents = "def list = Collections.<String>emptyList()";
        String toFind = "list";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.util.List<java.lang.String>");
    }

    public void testStaticMethod10() {
        // Collection: public boolean removeAll(Collection<?>)
        String contents = "List<String> list = ['1','2']; list.removeAll(['1'])";
        String toFind = "removeAll";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Boolean");
        MethodNode m = assertDeclaration(contents, start, end, "java.util.Collection<java.lang.String>", toFind, DeclarationKind.METHOD);
        assertEquals("Parameter type should be resolved", "java.util.Collection<?>", printTypeName(m.getParameters()[0].getType()));
    }

    public void testStaticMethod11() {
        // Collection: public boolean addAll(Collection<? extends E>)
        String contents = "List<String> list = ['1','2']; list.addAll(['3'])";
        String toFind = "addAll";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Boolean");
        MethodNode m = assertDeclaration(contents, start, end, "java.util.Collection<java.lang.String>", toFind, DeclarationKind.METHOD);
        assertEquals("Parameter type should be resolved", "java.util.Collection<? extends java.lang.String>", printTypeName(m.getParameters()[0].getType()));
    }

    public void testStaticMethodOverloads1() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) { obj }\n" +
            "  static <T extends CharSequence> T b(T seq) { seq }\n" +
            "}\n" +
            "def result = A.b([])";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Object"); // should not satisfy bounds of T
    }

    public void testStaticMethodOverloads2() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) { obj }\n" +
            "  static <T extends CharSequence> T b(T seq) { seq }\n" +
            "}\n" +
            "def result = A.b('')";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.String"); // should satisfy bounds of T
    }

    public void testStaticMethodOverloads3() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) { obj }\n" +
            "  static <T extends CharSequence & Serializable> T b(T seq) { seq }\n" +
            "}\n" +
            "def result = A.b([])";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Object"); // should not satisfy bounds of T
    }

    public void testStaticMethodOverloads4() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) { obj }\n" +
            "  static <T extends CharSequence & Iterable> T b(T seq) { seq }\n" +
            "}\n" +
            "def result = A.b('')";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Object"); // should not satisfy bounds of T
    }

    public void testStaticMethodOverloads5() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) { obj }\n" +
            "  static <T extends CharSequence & Serializable> T b(T seq) { seq }\n" +
            "}\n" +
            "def result = A.b('')";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.String"); // should satisfy bounds of T
    }

    public void testStaticMethodOverloads6() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) { obj }\n" +
            "  static <T extends CharSequence> T b(T[] seq) { seq }\n" +
            "}\n" +
            "def result = A.b(new Object[0])";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Object"); // should not satisfy bounds of T
    }

    public void testStaticMethodOverloads7() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) { obj }\n" +
            "  static <T extends CharSequence> T b(T[] seq) { seq }\n" +
            "}\n" +
            "def result = A.b(new String[0])";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.String"); // should satisfy bounds of T
    }

    public void testStaticMethodOverloads8() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) { obj }\n" +
            "  static <T extends CharSequence & Serializable> T b(T[] seq) { seq }\n" +
            "}\n" +
            "def result = A.b(new Object[0])";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Object"); // should not satisfy bounds of T
    }

    public void testStaticMethodOverloads9() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) { obj }\n" +
            "  static <T extends CharSequence & Iterable> T b(T[] seq) { seq }\n" +
            "}\n" +
            "def result = A.b(new String[0])";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Object"); // should not satisfy bounds of T
    }

    public void testStaticMethodOverloads10() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) { obj }\n" +
            "  static <T extends CharSequence & Serializable> T b(T[] seq) { seq }\n" +
            "}\n" +
            "def result = A.b(new String[0])";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.String"); // should satisfy bounds of T
    }

    public void testStaticMethodOverloads11() {
        String contents =
            "class Preconditions {\n" +
            "  static <T> T checkNotNull(T ref) { null }\n" +
            "  static <T> T checkNotNull(T ref, Object errorMessage) { null }\n" +
            "  static <T> T checkNotNull(T ref, String errorMessageTemplate, Object arg1) { null }\n" +
            "  static <T> T checkNotNull(T ref, String errorMessageTemplate, Object arg1, Object arg2) { null }\n" +
            "  static <T> T checkNotNull(T ref, String errorMessageTemplate, Object... errorMessageArgs) { null }\n" +
            "}\n" +
            "String s = Preconditions.checkNotNull('blah', 'Should not be null')";

        String toFind = "checkNotNull";
        int start = contents.lastIndexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.String"); // return type should be resolved
        MethodNode m = assertDeclaration(contents, start, end, "Preconditions", toFind, DeclarationKind.METHOD);
        assertEquals("Parameter type should be resolved", "java.lang.String", printTypeName(m.getParameters()[0].getType()));
        assertEquals("Second overload should be selected", "java.lang.Object", printTypeName(m.getParameters()[1].getType()));
    }

    public void _testJira1718() throws Exception {
        // the type checking script
        IPath robotPath = env.addPackage(project.getFolder("src").getFullPath(), "p2");

        env.addGroovyClass(robotPath, "Renderer",
            "package p2\n" +
            "interface Renderer<T> {\n" +
            "Class<T> getTargetType()\n" +
            "void render(T object, String context)\n" +
            "}\n");

        env.addGroovyClass(robotPath, "AbstractRenderer",
            "package p2\n" +
            "abstract class AbstractRenderer<T> implements Renderer<T> {\n" +
            "private Class<T> targetType\n" +
            "public Class<T> getTargetType() {\n" +
            "return null\n" +
            "}\n" +
            "public void render(T object, String context) {\n" +
            "}\n" +
            "}\n");

        env.addGroovyClass(robotPath, "DefaultRenderer",
            "package p2\n" +
            "class DefaultRenderer<T> implements Renderer<T> {\n" +
            "Class<T> targetType\n" +
            "DefaultRenderer(Class<T> targetType) {\n" +
            "this.targetType = targetType\n" +
            "}\n" +
            "public Class<T> getTargetType() {\n" +
            "return null\n" +
            "}\n" +
            "public void render(T object, String context) {\n" +
            "}\n" +
            "}");

        env.addGroovyClass(robotPath, "RendererRegistry",
            "package p2\n" +
            "interface RendererRegistry {\n" +
            "public <T> Renderer<T> findRenderer(String contentType, T object)\n" +
            "}\n");

        env.addGroovyClass(robotPath, "DefaultRendererRegistry",
            "package p2\n" +
            "class DefaultRendererRegistry implements RendererRegistry {\n" +
            "def <T> Renderer<T> findRenderer(String contentType, T object) {\n" +
            "return null\n" +
            "}\n" +
            "}\n");

        IPath path = env.addGroovyClass(robotPath, "LinkingRenderer",
            "package p2\n" +
            "import groovy.transform.CompileStatic\n" +
            "@CompileStatic\n" +
            "class LinkingRenderer<T> extends AbstractRenderer<T> {\n" +
            "public void render(T object, String context) {\n" +
            "DefaultRendererRegistry registry = new DefaultRendererRegistry()\n" +
            "Renderer htmlRenderer = registry.findRenderer(\"HTML\", object)\n" +
            "if (htmlRenderer == null) {\n" +
            "htmlRenderer = new DefaultRenderer(targetType)\n" +
            "}\n" +
            "htmlRenderer.render(object, context)\n" +
            "}\n" +
            "}\n");

        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        Set<IProblem> problems = ReconcilerUtils.reconcile(JavaCore.createCompilationUnitFrom(file));

        assertTrue(problems.isEmpty());
    }
}
