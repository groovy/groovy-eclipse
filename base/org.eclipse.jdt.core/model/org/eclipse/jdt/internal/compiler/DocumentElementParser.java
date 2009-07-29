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
	intArrayStack = new int[30][];
	this.options = options;
	this.javadocParser.checkDocComment = false;
	
	this.setMethodsFullRecovery(false);
	this.setStatementsRecovery(false);
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
	pushOnIntArrayStack(this.getJavaDocPositions());
	boolean deprecated = false;
	int lastCommentIndex = -1;
	int commentPtr = scanner.commentPtr;

	//since jdk1.2 look only in the last java doc comment...
	nextComment : for (lastCommentIndex = scanner.commentPtr; lastCommentIndex >= 0; lastCommentIndex--){
		//look for @deprecated into the first javadoc comment preceeding the declaration
		int commentSourceStart = scanner.commentStarts[lastCommentIndex];
		// javadoc only (non javadoc comment have negative end positions.)
		if (modifiersSourceStart != -1 && modifiersSourceStart < commentSourceStart) {
			continue nextComment;
		}
		if (scanner.commentStops[lastCommentIndex] < 0) {
			continue nextComment;
		}
		deprecated =
			this.javadocParser.checkDeprecation(lastCommentIndex);
		break nextComment;
	}
	if (deprecated) {
		checkAndSetModifiers(ClassFileConstants.AccDeprecated);
	}
	// modify the modifier source start to point at the first comment
	if (commentPtr >= 0) {
		declarationSourceStart = scanner.commentStarts[0];
	}
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeClassBodyDeclaration() {
	// ClassBodyDeclaration ::= Diet Block
	//push an Initializer
	//optimize the push/pop

	super.consumeClassBodyDeclaration();
	Initializer initializer = (Initializer) astStack[astPtr];
	requestor.acceptInitializer(
		initializer.declarationSourceStart,
		initializer.declarationSourceEnd,
		intArrayStack[intArrayPtr--], 
		0,
		modifiersSourceStart, 
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
	requestor.exitClass(endStatementPosition, // '}' is the end of the body 
	 ((TypeDeclaration) astStack[astPtr]).declarationSourceEnd);
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
		intArrayPtr--;
		return;
	}
	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];
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
	scanner.commentPtr = -1;
	TypeReference superclass = typeDecl.superclass;
	if (superclass == null) {
		requestor.enterClass(
			typeDecl.declarationSourceStart, 
			intArrayStack[intArrayPtr--], 
			typeDecl.modifiers, 
			typeDecl.modifiersSourceStart, 
			typeStartPosition, 
			typeDecl.name, 
			typeDecl.sourceStart, 
			typeDecl.sourceEnd, 
			null, 
			-1, 
			-1, 
			interfaceNames, 
			interfaceNameStarts, 
			interfaceNameEnds, 
			scanner.currentPosition - 1); 
	} else {
		requestor.enterClass(
			typeDecl.declarationSourceStart, 
			intArrayStack[intArrayPtr--], 
			typeDecl.modifiers, 
			typeDecl.modifiersSourceStart, 
			typeStartPosition, 
			typeDecl.name, 
			typeDecl.sourceStart, 
			typeDecl.sourceEnd, 
			CharOperation.concatWith(superclass.getTypeName(), '.'), 
			superclass.sourceStart, 
			superclass.sourceEnd, 
			interfaceNames, 
			interfaceNameStarts, 
			interfaceNameEnds, 
			scanner.currentPosition - 1); 

	}
}
protected void consumeClassHeaderName1() {
	// ClassHeaderName ::= Modifiersopt 'class' 'Identifier'
	TypeDeclaration typeDecl = new TypeDeclaration(this.compilationUnit.compilationResult);
	if (nestedMethod[nestedType] == 0) {
		if (nestedType != 0) {
			typeDecl.bits |= ASTNode.IsMemberType;
		}
	} else {
		// Record that the block has a declaration for local types
		typeDecl.bits |= ASTNode.IsLocalType;
		markEnclosingMemberWithLocalType();
		blockReal();
	}

	//highlight the name of the type
	long pos = identifierPositionStack[identifierPtr];
	typeDecl.sourceEnd = (int) pos;
	typeDecl.sourceStart = (int) (pos >>> 32);
	typeDecl.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;

	//compute the declaration source too
	// 'class' and 'interface' push an int position
	typeStartPosition = typeDecl.declarationSourceStart = intStack[intPtr--];
	intPtr--;
	int declSourceStart = intStack[intPtr--];
	typeDecl.modifiersSourceStart = intStack[intPtr--];
	typeDecl.modifiers = intStack[intPtr--];
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
	requestor.exitCompilationUnit(scanner.source.length - 1);
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
	ConstructorDeclaration cd = (ConstructorDeclaration) astStack[astPtr];
	requestor.exitConstructor(endStatementPosition, cd.declarationSourceEnd);
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
		intArrayPtr--;
		return;
	}
	ConstructorDeclaration cd = (ConstructorDeclaration) astStack[astPtr];
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
	requestor
		.enterConstructor(
			cd.declarationSourceStart, 
			intArrayStack[intArrayPtr--], 
			cd.modifiers,
			cd.modifiersSourceStart, 
			cd.selector, 
			cd.sourceStart, 
			(int) (selectorSourcePositions & 0xFFFFFFFFL), 
			// retrieve the source end of the name
			argumentTypes, 
			argumentTypeStarts, 
			argumentTypeEnds, 
			argumentNames, 
			argumentNameStarts, 
			argumentNameEnds, 
			rParenPos, 
			// right parenthesis
			exceptionTypes, 
			exceptionTypeStarts, 
			exceptionTypeEnds, 
			scanner.currentPosition - 1); 
}
protected void consumeConstructorHeaderName() {
	// ConstructorHeaderName ::=  Modifiersopt 'Identifier' '('
	ConstructorDeclaration cd = new ConstructorDeclaration(this.compilationUnit.compilationResult);

	//name -- this is not really revelant but we do .....
	cd.selector = identifierStack[identifierPtr];
	selectorSourcePositions = identifierPositionStack[identifierPtr--];
	identifierLengthPtr--;

	//modifiers
	cd.declarationSourceStart = intStack[intPtr--];
	cd.modifiersSourceStart = intStack[intPtr--];
	cd.modifiers = intStack[intPtr--];
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
	cd.sourceStart = (int) (selectorSourcePositions >>> 32);
	pushOnAstStack(cd);

	cd.sourceEnd = lParenPos;
	cd.bodyStart = lParenPos + 1;
}
protected void consumeDefaultModifiers() {
	checkComment(); // might update modifiers with AccDeprecated
	pushOnIntStack(modifiers); // modifiers
	pushOnIntStack(-1);
	pushOnIntStack(
		declarationSourceStart >= 0 ? declarationSourceStart : scanner.startPosition); 
	resetModifiers();
	pushOnExpressionStackLengthStack(0);
}
protected void consumeDiet() {
	// Diet ::= $empty
	super.consumeDiet();
	/* persisting javadoc positions
	 * Will be consume in consumeClassBodyDeclaration
	 */
	pushOnIntArrayStack(this.getJavaDocPositions());	
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeEnterCompilationUnit() {
	// EnterCompilationUnit ::= $empty
	requestor.enterCompilationUnit();
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeEnterVariable() {
	// EnterVariable ::= $empty
	boolean isLocalDeclaration = isLocalDeclaration();
	if (!isLocalDeclaration && (variablesCounter[nestedType] != 0)) {
		requestor.exitField(lastFieldBodyEndPosition, lastFieldEndPosition);
	}
	char[] varName = identifierStack[identifierPtr];
	long namePosition = identifierPositionStack[identifierPtr--];
	int extendedTypeDimension = intStack[intPtr--];

	AbstractVariableDeclaration declaration;
	if (nestedMethod[nestedType] != 0) {
		// create the local variable declarations
		declaration = 
			new LocalDeclaration(varName, (int) (namePosition >>> 32), (int) namePosition); 
	} else {
		// create the field declaration
		declaration = 
			new FieldDeclaration(varName, (int) (namePosition >>> 32), (int) namePosition); 
	}
	identifierLengthPtr--;
	TypeReference type;
	int variableIndex = variablesCounter[nestedType];
	int typeDim = 0;
	if (variableIndex == 0) {
		// first variable of the declaration (FieldDeclaration or LocalDeclaration)
		if (nestedMethod[nestedType] != 0) {
			// local declaration
			declaration.declarationSourceStart = intStack[intPtr--];
			declaration.modifiersSourceStart = intStack[intPtr--];
			declaration.modifiers = intStack[intPtr--];
			type = getTypeReference(typeDim = intStack[intPtr--]); // type dimension
			pushOnAstStack(type);
		} else {
			// field declaration
			type = getTypeReference(typeDim = intStack[intPtr--]); // type dimension
			pushOnAstStack(type);
			declaration.declarationSourceStart = intStack[intPtr--];
			declaration.modifiersSourceStart = intStack[intPtr--];
			declaration.modifiers = intStack[intPtr--];
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
		type = (TypeReference) astStack[astPtr - variableIndex];
		typeDim = type.dimensions();
		AbstractVariableDeclaration previousVariable = 
			(AbstractVariableDeclaration) astStack[astPtr]; 
		declaration.declarationSourceStart = previousVariable.declarationSourceStart;
		declaration.modifiers = previousVariable.modifiers;
		declaration.modifiersSourceStart = previousVariable.modifiersSourceStart;
		final Annotation[] annotations = previousVariable.annotations;
		if (annotations != null) {
			final int annotationsLength = annotations.length;
			System.arraycopy(annotations, 0, declaration.annotations = new Annotation[annotationsLength], 0, annotationsLength);
		}
	}

	localIntPtr = intPtr;

	if (extendedTypeDimension == 0) {
		declaration.type = type;
	} else {
		int dimension = typeDim + extendedTypeDimension;
		declaration.type = this.copyDims(type, dimension);
	}
	variablesCounter[nestedType]++;
	nestedMethod[nestedType]++;
	pushOnAstStack(declaration);

	int[] javadocPositions = intArrayStack[intArrayPtr];
	if (!isLocalDeclaration) {
		requestor
			.enterField(
				declaration.declarationSourceStart, 
				javadocPositions, 
				declaration.modifiers, 
				declaration.modifiersSourceStart, 
				returnTypeName(declaration.type), 
				type.sourceStart, 
				type.sourceEnd, 
				typeDims, 
				varName, 
				(int) (namePosition >>> 32), 
				(int) namePosition, 
				extendedTypeDimension, 
				extendedTypeDimension == 0 ? -1 : endPosition); 
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
	nestedMethod[nestedType]--;	
	lastFieldEndPosition = scanner.currentPosition - 1;
	lastFieldBodyEndPosition = 	((AbstractVariableDeclaration) astStack[astPtr]).initialization.sourceEnd;
}
protected void consumeExitVariableWithoutInitialization() {
	// ExitVariableWithoutInitialization ::= $empty
	// do nothing by default
	super.consumeExitVariableWithoutInitialization();
	nestedMethod[nestedType]--;	
	lastFieldEndPosition = scanner.currentPosition - 1;
	lastFieldBodyEndPosition = scanner.startPosition - 1;
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeFieldDeclaration() {
	// See consumeLocalVariableDeclarationDefaultModifier() in case of change: duplicated code
	// FieldDeclaration ::= Modifiersopt Type VariableDeclarators ';'
	// the super.consumeFieldDeclaration will reinitialize the variableCounter[nestedType]	
	int variableIndex = variablesCounter[nestedType];
	super.consumeFieldDeclaration();
	intArrayPtr--;
	if (isLocalDeclaration())
		return;
	if (variableIndex != 0) {
		requestor.exitField(lastFieldBodyEndPosition, lastFieldEndPosition);
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

	identifierLengthPtr--;
	char[] parameterName = identifierStack[identifierPtr];
	long namePositions = identifierPositionStack[identifierPtr--];
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
	intPtr -= 3;
	Argument arg = 
		new Argument(
			parameterName, 
			namePositions, 
			type, 
			intStack[intPtr + 1]);// modifiers
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
	intArrayPtr--;
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
	requestor.exitInterface(endStatementPosition, // the '}' is the end of the body
	 ((TypeDeclaration) astStack[astPtr]).declarationSourceEnd);
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
		intArrayPtr--;
		return;
	}
	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];
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
	scanner.commentPtr = -1;
	requestor.enterInterface(
		typeDecl.declarationSourceStart, 
		intArrayStack[intArrayPtr--], 
		typeDecl.modifiers, 
		typeDecl.modifiersSourceStart, 
		typeStartPosition, 
		typeDecl.name, 
		typeDecl.sourceStart, 
		typeDecl.sourceEnd, 
		interfaceNames, 
		interfaceNameStarts, 
		interfacenameEnds, 
		scanner.currentPosition - 1); 
}
protected void consumeInterfaceHeaderName1() {
	// InterfaceHeaderName ::= Modifiersopt 'interface' 'Identifier'
	TypeDeclaration typeDecl = new TypeDeclaration(this.compilationUnit.compilationResult);
	if (nestedMethod[nestedType] == 0) {
		if (nestedType != 0) {
			typeDecl.bits |= ASTNode.IsMemberType;
		}
	} else {
		// Record that the block has a declaration for local types
		typeDecl.bits |= ASTNode.IsLocalType;
		markEnclosingMemberWithLocalType();
		blockReal();
	}

	//highlight the name of the type
	long pos = identifierPositionStack[identifierPtr];
	typeDecl.sourceEnd = (int) pos;
	typeDecl.sourceStart = (int) (pos >>> 32);
	typeDecl.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;

	//compute the declaration source too
	// 'class' and 'interface' push an int position
	typeStartPosition = typeDecl.declarationSourceStart = intStack[intPtr--];
	intPtr--;
	int declSourceStart = intStack[intPtr--];
	typeDecl.modifiersSourceStart = intStack[intPtr--];
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
	intArrayPtr--;
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
	MethodDeclaration md = (MethodDeclaration) astStack[astPtr];
	requestor.exitMethod(endStatementPosition, md.declarationSourceEnd);
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
		intArrayPtr--;
		return;
	}
	MethodDeclaration md = (MethodDeclaration) astStack[astPtr];

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
	requestor
		.enterMethod(
			md.declarationSourceStart, 
			intArrayStack[intArrayPtr--], 
			md.modifiers, 
			md.modifiersSourceStart, 
			returnTypeName, 
			returnType.sourceStart, 
			returnType.sourceEnd, 
			typeDims, 
			md.selector, 
			md.sourceStart, 
			(int) (selectorSourcePositions & 0xFFFFFFFFL), 
			argumentTypes, 
			argumentTypeStarts, 
			argumentTypeEnds, 
			argumentNames, 
			argumentNameStarts, 
			argumentNameEnds, 
			rParenPos, 
			extendsDim, 
			extendsDim == 0 ? -1 : endPosition, 
			exceptionTypes, 
			exceptionTypeStarts, 
			exceptionTypeEnds, 
			scanner.currentPosition - 1); 
}
protected void consumeMethodHeaderExtendedDims() {
	// MethodHeaderExtendedDims ::= Dimsopt
	// now we update the returnType of the method
	MethodDeclaration md = (MethodDeclaration) astStack[astPtr];
	int extendedDims = intStack[intPtr--];
	extendsDim = extendedDims;
	if (extendedDims != 0) {
		TypeReference returnType = md.returnType;
		md.sourceEnd = endPosition;
		int dims = returnType.dimensions() + extendedDims;
		md.returnType = this.copyDims(returnType, dims);
		if (currentToken == TokenNameLBRACE) {
			md.bodyStart = endPosition + 1;
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
	md.selector = identifierStack[identifierPtr];
	selectorSourcePositions = identifierPositionStack[identifierPtr--];
	identifierLengthPtr--;
	//type
	md.returnType = getTypeReference(typeDims = intStack[intPtr--]);
	//modifiers
	md.declarationSourceStart = intStack[intPtr--];
	md.modifiersSourceStart = intStack[intPtr--];
	md.modifiers = intStack[intPtr--];
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
	md.sourceStart = (int) (selectorSourcePositions >>> 32);
	pushOnAstStack(md);
	md.bodyStart = scanner.currentPosition-1;
}
protected void consumeModifiers() {
	checkComment(); // might update modifiers with AccDeprecated
	pushOnIntStack(modifiers); // modifiers
	pushOnIntStack(modifiersSourceStart);
	pushOnIntStack(
		declarationSourceStart >= 0 ? declarationSourceStart : modifiersSourceStart); 
	resetModifiers();
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumePackageDeclarationName() {
	/* persisting javadoc positions */
	pushOnIntArrayStack(this.getJavaDocPositions());

	super.consumePackageDeclarationName();
	ImportReference importReference = compilationUnit.currentPackage;

	requestor.acceptPackage(
		importReference.declarationSourceStart, 
		importReference.declarationSourceEnd, 
		intArrayStack[intArrayPtr--], 
		CharOperation.concatWith(importReference.getImportName(), '.'),
		importReference.sourceStart);
}
/*
*
* INTERNAL USE-ONLY
*/
protected void consumePackageDeclarationNameWithModifiers() {
	/* persisting javadoc positions */
	pushOnIntArrayStack(this.getJavaDocPositions());

	super.consumePackageDeclarationNameWithModifiers();
	ImportReference importReference = compilationUnit.currentPackage;

	requestor.acceptPackage(
		importReference.declarationSourceStart, 
		importReference.declarationSourceEnd, 
		intArrayStack[intArrayPtr--], 
		CharOperation.concatWith(importReference.getImportName(), '.'),
		importReference.sourceStart);
}
protected void consumePushModifiers() {
	checkComment(); // might update modifiers with AccDeprecated
	pushOnIntStack(modifiers); // modifiers
	if (modifiersSourceStart < 0) {
		pushOnIntStack(-1);
		pushOnIntStack(
			declarationSourceStart >= 0 ? declarationSourceStart : scanner.startPosition); 
	} else {
		pushOnIntStack(modifiersSourceStart);
		pushOnIntStack(
			declarationSourceStart >= 0 ? declarationSourceStart : modifiersSourceStart); 
	}
	resetModifiers();
	pushOnExpressionStackLengthStack(0);
}
protected void consumePushRealModifiers() {
	checkComment(); // might update modifiers with AccDeprecated
	pushOnIntStack(modifiers); // modifiers
	if (modifiersSourceStart < 0) {
		pushOnIntStack(-1);
		pushOnIntStack(
			declarationSourceStart >= 0 ? declarationSourceStart : scanner.startPosition); 
	} else {
		pushOnIntStack(modifiersSourceStart);
		pushOnIntStack(
			declarationSourceStart >= 0 ? declarationSourceStart : modifiersSourceStart); 
	}
	resetModifiers();
}
protected void consumeSingleStaticImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' 'static' Name

	/* persisting javadoc positions */
	pushOnIntArrayStack(this.getJavaDocPositions());

	super.consumeSingleStaticImportDeclarationName();
	ImportReference importReference = (ImportReference) astStack[astPtr];
	requestor.acceptImport(
		importReference.declarationSourceStart, 
		importReference.declarationSourceEnd,
		intArrayStack[intArrayPtr--],
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
	pushOnIntArrayStack(this.getJavaDocPositions());

	super.consumeSingleTypeImportDeclarationName();
	ImportReference importReference = (ImportReference) astStack[astPtr];
	requestor.acceptImport(
		importReference.declarationSourceStart, 
		importReference.declarationSourceEnd,
		intArrayStack[intArrayPtr--],
		CharOperation.concatWith(importReference.getImportName(), '.'),
		importReference.sourceStart,
		false,
		ClassFileConstants.AccDefault);
}
protected void consumeStaticImportOnDemandDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' 'static' Name '.' '*'

	/* persisting javadoc positions */
	pushOnIntArrayStack(this.getJavaDocPositions());

	super.consumeStaticImportOnDemandDeclarationName();
	ImportReference importReference = (ImportReference) astStack[astPtr];
	requestor.acceptImport(
		importReference.declarationSourceStart, 
		importReference.declarationSourceEnd,
		intArrayStack[intArrayPtr--],
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
	Initializer initializer = (Initializer) astStack[astPtr];
	requestor.acceptInitializer(
		initializer.declarationSourceStart,
		initializer.declarationSourceEnd,
		intArrayStack[intArrayPtr--],
		ClassFileConstants.AccStatic, 
		intStack[intPtr--], 
		initializer.block.sourceStart,
		initializer.declarationSourceEnd);
}
protected void consumeStaticOnly() {
	// StaticOnly ::= 'static'
	checkComment(); // might update declaration source start
	pushOnIntStack(modifiersSourceStart);
	pushOnIntStack(scanner.currentPosition);
	pushOnIntStack(
		declarationSourceStart >= 0 ? declarationSourceStart : modifiersSourceStart); 
	jumpOverMethodBody();
	nestedMethod[nestedType]++;
	resetModifiers();
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeTypeImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' Name '.' '*'

	/* persisting javadoc positions */
	pushOnIntArrayStack(this.getJavaDocPositions());

	super.consumeTypeImportOnDemandDeclarationName();
	ImportReference importReference = (ImportReference) astStack[astPtr];
	requestor.acceptImport(
		importReference.declarationSourceStart, 
		importReference.declarationSourceEnd,
		intArrayStack[intArrayPtr--],
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

	return lastFieldEndPosition = super.flushCommentsDefinedPriorTo(position);
}
public CompilationUnitDeclaration endParse(int act) {
	if (scanner.recordLineSeparator) {
		requestor.acceptLineSeparatorPositions(scanner.getLineEnds());
	}
	return super.endParse(act);
}
public void initialize(boolean initializeNLS) {
	//positionning the parser for a new compilation unit
	//avoiding stack reallocation and all that....
	super.initialize(initializeNLS);
	intArrayPtr = -1;
}
public void initialize() {
	//positionning the parser for a new compilation unit
	//avoiding stack reallocation and all that....
	super.initialize();
	intArrayPtr = -1;
}
/*
 *
 * INTERNAL USE-ONLY
 */
private boolean isLocalDeclaration() {
	int nestedDepth = nestedType;
	while (nestedDepth >= 0) {
		if (nestedMethod[nestedDepth] != 0) {
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
		referenceContext =
			compilationUnit = 
				new CompilationUnitDeclaration(
					problemReporter(), 
					new CompilationResult(unit, 0, 0, this.options.maxProblemsPerUnit), 
					regionSource.length); 
		scanner.resetTo(0, regionSource.length);
		scanner.setSource(regionSource);
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
		referenceContext = 
			compilationUnit = 
				new CompilationUnitDeclaration(
					problemReporter(), 
					new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit), 
					regionSource.length); 
		scanner.resetTo(0, regionSource.length);
		scanner.setSource(regionSource);
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
		referenceContext = 
			compilationUnit = 
				new CompilationUnitDeclaration(
					problemReporter(), 
					new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit), 
					regionSource.length); 
		scanner.resetTo(0, regionSource.length);
		scanner.setSource(regionSource);
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
		referenceContext = 
			compilationUnit = 
				new CompilationUnitDeclaration(
					problemReporter(), 
					new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit), 
					regionSource.length); 
		scanner.resetTo(0, regionSource.length);
		scanner.setSource(regionSource);
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
		referenceContext = 
			compilationUnit = 
				new CompilationUnitDeclaration(
					problemReporter(), 
					new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit), 
					regionSource.length); 
		scanner.resetTo(0, regionSource.length);
		scanner.setSource(regionSource);
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
		referenceContext = 
			compilationUnit = 
				new CompilationUnitDeclaration(
					problemReporter(), 
					new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit), 
					regionSource.length); 
		scanner.resetTo(0, regionSource.length);
		scanner.setSource(regionSource);
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
		referenceContext = 
			compilationUnit = 
				new CompilationUnitDeclaration(
					problemReporter(), 
					new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit), 
					regionSource.length); 
		scanner.resetTo(0, regionSource.length);
		scanner.setSource(regionSource);
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
		referenceContext = 
			compilationUnit = 
				new CompilationUnitDeclaration(
					problemReporter(), 
					new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit), 
					regionSource.length); 
		scanner.resetTo(0, regionSource.length);
		scanner.setSource(regionSource);
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
	problemReporter.referenceContext = referenceContext;
	return problemReporter;
}
protected void pushOnIntArrayStack(int[] positions) {

	int stackLength = this.intArrayStack.length;
	if (++this.intArrayPtr >= stackLength) {
		System.arraycopy(
			this.intArrayStack, 0,
			this.intArrayStack = new int[stackLength + StackIncrement][], 0,
			stackLength);
	}
	intArrayStack[intArrayPtr] = positions;
}
protected void resetModifiers() {
	super.resetModifiers();
	declarationSourceStart = -1;
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
	buffer.append("intArrayPtr = " + intArrayPtr + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
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
	if ((length = identifierLengthStack[localIdentifierLengthPtr]) == 1) {
		// single variable reference
		if (dim == 0) {
			ref = 
				new SingleTypeReference(
					identifierStack[localIdentifierPtr], 
					identifierPositionStack[localIdentifierPtr--]); 
		} else {
			ref = 
				new ArrayTypeReference(
					identifierStack[localIdentifierPtr], 
					dim, 
					identifierPositionStack[localIdentifierPtr--]); 
			ref.sourceEnd = endPosition;				
		}
	} else {
		if (length < 0) { //flag for precompiled type reference on base types
			ref = TypeReference.baseTypeReference(-length, dim);
			ref.sourceStart = intStack[localIntPtr--];
			if (dim == 0) {
				ref.sourceEnd = intStack[localIntPtr--];
			} else {
				localIntPtr--;
				ref.sourceEnd = endPosition;
			}
		} else { //Qualified variable reference
			char[][] tokens = new char[length][];
			localIdentifierPtr -= length;
			long[] positions = new long[length];
			System.arraycopy(identifierStack, localIdentifierPtr + 1, tokens, 0, length);
			System.arraycopy(
				identifierPositionStack, 
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
