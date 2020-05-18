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
import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.Set;

import org.codehaus.groovy.ast.MethodNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.groovy.tests.ReconcilerUtils;
import org.junit.Ignore;
import org.junit.Test;

public final class GenericInferencingTests extends InferencingTestSuite {

    @Test
    public void testEnum1() {
        String contents =
            "Blah<Some> farb\n" +
            "farb.something().AA.other\n" +
            "enum Some {\n" +
            "  AA(List)\n" +
            "  public final Class<List<String>> other\n" +
            "  public Some(Class<List<String>> other) {\n" +
            "    this.other = other\n" +
            "  }\n" +
            "}\n" +
            "class Blah<K> {\n" +
            "  K something() {\n" +
            "  }\n" +
            "}";

        assertType(contents, "other", "java.lang.Class<java.util.List<java.lang.String>>");
    }

    @Test
    public void testRange1() {
        String contents =
            "def x = 9\n" +
            "def xxx = x..x\n";

        assertType(contents, "xxx", "groovy.lang.Range<java.lang.Integer>");
    }

    @Test
    public void testRange2() {
        String contents =
            "def x = 9\n" +
            "def xxx = (x*1)..x\n";

        assertType(contents, "xxx", "groovy.lang.Range<java.lang.Integer>");
    }

    @Test // GRECLIPSE-1040
    public void testList1() {
        String contents =
            "def xxx = new LinkedList()\n";

        assertType(contents, "xxx", "java.util.LinkedList");
    }

    @Test
    public void testList2() {
        String contents =
            "def xxx = new LinkedList<>()\n";

        assertType(contents, "xxx", "java.util.LinkedList");
    }

    @Test
    public void testList3() {
        String contents =
            "def xxx = new LinkedList<String>()\n";

        assertType(contents, "xxx", "java.util.LinkedList<java.lang.String>");
    }

    @Test
    public void testList4() {
        String contents =
            "def xxx = []\n";

        assertType(contents, "xxx", "java.util.List<java.lang.Object>");
    }

    @Test
    public void testList5() {
        String contents =
            "def xxx = [ '' ]\n";

        assertType(contents, "xxx", "java.util.List<java.lang.String>");
    }

    @Test
    public void testList6() {
        String contents =
            "def xxx = [ 123 ]\n";

        assertType(contents, "xxx", "java.util.List<java.lang.Integer>");
    }

    @Test
    public void testList7() {
        String contents =
            "def xxx = [ 1 ].get(0)\n";

        assertType(contents, "xxx", "java.lang.Integer");
    }

    @Test
    public void testList8() {
        String contents =
            "def xxx = [ 1 ].iterator()\n";

        assertType(contents, "xxx", "java.util.Iterator<java.lang.Integer>");
    }

    @Test
    public void testList9() {
        String contents =
            "def xxx = [ 1 ].iterator().next()\n";

        assertType(contents, "xxx", "java.lang.Integer");
    }

    @Test
    public void testList10() {
        String contents =
            "def xxx = [] << ''\n";

        assertType(contents, "xxx", "java.util.List<java.lang.String>");
    }

    @Test
    public void testList11() {
        String contents =
            "def x = 9\n" +
            "def xxx = [x]\n";

        assertType(contents, "xxx", "java.util.List<java.lang.Integer>");
    }

    @Test
    public void testList12() {
        String contents =
            "def x = 9\n" +
            "def xxx = [x, '']\n";

        assertType(contents, "xxx", "java.util.List<java.lang.Integer>");
    }

    @Test
    public void testList13() {
        String contents =
            "def x = 9\n" +
            "def xxx = [x + 9 * 8, '']\n";

        assertType(contents, "xxx", "java.util.List<java.lang.Integer>");
    }

    @Test // GRECLIPSE-1040
    public void testSet1() {
        String contents =
            "def xxx = new HashSet()\n";

        assertType(contents, "xxx", "java.util.HashSet");
    }

    @Test
    public void testSet2() {
        String contents =
            "def xxx = new HashSet<>()\n";

        assertType(contents, "xxx", "java.util.HashSet");
    }

    @Test
    public void testSet3() {
        String contents =
            "def xxx = new HashSet<String>()\n";

        assertType(contents, "xxx", "java.util.HashSet<java.lang.String>");
    }

    @Test
    public void testSet4() {
        String contents =
            "def xxx = [] as Set\n";

        assertType(contents, "xxx", "java.util.Set");
    }

    @Test
    public void testSet5() {
        String contents =
            "def xxx = [ '' ] as Set\n";

        assertType(contents, "xxx", "java.util.Set");
    }

    @Test
    public void testSet6() {
        String contents =
            "def xxx = [ '' ] as Set<String>\n";

        assertType(contents, "xxx", "java.util.Set<java.lang.String>");
    }

    @Test
    public void testSet7() {
        String contents =
            "SortedSet<Integer> ints = [1,2,3] as TreeSet\n" +
            "def xxx = ints*.shortValue()\n";

        assertType(contents, "xxx", "java.util.List<java.lang.Short>");
    }

    @Test
    public void testSet8() {
        String contents =
            "NavigableSet<Integer> ints = [1,2,3] as TreeSet\n" +
            "def xxx = ints*.shortValue()\n";

        assertType(contents, "xxx", "java.util.List<java.lang.Short>");
    }

    @Test // GRECLIPSE-1040
    public void testMap1() {
        String contents =
            "def xxx = new HashMap()\n";

        assertType(contents, "xxx", "java.util.HashMap");
    }

    @Test
    public void testMap2() {
        String contents =
            "def xxx = new HashMap<>()\n";

        assertType(contents, "xxx", "java.util.HashMap");
    }

    @Test
    public void testMap3() {
        String contents =
            "def xxx = new HashMap<String,Integer>()\n";

        assertType(contents, "xxx", "java.util.HashMap<java.lang.String,java.lang.Integer>");
    }

    @Test
    public void testMap4() {
        String contents =
            "def xxx = new HashMap<String,Integer>().keySet()\n";

        assertType(contents, "xxx", "java.util.Set<java.lang.String>");
    }

    @Test
    public void testMap5() {
        String contents =
            "def xxx = new HashMap<String,Integer>().values()\n";

        assertType(contents, "xxx", "java.util.Collection<java.lang.Integer>");
    }

    @Test
    public void testMap6() {
        String contents =
            "def xxx = new HashMap<String,Integer>().entrySet()\n";

        assertType(contents, "xxx", "java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.Integer>>");
    }

    @Test
    public void testMap7() {
        String contents =
            "def xxx = new HashMap<String,Integer>().entrySet().iterator().next().key\n";

        assertType(contents, "xxx", "java.lang.String");
    }

    @Test
    public void testMap8() {
        String contents =
            "def xxx = new HashMap<String,Integer>().entrySet().iterator().next().value";

        assertType(contents, "xxx", "java.lang.Integer");
    }

    @Test
    public void testMap9() {
        String contents =
            "Map<String,Integer> m\n" +
            "def xxx = m.entrySet().iterator().next().value\n";

        assertType(contents, "xxx", "java.lang.Integer");
    }

    @Test
    public void testMap10() {
        String contents =
            "def xxx = [:]\n";

        assertType(contents, "xxx", "java.util.Map<java.lang.Object,java.lang.Object>");
    }

    @Test
    public void testMap11() {
        String contents =
            "def xxx = [ 1:1 ]\n";

        assertType(contents, "xxx", "java.util.Map<java.lang.Integer,java.lang.Integer>");
    }

    @Test
    public void testMap12() {
        String contents =
            "Map<Integer, Integer> m() {}\n" +
            "def xxx = m()\n";

        assertType(contents, "xxx", "java.util.Map<java.lang.Integer,java.lang.Integer>");
    }

    @Test
    public void testMap13() {
        String contents =
            "def xxx = [ 1:1 ].entrySet()\n";

        assertType(contents, "xxx", "java.util.Set<java.util.Map$Entry<java.lang.Integer,java.lang.Integer>>");
    }

    @Test
    public void testMap14() {
        String contents =
            "def x = 9\n" +
            "def y = false\n" +
            "def xxx = [(x):y]\n";

        assertType(contents, "xxx", "java.util.Map<java.lang.Integer,java.lang.Boolean>");
    }

    @Test
    public void testMap15() {
        String contents =
            "def x = 9\n" +
            "def y = false\n" +
            "def xxx = [(x+x):!y]\n";

        assertType(contents, "xxx", "java.util.Map<java.lang.Integer,java.lang.Boolean>");
    }

    @Test
    public void testMap16() {
        String contents =
            "def x = 9\n" +
            "def y = false\n" +
            "def xxx = [(x+x):!y, a:'a', b:'b']\n";

        assertType(contents, "xxx", "java.util.Map<java.lang.Integer,java.lang.Boolean>");
    }

    @Test
    public void testListOfMap1() {
        String contents =
            "def x = 9\n" +
            "def y = false\n" +
            "def xxx = [[(x+x):!y, a:'a', b:'b']]\n";

        assertType(contents, "xxx", "java.util.List<java.util.Map<java.lang.Integer,java.lang.Boolean>>");
    }

    @Test
    public void testListOfMap2() {
        String contents = "def x = [ ['a':11, 'b':12], ['a':21, 'b':22] ]\n" +
            "def xxx = x*.a\n";

        assertType(contents, "xxx", "java.util.List<java.lang.Integer>");
    }

    @Test
    public void testMapOfList1() {
        String contents =
            "Map<String,List<Integer>> m\n" +
            "def xxx = m.entrySet().iterator().next().value\n";

        assertType(contents, "xxx", "java.util.List<java.lang.Integer>");
    }

    @Test
    public void testMapOfList2() {
        String contents =
            "Map<String,List<Integer>> m\n" +
            "def xxx = m.entrySet().iterator().next().value.iterator().next()\n";

        assertType(contents, "xxx", "java.lang.Integer");
    }

    @Test
    public void testMapOfList3() {
        String contents =
            "def m = [1: [1]]\n" +
            "def xxx = m.entrySet().iterator().next().key\n";

        assertType(contents, "xxx", "java.lang.Integer");
    }

    @Test
    public void testMapOfList4() {
        String contents =
            "def m = [1: [1]]\n" +
            "def xxx = m.entrySet().iterator().next().value.iterator().next()\n";

        assertType(contents, "xxx", "java.lang.Integer");
    }

    @Test
    public void testMapOfList5() {
        String contents =
            "def m = [1: [1]]\n" +
            "def xxx = m.entrySet().iterator().next().value.iterator().next()\n";

        assertType(contents, "xxx", "java.lang.Integer");
    }

    @Test
    public void testMapOfList6() {
        String contents =
            "Map<String, Map<Integer, List<Date>>> m\n" +
            "def xxx = m.get('foo').get(5).get(2)\n";

        assertType(contents, "xxx", "java.util.Date");
    }

    @Test // GRECLIPSE-941
    public void testMapOfList7() {
        String contents =
            "Map<String, Map<Integer, List<Date>>> m\n" +
            "def xxx = m['foo'][5][2]\n";

        assertType(contents, "xxx", "java.util.Date");
    }

    @Test
    public void testMapOfMaps() {
        String contents =
            "def m = [ ['a':11, 'b':12] : ['a':21, 'b':22] ]\n" +
            "def xxx = m\n";

        assertType(contents, "xxx", "java.util.Map<java.util.Map<java.lang.String,java.lang.Integer>,java.util.Map<java.lang.String,java.lang.Integer>>");
    }

    @Test
    public void testTypeExtendsMap() {
        String contents =
            "interface Config extends Map<String, Number> {}\n" +
            "void meth(Config config) {\n" +
            "  def xxx = config.whatever\n" +
            "}\n";

        assertType(contents, "xxx", "java.lang.Number");
    }

    @Test
    public void testForLoop1() {
        String contents = "def x = 1..4\n" +
            "for (a in x) {\n" +
            "  a\n" +
            "}\n";
        assertType(contents, "a", "java.lang.Integer");
    }

    @Test
    public void testForLoop2() {
        String contents = "for (a in 1..4) {\n" +
            "  a\n" +
            "}\n";
        assertType(contents, "a", "java.lang.Integer");
    }

    @Test
    public void testForLoop2a() {
        String contents = "for (a in 1..4) {\n}";
        assertType(contents, "a", "java.lang.Integer");
    }

    @Test
    public void testForLoop3() {
        String contents = "for (a in [1, 2].iterator()) {\n a\n}";
        assertType(contents, "a", "java.lang.Integer");
    }

    @Test
    public void testForLoop4() {
        String contents = "for (a in (1..4).iterator()) {\n a\n}";
        assertType(contents, "a", "java.lang.Integer");
    }

    @Test
    public void testForLoop5() {
        String contents = "for (a in [1 : 1]) {\n a.key\n}";
        assertType(contents, "key", "java.lang.Integer");
    }

    @Test
    public void testForLoop6() {
        String contents = "for (a in [1 : 1]) {\n a.value\n}";
        assertType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testForLoop7() {
        String contents = "InputStream x\nfor (a in x) {\n a\n}";
        assertType(contents, "a", "java.lang.Byte");
    }

    @Test
    public void testForLoop8() {
        String contents = "DataInputStream x\nfor (a in x) {\n a\n}";
        assertType(contents, "a", "java.lang.Byte");
    }

    @Test
    public void testForLoop9() {
        String contents = "class X {\n" +
            "  List<String> images\n" +
            "}\n" +
            "def sample = new X()\n" + "for (img in sample.images) {\n" +
            "  img\n" +
            "}";
        assertType(contents, "img", "java.lang.String");
    }

    @Test
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
        assertType(contents, "foo", "java.lang.String");
    }

    @Test
    public void testClosure1() {
        String contents = "def fn = { int a, int b ->\n a + b\n}";
        assertType(contents, "fn", "groovy.lang.Closure<java.lang.Integer>");
    }

    @Test
    public void testClosure2() {
        String contents = "def fn = 'abc'.&length";
        assertType(contents, "fn", "groovy.lang.Closure<java.lang.Integer>");
    }

    @Test
    public void testClosure3() {
        String contents = "def fn = Collections.&emptyList";
        assertType(contents, "fn", "groovy.lang.Closure<java.util.List<T>>");
    }

    @Test
    public void testClosure4() {
        String contents = "def fn = (String.&trim) >> (Class.&forName)";
        assertType(contents, "fn", "groovy.lang.Closure<java.lang.Class<?>>");
    }

    @Test
    public void testClosure5() {
        String contents = "def fn = String[].&new";
        assertType(contents, "fn", isAtLeastGroovy(30) ? "groovy.lang.Closure<java.lang.String[]>" : "groovy.lang.Closure");
    }

    @Test
    public void testClosure5a() {
        assumeTrue(isParrotParser());
        String contents = "def fn = String[]::new";
        assertType(contents, "fn", "groovy.lang.Closure<java.lang.String[]>");
    }

    @Test
    public void testArrayDGM() {
        String contents =
            "void test(String[] array) {\n" +
            "  array.iterator()\n" +
            "}\n";
        String target = "iterator";
        int offset = contents.lastIndexOf(target), end = offset + target.length();
        assertType(contents, offset, end, "java.util.Iterator<java.lang.String>");
        MethodNode dgm = assertDeclaration(contents, offset, end, "org.codehaus.groovy.runtime.DefaultGroovyMethods", "iterator", DeclarationKind.METHOD);
        assertEquals("First parameter type should be resolved from object expression", "java.lang.String[]", printTypeName(dgm.getParameters()[0].getType()));
    }

    @Test // GRECLIPSE-997
    public void testNestedGenerics1() {
        String contents =
            "class MyMap extends HashMap<String,Class> {\n}\n" +
            "MyMap m\n" +
            "m.get()";
        String toFind = "get";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Class");
    }

    @Test // GRECLIPSE-997
    public void testNestedGenerics2() {
        String contents =
            "class MyMap extends HashMap<String,Class> {\n}\n" +
            "MyMap m\n" +
            "m.entrySet()";
        String toFind = "entrySet";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.Class>>");
    }

    @Test // GRECLIPSE-997
    public void testNestedGenerics3() {
        String contents =
            "import java.lang.ref.WeakReference\n" +
            "class MyMap<K,V> extends HashMap<K,WeakReference<V>>{\n}\n" +
            "MyMap<String,Class> m\n" +
            "m.entrySet()";
        String toFind = "entrySet";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.ref.WeakReference<java.lang.Class>>>");
    }

    @Test // GRECLIPSE-997
    public void testNestedGenerics4() {
        String contents =
            "import java.lang.ref.WeakReference\n" +
            "class MyMap<K,V> extends HashMap<K,WeakReference<V>>{\n}\n" +
            "class MySubMap extends MyMap<String,Class>{\n}\n" +
            "MySubMap m\n" +
            "m.entrySet()";
        String toFind = "entrySet";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.ref.WeakReference<java.lang.Class>>>");
    }

    @Test // GRECLIPSE-997
    public void testNestedGenerics5() {
        String contents =
            "import java.lang.ref.WeakReference\n" +
            "class MyMap<K,V> extends HashMap<K,WeakReference<V>>{\n}\n" +
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

    @Test // GRECLIPSE-997
    public void testNestedGenerics6() {
        String contents =
            "import java.lang.ref.WeakReference\n" +
            "class MyMap<K,V> extends HashMap<K,WeakReference<List<K>>>{\n}\n" +
            "class MySubMap extends MyMap<String,Class>{\n}\n" +
            "MySubMap m\n" +
            "m.entrySet()";
        String toFind = "entrySet";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.ref.WeakReference<java.util.List<java.lang.String>>>>");
    }

    @Test // GRECLIPSE-997
    public void testNestedGenerics7() {
        String contents =
            "class MyMap<K,V> extends HashMap<V,K>{\n}\n" +
            "MyMap<Integer,Class> m\n" +
            "m.get(Object)";
        String toFind = "get";
        int start = contents.lastIndexOf(toFind);
        int end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test // GRECLIPSE-997
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

    @Test
    public void testGetClass1() {
        String contents = "''.getClass()", toFind = "getClass";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Class<? extends java.lang.String>");
    }

    @Test
    public void testGetClass2() {
        String contents = "[''].getClass()", toFind = "getClass";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Class<? extends java.util.List<java.lang.String>>");
    }

    @Test
    public void testGetClass3() {
        String contents = "[a:''].getClass()", toFind = "getClass";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Class<? extends java.util.Map<java.lang.String,java.lang.String>>");
    }

    @Test // GRECLIPSE-1696: Generic method type inference with @CompileStatic
    public void testMethod1() {
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

    @Test // Generic method type inference without @CompileStatic
    public void testMethod2() {
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

    @Test // Generic method without object type inference with @CompileStatic
    public void testMethod3() {
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

    @Test // Generic method without object type inference without @CompileStatic
    public void testMethod4() {
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

    @Test // GRECLIPSE-1129: Static generic method type inference with @CompileStatic
    public void testStaticMethod1() {
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

    @Test // Static generic method type inference without @CompileStatic
    public void testStaticMethod2() {
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

    @Test // Static generic method without class type inference with @CompileStatic
    public void testStaticMethod3() {
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

    @Test // Static generic method without class type inference without @CompileStatic
    public void testStaticMethod4() {
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

    @Test // GRECLIPSE-1129
    public void testStaticMethod5() {
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

    @Test
    public void testStaticMethod6() {
        String contents =
            "class A {}\n" +
            "class B extends A {}\n" +
            "class C {\n" +
            "  static <T extends A> T loadSomething(T t) {\n" +
            "    return t\n" +
            "  }\n" +
            "  def col = loadSomething(new B())\n" +
            "  def m() {\n" +
            "    col\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "col", "B");
    }

    @Test
    public void testStaticMethod7() {
        // Collections: public static final <T> List<T> singletonList(T)
        String contents = "List<String> list = Collections.singletonList('')";
        String toFind = "singletonList";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.util.List<java.lang.String>");
        MethodNode m = assertDeclaration(contents, start, end, "java.util.Collections", toFind, DeclarationKind.METHOD);
        assertEquals("Parameter type should be resolved", "java.lang.String", printTypeName(m.getParameters()[0].getType()));
    }

    @Test @Ignore
    public void testStaticMethod8() { // no help from parameters
        // Collections: public static final <T> List<T> emptyList()
        String contents = "List<String> list = Collections.emptyList()";
        String toFind = "emptyList";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.util.List<java.lang.String>");
    }

    @Test
    public void testStaticMethod9() {
        // Collections: public static final <T> List<T> emptyList()
        String contents = "def list = Collections.<String>emptyList()";
        String toFind = "list";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.util.List<java.lang.String>");
    }

    @Test
    public void testStaticMethod10() {
        // Collection: public boolean removeAll(Collection<?>)
        String contents = "List<String> list = ['1','2']; list.removeAll(['1'])";
        String toFind = "removeAll";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Boolean");
        MethodNode m = assertDeclaration(contents, start, end, "java.util.Collection<java.lang.String>", toFind, DeclarationKind.METHOD);
        assertEquals("Parameter type should be resolved", "java.util.Collection<?>", printTypeName(m.getParameters()[0].getType()));
    }

    @Test
    public void testStaticMethod11() {
        // Collection: public boolean addAll(Collection<? extends E>)
        String contents = "List<String> list = ['1','2']; list.addAll(['3'])";
        String toFind = "addAll";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Boolean");
        MethodNode m = assertDeclaration(contents, start, end, "java.util.Collection<java.lang.String>", toFind, DeclarationKind.METHOD);
        assertEquals("Parameter type should be resolved", "java.util.Collection<? extends java.lang.String>", printTypeName(m.getParameters()[0].getType()));
    }

    @Test
    public void testStaticMethodOverloads1() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) {\n" +
            "    obj\n" +
            "  }\n" +
            "  static <T extends CharSequence> T b(T seq) {\n" +
            "    seq\n" +
            "  }\n" +
            "}\n" +
            "def result = A.b([])";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Object"); // should not satisfy bounds of T
    }

    @Test
    public void testStaticMethodOverloads2() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) {\n" +
            "    obj\n" +
            "  }\n" +
            "  static <T extends CharSequence> T b(T seq) {\n" +
            "    seq\n" +
            "  }\n" +
            "}\n" +
            "def result = A.b('')";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.String"); // should satisfy bounds of T
    }

    @Test
    public void testStaticMethodOverloads3() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) {\n" +
            "    obj\n" +
            "  }\n" +
            "  static <T extends CharSequence & Serializable> T b(T seq) {\n" +
            "    seq\n" +
            "  }\n" +
            "}\n" +
            "def result = A.b([])";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Object"); // should not satisfy bounds of T
    }

    @Test
    public void testStaticMethodOverloads4() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) {\n" +
            "    obj\n" +
            "  }\n" +
            "  static <T extends CharSequence & Iterable> T b(T seq) {\n" +
            "    seq\n" +
            "  }\n" +
            "}\n" +
            "def result = A.b('')";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Object"); // should not satisfy bounds of T
    }

    @Test
    public void testStaticMethodOverloads5() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) {\n" +
            "    obj\n" +
            "  }\n" +
            "  static <T extends CharSequence & Serializable> T b(T seq) {\n" +
            "    seq\n" +
            "  }\n" +
            "}\n" +
            "def result = A.b('')";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.String"); // should satisfy bounds of T
    }

    @Test
    public void testStaticMethodOverloads6() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) {\n" +
            "    obj\n" +
            "  }\n" +
            "  static <T extends CharSequence> T b(T[] seq) {\n" +
            "    seq\n" +
            "  }\n" +
            "}\n" +
            "def result = A.b(new Object[0])";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Object"); // should not satisfy bounds of T
    }

    @Test
    public void testStaticMethodOverloads7() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) {\n" +
            "    obj\n" +
            "  }\n" +
            "  static <T extends CharSequence> T b(T[] seq) {\n" +
            "    seq\n" +
            "  }\n" +
            "}\n" +
            "def result = A.b(new String[0])";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.String"); // should satisfy bounds of T
    }

    @Test
    public void testStaticMethodOverloads8() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) {\n" +
            "    obj\n" +
            "  }\n" +
            "  static <T extends CharSequence & Serializable> T b(T[] seq) {\n" +
            "    seq\n" +
            "  }\n" +
            "}\n" +
            "def result = A.b(new Object[0])";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Object"); // should not satisfy bounds of T
    }

    @Test
    public void testStaticMethodOverloads9() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) {\n" +
            "    obj\n" +
            "  }\n" +
            "  static <T extends CharSequence & Iterable> T b(T[] seq) {\n" +
            "    seq\n" +
            "  }\n" +
            "}\n" +
            "def result = A.b(new String[0])";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Object"); // should not satisfy bounds of T
    }

    @Test
    public void testStaticMethodOverloads10() {
        String contents =
            "class A {\n" +
            "  static Object b(Object obj) {\n" +
            "    obj\n" +
            "  }\n" +
            "  static <T extends CharSequence & Serializable> T b(T[] seq) {\n" +
            "    seq\n" +
            "  }\n" +
            "}\n" +
            "def result = A.b(new String[0])";
        String toFind = "result";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.String"); // should satisfy bounds of T
    }

    @Test
    public void testStaticMethodOverloads11() {
        String contents =
            "class Preconditions {\n" +
            "  static <T> T checkNotNull(T ref) {}\n" +
            "  static <T> T checkNotNull(T ref, Object errorMessage) {}\n" +
            "  static <T> T checkNotNull(T ref, String errorMessageTemplate, Object arg1) {}\n" +
            "  static <T> T checkNotNull(T ref, String errorMessageTemplate, Object arg1, Object arg2) {}\n" +
            "  static <T> T checkNotNull(T ref, String errorMessageTemplate, Object... errorMessageArgs) {}\n" +
            "}\n" +
            "String s = Preconditions.checkNotNull('blah', 'Should not be null')";

        String toFind = "checkNotNull";
        int start = contents.lastIndexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.String"); // return type should be resolved
        MethodNode m = assertDeclaration(contents, start, end, "Preconditions", toFind, DeclarationKind.METHOD);
        assertEquals("Parameter type should be resolved", "java.lang.String", printTypeName(m.getParameters()[0].getType()));
        assertEquals("Second overload should be selected", "java.lang.Object", printTypeName(m.getParameters()[1].getType()));
    }

    @Test
    public void testCircularReference() {
        String contents =
            "abstract class Abstract<T extends Abstract<T>> {\n" +
            "  T withStuff(value) {\n" +
            "    return (T) this\n" +
            "  }\n" +
            "}\n" +
            "class Concrete extends Abstract<Concrete> {\n" +
            "  Concrete withThing(value) {\n" +
            "    return this\n" +
            "  }\n" +
            "}\n" +
            "def x,y\n" +
            "new Concrete().withThing(x).withStuff(y)\n" +
            "new Concrete().withStuff(x).withThing(y)\n";

        int offset = contents.lastIndexOf("withThing(x)");
        assertType(contents, offset, offset + "withThing".length(), "Concrete");
            offset = contents.lastIndexOf("withStuff(x)");
        assertType(contents, offset, offset + "withStuff".length(), "Concrete");
    }

    @Test @Ignore
    public void testJira1718() throws Exception {
        IPath p2 = env.addPackage(project.getFolder("src").getFullPath(), "p2");

        env.addGroovyClass(p2, "Renderer",
            "package p2\n" +
            "interface Renderer<T> {\n" +
            "  Class<T> getTargetType()\n" +
            "  void render(T object, String context)\n" +
            "}\n");

        env.addGroovyClass(p2, "AbstractRenderer",
            "package p2\n" +
            "abstract class AbstractRenderer<T> implements Renderer<T> {\n" +
            "  private Class<T> targetType\n" +
            "  Class<T> getTargetType() {\n" +
            "    return null\n" +
            "  }\n" +
            "  void render(T object, String context) {\n" +
            "  }\n" +
            "}\n");

        env.addGroovyClass(p2, "DefaultRenderer",
            "package p2\n" +
            "class DefaultRenderer<T> implements Renderer<T> {\n" +
            "  Class<T> targetType\n" +
            "  DefaultRenderer(Class<T> targetType) {\n" +
            "    this.targetType = targetType\n" +
            "  }\n" +
            "  Class<T> getTargetType() {\n" +
            "    return null\n" +
            "  }\n" +
            "  void render(T object, String context) {\n" +
            "  }\n" +
            "}\n");

        env.addGroovyClass(p2, "RendererRegistry",
            "package p2\n" +
            "interface RendererRegistry {\n" +
            "  public <T> Renderer<T> findRenderer(String contentType, T object)\n" +
            "}\n");

        env.addGroovyClass(p2, "DefaultRendererRegistry",
            "package p2\n" +
            "class DefaultRendererRegistry implements RendererRegistry {\n" +
            "  def <T> Renderer<T> findRenderer(String contentType, T object) {\n" +
            "    return null\n" +
            "  }\n" +
            "}\n");

        IPath path = env.addGroovyClass(p2, "LinkingRenderer",
            "package p2\n" +
            "@groovy.transform.CompileStatic\n" +
            "class LinkingRenderer<T> extends AbstractRenderer<T> {\n" +
            "  void render(T object, String context) {\n" +
            "    DefaultRendererRegistry registry = new DefaultRendererRegistry()\n" +
            "    Renderer htmlRenderer = registry.findRenderer('HTML', object)\n" +
            "    if (htmlRenderer == null) {\n" +
            "      htmlRenderer = new DefaultRenderer(targetType)\n" +
            "    }\n" +
            "    htmlRenderer.render(object, context)\n" + // TODO: Cannot call p2.Renderer<java.lang.Object>#render(java.lang.Object<java.lang.Object>, java.lang.String) with arguments [T, java.lang.String]
            "  }\n" +
            "}\n");

        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        Set<IProblem> problems = ReconcilerUtils.reconcile(JavaCore.createCompilationUnitFrom(file));

        assertTrue(problems.isEmpty());
    }
}
