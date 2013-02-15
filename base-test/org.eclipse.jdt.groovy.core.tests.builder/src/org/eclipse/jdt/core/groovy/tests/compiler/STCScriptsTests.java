/*******************************************************************************
 * Copyright (c) 2013 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.groovy.tests.compiler;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.groovy.tests.builder.ProjectUtils;
import org.eclipse.jdt.core.groovy.tests.compiler.ScriptFolderTests.MockScriptFolderSelector;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector.FileKind;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.CompilationUnit;

/**
 * Tests that the Static Type cheking DSL is working as expected
 * @author Andrew Eisenberg
 * @created Feb 14, 2012
 */
public class STCScriptsTests extends BuilderTests {
    
    class MockScriptFolderSelector extends ScriptFolderSelector {

        // override to make accessible here
        protected MockScriptFolderSelector(String preferences,
                boolean isDisabled) {
            super(toListOfString(preferences), isDisabled);
        }
    }
    static List<String> toListOfString(String preferences) {
        String[] splits = preferences.split(",");
        return Arrays.asList(splits);
    }
    
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
    
    protected IPath createGenericProject() throws Exception {
        if (genericProjectExists()) {
            return env.getProject("Project").getFullPath();
        }
        IPath projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyNature("Project");
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
        env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
        return projectPath;
    }
    protected boolean genericProjectExists() {
        return env.getProject("Project") != null && env.getProject("Project").exists();
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
        assertEquals("Groovy:[Static type checking] - Cannot find matching method Robot#move(java.lang.String). Please check if the declared type is right and if the method exists.",
                problems[0].getMessage());
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