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