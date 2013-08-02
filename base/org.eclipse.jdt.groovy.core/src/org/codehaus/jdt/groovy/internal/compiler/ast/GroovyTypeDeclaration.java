/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.parser.Parser;

@SuppressWarnings("restriction")
public class GroovyTypeDeclaration extends TypeDeclaration {

	public List<PropertyNode> properties;

	// The Groovy ClassNode that gave rise to this type declaration
	private ClassNode classNode;

	public GroovyTypeDeclaration(CompilationResult compilationResult, ClassNode classNode) {
		super(compilationResult);
		this.classNode = classNode;
	}

	public ClassNode getClassNode() {
		return classNode;
	}

	// FIXASC is this always what we want to do - are there any other implications?
	/*
	 * Prevent groovy types from having their methods re-parsed
	 */
	@Override
	public void parseMethods(Parser parser, CompilationUnitDeclaration unit) {
		// noop
	}

	// FIXASC (groovychange)
	@Override
	public boolean isScannerUsableOnThisDeclaration() {
		return false;
	}

	// FIXASC (groovychange) end

	/**
	 * Fixes the super types of anonymous inner classes These kinds of classes are always constructed so that they extend the super
	 * type, even if the super type is an interface. This is because during parse time we don't know if the super type is a class or
	 * interface, se we need to wait until after the resolve phase to fix this.
	 * 
	 * @param groovyCompilationUnitScope
	 */
	public void fixAnonymousTypeBinding(GroovyCompilationUnitScope groovyCompilationUnitScope) {
		if ((this.bits & ASTNode.IsAnonymousType) != 0) {
			if (classNode.getInterfaces() != null && classNode.getInterfaces().length == 1
					&& classNode.getSuperClass().getName().equals("java.lang.Object")) {

				this.superInterfaces = new TypeReference[] { this.superclass };
				this.binding.superInterfaces = new ReferenceBinding[] { (ReferenceBinding) this.superclass.resolvedType };
				this.superclass = null;

			}
		}
		if (anonymousTypes != null) {
			fixAnonymousTypeDeclarations(anonymousTypes, groovyCompilationUnitScope);
		}
	}

	private void fixAnonymousTypeDeclarations(GroovyTypeDeclaration[] types, Scope parentScope) {
		for (GroovyTypeDeclaration type : types) {
			GroovyClassScope anonScope = new GroovyClassScope(parentScope, type);
			type.scope = anonScope;
			if (type.anonymousTypes != null) {
				fixAnonymousTypeDeclarations(type.anonymousTypes, anonScope);
			}
		}
	}

	/**
	 * Anonymous types that are declared in this type's methods
	 */
	private GroovyTypeDeclaration[] anonymousTypes = null;

	/**
	 * If this type is anonymous, points to the enclosing method
	 */
	public AbstractMethodDeclaration enclosingMethod;

	public void addAnonymousType(GroovyTypeDeclaration anonymousType) {
		if (anonymousTypes == null) {
			anonymousTypes = new GroovyTypeDeclaration[] { anonymousType };
		} else {
			GroovyTypeDeclaration[] newTypes = new GroovyTypeDeclaration[anonymousTypes.length + 1];
			System.arraycopy(anonymousTypes, 0, newTypes, 0, anonymousTypes.length);
			newTypes[anonymousTypes.length] = anonymousType;
			anonymousTypes = newTypes;
		}
	}

	public GroovyTypeDeclaration[] getAnonymousTypes() {
		return anonymousTypes;
	}
}
