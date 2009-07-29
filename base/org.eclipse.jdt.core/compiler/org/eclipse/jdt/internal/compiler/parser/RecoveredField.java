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

/**
 * Internal field structure for parsing recovery 
 */
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

public class RecoveredField extends RecoveredElement {

	public FieldDeclaration fieldDeclaration;
	boolean alreadyCompletedFieldInitialization;
	
	public RecoveredAnnotation[] annotations;
	public int annotationCount;
	
	public int modifiers;
	public int modifiersStart;
	
	public RecoveredType[] anonymousTypes;
	public int anonymousTypeCount;
public RecoveredField(FieldDeclaration fieldDeclaration, RecoveredElement parent, int bracketBalance){
	this(fieldDeclaration, parent, bracketBalance, null);
}
public RecoveredField(FieldDeclaration fieldDeclaration, RecoveredElement parent, int bracketBalance, Parser parser){
	super(parent, bracketBalance, parser);
	this.fieldDeclaration = fieldDeclaration;
	this.alreadyCompletedFieldInitialization = fieldDeclaration.initialization != null;
}
/*
 * Record an expression statement if field is expecting an initialization expression,
 * used for completion inside field initializers.
 */
public RecoveredElement add(Statement statement, int bracketBalanceValue) {

	if (this.alreadyCompletedFieldInitialization || !(statement instanceof Expression)) {
		return super.add(statement, bracketBalanceValue);
	} else {
		this.alreadyCompletedFieldInitialization = true;
		this.fieldDeclaration.initialization = (Expression)statement;
		this.fieldDeclaration.declarationSourceEnd = statement.sourceEnd;
		this.fieldDeclaration.declarationEnd = statement.sourceEnd;
		return this;
	}
}
/*
 * Record a type declaration if this field is expecting an initialization expression 
 * and the type is an anonymous type.
 * Used for completion inside field initializers.
 */
public RecoveredElement add(TypeDeclaration typeDeclaration, int bracketBalanceValue) {

	if (this.alreadyCompletedFieldInitialization 
			|| ((typeDeclaration.bits & ASTNode.IsAnonymousType) == 0)
			|| (this.fieldDeclaration.declarationSourceEnd != 0 && typeDeclaration.sourceStart > this.fieldDeclaration.declarationSourceEnd)) {
		return super.add(typeDeclaration, bracketBalanceValue);
	} else { 
		// Prepare anonymous type list
		if (this.anonymousTypes == null) {
			this.anonymousTypes = new RecoveredType[5];
			this.anonymousTypeCount = 0;
		} else {
			if (this.anonymousTypeCount == this.anonymousTypes.length) {
				System.arraycopy(
					this.anonymousTypes, 
					0, 
					(this.anonymousTypes = new RecoveredType[2 * this.anonymousTypeCount]), 
					0, 
					this.anonymousTypeCount); 
			}
		}
		// Store type declaration as an anonymous type
		RecoveredType element = new RecoveredType(typeDeclaration, this, bracketBalanceValue);
		this.anonymousTypes[this.anonymousTypeCount++] = element;
		return element;
	}
}
public void attach(RecoveredAnnotation[] annots, int annotCount, int mods, int modsSourceStart) {
	if (annotCount > 0) {
		Annotation[] existingAnnotations = this.fieldDeclaration.annotations;
		if (existingAnnotations != null) {
			this.annotations = new RecoveredAnnotation[annotCount];
			this.annotationCount = 0;
			next : for (int i = 0; i < annotCount; i++) {
				for (int j = 0; j < existingAnnotations.length; j++) {
					if (annots[i].annotation == existingAnnotations[j]) continue next;
				}
				this.annotations[this.annotationCount++] = annots[i];
			}
		} else {
			this.annotations = annots;
			this.annotationCount = annotCount;
		}
	}
	
	if (mods != 0) {
		this.modifiers = mods;
		this.modifiersStart = modsSourceStart;
	}
}
/* 
 * Answer the associated parsed structure
 */
public ASTNode parseTree(){
	return fieldDeclaration;
}
/*
 * Answer the very source end of the corresponding parse node
 */
public int sourceEnd(){
	return this.fieldDeclaration.declarationSourceEnd;
}
public String toString(int tab){
	StringBuffer buffer = new StringBuffer(tabString(tab));
	buffer.append("Recovered field:\n"); //$NON-NLS-1$
	fieldDeclaration.print(tab + 1, buffer);
	if (this.annotations != null) {
		for (int i = 0; i < this.annotationCount; i++) {
			buffer.append("\n"); //$NON-NLS-1$
			buffer.append(this.annotations[i].toString(tab + 1));
		}
	}
	if (this.anonymousTypes != null) {
		for (int i = 0; i < this.anonymousTypeCount; i++){
			buffer.append("\n"); //$NON-NLS-1$
			buffer.append(anonymousTypes[i].toString(tab + 1));
		}
	}
	return buffer.toString();
}
public FieldDeclaration updatedFieldDeclaration(){
	/* update annotations */
	if (modifiers != 0) {
		this.fieldDeclaration.modifiers |= modifiers;
		if (this.modifiersStart < this.fieldDeclaration.declarationSourceStart) {
			this.fieldDeclaration.declarationSourceStart = modifiersStart;
		}
	}
	/* update annotations */
	if (annotationCount > 0){
		int existingCount = fieldDeclaration.annotations == null ? 0 : fieldDeclaration.annotations.length;
		Annotation[] annotationReferences = new Annotation[existingCount + annotationCount];
		if (existingCount > 0){
			System.arraycopy(fieldDeclaration.annotations, 0, annotationReferences, annotationCount, existingCount);
		}
		for (int i = 0; i < annotationCount; i++){
			annotationReferences[i] = annotations[i].updatedAnnotationReference();
		}
		fieldDeclaration.annotations = annotationReferences;
		
		int start = this.annotations[0].annotation.sourceStart;
		if (start < this.fieldDeclaration.declarationSourceStart) {
			this.fieldDeclaration.declarationSourceStart = start;
		}
	}
	
	if (this.anonymousTypes != null) {
		if(fieldDeclaration.initialization == null) {
			for (int i = 0; i < this.anonymousTypeCount; i++){
				RecoveredType recoveredType = anonymousTypes[i];
				TypeDeclaration typeDeclaration = recoveredType.typeDeclaration;
				if(typeDeclaration.declarationSourceEnd == 0) {
					typeDeclaration.declarationSourceEnd = this.fieldDeclaration.declarationSourceEnd;
					typeDeclaration.bodyEnd = this.fieldDeclaration.declarationSourceEnd;
				}
				if (recoveredType.preserveContent){
					TypeDeclaration anonymousType = recoveredType.updatedTypeDeclaration();
					fieldDeclaration.initialization = anonymousType.allocation;
					if(this.fieldDeclaration.declarationSourceEnd == 0) {
						int end = anonymousType.declarationSourceEnd;
						this.fieldDeclaration.declarationSourceEnd = end;
						this.fieldDeclaration.declarationEnd = end;
					}
				}
			}
			if (this.anonymousTypeCount > 0) fieldDeclaration.bits |= ASTNode.HasLocalType;
		} else if(fieldDeclaration.getKind() == AbstractVariableDeclaration.ENUM_CONSTANT) {
			// fieldDeclaration is an enum constant
			for (int i = 0; i < this.anonymousTypeCount; i++){
				RecoveredType recoveredType = anonymousTypes[i];
				TypeDeclaration typeDeclaration = recoveredType.typeDeclaration;
				if(typeDeclaration.declarationSourceEnd == 0) {
					typeDeclaration.declarationSourceEnd = this.fieldDeclaration.declarationSourceEnd;
					typeDeclaration.bodyEnd = this.fieldDeclaration.declarationSourceEnd;
				}
				recoveredType.updatedTypeDeclaration();
			}
		}
	}
	return fieldDeclaration;
}
/*
 * A closing brace got consumed, might have closed the current element,
 * in which case both the currentElement is exited.
 *
 * Fields have no associated braces, thus if matches, then update parent.
 */
public RecoveredElement updateOnClosingBrace(int braceStart, int braceEnd){
	if (bracketBalance > 0){ // was an array initializer
		bracketBalance--;
		if (bracketBalance == 0) {
			if(fieldDeclaration.getKind() == AbstractVariableDeclaration.ENUM_CONSTANT) {
				updateSourceEndIfNecessary(braceEnd - 1);
				return parent;
			} else {
				alreadyCompletedFieldInitialization = true;
			}
		}
		return this;
	} else if (bracketBalance == 0) {
		alreadyCompletedFieldInitialization = true;
		updateSourceEndIfNecessary(braceEnd - 1);
	}
	if (parent != null){
		return parent.updateOnClosingBrace(braceStart, braceEnd);
	}
	return this;
}
/*
 * An opening brace got consumed, might be the expected opening one of the current element,
 * in which case the bodyStart is updated.
 */
public RecoveredElement updateOnOpeningBrace(int braceStart, int braceEnd){
	if (fieldDeclaration.declarationSourceEnd == 0 
		&& (fieldDeclaration.type instanceof ArrayTypeReference || fieldDeclaration.type instanceof ArrayQualifiedTypeReference)
		&& !alreadyCompletedFieldInitialization){
		bracketBalance++;
		return null; // no update is necessary	(array initializer)
	}
	if (fieldDeclaration.declarationSourceEnd == 0 
		&& fieldDeclaration.getKind() == AbstractVariableDeclaration.ENUM_CONSTANT){
		bracketBalance++;
		return null; // no update is necessary	(enum constant)
	}
	// might be an array initializer
	this.updateSourceEndIfNecessary(braceStart - 1, braceEnd - 1);	
	return this.parent.updateOnOpeningBrace(braceStart, braceEnd);	
}
public void updateParseTree(){
	this.updatedFieldDeclaration();
}
/*
 * Update the declarationSourceEnd of the corresponding parse node
 */
public void updateSourceEndIfNecessary(int bodyStart, int bodyEnd){
	if (this.fieldDeclaration.declarationSourceEnd == 0) {
		this.fieldDeclaration.declarationSourceEnd = bodyEnd;
		this.fieldDeclaration.declarationEnd = bodyEnd;
	}
}
}
