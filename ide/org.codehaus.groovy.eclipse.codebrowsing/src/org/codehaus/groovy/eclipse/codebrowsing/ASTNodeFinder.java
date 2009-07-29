/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Edward Povazan   - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.codebrowsing;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codebrowsing.astfinders.ASTNodeFoundException;
import org.codehaus.groovy.eclipse.codebrowsing.astfinders.FindASTNode;
import org.codehaus.groovy.eclipse.codebrowsing.astfinders.FindSurroundingClosure;
import org.codehaus.groovy.eclipse.codebrowsing.astfinders.FindSurroundingMethod;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;

/**
 * Utility class to find different AST nodes.
 * 
 * @author emp
 */
public class ASTNodeFinder {
	/**
	 * Given an identifier and its coordinates, find its ASTNode.
	 * @param moduleNode
	 * @param identifier
	 * @param line
	 * @param column
	 * @return The search result, or null if no ASTNode was found.
	 */
	public static ASTSearchResult findASTNode(ModuleNode moduleNode,
			String identifier, IRegion region, IFile file) {
		try {
			new FindASTNode().doFind(moduleNode, identifier, region, file);
		} catch (ASTNodeFoundException e) {
			return new ASTSearchResult(e);
		}
		return null;
	}

	/**
	 * Given a node, find the surrounding ClosureExpression if there is one.
	 * @param moduleNode
	 * @param reference
	 * @return The search result, or null if no ASTNode was found.
	 */
	public static ASTSearchResult findSurroundingClosure(
			ModuleNode moduleNode, ASTNode reference) {
		try {
			new FindSurroundingClosure().doFind(moduleNode, reference
					.getLineNumber(), reference.getColumnNumber());
		} catch (ASTNodeFoundException e) {
			return new ASTSearchResult(e);
		}
		return null;
	}

	/**
	 * Given a node, find the surrounding MethodNode if there is one.
	 * @param moduleNode
	 * @param reference
	 * @return The search result, or null if no ASTNode was found.
	 */
	public static ASTSearchResult findSurroundingMethod(
			ModuleNode moduleNode, ASTNode reference) {
		try {
			new FindSurroundingMethod().doFind(moduleNode, reference
					.getLineNumber(), reference.getColumnNumber());
		} catch (ASTNodeFoundException e) {
			return new ASTSearchResult(e);
		}
		return null;
	}
}