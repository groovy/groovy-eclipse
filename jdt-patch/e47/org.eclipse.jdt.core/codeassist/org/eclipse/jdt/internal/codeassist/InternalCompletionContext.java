/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnJavadoc;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;


/**
 * Internal completion context
 * @since 3.1
 */
public class InternalCompletionContext extends CompletionContext {
	protected char[][] expectedTypesSignatures;
	protected char[][] expectedTypesKeys;
	protected int javadoc;

	protected int offset = -1;
	protected int tokenStart = -1;
	protected int tokenEnd = -1;
	protected char[] token = null;
	protected int tokenKind;
	protected int tokenLocation;

	protected boolean isExtended;
	protected InternalExtendedCompletionContext extendedContext;

	protected void setExpectedTypesKeys(char[][] expectedTypesKeys) {
		this.expectedTypesKeys = expectedTypesKeys;
	}

	protected void setExpectedTypesSignatures(char[][] expectedTypesSignatures) {
		this.expectedTypesSignatures = expectedTypesSignatures;
	}

	protected void setExtended() {
		this.isExtended = true;
	}

	protected void setExtendedData(
			ITypeRoot typeRoot,
			CompilationUnitDeclaration compilationUnitDeclaration,
			LookupEnvironment lookupEnvironment,
			Scope scope,
			ASTNode astNode,
			ASTNode astNodeParent,
			WorkingCopyOwner owner,
			CompletionParser parser) {
		this.isExtended = true;
		this.extendedContext =
			new InternalExtendedCompletionContext(
					this,
					typeRoot,
					compilationUnitDeclaration,
					lookupEnvironment,
					scope,
					astNode,
					astNodeParent,
					owner,
					parser);
	}

	protected void setJavadoc(int javadoc) {
		this.javadoc = javadoc;
	}

	protected void setOffset(int offset) {
		this.offset = offset;
	}

	protected void setToken(char[] token) {
		this.token = token;
	}

	protected void setTokenKind(int tokenKind) {
		this.tokenKind = tokenKind;
	}

	protected void setTokenLocation(int tokenLocation) {
		this.tokenLocation = tokenLocation;
	}

	protected void setTokenRange(int start, int end) {
		this.setTokenRange(start, end, -1);
	}

	protected void setTokenRange(int start, int end, int endOfEmptyToken) {
		this.tokenStart = start;
		this.tokenEnd = endOfEmptyToken > end ? endOfEmptyToken : end;

		// Work around for bug 132558 (https://bugs.eclipse.org/bugs/show_bug.cgi?id=132558).
		// completionLocation can be -1 if the completion occur at the start of a file or
		// the start of a code snippet but this API isn't design to support negative position.
		if(this.tokenEnd == -1) {
			this.tokenEnd = 0;
		}
	}

	/**
	 * Returns the innermost enclosing Java element which contains the completion location or <code>null</code> if this element cannot be computed.
	 * The returned Java element and all Java elements in the same compilation unit which can be navigated to from the returned Java element are special Java elements:
	 * <ul>
	 * <li>they are based on the current content of the compilation unit's buffer, they are not the result of a reconcile operation</li>
	 * <li>they are not updated if the buffer changes.</li>
	 * <li>they do not contain local types which are not visible from the completion location.</li>
	 * <li>they do not give information about categories. {@link IMember#getCategories()} will return an empty array</li>
	 * </ul>
	 *
	 * Reasons for returning <code>null</code> include:
	 * <ul>
	 * <li>the compilation unit no longer exists</li>
	 * <li>the completion occurred in a binary type. However this restriction might be relaxed in the future.</li>
	 * </ul>
	 *
	 * @return the innermost enclosing Java element which contains the completion location or <code>null</code> if this element cannot be computed.
	 *
	 * @exception UnsupportedOperationException if the context is not an extended context
	 *
	 * @since 3.4
	 */
	public IJavaElement getEnclosingElement() {
		if (!this.isExtended) throw new UnsupportedOperationException("Operation only supported in extended context"); //$NON-NLS-1$
	
		if (this.extendedContext == null) return null;
	
		return this.extendedContext.getEnclosingElement();
	}

	/**
	 * Return keys of expected types of a potential completion proposal at the completion position.
	 *
	 * It's not mandatory to a completion proposal to respect this expectation.
	 *
	 * @return keys of expected types of a potential completion proposal at the completion position or
	 * <code>null</code> if there is no expected types.
	 *
	 * @see org.eclipse.jdt.core.dom.ASTParser#createASTs(ICompilationUnit[], String[], org.eclipse.jdt.core.dom.ASTRequestor, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public char[][] getExpectedTypesKeys() {
		return this.expectedTypesKeys;
	}

	/**
	 * Return signatures of expected types of a potential completion proposal at the completion position.
	 *
	 * It's not mandatory to a completion proposal to respect this expectation.
	 *
	 * @return signatures expected types of a potential completion proposal at the completion position or
	 * <code>null</code> if there is no expected types.
	 *
	 * @see Signature
	 */
	public char[][] getExpectedTypesSignatures() {
		return this.expectedTypesSignatures;
	}

	/**
	 * Returns the offset position in the source file buffer
	 * after which code assist is requested.
	 *
	 * @return offset position in the source file buffer
	 * @since 3.2
	 */
	public int getOffset() {
		return this.offset;
	}

	/**
	 * Returns the completed token.
	 * This token is either the identifier or Java language keyword
	 * or the string literal under, immediately preceding,
	 * the original request offset. If the original request offset
	 * is not within or immediately after an identifier or keyword or
	 * a string literal then the returned value is <code>null</code>.
	 *
	 * @return completed token or <code>null</code>
	 * @since 3.2
	 */
	public char[] getToken() {
		return this.token;
	}

	/**
	 * Returns the character index of the end (exclusive) of the subrange
	 * in the source file buffer containing the
	 * relevant token. When there is no relevant token, the
	 * range is empty
	 * (<code>getTokenEnd() == getTokenStart() - 1</code>).
	 *
	 * @return character index of token end position (exclusive)
	 * @since 3.2
	 */
	// TODO (david) https://bugs.eclipse.org/bugs/show_bug.cgi?id=132558
	public int getTokenEnd() {
		return this.tokenEnd;
	}

	/**
	 * Returns the kind of completion token being proposed.
	 * <p>
	 * The set of different kinds of completion token is
	 * expected to change over time. It is strongly recommended
	 * that clients do <b>not</b> assume that the kind is one of the
	 * ones they know about, and code defensively for the
	 * possibility of unexpected future growth.
	 * </p>
	 *
	 * @return the kind; one of the kind constants declared on
	 * this class whose name starts with <code>TOKEN_KIND</code>,
	 * or possibly a kind unknown to the caller
	 * @since 3.2
	 */
	public int getTokenKind() {
		return this.tokenKind;
	}

	/**
	 * Returns the location of completion token being proposed.
	 * The returned location is a bit mask which can contain some values
	 * of the constants declared on this class whose name starts with <code>TL</code>,
	 * or possibly values unknown to the caller.
	 *
	 * <p>
	 * The set of different location values is expected to change over time.
	 * It is strongly recommended that clients do <b>not</b> assume that
	 * the location contains only known value, and code defensively for
	 * the possibility of unexpected future growth.
	 * </p>
	 *
	 * @return the location
	 *
	 * @since 3.4
	 */
	public int getTokenLocation() {
		return this.tokenLocation;
	}

	/**
	 * Returns the character index of the start of the
	 * subrange in the source file buffer containing the
	 * relevant token being completed. This
	 * token is either the identifier or Java language keyword
	 * under, or immediately preceding, the original request
	 * offset. If the original request offset is not within
	 * or immediately after an identifier or keyword, then the
	 * position returned is original request offset and the
	 * token range is empty.
	 *
	 * @return character index of token start position (inclusive)
	 * @since 3.2
	 */
	public int getTokenStart() {
		return this.tokenStart;
	}

	/**
	 * Return the elements which are visible from the completion location and which can be assigned to the given type.
	 * An element is assignable if its type can be assigned to a variable
	 * of the given type, as specified in section 5.2 of <em>The Java Language
	 * Specification, Third Edition</em> (JLS3).
	 * A visible element is either:
	 * <ul>
	 * <li>a {@link ILocalVariable} - the element type is {@link ILocalVariable#getTypeSignature()}</li>
	 * <li>a {@link IField} - the element type is {@link IField#getTypeSignature()}</li>
	 * <li>a {@link IMethod} - the element type is {@link IMethod#getReturnType()}</li>
	 * </ul>
	 *
	 * Returned elements defined in the completed compilation unit are special Java elements:
	 * <ul>
	 * <li>they are based on the current content of the compilation unit's buffer, they are not the result of a reconcile operation</li>
	 * <li>they are not updated if the buffer changes.</li>
	 * <li>they do not contain local types which are not visible from the completion location.</li>
	 * <li>they do not give information about categories. {@link IMember#getCategories()} will return an empty array</li>
	 * </ul>
	 *
	 * Note the array can be empty if:
	 * <ul>
	 * <li>the compilation unit no longer exists</li>
	 * <li>the completion occurred in a binary type. However this restriction might be relaxed in the future.</li>
	 * </ul>
	 *
	 * @param typeSignature elements which can be assigned to this type are returned.
	 * 		If <code>null</code> there is no constraint on the type of the returned elements.
	 *
	 * @return elements which are visible from the completion location and which can be assigned to the given type.
	 *
	 * @exception UnsupportedOperationException if the context is not an extended context
	 *
	 * @see #isExtended()
	 *
	 * @since 3.4
	 */
	public IJavaElement[] getVisibleElements(String typeSignature) {
		if (!this.isExtended) throw new UnsupportedOperationException("Operation only supported in extended context"); //$NON-NLS-1$
	
		if (this.extendedContext == null) return new IJavaElement[0];
	
		return this.extendedContext.getVisibleElements(typeSignature);
	}

	/**
	 * Returns whether this completion context is an extended context.
	 * Some methods of this context can be used only if this context is an extended context but an extended context consumes more memory.
	 *
	 * @return <code>true</code> if this completion context is an extended context.
	 *
	 * @since 3.4
	 */
	public boolean isExtended() {
		return this.isExtended;
	}

	/**
	 * Tell user whether completion takes place in a javadoc comment or not.
	 *
	 * @return boolean true if completion takes place in a javadoc comment, false otherwise.
	 * @since 3.2
	 */
	public boolean isInJavadoc() {
		return this.javadoc != 0;
	}

	/**
	 * Tell user whether completion takes place in a formal reference of a javadoc tag or not.
	 * Tags with formal reference are:
	 * <ul>
	 * 	<li>&#64;see</li>
	 * 	<li>&#64;throws</li>
	 * 	<li>&#64;exception</li>
	 * 	<li>{&#64;link Object}</li>
	 * 	<li>{&#64;linkplain Object}</li>
	 * 	<li>{&#64;value} when compiler compliance is set at leats to 1.5</li>
	 * </ul>
	 *
	 * @return boolean true if completion takes place in formal reference of a javadoc tag, false otherwise.
	 * @since 3.2
	 */
	public boolean isInJavadocFormalReference() {
		return (this.javadoc & CompletionOnJavadoc.FORMAL_REFERENCE) != 0;
	}

	/**
	 * Tell user whether completion takes place in text area of a javadoc comment or not.
	 *
	 * @return boolean true if completion takes place in a text area of a javadoc comment, false otherwise.
	 * @since 3.2
	 */
	public boolean isInJavadocText() {
		return (this.javadoc & CompletionOnJavadoc.TEXT) != 0;
	}
	
	/**
	 * Return the completion node associated with the current completion.
	 *
	 * @return completion AST node, or null if the extendedContext is null.
	 * @exception UnsupportedOperationException if the context is not an extended context
	 *
	 * @see #isExtended()
	 */
	public ASTNode getCompletionNode() {
		if (!this.isExtended) throw new UnsupportedOperationException("Operation only supported in extended context"); //$NON-NLS-1$
	
		if (this.extendedContext == null) return null;
	
		return this.extendedContext.getCompletionNode();
	}
	
	/**
	 * Return the parent AST node of the completion node associated with the current completion.
	 *
	 * @return completion parent AST node, or null if the extendedContext is null.
	 * @exception UnsupportedOperationException if the context is not an extended context
	 *
	 * @see #isExtended()
	 */
	public ASTNode getCompletionNodeParent() {
		if (!this.isExtended) throw new UnsupportedOperationException("Operation only supported in extended context"); //$NON-NLS-1$
	
		if (this.extendedContext == null) return null;
	
		return this.extendedContext.getCompletionNodeParent();
	}
	
	/**
	 * Return the bindings of all visible local variables in the current completion context.
	 *
	 * @return bindings of all visible local variables, or null if the extendedContext is null. Returned bindings are instances of
	 * {@link LocalVariableBinding}
	 * @exception UnsupportedOperationException if the context is not an extended context
	 *
	 * @see #isExtended()
	 */
	public ObjectVector getVisibleLocalVariables() {
		if (!this.isExtended) throw new UnsupportedOperationException("Operation only supported in extended context"); //$NON-NLS-1$
	
		if (this.extendedContext == null) return null;
	
		return this.extendedContext.getVisibleLocalVariables();
	}
	
	/**
	 * Return the bindings of all visible fields in the current completion context.
	 *
	 * @return bindings of all visible fields, or null if the extendedContext is null. Returned bindings are instances of
	 * {@link FieldBinding}
	 * @exception UnsupportedOperationException if the context is not an extended context
	 *
	 * @see #isExtended()
	 */
	public ObjectVector getVisibleFields() {
		if (!this.isExtended) throw new UnsupportedOperationException("Operation only supported in extended context"); //$NON-NLS-1$
	
		if (this.extendedContext == null) return null;
	
		return this.extendedContext.getVisibleFields();
	}
	
	/**
	 * Return the bindings of all visible methods in the current completion context.
	 *
	 * @return bindings of all visible methods, or null if the extendedContext is null. Returned bindings are instances of
	 * {@link MethodBinding}
	 * @exception UnsupportedOperationException if the context is not an extended context
	 *
	 * @see #isExtended()
	 */
	public ObjectVector getVisibleMethods() {
		if (!this.isExtended) throw new UnsupportedOperationException("Operation only supported in extended context"); //$NON-NLS-1$
	
		if (this.extendedContext == null) return null;
	
		return this.extendedContext.getVisibleMethods();
	}
}
