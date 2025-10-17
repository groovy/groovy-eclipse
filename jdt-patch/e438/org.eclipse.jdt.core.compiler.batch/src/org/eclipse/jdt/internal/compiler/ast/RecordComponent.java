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
import org.eclipse.jdt.internal.compiler.codegen.AnnotationContext;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.RecordComponentBinding;

public class RecordComponent extends AbstractVariableDeclaration {

	public RecordComponentBinding binding;

	public RecordComponent(char[] name, int sourceStart, int sourceEnd) {
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
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		if ((this.bits & IsReachable) == 0) {
			return;
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

	@Override
	public void getAllAnnotationContexts(int targetType, List<AnnotationContext> allAnnotationContexts) {
		AnnotationCollector collector = new AnnotationCollector(this, targetType, allAnnotationContexts);
		for (Annotation annotation : this.annotations) {
			annotation.traverse(collector, (BlockScope) null);
		}
	}

	// for record canonical constructor parameters
	@Override
	public void getAllAnnotationContexts(int targetType, int parameterIndex, List<AnnotationContext> allAnnotationContexts) {
		AnnotationCollector collector = new AnnotationCollector(this, targetType, parameterIndex, allAnnotationContexts);
		this.traverse(collector, (BlockScope) null);
	}

	@Override
	public boolean isVarArgs() {
		return this.type != null &&  (this.type.bits & IsVarArgs) != 0;
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
		}
		visitor.endVisit(this, scope);
	}

	@Override
	public RecordComponentBinding getBinding() {
		return this.binding;
	}

	@Override
	public void setBinding(Binding binding) {
		this.binding = (RecordComponentBinding) binding;
	}
}
