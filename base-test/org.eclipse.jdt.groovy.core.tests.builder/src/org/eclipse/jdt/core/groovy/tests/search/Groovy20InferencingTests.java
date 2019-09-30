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

import org.junit.Test;

public final class Groovy20InferencingTests extends InferencingTestSuite {

    @Test // tests CompareToNullExpression
    public void testCompileStatic1() {
        String contents = "import groovy.transform.CompileStatic\n" +
            "class Groovy20 {\n" +
            "  @CompileStatic\n" +
            "  def meth(String args) {\n" +
            "    args != null\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "args", "java.lang.String");
    }

    @Test // tests CompareToNullExpression
    public void testCompileStatic2() {
        String contents = "import groovy.transform.CompileStatic\n" +
            "class Groovy20 {\n" +
            "  @CompileStatic\n" +
            "  def meth(String args) {\n" +
            "    args == null\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "args", "java.lang.String");
    }

    @Test // tests CompareToNullExpression
    public void testCompileStatic3() {
        String contents = "import groovy.transform.CompileStatic\n" +
            "class Groovy20 {\n" +
            "  @CompileStatic\n" +
            "  def meth(String args) {\n" +
            "    null == args\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "args", "java.lang.String");
    }

    @Test // tests CompareIdentityExpression
    public void testCompileStatic4() {
        String contents = "import groovy.transform.CompileStatic\n" +
            "class Groovy20 {\n" +
            "  @CompileStatic\n" +
            "  def meth(String args) {\n" +
            "    args == 9\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "args", "java.lang.String");
    }

    @Test // tests CompareIdentityExpression
    public void testCompileStatic5() {
        String contents = "import groovy.transform.CompileStatic\n" +
            "class Groovy20 {\n" +
            "  @CompileStatic\n" +
            "  def meth(String args) {\n" +
            "    9 == args\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "args", "java.lang.String");
    }

    @Test // tests instanceof flow typing
    public void testCompileStatic6() {
        String contents = "@groovy.transform.CompileStatic\n" +
            "class Groovy20 {\n" +
            "  def meth(def x) {\n" +
            "    if (x instanceof Number) {\n" +
            "      x.intValue()\n" +
            "    }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "x", "java.lang.Number");
    }

    @Test // tests instanceof flow typing
    public void testCompileStatic7() {
        String contents = "@groovy.transform.CompileStatic\n" +
            "class Groovy20 {\n" +
            "  def meth(def x) {\n" +
            "    if (!(x instanceof Number)) {\n" +
            "      x.toString()\n" +
            "    }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "x", "java.lang.Object");
    }

    @Test // tests instanceof flow typing
    public void testCompileStatic8() {
        String contents = "@groovy.transform.CompileStatic\n" +
            "class Groovy20 {\n" +
            "  def meth(def x) {\n" +
            "    x instanceof Number ? x.intValue() : null\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "x", "java.lang.Number");
    }

    @Test // tests instanceof flow typing
    public void testCompileStatic9() {
        String contents = "@groovy.transform.CompileStatic\n" +
            "class Groovy20 {\n" +
            "  def meth(def x) {\n" +
            "    !(x instanceof Number) ? x.toString() : null\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "x", "java.lang.Object");
    }

    @Test // GRECLIPSE-1720
    public void testCompileStatic10() {
        String contents = "@groovy.transform.CompileStatic\n" +
            "public class Groovy20 {\n" +
            "  enum Letter { A,B,C }\n" +
            "  boolean bug(Letter l) {\n" +
            "    boolean isEarly = l in [Letter.A, Letter.B]\n" +
            "    isEarly\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "isEarly", "java.lang.Boolean");
    }

    @Test // indirect static-star method reference
    public void testCompileStatic11() {
        String contents = "import static B.*\n" +
            "class A {\n" +
            "  static boolean isOne() {}\n" +
            "}\n" +
            "class B extends A {\n" +
            "  static boolean isTwo() {}\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  isOne()\n" +
            "  isTwo()\n" +
            "}";
        assertExprType(contents, "isOne", "java.lang.Boolean");
        assertExprType(contents, "isTwo", "java.lang.Boolean");
    }

    @Test // indirect static-star property reference
    public void testCompileStatic12() {
        String contents = "import static B.*\n" +
            "class A {\n" +
            "  static boolean isOne() {}\n" +
            "}\n" +
            "class B extends A {\n" +
            "  static boolean isTwo() {}\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  one\n" +
            "  two\n" +
            "}";
        assertExprType(contents, "one", "java.lang.Boolean");
        assertExprType(contents, "two", "java.lang.Boolean");
    }

    @Test
    public void testCompileStatic13() {
        String contents = "@groovy.transform.CompileStatic\n" +
            "class X {\n" +
            "  Number getReadOnly() {}\n" +
            "  static {\n" +
            "    new X().with {\n" +
            "      def val = readOnly\n" +
            "      readOnly = []\n" +
            "    }\n" +
            "  }\n" +
            "}";

        int offset = contents.indexOf("readOnly");
        assertType(contents, offset, offset + "readOnly".length(), "java.lang.Number");
            offset = contents.lastIndexOf("readOnly");
        assertUnknownConfidence(contents, offset, offset + "readOnly".length());
    }

    @Test
    public void testCompileStatic14() {
        String contents = "@groovy.transform.CompileStatic\n" +
            "class X {\n" +
            "  void setWriteOnly(Number value) {}\n" +
            "  static {\n" +
            "    new X().with {\n" +
            "      writeOnly = 42\n" +
            "      def val = writeOnly\n" +
            "    }\n" +
            "  }\n" +
            "}";

        int offset = contents.indexOf("writeOnly");
        assertType(contents, offset, offset + "writeOnly".length(), "java.lang.Integer");
            offset = contents.lastIndexOf("writeOnly");
        assertUnknownConfidence(contents, offset, offset + "writeOnly".length());
    }

    @Test
    public void testCompileStatic15() {
        String contents = "@groovy.transform.CompileStatic\n" +
            "void meth() {\n" +
            "  java.util.concurrent.Callable<String> task = { -> '' }\n" +
            "  def result = task.call()" +
            "}";

        int offset = contents.lastIndexOf("task");
        assertType(contents, offset, offset + "task".length(), "java.util.concurrent.Callable<java.lang.String>");
            offset = contents.lastIndexOf("result");
        assertType(contents, offset, offset + "result".length(), "java.lang.String");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/872
    public void testCompileStatic16() {
        String contents = "@groovy.transform.CompileStatic\n" +
            "void meth(ObjectInputStream ois) {\n" +
            "  Map<String,Object> props = (Map) ois.readObject()\n" +
            "  props.each { Map.Entry<String,Object> e ->\n" +
            "    setProperty((String) e.key, e.value)\n" +
            "  }\n" +
            "  def map = [foo: 'cat', bar: 'hat']\n" +
            "  map.each { entry ->\n" +
            "    println entry.value\n" +
            "  }\n" +
            "}";

        int offset = contents.lastIndexOf("e.");
        assertType(contents, offset, offset + 1, "java.util.Map$Entry<java.lang.String,java.lang.Object>");
            offset = contents.lastIndexOf("entry");
        assertType(contents, offset, offset + "entry".length(), "java.util.Map$Entry<java.lang.String,java.lang.String>");
    }

    @Test // tests CompareToNullExpression
    public void testTypeChecked1() {
        String contents = "import groovy.transform.TypeChecked\n" +
            "class Groovy20 {\n" +
            "  @TypeChecked\n" +
            "  def meth(String args) {\n" +
            "    args != null\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "args", "java.lang.String");
    }

    @Test // tests CompareToNullExpression
    public void testTypeChecked2() {
        String contents = "import groovy.transform.TypeChecked\n" +
            "class Groovy20 {\n" +
            "  @TypeChecked\n" +
            "  def meth(String args) {\n" +
            "    args == null\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "args", "java.lang.String");
    }

    @Test // tests CompareToNullExpression
    public void testTypeChecked3() {
        String contents = "import groovy.transform.TypeChecked\n" +
            "class Groovy20 {\n" +
            "  @TypeChecked\n" +
            "  def meth(String args) {\n" +
            "    null == args\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "args", "java.lang.String");
    }

    @Test // tests CompareIdentityExpression
    public void testTypeChecked4() {
        String contents = "import groovy.transform.TypeChecked\n" +
            "class Groovy20 {\n" +
            "  @TypeChecked\n" +
            "  def meth(String args) {\n" +
            "    args== 9\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "args", "java.lang.String");
    }

    @Test // tests CompareIdentityExpression
    public void testTypeChecked5() {
        String contents = "import groovy.transform.TypeChecked\n" +
            "class Groovy20 {\n" +
            "  @TypeChecked\n" +
            "  def meth(String args) {\n" +
            "    9 == args\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "args", "java.lang.String");
    }

    @Test // tests instanceof flow typing
    public void testTypeChecked6() {
        String contents = "@groovy.transform.TypeChecked\n" +
            "class Groovy20 {\n" +
            "  def meth(def x) {\n" +
            "    if (x instanceof Number) {\n" +
            "      x.intValue()\n" +
            "    }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "x", "java.lang.Number");
    }

    @Test // tests instanceof flow typing
    public void testTypeChecked7() {
        String contents = "@groovy.transform.TypeChecked\n" +
            "class Groovy20 {\n" +
            "  def meth(def x) {\n" +
            "    if (!(x instanceof Number)) {\n" +
            "      x.toString()\n" +
            "    }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "x", "java.lang.Object");
    }

    @Test // tests instanceof flow typing
    public void testTypeChecked8() {
        String contents = "@groovy.transform.TypeChecked\n" +
            "class Groovy20 {\n" +
            "  def meth(def x) {\n" +
            "    x instanceof Number ? x.intValue() : ''\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "x", "java.lang.Number");
    }

    //--------------------------------------------------------------------------

    private void assertExprType(String source, String target, String type) {
        int offset = source.lastIndexOf(target);
        assertType(source, offset, offset + target.length(), type);
    }
}
