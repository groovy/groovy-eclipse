/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.test.actions;

import org.codehaus.groovy.eclipse.actions.AddGroovyNatureAction;
import org.codehaus.groovy.eclipse.actions.RemoveGroovyNatureAction;
import org.codehaus.groovy.eclipse.core.builder.ConvertLegacyProject;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

public class GroovyNatureActionTestCase extends EclipseTestCase {
	
	private AddGroovyNatureAction addGroovyAction; 
	private RemoveGroovyNatureAction removeGroovyAction;
	private ConvertLegacyProject convert;
	
	/* 
	 * @see org.codehaus.groovy.eclipse.test.EclipseTestCase#setUp()
	 */
	@Override
    public void setUp() throws Exception {
		super.setUp();
		GroovyRuntime.addGroovyRuntime(testProject.getProject());
		testProject.createGroovyTypeAndPackage(
				"pack1",
				"MainClass.groovy",
				"class MainClass { static void main(args){ println \"Hello Groovy World\" } } ");
		GroovyRuntime.removeGroovyNature(testProject.getProject());
		addGroovyAction = new AddGroovyNatureAction();
		removeGroovyAction = new RemoveGroovyNatureAction();
		convert = new ConvertLegacyProject();
	}
	
	/**
	 * Tests the action that adds GroovyNature to a java project.  Adds
	 * the nature twice to make sure nothing goes wrong with that.
	 * 
	 * @throws Exception
	 */
	public void testAddGroovyNature() throws Exception { 

		assertTrue("testProject must have the Java nature",
				testProject.getJavaProject().getProject().hasNature(JavaCore.NATURE_ID));
		
		IStructuredSelection selection = new StructuredSelection(new Object[] { testProject.getJavaProject() });

		addGroovyAction.selectionChanged(null, selection);
		
		assertFalse("testProject should not have Groovy nature before testing action", hasGroovyNature());
		
		addGroovyAction.run(null);
		
		assertTrue("testProject should have Groovy nature after testing action", hasGroovyNature());

		addGroovyAction.run(null);
		
		assertTrue("testProject should still have Groovy nature after testing action twice", hasGroovyNature());
		
		
		
	}
	
	/**
	 * This tests to show and assert that the GroovyNature will not be 
	 * added to a non java project.
	 * 
	 * @throws CoreException
	 */
	public void testGroovyNatureNotJavaProject() throws CoreException {
		
		testProject.removeNature(JavaCore.NATURE_ID);
		assertFalse(testProject.getJavaProject().getProject().hasNature(JavaCore.NATURE_ID));
		
		IStructuredSelection selection = new StructuredSelection(new Object[] { testProject.getProject() });
		addGroovyAction.selectionChanged(null, selection);

		assertFalse("testProject should not have Groovy nature before testing action", hasGroovyNature());

		try {
			addGroovyAction.run(null);
		} catch (Exception ex) {
			
		}

		assertFalse("testProject should not have Groovy nature after testing action", hasGroovyNature());
	}
	
	public void testRemoveGroovyNature() throws Exception {
        assertTrue("testProject must have the Java nature",
                testProject.getJavaProject().getProject().hasNature(JavaCore.NATURE_ID));
        
        IStructuredSelection selection = new StructuredSelection(new Object[] { testProject.getJavaProject() });

        addGroovyAction.selectionChanged(null, selection);
        removeGroovyAction.selectionChanged(null, selection);
        
        assertFalse("testProject should not have Groovy nature before testing action", hasGroovyNature());
        
        removeGroovyAction.run(null);
        
        assertFalse("testProject should not have Groovy nature after running remove nature action", hasGroovyNature());

        addGroovyAction.run(null);
        
        assertTrue("testProject should have Groovy nature after testing action", hasGroovyNature());

        removeGroovyAction.run(null);
        
        assertFalse("testProject should not have Groovy nature after running remove nature action", hasGroovyNature());
    }
	
	public void testConvertLegacyAction() throws Exception {
	    // can't add old nature since it doesn't exist
//	    testProject.addNature(ConvertLegacyProject.OLD_NATURE);
	    testProject.addBuilder(ConvertLegacyProject.OLD_BUILDER);
	    convert.convertProject(testProject.getProject());
        assertTrue("testProject should have Groovy nature after conversion", hasGroovyNature());
        assertFalse("testProject should not have OLD Groovy nature after conversion", hasOldGroovyNature());
        assertTrue("testProject should have Java builder after conversion", hasBuilder(JavaCore.BUILDER_ID));
        assertFalse("testProject should not have OLD Groovy builder after conversion", hasBuilder(ConvertLegacyProject.OLD_BUILDER));
    }
	
    protected boolean hasOldGroovyNature() throws CoreException {
        return testProject.getProject().hasNature(ConvertLegacyProject.OLD_NATURE);
    }
    protected boolean hasBuilder(String builderId) throws CoreException {
        IProjectDescription desc = testProject.getProject().getDescription();
        ICommand[] commands = desc.getBuildSpec();
        for (ICommand command : commands) {
            if (command.getBuilderName().equals(builderId)) {
                return true;
            }
        }
        return false;
    }

}
