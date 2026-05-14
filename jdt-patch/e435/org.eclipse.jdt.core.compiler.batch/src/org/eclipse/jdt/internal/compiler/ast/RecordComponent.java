/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler.ast;

import java.util.List;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.TypeReference.AnnotationCollector;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.AnnotationContext;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.RecordComponentBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class RecordComponent extends AbstractVariableDeclaration {

	public RecordComponentBinding binding;

	public RecordComponent(
		char[] name,
		int sourceStart,
		int sourceEnd) {

		this.name = name;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
		this.declarationEnd = sourceEnd;
	}
	public RecordComponent(char[] name, long posNom, TypeReference tr, int modifiers) {
		this(name, (int) (posNom >>> 32), (int) posNom);
		this.declarationSourceEnd = (int) posNom;
		this.modifiers = modifiers;
		this.type = tr;
		if (tr != null) {
			this.bits |= (tr.bits & ASTNode.HasTypeAnnotations);
		}
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		//TODO: Add error checking if relevant.
		return flowInfo;
	}

	public void checkModifiers() {

		//only potential valid modifier is <<final>>
		if (((this.modifiers & ExtraCompilerModifiers.AccJustFlag) & ~ClassFileConstants.AccFinal) != 0)
			//AccModifierProblem -> other (non-visibility problem)
			//AccAlternateModifierProblem -> duplicate modifier
			//AccModifierProblem | AccAlternateModifierProblem -> visibility problem"

			this.modifiers = (this.modifiers & ~ExtraCompilerModifiers.AccAlternateModifierProblem) | ExtraCompilerModifiers.AccModifierProblem;
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((this.bits & IsReachable) == 0) {
			return; // TODO: can this ever happen?
		}
		codeStream.recordPositionsFrom(codeStream.position, this.sourceStart);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration#getKind()
	 */
	@Override
	public int getKind() {
		return RECORD_COMPONENT;
	}

	public void getAllAnnotationContexts(int targetType, List<AnnotationContext> allAnnotationContexts) {
		AnnotationCollector collector = new AnnotationCollector(this, targetType, allAnnotationContexts);
		for (Annotation annotation : this.annotations) {
			annotation.traverse(collector, (BlockScope) null);
		}
	}

	public boolean isVarArgs() {
		return this.type != null &&  (this.type.bits & IsVarArgs) != 0;
	}

	@Override
	public void resolve(BlockScope scope) {
		resolveAnnotations(scope, this.annotations, this.binding);
		// Check if this declaration should now have the type annotations bit set
		if (this.annotations != null) {
			for (Annotation annotation : this.annotations) {
				TypeBinding resolvedAnnotationType = annotation.resolvedType;
				if (resolvedAnnotationType != null && (resolvedAnnotationType.getAnnotationTagBits() & TagBits.AnnotationForTypeUse) != 0) {
					this.bits |= ASTNode.HasTypeAnnotations;
					// also update the accessor's return type:
					if (this.binding != null && this.binding.declaringRecord != null) {
						for (MethodBinding methodBinding : this.binding.declaringRecord.methods()) {
							if (methodBinding instanceof SyntheticMethodBinding) {
								SyntheticMethodBinding smb = (SyntheticMethodBinding) methodBinding;
								if (smb.purpose == SyntheticMethodBinding.FieldReadAccess && smb.recordComponentBinding == this.binding) {
									smb.returnType = this.binding.type;
									break;
								}
							}
						}
					}
					break;
				}
			}
		}
		// check @Deprecated annotation presence - Mostly in the future :)
//		if ((this.binding.getAnnotationTagBits() & TagBits.AnnotationDeprecated) == 0
//				&& (this.binding.modifiers & ClassFileConstants.AccDeprecated) != 0
//				&& scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK14) {
//			scope.problemReporter().missingDeprecatedAnnotationForRecordComponent(this);
//		}
	}
	// TODO: check when to call/relevance?
	void validateNullAnnotations(BlockScope scope) {
		if (!scope.validateNullAnnotation(this.binding.tagBits, this.type, this.annotations))
			this.binding.tagBits &= ~TagBits.AnnotationNullMASK;
	}

	@Override
	public StringBuilder print(int indent, StringBuilder output) {

		printIndent(indent, output);
		printModifiers(this.modifiers, output);
		if (this.annotations != null) {
			printAnnotations(this.annotations, output);
			output.append(' ');
		}

		if (this.type == null) {
			output.append("<no type> "); //$NON-NLS-1$
		} else {
			this.type.print(0, output).append(' ');
		}
		return output.append(this.name);
	}

	@Override
	public StringBuilder printStatement(int indent, StringBuilder output) {

		return print(indent, output).append(';');
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (this.annotations != null) {
				int annotationsLength = this.annotations.length;
				for (int i = 0; i < annotationsLength; i++)
					this.annotations[i].traverse(visitor, scope);
			}
			this.type.traverse(visitor, scope);
			if (this.initialization != null)
				this.initialization.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}

}
