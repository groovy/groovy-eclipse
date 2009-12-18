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
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
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

}
