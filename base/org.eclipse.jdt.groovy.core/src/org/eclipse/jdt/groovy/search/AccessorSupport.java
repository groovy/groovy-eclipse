/*
 * Copyright 2011 the original author or authors.
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

import java.util.List;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;

/**
 * Determined what kind of accessor a method name may be and then does further processing on a method node if the name matches
 * 
 * @author andrew
 * @created Jan 23, 2012
 */
public enum AccessorSupport {
	GETTER("get"), SETTER("set"), ISSER("is"), NONE("");

	private final String prefix;

	private AccessorSupport(String prefix) {
		this.prefix = prefix;
	}

	public boolean isAccessor() {
		return this != NONE;
	}

	public boolean isAccessorKind(MethodNode node, boolean isCategory) {
		int args = isCategory ? 1 : 0;
		ClassNode returnType = node.getReturnType();
		switch (this) {
			case GETTER:
				return (node.getParameters() == null || node.getParameters().length == args)
						&& !returnType.equals(VariableScope.VOID_CLASS_NODE);
			case ISSER:
				return !isCategory
						&& (node.getParameters() == null || node.getParameters().length == args)
						&& (returnType.equals(VariableScope.OBJECT_CLASS_NODE)
								|| returnType.equals(VariableScope.BOOLEAN_CLASS_NODE) || returnType
									.equals(ClassHelper.boolean_TYPE));
			case SETTER:
				return node.getParameters() != null && node.getParameters().length == args + 1
						&& (returnType.equals(VariableScope.VOID_CLASS_NODE) || returnType.equals(VariableScope.OBJECT_CLASS_NODE));
			case NONE:
			default:
				return false;
		}
	}

	public String createAccessorName(String name) {
		if (!name.startsWith(GETTER.prefix) && !name.startsWith(SETTER.prefix) && name.length() > 0) {
			return this.prefix + Character.toUpperCase(name.charAt(0)) + (name.length() > 1 ? name.substring(1) : "");
		} else {
			return null;
		}
	}

	public static AccessorSupport findAccessorKind(MethodNode node, boolean isCategory) {
		AccessorSupport accessor = create(node.getName(), isCategory);
		return accessor.isAccessorKind(node, isCategory) ? accessor : NONE;
	}

	/**
	 * If maybeProperty is a property variant of a method in declaringType, then return that method
	 * 
	 */
	public static MethodNode findAccessorMethodForPropertyName(String name, ClassNode declaringType, boolean isCategory) {
		if (name.length() <= 0) {
			return null;
		}

		String suffix = Character.toUpperCase(name.charAt(0)) + (name.length() > 1 ? name.substring(1) : "");
		String getterName = "get" + suffix;
		List<MethodNode> methods = declaringType.getMethods(getterName);
		if (!methods.isEmpty()) {
			MethodNode maybeMethod = methods.get(0);
			if (findAccessorKind(maybeMethod, isCategory) == GETTER) {
				return maybeMethod;
			}
		}
		String setterName = "set" + suffix;
		methods = declaringType.getMethods(setterName);
		if (!methods.isEmpty()) {
			MethodNode maybeMethod = methods.get(0);
			if (findAccessorKind(maybeMethod, isCategory) == SETTER) {
				return maybeMethod;
			}
		}
		String isserName = "is" + suffix;
		methods = declaringType.getMethods(isserName);
		if (!methods.isEmpty()) {
			MethodNode maybeMethod = methods.get(0);
			if (findAccessorKind(maybeMethod, isCategory) == ISSER) {
				return maybeMethod;
			}
		}

		return null;
	}

	/**
	 * @return true if the methodNode looks like a getter method for a property: method starting get<Something> with a non void
	 *         return type and taking no parameters
	 */
	public static boolean isGetter(MethodNode node) {
		return node.getReturnType() != VariableScope.VOID_CLASS_NODE
				&& node.getParameters().length == 0
				&& ((node.getName().startsWith("get") && node.getName().length() > 3) || (node.getName().startsWith("is") && node
						.getName().length() > 2));
	}

	/**
	 * @param methodName
	 * @return
	 */
	public static AccessorSupport create(String methodName, boolean isCategory) {
		AccessorSupport accessor = AccessorSupport.NONE;
		// is is allowed only for non-category methods
		if (!isCategory && methodName.length() > 2 && methodName.startsWith("is") && Character.isUpperCase(methodName.charAt(2))) {
			accessor = AccessorSupport.ISSER;
		}

		if (!accessor.isAccessor()) {
			if (methodName.length() > 3 && (methodName.startsWith("get") || methodName.startsWith("set"))
					&& Character.isUpperCase(methodName.charAt(3))) {
				accessor = methodName.charAt(0) == 'g' ? AccessorSupport.GETTER : AccessorSupport.SETTER;
			}
		}
		return accessor;
	}
}