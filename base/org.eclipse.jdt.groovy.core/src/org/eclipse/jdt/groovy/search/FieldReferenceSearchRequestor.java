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
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.internal.core.search.matching.FieldPattern;
import org.eclipse.jdt.internal.core.search.matching.VariablePattern;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @author Andrew Eisenberg
 * @created Aug 31, 2009
 * 
 */
public class FieldReferenceSearchRequestor implements ITypeRequestor {
	private final SearchRequestor requestor;
	private final SearchParticipant participant;

	private final char[] name;
	private final String declaringSimpleName;
	private final String declaringQualification;
	private final boolean readAccess;
	private final boolean writeAccess;
	private final boolean findDeclarations;
	private final boolean findReferences;

	public FieldReferenceSearchRequestor(FieldPattern pattern, SearchRequestor requestor, SearchParticipant participant) {
		this.requestor = requestor;
		this.participant = participant;
		name = (char[]) ReflectionUtils.getPrivateField(VariablePattern.class, "name", pattern);
		char[] arr = (char[]) ReflectionUtils.getPrivateField(FieldPattern.class, "declaringSimpleName", pattern);
		declaringSimpleName = arr == null ? "" : new String(arr);
		arr = (char[]) ReflectionUtils.getPrivateField(FieldPattern.class, "declaringQualification", pattern);
		declaringQualification = ((arr == null || arr.length == 0) ? "" : (new String(arr) + "."));

		readAccess = ((Boolean) ReflectionUtils.getPrivateField(VariablePattern.class, "readAccess", pattern)).booleanValue();
		writeAccess = ((Boolean) ReflectionUtils.getPrivateField(VariablePattern.class, "writeAccess", pattern)).booleanValue();
		findDeclarations = ((Boolean) ReflectionUtils.getPrivateField(VariablePattern.class, "findDeclarations", pattern))
				.booleanValue();
		findReferences = ((Boolean) ReflectionUtils.getPrivateField(VariablePattern.class, "findReferences", pattern))
				.booleanValue();
	}

	public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
		boolean doCheck = false;
		boolean isAssignment = false;
		boolean isDeclaration = false;
		int start = 0;
		int end = 0;

		// include method calls here because of closures
		if (node instanceof MethodCallExpression) {
			String mName = ((MethodCallExpression) node).getMethodAsString();
			if (mName != null && CharOperation.equals(name, mName.toCharArray())) {
				doCheck = true;
				start = node.getStart();
				end = node.getEnd();
			}
		} else if (node instanceof FieldExpression) {
			if (CharOperation.equals(name, ((FieldExpression) node).getFieldName().toCharArray())) {
				doCheck = true;
				start = node.getStart();
				end = node.getEnd();
			}
		} else if (node instanceof PropertyExpression) {
			Expression prop = ((PropertyExpression) node).getProperty();
			if (CharOperation.equals(name, prop.getText().toCharArray())) {
				doCheck = true;
				start = prop.getStart();
				end = prop.getEnd();
			}
		} else if (node instanceof BinaryExpression) {
			if (((BinaryExpression) node).getOperation().getText().equals("=")) { //$NON-NLS-1$
				Expression expr = ((BinaryExpression) node).getLeftExpression();
				if (expr instanceof FieldExpression) {
					FieldExpression fexpr = (FieldExpression) expr;
					if (CharOperation.equals(name, fexpr.getFieldName().toCharArray())) {
						doCheck = true;
						isAssignment = true;
						start = fexpr.getStart();
						end = fexpr.getEnd();
					}
				}
			}
		} else if (node instanceof FieldNode) {
			FieldNode fnode = (FieldNode) node;
			if (CharOperation.equals(name, fnode.getName().toCharArray())) {
				doCheck = true;
				isAssignment = true;
				isDeclaration = true;
				start = fnode.getNameStart();
				end = fnode.getNameEnd() + 1; // arrrgh...why +1?
			}
		} else if (node instanceof VariableExpression) {
			VariableExpression vnode = (VariableExpression) node;
			if (CharOperation.equals(name, vnode.getName().toCharArray())) {
				// FIXADE what about isAssignment???
				doCheck = true;
				start = vnode.getStart();
				end = start + vnode.getName().length();
			}
		}

		if (doCheck) {
			// FIXADE M2 really, must check the hierarchy, but this is good enough for now.
			boolean isCompleteMatch = qualifiedNameMatches(result.getFullyQualifiedName());
			if (isCompleteMatch
					&& ((isAssignment && writeAccess) || (!isAssignment && readAccess) || (isDeclaration && findDeclarations) || (!isDeclaration && findReferences))) {
				SearchMatch match = new SearchMatch(enclosingElement, getAccuracy(result.confidence, isCompleteMatch), start, end
						- start, participant, enclosingElement.getResource());
				try {
					requestor.acceptSearchMatch(match);
					// don't search any further down this branch
					// I guess it is possible that there are further matches, but for the sake of efficiency, let's ignore them.
					return VisitStatus.CANCEL_BRANCH;
				} catch (CoreException e) {
					Util.log(e, "Error reporting search match inside of " + enclosingElement + " in resource "
							+ enclosingElement.getResource());
				}
			}
		}
		return VisitStatus.CONTINUE;
	}

	private boolean qualifiedNameMatches(String qualifiedName) {
		String newName = qualifiedName;
		// don't do * matching or camel case matching yet
		if (qualifiedName.equals(declaringQualification + declaringSimpleName)) {
			return true;
		}
		return false;
	}

	private int getAccuracy(TypeConfidence confidence, boolean isCompleteMatch) {
		if (!isCompleteMatch) {
			return SearchMatch.A_INACCURATE;
		}
		switch (confidence) {
			case EXACT:
				return SearchMatch.A_ACCURATE;
			default:
				return SearchMatch.A_INACCURATE;
		}
	}

}
