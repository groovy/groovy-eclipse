/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * Method declaration AST node type. A method declaration
 * is the union of a method declaration and a constructor declaration.
 * 
 * <pre>
 * MethodDeclaration:
 *    [ Javadoc ] { ExtendedModifier } [ <b>&lt;</b> TypeParameter { <b>,</b> TypeParameter } <b>&gt;</b> ] ( Type | <b>void</b> )
 *        Identifier <b>(</b>
 *            [ ReceiverParameter <b>,</b> ] [ FormalParameter { <b>,</b> FormalParameter } ]
 *        <b>)</b> { Dimension }
 *        [ <b>throws</b> Type { <b>,</b> Type } ]
 *        ( Block | <b>;</b> )
 * ConstructorDeclaration:
 *    [ Javadoc ] { ExtendedModifier } [ <b>&lt;</b> TypeParameter { <b>,</b> TypeParameter } <b>&gt;</b> ]
 *        Identifier <b>(</b>
 *            [ ReceiverParameter <b>,</b> ] [ FormalParameter { <b>,</b> FormalParameter } ]
 *        <b>)</b> { Dimension }
 *        [ <b>throws</b> Type { <b>,</b> Type } ]
 *        ( Block | <b>;</b> )
 * </pre>
 * <p>
 * The ReceiverParameter is represented as: <code>Type [ SimpleName <b>.</b> ] <b>this</b></code><br>
 * The FormalParameter is represented by a {@link SingleVariableDeclaration}.
 * </p>
 * <p>
 * When a Javadoc comment is present, the source
 * range begins with the first character of the "/**" comment delimiter.
 * When there is no Javadoc comment, the source range begins with the first
 * character of the first modifier keyword (if modifiers), or the
 * first character of the "&lt;" token (method, no modifiers, type parameters),
 * or the first character of the return type (method, no modifiers, no type
 * parameters), or the first character of the identifier (constructor,
 * no modifiers). The source range extends through the last character of the
 * ";" token (if no body), or the last character of the block (if body).
 * </p>
 *
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MethodDeclaration extends BodyDeclaration {

	/**
	 * The "javadoc" structural property of this node type (child type: {@link Javadoc}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor JAVADOC_PROPERTY =
		internalJavadocPropertyFactory(MethodDeclaration.class);

	/**
	 * The "modifiers" structural property of this node type (type: {@link Integer}) (JLS2 API only).
	 * @since 3.0
	 * @deprecated In the JLS3 API, this property is replaced by {@link #MODIFIERS2_PROPERTY}.
	 */
	public static final SimplePropertyDescriptor MODIFIERS_PROPERTY =
		internalModifiersPropertyFactory(MethodDeclaration.class);

	/**
	 * The "modifiers" structural property of this node type (element type: {@link IExtendedModifier}) (added in JLS3 API).
	 * @since 3.1
	 */
	public static final ChildListPropertyDescriptor MODIFIERS2_PROPERTY =
		internalModifiers2PropertyFactory(MethodDeclaration.class);

	/**
	 * The "constructor" structural property of this node type (type: {@link Boolean}).
	 * @since 3.0
	 */
	public static final SimplePropertyDescriptor CONSTRUCTOR_PROPERTY =
		new SimplePropertyDescriptor(MethodDeclaration.class, "constructor", boolean.class, MANDATORY); //$NON-NLS-1$

	/**
	 * The "name" structural property of this node type (child type: {@link SimpleName}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY =
		new ChildPropertyDescriptor(MethodDeclaration.class, "name", SimpleName.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "returnType" structural property of this node type (child type: {@link Type}) (JLS2 API only).
	 * @since 3.0
	 * @deprecated In the JLS3 API, this property is replaced by {@link #RETURN_TYPE2_PROPERTY}.
	 */
	public static final ChildPropertyDescriptor RETURN_TYPE_PROPERTY =
		new ChildPropertyDescriptor(MethodDeclaration.class, "returnType", Type.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "returnType2" structural property of this node type (child type: {@link Type}) (added in JLS3 API).
	 * @since 3.1
	 */
	public static final ChildPropertyDescriptor RETURN_TYPE2_PROPERTY =
		new ChildPropertyDescriptor(MethodDeclaration.class, "returnType2", Type.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "extraDimensions" structural property of this node type (type: {@link Integer}) (below JLS8 only).
	 *
	 * @since 3.0
	 * @deprecated In JLS8 and later, use {@link MethodDeclaration#EXTRA_DIMENSIONS2_PROPERTY} instead.
	 */
	public static final SimplePropertyDescriptor EXTRA_DIMENSIONS_PROPERTY =
		new SimplePropertyDescriptor(MethodDeclaration.class, "extraDimensions", int.class, MANDATORY); //$NON-NLS-1$
	
	/**
	 * The "extraDimensions2" structural property of this node type (element type: {@link Dimension}) (added in JLS8 API).
	 * @since 3.10
	 */
	public static final ChildListPropertyDescriptor EXTRA_DIMENSIONS2_PROPERTY =
			new ChildListPropertyDescriptor(MethodDeclaration.class, "extraDimensions2", Dimension.class, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "typeParameters" structural property of this node type (element type: {@link TypeParameter}) (added in JLS3 API).
	 * @since 3.1
	 */
	public static final ChildListPropertyDescriptor TYPE_PARAMETERS_PROPERTY =
		new ChildListPropertyDescriptor(MethodDeclaration.class, "typeParameters", TypeParameter.class, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "parameters" structural property of this node type (element type: {@link SingleVariableDeclaration}).
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor PARAMETERS_PROPERTY =
		new ChildListPropertyDescriptor(MethodDeclaration.class, "parameters", SingleVariableDeclaration.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "receiverType" structural property of this node type (child type: {@link Type}) (added in JLS8 API).
	 * @since 3.10
	 */
	public static final ChildPropertyDescriptor RECEIVER_TYPE_PROPERTY =
			new ChildPropertyDescriptor(MethodDeclaration.class, "receiverType", Type.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * The "receiverQualifier" structural property of this node type (child type: {@link SimpleName}) (added in JLS8 API).
	 * @since 3.10
	 */
	public static final ChildPropertyDescriptor RECEIVER_QUALIFIER_PROPERTY =
			new ChildPropertyDescriptor(MethodDeclaration.class, "receiverQualifier", SimpleName.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "thrownExceptions" structural property of this node type (element type: {@link Name}) (before JLS8 only).
	 * @deprecated In JLS8 and later, use {@link MethodDeclaration#THROWN_EXCEPTION_TYPES_PROPERTY} instead.
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor THROWN_EXCEPTIONS_PROPERTY =
		new ChildListPropertyDescriptor(MethodDeclaration.class, "thrownExceptions", Name.class, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "thrownExceptionTypes" structural property of this node type (element type: {@link Type}) (added in JLS8 API).
	 * @since 3.10
	 */
	public static final ChildListPropertyDescriptor THROWN_EXCEPTION_TYPES_PROPERTY =
		new ChildListPropertyDescriptor(MethodDeclaration.class, "thrownExceptionTypes", Type.class, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "body" structural property of this node type (child type: {@link Block}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor BODY_PROPERTY =
		new ChildPropertyDescriptor(MethodDeclaration.class, "body", Block.class, OPTIONAL, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.0
	 */
	private static final List PROPERTY_DESCRIPTORS_2_0;

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.1
	 */
	private static final List PROPERTY_DESCRIPTORS_3_0;

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.10
	 */
	private static final List PROPERTY_DESCRIPTORS_8_0;

	static {
		List propertyList = new ArrayList(10);
		createPropertyList(MethodDeclaration.class, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(MODIFIERS_PROPERTY, propertyList);
		addProperty(CONSTRUCTOR_PROPERTY, propertyList);
		addProperty(RETURN_TYPE_PROPERTY, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		addProperty(PARAMETERS_PROPERTY, propertyList);
		addProperty(EXTRA_DIMENSIONS_PROPERTY, propertyList);
		addProperty(THROWN_EXCEPTIONS_PROPERTY, propertyList);
		addProperty(BODY_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(propertyList);

		propertyList = new ArrayList(11);
		createPropertyList(MethodDeclaration.class, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(MODIFIERS2_PROPERTY, propertyList);
		addProperty(CONSTRUCTOR_PROPERTY, propertyList);
		addProperty(TYPE_PARAMETERS_PROPERTY, propertyList);
		addProperty(RETURN_TYPE2_PROPERTY, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		addProperty(PARAMETERS_PROPERTY, propertyList);
		addProperty(EXTRA_DIMENSIONS_PROPERTY, propertyList);
		addProperty(THROWN_EXCEPTIONS_PROPERTY, propertyList);
		addProperty(BODY_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList(propertyList);

		propertyList = new ArrayList(13);
		createPropertyList(MethodDeclaration.class, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(MODIFIERS2_PROPERTY, propertyList);
		addProperty(CONSTRUCTOR_PROPERTY, propertyList);
		addProperty(TYPE_PARAMETERS_PROPERTY, propertyList);
		addProperty(RETURN_TYPE2_PROPERTY, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		addProperty(RECEIVER_TYPE_PROPERTY, propertyList);
		addProperty(RECEIVER_QUALIFIER_PROPERTY, propertyList);
		addProperty(PARAMETERS_PROPERTY, propertyList);
		addProperty(EXTRA_DIMENSIONS2_PROPERTY, propertyList);
		addProperty(THROWN_EXCEPTION_TYPES_PROPERTY, propertyList);
		addProperty(BODY_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_8_0 = reapPropertyList(propertyList);	
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the AST.JLS* constants
	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		if (apiLevel == AST.JLS2_INTERNAL) {
			return PROPERTY_DESCRIPTORS_2_0;
		} else if (apiLevel < AST.JLS8_INTERNAL) {
			return PROPERTY_DESCRIPTORS_3_0;
		} else {
			return PROPERTY_DESCRIPTORS_8_0;
		}
	}

	/**
	 * <code>true</code> for a constructor, <code>false</code> for a method.
	 * Defaults to method.
	 */
	private boolean isConstructor = false;

	/**
	 * The method name; lazily initialized; defaults to an unspecified,
	 * legal Java identifier.
	 */
	private SimpleName methodName = null;

	/**
	 * The explicit receiver type, or <code>null</code> if none.
	 * Defaults to none.
	 * @since 3.10
	 */
	private Type optionalReceiverType = null;
	
	/**
	 * Qualifying name of the explicit </code>this</code> parameter, or <code>null</code> if none.
	 * Defaults to none.
	 * @since 3.10
	 */
	private SimpleName optionalReceiverQualifier = null;

	/**
	 * The parameter declarations
	 * (element type: {@link SingleVariableDeclaration}).
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList parameters =
		new ASTNode.NodeList(PARAMETERS_PROPERTY);

	/**
	 * The return type.
	 * JLS2 behavior: lazily initialized; defaults to void.
	 * JLS3 and later: lazily initialized; defaults to void; null allowed.
	 * Note that this field is ignored for constructor declarations.
	 */
	private Type returnType = null;

	/**
	 * Indicated whether the return type has been initialized.
	 * @since 3.1
	 */
	private boolean returnType2Initialized = false;

	/**
	 * The type paramters (element type: {@link TypeParameter}).
	 * Null in JLS2. Added in JLS3; defaults to an empty list
	 * (see constructor).
	 * @since 3.1
	 */
	private ASTNode.NodeList typeParameters = null;

	/**
	 * The number of array dimensions that appear after the parameters, rather
	 * than after the return type itself; defaults to 0. Not used in JLS8 and later.
	 *
	 * @since 2.1
	 * @deprecated In JLS8 and later, use {@link #extraDimensions} instead.
	 */
	private int extraArrayDimensions = 0;

	/**
	 * List of extra dimensions this node has with optional annotations
	 * (element type: {@link Dimension}).
	 * Null before JLS8. Added in JLS8; defaults to an empty list
	 * (see constructor).
	 * 
	 * @since 3.10
	 */
	private ASTNode.NodeList extraDimensions = null;

	/**
	 * The list of thrown exception names (element type: {@link Name}).
	 * Before JLS8: defaults to an empty list (see constructor).
	 * JLS8 and later: null.
	 * @deprecated In JLS8 and later, use {@link #thrownExceptionTypes} instead.
	 */
	private ASTNode.NodeList thrownExceptions = null;

	/**
	 * The list of thrown exception Types (element type: {@link Type}).
	 * Null before JLS8. Added in JLS8; defaults to an empty list
	 * (see constructor).
	 * 
	 * @since 3.10
	 */
	private ASTNode.NodeList thrownExceptionTypes = null;

	/**
	 * The method body, or <code>null</code> if none.
	 * Defaults to none.
	 */
	private Block optionalBody = null;

	/**
	 * Creates a new AST node for a method declaration owned
	 * by the given AST. By default, the declaration is for a method of an
	 * unspecified, but legal, name; no modifiers; no javadoc; no type
	 * parameters; void return type; no parameters; no array dimensions after
	 * the parameters; no thrown exceptions; and no body (as opposed to an
	 * empty body).
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be
	 * declared in the same package; clients are unable to declare
	 * additional subclasses.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	MethodDeclaration(AST ast) {
		super(ast);
		if (ast.apiLevel >= AST.JLS3_INTERNAL) {
			this.typeParameters = new ASTNode.NodeList(TYPE_PARAMETERS_PROPERTY);
		}
		if (ast.apiLevel < AST.JLS8_INTERNAL) {
			this.thrownExceptions = new ASTNode.NodeList(THROWN_EXCEPTIONS_PROPERTY);
		} else {
			this.extraDimensions = new ASTNode.NodeList(EXTRA_DIMENSIONS2_PROPERTY);
			this.thrownExceptionTypes = new ASTNode.NodeList(THROWN_EXCEPTION_TYPES_PROPERTY);
		}
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 * @since 3.0
	 */
	@Override
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	@Override
	final int internalGetSetIntProperty(SimplePropertyDescriptor property, boolean get, int value) {
		if (property == MODIFIERS_PROPERTY) {
			if (get) {
				return getModifiers();
			} else {
				internalSetModifiers(value);
				return 0;
			}
		}
		if (property == EXTRA_DIMENSIONS_PROPERTY) {
			if (get) {
				return getExtraDimensions();
			} else {
				setExtraDimensions(value);
				return 0;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetIntProperty(property, get, value);
	}

	@Override
	final boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean value) {
		if (property == CONSTRUCTOR_PROPERTY) {
			if (get) {
				return isConstructor();
			} else {
				setConstructor(value);
				return false;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetBooleanProperty(property, get, value);
	}

	@Override
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == JAVADOC_PROPERTY) {
			if (get) {
				return getJavadoc();
			} else {
				setJavadoc((Javadoc) child);
				return null;
			}
		}
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((SimpleName) child);
				return null;
			}
		}
		if (property == RETURN_TYPE_PROPERTY) {
			if (get) {
				return getReturnType();
			} else {
				setReturnType((Type) child);
				return null;
			}
		}
		if (property == RETURN_TYPE2_PROPERTY) {
			if (get) {
				return getReturnType2();
			} else {
				setReturnType2((Type) child);
				return null;
			}
		}
		if (property == RECEIVER_TYPE_PROPERTY) {
			if (get) {
				return getReceiverType();
			} else {
				setReceiverType((Type) child);
				return null;
			}
		}
		if (property == RECEIVER_QUALIFIER_PROPERTY) {
			if (get) {
				return getReceiverQualifier();
			} else {
				setReceiverQualifier((SimpleName) child);
				return null;
			}
		}
		if (property == BODY_PROPERTY) {
			if (get) {
				return getBody();
			} else {
				setBody((Block) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	@Override
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == MODIFIERS2_PROPERTY) {
			return modifiers();
		}
		if (property == TYPE_PARAMETERS_PROPERTY) {
			return typeParameters();
		}
		if (property == PARAMETERS_PROPERTY) {
			return parameters();
		}
		if (property == THROWN_EXCEPTIONS_PROPERTY) {
			return thrownExceptions();
		}
		if (property == THROWN_EXCEPTION_TYPES_PROPERTY) {
			return thrownExceptionTypes();
		}		
		if (property == EXTRA_DIMENSIONS2_PROPERTY) {
			return extraDimensions();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	@Override
	final ChildPropertyDescriptor internalJavadocProperty() {
		return JAVADOC_PROPERTY;
	}

	@Override
	final ChildListPropertyDescriptor internalModifiers2Property() {
		return MODIFIERS2_PROPERTY;
	}

	@Override
	final SimplePropertyDescriptor internalModifiersProperty() {
		return MODIFIERS_PROPERTY;
	}

	@Override
	final int getNodeType0() {
		return METHOD_DECLARATION;
	}

	@Override
	ASTNode clone0(AST target) {
		MethodDeclaration result = new MethodDeclaration(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
			result.internalSetModifiers(getModifiers());
			result.setReturnType(
					(Type) ASTNode.copySubtree(target, getReturnType()));
		}
		if (this.ast.apiLevel >= AST.JLS3_INTERNAL) {
			result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
			result.typeParameters().addAll(
					ASTNode.copySubtrees(target, typeParameters()));
			result.setReturnType2(
					(Type) ASTNode.copySubtree(target, getReturnType2()));
		}
		result.setConstructor(isConstructor());
		result.setName((SimpleName) getName().clone(target));
		if (this.ast.apiLevel >= AST.JLS8_INTERNAL) {
			result.setReceiverType((Type) ASTNode.copySubtree(target, getReceiverType()));
			result.setReceiverQualifier((SimpleName) ASTNode.copySubtree(target, getReceiverQualifier()));
		}
		result.parameters().addAll(
			ASTNode.copySubtrees(target, parameters()));
		if (this.ast.apiLevel >= AST.JLS8_INTERNAL) {
			result.extraDimensions().addAll(ASTNode.copySubtrees(target, extraDimensions()));
		} else {
			result.setExtraDimensions(getExtraDimensions());
		}
		if (this.ast.apiLevel() >= AST.JLS8_INTERNAL) {
			result.thrownExceptionTypes().addAll(ASTNode.copySubtrees(target, thrownExceptionTypes()));
		} else {
			result.thrownExceptions().addAll(ASTNode.copySubtrees(target, thrownExceptions()));			
		}
		result.setBody(
			(Block) ASTNode.copySubtree(target, getBody()));
		return result;
	}

	@Override
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	@Override
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			acceptChild(visitor, getJavadoc());
			if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
				acceptChild(visitor, getReturnType());
			} else {
				acceptChildren(visitor, this.modifiers);
				acceptChildren(visitor, this.typeParameters);
				acceptChild(visitor, getReturnType2());
			}
			// n.b. visit return type even for constructors
			acceptChild(visitor, getName());
			if (this.ast.apiLevel >= AST.JLS8_INTERNAL) {
				acceptChild(visitor, this.optionalReceiverType);
				acceptChild(visitor, this.optionalReceiverQualifier);
			}
			acceptChildren(visitor, this.parameters);
			if (this.ast.apiLevel() >= AST.JLS8_INTERNAL) {
				acceptChildren(visitor, this.extraDimensions);
				acceptChildren(visitor, this.thrownExceptionTypes);				
			} else {
				acceptChildren(visitor, this.thrownExceptions);				
			}
			acceptChild(visitor, getBody());
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns whether this declaration declares a constructor or a method.
	 *
	 * @return <code>true</code> if this is a constructor declaration,
	 *    and <code>false</code> if this is a method declaration
	 */
	public boolean isConstructor() {
		return this.isConstructor;
	}

	/**
	 * Sets whether this declaration declares a constructor or a method.
	 *
	 * @param isConstructor <code>true</code> for a constructor declaration,
	 *    and <code>false</code> for a method declaration
	 */
	public void setConstructor(boolean isConstructor) {
		preValueChange(CONSTRUCTOR_PROPERTY);
		this.isConstructor = isConstructor;
		postValueChange(CONSTRUCTOR_PROPERTY);
	}

	/**
	 * Returns the live ordered list of type parameters of this method
	 * declaration (added in JLS3 API). This list is non-empty for parameterized methods.
	 *
	 * @return the live list of type parameters
	 *    (element type: {@link TypeParameter})
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public List typeParameters() {
		// more efficient than just calling unsupportedIn2() to check
		if (this.typeParameters == null) {
			unsupportedIn2();
		}
		return this.typeParameters;
	}

	/**
	 * Returns the name of the method declared in this method declaration.
	 * For a constructor declaration, this should be the same as the name
	 * of the class.
	 *
	 * @return the method name node
	 */
	public SimpleName getName() {
		if (this.methodName == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.methodName == null) {
					preLazyInit();
					this.methodName = new SimpleName(this.ast);
					postLazyInit(this.methodName, NAME_PROPERTY);
				}
			}
		}
		return this.methodName;
	}

	/**
	 * Sets the name of the method declared in this method declaration to the
	 * given name. For a constructor declaration, this should be the same as
	 * the name of the class.
	 *
	 * @param methodName the new method name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public void setName(SimpleName methodName) {
		if (methodName == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.methodName;
		preReplaceChild(oldChild, methodName, NAME_PROPERTY);
		this.methodName = methodName;
		postReplaceChild(oldChild, methodName, NAME_PROPERTY);
	}

	/**
	 * Returns the receiver type explicitly declared in the method or constructor 
	 * declaration (added in JLS8 API).
	 *
	 * If the receiver is not explicitly declared in the method or constructor 
	 * declaration, <code>null</code> is returned.
	 *
	 * @return the receiver type or <code>null</code> if receiver is not declared explicitly
	 * @exception UnsupportedOperationException if this operation is used below JLS8
	 * @since 3.10
	 */
	public Type getReceiverType() {
		unsupportedIn2_3_4();
		return this.optionalReceiverType;
	}

	/**
	 * Sets or clears the given type as the type of explicit receiver parameter (added in JLS8 API).
	 * <p>
	 * A receiver type is only legal in Java code if it appears on an instance method or on a constructor of an inner class.
	 * </p>
	 * 
	 * @param receiverType type of the explicit receiver parameter, or <code>null</code> if there is none
	 * @exception UnsupportedOperationException if this operation is used below JLS8
	 * @since 3.10
	 */
	public void setReceiverType(Type receiverType) {
		unsupportedIn2_3_4();
		ASTNode oldChild = this.optionalReceiverType;
		preReplaceChild(oldChild, receiverType, RECEIVER_TYPE_PROPERTY);
		this.optionalReceiverType = receiverType;
		postReplaceChild(oldChild, receiverType, RECEIVER_TYPE_PROPERTY);
	}

	/**
	 * Returns the qualifying name, if any, for the explicit receiver or <code>null</code> if not used (added in JLS8 API).
	 * <p>
	 * A receiver qualifier is only legal in Java code if it appears on a constructor of an inner class.
	 * </p>
	 * 
	 * @return the qualifying name or <code>null</code> if a qualifier was not specified
	 * @exception UnsupportedOperationException if this operation is used below JLS8
	 * @since 3.10
	 */
	public SimpleName getReceiverQualifier() {
		unsupportedIn2_3_4();
		return this.optionalReceiverQualifier;
	}
	
	/**
	 * Sets the given simple name as the qualifier for the receiver (added in JLS8 API).
	 * 
	 * @param receiverQualifier explicit receiver parameter to be added to the method declaration
	 * @exception UnsupportedOperationException if this operation is used below JLS8
	 * @since 3.10
	 */
	public void setReceiverQualifier(SimpleName receiverQualifier) {
		unsupportedIn2_3_4();
		ASTNode oldChild = this.optionalReceiverQualifier;
		preReplaceChild(oldChild, receiverQualifier, RECEIVER_QUALIFIER_PROPERTY);
		this.optionalReceiverQualifier = receiverQualifier;
		postReplaceChild(oldChild, receiverQualifier, RECEIVER_QUALIFIER_PROPERTY);
	}
	
	/**
	 * Returns the live ordered list of method parameter declarations for this
	 * method declaration.
	 *
	 * @return the live list of method parameter declarations
	 *    (element type: {@link SingleVariableDeclaration})
	 */
	public List parameters() {
		return this.parameters;
	}

	/**
	 * Returns whether this method declaration declares a
	 * variable arity method (added in JLS3 API). The convenience method checks
	 * whether the last parameter is so marked.
	 *
	 * @return <code>true</code> if this is a variable arity method declaration,
	 *    and <code>false</code> otherwise
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @see SingleVariableDeclaration#isVarargs()
	 * @since 3.1
	 */
	public boolean isVarargs() {
		// more efficient than just calling unsupportedIn2() to check
		if (this.modifiers == null) {
			unsupportedIn2();
		}
		if (parameters().isEmpty()) {
			return false;
		} else {
			SingleVariableDeclaration v = (SingleVariableDeclaration) parameters().get(parameters().size() - 1);
			return v.isVarargs();
		}
	}

	/**
	 * Returns the live ordered list of thrown exception names in this method
	 * declaration (below JLS8 API only).
	 *
	 * @return the live list of exception names
	 *    (element type: {@link Name})
	 * @exception UnsupportedOperationException if this operation is used in
	 *    a JLS8 or later AST
	 * @deprecated In the JLS8 API, this method is replaced by {@link #thrownExceptionTypes()}.
	 */
	public List thrownExceptions() {
		return internalThrownExceptions();
	}

	/**
	 * Internal synonym for deprecated method. Used to avoid
	 * deprecation warnings.
	 * @exception UnsupportedOperationException if this operation is used in
	 *    a JLS8 or later AST
	 * @since 3.10
	 */
	/*package*/	List internalThrownExceptions() {
		// more efficient than just calling supportedOnlyIn2_3_4() to check
		if (this.thrownExceptions == null) {
			supportedOnlyIn2_3_4();
		}
		return this.thrownExceptions;
	}

	/**
	 * Returns the live ordered list of thrown exception types in this method
	 * declaration.
	 *
	 * @return the live list of exception types
	 *    (element type: {@link Type})
	 * @exception UnsupportedOperationException if this operation is used
	 *            in a JLS2, JLS3 or JLS4 AST    
	 * @since 3.10
	 */
	public List thrownExceptionTypes()  {
		if (this.thrownExceptionTypes == null) {
			unsupportedIn2_3_4();
		}
		return this.thrownExceptionTypes;
	}

	/**
	 * Returns the return type of the method declared in this method
	 * declaration, exclusive of any extra array dimensions (JLS2 API only).
	 * This is one of the few places where the void type is meaningful.
	 * <p>
	 * Note that this child is not relevant for constructor declarations
	 * (although, it does still figure in subtree equality comparisons
	 * and visits), and is devoid of the binding information ordinarily
	 * available.
	 * </p>
	 *
	 * @return the return type, possibly the void primitive type
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than JLS2
	 * @deprecated In the JLS3 API, this method is replaced by {@link #getReturnType2()},
	 * which may return <code>null</code>.
	 */
	public Type getReturnType() {
		return internalGetReturnType();
	}

	/**
	 * Internal synonym for deprecated method. Used to avoid
	 * deprecation warnings.
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than JLS2
	 * @since 3.1
	 */
	/*package*/ final Type internalGetReturnType() {
		supportedOnlyIn2();
		if (this.returnType == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.returnType == null) {
					preLazyInit();
					this.returnType = this.ast.newPrimitiveType(PrimitiveType.VOID);
					postLazyInit(this.returnType, RETURN_TYPE_PROPERTY);
				}
			}
		}
		return this.returnType;
	}

	/**
	 * Sets the return type of the method declared in this method declaration
	 * to the given type, exclusive of any extra array dimensions (JLS2 API only). This is one
	 * of the few places where the void type is meaningful.
	 * <p>
	 * Note that this child is not relevant for constructor declarations
	 * (although it does still figure in subtree equality comparisons and visits).
	 * </p>
	 *
	 * @param type the new return type, possibly the void primitive type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than JLS2
	 * @deprecated In the JLS3 API, this method is replaced by
	 * {@link #setReturnType2(Type)}, which accepts <code>null</code>.
	 */
	public void setReturnType(Type type) {
		internalSetReturnType(type);
	}

	/**
	 * Internal synonym for deprecated method. Used to avoid
	 * deprecation warnings.
	 * @since 3.1
	 */
	/*package*/ void internalSetReturnType(Type type) {
	    supportedOnlyIn2();
		if (type == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.returnType;
		preReplaceChild(oldChild, type, RETURN_TYPE_PROPERTY);
		this.returnType = type;
		postReplaceChild(oldChild, type, RETURN_TYPE_PROPERTY);
	}

	/**
	 * Returns the return type of the method declared in this method
	 * declaration, exclusive of any extra array dimensions (added in JLS3 API).
	 * This is one of the few places where the void type is meaningful.
	 * <p>
	 * Note that this child is not relevant for constructor declarations
	 * (although, if present, it does still figure in subtree equality comparisons
	 * and visits), and is devoid of the binding information ordinarily
	 * available. In the JLS2 API, the return type is mandatory.
	 * In the JLS3 API, the return type is optional.
	 * </p>
	 *
	 * @return the return type, possibly the void primitive type,
	 * or <code>null</code> if none
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */
	public Type getReturnType2() {
	    unsupportedIn2();
		if (this.returnType == null && !this.returnType2Initialized) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.returnType == null && !this.returnType2Initialized) {
					preLazyInit();
					this.returnType = this.ast.newPrimitiveType(PrimitiveType.VOID);
					this.returnType2Initialized = true;
					postLazyInit(this.returnType, RETURN_TYPE2_PROPERTY);
				}
			}
		}
		return this.returnType;
	}

	/**
	 * Sets the return type of the method declared in this method declaration
	 * to the given type, exclusive of any extra array dimensions (added in JLS3 API).
	 * This is one of the few places where the void type is meaningful.
	 * <p>
	 * Note that this child is not relevant for constructor declarations
	 * (although it does still figure in subtree equality comparisons and visits).
	 * In the JLS2 API, the return type is mandatory.
	 * In the JLS3 API, the return type is optional.
	 * </p>
	 *
	 * @param type the new return type, possibly the void primitive type,
	 * or <code>null</code> if none
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 * @since 3.1
	 */
	public void setReturnType2(Type type) {
	    unsupportedIn2();
		this.returnType2Initialized = true;
		ASTNode oldChild = this.returnType;
		preReplaceChild(oldChild, type, RETURN_TYPE2_PROPERTY);
		this.returnType = type;
		postReplaceChild(oldChild, type, RETURN_TYPE2_PROPERTY);
	}

	/**
	 * Returns the number of extra array dimensions over and above the
	 * explicitly-specified return type.
	 * <p>
	 * For example, <code>int foo()[][]</code> has a return type of
	 * <code>int</code> and two extra array dimensions;
	 * <code>int[][] foo()</code> has a return type of <code>int[][]</code>
	 * and zero extra array dimensions. The two constructs have different
	 * ASTs, even though there are really syntactic variants of the same
	 * method declaration.
	 * </p>
	 * <p>
	 * In the JLS8 API, this method is a convenience method that
	 * counts {@link #extraDimensions()}.
	 * </p>
	 *
	 * @return the number of extra array dimensions
	 * @since 2.1
	 */
	public int getExtraDimensions() {
		// more efficient than checking getAST().API_LEVEL
		if (this.extraDimensions == null) {
			// JLS2,3,4 behavior - bona fide property
			return this.extraArrayDimensions;
		} else {
			return this.extraDimensions.size();
		}
	}

	/**
	 * Sets the number of extra array dimensions over and above the
	 * explicitly-specified return type.
	 * <p>
	 * For example, <code>int foo()[][]</code> is rendered as a return
	 * type of <code>int</code> with two extra array dimensions;
	 * <code>int[][] foo()</code> is rendered as a return type of
	 * <code>int[][]</code> with zero extra array dimensions. The two
	 * constructs have different ASTs, even though there are really syntactic
	 * variants of the same method declaration.
	 * </p>
	 *
	 * @param dimensions the number of array dimensions
	 * @exception IllegalArgumentException if the number of dimensions is
	 *    negative
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS8 or later AST 
	 * @since 2.1
	 * @deprecated In the JLS8 API, this method is replaced by
	 * {@link #extraDimensions()} which contains a list of {@link Dimension} nodes.
	 */
	public void setExtraDimensions(int dimensions) {
		// more efficient than just calling supportedOnlyIn2_3_4() to check
		if (this.extraDimensions != null) {
			supportedOnlyIn2_3_4();
		}
		if (dimensions < 0) {
			throw new IllegalArgumentException();
		}
		preValueChange(EXTRA_DIMENSIONS_PROPERTY);
		this.extraArrayDimensions = dimensions;
		postValueChange(EXTRA_DIMENSIONS_PROPERTY);
	}

	/**
	 * Returns the live ordered list of extra dimensions with optional annotations (added in JLS8 API).
	 * 
	 * @return the live list of extra dimensions with optional annotations (element type: {@link Dimension})
	 * @exception UnsupportedOperationException if this operation is used below JLS8
	 * @since 3.10
	 */
	public List extraDimensions() {
		// more efficient than just calling unsupportedIn2_3_4() to check
		if (this.extraDimensions == null) {
			unsupportedIn2_3_4();
		}
		return this.extraDimensions;
	}

	/**
	 * Returns the body of this method declaration, or <code>null</code> if
	 * this method has <b>no</b> body.
	 * <p>
	 * Note that there is a subtle difference between having no body and having
	 * an empty body ("{}").
	 * </p>
	 *
	 * @return the method body, or <code>null</code> if this method has no
	 *    body
	 */
	public Block getBody() {
		return this.optionalBody;
	}

	/**
	 * Sets or clears the body of this method declaration.
	 * <p>
	 * Note that there is a subtle difference between having no body
	 * (as in <code>"void foo();"</code>) and having an empty body (as in
	 * "void foo() {}"). Abstract methods, and methods declared in interfaces,
	 * have no body. Non-abstract methods, and all constructors, have a body.
	 * </p>
	 *
	 * @param body the block node, or <code>null</code> if
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public void setBody(Block body) {
		// a MethodDeclaration may occur in a Block - must check cycles
		ASTNode oldChild = this.optionalBody;
		preReplaceChild(oldChild, body, BODY_PROPERTY);
		this.optionalBody = body;
		postReplaceChild(oldChild, body, BODY_PROPERTY);
	}

	/**
	 * Resolves and returns the binding for the method or constructor declared
	 * in this method or constructor declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 *
	 * @return the binding, or <code>null</code> if the binding cannot be
	 *    resolved
	 */
	public IMethodBinding resolveBinding() {
		return this.ast.getBindingResolver().resolveMethod(this);
	}

	@Override
	int memSize() {
		return super.memSize() + 13 * 4;
	}

	@Override
	int treeSize() {
		return
			memSize()
			+ (this.optionalDocComment == null ? 0 : getJavadoc().treeSize())
			+ (this.modifiers == null ? 0 : this.modifiers.listSize())
			+ (this.typeParameters == null ? 0 : this.typeParameters.listSize())
			+ (this.methodName == null ? 0 : getName().treeSize())
			+ (this.optionalReceiverType == null ? 0 : this.optionalReceiverType.treeSize())
			+ (this.optionalReceiverQualifier == null ? 0 : this.optionalReceiverQualifier.treeSize())
			+ (this.returnType == null ? 0 : this.returnType.treeSize())
			+ this.parameters.listSize()
			+ (this.ast.apiLevel < AST.JLS8_INTERNAL
					? this.thrownExceptions.listSize()
					: this.extraDimensions.listSize() + this.thrownExceptionTypes.listSize())
			+ (this.optionalBody == null ? 0 : getBody().treeSize());
	}
}

