/*******************************************************************************
* Copyright (c) 2024 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/

package org.eclipse.jdt.internal.compiler.codegen;

import java.util.Stack;
import java.util.function.Supplier;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class OperandStack {

	enum OperandCategory {
		ONE, TWO;
	}

	private Stack<TypeBinding> stack;
	private ClassFile classFile;

	public OperandStack() {}

	public OperandStack(ClassFile classFile) {
		this.stack = new Stack<>();
		this.classFile = classFile;
	}

	@SuppressWarnings("unchecked")
	private OperandStack(OperandStack operandStack) {
		this.stack = (Stack<TypeBinding>) operandStack.stack.clone();
		this.classFile = operandStack.classFile;
	}

	protected OperandStack copy() {
		return new OperandStack(this);
	}

	public void push(TypeBinding typeBinding) {
		if (typeBinding == null) {
			throw new AssertionError("Attempt to push null on operand stack!"); //$NON-NLS-1$
		}
		/* 4.9.2 Structural Constraints: ...
		   An instruction operating on values of type int is also permitted to operate on
		   values of type boolean, byte, char, and short.
		   As noted in §2.3.4 and §2.11.1, the Java Virtual Machine internally converts values of
		   types boolean, byte, short, and char to type int.)
		*/
		this.stack.push(switch(typeBinding.id) {
			case TypeIds.T_boolean, TypeIds.T_byte, TypeIds.T_short, TypeIds.T_char -> TypeBinding.INT;
			default -> typeBinding.erasure();
		});
	}

	public void push(int localSlot) {
		if (localSlot >= this.classFile.codeStream.maxLocals) {
			throw new AssertionError("Unexpected resolved position"); //$NON-NLS-1$
		}
		TypeBinding type = this.classFile.codeStream.retrieveLocalType(this.classFile.codeStream.position, localSlot);
		push(type);
	}

	public void push(char[] typeName) {
		Scope scope = this.classFile.referenceBinding.scope;
		Supplier<ReferenceBinding> finder = scope.getCommonReferenceBinding(typeName);
		TypeBinding type = finder != null ? finder.get() : TypeBinding.NULL;
		push(type);
	}

	public TypeBinding pop() {
		return this.stack.pop();
	}

	public void pop(int nSlots) {
		for (int i = 0; i < nSlots;) {
			TypeBinding t = pop();
			i += TypeIds.getCategory(t.id);
			if (i > nSlots)
				throw new AssertionError("Popped one too many words from operand stack!"); //$NON-NLS-1$
		}
	}

	public TypeBinding pop(OperandCategory category) {
		TypeBinding t = pop();
		if (TypeIds.getCategory(t.id) != switch (category) {
						case ONE -> 1;
						case TWO -> 2;
					}) {
			throw new AssertionError("Unexpected operand at stack top"); //$NON-NLS-1$
		}
		return t;
	}

	public TypeBinding pop(TypeBinding top) {
		if (TypeBinding.equalsEquals(top, pop()))
			return top;
		throw new AssertionError("Unexpected operand at stack top"); //$NON-NLS-1$
	}

	public void pop2() {
		TypeBinding v1 = pop();
		if (TypeIds.getCategory(v1.id) == 1) {
			v1 = pop();
			if (TypeIds.getCategory(v1.id) != 1)
				throw new AssertionError("pop2 on mixed operand types"); //$NON-NLS-1$
		}
	}

	public TypeBinding peek() {
		return this.stack.peek();
	}

	public TypeBinding peek(TypeBinding top) {
		if (TypeBinding.equalsEquals(top, peek()))
			return top;
		throw new AssertionError("Unexpected operand at stack top"); //$NON-NLS-1$
	}

	public TypeBinding get(int index) {
		return this.stack.get(index);
	}

	public int size() {
		return this.stack.size();
	}

	public void clear() {
		this.stack.clear();
	}

	public boolean depthEquals(int expected) {
		int depth = 0;
		for (int i = 0, size = size(); i < size; i++) {
			TypeBinding t = get(i);
			depth += TypeIds.getCategory(t.id);
		}
		return depth == expected;
	}

	public void xaload() { // [... arrayref, index] -> [... element]
		pop(TypeBinding.INT);
		push(((ArrayBinding) pop()).elementsType());
	}

	public void xastore() { // ..., arrayref, index, value → ...
		TypeBinding valueType = pop();
		pop(TypeBinding.INT);
		TypeBinding elementType = ((ArrayBinding) pop()).elementsType();
		boolean wellFormed = switch (elementType.id) {
			case TypeIds.T_boolean, TypeIds.T_byte, TypeIds.T_short, TypeIds.T_char -> TypeBinding.equalsEquals(valueType, TypeBinding.INT);
			default -> valueType.isCompatibleWith(elementType);
		};
		if (!wellFormed)
			throw new AssertionError("array store with invalid types"); //$NON-NLS-1$
	}

	public void dup2() {
		TypeBinding val1 = pop();
		if (TypeIds.getCategory(val1.id) == 2) { // ..., value → ..., value, value
			push(val1);
			push(val1);
		} else {
			TypeBinding val2 = pop();
			if (TypeIds.getCategory(val2.id) != 1)
				throw new AssertionError("dup2 on mixed operand types"); //$NON-NLS-1$
			// ..., value2, value1 → ..., value2, value1, value2, value1
			push(val2);
			push(val1);
			push(val2);
			push(val1);
		}
	}

	public void dup_x1() { // ..., value2, value1 → ..., value1, value2, value1
		TypeBinding[] topStack = { pop(OperandCategory.ONE), pop(OperandCategory.ONE) };
		push(topStack[0]);
		push(topStack[1]);
		push(topStack[0]);
	}

	public void dup_x2() {
		TypeBinding val1 = pop(OperandCategory.ONE);
		TypeBinding val2 = pop();
		if (TypeIds.getCategory(val2.id) == 2) { // Form 2: ..., value2, value1 → ..., value1, value2, value1
			push(val1);
			push(val2);
			push(val1);
		} else { // Form 1: ..., value3, value2, value1 → ..., value1, value3, value2, value1
			TypeBinding val3 = pop(OperandCategory.ONE);
			push(val1);
			push(val3);
			push(val2);
			push(val1);
		}
	}

	public void dup2_x1() {
		TypeBinding val1 = pop();
		if (TypeIds.getCategory(val1.id) == 2) { // Form 2: ..., value2, value1 → ..., value1, value2, value1
			TypeBinding val2 = pop(OperandCategory.ONE);
			push(val1);
			push(val2);
			push(val1);
		} else { // Form 1: ..., value3, value2, value1 → ..., value2, value1, value3, value2, value1
			TypeBinding val2 = pop(OperandCategory.ONE);
			TypeBinding val3 = pop(OperandCategory.ONE);
			push(val2);
			push(val1);
			push(val3);
			push(val2);
			push(val1);
		}
	}

	public void dup2_x2() {
		TypeBinding val1 = pop();
		if (TypeIds.getCategory(val1.id) == 2) {
			TypeBinding val2 = pop();
			if (TypeIds.getCategory(val2.id) == 2) { // Form 4: ..., value2, value1 → ..., value1, value2, value1
				push(val1);
				push(val2);
				push(val1);
			} else {
				// Form 2: ..., value3, value2, value1 → ..., value1, value3, value2, value1
				TypeBinding val3 = pop(OperandCategory.ONE);
				push(val1);
				push(val3);
				push(val2);
				push(val1);
			}
		} else { // val1 category 1
			TypeBinding val2 = pop(OperandCategory.ONE);
			TypeBinding val3 = pop();
			if (TypeIds.getCategory(val3.id) == 2) { // Form 3: ..., value3, value2, value1 → ..., value2, value1, value3, value2, value1
				push(val2);
				push(val1);
				push(val3);
				push(val2);
				push(val1);
			} else {
				// Form 1: ..., value4, value3, value2, value1 → ..., value2, value1, value4, value3, value2, value1
				TypeBinding val4 = pop(OperandCategory.ONE);
				push(val2);
				push(val1);
				push(val4);
				push(val3);
				push(val2);
				push(val1);
			}
		}
	}

	@Override
	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0, length = size(); i < length; i++) {
        	if (i != 0)
        		sb.append(", "); //$NON-NLS-1$
        	TypeBinding type = this.stack.get(i);
        	sb.append(type.shortReadableName());
        }
        sb.append("]\n"); //$NON-NLS-1$
        return sb.toString();
	}

	public static class NullStack extends OperandStack {

		public NullStack() {
			return;
		}

		@Override
		protected NullStack copy()   {
			return new NullStack();
		}

		@Override
		public void push(TypeBinding typeBinding) {
			return;
		}

		@Override
		public void push(int localSlot) {
			return;
		}

		@Override
		public void push(char[] typeName) {
			return;
		}

		@Override
		public TypeBinding pop() {
			return TypeBinding.VOID;
		}

		@Override
		public void pop(int nSlots) {
			return;
		}

		@Override
		public TypeBinding pop(OperandCategory category) {
			return TypeBinding.VOID;
		}

		@Override
		public TypeBinding pop(TypeBinding top) {
			return TypeBinding.VOID;
		}

		@Override
		public void pop2() {
			return;
		}

		@Override
		public TypeBinding peek() {
			return TypeBinding.VOID;
		}

		@Override
		public TypeBinding peek(TypeBinding top) {
			return TypeBinding.VOID;
		}

		@Override
		public TypeBinding get(int index) {
			return TypeBinding.VOID;
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public void clear() {
			return;
		}

		@Override
		public boolean depthEquals(int expected) {
			return true;
		}

		@Override
		public void xaload() { // [... arrayref, index] -> [... element]
			return;
		}

		@Override
		public void xastore() { // ..., arrayref, index, value → ...
			return;
		}

		@Override
		public void dup2() {
			return;
		}

		@Override
		public void dup_x1() {
			return;
		}

		@Override
		public void dup_x2() {
			return;
		}

		@Override
		public void dup2_x1() {
			return;
		}

		@Override
		public void dup2_x2() {
			return;
		}
	}
}
