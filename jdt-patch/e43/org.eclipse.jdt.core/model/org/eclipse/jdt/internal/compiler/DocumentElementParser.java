/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;

public class DocumentElementParser extends Parser {
	IDocumentElementRequestor requestor;
	private int localIntPtr;
	private int lastFieldEndPosition;
	private int lastFieldBodyEndPosition;
	private int typeStartPosition;
	private long selectorSourcePositions;
	private int typeDims;
	private int extendsDim;
	private int declarationSourceStart;

	/* int[] stack for storing javadoc positions */
	int[][] intArrayStack;
	int intArrayPtr;

public DocumentElementParser(
	final IDocumentElementRequestor requestor,
	IProblemFactory problemFactory,
	CompilerOptions options) {
	super(new ProblemReporter(
		DefaultErrorHandlingPolicies.exitAfterAllProblems(),
		options,
		problemFactory),
	false);
	this.requestor = requestor;
	this.intArrayStack = new int[30][];
	this.options = options;
	this.javadocParser.checkDocComment = false;

	setMethodsFullRecovery(false);
	setStatementsRecovery(false);
}
/*
 * Will clear the comment stack when looking
 * for a potential JavaDoc which might contain @deprecated.
 *
 * Additionally, before investigating for @deprecated, retrieve the positions
 * of the JavaDoc comments so as to notify requestor with them.
 */
public void checkComment() {

	/* persisting javadoc positions */
	pushOnIntArrayStack(getJavaDocPositions());
	boolean deprecated = false;
	int lastCommentIndex = -1;
	int commentPtr = this.scanner.commentPtr;

	//since jdk1.2 look only in the last java doc comment...
	nextComment : for (lastCommentIndex = this.scanner.commentPtr; lastCommentIndex >= 0; lastCommentIndex--){
		// skip all non-javadoc comments or those which are after the last modifier
		int commentSourceStart = this.scanner.commentStarts[lastCommentIndex];
		if (commentSourceStart < 0 || // line comment
			this.scanner.commentStops[lastCommentIndex] < 0 || // block comment
			(this.modifiersSourceStart != -1 && this.modifiersSourceStart < commentSourceStart)) // the comment is after the modifier
		{
			continue nextComment;
		}
		// check comment
		deprecated = this.javadocParser.checkDeprecation(lastCommentIndex);
		break nextComment;
	}
	if (deprecated) {
		checkAndSetModifiers(ClassFileConstants.AccDeprecated);
	}
	// modify the modifier source start to point at the first comment
	if (commentPtr >= 0) {
		this.declarationSourceStart = this.scanner.commentStarts[0];
		if (this.declarationSourceStart < 0) this.declarationSourceStart = -this.declarationSourceStart;
	}
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeCatchFormalParameter() {
	// FormalParameter ::= Type VariableDeclaratorId ==> false
	// FormalParameter ::= Modifiers Type VariableDeclaratorId ==> true
	/*
	astStack :
	identifierStack : type identifier
	intStack : dim dim
	 ==>
	astStack : Argument
	identifierStack :
	intStack :
	*/

	this.identifierLengthPtr--;
	char[] parameterName = this.identifierStack[this.identifierPtr];
	long namePositions = this.identifierPositionStack[this.identifierPtr--];
	this.intPtr--; // dimension from the variabledeclaratorid
	TypeReference type = (TypeReference) this.astStack[this.astPtr--];
	this.intPtr -= 3;
	Argument arg =
		new Argument(
			parameterName,
			namePositions,
			type,
			this.intStack[this.intPtr + 1]);// modifiers
	arg.bits &= ~ASTNode.IsArgument;
	// consume annotations
	int length;
	if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
		System.arraycopy(
			this.expressionStack,
			(this.expressionPtr -= length) + 1,
			arg.annotations = new Annotation[length],
			0,
			length);
	}
	pushOnAstStack(arg);
	this.intArrayPtr--;
}
protected void consumeClassBodyDeclaration() {
	// ClassBodyDeclaration ::= Diet Block
	//push an Initializer
	//optimize the push/pop

	super.consumeClassBodyDeclaration();
	Initializer initializer = (Initializer) this.astStack[this.astPtr];
	this.requestor.acceptInitializer(
		initializer.declarationSourceStart,
		initializer.declarationSourceEnd,
		this.intArrayStack[this.intArrayPtr--],
		0,
		this.modifiersSourceStart,
		initializer.block.sourceStart,
		initializer.block.sourceEnd);
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeClassDeclaration() {
	super.consumeClassDeclaration();
	// we know that we have a TypeDeclaration on the top of the astStack
	if (isLocalDeclaration()) {
		// we ignore the local variable declarations
		return;
	}
	this.requestor.exitClass(this.endStatementPosition, // '}' is the end of the body
	 ((TypeDeclaration) this.astStack[this.astPtr]).declarationSourceEnd);
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeClassHeader() {
	//ClassHeader ::= $empty
	super.consumeClassHeader();
	if (isLocalDeclaration()) {
		// we ignore the local variable declarations
		this.intArrayPtr--;
		return;
	}
	TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
	TypeReference[] superInterfaces = typeDecl.superInterfaces;
	char[][] interfaceNames = null;
	int[] interfaceNameStarts = null;
	int[] interfaceNameEnds = null;
	if (superInterfaces != null) {
		int superInterfacesLength = superInterfaces.length;
		interfaceNames = new char[superInterfacesLength][];
		interfaceNameStarts = new int[superInterfacesLength];
		interfaceNameEnds = new int[superInterfacesLength];
		for (int i = 0; i < superInterfacesLength; i++) {
			TypeReference superInterface = superInterfaces[i];
			interfaceNames[i] = CharOperation.concatWith(superInterface.getTypeName(), '.');
			interfaceNameStarts[i] = superInterface.sourceStart;
			interfaceNameEnds[i] = superInterface.sourceEnd;
		}
	}
	// flush the comments related to the class header
	this.scanner.commentPtr = -1;
	TypeReference superclass = typeDecl.superclass;
	if (superclass == null) {
		this.requestor.enterClass(
			typeDecl.declarationSourceStart,
			this.intArrayStack[this.intArrayPtr--],
			typeDecl.modifiers,
			typeDecl.modifiersSourceStart,
			this.typeStartPosition,
			typeDecl.name,
			typeDecl.sourceStart,
			typeDecl.sourceEnd,
			null,
			-1,
			-1,
			interfaceNames,
			interfaceNameStarts,
			interfaceNameEnds,
			this.scanner.currentPosition - 1);
	} else {
		this.requestor.enterClass(
			typeDecl.declarationSourceStart,
			this.intArrayStack[this.intArrayPtr--],
			typeDecl.modifiers,
			typeDecl.modifiersSourceStart,
			this.typeStartPosition,
			typeDecl.name,
			typeDecl.sourceStart,
			typeDecl.sourceEnd,
			CharOperation.concatWith(superclass.getTypeName(), '.'),
			superclass.sourceStart,
			superclass.sourceEnd,
			interfaceNames,
			interfaceNameStarts,
			interfaceNameEnds,
			this.scanner.currentPosition - 1);

	}
}
protected void consumeClassHeaderName1() {
	// ClassHeaderName ::= Modifiersopt 'class' 'Identifier'
	TypeDeclaration typeDecl = new TypeDeclaration(this.compilationUnit.compilationResult);
	if (this.nestedMethod[this.nestedType] == 0) {
		if (this.nestedType != 0) {
			typeDecl.bits |= ASTNode.IsMemberType;
		}
	} else {
		// Record that the block has a declaration for local types
		typeDecl.bits |= ASTNode.IsLocalType;
		markEnclosingMemberWithLocalType();
		blockReal();
	}

	//highlight the name of the type
	long pos = this.identifierPositionStack[this.identifierPtr];
	typeDecl.sourceEnd = (int) pos;
	typeDecl.sourceStart = (int) (pos >>> 32);
	typeDecl.name = this.identifierStack[this.identifierPtr--];
	this.identifierLengthPtr--;

	//compute the declaration source too
	// 'class' and 'interface' push an int position
	this.typeStartPosition = typeDecl.declarationSourceStart = this.intStack[this.intPtr--];
	this.intPtr--;
	int declSourceStart = this.intStack[this.intPtr--];
	typeDecl.modifiersSourceStart = this.intStack[this.intPtr--];
	typeDecl.modifiers = this.intStack[this.intPtr--];
	if (typeDecl.declarationSourceStart > declSourceStart) {
		typeDecl.declarationSourceStart = declSourceStart;
	}
	// consume annotations
	int length;
	if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
		System.arraycopy(
			this.expressionStack,
			(this.expressionPtr -= length) + 1,
			typeDecl.annotations = new Annotation[length],
			0,
			length);
	}
	typeDecl.bodyStart = typeDecl.sourceEnd + 1;
	pushOnAstStack(typeDecl);
	// javadoc
	typeDecl.javadoc = this.javadoc;
	this.javadoc = null;
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeCompilationUnit() {
	// CompilationUnit ::= EnterCompilationUnit PackageDeclarationopt ImportDeclarationsopt
	this.requestor.exitCompilationUnit(this.scanner.source.length - 1);
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeConstructorDeclaration() {
	// ConstructorDeclaration ::= ConstructorHeader ConstructorBody
	super.consumeConstructorDeclaration();
	if (isLocalDeclaration()) {
		// we ignore the local variable declarations
		return;
	}
	ConstructorDeclaration cd = (ConstructorDeclaration) this.astStack[this.astPtr];
	this.requestor.exitConstructor(this.endStatementPosition, cd.declarationSourceEnd);
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeConstructorHeader() {
	// ConstructorHeader ::= ConstructorHeaderName MethodHeaderParameters MethodHeaderThrowsClauseopt
	super.consumeConstructorHeader();
	if (isLocalDeclaration()) {
		// we ignore the local variable declarations
		this.intArrayPtr--;
		return;
	}
	ConstructorDeclaration cd = (ConstructorDeclaration) this.astStack[this.astPtr];
	Argument[] arguments = cd.arguments;
	char[][] argumentTypes = null;
	char[][] argumentNames = null;
	int[] argumentTypeStarts = null;
	int[] argumentTypeEnds = null;
	int[] argumentNameStarts = null;
	int[] argumentNameEnds = null;
	if (arguments != null) {
		int argumentLength = arguments.length;
		argumentTypes = new char[argumentLength][];
		argumentNames = new char[argumentLength][];
		argumentNameStarts = new int[argumentLength];
		argumentNameEnds = new int[argumentLength];
		argumentTypeStarts = new int[argumentLength];
		argumentTypeEnds = new int[argumentLength];
		for (int i = 0; i < argumentLength; i++) {
			Argument argument = arguments[i];
			TypeReference argumentType = argument.type;
			argumentTypes[i] = returnTypeName(argumentType);
			argumentNames[i] = argument.name;
			argumentNameStarts[i] = argument.sourceStart;
			argumentNameEnds[i] = argument.sourceEnd;
			argumentTypeStarts[i] = argumentType.sourceStart;
			argumentTypeEnds[i] = argumentType.sourceEnd;
		}
	}
	TypeReference[] thrownExceptions = cd.thrownExceptions;
	char[][] exceptionTypes = null;
	int[] exceptionTypeStarts = null;
	int[] exceptionTypeEnds = null;
	if (thrownExceptions != null) {
		int thrownExceptionLength = thrownExceptions.length;
		exceptionTypes = new char[thrownExceptionLength][];
		exceptionTypeStarts = new int[thrownExceptionLength];
		exceptionTypeEnds = new int[thrownExceptionLength];
		for (int i = 0; i < thrownExceptionLength; i++) {
			TypeReference exception = thrownExceptions[i];
			exceptionTypes[i] = CharOperation.concatWith(exception.getTypeName(), '.');
			exceptionTypeStarts[i] = exception.sourceStart;
			exceptionTypeEnds[i] = exception.sourceEnd;
		}
	}
	this.requestor
		.enterConstructor(
			cd.declarationSourceStart,
			this.intArrayStack[this.intArrayPtr--],
			cd.modifiers,
			cd.modifiersSourceStart,
			cd.selector,
			cd.sourceStart,
			(int) (this.selectorSourcePositions & 0xFFFFFFFFL),
			// retrieve the source end of the name
			argumentTypes,
			argumentTypeStarts,
			argumentTypeEnds,
			argumentNames,
			argumentNameStarts,
			argumentNameEnds,
			this.rParenPos,
			// right parenthesis
			exceptionTypes,
			exceptionTypeStarts,
			exceptionTypeEnds,
			this.scanner.currentPosition - 1);
}
protected void consumeConstructorHeaderName() {
	// ConstructorHeaderName ::=  Modifiersopt 'Identifier' '('
	ConstructorDeclaration cd = new ConstructorDeclaration(this.compilationUnit.compilationResult);

	//name -- this is not really revelant but we do .....
	cd.selector = this.identifierStack[this.identifierPtr];
	this.selectorSourcePositions = this.identifierPositionStack[this.identifierPtr--];
	this.identifierLengthPtr--;

	//modifiers
	cd.declarationSourceStart = this.intStack[this.intPtr--];
	cd.modifiersSourceStart = this.intStack[this.intPtr--];
	cd.modifiers = this.intStack[this.intPtr--];
	// consume annotations
	int length;
	if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
		System.arraycopy(
			this.expressionStack,
			(this.expressionPtr -= length) + 1,
			cd.annotations = new Annotation[length],
			0,
			length);
	}
	// javadoc
	cd.javadoc = this.javadoc;
	this.javadoc = null;

	//highlight starts at the selector starts
	cd.sourceStart = (int) (this.selectorSourcePositions >>> 32);
	pushOnAstStack(cd);

	cd.sourceEnd = this.lParenPos;
	cd.bodyStart = this.lParenPos + 1;
}
protected void consumeDefaultModifiers() {
	checkComment(); // might update modifiers with AccDeprecated
	pushOnIntStack(this.modifiers); // modifiers
	pushOnIntStack(-1);
	pushOnIntStack(
		this.declarationSourceStart >= 0 ? this.declarationSourceStart : this.scanner.startPosition);
	resetModifiers();
	pushOnExpressionStackLengthStack(0);
}
protected void consumeDiet() {
	// Diet ::= $empty
	super.consumeDiet();
	/* persisting javadoc positions
	 * Will be consume in consumeClassBodyDeclaration
	 */
	pushOnIntArrayStack(getJavaDocPositions());
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeEnterCompilationUnit() {
	// EnterCompilationUnit ::= $empty
	this.requestor.enterCompilationUnit();
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeEnterVariable() {
	// EnterVariable ::= $empty
	boolean isLocalDeclaration = isLocalDeclaration();
	if (!isLocalDeclaration && (this.variablesCounter[this.nestedType] != 0)) {
		this.requestor.exitField(this.lastFieldBodyEndPosition, this.lastFieldEndPosition);
	}
	char[] varName = this.identifierStack[this.identifierPtr];
	long namePosition = this.identifierPositionStack[this.identifierPtr--];
	int extendedTypeDimension = this.intStack[this.intPtr--];

	AbstractVariableDeclaration declaration;
	if (this.nestedMethod[this.nestedType] != 0) {
		// create the local variable declarations
		declaration =
			new LocalDeclaration(varName, (int) (namePosition >>> 32), (int) namePosition);
	} else {
		// create the field declaration
		declaration =
			new FieldDeclaration(varName, (int) (namePosition >>> 32), (int) namePosition);
	}
	this.identifierLengthPtr--;
	TypeReference type;
	int variableIndex = this.variablesCounter[this.nestedType];
	int typeDim = 0;
	if (variableIndex == 0) {
		// first variable of the declaration (FieldDeclaration or LocalDeclaration)
		if (this.nestedMethod[this.nestedType] != 0) {
			// local declaration
			declaration.declarationSourceStart = this.intStack[this.intPtr--];
			declaration.modifiersSourceStart = this.intStack[this.intPtr--];
			declaration.modifiers = this.intStack[this.intPtr--];
			type = getTypeReference(typeDim = this.intStack[this.intPtr--]); // type dimension
			pushOnAstStack(type);
		} else {
			// field declaration
			type = getTypeReference(typeDim = this.intStack[this.intPtr--]); // type dimension
			pushOnAstStack(type);
			declaration.declarationSourceStart = this.intStack[this.intPtr--];
			declaration.modifiersSourceStart = this.intStack[this.intPtr--];
			declaration.modifiers = this.intStack[this.intPtr--];
		}
		// consume annotations
		int length;
		if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
			System.arraycopy(
				this.expressionStack,
				(this.expressionPtr -= length) + 1,
				declaration.annotations = new Annotation[length],
				0,
				length);
		}
	} else {
		type = (TypeReference) this.astStack[this.astPtr - variableIndex];
		typeDim = type.dimensions();
		AbstractVariableDeclaration previousVariable =
			(AbstractVariableDeclaration) this.astStack[this.astPtr];
		declaration.declarationSourceStart = previousVariable.declarationSourceStart;
		declaration.modifiers = previousVariable.modifiers;
		declaration.modifiersSourceStart = previousVariable.modifiersSourceStart;
		final Annotation[] annotations = previousVariable.annotations;
		if (annotations != null) {
			final int annotationsLength = annotations.length;
			System.arraycopy(annotations, 0, declaration.annotations = new Annotation[annotationsLength], 0, annotationsLength);
		}
	}

	this.localIntPtr = this.intPtr;

	if (extendedTypeDimension == 0) {
		declaration.type = type;
	} else {
		int dimension = typeDim + extendedTypeDimension;
		declaration.type = copyDims(type, dimension);
	}
	this.variablesCounter[this.nestedType]++;
	this.nestedMethod[this.nestedType]++;
	pushOnAstStack(declaration);

	int[] javadocPositions = this.intArrayStack[this.intArrayPtr];
	if (!isLocalDeclaration) {
		this.requestor
			.enterField(
				declaration.declarationSourceStart,
				javadocPositions,
				declaration.modifiers,
				declaration.modifiersSourceStart,
				returnTypeName(declaration.type),
				type.sourceStart,
				type.sourceEnd,
				this.typeDims,
				varName,
				(int) (namePosition >>> 32),
				(int) namePosition,
				extendedTypeDimension,
				extendedTypeDimension == 0 ? -1 : this.endPosition);
	}
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeExitVariableWithInitialization() {
	// ExitVariableWithInitialization ::= $empty
	// the scanner is located after the comma or the semi-colon.
	// we want to include the comma or the semi-colon
	super.consumeExitVariableWithInitialization();
	this.nestedMethod[this.nestedType]--;
	this.lastFieldEndPosition = this.scanner.currentPosition - 1;
	this.lastFieldBodyEndPosition = 	((AbstractVariableDeclaration) this.astStack[this.astPtr]).initialization.sourceEnd;
}
protected void consumeExitVariableWithoutInitialization() {
	// ExitVariableWithoutInitialization ::= $empty
	// do nothing by default
	super.consumeExitVariableWithoutInitialization();
	this.nestedMethod[this.nestedType]--;
	this.lastFieldEndPosition = this.scanner.currentPosition - 1;
	this.lastFieldBodyEndPosition = this.scanner.startPosition - 1;
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeFieldDeclaration() {
	// See consumeLocalVariableDeclarationDefaultModifier() in case of change: duplicated code
	// FieldDeclaration ::= Modifiersopt Type VariableDeclarators ';'
	// the super.consumeFieldDeclaration will reinitialize the variableCounter[nestedType]
	int variableIndex = this.variablesCounter[this.nestedType];
	super.consumeFieldDeclaration();
	this.intArrayPtr--;
	if (isLocalDeclaration())
		return;
	if (variableIndex != 0) {
		this.requestor.exitField(this.lastFieldBodyEndPosition, this.lastFieldEndPosition);
	}
}
protected void consumeFormalParameter(boolean isVarArgs) {
	// FormalParameter ::= Type VariableDeclaratorId ==> false
	// FormalParameter ::= Modifiers Type VariableDeclaratorId ==> true
	/*
	astStack :
	identifierStack : type identifier
	intStack : dim dim
	 ==>
	astStack : Argument
	identifierStack :
	intStack :
	*/

	this.identifierLengthPtr--;
	char[] parameterName = this.identifierStack[this.identifierPtr];
	long namePositions = this.identifierPositionStack[this.identifierPtr--];
	int extendedDimensions = this.intStack[this.intPtr--];
	int endOfEllipsis = 0;
	if (isVarArgs) {
		endOfEllipsis = this.intStack[this.intPtr--];
	}
	int firstDimensions = this.intStack[this.intPtr--];
	final int typeDimensions = firstDimensions + extendedDimensions;
	TypeReference type = getTypeReference(typeDimensions);
	if (isVarArgs) {
		type = copyDims(type, typeDimensions + 1);
		if (extendedDimensions == 0) {
			type.sourceEnd = endOfEllipsis;
		}
		type.bits |= ASTNode.IsVarArgs; // set isVarArgs
	}
	this.intPtr -= 3;
	Argument arg =
		new Argument(
			parameterName,
			namePositions,
			type,
			this.intStack[this.intPtr + 1]);// modifiers
	// consume annotations
	int length;
	if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
		System.arraycopy(
			this.expressionStack,
			(this.expressionPtr -= length) + 1,
			arg.annotations = new Annotation[length],
			0,
			length);
		RecoveredType currentRecoveryType = this.currentRecoveryType();
		if (currentRecoveryType != null)
			currentRecoveryType.annotationsConsumed(arg.annotations);
	}
	pushOnAstStack(arg);
	this.intArrayPtr--;
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeInterfaceDeclaration() {
	super.consumeInterfaceDeclaration();
	// we know that we have a TypeDeclaration on the top of the astStack
	if (isLocalDeclaration()) {
		// we ignore the local variable declarations
		return;
	}
	this.requestor.exitInterface(this.endStatementPosition, // the '}' is the end of the body
	 ((TypeDeclaration) this.astStack[this.astPtr]).declarationSourceEnd);
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeInterfaceHeader() {
	//InterfaceHeader ::= $empty
	super.consumeInterfaceHeader();
	if (isLocalDeclaration()) {
		// we ignore the local variable declarations
		this.intArrayPtr--;
		return;
	}
	TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];
	TypeReference[] superInterfaces = typeDecl.superInterfaces;
	char[][] interfaceNames = null;
	int[] interfaceNameStarts = null;
	int[] interfacenameEnds = null;
	int superInterfacesLength = 0;
	if (superInterfaces != null) {
		superInterfacesLength = superInterfaces.length;
		interfaceNames = new char[superInterfacesLength][];
		interfaceNameStarts = new int[superInterfacesLength];
		interfacenameEnds = new int[superInterfacesLength];
	}
	if (superInterfaces != null) {
		for (int i = 0; i < superInterfacesLength; i++) {
			TypeReference superInterface = superInterfaces[i];
			interfaceNames[i] = CharOperation.concatWith(superInterface.getTypeName(), '.');
			interfaceNameStarts[i] = superInterface.sourceStart;
			interfacenameEnds[i] = superInterface.sourceEnd;
		}
	}
	// flush the comments related to the interface header
	this.scanner.commentPtr = -1;
	this.requestor.enterInterface(
		typeDecl.declarationSourceStart,
		this.intArrayStack[this.intArrayPtr--],
		typeDecl.modifiers,
		typeDecl.modifiersSourceStart,
		this.typeStartPosition,
		typeDecl.name,
		typeDecl.sourceStart,
		typeDecl.sourceEnd,
		interfaceNames,
		interfaceNameStarts,
		interfacenameEnds,
		this.scanner.currentPosition - 1);
}
protected void consumeInterfaceHeaderName1() {
	// InterfaceHeaderName ::= Modifiersopt 'interface' 'Identifier'
	TypeDeclaration typeDecl = new TypeDeclaration(this.compilationUnit.compilationResult);
	if (this.nestedMethod[this.nestedType] == 0) {
		if (this.nestedType != 0) {
			typeDecl.bits |= ASTNode.IsMemberType;
		}
	} else {
		// Record that the block has a declaration for local types
		typeDecl.bits |= ASTNode.IsLocalType;
		markEnclosingMemberWithLocalType();
		blockReal();
	}

	//highlight the name of the type
	long pos = this.identifierPositionStack[this.identifierPtr];
	typeDecl.sourceEnd = (int) pos;
	typeDecl.sourceStart = (int) (pos >>> 32);
	typeDecl.name = this.identifierStack[this.identifierPtr--];
	this.identifierLengthPtr--;

	//compute the declaration source too
	// 'class' and 'interface' push an int position
	this.typeStartPosition = typeDecl.declarationSourceStart = this.intStack[this.intPtr--];
	this.intPtr--;
	int declSourceStart = this.intStack[this.intPtr--];
	typeDecl.modifiersSourceStart = this.intStack[this.intPtr--];
	typeDecl.modifiers = this.intStack[this.intPtr--] | ClassFileConstants.AccInterface;
	if (typeDecl.declarationSourceStart > declSourceStart) {
		typeDecl.declarationSourceStart = declSourceStart;
	}
	// consume annotations
	int length;
	if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
		System.arraycopy(
			this.expressionStack,
			(this.expressionPtr -= length) + 1,
			typeDecl.annotations = new Annotation[length],
			0,
			length);
	}
	typeDecl.bodyStart = typeDecl.sourceEnd + 1;
	pushOnAstStack(typeDecl);
	// javadoc
	typeDecl.javadoc = this.javadoc;
	this.javadoc = null;
}
protected void consumeInternalCompilationUnit() {
	// InternalCompilationUnit ::= PackageDeclaration
	// InternalCompilationUnit ::= PackageDeclaration ImportDeclarations ReduceImports
	// InternalCompilationUnit ::= ImportDeclarations ReduceImports
}
protected void consumeInternalCompilationUnitWithTypes() {
	// InternalCompilationUnit ::= PackageDeclaration ImportDeclarations ReduceImports TypeDeclarations
	// InternalCompilationUnit ::= PackageDeclaration TypeDeclarations
	// InternalCompilationUnit ::= TypeDeclarations
	// InternalCompilationUnit ::= ImportDeclarations ReduceImports TypeDeclarations
	// consume type declarations
	int length;
	if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
		this.compilationUnit.types = new TypeDeclaration[length];
		this.astPtr -= length;
		System.arraycopy(this.astStack, this.astPtr + 1, this.compilationUnit.types, 0, length);
	}
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeLocalVariableDeclaration() {
	// See consumeLocalVariableDeclarationDefaultModifier() in case of change: duplicated code
	// FieldDeclaration ::= Modifiersopt Type VariableDeclarators ';'

	super.consumeLocalVariableDeclaration();
	this.intArrayPtr--;
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeMethodDeclaration(boolean isNotAbstract) {
	// MethodDeclaration ::= MethodHeader MethodBody
	// AbstractMethodDeclaration ::= MethodHeader ';'
	super.consumeMethodDeclaration(isNotAbstract);
	if (isLocalDeclaration()) {
		// we ignore the local variable declarations
		return;
	}
	MethodDeclaration md = (MethodDeclaration) this.astStack[this.astPtr];
	this.requestor.exitMethod(this.endStatementPosition, md.declarationSourceEnd);
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeMethodHeader() {
	// MethodHeader ::= MethodHeaderName MethodHeaderParameters MethodHeaderExtendedDims ThrowsClauseopt
	super.consumeMethodHeader();
	if (isLocalDeclaration()) {
		// we ignore the local variable declarations
		this.intArrayPtr--;
		return;
	}
	MethodDeclaration md = (MethodDeclaration) this.astStack[this.astPtr];

	TypeReference returnType = md.returnType;
	char[] returnTypeName = returnTypeName(returnType);
	Argument[] arguments = md.arguments;
	char[][] argumentTypes = null;
	char[][] argumentNames = null;
	int[] argumentTypeStarts = null;
	int[] argumentTypeEnds = null;
	int[] argumentNameStarts = null;
	int[] argumentNameEnds = null;
	if (arguments != null) {
		int argumentLength = arguments.length;
		argumentTypes = new char[argumentLength][];
		argumentNames = new char[argumentLength][];
		argumentNameStarts = new int[argumentLength];
		argumentNameEnds = new int[argumentLength];
		argumentTypeStarts = new int[argumentLength];
		argumentTypeEnds = new int[argumentLength];
		for (int i = 0; i < argumentLength; i++) {
			Argument argument = arguments[i];
			TypeReference argumentType = argument.type;
			argumentTypes[i] = returnTypeName(argumentType);
			argumentNames[i] = argument.name;
			argumentNameStarts[i] = argument.sourceStart;
			argumentNameEnds[i] = argument.sourceEnd;
			argumentTypeStarts[i] = argumentType.sourceStart;
			argumentTypeEnds[i] = argumentType.sourceEnd;
		}
	}
	TypeReference[] thrownExceptions = md.thrownExceptions;
	char[][] exceptionTypes = null;
	int[] exceptionTypeStarts = null;
	int[] exceptionTypeEnds = null;
	if (thrownExceptions != null) {
		int thrownExceptionLength = thrownExceptions.length;
		exceptionTypeStarts = new int[thrownExceptionLength];
		exceptionTypeEnds = new int[thrownExceptionLength];
		exceptionTypes = new char[thrownExceptionLength][];
		for (int i = 0; i < thrownExceptionLength; i++) {
			TypeReference exception = thrownExceptions[i];
			exceptionTypes[i] = CharOperation.concatWith(exception.getTypeName(), '.');
			exceptionTypeStarts[i] = exception.sourceStart;
			exceptionTypeEnds[i] = exception.sourceEnd;
		}
	}
	this.requestor
		.enterMethod(
			md.declarationSourceStart,
			this.intArrayStack[this.intArrayPtr--],
			md.modifiers,
			md.modifiersSourceStart,
			returnTypeName,
			returnType.sourceStart,
			returnType.sourceEnd,
			this.typeDims,
			md.selector,
			md.sourceStart,
			(int) (this.selectorSourcePositions & 0xFFFFFFFFL),
			argumentTypes,
			argumentTypeStarts,
			argumentTypeEnds,
			argumentNames,
			argumentNameStarts,
			argumentNameEnds,
			this.rParenPos,
			this.extendsDim,
			this.extendsDim == 0 ? -1 : this.endPosition,
			exceptionTypes,
			exceptionTypeStarts,
			exceptionTypeEnds,
			this.scanner.currentPosition - 1);
}
protected void consumeMethodHeaderExtendedDims() {
	// MethodHeaderExtendedDims ::= Dimsopt
	// now we update the returnType of the method
	MethodDeclaration md = (MethodDeclaration) this.astStack[this.astPtr];
	int extendedDims = this.intStack[this.intPtr--];
	this.extendsDim = extendedDims;
	if (extendedDims != 0) {
		TypeReference returnType = md.returnType;
		md.sourceEnd = this.endPosition;
		int dims = returnType.dimensions() + extendedDims;
		md.returnType = copyDims(returnType, dims);
		if (this.currentToken == TokenNameLBRACE) {
			md.bodyStart = this.endPosition + 1;
		}
	}
}
protected void consumeMethodHeaderName(boolean isAnnotationMethod) {
	// MethodHeaderName ::= Modifiersopt Type 'Identifier' '('
	MethodDeclaration md = null;
	if(isAnnotationMethod) {
		md = new AnnotationMethodDeclaration(this.compilationUnit.compilationResult);
	} else {
		md = new MethodDeclaration(this.compilationUnit.compilationResult);
	}
	//name
	md.selector = this.identifierStack[this.identifierPtr];
	this.selectorSourcePositions = this.identifierPositionStack[this.identifierPtr--];
	this.identifierLengthPtr--;
	//type
	md.returnType = getTypeReference(this.typeDims = this.intStack[this.intPtr--]);
	//modifiers
	md.declarationSourceStart = this.intStack[this.intPtr--];
	md.modifiersSourceStart = this.intStack[this.intPtr--];
	md.modifiers = this.intStack[this.intPtr--];
	// consume annotations
	int length;
	if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
		System.arraycopy(
			this.expressionStack,
			(this.expressionPtr -= length) + 1,
			md.annotations = new Annotation[length],
			0,
			length);
	}
	// javadoc
	md.javadoc = this.javadoc;
	this.javadoc = null;

	//highlight starts at selector start
	md.sourceStart = (int) (this.selectorSourcePositions >>> 32);
	pushOnAstStack(md);
	md.bodyStart = this.scanner.currentPosition-1;
}
protected void consumeModifiers() {
	checkComment(); // might update modifiers with AccDeprecated
	pushOnIntStack(this.modifiers); // modifiers
	pushOnIntStack(this.modifiersSourceStart);
	pushOnIntStack(
		this.declarationSourceStart >= 0 ? this.declarationSourceStart : this.modifiersSourceStart);
	resetModifiers();
}
protected void consumePackageComment() {
	// get possible comment for syntax since 1.5
	if(this.options.sourceLevel >= ClassFileConstants.JDK1_5) {
		checkComment();
	} else {
		pushOnIntArrayStack(getJavaDocPositions());
	}
	resetModifiers();
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumePackageDeclarationName() {
	/*
	 * Javadoc positions are persisted in consumePackageComment
	 */
	super.consumePackageDeclarationName();
	ImportReference importReference = this.compilationUnit.currentPackage;

	this.requestor.acceptPackage(
		importReference.declarationSourceStart,
		importReference.declarationSourceEnd,
		this.intArrayStack[this.intArrayPtr--],
		CharOperation.concatWith(importReference.getImportName(), '.'),
		importReference.sourceStart);
}
/*
*
* INTERNAL USE-ONLY
*/
protected void consumePackageDeclarationNameWithModifiers() {
	super.consumePackageDeclarationNameWithModifiers();
	ImportReference importReference = this.compilationUnit.currentPackage;

	this.requestor.acceptPackage(
		importReference.declarationSourceStart,
		importReference.declarationSourceEnd,
		this.intArrayStack[this.intArrayPtr--],
		CharOperation.concatWith(importReference.getImportName(), '.'),
		importReference.sourceStart);
}
protected void consumePushModifiers() {
	checkComment(); // might update modifiers with AccDeprecated
	pushOnIntStack(this.modifiers); // modifiers
	if (this.modifiersSourceStart < 0) {
		pushOnIntStack(-1);
		pushOnIntStack(
			this.declarationSourceStart >= 0 ? this.declarationSourceStart : this.scanner.startPosition);
	} else {
		pushOnIntStack(this.modifiersSourceStart);
		pushOnIntStack(
			this.declarationSourceStart >= 0 ? this.declarationSourceStart : this.modifiersSourceStart);
	}
	resetModifiers();
	pushOnExpressionStackLengthStack(0);
}
protected void consumePushRealModifiers() {
	checkComment(); // might update modifiers with AccDeprecated
	pushOnIntStack(this.modifiers); // modifiers
	if (this.modifiersSourceStart < 0) {
		pushOnIntStack(-1);
		pushOnIntStack(
			this.declarationSourceStart >= 0 ? this.declarationSourceStart : this.scanner.startPosition);
	} else {
		pushOnIntStack(this.modifiersSourceStart);
		pushOnIntStack(
			this.declarationSourceStart >= 0 ? this.declarationSourceStart : this.modifiersSourceStart);
	}
	resetModifiers();
}
protected void consumeSingleStaticImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' 'static' Name

	/* persisting javadoc positions */
	pushOnIntArrayStack(getJavaDocPositions());

	super.consumeSingleStaticImportDeclarationName();
	ImportReference importReference = (ImportReference) this.astStack[this.astPtr];
	this.requestor.acceptImport(
		importReference.declarationSourceStart,
		importReference.declarationSourceEnd,
		this.intArrayStack[this.intArrayPtr--],
		CharOperation.concatWith(importReference.getImportName(), '.'),
		importReference.sourceStart,
		false,
		ClassFileConstants.AccStatic);
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeSingleTypeImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' Name

	/* persisting javadoc positions */
	pushOnIntArrayStack(getJavaDocPositions());

	super.consumeSingleTypeImportDeclarationName();
	ImportReference importReference = (ImportReference) this.astStack[this.astPtr];
	this.requestor.acceptImport(
		importReference.declarationSourceStart,
		importReference.declarationSourceEnd,
		this.intArrayStack[this.intArrayPtr--],
		CharOperation.concatWith(importReference.getImportName(), '.'),
		importReference.sourceStart,
		false,
		ClassFileConstants.AccDefault);
}
protected void consumeStaticImportOnDemandDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' 'static' Name '.' '*'

	/* persisting javadoc positions */
	pushOnIntArrayStack(getJavaDocPositions());

	super.consumeStaticImportOnDemandDeclarationName();
	ImportReference importReference = (ImportReference) this.astStack[this.astPtr];
	this.requestor.acceptImport(
		importReference.declarationSourceStart,
		importReference.declarationSourceEnd,
		this.intArrayStack[this.intArrayPtr--],
		CharOperation.concatWith(importReference.getImportName(), '.'),
		importReference.sourceStart,
		true,
		ClassFileConstants.AccStatic);
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeStaticInitializer() {
	// StaticInitializer ::=  StaticOnly Block
	//push an Initializer
	//optimize the push/pop
	super.consumeStaticInitializer();
	Initializer initializer = (Initializer) this.astStack[this.astPtr];
	this.requestor.acceptInitializer(
		initializer.declarationSourceStart,
		initializer.declarationSourceEnd,
		this.intArrayStack[this.intArrayPtr--],
		ClassFileConstants.AccStatic,
		this.intStack[this.intPtr--],
		initializer.block.sourceStart,
		initializer.declarationSourceEnd);
}
protected void consumeStaticOnly() {
	// StaticOnly ::= 'static'
	checkComment(); // might update declaration source start
	pushOnIntStack(this.modifiersSourceStart);
	pushOnIntStack(this.scanner.currentPosition);
	pushOnIntStack(
		this.declarationSourceStart >= 0 ? this.declarationSourceStart : this.modifiersSourceStart);
	jumpOverMethodBody();
	this.nestedMethod[this.nestedType]++;
	resetModifiers();
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeTypeImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' Name '.' '*'

	/* persisting javadoc positions */
	pushOnIntArrayStack(getJavaDocPositions());

	super.consumeTypeImportOnDemandDeclarationName();
	ImportReference importReference = (ImportReference) this.astStack[this.astPtr];
	this.requestor.acceptImport(
		importReference.declarationSourceStart,
		importReference.declarationSourceEnd,
		this.intArrayStack[this.intArrayPtr--],
		CharOperation.concatWith(importReference.getImportName(), '.'),
		importReference.sourceStart,
		true,
		ClassFileConstants.AccDefault);
}
/*
 * Flush javadocs defined prior to a given positions.
 *
 * Note: javadocs are stacked in syntactical order
 *
 * Either answer given <position>, or the end position of a comment line
 * immediately following the <position> (same line)
 *
 * e.g.
 * void foo(){
 * } // end of method foo
 */

public int flushCommentsDefinedPriorTo(int position) {

	return this.lastFieldEndPosition = super.flushCommentsDefinedPriorTo(position);
}
public CompilationUnitDeclaration endParse(int act) {
	if (this.scanner.recordLineSeparator) {
		this.requestor.acceptLineSeparatorPositions(this.scanner.getLineEnds());
	}
	return super.endParse(act);
}
public void initialize(boolean initializeNLS) {
	//positionning the parser for a new compilation unit
	//avoiding stack reallocation and all that....
	super.initialize(initializeNLS);
	this.intArrayPtr = -1;
}
public void initialize() {
	//positionning the parser for a new compilation unit
	//avoiding stack reallocation and all that....
	super.initialize();
	this.intArrayPtr = -1;
}
/*
 *
 * INTERNAL USE-ONLY
 */
private boolean isLocalDeclaration() {
	int nestedDepth = this.nestedType;
	while (nestedDepth >= 0) {
		if (this.nestedMethod[nestedDepth] != 0) {
			return true;
		}
		nestedDepth--;
	}
	return false;
}
protected void parse() {
	this.diet = true;
	super.parse();
}
/*
 * Investigate one entire unit.
 */
public void parseCompilationUnit(ICompilationUnit unit) {
	char[] regionSource = unit.getContents();
	try {
		initialize(true);
		goForCompilationUnit();
		this.referenceContext =
			this.compilationUnit =
				new CompilationUnitDeclaration(
					problemReporter(),
					new CompilationResult(unit, 0, 0, this.options.maxProblemsPerUnit),
					regionSource.length);
		this.scanner.resetTo(0, regionSource.length);
		this.scanner.setSource(regionSource);
		parse();
	} catch (AbortCompilation ex) {
		// ignore this exception
	}
}
/*
 * Investigate one constructor declaration.
 */
public void parseConstructor(char[] regionSource) {
	try {
		initialize();
		goForClassBodyDeclarations();
		this.referenceContext =
			this.compilationUnit =
				new CompilationUnitDeclaration(
					problemReporter(),
					new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit),
					regionSource.length);
		this.scanner.resetTo(0, regionSource.length);
		this.scanner.setSource(regionSource);
		parse();
	} catch (AbortCompilation ex) {
		// ignore this exception
	}
}
/*
 * Investigate one field declaration statement (might have multiple declarations in it).
 */
public void parseField(char[] regionSource) {
	try {
		initialize();
		goForFieldDeclaration();
		this.referenceContext =
			this.compilationUnit =
				new CompilationUnitDeclaration(
					problemReporter(),
					new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit),
					regionSource.length);
		this.scanner.resetTo(0, regionSource.length);
		this.scanner.setSource(regionSource);
		parse();
	} catch (AbortCompilation ex) {
		// ignore this exception
	}

}
/*
 * Investigate one import statement declaration.
 */
public void parseImport(char[] regionSource) {
	try {
		initialize();
		goForImportDeclaration();
		this.referenceContext =
			this.compilationUnit =
				new CompilationUnitDeclaration(
					problemReporter(),
					new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit),
					regionSource.length);
		this.scanner.resetTo(0, regionSource.length);
		this.scanner.setSource(regionSource);
		parse();
	} catch (AbortCompilation ex) {
		// ignore this exception
	}

}
/*
 * Investigate one initializer declaration.
 * regionSource need to content exactly an initializer declaration.
 * e.g: static { i = 4; }
 * { name = "test"; }
 */
public void parseInitializer(char[] regionSource) {
	try {
		initialize();
		goForInitializer();
		this.referenceContext =
			this.compilationUnit =
				new CompilationUnitDeclaration(
					problemReporter(),
					new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit),
					regionSource.length);
		this.scanner.resetTo(0, regionSource.length);
		this.scanner.setSource(regionSource);
		parse();
	} catch (AbortCompilation ex) {
		// ignore this exception
	}

}
/*
 * Investigate one method declaration.
 */
public void parseMethod(char[] regionSource) {
	try {
		initialize();
		goForGenericMethodDeclaration();
		this.referenceContext =
			this.compilationUnit =
				new CompilationUnitDeclaration(
					problemReporter(),
					new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit),
					regionSource.length);
		this.scanner.resetTo(0, regionSource.length);
		this.scanner.setSource(regionSource);
		parse();
	} catch (AbortCompilation ex) {
		// ignore this exception
	}
}
/*
 * Investigate one package statement declaration.
 */
public void parsePackage(char[] regionSource) {
	try {
		initialize();
		goForPackageDeclaration();
		this.referenceContext =
			this.compilationUnit =
				new CompilationUnitDeclaration(
					problemReporter(),
					new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit),
					regionSource.length);
		this.scanner.resetTo(0, regionSource.length);
		this.scanner.setSource(regionSource);
		parse();
	} catch (AbortCompilation ex) {
		// ignore this exception
	}

}
/*
 * Investigate one type declaration, its fields, methods and member types.
 */
public void parseType(char[] regionSource) {
	try {
		initialize();
		goForTypeDeclaration();
		this.referenceContext =
			this.compilationUnit =
				new CompilationUnitDeclaration(
					problemReporter(),
					new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit),
					regionSource.length);
		this.scanner.resetTo(0, regionSource.length);
		this.scanner.setSource(regionSource);
		parse();
	} catch (AbortCompilation ex) {
		// ignore this exception
	}

}
/**
 * Returns this parser's problem reporter initialized with its reference context.
 * Also it is assumed that a problem is going to be reported, so initializes
 * the compilation result's line positions.
 *
 * @return ProblemReporter
 */
public ProblemReporter problemReporter() {
	this.problemReporter.referenceContext = this.referenceContext;
	return this.problemReporter;
}
protected void pushOnIntArrayStack(int[] positions) {

	int stackLength = this.intArrayStack.length;
	if (++this.intArrayPtr >= stackLength) {
		System.arraycopy(
			this.intArrayStack, 0,
			this.intArrayStack = new int[stackLength + StackIncrement][], 0,
			stackLength);
	}
	this.intArrayStack[this.intArrayPtr] = positions;
}
protected void resetModifiers() {
	super.resetModifiers();
	this.declarationSourceStart = -1;
}
/*
 * Syntax error was detected. Will attempt to perform some recovery action in order
 * to resume to the regular parse loop.
 */
protected boolean resumeOnSyntaxError() {
	return false;
}
/*
 * Answer a char array representation of the type name formatted like:
 * - type name + dimensions
 * Example:
 * "A[][]".toCharArray()
 * "java.lang.String".toCharArray()
 */
private char[] returnTypeName(TypeReference type) {
	int dimension = type.dimensions();
	if (dimension != 0) {
		char[] dimensionsArray = new char[dimension * 2];
		for (int i = 0; i < dimension; i++) {
			dimensionsArray[i*2] = '[';
			dimensionsArray[(i*2) + 1] = ']';
		}
		return CharOperation.concat(
			CharOperation.concatWith(type.getTypeName(), '.'),
			dimensionsArray);
	}
	return CharOperation.concatWith(type.getTypeName(), '.');
}
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("intArrayPtr = " + this.intArrayPtr + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	buffer.append(super.toString());
	return buffer.toString();
}
/**
 * INTERNAL USE ONLY
 */
protected TypeReference typeReference(
	int dim,
	int localIdentifierPtr,
	int localIdentifierLengthPtr) {
	/* build a Reference on a variable that may be qualified or not
	 * This variable is a type reference and dim will be its dimensions.
	 * We don't have any side effect on the stacks' pointers.
	 */

	int length;
	TypeReference ref;
	if ((length = this.identifierLengthStack[localIdentifierLengthPtr]) == 1) {
		// single variable reference
		if (dim == 0) {
			ref =
				new SingleTypeReference(
					this.identifierStack[localIdentifierPtr],
					this.identifierPositionStack[localIdentifierPtr--]);
		} else {
			ref =
				new ArrayTypeReference(
					this.identifierStack[localIdentifierPtr],
					dim,
					this.identifierPositionStack[localIdentifierPtr--]);
			ref.sourceEnd = this.endPosition;
		}
	} else {
		if (length < 0) { //flag for precompiled type reference on base types
			ref = TypeReference.baseTypeReference(-length, dim);
			ref.sourceStart = this.intStack[this.localIntPtr--];
			if (dim == 0) {
				ref.sourceEnd = this.intStack[this.localIntPtr--];
			} else {
				this.localIntPtr--;
				ref.sourceEnd = this.endPosition;
			}
		} else { //Qualified variable reference
			char[][] tokens = new char[length][];
			localIdentifierPtr -= length;
			long[] positions = new long[length];
			System.arraycopy(this.identifierStack, localIdentifierPtr + 1, tokens, 0, length);
			System.arraycopy(
				this.identifierPositionStack,
				localIdentifierPtr + 1,
				positions,
				0,
				length);
			if (dim == 0)
				ref = new QualifiedTypeReference(tokens, positions);
			else
				ref = new ArrayQualifiedTypeReference(tokens, dim, positions);
		}
	}
	return ref;
}
}
