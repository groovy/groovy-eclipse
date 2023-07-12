/*******************************************************************************
 * Copyright (c) 2006, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.dispatch;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.apt.model.ElementImpl;
import org.eclipse.jdt.internal.compiler.apt.model.Factory;
import org.eclipse.jdt.internal.compiler.apt.util.ManyToMany;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ModuleDeclaration;
import org.eclipse.jdt.internal.compiler.ast.RecordComponent;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.AptSourceLocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.RecordComponentBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/**
 * This class is used to visit the JDT compiler internal AST to discover annotations,
 * in the course of dispatching to annotation processors.
 */
public class AnnotationDiscoveryVisitor extends ASTVisitor {
	final BaseProcessingEnvImpl _env;
	final Factory _factory;
	/**
	 * Collects a many-to-many map of annotation types to
	 * the elements they appear on.
	 */
	final ManyToMany<TypeElement, Element> _annoToElement;

	public AnnotationDiscoveryVisitor(BaseProcessingEnvImpl env) {
		this._env = env;
		this._factory = env.getFactory();
		this._annoToElement = new ManyToMany<>();
	}

	@Override
	public boolean visit(Argument argument, BlockScope scope) {
		Annotation[] annotations = argument.annotations;
		ReferenceContext referenceContext = scope.referenceContext();
		if (referenceContext instanceof AbstractMethodDeclaration) {
			MethodBinding binding = ((AbstractMethodDeclaration) referenceContext).binding;
			if (binding != null) {
				TypeDeclaration typeDeclaration = scope.referenceType();
				typeDeclaration.binding.resolveTypesFor(binding);
				if (argument.binding != null) {
					argument.binding = new AptSourceLocalVariableBinding(argument.binding, binding);
				}
			}
			if (annotations != null && argument.binding != null) {
				this.resolveAnnotations(
						scope,
						annotations,
						argument.binding);
			}
		}
		return false;
	}

	@Override
	public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
		Annotation[] annotations = constructorDeclaration.annotations;
		if (annotations != null) {
			MethodBinding constructorBinding = constructorDeclaration.binding;
			if (constructorBinding == null) {
				return false;
			}
			((SourceTypeBinding) constructorBinding.declaringClass).resolveTypesFor(constructorBinding);
			this.resolveAnnotations(
					constructorDeclaration.scope,
					annotations,
					constructorBinding);
		}

		TypeParameter[] typeParameters = constructorDeclaration.typeParameters;
		if (typeParameters != null) {
			int typeParametersLength = typeParameters.length;
			for (int i = 0; i < typeParametersLength; i++) {
				typeParameters[i].traverse(this, constructorDeclaration.scope);
			}
		}

		Argument[] arguments = constructorDeclaration.arguments;
		if (arguments != null) {
			int argumentLength = arguments.length;
			for (int i = 0; i < argumentLength; i++) {
				arguments[i].traverse(this, constructorDeclaration.scope);
			}
		}
		return false;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		Annotation[] annotations = fieldDeclaration.annotations;
		if (annotations != null) {
			FieldBinding fieldBinding = fieldDeclaration.binding;
			if (fieldBinding == null) {
				return false;
			}
			((SourceTypeBinding) fieldBinding.declaringClass).resolveTypeFor(fieldBinding);
			if (fieldDeclaration.binding == null) {
				return false;
			}
			this.resolveAnnotations(scope, annotations, fieldBinding);
		}
		return false;
	}

	@Override
	public boolean visit(RecordComponent recordComponent, BlockScope scope) {
		Annotation[] annotations = recordComponent.annotations;
		if (annotations != null) {
			RecordComponentBinding recordComponentBinding = recordComponent.binding;
			if (recordComponentBinding == null) {
				return false;
			}
			((SourceTypeBinding) recordComponentBinding.declaringRecord).resolveTypeFor(recordComponentBinding);
			if (recordComponent.binding == null) {
				return false;
			}
			this.resolveAnnotations(scope, annotations, recordComponentBinding);
		}
		return false;
	}
	@Override
	public boolean visit(TypeParameter typeParameter, ClassScope scope) {
		Annotation[] annotations = typeParameter.annotations;
		if (annotations != null) {
			TypeVariableBinding binding = typeParameter.binding;
			if (binding == null) {
				return false;
			}
			this.resolveAnnotations(scope.referenceContext.initializerScope, annotations, binding);
		}
		return false;
	}

	@Override
	public boolean visit(TypeParameter typeParameter, BlockScope scope) {
		Annotation[] annotations = typeParameter.annotations;
		if (annotations != null) {
			TypeVariableBinding binding = typeParameter.binding;
			if (binding == null) {
				return false;
			}
			// when we get here, it is guaranteed that class type parameters are connected, but method type parameters may not be.
			MethodBinding methodBinding = (MethodBinding) binding.declaringElement;
			((SourceTypeBinding) methodBinding.declaringClass).resolveTypesFor(methodBinding);
			this.resolveAnnotations(scope, annotations, binding);
		}
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		Annotation[] annotations = methodDeclaration.annotations;
		if (annotations != null) {
			MethodBinding methodBinding = methodDeclaration.binding;
			if (methodBinding == null) {
				return false;
			}
			((SourceTypeBinding) methodBinding.declaringClass).resolveTypesFor(methodBinding);
			this.resolveAnnotations(
					methodDeclaration.scope,
					annotations,
					methodBinding);
		}

		TypeParameter[] typeParameters = methodDeclaration.typeParameters;
		if (typeParameters != null) {
			int typeParametersLength = typeParameters.length;
			for (int i = 0; i < typeParametersLength; i++) {
				typeParameters[i].traverse(this, methodDeclaration.scope);
			}
		}

		Argument[] arguments = methodDeclaration.arguments;
		if (arguments != null) {
			int argumentLength = arguments.length;
			for (int i = 0; i < argumentLength; i++) {
				arguments[i].traverse(this, methodDeclaration.scope);
			}
		}
		return false;
	}

	@Override
	public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
		SourceTypeBinding binding = memberTypeDeclaration.binding;
		if (binding == null) {
			return false;
		}
		Annotation[] annotations = memberTypeDeclaration.annotations;
		if (annotations != null) {
			this.resolveAnnotations(
					memberTypeDeclaration.staticInitializerScope,
					annotations,
					binding);
		}
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
		SourceTypeBinding binding = typeDeclaration.binding;
		if (binding == null) {
			return false;
		}
		Annotation[] annotations = typeDeclaration.annotations;
		if (annotations != null) {
			this.resolveAnnotations(
					typeDeclaration.staticInitializerScope,
					annotations,
					binding);
		}
		return true;
	}
	@Override
	public boolean visit(ModuleDeclaration module, CompilationUnitScope scope) {
		ModuleBinding binding = module.binding;
		if (binding == null) {
			return false;
		}
		//module.resolveTypeDirectives(scope);
		// The above call also resolvesAnnotations <=== actually this doesn't populate _annoToElement which is what we need

		Annotation[] annotations = module.annotations;
		if (annotations != null) {
			this.resolveAnnotations(module.scope, 
					annotations, 
					binding);
		}
		return true;
	}

	private void resolveAnnotations(BlockScope scope, Annotation[] annotations, Binding currentBinding) {

		int length = annotations == null ? 0 : annotations.length;
		if (length == 0)
			return;

		boolean old = scope.insideTypeAnnotation;
		scope.insideTypeAnnotation = true;
		currentBinding.getAnnotationTagBits();
		scope.insideTypeAnnotation = old;
		ElementImpl element = (ElementImpl) this._factory.newElement(currentBinding);
		AnnotationBinding [] annotationBindings = element.getPackedAnnotationBindings(); // discovery is never in terms of repeating annotation.
		for (AnnotationBinding binding : annotationBindings) {
			ReferenceBinding annotationType = binding.getAnnotationType();
			if (Annotation.isAnnotationTargetAllowed(scope, annotationType, currentBinding)
					) { // binding should be resolved, but in case it's not, ignore it: it could have been wrapped into a container.
				TypeElement anno = (TypeElement)this._factory.newElement(annotationType);
				this._annoToElement.put(anno, element);
			}
		}
	}
}
