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
package org.codehaus.groovy.eclipse.core.util;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.core.model.IDocumentFacade;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

public class ASTUtils {

	/**
	 * Get a region starting at the nodes lineNumber/columnNumber and ending at
	 * the nodes lastLineNumber/lastColumnNumber.
	 * 
	 * @param facade
	 * @param node
	 * @return The region, or null if one could not be created for the
	 *         node/editor.
	 */
	public static IRegion getRegion(IDocumentFacade facade, ASTNode node) {
		try {
			int offset0 = facade.getOffset(node.getLineNumber() - 1, node
					.getColumnNumber() - 1);
			int offset1 = facade.getOffset(node.getLastLineNumber() - 1, node
					.getLastColumnNumber() - 1);
			return new Region(offset0, offset1 - offset0);
		} catch (BadLocationException e) {
		}
		return null;
	}

	/**
	 * Get a region with offset starting at the first node and length is the
	 * distance from the first node to the second node.
	 * 
	 * @param facade
	 * @param first
	 * @param next
	 *            The second ASTNode, which may be null, in which case the first
	 *            node line/column info is used to calculate the region.
	 * @return The region from the first ASTNode to before the next ASTNode.
	 */
	public static IRegion getRegion(IDocumentFacade facade, ASTNode first,
			ASTNode next) {
		if (next == null) {
			return getRegion(facade, first);
		}

		try {
			int offset0 = facade.getOffset(first.getLineNumber() - 1, first
					.getColumnNumber() - 1);
			int offset1 = facade.getOffset(next.getLineNumber() - 1, next
					.getColumnNumber() - 1);
			return new Region(offset0, offset1 - offset0 - 1);
		} catch (BadLocationException e) {
		}
		return null;
	}

	/**
	 * Get a region starting at the specified nodes lineNumber/columnNumber with
	 * the given length.
	 * 
	 * @param facade
	 * @param node
	 * @param length
	 * @return The region with the offset set to the line/column of the node,
	 *         and the length as specified.
	 */
	public static IRegion getRegion(IDocumentFacade facade, ASTNode node,
			int length) {
		try {
			int offset = facade.getOffset(node.getLineNumber() - 1, node
					.getColumnNumber() - 1);
			return new Region(offset, length);
		} catch (BadLocationException e) {
		}
		return null;
	}

	/**
	 * Creates a string suitable for display. This method should be used
	 * wherever possible for consistent results.
	 * 
	 * @param node
	 * @return The display string.
	 */
	public static String createDisplayString(ClassNode node) {
		return node.getName();
	}

	/**
	 * Creates a string suitable for display. This method should be used
	 * wherever possible for consistent results.
	 * 
	 * @param node
	 * @return The display string.
	 */
	public static String createDisplayString(MethodNode node) {
		StringBuffer sb = new StringBuffer();
		sb.append(node.getName());
		sb.append(createParameterString(node.getParameters()));
		sb.append(" ").append(node.getReturnType().getName());
		sb.append(" ").append(node.getDeclaringClass().getName());
		return sb.toString();
	}

	/**
	 * Creates a string suitable for display. This method should be used
	 * wherever possible for consistent results.
	 * 
	 * @param node
	 * @return The display string.
	 */
	public static String createDisplayString(FieldNode node) {
		StringBuffer sb = new StringBuffer();
		sb.append(node.getName()).append(" ").append(node.getType().getName());
		return sb.toString();
	}

	/**
	 * Creates a string suitable for display. This method should be used
	 * wherever possible for consistent results.
	 * 
	 * @param node
	 * @return The display string.
	 */
	public static String createDisplayString(VariableExpression expr) {
		StringBuffer sb = new StringBuffer();
		sb.append(expr.getName()).append(" ").append(expr.getType().getName());
		return sb.toString();
	}

	public static String createParameterString(Parameter[] parameters) {
		return createParameterString(parameters, ",", true);
	}

	/**
	 * @param parameters
	 * @param delimiter
	 *            The delimiter, normally ','
	 * @param parenthesis
	 *            If true, surround the string with ( )
	 * @return A parameter string.
	 */
	public static String createParameterString(Parameter[] parameters,
			String delimiter, boolean parenthesis) {
		StringBuffer sb = new StringBuffer();
		if (parenthesis)
			sb.append("(");
		for (int i = 0; i < parameters.length; ++i) {
			sb.append(parameters[i].getType().getName());
			sb.append(" ");
			sb.append(parameters[i].getName());
			sb.append(delimiter);
		}
		if (parameters.length > 0)
			sb.setLength(sb.length() - delimiter.length());
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Check if some reference line/number coordinate is inside a span.
	 * 
	 * @param line0
	 * @param line1
	 * @param col0
	 * @param col1
	 * @param refLine
	 * @param refCol
	 * @return True if line0..line1 and col0..<col1 are surrounding the
	 *         reference line/col.
	 */
	public static boolean isInsideSpan(int line0, int col0, int line1,
			int col1, int refLine, int refCol) {
		if ((line0 == refLine && line1 == refLine && col0 <= refCol && refCol < col1)
				|| (line0 == refLine && refLine < line1 && col0 <= refCol)
				|| (line0 < refLine && refLine < line1)
				|| (line1 == refLine && refCol < col1)) {
			return true;
		}
		return false;
	}

	/**
	 * Check if one node is inside another.
	 * 
	 * @param candidate
	 *            The candidate that might surround the reference.
	 * @param reference
	 *            The reference that might be inside the candidate.
	 * @return True if surrounding, else false.
	 */
	public static boolean isInsideNode(ASTNode candidate, ASTNode reference) {
		return isInsideNode(candidate, reference.getLineNumber(), reference
				.getColumnNumber());
	}

	/**
	 * @see #isInsideNode(ASTNode, ASTNode)
	 */
	public static boolean isInsideNode(ASTNode candidate, int line, int col) {
		int cline0 = candidate.getLineNumber();
		int cline1 = candidate.getLastLineNumber();
		int ccol0 = candidate.getColumnNumber();
		int ccol1 = candidate.getLastColumnNumber();

		return isInsideSpan(cline0, ccol0, cline1, ccol1, line, col);
	}
	
	public static boolean isInsideNode(ASTNode candidate, int offset) {
		return candidate.getStart() <= offset && candidate.getEnd() >= offset;
	}

	/**
	 * Check to see if a reference node is between two other nodes.
	 * 
	 * @param candidate1
	 * @param candidate2
	 * @param reference
	 * @return True if in between, else false.
	 */
	public static boolean isBetweenNodes(ASTNode candidate1,
			ASTNode candidate2, ASTNode reference) {
		int line1 = candidate1.getLineNumber();
		int col1 = candidate1.getColumnNumber();
		int line2 = candidate2.getLineNumber();
		int col2 = candidate2.getColumnNumber();
		int line = reference.getLineNumber();
		int col = reference.getColumnNumber();
		return isInsideSpan(line1, col1, line2, col2, line, col);
	}
}