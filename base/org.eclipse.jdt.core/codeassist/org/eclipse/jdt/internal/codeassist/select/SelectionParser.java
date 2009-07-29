/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.select;

/*
 * Parser able to build specific completion parse nodes, given a cursorLocation.
 *
 * Cursor location denotes the position of the last character behind which completion
 * got requested:
 *  -1 means completion at the very beginning of the source
 *	0  means completion behind the first character
 *  n  means completion behind the n-th character
 */
 
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.codeassist.impl.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.Util;

public class SelectionParser extends AssistParser {
	// OWNER
	protected static final int SELECTION_PARSER = 1024;
	protected static final int SELECTION_OR_ASSIST_PARSER = ASSIST_PARSER + SELECTION_PARSER;
	
	// KIND : all values known by SelectionParser are between 1025 and 1549
	protected static final int K_BETWEEN_CASE_AND_COLON = SELECTION_PARSER + 1; // whether we are inside a block

	public ASTNode assistNodeParent; // the parent node of assist node
	
	/* public fields */

	public int selectionStart, selectionEnd;

	public static final char[] SUPER = "super".toCharArray(); //$NON-NLS-1$
	public static final char[] THIS = "this".toCharArray(); //$NON-NLS-1$
	
public SelectionParser(ProblemReporter problemReporter) {
	super(problemReporter);
	this.javadocParser.checkDocComment = true;
}
public char[] assistIdentifier(){
	return ((SelectionScanner)scanner).selectionIdentifier;
}
protected void attachOrphanCompletionNode(){
	if (isOrphanCompletionNode){
		ASTNode orphan = this.assistNode;
		isOrphanCompletionNode = false;
		
		
		/* if in context of a type, then persists the identifier into a fake field return type */
		if (currentElement instanceof RecoveredType){
			RecoveredType recoveredType = (RecoveredType)currentElement;
			/* filter out cases where scanner is still inside type header */
			if (recoveredType.foundOpeningBrace) {
				/* generate a pseudo field with a completion on type reference */	
				if (orphan instanceof TypeReference){
					currentElement = currentElement.add(new SelectionOnFieldType((TypeReference)orphan), 0);
					return;
				}
			}
		}
		
		if (orphan instanceof Expression) {
			buildMoreCompletionContext((Expression)orphan);
		} else {
			Statement statement = (Statement) orphan;
			currentElement = currentElement.add(statement, 0);
		}
		currentToken = 0; // given we are not on an eof, we do not want side effects caused by looked-ahead token
	}
}
private void buildMoreCompletionContext(Expression expression) {
	ASTNode parentNode = null;
	
	int kind = topKnownElementKind(SELECTION_OR_ASSIST_PARSER);
	if(kind != 0) {
//		int info = topKnownElementInfo(SELECTION_OR_ASSIST_PARSER);
		switch (kind) {
			case K_BETWEEN_CASE_AND_COLON :
				if(this.expressionPtr > 0) {
					SwitchStatement switchStatement = new SwitchStatement();
					switchStatement.expression = this.expressionStack[this.expressionPtr - 1];
					if(this.astLengthPtr > -1 && this.astPtr > -1) {
						int length = this.astLengthStack[this.astLengthPtr];
						int newAstPtr = this.astPtr - length;
						ASTNode firstNode = this.astStack[newAstPtr + 1];
						if(length != 0 && firstNode.sourceStart > switchStatement.expression.sourceEnd) {
							switchStatement.statements = new Statement[length + 1];
							System.arraycopy(
								this.astStack, 
								newAstPtr + 1, 
								switchStatement.statements, 
								0, 
								length); 
						}
					}
					CaseStatement caseStatement = new CaseStatement(expression, expression.sourceStart, expression.sourceEnd);
					if(switchStatement.statements == null) {
						switchStatement.statements = new Statement[]{caseStatement};
					} else {
						switchStatement.statements[switchStatement.statements.length - 1] = caseStatement;
					}
					parentNode = switchStatement;
					this.assistNodeParent = parentNode;
				}
				break;
		}
	}
	if(parentNode != null) {
		currentElement = currentElement.add((Statement)parentNode, 0);
	} else {
		currentElement = currentElement.add((Statement)wrapWithExplicitConstructorCallIfNeeded(expression), 0);
		if(lastCheckPoint < expression.sourceEnd) {
			lastCheckPoint = expression.sourceEnd + 1;
		}
	}
}
private boolean checkRecoveredType() {
	if (currentElement instanceof RecoveredType){
		/* check if current awaiting identifier is the completion identifier */
		if (this.indexOfAssistIdentifier() < 0) return false;

		if ((lastErrorEndPosition >= selectionStart)
			&& (lastErrorEndPosition <= selectionEnd+1)){
			return false;
		}
		RecoveredType recoveredType = (RecoveredType)currentElement;
		/* filter out cases where scanner is still inside type header */
		if (recoveredType.foundOpeningBrace) {
			this.assistNode = this.getTypeReference(0);
			this.lastCheckPoint = this.assistNode.sourceEnd + 1;
			this.isOrphanCompletionNode = true;
			return true;
		}
	}
	return false;
}
protected void classInstanceCreation(boolean hasClassBody) {
	
	// ClassInstanceCreationExpression ::= 'new' ClassType '(' ArgumentListopt ')' ClassBodyopt

	// ClassBodyopt produces a null item on the astStak if it produces NO class body
	// An empty class body produces a 0 on the length stack.....

	
	if ((astLengthStack[astLengthPtr] == 1)
		&& (astStack[astPtr] == null)) {

		
		int index;
		if ((index = this.indexOfAssistIdentifier()) < 0) {
			super.classInstanceCreation(hasClassBody);
			return;
		} else if(this.identifierLengthPtr > -1 &&
					(this.identifierLengthStack[this.identifierLengthPtr] - 1) != index) {
			super.classInstanceCreation(hasClassBody);
			return;
		}
		QualifiedAllocationExpression alloc;
		astPtr--;
		astLengthPtr--;
		alloc = new SelectionOnQualifiedAllocationExpression();
		alloc.sourceEnd = endPosition; //the position has been stored explicitly

		int length;
		if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
			expressionPtr -= length;
			System.arraycopy(
				expressionStack, 
				expressionPtr + 1, 
				alloc.arguments = new Expression[length], 
				0, 
				length); 
		}
		// trick to avoid creating a selection on type reference
		char [] oldIdent = this.assistIdentifier();
		this.setAssistIdentifier(null);
		alloc.type = getTypeReference(0);
		
		this.setAssistIdentifier(oldIdent);
		
		//the default constructor with the correct number of argument
		//will be created and added by the TC (see createsInternalConstructorWithBinding)
		alloc.sourceStart = intStack[intPtr--];
		pushOnExpressionStack(alloc);

		this.assistNode = alloc;
		this.lastCheckPoint = alloc.sourceEnd + 1;
		if (!diet){
			this.restartRecovery	= true;	// force to restart in recovery mode
			this.lastIgnoredToken = -1;	
		}
		this.isOrphanCompletionNode = true;
	} else {
		super.classInstanceCreation(hasClassBody);
	}
}
protected void consumeArrayCreationExpressionWithoutInitializer() {
	// ArrayCreationWithoutArrayInitializer ::= 'new' PrimitiveType DimWithOrWithOutExprs
	// ArrayCreationWithoutArrayInitializer ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs

	super.consumeArrayCreationExpressionWithoutInitializer();

	ArrayAllocationExpression alloc = (ArrayAllocationExpression)expressionStack[expressionPtr];
	if (alloc.type == assistNode){
		if (!diet){
			this.restartRecovery	= true;	// force to restart in recovery mode
			this.lastIgnoredToken = -1;	
		}
		this.isOrphanCompletionNode = true;
	}
}
protected void consumeArrayCreationExpressionWithInitializer() {
	// ArrayCreationWithArrayInitializer ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs ArrayInitializer

	super.consumeArrayCreationExpressionWithInitializer();

	ArrayAllocationExpression alloc = (ArrayAllocationExpression)expressionStack[expressionPtr];
	if (alloc.type == assistNode){
		if (!diet){
			this.restartRecovery	= true;	// force to restart in recovery mode
			this.lastIgnoredToken = -1;	
		}
		this.isOrphanCompletionNode = true;
	}
}
protected void consumeClassInstanceCreationExpressionQualifiedWithTypeArguments() {
	// ClassInstanceCreationExpression ::= Primary '.' 'new' TypeArguments SimpleName '(' ArgumentListopt ')' ClassBodyopt
	// ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' TypeArguments SimpleName '(' ArgumentListopt ')' ClassBodyopt

	QualifiedAllocationExpression alloc;
	int length;
	if (((length = this.astLengthStack[this.astLengthPtr]) == 1) && (this.astStack[this.astPtr] == null)) {
		
		if (this.indexOfAssistIdentifier() < 0) {
			super.consumeClassInstanceCreationExpressionQualifiedWithTypeArguments();
			return;
		}
		
		//NO ClassBody
		this.astPtr--;
		this.astLengthPtr--;
		alloc = new SelectionOnQualifiedAllocationExpression();
		alloc.sourceEnd = this.endPosition; //the position has been stored explicitly

		if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
			this.expressionPtr -= length;
			System.arraycopy(
				this.expressionStack, 
				this.expressionPtr + 1, 
				alloc.arguments = new Expression[length], 
				0, 
				length); 
		}
		
		// trick to avoid creating a selection on type reference
		char [] oldIdent = this.assistIdentifier();
		this.setAssistIdentifier(null);
		alloc.type = getTypeReference(0);
		
		this.setAssistIdentifier(oldIdent);

		length = this.genericsLengthStack[this.genericsLengthPtr--];
		this.genericsPtr -= length;
		System.arraycopy(this.genericsStack, this.genericsPtr + 1, alloc.typeArguments = new TypeReference[length], 0, length);
		intPtr--; // remove the position of the '<'

		//the default constructor with the correct number of argument
		//will be created and added by the TC (see createsInternalConstructorWithBinding)
		alloc.sourceStart = this.intStack[this.intPtr--];
		pushOnExpressionStack(alloc);
		
		this.assistNode = alloc;
		this.lastCheckPoint = alloc.sourceEnd + 1;
		if (!diet){
			this.restartRecovery	= true;	// force to restart in recovery mode
			this.lastIgnoredToken = -1;	
		}
		this.isOrphanCompletionNode = true;
	} else {
		super.consumeClassInstanceCreationExpressionQualifiedWithTypeArguments();
	}

	this.expressionLengthPtr--;
	QualifiedAllocationExpression qae = 
		(QualifiedAllocationExpression) this.expressionStack[this.expressionPtr--]; 
	qae.enclosingInstance = this.expressionStack[this.expressionPtr];
	this.expressionStack[this.expressionPtr] = qae;
	qae.sourceStart = qae.enclosingInstance.sourceStart;
}
protected void consumeClassInstanceCreationExpressionWithTypeArguments() {
	// ClassInstanceCreationExpression ::= 'new' TypeArguments ClassType '(' ArgumentListopt ')' ClassBodyopt
	AllocationExpression alloc;
	int length;
	if (((length = this.astLengthStack[this.astLengthPtr]) == 1)
		&& (this.astStack[this.astPtr] == null)) {
		
		if (this.indexOfAssistIdentifier() < 0) {
			super.consumeClassInstanceCreationExpressionWithTypeArguments();
			return;
		}
		
		//NO ClassBody
		this.astPtr--;
		this.astLengthPtr--;
		alloc = new SelectionOnQualifiedAllocationExpression();
		alloc.sourceEnd = this.endPosition; //the position has been stored explicitly

		if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
			this.expressionPtr -= length;
			System.arraycopy(
				this.expressionStack, 
				this.expressionPtr + 1, 
				alloc.arguments = new Expression[length], 
				0, 
				length); 
		}
		
		// trick to avoid creating a selection on type reference
		char [] oldIdent = this.assistIdentifier();
		this.setAssistIdentifier(null);
		alloc.type = getTypeReference(0);
		
		this.setAssistIdentifier(oldIdent);

		length = this.genericsLengthStack[this.genericsLengthPtr--];
		this.genericsPtr -= length;
		System.arraycopy(this.genericsStack, this.genericsPtr + 1, alloc.typeArguments = new TypeReference[length], 0, length);
		intPtr--; // remove the position of the '<'
		
		//the default constructor with the correct number of argument
		//will be created and added by the TC (see createsInternalConstructorWithBinding)
		alloc.sourceStart = this.intStack[this.intPtr--];
		pushOnExpressionStack(alloc);
		
		this.assistNode = alloc;
		this.lastCheckPoint = alloc.sourceEnd + 1;
		if (!diet){
			this.restartRecovery	= true;	// force to restart in recovery mode
			this.lastIgnoredToken = -1;	
		}
		this.isOrphanCompletionNode = true;
	} else {
		super.consumeClassInstanceCreationExpressionWithTypeArguments();
	}
}
protected void consumeEnterAnonymousClassBody() {
	// EnterAnonymousClassBody ::= $empty

	if (this.indexOfAssistIdentifier() < 0) {
		super.consumeEnterAnonymousClassBody();
		return;
	}
	
	// trick to avoid creating a selection on type reference
	char [] oldIdent = this.assistIdentifier();
	this.setAssistIdentifier(null);		
	TypeReference typeReference = getTypeReference(0);
	this.setAssistIdentifier(oldIdent);		

	TypeDeclaration anonymousType = new TypeDeclaration(this.compilationUnit.compilationResult); 
	anonymousType.name = CharOperation.NO_CHAR;
	anonymousType.bits |= (ASTNode.IsAnonymousType|ASTNode.IsLocalType);
	QualifiedAllocationExpression alloc = new SelectionOnQualifiedAllocationExpression(anonymousType); 
	markEnclosingMemberWithLocalType();
	pushOnAstStack(anonymousType);

	alloc.sourceEnd = rParenPos; //the position has been stored explicitly
	int argumentLength;
	if ((argumentLength = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= argumentLength;
		System.arraycopy(
			expressionStack, 
			expressionPtr + 1, 
			alloc.arguments = new Expression[argumentLength], 
			0, 
			argumentLength); 
	}

	alloc.type = typeReference;	

	anonymousType.sourceEnd = alloc.sourceEnd;
	//position at the type while it impacts the anonymous declaration
	anonymousType.sourceStart = anonymousType.declarationSourceStart = alloc.type.sourceStart;
	alloc.sourceStart = intStack[intPtr--];
	pushOnExpressionStack(alloc);

	assistNode = alloc;
	this.lastCheckPoint = alloc.sourceEnd + 1;
	if (!diet){
		this.restartRecovery	= true;	// force to restart in recovery mode
		this.lastIgnoredToken = -1;
		currentToken = 0; // opening brace already taken into account
		hasReportedError = true;
	}

	anonymousType.bodyStart = scanner.currentPosition;
	listLength = 0; // will be updated when reading super-interfaces
	// recovery
	if (currentElement != null){
		lastCheckPoint = anonymousType.bodyStart;
		currentElement = currentElement.add(anonymousType, 0);
		currentToken = 0; // opening brace already taken into account
		lastIgnoredToken = -1;		
	}
}
protected void consumeEnterVariable() {
	// EnterVariable ::= $empty
	// do nothing by default

	super.consumeEnterVariable();

	AbstractVariableDeclaration variable = (AbstractVariableDeclaration) astStack[astPtr];
	if (variable.type == assistNode){
		if (!diet){
			this.restartRecovery	= true;	// force to restart in recovery mode
			this.lastIgnoredToken = -1;	
		}
		isOrphanCompletionNode = false; // already attached inside variable decl
	}
}

protected void consumeExitVariableWithInitialization() {
	super.consumeExitVariableWithInitialization();
	
	// does not keep the initialization if selection is not inside
	AbstractVariableDeclaration variable = (AbstractVariableDeclaration) astStack[astPtr];
	int start = variable.initialization.sourceStart;
	int end =  variable.initialization.sourceEnd;
	if ((selectionStart < start) &&  (selectionEnd < start) ||
			(selectionStart > end) && (selectionEnd > end)) {
		variable.initialization = null;
	}

}

protected void consumeFieldAccess(boolean isSuperAccess) {
	// FieldAccess ::= Primary '.' 'Identifier'
	// FieldAccess ::= 'super' '.' 'Identifier'

	if (this.indexOfAssistIdentifier() < 0) {
		super.consumeFieldAccess(isSuperAccess);
		return;
	} 
	FieldReference fieldReference = 
		new SelectionOnFieldReference(
			identifierStack[identifierPtr], 
			identifierPositionStack[identifierPtr--]);
	identifierLengthPtr--;
	if (isSuperAccess) { //considerates the fieldReferenceerence beginning at the 'super' ....	
		fieldReference.sourceStart = intStack[intPtr--];
		fieldReference.receiver = new SuperReference(fieldReference.sourceStart, endPosition);
		pushOnExpressionStack(fieldReference);
	} else { //optimize push/pop
		if ((fieldReference.receiver = expressionStack[expressionPtr]).isThis()) { //fieldReferenceerence begins at the this
			fieldReference.sourceStart = fieldReference.receiver.sourceStart;
		}
		expressionStack[expressionPtr] = fieldReference;
	}
	assistNode = fieldReference;
	this.lastCheckPoint = fieldReference.sourceEnd + 1;
	if (!diet){
		this.restartRecovery	= true;	// force to restart in recovery mode
		this.lastIgnoredToken = -1;
	}
	this.isOrphanCompletionNode = true;	
}
protected void consumeFormalParameter(boolean isVarArgs) {
	if (this.indexOfAssistIdentifier() < 0) {
		super.consumeFormalParameter(isVarArgs);
		if((!diet || dietInt != 0) && astPtr > -1) {
			Argument argument = (Argument) astStack[astPtr];
			if(argument.type == assistNode) {
				isOrphanCompletionNode = true;
				this.restartRecovery	= true;	// force to restart in recovery mode
				this.lastIgnoredToken = -1;	
			}
		}
	} else {
		identifierLengthPtr--;
		char[] identifierName = identifierStack[identifierPtr];
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
		int modifierPositions = intStack[intPtr--];
		intPtr--;
		Argument arg = 
			new SelectionOnArgumentName(
				identifierName, 
				namePositions, 
				type, 
				intStack[intPtr + 1] & ~ClassFileConstants.AccDeprecated); // modifiers
		arg.declarationSourceStart = modifierPositions;
		
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
		
		assistNode = arg;
		this.lastCheckPoint = (int) namePositions;
		isOrphanCompletionNode = true;
		
		if (!diet){
			this.restartRecovery	= true;	// force to restart in recovery mode
			this.lastIgnoredToken = -1;	
		}

		/* if incomplete method header, listLength counter will not have been reset,
			indicating that some arguments are available on the stack */
		listLength++;
	} 	
}
protected void consumeInstanceOfExpression() {
	if (indexOfAssistIdentifier() < 0) {
		super.consumeInstanceOfExpression();
	} else {
		getTypeReference(intStack[intPtr--]);
		this.isOrphanCompletionNode = true;
		this.restartRecovery = true;
		this.lastIgnoredToken = -1;
	}
}
protected void consumeInstanceOfExpressionWithName() {
	if (indexOfAssistIdentifier() < 0) {
		super.consumeInstanceOfExpressionWithName();
	} else {
		getTypeReference(intStack[intPtr--]);
		this.isOrphanCompletionNode = true;
		this.restartRecovery = true;
		this.lastIgnoredToken = -1;
	}
}
protected void consumeLocalVariableDeclarationStatement() {
	super.consumeLocalVariableDeclarationStatement();
	
	// force to restart in recovery mode if the declaration contains the selection
	if (!this.diet) {
		LocalDeclaration localDeclaration = (LocalDeclaration) this.astStack[this.astPtr];
		if ((this.selectionStart >= localDeclaration.sourceStart) 
				&&  (this.selectionEnd <= localDeclaration.sourceEnd)) {
			this.restartRecovery	= true;	
			this.lastIgnoredToken = -1;	
		}
	}
}
protected void consumeMarkerAnnotation() {
	int index;
	
	if ((index = this.indexOfAssistIdentifier()) < 0) {
		super.consumeMarkerAnnotation();
		return;
	} 
	
	MarkerAnnotation markerAnnotation = null;
	int length = this.identifierLengthStack[this.identifierLengthPtr];
	TypeReference typeReference;

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
		/* assist inside first identifier */
		typeReference = this.createSingleAssistTypeReference(
						assistIdentifier(), 
						positions[0]);
	} else {
		/* assist inside subsequent identifier */
		typeReference =	this.createQualifiedAssistTypeReference(
						subset,  
						assistIdentifier(), 
						positions);
	}
	assistNode = typeReference;
	this.lastCheckPoint = typeReference.sourceEnd + 1;

	markerAnnotation = new MarkerAnnotation(typeReference, this.intStack[this.intPtr--]);
	markerAnnotation.declarationSourceEnd = markerAnnotation.sourceEnd;
	pushOnExpressionStack(markerAnnotation);
}
protected void consumeMemberValuePair() {
	if (this.indexOfAssistIdentifier() < 0) {
		super.consumeMemberValuePair();
		return;
	} 
	
	char[] simpleName = this.identifierStack[this.identifierPtr];
	long position = this.identifierPositionStack[this.identifierPtr--];
	this.identifierLengthPtr--;
	int end = (int) position;
	int start = (int) (position >>> 32);
	Expression value = this.expressionStack[this.expressionPtr--];
	this.expressionLengthPtr--;
	MemberValuePair memberValuePair = new SelectionOnNameOfMemberValuePair(simpleName, start, end, value);
	pushOnAstStack(memberValuePair);
	
	assistNode = memberValuePair;
	this.lastCheckPoint = memberValuePair.sourceEnd + 1;
	
	
}
protected void consumeMethodInvocationName() {
	// MethodInvocation ::= Name '(' ArgumentListopt ')'

	// when the name is only an identifier...we have a message send to "this" (implicit)

	char[] selector = identifierStack[identifierPtr];
	int accessMode;
	if(selector == this.assistIdentifier()) {
		if(CharOperation.equals(selector, SUPER)) {
			accessMode = ExplicitConstructorCall.Super;
		} else if(CharOperation.equals(selector, THIS)) {
			accessMode = ExplicitConstructorCall.This;
		} else {
			super.consumeMethodInvocationName();
			return;
		}
	} else {
		super.consumeMethodInvocationName();
		return;
	}
	
	final ExplicitConstructorCall constructorCall = new SelectionOnExplicitConstructorCall(accessMode);
	constructorCall.sourceEnd = rParenPos;
	constructorCall.sourceStart = (int) (identifierPositionStack[identifierPtr] >>> 32);
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(expressionStack, expressionPtr + 1, constructorCall.arguments = new Expression[length], 0, length);
	}

	if (!diet){
		pushOnAstStack(constructorCall);
		this.restartRecovery	= true;	// force to restart in recovery mode
		this.lastIgnoredToken = -1;
	} else {
		pushOnExpressionStack(new Expression(){
			public TypeBinding resolveType(BlockScope scope) {
				constructorCall.resolve(scope);
				return null;
			}
			public StringBuffer printExpression(int indent, StringBuffer output) {
				return output; 
			}
		});
	}
	this.assistNode = constructorCall;	
	this.lastCheckPoint = constructorCall.sourceEnd + 1;
	this.isOrphanCompletionNode = true;
}
protected void consumeMethodInvocationPrimary() {
	//optimize the push/pop
	//MethodInvocation ::= Primary '.' 'Identifier' '(' ArgumentListopt ')'

	char[] selector = identifierStack[identifierPtr];
	int accessMode;
	if(selector == this.assistIdentifier()) {
		if(CharOperation.equals(selector, SUPER)) {
			accessMode = ExplicitConstructorCall.Super;
		} else if(CharOperation.equals(selector, THIS)) {
			accessMode = ExplicitConstructorCall.This;
		} else {
			super.consumeMethodInvocationPrimary();
			return;
		}
	} else {
		super.consumeMethodInvocationPrimary();
		return;
	}
	
	final ExplicitConstructorCall constructorCall = new SelectionOnExplicitConstructorCall(accessMode);
	constructorCall.sourceEnd = rParenPos;
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(expressionStack, expressionPtr + 1, constructorCall.arguments = new Expression[length], 0, length);
	}
	constructorCall.qualification = expressionStack[expressionPtr--];
	constructorCall.sourceStart = constructorCall.qualification.sourceStart;
	
	if (!diet){
		pushOnAstStack(constructorCall);
		this.restartRecovery	= true;	// force to restart in recovery mode
		this.lastIgnoredToken = -1;
	} else {
		pushOnExpressionStack(new Expression(){
			public TypeBinding resolveType(BlockScope scope) {
				constructorCall.resolve(scope);
				return null;
			}
			public StringBuffer printExpression(int indent, StringBuffer output) {
				return output; 
			}
		});
	}
	
	this.assistNode = constructorCall;
	this.lastCheckPoint = constructorCall.sourceEnd + 1;
	this.isOrphanCompletionNode = true;
}
protected void consumeNormalAnnotation() {
	int index;
	
	if ((index = this.indexOfAssistIdentifier()) < 0) {
		super.consumeNormalAnnotation();
		return;
	} 
	
	NormalAnnotation normalAnnotation = null;
	int length = this.identifierLengthStack[this.identifierLengthPtr];
	TypeReference typeReference;
	
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
		/* assist inside first identifier */
		typeReference = this.createSingleAssistTypeReference(
						assistIdentifier(), 
						positions[0]);
	} else {
		/* assist inside subsequent identifier */
		typeReference =	this.createQualifiedAssistTypeReference(
						subset,  
						assistIdentifier(), 
						positions);
	}
	assistNode = typeReference;
	this.lastCheckPoint = typeReference.sourceEnd + 1;
		
	normalAnnotation = new NormalAnnotation(typeReference, this.intStack[this.intPtr--]);
	if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
		System.arraycopy(
			this.astStack, 
			(this.astPtr -= length) + 1, 
			normalAnnotation.memberValuePairs = new MemberValuePair[length], 
			0, 
			length); 
	}
	normalAnnotation.declarationSourceEnd = this.rParenPos;
	pushOnExpressionStack(normalAnnotation);
}
protected void consumeSingleMemberAnnotation() {
	int index;
	
	if ((index = this.indexOfAssistIdentifier()) < 0) {
		super.consumeSingleMemberAnnotation();
		return;
	} 
	
	SingleMemberAnnotation singleMemberAnnotation = null;
	int length = this.identifierLengthStack[this.identifierLengthPtr];
	TypeReference typeReference;
	
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
		/* assist inside first identifier */
		typeReference = this.createSingleAssistTypeReference(
						assistIdentifier(), 
						positions[0]);
	} else {
		/* assist inside subsequent identifier */
		typeReference =	this.createQualifiedAssistTypeReference(
						subset,  
						assistIdentifier(), 
						positions);
	}
	assistNode = typeReference;
	this.lastCheckPoint = typeReference.sourceEnd + 1;

	singleMemberAnnotation = new SingleMemberAnnotation(typeReference, this.intStack[this.intPtr--]);
	singleMemberAnnotation.memberValue = this.expressionStack[this.expressionPtr--];
	this.expressionLengthPtr--;
	singleMemberAnnotation.declarationSourceEnd = this.rParenPos;
	pushOnExpressionStack(singleMemberAnnotation);
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
protected void consumeToken(int token) {
	super.consumeToken(token);
	
	// if in a method or if in a field initializer 
	if (isInsideMethod() || isInsideFieldInitialization()) {
		switch (token) {
			case TokenNamecase :
				pushOnElementStack(K_BETWEEN_CASE_AND_COLON);
				break;
			case TokenNameCOLON:
				if(topKnownElementKind(SELECTION_OR_ASSIST_PARSER) == K_BETWEEN_CASE_AND_COLON) { 
					popElement(K_BETWEEN_CASE_AND_COLON);
				}
				break;
		}
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
public ImportReference createAssistImportReference(char[][] tokens, long[] positions, int mod){
	return new SelectionOnImportReference(tokens, positions, mod);
}
public ImportReference createAssistPackageReference(char[][] tokens, long[] positions){
	return new SelectionOnPackageReference(tokens, positions);
}
protected JavadocParser createJavadocParser() {
	return new SelectionJavadocParser(this);
}
protected LocalDeclaration createLocalDeclaration(char[] assistName,int sourceStart,int sourceEnd) {
	if (this.indexOfAssistIdentifier() < 0) {
		return super.createLocalDeclaration(assistName, sourceStart, sourceEnd);
	} else {
		SelectionOnLocalName local = new SelectionOnLocalName(assistName, sourceStart, sourceEnd);
		this.assistNode = local;
		this.lastCheckPoint = sourceEnd + 1;
		return local;
	}
}
public NameReference createQualifiedAssistNameReference(char[][] previousIdentifiers, char[] assistName, long[] positions){
	return new SelectionOnQualifiedNameReference(
					previousIdentifiers, 
					assistName, 
					positions); 	
}
public TypeReference createQualifiedAssistTypeReference(char[][] previousIdentifiers, char[] assistName, long[] positions){
	return new SelectionOnQualifiedTypeReference(
					previousIdentifiers, 
					assistName, 
					positions); 	
}
public TypeReference createParameterizedQualifiedAssistTypeReference(
		char[][] tokens, TypeReference[][] typeArguments, char[] assistname, TypeReference[] assistTypeArguments, long[] positions) {
	return new SelectionOnParameterizedQualifiedTypeReference(tokens, assistname, typeArguments, assistTypeArguments, positions);

}
public NameReference createSingleAssistNameReference(char[] assistName, long position) {
	return new SelectionOnSingleNameReference(assistName, position);
}
public TypeReference createSingleAssistTypeReference(char[] assistName, long position) {
	return new SelectionOnSingleTypeReference(assistName, position);
}
public TypeReference createParameterizedSingleAssistTypeReference(TypeReference[] typeArguments, char[] assistName, long position) {
	return new SelectionOnParameterizedSingleTypeReference(assistName, typeArguments, position);
}
public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult, int start, int end) {

	this.selectionStart = start;
	this.selectionEnd = end;	
	SelectionScanner selectionScanner = (SelectionScanner)this.scanner;
	selectionScanner.selectionIdentifier = null;
	selectionScanner.selectionStart = start;
	selectionScanner.selectionEnd = end;	
	return this.dietParse(sourceUnit, compilationResult);
}
protected NameReference getUnspecifiedReference() {
	/* build a (unspecified) NameReference which may be qualified*/

	int completionIndex;

	/* no need to take action if not inside completed identifiers */
	if ((completionIndex = indexOfAssistIdentifier()) < 0) {
		return super.getUnspecifiedReference();
	}

	int length = identifierLengthStack[identifierLengthPtr];
	if (CharOperation.equals(assistIdentifier(), SUPER)){
		Reference reference;
		if (completionIndex > 0){ // qualified super
			// discard 'super' from identifier stacks
			identifierLengthStack[identifierLengthPtr] = completionIndex;
			int ptr = identifierPtr -= (length - completionIndex);
			pushOnGenericsLengthStack(0);
			pushOnGenericsIdentifiersLengthStack(identifierLengthStack[identifierLengthPtr]);
			reference = 
				new SelectionOnQualifiedSuperReference(
					getTypeReference(0), 
					(int)(identifierPositionStack[ptr+1] >>> 32),
					(int) identifierPositionStack[ptr+1]);
		} else { // standard super
			identifierPtr -= length;
			identifierLengthPtr--;
			reference = new SelectionOnSuperReference((int)(identifierPositionStack[identifierPtr+1] >>> 32), (int) identifierPositionStack[identifierPtr+1]);
		}
		pushOnAstStack(reference);
		this.assistNode = reference;	
		this.lastCheckPoint = reference.sourceEnd + 1;
		if (!diet || dietInt != 0){
			this.restartRecovery	= true;	// force to restart in recovery mode
			this.lastIgnoredToken = -1;		
		}
		this.isOrphanCompletionNode = true;
		return new SingleNameReference(CharOperation.NO_CHAR, 0); // dummy reference
	}
	NameReference nameReference;
	/* retrieve identifiers subset and whole positions, the completion node positions
		should include the entire replaced source. */
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
	if (completionIndex == 0) {
		/* completion inside first identifier */
		nameReference = this.createSingleAssistNameReference(assistIdentifier(), positions[0]);
	} else {
		/* completion inside subsequent identifier */
		nameReference = this.createQualifiedAssistNameReference(subset, assistIdentifier(), positions);
	}
	assistNode = nameReference;
	this.lastCheckPoint = nameReference.sourceEnd + 1;
	if (!diet){
		this.restartRecovery	= true;	// force to restart in recovery mode
		this.lastIgnoredToken = -1;	
	}
	this.isOrphanCompletionNode = true;
	return nameReference;
}
/*
 * Copy of code from superclass with the following change:
 * In the case of qualified name reference if the cursor location is on the 
 * qualified name reference, then create a CompletionOnQualifiedNameReference 
 * instead.
 */
protected NameReference getUnspecifiedReferenceOptimized() {

	int index = indexOfAssistIdentifier();
	NameReference reference = super.getUnspecifiedReferenceOptimized();

	if (index >= 0){
		if (!diet){
			this.restartRecovery	= true;	// force to restart in recovery mode
			this.lastIgnoredToken = -1;		
		}
		this.isOrphanCompletionNode = true;
	}
	return reference;
}
public void initializeScanner(){
	this.scanner = new SelectionScanner(this.options.sourceLevel);
}
protected MessageSend newMessageSend() {
	// '(' ArgumentListopt ')'
	// the arguments are on the expression stack

	char[] selector = identifierStack[identifierPtr];
	if (selector != this.assistIdentifier()){
		return super.newMessageSend();
	}	
	MessageSend messageSend = new SelectionOnMessageSend();
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(
			expressionStack, 
			expressionPtr + 1, 
			messageSend.arguments = new Expression[length], 
			0, 
			length); 
	}
	assistNode = messageSend;
	if (!diet){
		this.restartRecovery	= true;	// force to restart in recovery mode
		this.lastIgnoredToken = -1;	
	}
	
	this.isOrphanCompletionNode = true;
	return messageSend;
}
protected MessageSend newMessageSendWithTypeArguments() {
	char[] selector = identifierStack[identifierPtr];
	if (selector != this.assistIdentifier()){
		return super.newMessageSendWithTypeArguments();
	}	
	MessageSend messageSend = new SelectionOnMessageSend();
	int length;
	if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
		this.expressionPtr -= length;
		System.arraycopy(
			this.expressionStack, 
			this.expressionPtr + 1, 
			messageSend.arguments = new Expression[length], 
			0, 
			length); 
	}
	assistNode = messageSend;
	if (!diet){
		this.restartRecovery	= true;	// force to restart in recovery mode
		this.lastIgnoredToken = -1;	
	}
	
	this.isOrphanCompletionNode = true;
	return messageSend;
}
public CompilationUnitDeclaration parse(ICompilationUnit sourceUnit, CompilationResult compilationResult, int start, int end) {

	if (end == -1) return super.parse(sourceUnit, compilationResult, start, end);
	
	this.selectionStart = start;
	this.selectionEnd = end;	
	SelectionScanner selectionScanner = (SelectionScanner)this.scanner;
	selectionScanner.selectionIdentifier = null;
	selectionScanner.selectionStart = start;
	selectionScanner.selectionEnd = end;	
	return super.parse(sourceUnit, compilationResult, -1, -1/*parse without reseting the scanner*/);
}
/*
 * Reset context so as to resume to regular parse loop
 * If unable to reset for resuming, answers false.
 *
 * Move checkpoint location, reset internal stacks and
 * decide which grammar goal is activated.
 */
protected boolean resumeAfterRecovery() {

	/* if reached assist node inside method body, but still inside nested type,
		should continue in diet mode until the end of the method body */
	if (this.assistNode != null
		&& !(referenceContext instanceof CompilationUnitDeclaration)){
		currentElement.preserveEnclosingBlocks();
		if (currentElement.enclosingType() == null) {
			if(!(currentElement instanceof RecoveredType)) {
				this.resetStacks();
				return false;
			}
			
			RecoveredType recoveredType = (RecoveredType)currentElement;
			if(recoveredType.typeDeclaration != null && recoveredType.typeDeclaration.allocation == this.assistNode){
				this.resetStacks();
				return false;
			}
		}
	}
	return super.resumeAfterRecovery();			
}

public void selectionIdentifierCheck(){
	if (checkRecoveredType()) return;
}
public void setAssistIdentifier(char[] assistIdent){
	((SelectionScanner)scanner).selectionIdentifier = assistIdent;
}
/*
 * Update recovery state based on current parser/scanner state
 */
protected void updateRecoveryState() {

	/* expose parser state to recovery state */
	currentElement.updateFromParserState();

	/* may be able to retrieve completionNode as an orphan, and then attach it */
	this.selectionIdentifierCheck();
	this.attachOrphanCompletionNode();
	
	// if an assist node has been found and a recovered element exists,
	// mark enclosing blocks as to be preserved
	if (this.assistNode != null && this.currentElement != null) {
		currentElement.preserveEnclosingBlocks();
	}
	
	/* check and update recovered state based on current token,
		this action is also performed when shifting token after recovery
		got activated once. 
	*/
	this.recoveryTokenCheck();
}

public  String toString() {
	String s = Util.EMPTY_STRING;
	s = s + "elementKindStack : int[] = {"; //$NON-NLS-1$
	for (int i = 0; i <= elementPtr; i++) {
		s = s + String.valueOf(elementKindStack[i]) + ","; //$NON-NLS-1$
	}
	s = s + "}\n"; //$NON-NLS-1$
	s = s + "elementInfoStack : int[] = {"; //$NON-NLS-1$
	for (int i = 0; i <= elementPtr; i++) {
		s = s + String.valueOf(elementInfoStack[i]) + ","; //$NON-NLS-1$
	}
	s = s + "}\n"; //$NON-NLS-1$
	return s + super.toString();
}
}
