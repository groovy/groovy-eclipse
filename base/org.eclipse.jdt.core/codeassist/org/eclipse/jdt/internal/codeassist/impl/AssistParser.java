/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.impl;

/*
 * Parser extension for code assist task
 *
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.RecoveredBlock;
import org.eclipse.jdt.internal.compiler.parser.RecoveredElement;
import org.eclipse.jdt.internal.compiler.parser.RecoveredField;
import org.eclipse.jdt.internal.compiler.parser.RecoveredInitializer;
import org.eclipse.jdt.internal.compiler.parser.RecoveredMethod;
import org.eclipse.jdt.internal.compiler.parser.RecoveredType;
import org.eclipse.jdt.internal.compiler.parser.RecoveredUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public abstract class AssistParser extends Parser {
	public ASTNode assistNode;
	public boolean isOrphanCompletionNode;
		
	/* recovery */
	int[] blockStarts = new int[30];

	// the previous token read by the scanner
	protected int previousToken;

	// the index in the identifier stack of the previous identifier
	protected int previousIdentifierPtr;
	
	// element stack
	protected static final int ElementStackIncrement = 100;
	protected int elementPtr;
	protected int[] elementKindStack = new int[ElementStackIncrement];
	protected int[] elementInfoStack = new int[ElementStackIncrement];
	protected Object[] elementObjectInfoStack = new Object[ElementStackIncrement];
	protected int previousKind;
	protected int previousInfo;
	protected Object previousObjectInfo;
	
	// OWNER
	protected static final int ASSIST_PARSER = 512;
	
	// KIND : all values known by AssistParser are between 513 and 1023
	protected static final int K_SELECTOR = ASSIST_PARSER + 1; // whether we are inside a message send
	protected static final int K_TYPE_DELIMITER = ASSIST_PARSER + 2; // whether we are inside a type declaration
	protected static final int K_METHOD_DELIMITER = ASSIST_PARSER + 3; // whether we are inside a method declaration
	protected static final int K_FIELD_INITIALIZER_DELIMITER = ASSIST_PARSER + 4; // whether we are inside a field initializer
	protected static final int K_ATTRIBUTE_VALUE_DELIMITER = ASSIST_PARSER + 5; // whether we are inside a annotation attribute valuer
	protected static final int K_ENUM_CONSTANT_DELIMITER = ASSIST_PARSER + 6; // whether we are inside a field initializer
	
	// selector constants
	protected static final int THIS_CONSTRUCTOR = -1;
	protected static final int SUPER_CONSTRUCTOR = -2;
	
	// enum constant constants
	protected static final int NO_BODY = 0;
	protected static final int WITH_BODY = 1;
	
	protected boolean isFirst = false;

public AssistParser(ProblemReporter problemReporter) {
	super(problemReporter, true);
	this.javadocParser.checkDocComment = false;
	
	this.setMethodsFullRecovery(false);
	this.setStatementsRecovery(false);
	this.annotationRecoveryActivated = false;
}
public abstract char[] assistIdentifier();
public int bodyEnd(AbstractMethodDeclaration method){
	return method.bodyEnd;
}
public int bodyEnd(Initializer initializer){
	return initializer.declarationSourceEnd;
}
/*
 * Build initial recovery state.
 * Recovery state is inferred from the current state of the parser (reduced node stack).
 */
public RecoveredElement buildInitialRecoveryState(){
	/* recovery in unit structure */
	if (referenceContext instanceof CompilationUnitDeclaration){
		RecoveredElement element = super.buildInitialRecoveryState();
		flushAssistState();
		flushElementStack();
		return element;
	}

	/* recovery in method body */
	lastCheckPoint = 0;

	RecoveredElement element = null;
	if (referenceContext instanceof AbstractMethodDeclaration){
		element = new RecoveredMethod((AbstractMethodDeclaration) referenceContext, null, 0, this);
		lastCheckPoint = ((AbstractMethodDeclaration) referenceContext).bodyStart;
	} else {
		/* Initializer bodies are parsed in the context of the type declaration, we must thus search it inside */
		if (referenceContext instanceof TypeDeclaration){
			TypeDeclaration type = (TypeDeclaration) referenceContext;
			for (int i = 0; i < type.fields.length; i++){
				FieldDeclaration field = type.fields[i];					
				if (field != null
						&& field.getKind() == AbstractVariableDeclaration.INITIALIZER
						&& field.declarationSourceStart <= scanner.initialPosition
						&& scanner.initialPosition <= field.declarationSourceEnd
						&& scanner.eofPosition <= field.declarationSourceEnd+1){
					element = new RecoveredInitializer(field, null, 1, this);
					lastCheckPoint = field.declarationSourceStart;					
					break;
				}
			}
		} 
	}

	if (element == null) return element;

	/* add initial block */
	Block block = new Block(0);
	int lastStart = blockStarts[0];
	block.sourceStart = lastStart;
	element = element.add(block, 1);
	int blockIndex = 1;	// ignore first block start, since manually rebuilt here

	for(int i = 0; i <= astPtr; i++){
		ASTNode node = astStack[i];
		
		if(node instanceof ForeachStatement && ((ForeachStatement)node).action == null) {
			node = ((ForeachStatement)node).elementVariable;
		}

		/* check for intermediate block creation, so recovery can properly close them afterwards */
		int nodeStart = node.sourceStart;
		for (int j = blockIndex; j <= realBlockPtr; j++){
			if (blockStarts[j] >= 0) {
				if (blockStarts[j] > nodeStart){
					blockIndex = j; // shift the index to the new block
					break;
				}
				if (blockStarts[j] != lastStart){ // avoid multiple block if at same position
					block = new Block(0);
					block.sourceStart = lastStart = blockStarts[j];
					element = element.add(block, 1);
				}
			} else {
				if (-blockStarts[j] > nodeStart){
					blockIndex = j; // shift the index to the new block
					break;
				}
				block = new Block(0);
				block.sourceStart = lastStart = -blockStarts[j];
				element = element.add(block, 1);
			}
			blockIndex = j+1; // shift the index to the new block
		}
		if (node instanceof LocalDeclaration){
			LocalDeclaration local = (LocalDeclaration) node;
			if (local.declarationSourceEnd == 0){
				element = element.add(local, 0);
				if (local.initialization == null){
					lastCheckPoint = local.sourceEnd + 1;
				} else {
					lastCheckPoint = local.initialization.sourceEnd + 1;
				}
			} else {
				element = element.add(local, 0);
				lastCheckPoint = local.declarationSourceEnd + 1;
			}
			continue;
		}		
		if (node instanceof AbstractMethodDeclaration){
			AbstractMethodDeclaration method = (AbstractMethodDeclaration) node;
			if (method.declarationSourceEnd == 0){
				element = element.add(method, 0);
				lastCheckPoint = method.bodyStart;
			} else {
				element = element.add(method, 0);
				lastCheckPoint = method.declarationSourceEnd + 1;
			}
			continue;
		}
		if (node instanceof Initializer){
			Initializer initializer = (Initializer) node;
			if (initializer.declarationSourceEnd == 0){
				element = element.add(initializer, 1);
				lastCheckPoint = initializer.sourceStart;				
			} else {
				element = element.add(initializer, 0);
				lastCheckPoint = initializer.declarationSourceEnd + 1;
			}
			continue;
		}		
		if (node instanceof FieldDeclaration){
			FieldDeclaration field = (FieldDeclaration) node;
			if (field.declarationSourceEnd == 0){
				element = element.add(field, 0);
				if (field.initialization == null){
					lastCheckPoint = field.sourceEnd + 1;
				} else {
					lastCheckPoint = field.initialization.sourceEnd + 1;
				}
			} else {
				element = element.add(field, 0);
				lastCheckPoint = field.declarationSourceEnd + 1;
			}
			continue;
		}
		if (node instanceof TypeDeclaration){
			TypeDeclaration type = (TypeDeclaration) node;
			if (type.declarationSourceEnd == 0){
				element = element.add(type, 0);	
				lastCheckPoint = type.bodyStart;
			} else {
				element = element.add(type, 0);				
				lastCheckPoint = type.declarationSourceEnd + 1;
			}
			continue;
		}
		if (node instanceof ImportReference){
			ImportReference importRef = (ImportReference) node;
			element = element.add(importRef, 0);
			lastCheckPoint = importRef.declarationSourceEnd + 1;
		}
	}
	if (this.currentToken == TokenNameRBRACE) {
		this.currentToken = 0; // closing brace has already been taken care of
	}

	/* might need some extra block (after the last reduced node) */
	int pos = this.assistNode == null ? lastCheckPoint : this.assistNode.sourceStart;
	for (int j = blockIndex; j <= realBlockPtr; j++){
		if (blockStarts[j] >= 0) {
			if ((blockStarts[j] < pos) && (blockStarts[j] != lastStart)){ // avoid multiple block if at same position
				block = new Block(0);
				block.sourceStart = lastStart = blockStarts[j];
				element = element.add(block, 1);
			}
		} else {
			if ((blockStarts[j] < pos)){ // avoid multiple block if at same position
				block = new Block(0);
				block.sourceStart = lastStart = -blockStarts[j];
				element = element.add(block, 1);
			}
		}
	}
	
	return element;
}
protected void consumeAnnotationTypeDeclarationHeader() {
	super.consumeAnnotationTypeDeclarationHeader();
	pushOnElementStack(K_TYPE_DELIMITER);
}
protected void consumeClassBodyDeclaration() {
	popElement(K_METHOD_DELIMITER);
	super.consumeClassBodyDeclaration();
}
protected void consumeClassBodyopt() {
	super.consumeClassBodyopt();
	popElement(K_SELECTOR);
}
protected void consumeClassHeader() {
	super.consumeClassHeader();
	pushOnElementStack(K_TYPE_DELIMITER);
}
protected void consumeConstructorBody() {
	super.consumeConstructorBody();
	popElement(K_METHOD_DELIMITER);
}
protected void consumeConstructorHeader() {
	super.consumeConstructorHeader();
	pushOnElementStack(K_METHOD_DELIMITER);
}
protected void consumeEnhancedForStatementHeaderInit(boolean hasModifiers) {
	super.consumeEnhancedForStatementHeaderInit(hasModifiers);
	
	if (this.currentElement != null) {
		LocalDeclaration localDecl = ((ForeachStatement)this.astStack[this.astPtr]).elementVariable;
		this.lastCheckPoint = localDecl.sourceEnd + 1;
		this.currentElement = this.currentElement.add(localDecl, 0);
	}
}
protected void consumeEnterAnonymousClassBody() {
	super.consumeEnterAnonymousClassBody();
	popElement(K_SELECTOR);
	pushOnElementStack(K_TYPE_DELIMITER);
}
protected void consumeEnterMemberValue() {
	super.consumeEnterMemberValue();
	pushOnElementStack(K_ATTRIBUTE_VALUE_DELIMITER, this.identifierPtr);
}
protected void consumeEnumConstantHeader() {
	if(this.currentToken == TokenNameLBRACE) {
		popElement(K_ENUM_CONSTANT_DELIMITER);
		pushOnElementStack(K_ENUM_CONSTANT_DELIMITER, WITH_BODY);
		pushOnElementStack(K_FIELD_INITIALIZER_DELIMITER);
		pushOnElementStack(K_TYPE_DELIMITER);
	}
	super.consumeEnumConstantHeader();
}
protected void consumeEnumConstantHeaderName() {
	super.consumeEnumConstantHeaderName();
	pushOnElementStack(K_ENUM_CONSTANT_DELIMITER);
}
protected void consumeEnumConstantWithClassBody() {
	popElement(K_TYPE_DELIMITER);
	popElement(K_FIELD_INITIALIZER_DELIMITER);
	popElement(K_ENUM_CONSTANT_DELIMITER);
	super.consumeEnumConstantWithClassBody();
}
protected void consumeEnumConstantNoClassBody() {
	popElement(K_ENUM_CONSTANT_DELIMITER);
	super.consumeEnumConstantNoClassBody();
}
protected void consumeEnumHeader() {
	super.consumeEnumHeader();
	pushOnElementStack(K_TYPE_DELIMITER);
}
protected void consumeExitMemberValue() {
	super.consumeExitMemberValue();
	popElement(K_ATTRIBUTE_VALUE_DELIMITER);
}
protected void consumeExplicitConstructorInvocation(int flag, int recFlag) {
	super.consumeExplicitConstructorInvocation(flag, recFlag);
	popElement(K_SELECTOR);
}
protected void consumeForceNoDiet() {
	super.consumeForceNoDiet();
	// if we are not in a method (ie. we are not in a local variable initializer)
	// then we are entering a field initializer
	if (!isInsideMethod()) {
		if(topKnownElementKind(ASSIST_PARSER) != K_ENUM_CONSTANT_DELIMITER) {
			if(topKnownElementKind(ASSIST_PARSER, 2) != K_ENUM_CONSTANT_DELIMITER) {
				pushOnElementStack(K_FIELD_INITIALIZER_DELIMITER);
			}
		} else {
			int info = topKnownElementInfo(ASSIST_PARSER);
			if(info != NO_BODY) {
				pushOnElementStack(K_FIELD_INITIALIZER_DELIMITER);
			}
		}
		
	}
}
protected void consumeInterfaceHeader() {
	super.consumeInterfaceHeader();
	pushOnElementStack(K_TYPE_DELIMITER);
}
protected void consumeMethodBody() {
	super.consumeMethodBody();
	popElement(K_METHOD_DELIMITER);
}
protected void consumeMethodDeclaration(boolean isNotAbstract) {
	if (!isNotAbstract) {
		popElement(K_METHOD_DELIMITER);
	}
	super.consumeMethodDeclaration(isNotAbstract);
}
protected void consumeMethodHeader() {
	super.consumeMethodHeader();
	pushOnElementStack(K_METHOD_DELIMITER);
}
protected void consumeMethodInvocationName() {
	super.consumeMethodInvocationName();
	popElement(K_SELECTOR);
	MessageSend messageSend = (MessageSend)expressionStack[expressionPtr];
	if (messageSend == assistNode){
		this.lastCheckPoint = messageSend.sourceEnd + 1;
	}
}
protected void consumeMethodInvocationNameWithTypeArguments() {
	super.consumeMethodInvocationNameWithTypeArguments();
	popElement(K_SELECTOR);
	MessageSend messageSend = (MessageSend)expressionStack[expressionPtr];
	if (messageSend == assistNode){
		this.lastCheckPoint = messageSend.sourceEnd + 1;
	}
}
protected void consumeMethodInvocationPrimary() {
	super.consumeMethodInvocationPrimary();
	popElement(K_SELECTOR);
	MessageSend messageSend = (MessageSend)expressionStack[expressionPtr];
	if (messageSend == assistNode){
		this.lastCheckPoint = messageSend.sourceEnd + 1;
	}
}
protected void consumeMethodInvocationPrimaryWithTypeArguments() {
	super.consumeMethodInvocationPrimaryWithTypeArguments();
	popElement(K_SELECTOR);
	MessageSend messageSend = (MessageSend)expressionStack[expressionPtr];
	if (messageSend == assistNode){
		this.lastCheckPoint = messageSend.sourceEnd + 1;
	}
}
protected void consumeMethodInvocationSuper() {
	super.consumeMethodInvocationSuper();
	popElement(K_SELECTOR);
	MessageSend messageSend = (MessageSend)expressionStack[expressionPtr];
	if (messageSend == assistNode){
		this.lastCheckPoint = messageSend.sourceEnd + 1;
	}
}
protected void consumeMethodInvocationSuperWithTypeArguments() {
	super.consumeMethodInvocationSuperWithTypeArguments();
	popElement(K_SELECTOR);
	MessageSend messageSend = (MessageSend)expressionStack[expressionPtr];
	if (messageSend == assistNode){
		this.lastCheckPoint = messageSend.sourceEnd + 1;
	}
}
protected void consumeNestedMethod() {
	super.consumeNestedMethod();
	if(!isInsideMethod()) pushOnElementStack(K_METHOD_DELIMITER);
}
protected void consumeOpenBlock() {
	// OpenBlock ::= $empty

	super.consumeOpenBlock();
	int stackLength = this.blockStarts.length;
	if (this.realBlockPtr >= stackLength) {
		System.arraycopy(
			this.blockStarts, 0,
			this.blockStarts = new int[stackLength + StackIncrement], 0,
			stackLength);
	}
	this.blockStarts[this.realBlockPtr] = scanner.startPosition;
}
protected void consumeOpenFakeBlock() {
	// OpenBlock ::= $empty

	super.consumeOpenBlock();
	int stackLength = this.blockStarts.length;
	if (this.realBlockPtr >= stackLength) {
		System.arraycopy(
			this.blockStarts, 0,
			this.blockStarts = new int[stackLength + StackIncrement], 0,
			stackLength);
	}
	this.blockStarts[this.realBlockPtr] = -scanner.startPosition;
}
protected void consumePackageDeclarationName() {
	// PackageDeclarationName ::= 'package' Name
	/* build an ImportRef build from the last name 
	stored in the identifier stack. */

	int index;

	/* no need to take action if not inside assist identifiers */
	if ((index = indexOfAssistIdentifier()) < 0) {
		super.consumePackageDeclarationName();
		return;
	}
	/* retrieve identifiers subset and whole positions, the assist node positions
		should include the entire replaced source. */
	int length = identifierLengthStack[identifierLengthPtr];
	char[][] subset = identifierSubSet(index+1); // include the assistIdentifier
	identifierLengthPtr--;
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(
		identifierPositionStack, 
		identifierPtr + 1, 
		positions, 
		0, 
		length); 

	/* build specific assist node on package statement */
	ImportReference reference = this.createAssistPackageReference(subset, positions);
	assistNode = reference;
	this.lastCheckPoint = reference.sourceEnd + 1;
	compilationUnit.currentPackage = reference; 

	if (currentToken == TokenNameSEMICOLON){
		reference.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		reference.declarationSourceEnd = (int) positions[length-1];
	}
	//endPosition is just before the ;
	reference.declarationSourceStart = intStack[intPtr--];
	// flush comments defined prior to import statements
	reference.declarationSourceEnd = this.flushCommentsDefinedPriorTo(reference.declarationSourceEnd);

	// recovery
	if (currentElement != null){
		lastCheckPoint = reference.declarationSourceEnd+1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}	
}
protected void consumePackageDeclarationNameWithModifiers() {
	// PackageDeclarationName ::= Modifiers 'package' PushRealModifiers Name
	/* build an ImportRef build from the last name 
	stored in the identifier stack. */

	int index;

	/* no need to take action if not inside assist identifiers */
	if ((index = indexOfAssistIdentifier()) < 0) {
		super.consumePackageDeclarationNameWithModifiers();
		return;
	}
	/* retrieve identifiers subset and whole positions, the assist node positions
		should include the entire replaced source. */
	int length = identifierLengthStack[identifierLengthPtr];
	char[][] subset = identifierSubSet(index+1); // include the assistIdentifier
	identifierLengthPtr--;
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(
		identifierPositionStack, 
		identifierPtr + 1, 
		positions, 
		0, 
		length); 

	this.intPtr--; // we don't need the modifiers start
	this.intPtr--; // we don't need the package modifiers
	ImportReference reference = this.createAssistPackageReference(subset, positions);
	// consume annotations
	if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
		System.arraycopy(
			this.expressionStack, 
			(this.expressionPtr -= length) + 1, 
			reference.annotations = new Annotation[length], 
			0, 
			length); 
	}	
	/* build specific assist node on package statement */
	assistNode = reference;
	this.lastCheckPoint = reference.sourceEnd + 1;
	compilationUnit.currentPackage = reference; 

	if (currentToken == TokenNameSEMICOLON){
		reference.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		reference.declarationSourceEnd = (int) positions[length-1];
	}
	//endPosition is just before the ;
	reference.declarationSourceStart = intStack[intPtr--];
	// flush comments defined prior to import statements
	reference.declarationSourceEnd = this.flushCommentsDefinedPriorTo(reference.declarationSourceEnd);

	// recovery
	if (currentElement != null){
		lastCheckPoint = reference.declarationSourceEnd+1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}	
}
protected void consumeRestoreDiet() {
	super.consumeRestoreDiet();
	// if we are not in a method (ie. we were not in a local variable initializer)
	// then we are exiting a field initializer
	if (!isInsideMethod()) {
		popElement(K_FIELD_INITIALIZER_DELIMITER);
	}
}
protected void consumeSingleStaticImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' 'static' Name
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	int index;

	/* no need to take action if not inside assist identifiers */
	if ((index = indexOfAssistIdentifier()) < 0) {
		super.consumeSingleStaticImportDeclarationName();
		return;
	}
	/* retrieve identifiers subset and whole positions, the assist node positions
		should include the entire replaced source. */
	int length = identifierLengthStack[identifierLengthPtr];
	char[][] subset = identifierSubSet(index+1); // include the assistIdentifier
	identifierLengthPtr--;
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(
		identifierPositionStack, 
		identifierPtr + 1, 
		positions, 
		0, 
		length); 

	/* build specific assist node on import statement */
	ImportReference reference = this.createAssistImportReference(subset, positions, ClassFileConstants.AccStatic);
	assistNode = reference;
	this.lastCheckPoint = reference.sourceEnd + 1;

	pushOnAstStack(reference);

	if (currentToken == TokenNameSEMICOLON){
		reference.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		reference.declarationSourceEnd = (int) positions[length-1];
	}
	//endPosition is just before the ;
	reference.declarationSourceStart = intStack[intPtr--];
	// flush annotations defined prior to import statements
	reference.declarationSourceEnd = this.flushCommentsDefinedPriorTo(reference.declarationSourceEnd);

	// recovery
	if (currentElement != null){
		lastCheckPoint = reference.declarationSourceEnd+1;
		currentElement = currentElement.add(reference, 0);
		lastIgnoredToken = -1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
}
protected void consumeSingleTypeImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' Name
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	int index;

	/* no need to take action if not inside assist identifiers */
	if ((index = indexOfAssistIdentifier()) < 0) {
		super.consumeSingleTypeImportDeclarationName();
		return;
	}
	/* retrieve identifiers subset and whole positions, the assist node positions
		should include the entire replaced source. */
	int length = identifierLengthStack[identifierLengthPtr];
	char[][] subset = identifierSubSet(index+1); // include the assistIdentifier
	identifierLengthPtr--;
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(
		identifierPositionStack, 
		identifierPtr + 1, 
		positions, 
		0, 
		length); 

	/* build specific assist node on import statement */
	ImportReference reference = this.createAssistImportReference(subset, positions, ClassFileConstants.AccDefault);
	assistNode = reference;
	this.lastCheckPoint = reference.sourceEnd + 1;

	pushOnAstStack(reference);

	if (currentToken == TokenNameSEMICOLON){
		reference.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		reference.declarationSourceEnd = (int) positions[length-1];
	}
	//endPosition is just before the ;
	reference.declarationSourceStart = intStack[intPtr--];
	// flush comments defined prior to import statements
	reference.declarationSourceEnd = this.flushCommentsDefinedPriorTo(reference.declarationSourceEnd);

	// recovery
	if (currentElement != null){
		lastCheckPoint = reference.declarationSourceEnd+1;
		currentElement = currentElement.add(reference, 0);
		lastIgnoredToken = -1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
}
protected void consumeStaticImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' 'static' Name '.' '*'
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	int index;

	/* no need to take action if not inside assist identifiers */
	if ((index = indexOfAssistIdentifier()) < 0) {
		super.consumeStaticImportOnDemandDeclarationName();
		return;
	}
	/* retrieve identifiers subset and whole positions, the assist node positions
		should include the entire replaced source. */
	int length = identifierLengthStack[identifierLengthPtr];
	char[][] subset = identifierSubSet(index+1); // include the assistIdentifier
	identifierLengthPtr--;
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(
		identifierPositionStack, 
		identifierPtr + 1, 
		positions, 
		0, 
		length); 

	/* build specific assist node on import statement */
	ImportReference reference = this.createAssistImportReference(subset, positions, ClassFileConstants.AccStatic);
	reference.bits |= ASTNode.OnDemand;
	assistNode = reference;
	this.lastCheckPoint = reference.sourceEnd + 1;

	pushOnAstStack(reference);

	if (currentToken == TokenNameSEMICOLON){
		reference.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		reference.declarationSourceEnd = (int) positions[length-1];
	}
	//endPosition is just before the ;
	reference.declarationSourceStart = intStack[intPtr--];
	// flush annotations defined prior to import statements
	reference.declarationSourceEnd = this.flushCommentsDefinedPriorTo(reference.declarationSourceEnd);

	// recovery
	if (currentElement != null){
		lastCheckPoint = reference.declarationSourceEnd+1;
		currentElement = currentElement.add(reference, 0);
		lastIgnoredToken = -1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
}
protected void consumeStaticInitializer() {
	super.consumeStaticInitializer();
	popElement(K_METHOD_DELIMITER);
}
protected void consumeStaticOnly() {
	super.consumeStaticOnly();
	pushOnElementStack(K_METHOD_DELIMITER);
}
protected void consumeToken(int token) {
	super.consumeToken(token);
	
	if(isFirst) {
		isFirst = false;
		return;
	}
	// register message send selector only if inside a method or if looking at a field initializer 
	// and if the current token is an open parenthesis
	if (isInsideMethod() || isInsideFieldInitialization() || isInsideAttributeValue()) {
		switch (token) {
			case TokenNameLPAREN :
				switch (this.previousToken) {
					case TokenNameIdentifier:
						this.pushOnElementStack(K_SELECTOR, this.identifierPtr);
						break;
					case TokenNamethis: // explicit constructor invocation, eg. this(1, 2)
						this.pushOnElementStack(K_SELECTOR, THIS_CONSTRUCTOR);
						break;
					case TokenNamesuper: // explicit constructor invocation, eg. super(1, 2)
						this.pushOnElementStack(K_SELECTOR, SUPER_CONSTRUCTOR);
						break;
					case TokenNameGREATER: // explicit constructor invocation, eg. Fred<X>[(]1, 2)
					case TokenNameRIGHT_SHIFT: // or fred<X<X>>[(]1, 2) 
					case TokenNameUNSIGNED_RIGHT_SHIFT: //or Fred<X<X<X>>>[(]1, 2)
						if(this.identifierPtr > -1) {
							this.pushOnElementStack(K_SELECTOR, this.identifierPtr);
						}
						break;
				}
				break;
		}
	} else {
		switch (token) {
			case TokenNameRBRACE :
				if(topKnownElementKind(ASSIST_PARSER) == K_TYPE_DELIMITER) {
					popElement(K_TYPE_DELIMITER);
				}
				break;
		}
	}
	this.previousToken = token;
	if (token == TokenNameIdentifier) {
		this.previousIdentifierPtr = this.identifierPtr;
	}
}
protected void consumeTypeImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' Name '.' '*'
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	int index;

	/* no need to take action if not inside assist identifiers */
	if ((index = indexOfAssistIdentifier()) < 0) {
		super.consumeTypeImportOnDemandDeclarationName();
		return;
	}
	/* retrieve identifiers subset and whole positions, the assist node positions
		should include the entire replaced source. */
	int length = identifierLengthStack[identifierLengthPtr];
	char[][] subset = identifierSubSet(index+1); // include the assistIdentifier
	identifierLengthPtr--;
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(
		identifierPositionStack, 
		identifierPtr + 1, 
		positions, 
		0, 
		length); 

	/* build specific assist node on import statement */
	ImportReference reference = this.createAssistImportReference(subset, positions, ClassFileConstants.AccDefault);
	reference.bits |= ASTNode.OnDemand;
	assistNode = reference;
	this.lastCheckPoint = reference.sourceEnd + 1;

	pushOnAstStack(reference);

	if (currentToken == TokenNameSEMICOLON){
		reference.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		reference.declarationSourceEnd = (int) positions[length-1];
	}
	//endPosition is just before the ;
	reference.declarationSourceStart = intStack[intPtr--];
	// flush comments defined prior to import statements
	reference.declarationSourceEnd = this.flushCommentsDefinedPriorTo(reference.declarationSourceEnd);

	// recovery
	if (currentElement != null){
		lastCheckPoint = reference.declarationSourceEnd+1;
		currentElement = currentElement.add(reference, 0);
		lastIgnoredToken = -1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
}
public abstract ImportReference createAssistImportReference(char[][] tokens, long[] positions, int mod);
public abstract ImportReference createAssistPackageReference(char[][] tokens, long[] positions);
public abstract NameReference createQualifiedAssistNameReference(char[][] previousIdentifiers, char[] assistName, long[] positions);
public abstract TypeReference createQualifiedAssistTypeReference(char[][] previousIdentifiers, char[] assistName, long[] positions);
public abstract TypeReference createParameterizedQualifiedAssistTypeReference(char[][] previousIdentifiers, TypeReference[][] typeArguments, char[] asistIdentifier, TypeReference[] assistTypeArguments, long[] positions);
public abstract NameReference createSingleAssistNameReference(char[] assistName, long position);
public abstract TypeReference createSingleAssistTypeReference(char[] assistName, long position);
public abstract TypeReference createParameterizedSingleAssistTypeReference(TypeReference[] typeArguments, char[] assistName, long position);
/*
 * Flush parser/scanner state regarding to code assist
 */
public void flushAssistState(){
	this.assistNode = null;
	this.isOrphanCompletionNode = false;
	this.setAssistIdentifier(null);
}
protected void flushElementStack() {
	for (int j = 0; j <= elementPtr; j++) {
		elementObjectInfoStack[j] = null;
	}
	
	this.elementPtr = -1;
	this.previousKind = 0;
	this.previousInfo = 0;
	this.previousObjectInfo = null;
}
/*
 * Build specific type reference nodes in case the cursor is located inside the type reference
 */
protected TypeReference getTypeReference(int dim) {

	int index;

	/* no need to take action if not inside completed identifiers */
	if ((index = indexOfAssistIdentifier(true)) < 0) {
		return super.getTypeReference(dim);
	}
	int length = identifierLengthStack[identifierLengthPtr];
	TypeReference reference;
	int numberOfIdentifiers = this.genericsIdentifiersLengthStack[this.genericsIdentifiersLengthPtr--];
	if (length != numberOfIdentifiers || this.genericsLengthStack[this.genericsLengthPtr] != 0) {
		identifierLengthPtr--;
		// generic type
		reference = getAssistTypeReferenceForGenericType(dim, length, numberOfIdentifiers);
	} else {
		/* retrieve identifiers subset and whole positions, the assist node positions
			should include the entire replaced source. */
		
		char[][] subset = identifierSubSet(index);
		identifierLengthPtr--;
		identifierPtr -= length;
		long[] positions = new long[length];
		System.arraycopy(
			identifierPositionStack, 
			identifierPtr + 1, 
			positions, 
			0, 
			length); 
	
		/* build specific assist on type reference */
		
		if (index == 0) {
//			genericsIdentifiersLengthPtr--;
			genericsLengthPtr--;
			/* assist inside first identifier */
			reference = this.createSingleAssistTypeReference(
							assistIdentifier(), 
							positions[0]);
		} else {
//			genericsIdentifiersLengthPtr--;
			genericsLengthPtr--;
			/* assist inside subsequent identifier */
			reference =	this.createQualifiedAssistTypeReference(
							subset,  
							assistIdentifier(), 
							positions);
		}
		assistNode = reference;
		this.lastCheckPoint = reference.sourceEnd + 1;
	}
	return reference;
}
protected TypeReference getAssistTypeReferenceForGenericType(int dim, int identifierLength, int numberOfIdentifiers) {
	/* no need to take action if not inside completed identifiers */
	if (/*(indexOfAssistIdentifier()) < 0 ||*/ (identifierLength == 1 && numberOfIdentifiers == 1)) {
		int currentTypeArgumentsLength = this.genericsLengthStack[this.genericsLengthPtr--];
		TypeReference[] typeArguments = new TypeReference[currentTypeArgumentsLength];
		this.genericsPtr -= currentTypeArgumentsLength;
		System.arraycopy(this.genericsStack, this.genericsPtr + 1, typeArguments, 0, currentTypeArgumentsLength);
		long[] positions = new long[identifierLength];
		System.arraycopy(
			identifierPositionStack, 
			identifierPtr, 
			positions, 
			0, 
			identifierLength); 
		
		this.identifierPtr--;
				
		TypeReference reference = this.createParameterizedSingleAssistTypeReference(
				typeArguments,
				assistIdentifier(),
				positions[0]);
		
		this.assistNode = reference;
		this.lastCheckPoint = reference.sourceEnd + 1;
		return reference;
	}
	
	TypeReference[][] typeArguments = new TypeReference[numberOfIdentifiers][];
	char[][] tokens = new char[numberOfIdentifiers][];
	long[] positions = new long[numberOfIdentifiers];
	int index = numberOfIdentifiers;
	int currentIdentifiersLength = identifierLength;
	while (index > 0) {
		int currentTypeArgumentsLength = this.genericsLengthStack[this.genericsLengthPtr--];
		if (currentTypeArgumentsLength != 0) {
			this.genericsPtr -= currentTypeArgumentsLength;
			System.arraycopy(this.genericsStack, this.genericsPtr + 1, typeArguments[index - 1] = new TypeReference[currentTypeArgumentsLength], 0, currentTypeArgumentsLength);
		}
		switch(currentIdentifiersLength) {
			case 1 :
				// we are in a case A<B>.C<D> or A<B>.C<D>
				tokens[index - 1] = this.identifierStack[this.identifierPtr];
				positions[index - 1] = this.identifierPositionStack[this.identifierPtr--];
				break;
			default:
				// we are in a case A.B.C<B>.C<D> or A.B.C<B>...
				this.identifierPtr -= currentIdentifiersLength;
				System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, index - currentIdentifiersLength, currentIdentifiersLength);
				System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, index - currentIdentifiersLength, currentIdentifiersLength);
		}
		index -= currentIdentifiersLength;
		if (index > 0) {
			currentIdentifiersLength = this.identifierLengthStack[this.identifierLengthPtr--];
		}
	}
	
	// remove completion token
	int realLength = numberOfIdentifiers;
	for (int i = 0; i < numberOfIdentifiers; i++) {
		if(tokens[i] == assistIdentifier()) {
			realLength = i;
		}
	}
	TypeReference reference;
	if(realLength == 0) {
		if(typeArguments[0] != null && typeArguments[0].length > 0) {
			reference = this.createParameterizedSingleAssistTypeReference(typeArguments[0], assistIdentifier(), positions[0]);
		} else {
			reference = this.createSingleAssistTypeReference(assistIdentifier(), positions[0]);
		}
	} else {
		TypeReference[] assistTypeArguments = typeArguments[realLength];
		System.arraycopy(tokens, 0, tokens = new char[realLength][], 0, realLength);
		System.arraycopy(typeArguments, 0, typeArguments = new TypeReference[realLength][], 0, realLength);
		
		boolean isParameterized = false;
		for (int i = 0; i < typeArguments.length; i++) {
			if(typeArguments[i] != null) {
				isParameterized = true;
			}
		}
		if(isParameterized || (assistTypeArguments != null && assistTypeArguments.length > 0)) {
			reference = this.createParameterizedQualifiedAssistTypeReference(tokens, typeArguments, assistIdentifier(), assistTypeArguments, positions);
		} else {
			reference = this.createQualifiedAssistTypeReference(tokens, assistIdentifier(), positions);
		}
	}

	assistNode = reference;
	this.lastCheckPoint = reference.sourceEnd + 1;
	return reference;
}
/*
 * Copy of code from superclass with the following change:
 * In the case of qualified name reference if the cursor location is on the 
 * qualified name reference, then create a CompletionOnQualifiedNameReference 
 * instead.
 */
protected NameReference getUnspecifiedReferenceOptimized() {

	int completionIndex;

	/* no need to take action if not inside completed identifiers */
	if ((completionIndex = indexOfAssistIdentifier()) < 0) {
		return super.getUnspecifiedReferenceOptimized();
	}

	/* retrieve identifiers subset and whole positions, the completion node positions
		should include the entire replaced source. */
	int length = identifierLengthStack[identifierLengthPtr];
	char[][] subset = identifierSubSet(completionIndex);
	identifierLengthPtr--;
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(
		identifierPositionStack, 
		identifierPtr + 1, 
		positions, 
		0, 
		length);

	/* build specific completion on name reference */
	NameReference reference;
	if (completionIndex == 0) {
		/* completion inside first identifier */
		reference = this.createSingleAssistNameReference(assistIdentifier(), positions[0]);
	} else {
		/* completion inside subsequent identifier */
		reference = this.createQualifiedAssistNameReference(subset, assistIdentifier(), positions);
	}
	reference.bits &= ~ASTNode.RestrictiveFlagMASK;
	reference.bits |= Binding.LOCAL | Binding.FIELD;
	
	assistNode = reference;
	lastCheckPoint = reference.sourceEnd + 1;
	return reference;
}
public void goForBlockStatementsopt() {
	super.goForBlockStatementsopt();
	isFirst = true;
}
public void goForHeaders(){
	super.goForHeaders();
	isFirst = true;
}
public void goForCompilationUnit(){
	super.goForCompilationUnit();
	isFirst = true;
}
public void goForBlockStatementsOrCatchHeader() {
	super.goForBlockStatementsOrCatchHeader();
	isFirst = true;
}
/*
 * Retrieve a partial subset of a qualified name reference up to the completion point.
 * It does not pop the actual awaiting identifiers, so as to be able to retrieve position
 * information afterwards.
 */
protected char[][] identifierSubSet(int subsetLength){

	if (subsetLength == 0) return null;
	
	char[][] subset;
	System.arraycopy(
		identifierStack,
		identifierPtr - identifierLengthStack[identifierLengthPtr] + 1,
		(subset = new char[subsetLength][]),
		0,
		subsetLength);
	return subset;
}

protected int indexOfAssistIdentifier(){
	return this.indexOfAssistIdentifier(false);
}
/*
 * Iterate the most recent group of awaiting identifiers (grouped for qualified name reference (eg. aa.bb.cc)
 * so as to check whether one of them is the assist identifier.
 * If so, then answer the index of the assist identifier (0 being the first identifier of the set).
 *	eg. aa(0).bb(1).cc(2)
 * If no assist identifier was found, answers -1.
 */
protected int indexOfAssistIdentifier(boolean useGenericsStack){

	if (identifierLengthPtr < 0){
		return -1; // no awaiting identifier
	}

	char[] assistIdentifier ;
	if ((assistIdentifier = this.assistIdentifier()) == null){
		return -1; // no assist identifier found yet
	}

	// iterate awaiting identifiers backwards
	int length = identifierLengthStack[identifierLengthPtr];
	if(useGenericsStack && length > 0 && this.genericsIdentifiersLengthPtr > -1 ) {
		length = this.genericsIdentifiersLengthStack[this.genericsIdentifiersLengthPtr];
	}
	for (int i = 0; i < length; i++){ 
		if (identifierStack[identifierPtr - i] == assistIdentifier){
			return length - i - 1;
		}
	}
	// none of the awaiting identifiers is the completion one
	return -1;
}
public void initialize() {
	super.initialize();
	this.flushAssistState();
	this.flushElementStack();
	this.previousIdentifierPtr = -1;
}
public void initialize(boolean initializeNLS) {
	super.initialize(initializeNLS);
	this.flushAssistState();
	this.flushElementStack();
	this.previousIdentifierPtr = -1;
}
public abstract void initializeScanner();
protected boolean isIndirectlyInsideFieldInitialization(){
	int i = elementPtr;
	while(i > -1) {
		if(elementKindStack[i] == K_FIELD_INITIALIZER_DELIMITER)
			return true;
		i--;
	}
	return false;
}
protected boolean isIndirectlyInsideMethod(){
	int i = elementPtr;
	while(i > -1) {
		if(elementKindStack[i] == K_METHOD_DELIMITER)
			return true;
		i--;
	}
	return false;
}
protected boolean isIndirectlyInsideType(){
	int i = elementPtr;
	while(i > -1) {
		if(elementKindStack[i] == K_TYPE_DELIMITER)
			return true;
		i--;
	}
	return false;
}
protected boolean isInsideAttributeValue(){
	int i = elementPtr;
	while(i > -1) {
		switch (elementKindStack[i]) {
			case K_TYPE_DELIMITER : return false;
			case K_METHOD_DELIMITER : return false;
			case K_FIELD_INITIALIZER_DELIMITER : return false;
			case K_ATTRIBUTE_VALUE_DELIMITER : return true;
		}
		i--;
	}
	return false;
}
protected boolean isInsideFieldInitialization(){
	int i = elementPtr;
	while(i > -1) {
		switch (elementKindStack[i]) {
			case K_TYPE_DELIMITER : return false;
			case K_METHOD_DELIMITER : return false;
			case K_FIELD_INITIALIZER_DELIMITER : return true;
		}
		i--;
	}
	return false;
}
protected boolean isInsideMethod(){
	int i = elementPtr;
	while(i > -1) {
		switch (elementKindStack[i]) {
			case K_TYPE_DELIMITER : return false;
			case K_METHOD_DELIMITER : return true;
			case K_FIELD_INITIALIZER_DELIMITER : return false;
		}	
		i--;
	}
	return false;
}
protected boolean isInsideType(){
	int i = elementPtr;
	while(i > -1) {
		switch (elementKindStack[i]) {
			case K_TYPE_DELIMITER : return true;
			case K_METHOD_DELIMITER : return false;
			case K_FIELD_INITIALIZER_DELIMITER : return false;
		}
		i--;
	}
	return false;
}
protected int lastIndexOfElement(int kind) {
	int i = elementPtr;
	while(i > -1) {
		if(elementKindStack[i] == kind) return i;
		i--;	
	}
	return -1;
}
/**
 * Parse the block statements inside the given method declaration and try to complete at the
 * cursor location.
 */
public void parseBlockStatements(AbstractMethodDeclaration md, CompilationUnitDeclaration unit) {
	if (md instanceof MethodDeclaration) {
		parseBlockStatements((MethodDeclaration) md, unit);
	} else if (md instanceof ConstructorDeclaration) {
		parseBlockStatements((ConstructorDeclaration) md, unit);
	}
}
/**
 * Parse the block statements inside the given constructor declaration and try to complete at the
 * cursor location.
 */
public void parseBlockStatements(ConstructorDeclaration cd, CompilationUnitDeclaration unit) {
	//only parse the method body of cd
	//fill out its statements

	//convert bugs into parse error

	initialize();
	
	// simulate goForConstructorBody except that we don't want to balance brackets because they are not going to be balanced
	goForBlockStatementsopt();

	referenceContext = cd;
	compilationUnit = unit;

	scanner.resetTo(cd.bodyStart, bodyEnd(cd));
	consumeNestedMethod();
	try {
		parse();
	} catch (AbortCompilation ex) {
		lastAct = ERROR_ACTION;
	}
	
	if (lastAct == ERROR_ACTION) {
		return;
	}

	// attach the statements as we might be searching for a reference to a local type
	cd.explicitDeclarations = realBlockStack[realBlockPtr--];
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		astPtr -= length;
		if (astStack[astPtr + 1] instanceof ExplicitConstructorCall)
			//avoid a isSomeThing that would only be used here BUT what is faster between two alternatives ?
			{
			System.arraycopy(
				astStack, 
				astPtr + 2, 
				cd.statements = new Statement[length - 1], 
				0, 
				length - 1); 
			cd.constructorCall = (ExplicitConstructorCall) astStack[astPtr + 1];
		} else { //need to add explicitly the super();
			System.arraycopy(
				astStack, 
				astPtr + 1, 
				cd.statements = new Statement[length], 
				0, 
				length); 
			cd.constructorCall = SuperReference.implicitSuperConstructorCall();
		}
	} else {
		cd.constructorCall = SuperReference.implicitSuperConstructorCall();
		if (!containsComment(cd.bodyStart, cd.bodyEnd)) {
			cd.bits |= ASTNode.UndocumentedEmptyBlock;
		}		
	}

	if (cd.constructorCall.sourceEnd == 0) {
		cd.constructorCall.sourceEnd = cd.sourceEnd;
		cd.constructorCall.sourceStart = cd.sourceStart;
	}
}
/**
 * Parse the block statements inside the given initializer and try to complete at the
 * cursor location.
 */
public void parseBlockStatements(
	Initializer initializer,
	TypeDeclaration type, 
	CompilationUnitDeclaration unit) {

	initialize();

	// simulate goForInitializer except that we don't want to balance brackets because they are not going to be balanced
	goForBlockStatementsopt();
	
	referenceContext = type;
	compilationUnit = unit;

	scanner.resetTo(initializer.sourceStart, bodyEnd(initializer)); // just after the beginning {
	consumeNestedMethod();
	try {
		parse();
	} catch (AbortCompilation ex) {
		lastAct = ERROR_ACTION;
	} finally {
		nestedMethod[nestedType]--;
	}
	
	if (lastAct == ERROR_ACTION) {
		return;
	}

	// attach the statements as we might be searching for a reference to a local type
	initializer.block.explicitDeclarations = realBlockStack[realBlockPtr--];
	int length;
	if ((length = astLengthStack[astLengthPtr--]) > 0) {
		System.arraycopy(astStack, (astPtr -= length) + 1, initializer.block.statements = new Statement[length], 0, length); 
	} else {
		// check whether this block at least contains some comment in it
		if (!containsComment(initializer.block.sourceStart, initializer.block.sourceEnd)) {
			initializer.block.bits |= ASTNode.UndocumentedEmptyBlock;
		}
	}
	
	// mark initializer with local type if one was found during parsing
	if ((type.bits & ASTNode.HasLocalType) != 0) {
		initializer.bits |= ASTNode.HasLocalType;
	}	
}
/**
 * Parse the block statements inside the given method declaration and try to complete at the
 * cursor location.
 */
public void parseBlockStatements(MethodDeclaration md, CompilationUnitDeclaration unit) {
	//only parse the method body of md
	//fill out method statements

	//convert bugs into parse error

	if (md.isAbstract())
		return;
	if (md.isNative())
		return;
	if ((md.modifiers & ExtraCompilerModifiers.AccSemicolonBody) != 0)
		return;

	initialize();

	// simulate goForMethodBody except that we don't want to balance brackets because they are not going to be balanced
	goForBlockStatementsopt();

	referenceContext = md;
	compilationUnit = unit;
	
	scanner.resetTo(md.bodyStart, bodyEnd(md)); // reset the scanner to parser from { down to the cursor location
	consumeNestedMethod();
	try {
		parse();
	} catch (AbortCompilation ex) {
		lastAct = ERROR_ACTION;
	} finally {
		nestedMethod[nestedType]--;		
	}
	
	if (lastAct == ERROR_ACTION) {
		return;
	}

	// attach the statements as we might be searching for a reference to a local type
	md.explicitDeclarations = realBlockStack[realBlockPtr--];
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		System.arraycopy(
			astStack, 
			(astPtr -= length) + 1, 
			md.statements = new Statement[length], 
			0, 
			length); 
	} else {
		if (!containsComment(md.bodyStart, md.bodyEnd)) {
			md.bits |= ASTNode.UndocumentedEmptyBlock;
		}
	}

}
protected void popElement(int kind){
	if(elementPtr < 0 || elementKindStack[elementPtr] != kind) return;
	
	previousKind = elementKindStack[elementPtr];
	previousInfo = elementInfoStack[elementPtr];
	previousObjectInfo = elementObjectInfoStack[elementPtr];
	
	elementObjectInfoStack[elementPtr] = null;
	
	switch (kind) {
		default :
			elementPtr--;
			break;
	}
}
protected void popUntilElement(int kind){
	if(elementPtr < 0) return;
	int i = elementPtr;
	while (i >= 0 && elementKindStack[i] != kind) {
		i--;
	}
	if(i >= 0) {
		if(i < elementPtr) {
			previousKind = elementKindStack[i+1];
			previousInfo = elementInfoStack[i+1];
			previousObjectInfo = elementObjectInfoStack[i+1];
			
			for (int j = i + 1; j <= elementPtr; j++) {
				elementObjectInfoStack[j] = null;
			}
		}
		elementPtr = i;	
	}
}
/*
 * Prepares the state of the parser to go for BlockStatements.
 */
protected void prepareForBlockStatements() {
	this.nestedMethod[this.nestedType = 0] = 1;
	this.variablesCounter[this.nestedType] = 0;
	this.realBlockStack[this.realBlockPtr = 1] = 0;
	
	// initialize element stack
	int fieldInitializerIndex = lastIndexOfElement(K_FIELD_INITIALIZER_DELIMITER);
	int methodIndex = lastIndexOfElement(K_METHOD_DELIMITER);
	if(methodIndex == fieldInitializerIndex) {
		// there is no method and no field initializer
		flushElementStack();
	} else if(methodIndex > fieldInitializerIndex) {
		popUntilElement(K_METHOD_DELIMITER);
	} else {
		popUntilElement(K_FIELD_INITIALIZER_DELIMITER);
	}
}
/*
 * Prepares the state of the parser to go for Headers.
 */
protected void prepareForHeaders() {
	nestedMethod[nestedType = 0] = 0;
	variablesCounter[nestedType] = 0;
	realBlockStack[realBlockPtr = 0] = 0;
	
	popUntilElement(K_TYPE_DELIMITER);

	if(this.topKnownElementKind(ASSIST_PARSER) != K_TYPE_DELIMITER) {
		// is outside a type and inside a compilation unit.
		// remove all elements.
		this.flushElementStack();
	}
}
protected void pushOnElementStack(int kind){
	this.pushOnElementStack(kind, 0, null);
}
protected void pushOnElementStack(int kind, int info){
	this.pushOnElementStack(kind, info, null);
}
protected void pushOnElementStack(int kind, int info, Object objectInfo){
	if (this.elementPtr < -1) return;
	
	this.previousKind = 0;
	this.previousInfo = 0;
	this.previousObjectInfo = null;
	
	int stackLength = this.elementKindStack.length;
	if (++this.elementPtr >= stackLength) {
		System.arraycopy(
			this.elementKindStack, 0,
			this.elementKindStack = new int[stackLength + StackIncrement], 0,
			stackLength);
		System.arraycopy(
			this.elementInfoStack, 0,
			this.elementInfoStack = new int[stackLength + StackIncrement], 0,
			stackLength);
		System.arraycopy(
			this.elementObjectInfoStack, 0,
			this.elementObjectInfoStack = new Object[stackLength + StackIncrement], 0,
			stackLength);
	}
	this.elementKindStack[this.elementPtr] = kind;
	this.elementInfoStack[this.elementPtr] = info;
	this.elementObjectInfoStack[this.elementPtr] = objectInfo;
}
public void recoveryExitFromVariable() {
	if(currentElement != null && currentElement instanceof RecoveredField
		&& !(currentElement instanceof RecoveredInitializer)) {
		RecoveredElement oldElement = currentElement;
		super.recoveryExitFromVariable();
		if(oldElement != currentElement) {
			popElement(K_FIELD_INITIALIZER_DELIMITER);
		}
	} else {
		super.recoveryExitFromVariable();
	}
}
public void recoveryTokenCheck() {
	RecoveredElement oldElement = currentElement;
	switch (currentToken) {
		case TokenNameLBRACE :
			super.recoveryTokenCheck();
			if(currentElement instanceof RecoveredInitializer) {
				if(oldElement instanceof RecoveredField) {
					popUntilElement(K_FIELD_INITIALIZER_DELIMITER);
					popElement(K_FIELD_INITIALIZER_DELIMITER);
				}
				if(currentElement != oldElement
					&& topKnownElementKind(ASSIST_PARSER) != K_METHOD_DELIMITER) {
					pushOnElementStack(K_METHOD_DELIMITER);
				}
			}
			break;
		case TokenNameRBRACE :
			super.recoveryTokenCheck();
			if(currentElement != oldElement && !isInsideAttributeValue()) {
				if(oldElement instanceof RecoveredInitializer
					|| oldElement instanceof RecoveredMethod
					|| (oldElement instanceof RecoveredBlock && oldElement.parent instanceof RecoveredInitializer)
					|| (oldElement instanceof RecoveredBlock && oldElement.parent instanceof RecoveredMethod)) {
					popUntilElement(K_METHOD_DELIMITER);
					popElement(K_METHOD_DELIMITER);
				} else if(oldElement instanceof RecoveredType) {
					popUntilElement(K_TYPE_DELIMITER);
					if(!(referenceContext instanceof CompilationUnitDeclaration) 
							|| isIndirectlyInsideFieldInitialization()
							|| currentElement instanceof RecoveredUnit) {
						popElement(K_TYPE_DELIMITER);
					}
				}
			}
			break;
		default :
			super.recoveryTokenCheck();
			break;
	}
}
public void reset(){
	this.flushAssistState();
}
/*
 * Reset context so as to resume to regular parse loop
 * If unable to reset for resuming, answers false.
 *
 * Move checkpoint location, reset internal stacks and
 * decide which grammar goal is activated.
 */
protected boolean resumeAfterRecovery() {

	// reset internal stacks 
	this.astPtr = -1;
	this.astLengthPtr = -1;
	this.expressionPtr = -1;
	this.expressionLengthPtr = -1;
	this.identifierPtr = -1;	
	this.identifierLengthPtr	= -1;
	this.intPtr = -1;
	this.dimensions = 0 ;
	this.recoveredStaticInitializerStart = 0;
	
	this.genericsIdentifiersLengthPtr = -1;
	this.genericsLengthPtr = -1;
	this.genericsPtr = -1;
	
	this.modifiers = ClassFileConstants.AccDefault;
	this.modifiersSourceStart = -1;

	// if in diet mode, reset the diet counter because we're going to restart outside an initializer.
	if (diet) dietInt = 0;

	/* attempt to move checkpoint location */
	if (!this.moveRecoveryCheckpoint()) return false;

	// only look for headers
	if (referenceContext instanceof CompilationUnitDeclaration
		|| this.assistNode != null){
		if(isInsideMethod() &&
			isIndirectlyInsideFieldInitialization() &&		
			this.assistNode == null
			){ 
			this.prepareForBlockStatements();
			goForBlockStatementsOrCatchHeader();
		} else {
			this.prepareForHeaders();
			goForHeaders();
			diet = true; // passed this point, will not consider method bodies
		}
		return true;
	}
	if (referenceContext instanceof AbstractMethodDeclaration
		|| referenceContext instanceof TypeDeclaration){
			
		if (currentElement instanceof RecoveredType){
			this.prepareForHeaders();
			goForHeaders();
		} else {
			this.prepareForBlockStatements();
			goForBlockStatementsOrCatchHeader();
		}
		return true;
	}
	// does not know how to restart
	return false;
}
public abstract void setAssistIdentifier(char[] assistIdent);
protected int topKnownElementInfo(int owner) {
	return topKnownElementInfo(owner, 0);
}
protected int topKnownElementInfo(int owner, int offSet) {
	int i = elementPtr;
	while(i > -1) {
		if((elementKindStack[i] & owner) != 0) {
			if(offSet <= 0) return elementInfoStack[i];
			offSet--;
		}
		i--;
	}
	return 0;
}
protected int topKnownElementKind(int owner) {
	return topKnownElementKind(owner, 0);
}
protected int topKnownElementKind(int owner, int offSet) {
	int i = elementPtr;
	while(i > -1) {
		if((elementKindStack[i] & owner) != 0) {
			if(offSet <= 0) return elementKindStack[i];
			offSet--;
		}
		i--;
	}
	return 0;
}
protected Object topKnownElementObjectInfo(int owner, int offSet) {
	int i = elementPtr;
	while(i > -1) {
		if((elementKindStack[i] & owner) != 0) {
			if(offSet <= 0) return elementObjectInfoStack[i];
			offSet--;
		}
		i--;
	}
	return null;
}
protected Object topKnownElementObjectInfo(int owner) {
	return topKnownElementObjectInfo(owner, 0);
}
/**
 * If the given ast node is inside an explicit constructor call
 * then wrap it with a fake constructor call.
 * Returns the wrapped completion node or the completion node itself.
 */
protected ASTNode wrapWithExplicitConstructorCallIfNeeded(ASTNode ast) {
	int selector;
	if (ast != null && topKnownElementKind(ASSIST_PARSER) == K_SELECTOR && ast instanceof Expression &&
			(((selector = topKnownElementInfo(ASSIST_PARSER)) == THIS_CONSTRUCTOR) ||
			(selector == SUPER_CONSTRUCTOR))) {
		ExplicitConstructorCall call = new ExplicitConstructorCall(
			(selector == THIS_CONSTRUCTOR) ? 
				ExplicitConstructorCall.This : 
				ExplicitConstructorCall.Super
		);
		call.arguments = new Expression[] {(Expression)ast};
		call.sourceStart = ast.sourceStart;
		call.sourceEnd = ast.sourceEnd;
		return call;
	} else {
		return ast;
	}
}
}
