/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public abstract class Reference extends Expression  {
/**
 * BaseLevelReference constructor comment.
 */
public Reference() {
	super();
}
public abstract FlowInfo analyseAssignment(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, Assignment assignment, boolean isCompound);

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	return flowInfo;
}

public FieldBinding fieldBinding() {
	//this method should be sent one FIELD-tagged references
	//  (ref.bits & BindingIds.FIELD != 0)()
	return null ;
}

public void fieldStore(Scope currentScope, CodeStream codeStream, FieldBinding fieldBinding, MethodBinding syntheticWriteAccessor, TypeBinding receiverType, boolean isImplicitThisReceiver, boolean valueRequired) {
	int pc = codeStream.position;
	if (fieldBinding.isStatic()) {
		if (valueRequired) {
			switch (fieldBinding.type.id) {
				case TypeIds.T_long :
				case TypeIds.T_double :
					codeStream.dup2();
					break;
				default : 
					codeStream.dup();
					break;
			}
		}
		if (syntheticWriteAccessor == null) {
			TypeBinding constantPoolDeclaringClass = CodeStream.getConstantPoolDeclaringClass(currentScope, fieldBinding, receiverType, isImplicitThisReceiver);
			codeStream.fieldAccess(Opcodes.OPC_putstatic, fieldBinding, constantPoolDeclaringClass);
		} else {
			codeStream.invoke(Opcodes.OPC_invokestatic, syntheticWriteAccessor, null /* default declaringClass */);
		}
	} else { // Stack:  [owner][new field value]  ---> [new field value][owner][new field value]
		if (valueRequired) {
			switch (fieldBinding.type.id) {
				case TypeIds.T_long :
				case TypeIds.T_double :
					codeStream.dup2_x1();
					break;
				default : 
					codeStream.dup_x1();
					break;
			}
		}
		if (syntheticWriteAccessor == null) {
			TypeBinding constantPoolDeclaringClass = CodeStream.getConstantPoolDeclaringClass(currentScope, fieldBinding, receiverType, isImplicitThisReceiver);
			codeStream.fieldAccess(Opcodes.OPC_putfield, fieldBinding, constantPoolDeclaringClass);
		} else {
			codeStream.invoke(Opcodes.OPC_invokestatic, syntheticWriteAccessor, null /* default declaringClass */);
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

public abstract void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired);

public abstract void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired);

public abstract void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired);
}
