/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * AST node for a simple name. A simple name is an identifier other than
 * a keyword, boolean literal ("true", "false") or null literal ("null").
 * <pre>
 * SimpleName:
 *     Identifier
 * </pre>
 *
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings("rawtypes")
public class SimpleName extends Name {

	/**
	 * The "identifier" structural property of this node type (type: {@link String}).
	 *
	 * @since 3.0
	 */
	public static final SimplePropertyDescriptor IDENTIFIER_PROPERTY =
		new SimplePropertyDescriptor(SimpleName.class, "identifier", String.class, MANDATORY); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.0
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List propertyList = new ArrayList(2);
		createPropertyList(SimpleName.class, propertyList);
		addProperty(IDENTIFIER_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
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
		return PROPERTY_DESCRIPTORS;
	}

	/**
	 * An unspecified (but externally observable) legal Java identifier.
	 */
	private static final String MISSING_IDENTIFIER = "MISSING";//$NON-NLS-1$

	/**
	 * The identifier; defaults to a unspecified, legal Java identifier.
	 */
	private String identifier = MISSING_IDENTIFIER;

	/**
	 * Creates a new AST node for a simple name owned by the given AST.
	 * The new node has an unspecified, legal Java identifier.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be
	 * declared in the same package; clients are unable to declare
	 * additional subclasses.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	SimpleName(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 * @since 3.0
	 */
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final Object internalGetSetObjectProperty(SimplePropertyDescriptor property, boolean get, Object value) {
		if (property == IDENTIFIER_PROPERTY) {
			if (get) {
				return getIdentifier();
			} else {
				setIdentifier((String) value);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetObjectProperty(property, get, value);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final int getNodeType0() {
		return SIMPLE_NAME;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		SimpleName result = new SimpleName(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setIdentifier(getIdentifier());
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	/**
	 * Returns this node's identifier.
	 *
	 * @return the identifier of this node
	 */
	public String getIdentifier() {
		return this.identifier;
	}

	/**
	 * Sets the identifier of this node to the given value.
	 * The identifier should be legal according to the rules
	 * of the Java language. Note that keywords are not legal
	 * identifiers.
	 * <p>
	 * Note that the list of keywords may depend on the version of the
	 * language (determined when the AST object was created).
	 * </p>
	 *
	 * @param identifier the identifier of this node
	 * @exception IllegalArgumentException if the identifier is invalid
	 */
	public void setIdentifier(String identifier) {
		// update internalSetIdentifier if this is changed
		if (identifier == null) {
			throw new IllegalArgumentException();
		}
		Scanner scanner = this.ast.scanner;
		long sourceLevel = scanner.sourceLevel;
		long complianceLevel = scanner.complianceLevel;

		try {
			scanner.sourceLevel = ClassFileConstants.JDK1_3;
			scanner.complianceLevel = ClassFileConstants.JDK1_5;
			char[] source = identifier.toCharArray();
			scanner.setSource(source);
			final int length = source.length;
			scanner.resetTo(0, length - 1);
			try {
				int tokenType = scanner.scanIdentifier();
				if (tokenType != TerminalTokens.TokenNameIdentifier) {
					throw new IllegalArgumentException("Invalid identifier : >" + identifier + "<");  //$NON-NLS-1$//$NON-NLS-2$
				}
				if (scanner.currentPosition != length) {
					// this is the case when there is only one identifier see 87849
					throw new IllegalArgumentException("Invalid identifier : >" + identifier + "<");  //$NON-NLS-1$//$NON-NLS-2$
				}
			} catch (InvalidInputException e) {
				IllegalArgumentException iae = new IllegalArgumentException("Invalid identifier : >" + identifier + "<"); //$NON-NLS-1$//$NON-NLS-2$
				iae.initCause(e);
				throw iae; 
			}
		} finally {
			this.ast.scanner.sourceLevel = sourceLevel;
			this.ast.scanner.complianceLevel = complianceLevel;
		}
		preValueChange(IDENTIFIER_PROPERTY);
		this.identifier = identifier;
		postValueChange(IDENTIFIER_PROPERTY);
	}

	/* (omit javadoc for this method)
	 * This method is a copy of setIdentifier(String) that doesn't do any validation.
	 */
	void internalSetIdentifier(String ident) {
		preValueChange(IDENTIFIER_PROPERTY);
		this.identifier = ident;
		postValueChange(IDENTIFIER_PROPERTY);
	}

	/**
	 * Returns whether this simple name represents a name that is being defined,
	 * as opposed to one being referenced. The following positions are considered
	 * ones where a name is defined:
	 * <ul>
	 * <li>The type name in a <code>TypeDeclaration</code> node.</li>
	 * <li>The method name in a <code>MethodDeclaration</code> node
	 * providing <code>isConstructor</code> is <code>false</code>.</li>
	 * <li>The variable name in any type of <code>VariableDeclaration</code>
	 * node.</li>
	 * <li>The enum type name in a <code>EnumDeclaration</code> node.</li>
	 * <li>The enum constant name in an <code>EnumConstantDeclaration</code>
	 * node.</li>
	 * <li>The variable name in an <code>EnhancedForStatement</code>
	 * node.</li>
	 * <li>The type variable name in a <code>TypeParameter</code>
	 * node.</li>
	 * <li>The type name in an <code>AnnotationTypeDeclaration</code> node.</li>
	 * <li>The member name in an <code>AnnotationTypeMemberDeclaration</code> node.</li>
	 * </ul>
	 * <p>
	 * Note that this is a convenience method that simply checks whether
	 * this node appears in the declaration position relative to its parent.
	 * It always returns <code>false</code> if this node is unparented.
	 * </p>
	 *
	 * @return <code>true</code> if this node declares a name, and
	 *    <code>false</code> otherwise
	 */
	public boolean isDeclaration() {
		StructuralPropertyDescriptor d = getLocationInParent();
		if (d == null) {
			// unparented node
			return false;
		}
		ASTNode parent = getParent();
		if (parent instanceof TypeDeclaration) {
			return (d == TypeDeclaration.NAME_PROPERTY);
		}
		if (parent instanceof MethodDeclaration) {
			MethodDeclaration p = (MethodDeclaration) parent;
			// could be the name of the method or constructor
			return !p.isConstructor() && (d == MethodDeclaration.NAME_PROPERTY);
		}
		if (parent instanceof SingleVariableDeclaration) {
			return (d == SingleVariableDeclaration.NAME_PROPERTY);
		}
		if (parent instanceof VariableDeclarationFragment) {
			return (d == VariableDeclarationFragment.NAME_PROPERTY);
		}
		if (parent instanceof EnumDeclaration) {
			return (d == EnumDeclaration.NAME_PROPERTY);
		}
		if (parent instanceof EnumConstantDeclaration) {
			return (d == EnumConstantDeclaration.NAME_PROPERTY);
		}
		if (parent instanceof TypeParameter) {
			return (d == TypeParameter.NAME_PROPERTY);
		}
		if (parent instanceof AnnotationTypeDeclaration) {
			return (d == AnnotationTypeDeclaration.NAME_PROPERTY);
		}
		if (parent instanceof AnnotationTypeMemberDeclaration) {
			return (d == AnnotationTypeMemberDeclaration.NAME_PROPERTY);
		}
		return false;
	}

	/* (omit javadoc for this method)
	 * Method declared on Name.
	 */
	void appendName(StringBuffer buffer) {
		buffer.append(getIdentifier());
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		int size = BASE_NAME_NODE_SIZE + 2 * 4;
		if (this.identifier != MISSING_IDENTIFIER) {
			// everything but our missing id costs
			size += stringSize(this.identifier);
		}
		return size;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize();
	}
}

