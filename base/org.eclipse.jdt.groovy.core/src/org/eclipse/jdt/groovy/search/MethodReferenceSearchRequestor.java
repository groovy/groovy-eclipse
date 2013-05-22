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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyTypeDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;
import org.codehaus.jdt.groovy.model.GroovyClassFileWorkingCopy;
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
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
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
	private static final int MAX_PARAMS = 10;
	private final SearchRequestor requestor;
	private final SearchParticipant participant;

	private final char[] name;
	private final String declaringQualifiedName;
	private final boolean findDeclarations;
	private final boolean findReferences;
	// currently, not used, but maybe they should be
	@SuppressWarnings("unused")
	private char[][] parameterQualifications;
	@SuppressWarnings("unused")
	private char[][] parameterSimpleNames;

	private final int declaredParameterCount;

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
		declaredParameterCount = pattern.parameterSimpleNames == null ? 0 : pattern.parameterSimpleNames.length;
	}

	public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
		boolean doCheck = false;
		boolean isDeclaration = false;
		boolean isConstructorCall = false; // FIXADE hmmm...not capturing constructor calls here.
		int start = 0;
		int end = 0;

		if (result.declaringType == null) {
			// GRECLIPSE-1180 probably a literal of some kind
			return VisitStatus.CONTINUE;
		}

		if (node instanceof ConstantExpression) {
			String cName = ((ConstantExpression) node).getText();
			if (cName != null && CharOperation.equals(name, cName.toCharArray())) {
				start = node.getStart();
				end = node.getEnd();
				doCheck = end > 0; // avoid synthetic references
			}
		} else if (node instanceof FieldExpression) {
			if (CharOperation.equals(name, ((FieldExpression) node).getFieldName().toCharArray())) {
				start = node.getStart();
				end = node.getEnd();
				doCheck = end > 0; // avoid synthetic references
			}
		} else if (node instanceof MethodNode) {
			MethodNode mnode = (MethodNode) node;
			if (CharOperation.equals(name, mnode.getName().toCharArray())) {
				isDeclaration = true;
				start = mnode.getNameStart();
				end = mnode.getNameEnd() + 1; // arrrgh...why +1?
				doCheck = true;
			}
		} else if (node instanceof VariableExpression) {
			VariableExpression vnode = (VariableExpression) node;
			if (CharOperation.equals(name, vnode.getName().toCharArray())) {
				start = vnode.getStart();
				end = start + vnode.getName().length();
				doCheck = true;
			}
		} else if (node instanceof StaticMethodCallExpression) {
			StaticMethodCallExpression smnode = (StaticMethodCallExpression) node;
			if (CharOperation.equals(name, smnode.getMethod().toCharArray())) {
				start = smnode.getStart();
				end = start + name.length;
				doCheck = true;
			}
		}

		// at this point, if doCheck is true, then we know that the method name matches
		if (doCheck && end > 0) {
			// don't want to double accept nodes. This could happen with field and object initializers can get pushed into multiple
			// constructors
			Position position = new Position(start, end - start);
			if (!acceptedPositions.contains(position)) {
				int numberOfParameters = findNumberOfParameters(node, result);
				boolean isCompleteMatch = nameAndArgsMatch(removeArray(result.declaringType), numberOfParameters);
				if (isCompleteMatch) {
					IJavaElement realElement = enclosingElement.getOpenable() instanceof GroovyClassFileWorkingCopy ? ((GroovyClassFileWorkingCopy) enclosingElement
							.getOpenable()).convertToBinary(enclosingElement) : enclosingElement;
					SearchMatch match = null;
					if (isDeclaration && findDeclarations) {
						match = new MethodDeclarationMatch(realElement, getAccuracy(result.confidence, isCompleteMatch), start, end
								- start, participant, realElement.getResource());
					} else if (!isDeclaration && findReferences) {
						match = new MethodReferenceMatch(realElement, getAccuracy(result.confidence, isCompleteMatch), start, end
								- start, isConstructorCall, false, false, false, participant, realElement.getResource());
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

	/**
	 * @param node
	 * @param result
	 * @return finds the number of parameters in the method reference/declaration currently being analyzed.
	 */
	private int findNumberOfParameters(ASTNode node, TypeLookupResult result) {
		return node instanceof MethodNode && ((MethodNode) node).getParameters() != null ? ((MethodNode) node).getParameters().length
				: Math.max(0, result.scope.getMethodCallNumberOfArguments());
	}

	private Map<ClassNode, Boolean> cachedDeclaringNameMatches = new HashMap<ClassNode, Boolean>();
	private Map<ClassNode, boolean[]> cachedParameterCounts = new HashMap<ClassNode, boolean[]>();

	/**
	 * Recursively checks the hierarchy for matching names
	 * 
	 * @param declaringType
	 * @return
	 */
	private boolean nameAndArgsMatch(ClassNode declaringType, int currentCallCount) {
		return matchOnName(declaringType) && matchOnNumberOfParameters(declaringType, currentCallCount);
	}

	private boolean matchOnName(ClassNode declaringType) {
		if (declaringType == null) {
			return false;
		}
		String declaringTypeName = declaringType.getName();
		if (// since local variables have a declaring type of object, we don't accidentally want to return them as a match
		(declaringTypeName.equals("java.lang.Object") && declaringType.getDeclaredMethods(String.valueOf(name)).size() == 0)) {
			return false;
		}
		if (declaringQualifiedName == null || declaringQualifiedName.equals("")) {
			// no type specified, accept all
			return true;
		}

		declaringTypeName = declaringTypeName.replace('$', '.');

		Boolean maybeMatch = cachedDeclaringNameMatches.get(declaringType);
		if (maybeMatch != null) {
			return maybeMatch;
		}

		if (declaringTypeName.equals(declaringQualifiedName)) {
			cachedDeclaringNameMatches.put(declaringType, true);

			// the name matches, now what about number of arguments?
			return true;
		} else {

			// check the supers
			maybeMatch = matchOnName(declaringType.getSuperClass());
			if (!maybeMatch) {
				for (ClassNode iface : declaringType.getInterfaces()) {
					maybeMatch = matchOnName(iface);
					if (maybeMatch) {
						break;
					}
				}
			}
			cachedDeclaringNameMatches.put(declaringType, maybeMatch);
			return maybeMatch;
		}
	}

	/**
	 * When matching method references and declarations, we can't actually match on parameter types. Instead, we match on the number
	 * of parameterrs and assume that it is slightly more preceise than just matching on name.
	 * 
	 * The heuristic that is used in this method is this:
	 * <ol>
	 * <li>The search pattern expects 'n' parameters
	 * <li>the current node has 'm' arguments.
	 * <li>if the m == n, then there is a precise match.
	 * <li>if not, look at all methods in current type with same name.
	 * <li>if there is a method in the current type with the same number of arguments, then assume the current node matches that
	 * other method, and there is no match.
	 * <li>If there are no existing methods with same number of parameters, then assume that current method call is an alternative
	 * way of calling the method and return a match
	 * </ol>
	 * 
	 * @param declaringType
	 * @param currentCallCount
	 * @return true if there is a precise match between number of arguments and numner of parameters. false if there exists a
	 *         different method with same number of arguments in current type, or true otherwise
	 * 
	 */
	private boolean matchOnNumberOfParameters(ClassNode declaringType, int currentCallCount) {
		boolean methodParamNumberMatch;
		if (currentCallCount == declaredParameterCount) {
			// precise match
			methodParamNumberMatch = true;
		} else {

			boolean[] foundParameterNumbers = cachedParameterCounts.get(declaringType);
			if (foundParameterNumbers == null) {
				foundParameterNumbers = new boolean[MAX_PARAMS + 1];
				gatherParameters(declaringType, foundParameterNumbers);
				cachedParameterCounts.put(declaringType, foundParameterNumbers);
			}
			// now, if we find a method that has the same number of parameters in the call,
			// then assume the call is for this target method (and therefore there is no match)
			methodParamNumberMatch = !foundParameterNumbers[Math.min(MAX_PARAMS, currentCallCount)];
		}
		return methodParamNumberMatch;
	}

	/**
	 * @param declaringType
	 * @param foundParameterNumbers
	 */
	private void gatherParameters(ClassNode declaringType, boolean[] foundParameterNumbers) {
		if (declaringType == null) {
			return;
		}
		declaringType = findWrappedNode(declaringType.redirect());
		List<MethodNode> methods = declaringType.getMethods(String.valueOf(name));
		for (MethodNode method : methods) {
			// GRECLIPSE-1233
			// ensure default parameters are ignored
			method = method.getOriginal();
			foundParameterNumbers[Math.min(method.getParameters().length, MAX_PARAMS)] = true;
		}

		gatherParameters(declaringType.getSuperClass(), foundParameterNumbers);
		for (ClassNode iface : declaringType.getInterfaces()) {
			gatherParameters(iface, foundParameterNumbers);
		}
	}

	/**
	 * Attempt to convert from a {@link JDTClassNode} to a {@link ClassNode} in order to check default parameters
	 * 
	 * @param declaringType
	 * @return
	 */
	private ClassNode findWrappedNode(ClassNode declaringType) {
		ClassNode wrappedNode = null;
		if (declaringType instanceof JDTClassNode) {
			ReferenceBinding binding = ((JDTClassNode) declaringType).getJdtBinding();
			if (binding instanceof SourceTypeBinding) {
				SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) binding;
				if (sourceTypeBinding.scope != null) {
					TypeDeclaration typeDeclaration = sourceTypeBinding.scope.referenceContext;
					if (typeDeclaration instanceof GroovyTypeDeclaration) {
						GroovyTypeDeclaration groovyTypeDeclaration = (GroovyTypeDeclaration) typeDeclaration;
						wrappedNode = groovyTypeDeclaration.getClassNode();
					}
				}
			}
		}
		return wrappedNode == null ? declaringType : wrappedNode;
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
