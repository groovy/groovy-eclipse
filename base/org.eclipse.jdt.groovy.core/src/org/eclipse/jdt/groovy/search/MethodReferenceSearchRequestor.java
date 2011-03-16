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

import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.MethodDeclarationMatch;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.internal.core.search.matching.MethodPattern;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.Position;

/**
 * @author Andrew Eisenberg
 * @created Aug 31, 2009
 * 
 */
@SuppressWarnings("nls")
public class MethodReferenceSearchRequestor implements ITypeRequestor {
	private final SearchRequestor requestor;
	private final SearchParticipant participant;

	private final char[] name;
	private final String declaringQualifiedName;
	private final boolean findDeclarations;
	private final boolean findReferences;
	private char[][] parameterQualifications;
	private char[][] parameterSimpleNames;

	private final Set<Position> acceptedPositions = new HashSet<Position>();

	public MethodReferenceSearchRequestor(MethodPattern pattern, SearchRequestor requestor, SearchParticipant participant) {
		this.requestor = requestor;
		this.participant = participant;
		name = (char[]) ReflectionUtils.getPrivateField(MethodPattern.class, "selector", pattern);
		char[] arr = (char[]) ReflectionUtils.getPrivateField(MethodPattern.class, "declaringSimpleName", pattern);
		String declaringSimpleName = arr == null ? "" : new String(arr);
		arr = (char[]) ReflectionUtils.getPrivateField(MethodPattern.class, "declaringQualification", pattern);
		String declaringQualification = ((arr == null || arr.length == 0) ? "" : (new String(arr) + "."));
		declaringQualifiedName = declaringQualification + declaringSimpleName;
		findDeclarations = ((Boolean) ReflectionUtils.getPrivateField(MethodPattern.class, "findDeclarations", pattern))
				.booleanValue();
		findReferences = ((Boolean) ReflectionUtils.getPrivateField(MethodPattern.class, "findReferences", pattern)).booleanValue();

		parameterQualifications = pattern.parameterQualifications;
		parameterSimpleNames = pattern.parameterSimpleNames;
	}

	public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
		boolean doCheck = false;
		boolean isDeclaration = false;
		boolean isConstructorCall = false; // FIXADE hmmm...not capturing constructor calls here.
		int start = 0;
		int end = 0;

		// include method calls here because of closures
		if (node instanceof ConstantExpression) {
			String cName = ((ConstantExpression) node).getText();
			if (cName != null && CharOperation.equals(name, cName.toCharArray())) {
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
		} else if (node instanceof MethodNode) {
			MethodNode mnode = (MethodNode) node;
			if (CharOperation.equals(name, mnode.getName().toCharArray())) {
				doCheck = true;
				isDeclaration = true;
				start = mnode.getNameStart();
				end = mnode.getNameEnd() + 1; // arrrgh...why +1?
			}
		} else if (node instanceof VariableExpression) {
			VariableExpression vnode = (VariableExpression) node;
			if (CharOperation.equals(name, vnode.getName().toCharArray())) {
				doCheck = true;
				start = vnode.getStart();
				end = start + vnode.getName().length();
			}
		} else if (node instanceof StaticMethodCallExpression) {
			StaticMethodCallExpression smnode = (StaticMethodCallExpression) node;
			if (CharOperation.equals(name, smnode.getMethod().toCharArray())) {
				doCheck = true;
				start = smnode.getStart();
				end = start + name.length;
			}
		}

		if (doCheck) {
			// don't want to double accept nodes. This could happen with field and object initializers can get pushed into multiple
			// constructors
			Position position = new Position(start, end - start);
			if (!acceptedPositions.contains(position)) {
				boolean isCompleteMatch = qualifiedNameMatches(removeArray(result.declaringType));
				if (isCompleteMatch) {
					SearchMatch match = null;
					if (isDeclaration && findDeclarations) {
						match = new MethodDeclarationMatch(enclosingElement, getAccuracy(result.confidence, isCompleteMatch),
								start, end - start, participant, enclosingElement.getResource());
					} else if (!isDeclaration && findReferences) {
						match = new MethodReferenceMatch(enclosingElement, getAccuracy(result.confidence, isCompleteMatch), start,
								end - start, isConstructorCall, false, false, false, participant, enclosingElement.getResource());
					}
					if (match != null) {
						try {
							requestor.acceptSearchMatch(match);
							acceptedPositions.add(position);
						} catch (CoreException e) {
							Util.log(e, "Error reporting search match inside of " + enclosingElement + " in resource "
									+ enclosingElement.getResource());
						}
					}
				}
			}
		}
		return VisitStatus.CONTINUE;
	}

	// recursively check the hierarchy
	private boolean qualifiedNameMatches(ClassNode declaringType) {
		if (declaringQualifiedName == null || declaringQualifiedName.equals("")) {
			// no type specified, accept all
			return true;
		}
		if (declaringType == null) {
			return false;
		} else if (declaringType.getName().equals(declaringQualifiedName)) {
			return true;
		} else {
			return qualifiedNameMatches(declaringType.getSuperClass());
		}
	}

	private int getAccuracy(TypeConfidence confidence, boolean isCompleteMatch) {
		if (shouldAlwaysBeAccurate()) {
			return SearchMatch.A_ACCURATE;
		}
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

	/**
	 * check to see if this requestor has something to do with refactoring, if so, we always want an accurate match otherwise we get
	 * complaints in the refactoring wizard of "possible matches"
	 * 
	 * @return
	 */
	private boolean shouldAlwaysBeAccurate() {
		return requestor.getClass().getPackage().getName().indexOf("refactoring") != -1;
	}

	/**
	 * @param declaration
	 * @return
	 */
	private ClassNode removeArray(ClassNode declaration) {
		return declaration.getComponentType() != null ? removeArray(declaration.getComponentType()) : declaration;
	}
}
