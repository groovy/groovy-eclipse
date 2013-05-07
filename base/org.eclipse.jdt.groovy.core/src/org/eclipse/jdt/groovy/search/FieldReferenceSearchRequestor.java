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
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.jdt.groovy.model.GroovyClassFileWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.FieldDeclarationMatch;
import org.eclipse.jdt.core.search.FieldReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.internal.core.search.matching.FieldPattern;
import org.eclipse.jdt.internal.core.search.matching.VariablePattern;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.Position;

/**
 * @author Andrew Eisenberg
 * @created Aug 31, 2009
 * 
 */
public class FieldReferenceSearchRequestor implements ITypeRequestor {
	private final SearchRequestor requestor;
	private final SearchParticipant participant;

	private final char[] name;
	private final String declaringQualifiedName;
	private final boolean readAccess;
	private final boolean writeAccess;
	private final boolean findDeclarations;
	private final boolean findReferences;

	private final Set<Position> acceptedPositions = new HashSet<Position>();

	@SuppressWarnings("nls")
	public FieldReferenceSearchRequestor(FieldPattern pattern, SearchRequestor requestor, SearchParticipant participant) {
		this.requestor = requestor;
		this.participant = participant;
		name = (char[]) ReflectionUtils.getPrivateField(VariablePattern.class, "name", pattern);
		char[] arr = (char[]) ReflectionUtils.getPrivateField(FieldPattern.class, "declaringSimpleName", pattern);
		String declaringSimpleName = arr == null ? "" : new String(arr);
		arr = (char[]) ReflectionUtils.getPrivateField(FieldPattern.class, "declaringQualification", pattern);
		String declaringQualification = ((arr == null || arr.length == 0) ? "" : (new String(arr) + "."));
		declaringQualifiedName = declaringQualification + declaringSimpleName;

		readAccess = ((Boolean) ReflectionUtils.getPrivateField(VariablePattern.class, "readAccess", pattern)).booleanValue();
		writeAccess = ((Boolean) ReflectionUtils.getPrivateField(VariablePattern.class, "writeAccess", pattern)).booleanValue();
		findDeclarations = ((Boolean) ReflectionUtils.getPrivateField(VariablePattern.class, "findDeclarations", pattern))
				.booleanValue();
		findReferences = ((Boolean) ReflectionUtils.getPrivateField(VariablePattern.class, "findReferences", pattern))
				.booleanValue();
	}

	@SuppressWarnings("nls")
	public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
		boolean doCheck = false;
		boolean isAssignment = false;
		boolean isDeclaration = false;
		int start = 0;
		int end = 0;

		// include method calls here because of closures
		if (node instanceof ConstantExpression) {
			String cName = ((ConstantExpression) node).getText();
			if (cName != null && CharOperation.equals(name, cName.toCharArray())) {
				doCheck = true;
				if (EqualityVisitor.checkForAssignment(node, result.enclosingAssignment)) {
					isAssignment = true;
				}
				start = node.getStart();
				end = node.getEnd();
			}
		} else if (node instanceof FieldExpression) {
			if (CharOperation.equals(name, ((FieldExpression) node).getFieldName().toCharArray())) {
				doCheck = true;
				if (EqualityVisitor.checkForAssignment(node, result.enclosingAssignment)) {
					isAssignment = true;
				}
				// fully qualified field expressions in static contexts will have an sloc of the entire qualified name
				end = node.getEnd();
				start = end - name.length;
			}
		} else if (node instanceof FieldNode) {
			FieldNode fnode = (FieldNode) node;
			if (CharOperation.equals(name, fnode.getName().toCharArray())) {
				doCheck = true;
				isDeclaration = true;
				// assume all fieldNodes are assignments. Not true if there is no initializer, but we can't know this at this point
				// since the initializer has already been moved to the <init>
				isAssignment = true;
				start = fnode.getNameStart();
				end = fnode.getNameEnd() + 1; // arrrgh...why +1?
			}
		} else if (node instanceof VariableExpression) {
			VariableExpression vnode = (VariableExpression) node;
			if (CharOperation.equals(name, vnode.getName().toCharArray())) {
				doCheck = true;
				if (EqualityVisitor.checkForAssignment(node, result.enclosingAssignment)) {
					isAssignment = true;
				}

				start = vnode.getStart();
				end = start + vnode.getName().length();
			}
		}

		if (doCheck && end > 0) {
			// don't want to double accept nodes. This could happen with field and object initializers can get pushed into multiple
			// constructors
			Position position = new Position(start, end - start);
			if (!acceptedPositions.contains(position)) {
				boolean isCompleteMatch = qualifiedNameMatches(removeArray(result.declaringType));
				// GRECLIPSE-540 still unresolved is that all field and variable references are considered reads. We don't know
				// about writes
				if (isCompleteMatch
						&& ((isAssignment && writeAccess) || (!isAssignment && readAccess) || (isDeclaration && findDeclarations))) {
					SearchMatch match = null;

					// must translate from synthetic source to binary if necessary
					IJavaElement realElement = enclosingElement.getOpenable() instanceof GroovyClassFileWorkingCopy ? ((GroovyClassFileWorkingCopy) enclosingElement
							.getOpenable()).convertToBinary(enclosingElement) : enclosingElement;
					if (isDeclaration && findDeclarations) {
						match = new FieldDeclarationMatch(realElement, getAccuracy(result.confidence, isCompleteMatch), start, end
								- start, participant, realElement.getResource());
					} else if (!isDeclaration && findReferences) {
						match = new FieldReferenceMatch(realElement, getAccuracy(result.confidence, isCompleteMatch), start, end
								- start, !isAssignment, isAssignment, false, participant, realElement.getResource());
					}
					if (match != null) {
						try {
							requestor.acceptSearchMatch(match);
							acceptedPositions.add(position);
						} catch (CoreException e) {
							Util.log(
									e,
									"Error reporting search match inside of " + realElement + " in resource "
											+ realElement.getResource());
						}
					}
				}
			}
		}
		return VisitStatus.CONTINUE;
	}

	// recursively check the hierarchy
	private boolean qualifiedNameMatches(ClassNode declaringType) {
		if (declaringType == null) {
			// no declaring type---probably a variable declaration
			return false;
		} else if (declaringQualifiedName == null || declaringQualifiedName.equals("")) { //$NON-NLS-1$
			// no type specified, accept all
			return true;
		} else if (declaringType.getName().equals(declaringQualifiedName)) {
			return true;
		} else {
			return false;
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
		return (declaration != null && declaration.getComponentType() != null) ? removeArray(declaration.getComponentType())
				: declaration;
	}
}
