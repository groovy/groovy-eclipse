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

package org.eclipse.jdt.groovy.search;

import groovy.lang.GString;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

/**
 * @author Andrew Eisenberg
 * @created Sep 25, 2009
 *          <p>
 *          This class maps variable names to types in a hierarchy
 *          </p>
 */
public class VariableScope {

	public static final ClassNode DGM_CLASS_NODE = ClassHelper.makeCached(DefaultGroovyMethods.class);
	public static final ClassNode OBJECT_CLASS_NODE = ClassHelper.makeCached(Object.class);
	public static final ClassNode LIST_CLASS_NODE = ClassHelper.makeCached(List.class);
	public static final ClassNode VOID_CLASS_NODE = ClassHelper.makeCached(void.class);
	public static final ClassNode GSTRING_CLASS_NODE = ClassHelper.makeCached(GString.class);
	public static final ClassNode STRING_CLASS_NODE = ClassHelper.makeCached(String.class);
	public static final ClassNode PATTERN_CLASS_NODE = ClassHelper.makeCached(Pattern.class);
	public static final ClassNode MAP_CLASS_NODE = ClassHelper.makeCached(Map.class);
	public static final ClassNode NUMBER_CLASS_NODE = ClassHelper.makeCached(Number.class);

	// don't cache because we have to add properties
	public static final ClassNode CLASS_CLASS_NODE = ClassHelper.makeWithoutCaching(Class.class);
	static {
		initializeProperties(CLASS_CLASS_NODE);
	}

	// primitive wrapper classes
	public static final ClassNode INTEGER_CLASS_NODE = ClassHelper.makeCached(Integer.class);
	public static final ClassNode LONG_CLASS_NODE = ClassHelper.makeCached(Long.class);
	public static final ClassNode SHORT_CLASS_NODE = ClassHelper.makeCached(Short.class);
	public static final ClassNode FLOAT_CLASS_NODE = ClassHelper.makeCached(Float.class);
	public static final ClassNode DOUBLE_CLASS_NODE = ClassHelper.makeCached(Double.class);
	public static final ClassNode BYTE_CLASS_NODE = ClassHelper.makeCached(Byte.class);
	public static final ClassNode BOOLEAN_CLASS_NODE = ClassHelper.makeCached(Boolean.class);
	public static final ClassNode CHARACTER_CLASS_NODE = ClassHelper.makeCached(Character.class);

	public static class VariableInfo {
		public final ClassNode type;
		public final ClassNode declaringType;

		public VariableInfo(ClassNode type, ClassNode declaringType) {
			super();
			this.type = type;
			this.declaringType = declaringType;
		}
	}

	public static ClassNode NO_CATEGORY = null;

	/**
	 * Null for the top level scope
	 */
	private VariableScope parent;

	private ASTNode enclosingNode;

	private Map<String, VariableInfo> nameVariableMap = new HashMap<String, VariableInfo>();

	/**
	 * Category that will be declared in the next scope
	 */
	private ClassNode categoryBeingDeclared;

	public VariableScope(VariableScope parent, ASTNode enclosingNode) {
		this.parent = parent;
		this.enclosingNode = enclosingNode;
	}

	/**
	 * The name of all categories in scope.
	 * 
	 * @return
	 */
	public Set<ClassNode> getCategoryNames() {
		if (parent != null) {
			Set<ClassNode> categories = parent.getCategoryNames();
			// don't look at this scope's category, but the parent scope's
			// category. This is because although current scope knows that it
			// is a category scope, the category type is only available from parent
			// scope
			if (parent.isCategoryBeingDeclared()) {
				categories.add(parent.categoryBeingDeclared);
			}
			return categories;
		} else {
			Set<ClassNode> categories = new HashSet<ClassNode>();
			categories.add(DGM_CLASS_NODE);
			return categories;
		}
	}

	/**
	 * @return
	 */
	private boolean isCategoryBeingDeclared() {
		return categoryBeingDeclared != null;
	}

	public void setCategoryBeingDeclared(ClassNode categoryBeingDeclared) {
		this.categoryBeingDeclared = categoryBeingDeclared;
	}

	/**
	 * Find the variable in the current environment,
	 * 
	 * @param name
	 * @return the variable info or null if not found
	 */
	public VariableInfo lookupName(String name) {
		if ("this".equals(name)) { //$NON-NLS-1$
			ClassNode declaringType = getEnclosingTypeDeclaration();
			return new VariableInfo(declaringType, declaringType);
		} else if ("super".equals(name)) { //$NON-NLS-1$
			ClassNode declaringType = getEnclosingTypeDeclaration();
			ClassNode superType = declaringType != null ? declaringType.getSuperClass() : null;
			return new VariableInfo(superType, superType);
		}

		VariableInfo var = nameVariableMap.get(name);
		if (var == null && parent != null) {
			var = parent.lookupName(name);
		}
		return var;
	}

	public boolean isThisOrSuper(Variable var) {
		return var.getName().equals("this") || var.getName().equals("super");
	}

	public void addVariable(String name, ClassNode type, ClassNode declaringType) {
		nameVariableMap.put(name, new VariableInfo(type, declaringType != null ? declaringType : OBJECT_CLASS_NODE));
	}

	public void addVariable(Variable var) {
		addVariable(var.getName(), var.getType(), var.getOriginType());
	}

	public ClassNode getEnclosingTypeDeclaration() {
		if (enclosingNode instanceof ClassNode) {
			return (ClassNode) enclosingNode;
		} else if (parent != null) {
			return parent.getEnclosingTypeDeclaration();
		} else {
			return null;
		}
	}

	public FieldNode getEnclosingFieldDeclaration() {
		if (enclosingNode instanceof FieldNode) {
			return (FieldNode) enclosingNode;
		} else if (parent != null) {
			return parent.getEnclosingFieldDeclaration();
		} else {
			return null;
		}
	}

	public MethodNode getEnclosingMethodDeclaration() {
		if (enclosingNode instanceof FieldNode) {
			return (MethodNode) enclosingNode;
		} else if (parent != null) {
			return parent.getEnclosingMethodDeclaration();
		} else {
			return null;
		}
	}

	public static ClassNode maybeConvertFromPrimitive(ClassNode type) {
		if (ClassHelper.isPrimitiveType(type)) {
			return ClassHelper.getWrapper(type);
		}
		return type;
	}

	private static PropertyNode createPropertyNodeForMethodNode(MethodNode methodNode) {
		ClassNode propertyType = methodNode.getReturnType();
		String methodName = methodNode.getName();
		StringBuffer propertyName = new StringBuffer();
		propertyName.append(Character.toLowerCase(methodName.charAt(3)));
		if (methodName.length() > 4) {
			propertyName.append(methodName.substring(4));
		}
		int mods = methodNode.getModifiers();
		return new PropertyNode(propertyName.toString(), mods, propertyType, methodNode.getDeclaringClass(), null, null, null);
	}

	/**
	 * @return true if the methodNode looks like a getter method for a property: method starting get<Something> with a non void
	 *         return type and taking no parameters
	 */
	private static boolean isGetter(MethodNode methodNode) {
		return methodNode.getReturnType() != VOID_CLASS_NODE && methodNode.getParameters().length == 0
				&& methodNode.getName().startsWith("get") && methodNode.getName().length() > 3; //$NON-NLS-1$
	}

	private static void initializeProperties(ClassNode node) {
		// getX methods
		for (MethodNode methodNode : node.getMethods()) {
			if (isGetter(methodNode)) {
				node.addProperty(createPropertyNodeForMethodNode(methodNode));
			}
		}
	}

}
