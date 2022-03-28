/*
 * Copyright 2009-2022 the original author or authors.
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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.groovy.tests.ReconcilerUtils;
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

    @Test
    public void testList0() {
        String contents =
            "def xxx = Collections.emptyList()\n";

        assertType(contents, "xxx", "java.util.List<java.lang.Object>");
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

    @Test
    public void testSet0() {
        String contents =
            "def xxx = Collections.emptySet()\n";

        assertType(contents, "xxx", "java.util.Set<java.lang.Object>");
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

    @Test
    public void testSet9() {
        String contents =
            "@groovy.transform.TypeChecked\n" +
            "void test(obj) {\n" +
            "  if (obj instanceof Map)\n" +
            "    def xxx = obj.entrySet()\n" +
            "}\n";

        assertType(contents, "xxx", "java.util.Set<java.util.Map$Entry<java.lang.Object,java.lang.Object>>");
    }

    @Test
    public void testMap0() {
        String contents =
            "def xxx = Collections.emptyMap()\n";

        assertType(contents, "xxx", "java.util.Map<java.lang.Object,java.lang.Object>");
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

        assertType(contents, "xxx", "java.util.Map<java.lang.Object,java.lang.Object>"); // raw type?
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
    public void testMap17() {
        String contents =
            "def xxx = [*:[1:true]]\n";

        assertType(contents, "xxx", "java.util.Map<java.lang.Integer,java.lang.Boolean>");
    }

    @Test
    public void testMap18() {
        String contents =
            "def xxx = ['item'].collectEntries {str -> /*...*/}\n";

        assertType(contents, "xxx", "java.util.Map<java.lang.Object,java.lang.Object>");
    }

    @Test
    public void testMap19() {
        String contents =
            "def map = [key:'val']\n" +
            "map.getAt('key').toUpperCase()\n" +
            "map.get('key').toUpperCase()\n" +
            "map['key'].toUpperCase()\n" +
            "map['key'] = 'value'\n" +
            "map.key.toUpperCase()\n" +
            "map.key = 'value'\n";

        int offset = contents.indexOf("map");
        assertType(contents, offset, offset + 3, "java.util.Map<java.lang.String,java.lang.String>");

            offset = contents.indexOf("toUpperCase");
        assertDeclaringType(contents, offset, offset + 11, "java.lang.String");

            offset = contents.indexOf("toUpperCase", offset + 1);
        assertDeclaringType(contents, offset, offset + 11, "java.lang.String");

            offset = contents.indexOf("toUpperCase", offset + 1);
        assertDeclaringType(contents, offset, offset + 11, "java.lang.String");

            offset = contents.indexOf("map['key'] = 'value'", offset + 1);
        assertType(contents, offset, offset + "map['key'] = 'value'".length(), "java.lang.String");

            offset = contents.indexOf("toUpperCase", offset + 1);
        assertDeclaringType(contents, offset, offset + 11, "java.lang.String");

            offset = contents.indexOf("map.key = 'value'", offset + 1);
        assertType(contents, offset, offset + "map.key = 'value'".length(), "java.lang.String");
    }

    @Test
    public void testMap20() {
        String contents =
            "import groovy.transform.stc.*\n" +
            "def map = [key:'val']\n" +
            "with(map) {\n" +
            "  it.getAt('key').toUpperCase()\n" +
            "  getAt('key').toUpperCase()\n" +
            "  it.get('key').toUpperCase()\n" +
            "  get('key').toUpperCase()\n" +
            "  it['key'].toUpperCase()\n" +
            "  it['key'] = 'value'\n" +
            "  it.key.toUpperCase()\n" +
            "  key.toUpperCase()\n" +
            "  it.key = 'value'\n" +
            "  key = 'value'\n" +
            "}\n" +
            "private <T> void with(@DelegatesTo.Target T map, @DelegatesTo(strategy=Closure.DELEGATE_FIRST) @ClosureParams(FirstParam) Closure block) {\n" +
            "  map.with(block)\n" +
            "}\n";

        int offset = contents.indexOf("it."); // line 4
        assertType(contents, offset, offset + 2, "java.util.Map<java.lang.String,java.lang.String>");

            offset = contents.indexOf("toUpperCase");
        assertDeclaringType(contents, offset, offset + 11, "java.lang.String");

            offset = contents.indexOf("toUpperCase", offset + 1);
        assertDeclaringType(contents, offset, offset + 11, "java.lang.String");

            offset = contents.indexOf("toUpperCase", offset + 1);
        assertDeclaringType(contents, offset, offset + 11, "java.lang.String");

            offset = contents.indexOf("toUpperCase", offset + 1);
        assertDeclaringType(contents, offset, offset + 11, "java.lang.String");

            offset = contents.indexOf("toUpperCase", offset + 1);
        assertDeclaringType(contents, offset, offset + 11, "java.lang.String");

            offset = contents.indexOf("it['key'] = 'value'", offset + 1);
        assertType(contents, offset, offset + "it['key'] = 'value'".length(), "java.lang.String");

            offset = contents.indexOf("toUpperCase", offset + 1);
        assertDeclaringType(contents, offset, offset + 11, "java.lang.String");

            offset = contents.indexOf("toUpperCase", offset + 1);
        assertDeclaringType(contents, offset, offset + 11, "java.lang.String");

            offset = contents.indexOf("it.key = 'value'", offset + 1);
        assertType(contents, offset, offset + "it.key = 'value'".length(), "java.lang.String");

            offset = contents.lastIndexOf("key = 'value'");
        assertType(contents, offset, offset + "key = 'value'".length(), "java.lang.String");
    }

    @Test // methods and property resolution differs
    public void testMap21() {
        String contents =
            "def map = [foo:'bar']\n" +
            "map.getMetaClass()\n" +
            "map.metaClass\n" +
            "map.getClass()\n" +
            "map.class\n" +
            "map.with {\n" +
            "  getMetaClass()\n" +
            "  metaClass\n" +
            "  isEmpty()\n" +
            "  empty\n" +
            "}\n";

        int offset = contents.indexOf("map");
        assertType(contents, offset, offset + 3, "java.util.Map<java.lang.String,java.lang.String>");

            offset = contents.indexOf("getMetaClass");
        assertType(contents, offset, offset + "getMetaClass".length(), "groovy.lang.MetaClass");

            offset = contents.indexOf("metaClass");
        assertType(contents, offset, offset + "metaClass".length(), "java.lang.String");

            offset = contents.indexOf("getClass");
        assertType(contents, offset, offset + "getClass".length(), "java.lang.Class<? extends java.util.Map<java.lang.String,java.lang.String>>");

            offset = contents.indexOf("class");
        assertType(contents, offset, offset + "class".length(), "java.lang.String");

        //

            offset = contents.lastIndexOf("getMetaClass");
        assertType(contents, offset, offset + "getMetaClass".length(), "groovy.lang.MetaClass");

            offset = contents.lastIndexOf("metaClass");
        assertType(contents, offset, offset + "metaClass".length(), "java.lang.String");

            offset = contents.indexOf("isEmpty");
        assertType(contents, offset, offset + "isEmpty".length(), "java.lang.Boolean");

            offset = contents.indexOf("empty");
        assertType(contents, offset, offset + "empty".length(), "java.lang.String");
    }

    @Test
    public void testMap22() {
        String contents =
            "LinkedHashMap<String,String> map = [foo:'bar']\n" +
            "def put = map.&put\n" +
            "put('key', 'val')\n" +
            "map.@accessOrder\n" +
            "map.@class\n" +
            "map.&getAt\n" +
            "map.with {\n" +
            "  put = it.&put\n" +
            "  put('key', 'val')\n" +
            "  it.@accessOrder\n" +
            "  it.@class\n" +
            "  it.&getAt\n" +
            "}\n";

        int offset = contents.indexOf("map");
        assertType(contents, offset, offset + 3, "java.util.LinkedHashMap<java.lang.String,java.lang.String>");

            offset = contents.indexOf(".&put") + 2;
        assertDeclaration(contents, offset, offset + 3, "java.util.HashMap<java.lang.String,java.lang.String>", "put", DeclarationKind.METHOD);

            offset = contents.indexOf("put(");
        assertType(contents, offset, offset + 3, "groovy.lang.Closure<java.lang.String>");

            offset = contents.indexOf("accessOrder");
        assertType(contents, offset, offset + "accessOrder".length(), "java.lang.Boolean");

            offset = contents.indexOf("class");
        assertUnknownConfidence(contents, offset, offset + "class".length());

            offset = contents.indexOf("getAt");
        assertDeclaringType(contents, offset, offset + "getAt".length(), "org.codehaus.groovy.runtime.DefaultGroovyMethods");

        //

            offset = contents.lastIndexOf(".&put") + 2;
        assertDeclaration(contents, offset, offset + 3, "java.util.HashMap<java.lang.String,java.lang.String>", "put", DeclarationKind.METHOD);

            offset = contents.lastIndexOf("put(");
        assertType(contents, offset, offset + 3, "groovy.lang.Closure<java.lang.String>");

            offset = contents.lastIndexOf("accessOrder");
        assertType(contents, offset, offset + "accessOrder".length(), "java.lang.Boolean");

            offset = contents.lastIndexOf("class");
        assertUnknownConfidence(contents, offset, offset + "class".length());

            offset = contents.lastIndexOf("getAt");
        assertDeclaringType(contents, offset, offset + "getAt".length(), "org.codehaus.groovy.runtime.DefaultGroovyMethods");
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
            "Map<Integer, Map<Integer, List<Date>>> m\n" +
            "def xxx = m.get(1).get(2).get(3)\n";

        assertType(contents, "xxx", "java.util.Date");
    }

    @Test // GRECLIPSE-941
    public void testMapOfList7() {
        String contents =
            "Map<Integer, Map<Integer, List<Date>>> m\n" +
            "def xxx = m[1][2][3]\n";

        assertType(contents, "xxx", "java.util.Date");
    }

    @Test // GROOVY-9420
    public void testMapOfList8() {
        String contents =
            "Map<String, List<Date>> m\n" +
            "def xxx = m['a'][1]\n";

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
    public void testTypeExtendsMap1() {
        String contents =
            "interface I extends Map<String, Number> {}\n" +
            "void m(I config) {\n" +
            "  def xxx = config.whatever\n" +
            "  def yyy = config['whatever']\n" +
            "}\n";

        assertType(contents, "xxx", "java.lang.Number");
        assertType(contents, "yyy", "java.lang.Number");
    }

    @Test
    public void testTypeExtendsMap2() {
        String contents =
            "interface I extends Map<String, Number> {}\n" +
            "abstract class A implements I {}\n" +
            "void m(A config) {\n" +
            "  def xxx = config.whatever\n" +
            "  def yyy = config['whatever']\n" +
            "}\n";

        assertType(contents, "xxx", "java.lang.Number");
        assertType(contents, "yyy", "java.lang.Number");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1189
    public void testTypeExtendsMap3() {
        String contents =
            "interface I extends Map<String, Number> {}\n" +
            "abstract class A implements I {}\n" +
            "class C extends A {}\n" +
            "void m(C config) {\n" +
            "  def xxx = config.whatever\n" +
            "  def yyy = config['whatever']\n" +
            "}\n";

        assertType(contents, "xxx", "java.lang.Number");
        assertType(contents, "yyy", "java.lang.Number");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1189
    public void testTypeExtendsMap4() {
        String contents =
            "interface I<T> extends Map<String, T> {}\n" +
            "abstract class A<U> implements I<U> {}\n" +
            "class C extends A<Number> {}\n" +
            "void m(C config) {\n" +
            "  def xxx = config.whatever\n" +
            "  def yyy = config['whatever']\n" +
            "}\n";

        assertType(contents, "xxx", "java.lang.Number");
        assertType(contents, "yyy", "java.lang.Number");
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
    public void testForLoop3() {
        String contents = "for (a in 1..4) {\n}";
        assertType(contents, "a", "java.lang.Integer");
    }

    @Test
    public void testForLoop4() {
        String contents = "for (a in [1, 2].iterator()) {\n a\n}";
        assertType(contents, "a", "java.lang.Integer");
    }

    @Test
    public void testForLoop5() {
        String contents = "for (a in (1..4).iterator()) {\n a\n}";
        assertType(contents, "a", "java.lang.Integer");
    }

    @Test
    public void testForLoop6() {
        String contents = "for (a in [1 : 1]) {\n a.key\n}";
        assertType(contents, "key", "java.lang.Integer");
    }

    @Test
    public void testForLoop7() {
        String contents = "for (a in [1 : 1]) {\n a.value\n}";
        assertType(contents, "value", "java.lang.Integer");
    }

    @Test
    public void testForLoop8() {
        String contents = "InputStream x\nfor (a in x) {\n a\n}";
        assertType(contents, "a", "java.lang.Byte");
    }

    @Test
    public void testForLoop9() {
        String contents = "DataInputStream x\nfor (a in x) {\n a\n}";
        assertType(contents, "a", "java.lang.Byte");
    }

    @Test
    public void testForLoop10() {
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
        assertType(contents, "fn", "groovy.lang.Closure<java.util.List<java.lang.Object>>");
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
    public void testClosure6() {
        assumeTrue(isParrotParser());
        String contents = "def fn = String[]::new";
        assertType(contents, "fn", "groovy.lang.Closure<java.lang.String[]>");
    }

    @Test
    public void testClosure7() {
        String contents =
            "void test(List<String> list) {\n" +
            "  def array = list.stream().toArray {\n" +
            "    int n -> new String[n]\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "array", "java.lang.String[]");
    }

    @Test
    public void testClosure8() {
        assumeTrue(isParrotParser());
        String contents =
            "void test(List<String> list) {\n" +
            "  def array = list.stream().toArray(String[]::new)\n" +
            "}\n";
        assertType(contents, "toArray", "java.lang.String[]");
        assertType(contents, "array", "java.lang.String[]");
    }

    @Test
    public void testClosure9() {
        assumeTrue(isAtLeastGroovy(30));
        String contents =
            "void test(List<String> list) {\n" +
            "  def array = list.stream().toArray(String[].&new)\n" +
            "}\n";
        assertType(contents, "toArray", "java.lang.String[]");
        assertType(contents, "array", "java.lang.String[]");
    }

    @Test // incorrect LHS type should not alter RHS type
    public void testClosure10() {
        String contents =
            "void test(List<String> list) {\n" +
            "  Number[] array = list.stream().toArray(String[].&new)\n" +
            "}\n";
        assertType(contents, "toArray", "java.lang.String[]");
        assertType(contents, "array", "java.lang.Number[]");
    }

    @Test // GROOVY-9803
    public void testClosure11() {
        for (String toSet : new String[] {"D.&wrap", "Collections.&singleton", "{x -> [x].toSet()}", "{Collections.singleton(it)}"}) {
            String contents =
                "class C<T> {\n" +
                "  static <U> C<U> of(U item) {}\n" +
                "  def <V> C<V> map(F<? super T, ? extends V> func) {}\n" +
                "}\n" +
                "class D {\n" +
                "  static <W> Set<W> wrap(W o) {}\n" +
                "}\n" +
                "interface F<X,Y> {\n" +
                "  Y apply(X x)\n" +
                "}\n" +
                "@groovy.transform.TypeChecked\n" +
                "void test() {\n" +
                "  def c = C.of(123)\n" +
                "  def d = c.map(" + toSet + ")\n" +
                "  def e = d.map{x -> x.first()}\n" +
                "}\n";
            assertType(contents, "d", "C<java.util.Set<java.lang.Integer>>");
            assertType(contents, "x", "java.util.Set<java.lang.Integer>");
            assertType(contents, "e", "C<java.lang.Integer>");
        }
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1194
    public void testClosure12() {
        String contents = "Optional.of(1).map(Arrays.&asList).map{x -> x.first()}\n";
        assertType(contents, "asList", "java.util.List<java.lang.Integer>");
        assertType(contents, "x", "java.util.List<java.lang.Integer>");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1194
    public void testClosure13() {
        String contents = "Optional.of(1).map(Collections.&singletonList).map{x -> x.first()}\n";
        assertType(contents, "singletonList", "java.util.List<java.lang.Integer>");
        assertType(contents, "x", "java.util.List<java.lang.Integer>");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1194
    public void testClosure14() {
        String contents = "void test(Closure<List<Integer>> cl) {}\n" + "test(Arrays.&asList)\n";
        assertType(contents, "asList", "java.util.List<java.lang.Integer>");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1194
    public void testClosure15() {
        String contents = "import groovy.transform.stc.*\n" +
            "def m(@ClosureParams(value=SimpleType,options='java.lang.Integer') Closure c) {}\n" +
            "def <T> String test(T t) {}\n" +
            "m(this.&test)\n";
        int offset = contents.lastIndexOf("test");

        MethodNode m = assertDeclaration(contents, offset, offset + 4, "Search", "test", DeclarationKind.METHOD);
        assertEquals("java.lang.Integer", printTypeName(m.getParameters()[0].getType()));
        assertEquals("java.lang.String", printTypeName(m.getReturnType()));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1198
    public void testClosure16() {
        assumeTrue(isParrotParser());
        String contents = "Optional.of(21).map(num -> num * 2).get()\n";
        assertType(contents, "get", "java.lang.Integer");
        assertDeclaringType(contents, "get", "java.util.Optional<java.lang.Integer>");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1199
    public void testClosure17() {
        String contents = "java.util.function.Function<Integer, List<Integer>> f = Arrays.&asList\n";
        assertType(contents, "asList", "java.util.List<java.lang.Integer>");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1199
    public void testClosure18() {
        String contents =
            "def <T> String test(T t) {}\n" +
            "java.util.function.Function<Integer, String> f = this.&test\n";

        int offset = contents.lastIndexOf("test");

        MethodNode m = assertDeclaration(contents, offset, offset + 4, "Search", "test", DeclarationKind.METHOD);
        assertEquals("java.lang.Integer", printTypeName(m.getParameters()[0].getType()));
        assertEquals("java.lang.String", printTypeName(m.getReturnType()));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1282
    public void testClosure19() {
        String contents =
            "def <T> void test(Iterator<T> it) {\n" +
            "  it.forEachRemaining{t->}\n" +
            "}\n";
        assertType(contents, "t", "T");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1000
    public void testClosure20() {
        String contents =
            "void test(Collection<Integer> c) {\n" +
            "  boolean b = c.removeIf{i->false}\n" +
            "}\n";
        assertType(contents, "i", "java.lang.Integer");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1000
    public void testClosure21() {
        assumeTrue(isParrotParser());
        String contents =
            "void test(Collection<Integer> c) {\n" +
            "  boolean b = c.removeIf(i->false)\n" +
            "}\n";
        assertType(contents, "i", "java.lang.Integer");
    }

    @Test
    public void testClosure22() {
        String contents =
            "void test(Collection<Integer> c) {\n" +
            "  boolean b = c.removeIf{Number n->}\n" +
            "}\n";
        assertType(contents, "n", "java.lang.Number");
    }

    @Test
    public void testClosure23() {
        String contents =
            "void test(List list) {\n" +
            "  list.stream().map{e->}\n" +
            "}\n";
        assertType(contents, "e", "java.lang.Object");
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
            "m.get('')";
        assertType(contents, "get", "java.lang.Class");
    }

    @Test // GRECLIPSE-997
    public void testNestedGenerics2() {
        String contents =
            "class MyMap extends HashMap<String,Class> {\n}\n" +
            "MyMap m\n" +
            "m.entrySet()";
        assertType(contents, "entrySet", "java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.Class>>");
    }

    @Test // GRECLIPSE-997
    public void testNestedGenerics3() {
        String contents =
            "import java.lang.ref.WeakReference\n" +
            "class MyMap<K,V> extends HashMap<K,WeakReference<V>>{\n}\n" +
            "MyMap<String,Class> m\n" +
            "m.entrySet()";
        assertType(contents, "entrySet", "java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.ref.WeakReference<java.lang.Class>>>");
    }

    @Test // GRECLIPSE-997
    public void testNestedGenerics4() {
        String contents =
            "import java.lang.ref.WeakReference\n" +
            "class MyMap<K,V> extends HashMap<K,WeakReference<V>>{\n}\n" +
            "class MySubMap extends MyMap<String,Class>{\n}\n" +
            "MySubMap m\n" +
            "m.entrySet()";
        assertType(contents, "entrySet", "java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.ref.WeakReference<java.lang.Class>>>");
    }

    @Test // GRECLIPSE-997
    public void testNestedGenerics5() {
        String contents =
            "import java.lang.ref.WeakReference\n" +
            "class MyMap<K,V> extends HashMap<K,WeakReference<V>>{\n}\n" +
            "class MySubMap<L> extends MyMap<String,Class>{\n" +
            "  Map<L,Class> val\n" +
            "}\n" +
            "MySubMap<Integer> m\n" +
            "m.@val";
        assertType(contents, "val", "java.util.Map<java.lang.Integer,java.lang.Class>");
    }

    @Test // GRECLIPSE-997
    public void testNestedGenerics6() {
        String contents =
            "import java.lang.ref.WeakReference\n" +
            "class MyMap<K,V> extends HashMap<K,WeakReference<List<K>>>{\n}\n" +
            "class MySubMap extends MyMap<String,Class>{\n}\n" +
            "MySubMap m\n" +
            "m.entrySet()";
        assertType(contents, "entrySet", "java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.ref.WeakReference<java.util.List<java.lang.String>>>>");
    }

    @Test // GRECLIPSE-997
    public void testNestedGenerics7() {
        String contents =
            "class MyMap<K,V> extends HashMap<V,K>{\n}\n" +
            "MyMap<Integer,Class> m\n" +
            "m.get(Object)";
        assertType(contents, "get", "java.lang.Integer");
    }

    @Test // GRECLIPSE-997
    public void testNestedGenerics8() {
        String contents =
            "class MyMap<K,V> extends HashMap<K,V>{\n" +
            "  Map<V,Class<K>> val\n" +
            "}\n" +
            "MyMap<Integer,Class> m\n" +
            "m.@val";
        assertType(contents, "val", "java.util.Map<java.lang.Class,java.lang.Class<java.lang.Integer>>");
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
            "class A {\n" +
            "  def <T> T myMethod(Class<T> claz) {\n" +
            "  }\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  static main(args) {\n" +
            "    A a = new A()\n" +
            "    def val = a.myMethod(String.class)\n" +
            "    val.trim()\n" +
            "  }\n" +
            "}\n";
        int start = contents.lastIndexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // Generic method type inference without @CompileStatic
    public void testMethod2() {
        String contents =
            "class A {\n" +
            "  def <T> T myMethod(Class<T> claz) {\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    A a = new A()\n" +
            "    def val = a.myMethod(String.class)\n" +
            "    val.trim()\n" +
            "  }\n" +
            "}\n";
        int start = contents.lastIndexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // Generic method without object type inference with @CompileStatic
    public void testMethod3() {
        String contents =
            "class A {\n" +
            "  def <T> T myMethod(Class<T> claz) {\n" +
            "  }\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  def m() {\n" +
            "    def val = myMethod(String.class)\n" +
            "    val.trim()\n" +
            "  }\n" +
            "}\n";
        int start = contents.lastIndexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // Generic method without object type inference without @CompileStatic
    public void testMethod4() {
        String contents =
            "class A {\n" +
            "  def <T> T myMethod(Class<T> claz) {\n" +
            "  }\n" +
            "  def m() {\n" +
            "    def val = myMethod(String.class)\n" +
            "    val.trim()\n" +
            "  }\n" +
            "}\n";
        int start = contents.lastIndexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1266
    public void testMethod5() {
        String contents =
            "class C {\n" +
            "  private <N extends Number> N m(Class<N> t) {\n" +
            "    t.newInstance()\n" +
            "  }\n" +
            "  void test() {\n" +
            "    def n = m()\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "m", "java.lang.Number");
        assertType(contents, "n", "java.lang.Number");
    }

    @Test
    public void testMethod6() {
        String contents =
            "class C {\n" +
            "  private <N extends Number> N m(Class<N> t) {\n" +
            "    t.newInstance()\n" +
            "  }\n" +
            "  void test() {\n" +
            "    def n = m(Byte)\n" +
            "  }\n" +
            "}\n";
        assertType(contents, "m", "java.lang.Byte");
        assertType(contents, "n", "java.lang.Byte");
    }

    @Test // GROOVY-10544
    public void testMethod7() {
        String contents =
            "import java.util.function.Function\n" +
            "interface I<T> {\n" +
            "  def <U> Iterable<U> m(Function<? super T, ? extends U> f)\n" +
            "}\n" +
            "interface J<T> extends I<T> {\n" +
            "  def <U> List<U> m(Function<? super T, ? extends U> f)\n" +
            "}\n" +
            "void test(J<String> j) {\n" +
            "  def list = j.m{s -> java.util.regex.Pattern.compile(s)}\n" +
            "}\n";
        assertType(contents, "list", "java.util.List<java.util.regex.Pattern>");
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

    @Test // https://github.com/groovy/groovy-eclipse/issues/1199
    public void testStaticMethod7() {
        // Arrays: public static final <T> List<T> asList(T...)
        String contents = "List<String> list = Arrays.asList()";
        assertType(contents, "asList", "java.util.List<java.lang.String>");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1199
    public void testStaticMethod8() {
        // Collections: public static final <T> List<T> emptyList()
        String contents = "List<String> list = Collections.emptyList()";
        assertType(contents, "emptyList", "java.util.List<java.lang.String>");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1199
    public void testStaticMethod8a() {
        // Collections: public static final <T> List<T> emptyList()
        String contents = "import static java.util.Collections.*; List<String> list = emptyList()";
        assertType(contents, "emptyList", "java.util.List<java.lang.String>");
    }

    @Test
    public void testStaticMethod9() {
        // Collections: public static final <T> List<T> emptyList()
        String contents = "def list = Collections.<String>emptyList()";
        assertType(contents, "list", "java.util.List<java.lang.String>");
        assertType(contents, "emptyList", "java.util.List<java.lang.String>");
    }

    @Test
    public void testStaticMethod10() {
        // Collections: public static final <T> List<T> singletonList(T)
        String contents = "List<String> list = Collections.singletonList('')";
        String toFind = "singletonList";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.util.List<java.lang.String>");
        MethodNode m = assertDeclaration(contents, start, end, "java.util.Collections", toFind, DeclarationKind.METHOD);
        assertEquals("Parameter type should be resolved", "java.lang.String", printTypeName(m.getParameters()[0].getType()));
    }

    @Test
    public void testStaticMethod11() {
        // Collection: public boolean removeAll(Collection<?>)
        String contents = "List<String> list = ['1','2']; list.removeAll(['1'])";
        String toFind = "removeAll";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Boolean");
        MethodNode m = assertDeclaration(contents, start, end, "java.util.List<java.lang.String>", toFind, DeclarationKind.METHOD);
        assertEquals("java.util.Collection<?>", printTypeName(m.getParameters()[0].getType()));
    }

    @Test
    public void testStaticMethod12() {
        // Collection: public boolean addAll(Collection<? extends E>)
        String contents = "List<String> list = ['1','2']; list.addAll(['3'])";
        String toFind = "addAll";
        int start = contents.indexOf(toFind), end = start + toFind.length();
        assertType(contents, start, end, "java.lang.Boolean");
        MethodNode m = assertDeclaration(contents, start, end, "java.util.List<java.lang.String>", toFind, DeclarationKind.METHOD);
        assertEquals("Parameter type should be resolved", "java.util.Collection<? extends java.lang.String>", printTypeName(m.getParameters()[0].getType()));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1249
    public void testStaticMethod13() {
        String contents = "Comparator.<String>comparing{it.length()}";
        assertType(contents, "comparing", "java.util.Comparator<java.lang.String>");
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

    @Test
    public void testJira1718() throws Exception {
        createUnit("p2", "Renderer",
            "package p2\n" +
            "interface Renderer<T> {\n" +
            "  Class<T> getTargetType()\n" +
            "  void render(T object, String context)\n" +
            "}\n");

        createUnit("p2", "AbstractRenderer",
            "package p2\n" +
            "abstract class AbstractRenderer<T> implements Renderer<T> {\n" +
            "  private Class<T> targetType\n" +
            "  Class<T> getTargetType() {\n" +
            "    return null\n" +
            "  }\n" +
            "  void render(T object, String context) {\n" +
            "  }\n" +
            "}\n");

        createUnit("p2", "DefaultRenderer",
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

        createUnit("p2", "RendererRegistry",
            "package p2\n" +
            "interface RendererRegistry {\n" +
            "  public <T> Renderer<T> findRenderer(String contentType, T object)\n" +
            "}\n");

        createUnit("p2", "DefaultRendererRegistry",
            "package p2\n" +
            "class DefaultRendererRegistry implements RendererRegistry {\n" +
            "  def <T> Renderer<T> findRenderer(String contentType, T object) {\n" +
            "    return null\n" +
            "  }\n" +
            "}\n");

        ICompilationUnit unit = createUnit("p2", "LinkingRenderer",
            "package p2\n" +
            "@groovy.transform.CompileStatic\n" +
            "class LinkingRenderer<T> extends AbstractRenderer<T> {\n" +
            "  void render(T object, String context) {\n" +
            "    DefaultRendererRegistry registry = new DefaultRendererRegistry()\n" +
            "    Renderer htmlRenderer = registry.findRenderer('HTML', object)\n" +
            "    if (htmlRenderer == null) {\n" +
            "      htmlRenderer = new DefaultRenderer(targetType)\n" +
            "    }\n" +
            "    htmlRenderer.render(object, context)\n" + // Cannot call p2.Renderer#render(java.lang.Object<java.lang.Object>, java.lang.String) with arguments [T, java.lang.String]
            "  }\n" +
            "}\n");

        Set<IProblem> problems = ReconcilerUtils.reconcile(unit);

        assertTrue(problems.isEmpty());
    }
}
