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
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class RecoveredInitializer extends RecoveredField implements TerminalTokens {

	public RecoveredType[] localTypes;
	public int localTypeCount;

	public RecoveredBlock initializerBody;	
	
	int pendingModifiers;
	int pendingModifersSourceStart = -1;
	RecoveredAnnotation[] pendingAnnotations;
	int pendingAnnotationCount;

public RecoveredInitializer(FieldDeclaration fieldDeclaration, RecoveredElement parent, int bracketBalance){
	this(fieldDeclaration, parent, bracketBalance, null);
}
public RecoveredInitializer(FieldDeclaration fieldDeclaration, RecoveredElement parent, int bracketBalance, Parser parser){
	super(fieldDeclaration, parent, bracketBalance, parser);
	this.foundOpeningBrace = true;
}
/*
 * Record a nested block declaration
 */
public RecoveredElement add(Block nestedBlockDeclaration, int bracketBalanceValue) {

	/* default behavior is to delegate recording to parent if any,
	do not consider elements passed the known end (if set)
	it must be belonging to an enclosing element 
	*/
	if (fieldDeclaration.declarationSourceEnd > 0
			&& nestedBlockDeclaration.sourceStart > fieldDeclaration.declarationSourceEnd){
		resetPendingModifiers();
		if (this.parent == null) return this; // ignore
		return this.parent.add(nestedBlockDeclaration, bracketBalanceValue);
	}
	/* consider that if the opening brace was not found, it is there */
	if (!foundOpeningBrace){
		foundOpeningBrace = true;
		this.bracketBalance++;
	}
	initializerBody = new RecoveredBlock(nestedBlockDeclaration, this, bracketBalanceValue);
	if (nestedBlockDeclaration.sourceEnd == 0) return initializerBody;
	return this;
}
/*
 * Record a field declaration (act like inside method body)
 */
public RecoveredElement add(FieldDeclaration newFieldDeclaration, int bracketBalanceValue) {
	this.resetPendingModifiers();

	/* local variables inside initializer can only be final and non void */
	char[][] fieldTypeName;
	if ((newFieldDeclaration.modifiers & ~ClassFileConstants.AccFinal) != 0 /* local var can only be final */
			|| (newFieldDeclaration.type == null) // initializer
			|| ((fieldTypeName = newFieldDeclaration.type.getTypeName()).length == 1 // non void
				&& CharOperation.equals(fieldTypeName[0], TypeBinding.VOID.sourceName()))){ 
		if (this.parent == null) return this; // ignore
		this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(newFieldDeclaration.declarationSourceStart - 1));
		return this.parent.add(newFieldDeclaration, bracketBalanceValue);
	}

	/* default behavior is to delegate recording to parent if any,
	do not consider elements passed the known end (if set)
	it must be belonging to an enclosing element 
	*/
	if (this.fieldDeclaration.declarationSourceEnd > 0
			&& newFieldDeclaration.declarationSourceStart > this.fieldDeclaration.declarationSourceEnd){
		if (this.parent == null) return this; // ignore
		return this.parent.add(newFieldDeclaration, bracketBalanceValue);
	}
	// still inside initializer, treat as local variable
	return this; // ignore
}
/*
 * Record a local declaration - regular method should have been created a block body
 */
public RecoveredElement add(LocalDeclaration localDeclaration, int bracketBalanceValue) {

	/* do not consider a type starting passed the type end (if set)
		it must be belonging to an enclosing type */
	if (fieldDeclaration.declarationSourceEnd != 0 
			&& localDeclaration.declarationSourceStart > fieldDeclaration.declarationSourceEnd){
		resetPendingModifiers();
		if (parent == null) return this; // ignore
		return this.parent.add(localDeclaration, bracketBalanceValue);
	}
	/* method body should have been created */
	Block block = new Block(0);
	block.sourceStart = ((Initializer)fieldDeclaration).sourceStart;
	RecoveredElement element = this.add(block, 1);
	if (initializerBody != null) {
		initializerBody.attachPendingModifiers(
				this.pendingAnnotations,
				this.pendingAnnotationCount,
				this.pendingModifiers,
				this.pendingModifersSourceStart);
	}
	this.resetPendingModifiers();
	return element.add(localDeclaration, bracketBalanceValue);	
}
/*
 * Record a statement - regular method should have been created a block body
 */
public RecoveredElement add(Statement statement, int bracketBalanceValue) {

	/* do not consider a statement starting passed the initializer end (if set)
		it must be belonging to an enclosing type */
	if (fieldDeclaration.declarationSourceEnd != 0 
			&& statement.sourceStart > fieldDeclaration.declarationSourceEnd){
		resetPendingModifiers();
		if (parent == null) return this; // ignore
		return this.parent.add(statement, bracketBalanceValue);
	}
	/* initializer body should have been created */
	Block block = new Block(0);
	block.sourceStart = ((Initializer)fieldDeclaration).sourceStart;
	RecoveredElement element = this.add(block, 1);
	
	if (initializerBody != null) {
		initializerBody.attachPendingModifiers(
				this.pendingAnnotations,
				this.pendingAnnotationCount,
				this.pendingModifiers,
				this.pendingModifersSourceStart);
	}
	this.resetPendingModifiers();
		
	return element.add(statement, bracketBalanceValue);	
}
public RecoveredElement add(TypeDeclaration typeDeclaration, int bracketBalanceValue) {

	/* do not consider a type starting passed the type end (if set)
		it must be belonging to an enclosing type */
	if (fieldDeclaration.declarationSourceEnd != 0 
			&& typeDeclaration.declarationSourceStart > fieldDeclaration.declarationSourceEnd){
		resetPendingModifiers();
		if (parent == null) return this; // ignore
		return this.parent.add(typeDeclaration, bracketBalanceValue);
	}
	if ((typeDeclaration.bits & ASTNode.IsLocalType) != 0  || this.parser().methodRecoveryActivated || this.parser().statementRecoveryActivated){
		/* method body should have been created */
		Block block = new Block(0);
		block.sourceStart = ((Initializer)fieldDeclaration).sourceStart;
		RecoveredElement element = this.add(block, 1);
		if (initializerBody != null) {
			initializerBody.attachPendingModifiers(
					this.pendingAnnotations,
					this.pendingAnnotationCount,
					this.pendingModifiers,
					this.pendingModifersSourceStart);
		}
		this.resetPendingModifiers();
		return element.add(typeDeclaration, bracketBalanceValue);	
	}	
	if (localTypes == null) {
		localTypes = new RecoveredType[5];
		localTypeCount = 0;
	} else {
		if (localTypeCount == localTypes.length) {
			System.arraycopy(
				localTypes, 
				0, 
				(localTypes = new RecoveredType[2 * localTypeCount]), 
				0, 
				localTypeCount); 
		}
	}
	RecoveredType element = new RecoveredType(typeDeclaration, this, bracketBalanceValue);
	localTypes[localTypeCount++] = element;
	
	if(this.pendingAnnotationCount > 0) {
		element.attach(
				pendingAnnotations,
				pendingAnnotationCount,
				pendingModifiers,
				pendingModifersSourceStart);
	}
	this.resetPendingModifiers();
	
	/* consider that if the opening brace was not found, it is there */
	if (!foundOpeningBrace){
		foundOpeningBrace = true;
		this.bracketBalance++;
	}
	return element;
}
public RecoveredElement addAnnotationName(int identifierPtr, int identifierLengthPtr, int annotationStart, int bracketBalanceValue) {
	if (pendingAnnotations == null) {
		pendingAnnotations = new RecoveredAnnotation[5];
		pendingAnnotationCount = 0;
	} else {
		if (pendingAnnotationCount == pendingAnnotations.length) {
			System.arraycopy(
				pendingAnnotations, 
				0, 
				(pendingAnnotations = new RecoveredAnnotation[2 * pendingAnnotationCount]), 
				0, 
				pendingAnnotationCount); 
		}
	}
	
	RecoveredAnnotation element = new RecoveredAnnotation(identifierPtr, identifierLengthPtr, annotationStart, this, bracketBalanceValue);
	
	pendingAnnotations[pendingAnnotationCount++] = element;
	
	return element;
}
public void addModifier(int flag, int modifiersSourceStart) {
	this.pendingModifiers |= flag;
	
	if (this.pendingModifersSourceStart < 0) {
		this.pendingModifersSourceStart = modifiersSourceStart;
	}
}
public void resetPendingModifiers() {
	this.pendingAnnotations = null;
	this.pendingAnnotationCount = 0;
	this.pendingModifiers = 0;
	this.pendingModifersSourceStart = -1;
}
public String toString(int tab) {
	StringBuffer result = new StringBuffer(tabString(tab));
	result.append("Recovered initializer:\n"); //$NON-NLS-1$
	this.fieldDeclaration.print(tab + 1, result);
	if (this.annotations != null) {
		for (int i = 0; i < this.annotationCount; i++) {
			result.append("\n"); //$NON-NLS-1$
			result.append(this.annotations[i].toString(tab + 1));
		}
	}
	if (this.initializerBody != null) {
		result.append("\n"); //$NON-NLS-1$
		result.append(this.initializerBody.toString(tab + 1));
	}
	return result.toString();
}
public FieldDeclaration updatedFieldDeclaration(){

	if (initializerBody != null){
		Block block = initializerBody.updatedBlock();
		if (block != null){
			Initializer initializer = (Initializer) fieldDeclaration;
			initializer.block = block;
			
			if (initializer.declarationSourceEnd == 0) {
				initializer.declarationSourceEnd = block.sourceEnd;
				initializer.bodyEnd = block.sourceEnd;
			}
		}
		if (this.localTypeCount > 0) fieldDeclaration.bits |= ASTNode.HasLocalType;

	}	
	if (fieldDeclaration.sourceEnd == 0){
		fieldDeclaration.sourceEnd = fieldDeclaration.declarationSourceEnd;
	}
	return fieldDeclaration;
}
/*
 * A closing brace got consumed, might have closed the current element,
 * in which case both the currentElement is exited
 */
public RecoveredElement updateOnClosingBrace(int braceStart, int braceEnd){
	if ((--bracketBalance <= 0) && (parent != null)){
		this.updateSourceEndIfNecessary(braceStart, braceEnd);
		return parent;
	}
	return this;
}
/*
 * An opening brace got consumed, might be the expected opening one of the current element,
 * in which case the bodyStart is updated.
 */
public RecoveredElement updateOnOpeningBrace(int braceStart, int braceEnd){
	bracketBalance++;
	return this; // request to restart
}
/*
 * Update the declarationSourceEnd of the corresponding parse node
 */
public void updateSourceEndIfNecessary(int braceStart, int braceEnd){
	if (this.fieldDeclaration.declarationSourceEnd == 0) {
		Initializer initializer = (Initializer)fieldDeclaration;
		if(parser().rBraceSuccessorStart >= braceEnd) {
			if (initializer.bodyStart < parser().rBraceEnd) {
				initializer.declarationSourceEnd = parser().rBraceEnd;
			} else {
				initializer.declarationSourceEnd = initializer.bodyStart;
			}
			if (initializer.bodyStart < parser().rBraceStart) {
				initializer.bodyEnd = parser().rBraceStart;
			} else {
				initializer.bodyEnd = initializer.bodyStart;
			}
		} else {
			initializer.declarationSourceEnd = braceEnd;
			initializer.bodyEnd  = braceStart - 1;
		}
		if(initializer.block != null) {
			initializer.block.sourceEnd = initializer.declarationSourceEnd;
		}
	}
}
}
