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
import org.codehaus.groovy.ast.ClassNode;
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

	public static final ClassNode DGM_CLASS_NODE = new ClassNode(DefaultGroovyMethods.class);
	public static final ClassNode OBJECT_CLASS_NODE = new ClassNode(Object.class);
	public static final ClassNode LIST_CLASS_NODE = new ClassNode(List.class);
	public static final ClassNode VOID_CLASS_NODE = new ClassNode(Void.class);
	public static final ClassNode GSTRING_CLASS_NODE = new ClassNode(GString.class);
	public static final ClassNode STRING_CLASS_NODE = new ClassNode(String.class);
	public static final ClassNode PATTERN_CLASS_NODE = new ClassNode(Pattern.class);
	public static final ClassNode MAP_CLASS_NODE = new ClassNode(Map.class);
	public static final ClassNode NUMBER_CLASS_NODE = new ClassNode(Number.class);

	public static final ClassNode INTEGER_CLASS_NODE = new ClassNode(Integer.class);
	public static final ClassNode LONG_CLASS_NODE = new ClassNode(Long.class);
	public static final ClassNode SHORT_CLASS_NODE = new ClassNode(Short.class);
	public static final ClassNode FLOAT_CLASS_NODE = new ClassNode(Float.class);
	public static final ClassNode DOUBLE_CLASS_NODE = new ClassNode(Double.class);
	public static final ClassNode BYTE_CLASS_NODE = new ClassNode(Byte.class);
	public static final ClassNode BOOLEAN_CLASS_NODE = new ClassNode(Boolean.class);
	public static final ClassNode CHARACTER_CLASS_NODE = new ClassNode(Character.class);

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

	public void addVariable(String name, ClassNode type, ClassNode declaringType) {
		nameVariableMap.put(name, new VariableInfo(type, declaringType));
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

	@SuppressWarnings("nls")
	public static ClassNode maybeConvertFromPrimitive(ClassNode type) {
		if (Character.isUpperCase(type.getNameWithoutPackage().charAt(0)) || type.getName().contains(".")) {
			return type;
		}
		if (type.getName().equals("int")) {
			return VariableScope.INTEGER_CLASS_NODE;
		} else if (type.getName().equals("boolean")) {
			return VariableScope.BOOLEAN_CLASS_NODE;
		} else if (type.getName().equals("byte")) {
			return VariableScope.BYTE_CLASS_NODE;
		} else if (type.getName().equals("double")) {
			return VariableScope.DOUBLE_CLASS_NODE;
		} else if (type.getName().equals("float")) {
			return VariableScope.FLOAT_CLASS_NODE;
		} else if (type.getName().equals("char")) {
			return VariableScope.CHARACTER_CLASS_NODE;
		} else if (type.getName().equals("short")) {
			return VariableScope.SHORT_CLASS_NODE;
		} else if (type.getName().equals("long")) {
			return VariableScope.LONG_CLASS_NODE;
		}
		return type;
	}

}
