/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Michael Klenk and others        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package tests;

import java.io.File;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyChange;
import org.codehaus.groovy.eclipse.refactoring.core.hierarchy.HierarchyNode;
import org.codehaus.groovy.eclipse.refactoring.core.hierarchy.HierarchyTreeBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import core.FilePathHelper;

public class HierarchyTreeTestCase extends TestCase {
	
	private HierarchyTreeBuilder tree;

	@Override
    protected void setUp(){
		try {
			super.setUp();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		TestHelper t = new TestHelper("Hierarchy_Test_Tree01.txt",new File(FilePathHelper.getPathToTestFiles() + "hierarchy/Hierarchy_Test_Tree01.txt"));
		tree = new HierarchyTreeBuilder(t.getFileProvider());
	}
	
	public void testTree() {
		
		HierarchyNode A = tree.getNode("A");
		HierarchyNode AA = tree.getNode("AA");
		HierarchyNode AB = tree.getNode("AB");
		HierarchyNode ABB = tree.getNode("ABB");
		
		assertEquals("Class AB should extend class A",A, AB.getExtendingClass());
		assertEquals("Class AA should extend class A",A, AA.getExtendingClass());
		assertEquals("Class ABB should extend class AB",AB, ABB.getExtendingClass());	
	}

	public void testLinking() {
		Map<String, HierarchyNode> result = tree.getInterconnectedClasses(new ClassNode("AB",0,null));
		assertNotNull("Class A is not connected with Class AB!", result.get("A"));
		assertNotNull("Class AA is not connected with Class AB!", result.get("AA"));
		assertNotNull("Class ABB is not connected with Class AB!", result.get("ABB"));
		assertNotNull("Class D is not connected with Class AB!", result.get("D"));
		assertNotNull("Class DD is not connected with Class AB!", result.get("DD"));
		assertNotNull("Class DE is not connected with Class AB!", result.get("DE"));
		assertNotNull("Class IR is not connected with Class AB!", result.get("IR"));
		assertNotNull("Class IRR is not connected with Class AB!", result.get("IRR"));
		assertNotNull("Class IRN is not connected with Class AB!", result.get("IRN"));

		assertNull("Class C is connected with Class AB!", result.get("C"));
	}
	
	private class TestHelper extends MultiFileTestCase {

		public TestHelper(String name, File fileToTest) {
			super(name, fileToTest);
		}

		@Override
		public RefactoringStatus checkFinalCondition()
				throws OperationCanceledException, CoreException {
			return null;
		}

		@Override
		public RefactoringStatus checkInitialCondition()
				throws OperationCanceledException, CoreException {
			return null;
		}

		@Override
		public GroovyChange createChange() throws OperationCanceledException,
				CoreException {
			return null;
		}

		@Override
		public void preAction() {
			
		}

		@Override
		public void simulateUserInput() {
			
		}
		
	}
}
