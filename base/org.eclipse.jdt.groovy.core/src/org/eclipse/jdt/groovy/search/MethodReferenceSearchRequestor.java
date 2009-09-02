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
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.internal.core.search.matching.MethodPattern;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @author Andrew Eisenberg
 * @created Aug 31, 2009
 * 
 */
public class MethodReferenceSearchRequestor implements ITypeRequestor {
	private final SearchRequestor requestor;
	private final SearchParticipant participant;

	private final char[] name;
	private final String declaringSimpleName;
	private final String declaringQualification;
	private final boolean findDeclarations;
	private final boolean findReferences;
	private char[][] parameterQualifications;
	private char[][] parameterSimpleNames;

	public MethodReferenceSearchRequestor(MethodPattern pattern, SearchRequestor requestor, SearchParticipant participant) {
		this.requestor = requestor;
		this.participant = participant;
		name = (char[]) ReflectionUtils.getPrivateField(MethodPattern.class, "selector", pattern);
		char[] arr = (char[]) ReflectionUtils.getPrivateField(MethodPattern.class, "declaringSimpleName", pattern);
		declaringSimpleName = arr == null ? "" : new String(arr);
		arr = (char[]) ReflectionUtils.getPrivateField(MethodPattern.class, "declaringQualification", pattern);
		declaringQualification = ((arr == null || arr.length == 0) ? "" : (new String(arr) + "."));
		findDeclarations = ((Boolean) ReflectionUtils.getPrivateField(MethodPattern.class, "findDeclarations", pattern))
				.booleanValue();
		findReferences = ((Boolean) ReflectionUtils.getPrivateField(MethodPattern.class, "findReferences", pattern)).booleanValue();

		parameterQualifications = pattern.parameterQualifications;
		parameterSimpleNames = pattern.parameterSimpleNames;
	}

	public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
		boolean doCheck = false;
		boolean isDeclaration = false;
		int start = 0;
		int end = 0;

		if (node instanceof MethodCallExpression) {
			MethodCallExpression expr = (MethodCallExpression) node;
			String mName = expr.getMethodAsString();
			if (mName != null && CharOperation.equals(name, mName.toCharArray())) {
				doCheck = true;
				start = expr.getMethod().getStart();
				end = expr.getMethod().getEnd();
			}
		} else if (node instanceof StaticMethodCallExpression) {
			String mName = ((StaticMethodCallExpression) node).getMethod();
			if (mName != null && CharOperation.equals(name, mName.toCharArray())) {
				doCheck = true;
				start = node.getStart();
				end = node.getEnd();
			}
		} else if (node instanceof MethodPointerExpression) {
			Expression expr = ((MethodPointerExpression) node).getMethodName();
			if (expr.getText() != null && CharOperation.equals(name, expr.getText().toCharArray())) {
				doCheck = true;
				start = expr.getStart();
				end = expr.getEnd();
			}
		} else if (node instanceof MethodNode) {
			MethodNode fnode = (MethodNode) node;
			if (CharOperation.equals(name, fnode.getName().toCharArray())) {
				doCheck = true;
				isDeclaration = true;
				start = fnode.getNameStart();
				end = fnode.getNameEnd() + 1; // arrrgh...why +1?
			}
		}

		if (doCheck) {
			// FIXADE M2 really, must check the hierarchy, but this is good enough for now.
			// FIXADE M2 ignore checking parameters for now
			boolean isCompleteMatch = qualifiedNameMatches(result.getFullyQualifiedName());
			if ((isDeclaration && findDeclarations) || (!isDeclaration && findReferences)) {
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
