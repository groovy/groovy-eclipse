 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.actions;

import org.codehaus.groovy.eclipse.actions.AddGroovyNatureAction;
import org.codehaus.groovy.eclipse.actions.RemoveGroovyNatureAction;
import org.codehaus.groovy.eclipse.core.builder.ConvertLegacyProject;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
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
		removeGroovyAction.doNotAskToRemoveJars();
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
		// groovy runtime added automatically for testProject
//        assertFalse("testProject should not have Groovy jars after running remove nature action", hasGroovyJars());

		addGroovyAction.run(null);
		
		assertTrue("testProject should have Groovy nature after testing action", hasGroovyNature());
        assertTrue("testProject should have Groovy jars after running remove nature action", hasGroovyJars());

		addGroovyAction.run(null);
		
		assertTrue("testProject should still have Groovy nature after testing action twice", hasGroovyNature());
        assertTrue("testProject should have Groovy jars after running remove nature action", hasGroovyJars());
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
        assertTrue("testProject should have Groovy jars after running add nature action", hasGroovyJars());
        
        removeGroovyAction.run(null);
        
        assertFalse("testProject should not have Groovy nature after running remove nature action", hasGroovyNature());
        assertFalse("testProject should not have Groovy jars after running remove nature action", hasGroovyJars());

        addGroovyAction.run(null);
        
        assertTrue("testProject should have Groovy nature after testing action", hasGroovyNature());
        assertTrue("testProject should have Groovy jars after running add nature action", hasGroovyJars());

        removeGroovyAction.run(null);
        
        assertFalse("testProject should not have Groovy nature after running remove nature action", hasGroovyNature());
        assertFalse("testProject should not have Groovy jars after running remove nature action", hasGroovyJars());
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

    /**
     * @return
     * @throws CoreException 
     */
    private boolean hasGroovyJars() throws CoreException {
        return GroovyRuntime.hasGroovyClasspathContainer(testProject.getJavaProject());
    }
}
