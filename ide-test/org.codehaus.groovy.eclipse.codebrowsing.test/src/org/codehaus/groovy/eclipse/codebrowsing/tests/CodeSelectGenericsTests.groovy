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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedBinaryMethod
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.SourceRange
import org.junit.Test

final class CodeSelectGenericsTests extends BrowsingTestSuite {

    private static final String GENERICS_CLASS = '''\
        |interface SomeInterface<T> { }
        |interface SomeInterface2<T,U> { }
        |class SomeClass { }
        |class Bart implements /*0*/SomeInterface</*1*/SomeInterface2</*2*/SomeClass,/*3*/SomeInterface<? extends /*4*/SomeClass>>> {
        |    /*12*/SomeInterface</*5*/SomeClass> ff
        |    /*6*/SomeInterface</*7*/SomeClass> yy(/*8*/SomeInterface</*9*/SomeClass> yyy) { }
        |  def c = { /*10*/SomeInterface2</*11*/SomeClass> a -> }
        |}'''.stripMargin()

    private int find(int toFind) {
        String lookFor = '/*' + toFind + '*/'
        int index = GENERICS_CLASS.indexOf(lookFor)
        return index < 0 ? index : index + lookFor.length()
    }

    //

    @Test
    void testCodeSelectGenericField1() {
        String structureContents = 'class Structure { java.util.List<String> field; }'
        String javaContents = 'class Java { { new Structure().field = null; } }'
        String groovyContents = 'new Structure().field'
        String toFind = 'field'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test
    void testCodeSelectGenericField2() {
        String structureContents = 'class Structure { java.util.Map<String, Integer> field; }'
        String javaContents = 'class Java { { new Structure().field = null; } }'
        String groovyContents = 'new Structure().field'
        String toFind = 'field'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test
    void testCodeSelectGenericField3() {
        String structureContents = 'class Structure { java.util.Map<String[], java.util.List<Integer>> field; }'
        String javaContents = 'class Java { { new Structure().field = null; } }'
        String groovyContents = 'new Structure().field'
        String toFind = 'field'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test
    void testCodeSelectGenericMethod1() {
        String structureContents = 'class Structure { java.util.Map<String[], java.util.List<Integer>> field; }'
        String javaContents = 'class Java { { new Structure().field.entrySet(); } }'
        String groovyContents = 'new Structure().field.entrySet()'
        String toFind = 'entrySet'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test
    void testCodeSelectGenericMethod2() {
        String structureContents = 'class Structure { java.util.List<Integer> method() { return null; } }'
        String javaContents = 'class Java { { new Structure().method(); } }'
        String groovyContents = 'new Structure().method()'
        String toFind = 'method'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test
    void testCodeSelectGenericMethod3() {
        String structureContents = 'class Structure { java.util.List<Integer> method(java.util.List<Integer> a) { return null; } }'
        String javaContents = 'class Java { { new Structure().method(null); } }'
        String groovyContents = 'new Structure().method(null)'
        String toFind = 'method'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test
    void testCodeSelectGenericMethod4() {
        String structureContents = 'class Structure { java.util.List<Integer> method(java.util.List<Integer> a, java.util.List<String> b) { return null; } }'
        String javaContents = 'class Java { { new Structure().method(null, null); } }'
        String groovyContents = 'new Structure().method(null, null)'
        String toFind = 'method'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test
    void testCodeSelectGenericMethod5() {
        String structureContents = 'class Structure { java.util.List<Integer> method(int a, int b, char x) { return null; } }'
        String javaContents = 'class Java { { new Structure().method(1, 2, \'c\'); } }'
        String groovyContents = 'new Structure().method(1, 2, \'c\')'
        String toFind = 'method'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test
    void testCodeSelectGenericMethod6() {
        String contents = '[a: Number].keySet()'
        IJavaElement elem = assertCodeSelect([contents], 'keySet')
        MethodNode method = ((GroovyResolvedBinaryMethod) elem).inferredElement
        assert method.returnType.toString(false) == 'java.util.Set <java.lang.String>'
    }

    @Test
    void testCodeSelectGenericMethod7() {
        String contents = '[a: Number].values()'
        IJavaElement elem = assertCodeSelect([contents], 'values')
        MethodNode method = ((GroovyResolvedBinaryMethod) elem).inferredElement
        assert method.returnType.toString(false) == 'java.util.Collection <java.lang.Class>'
    }

    @Test
    void testCodeSelectGenericMethod8() {
        String contents = '[a: Number].entrySet()'
        IJavaElement elem = assertCodeSelect([contents], 'entrySet')
        MethodNode method = ((GroovyResolvedBinaryMethod) elem).inferredElement
        assert method.returnType.toString(false) == 'java.util.Set <java.util.Map$Entry>'
    }

    @Test
    void testCodeSelectGenericCategoryMethod() {
        String contents = '[a: Number].collect { k,v -> "" }'
        IJavaElement elem = assertCodeSelect([contents], 'collect')
        MethodNode method = ((GroovyResolvedBinaryMethod) elem).inferredElement
        assert method.returnType.toString(false) == 'java.util.List <java.lang.String>'
    }

    @Test
    void testCodeSelectGenericType1() {
        String structureContents = 'class Structure<T> { }'
        String javaContents = 'class Java { { Structure<Integer> x = null;  if (x instanceof Object) { x.toString(); } } }'
        String groovyContents = 'Structure<Integer> obj = new Structure<Integer>()\nobj'
        String toFind = 'Structure'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test
    void testCodeSelectGenericTypeAndField1() {
        String structureContents = 'class Structure<T> { java.util.List<T> field; }'
        String javaContents = 'class Java { { new Structure<Integer>().field = null;} }'
        String groovyContents = 'new Structure<Integer>().field'
        String toFind = 'field'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test
    void testCodeSelectGenericTypeAndField2() {
        String structureContents = 'class Structure<T,U> { java.util.Map<T,U> field; }'
        String javaContents = 'class Java { { new Structure<String, Integer>().field = null;} }'
        String groovyContents = 'new Structure<String, Integer>().field'
        String toFind = 'field'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test
    void testCodeSelectGenericTypeAndField3() {
        String structureContents = 'class Structure<T,U> { java.util.Map<T,U> field; }'
        String javaContents = 'import java.util.List;\nclass Java { { new Structure<String[], List<Integer>> ().field = null;} }'
        String groovyContents = 'new Structure<String[], List<Integer>>().field'
        String toFind = 'field'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test
    void testCodeSelectGenericTypeAndMethod1() {
        String structureContents = 'class Structure<T,U> { java.util.Map<T,U> field; }'
        String javaContents = 'import java.util.List;\nclass Java { { new Structure<String[], List<Integer>> ().field.entrySet();} }'
        String groovyContents = 'new Structure<String[], List<Integer>>().field.entrySet()'
        String toFind = 'entrySet'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test
    void testCodeSelectGenericTypeAndMethod2() {
        String structureContents = 'class Structure<T> { java.util.List<T> method() { return null; } }'
        String javaContents = 'class Java { { new Structure<Integer>().method();} }'
        String groovyContents = 'new Structure<Integer>().method()'
        String toFind = 'method'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test
    void testCodeSelectGenericTypeAndMethod3() {
        String structureContents = 'class Structure<T> { java.util.List<T> method(java.util.List<T> a) { return null; } }'
        String javaContents = 'class Java { { new Structure<Integer>().method(null);} }'
        String groovyContents = 'new Structure<Integer>().method(null)'
        String toFind = 'method'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test
    void testCodeSelectGenericTypeAndMethod4() {
        String structureContents = 'class Structure<T> { java.util.List<T> method(T a) { return null; } }'
        String javaContents = 'import java.util.List;\nclass Java { { new Structure<List<Integer>>().method(null);} }'
        String groovyContents = 'new Structure<List<Integer>>().method(null)'
        String toFind = 'method'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test
    void testCodeSelectGenericTypeAndMethod5() {
        String structureContents = 'import java.util.List;\nclass Structure<T,U> { T method(T a, List<U> b) { return null; } }'
        String javaContents = 'import java.util.List;\nclass Java { { new Structure<List<String>, Integer>().method(null, null);} }'
        String groovyContents = 'new Structure<List<String>, Integer>().method(null, null)'
        String toFind = 'method'
        assertCodeSelect([structureContents, javaContents, groovyContents], toFind)
    }

    @Test // GRECLIPSE-1238
    void testCodeSelectTypeParam0() {
        String name = 'SomeInterface'
        int len = name.length()
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(0), len), name)
    }

    @Test // GRECLIPSE-1238
    void testCodeSelectTypeParam1() {
        String name = 'SomeInterface2'
        int len = name.length()
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(1), len), name)
    }

    @Test // GRECLIPSE-1238
    void testCodeSelectTypeParam2() {
        String name = 'SomeClass'
        int len = name.length()
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(2), len), name)
    }

    @Test // GRECLIPSE-1238
    void testCodeSelectTypeParam3() {
        String name = 'SomeInterface'
        int len = name.length()
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(3), len), name)
    }

    @Test // GRECLIPSE-1238
    void testCodeSelectTypeParam4() {
        String name = 'SomeClass'
        int len = name.length()
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(4), len), name)
    }

    @Test // GRECLIPSE-1238
    void testCodeSelectTypeParam5() {
        String name = 'SomeClass'
        int len = name.length()
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(5), len), name)
    }

    @Test // GRECLIPSE-1238
    void testCodeSelectTypeParam6() {
        String name = 'SomeInterface'
        int len = name.length()
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(6), len), name)
    }

    @Test // GRECLIPSE-1238
    void testCodeSelectTypeParam7() {
        String name = 'SomeClass'
        int len = name.length()
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(7), len), name)
    }

    @Test // GRECLIPSE-1238
    void testCodeSelectTypeParam8() {
        String name = 'SomeInterface'
        int len = name.length()
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(8), len), name)
    }

    @Test // GRECLIPSE-1238
    void testCodeSelectTypeParam9() {
        String name = 'SomeClass'
        int len = name.length()
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(9), len), name)
    }

    @Test // GRECLIPSE-1238
    void testCodeSelectTypeParam10() {
        String name = 'SomeInterface2'
        int len = name.length()
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(10), len), name)
    }

    @Test // GRECLIPSE-1238
    void testCodeSelectTypeParam11() {
        String name = 'SomeClass'
        int len = name.length()
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(11), len), name)
    }

    @Test // GRECLIPSE-1238
    void testCodeSelectTypeParam12() {
        String name = 'SomeInterface'
        int len = name.length()
        assertCodeSelect(GENERICS_CLASS, new SourceRange(find(12), len), name)
    }
}
