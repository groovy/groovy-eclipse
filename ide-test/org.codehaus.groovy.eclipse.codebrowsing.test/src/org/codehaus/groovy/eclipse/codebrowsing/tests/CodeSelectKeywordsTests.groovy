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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy
import static org.junit.Assume.assumeTrue

import org.junit.Ignore
import org.junit.Test

final class CodeSelectKeywordsTests extends BrowsingTestSuite {

    @Test
    void testCodeSelectKeywordPackage() {
        String contents = '''\
            package a.b.c.d
            '''.stripIndent()
        assertCodeSelect([contents], 'package', null)
    }

    @Test
    void testCodeSelectKeywordImport() {
        String contents = '''\
            import java.util.regex.Pattern
            Pattern p = ~/123/
            '''.stripIndent()
        assertCodeSelect([contents], 'import', null)
    }

    @Test
    void testCodeSelectKeywordStaticInImport() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            def p = compile('123')
            '''.stripIndent()
        assertCodeSelect([contents], 'static', null)
    }

    @Test
    void testCodeSelectKeywordAsInImport() {
        String contents = '''\
            import static java.util.regex.Pattern.compile as build
            def p = build('123')
            '''.stripIndent()
        assertCodeSelect([contents], 'as', null)
    }

    @Test @Ignore('Is this just shorthand for getClass()? Java Editor doesn\'t code select on "class" in literal')
    void testSelectKeywordClass1() {
        String contents = 'String.class'
        assertCodeSelect([contents], 'class', null)
    }

    @Test
    void testSelectKeywordClass2() {
        String contents = 'class C { }'
        assertCodeSelect([contents], 'class', null)
    }

    @Test
    void testSelectKeywordClass3() {
        String contents = '@groovy.transform.Canonical class C { }'
        assertCodeSelect([contents], 'class', null)
    }

    @Test
    void testSelectKeywordClass4() {
        String contents = '@groovy.transform.Immutable class C { }'
        assertCodeSelect([contents], 'class', null)
    }

    @Test
    void testSelectKeywordDef1() {
        String contents = 'class C { def x() { } }'
        assertCodeSelect([contents], 'def', null)
    }

    @Test
    void testSelectKeywordDef2() {
        String contents = 'class C { Object x() { def y } }'
        assertCodeSelect([contents], 'def', null)
    }

    @Test
    void testSelectKeywordNew1() {
        String contents = 'def x = new Object()'
        assertCodeSelect([contents], 'new', null)
    }

    @Test
    void testSelectKeywordNew2() {
        String contents = 'def x = new double[0]'
        assertCodeSelect([contents], 'new', null)
    }

    @Test
    void testSelectKeywordNull() {
        String contents = 'Object o = null'
        assertCodeSelect([contents], 'null', null)
    }

    @Test
    void testSelectKeywordTrue() {
        String contents = 'boolean b = true'
        assertCodeSelect([contents], 'true', null)
    }

    @Test
    void testSelectKeywordFalse() {
        String contents = 'boolean b = false'
        assertCodeSelect([contents], 'false', null)
    }

    @Test
    void testSelectKeywordThis1() {
        // Java Editor doesn't code select on 'this' variable expression
        String contents = 'class C { def x() { this } }'
        assertCodeSelect([contents], 'this', null)
    }

    @Test
    void testSelectKeywordThis2() {
        // Java Editor doesn't code select on 'this' variable expression
        String contents = 'class C { def x() { this.toString() } }'
        assertCodeSelect([contents], 'this', null)
    }

    @Test // GRECLIPSE-548
    void testSelectKeywordSuper1() {
        String contents = '''\
            class Super { }
            class C extends Super {
              def x() { super }
            }
            '''.stripIndent()
        assertCodeSelect([contents], 'super', 'Super')
    }

    @Test // GRECLIPSE-548
    void testSelectKeywordSuper2() {
        String contents = '''\
            class Super { }
            class C extends Super {
              def x() { super.toString() }
            }
            '''.stripIndent()
        assertCodeSelect([contents], 'super', 'Super')
    }

    @Test
    void testSelectKeywordReturn1() {
        String contents = 'def meth() { return null }'
        assertCodeSelect([contents], 'return', null)
    }

    @Test
    void testSelectKeywordReturn2() {
        String contents = 'def closure = { -> return null }'
        assertCodeSelect([contents], 'return', null)
    }

    @Test
    void testSelectKeywordAssert1() {
        String contents = 'assert it != null'
        assertCodeSelect([contents], 'assert', null)
    }

    @Test
    void testSelectKeywordAssert2() {
        String contents = 'def closure = { assert it != null }'
        assertCodeSelect([contents], 'assert', null)
    }

    @Test
    void testSelectKeywordIf1() {
        String contents = 'if(true){}'
        assertCodeSelect([contents], 'if', null)
    }

    @Test
    void testSelectKeywordIf2() {
        String contents = 'def closure = { if(true){} }'
        assertCodeSelect([contents], 'if', null)
    }

    @Test
    void testSelectKeywordIf3() {
        String contents = 'def closure = { if(true){}else if(false){} }'
        assertCodeSelect([contents], 'if', null)
    }

    @Test
    void testSelectKeywordIf4() {
        String contents = 'class C {def f= new Object(){void m(p){ if(p){} }}}'
        assertCodeSelect([contents], 'if', null)
    }

    @Test
    void testSelectKeywordElse1() {
        String contents = 'if(true){}else{}'
        assertCodeSelect([contents], 'else', null)
    }

    @Test
    void testSelectKeywordElse2() {
        String contents = 'def closure = { if(true){}else{} }'
        assertCodeSelect([contents], 'else', null)
    }

    @Test
    void testSelectKeywordElse3() {
        String contents = 'def closure = { if(true){}else if (false){}else{} }'
        assertCodeSelect([contents], 'else', null)
    }

    @Test
    void testSelectKeywordElse4() {
        String contents = 'class C {def f= new Object(){void m(p){ if(p){}else{} }}}'
        assertCodeSelect([contents], 'else', null)
    }

    @Test
    void testSelectKeywordFor1() {
        String contents = 'for(item in []){}'
        assertCodeSelect([contents], 'for', null)
    }

    @Test
    void testSelectKeywordFor2() {
        String contents = 'def closure = { for(item in []){} }'
        assertCodeSelect([contents], 'for', null)
    }

    @Test
    void testSelectKeywordFor3() {
        String contents = 'class C {def f= new Object(){void m(p){ for(item in []){} }}}'
        assertCodeSelect([contents], 'for', null)
    }

    @Test
    void testSelectKeywordIn1() {
        String contents = 'for(item in []){}'
        assertCodeSelect([contents], 'in', null)
    }

    @Test
    void testSelectKeywordIn2() {
        String contents = 'def closure = { for(item in []){} }'
        assertCodeSelect([contents], 'in', null)
    }

    @Test
    void testSelectKeywordIn3() {
        String contents = 'class C {def f= new Object(){void m(p){ for(item in []){} }}}'
        assertCodeSelect([contents], 'in', null)
    }

    @Test
    void testSelectKeywordIn4() {
        String contents = 'def closure = { it in [] }'
        assertCodeSelect([contents], 'in', null)
    }

    @Test
    void testSelectKeywordIn5() {
        assumeTrue(isAtLeastGroovy(20))

        // 'in' binary expression gets xformed
        String contents = '''\
            @groovy.transform.CompileStatic
            def meth(Object param, List list) {
              def result = param in list
            }
            '''.stripIndent()
        assertCodeSelect([contents], 'in', null)
    }

    @Test
    void testSelectKeywordIn6() {
        assumeTrue(isAtLeastGroovy(20))

        // 'in' binary expression gets xformed
        String contents = '''\
            @groovy.transform.CompileStatic
            def meth(Object param, List list) {
              [].findResults {
                if (param in list) {
                  return param
                }
              }
            }
            '''.stripIndent()
        assertCodeSelect([contents], 'in', null)
    }

    @Test
    void testSelectKeywordWhile1() {
        String contents = 'while(true){}'
        assertCodeSelect([contents], 'while', null)
    }

    @Test
    void testSelectKeywordWhile2() {
        String contents = 'def closure = { while(true){} }'
        assertCodeSelect([contents], 'while', null)
    }

    @Test
    void testSelectKeywordWhile3() {
        String contents = 'class C {def f= new Object(){void m(p){ while(p){} }}}'
        assertCodeSelect([contents], 'while', null)
    }

    @Test
    void testSelectKeywordSwitch1() {
        String contents = 'switch(it){}'
        assertCodeSelect([contents], 'switch', null)
    }

    @Test
    void testSelectKeywordSwitch2() {
        String contents = 'def closure = { switch(it){} }'
        assertCodeSelect([contents], 'switch', null)
    }

    @Test
    void testSelectKeywordSwitch3() {
        String contents = 'class C {def f= new Object(){void m(p){ switch(p){} }}}'
        assertCodeSelect([contents], 'switch', null)
    }

    @Test
    void testSelectKeywordCase1() {
        String contents = 'switch(it){case "":break;}'
        assertCodeSelect([contents], 'case', null)
    }

    @Test
    void testSelectKeywordCase2() {
        String contents = 'def closure = { switch(it){case"":break;} }'
        assertCodeSelect([contents], 'case', null)
    }

    @Test
    void testSelectKeywordCase3() {
        String contents = 'class C {def f= new Object(){void m(p){ switch(p){case"":break;} }}}'
        assertCodeSelect([contents], 'case', null)
    }

    @Test
    void testSelectKeywordBreak1() {
        String contents = 'switch(it){case "":break;}'
        assertCodeSelect([contents], 'break', null)
    }

    @Test
    void testSelectKeywordBreak2() {
        String contents = 'def closure = { switch(it){case"":break;} }'
        assertCodeSelect([contents], 'break', null)
    }

    @Test
    void testSelectKeywordBreak3() {
        String contents = 'class C {def f= new Object(){void m(p){ switch(p){case"":break;} }}}'
        assertCodeSelect([contents], 'break', null)
    }

    @Test
    void testSelectKeywordDefault1() {
        String contents = 'switch(it){case "":break;/**/default:}'
        assertCodeSelect([contents], 'default', null)
    }

    @Test
    void testSelectKeywordDefault2() {
        String contents = 'def closure = { switch(it){case"":break;/**/default:} }'
        assertCodeSelect([contents], 'default', null)
    }

    @Test
    void testSelectKeywordDefault3() {
        String contents = 'def closure = { switch(it){/**/default:/**/return null} }'
        assertCodeSelect([contents], 'default', null)
    }

    @Test
    void testSelectKeywordDefault4() {
        String contents = 'class C {def f= new Object(){void m(p){ switch(p){/**/default:/**/return null} }}}'
        assertCodeSelect([contents], 'default', null)
    }

    @Test
    void testSelectKeywordTry1() {
        String contents = 'try{}catch(any){}'
        assertCodeSelect([contents], 'try', null)
    }

    @Test
    void testSelectKeywordTry2() {
        String contents = 'def closure = { try{}catch(any){} }'
        assertCodeSelect([contents], 'try', null)
    }

    @Test
    void testSelectKeywordTry2a() {
        String contents = 'def closure = { try{Object o;}catch(any){} }'
        assertCodeSelect([contents], 'Object')
    }

    @Test
    void testSelectKeywordTry3() {
        String contents = 'class C {def f= new Object(){void m(p){ try{}catch(any){} }}}'
        assertCodeSelect([contents], 'try', null)
    }

    @Test
    void testSelectKeywordCatch1() {
        String contents = 'try{}catch(any){}'
        assertCodeSelect([contents], 'catch', null)
    }

    @Test
    void testSelectKeywordCatch2() {
        String contents = 'def closure = { try{}catch(any){} }'
        assertCodeSelect([contents], 'catch', null)
    }

    @Test
    void testSelectKeywordCatch2a() {
        String contents = 'def closure = { try{}catch(any){} }'
        assertCodeSelect([contents], 'any')
    }

    @Test
    void testSelectKeywordCatch3() {
        String contents = 'class C {def f= new Object(){void m(p){ try{}catch(any){} }}}'
        assertCodeSelect([contents], 'catch', null)
    }

    @Test
    void testSelectKeywordFinally1() {
        String contents = 'try{}catch(any){}finally{}'
        assertCodeSelect([contents], 'finally', null)
    }

    @Test
    void testSelectKeywordFinally2() {
        String contents = 'def closure = { try{}catch(any){}finally{} }'
        assertCodeSelect([contents], 'finally', null)
    }

    @Test
    void testSelectKeywordFinally2a() {
        String contents = 'def closure = { try{}catch(any){}finally{Object o;} }'
        assertCodeSelect([contents], 'Object')
    }

    @Test
    void testSelectKeywordFinally3() {
        String contents = 'class C {def f= new Object(){void m(p){ try{}finally{} }}}'
        assertCodeSelect([contents], 'finally', null)
    }

    // TODO: enum, trait, interface, @interface, extends, implements, throws, throw, instanceof, synchronized, modifiers, break/continue in for/while
}
