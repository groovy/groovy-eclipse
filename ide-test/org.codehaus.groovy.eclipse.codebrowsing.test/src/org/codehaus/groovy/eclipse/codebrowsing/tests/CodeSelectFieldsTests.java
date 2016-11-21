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
package org.codehaus.groovy.eclipse.codebrowsing.tests;

import static java.util.Arrays.asList;

/**
 * @author Andrew Eisenberg
 * @created Jun 3, 2009
 */
public final class CodeSelectFieldsTests extends BrowsingTestCase {

    public static junit.framework.Test suite() {
        return newTestSuite(CodeSelectFieldsTests.class);
    }

    public void testCodeSelectFieldInClass() {
        assertCodeSelect(asList("class Foo {\n def x = 9\ndef y() { x++ }\n }"), "x");
    }

    public void testCodeSelectFieldInOtherClass() {
        assertCodeSelect(asList("class Foo { def x = 9\n }", "class Bar { def y() { new Foo().x++\n }\n }"), "x");
    }

    public void testCodeSelectFieldInSuperClass() {
        assertCodeSelect(asList("class Foo { def x = 9\n }", "class Bar extends Foo { def y() { x++\n }\n }"), "x");
    }

    public void testCodeSelectStaticFieldInClass() {
        assertCodeSelect(asList("class Foo {\n static def x = 9\ndef y() { Foo.x++ }\n }"), "x");
    }

    public void testCodeSelectStaticFieldInOtherClass() {
        assertCodeSelect(asList("class Foo { static def x = 9\n }", "class Bar { def y() { Foo.x++\n }\n }"), "x");
    }

    public void testCodeSelectLazyFieldInClass() {
        assertCodeSelect(asList("class Foo {\n  @Lazy def x = 9\n}"), "x");
    }

    public void testCodeSelectLoggerFieldInClass() {
        // field added by @Log transform is not fully selectable
        assertCodeSelect(asList(
            "@groovy.util.logging.Log\n" +
            "class Foo {\n" +
            "  String str\n" +
            "  def meth() {\n" +
            "    log.info \"$str msg\"\n" +
            "  }\n" +
            "}"
        ), "log", "Foo"); // TODO: Want this to be "log", but field is not in Java model.
    }

    public void testCodeSelectInClosure() {
        assertCodeSelect(asList("def x = {\nt -> print t\n}\nx(\"hello\")"), "t");
    }

    public void testCodeSelectInClosure2Params() {
        assertCodeSelect(asList("def x = {\ns, t -> print t\n}\nx(\"hello\")"), "t");
    }

    public void testCodeSelectLocalVarInClosure() {
        assertCodeSelect(asList("def y = 9\ndef x = {\nt -> print y\n}"), "y");
    }

    public void testCodeSelectFieldInClosure() {
        assertCodeSelect(asList("class X { \n def y=9\n } \ndef x = {\nt -> print new X().y\n}"), "y");
    }

    public void testCodeSelectFieldFromSuperInClosure() {
        assertCodeSelect(asList("class X { \n def y=9\n } \nclass Y extends X { }\ndef x = {\nt -> print new Y().y\n}"), "y");
    }

    public void testCodeSelectStaticFieldInClosure() {
        assertCodeSelect(asList("class X { \n static def y=9\n \ndef z() {\ndef x = {\nt -> print X.y\n}\n}\n}"), "y");
    }

    public void testCodeSelectStaticFieldFromOtherInClosure() {
        assertCodeSelect(asList("class X { \n static def y=9\n } \ndef x = {\nt -> print X.y\n}"), "y");
    }

    public void testCodeSelectInFieldInitializer() {
        assertCodeSelect(asList("class X { \n def y= { z() }\ndef z() { } }"), "z");
    }

    public void testCodeSelectInStaticFieldInitializer() {
        assertCodeSelect(asList("class X { \n static y= { z() }\nstatic z() { } }"), "z");
    }

    // GRECLIPSE-516
    public void testCodeSelectOfGeneratedGetter() {
        assertCodeSelect(asList("class C { \n int num\ndef foo() {\n getNum() } }"), "getNum", "num");
    }

    // GRECLIPSE-516
    public void testCodeSelectOfGeneratedSetter() {
        assertCodeSelect(asList("class C { \n int num\ndef foo() {\n setNum() } }"), "setNum", "num");
    }

    public void testCodeSelectInsideGString1() {
        assertCodeSelect(asList("def foo\n\"${foo}\""), "foo");
    }

    public void testCodeSelectInsideGString2() {
        assertCodeSelect(asList("def foo\n\"${foo.toString()}\""), "foo");
    }

    public void testCodeSelectInsideGString3() {
        assertCodeSelect(asList("def foo\n\"${foo.toString()}\""), "toString");
    }

    public void testCodeSelectInsideGString4() {
        assertCodeSelect(asList("def foo\n\"${foo}\""), "o", "foo");
    }

    public void testCodeSelectInsideGString5() {
        assertCodeSelect(asList("def foo\n\"${toString()}\""), "toString");
    }
}
