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

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assume.assumeTrue;

import org.junit.Before;
import org.junit.Test;

public final class Groovy20InferencingTests extends InferencingTestSuite {

    @Before
    public void setUp() {
        assumeTrue(isAtLeastGroovy(20));
    }

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
