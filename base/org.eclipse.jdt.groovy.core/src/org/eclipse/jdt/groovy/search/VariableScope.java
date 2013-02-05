/*******************************************************************************
 * Copyright (c) 2009-2011 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     SpringSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.groovy.search;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.Tuple;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.runtime.DateGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;
import org.codehaus.groovy.runtime.EncodingGroovyMethods;
import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.codehaus.groovy.runtime.SwingGroovyMethods;
import org.codehaus.groovy.runtime.XmlGroovyMethods;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.Signature;

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
	public static final ClassNode RANGE_CLASS_NODE = ClassHelper.RANGE_TYPE;
	public static final ClassNode TUPLE_CLASS_NODE = ClassHelper.make(Tuple.class);
	public static final ClassNode PATTERN_CLASS_NODE = ClassHelper.PATTERN_TYPE;
	public static final ClassNode MATCHER_CLASS_NODE = ClassHelper.make(Matcher.class);
	public static final ClassNode MAP_CLASS_NODE = ClassHelper.MAP_TYPE;
	public static final ClassNode STRING_CLASS_NODE = ClassHelper.STRING_TYPE;
	public static final ClassNode GSTRING_CLASS_NODE = ClassHelper.GSTRING_TYPE;
	public static final ClassNode VOID_CLASS_NODE = ClassHelper.make(void.class);
	public static final ClassNode VOID_WRAPPER_CLASS_NODE = ClassHelper.void_WRAPPER_TYPE;
	public static final ClassNode NUMBER_CLASS_NODE = ClassHelper.make(Number.class);
	public static final ClassNode ITERATOR_CLASS = ClassHelper.make(Iterator.class);
	public static final ClassNode ENUMERATION_CLASS = ClassHelper.make(Enumeration.class);
	public static final ClassNode INPUT_STREAM_CLASS = ClassHelper.make(InputStream.class);
	public static final ClassNode OUTPUT_STREAM_CLASS = ClassHelper.make(OutputStream.class);
	public static final ClassNode DATA_INPUT_STREAM_CLASS = ClassHelper.make(DataInputStream.class);
	public static final ClassNode DATA_OUTPUT_STREAM_CLASS = ClassHelper.make(DataOutputStream.class);
	public static final ClassNode OBJECT_OUTPUT_STREAM = ClassHelper.make(ObjectOutputStream.class);
	public static final ClassNode OBJECT_INPUT_STREAM = ClassHelper.make(ObjectInputStream.class);
	public static final ClassNode FILE_CLASS_NODE = ClassHelper.make(File.class);
	public static final ClassNode BUFFERED_READER_CLASS_NODE = ClassHelper.make(BufferedReader.class);
	public static final ClassNode BUFFERED_WRITER_CLASS_NODE = ClassHelper.make(BufferedWriter.class);
	public static final ClassNode PRINT_WRITER_CLASS_NODE = ClassHelper.make(PrintWriter.class);
	public static final ClassNode CLOSURE_CLASS = ClassHelper.CLOSURE_TYPE;
	public static final ClassNode GROOVY_OBJECT_SUPPORT = ClassHelper.make(GroovyObjectSupport.class);

	// standard category classes
	public static final ClassNode DGM_CLASS_NODE = ClassHelper.make(DefaultGroovyMethods.class);
	public static final ClassNode EGM_CLASS_NODE = ClassHelper.make(EncodingGroovyMethods.class);
	public static final ClassNode PGM_CLASS_NODE = ClassHelper.make(ProcessGroovyMethods.class);
	public static final ClassNode SGM_CLASS_NODE = ClassHelper.make(SwingGroovyMethods.class);
	public static final ClassNode XGM_CLASS_NODE = ClassHelper.make(XmlGroovyMethods.class);
	public static final ClassNode DGSM_CLASS_NODE = ClassHelper.make(DefaultGroovyStaticMethods.class);
	public static final ClassNode DATE_GM_CLASS_NODE = ClassHelper.make(DateGroovyMethods.class);

	// only exists on Groovy 2.0
	public static ClassNode RESOURCE_GROOVY_METHODS;
	public static ClassNode STRING_GROOVY_METHODS;
	public static ClassNode IO_GROOVY_METHODS;
	static {
		try {
			RESOURCE_GROOVY_METHODS = ClassHelper.make(Class.forName("org.codehaus.groovy.runtime.ResourceGroovyMethods"));
			STRING_GROOVY_METHODS = ClassHelper.make(Class.forName("org.codehaus.groovy.runtime.StringGroovyMethods"));
			IO_GROOVY_METHODS = ClassHelper.make(Class.forName("org.codehaus.groovy.runtime.IOGroovyMethods"));
		} catch (ClassNotFoundException e) {
			RESOURCE_GROOVY_METHODS = null;
			STRING_GROOVY_METHODS = null;
			IO_GROOVY_METHODS = null;
		}
	}
	// not available on all platforms
	// public static final ClassNode PLUGIN5_GM_CLASS_NODE = ClassHelper
	// .make(org.codehaus.groovy.vmplugin.v5.PluginDefaultGroovyMethods.class);
	// public static final ClassNode PLUGIN6_GM_CLASS_NODE = ClassHelper
	// .make(org.codehaus.groovy.vmplugin.v6.PluginDefaultGroovyMethods.class);

	// only exists on 2.1 and later
	public static ClassNode DELEGATES_TO;
	static {
		try {
			DELEGATES_TO = ClassHelper.make(Class.forName("groovy.lang.DelegatesTo"));
		} catch (ClassNotFoundException e) {
			DELEGATES_TO = null;
		}
	}

	public static Set<ClassNode> ALL_DEFAULT_CATEGORIES;
	static {
		// add all of the known DGM classes. Order counts since we look up earlier in the list before later and need to
		// ensure we don't accidentally place deprecated elements early in the list
		List<ClassNode> dgm_classes = new ArrayList<ClassNode>(10);
		if (STRING_GROOVY_METHODS != null) {
			dgm_classes.add(STRING_GROOVY_METHODS);
		}
		if (RESOURCE_GROOVY_METHODS != null) {
			dgm_classes.add(RESOURCE_GROOVY_METHODS);
		}
		if (IO_GROOVY_METHODS != null) {
			dgm_classes.add(IO_GROOVY_METHODS);
		}
		dgm_classes.add(EGM_CLASS_NODE);
		dgm_classes.add(PGM_CLASS_NODE);
		dgm_classes.add(SGM_CLASS_NODE);
		dgm_classes.add(XGM_CLASS_NODE);
		dgm_classes.add(DATE_GM_CLASS_NODE);
		dgm_classes.add(DGSM_CLASS_NODE);
		dgm_classes.add(DGM_CLASS_NODE);
		ALL_DEFAULT_CATEGORIES = Collections.unmodifiableSet(new LinkedHashSet<ClassNode>(dgm_classes));
	}

	// don't cache because we have to add properties
	public static final ClassNode CLASS_CLASS_NODE = ClassHelper.makeWithoutCaching(Class.class);
	static {
		initializeProperties(CLASS_CLASS_NODE);
	}

	public static final ClassNode CLASS_ARRAY_CLASS_NODE = CLASS_CLASS_NODE.makeArray();

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

		public String getTypeSignature() {
			String typeName = type.getName();
			if (typeName.startsWith("[")) {
				return typeName;
			} else {
				return Signature.createTypeSignature(typeName, true);
			}
		}
	}

	public static class CallAndType {

		public CallAndType(MethodCallExpression call, ClassNode declaringType, ASTNode declaration) {
			this.call = call;
			this.declaringType = declaringType;
			this.declaration = declaration;

			// the @DelegatesTo Groovy 2.1 annotation
			if (DELEGATES_TO != null && declaration instanceof MethodNode) {
				MethodNode methodDecl = (MethodNode) declaration;
				if (methodDecl.getParameters() != null) {
					Expression argsExpr = call.getArguments();
					List<Expression> args = null;
					if (argsExpr instanceof TupleExpression) {
						args = ((TupleExpression) argsExpr).getExpressions();
					}
					if (args != null) {
						Parameter[] parameters = methodDecl.getParameters();
						for (int i = 0; i < parameters.length; i++) {
							Parameter p = parameters[i];
							List<AnnotationNode> annotations = p.getAnnotations();
							if (annotations != null) {
								for (AnnotationNode annotation : annotations) {
									if (annotation.getClassNode().getName().equals(DELEGATES_TO.getName()) && args.size() > i
											&& args.get(i) instanceof ClosureExpression
											&& annotation.getMember("value") instanceof ClassExpression) {
										delegatesToClosures = new HashMap<ClosureExpression, ClassNode>(3);
										delegatesToClosures.put((ClosureExpression) args.get(i), annotation.getMember("value")
												.getType());
									}
								}
							}
						}
					}
				}
			}
			if (delegatesToClosures == null) {
				delegatesToClosures = Collections.emptyMap();

			}
		}

		public final ASTNode declaration;
		public final MethodCallExpression call;
		public final ClassNode declaringType;
		public Map<ClosureExpression, ClassNode> delegatesToClosures;
	}

	/**
	 * Contains state that is shared amongst {@link VariableScope}s
	 */
	private class SharedState {
		/**
		 * this field stores values that need to get passed between parts of the file to another
		 */
		final Map<String, Object> wormhole = new HashMap<String, Object>();
		/**
		 * the enclosing method call is the one where there are the current node is part of an argument list
		 */
		final Stack<CallAndType> enclosingCallStack = new Stack<VariableScope.CallAndType>();
		/**
		 * Node currently being evaluated, or null if none
		 */
		final Stack<ASTNode> nodeStack = new Stack<ASTNode>();

		/**
		 * true iff current scope is implicit run method of script
		 */
		boolean isRunMethod;
	}

	public static ClassNode NO_CATEGORY = null;

	/**
	 * Null for the top level scope
	 */
	private VariableScope parent;

	/**
	 * Shared with parent scopes
	 */
	private SharedState shared;

	/**
	 * AST node for this scope, typically, a block, closure, or body declaration
	 */
	private ASTNode scopeNode;

	private Map<String, VariableInfo> nameVariableMap = new HashMap<String, VariableInfo>();

	private boolean isStaticScope;

	private final ClosureExpression enclosingClosure;

	/**
	 * Category that will be declared in the next scope
	 */
	private ClassNode categoryBeingDeclared;

	/**
	 * number of parameters of current method call or -1 if not a method call
	 */
	private int methodCallNumberOfArguments = -1;
	private boolean isPrimaryNode;

	public VariableScope(VariableScope parent, ASTNode enclosingNode, boolean isStatic) {
		this.parent = parent;
		this.scopeNode = enclosingNode;
		if (parent != null) {
			this.shared = parent.shared;
		} else {
			this.shared = new SharedState();
		}

		// keep track of whether or not in a script body
		// also, try not to recalculate each time.
		if (enclosingNode instanceof MethodNode) {
			this.shared.isRunMethod = ((MethodNode) enclosingNode).isScriptBody();
		} else if (enclosingNode instanceof FieldNode || enclosingNode instanceof ClassNode) {
			this.shared.isRunMethod = false;
		}

		// this scope is considered static if in a static method, or
		// its parent is static
		this.isStaticScope = isStatic || (parent != null && parent.isStaticScope);
		if (enclosingNode instanceof ClosureExpression) {
			this.enclosingClosure = (ClosureExpression) enclosingNode;
		} else {
			this.enclosingClosure = null;
		}
	}

	/**
	 * Back door for storing and retrieving objects between lookup locations
	 * 
	 * @return the wormhole object
	 */
	public Map<String, Object> getWormhole() {
		return shared.wormhole;
	}

	public ASTNode getEnclosingNode() {
		if (shared.nodeStack.size() > 1) {
			ASTNode current = shared.nodeStack.pop();
			ASTNode enclosing = shared.nodeStack.peek();
			shared.nodeStack.push(current);
			return enclosing;
		} else {
			return null;
		}
	}

	public void setPrimaryNode(boolean isPrimaryNode) {
		this.isPrimaryNode = isPrimaryNode;
	}

	public void setCurrentNode(ASTNode currentNode) {
		shared.nodeStack.push(currentNode);
	}

	public void forgetCurrentNode() {
		if (!shared.nodeStack.isEmpty()) {
			shared.nodeStack.pop();
		}
	}

	public ASTNode getCurrentNode() {
		if (!shared.nodeStack.isEmpty()) {
			return shared.nodeStack.peek();
		} else {
			return null;
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
			return new LinkedHashSet<ClassNode>(ALL_DEFAULT_CATEGORIES);
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
		if ("super".equals(name)) { //$NON-NLS-1$
			ClassNode type = getDelegateOrThis();
			if (type != null) {
				ClassNode superType = type.getSuperClass();
				superType = superType == null ? VariableScope.OBJECT_CLASS_NODE : superType;
				return new VariableInfo(superType, superType);
			}
		}

		VariableInfo var = lookupNameInCurrentScope(name);
		if (var == null && parent != null) {
			var = parent.lookupName(name);
		}
		return var;
	}

	public ClassNode getThis() {
		VariableInfo thiz = lookupName("this");
		return thiz != null ? thiz.type : null;
	}

	public ClassNode getDelegate() {
		VariableInfo delegate = lookupName("delegate");
		return delegate != null ? delegate.type : null;
	}

	/**
	 * @return the current delegate type if exists, or this type if exists, or Object. Returns null if in top level scope (ie- in
	 *         import statement)
	 */
	public VariableInfo getDelegateOrThisInfo() {
		VariableInfo info = lookupName("delegate");
		if (info != null) {
			return info;
		}
		info = lookupName("this");

		// might be null if in imports
		return info;
	}

	public ClassNode getDelegateOrThis() {
		VariableInfo info = getDelegateOrThisInfo();
		return info != null ? info.type : null;
	}

	/**
	 * Looks up the name in the current scope. Does not recur up to parent scopes
	 * 
	 * @param name
	 * @return
	 */
	public VariableInfo lookupNameInCurrentScope(String name) {
		return nameVariableMap.get(name);
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
		if (scopeNode instanceof ModuleNode) {
			return (ModuleNode) scopeNode;
		} else if (parent != null) {
			return parent.getEnclosingModuleNode();
		} else {
			return null;
		}
	}

	public ClassNode getEnclosingTypeDeclaration() {
		if (scopeNode instanceof ClassNode) {
			return (ClassNode) scopeNode;
		} else if (parent != null) {
			return parent.getEnclosingTypeDeclaration();
		} else {
			return null;
		}
	}

	public FieldNode getEnclosingFieldDeclaration() {
		if (scopeNode instanceof FieldNode) {
			return (FieldNode) scopeNode;
		} else if (parent != null) {
			return parent.getEnclosingFieldDeclaration();
		} else {
			return null;
		}
	}

	public MethodNode getEnclosingMethodDeclaration() {
		if (scopeNode instanceof MethodNode) {
			return (MethodNode) scopeNode;
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
		ClassNode declaringClass = methodNode.getDeclaringClass();
		PropertyNode property = new PropertyNode(propertyName.toString(), mods, propertyType, declaringClass, null, null, null);
		property.setDeclaringClass(declaringClass);
		property.getField().setDeclaringClass(declaringClass);
		return property;
	}

	private static void initializeProperties(ClassNode node) {
		// getX methods
		for (MethodNode methodNode : node.getMethods()) {
			if (AccessorSupport.isGetter(methodNode)) {
				node.addProperty(createPropertyNodeForMethodNode(methodNode));
			}
		}
	}

	public static boolean isVoidOrObject(ClassNode maybeVoid) {
		return maybeVoid != null
				&& (maybeVoid.getName().equals(VOID_CLASS_NODE.getName())
						|| maybeVoid.getName().equals(VOID_WRAPPER_CLASS_NODE.getName()) || maybeVoid.getName().equals(
						OBJECT_CLASS_NODE.getName()));
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
	 * Updates the identifier if it exists in this scope or a parent scope. Otherwise does nothing
	 * 
	 * @param name identifier to update
	 * @param type type of identifier
	 * @param declaringType declaring type of identifier
	 * @return true iff the variable exists in scope and was updated
	 */
	public boolean updateVariable(String name, ClassNode type, ClassNode declaringType) {
		return internalUpdateVariable(name, type, declaringType);
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
		VariableInfo info = lookupNameInCurrentScope(name);
		if (info != null) {
			nameVariableMap.put(name, new VariableInfo(type, declaringType == null ? info.declaringType : declaringType));
			return true;
		} else if (parent != null) {
			return parent.internalUpdateVariable(name, type, declaringType);
		} else {
			return false;
		}
	}

	public static ClassNode resolveTypeParameterization(GenericsMapper mapper, ClassNode typeToParameterize) {
		if (!mapper.hasGenerics()) {
			return typeToParameterize;
		}
		GenericsType[] typesToParameterize = typeToParameterize.getGenericsTypes();
		if (typesToParameterize == null) {
			return typeToParameterize;
		}
		// try to match
		for (int i = 0; i < typesToParameterize.length; i++) {
			GenericsType genericsToParameterize = typesToParameterize[i];

			// recur down the type parameter
			resolveTypeParameterization(mapper, genericsToParameterize.getType());

			String toParameterizeName = genericsToParameterize.getName();
			ClassNode resolved = mapper.findParameter(toParameterizeName, genericsToParameterize.getType());
			// we have a match, three possibilities, this type is the resolved type parameter of a generic type (eg-
			// Iterator<E> --> Iterator<String>)
			// or it is the resolution of a type parameter itself (eg- E --> String)
			// or it is a substitution of one type parameter for another (eg- List<T> --> List<E>, where T comes from
			// the declaring type)
			// if this parameter exists in the redirect, then it is the former, if not, then check the redirect for type
			// parameters
			if (typeParameterExistsInRedirected(typeToParameterize, toParameterizeName)) {
				Assert.isLegal(typeToParameterize.redirect() != typeToParameterize,
						"Error: trying to resolve type parameters of a type declaration: " + typeToParameterize);
				// we have: Iterator<E> --> Iterator<String>
				typeToParameterize.getGenericsTypes()[i].setType(resolved);
				genericsToParameterize.setName(genericsToParameterize.getType().getName());
				genericsToParameterize.setUpperBounds(null);
				genericsToParameterize.setLowerBound(null);
			} else {
				// E --> String
				// no need to recur since this is the resolution of a type parameter
				typeToParameterize = resolved;

				// I *think* this means we are done.
				// I *think* this can only be reached when typesToParameterize.length == 1
				break;
			}
		}
		return typeToParameterize;
	}

	static final public GenericsType[] NO_GENERICS = new GenericsType[0];

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
		}
		return false;
	}

	/**
	 * Create a copy of this class, taking into account generics and redirects
	 * 
	 * @param type type to copy
	 * @return a copy of this type
	 */
	public static ClassNode clone(ClassNode type) {
		return cloneInternal(type, 0);
	}

	public static ClassNode clonedMap() {
		ClassNode clone = clone(MAP_CLASS_NODE);
		cleanGenerics(clone.getGenericsTypes()[0]);
		cleanGenerics(clone.getGenericsTypes()[1]);
		return clone;
	}

	public static ClassNode clonedList() {
		ClassNode clone = clone(LIST_CLASS_NODE);
		cleanGenerics(clone.getGenericsTypes()[0]);
		return clone;
	}

	public static ClassNode clonedRange() {
		ClassNode clone = clone(RANGE_CLASS_NODE);
		cleanGenerics(clone.getGenericsTypes()[0]);
		return clone;
	}

	public static ClassNode clonedTuple() {
		// ClassNode clone = clone(TUPLE_CLASS_NODE);
		// cleanGenerics(clone.getGenericsTypes()[0]);
		// return clone;
		// the typle class is not parameterized in Groovy 1.7, so just return list.
		return clonedList();
	}

	private static void cleanGenerics(GenericsType gt) {
		gt.getType().setGenericsTypes(null);
		gt.setName("java.lang.Object");
		gt.setPlaceholder(false);
		gt.setWildcard(false);
		gt.setResolved(true);
		gt.setUpperBounds(null);
		gt.setLowerBound(null);
	}

	/**
	 * Internal variant of clone that ensures stack recursion never gets too large
	 * 
	 * @param type class to clone
	 * @param depth prevent recursion
	 * @return
	 */
	private static ClassNode cloneInternal(ClassNode type, int depth) {
		if (type == null) {
			return type;
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

		// See GRECLIPSE-1024 set an arbitrary depth to return from
		// ensures that improperly set up generics do not lead to infinite recursion
		if (depth > 10) {
			return newType;
		}

		GenericsType[] origgts = type.getGenericsTypes();
		if (origgts != null) {
			GenericsType[] newgts = new GenericsType[origgts.length];
			for (int i = 0; i < origgts.length; i++) {
				newgts[i] = clone(origgts[i], depth);
			}
			newType.setGenericsTypes(newgts);
		}
		return newType;
	}

	/**
	 * Create a copy of this {@link GenericsType}
	 * 
	 * @param origgt the original {@link GenericsType} to copy
	 * @param depth prevent infinite recursion on bad generics
	 * @return a copy
	 */
	private static GenericsType clone(GenericsType origgt, int depth) {
		GenericsType newgt = new GenericsType();
		newgt.setType(cloneInternal(origgt.getType(), depth + 1));
		newgt.setLowerBound(cloneInternal(origgt.getLowerBound(), depth + 1));
		ClassNode[] oldUpperBounds = origgt.getUpperBounds();
		if (oldUpperBounds != null) {
			ClassNode[] newUpperBounds = new ClassNode[oldUpperBounds.length];
			for (int i = 0; i < newUpperBounds.length; i++) {
				// avoid infinite recursion of Enum<E extends Enum<?>>
				if (oldUpperBounds[i].getName().equals(newgt.getType().getName())) {
					newUpperBounds[i] = VariableScope.OBJECT_CLASS_NODE;
				} else {
					newUpperBounds[i] = cloneInternal(oldUpperBounds[i], depth + 1);
				}
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
	 * @return true iff this is a static stack frame
	 */
	public boolean isStatic() {
		return isStaticScope;
	}

	/**
	 * @return true iff the current node is not the RHS of a dotted expression
	 */
	public boolean isPrimaryNode() {
		return isPrimaryNode;
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
		return shared.enclosingCallStack;
	}

	public CallAndType getEnclosingMethodCallExpression() {
		if (shared.enclosingCallStack.isEmpty()) {
			return null;
		} else {
			return shared.enclosingCallStack.peek();
		}
	}

	public void addEnclosingMethodCall(CallAndType enclosingMethodCall) {
		shared.enclosingCallStack.push(enclosingMethodCall);
	}

	public void forgetEnclosingMethodCall() {
		shared.enclosingCallStack.pop();
	}

	public boolean isTopLevel() {
		return parent == null;
	}

	/**
	 * Does the following name exist in this scope (does not recur up to parent scopes).
	 * 
	 * @param name
	 * @return true iff in the {@link #nameVariableMap}
	 */
	public boolean containsInThisScope(String name) {
		return nameVariableMap.containsKey(name);
	}

	/**
	 * If visiting the identifier of a method call expression, this field will be equal to the number of arguments to the method
	 * call.
	 */
	int getMethodCallNumberOfArguments() {
		return methodCallNumberOfArguments;
	}

	void setMethodCallNumberOfArguments(int methodCallNumberOfArguments) {
		this.methodCallNumberOfArguments = methodCallNumberOfArguments;
	}

	public boolean isMethodCall() {
		return methodCallNumberOfArguments >= 0;
	}

	public Iterator<Map.Entry<String, VariableInfo>> variablesIterator() {
		return new Iterator<Map.Entry<String, VariableInfo>>() {
			VariableScope currentScope = VariableScope.this;
			Iterator<Map.Entry<String, VariableInfo>> currentIter = currentScope.nameVariableMap.entrySet().iterator();

			public boolean hasNext() {
				if (currentIter == null) {
					return false;
				}
				if (!currentIter.hasNext()) {
					currentScope = currentScope.parent;
					currentIter = currentScope == null ? null : currentScope.nameVariableMap.entrySet().iterator();
				}
				return currentIter != null && currentIter.hasNext();
			}

			public Entry<String, VariableInfo> next() {
				if (!currentIter.hasNext()) {
					throw new NoSuchElementException();
				}
				return currentIter.next();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Finds all interfaces transitively implemented by the type passed in (including <code>type</code> if it is an interface). The
	 * ordering is that the interfaces closest to type are first (in declared order) and then interfaces declared on super
	 * interfaces occur (if they are not duplicates).
	 * 
	 * @param type the interface to look for
	 * @param allInterfaces an accumulator set that will ensure that each interface exists at most once and in a predictible order
	 * @param useResolved whether or not to use the resolved interfaces.
	 */
	public static void findAllInterfaces(ClassNode type, LinkedHashSet<ClassNode> allInterfaces, boolean useResolved) {
		if (!useResolved) {
			type = type.redirect();
		}
		// do the !isInterface check because if this call is coming from createHierarchy, then
		// the class would have already been added.
		if (!type.isInterface() || !allInterfaces.contains(type)) {
			if (type.isInterface()) {
				allInterfaces.add(type);
			}
			ClassNode[] interfaces;
			// Urrrgh...I don't like this.
			// Groovy compiler has a different notion of 'resolved' than we do here.
			// Groovy compiler considers a resolved ClassNode one that has no redirect.
			// however, we consider a ClassNode to be resolved if its type parameters are resolved.
			// that is why we call getUnresolvedInterfaces if useResolved is true (and vice versa).
			if (useResolved) {
				interfaces = type.getUnresolvedInterfaces();
			} else {
				interfaces = type.getInterfaces();
			}
			if (interfaces != null) {
				for (ClassNode superInterface : interfaces) {
					findAllInterfaces(superInterface, allInterfaces, useResolved);
				}
			}
		}
	}

	/**
	 * Creates a type hierarchy for the <code>clazz</code>>, including self. Classes come first and then interfaces. FIXADE The
	 * ordering of super interfaces will not be the same as in
	 * {@link VariableScope#findAllInterfaces(ClassNode, LinkedHashSet, boolean)}. Should we make it the same?
	 * 
	 * @param type
	 * @param allClasses
	 * @param useResolved
	 */
	public static void createTypeHierarchy(ClassNode type, LinkedHashSet<ClassNode> allClasses, boolean useResolved) {
		if (!useResolved) {
			type = type.redirect();
		}
		if (!allClasses.contains(type)) {
			if (!type.isInterface()) {
				allClasses.add(type);
				ClassNode superClass;
				// Urrrgh...I don't like this.
				// Groovy compiler has a different notion of 'resolved' than we do here.
				// Groovy compiler considers a resolved ClassNode one that has no redirect.
				// however, we consider a ClassNode to be resolved if its type parameters are resolved.
				// that is why we call getUnresolvedSuperClass if useResolved is true (and vice versa).
				if (useResolved) {
					superClass = type.getUnresolvedSuperClass();
				} else {
					superClass = type.getSuperClass();
				}

				if (superClass != null) {
					createTypeHierarchy(superClass, allClasses, useResolved);
				}
			}
			// interfaces will be added from the top-most type first
			findAllInterfaces(type, allClasses, useResolved);
		}
	}

	/**
	 * Extracts an element type from a collection
	 * 
	 * @param collectionType a collection object, or an object that is iterable
	 * @return
	 */
	public static ClassNode extractElementType(ClassNode collectionType) {

		// if array, then use the component type
		if (collectionType.isArray()) {
			return collectionType.getComponentType();
		}

		// check to see if this type has an iterator method
		// if so, then resolve the type parameters
		MethodNode iterator = collectionType.getMethod("iterator", new Parameter[0]);
		ClassNode typeToResolve = null;
		if (iterator == null && collectionType.isInterface()) {
			// could be a type that implements List
			if (collectionType.implementsInterface(VariableScope.LIST_CLASS_NODE) && collectionType.getGenericsTypes() != null
					&& collectionType.getGenericsTypes().length == 1) {
				typeToResolve = collectionType;
			} else if (collectionType.declaresInterface(ITERATOR_CLASS) || collectionType.equals(ITERATOR_CLASS)
					|| collectionType.declaresInterface(ENUMERATION_CLASS) || collectionType.equals(ENUMERATION_CLASS)) {
				// if the type is an iterator or an enumeration, then resolve the type parameter
				typeToResolve = collectionType;
			} else if (collectionType.declaresInterface(MAP_CLASS_NODE) || collectionType.equals(MAP_CLASS_NODE)) {
				// if the type is a map, then resolve the entrySet
				MethodNode entrySetMethod = collectionType.getMethod("entrySet", new Parameter[0]);
				if (entrySetMethod != null) {
					typeToResolve = entrySetMethod.getReturnType();
				}
			}
		} else if (iterator != null) {
			typeToResolve = iterator.getReturnType();
		}

		if (typeToResolve != null) {
			typeToResolve = clone(typeToResolve);
			ClassNode unresolvedCollectionType = collectionType.redirect();
			GenericsMapper mapper = GenericsMapper.gatherGenerics(collectionType, unresolvedCollectionType);
			ClassNode resolved = resolveTypeParameterization(mapper, typeToResolve);

			// the first type parameter of resolvedReturn should be what we want
			GenericsType[] resolvedReturnGenerics = resolved.getGenericsTypes();
			if (resolvedReturnGenerics != null && resolvedReturnGenerics.length > 0) {
				return resolvedReturnGenerics[0].getType();
			}
		}

		// this is hardcoded from DGM
		if (collectionType.declaresInterface(INPUT_STREAM_CLASS) || collectionType.declaresInterface(DATA_INPUT_STREAM_CLASS)
				|| collectionType.equals(INPUT_STREAM_CLASS) || collectionType.equals(DATA_INPUT_STREAM_CLASS)) {
			return BYTE_CLASS_NODE;
		}

		// else assume collection of size 1 (itself)
		return collectionType;
	}

	/**
	 * @return true iff the current scope is the implicit run method of a script
	 */
	public boolean inScriptRunMethod() {
		return shared.isRunMethod;
	}
}
