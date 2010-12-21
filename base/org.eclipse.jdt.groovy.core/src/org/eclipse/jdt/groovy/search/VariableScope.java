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

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;
import org.codehaus.jdt.groovy.internal.compiler.ast.LazyGenericsType;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @author Andrew Eisenberg
 * @created Sep 25, 2009
 *          <p>
 *          This class maps variable names to types in a hierarchy
 *          </p>
 */
public class VariableScope {

	public static final ClassNode OBJECT_CLASS_NODE = ClassHelper.OBJECT_TYPE;
	public static final ClassNode LIST_CLASS_NODE = ClassHelper.LIST_TYPE;
	public static final ClassNode PATTERN_CLASS_NODE = ClassHelper.PATTERN_TYPE;
	public static final ClassNode MAP_CLASS_NODE = ClassHelper.MAP_TYPE;
	public static final ClassNode STRING_CLASS_NODE = ClassHelper.STRING_TYPE;
	public static final ClassNode GSTRING_CLASS_NODE = ClassHelper.GSTRING_TYPE;
	public static final ClassNode DGM_CLASS_NODE = ClassHelper.make(DefaultGroovyMethods.class);
	public static final ClassNode DGSM_CLASS_NODE = ClassHelper.make(DefaultGroovyStaticMethods.class);
	public static final ClassNode VOID_CLASS_NODE = ClassHelper.make(void.class);
	public static final ClassNode VOID_WRAPPER_CLASS_NODE = ClassHelper.void_WRAPPER_TYPE;
	public static final ClassNode NUMBER_CLASS_NODE = ClassHelper.make(Number.class);
	public static final ClassNode ITERATOR_CLASS = ClassHelper.make(Iterator.class);
	public static final ClassNode ENUMERATION_CLASS = ClassHelper.make(Enumeration.class);
	public static final ClassNode INPUT_STREAM_CLASS = ClassHelper.make(InputStream.class);
	public static final ClassNode DATA_INPUT_STREAM_CLASS = ClassHelper.make(DataInputStream.class);

	// don't cache because we have to add properties
	public static final ClassNode CLASS_CLASS_NODE = ClassHelper.makeWithoutCaching(Class.class);
	static {
		initializeProperties(CLASS_CLASS_NODE);
	}

	// primitive wrapper classes
	public static final ClassNode INTEGER_CLASS_NODE = ClassHelper.Integer_TYPE;
	public static final ClassNode LONG_CLASS_NODE = ClassHelper.Long_TYPE;
	public static final ClassNode SHORT_CLASS_NODE = ClassHelper.Short_TYPE;
	public static final ClassNode FLOAT_CLASS_NODE = ClassHelper.Float_TYPE;
	public static final ClassNode DOUBLE_CLASS_NODE = ClassHelper.Double_TYPE;
	public static final ClassNode BYTE_CLASS_NODE = ClassHelper.Byte_TYPE;
	public static final ClassNode BOOLEAN_CLASS_NODE = ClassHelper.Boolean_TYPE;
	public static final ClassNode CHARACTER_CLASS_NODE = ClassHelper.Character_TYPE;

	public static class VariableInfo {
		public final ClassNode type;
		public final ClassNode declaringType;

		public VariableInfo(ClassNode type, ClassNode declaringType) {
			super();
			this.type = type;
			this.declaringType = declaringType;
		}
	}

	public static class CallAndType {
		public CallAndType(MethodCallExpression call, ClassNode declaringType) {
			this.call = call;
			this.declaringType = declaringType;
		}

		public final MethodCallExpression call;
		public final ClassNode declaringType;
	}

	public static ClassNode NO_CATEGORY = null;

	/**
	 * Null for the top level scope
	 */
	private VariableScope parent;

	/**
	 * AST node for this scope, typically, a block, closure, or body declaration
	 */
	private ASTNode enclosingNode;

	private Map<String, VariableInfo> nameVariableMap = new HashMap<String, VariableInfo>();

	private boolean isStaticScope;

	/**
	 * the enclosing method call is the one where there are the current node is part of an argument list
	 */
	private CallAndType enclosingMethodCall;

	private final ClosureExpression enclosingClosure;

	/**
	 * Category that will be declared in the next scope
	 */
	private ClassNode categoryBeingDeclared;

	public VariableScope(VariableScope parent, ASTNode enclosingNode, boolean isStatic) {
		this.parent = parent;
		this.enclosingNode = enclosingNode;

		// this scope is considered static if in a static method, or
		// it's parent is static
		this.isStaticScope = isStatic || (parent != null && parent.isStaticScope);
		if (enclosingNode instanceof ClosureExpression) {
			this.enclosingClosure = (ClosureExpression) enclosingNode;
		} else {
			this.enclosingClosure = null;
		}
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
			categories.add(DGM_CLASS_NODE); // default category
			categories.add(DGSM_CLASS_NODE); // default category
			return categories;
		}
	}

	private boolean isCategoryBeingDeclared() {
		return categoryBeingDeclared != null;
	}

	public void setCategoryBeingDeclared(ClassNode categoryBeingDeclared) {
		this.categoryBeingDeclared = categoryBeingDeclared;
	}

	/**
	 * Find the variable in the current environment, Look in this scope or parent scope if not found here
	 * 
	 * @param name
	 * @return the variable info or null if not found
	 */
	public VariableInfo lookupName(String name) {
		//		if ("this".equals(name)) { //$NON-NLS-1$
		// ClassNode declaringType = getEnclosingTypeDeclaration();
		// return new VariableInfo(declaringType, declaringType);
		// } else
		if ("super".equals(name)) { //$NON-NLS-1$
			VariableInfo var = lookupName("this");
			if (var != null) {
				ClassNode superType = var.declaringType.getSuperClass();
				return new VariableInfo(superType, superType);
			}
		}

		VariableInfo var = nameVariableMap.get(name);
		if (var == null && parent != null) {
			var = parent.lookupName(name);
		}
		return var;
	}

	public boolean isThisOrSuper(Variable var) {
		return var.getName().equals("this") || var.getName().equals("super"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void addVariable(String name, ClassNode type, ClassNode declaringType) {
		nameVariableMap.put(name, new VariableInfo(type, declaringType != null ? declaringType : OBJECT_CLASS_NODE));
	}

	public void addVariable(Variable var) {
		addVariable(var.getName(), var.getType(), var.getOriginType());
	}

	public ModuleNode getEnclosingModuleNode() {
		if (enclosingNode instanceof ModuleNode) {
			return (ModuleNode) enclosingNode;
		} else if (parent != null) {
			return parent.getEnclosingModuleNode();
		} else {
			return null;
		}
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

	public static boolean isVoidOrObject(ClassNode maybeVoid) {
		return maybeVoid.getName().equals(VOID_CLASS_NODE.getName())
				|| maybeVoid.getName().equals(VOID_WRAPPER_CLASS_NODE.getName())
				|| maybeVoid.getName().equals(OBJECT_CLASS_NODE.getName());
	}

	/**
	 * Updates the type info of this variable if it already exists in scope, or just adds it if it doesn't
	 * 
	 * @param name
	 * @param objectExpressionType
	 * @param declaringType
	 */
	public void updateOrAddVariable(String name, ClassNode type, ClassNode declaringType) {
		if (!internalUpdateVariable(name, type, declaringType)) {
			addVariable(name, type, declaringType);
		}
	}

	/**
	 * Return true if the type has been udpated, false otherwise
	 * 
	 * @param name
	 * @param objectExpressionType
	 * @param declaringType
	 * @return
	 */
	private boolean internalUpdateVariable(String name, ClassNode type, ClassNode declaringType) {
		VariableInfo info = nameVariableMap.get(name);
		if (info != null) {
			nameVariableMap.put(name, new VariableInfo(type, declaringType == null ? info.declaringType : declaringType));
			return true;
		} else if (parent != null) {
			return parent.internalUpdateVariable(name, type, declaringType);
		} else {
			return false;
		}
	}

	/**
	 * @param resolvedGenerics
	 * @param unresolvedGenerics
	 * @param type
	 * @return the resolved class node, or null if there was nothing to resolve
	 */
	public static ClassNode resolveTypeParameterization(GenericsType[] resolvedGenerics, GenericsType[] unresolvedGenerics,
			ClassNode type) {

		if (isValidGenerics(resolvedGenerics, unresolvedGenerics, type)) {
			GenericsType[] typesToParameterize = type.getGenericsTypes();

			// try to match
			outer: for (int i = 0; i < typesToParameterize.length; i++) {
				GenericsType typeToParameterize = typesToParameterize[i];

				if (typeToParameterize instanceof LazyGenericsType) {
					// LazyGenericsType is immutable
					// shouldn't get here...log error and continue
					Util.log(new RuntimeException(), "Found a JDTClassNode while resolving type parameters.  " //$NON-NLS-1$
							+ "This shouldn't happen.  Not trying to resolve any further " + "and continuing.  Type: " + type); //$NON-NLS-1$ //$NON-NLS-2$
					continue;
				}

				// recur down the type parameter
				resolveTypeParameterization(resolvedGenerics, unresolvedGenerics, typeToParameterize.getType());

				String toParameterizeName = typeToParameterize.getName();
				for (int j = 0; j < unresolvedGenerics.length; j++) {
					if (toParameterizeName.equals(unresolvedGenerics[j].getName())) {
						// we have a match, three possibilities, this type is the resolved type parameter of a generic type (eg-
						// Iterator<E> --> Iterator<String>)
						// or it is the resolution of a type parameter itself (eg- E --> String)
						// or it is a substitution of one type parameter for another (eg- List<T> --> List<E>, where T comes from
						// the declaring type)
						// if this parameter exists in the redirect, then it is the former, if not, then check the redirect for type
						// parameters
						if (typeParameterExistsInRedirected(type, toParameterizeName)) {
							// we have: Iterator<E> --> Iterator<String>
							type.getGenericsTypes()[i].setType(resolvedGenerics[j].getType());
							typeToParameterize.setName(typeToParameterize.getType().getName());
							typeToParameterize.setUpperBounds(null);
							typeToParameterize.setLowerBound(null);
						} else {
							// E --> String
							// no need to recur since this is the resolution of a type parameter
							type = resolvedGenerics[j].getType();

							// I *think* this means we are done.
							// I *think* this can only be reached when typesToParameterize.length == 1
							break outer;
						}
					}
				}
			}
		}
		return type;
	}

	/**
	 * @param type
	 * @param toParameterizeName
	 * @return
	 */
	private static boolean typeParameterExistsInRedirected(ClassNode type, String toParameterizeName) {
		ClassNode redirect = type.redirect();
		GenericsType[] genericsTypes = redirect.getGenericsTypes();
		if (genericsTypes != null) {
			// I don't *think* we need to check here. if any type parameter exists in the redirect, then we are parameterizing
			return true;
			// for (GenericsType gt : genericsTypes) {
			// if (gt.getName().equals(toParameterizeName)) {
			// return true;
			// }
			// }
		}
		return false;
	}

	/**
	 * This class checks to see if there are type parameters on the type, and if the generics to resolve against are valid.
	 * 
	 * @param resolvedGenerics bound type parameters
	 * @param unresolvedGenerics unbound type parameters
	 * @param type type to resolve
	 * @return
	 */
	private static boolean isValidGenerics(GenericsType[] resolvedGenerics, GenericsType[] unresolvedGenerics, ClassNode type) {
		// first a quick check
		GenericsType[] thisTypeGenerics = type.getGenericsTypes();
		if (thisTypeGenerics == null || thisTypeGenerics.length == 0) {
			return false;
		}

		// now a more detailed check
		return resolvedGenerics != null && unresolvedGenerics != null && unresolvedGenerics.length == resolvedGenerics.length
				&& resolvedGenerics.length > 0;
	}

	/**
	 * Create a copy of this class, taking into account generics and redirects
	 * 
	 * @param type type to copy
	 * @return a copy of this type
	 */
	public static ClassNode clone(ClassNode type) {
		if (type == null) {
			return null;
		}
		ClassNode newType;
		newType = type.getPlainNodeReference();
		newType.setRedirect(type.redirect());
		ClassNode[] origIFaces = type.getInterfaces();
		if (origIFaces != null) {
			ClassNode[] newIFaces = new ClassNode[origIFaces.length];
			for (int i = 0; i < newIFaces.length; i++) {
				newIFaces[i] = origIFaces[i];
			}
			newType.setInterfaces(newIFaces);
		}
		newType.setSourcePosition(type);
		GenericsType[] origgts = type.getGenericsTypes();
		if (origgts != null) {
			GenericsType[] newgts = new GenericsType[origgts.length];
			for (int i = 0; i < origgts.length; i++) {
				newgts[i] = clone(origgts[i]);
			}
			newType.setGenericsTypes(newgts);
		}
		return newType;
	}

	/**
	 * Create a copy of this {@link GenericsType}
	 * 
	 * @param origgt
	 * @return a copy
	 */
	public static GenericsType clone(GenericsType origgt) {
		GenericsType newgt = new GenericsType();
		newgt.setType(clone(origgt.getType()));
		newgt.setLowerBound(clone(origgt.getLowerBound()));
		ClassNode[] oldUpperBounds = origgt.getUpperBounds();
		if (oldUpperBounds != null) {
			ClassNode[] newUpperBounds = new ClassNode[oldUpperBounds.length];
			for (int i = 0; i < newUpperBounds.length; i++) {
				newUpperBounds[i] = clone(oldUpperBounds[i]);
			}
			newgt.setUpperBounds(newUpperBounds);
		}
		newgt.setName(origgt.getName());
		newgt.setPlaceholder(origgt.isPlaceholder());
		newgt.setWildcard(origgt.isWildcard());
		newgt.setResolved(origgt.isResolved());
		newgt.setSourcePosition(origgt);
		return newgt;
	}

	/**
	 * attempt to get the component type of rhs
	 * 
	 * @param c
	 * @return component type, generic type of collection, or c
	 */
	public static ClassNode deref(ClassNode c) {
		if (c.isArray()) {
			return c.getComponentType();
		} else {
			GenericsType[] genericsTypes = c.getGenericsTypes();
			if (genericsTypes != null && genericsTypes.length > 0) {
				// use length-1 so that both Maps and Collections are handled
				return genericsTypes[genericsTypes.length - 1].getType();
			}
		}
		return c;
	}

	/**
	 * @return true iff this is a static stack frame
	 */
	public boolean isStatic() {
		return isStaticScope;
	}

	public ClosureExpression getEnclosingClosure() {
		if (enclosingClosure == null && parent != null) {
			return parent.getEnclosingClosure();
		}
		return enclosingClosure;
	}

	/**
	 * @return the enclosing method call expression if one exists, or null otherwise. For example, when visiting the following
	 *         closure, the enclosing method call is 'run'
	 * 
	 *         <pre>
	 * def runner = new Runner()
	 * runner.run {
	 *   print "hello!"
	 * }
	 * </pre>
	 */
	public List<CallAndType> getAllEnclosingMethodCallExpressions() {
		List<CallAndType> list;
		if (parent == null) {
			list = new ArrayList<CallAndType>();
		} else {
			list = parent.getAllEnclosingMethodCallExpressions();
			if (enclosingMethodCall != null) {
				list.add(enclosingMethodCall);
			}
		}
		return list;
	}

	public CallAndType getEnclosingMethodCallExpression() {
		if (parent != null && enclosingMethodCall == null) {
			return parent.getEnclosingMethodCallExpression();
		}
		return enclosingMethodCall;
	}

	public void setEnclosingMethodCall(CallAndType enclosingMethodCall) {
		this.enclosingMethodCall = enclosingMethodCall;
	}

	// this is the expression cache. I don't know if I like this.
	// records the type of all expressions that were seen so far by this scope
	private final Map<Expression, ClassNode> expressionTypeCache = new HashMap<Expression, ClassNode>();

	public void recordExpressionType(Expression expr, ClassNode type) {
		expressionTypeCache.put(expr, type);
	}

	public ClassNode queryExpressionType(Expression expr) {
		return expressionTypeCache.get(expr);
	}

	/**
	 * @return
	 */
	public boolean isTopLevel() {
		return parent == null;
	}
}
