/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.matching.*;

public abstract class AbstractIndexer implements IIndexConstants {

	SearchDocument document;

	public AbstractIndexer(SearchDocument document) {
		this.document = document;
	}
	public void addAnnotationTypeDeclaration(int modifiers, char[] packageName, char[] name, char[][] enclosingTypeNames, boolean secondary) {
		addTypeDeclaration(modifiers, packageName, name, enclosingTypeNames, secondary);

		addIndexEntry(
			SUPER_REF,
			SuperTypeReferencePattern.createIndexKey(
				modifiers, packageName, name, enclosingTypeNames, null, ANNOTATION_TYPE_SUFFIX, CharOperation.concatWith(TypeConstants.JAVA_LANG_ANNOTATION_ANNOTATION, '.'), ANNOTATION_TYPE_SUFFIX));
	}
	public void addAnnotationTypeReference(char[] typeName) {
		addIndexEntry(ANNOTATION_REF, CharOperation.lastSegment(typeName, '.'));
	}
	public void addClassDeclaration(
			int modifiers,
			char[] packageName,
			char[] name,
			char[][] enclosingTypeNames,
			char[] superclass,
			char[][] superinterfaces,
			char[][] typeParameterSignatures,
			boolean secondary) {
		addTypeDeclaration(modifiers, packageName, name, enclosingTypeNames, secondary);

		if (superclass != null) {
			superclass = erasure(superclass);
			addTypeReference(superclass);
		}
		addIndexEntry(
			SUPER_REF,
			SuperTypeReferencePattern.createIndexKey(
				modifiers, packageName, name, enclosingTypeNames, typeParameterSignatures, CLASS_SUFFIX, superclass, CLASS_SUFFIX));
		if (superinterfaces != null) {
			for (int i = 0, max = superinterfaces.length; i < max; i++) {
				char[] superinterface = erasure(superinterfaces[i]);
				addTypeReference(superinterface);
				addIndexEntry(
					SUPER_REF,
					SuperTypeReferencePattern.createIndexKey(
						modifiers, packageName, name, enclosingTypeNames, typeParameterSignatures, CLASS_SUFFIX, superinterface, INTERFACE_SUFFIX));
			}
		}
	}
	private char[] erasure(char[] typeName) {
		int genericStart = CharOperation.indexOf(Signature.C_GENERIC_START, typeName);
		if (genericStart > -1)
			typeName = CharOperation.subarray(typeName, 0, genericStart);
		return typeName;
	}
	public void addConstructorDeclaration(
			char[] typeName,
			int argCount,
			char[] signature,
			char[][] parameterTypes,
			char[][] parameterNames,
			int modifiers,
			char[] packageName,
			int typeModifiers,
			char[][] exceptionTypes,
			int extraFlags) {
		addIndexEntry(
				CONSTRUCTOR_DECL,
				ConstructorPattern.createDeclarationIndexKey(
						typeName,
						argCount,
						signature,
						parameterTypes,
						parameterNames,
						modifiers,
						packageName,
						typeModifiers,
						extraFlags));
		
		if (parameterTypes != null) {
			for (int i = 0; i < argCount; i++)
				addTypeReference(parameterTypes[i]);
		}
		if (exceptionTypes != null)
			for (int i = 0, max = exceptionTypes.length; i < max; i++)
				addTypeReference(exceptionTypes[i]);
	}
	public void addConstructorReference(char[] typeName, int argCount) {
		char[] simpleTypeName = CharOperation.lastSegment(typeName,'.');
		addTypeReference(simpleTypeName);
		addIndexEntry(CONSTRUCTOR_REF, ConstructorPattern.createIndexKey(simpleTypeName, argCount));
		char[] innermostTypeName = CharOperation.lastSegment(simpleTypeName,'$');
		if (innermostTypeName != simpleTypeName)
			addIndexEntry(CONSTRUCTOR_REF, ConstructorPattern.createIndexKey(innermostTypeName, argCount));
	}
	public void addDefaultConstructorDeclaration(
			char[] typeName,
			char[] packageName,
			int typeModifiers,
			int extraFlags) {
		addIndexEntry(CONSTRUCTOR_DECL, ConstructorPattern.createDefaultDeclarationIndexKey(CharOperation.lastSegment(typeName,'.'), packageName, typeModifiers, extraFlags));
	}
	public void addEnumDeclaration(int modifiers, char[] packageName, char[] name, char[][] enclosingTypeNames, char[] superclass, char[][] superinterfaces, boolean secondary) {
		addTypeDeclaration(modifiers, packageName, name, enclosingTypeNames, secondary);

		addIndexEntry(
			SUPER_REF,
			SuperTypeReferencePattern.createIndexKey(
				modifiers, packageName, name, enclosingTypeNames, null, ENUM_SUFFIX, superclass, CLASS_SUFFIX));
		if (superinterfaces != null) {
			for (int i = 0, max = superinterfaces.length; i < max; i++) {
				char[] superinterface = erasure(superinterfaces[i]);
				addTypeReference(superinterface);
				addIndexEntry(
					SUPER_REF,
					SuperTypeReferencePattern.createIndexKey(
						modifiers, packageName, name, enclosingTypeNames, null, ENUM_SUFFIX, superinterface, INTERFACE_SUFFIX));
			}
		}
	}
	public void addFieldDeclaration(char[] typeName, char[] fieldName) {
		addIndexEntry(FIELD_DECL, FieldPattern.createIndexKey(fieldName));
		addTypeReference(typeName);
	}
	public void addFieldReference(char[] fieldName) {
		addNameReference(fieldName);
	}
	protected void addIndexEntry(char[] category, char[] key) {
		this.document.addIndexEntry(category, key);
	}
	public void addInterfaceDeclaration(int modifiers, char[] packageName, char[] name, char[][] enclosingTypeNames, char[][] superinterfaces, char[][] typeParameterSignatures, boolean secondary) {
		addTypeDeclaration(modifiers, packageName, name, enclosingTypeNames, secondary);

		if (superinterfaces != null) {
			for (int i = 0, max = superinterfaces.length; i < max; i++) {
				char[] superinterface = erasure(superinterfaces[i]);
				addTypeReference(superinterface);
				addIndexEntry(
					SUPER_REF,
					SuperTypeReferencePattern.createIndexKey(
						modifiers, packageName, name, enclosingTypeNames, typeParameterSignatures, INTERFACE_SUFFIX, superinterface, INTERFACE_SUFFIX));
			}
		}
	}
	public void addMethodDeclaration(char[] methodName, char[][] parameterTypes, char[] returnType, char[][] exceptionTypes) {
		int argCount = parameterTypes == null ? 0 : parameterTypes.length;
		addIndexEntry(METHOD_DECL, MethodPattern.createIndexKey(methodName, argCount));

		if (parameterTypes != null) {
			for (int i = 0; i < argCount; i++)
				addTypeReference(parameterTypes[i]);
		}
		if (exceptionTypes != null)
			for (int i = 0, max = exceptionTypes.length; i < max; i++)
				addTypeReference(exceptionTypes[i]);
		if (returnType != null)
			addTypeReference(returnType);
	}
	public void addMethodReference(char[] methodName, int argCount) {
		addIndexEntry(METHOD_REF, MethodPattern.createIndexKey(methodName, argCount));
	}
	public void addNameReference(char[] name) {
		addIndexEntry(REF, name);
	}
	protected void addTypeDeclaration(int modifiers, char[] packageName, char[] name, char[][] enclosingTypeNames, boolean secondary) {
		char[] indexKey = TypeDeclarationPattern.createIndexKey(modifiers, name, packageName, enclosingTypeNames, secondary);
		if (secondary)
			JavaModelManager.getJavaModelManager().secondaryTypeAdding(
				this.document.getPath(),
				name == null ? CharOperation.NO_CHAR : name,
				packageName == null ? CharOperation.NO_CHAR : packageName);

		addIndexEntry(TYPE_DECL, indexKey);
	}
	public void addTypeReference(char[] typeName) {
		addNameReference(CharOperation.lastSegment(typeName, '.'));
	}
	public abstract void indexDocument();
}
