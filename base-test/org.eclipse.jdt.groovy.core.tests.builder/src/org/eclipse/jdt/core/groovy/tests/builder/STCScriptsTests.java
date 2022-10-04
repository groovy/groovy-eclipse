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
package org.eclipse.jdt.core.groovy.tests.builder;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.util.Arrays;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.groovy.core.Activator;
import org.junit.After;
import org.junit.Test;

/**
 * Tests that the Static Type cheking DSL is working as expected.
 */
public final class STCScriptsTests extends BuilderTestSuite {

    @After
    public void tearDown() throws Exception {
        Activator.getInstancePreferences().putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, Activator.DEFAULT_SCRIPT_FILTERS_ENABLED);
        Activator.getInstancePreferences().put(Activator.GROOVY_SCRIPT_FILTERS, Activator.DEFAULT_GROOVY_SCRIPT_FILTER);
    }

    private IPath createGenericProject() throws Exception {
        if (genericProjectExists()) {
            return env.getProject("Project").getFullPath();
        }
        IPath projectPath = env.addProject("Project");
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        return projectPath;
    }

    private boolean genericProjectExists() {
        return env.getProject("Project") != null && env.getProject("Project").exists();
    }

    //--------------------------------------------------------------------------

    @Test
    public void testStaticTypeCheckingDSL1() throws Exception {
        Activator.getInstancePreferences().putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, true);
        Activator.getInstancePreferences().put(Activator.GROOVY_SCRIPT_FILTERS, "src/*Move.groovy,y");

        IPath projPath = createGenericProject();
        //@formatter:off
        env.addGroovyClass(projPath.append("src"), "RobotMove",
            "unresolvedVariable { VariableExpression vexp ->\n" +
            "  if ('robot' == vexp.name) {\n" +
            "    def robotClass = lookupClassNodeFor('Robot')\n" +
            "    storeType(vexp, robotClass)\n" +
            "    handled = true\n" +
            "  }\n" +
            "}\n");
        env.addGroovyClass(projPath.append("src"), "Robot",
            "@groovy.transform.TypeChecked(extensions='RobotMove.groovy')\n" +
            "void operate() {\n" +
            "  robot.move \"left\"\n" +
            "}\n");
        //@formatter:on

        env.fullBuild(projPath);
        Problem[] problems = env.getProblemsFor(projPath);
        assertEquals("Should have found one problem in:\n" + Arrays.toString(problems), 1, problems.length);
        assertEquals("Groovy:[Static type checking] - Cannot find matching method Robot#move(java.lang.String)." +
            " Please check if the declared type is correct and if the method exists.", problems[0].getMessage());
    }

    @Test
    public void testStaticTypeCheckingDSL2() throws Exception {
        Activator.getInstancePreferences().putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, true);
        Activator.getInstancePreferences().put(Activator.GROOVY_SCRIPT_FILTERS, "src/*Move.groovy,y");

        IPath projPath = createGenericProject();
        //@formatter:off
        env.addGroovyClass(projPath.append("src"), "RobotMove",
            "unresolvedVariable { VariableExpression vexp ->\n" +
            "  if ('robot' == vexp.name) {\n" +
            "    def robotClass = lookupClassNodeFor('Robot')\n" +
            "    storeType(vexp, robotClass)\n" +
            "    handled = true\n" +
            "  }\n" +
            "}\n");
        env.addGroovyClass(projPath.append("src"), "RobotScript",
            "class Robot {\n" +
            "  void move(String dist) { println \"Moved $dist\" }\n" +
            "}\n" +
            "robot = new Robot()\n" +
            "@groovy.transform.TypeChecked(extensions='RobotMove.groovy')\n" +
            "void operate() {\n" +
            "  robot.move \"left\"\n" +
            "}\n");
        //@formatter:on

        env.fullBuild(projPath);
        Problem[] problems = env.getProblemsFor(projPath);
        assertEquals("Should have found no problems in:\n" + Arrays.toString(problems), 0, problems.length);
    }

    @Test // GROOVY-6328
    public void testStaticTypeCheckingDSL3() throws Exception { assumeTrue(isAtLeastGroovy(40));
        Activator.getInstancePreferences().putBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, true);
        Activator.getInstancePreferences().put(Activator.GROOVY_SCRIPT_FILTERS, "src/*Checker.groovy,y");

        IPath projPath = createGenericProject();
        //@formatter:off
        env.addGroovyClass(projPath.append("src"), "TypeChecker",
            "onMethodSelection { expr, node ->\n" +
            "  if (node.name == 'setS') {\n" +
            "    def closure = enclosingClosure.closureExpression\n" +
            "    def setting = closure.code.statements[0].expression\n" +
            "    setting.putNodeMetaData('notified', true)\n" +
            "  }\n" +
            "}\n");
        //@formatter:on

        for (String methods : new String[] {"", "void setS(String s) {this.s = s}"}) {
            env.addGroovyClass(projPath.append("src"), "TestScript",
                "import static org.codehaus.groovy.control.CompilePhase.INSTRUCTION_SELECTION\n" +
                "class C {\n" +
                "  String s\n" +
                "  " + methods + "\n" +
                "}\n" +
                "C make(@DelegatesTo(value=C.class, strategy=Closure.DELEGATE_FIRST) Closure closure) {\n" +
                "  new C().tap(closure)\n" +
                "}\n" +
                "@groovy.transform.ASTTest(phase=INSTRUCTION_SELECTION, value={\n" +
                "  def assignment = lookup.call('here')[0].expression\n" +
                "  assert assignment.getNodeMetaData('notified')\n" +
                "})\n" +
                "@groovy.transform.TypeChecked(extensions='TypeChecker.groovy')\n" +
                "void test() {\n" +
                "  def c = make {\n" +
                "  here: s = 'foo'\n" + // expecting onMethodSelection for setter
                "  }\n" +
                "}\n");
            //@formatter:on

            env.fullBuild(projPath);
            Problem[] problems = env.getProblemsFor(projPath);
            assertEquals("Should have found no problems in:\n" + Arrays.toString(problems), 0, problems.length);
        }
    }
}
