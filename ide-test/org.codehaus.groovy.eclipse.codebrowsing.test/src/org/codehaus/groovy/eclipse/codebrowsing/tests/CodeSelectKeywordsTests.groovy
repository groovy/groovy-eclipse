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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import org.junit.Test

final class CodeSelectKeywordsTests extends BrowsingTestSuite {

    @Test
    void testCodeSelectKeywordPackage() {
        String contents = '''\
            |package a.b.c.d
            |'''.stripMargin()
        assertCodeSelect([contents], 'package', null)
    }

    @Test
    void testCodeSelectKeywordImport() {
        String contents = '''\
            |import java.util.regex.Pattern
            |Pattern p = ~/123/
            |'''.stripMargin()
        assertCodeSelect([contents], 'import', null)
    }

    @Test
    void testCodeSelectKeywordStaticInImport() {
        String contents = '''\
            |import static java.util.regex.Pattern.compile
            |def p = compile('123')
            |'''.stripMargin()
        assertCodeSelect([contents], 'static', null)
    }

    @Test
    void testCodeSelectKeywordAsInImport() {
        String contents = '''\
            |import static java.util.regex.Pattern.compile as build
            |def p = build('123')
            |'''.stripMargin()
        assertCodeSelect([contents], 'as', null)
    }

    @Test
    void testCodeSelectNotKeywordClass() {
        String contents = 'Object obj; obj.class.name'
        assertCodeSelect([contents], 'class', 'getClass')
    }

    @Test
    void testCodeSelectKeywordClass1() {
        String contents = 'String.class'
        assertCodeSelect([contents], 'class', null)
    }

    @Test
    void testCodeSelectKeywordClass2() {
        String contents = 'class C { }'
        assertCodeSelect([contents], 'class', null)
    }

    @Test
    void testCodeSelectKeywordClass3() {
        String contents = '@groovy.transform.Canonical class C { }'
        assertCodeSelect([contents], 'class', null)
    }

    @Test
    void testCodeSelectKeywordClass4() {
        String contents = '@groovy.transform.Immutable class C { }'
        assertCodeSelect([contents], 'class', null)
    }

    @Test
    void testCodeSelectKeywordDef1() {
        String contents = 'class C { def x() { } }'
        assertCodeSelect([contents], 'def', null)
    }

    @Test
    void testCodeSelectKeywordDef2() {
        String contents = 'class C { Object x() { def y } }'
        assertCodeSelect([contents], 'def', null)
    }

    @Test
    void testCodeSelectKeywordNew1() {
        String contents = 'def x = new Object()'
        assertCodeSelect([contents], 'new', null)
    }

    @Test
    void testCodeSelectKeywordNew2() {
        String contents = 'def x = new double[0]'
        assertCodeSelect([contents], 'new', null)
    }

    @Test
    void testCodeSelectKeywordNull() {
        String contents = 'Object o = null'
        assertCodeSelect([contents], 'null', null)
    }

    @Test
    void testCodeSelectKeywordTrue() {
        String contents = 'boolean b = true'
        assertCodeSelect([contents], 'true', null)
    }

    @Test
    void testCodeSelectKeywordFalse() {
        String contents = 'boolean b = false'
        assertCodeSelect([contents], 'false', null)
    }

    @Test
    void testCodeSelectKeywordThis1() {
        // Java Editor doesn't code select on 'this' variable expression
        String contents = 'class C { def x() { this } }'
        assertCodeSelect([contents], 'this', null)
    }

    @Test
    void testCodeSelectKeywordThis2() {
        // Java Editor doesn't code select on 'this' variable expression
        String contents = 'class C { def x() { this.toString() } }'
        assertCodeSelect([contents], 'this', null)
    }

    @Test // GRECLIPSE-548
    void testCodeSelectKeywordSuper1() {
        String contents = '''\
            |class Super { }
            |class C extends Super {
            |  def x() { super }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'super', 'Super')
    }

    @Test // GRECLIPSE-548
    void testCodeSelectKeywordSuper2() {
        String contents = '''\
            |class Super { }
            |class C extends Super {
            |  def x() { super.toString() }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'super', 'Super')
    }

    @Test
    void testCodeSelectKeywordReturn1() {
        String contents = 'def meth() { return null }'
        assertCodeSelect([contents], 'return', null)
    }

    @Test
    void testCodeSelectKeywordReturn2() {
        String contents = 'def closure = { -> return null }'
        assertCodeSelect([contents], 'return', null)
    }

    @Test
    void testCodeSelectKeywordAssert1() {
        String contents = 'assert it != null'
        assertCodeSelect([contents], 'assert', null)
    }

    @Test
    void testCodeSelectKeywordAssert2() {
        String contents = 'def closure = { assert it != null }'
        assertCodeSelect([contents], 'assert', null)
    }

    @Test
    void testCodeSelectKeywordIf1() {
        String contents = 'if(true){}'
        assertCodeSelect([contents], 'if', null)
    }

    @Test
    void testCodeSelectKeywordIf2() {
        String contents = 'def closure = { if(true){} }'
        assertCodeSelect([contents], 'if', null)
    }

    @Test
    void testCodeSelectKeywordIf3() {
        String contents = 'def closure = { if(true){}else if(false){} }'
        assertCodeSelect([contents], 'if', null)
    }

    @Test
    void testCodeSelectKeywordIf4() {
        String contents = 'class C {def f= new Object(){void m(p){ if(p){} }}}'
        assertCodeSelect([contents], 'if', null)
    }

    @Test
    void testCodeSelectKeywordElse1() {
        String contents = 'if(true){}else{}'
        assertCodeSelect([contents], 'else', null)
    }

    @Test
    void testCodeSelectKeywordElse2() {
        String contents = 'def closure = { if(true){}else{} }'
        assertCodeSelect([contents], 'else', null)
    }

    @Test
    void testCodeSelectKeywordElse3() {
        String contents = 'def closure = { if(true){}else if (false){}else{} }'
        assertCodeSelect([contents], 'else', null)
    }

    @Test
    void testCodeSelectKeywordElse4() {
        String contents = 'class C {def f= new Object(){void m(p){ if(p){}else{} }}}'
        assertCodeSelect([contents], 'else', null)
    }

    @Test
    void testCodeSelectKeywordFor1() {
        String contents = 'for(item in []){}'
        assertCodeSelect([contents], 'for', null)
    }

    @Test
    void testCodeSelectKeywordFor2() {
        String contents = 'def closure = { for(item in []){} }'
        assertCodeSelect([contents], 'for', null)
    }

    @Test
    void testCodeSelectKeywordFor3() {
        String contents = 'class C {def f= new Object(){void m(p){ for(item in []){} }}}'
        assertCodeSelect([contents], 'for', null)
    }

    @Test
    void testCodeSelectKeywordIn1() {
        String contents = 'for(item in []){}'
        assertCodeSelect([contents], 'in', null)
    }

    @Test
    void testCodeSelectKeywordIn2() {
        String contents = 'def closure = { for(item in []){} }'
        assertCodeSelect([contents], 'in', null)
    }

    @Test
    void testCodeSelectKeywordIn3() {
        String contents = 'class C {def f= new Object(){void m(p){ for(item in []){} }}}'
        assertCodeSelect([contents], 'in', null)
    }

    @Test
    void testCodeSelectKeywordIn4() {
        String contents = 'def closure = { it in [] }'
        assertCodeSelect([contents], 'in', null)
    }

    @Test
    void testCodeSelectKeywordIn5() {
        // 'in' binary expression gets xformed
        String contents = '''\
            |@groovy.transform.CompileStatic
            |def meth(Object param, List list) {
            |  def result = param in list
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'in', null)
    }

    @Test
    void testCodeSelectKeywordIn6() {
        // 'in' binary expression gets xformed
        String contents = '''\
            |@groovy.transform.CompileStatic
            |def meth(Object param, List list) {
            |  [].findResults {
            |    if (param in list) {
            |      return param
            |    }
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'in', null)
    }

    @Test
    void testCodeSelectKeywordWhile1() {
        String contents = 'while(true){}'
        assertCodeSelect([contents], 'while', null)
    }

    @Test
    void testCodeSelectKeywordWhile2() {
        String contents = 'def closure = { while(true){} }'
        assertCodeSelect([contents], 'while', null)
    }

    @Test
    void testCodeSelectKeywordWhile3() {
        String contents = 'class C {def f= new Object(){void m(p){ while(p){} }}}'
        assertCodeSelect([contents], 'while', null)
    }

    @Test
    void testCodeSelectKeywordSwitch1() {
        String contents = 'switch(it){}'
        assertCodeSelect([contents], 'switch', null)
    }

    @Test
    void testCodeSelectKeywordSwitch2() {
        String contents = 'def closure = { switch(it){} }'
        assertCodeSelect([contents], 'switch', null)
    }

    @Test
    void testCodeSelectKeywordSwitch3() {
        String contents = 'class C {def f= new Object(){void m(p){ switch(p){} }}}'
        assertCodeSelect([contents], 'switch', null)
    }

    @Test
    void testCodeSelectKeywordCase1() {
        String contents = 'switch(it){case "":break;}'
        assertCodeSelect([contents], 'case', null)
    }

    @Test
    void testCodeSelectKeywordCase2() {
        String contents = 'def closure = { switch(it){case"":break;} }'
        assertCodeSelect([contents], 'case', null)
    }

    @Test
    void testCodeSelectKeywordCase3() {
        String contents = 'class C {def f= new Object(){void m(p){ switch(p){case"":break;} }}}'
        assertCodeSelect([contents], 'case', null)
    }

    @Test
    void testCodeSelectKeywordBreak1() {
        String contents = 'switch(it){case "":break;}'
        assertCodeSelect([contents], 'break', null)
    }

    @Test
    void testCodeSelectKeywordBreak2() {
        String contents = 'def closure = { switch(it){case"":break;} }'
        assertCodeSelect([contents], 'break', null)
    }

    @Test
    void testCodeSelectKeywordBreak3() {
        String contents = 'class C {def f= new Object(){void m(p){ switch(p){case"":break;} }}}'
        assertCodeSelect([contents], 'break', null)
    }

    @Test
    void testCodeSelectKeywordDefault1() {
        String contents = 'switch(it){case "":break;/**/default:}'
        assertCodeSelect([contents], 'default', null)
    }

    @Test
    void testCodeSelectKeywordDefault2() {
        String contents = 'def closure = { switch(it){case"":break;/**/default:} }'
        assertCodeSelect([contents], 'default', null)
    }

    @Test
    void testCodeSelectKeywordDefault3() {
        String contents = 'def closure = { switch(it){/**/default:/**/return null} }'
        assertCodeSelect([contents], 'default', null)
    }

    @Test
    void testCodeSelectKeywordDefault4() {
        String contents = 'class C {def f= new Object(){void m(p){ switch(p){/**/default:/**/return null} }}}'
        assertCodeSelect([contents], 'default', null)
    }

    @Test
    void testCodeSelectKeywordTry1() {
        String contents = 'try{}catch(any){}'
        assertCodeSelect([contents], 'try', null)
    }

    @Test
    void testCodeSelectKeywordTry2() {
        String contents = 'def closure = { try{}catch(any){} }'
        assertCodeSelect([contents], 'try', null)
    }

    @Test
    void testCodeSelectKeywordTry2a() {
        String contents = 'def closure = { try{Object o;}catch(any){} }'
        assertCodeSelect([contents], 'Object')
    }

    @Test
    void testCodeSelectKeywordTry3() {
        String contents = 'class C {def f= new Object(){void m(p){ try{}catch(any){} }}}'
        assertCodeSelect([contents], 'try', null)
    }

    @Test
    void testCodeSelectKeywordCatch1() {
        String contents = 'try{}catch(any){}'
        assertCodeSelect([contents], 'catch', null)
    }

    @Test
    void testCodeSelectKeywordCatch2() {
        String contents = 'def closure = { try{}catch(any){} }'
        assertCodeSelect([contents], 'catch', null)
    }

    @Test
    void testCodeSelectKeywordCatch2a() {
        String contents = 'def closure = { try{}catch(any){} }'
        assertCodeSelect([contents], 'any')
    }

    @Test
    void testCodeSelectKeywordCatch3() {
        String contents = 'class C {def f= new Object(){void m(p){ try{}catch(any){} }}}'
        assertCodeSelect([contents], 'catch', null)
    }

    @Test
    void testCodeSelectKeywordFinally1() {
        String contents = 'try{}catch(any){}finally{}'
        assertCodeSelect([contents], 'finally', null)
    }

    @Test
    void testCodeSelectKeywordFinally2() {
        String contents = 'def closure = { try{}catch(any){}finally{} }'
        assertCodeSelect([contents], 'finally', null)
    }

    @Test
    void testCodeSelectKeywordFinally2a() {
        String contents = 'def closure = { try{}catch(any){}finally{Object o;} }'
        assertCodeSelect([contents], 'Object')
    }

    @Test
    void testCodeSelectKeywordFinally3() {
        String contents = 'class C {def f= new Object(){void m(p){ try{}finally{} }}}'
        assertCodeSelect([contents], 'finally', null)
    }

    // TODO: enum, trait, interface, @interface, extends, implements, throws, throw, instanceof, synchronized, modifiers, break/continue in for/while
}
