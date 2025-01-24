/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IElementInfo;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.RecoveryScanner;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.core.ModuleDescriptionInfo.ModuleReferenceInfo;
import org.eclipse.jdt.internal.core.ModuleDescriptionInfo.PackageExportInfo;
import org.eclipse.jdt.internal.core.ModuleDescriptionInfo.ServiceInfo;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Process an AST to populate a tree of IJavaElement->JavaElementInfo.
 * DOM-first approach to what legacy implements through ECJ parser and CompilationUnitStructureRequestor
 */
public class DOMToModelPopulator extends ASTVisitor {

	private final Map<IJavaElement, IElementInfo> toPopulate;
	private final Stack<JavaElement> elements = new Stack<>();
	private final Stack<JavaElementInfo> infos = new Stack<>();
	private final Set<String> currentTypeParameters = new HashSet<>();
	private final Map<SourceType, Integer> nestedTypesCount = new HashMap<>();
	private final CompilationUnitElementInfo unitInfo;
	private ImportContainer importContainer;
	private ImportContainerInfo importContainerInfo;
	private final CompilationUnit root;
	private Boolean alternativeDeprecated = null;

	public DOMToModelPopulator(Map<IJavaElement, IElementInfo> newElements, CompilationUnit root, CompilationUnitElementInfo unitInfo) {
		this.toPopulate = newElements;
		this.elements.push(root);
		this.infos.push(unitInfo);
		this.root = root;
		this.unitInfo = unitInfo;
	}

	private void addAsChild(JavaElementInfo parentInfo, IJavaElement childElement) {
		if (childElement instanceof SourceRefElement element) {
			while (Stream.of(parentInfo.getChildren())
					.filter(other -> other.getElementType() == element.getElementType())
					.filter(other -> Objects.equals(other.getHandleIdentifier(), element.getHandleIdentifier()))
					.findAny().isPresent()) {
				element.incOccurrenceCount();
			}
			if (childElement instanceof SourceType anonymousType && anonymousType.isAnonymous()) {
				// occurrence count for anonymous types are counted from the including type
				IJavaElement parent = element.getParent().getAncestor(IJavaElement.TYPE);
				if (parent instanceof SourceType nestType) {
					anonymousType.localOccurrenceCount = this.nestedTypesCount.compute(nestType, (nest, currentCount) -> currentCount == null ? 1 : currentCount + 1); // occurrences count are 1-based
				}
			}
		}
		if (parentInfo instanceof AnnotatableInfo annotable && childElement instanceof IAnnotation annotation) {
			if (Stream.of(annotable.annotations).noneMatch(annotation::equals)) {
				IAnnotation[] newAnnotations = Arrays.copyOf(annotable.annotations, annotable.annotations.length + 1);
				newAnnotations[newAnnotations.length - 1] = annotation;
				annotable.annotations = newAnnotations;
			}
			return;
		}
		if (childElement instanceof TypeParameter typeParam) {
			if (parentInfo instanceof SourceTypeElementInfo type) {
				type.typeParameters = Arrays.copyOf(type.typeParameters, type.typeParameters.length + 1);
				type.typeParameters[type.typeParameters.length - 1] = typeParam;
				return;
			}
			if (parentInfo instanceof SourceMethodElementInfo method) {
				method.typeParameters = Arrays.copyOf(method.typeParameters, method.typeParameters.length + 1);
				method.typeParameters[method.typeParameters.length - 1] = typeParam;
				return;
			}
		}
		if (parentInfo instanceof ImportContainerInfo current && childElement instanceof org.eclipse.jdt.internal.core.ImportDeclaration importDecl) {
			IJavaElement[] newImports = Arrays.copyOf(current.getChildren(), current.getChildren().length + 1);
			newImports[newImports.length - 1] = importDecl;
			current.children = newImports;
			return;
		}
		// if nothing more specialized, add as child
		if (parentInfo instanceof SourceTypeElementInfo type) {
			type.children = Arrays.copyOf(type.children, type.children.length + 1);
			type.children[type.children.length - 1] = childElement;
			return;
		}
		if (parentInfo instanceof OpenableElementInfo openable) {
			openable.addChild(childElement);
			return;
		}
		if (parentInfo instanceof SourceMethodElementInfo method // also matches constructor
			&& childElement instanceof LocalVariable variable
			&& variable.isParameter()) {
			ILocalVariable[] parameters = method.arguments != null ? Arrays.copyOf(method.arguments, method.arguments.length + 1) : new ILocalVariable[1];
			parameters[parameters.length - 1] = variable;
			method.arguments = parameters;
			return;
		}
		if (parentInfo instanceof SourceMethodWithChildrenInfo method) {
			IJavaElement[] newElements = Arrays.copyOf(method.children, method.children.length + 1);
			newElements[newElements.length - 1] = childElement;
			method.children = newElements;
			return;
		}
		if (parentInfo instanceof SourceFieldWithChildrenInfo field) {
			IJavaElement[] newElements = Arrays.copyOf(field.children, field.children.length + 1);
			newElements[newElements.length - 1] = childElement;
			field.children = newElements;
			return;
		}
		if (parentInfo instanceof SourceConstructorWithChildrenInfo constructor) {
			IJavaElement[] newElements = Arrays.copyOf(constructor.children, constructor.children.length + 1);
			newElements[newElements.length - 1] = childElement;
			constructor.children = newElements;
			return;
		}
		if (parentInfo instanceof InitializerWithChildrenInfo info) {
			IJavaElement[] newElements = Arrays.copyOf(info.getChildren(), info.getChildren().length + 1);
			newElements[newElements.length - 1] = childElement;
			info.children = newElements;
			return;
		}
	}

	@Override
	public boolean visit(org.eclipse.jdt.core.dom.CompilationUnit node) {
		this.unitInfo.setSourceLength(node.getLength());
		return true;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		org.eclipse.jdt.internal.core.PackageDeclaration newElement = new org.eclipse.jdt.internal.core.PackageDeclaration(this.root, node.getName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		AnnotatableInfo newInfo = new AnnotatableInfo();
		setSourceRange(newInfo, node);
		newInfo.setNameSourceStart(node.getName().getStartPosition());
		newInfo.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(PackageDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		if (this.importContainer == null) {
			this.importContainer = this.root.getImportContainer();
			this.importContainerInfo = new ImportContainerInfo();
			JavaElementInfo parentInfo = this.infos.peek();
			addAsChild(parentInfo, this.importContainer);
			this.toPopulate.put(this.importContainer, this.importContainerInfo);
		}
		org.eclipse.jdt.internal.core.ImportDeclaration newElement = new org.eclipse.jdt.internal.core.ImportDeclaration(this.importContainer, node.getName().toString(), node.isOnDemand());
		this.elements.push(newElement);
		addAsChild(this.importContainerInfo, newElement);
		ImportDeclarationElementInfo newInfo = new ImportDeclarationElementInfo();
		setSourceRange(newInfo, node);
		newInfo.setNameSourceStart(node.getName().getStartPosition());
		int nameSourceEnd = node.getName().getStartPosition() + node.getName().getLength() - 1;
		if (node.isOnDemand()) {
			nameSourceEnd = node.getStartPosition() + node.getLength() - 1;
			char[] contents = this.root.getContents();
			List<Comment> comments = domUnit(node).getCommentList();
			boolean changed = false;
			do {
				while (contents[nameSourceEnd] == ';' || Character.isWhitespace(contents[nameSourceEnd])) {
					nameSourceEnd--;
					changed = true;
				}
				final int currentEnd = nameSourceEnd;
				int newEnd = comments.stream()
					.filter(comment -> comment.getStartPosition() <= currentEnd && comment.getStartPosition() + comment.getLength() >= currentEnd)
					.findAny()
					.map(comment -> comment.getStartPosition() - 1)
					.orElse(currentEnd);
				changed = (currentEnd != newEnd);
				nameSourceEnd = newEnd;
			} while (nameSourceEnd > 0 && changed);
		}
		newInfo.setNameSourceEnd(nameSourceEnd);
		newInfo.setFlags(node.isStatic() ? Flags.AccStatic : 0);
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(ImportDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(ImplicitTypeDeclaration node) {
		SourceType newElement = new SourceType(this.elements.peek(), this.root.getElementName().endsWith(".java") ? this.root.getElementName().substring(0, this.root.getElementName().length() - 5) : this.root.getElementName()); //$NON-NLS-1$
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceTypeElementInfo newInfo = new SourceTypeElementInfo();
		newInfo.setFlags(ExtraCompilerModifiers.AccImplicitlyDeclared);
		setSourceRange(newInfo, node);

		newInfo.setHandle(newElement);
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(ImplicitTypeDeclaration node) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean visit(TypeDeclaration node) {
		if (TypeConstants.MODULE_INFO_FILE_NAME_STRING.equals(this.root.getElementName())) {
			// ignore as it can cause downstream issues
			return false;
		}
		if (node.getAST().apiLevel() > 2) {
			((List<org.eclipse.jdt.core.dom.TypeParameter>)node.typeParameters())
				.stream()
				.map(org.eclipse.jdt.core.dom.TypeParameter::getName)
				.map(Name::getFullyQualifiedName)
				.forEach(this.currentTypeParameters::add);
		}
		SourceType newElement = new SourceType(this.elements.peek(), node.getName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceTypeElementInfo newInfo = new SourceTypeElementInfo();
		boolean isDeprecated = isNodeDeprecated(node);
		char[][] categories = getCategories(node);
		newInfo.addCategories(newElement, categories);
		JavaElementInfo toPopulateCategories = this.infos.peek();
		while (toPopulateCategories != null) {
			if (toPopulateCategories instanceof SourceTypeElementInfo parentTypeInfo) {
				parentTypeInfo.addCategories(newElement, categories);
				toPopulateCategories = (JavaElementInfo)parentTypeInfo.getEnclosingType();
			} else {
				break;
			}
		}
		if (node.getAST().apiLevel() > 2) {
			char[][] superInterfaces = ((List<Type>)node.superInterfaceTypes()).stream().map(Type::toString).map(String::toCharArray).toArray(char[][]::new);
			if (superInterfaces.length > 0) {
				newInfo.setSuperInterfaceNames(superInterfaces);
			}
		}
		if (node.getAST().apiLevel() > 2 && node.getSuperclassType() != null) {
			newInfo.setSuperclassName(node.getSuperclassType().toString().toCharArray());
		}
		if (node.getAST().apiLevel() >= AST.JLS17) {
			char[][] permitted = ((List<Type>)node.permittedTypes()).stream().map(Type::toString).map(String::toCharArray).toArray(char[][]::new);
			if (permitted.length > 0) {
				newInfo.setPermittedSubtypeNames(permitted);
			}
		}
		setSourceRange(newInfo, node);
		newInfo.setFlags(toModelFlags(node.getModifiers(), isDeprecated) | (node.isInterface() ? Flags.AccInterface : 0));

		newInfo.setHandle(newElement);
		newInfo.setNameSourceStart(node.getName().getStartPosition());
		newInfo.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(TypeDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
		if (decl.getAST().apiLevel() > 2) {
			((List<org.eclipse.jdt.core.dom.TypeParameter>)decl.typeParameters())
				.stream()
				.map(org.eclipse.jdt.core.dom.TypeParameter::getName)
				.map(Name::getFullyQualifiedName)
				.forEach(this.currentTypeParameters::remove);
		}
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		SourceType newElement = new SourceType(this.elements.peek(), node.getName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceTypeElementInfo newInfo = new SourceTypeElementInfo();
		setSourceRange(newInfo, node);
		char[][] categories = getCategories(node);
		newInfo.addCategories(newElement, categories);
		JavaElementInfo toPopulateCategories = this.infos.peek();
		while (toPopulateCategories != null) {
			if (toPopulateCategories instanceof SourceTypeElementInfo parentTypeInfo) {
				parentTypeInfo.addCategories(newElement, categories);
				toPopulateCategories = (JavaElementInfo)parentTypeInfo.getEnclosingType();
			} else {
				break;
			}
		}
		boolean isDeprecated = isNodeDeprecated(node);
		newInfo.setFlags(toModelFlags(node.getModifiers(), isDeprecated) | Flags.AccInterface | Flags.AccAnnotation);
		newInfo.setHandle(newElement);
		newInfo.setNameSourceStart(node.getName().getStartPosition());
		newInfo.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}

	@Override
	public void endVisit(AnnotationTypeDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		SourceType newElement = new SourceType(this.elements.peek(), node.getName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceTypeElementInfo newInfo = new SourceTypeElementInfo();
		setSourceRange(newInfo, node);
		char[][] categories = getCategories(node);
		newInfo.addCategories(newElement, categories);
		JavaElementInfo toPopulateCategories = this.infos.peek();
		while (toPopulateCategories != null) {
			if (toPopulateCategories instanceof SourceTypeElementInfo parentTypeInfo) {
				parentTypeInfo.addCategories(newElement, categories);
				toPopulateCategories = (JavaElementInfo)parentTypeInfo.getEnclosingType();
			} else {
				break;
			}
		}
		boolean isDeprecated = isNodeDeprecated(node);
		newInfo.setFlags(toModelFlags(node.getModifiers(), isDeprecated) | Flags.AccEnum);
		newInfo.setHandle(newElement);
		newInfo.setNameSourceStart(node.getName().getStartPosition());
		newInfo.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
		newInfo.setSuperInterfaceNames(((List<Type>)node.superInterfaceTypes()).stream().map(Type::toString).map(String::toCharArray).toArray(char[][]::new));
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(EnumDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		IJavaElement parent = this.elements.peek();
		SourceField newElement = new SourceField(this.elements.peek(), node.getName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceFieldWithChildrenInfo info = new SourceFieldWithChildrenInfo(new IJavaElement[0]);
		info.setTypeName(parent.getElementName().toCharArray());
		setSourceRange(info, node);
		boolean isDeprecated = isNodeDeprecated(node);
		info.setFlags(toModelFlags(node.getModifiers(), isDeprecated) | ClassFileConstants.AccEnum);
		info.setNameSourceStart(node.getName().getStartPosition());
		info.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
		// TODO populate info
		this.infos.push(info);
		this.toPopulate.put(newElement, info);
		return true;
	}
	@Override
	public void endVisit(EnumConstantDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(RecordDeclaration node) {
		SourceType newElement = new SourceType(this.elements.peek(), node.getName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceTypeElementInfo newInfo = new SourceTypeElementInfo();
		setSourceRange(newInfo, node);
		char[][] categories = getCategories(node);
		newInfo.addCategories(newElement, categories);
		newInfo.setSuperclassName(Record.class.getName().toCharArray());
		newInfo.setSuperInterfaceNames(((List<Type>)node.superInterfaceTypes()).stream().map(Type::toString).map(String::toCharArray).toArray(char[][]::new));
		JavaElementInfo toPopulateCategories = this.infos.peek();
		while (toPopulateCategories != null) {
			if (toPopulateCategories instanceof SourceTypeElementInfo parentTypeInfo) {
				parentTypeInfo.addCategories(newElement, categories);
				toPopulateCategories = (JavaElementInfo)parentTypeInfo.getEnclosingType();
			} else {
				break;
			}
		}
		boolean isDeprecated = isNodeDeprecated(node);
		newInfo.setFlags(toModelFlags(node.getModifiers(), isDeprecated) | Flags.AccRecord);
		newInfo.setHandle(newElement);
		newInfo.setNameSourceStart(node.getName().getStartPosition());
		newInfo.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(RecordDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		if (node.getParent() instanceof RecordDeclaration) {
			SourceField newElement = new SourceField(this.elements.peek(), node.getName().toString()) {
				@Override
				public boolean isRecordComponent() throws JavaModelException {
					return true;
				}
			};
			this.elements.push(newElement);
			addAsChild(this.infos.peek(), newElement);
			SourceFieldElementInfo newInfo = new SourceFieldElementInfo();
			setSourceRange(newInfo, node);
			newInfo.setNameSourceStart(node.getName().getStartPosition());
			newInfo.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
			newInfo.setTypeName(node.getType().toString().toCharArray());
			newInfo.setFlags(toModelFlags(node.getModifiers(), false));
			newInfo.isRecordComponent = true;
			this.infos.push(newInfo);
			this.toPopulate.put(newElement, newInfo);
		} else if (node.getParent() instanceof MethodDeclaration) {
			LocalVariable newElement = toLocalVariable(node, this.elements.peek());
			this.elements.push(newElement);
			addAsChild(this.infos.peek(), newElement);
			AnnotatableInfo newInfo = new AnnotatableInfo();
			setSourceRange(newInfo, node);
			newInfo.setNameSourceStart(node.getName().getStartPosition());
			newInfo.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
			newInfo.setFlags(toModelFlags(node.getModifiers(), false));
			this.infos.push(newInfo);
			this.toPopulate.put(newElement, newInfo);
		}
		return true;
	}
	@Override
	public void endVisit(SingleVariableDeclaration decl) {
		if (decl.getParent() instanceof RecordDeclaration || decl.getParent() instanceof MethodDeclaration) {
			this.elements.pop();
			this.infos.pop();
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean visit(MethodDeclaration method) {
		if (method.getAST().apiLevel() > 2) {
			((List<org.eclipse.jdt.core.dom.TypeParameter>)method.typeParameters())
				.stream()
				.map(org.eclipse.jdt.core.dom.TypeParameter::getName)
				.map(Name::getFullyQualifiedName)
				.forEach(this.currentTypeParameters::add);
		}
		List<SingleVariableDeclaration> parameters = method.parameters();
		if (method.getAST().apiLevel() >= AST.JLS16
			&& method.isCompactConstructor()
			&& (parameters == null || parameters.isEmpty())
			&& method.getParent() instanceof RecordDeclaration parentRecord) {
			parameters = parentRecord.recordComponents();
		}
		SourceMethod newElement = new SourceMethod(this.elements.peek(),
			method.getName().getIdentifier(),
			parameters.stream()
				.map(this::createSignature)
				.toArray(String[]::new));
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceMethodElementInfo info = method.isConstructor() ?
			new SourceConstructorWithChildrenInfo(new IJavaElement[0]) :
			new SourceMethodWithChildrenInfo(new IJavaElement[0]);
		info.setArgumentNames(parameters.stream().map(param -> param.getName().toString().toCharArray()).toArray(char[][]::new));
		if (method.getAST().apiLevel() > 2) {
			if (method.getReturnType2() != null) {
				info.setReturnType(method.getReturnType2().toString().toCharArray());
			} else {
				info.setReturnType("void".toCharArray()); //$NON-NLS-1$
			}
		}
		if (this.infos.peek() instanceof SourceTypeElementInfo parentInfo) {
			parentInfo.addCategories(newElement, getCategories(method));
		}
		if (method.getAST().apiLevel() >= AST.JLS8) {
			info.setExceptionTypeNames(((List<Type>)method.thrownExceptionTypes()).stream().map(Type::toString).map(String::toCharArray).toArray(char[][]::new));
		}
		setSourceRange(info, method);
		boolean isDeprecated = isNodeDeprecated(method);
		info.setFlags(toModelFlags(method.getModifiers(), isDeprecated)
			| ((method.getAST().apiLevel() > AST.JLS2 && ((List<SingleVariableDeclaration>)method.parameters()).stream().anyMatch(SingleVariableDeclaration::isVarargs)) ? Flags.AccVarargs : 0));
		info.setNameSourceStart(method.getName().getStartPosition());
		info.setNameSourceEnd(method.getName().getStartPosition() + method.getName().getLength() - 1);
		if (method.getAST().apiLevel() >= AST.JLS16 && method.isCompactConstructor()) {
			info.arguments = parameters.stream().map(param -> toLocalVariable(param, newElement, true)).toArray(ILocalVariable[]::new);
		}
		this.infos.push(info);
		this.toPopulate.put(newElement, info);
		return true;
	}
	@Override
	public void endVisit(MethodDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
		if (decl.getAST().apiLevel() > 2) {
			((List<org.eclipse.jdt.core.dom.TypeParameter>)decl.typeParameters())
				.stream()
				.map(org.eclipse.jdt.core.dom.TypeParameter::getName)
				.map(Name::getFullyQualifiedName)
				.forEach(this.currentTypeParameters::remove);
		}
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration method) {
		SourceMethod newElement = new SourceMethod(this.elements.peek(),
			method.getName().getIdentifier(),
			new String[0]);
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceAnnotationMethodInfo info = new SourceAnnotationMethodInfo();
		info.setReturnType(method.getType().toString().toCharArray());
		setSourceRange(info, method);
		((SourceTypeElementInfo)this.infos.peek()).addCategories(newElement, getCategories(method));
		boolean isDeprecated = isNodeDeprecated(method);
		info.setFlags(toModelFlags(method.getModifiers(), isDeprecated));
		info.setNameSourceStart(method.getName().getStartPosition());
		info.setNameSourceEnd(method.getName().getStartPosition() + method.getName().getLength() - 1);
		Expression defaultExpr = method.getDefault();
		if (defaultExpr != null) {
			Entry<Object, Integer> value = memberValue(defaultExpr);
			org.eclipse.jdt.internal.core.MemberValuePair mvp = new org.eclipse.jdt.internal.core.MemberValuePair(newElement.getElementName(), value.getKey(), value.getValue());
			info.defaultValue = mvp;
			info.defaultValueStart = defaultExpr.getStartPosition();
			info.defaultValueEnd = defaultExpr.getStartPosition() + defaultExpr.getLength();
		}
		this.infos.push(info);
		this.toPopulate.put(newElement, info);
		return true;
	}
	@Override
	public void endVisit(AnnotationTypeMemberDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(org.eclipse.jdt.core.dom.TypeParameter node) {
		TypeParameter newElement = new TypeParameter(this.elements.peek(), node.getName().getFullyQualifiedName());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		TypeParameterElementInfo info = new TypeParameterElementInfo();
		setSourceRange(info, node);
		info.nameStart = node.getName().getStartPosition();
		info.nameEnd = node.getName().getStartPosition() + node.getName().getLength() - 1;
		info.bounds = ((List<Type>)node.typeBounds()).stream().map(Type::toString).map(String::toCharArray).toArray(char[][]::new);
		info.boundsSignatures = ((List<Type>)node.typeBounds()).stream().map(Util::getSignature).map(String::toCharArray).toArray(char[][]::new);
		this.infos.push(info);
		this.toPopulate.put(newElement, info);
		return true;
	}
	@Override
	public void endVisit(org.eclipse.jdt.core.dom.TypeParameter typeParam) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		JavaElement parent = this.elements.peek();
		Annotation newElement = new Annotation(parent, node.getTypeName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		if (parent instanceof LocalVariable variable) {
			// also need to explicitly add annotations in the parent node,
			// populating the elementInfo is not sufficient?
			variable.annotations = Arrays.copyOf(variable.annotations, variable.annotations.length + 1);
			variable.annotations[variable.annotations.length - 1] = newElement;
		}
		AnnotationInfo newInfo = new AnnotationInfo();
		setSourceRange(newInfo, node);
		newInfo.nameStart = node.getTypeName().getStartPosition();
		newInfo.nameEnd = node.getTypeName().getStartPosition() + node.getTypeName().getLength() - 1;
		newInfo.members = ((List<org.eclipse.jdt.core.dom.MemberValuePair>)node.values())
			.stream()
			.map(domMemberValuePair -> {
				Entry<Object, Integer> value = memberValue(domMemberValuePair.getValue());
				return new org.eclipse.jdt.internal.core.MemberValuePair(domMemberValuePair.getName().toString(), value.getKey(), value.getValue());
			})
			.toArray(IMemberValuePair[]::new);
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(NormalAnnotation decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		JavaElement parent = this.elements.peek();
		Annotation newElement = new Annotation(parent, node.getTypeName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		if (parent instanceof LocalVariable variable) {
			// also need to explicitly add annotations in the parent node,
			// populating the elementInfo is not sufficient?
			variable.annotations = Arrays.copyOf(variable.annotations, variable.annotations.length + 1);
			variable.annotations[variable.annotations.length - 1] = newElement;
		}
		AnnotationInfo newInfo = new AnnotationInfo();
		setSourceRange(newInfo, node);
		newInfo.nameStart = node.getTypeName().getStartPosition();
		newInfo.nameEnd = node.getTypeName().getStartPosition() + node.getTypeName().getLength() - 1;
		newInfo.members = new IMemberValuePair[0];
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(MarkerAnnotation decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		JavaElement parent = this.elements.peek();
		Annotation newElement = new Annotation(parent, node.getTypeName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		if (parent instanceof LocalVariable variable) {
			// also need to explicitly add annotations in the parent node,
			// populating the elementInfo is not sufficient?
			variable.annotations = Arrays.copyOf(variable.annotations, variable.annotations.length + 1);
			variable.annotations[variable.annotations.length - 1] = newElement;
		}
		AnnotationInfo newInfo = new AnnotationInfo();
		setSourceRange(newInfo, node);
		newInfo.nameStart = node.getTypeName().getStartPosition();
		newInfo.nameEnd = node.getTypeName().getStartPosition() + node.getTypeName().getLength() - 1;
		Entry<Object, Integer> value = memberValue(node.getValue());
		newInfo.members = new IMemberValuePair[] { new org.eclipse.jdt.internal.core.MemberValuePair("value", value.getKey(), value.getValue()) }; //$NON-NLS-1$
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(SingleMemberAnnotation decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(AnonymousClassDeclaration decl) {
		SourceType newElement = new SourceType(this.elements.peek(), ""); //$NON-NLS-1$
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		SourceTypeElementInfo newInfo = new SourceTypeElementInfo() {
			@Override
			public boolean isAnonymousMember() {
				return true;
			}
		};
		JavaElementInfo toPopulateCategories = this.infos.peek();
		while (toPopulateCategories != null) {
			if (toPopulateCategories instanceof SourceTypeElementInfo parentTypeInfo) {
				toPopulateCategories = (JavaElementInfo)parentTypeInfo.getEnclosingType();
			} else {
				break;
			}
		}
		newInfo.setHandle(newElement);
		setSourceRange(newInfo, decl);
		if (decl.getParent() instanceof EnumConstantDeclaration enumConstantDeclaration) {
			setSourceRange(newInfo, enumConstantDeclaration);
			newInfo.setNameSourceStart(enumConstantDeclaration.getName().getStartPosition());
			newInfo.setNameSourceEnd(enumConstantDeclaration.getName().getStartPosition() + enumConstantDeclaration.getName().getLength() - 1);
		} else if (decl.getParent() instanceof ClassInstanceCreation constructorInvocation) {
			if (constructorInvocation.getAST().apiLevel() > 2) {
				((List<SimpleType>)constructorInvocation.typeArguments())
					.stream()
					.map(SimpleType::getName)
					.map(Name::getFullyQualifiedName)
					.forEach(this.currentTypeParameters::add);
				Type type = constructorInvocation.getType();
				newInfo.setSuperclassName(type.toString().toCharArray());
				newInfo.setNameSourceStart(type.getStartPosition());
				// TODO consider leading comments just like in setSourceRange(newInfo, node);
				newInfo.setSourceRangeStart(constructorInvocation.getStartPosition());
				int length;
				if (type instanceof ParameterizedType pType) {
					length= pType.getType().getLength();
				} else {
					length = type.getLength();
				}
				newInfo.setNameSourceEnd(type.getStartPosition() + length - 1);
			} else {
				newInfo.setNameSourceStart(constructorInvocation.getType().getStartPosition());
				newInfo.setSourceRangeStart(constructorInvocation.getType().getStartPosition());
				newInfo.setNameSourceEnd(constructorInvocation.getType().getStartPosition() + constructorInvocation.getType().getLength() - 1);
			}
		}
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(AnonymousClassDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
		if (decl.getParent() instanceof ClassInstanceCreation constructorInvocation) {
			if (constructorInvocation.getAST().apiLevel() > 2) {
				((List<SimpleType>)constructorInvocation.typeArguments())
				.stream()
				.map(SimpleType::getName)
				.map(Name::getFullyQualifiedName)
				.forEach(this.currentTypeParameters::remove);
			}
		}
	}

	public Entry<Object, Integer> memberValue(Expression dom) {
		if (dom == null ||
			dom instanceof NullLiteral ||
			(dom instanceof SimpleName name && (
				"MISSING".equals(name.getIdentifier()) || //$NON-NLS-1$ // better compare with internal SimpleName.MISSING
				Arrays.equals(RecoveryScanner.FAKE_IDENTIFIER, name.getIdentifier().toCharArray())))) {
			return new SimpleEntry<>(null, IMemberValuePair.K_UNKNOWN);
		}
		if (dom instanceof StringLiteral stringValue) {
			return new SimpleEntry<>(stringValue.getLiteralValue(), IMemberValuePair.K_STRING);
		}
		if (dom instanceof BooleanLiteral booleanValue) {
			return new SimpleEntry<>(booleanValue.booleanValue(), IMemberValuePair.K_BOOLEAN);
		}
		if (dom instanceof CharacterLiteral charValue) {
			return new SimpleEntry<>(charValue.charValue(), IMemberValuePair.K_CHAR);
		}
		if (dom instanceof TypeLiteral typeLiteral) {
			return new SimpleEntry<>(typeLiteral.getType(), IMemberValuePair.K_CLASS);
		}
		if (dom instanceof SimpleName simpleName) {
			return new SimpleEntry<>(simpleName.toString(), IMemberValuePair.K_SIMPLE_NAME);
		}
		if (dom instanceof QualifiedName qualifiedName) {
			return new SimpleEntry<>(qualifiedName.toString(), IMemberValuePair.K_QUALIFIED_NAME);
		}
		if (dom instanceof org.eclipse.jdt.core.dom.Annotation annotation) {
			return new SimpleEntry<>(toModelAnnotation(annotation, null), IMemberValuePair.K_ANNOTATION);
		}
		if (dom instanceof ArrayInitializer arrayInitializer) {
			var values = ((List<Expression>)arrayInitializer.expressions()).stream().map(this::memberValue).toList();
			var types = values.stream().map(Entry::getValue).distinct().toList();
			return new SimpleEntry<>(values.stream().map(Entry::getKey).toArray(), types.size() == 1 ? types.get(0) : IMemberValuePair.K_UNKNOWN);
		}
		if (dom instanceof NumberLiteral number) {
			String token = number.getToken();
			int type = toAnnotationValuePairType(token);
			Object value = token;
			if ((type == IMemberValuePair.K_LONG && token.endsWith("L")) || //$NON-NLS-1$
				(type == IMemberValuePair.K_FLOAT && token.endsWith("f"))) { //$NON-NLS-1$
				value = token.substring(0, token.length() - 1);
			}
			if (value instanceof String valueString) {
				// I tried using `yield`, but this caused ECJ to throw an AIOOB, preventing compilation
				switch (type) {
					case IMemberValuePair.K_INT: {
						try {
							value =  Integer.parseInt(valueString);
						} catch (NumberFormatException e) {
							type = IMemberValuePair.K_LONG;
							value = Long.parseLong(valueString);
						}
						break;
					}
					case IMemberValuePair.K_LONG: value = Long.parseLong(valueString); break;
					case IMemberValuePair.K_SHORT: value = Short.parseShort(valueString); break;
					case IMemberValuePair.K_BYTE: value = Byte.parseByte(valueString); break;
					case IMemberValuePair.K_FLOAT: value = Float.parseFloat(valueString); break;
					case IMemberValuePair.K_DOUBLE: value = Double.parseDouble(valueString); break;
					default: throw new IllegalArgumentException("Type not (yet?) supported"); //$NON-NLS-1$
				}
			}
			return new SimpleEntry<>(value, type);
		}
		if (dom instanceof PrefixExpression prefixExpression) {
			Expression operand = prefixExpression.getOperand();
			if (!(operand instanceof NumberLiteral) && !(operand instanceof BooleanLiteral)) {
				return new SimpleEntry<>(null, IMemberValuePair.K_UNKNOWN);
			}
			Entry<Object, Integer> entry = memberValue(prefixExpression.getOperand());
			return new SimpleEntry<>(prefixExpression.getOperator().toString() + entry.getKey(), entry.getValue());
		}
		return new SimpleEntry<>(null, IMemberValuePair.K_UNKNOWN);
	}

	private int toAnnotationValuePairType(String token) {
		// inspired by NumberLiteral.setToken
		Scanner scanner = new Scanner();
		scanner.setSource(token.toCharArray());
		try {
			int tokenType = scanner.getNextToken();
			return switch(tokenType) {
				case TerminalTokens.TokenNameDoubleLiteral -> IMemberValuePair.K_DOUBLE;
				case TerminalTokens.TokenNameIntegerLiteral -> IMemberValuePair.K_INT;
				case TerminalTokens.TokenNameFloatingPointLiteral -> IMemberValuePair.K_FLOAT;
				case TerminalTokens.TokenNameLongLiteral -> IMemberValuePair.K_LONG;
				case TerminalTokens.TokenNameMINUS ->
					switch (scanner.getNextToken()) {
						case TerminalTokens.TokenNameDoubleLiteral -> IMemberValuePair.K_DOUBLE;
						case TerminalTokens.TokenNameIntegerLiteral -> IMemberValuePair.K_INT;
						case TerminalTokens.TokenNameFloatingPointLiteral -> IMemberValuePair.K_FLOAT;
						case TerminalTokens.TokenNameLongLiteral -> IMemberValuePair.K_LONG;
						default -> throw new IllegalArgumentException("Invalid number literal : >" + token + "<"); //$NON-NLS-1$//$NON-NLS-2$
					};
				default -> throw new IllegalArgumentException("Invalid number literal : >" + token + "<"); //$NON-NLS-1$//$NON-NLS-2$
			};
		} catch (InvalidInputException ex) {
			ILog.get().error(ex.getMessage(), ex);
			return IMemberValuePair.K_UNKNOWN;
		}
	}

	private Annotation toModelAnnotation(org.eclipse.jdt.core.dom.Annotation domAnnotation, JavaElement parent) {
		IMemberValuePair[] members;
		if (domAnnotation instanceof NormalAnnotation normalAnnotation) {
			members = ((List<MemberValuePair>)normalAnnotation.values()).stream().map(domMemberValuePair -> {
				Entry<Object, Integer> value = memberValue(domMemberValuePair.getValue());
				return new org.eclipse.jdt.internal.core.MemberValuePair(domMemberValuePair.getName().toString(), value.getKey(), value.getValue());
			}).toArray(IMemberValuePair[]::new);
		} else if (domAnnotation instanceof SingleMemberAnnotation single) {
			Entry<Object, Integer> value = memberValue(single.getValue());
			members = new IMemberValuePair[] { new org.eclipse.jdt.internal.core.MemberValuePair("value", value.getKey(), value.getValue())}; //$NON-NLS-1$
		} else {
			members = new IMemberValuePair[0];
		}

		return new Annotation(parent, domAnnotation.getTypeName().toString()) {
			@Override
			public IMemberValuePair[] getMemberValuePairs() {
				return members;
			}
		};
	}

	public static LocalVariable toLocalVariable(SingleVariableDeclaration parameter, JavaElement parent) {
		return toLocalVariable(parameter, parent, parameter.getParent() instanceof MethodDeclaration);
	}

	private static LocalVariable toLocalVariable(SingleVariableDeclaration parameter, JavaElement parent, boolean isParameter) {
		return new LocalVariable(parent,
				parameter.getName().getIdentifier(),
				getStartConsideringLeadingComments(parameter),
				parameter.getStartPosition() + parameter.getLength() - 1,
				parameter.getName().getStartPosition(),
				parameter.getName().getStartPosition() + parameter.getName().getLength() - 1,
				Util.getSignature(parameter.getType()),
				null, // should be populated while navigating children
				toModelFlags(parameter.getModifiers(), false),
				isParameter);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean visit(FieldDeclaration field) {
		JavaElementInfo parentInfo = this.infos.peek();
		JavaElement parentElement = this.elements.peek();
		boolean isDeprecated = isNodeDeprecated(field);
		char[][] categories = getCategories(field);
		for (VariableDeclarationFragment fragment : (Collection<VariableDeclarationFragment>) field.fragments()) {
			SourceField newElement = new SourceField(parentElement, fragment.getName().toString());
			this.elements.push(newElement);
			addAsChild(parentInfo, newElement);
			SourceFieldWithChildrenInfo info = new SourceFieldWithChildrenInfo(new IJavaElement[0]);
			info.setTypeName(field.getType().toString().toCharArray());
			setSourceRange(info, field);
			if (parentInfo instanceof SourceTypeElementInfo parentTypeInfo) {
				parentTypeInfo.addCategories(newElement, categories);
			}
			info.setFlags(toModelFlags(field.getModifiers(), isDeprecated));
			info.setNameSourceStart(fragment.getName().getStartPosition());
			info.setNameSourceEnd(fragment.getName().getStartPosition() + fragment.getName().getLength() - 1);
			Expression initializer = fragment.getInitializer();
			if (((field.getParent() instanceof TypeDeclaration type && type.isInterface())
					|| Flags.isFinal(field.getModifiers()))
			 	&& initializer != null && initializer.getStartPosition() >= 0) {
				info.initializationSource = Arrays.copyOfRange(this.root.getContents(), initializer.getStartPosition(), initializer.getStartPosition() + initializer.getLength());
			}
			this.infos.push(info);
			this.toPopulate.put(newElement, info);
			if (field.getAST().apiLevel() >= AST.JLS3) {
				List<IExtendedModifier> modifiers = field.modifiers();
				if (modifiers != null) {
					modifiers.stream()
						.filter(org.eclipse.jdt.core.dom.Annotation.class::isInstance)
						.map(org.eclipse.jdt.core.dom.Annotation.class::cast)
						.forEach(annotation -> annotation.accept(this)); // force processing of annotation on each fragment
				}
			}
		}
		return true;
	}
	@Override
	public void endVisit(FieldDeclaration decl) {
		int numFragments = decl.fragments().size();
		for (int i = 0; i < numFragments; i++) {
			this.elements.pop();
			this.infos.pop();
		}
	}

	@SuppressWarnings("deprecation")
	private String createSignature(SingleVariableDeclaration decl) {
		String initialSignature = Util.getSignature(decl.getType());
		int extraDimensions = decl.getExtraDimensions();
		if (decl.getAST().apiLevel() > AST.JLS2 && decl.isVarargs()) {
			extraDimensions++;
		}
		return Signature.createArraySignature(initialSignature, extraDimensions);
	}

	@Override
	public boolean visit(Initializer node) {
		org.eclipse.jdt.internal.core.Initializer newElement = new org.eclipse.jdt.internal.core.Initializer(this.elements.peek(), 1);
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		InitializerElementInfo newInfo = new InitializerWithChildrenInfo(new IJavaElement[0]);
		setSourceRange(newInfo, node);
		newInfo.setFlags(toModelFlags(node.getModifiers(), false));
		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);
		return true;
	}
	@Override
	public void endVisit(Initializer decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(ModuleDeclaration node) {
		SourceModule newElement = new SourceModule(this.elements.peek(), node.getName().toString());
		this.elements.push(newElement);
		addAsChild(this.infos.peek(), newElement);
		ModuleDescriptionInfo newInfo = new ModuleDescriptionInfo();
		newInfo.setHandle(newElement);
		newInfo.name = node.getName().toString().toCharArray();
		newInfo.setNameSourceStart(node.getName().getStartPosition());
		newInfo.setNameSourceEnd(node.getName().getStartPosition() + node.getName().getLength() - 1);
		setSourceRange(newInfo, node);
		newInfo.setFlags((hasDeprecatedComment(node.getJavadoc()) || hasDeprecatedAnnotation(node.annotations())) ? Flags.AccDeprecated : 0);
		List<?> moduleStatements = node.moduleStatements();
		LinkedHashSet<ModuleReferenceInfo> requires = new LinkedHashSet<>(moduleStatements.stream()
			.filter(RequiresDirective.class::isInstance)
			.map(RequiresDirective.class::cast)
			.map(this::toModuleReferenceInfo)
			.toList());
		var javaBase = CharOperation.concatWith(TypeConstants.JAVA_BASE, '.');
		if (!Arrays.equals(node.getName().toString().toCharArray(), javaBase)) {
			ModuleReferenceInfo ref = new ModuleReferenceInfo();
			ref.name = javaBase;
			requires.add(ref);
		}
		newInfo.requires = requires.toArray(ModuleReferenceInfo[]::new);
		newInfo.exports = moduleStatements.stream()
			.filter(ExportsDirective.class::isInstance)
			.map(ExportsDirective.class::cast)
			.map(this::toPackageExportInfo)
			.toArray(PackageExportInfo[]::new);
		newInfo.opens = moduleStatements.stream()
			.filter(OpensDirective.class::isInstance)
			.map(OpensDirective.class::cast)
			.map(this::toPackageExportInfo)
			.toArray(PackageExportInfo[]::new);
		newInfo.usedServices = moduleStatements.stream()
			.filter(UsesDirective.class::isInstance)
			.map(UsesDirective.class::cast)
			.map(UsesDirective::getName)
			.map(Name::toString)
			.map(String::toCharArray)
			.toArray(char[][]::new);
		newInfo.services = moduleStatements.stream()
			.filter(ProvidesDirective.class::isInstance)
			.map(ProvidesDirective.class::cast)
			.map(this::toServiceInfo)
			.toArray(ServiceInfo[]::new);
		char[][] categories = getCategories(node);
		newInfo.addCategories(newElement, categories);

		this.infos.push(newInfo);
		this.toPopulate.put(newElement, newInfo);

		this.unitInfo.setModule(newElement);
		try {
			this.root.getJavaProject().setModuleDescription(newElement);
		} catch (JavaModelException e) {
			ILog.get().error(e.getMessage(), e);
		}
		return true;
	}
	@Override
	public void endVisit(ModuleDeclaration decl) {
		this.elements.pop();
		this.infos.pop();
	}

	@Override
	public boolean visit(LambdaExpression node) {
		this.unitInfo.hasFunctionalTypes = true;
		return true;
	}
	@Override
	public boolean visit(CreationReference node) {
		this.unitInfo.hasFunctionalTypes = true;
		return true;
	}
	@Override
	public boolean visit(ExpressionMethodReference node) {
		this.unitInfo.hasFunctionalTypes = true;
		return true;
	}
	@Override
	public boolean visit(TypeMethodReference node) {
		this.unitInfo.hasFunctionalTypes = true;
		return true;
	}
	@Override
	public boolean visit(SuperMethodReference node) {
		this.unitInfo.hasFunctionalTypes = true;
		return true;
	}


	private ModuleReferenceInfo toModuleReferenceInfo(RequiresDirective node) {
		ModuleReferenceInfo res = new ModuleReferenceInfo();
		res.modifiers =
			(ModuleModifier.isTransitive(node.getModifiers()) ? ClassFileConstants.ACC_TRANSITIVE : 0) |
			(ModuleModifier.isStatic(node.getModifiers()) ? Flags.AccStatic : 0);
		res.name = node.getName().toString().toCharArray();
		setSourceRange(res, node);
		return res;
	}
	private PackageExportInfo toPackageExportInfo(ModulePackageAccess node) {
		PackageExportInfo res = new PackageExportInfo();
		res.pack = node.getName().toString().toCharArray();
		setSourceRange(res, node);
		List<Name> modules = node.modules();
		res.target = modules == null || modules.isEmpty() ? null :
			modules.stream().map(name -> name.toString().toCharArray()).toArray(char[][]::new);
		return res;
	}
	private ServiceInfo toServiceInfo(ProvidesDirective node) {
		ServiceInfo res = new ServiceInfo();
		res.flags = node.getFlags();
		res.serviceName = node.getName().toString().toCharArray();
		res.implNames = ((List<Name>)node.implementations()).stream().map(Name::toString).map(String::toCharArray).toArray(char[][]::new);
		setSourceRange(res, node);
		return res;
	}
	private boolean hasDeprecatedComment(Javadoc javadoc) {
		return javadoc != null && javadoc.tags().stream() //
					.anyMatch(tag -> {
						return TagElement.TAG_DEPRECATED.equals(((AbstractTagElement)tag).getTagName());
					});
	}
	private boolean hasDeprecatedAnnotation(List<IExtendedModifier> modifiers) {
	return modifiers != null && modifiers.stream() //
				.anyMatch(modifier ->
					modifier instanceof org.eclipse.jdt.core.dom.Annotation annotation &&
						(Deprecated.class.getName().equals(annotation.getTypeName().toString())
						|| (Deprecated.class.getSimpleName().equals(annotation.getTypeName().toString()) && !hasAlternativeDeprecated()))
				);
	}
	private boolean isNodeDeprecated(BodyDeclaration node) {
		if (hasDeprecatedComment(node.getJavadoc())) {
			return true;
		}
		if (node.getAST().apiLevel() <= 2) {
			return false;
		}
		return hasDeprecatedAnnotation(node.modifiers());
	}
	private boolean hasAlternativeDeprecated() {
		if (this.alternativeDeprecated != null) {
			return this.alternativeDeprecated;
		}
		if (this.importContainer != null) {
			try {
				IJavaElement[] importElements = this.importContainer.getChildren();
				for (IJavaElement child : importElements) {
					IImportDeclaration importDeclaration = (IImportDeclaration) child;
					// It's possible that the user has imported
					// an annotation called "Deprecated" using a wildcard import
					// that replaces "java.lang.Deprecated"
					// However, it's very costly and complex to check if they've done this,
					// so I haven't bothered.
					if (!importDeclaration.isOnDemand()
							&& importDeclaration.getElementName().endsWith("Deprecated")) { //$NON-NLS-1$
						this.alternativeDeprecated = true;
						return this.alternativeDeprecated;
					}
				}
			} catch (JavaModelException e) {
				// do nothing
			}
		}
		this.alternativeDeprecated = false;
		return this.alternativeDeprecated;
	}
	private char[][] getCategories(ASTNode node) {
		Javadoc javadoc = javadoc(node);
		if (javadoc != null) {
			char[][] categories = ((List<AbstractTagElement>)javadoc.tags()).stream() //
					.filter(tag -> "@category".equals(tag.getTagName()) && ((List<ASTNode>)tag.fragments()).size() > 0) //$NON-NLS-1$
					.map(tag -> ((List<ASTNode>)tag.fragments()).get(0)) //
					.map(fragment -> {
						String fragmentString = fragment.toString();
						/**
						 * I think this is a bug in JDT, but I am replicating the behaviour.
						 *
						 * @see CompilationUnitTests.testGetCategories13()
						 */
						int firstAsterix = fragmentString.indexOf('*');
						return fragmentString.substring(0, firstAsterix != -1 ? firstAsterix : fragmentString.length());
					}) //
					.flatMap(fragment -> (Stream<String>)Stream.of(fragment.split("\\s+"))) // //$NON-NLS-1$
					.filter(category -> category.length() > 0) //
					.map(category -> (category).toCharArray()) //
					.toArray(char[][]::new);
			return categories.length > 0 ? categories : null;
		}
		return null;
	}

	private static void setSourceRange(SourceRefElementInfo info, ASTNode node) {
		info.setSourceRangeStart(getStartConsideringLeadingComments(node));
		info.setSourceRangeEnd(node.getStartPosition() + node.getLength() - 1);
	}

	private static int getStartConsideringLeadingComments(ASTNode node) {
		int start = node.getStartPosition();
		var unit = domUnit(node);
		int index = unit.firstLeadingCommentIndex(node);
		if (index >= 0 && index <= unit.getCommentList().size()) {
			Comment comment = (Comment)unit.getCommentList().get(index);
			start = comment.getStartPosition();
		}
		return start;
	}

	private static org.eclipse.jdt.core.dom.CompilationUnit domUnit(ASTNode node) {
		while (node != null && !(node instanceof org.eclipse.jdt.core.dom.CompilationUnit)) {
			node = node.getParent();
		}
		return (org.eclipse.jdt.core.dom.CompilationUnit)node;
	}

	private static int toModelFlags(int domModifiers, boolean isDeprecated) {
		int res = 0;
		if (Modifier.isAbstract(domModifiers)) res |= Flags.AccAbstract;
		if (Modifier.isDefault(domModifiers)) res |= Flags.AccDefaultMethod;
		if (Modifier.isFinal(domModifiers)) res |= Flags.AccFinal;
		if (Modifier.isNative(domModifiers)) res |= Flags.AccNative;
		if (Modifier.isNonSealed(domModifiers)) res |= Flags.AccNonSealed;
		if (Modifier.isPrivate(domModifiers)) res |= Flags.AccPrivate;
		if (Modifier.isProtected(domModifiers)) res |= Flags.AccProtected;
		if (Modifier.isPublic(domModifiers)) res |= Flags.AccPublic;
		if (Modifier.isSealed(domModifiers)) res |= Flags.AccSealed;
		if (Modifier.isStatic(domModifiers)) res |= Flags.AccStatic;
		if (Modifier.isStrictfp(domModifiers)) res |= Flags.AccStrictfp;
		if (Modifier.isSynchronized(domModifiers)) res |= Flags.AccSynchronized;
		if (Modifier.isTransient(domModifiers)) res |= Flags.AccTransient;
		if (Modifier.isVolatile(domModifiers)) res |= Flags.AccVolatile;
		if (isDeprecated) res |= Flags.AccDeprecated;
		return res;
	}

	private Javadoc javadoc(ASTNode node) {
		if (node instanceof BodyDeclaration body && body.getJavadoc() != null) {
			return body.getJavadoc();
		}
		if (node instanceof ModuleDeclaration module && module.getJavadoc() != null) {
			return module.getJavadoc();
		}
		if (node instanceof TypeDeclaration type && type.getJavadoc() != null) {
			return type.getJavadoc();
		}
		if (node instanceof EnumDeclaration enumType && enumType.getJavadoc() != null) {
			return enumType.getJavadoc();
		}
		if (node instanceof FieldDeclaration field && field.getJavadoc() != null) {
			return field.getJavadoc();
		}
		org.eclipse.jdt.core.dom.CompilationUnit unit = domUnit(node);
		int commentIndex = unit.firstLeadingCommentIndex(node);
		if (commentIndex >= 0) {
			for (int i = commentIndex; i < unit.getCommentList().size(); i++) {
				Comment comment = (Comment)unit.getCommentList().get(i);
				if (comment.getStartPosition() > node.getStartPosition()) {
					return null;
				}
				if (comment instanceof Javadoc javadoc &&
					javadoc.getStartPosition() <= node.getStartPosition()) {
					return javadoc;
				}
			}
		}
		return null;
	}
}
