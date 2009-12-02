/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.groovy.tests.model;

import junit.framework.Test;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.core.tests.util.Util;



/**
 * @author Andrew Eisenberg
 * @created Dec 1, 2009
 *
 */
public class MoveRenameCopyTests extends BuilderTests {
    public MoveRenameCopyTests(String name) {
        super(name);
    }
	public static Test suite() {
		return buildTestSuite(MoveRenameCopyTests.class);
	}
	
	final static String GROOVY_CLASS_CONTENTS = "class Groovy {\n" +
			"Groovy() { }\n" +
			"Groovy(arg1) { }\n" +
			"}"; 
	
	final static String GROOVY_SCRIPT_CONTENTS = "def x = 9";
	
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
	
	public void testCopyGroovyClass() throws Exception {
	    GroovyCompilationUnit unit = createSimpleGroovyProject("foo", GROOVY_CLASS_CONTENTS);
	    unit.copy(unit.getParent(), unit, "GroovyNew.groovy", true, null);
	    GroovyCompilationUnit newUnit = (GroovyCompilationUnit) ((IPackageFragment) unit.getParent()).getCompilationUnit("GroovyNew.groovy");
	    
	    checkExist(unit);
	    checkExist(newUnit);
	}
	
    public void testMoveGroovyClass() throws Exception {
        GroovyCompilationUnit unit = createSimpleGroovyProject("foo.bar", GROOVY_CLASS_CONTENTS);
        IPackageFragment pack = unit.getPackageFragmentRoot().createPackageFragment("foo", true, null);
        unit.move(pack, null, "GroovyNew.groovy", true, null);
        GroovyCompilationUnit newUnit = (GroovyCompilationUnit) pack.getCompilationUnit("GroovyNew.groovy");
        
        checkNoExist(unit);
        checkExist(newUnit);
    }

	
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
	
    public void testCopyGroovyScript() throws Exception {
        GroovyCompilationUnit unit = createSimpleGroovyProject("foo", GROOVY_SCRIPT_CONTENTS);
        unit.copy(unit.getParent(), unit, "GroovyNew.groovy", true, null);
        GroovyCompilationUnit newUnit = (GroovyCompilationUnit) ((IPackageFragment) unit.getParent()).getCompilationUnit("GroovyNew.groovy");
        
        checkExist(unit);
        checkExist(newUnit);
    }
    
    public void testMoveGroovyScript() throws Exception {
        GroovyCompilationUnit unit = createSimpleGroovyProject("foo.bar", GROOVY_SCRIPT_CONTENTS);
        IPackageFragment pack = unit.getPackageFragmentRoot().createPackageFragment("foo", true, null);
        unit.move(pack, null, "GroovyNew.groovy", true, null);
        GroovyCompilationUnit newUnit = (GroovyCompilationUnit) pack.getCompilationUnit("GroovyNew.groovy");
        
        checkNoExist(unit);
        checkExist(newUnit);
    }

	
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
        IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
        env.addGroovyNature("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        expectingNoProblems();
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
        
        IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
        env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
        IPath path = env.addGroovyClass(root, "", "Groovy", contents);
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        return (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
    }

}
