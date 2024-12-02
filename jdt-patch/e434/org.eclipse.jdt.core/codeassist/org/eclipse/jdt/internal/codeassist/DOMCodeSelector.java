// GROOVY PATCHED
/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.internal.core.AnnotatableInfo;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.DOMToModelPopulator;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.core.search.TypeNameMatchRequestorWrapper;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * A util to select relevant IJavaElement from a DOM (as opposed to {@link SelectionEngine}
 * which processes it using lower-level ECJ parser)
 */
public class DOMCodeSelector {

	private final CompilationUnit unit;
	private final WorkingCopyOwner owner;

	public DOMCodeSelector(CompilationUnit unit, WorkingCopyOwner owner) {
		this.unit = unit;
		this.owner = owner;
	}

	public IJavaElement[] codeSelect(int offset, int length) throws JavaModelException {
		if (offset < 0) {
			throw new JavaModelException(new IndexOutOfBoundsException(offset), IJavaModelStatusConstants.INDEX_OUT_OF_BOUNDS);
		}
		if (offset + length > this.unit.getSource().length()) {
			throw new JavaModelException(new IndexOutOfBoundsException(offset + length), IJavaModelStatusConstants.INDEX_OUT_OF_BOUNDS);
		}
		org.eclipse.jdt.core.dom.CompilationUnit currentAST = this.unit.getOrBuildAST(this.owner);
		if (currentAST == null) {
			return new IJavaElement[0];
		}
		String rawText = this.unit.getSource().substring(offset, offset + length);
		int initialOffset = offset, initialLength = length;
		boolean insideComment = ((List<Comment>)currentAST.getCommentList()).stream()
			.anyMatch(comment -> comment.getStartPosition() <= initialOffset && comment.getStartPosition() + comment.getLength() >= initialOffset + initialLength);
		if (!insideComment) { // trim whitespaces and surrounding comments
			boolean changed = false;
			do {
				changed = false;
				if (length > 0 && Character.isWhitespace(this.unit.getSource().charAt(offset))) {
					offset++;
					length--;
					changed = true;
				}
				if (length > 0 && Character.isWhitespace(this.unit.getSource().charAt(offset + length - 1))) {
					length--;
					changed = true;
				}
				List<Comment> comments = currentAST.getCommentList();
				// leading comment
				int offset1 = offset, length1 = length;
				OptionalInt leadingCommentEnd = comments.stream().filter(comment -> {
					int commentEndOffset = comment.getStartPosition() + comment.getLength() -1;
					return comment.getStartPosition() <= offset1 && commentEndOffset > offset1 && commentEndOffset < offset1 + length1 - 1;
				}).mapToInt(comment -> comment.getStartPosition() + comment.getLength() - 1)
				.findAny();
				if (length > 0 && leadingCommentEnd.isPresent()) {
					changed = true;
					int newStart = leadingCommentEnd.getAsInt();
					int removedLeading = newStart + 1 - offset;
					offset = newStart + 1;
					length -= removedLeading;
				}
				// Trailing comment
				int offset2 = offset, length2 = length;
				OptionalInt trailingCommentStart = comments.stream().filter(comment -> {
					return comment.getStartPosition() >= offset2
						&& comment.getStartPosition() < offset2 + length2
						&& comment.getStartPosition() + comment.getLength() > offset2 + length2;
				}).mapToInt(Comment::getStartPosition)
				.findAny();
				if (length > 0 && trailingCommentStart.isPresent()) {
					changed = true;
					int newEnd = trailingCommentStart.getAsInt();
					int removedTrailing = offset + length - 1 - newEnd;
					length -= removedTrailing;
				}
			} while (changed);
		}
		String trimmedText = rawText.trim();
		NodeFinder finder = new NodeFinder(currentAST, offset, length);
		final ASTNode node = finder.getCoveredNode() != null && finder.getCoveredNode().getStartPosition() > offset && finder.getCoveringNode().getStartPosition() + finder.getCoveringNode().getLength() > offset + length ?
			finder.getCoveredNode() :
			finder.getCoveringNode();
		if (node instanceof TagElement tagElement && TagElement.TAG_INHERITDOC.equals(tagElement.getTagName())) {
			ASTNode javadocNode = node;
			while (javadocNode != null && !(javadocNode instanceof Javadoc)) {
				javadocNode = javadocNode.getParent();
			}
			if (javadocNode instanceof Javadoc javadoc) {
				ASTNode parent = javadoc.getParent();
				IBinding binding = resolveBinding(parent);
				if (binding instanceof IMethodBinding methodBinding) {
					var typeBinding = methodBinding.getDeclaringClass();
					if (typeBinding != null) {
						List<ITypeBinding> types = new ArrayList<>(Arrays.asList(typeBinding.getInterfaces()));
						if (typeBinding.getSuperclass() != null) {
							types.add(typeBinding.getSuperclass());
						}
						while (!types.isEmpty()) {
							ITypeBinding type = types.remove(0);
							for (IMethodBinding m : Arrays.stream(type.getDeclaredMethods()).filter(methodBinding::overrides).toList()) {
								if (m.getJavaElement() instanceof IMethod methodElement && methodElement.getJavadocRange() != null) {
									return new IJavaElement[] { methodElement };
								} else {
									types.addAll(Arrays.asList(type.getInterfaces()));
									if (type.getSuperclass() != null) {
										types.add(type.getSuperclass());
									}
								}
							}
						}
					}
					IJavaElement element = methodBinding.getJavaElement();
					if (element != null) {
						return new IJavaElement[] { element };
					}
				}
			}
		}
		org.eclipse.jdt.core.dom.ImportDeclaration importDecl = findImportDeclaration(node);
		if (node instanceof ExpressionMethodReference emr &&
			emr.getExpression().getStartPosition() + emr.getExpression().getLength() <= offset && offset + length <= emr.getName().getStartPosition()) {
			if (!(rawText.isEmpty() || rawText.equals(":") || rawText.equals("::"))) { //$NON-NLS-1$ //$NON-NLS-2$
				return new IJavaElement[0];
			}
			if (emr.getParent() instanceof MethodInvocation methodInvocation) {
				int index = methodInvocation.arguments().indexOf(emr);
				return new IJavaElement[] {methodInvocation.resolveMethodBinding().getParameterTypes()[index].getDeclaredMethods()[0].getJavaElement()};
			}
			if (emr.getParent() instanceof VariableDeclaration variableDeclaration) {
				ITypeBinding requestedType = variableDeclaration.resolveBinding().getType();
				if (requestedType.getDeclaredMethods().length == 1
					&& requestedType.getDeclaredMethods()[0].getJavaElement() instanceof IMethod overridenMethod) {
					return new IJavaElement[] { overridenMethod };
				}
			}
		}
		if (node instanceof LambdaExpression lambda) {
			if (!(rawText.isEmpty() || rawText.equals("-") || rawText.equals(">") || rawText.equals("->"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return new IJavaElement[0]; // as requested by some tests
			}
			if (lambda.resolveMethodBinding() != null
				&& lambda.resolveMethodBinding().getMethodDeclaration() != null
				&& lambda.resolveMethodBinding().getMethodDeclaration().getJavaElement() != null) {
				return new IJavaElement[] { lambda.resolveMethodBinding().getMethodDeclaration().getJavaElement() };
			}
		}
		if (importDecl != null && importDecl.isStatic()) {
			IBinding importBinding = importDecl.resolveBinding();
			if (importBinding instanceof IMethodBinding methodBinding) {
				ArrayDeque<IJavaElement> overloadedMethods = Stream.of(methodBinding.getDeclaringClass().getDeclaredMethods()) //
						.filter(otherMethodBinding -> methodBinding.getName().equals(otherMethodBinding.getName())) //
						.map(IMethodBinding::getJavaElement) //
						.filter(IJavaElement::exists)
						.collect(Collectors.toCollection(ArrayDeque::new));
				IJavaElement[] reorderedOverloadedMethods = new IJavaElement[overloadedMethods.size()];
				Iterator<IJavaElement> reverseIterator = overloadedMethods.descendingIterator();
				for (int i = 0; i < reorderedOverloadedMethods.length; i++) {
					reorderedOverloadedMethods[i] = reverseIterator.next();
				}
				return reorderedOverloadedMethods;
			}
			return new IJavaElement[] { importBinding.getJavaElement() };
		} else if (findTypeDeclaration(node) == null) {
			IBinding binding = resolveBinding(node);
			if (binding != null && !binding.isRecovered()) {
				if (node instanceof SuperMethodInvocation && // on `super`
					binding instanceof IMethodBinding methodBinding &&
					/* GROOVY edit
					methodBinding.getDeclaringClass() instanceof ITypeBinding typeBinding &&
					typeBinding.getJavaElement() instanceof IType type) {
					*/
					methodBinding.getDeclaringClass() != null &&
					methodBinding.getDeclaringClass().getJavaElement() instanceof IType type) {
					// GROOVY end
					return new IJavaElement[] { type };
				}
				if (binding instanceof IPackageBinding packageBinding
						&& trimmedText.length() > 0
						&& !trimmedText.equals(packageBinding.getName())
						&& packageBinding.getName().startsWith(trimmedText)) {
					// resolved a too wide node for package name, restrict to selected name only
					IJavaElement fragment = this.unit.getJavaProject().findPackageFragment(trimmedText);
					if (fragment != null) {
						return new IJavaElement[] { fragment };
					}
				}
				// workaround https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2177
				if (binding instanceof IVariableBinding variableBinding &&
					/* GROOVY edit
					variableBinding.getDeclaringMethod() instanceof IMethodBinding declaringMethod &&
					declaringMethod.isCompactConstructor() &&
					Arrays.stream(declaringMethod.getParameterNames()).anyMatch(variableBinding.getName()::equals) &&
					declaringMethod.getDeclaringClass() instanceof ITypeBinding recordBinding &&
					recordBinding.isRecord() &&
					recordBinding.getJavaElement() instanceof IType recordType &&
					*/
					variableBinding.getDeclaringMethod() != null &&
					variableBinding.getDeclaringMethod().isCompactConstructor() &&
					Arrays.stream(variableBinding.getDeclaringMethod().getParameterNames()).anyMatch(pn -> variableBinding.getName().equals(pn)) &&
					variableBinding.getDeclaringMethod().getDeclaringClass() != null &&
					variableBinding.getDeclaringMethod().getDeclaringClass().isRecord() &&
					variableBinding.getDeclaringMethod().getDeclaringClass().getJavaElement() instanceof IType recordType &&
					// GROOVY end
					recordType.getField(variableBinding.getName()) instanceof SourceField field) {
					// the parent must be the field and not the method
					return new IJavaElement[] { new LocalVariable(field,
						variableBinding.getName(),
						0, // must be 0 for subsequent call to LocalVariableLocator.matchLocalVariable() to work
						field.getSourceRange().getOffset() + field.getSourceRange().getLength() - 1,
						field.getNameRange().getOffset(),
						field.getNameRange().getOffset() + field.getNameRange().getLength() - 1,
						field.getTypeSignature(),
						null,
						field.getFlags(),
						true) };
				}
				if (binding instanceof ITypeBinding typeBinding &&
					typeBinding.isIntersectionType()) {
					return Arrays.stream(typeBinding.getTypeBounds())
							.map(ITypeBinding::getJavaElement)
							.filter(Objects::nonNull)
							.toArray(IJavaElement[]::new);
				}
				IJavaElement element = binding.getJavaElement();
				if (element != null && (element instanceof IPackageFragment || element.exists())) {
					return new IJavaElement[] { element };
				}
				if (binding instanceof ITypeBinding typeBinding) {
					if (this.unit.getJavaProject() != null) {
						IType type = this.unit.getJavaProject().findType(typeBinding.getQualifiedName());
						if (type != null) {
							return new IJavaElement[] { type };
						}
					}
					// fallback to calling index, inspired/copied from SelectionEngine
					IJavaElement[] indexMatch = findTypeInIndex(typeBinding.getPackage() != null ? typeBinding.getPackage().getName() : null, typeBinding.getName());
					if (indexMatch.length > 0) {
						return indexMatch;
					}
				}
				if (binding instanceof IVariableBinding variableBinding && variableBinding.getDeclaringMethod() != null && variableBinding.getDeclaringMethod().isCompactConstructor()) {
					// workaround for JavaSearchBugs15Tests.testBug558812_012
					if (variableBinding.getDeclaringMethod().getJavaElement() instanceof IMethod method) {
						Optional<ILocalVariable> parameter = Arrays.stream(method.getParameters()).filter(param -> Objects.equals(param.getElementName(), variableBinding.getName())).findAny();
						if (parameter.isPresent()) {
							return new IJavaElement[] { parameter.get() };
						}
					}
				}
				IField field; // GROOVY add
				if (binding instanceof IMethodBinding methodBinding &&
					methodBinding.isSyntheticRecordMethod() &&
					methodBinding.getDeclaringClass().getJavaElement() instanceof IType recordType &&
					/* GROOVY edigt
					recordType.getField(methodBinding.getName()) instanceof IField field) {
					*/
					(field = recordType.getField(methodBinding.getName())) != null) {
					// GROOVY end
					return new IJavaElement[] { field };
				}
				ASTNode bindingNode = currentAST.findDeclaringNode(binding);
				if (bindingNode != null) {
					IJavaElement parent = this.unit.getElementAt(bindingNode.getStartPosition());
					if (parent != null && bindingNode instanceof SingleVariableDeclaration variableDecl) {
						return new IJavaElement[] { DOMToModelPopulator.toLocalVariable(variableDecl, (JavaElement)parent) };
					}
				}
			}
		}
		// fallback: crawl the children of this unit
		IJavaElement currentElement = this.unit;
		boolean newChildFound;
		int finalOffset = offset;
		int finalLength = length;
		do {
			newChildFound = false;
			if (currentElement instanceof IParent parentElement) {
				Optional<IJavaElement> candidate = Stream.of(parentElement.getChildren())
					.filter(ISourceReference.class::isInstance)
					.map(ISourceReference.class::cast)
					.filter(sourceRef -> {
						try {
							ISourceRange elementRange = sourceRef.getSourceRange();
							return elementRange != null
								&& elementRange.getOffset() >= 0
								&& elementRange.getOffset() <= finalOffset
								&& elementRange.getOffset() + elementRange.getLength() >= finalOffset + finalLength;
						} catch (JavaModelException e) {
							return false;
						}
					}).map(IJavaElement.class::cast)
					.findAny();
				if (candidate.isPresent()) {
					newChildFound = true;
					currentElement = candidate.get();
				}
			}
		} while (newChildFound);
		if (currentElement instanceof JavaElement impl &&
				impl.getElementInfo() instanceof AnnotatableInfo annotable &&
				annotable.getNameSourceStart() >= 0 &&
				annotable.getNameSourceStart() <= offset &&
				annotable.getNameSourceEnd() + 1 /* end exclusive vs offset inclusive */ >= offset) {
			return new IJavaElement[] { currentElement };
		}
		if (insideComment) {
			String toSearch = trimmedText.isBlank() ? findWord(offset) : trimmedText;
			String resolved = ((List<org.eclipse.jdt.core.dom.ImportDeclaration>)currentAST.imports()).stream()
				.map(org.eclipse.jdt.core.dom.ImportDeclaration::getName)
				.map(Name::toString)
				.filter(importedPackage -> importedPackage.endsWith(toSearch))
				.findAny()
				.orElse(toSearch);
			/* GROOVY edit
			if (this.unit.getJavaProject().findType(resolved) instanceof IType type) {
			*/
			IType type;
			if ((type = this.unit.getJavaProject().findType(resolved)) != null) {
			// GROOVY end
				return new IJavaElement[] { type };
			}
		}
		// failback to lookup search
		ASTNode currentNode = node;
		while (currentNode != null && !(currentNode instanceof Type)) {
			currentNode = currentNode.getParent();
		}
		if (currentNode instanceof Type parentType) {
			if (this.unit.getJavaProject() != null) {
				StringBuilder buffer = new StringBuilder();
				Util.getFullyQualifiedName(parentType, buffer);
				IType type = this.unit.getJavaProject().findType(buffer.toString());
				if (type != null) {
					return new IJavaElement[] { type };
				}
			}
			String packageName = parentType instanceof QualifiedType qType ? qType.getQualifier().toString() :
				parentType instanceof SimpleType sType ?
					sType.getName() instanceof QualifiedName qName ? qName.getQualifier().toString() :
					null :
				null;
			String simpleName = parentType instanceof QualifiedType qType ? qType.getName().toString() :
				parentType instanceof SimpleType sType ?
					sType.getName() instanceof SimpleName sName ? sName.getIdentifier() :
					sType.getName() instanceof QualifiedName qName ? qName.getName().toString() :
					null :
				null;
			IJavaElement[] indexResult = findTypeInIndex(packageName, simpleName);
			if (indexResult.length > 0) {
				return indexResult;
			}
		}
		// no good idea left
		return new IJavaElement[0];
	}

	static IBinding resolveBinding(ASTNode node) {
		if (node instanceof MethodDeclaration decl) {
			return decl.resolveBinding();
		}
		if (node instanceof MethodInvocation invocation) {
			return invocation.resolveMethodBinding();
		}
		if (node instanceof VariableDeclaration decl) {
			return decl.resolveBinding();
		}
		if (node instanceof FieldAccess access) {
			return access.resolveFieldBinding();
		}
		if (node instanceof Type type) {
			return type.resolveBinding();
		}
		if (node instanceof Name aName) {
			ClassInstanceCreation newInstance = findConstructor(aName);
			if (newInstance != null) {
				var constructorBinding = newInstance.resolveConstructorBinding();
				if (constructorBinding != null) {
					var constructorElement = constructorBinding.getJavaElement();
					if (constructorElement != null) {
						boolean hasSource = true;
						try {
							hasSource = ((ISourceReference)constructorElement.getParent()).getSource() != null;
						} catch (Exception e) {
							hasSource = false;
						}
						if ((constructorBinding.getParameterTypes().length > 0 /*non-default*/ ||
								constructorElement instanceof SourceMethod || !hasSource)) {
							return constructorBinding;
						}
					} else if (newInstance.resolveTypeBinding().isAnonymous()) {
						// it's not in the anonymous class body, check for constructor decl in parent types

						ITypeBinding superclassBinding = newInstance.getType().resolveBinding();

						while (superclassBinding != null) {
							Optional<IMethodBinding> potentialConstructor = Stream.of(superclassBinding.getDeclaredMethods()) //
									.filter(methodBinding -> methodBinding.isConstructor() && matchSignatures(constructorBinding, methodBinding))
									.findFirst();
							if (potentialConstructor.isPresent()) {
								IMethodBinding theConstructor = potentialConstructor.get();
								if (theConstructor.isDefaultConstructor()) {
									return theConstructor.getDeclaringClass();
								}
								return theConstructor;
							}
							superclassBinding = superclassBinding.getSuperclass();
						}
						return null;
					}
				}
			}
			if (node.getParent() instanceof ExpressionMethodReference exprMethodReference && exprMethodReference.getName() == node) {
				return resolveBinding(exprMethodReference);
			}
			if (node.getParent() instanceof TypeMethodReference typeMethodReference && typeMethodReference.getName() == node) {
				return resolveBinding(typeMethodReference);
			}
			IBinding res = aName.resolveBinding();
			if (res != null) {
				return res;
			}
			return resolveBinding(aName.getParent());
		}
		if (node instanceof org.eclipse.jdt.core.dom.LambdaExpression lambda) {
			return lambda.resolveMethodBinding();
		}
		if (node instanceof ExpressionMethodReference methodRef) {
			IMethodBinding methodBinding = methodRef.resolveMethodBinding();
			try {
				if (methodBinding == null) {
					return null;
				}
				IMethod methodModel = ((IMethod)methodBinding.getJavaElement());
				boolean allowExtraParam = true;
				if ((methodModel.getFlags() & Flags.AccStatic) != 0) {
					allowExtraParam = false;
					if (methodRef.getExpression() instanceof ClassInstanceCreation) {
						return null;
					}
				}

				// find the type that the method is bound to
				ITypeBinding type = null;
				ASTNode cursor = methodRef;
				while (type == null && cursor != null) {
					if (cursor.getParent() instanceof VariableDeclarationFragment declFragment) {
						type = declFragment.resolveBinding().getType();
					}
					else if (cursor.getParent() instanceof MethodInvocation methodInvocation) {
						IMethodBinding methodInvocationBinding = methodInvocation.resolveMethodBinding();
						int index = methodInvocation.arguments().indexOf(cursor);
						type = methodInvocationBinding.getParameterTypes()[index];
					} else {
						cursor = cursor.getParent();
					}
				}

				IMethodBinding boundMethod = type.getDeclaredMethods()[0];

				if (boundMethod.getParameterTypes().length != methodBinding.getParameterTypes().length && (!allowExtraParam || boundMethod.getParameterTypes().length != methodBinding.getParameterTypes().length + 1)) {
					return null;
				}
			} catch (JavaModelException e) {
				return null;
			}
			return methodBinding;
		}
		if (node instanceof MethodReference methodRef) {
			return methodRef.resolveMethodBinding();
		}
		if (node instanceof org.eclipse.jdt.core.dom.TypeParameter typeParameter) {
			return typeParameter.resolveBinding();
		}
		if (node instanceof SuperConstructorInvocation superConstructor) {
			return superConstructor.resolveConstructorBinding();
		}
		if (node instanceof ConstructorInvocation constructor) {
			return constructor.resolveConstructorBinding();
		}
		if (node instanceof org.eclipse.jdt.core.dom.Annotation annotation) {
			return annotation.resolveTypeBinding();
		}
		if (node instanceof SuperMethodInvocation superMethod) {
			return superMethod.resolveMethodBinding();
		}
		return null;
	}

	private static ClassInstanceCreation findConstructor(ASTNode node) {
		while (node != null && !(node instanceof ClassInstanceCreation)) {
			ASTNode parent = node.getParent();
			if ((parent instanceof SimpleType type && type.getName() == node) ||
				(parent instanceof ClassInstanceCreation constructor && constructor.getType() == node) ||
				(parent instanceof ParameterizedType parameterized && parameterized.getType() == node)) {
				node = parent;
			} else {
				node = null;
			}
		}
		return (ClassInstanceCreation)node;
	}

	private static AbstractTypeDeclaration findTypeDeclaration(ASTNode node) {
		ASTNode cursor = node;
		while (cursor != null && (cursor instanceof Type || cursor instanceof Name)) {
			cursor = cursor.getParent();
		}
		if (cursor instanceof AbstractTypeDeclaration typeDecl && typeDecl.getName() == node) {
			return typeDecl;
		}
		return null;
	}

	private static org.eclipse.jdt.core.dom.ImportDeclaration findImportDeclaration(ASTNode node) {
		while (node != null && !(node instanceof org.eclipse.jdt.core.dom.ImportDeclaration)) {
			node = node.getParent();
		}
		return (org.eclipse.jdt.core.dom.ImportDeclaration)node;
	}

	private static boolean matchSignatures(IMethodBinding invocation, IMethodBinding declaration) {
		if (declaration.getTypeParameters().length == 0) {
			return invocation.isSubsignature(declaration);
		}
		if (invocation.getParameterTypes().length != declaration.getParameterTypes().length) {
			return false;
		}
		for (int i = 0; i < invocation.getParameterTypes().length; i++) {
			if (declaration.getParameterTypes()[i].isTypeVariable()) {
				if (declaration.getParameterTypes()[i].getTypeBounds().length > 0) {
					ITypeBinding[] bounds = declaration.getParameterTypes()[i].getTypeBounds();
					for (int j = 0; j < bounds.length; j++) {
						if (!invocation.getParameterTypes()[i].isSubTypeCompatible(bounds[j])) {
							return false;
						}
					}
				}
			} else if (!invocation.getParameterTypes()[i].isSubTypeCompatible(declaration.getParameterTypes()[i])) {
				return false;
			}

		}
		return true;
	}

	private IJavaElement[] findTypeInIndex(String packageName, String simpleName) throws JavaModelException {
		List<IType> indexMatch = new ArrayList<>();
		TypeNameMatchRequestor requestor = new TypeNameMatchRequestor() {
			@Override
			public void acceptTypeNameMatch(org.eclipse.jdt.core.search.TypeNameMatch match) {
				indexMatch.add(match.getType());
			}
		};
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] { this.unit.getJavaProject() });
		new SearchEngine(this.owner).searchAllTypeNames(
			packageName != null ? packageName.toCharArray() : null,
			SearchPattern.R_EXACT_MATCH,
			simpleName.toCharArray(),
			SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE,
			IJavaSearchConstants.TYPE,
			scope,
			requestor,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			new NullProgressMonitor());
		if (!indexMatch.isEmpty()) {
			return indexMatch.toArray(IJavaElement[]::new);
		}
		scope = BasicSearchEngine.createWorkspaceScope();
		new BasicSearchEngine(this.owner).searchAllTypeNames(
			packageName != null ? packageName.toCharArray() : null,
			SearchPattern.R_EXACT_MATCH,
			simpleName.toCharArray(),
			SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE,
			IJavaSearchConstants.TYPE,
			scope,
			new TypeNameMatchRequestorWrapper(requestor, scope),
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			new NullProgressMonitor());
		if (!indexMatch.isEmpty()) {
			return indexMatch.toArray(IJavaElement[]::new);
		}
		return new IJavaElement[0];
	}

	private String findWord(int offset) throws JavaModelException {
		int start = offset;
		String source = this.unit.getSource();
		while (start >= 0 && Character.isJavaIdentifierPart(source.charAt(start))) start--;
		int end = offset + 1;
		while (end < source.length() && Character.isJavaIdentifierPart(source.charAt(end))) end++;
		return source.substring(start, end);
	}
}
