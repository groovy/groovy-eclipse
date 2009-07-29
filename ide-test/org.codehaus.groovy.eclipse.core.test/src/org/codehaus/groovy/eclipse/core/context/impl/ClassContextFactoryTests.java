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
package org.codehaus.groovy.eclipse.core.context.impl;

/**
 * Tests that the various contexts are created along with their ASTNode paths.
 * <p>
 * The test relies on a test file that has special comment tags. These are parsed from the test file before testing
 * begins.
 * 
 * @author empovazan
 */
public class ClassContextFactoryTests extends GroovyContextFactoryTests {
	@Override
    protected void setUp() throws Exception {
		super.setUp();
		setSourceCode("ContextTestCode.groovy");
	}
	
	public void testInModule1() {
		checkContextCount(getName(), ModuleContext.class);
		checkContextPath(getName(), new String[] { MODULE, MODULE_SCOPE });
	}
	
	public void testInModule2() {
		checkContextCount(getName(), ModuleContext.class);
		checkContextPath(getName(), new String[] { MODULE, CLASS, CLASS_SCOPE });
	}

	public void testInModuleBody() {
		checkContextCount(getName(), ModuleScopeContext.class);
		checkContextPath(getName(), new String[] { MODULE, MODULE_SCOPE });
	}

	public void testNotInModuleBody() {
		checkNoContext(getName(), ModuleScopeContext.class);
	}

	public void testInClass() {
		checkContextCount(getName(), ClassContext.class);
		checkContextPath(getName(), new String[] { MODULE, CLASS, CLASS_SCOPE });
	}

	public void testInClassBody1() {
		checkContextCount(getName(), ClassScopeContext.class);
		checkContextPath(getName(), new String[] { MODULE, CLASS, CLASS_SCOPE });
	}

	public void testInClassBody2() {
		checkContextCount(getName(), ClassScopeContext.class);
		checkContextPath(getName(), new String[] { MODULE, CLASS, CLASS_SCOPE });
	}
	
	public void testInClassBody3() {
		checkContextCount(getName(), ClassScopeContext.class);
		checkContextPath(getName(), new String[] { MODULE, CLASS, CLASS_SCOPE });
	}

	public void testNotInClass() {
		checkNoContext(getName(), ClassContext.class);
	}

	public void testInCtorParams() {
		checkContextCount(getName(), ConstructorParametersContext.class);
		checkContextPath(getName(), new String[] { MODULE, CLASS, CONSTRUCTOR_PARAMETERS });
	}

	public void testInCtor() {
		checkContextCount(getName(), ConstructorScopeContext.class);
		checkContextPath(getName(), new String[] { MODULE, CLASS, CONSTRUCTOR_SCOPE });
	}

	public void testNotInCtor() {
		checkNoContext(getName(), MethodScopeContext.class);
	}

	public void testInMethodParams() {
		checkContextCount(getName(), MethodParametersContext.class);
		checkContextPath(getName(), new String[] { MODULE, CLASS, METHOD_PARAMETERS });
	}
	
	public void testNotInMethodParams() {
		checkNoContext(getName(), MethodParametersContext.class);
	}

	public void testInMethodBody() {
		checkContextCount(getName(), MethodScopeContext.class);
		checkContextPath(getName(), new String[] { MODULE, CLASS, METHOD_SCOPE });
	}

	public void testNotInMethodBody() {
		checkNoContext(getName(), MethodScopeContext.class);
	}

	public void testInClosureBody1() {
		checkContextCount(getName(), ClosureScopeContext.class);
		checkContextPath(getName(), new String[] { MODULE, CLASS, CLOSURE_SCOPE });
	}
	
	public void testInClosureBody2() {
		checkContextCount(getName(), ClosureScopeContext.class);
		checkContextPath(getName(), new String[] { MODULE, CLASS, METHOD_SCOPE, CLOSURE_SCOPE });
	}
}