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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class TypeDeclarationLocator extends PatternLocator {

protected TypeDeclarationPattern pattern; // can be a QualifiedTypeDeclarationPattern

public TypeDeclarationLocator(TypeDeclarationPattern pattern) {
	super(pattern);

	this.pattern = pattern;
}
//public int match(ASTNode node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(ConstructorDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(Expression node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(FieldDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(MethodDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(MessageSend node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(Reference node, MatchingNodeSet nodeSet) - SKIP IT
public int match(TypeDeclaration node, MatchingNodeSet nodeSet) {
	if (this.pattern.simpleName == null || matchesName(this.pattern.simpleName, node.name))
		return nodeSet.addMatch(node, this.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);

	return IMPOSSIBLE_MATCH;
}
//public int match(TypeReference node, MatchingNodeSet nodeSet) - SKIP IT

public int resolveLevel(ASTNode node) {
	if (!(node instanceof TypeDeclaration)) return IMPOSSIBLE_MATCH;

	return resolveLevel(((TypeDeclaration) node).binding);
}
public int resolveLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof TypeBinding)) return IMPOSSIBLE_MATCH;

	TypeBinding type = (TypeBinding) binding;

	switch (this.pattern.typeSuffix) {
		case CLASS_SUFFIX:
			if (!type.isClass()) return IMPOSSIBLE_MATCH;
			break;
		case CLASS_AND_INTERFACE_SUFFIX:
			if (!(type.isClass() || (type.isInterface() && !type.isAnnotationType()))) return IMPOSSIBLE_MATCH;
			break;
		case CLASS_AND_ENUM_SUFFIX:
			if (!(type.isClass() || type.isEnum())) return IMPOSSIBLE_MATCH;
			break;
		case INTERFACE_SUFFIX:
			if (!type.isInterface() || type.isAnnotationType()) return IMPOSSIBLE_MATCH;
			break;
		case INTERFACE_AND_ANNOTATION_SUFFIX:
			if (!(type.isInterface() || type.isAnnotationType())) return IMPOSSIBLE_MATCH;
			break;
		case ENUM_SUFFIX:
			if (!type.isEnum()) return IMPOSSIBLE_MATCH;
			break;
		case ANNOTATION_TYPE_SUFFIX:
			if (!type.isAnnotationType()) return IMPOSSIBLE_MATCH;
			break;
		case TYPE_SUFFIX : // nothing
	}

	// fully qualified name
	if (this.pattern instanceof QualifiedTypeDeclarationPattern) {
		QualifiedTypeDeclarationPattern qualifiedPattern = (QualifiedTypeDeclarationPattern) this.pattern;
		return resolveLevelForType(qualifiedPattern.simpleName, qualifiedPattern.qualification, type);
	} else {
		char[] enclosingTypeName = this.pattern.enclosingTypeNames == null ? null : CharOperation.concatWith(this.pattern.enclosingTypeNames, '.');
		return resolveLevelForType(this.pattern.simpleName, this.pattern.pkg, enclosingTypeName, type);
	}
}
/**
 * Returns whether the given type binding matches the given simple name pattern
 * qualification pattern and enclosing type name pattern.
 */
protected int resolveLevelForType(char[] simpleNamePattern, char[] qualificationPattern, char[] enclosingNamePattern, TypeBinding type) {
	if (enclosingNamePattern == null)
		return resolveLevelForType(simpleNamePattern, qualificationPattern, type);
	if (qualificationPattern == null)
		return resolveLevelForType(simpleNamePattern, enclosingNamePattern, type);

	// case of an import reference while searching for ALL_OCCURENCES of a type (see bug 37166)
	if (type instanceof ProblemReferenceBinding) return IMPOSSIBLE_MATCH;

	// pattern was created from a Java element: qualification is the package name.
	char[] fullQualificationPattern = CharOperation.concat(qualificationPattern, enclosingNamePattern, '.');
	if (CharOperation.equals(this.pattern.pkg, CharOperation.concatWith(type.getPackage().compoundName, '.')))
		return resolveLevelForType(simpleNamePattern, fullQualificationPattern, type);
	return IMPOSSIBLE_MATCH;
}
public String toString() {
	return "Locator for " + this.pattern.toString(); //$NON-NLS-1$
}
}
