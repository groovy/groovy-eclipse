/*******************************************************************************
 * Copyright (c) 2007, 2023 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - derived base class from BatchMessagerImpl
 *
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.dispatch;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.apt.model.AnnotationMemberValue;
import org.eclipse.jdt.internal.compiler.apt.model.AnnotationMirrorImpl;
import org.eclipse.jdt.internal.compiler.apt.model.ExecutableElementImpl;
import org.eclipse.jdt.internal.compiler.apt.model.ModuleElementImpl;
import org.eclipse.jdt.internal.compiler.apt.model.TypeElementImpl;
import org.eclipse.jdt.internal.compiler.apt.model.VariableElementImpl;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.AptSourceLocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Util;

public class BaseMessagerImpl {

	static final String[] NO_ARGUMENTS = new String[0];

	/**
	 * Create a CategorizedProblem that can be reported to an ICompilerRequestor, etc.
	 *
	 * @param e the element against which to report the message.  If the element is not
	 * in the set of elements being compiled in the current round, the reference context
	 * and filename will be set to null.
	 * @return a new {@link AptProblem}
	 */
	public static AptProblem createProblem(Kind kind, CharSequence msg, Element e,
			AnnotationMirror a, AnnotationValue v) {
		ReferenceContext referenceContext = null;
		Annotation[] elementAnnotations = null;
		int startPosition = 0;
		int endPosition = 0;
		if (e != null) {
			switch(e.getKind()) {
				case MODULE:
					ModuleElementImpl moduleElementImpl = (ModuleElementImpl) e;
					Binding moduleBinding = moduleElementImpl._binding;
					if (moduleBinding instanceof SourceModuleBinding) {
						SourceModuleBinding sourceModuleBinding = (SourceModuleBinding) moduleBinding;
						CompilationUnitDeclaration unitDeclaration = (CompilationUnitDeclaration) sourceModuleBinding.scope.referenceContext();
						referenceContext = unitDeclaration;
						elementAnnotations = unitDeclaration.moduleDeclaration.annotations;
						startPosition = unitDeclaration.moduleDeclaration.sourceStart;
						endPosition = unitDeclaration.moduleDeclaration.sourceEnd;
					}
					break;
				case ANNOTATION_TYPE :
				case INTERFACE :
				case CLASS :
				case ENUM :
					TypeElementImpl typeElementImpl = (TypeElementImpl) e;
					Binding typeBinding = typeElementImpl._binding;
					if (typeBinding instanceof SourceTypeBinding) {
						SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) typeBinding;
						TypeDeclaration typeDeclaration = (TypeDeclaration) sourceTypeBinding.scope.referenceContext();
						referenceContext = typeDeclaration;
						elementAnnotations = typeDeclaration.annotations;
						startPosition = typeDeclaration.sourceStart;
						endPosition = typeDeclaration.sourceEnd;
					}
					break;
				case PACKAGE :
					// nothing to do: there is no reference context for a package
					break;
				case CONSTRUCTOR :
				case METHOD :
					ExecutableElementImpl executableElementImpl = (ExecutableElementImpl) e;
					Binding binding = executableElementImpl._binding;
					if (binding instanceof MethodBinding) {
						MethodBinding methodBinding = (MethodBinding) binding;
						AbstractMethodDeclaration sourceMethod = methodBinding.sourceMethod();
						if (sourceMethod != null) {
							referenceContext = sourceMethod;
							elementAnnotations = sourceMethod.annotations;
							startPosition = sourceMethod.sourceStart;
							endPosition = sourceMethod.sourceEnd;
						}
					}
					break;
				case ENUM_CONSTANT :
					break;
				case EXCEPTION_PARAMETER :
					break;
				case FIELD :
				case PARAMETER :
					VariableElementImpl variableElementImpl = (VariableElementImpl) e;
					binding = variableElementImpl._binding;
					if (binding instanceof FieldBinding) {
						FieldBinding fieldBinding = (FieldBinding) binding;
						FieldDeclaration fieldDeclaration = fieldBinding.sourceField();
						if (fieldDeclaration != null) {
							ReferenceBinding declaringClass = fieldBinding.declaringClass;
							if (declaringClass instanceof SourceTypeBinding) {
								SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) declaringClass;
								TypeDeclaration typeDeclaration = (TypeDeclaration) sourceTypeBinding.scope.referenceContext();
								referenceContext = typeDeclaration;
							}
							elementAnnotations = fieldDeclaration.annotations;
							startPosition = fieldDeclaration.sourceStart;
							endPosition = fieldDeclaration.sourceEnd;
						}
					} else if (binding instanceof AptSourceLocalVariableBinding){
						AptSourceLocalVariableBinding parameterBinding = (AptSourceLocalVariableBinding) binding;
						LocalDeclaration parameterDeclaration = parameterBinding.declaration;
						if (parameterDeclaration != null) {
							MethodBinding methodBinding = parameterBinding.methodBinding;
							if (methodBinding != null) {
								referenceContext = methodBinding.sourceMethod();
							}
							elementAnnotations = parameterDeclaration.annotations;
							startPosition = parameterDeclaration.sourceStart;
							endPosition = parameterDeclaration.sourceEnd;
						}
					}
					break;
				case INSTANCE_INIT :
				case STATIC_INIT :
					break;
				case LOCAL_VARIABLE :
					break;
				case TYPE_PARAMETER :
				default:
					break;
			}
		}
		StringBuilder builder = new StringBuilder();
		if (msg != null) {
			builder.append(msg);
		}
		if (a != null && elementAnnotations != null) {
			AnnotationBinding annotationBinding = ((AnnotationMirrorImpl) a)._binding;
			Annotation annotation = findAnnotation(elementAnnotations, annotationBinding);
			if (annotation != null) {
				startPosition = annotation.sourceStart;
				endPosition = annotation.sourceEnd;
				if (v != null && v instanceof AnnotationMemberValue) {
					MethodBinding methodBinding = ((AnnotationMemberValue) v).getMethodBinding();
					MemberValuePair[] memberValuePairs = annotation.memberValuePairs();
					MemberValuePair memberValuePair = null;
					for (int i = 0; memberValuePair == null && i < memberValuePairs.length; i++) {
						if (methodBinding == memberValuePairs[i].binding) {
							memberValuePair = memberValuePairs[i];
						}
					}
					if (memberValuePair != null) {
						startPosition = memberValuePair.sourceStart;
						endPosition = memberValuePair.sourceEnd;
					}
				}
			}
		}
		int lineNumber = 0;
		int columnNumber = 1;
		char[] fileName = null;
		if (referenceContext != null) {
			CompilationResult result = referenceContext.compilationResult();
			fileName = result.fileName;
			int[] lineEnds = null;
			lineNumber = startPosition >= 0
					? Util.getLineNumber(startPosition, lineEnds = result.getLineSeparatorPositions(), 0, lineEnds.length-1)
					: 0;
			columnNumber = startPosition >= 0
					? Util.searchColumnNumber(result.getLineSeparatorPositions(), lineNumber,startPosition)
					: 0;
		}
		int severity;
		switch(kind) {
			case ERROR :
				severity = ProblemSeverities.Error;
				break;
			case NOTE :
			case OTHER:
				severity = ProblemSeverities.Info;
				break;
			default :
				severity = ProblemSeverities.Warning;
				break;
		}
		return new AptProblem(
				referenceContext,
				fileName,
				String.valueOf(builder),
				0,
				NO_ARGUMENTS,
				severity,
				startPosition,
				endPosition,
				lineNumber,
				columnNumber);
	}

	private static Annotation findAnnotation(Annotation[] elementAnnotations, AnnotationBinding annotationBinding) {
		for (Annotation elementAnnotation : elementAnnotations) {
			Annotation annotation = findAnnotation(elementAnnotation, annotationBinding);
			if (annotation != null) {
				return annotation;
			}
		}
		return null;
	}

	private static Annotation findAnnotation(Annotation elementAnnotation, AnnotationBinding annotationBinding) {
		if (annotationBinding == elementAnnotation.getCompilerAnnotation()) {
			return elementAnnotation;
		}

		MemberValuePair[] memberValuePairs = elementAnnotation.memberValuePairs();
		for (MemberValuePair mvp : memberValuePairs) {
			Expression v = mvp.value;
			if (v instanceof Annotation) {
				Annotation a = findAnnotation((Annotation) v, annotationBinding);
				if (a != null) {
					return a;
				}
			} else if (v instanceof ArrayInitializer) {
				Expression[] expressions = ((ArrayInitializer) v).expressions;
				for (Expression e : expressions) {
					if (e instanceof Annotation) {
						Annotation a = findAnnotation((Annotation) e, annotationBinding);
						if (a != null) {
							return a;
						}
					}
				}
			}
		}
		return null;
	}

	public BaseMessagerImpl() {
		super();
	}

}
