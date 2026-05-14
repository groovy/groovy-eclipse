/*
 * Copyright 2009-2021 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.groovy.tests.builder.BuilderTestSuite;
import org.junit.Test;

public final class MoveRenameCopyTests extends BuilderTestSuite {

    //@formatter:off
    private static final String GROOVY_CLASS_CONTENTS =
        "class Groovy {\n" +
        "  Groovy() { }\n" +
        "  Groovy(arg1) { }\n" +
        "}";
    //@formatter:on

    private static final String GROOVY_SCRIPT_CONTENTS =
        "def x = 9";

    @Test
    public void testRenameGroovyClass() throws Exception {
        GroovyCompilationUnit unit = createSimpleGroovyProject("foo", GROOVY_CLASS_CONTENTS);
        unit.rename("GroovyNew.groovy", true, null);
        GroovyCompilationUnit newUnit = (GroovyCompilationUnit) ((IPackageFragment) unit.getParent()).getCompilationUnit("GroovyNew.groovy");

        checkNoExist(unit);
        checkExist(newUnit);
        newUnit.rename(unit.getElementName(), true, null);
        checkNoExist(newUnit);
        checkExist(unit);
    }

    @Test
    public void testCopyGroovyClass() throws Exception {
        GroovyCompilationUnit unit = createSimpleGroovyProject("foo", GROOVY_CLASS_CONTENTS);
        unit.copy(unit.getParent(), unit, "GroovyNew.groovy", true, null);
        GroovyCompilationUnit newUnit = (GroovyCompilationUnit) ((IPackageFragment) unit.getParent()).getCompilationUnit("GroovyNew.groovy");

        checkExist(unit);
        checkExist(newUnit);
    }

    @Test
    public void testMoveGroovyClass() throws Exception {
        GroovyCompilationUnit unit = createSimpleGroovyProject("foo.bar", GROOVY_CLASS_CONTENTS);
        IPackageFragment pack = unit.getPackageFragmentRoot().createPackageFragment("foo", true, null);
        unit.move(pack, null, "GroovyNew.groovy", true, null);
        GroovyCompilationUnit newUnit = (GroovyCompilationUnit) pack.getCompilationUnit("GroovyNew.groovy");

        checkNoExist(unit);
        checkExist(newUnit);
    }

    @Test
    public void testRenameGroovyScript() throws Exception {
        GroovyCompilationUnit unit = createSimpleGroovyProject("foo", GROOVY_SCRIPT_CONTENTS);
        unit.rename("GroovyNew.groovy", true, null);
        GroovyCompilationUnit newUnit = (GroovyCompilationUnit) ((IPackageFragment) unit.getParent()).getCompilationUnit("GroovyNew.groovy");

        checkNoExist(unit);
        checkExist(newUnit);
        newUnit.rename(unit.getElementName(), true, null);
        checkNoExist(newUnit);
        checkExist(unit);
    }

    @Test
    public void testCopyGroovyScript() throws Exception {
        GroovyCompilationUnit unit = createSimpleGroovyProject("foo", GROOVY_SCRIPT_CONTENTS);
        unit.copy(unit.getParent(), unit, "GroovyNew.groovy", true, null);
        GroovyCompilationUnit newUnit = (GroovyCompilationUnit) ((IPackageFragment) unit.getParent()).getCompilationUnit("GroovyNew.groovy");

        checkExist(unit);
        checkExist(newUnit);
    }

    @Test
    public void testMoveGroovyScript() throws Exception {
        GroovyCompilationUnit unit = createSimpleGroovyProject("foo.bar", GROOVY_SCRIPT_CONTENTS);
        IPackageFragment pack = unit.getPackageFragmentRoot().createPackageFragment("foo", true, null);
        unit.move(pack, null, "GroovyNew.groovy", true, null);
        GroovyCompilationUnit newUnit = (GroovyCompilationUnit) pack.getCompilationUnit("GroovyNew.groovy");

        checkNoExist(unit);
        checkExist(newUnit);
    }

    //--------------------------------------------------------------------------

    private void checkNoExist(GroovyCompilationUnit unit) {
        assertFalse("Compilation unit " + unit.getElementName() + " should not exist", unit.exists());
        assertFalse("Compilation unit " + unit.getElementName() + " should not be a working copy", unit.isWorkingCopy());
        assertFalse("File " + unit.getResource().getName() + " should not exist", unit.getResource().exists());
    }

    private void checkExist(GroovyCompilationUnit unit) {
        assertTrue("Compilation unit " + unit.getElementName() + " should exist", unit.exists());
        assertTrue("File " + unit.getResource().getName() + " should exist", unit.getResource().exists());
        env.fullBuild();
        expectingNoProblems();
        assertTrue(unit.getType(unit.getElementName().substring(0, unit.getElementName().length() - ".groovy".length())).exists());
    }

    private GroovyCompilationUnit createSimpleGroovyProject(String pack, String contents) throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        expectingNoProblems();

        IPath root = env.getPackageFragmentRootPath(projectPath, "src");
        IPath path = env.addGroovyClass(root, "Groovy", contents);
        return env.getUnit(path);
    }
}
