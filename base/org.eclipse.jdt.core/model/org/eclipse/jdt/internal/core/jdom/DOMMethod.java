/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.jdom;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.jdom.*;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;
import org.eclipse.jdt.internal.core.util.Util;
/**
 * DOMMethod provides an implementation of IDOMMethod.
 *
 * @see IDOMMethod
 * @see DOMNode
 * @deprecated The JDOM was made obsolete by the addition in 2.0 of the more
 * powerful, fine-grained DOM/AST API found in the 
 * org.eclipse.jdt.core.dom package.
 */
// TODO (jerome) - add implementation support for 1.5 features
class DOMMethod extends DOMMember implements IDOMMethod {

	/**
	 * Contains the return type of the method when the
	 * return type has been altered from the contents
	 * in the document, otherwise <code>null</code>.
	 */
	protected String fReturnType;

	/**
	 * The original inclusive source range of the
	 * method's return type in the document, or -1's
	 * if no return type is present in the document.
	 * If the return type of this method is qualified with
	 * '[]' following the parameter list, this array has
	 * four entries. In this case, the last two entries
	 * of the array are the inclusive source range of
	 * the array qualifiers.
	 */
	protected int[]  fReturnTypeRange;

	/**
	 * Contains the textual representation of the method's
	 * parameter list, including open and closing parentheses
	 * when the parameters had been altered from the contents
	 * in the document, otherwise <code>null</code>.
	 */
	protected char[] fParameterList;

	/**
	 * The original inclusive source range of the
	 * method's parameter list in the document.
	 */
	protected int[]  fParameterRange;

	/**
	 * Contains the textual representation of the method's
	 * exception list when the exceptions had been altered
	 * from the contents in the document, otherwise
	 * <code>null</code>. The exception list is a comment
	 * delimited list of exceptions, not including the "throws"
	 * keyword.
	 */
	protected char[] fExceptionList;

	/**
	 * The original inclusive source range of the
	 * method's exception list in the document.
	 */
	protected int[]  fExceptionRange;
	
	/**
	 * Contains the method's body when the body has
	 * been altered from the contents in the document,
	 * otherwise <code>null</code>. The body includes everything
	 * between and including the enclosing braces, and trailing
	 * whitespace.
	 */
	protected String fBody;

	/**
	 * The original inclusive source range of the
	 * method's body.
	 */
	protected int[]  fBodyRange;


	/**
	 * Names of parameters in the method parameter list,
	 * or <code>null</code> if the method has no parameters.
	 */
	protected String[] fParameterNames;

	/**
	 * Types of parameters in the method parameter list,
	 * or <code>null</code> if the method has no parameters.
	 */
	protected String[] fParameterTypes;

	/**
	 * The exceptions the method throws, or <code>null</code>
	 * if the method throws no exceptions.
	 */
	protected String[] fExceptions;

	/**
	 * The formal type parameters.
	 * @since 3.0
	 */
	protected String[] fTypeParameters = CharOperation.NO_STRINGS;

	/**
	 * Default value for this attotation type member (only),
	 * or <code>null</code> if none.
	 * @since 3.0
	 */
	protected String fDefaultValue = null;
	
/**
 * Constructs an empty method node.
 */
DOMMethod() {
	// Constructs an empty method node
}
/**
 * Creates a new detailed METHOD document fragment on the given range of the document.
 *
 * @param document - the document containing this node's original contents
 * @param sourceRange - a two element array of integers describing the
 *		entire inclusive source range of this node within its document.
 * 		Contents start on and include the character at the first position.
 *		Contents end on and include the character at the last position.
 *		An array of -1's indicates this node's contents do not exist
 *		in the document.
 * @param name - the identifier portion of the name of this node, or
 *		<code>null</code> if this node does not have a name
 * @param nameRange - a two element array of integers describing the
 *		entire inclusive source range of this node's name within its document,
 *		including any array qualifiers that might immediately follow the name
 *		or -1's if this node does not have a name.
 * @param commentRange - a two element array describing the comments that precede
 *		the member declaration. The first matches the start of this node's
 *		sourceRange, and the second is the new-line or first non-whitespace
 *		character following the last comment. If no comments are present,
 *		this array contains two -1's.
 * @param flags - an integer representing the modifiers for this member. The
 *		integer can be analyzed with org.eclipse.jdt.core.Flags
 * @param modifierRange - a two element array describing the location of
 *		modifiers for this member within its source range. The first integer
 *		is the first character of the first modifier for this member, and
 *		the second integer is the last whitespace character preceeding the
 *		next part of this member declaration. If there are no modifiers present
 *		in this node's source code (that is, package default visibility), this array
 *		contains two -1's.
 * @param isConstructor - true if the method is a contructor, otherwise false
 * @param returnType - the normalized return type of this method
 * @param returnTypeRange - a two element array describing the location of the
 *		return type within the method's source range. The first integer is is
 *		the position of the first character in the return type, and the second
 *		integer is the position of the last character in the return type.
 *		For constructors, the contents of this array are -1's.
 * 		If the return type of this method is qualified with '[]' following the
 *		parameter list, this array has four entries. In this case, the last
 *		two entries of the array are the inclusive source range of the array
 *		qualifiers.
 * @param parameterTypes - an array of parameter types in the method declaration
 * 		or <code>null</code> if the method has no parameters
 * @param parameterNames - an array of parameter names in the method declaration
 * 		or <code>null</code> if the method has no parameters
 * @param parameterRange - a two element array describing the location of the
 * 		parameter list in the method. The first integer is the location of the
 *		open parenthesis and the second integer is the location of the closing
 *		parenthesis.
 * @param exceptions - an array of the names of exceptions thrown by this method
 *		or <code>null</code> if the method throws no exceptions
 * @param exceptionRange - a two element array describing the location of the
 * 		exception list in the method declaration. The first integer is the position
 * 		of the first character in the first exception the method throws, and the
 *		second integer is the position of the last character of the last exception
 *		this method throws.
 * @param bodyRange - a two element array describing the location of the method's body.
 *		The first integer is the first character following the method's
 *		parameter list, or exception list (if present). The second integer is the location
 * 		of the last character in the method's source range.
 */
DOMMethod(char[] document, int[] sourceRange, String name, int[] nameRange, int[] commentRange, int flags, int[] modifierRange, boolean isConstructor, String returnType, int[] returnTypeRange, String[] parameterTypes, String[] parameterNames, int[] parameterRange, String[] exceptions, int[] exceptionRange, int[] bodyRange) {
	super(document, sourceRange, name, nameRange, commentRange, flags, modifierRange);

	setMask(MASK_IS_CONSTRUCTOR, isConstructor);
	fReturnType= returnType;
	fReturnTypeRange= returnTypeRange;
	fParameterTypes= parameterTypes;
	fParameterNames= parameterNames;
	fParameterRange= parameterRange;
	fExceptionRange= exceptionRange;
	fExceptions= exceptions;
	setHasBody(true);
	fBodyRange= bodyRange;
	setMask(MASK_DETAILED_SOURCE_INDEXES, true);
	
}
/**
 * Creates a new simple METHOD document fragment on the given range of the document.
 *
 * @param document - the document containing this node's original contents
 * @param sourceRange - a two element array of integers describing the
 *		entire inclusive source range of this node within its document.
 * 		Contents start on and include the character at the first position.
 *		Contents end on and include the character at the last position.
 *		An array of -1's indicates this node's contents do not exist
 *		in the document.
 * @param name - the identifier portion of the name of this node, or
 *		<code>null</code> if this node does not have a name
 * @param nameRange - a two element array of integers describing the
 *		entire inclusive source range of this node's name within its document,
 *		including any array qualifiers that might immediately follow the name
 *		or -1's if this node does not have a name.
 * @param flags - an integer representing the modifiers for this member. The
 *		integer can be analyzed with org.eclipse.jdt.core.Flags
 * @param isConstructor - true if the method is a contructor, otherwise false
 * @param returnType - the normalized return type of this method
 * @param parameterTypes - an array of parameter types in the method declaration
 * 		or <code>null</code> if the method has no parameters
 * @param parameterNames - an array of parameter names in the method declaration
 * 		or <code>null</code> if the method has no parameters
 * @param exceptions - an array of the names of exceptions thrown by this method
 *		or <code>null</code> if the method throws no exceptions
 */
DOMMethod(char[] document, int[] sourceRange, String name, int[] nameRange, int flags, boolean isConstructor, String returnType, String[] parameterTypes, String[] parameterNames, String[] exceptions) {
	this(document, sourceRange, name, nameRange, new int[] {-1, -1}, flags, new int[] {-1, -1}, isConstructor, returnType, new int[] {-1, -1}, parameterTypes, parameterNames, new int[] {-1, -1}, exceptions, new int[] {-1, -1}, new int[] {-1, -1});
	setMask(MASK_DETAILED_SOURCE_INDEXES, false);
}
/**
 * @see IDOMMethod#addException(String)
 */
public void addException(String name) throws IllegalArgumentException {
	if (name == null) {
		throw new IllegalArgumentException(Messages.dom_nullExceptionType); 
	}
	if (fExceptions == null) {
		fExceptions= new String[1];
		fExceptions[0]= name;
	} else {
		fExceptions= appendString(fExceptions, name);
	}
	setExceptions(fExceptions);
}
/**
 * @see IDOMMethod#addParameter(String, String)
 */
public void addParameter(String type, String name) throws IllegalArgumentException {
	if (type == null) {
		throw new IllegalArgumentException(Messages.dom_nullTypeParameter); 
	}
	if (name == null) {
		throw new IllegalArgumentException(Messages.dom_nullNameParameter); 
	}
	if (fParameterNames == null) {
		fParameterNames= new String[1];
		fParameterNames[0]= name;
	} else {
		fParameterNames= appendString(fParameterNames, name);
	}
	if (fParameterTypes == null) {
		fParameterTypes= new String[1];
		fParameterTypes[0]= type;
	} else {
		fParameterTypes= appendString(fParameterTypes, type);
	}
	setParameters(fParameterTypes, fParameterNames);
}
/**
 * @see DOMMember#appendMemberBodyContents(CharArrayBuffer)
 */
protected void appendMemberBodyContents(CharArrayBuffer buffer) {
	if (fBody != null) {
		buffer.append(fBody);
	} else {
		buffer.append(fDocument, fBodyRange[0], fBodyRange[1] + 1 - fBodyRange[0]);
	}
}
/**
 * @see DOMMember#appendMemberDeclarationContents(CharArrayBuffer)
 */
protected void appendMemberDeclarationContents(CharArrayBuffer buffer) {

	if (isConstructor()) {
		buffer
			.append(getConstructorName())
			.append(fDocument, fNameRange[1] + 1, fParameterRange[0] - fNameRange[1] - 1);
	} else {
		buffer.append(getReturnTypeContents());
		if (fReturnTypeRange[0] >= 0) {
			buffer.append(fDocument, fReturnTypeRange[1] + 1, fNameRange[0] - fReturnTypeRange[1] - 1);
		} else {
			buffer.append(' ');
		}
		buffer
			.append(getNameContents())
			.append(fDocument, fNameRange[1] + 1, fParameterRange[0] - fNameRange[1] - 1);
	}
	if (fParameterList != null) {
		buffer.append(fParameterList);
	} else {
		buffer.append(fDocument, fParameterRange[0], fParameterRange[1] + 1 - fParameterRange[0]);
	}
	int start;
	if (hasTrailingArrayQualifier() && isReturnTypeAltered()) {
		start= fReturnTypeRange[3] + 1;
	} else {
		start= fParameterRange[1] + 1;
	}
	if (fExceptions != null) {
		// add 'throws' keyword
		if (fExceptionRange[0] >= 0) {
			buffer.append(fDocument, start, fExceptionRange[0] - start);
		} else {
			buffer.append(" throws "); //$NON-NLS-1$
		}
		// add exception list
		if (fExceptionList != null) {
			buffer.append(fExceptionList);
			// add space before body
			if (fExceptionRange[0] >= 0) {
				buffer.append(fDocument, fExceptionRange[1] + 1, fBodyRange[0] - fExceptionRange[1] - 1);
			} else {
				buffer.append(fDocument, fParameterRange[1] + 1, fBodyRange[0] - fParameterRange[1] - 1);
			}
		} else {
			// add list and space before body
			buffer.append(fDocument, fExceptionRange[0], fBodyRange[0] - fExceptionRange[0]);
		}
	} else {
		// add space before body
		if (fExceptionRange[0] >= 0) {
			buffer.append(fDocument, fExceptionRange[1] + 1, fBodyRange[0] - fExceptionRange[1] - 1);
		} else {
			buffer.append(fDocument, start, fBodyRange[0] - start);
		}
	}
	
}
/**
 * @see DOMMember#appendSimpleContents(CharArrayBuffer)
 */
protected void appendSimpleContents(CharArrayBuffer buffer) {
	// append eveything before my name
	buffer.append(fDocument, fSourceRange[0], fNameRange[0] - fSourceRange[0]);
	// append my name
	if (isConstructor()) {
		buffer.append(getConstructorName());
	} else {
		buffer.append(fName);
	}
	// append everything after my name
	buffer.append(fDocument, fNameRange[1] + 1, fSourceRange[1] - fNameRange[1]);
}
/**
 * @see IDOMMethod#getBody()
 */
public String getBody() {
	becomeDetailed();
	if (hasBody()) {
		if (fBody != null) {
			return fBody;
		} else {
			return new String(fDocument, fBodyRange[0], fBodyRange[1] + 1 - fBodyRange[0]);
		}
	} else {
		return null;
	}
}
/**
 * Returns the simple name of the enclsoing type for this constructor.
 * If the constuctor is not currently enclosed in a type, the original
 * name of the constructor as found in the documnent is returned.
 */
protected String getConstructorName() {

	if (isConstructor()) {
		if (getParent() != null) {
			return getParent().getName();
		} else {
			// If there is no parent use the original name
			return new String(getNameContents());
		}
	} else {
		return null;
	}
	
}
/**
 * @see DOMNode#getDetailedNode()
 */
protected DOMNode getDetailedNode() {
	return (DOMNode)getFactory().createMethod(getContents());
}
/**
 * @see IDOMMethod#getExceptions()
 */
public String[] getExceptions() {
	return fExceptions;
}
protected char[] generateFlags() {
	char[] flags= Flags.toString(getFlags() & ~Flags.AccVarargs).toCharArray();
	if (flags.length == 0) {
		return flags;
	} else {
		return CharOperation.concat(flags, new char[] {' '});
	}
}/**
 * @see IDOMNode#getJavaElement
 */
public IJavaElement getJavaElement(IJavaElement parent) throws IllegalArgumentException {
	if (parent.getElementType() == IJavaElement.TYPE) {
		// translate parameter types to signatures
		String[] sigs= null;
		if (fParameterTypes != null) {
			sigs= new String[fParameterTypes.length];
			int i;
			for (i= 0; i < fParameterTypes.length; i++) {
				sigs[i]= Signature.createTypeSignature(fParameterTypes[i].toCharArray(), false);
			}
		}
		String name= null;
		if (isConstructor()) {
			name= getConstructorName();
		} else {
			name= getName();
		}
		return ((IType)parent).getMethod(name, sigs);
	} else {
		throw new IllegalArgumentException(Messages.element_illegalParent); 
	}
}
/**
 * @see DOMMember#getMemberDeclarationStartPosition()
 */
protected int getMemberDeclarationStartPosition() {
	if (fReturnTypeRange[0] >= 0) {
		return fReturnTypeRange[0];
	} else {
		return fNameRange[0];
	}
}
/**
 * @see IDOMNode#getName()
 */
public String getName() {
	if (isConstructor()) {
		return null;
	} else {
		return super.getName();
	}
}
/**
 * @see IDOMNode#getNodeType()
 */
public int getNodeType() {
	return IDOMNode.METHOD;
}
/**
 * @see IDOMMethod#getParameterNames()
 */
public String[] getParameterNames() {
	return fParameterNames;
}
/**
 * @see IDOMMethod#getParameterTypes()
 */
public String[] getParameterTypes() {
	return fParameterTypes;
}
/**
 * @see IDOMMethod#getReturnType()
 */
public String getReturnType() {
	if (isConstructor()) {
		return null;
	} else {
		return fReturnType;
	}
}
/**
 * Returns the source code to be used for this method's return type
 */
protected char[] getReturnTypeContents() {
	if (isConstructor()) {
		return null;
	} else {
		if (isReturnTypeAltered()) {
			return fReturnType.toCharArray();
		} else {
			return CharOperation.subarray(fDocument, fReturnTypeRange[0], fReturnTypeRange[1] + 1);
		}

	}
}
/**
 * Returns true if this method's return type has
 * array qualifiers ('[]') following the parameter list.
 */
protected boolean hasTrailingArrayQualifier() {
	return fReturnTypeRange.length > 2;
}
/**
 * @see IDOMMethod#isConstructor()
 */
public boolean isConstructor() {
	return getMask(MASK_IS_CONSTRUCTOR);
}
/**
 * Returns true if this method's return type has been altered
 * from the original document contents.
 */
protected boolean isReturnTypeAltered() {
	return getMask(MASK_RETURN_TYPE_ALTERED);
}
/**
 * @see IDOMNode#isSignatureEqual(IDOMNode)
 *
 * <p>Two methods have equal signatures if there names are the same
 * and their parameter types are the same.
 */
public boolean isSignatureEqual(IDOMNode node) {
	boolean ok= node.getNodeType() == getNodeType();
	if (ok) {
		IDOMMethod method= (IDOMMethod)node;
		ok = (isConstructor() && method.isConstructor()) ||  
			(!isConstructor() && !method.isConstructor());
		if (ok && !isConstructor()) {
			ok= getName().equals(method.getName());
		}
		if (!ok) {
			return false;
		}
		
		String[] types= method.getParameterTypes();
		if (fParameterTypes == null || fParameterTypes.length == 0) {
			// this method has no parameters
			if (types == null || types.length == 0) {
				// the other method has no parameters either
				return true;
			}
		} else {
			// this method has parameters
			if (types == null || types.length == 0) {
				// the other method has no parameters
				return false;
			}
			if (fParameterTypes.length != types.length) {
				// the methods have a different number of parameters
				return false;
			}
			int i;
			for (i= 0; i < types.length; i++) {
				if (!fParameterTypes[i].equals(types[i])) {
					return false;
				}
			}
			return true;
		}
	}
	return false;
	
}
/**
 * @see DOMNode
 */
protected DOMNode newDOMNode() {
	return new DOMMethod();
}
/**
 * Offsets all the source indexes in this node by the given amount.
 */
protected void offset(int offset) {
	super.offset(offset);
	offsetRange(fBodyRange, offset);
	offsetRange(fExceptionRange, offset);
	offsetRange(fParameterRange, offset);
	offsetRange(fReturnTypeRange, offset);
}
/**
 * @see IDOMMethod#setBody
 */
public void setBody(String body) {
	becomeDetailed();
	fragment();
	fBody= body;
	setHasBody(body != null);
	if (!hasBody()) {
		fBody= ";" + Util.getLineSeparator(body, null); //$NON-NLS-1$
	}
}
/**
 * Sets the end of the body range
 */
void setBodyRangeEnd(int end) {
	fBodyRange[1] = end;
}
/**
 * @see IDOMMethod#setConstructor(boolean)
 */
public void setConstructor(boolean b) {
	becomeDetailed();
	setMask(MASK_IS_CONSTRUCTOR, b);
	fragment();
}
/**
 * @see IDOMMethod#setExceptions(String[])
 */
public void setExceptions(String[] names) {
	becomeDetailed();
	if (names == null || names.length == 0) {
		fExceptions= null;
	} else {
		fExceptions= names;
		CharArrayBuffer buffer = new CharArrayBuffer();
		char[] comma = new char[] {',', ' '};
		for (int i = 0, length = names.length; i < length; i++) {
			if (i > 0)
				buffer.append(comma);
			buffer.append(names[i]);
		}
		fExceptionList= buffer.getContents();		
	}
	fragment();
}
/**
 * @see IDOMMethod#setName
 */
public void setName(String name) {
	if (name == null) {
		throw new IllegalArgumentException(Messages.element_nullName); 
	} else {
		super.setName(name);
	}
}
/**
 * @see IDOMMethod#setParameters(String[], String[])
 */
public void setParameters(String[] types, String[] names) throws IllegalArgumentException {
	becomeDetailed();
	if (types== null || names == null) {
		if (types == null && names == null) {
			fParameterTypes= null;
			fParameterNames= null;
			fParameterList= new char[] {'(',')'};
		} else {
			throw new IllegalArgumentException(Messages.dom_mismatchArgNamesAndTypes); 
		}
	} else if (names.length != types.length) {
		throw new IllegalArgumentException(Messages.dom_mismatchArgNamesAndTypes); 
	} else if (names.length == 0) {
		setParameters(null, null);
	} else {
		fParameterNames= names;
		fParameterTypes= types;
		CharArrayBuffer parametersBuffer = new CharArrayBuffer();
		parametersBuffer.append("("); //$NON-NLS-1$
		char[] comma = new char[] {',', ' '};
		for (int i = 0; i < names.length; i++) {
			if (i > 0) {
				parametersBuffer.append(comma);
			}
			parametersBuffer
				.append(types[i])
				.append(' ')
				.append(names[i]);
		}
		parametersBuffer.append(')');
		fParameterList= parametersBuffer.getContents();		
	}
	fragment();
}
/**
 * @see IDOMMethod#setReturnType(String)
 */
public void setReturnType(String name) throws IllegalArgumentException {
	if (name == null) {
		throw new IllegalArgumentException(Messages.dom_nullReturnType); 
	}
	becomeDetailed();
	fragment();
	setReturnTypeAltered(true);
	fReturnType= name;
}
/**
 * Sets the state of this method declaration as having
 * the return type altered from the original document.
 */
protected void setReturnTypeAltered(boolean typeAltered) {
	setMask(MASK_RETURN_TYPE_ALTERED, typeAltered);
}
/**
 */
protected void setSourceRangeEnd(int end) {
	super.setSourceRangeEnd(end);
	fBodyRange[1]= end;
}
/**
 * @see DOMNode#shareContents(DOMNode)
 */
protected void shareContents(DOMNode node) {
	super.shareContents(node);
	DOMMethod method= (DOMMethod)node;
	fBody= method.fBody;
	fBodyRange= rangeCopy(method.fBodyRange);
	fExceptionList= method.fExceptionList;
	fExceptionRange= rangeCopy(method.fExceptionRange);
	fExceptions= method.fExceptions;
	fParameterList= method.fParameterList;
	fParameterNames= method.fParameterNames;
	fParameterRange= rangeCopy(method.fParameterRange);
	fParameterTypes= method.fParameterTypes;
	fReturnType= method.fReturnType;
	fReturnTypeRange= rangeCopy(method.fReturnTypeRange);
}
/**
 * @see IDOMNode#toString()
 */
public String toString() {
	if (isConstructor()) {
		return "CONSTRUCTOR"; //$NON-NLS-1$
	} else {
		return "METHOD: " + getName(); //$NON-NLS-1$
	}
}

/**
 * @see IDOMMethod#setDefault(java.lang.String)
 * @since 3.0
 */
public void setDefault(String defaultValue) {
	this.fDefaultValue =  defaultValue;
}

/**
 * @see IDOMMethod#getDefault()
 * @since 3.0
 */
public String getDefault() {
	return this.fDefaultValue;
}

/**
 * @see IDOMMethod#getTypeParameters()
 * @since 3.0
 */
public String[] getTypeParameters() {
	return this.fTypeParameters;
}

/**
 * @see IDOMMethod#setTypeParameters(java.lang.String[])
 * @since 3.0
 */
public void setTypeParameters(String[] typeParameters) {
	this.fTypeParameters = typeParameters;
}
}
