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
package org.eclipse.jdt.core.groovy.tests.builder;

import java.util.Arrays;

import junit.framework.Test;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.groovy.core.Activator;

/**
 * Tests that the Static Type cheking DSL is working as expected.
 */
public final class STCScriptsTests extends BuilderTests {

    public STCScriptsTests(String name) {
        super(name);
    }

    public static Test suite() {
        return buildTestSuite(STCScriptsTests.class);
    }

    private boolean origEnabled;
    private String origPatterns;

    @Override
    protected void setUp() throws Exception {
        try {
            super.setUp();
        } finally {
            origEnabled = Activator.getDefault().getBooleanPreference(null, Activator.GROOVY_SCRIPT_FILTERS_ENABLED, false);
            origPatterns = Activator.getDefault().getStringPreference(null, Activator.GROOVY_SCRIPT_FILTERS, Activator.DEFAULT_GROOVY_SCRIPT_FILTER);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            Activator.getDefault().setPreference(null, Activator.GROOVY_SCRIPT_FILTERS_ENABLED, String.valueOf(origEnabled));
            Activator.getDefault().setPreference(null, Activator.GROOVY_SCRIPT_FILTERS, origPatterns);
        }
    }

    private IPath createGenericProject() throws Exception {
        if (genericProjectExists()) {
            return env.getProject("Project").getFullPath();
        }
        IPath projectPath = env.addProject("Project", "1.5");
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, "");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyNature("Project");
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");
        return projectPath;
    }

    private boolean genericProjectExists() {
        return env.getProject("Project") != null && env.getProject("Project").exists();
    }

    //--------------------------------------------------------------------------


    public void testStaticTypeCheckingDSL1() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 21) {
            return;
        }

        Activator.getDefault().setPreference(null, Activator.GROOVY_SCRIPT_FILTERS_ENABLED, Boolean.TRUE.toString());
        Activator.getDefault().setPreference(null, Activator.GROOVY_SCRIPT_FILTERS, "src/*Move.groovy,y");

        IPath projPath = createGenericProject();
        env.addGroovyClass(projPath.append("src"), "", "RobotMove",
                "import org.codehaus.groovy.ast.expr.VariableExpression\n" +
                "unresolvedVariable { VariableExpression var ->\n" +
                "    if ('robot' == var.name) {\n" +
                "        def robotClass = context.source.AST.classes.find { it.name == 'Robot' }\n" +
                "        storeType(var, robotClass)\n" +
                "        handled = true\n" +
                "    }\n" +
                "}");
        env.addGroovyClass(projPath.append("src"), "", "Robot",
                "@groovy.transform.TypeChecked(extensions = 'RobotMove.groovy')\n" +
                "void operate() {\n" +
                "    robot.move \"left\"\n" +
                "}");

        env.fullBuild(projPath);
        Problem[] problems = env.getProblemsFor(projPath);
        assertEquals("Should have found one problem in:\n" + Arrays.toString(problems), 1, problems.length);
        assertEquals("Groovy:[Static type checking] - Cannot find matching method Robot#move(java.lang.String). Please check if the declared type is right and if the method exists.", problems[0].getMessage());
    }

    public void testStaticTypeCheckingDSL2() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 21) {
            return;
        }

        Activator.getDefault().setPreference(null, Activator.GROOVY_SCRIPT_FILTERS_ENABLED, Boolean.TRUE.toString());
        Activator.getDefault().setPreference(null, Activator.GROOVY_SCRIPT_FILTERS, "src/*Move.groovy,y");

        IPath projPath = createGenericProject();
        env.addGroovyClass(projPath.append("src"), "", "RobotMove",
                "import org.codehaus.groovy.ast.expr.VariableExpression\n" +
                "unresolvedVariable { VariableExpression var ->\n" +
                "    if ('robot' == var.name) {\n" +
                "        def robotClass = context.source.AST.classes.find { it.name == 'Robot' }\n" +
                "        storeType(var, robotClass)\n" +
                "        handled = true\n" +
                "    }\n" +
                "}");
        env.addGroovyClass(projPath.append("src"), "", "RobotScript",
                "import groovy.transform.TypeChecked\n" +
                "class Robot {\n" +
                "    void move(String dist) { println \"Moved $dist\" }\n" +
                "}\n" +
                "robot = new Robot()\n" +
                "@TypeChecked(extensions = 'RobotMove.groovy')\n" +
                "void operate() {\n" +
                "    robot.move \"left\"\n" +
                "}");

        env.fullBuild(projPath);
        Problem[] problems = env.getProblemsFor(projPath);
        assertEquals("Should have found no problems in:\n" + Arrays.toString(problems), 0, problems.length);
    }
}
