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

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * Internal type structure for parsing recovery 
 */

public class RecoveredType extends RecoveredStatement implements TerminalTokens {
	public TypeDeclaration typeDeclaration;
	
	public RecoveredAnnotation[] annotations;
	public int annotationCount;
	
	public int modifiers;
	public int modifiersStart;

	public RecoveredType[] memberTypes;
	public int memberTypeCount;
	public RecoveredField[] fields;
	public int fieldCount;
	public RecoveredMethod[] methods;
	public int methodCount;

	public boolean preserveContent = false;	// only used for anonymous types
	public int bodyEnd;
	
	public boolean insideEnumConstantPart = false;
	
	public TypeParameter[] pendingTypeParameters;
	public int pendingTypeParametersStart;
	
	int pendingModifiers;
	int pendingModifersSourceStart = -1;
	RecoveredAnnotation[] pendingAnnotations;
	int pendingAnnotationCount;
	
public RecoveredType(TypeDeclaration typeDeclaration, RecoveredElement parent, int bracketBalance){
	super(typeDeclaration, parent, bracketBalance);
	this.typeDeclaration = typeDeclaration;
	if(typeDeclaration.allocation != null && typeDeclaration.allocation.type == null) {
		// an enum constant body can not exist if there is no opening brace
		this.foundOpeningBrace = true;
	} else {
		this.foundOpeningBrace = !bodyStartsAtHeaderEnd();
	}
	this.insideEnumConstantPart = TypeDeclaration.kind(typeDeclaration.modifiers) == TypeDeclaration.ENUM_DECL;
	if(this.foundOpeningBrace) {
		this.bracketBalance++;
	}
	
	this.preserveContent = this.parser().methodRecoveryActivated || this.parser().statementRecoveryActivated;
}
public RecoveredElement add(AbstractMethodDeclaration methodDeclaration, int bracketBalanceValue) {

	/* do not consider a method starting passed the type end (if set)
		it must be belonging to an enclosing type */
	if (typeDeclaration.declarationSourceEnd != 0 
		&& methodDeclaration.declarationSourceStart > typeDeclaration.declarationSourceEnd){
		this.pendingTypeParameters = null;
		this.resetPendingModifiers();
		
		return this.parent.add(methodDeclaration, bracketBalanceValue);
	}

	if (methods == null) {
		methods = new RecoveredMethod[5];
		methodCount = 0;
	} else {
		if (methodCount == methods.length) {
			System.arraycopy(
				methods, 
				0, 
				(methods = new RecoveredMethod[2 * methodCount]), 
				0, 
				methodCount); 
		}
	}
	RecoveredMethod element = new RecoveredMethod(methodDeclaration, this, bracketBalanceValue, this.recoveringParser);
	methods[methodCount++] = element;
	
	if(this.pendingTypeParameters != null) {
		element.attach(this.pendingTypeParameters, this.pendingTypeParametersStart);
		this.pendingTypeParameters = null;
	}
	
	if(this.pendingAnnotationCount > 0) {
		element.attach(
				pendingAnnotations,
				pendingAnnotationCount,
				pendingModifiers,
				pendingModifersSourceStart);
	}
	this.resetPendingModifiers();
	
	this.insideEnumConstantPart = false;

	/* consider that if the opening brace was not found, it is there */
	if (!foundOpeningBrace){
		foundOpeningBrace = true;
		this.bracketBalance++;
	}
	/* if method not finished, then method becomes current */
	if (methodDeclaration.declarationSourceEnd == 0) return element;
	return this;
}
public RecoveredElement add(Block nestedBlockDeclaration,int bracketBalanceValue) {
	this.pendingTypeParameters = null;
	this.resetPendingModifiers();
	
	int mods = ClassFileConstants.AccDefault;
	if(this.parser().recoveredStaticInitializerStart != 0) {
		mods = ClassFileConstants.AccStatic;
	}
	return this.add(new Initializer(nestedBlockDeclaration, mods), bracketBalanceValue);
}
public RecoveredElement add(FieldDeclaration fieldDeclaration, int bracketBalanceValue) {
	this.pendingTypeParameters = null;
	
	/* do not consider a field starting passed the type end (if set)
	it must be belonging to an enclosing type */
	if (typeDeclaration.declarationSourceEnd != 0
		&& fieldDeclaration.declarationSourceStart > typeDeclaration.declarationSourceEnd) {
		
		this.resetPendingModifiers();
		
		return this.parent.add(fieldDeclaration, bracketBalanceValue);
	}
	if (fields == null) {
		fields = new RecoveredField[5];
		fieldCount = 0;
	} else {
		if (fieldCount == fields.length) {
			System.arraycopy(
				fields, 
				0, 
				(fields = new RecoveredField[2 * fieldCount]), 
				0, 
				fieldCount); 
		}
	}
	RecoveredField element;
	switch (fieldDeclaration.getKind()) {
		case AbstractVariableDeclaration.FIELD:
		case AbstractVariableDeclaration.ENUM_CONSTANT:
			element = new RecoveredField(fieldDeclaration, this, bracketBalanceValue);
			break;
		case AbstractVariableDeclaration.INITIALIZER:
			element = new RecoveredInitializer(fieldDeclaration, this, bracketBalanceValue);
			break;
		default:
			// never happens, as field is always identified
			return this;
	}
	fields[fieldCount++] = element;
	
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
	/* if field not finished, then field becomes current */
	if (fieldDeclaration.declarationSourceEnd == 0) return element;
	return this;
}
public RecoveredElement add(TypeDeclaration memberTypeDeclaration, int bracketBalanceValue) {
	this.pendingTypeParameters = null;
	
	/* do not consider a type starting passed the type end (if set)
		it must be belonging to an enclosing type */
	if (typeDeclaration.declarationSourceEnd != 0 
		&& memberTypeDeclaration.declarationSourceStart > typeDeclaration.declarationSourceEnd){
		
		this.resetPendingModifiers();
		
		return this.parent.add(memberTypeDeclaration, bracketBalanceValue);
	}
	
	this.insideEnumConstantPart = false;
	
	if ((memberTypeDeclaration.bits & ASTNode.IsAnonymousType) != 0){
		if (this.methodCount > 0) {
			// add it to the last method body
			RecoveredMethod lastMethod = this.methods[this.methodCount-1];
			lastMethod.methodDeclaration.bodyEnd = 0; // reopen method
			lastMethod.methodDeclaration.declarationSourceEnd = 0; // reopen method
			lastMethod.bracketBalance++; // expect one closing brace
			
			this.resetPendingModifiers();
			
			return lastMethod.add(memberTypeDeclaration, bracketBalanceValue);
		} else {
			// ignore
			return this;
		}
	}	
		
	if (memberTypes == null) {
		memberTypes = new RecoveredType[5];
		memberTypeCount = 0;
	} else {
		if (memberTypeCount == memberTypes.length) {
			System.arraycopy(
				memberTypes, 
				0, 
				(memberTypes = new RecoveredType[2 * memberTypeCount]), 
				0, 
				memberTypeCount); 
		}
	}
	RecoveredType element = new RecoveredType(memberTypeDeclaration, this, bracketBalanceValue);
	memberTypes[memberTypeCount++] = element;
	
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
	/* if member type not finished, then member type becomes current */
	if (memberTypeDeclaration.declarationSourceEnd == 0) return element;
	return this;
}
public void add(TypeParameter[] parameters, int startPos) {
	this.pendingTypeParameters = parameters;
	this.pendingTypeParametersStart = startPos;
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
public void attach(RecoveredAnnotation[] annots, int annotCount, int mods, int modsSourceStart) {
	if (annotCount > 0) {
		Annotation[] existingAnnotations = this.typeDeclaration.annotations;
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
 * Answer the body end of the corresponding parse node
 */
public int bodyEnd(){
	if (bodyEnd == 0) return typeDeclaration.declarationSourceEnd;
	return bodyEnd;
}
public boolean bodyStartsAtHeaderEnd(){
	if (typeDeclaration.superInterfaces == null){
		if (typeDeclaration.superclass == null){
			if(typeDeclaration.typeParameters == null) {
				return typeDeclaration.bodyStart == typeDeclaration.sourceEnd+1;
			} else {
				return typeDeclaration.bodyStart == typeDeclaration.typeParameters[typeDeclaration.typeParameters.length-1].sourceEnd+1;
			}
		} else {
			return typeDeclaration.bodyStart == typeDeclaration.superclass.sourceEnd+1;
		}
	} else {
		return typeDeclaration.bodyStart 
				== typeDeclaration.superInterfaces[typeDeclaration.superInterfaces.length-1].sourceEnd+1;
	}
}
/*
 * Answer the enclosing type node, or null if none
 */
public RecoveredType enclosingType(){
	RecoveredElement current = parent;
	while (current != null){
		if (current instanceof RecoveredType){
			return (RecoveredType) current;
		}
		current = current.parent;
	}
	return null;
}
public int lastMemberEnd() {
	int lastMemberEnd = this.typeDeclaration.bodyStart;
	
	if (this.fieldCount > 0) {
		FieldDeclaration lastField = this.fields[fieldCount - 1].fieldDeclaration;
		if (lastMemberEnd < lastField.declarationSourceEnd && lastField.declarationSourceEnd != 0) {
			lastMemberEnd = lastField.declarationSourceEnd;
		}
	}
	
	if (this.methodCount > 0) {
		AbstractMethodDeclaration lastMethod = this.methods[this.methodCount - 1].methodDeclaration;
		if (lastMemberEnd < lastMethod.declarationSourceEnd && lastMethod.declarationSourceEnd != 0) {
			lastMemberEnd = lastMethod.declarationSourceEnd;
		}
	}
	
	if (this.memberTypeCount > 0) {
		TypeDeclaration lastType = this.memberTypes[this.memberTypeCount - 1].typeDeclaration;
		if (lastMemberEnd < lastType.declarationSourceEnd && lastType.declarationSourceEnd != 0) {
			lastMemberEnd = lastType.declarationSourceEnd;
		}
	}
	
	return lastMemberEnd;
}
public char[] name(){
	return typeDeclaration.name;
}
/* 
 * Answer the associated parsed structure
 */
public ASTNode parseTree(){
	return typeDeclaration;
}
public void resetPendingModifiers() {
	this.pendingAnnotations = null;
	this.pendingAnnotationCount = 0;
	this.pendingModifiers = 0;
	this.pendingModifersSourceStart = -1;
}
/*
 * Answer the very source end of the corresponding parse node
 */
public int sourceEnd(){
	return this.typeDeclaration.declarationSourceEnd;
}
public String toString(int tab) {
	StringBuffer result = new StringBuffer(tabString(tab));
	result.append("Recovered type:\n"); //$NON-NLS-1$
	if ((typeDeclaration.bits & ASTNode.IsAnonymousType) != 0) {
		result.append(tabString(tab));
		result.append(" "); //$NON-NLS-1$
	}
	typeDeclaration.print(tab + 1, result);
	if (this.annotations != null) {
		for (int i = 0; i < this.annotationCount; i++) {
			result.append("\n"); //$NON-NLS-1$
			result.append(this.annotations[i].toString(tab + 1));
		}
	}
	if (this.memberTypes != null) {
		for (int i = 0; i < this.memberTypeCount; i++) {
			result.append("\n"); //$NON-NLS-1$
			result.append(this.memberTypes[i].toString(tab + 1));
		}
	}
	if (this.fields != null) {
		for (int i = 0; i < this.fieldCount; i++) {
			result.append("\n"); //$NON-NLS-1$
			result.append(this.fields[i].toString(tab + 1));
		}
	}
	if (this.methods != null) {
		for (int i = 0; i < this.methodCount; i++) {
			result.append("\n"); //$NON-NLS-1$
			result.append(this.methods[i].toString(tab + 1));
		}
	}
	return result.toString();
}
/*
 * Update the bodyStart of the corresponding parse node
 */
public void updateBodyStart(int bodyStart){
	this.foundOpeningBrace = true;
	this.typeDeclaration.bodyStart = bodyStart;
}
public Statement updatedStatement(){

	// ignore closed anonymous type
	if ((typeDeclaration.bits & ASTNode.IsAnonymousType) != 0 && !this.preserveContent){
		return null;
	}
		
	TypeDeclaration updatedType = this.updatedTypeDeclaration();
	if ((updatedType.bits & ASTNode.IsAnonymousType) != 0){
		/* in presence of an anonymous type, we want the full allocation expression */
		QualifiedAllocationExpression allocation = updatedType.allocation;
		
		if (allocation.statementEnd == -1) {
			allocation.statementEnd = updatedType.declarationSourceEnd;
		}
		return allocation;
	}
	return updatedType;
}
public TypeDeclaration updatedTypeDeclaration(){
	int lastEnd = typeDeclaration.bodyStart;
	/* update annotations */
	if (modifiers != 0) {
		this.typeDeclaration.modifiers |= modifiers;
		if (this.modifiersStart < this.typeDeclaration.declarationSourceStart) {
			this.typeDeclaration.declarationSourceStart = modifiersStart;
		}
	}
	/* update annotations */
	if (annotationCount > 0){
		int existingCount = typeDeclaration.annotations == null ? 0 : typeDeclaration.annotations.length;
		Annotation[] annotationReferences = new Annotation[existingCount + annotationCount];
		if (existingCount > 0){
			System.arraycopy(typeDeclaration.annotations, 0, annotationReferences, annotationCount, existingCount);
		}
		for (int i = 0; i < annotationCount; i++){
			annotationReferences[i] = annotations[i].updatedAnnotationReference();
		}
		typeDeclaration.annotations = annotationReferences;
		
		int start = this.annotations[0].annotation.sourceStart;
		if (start < this.typeDeclaration.declarationSourceStart) {
			this.typeDeclaration.declarationSourceStart = start;
		}
	}
	/* update member types */
	if (memberTypeCount > 0){
		int existingCount = typeDeclaration.memberTypes == null ? 0 : typeDeclaration.memberTypes.length;
		TypeDeclaration[] memberTypeDeclarations = new TypeDeclaration[existingCount + memberTypeCount];
		if (existingCount > 0){
			System.arraycopy(typeDeclaration.memberTypes, 0, memberTypeDeclarations, 0, existingCount);
		}
		// may need to update the declarationSourceEnd of the last type
		if (memberTypes[memberTypeCount - 1].typeDeclaration.declarationSourceEnd == 0){
			int bodyEndValue = bodyEnd();
			memberTypes[memberTypeCount - 1].typeDeclaration.declarationSourceEnd = bodyEndValue;
			memberTypes[memberTypeCount - 1].typeDeclaration.bodyEnd =  bodyEndValue;
		}
		for (int i = 0; i < memberTypeCount; i++){
			memberTypeDeclarations[existingCount + i] = memberTypes[i].updatedTypeDeclaration();
		}
		typeDeclaration.memberTypes = memberTypeDeclarations;
		if(memberTypeDeclarations[memberTypeDeclarations.length - 1].declarationSourceEnd > lastEnd) {
			lastEnd = memberTypeDeclarations[memberTypeDeclarations.length - 1].declarationSourceEnd;
		}
	}
	/* update fields */
	if (fieldCount > 0){
		int existingCount = typeDeclaration.fields == null ? 0 : typeDeclaration.fields.length;
		FieldDeclaration[] fieldDeclarations = new FieldDeclaration[existingCount + fieldCount];
		if (existingCount > 0){
			System.arraycopy(typeDeclaration.fields, 0, fieldDeclarations, 0, existingCount);
		}
		// may need to update the declarationSourceEnd of the last field
		if (fields[fieldCount - 1].fieldDeclaration.declarationSourceEnd == 0){
			int temp = bodyEnd();
			fields[fieldCount - 1].fieldDeclaration.declarationSourceEnd = temp;
			fields[fieldCount - 1].fieldDeclaration.declarationEnd = temp;
		}
		for (int i = 0; i < fieldCount; i++){
			fieldDeclarations[existingCount + i] = fields[i].updatedFieldDeclaration();
		}
		typeDeclaration.fields = fieldDeclarations;
		if(fieldDeclarations[fieldDeclarations.length - 1].declarationSourceEnd > lastEnd) {
			lastEnd = fieldDeclarations[fieldDeclarations.length - 1].declarationSourceEnd;
		}
	}
	/* update methods */
	int existingCount = typeDeclaration.methods == null ? 0 : typeDeclaration.methods.length;
	boolean hasConstructor = false, hasRecoveredConstructor = false;
	boolean hasAbstractMethods = false;
	int defaultConstructorIndex = -1;
	if (methodCount > 0){
		AbstractMethodDeclaration[] methodDeclarations = new AbstractMethodDeclaration[existingCount + methodCount];
		for (int i = 0; i < existingCount; i++){
			AbstractMethodDeclaration m = typeDeclaration.methods[i];
			if (m.isDefaultConstructor()) defaultConstructorIndex = i;
			if (m.isAbstract()) hasAbstractMethods = true;
			methodDeclarations[i] = m;
		}
		// may need to update the declarationSourceEnd of the last method
		if (methods[methodCount - 1].methodDeclaration.declarationSourceEnd == 0){
			int bodyEndValue = bodyEnd();
			methods[methodCount - 1].methodDeclaration.declarationSourceEnd = bodyEndValue;
			methods[methodCount - 1].methodDeclaration.bodyEnd = bodyEndValue;
		}
		for (int i = 0; i < methodCount; i++){
			AbstractMethodDeclaration updatedMethod = methods[i].updatedMethodDeclaration();			
			if (updatedMethod.isConstructor()) hasRecoveredConstructor = true;
			if (updatedMethod.isAbstract()) hasAbstractMethods = true;
			methodDeclarations[existingCount + i] = updatedMethod;			
		}
		typeDeclaration.methods = methodDeclarations;
		if(methodDeclarations[methodDeclarations.length - 1].declarationSourceEnd > lastEnd) {
			lastEnd = methodDeclarations[methodDeclarations.length - 1].declarationSourceEnd;
		}
		if (hasAbstractMethods) typeDeclaration.bits |= ASTNode.HasAbstractMethods;
		hasConstructor = typeDeclaration.checkConstructors(this.parser());
	} else {
		for (int i = 0; i < existingCount; i++){
			if (typeDeclaration.methods[i].isConstructor()) hasConstructor = true;
		}		
	}
	/* add clinit ? */
	if (typeDeclaration.needClassInitMethod()){
		boolean alreadyHasClinit = false;
		for (int i = 0; i < existingCount; i++){
			if (typeDeclaration.methods[i].isClinit()){
				alreadyHasClinit = true;
				break;
			}
		}
		if (!alreadyHasClinit) typeDeclaration.addClinit();
	}
	/* add default constructor ? */
	if (defaultConstructorIndex >= 0 && hasRecoveredConstructor){
		/* should discard previous default construtor */
		AbstractMethodDeclaration[] methodDeclarations = new AbstractMethodDeclaration[typeDeclaration.methods.length - 1];
		if (defaultConstructorIndex != 0){
			System.arraycopy(typeDeclaration.methods, 0, methodDeclarations, 0, defaultConstructorIndex);
		}
		if (defaultConstructorIndex != typeDeclaration.methods.length-1){
			System.arraycopy(
				typeDeclaration.methods, 
				defaultConstructorIndex+1, 
				methodDeclarations, 
				defaultConstructorIndex, 
				typeDeclaration.methods.length - defaultConstructorIndex - 1);
		}
		typeDeclaration.methods = methodDeclarations;
	} else {
		int kind = TypeDeclaration.kind(typeDeclaration.modifiers);
		if (!hasConstructor &&
				kind != TypeDeclaration.INTERFACE_DECL &&
				kind != TypeDeclaration.ANNOTATION_TYPE_DECL &&
				typeDeclaration.allocation == null) {// if was already reduced, then constructor
			boolean insideFieldInitializer = false;
			RecoveredElement parentElement = this.parent; 
			while (parentElement != null){
				if (parentElement instanceof RecoveredField){
						insideFieldInitializer = true;
						break; 
				}
				parentElement = parentElement.parent;
			}
			typeDeclaration.createDefaultConstructor(!parser().diet || insideFieldInitializer, true);
		} 
	}
	if (parent instanceof RecoveredType){
		typeDeclaration.bits |= ASTNode.IsMemberType;
	} else if (parent instanceof RecoveredMethod){
		typeDeclaration.bits |= ASTNode.IsLocalType;
	}
	if(typeDeclaration.declarationSourceEnd == 0) {
		typeDeclaration.declarationSourceEnd = lastEnd;
		typeDeclaration.bodyEnd = lastEnd;
	}
	return typeDeclaration;
}
/*
 * Update the corresponding parse node from parser state which
 * is about to disappear because of restarting recovery
 */
public void updateFromParserState(){

	// anymous type and enum constant doesn't need to be updated
	if(this.bodyStartsAtHeaderEnd() && typeDeclaration.allocation == null){
		Parser parser = this.parser();
		/* might want to recover implemented interfaces */
		// protection for bugs 15142
		if (parser.listLength > 0 && parser.astLengthPtr > 0){ // awaiting interface type references
			int length = parser.astLengthStack[parser.astLengthPtr];
			int astPtr = parser.astPtr - length;
			boolean canConsume = astPtr >= 0;
			if(canConsume) {
				if((!(parser.astStack[astPtr] instanceof TypeDeclaration))) {
					canConsume = false;
				}
				for (int i = 1, max = length + 1; i < max; i++) {
					if(!(parser.astStack[astPtr + i ] instanceof TypeReference)) {
						canConsume = false;
					}
				}
			}
			if(canConsume) {
				parser.consumeClassHeaderImplements(); 
				// will reset typeListLength to zero
				// thus this check will only be performed on first errorCheck after class X implements Y,Z,
			}
		} else if (parser.listTypeParameterLength > 0) {
			int length = parser.listTypeParameterLength;
			int genericsPtr = parser.genericsPtr;
			boolean canConsume = genericsPtr + 1 >= length && parser.astPtr > -1;
			if(canConsume) {
				if (!(parser.astStack[parser.astPtr] instanceof TypeDeclaration)) {
					canConsume = false;
				}
				while(genericsPtr + 1 > length && !(parser.genericsStack[genericsPtr] instanceof TypeParameter)) {
					genericsPtr--;
				}
				for (int i = 0; i < length; i++) {
					if(!(parser.genericsStack[genericsPtr - i] instanceof TypeParameter)) {
						canConsume = false;
					}
				}
			}
			if(canConsume) {
				TypeDeclaration typeDecl = (TypeDeclaration)parser.astStack[parser.astPtr];
				System.arraycopy(parser.genericsStack, genericsPtr - length + 1, typeDecl.typeParameters = new TypeParameter[length], 0, length);
				typeDecl.bodyStart = typeDecl.typeParameters[length-1].declarationSourceEnd + 1;
				parser.listTypeParameterLength = 0;
				parser.lastCheckPoint = typeDecl.bodyStart;
			}
		}
	}
}
/*
 * A closing brace got consumed, might have closed the current element,
 * in which case both the currentElement is exited
 */
public RecoveredElement updateOnClosingBrace(int braceStart, int braceEnd){
	if ((--bracketBalance <= 0) && (parent != null)){
		this.updateSourceEndIfNecessary(braceStart, braceEnd);
		this.bodyEnd = braceStart - 1;
		return parent;
	}
	return this;
}
/*
 * An opening brace got consumed, might be the expected opening one of the current element,
 * in which case the bodyStart is updated.
 */
public RecoveredElement updateOnOpeningBrace(int braceStart, int braceEnd){
	/* in case the opening brace is not close enough to the signature, ignore it */
	if (bracketBalance == 0){
		/*
			if (parser.scanner.searchLineNumber(typeDeclaration.sourceEnd) 
				!= parser.scanner.searchLineNumber(braceEnd)){
		 */
		Parser parser = this.parser();
		switch(parser.lastIgnoredToken){
			case -1 :
			case TokenNameextends :
			case TokenNameimplements :
			case TokenNameGREATER :
			case TokenNameRIGHT_SHIFT :
			case TokenNameUNSIGNED_RIGHT_SHIFT :
				if (parser.recoveredStaticInitializerStart == 0) break;
			default:
				this.foundOpeningBrace = true;				
				bracketBalance = 1; // pretend the brace was already there
		}
	}	
	// might be an initializer
	if (this.bracketBalance == 1){
		Block block = new Block(0);
		Parser parser = this.parser();
		block.sourceStart = parser.scanner.startPosition;
		Initializer init;
		if (parser.recoveredStaticInitializerStart == 0){
			init = new Initializer(block, ClassFileConstants.AccDefault);
		} else {
			init = new Initializer(block, ClassFileConstants.AccStatic);
			init.declarationSourceStart = parser.recoveredStaticInitializerStart;
		}
		init.bodyStart = parser.scanner.currentPosition;
		return this.add(init, 1);
	}
	return super.updateOnOpeningBrace(braceStart, braceEnd);
}
public void updateParseTree(){
	this.updatedTypeDeclaration();
}
/*
 * Update the declarationSourceEnd of the corresponding parse node
 */
public void updateSourceEndIfNecessary(int start, int end){
	if (this.typeDeclaration.declarationSourceEnd == 0){
		this.bodyEnd = 0;
		this.typeDeclaration.declarationSourceEnd = end;
		this.typeDeclaration.bodyEnd = end;
	}
}
}
