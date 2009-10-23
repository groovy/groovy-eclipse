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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Variable;

/**
 * @author Andrew Eisenberg
 * @created Sep 25, 2009
 *          <p>
 *          This class maps variable names to types in a hierarchy
 *          </p>
 */
public class VariableScope {

	public static class VariableInfo {
		public final ClassNode type;
		public final ClassNode declaringType;

		public VariableInfo(ClassNode type, ClassNode declaringType) {
			super();
			this.type = type;
			this.declaringType = declaringType;
		}
	}

	/**
	 * Null for the top level scope
	 */
	private VariableScope parent;

	private ASTNode enclosingNode;

	private Map<String, VariableInfo> nameVariableMap = new HashMap<String, VariableInfo>();

	public VariableScope(VariableScope parent, ASTNode enclosingNode) {
		this.parent = parent;
		this.enclosingNode = enclosingNode;
	}

	/**
	 * Find the variable in the current environment,
	 * 
	 * @param name
	 * @return the variable info or null if not found
	 */
	public VariableInfo lookupName(String name) {
		if (name.equals("this")) {
			ClassNode declaringType = getEnclosingTypeDeclaration();
			return new VariableInfo(declaringType, declaringType);
		} else if (name.equals("super")) {
			ClassNode declaringType = getEnclosingTypeDeclaration();
			ClassNode superType = declaringType != null ? declaringType.getSuperClass() : null;
			return new VariableInfo(superType, superType);
		}

		VariableInfo var = nameVariableMap.get(name);
		if (name == null && parent != null) {
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
}
