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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.groovy.core.Activator;
import org.junit.Test;

/**
 * Tests for all Groovy 2.1 specific things for example, {@link groovy.lang.DelegatesTo}.
 */
public final class Groovy21InferencingTests extends InferencingTestSuite {

    @Test
    public void testDelegatesToValue() {
        String contents =
            "class Other { }\n" +
            "def meth(@DelegatesTo(Other) Closure c) { }\n" +
            "meth { delegate }";

        assertType(contents, "delegate", "Other");
    }

    @Test
    public void testDelegatesToValue2() {
        String contents =
            "class Other { }\n" +
            "def meth(@DelegatesTo(Other) c) { }\n" +
            "meth { delegate }";

        assertType(contents, "delegate", "Other");
    }

    @Test
    public void testDelegatesToValue3() {
        String contents =
            "class Other { int xxx }\n" +
            "def meth(@DelegatesTo(Other) Closure c) { }\n" +
            "meth { xxx }";

        assertType(contents, "xxx", "java.lang.Integer");
    }

    @Test
    public void testDelegatesToValue4() {
        String contents =
            "def meth(@DelegatesTo(List) Closure c) { }\n" +
            "meth { delegate }";

        assertType(contents, "delegate", "java.util.List");
    }

    @Test
    public void testDelegatesToValue5() {
        String contents =
            "def meth(int x, int y, @DelegatesTo(List) Closure c) { }\n" +
            "meth 1, 2, { delegate }";

        assertType(contents, "delegate", "java.util.List");
    }

    @Test // expected to be broken (due to missing closing angle bracket on type)
    public void testDelegatesToValue6() {
        String contents =
            "def meth(int x, int y, @DelegatesTo(List<String) Closure c) { }\n" +
            "meth { delegate }";

        assertType(contents, "delegate", DEFAULT_UNIT_NAME);
    }

    @Test
    public void testDelegatesToTarget1() {
        createUnit("C", "import groovy.lang.DelegatesTo.Target; class C { static def cat(\n" +
            "@Target Object self, @DelegatesTo(strategy=Closure.DELEGATE_FIRST) Closure code) { } }");

        String contents = "class A { def x }\n" +
            "class B { def x, y\n" +
            "  def m(A a) {\n" +
            "    use (C) {\n" +
            "      a.cat {" + // delegate is A, owner is B
            "        x\n" +
            "        y\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

        assertDeclaringType(contents, "x", "A");
        assertDeclaringType(contents, "y", "B");
    }

    @Test
    public void testDelegatesToTarget2() {
        createUnit("C", "import groovy.lang.DelegatesTo.Target; class C { static def cat(\n" +
            "@Target('self') Object self, @DelegatesTo(target='self', strategy=Closure.DELEGATE_FIRST) Closure code) { } }");

        String contents = "class A { def x }\n" +
            "class B { def x, y\n" +
            "  def m(A a) {\n" +
            "    use (C) {\n" +
            "      a.cat {" + // delegate is A, owner is B
            "        x\n" +
            "        y\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

        assertDeclaringType(contents, "x", "A");
        assertDeclaringType(contents, "y", "B");
    }

    @Test // uses constant instead of literal for target
    public void testDelegatesToTarget2a() {
        createUnit("C", "import groovy.lang.DelegatesTo.Target\n" +
            "class C {\n" +
            "  private static final String SELF = 'self'\n" +
            "  static def cat(\n" +
            "    @Target(C.SELF) Object self,\n" + // getText() will not work with qualifier
            "    @DelegatesTo(target=SELF, strategy=Closure.DELEGATE_FIRST) Closure code\n" +
            "   ) { }\n" +
            "}");

        String contents = "class A { def x }\n" +
            "class B { def x, y\n" +
            "  def m(A a) {\n" +
            "    use (C) {\n" +
            "      a.cat {" + // delegate is A, owner is B
            "        x\n" +
            "        y\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

        assertDeclaringType(contents, "x", "A");
        assertDeclaringType(contents, "y", "B");
    }

    @Test
    public void testDelegatesToTarget3() {
        createUnit("C", "import groovy.lang.DelegatesTo.Target; class C { static def cat(\n" +
            "@Target('self') Object self, @DelegatesTo(target='self', strategy=Closure.DELEGATE_ONLY) Closure code) { } }");

        String contents = "class A { def x }\n" +
            "class B { def x, y\n" +
            "  def m(A a) {\n" +
            "    use (C) {\n" +
            "      a.cat {" + // delegate is A, owner is B
            "        x\n" +
            "        y\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

        assertDeclaringType(contents, "x", "A");
        int offset = contents.lastIndexOf('y');
        assertUnknownConfidence(contents, offset, offset + 1);
    }

    @Test
    public void testDelegatesToTarget4() {
        createUnit("C", "import groovy.lang.DelegatesTo.Target; class C { static def cat(\n" +
            "@Target('self') Object self, @DelegatesTo(target='self', strategy=Closure.OWNER_FIRST) Closure code) { } }");

        String contents = "class A { def x, z }\n" +
            "class B { def x, y\n" +
            "  def m(A a) {\n" +
            "    use (C) {\n" +
            "      a.cat {" + // delegate is A, owner is B
            "        x\n" +
            "        y\n" +
            "        z\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

        assertDeclaringType(contents, "x", "B");
        assertDeclaringType(contents, "y", "B");
        assertDeclaringType(contents, "z", "A");
    }

    @Test
    public void testDelegatesToTarget5() {
        createUnit("C", "import groovy.lang.DelegatesTo.Target; class C { static def cat(\n" +
            "@Target('self') Object self, @DelegatesTo(target='self', strategy=Closure.OWNER_ONLY) Closure code) { } }");

        String contents = "class A { def x }\n" +
            "class B { def x, y\n" +
            "  def m(A a) {\n" +
            "    use (C) {\n" +
            "      a.cat {" + // delegate is A, owner is B
            "        x\n" +
            "        y\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

        assertDeclaringType(contents, "x", "B");
        assertDeclaringType(contents, "y", "B");
    }

    @Test // seemingly invalid combination
    public void testDelegatesToTarget6() {
        createUnit("C", "import groovy.lang.DelegatesTo.Target; class C { static def cat(\n" +
            "@Target('self') Object self, @DelegatesTo(target='self', strategy=Closure.TO_SELF) Closure code) { } }");

        String contents = "class A { def x }\n" +
            "class B { def x, y\n" +
            "  def m(A a) {\n" +
            "    use (C) {\n" +
            "      a.cat {" + // delegate is A, owner is B
            "        x\n" +
            "        y\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

        int offset = contents.lastIndexOf('x');
        assertUnknownConfidence(contents, offset, offset + 1);
        offset = contents.lastIndexOf('y');
        assertUnknownConfidence(contents, offset, offset + 1);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/415
    public void testDelegatesToTypeName1() {
        String contents =
            "def meth(int x, int y, @DelegatesTo(type='java.util.List') Closure c) { }\n" +
            "meth 1, 2, { delegate }";

        assertType(contents, "delegate", "java.util.List");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/415
    public void testDelegatesToTypeName2() {
        String contents =
            "class C {\n" +
            "  private static final String LIST = 'java.util.List'\n" +
            "  static void meth(int x, int y, @DelegatesTo(type=LIST) Closure c) { }\n" +
            "}\n" +
            "C.meth 1, 2, { delegate }";

        assertType(contents, "delegate", "java.util.List");
    }

    @Test
    public void testDelegatesToResolveStrategy2() {
        String contents =
            "class A {}\n" +
            "class B { \n" +
            "  def m(@DelegatesTo(value=A, strategy=Closure.OWNER_ONLY) Closure code) {\n" +
            "  }\n" +
            "  def x() {\n" +
            "    m {" + // delegate is A, owner is B
            "      delegate\n" +
            "      owner\n" +
            "    }\n" +
            "  }\n" +
            "}";

        assertType(contents, "delegate", "A");
        assertType(contents, "owner", "B");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/657
    public void testDelegatesToResolveStrategy3() {
        String contents =
            "class A {}\n" +
            "class B { \n" +
            "  def m(@DelegatesTo(value=A, strategy=Closure.DELEGATE_ONLY) Closure code) {\n" +
            "  }\n" +
            "  def x() {\n" +
            "    m {" + // delegate is A, owner is B
            "      delegate\n" +
            "      owner\n" +
            "    }\n" +
            "  }\n" +
            "}";

        assertType(contents, "delegate", "A");
        assertType(contents, "owner", "B");
    }

    @Test
    public void testDelegatesToResolveStrategy4() {
        String contents =
            "class A {}\n" +
            "class B { \n" +
            "  def m(@DelegatesTo(value=A, strategy=Closure.TO_SELF) Closure code) {\n" +
            "  }\n" +
            "  def x() {\n" +
            "    m {" + // delegate is A, owner is B
            "      delegate\n" +
            "      owner\n" +
            "    }\n" +
            "  }\n" +
            "}";

        assertType(contents, "delegate", "A");
        assertType(contents, "owner", "B");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/389
    public void testEnumOverrides() {
        String contents =
            "enum E {\n" +
            "  ONE() {\n" +
            "    void meth(Number param) { println param }\n" +
            "  },\n" +
            "  TWO() {\n" +
            "    void meth(Number param) { null }\n" +
            "  }\n" +
            "  abstract void meth(Number param);\n" +
            "}";

        int offset = contents.indexOf("println param") + "println ".length();
        assertType(contents, offset, offset + "param".length(), "java.lang.Number");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/390
    public void testEnumOverrides2() {
        String contents =
            "@groovy.transform.CompileStatic\n" +
            "enum E {\n" +
            "  ONE() {\n" +
            "    void meth(Number param) { println param }\n" +
            "  },\n" +
            "  TWO() {\n" +
            "    void meth(Number param) { null }\n" +
            "  }\n" +
            "  abstract void meth(Number param);\n" +
            "}";

        int offset = contents.indexOf("println param") + "println ".length();
        assertType(contents, offset, offset + "param".length(), "java.lang.Number");
    }

    @Test
    public void testTypeCheckingExtension() {
        Activator.getInstancePreferences().getBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, Activator.DEFAULT_SCRIPT_FILTERS_ENABLED);
        Activator.getInstancePreferences().get(Activator.GROOVY_SCRIPT_FILTERS, Activator.DEFAULT_GROOVY_SCRIPT_FILTER);
        try {
            // the type checking script
            IPath robotPath = env.addPackage(project.getFolder("src").getFullPath(), "robot");
            env.addGroovyClass(robotPath, "RobotMove", "package robot\n" +
                "import org.codehaus.groovy.ast.expr.MethodCall\n" +
                "import org.codehaus.groovy.ast.expr.VariableExpression\n" +
                "unresolvedVariable { VariableExpression var ->\n" +
                "    if ('robot' == var.name) {\n" +
                "        def robotClass = context.source.AST.classes.find { it.name == 'Robot' }\n" +
                "        storeType(var, robotClass)\n" +
                "        handled = true\n" +
                "    }\n" +
                "}\n" +
                "afterMethodCall { MethodCall mc ->\n" +
                "    def method = getTargetMethod(mc)\n" +
                "    if (mc.objectExpression.name == 'robot' && method.name == 'move') {\n" +
                "        def args = getArguments(mc)\n" +
                "        if (args && isConstantExpression(args[0]) && args[0].value instanceof String) {\n" +
                "            def content = args[0].text\n" +
                "            if (!(content in ['left', 'right', 'backward', 'forward'])) {\n" +
                "                addStaticTypeError(\"'${content}' is not a valid direction\", args[0])\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}");

            // set the script folders
            Activator.getInstancePreferences().putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, true);
            Activator.getInstancePreferences().put(Activator.GROOVY_SCRIPT_FILTERS, "src/robot/*Move.groovy,y");

            env.fullBuild();

            String contents =
                "import groovy.transform.TypeChecked\n" +
                "class Robot {\n" +
                "    void move(String dist) { println \"Moved $dist\" }\n" +
                "}\n" +
                "@TypeChecked(extensions = 'robot/RobotMove.groovy')\n" +
                "void operate() {\n" +
                "    robot.move \"left\"\n" +
                "}";

            assertType(contents, "robot", "Robot");
            assertType(contents, "move", "java.lang.Void");

            // also, just make sure no problems
            env.fullBuild(project.getFullPath());
            Problem[] problems = env.getProblemsFor(project.getFullPath());
            assertEquals("Should have found no problems in:\n" + Arrays.toString(problems), 0, problems.length);
        } finally {
            Activator.getInstancePreferences().putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, Activator.DEFAULT_SCRIPT_FILTERS_ENABLED);
            Activator.getInstancePreferences().put(Activator.GROOVY_SCRIPT_FILTERS, Activator.DEFAULT_GROOVY_SCRIPT_FILTER);
        }
    }
}
