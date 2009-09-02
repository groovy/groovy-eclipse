/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.jdt.groovy.search;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;

/**
 * @author Andrew Eisenberg
 * @created Aug 29, 2009
 *          <p>
 *          Instances of this interface will attempt to determine the type of a Groovy ASTNode that is passed in.
 */
public interface ITypeLookup {

	/**
	 * Specifies if this type look up should look for the return type of the expression or the declaring type of the expression.
	 */
	public static enum LookupType {
		RETURN_TYPE, DECLARING_TYPE
	}

	void setLookupType(LookupType lookupType);

	/**
	 * Determine the type for an expression node.
	 * 
	 * @param node the AST Node to determine the type for
	 * @param enclosingDeclarationNode TODO
	 * @return the type for the node and confidence in that type, or null if cannot determine
	 */
	TypeLookupResult lookupType(Expression node, ASTNode enclosingDeclarationNode);

	/**
	 * Determine the type for a field node.
	 * 
	 * @param node the AST Node to determine the type for
	 * @return the type for the node and confidence in that type, or null if cannot determine
	 */
	TypeLookupResult lookupType(FieldNode node);

	/**
	 * Determine the type for a method node.
	 * 
	 * @param node the AST Node to determine the type for
	 * @return the type for the node and confidence in that type, or null if cannot determine
	 */
	TypeLookupResult lookupType(MethodNode node);

	/**
	 * Determine the type for an annotation node.
	 * 
	 * @param node the AST Node to determine the type for
	 * @return the type for the node and confidence in that type, or null if cannot determine
	 */
	TypeLookupResult lookupType(AnnotationNode node);

	/**
	 * Determine the type for an expression node.
	 * 
	 * @param node the AST Node to determine the type for
	 * @return the type for the node and confidence in that type, or null if cannot determine
	 */
	TypeLookupResult lookupType(ImportNode node);

	/**
	 * Determine the type for a class node. Implementors should return null unless the type returned should be different from the
	 * type passed in.
	 * 
	 * @param node the AST Node to determine the type for
	 * @return the type for the node and confidence in that type, or null if cannot determine
	 */
	TypeLookupResult lookupType(ClassNode node);
}