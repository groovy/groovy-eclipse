/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.builder.ReferenceCollection;
import org.eclipse.jdt.internal.core.builder.State;
import org.eclipse.jdt.internal.core.builder.StringSet;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FriendDependencyTests extends BuilderTests {

	public FriendDependencyTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(FriendDependencyTests.class);
	}
// this is a compilation only test to verify that this method still exists since API Tooling is using it
public void testIncludes() {
	try {
		State state = (State) JavaModelManager.getJavaModelManager().getLastBuiltState(null, null);
		Map<String, ReferenceCollection> references = state.getReferences();
		ReferenceCollection r = references.values().iterator().next();
		char[][][] qualifiedNames = null;
		char[][] simpleNames = null;
		char[][] rootNames = null;
		r.includes(qualifiedNames, simpleNames, rootNames);
	} catch (NullPointerException e) {
		// expected
	}
}

// this is a compilation only test to verify that this method still exists since API Tooling is using it
public void testInternSimpleNames() {
	ReferenceCollection.internSimpleNames(new StringSet(1), true);

	try {
		String className = "org.eclipse.jdt.internal.core.builder.ReferenceCollection";
		Class clazz = Class.forName(className);
		//org.eclipse.jdt.internal.core.JavaModelManager.getLastBuiltState(IProject, IProgressMonitor)
		Class[] arguments = new Class[2];
		String argumentClassName = "org.eclipse.jdt.internal.core.builder.StringSet";
		arguments[0] = Class.forName(argumentClassName);
		arguments[1] = Boolean.TYPE;
		clazz.getDeclaredMethod("internSimpleNames", arguments);
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (SecurityException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (NoSuchMethodException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	}
}

//this is a compilation only test to verify that this method still exists since API Tooling is using it
public void testInternQualifiedNames() {
	ReferenceCollection.internQualifiedNames(new StringSet(1));

	try {
		String className = "org.eclipse.jdt.internal.core.builder.ReferenceCollection";
		Class clazz = Class.forName(className);
		//org.eclipse.jdt.internal.core.JavaModelManager.getLastBuiltState(IProject, IProgressMonitor)
		Class[] arguments = new Class[1];
		String argumentClassName = "org.eclipse.jdt.internal.core.builder.StringSet";
		arguments[0] = Class.forName(argumentClassName);
		clazz.getDeclaredMethod("internQualifiedNames", arguments);
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (SecurityException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (NoSuchMethodException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	}
}
//this is a compilation only test to verify that this method still exists since API Tooling is using it
public void testGetReferences() {
	try {
		State state = (State) JavaModelManager.getJavaModelManager().getLastBuiltState(null, null);
		state.getReferences();
	} catch (NullPointerException e) {
		// expected
	}
	try {
		String className = "org.eclipse.jdt.internal.core.builder.State";
		Class clazz = Class.forName(className);
		clazz.getDeclaredMethod("getReferences", new Class[0]);
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (SecurityException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (NoSuchMethodException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	}
}
//this is a compilation only test to verify that this method still exists since API Tooling is using it
public void testStringSetAdd() {
	StringSet s = new StringSet(3);
	s.add("");

	try {
		String className = "org.eclipse.jdt.internal.core.builder.StringSet";
		Class clazz = Class.forName(className);
		Class[] arguments = new Class[1];
		String argumentClassName = "java.lang.String";
		arguments[0] = Class.forName(argumentClassName);
		clazz.getDeclaredMethod("add", arguments);
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (SecurityException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (NoSuchMethodException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	}
}
//this is a compilation only test to verify that this method still exists since API Tooling is using it
public void testStringSetclear() {
	StringSet s = new StringSet(3);
	s.clear();

	try {
		String className = "org.eclipse.jdt.internal.core.builder.StringSet";
		Class clazz = Class.forName(className);
		clazz.getDeclaredMethod("clear", new Class[0]);
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (SecurityException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (NoSuchMethodException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	}
}
//this is a compilation only test to verify that this method still exists since API Tooling is using it
public void testStringSetNew() {
	new StringSet(3);

	try {
		String className = "org.eclipse.jdt.internal.core.builder.StringSet";
		Class clazz = Class.forName(className);
		Class[] arguments = new Class[1];
		arguments[0] = Integer.TYPE;
		clazz.getDeclaredConstructor(arguments);
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (SecurityException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (NoSuchMethodException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	}
}
//this is a compilation only test to verify that this field still exists since API Tooling is using it
public void testStringSetElementSize() {
	StringSet s = new StringSet(3);
	assertEquals("Not expected", 0, s.elementSize);

	try {
		String className = "org.eclipse.jdt.internal.core.builder.StringSet";
		Class clazz = Class.forName(className);
		clazz.getDeclaredField("elementSize");
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (SecurityException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (NoSuchFieldException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	}
}
//this is a compilation only test to verify that this field still exists since API Tooling is using it
public void testSimpleLookupTableKeyTable() {
	SimpleLookupTable t = new SimpleLookupTable(3);
	assertNotNull("Null", t.keyTable);

	try {
		String className = "org.eclipse.jdt.internal.compiler.util.SimpleLookupTable";
		Class clazz = Class.forName(className);
		clazz.getDeclaredField("keyTable");
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (SecurityException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (NoSuchFieldException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	}
}
//this is a compilation only test to verify that this field still exists since API Tooling is using it
public void testSimpleLookupTableValueTable() {
	SimpleLookupTable t = new SimpleLookupTable(3);
	assertNotNull("Null", t.valueTable);

	try {
		String className = "org.eclipse.jdt.internal.compiler.util.SimpleLookupTable";
		Class clazz = Class.forName(className);
		clazz.getDeclaredField("valueTable");
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (SecurityException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (NoSuchFieldException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	}
}
//this is a compilation only test to verify that this method still exists since API Tooling is using it
public void testJavaModelManagerGetJavaModelManager() {
	JavaModelManager.getJavaModelManager();
	try {
		String className = "org.eclipse.jdt.internal.core.JavaModelManager";
		Class clazz = Class.forName(className);
		clazz.getDeclaredMethod("getJavaModelManager", new Class[0]);
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (SecurityException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (NoSuchMethodException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	}
}
//this is a compilation only test to verify that this method still exists since API Tooling is using it
public void testJavaModelManagerGetLastBuiltState() {
	try {
		JavaModelManager.getJavaModelManager().getLastBuiltState(null, null);
	} catch (NullPointerException e) {
		// expected
	}
	try {
		String className = "org.eclipse.jdt.internal.core.JavaModelManager";
		Class clazz = Class.forName(className);
		//org.eclipse.jdt.internal.core.JavaModelManager.getLastBuiltState(IProject, IProgressMonitor)
		Class[] arguments = new Class[2];
		String argumentClassName = "org.eclipse.core.resources.IProject";
		String argumentClassName2 = "org.eclipse.core.runtime.IProgressMonitor";
		arguments[0] = Class.forName(argumentClassName);
		arguments[1] = Class.forName(argumentClassName2);
		clazz.getDeclaredMethod("getLastBuiltState", arguments);
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (SecurityException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (NoSuchMethodException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
		assertTrue("Should be there", false);
	}
}
}
