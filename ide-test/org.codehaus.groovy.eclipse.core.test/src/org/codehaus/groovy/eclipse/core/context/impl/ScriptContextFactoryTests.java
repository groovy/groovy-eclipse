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
public class ScriptContextFactoryTests extends GroovyContextFactoryTests {
	@Override
    protected void setUp() throws Exception {
		super.setUp();
		setSourceCode("ScriptContextTestCode.groovy");
	}
	
	public void testInModule1() {
		checkContextCount(getName(), ModuleContext.class);
		checkContextPath(getName(), new String[] { MODULE, MODULE_SCOPE, CLASS, METHOD_SCOPE });
	}
	
	public void testInModule2() {
		checkContextCount(getName(), ModuleContext.class);
		checkContextPath(getName(), new String[] { MODULE, MODULE_SCOPE, CLASS, METHOD_SCOPE });
	}

	public void testInModuleBody() {
		checkContextCount(getName(), ModuleScopeContext.class);
		checkContextPath(getName(), new String[] { MODULE, MODULE_SCOPE, CLASS, METHOD_SCOPE });
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

	public void testInClosureBody1() {
		checkContextCount(getName(), ClosureScopeContext.class);
		checkContextPath(getName(), new String[] { MODULE, CLASS, METHOD_SCOPE, CLOSURE_SCOPE });
	}
	
	public void testInClosureBody2() {
		checkContextCount(getName(), ClosureScopeContext.class);
		checkContextPath(getName(), new String[] { MODULE, CLASS, METHOD_SCOPE, CLOSURE_SCOPE });
	}
}