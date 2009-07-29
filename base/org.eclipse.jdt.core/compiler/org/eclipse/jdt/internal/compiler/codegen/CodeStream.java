/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.codegen;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.flow.UnconditionalFlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
import org.eclipse.jdt.internal.compiler.util.Util;

public class CodeStream {
	public static final boolean DEBUG = false;
	
	// It will be responsible for the following items.
	// -> Tracking Max Stack.

	public static FieldBinding[] ImplicitThis = new FieldBinding[] {};
	public static final int LABELS_INCREMENT = 5;
	// local variable attributes output
	public static final int LOCALS_INCREMENT = 10;
	static ExceptionLabel[] noExceptionHandlers = new ExceptionLabel[LABELS_INCREMENT];
	static BranchLabel[] noLabels = new BranchLabel[LABELS_INCREMENT];
	static LocalVariableBinding[] noLocals = new LocalVariableBinding[LOCALS_INCREMENT];
	static LocalVariableBinding[] noVisibleLocals = new LocalVariableBinding[LOCALS_INCREMENT];
	public static final CompilationResult RESTART_IN_WIDE_MODE = new CompilationResult((char[])null, 0, 0, 0);
	/**
	 * This methods searches for an existing entry inside the pcToSourceMap table with a pc equals to @pc.
	 * If there is an existing entry it returns -1 (no insertion required).
	 * Otherwise it returns the index where the entry for the pc has to be inserted.
	 * This is based on the fact that the pcToSourceMap table is sorted according to the pc.
	 *
	 * @param pcToSourceMap the given pcToSourceMap array
	 * @param length the given length
	 * @param pc the given pc
	 * @return int
	 */
	public static int insertionIndex(int[] pcToSourceMap, int length, int pc) {
		int g = 0;
		int d = length - 2;
		int m = 0;
		while (g <= d) {
			m = (g + d) / 2;
			// we search only on even indexes
			if ((m & 1) != 0) // faster than ((m % 2) != 0)
				m--;
			int currentPC = pcToSourceMap[m];
			if (pc < currentPC) {
				d = m - 2;
			} else
				if (pc > currentPC) {
					g = m + 2;
				} else {
					return -1;
				}
		}
		if (pc < pcToSourceMap[m])
			return m;
		return m + 2;
	}
	public static final void sort(int[] tab, int lo0, int hi0, int[] result) {
		int lo = lo0;
		int hi = hi0;
		int mid;
		if (hi0 > lo0) {
			/* Arbitrarily establishing partition element as the midpoint of
			  * the array.
			  */
			mid = tab[lo0 + (hi0 - lo0) / 2];
			// loop through the array until indices cross
			while (lo <= hi) {
				/* find the first element that is greater than or equal to 
				 * the partition element starting from the left Index.
				 */
				while ((lo < hi0) && (tab[lo] < mid))
					++lo;
				/* find an element that is smaller than or equal to 
				 * the partition element starting from the right Index.
				 */
				while ((hi > lo0) && (tab[hi] > mid))
					--hi;
				// if the indexes have not crossed, swap
				if (lo <= hi) {
					swap(tab, lo, hi, result);
					++lo;
					--hi;
				}
			}
			/* If the right index has not reached the left side of array
			  * must now sort the left partition.
			  */
			if (lo0 < hi)
				sort(tab, lo0, hi, result);
			/* If the left index has not reached the right side of array
			  * must now sort the right partition.
			  */
			if (lo < hi0)
				sort(tab, lo, hi0, result);
		}
	}
	private static final void swap(int a[], int i, int j, int result[]) {
		int T;
		T = a[i];
		a[i] = a[j];
		a[j] = T;
		T = result[j];
		result[j] = result[i];
		result[i] = T;
	}
	public int allLocalsCounter;
	public byte[] bCodeStream;
	public ClassFile classFile; // The current classfile it is associated to.
	public int classFileOffset;
	public ConstantPool constantPool; // The constant pool used to generate bytecodes that need to store information into the constant pool
	public int countLabels;
	public ExceptionLabel[] exceptionLabels = new ExceptionLabel[LABELS_INCREMENT];
	public int exceptionLabelsCounter;
	public int generateAttributes;
	// store all the labels placed at the current position to be able to optimize
	// a jump to the next bytecode.
	static final int L_UNKNOWN = 0, L_OPTIMIZABLE = 2, L_CANNOT_OPTIMIZE = 4;	
	public BranchLabel[] labels = new BranchLabel[LABELS_INCREMENT];
	public int lastEntryPC; // last entry recorded
	public int lastAbruptCompletion; // position of last instruction which abrupts completion: goto/return/athrow
	public int[] lineSeparatorPositions;
	// line number of the body start and the body end
	public int lineNumberStart;
	public int lineNumberEnd;
	
	public LocalVariableBinding[] locals = new LocalVariableBinding[LOCALS_INCREMENT];
	public int maxFieldCount;
	
	public int maxLocals;
	public AbstractMethodDeclaration methodDeclaration;
	public int[] pcToSourceMap = new int[24];
	public int pcToSourceMapSize;
	public int position; // So when first set can be incremented
	public boolean preserveUnusedLocals;
	public int stackDepth; // Use Ints to keep from using extra bc when adding
	public int stackMax; // Use Ints to keep from using extra bc when adding
	public int startingClassFileOffset; // I need to keep the starting point inside the byte array
	
	// target level to manage different code generation between different target levels
	protected long targetLevel;
	
public LocalVariableBinding[] visibleLocals = new LocalVariableBinding[LOCALS_INCREMENT];
int visibleLocalsCount;
// to handle goto_w
public boolean wideMode = false;
public CodeStream(ClassFile givenClassFile) {
	this.targetLevel = givenClassFile.targetJDK;
	this.generateAttributes = givenClassFile.produceAttributes;
	if ((givenClassFile.produceAttributes & ClassFileConstants.ATTR_LINES) != 0) {
		this.lineSeparatorPositions = givenClassFile.referenceBinding.scope.referenceCompilationUnit().compilationResult.getLineSeparatorPositions();
	}
}
public void aaload() {
	if (DEBUG) System.out.println(position + "\t\taaload"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_aaload;
}
public void aastore() {
	if (DEBUG) System.out.println(position + "\t\taastore"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_aastore;
}
public void aconst_null() {
	if (DEBUG) System.out.println(position + "\t\taconst_null"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax) {
		stackMax = stackDepth;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_aconst_null;
}
public void addDefinitelyAssignedVariables(Scope scope, int initStateIndex) {
	// Required to fix 1PR0XVS: LFRE:WINNT - Compiler: variable table for method appears incorrect
	if ((this.generateAttributes & (ClassFileConstants.ATTR_VARS
			| ClassFileConstants.ATTR_STACK_MAP_TABLE
			| ClassFileConstants.ATTR_STACK_MAP)) == 0)
		return;
	for (int i = 0; i < visibleLocalsCount; i++) {
		LocalVariableBinding localBinding = visibleLocals[i];
		if (localBinding != null) {
			// Check if the local is definitely assigned
			if (isDefinitelyAssigned(scope, initStateIndex, localBinding)) {
				if ((localBinding.initializationCount == 0) || (localBinding.initializationPCs[((localBinding.initializationCount - 1) << 1) + 1] != -1)) {
					/* There are two cases:
					 * 1) there is no initialization interval opened ==> add an opened interval
					 * 2) there is already some initialization intervals but the last one is closed ==> add an opened interval
					 * An opened interval means that the value at localBinding.initializationPCs[localBinding.initializationCount - 1][1]
					 * is equals to -1.
					 * initializationPCs is a collection of pairs of int:
					 * 	first value is the startPC and second value is the endPC. -1 one for the last value means that the interval
					 * 	is not closed yet.
					 */
					localBinding.recordInitializationStartPC(position);
				}
			}
		}
	}
}
public void addLabel(BranchLabel aLabel) {
	if (countLabels == labels.length)
		System.arraycopy(labels, 0, labels = new BranchLabel[countLabels + LABELS_INCREMENT], 0, countLabels);
	labels[countLabels++] = aLabel;
}
public void addVisibleLocalVariable(LocalVariableBinding localBinding) {
	if ((this.generateAttributes & (ClassFileConstants.ATTR_VARS
			| ClassFileConstants.ATTR_STACK_MAP_TABLE
			| ClassFileConstants.ATTR_STACK_MAP)) == 0)
		return;

	if (visibleLocalsCount >= visibleLocals.length)
		System.arraycopy(visibleLocals, 0, visibleLocals = new LocalVariableBinding[visibleLocalsCount * 2], 0, visibleLocalsCount);
	visibleLocals[visibleLocalsCount++] = localBinding;
}

public void addVariable(LocalVariableBinding localBinding) {
	/* do nothing */
}
public void aload(int iArg) {
	if (DEBUG) System.out.println(position + "\t\taload:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_wide;
		bCodeStream[classFileOffset++] = Opcodes.OPC_aload;
		writeUnsignedShort(iArg);
	} else {
		// Don't need to use the wide bytecode
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_aload;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
public void aload_0() {
	if (DEBUG) System.out.println(position + "\t\taload_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax) {
		stackMax = stackDepth;
	}
	if (maxLocals == 0) {
		maxLocals = 1;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_aload_0;
}
public void aload_1() {
	if (DEBUG) System.out.println(position + "\t\taload_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_aload_1;
}
public void aload_2() {
	if (DEBUG) System.out.println(position + "\t\taload_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_aload_2;
}
public void aload_3() {
	if (DEBUG) System.out.println(position + "\t\taload_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_aload_3;
}
public void anewarray(TypeBinding typeBinding) {
	if (DEBUG) System.out.println(position + "\t\tanewarray: " + typeBinding); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_anewarray;
	writeUnsignedShort(constantPool.literalIndexForType(typeBinding));
}
public void areturn() {
	if (DEBUG) System.out.println(position + "\t\tareturn"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	// the stackDepth should be equal to 0 
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_areturn;
	this.lastAbruptCompletion = this.position;		
}
public void arrayAt(int typeBindingID) {
	switch (typeBindingID) {
		case TypeIds.T_int :
			this.iaload();
			break;
		case TypeIds.T_byte :
		case TypeIds.T_boolean :
			this.baload();
			break;
		case TypeIds.T_short :
			this.saload();
			break;
		case TypeIds.T_char :
			this.caload();
			break;
		case TypeIds.T_long :
			this.laload();
			break;
		case TypeIds.T_float :
			this.faload();
			break;
		case TypeIds.T_double :
			this.daload();
			break;
		default :
			this.aaload();
	}
}
public void arrayAtPut(int elementTypeID, boolean valueRequired) {
	switch (elementTypeID) {
		case TypeIds.T_int :
			if (valueRequired)
				dup_x2();
			iastore();
			break;
		case TypeIds.T_byte :
		case TypeIds.T_boolean :
			if (valueRequired)
				dup_x2();
			bastore();
			break;
		case TypeIds.T_short :
			if (valueRequired)
				dup_x2();
			sastore();
			break;
		case TypeIds.T_char :
			if (valueRequired)
				dup_x2();
			castore();
			break;
		case TypeIds.T_long :
			if (valueRequired)
				dup2_x2();
			lastore();
			break;
		case TypeIds.T_float :
			if (valueRequired)
				dup_x2();
			fastore();
			break;
		case TypeIds.T_double :
			if (valueRequired)
				dup2_x2();
			dastore();
			break;
		default :
			if (valueRequired)
				dup_x2();
			aastore();
	}
}
public void arraylength() {
	if (DEBUG) System.out.println(position + "\t\tarraylength"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_arraylength;
}
public void astore(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tastore:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position+=2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_wide;
		bCodeStream[classFileOffset++] = Opcodes.OPC_astore;
		writeUnsignedShort(iArg);
	} else {
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position+=2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_astore;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
public void astore_0() {
	if (DEBUG) System.out.println(position + "\t\tastore_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals == 0) {
		maxLocals = 1;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_astore_0;
}
public void astore_1() {
	if (DEBUG) System.out.println(position + "\t\tastore_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_astore_1;
}
public void astore_2() {
	if (DEBUG) System.out.println(position + "\t\tastore_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_astore_2;
}
public void astore_3() {
	if (DEBUG) System.out.println(position + "\t\tastore_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_astore_3;
}
public void athrow() {
	if (DEBUG) System.out.println(position + "\t\tathrow"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_athrow;
	this.lastAbruptCompletion = this.position;		
}
public void baload() {
	if (DEBUG) System.out.println(position + "\t\tbaload"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_baload;
}
public void bastore() {
	if (DEBUG) System.out.println(position + "\t\tbastore"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_bastore;
}
public void bipush(byte b) {
	if (DEBUG) System.out.println(position + "\t\tbipush "+b); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset + 1 >= bCodeStream.length) {
		resizeByteArray();
	}
	position += 2;
	bCodeStream[classFileOffset++] = Opcodes.OPC_bipush;
	bCodeStream[classFileOffset++] = b;
}
public void caload() {
	if (DEBUG) System.out.println(position + "\t\tcaload"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_caload;
}
public void castore() {
	if (DEBUG) System.out.println(position + "\t\tcastore"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_castore;
}
public void checkcast(int baseId) {
	this.countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_checkcast;
	switch (baseId) {
		case TypeIds.T_byte :
			writeUnsignedShort(this.constantPool.literalIndexForType(ConstantPool.JavaLangByteConstantPoolName));
			break;
		case TypeIds.T_short :
			writeUnsignedShort(this.constantPool.literalIndexForType(ConstantPool.JavaLangShortConstantPoolName));
			break;
		case TypeIds.T_char :
			writeUnsignedShort(this.constantPool.literalIndexForType(ConstantPool.JavaLangCharacterConstantPoolName));
			break;
		case TypeIds.T_int :
			writeUnsignedShort(this.constantPool.literalIndexForType(ConstantPool.JavaLangIntegerConstantPoolName));
			break;
		case TypeIds.T_long :
			writeUnsignedShort(this.constantPool.literalIndexForType(ConstantPool.JavaLangLongConstantPoolName));
			break;
		case TypeIds.T_float :
			writeUnsignedShort(this.constantPool.literalIndexForType(ConstantPool.JavaLangFloatConstantPoolName));
			break;
		case TypeIds.T_double :
			writeUnsignedShort(this.constantPool.literalIndexForType(ConstantPool.JavaLangDoubleConstantPoolName));
			break;
		case TypeIds.T_boolean :
			writeUnsignedShort(this.constantPool.literalIndexForType(ConstantPool.JavaLangBooleanConstantPoolName));
	}
}
public void checkcast(TypeBinding typeBinding) {
	if (DEBUG) System.out.println(position + "\t\tcheckcast:"+typeBinding.debugName()); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_checkcast;
	writeUnsignedShort(constantPool.literalIndexForType(typeBinding));
}
public void d2f() {
	if (DEBUG) System.out.println(position + "\t\td2f"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_d2f;
}
public void d2i() {
	if (DEBUG) System.out.println(position + "\t\td2i"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_d2i;
}
public void d2l() {
	if (DEBUG) System.out.println(position + "\t\td2l"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_d2l;
}
public void dadd() {
	if (DEBUG) System.out.println(position + "\t\tdadd"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dadd;
}
public void daload() {
	if (DEBUG) System.out.println(position + "\t\tdaload"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_daload;
}
public void dastore() {
	if (DEBUG) System.out.println(position + "\t\tdastore"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 4;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dastore;
}
public void dcmpg() {
	if (DEBUG) System.out.println(position + "\t\tdcmpg"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dcmpg;
}
public void dcmpl() {
	if (DEBUG) System.out.println(position + "\t\tdcmpl"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dcmpl;
}
public void dconst_0() {
	if (DEBUG) System.out.println(position + "\t\tdconst_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dconst_0;
}
public void dconst_1() {
	if (DEBUG) System.out.println(position + "\t\tdconst_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dconst_1;
}
public void ddiv() {
	if (DEBUG) System.out.println(position + "\t\tddiv"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_ddiv;
}
public void decrStackSize(int offset) {
	stackDepth -= offset;
}
public void dload(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tdload:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals < iArg + 2) {
		maxLocals = iArg + 2; // + 2 because it is a double
	}
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_wide;
		bCodeStream[classFileOffset++] = Opcodes.OPC_dload;
		writeUnsignedShort(iArg);
	} else {
		// Don't need to use the wide bytecode
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_dload;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
public void dload_0() {
	if (DEBUG) System.out.println(position + "\t\tdload_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals < 2) {
		maxLocals = 2;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dload_0;
}
public void dload_1() {
	if (DEBUG) System.out.println(position + "\t\tdload_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals < 3) {
		maxLocals = 3;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dload_1;
}
public void dload_2() {
	if (DEBUG) System.out.println(position + "\t\tdload_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals < 4) {
		maxLocals = 4;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dload_2;
}
public void dload_3() {
	if (DEBUG) System.out.println(position + "\t\tdload_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals < 5) {
		maxLocals = 5;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dload_3;
}
public void dmul() {
	if (DEBUG) System.out.println(position + "\t\tdmul"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dmul;
}
public void dneg() {
	if (DEBUG) System.out.println(position + "\t\tdneg"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dneg;
}
public void drem() {
	if (DEBUG) System.out.println(position + "\t\tdrem"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_drem;
}
public void dreturn() {
	if (DEBUG) System.out.println(position + "\t\tdreturn"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	// the stackDepth should be equal to 0 
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dreturn;
	this.lastAbruptCompletion = this.position;		
}
public void dstore(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tdstore:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals <= iArg + 1) {
		maxLocals = iArg + 2;
	}
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_wide;
		bCodeStream[classFileOffset++] = Opcodes.OPC_dstore;
		writeUnsignedShort(iArg);
	} else {
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_dstore;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
public void dstore_0() {
	if (DEBUG) System.out.println(position + "\t\tdstore_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 2) {
		maxLocals = 2;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dstore_0;
}
public void dstore_1() {
	if (DEBUG) System.out.println(position + "\t\tdstore_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 3) {
		maxLocals = 3;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dstore_1;
}
public void dstore_2() {
	if (DEBUG) System.out.println(position + "\t\tdstore_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 4) {
		maxLocals = 4;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dstore_2;
}
public void dstore_3() {
	if (DEBUG) System.out.println(position + "\t\tdstore_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 5) {
		maxLocals = 5;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dstore_3;
}
public void dsub() {
	if (DEBUG) System.out.println(position + "\t\tdsub"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dsub;
}
public void dup() {
	if (DEBUG) System.out.println(position + "\t\tdup"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax) {
		stackMax = stackDepth;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dup;
}
public void dup_x1() {
	if (DEBUG) System.out.println(position + "\t\tdup_x1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dup_x1;
}
public void dup_x2() {
	if (DEBUG) System.out.println(position + "\t\tdup_x2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dup_x2;
}
public void dup2() {
	if (DEBUG) System.out.println(position + "\t\tdup2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dup2;
}
public void dup2_x1() {
	if (DEBUG) System.out.println(position + "\t\tdup2_x1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dup2_x1;
}
public void dup2_x2() {
	if (DEBUG) System.out.println(position + "\t\tdup2_x2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_dup2_x2;
}
public void exitUserScope(BlockScope currentScope) {
	// mark all the scope's locals as losing their definite assignment

	if ((this.generateAttributes & (ClassFileConstants.ATTR_VARS
			| ClassFileConstants.ATTR_STACK_MAP_TABLE
			| ClassFileConstants.ATTR_STACK_MAP)) == 0)
		return;
	int index = this.visibleLocalsCount - 1;
	while (index >= 0) {
		LocalVariableBinding visibleLocal = visibleLocals[index];
		if (visibleLocal == null || visibleLocal.declaringScope != currentScope) {
			// left currentScope
			index--;
			continue;
		}

		// there may be some preserved locals never initialized
		if (visibleLocal.initializationCount > 0) {
			visibleLocal.recordInitializationEndPC(position);
		}
		visibleLocals[index--] = null; // this variable is no longer visible afterwards
	}
}
public void exitUserScope(BlockScope currentScope, LocalVariableBinding binding) {
	// mark all the scope's locals as losing their definite assignment
	if ((this.generateAttributes & (ClassFileConstants.ATTR_VARS
			| ClassFileConstants.ATTR_STACK_MAP_TABLE
			| ClassFileConstants.ATTR_STACK_MAP)) == 0)
		return;
	int index = this.visibleLocalsCount - 1;
	while (index >= 0) {
		LocalVariableBinding visibleLocal = visibleLocals[index];
		if (visibleLocal == null || visibleLocal.declaringScope != currentScope || visibleLocal == binding) {
			// left currentScope
			index--;
			continue;
		}
		// there may be some preserved locals never initialized
		if (visibleLocal.initializationCount > 0) {
			visibleLocal.recordInitializationEndPC(position);
		}
		visibleLocals[index--] = null; // this variable is no longer visible afterwards
	}
}
public void f2d() {
	if (DEBUG) System.out.println(position + "\t\tf2d"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_f2d;
}
public void f2i() {
	if (DEBUG) System.out.println(position + "\t\tf2i"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_f2i;
}
public void f2l() {
	if (DEBUG) System.out.println(position + "\t\tf2l"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_f2l;
}
public void fadd() {
	if (DEBUG) System.out.println(position + "\t\tfadd"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fadd;
}
public void faload() {
	if (DEBUG) System.out.println(position + "\t\tfaload"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_faload;
}
public void fastore() {
	if (DEBUG) System.out.println(position + "\t\tfaload"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fastore;
}
public void fcmpg() {
	if (DEBUG) System.out.println(position + "\t\tfcmpg"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fcmpg;
}
public void fcmpl() {
	if (DEBUG) System.out.println(position + "\t\tfcmpl"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fcmpl;
}
public void fconst_0() {
	if (DEBUG) System.out.println(position + "\t\tfconst_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fconst_0;
}
public void fconst_1() {
	if (DEBUG) System.out.println(position + "\t\tfconst_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fconst_1;
}
public void fconst_2() {
	if (DEBUG) System.out.println(position + "\t\tfconst_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fconst_2;
}
public void fdiv() {
	if (DEBUG) System.out.println(position + "\t\tfdiv"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fdiv;
}
public void fload(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tfload:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_wide;
		bCodeStream[classFileOffset++] = Opcodes.OPC_fload;
		writeUnsignedShort(iArg);
	} else {
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_fload;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
public void fload_0() {
	if (DEBUG) System.out.println(position + "\t\tfload_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals == 0) {
		maxLocals = 1;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fload_0;
}
public void fload_1() {
	if (DEBUG) System.out.println(position + "\t\tfload_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fload_1;
}
public void fload_2() {
	if (DEBUG) System.out.println(position + "\t\tfload_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fload_2;
}
public void fload_3() {
	if (DEBUG) System.out.println(position + "\t\tfload_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fload_3;
}
public void fmul() {
	if (DEBUG) System.out.println(position + "\t\tfmul"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fmul;
}
public void fneg() {
	if (DEBUG) System.out.println(position + "\t\tfneg"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fneg;
}
public void frem() {
	if (DEBUG) System.out.println(position + "\t\tfrem"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_frem;
}
public void freturn() {
	if (DEBUG) System.out.println(position + "\t\tfreturn"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	// the stackDepth should be equal to 0 
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_freturn;
	this.lastAbruptCompletion = this.position;		
}
public void fstore(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tfstore:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_wide;
		bCodeStream[classFileOffset++] = Opcodes.OPC_fstore;
		writeUnsignedShort(iArg);
	} else {
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_fstore;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
public void fstore_0() {
	if (DEBUG) System.out.println(position + "\t\tfstore_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals == 0) {
		maxLocals = 1;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fstore_0;
}
public void fstore_1() {
	if (DEBUG) System.out.println(position + "\t\tfstore_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fstore_1;
}
public void fstore_2() {
	if (DEBUG) System.out.println(position + "\t\tfstore_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fstore_2;
}
public void fstore_3() {
	if (DEBUG) System.out.println(position + "\t\tfstore_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fstore_3;
}

public void fsub() {
	if (DEBUG) System.out.println(position + "\t\tfsub"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_fsub;
}
public void generateBoxingConversion(int unboxedTypeID) {
    switch (unboxedTypeID) {
        case TypeIds.T_byte :
            if (this.targetLevel >= ClassFileConstants.JDK1_5) {
    			if (DEBUG) System.out.println(position + "\t\tinvokestatic java.lang.Byte.valueOf(byte)"); //$NON-NLS-1$
               // invokestatic: Byte.valueOf(byte)
                this.invoke(
                    Opcodes.OPC_invokestatic,
                    1, // argCount
                    1, // return type size
                    ConstantPool.JavaLangByteConstantPoolName,
                    ConstantPool.ValueOf,
                    ConstantPool.byteByteSignature);
            } else {
               // new Byte( byte )
    			if (DEBUG) System.out.println(position + "\t\tinvokespecial java.lang.Byte(byte)"); //$NON-NLS-1$
                newWrapperFor(unboxedTypeID);
                dup_x1();
                swap();
                this.invoke(
                    Opcodes.OPC_invokespecial,
                    1, // argCount
                    0, // return type size
                    ConstantPool.JavaLangByteConstantPoolName,
                    ConstantPool.Init,
                    ConstantPool.ByteConstrSignature);
            }       
            break;
        case TypeIds.T_short :
            if ( this.targetLevel >= ClassFileConstants.JDK1_5 ) {
                // invokestatic: Short.valueOf(short)
    			if (DEBUG) System.out.println(position + "\t\tinvokestatic java.lang.Short.valueOf(short)"); //$NON-NLS-1$
                this.invoke(
                    Opcodes.OPC_invokestatic,
                    1, // argCount
                    1, // return type size
                    ConstantPool.JavaLangShortConstantPoolName,
                    ConstantPool.ValueOf,
                    ConstantPool.shortShortSignature);
            } else {
                // new Short(short)
            	if (DEBUG) System.out.println(position + "\t\tinvokespecial java.lang.Short(short)"); //$NON-NLS-1$
            	newWrapperFor(unboxedTypeID);                
                dup_x1();
                swap();             
                this.invoke(
                    Opcodes.OPC_invokespecial,
                    1, // argCount
                    0, // return type size
                    ConstantPool.JavaLangShortConstantPoolName,
                    ConstantPool.Init,
                    ConstantPool.ShortConstrSignature);     
            }
            break;
        case TypeIds.T_char :
            if ( this.targetLevel >= ClassFileConstants.JDK1_5 ) {
                // invokestatic: Character.valueOf(char)
            	if (DEBUG) System.out.println(position + "\t\tinvokestatic java.lang.Character.valueOf(char)"); //$NON-NLS-1$
                this.invoke(
                    Opcodes.OPC_invokestatic,
                    1, // argCount
                    1, // return type size
                    ConstantPool.JavaLangCharacterConstantPoolName,
                    ConstantPool.ValueOf,
                    ConstantPool.charCharacterSignature);
            } else {
                // new Char( char )
            	if (DEBUG) System.out.println(position + "\t\tinvokespecial java.lang.Character(char)"); //$NON-NLS-1$
                newWrapperFor(unboxedTypeID);
                dup_x1();
                swap();
                this.invoke(
                    Opcodes.OPC_invokespecial,
                    1, // argCount
                    0, // return type size
                    ConstantPool.JavaLangCharacterConstantPoolName,
                    ConstantPool.Init,
                    ConstantPool.CharConstrSignature);
            }       
            break;
        case TypeIds.T_int :             
            if (this.targetLevel >= ClassFileConstants.JDK1_5) {
                // invokestatic: Integer.valueOf(int)
            	if (DEBUG) System.out.println(position + "\t\tinvokestatic java.lang.Integer.valueOf(int)"); //$NON-NLS-1$
                this.invoke(
                    Opcodes.OPC_invokestatic,
                    1, // argCount
                    1, // return type size
                    ConstantPool.JavaLangIntegerConstantPoolName,
                    ConstantPool.ValueOf,
                    ConstantPool.IntIntegerSignature);
            } else {
                // new Integer(int)
            	if (DEBUG) System.out.println(position + "\t\tinvokespecial java.lang.Integer(int)"); //$NON-NLS-1$
                newWrapperFor(unboxedTypeID);
                dup_x1();
                swap();             
                this.invoke(
                    Opcodes.OPC_invokespecial,
                    1, // argCount
                    0, // return type size
                    ConstantPool.JavaLangIntegerConstantPoolName,
                    ConstantPool.Init,
                    ConstantPool.IntConstrSignature);
            }
            break;
        case TypeIds.T_long :
            if (this.targetLevel >= ClassFileConstants.JDK1_5) { 
                // invokestatic: Long.valueOf(long)
            	if (DEBUG) System.out.println(position + "\t\tinvokestatic java.lang.Long.valueOf(long)"); //$NON-NLS-1$
                this.invoke(
                    Opcodes.OPC_invokestatic,
                    2, // argCount
                    1, // return type size
                    ConstantPool.JavaLangLongConstantPoolName,
                    ConstantPool.ValueOf,
                    ConstantPool.longLongSignature);
            } else {
                // new Long( long )
            	if (DEBUG) System.out.println(position + "\t\tinvokespecial java.lang.Long(long)"); //$NON-NLS-1$
                newWrapperFor(unboxedTypeID);
                dup_x2();
                dup_x2();
                pop();
                this.invoke(
                    Opcodes.OPC_invokespecial,
                    2, // argCount
                    0, // return type size
                    ConstantPool.JavaLangLongConstantPoolName,
                    ConstantPool.Init,
                    ConstantPool.LongConstrSignature);
            }                   
            break;
        case TypeIds.T_float :
            if ( this.targetLevel >= ClassFileConstants.JDK1_5 ) {
                // invokestatic: Float.valueOf(float)
            	if (DEBUG) System.out.println(position + "\t\tinvokestatic java.lang.Float.valueOf(float)"); //$NON-NLS-1$
                this.invoke(
                    Opcodes.OPC_invokestatic,
                    1, // argCount
                    1, // return type size
                    ConstantPool.JavaLangFloatConstantPoolName,
                    ConstantPool.ValueOf,
                    ConstantPool.floatFloatSignature);
            } else {
                // new Float(float)
            	if (DEBUG) System.out.println(position + "\t\tinvokespecial java.lang.Float(float)"); //$NON-NLS-1$
                newWrapperFor(unboxedTypeID);
                dup_x1();
                swap();             
                this.invoke(
                    Opcodes.OPC_invokespecial,
                    1, // argCount
                    0, // return type size
                    ConstantPool.JavaLangFloatConstantPoolName,
                    ConstantPool.Init,
                    ConstantPool.FloatConstrSignature);
            }       
            break;
        case TypeIds.T_double :
            if ( this.targetLevel >= ClassFileConstants.JDK1_5 ) { 
                // invokestatic: Double.valueOf(double)
            	if (DEBUG) System.out.println(position + "\t\tinvokestatic java.lang.Double.valueOf(double)"); //$NON-NLS-1$
                this.invoke(
                    Opcodes.OPC_invokestatic,
                    2, // argCount
                    1, // return type size
                    ConstantPool.JavaLangDoubleConstantPoolName,
                    ConstantPool.ValueOf,
                    ConstantPool.doubleDoubleSignature);
            } else {
                // new Double( double )
            	if (DEBUG) System.out.println(position + "\t\tinvokespecial java.lang.Double(double)"); //$NON-NLS-1$
            	newWrapperFor(unboxedTypeID);                
                dup_x2();
                dup_x2();
                pop();
                
                this.invoke(
                    Opcodes.OPC_invokespecial,
                    2, // argCount
                    0, // return type size
                    ConstantPool.JavaLangDoubleConstantPoolName,
                    ConstantPool.Init,
                    ConstantPool.DoubleConstrSignature);
            }       
            
            break;  
        case TypeIds.T_boolean :
            if ( this.targetLevel >= ClassFileConstants.JDK1_5 ) {
                // invokestatic: Boolean.valueOf(boolean)
            	if (DEBUG) System.out.println(position + "\t\tinvokestatic java.lang.Boolean.valueOf(boolean)"); //$NON-NLS-1$
                this.invoke(
                    Opcodes.OPC_invokestatic,
                    1, // argCount
                    1, // return type size
                    ConstantPool.JavaLangBooleanConstantPoolName,
                    ConstantPool.ValueOf,
                    ConstantPool.booleanBooleanSignature);
            } else {
                // new Boolean(boolean)
            	if (DEBUG) System.out.println(position + "\t\tinvokespecial java.lang.Boolean(boolean)"); //$NON-NLS-1$
                newWrapperFor(unboxedTypeID);
                dup_x1();
                swap();             
                this.invoke(
                    Opcodes.OPC_invokespecial,
                    1, // argCount
                    0, // return type size
                    ConstantPool.JavaLangBooleanConstantPoolName,
                    ConstantPool.Init,
                    ConstantPool.BooleanConstrSignature);
            }
    }
}
/**
 * Macro for building a class descriptor object
 */
public void generateClassLiteralAccessForType(TypeBinding accessedType, FieldBinding syntheticFieldBinding) {
	if (accessedType.isBaseType() && accessedType != TypeBinding.NULL) {
		this.getTYPE(accessedType.id);
		return;
	}

	if (this.targetLevel >= ClassFileConstants.JDK1_5) {
		// generation using the new ldc_w bytecode
		this.ldc(accessedType);
	} else {
		BranchLabel endLabel = new BranchLabel(this);
		if (syntheticFieldBinding != null) { // non interface case
			this.getstatic(syntheticFieldBinding);
			this.dup();
			this.ifnonnull(endLabel);
			this.pop();
		}

		/* Macro for building a class descriptor object... using or not a field cache to store it into...
		this sequence is responsible for building the actual class descriptor.
		
		If the fieldCache is set, then it is supposed to be the body of a synthetic access method
		factoring the actual descriptor creation out of the invocation site (saving space).
		If the fieldCache is nil, then we are dumping the bytecode on the invocation site, since
		we have no way to get a hand on the field cache to do better. */
	
	
		// Wrap the code in an exception handler to convert a ClassNotFoundException into a NoClassDefError
	
		ExceptionLabel classNotFoundExceptionHandler = new ExceptionLabel(this, TypeBinding.NULL /*represents ClassNotFoundException*/);
		classNotFoundExceptionHandler.placeStart();
		this.ldc(accessedType == TypeBinding.NULL ? "java.lang.Object" : String.valueOf(accessedType.constantPoolName()).replace('/', '.')); //$NON-NLS-1$
		this.invokeClassForName();
	
		/* See https://bugs.eclipse.org/bugs/show_bug.cgi?id=37565
		if (accessedType == BaseTypes.NullBinding) {
			this.ldc("java.lang.Object"); //$NON-NLS-1$
		} else if (accessedType.isArrayType()) {
			this.ldc(String.valueOf(accessedType.constantPoolName()).replace('/', '.'));
		} else {
			// we make it an array type (to avoid class initialization)
			this.ldc("[L" + String.valueOf(accessedType.constantPoolName()).replace('/', '.') + ";"); //$NON-NLS-1$//$NON-NLS-2$
		}
		this.invokeClassForName();
		if (!accessedType.isArrayType()) { // extract the component type, which doesn't initialize the class
			this.invokeJavaLangClassGetComponentType();
		}	
		*/
		/* We need to protect the runtime code from binary inconsistencies
		in case the accessedType is missing, the ClassNotFoundException has to be converted
		into a NoClassDefError(old ex message), we thus need to build an exception handler for this one. */
		classNotFoundExceptionHandler.placeEnd();
	
		if (syntheticFieldBinding != null) { // non interface case
			this.dup();
			this.putstatic(syntheticFieldBinding);
		}
		this.goto_(endLabel);

		int savedStackDepth = this.stackDepth;
		// Generate the body of the exception handler
		/* ClassNotFoundException on stack -- the class literal could be doing more things
		on the stack, which means that the stack may not be empty at this point in the
		above code gen. So we save its state and restart it from 1. */
	
		this.pushExceptionOnStack(TypeBinding.NULL);/*represents ClassNotFoundException*/
		classNotFoundExceptionHandler.place();

		// Transform the current exception, and repush and throw a 
		// NoClassDefFoundError(ClassNotFound.getMessage())
	
		this.newNoClassDefFoundError();
		this.dup_x1();
		this.swap();
	
		// Retrieve the message from the old exception
		this.invokeThrowableGetMessage();
	
		// Send the constructor taking a message string as an argument
		this.invokeNoClassDefFoundErrorStringConstructor();
		this.athrow();
		endLabel.place();
		this.stackDepth = savedStackDepth;
	}
}
/**
 * This method generates the code attribute bytecode
 */
final public void generateCodeAttributeForProblemMethod(String problemMessage) {
	newJavaLangError();
	dup();
	ldc(problemMessage);
	invokeJavaLangErrorConstructor();
	athrow();
}
public void generateConstant(Constant constant, int implicitConversionCode) {
	int targetTypeID = (implicitConversionCode & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4;
	if (targetTypeID == 0) targetTypeID = constant.typeID(); // use default constant type
	switch (targetTypeID) {
		case TypeIds.T_boolean :
			generateInlinedValue(constant.booleanValue());
			break;
		case TypeIds.T_char :
			generateInlinedValue(constant.charValue());
			break;
		case TypeIds.T_byte :
			generateInlinedValue(constant.byteValue());
			break;
		case TypeIds.T_short :
			generateInlinedValue(constant.shortValue());
			break;
		case TypeIds.T_int :
			generateInlinedValue(constant.intValue());
			break;
		case TypeIds.T_long :
			generateInlinedValue(constant.longValue());
			break;
		case TypeIds.T_float :
			generateInlinedValue(constant.floatValue());
			break;
		case TypeIds.T_double :
			generateInlinedValue(constant.doubleValue());
			break;
		case TypeIds.T_JavaLangString :
			ldc(constant.stringValue());
	}
	if ((implicitConversionCode & TypeIds.BOXING) != 0) {
		// need boxing
		generateBoxingConversion(targetTypeID);
	}
}
public void generateEmulatedReadAccessForField(FieldBinding fieldBinding) {
	this.generateEmulationForField(fieldBinding);
	// swap  the field with the receiver
	this.swap();
	this.invokeJavaLangReflectFieldGetter(fieldBinding.type.id);
	if (!fieldBinding.type.isBaseType()) {
		this.checkcast(fieldBinding.type);
	}
}
public void generateEmulatedWriteAccessForField(FieldBinding fieldBinding) {
	this.invokeJavaLangReflectFieldSetter(fieldBinding.type.id);
}
public void generateEmulationForConstructor(Scope scope, MethodBinding methodBinding) {
	// leave a java.lang.reflect.Field object on the stack
	this.ldc(String.valueOf(methodBinding.declaringClass.constantPoolName()).replace('/', '.'));
	this.invokeClassForName();
	int paramLength = methodBinding.parameters.length;
	this.generateInlinedValue(paramLength);
	this.newArray(scope.createArrayType(scope.getType(TypeConstants.JAVA_LANG_CLASS, 3), 1));
	if (paramLength > 0) {
		this.dup();
		for (int i = 0; i < paramLength; i++) {
			this.generateInlinedValue(i);	
			TypeBinding parameter = methodBinding.parameters[i];
			if (parameter.isBaseType()) {
				this.getTYPE(parameter.id);
			} else if (parameter.isArrayType()) {
				ArrayBinding array = (ArrayBinding)parameter;
				if (array.leafComponentType.isBaseType()) {
					this.getTYPE(array.leafComponentType.id);
				} else {
					this.ldc(String.valueOf(array.leafComponentType.constantPoolName()).replace('/', '.'));
					this.invokeClassForName();
				}
				int dimensions = array.dimensions;
				this.generateInlinedValue(dimensions);
				this.newarray(TypeIds.T_int);	
				this.invokeArrayNewInstance();
				this.invokeObjectGetClass();
			} else {
				// parameter is a reference binding
				this.ldc(String.valueOf(methodBinding.declaringClass.constantPoolName()).replace('/', '.'));
				this.invokeClassForName();
			}
			this.aastore();
			if (i < paramLength - 1) {
				this.dup();
			}
		}
	}
	this.invokeClassGetDeclaredConstructor();
	this.dup();
	this.iconst_1();
	this.invokeAccessibleObjectSetAccessible();
}
public void generateEmulationForField(FieldBinding fieldBinding) {
	// leave a java.lang.reflect.Field object on the stack
	this.ldc(String.valueOf(fieldBinding.declaringClass.constantPoolName()).replace('/', '.'));
	this.invokeClassForName();
	this.ldc(String.valueOf(fieldBinding.name));
	this.invokeClassGetDeclaredField();
	this.dup();
	this.iconst_1();
	this.invokeAccessibleObjectSetAccessible();
}
public void generateEmulationForMethod(Scope scope, MethodBinding methodBinding) {
	// leave a java.lang.reflect.Field object on the stack
	this.ldc(String.valueOf(methodBinding.declaringClass.constantPoolName()).replace('/', '.'));
	this.invokeClassForName();
	this.ldc(String.valueOf(methodBinding.selector));
	int paramLength = methodBinding.parameters.length;
	this.generateInlinedValue(paramLength);
	this.newArray(scope.createArrayType(scope.getType(TypeConstants.JAVA_LANG_CLASS, 3), 1));
	if (paramLength > 0) {
		this.dup();
		for (int i = 0; i < paramLength; i++) {
			this.generateInlinedValue(i);	
			TypeBinding parameter = methodBinding.parameters[i];
			if (parameter.isBaseType()) {
				this.getTYPE(parameter.id);
			} else if (parameter.isArrayType()) {
				ArrayBinding array = (ArrayBinding)parameter;
				if (array.leafComponentType.isBaseType()) {
					this.getTYPE(array.leafComponentType.id);
				} else {
					this.ldc(String.valueOf(array.leafComponentType.constantPoolName()).replace('/', '.'));
					this.invokeClassForName();
				}
				int dimensions = array.dimensions;
				this.generateInlinedValue(dimensions);
				this.newarray(TypeIds.T_int);	
				this.invokeArrayNewInstance();
				this.invokeObjectGetClass();
			} else {
				// parameter is a reference binding
				this.ldc(String.valueOf(methodBinding.declaringClass.constantPoolName()).replace('/', '.'));
				this.invokeClassForName();
			}
			this.aastore();
			if (i < paramLength - 1) {
				this.dup();
			}
		}
	}
	this.invokeClassGetDeclaredMethod();
	this.dup();
	this.iconst_1();
	this.invokeAccessibleObjectSetAccessible();
}
private void generateFieldAccess(byte opcode, int returnTypeSize, char[] declaringClass, char[] name, char[] signature) {
	countLabels = 0;
	switch(opcode) {
		case Opcodes.OPC_getfield :
			if (returnTypeSize == 2) {
				stackDepth++;
			}
			break;
		case Opcodes.OPC_getstatic :
			if (returnTypeSize == 2) {
				stackDepth += 2;
			} else {
				stackDepth++;
			}
			break;
		case Opcodes.OPC_putfield :
			if (returnTypeSize == 2) {
				stackDepth -= 3;
			} else {
				stackDepth -= 2;
			}
			break;
		case Opcodes.OPC_putstatic :
			if (returnTypeSize == 2) {
				stackDepth -= 2;
			} else {
				stackDepth--;
			}
	}
	if (stackDepth > stackMax) {
		stackMax = stackDepth;
	}
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = opcode;
	writeUnsignedShort(constantPool.literalIndexForField(declaringClass, name, signature));
}
private void generateFieldAccess(byte opcode, int returnTypeSize, ReferenceBinding binding, char[] name, TypeBinding type) {
	if (binding.isNestedType()) {
		this.classFile.recordInnerClasses(binding);
	}
	TypeBinding leafComponentType = type.leafComponentType();
	if (leafComponentType.isNestedType()) {
		this.classFile.recordInnerClasses(leafComponentType);
	}
	this.generateFieldAccess(opcode, returnTypeSize, binding.constantPoolName(), name, type.signature());
}
/**
 * Generates the sequence of instructions which will perform the conversion of the expression
 * on the stack into a different type (e.g. long l = someInt; --> i2l must be inserted).
 * @param implicitConversionCode int
 */
public void generateImplicitConversion(int implicitConversionCode) {
	if ((implicitConversionCode & TypeIds.UNBOXING) != 0) {
		final int typeId = implicitConversionCode & TypeIds.COMPILE_TYPE_MASK;
		generateUnboxingConversion(typeId);
		// unboxing can further involve base type conversions
	}
	switch (implicitConversionCode & TypeIds.IMPLICIT_CONVERSION_MASK) {
		case TypeIds.Float2Char :
			this.f2i();
			this.i2c();
			break;
		case TypeIds.Double2Char :
			this.d2i();
			this.i2c();
			break;
		case TypeIds.Int2Char :
		case TypeIds.Short2Char :
		case TypeIds.Byte2Char :
			this.i2c();
			break;
		case TypeIds.Long2Char :
			this.l2i();
			this.i2c();
			break;
		case TypeIds.Char2Float :
		case TypeIds.Short2Float :
		case TypeIds.Int2Float :
		case TypeIds.Byte2Float :
			this.i2f();
			break;
		case TypeIds.Double2Float :
			this.d2f();
			break;
		case TypeIds.Long2Float :
			this.l2f();
			break;
		case TypeIds.Float2Byte :
			this.f2i();
			this.i2b();
			break;
		case TypeIds.Double2Byte :
			this.d2i();
			this.i2b();
			break;
		case TypeIds.Int2Byte :
		case TypeIds.Short2Byte :
		case TypeIds.Char2Byte :
			this.i2b();
			break;
		case TypeIds.Long2Byte :
			this.l2i();
			this.i2b();
			break;
		case TypeIds.Byte2Double :
		case TypeIds.Char2Double :
		case TypeIds.Short2Double :
		case TypeIds.Int2Double :
			this.i2d();
			break;
		case TypeIds.Float2Double :
			this.f2d();
			break;
		case TypeIds.Long2Double :
			this.l2d();
			break;
		case TypeIds.Byte2Short :
		case TypeIds.Char2Short :
		case TypeIds.Int2Short :
			this.i2s();
			break;
		case TypeIds.Double2Short :
			this.d2i();
			this.i2s();
			break;
		case TypeIds.Long2Short :
			this.l2i();
			this.i2s();
			break;
		case TypeIds.Float2Short :
			this.f2i();
			this.i2s();
			break;
		case TypeIds.Double2Int :
			this.d2i();
			break;
		case TypeIds.Float2Int :
			this.f2i();
			break;
		case TypeIds.Long2Int :
			this.l2i();
			break;
		case TypeIds.Int2Long :
		case TypeIds.Char2Long :
		case TypeIds.Byte2Long :
		case TypeIds.Short2Long :
			this.i2l();
			break;
		case TypeIds.Double2Long :
			this.d2l();
			break;
		case TypeIds.Float2Long :
			this.f2l();
	}
	if ((implicitConversionCode & TypeIds.BOXING) != 0) {
		// need to unbox/box the constant
		final int typeId = (implicitConversionCode & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4;
		generateBoxingConversion(typeId);
	}
}
public void generateInlinedValue(boolean inlinedValue) {
	if (inlinedValue)
		this.iconst_1();
	else
		this.iconst_0();
}

public void generateInlinedValue(byte inlinedValue) {
	switch (inlinedValue) {
		case -1 :
			this.iconst_m1();
			break;
		case 0 :
			this.iconst_0();
			break;
		case 1 :
			this.iconst_1();
			break;
		case 2 :
			this.iconst_2();
			break;
		case 3 :
			this.iconst_3();
			break;
		case 4 :
			this.iconst_4();
			break;
		case 5 :
			this.iconst_5();
			break;
		default :
			if ((-128 <= inlinedValue) && (inlinedValue <= 127)) {
				this.bipush(inlinedValue);
				return;
			}
	}
}

public void generateInlinedValue(char inlinedValue) {
	switch (inlinedValue) {
		case 0 :
			this.iconst_0();
			break;
		case 1 :
			this.iconst_1();
			break;
		case 2 :
			this.iconst_2();
			break;
		case 3 :
			this.iconst_3();
			break;
		case 4 :
			this.iconst_4();
			break;
		case 5 :
			this.iconst_5();
			break;
		default :
			if ((6 <= inlinedValue) && (inlinedValue <= 127)) {
				this.bipush((byte) inlinedValue);
				return;
			}
			if ((128 <= inlinedValue) && (inlinedValue <= 32767)) {
				this.sipush(inlinedValue);
				return;
			}
			this.ldc(inlinedValue);
	}
}
public void generateInlinedValue(double inlinedValue) {
	if (inlinedValue == 0.0) {
		if (Double.doubleToLongBits(inlinedValue) != 0L)
			this.ldc2_w(inlinedValue);
		else
			this.dconst_0();
		return;
	}
	if (inlinedValue == 1.0) {
		this.dconst_1();
		return;
	}
	this.ldc2_w(inlinedValue);
}
public void generateInlinedValue(float inlinedValue) {
	if (inlinedValue == 0.0f) {
		if (Float.floatToIntBits(inlinedValue) != 0)
			this.ldc(inlinedValue);
		else
			this.fconst_0();
		return;
	}
	if (inlinedValue == 1.0f) {
		this.fconst_1();
		return;
	}
	if (inlinedValue == 2.0f) {
		this.fconst_2();
		return;
	}
	this.ldc(inlinedValue);
}
public void generateInlinedValue(int inlinedValue) {
	switch (inlinedValue) {
		case -1 :
			this.iconst_m1();
			break;
		case 0 :
			this.iconst_0();
			break;
		case 1 :
			this.iconst_1();
			break;
		case 2 :
			this.iconst_2();
			break;
		case 3 :
			this.iconst_3();
			break;
		case 4 :
			this.iconst_4();
			break;
		case 5 :
			this.iconst_5();
			break;
		default :
			if ((-128 <= inlinedValue) && (inlinedValue <= 127)) {
				this.bipush((byte) inlinedValue);
				return;
			}
			if ((-32768 <= inlinedValue) && (inlinedValue <= 32767)) {
				this.sipush(inlinedValue);
				return;
			}
			this.ldc(inlinedValue);
	}
}
public void generateInlinedValue(long inlinedValue) {
	if (inlinedValue == 0) {
		this.lconst_0();
		return;
	}
	if (inlinedValue == 1) {
		this.lconst_1();
		return;
	}
	this.ldc2_w(inlinedValue);
}
public void generateInlinedValue(short inlinedValue) {
	switch (inlinedValue) {
		case -1 :
			this.iconst_m1();
			break;
		case 0 :
			this.iconst_0();
			break;
		case 1 :
			this.iconst_1();
			break;
		case 2 :
			this.iconst_2();
			break;
		case 3 :
			this.iconst_3();
			break;
		case 4 :
			this.iconst_4();
			break;
		case 5 :
			this.iconst_5();
			break;
		default :
			if ((-128 <= inlinedValue) && (inlinedValue <= 127)) {
				this.bipush((byte) inlinedValue);
				return;
			}
			this.sipush(inlinedValue);
	}
}
public void generateOuterAccess(Object[] mappingSequence, ASTNode invocationSite, Binding target, Scope scope) {
	if (mappingSequence == null) {
		if (target instanceof LocalVariableBinding) {
			scope.problemReporter().needImplementation(invocationSite); //TODO (philippe) should improve local emulation failure reporting
		} else {
			scope.problemReporter().noSuchEnclosingInstance((ReferenceBinding)target, invocationSite, false);
		}
		return;
	}
	if (mappingSequence == BlockScope.NoEnclosingInstanceInConstructorCall) {
		scope.problemReporter().noSuchEnclosingInstance((ReferenceBinding)target, invocationSite, true);
		return;
	} else if (mappingSequence == BlockScope.NoEnclosingInstanceInStaticContext) {
		scope.problemReporter().noSuchEnclosingInstance((ReferenceBinding)target, invocationSite, false);
		return;
	}
	
	if (mappingSequence == BlockScope.EmulationPathToImplicitThis) {
		this.aload_0();
		return;
	} else if (mappingSequence[0] instanceof FieldBinding) {
		FieldBinding fieldBinding = (FieldBinding) mappingSequence[0];
		this.aload_0();
		this.getfield(fieldBinding);
	} else {
		load((LocalVariableBinding) mappingSequence[0]);
	}
	for (int i = 1, length = mappingSequence.length; i < length; i++) {
		if (mappingSequence[i] instanceof FieldBinding) {
			FieldBinding fieldBinding = (FieldBinding) mappingSequence[i];
			this.getfield(fieldBinding);
		} else {
			this.invokestatic((MethodBinding) mappingSequence[i]);
		}
	}
}
public void generateReturnBytecode(Expression expression) {
	
	if (expression == null) {
		this.return_();
	} else {
		final int implicitConversion = expression.implicitConversion;
		if ((implicitConversion & TypeIds.BOXING) != 0) {
			this.areturn();
			return;
		}
		int runtimeType = (implicitConversion & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4;
		switch (runtimeType) {
			case TypeIds.T_boolean :
			case TypeIds.T_int :
				this.ireturn();
				break;
			case TypeIds.T_float :
				this.freturn();
				break;
			case TypeIds.T_long :
				this.lreturn();
				break;
			case TypeIds.T_double :
				this.dreturn();
				break;
			default :
				this.areturn();
		}
	}
}
/**
 * The equivalent code performs a string conversion:
 *
 * @param blockScope the given blockScope
 * @param oper1 the first expression
 * @param oper2 the second expression
 */
public void generateStringConcatenationAppend(BlockScope blockScope, Expression oper1, Expression oper2) {
	int pc;
	if (oper1 == null) {
		/* Operand is already on the stack, and maybe nil:
		note type1 is always to  java.lang.String here.*/
		this.newStringContatenation();
		this.dup_x1();
		this.swap();
		// If argument is reference type, need to transform it 
		// into a string (handles null case)
		this.invokeStringValueOf(TypeIds.T_JavaLangObject);
		this.invokeStringConcatenationStringConstructor();
	} else {
		pc = position;
		oper1.generateOptimizedStringConcatenationCreation(blockScope, this, oper1.implicitConversion & TypeIds.COMPILE_TYPE_MASK);
		this.recordPositionsFrom(pc, oper1.sourceStart);
	}
	pc = position;
	oper2.generateOptimizedStringConcatenation(blockScope, this, oper2.implicitConversion & TypeIds.COMPILE_TYPE_MASK);
	this.recordPositionsFrom(pc, oper2.sourceStart);
	this.invokeStringConcatenationToString();
}
/**
 * @param accessBinding the access method binding to generate
 */
public void generateSyntheticBodyForConstructorAccess(SyntheticMethodBinding accessBinding) {

	initializeMaxLocals(accessBinding);

	MethodBinding constructorBinding = accessBinding.targetMethod;
	TypeBinding[] parameters = constructorBinding.parameters;
	int length = parameters.length;
	int resolvedPosition = 1;
	this.aload_0();
	// special name&ordinal argument generation for enum constructors
	TypeBinding declaringClass = constructorBinding.declaringClass;
	if (declaringClass.erasure().id == TypeIds.T_JavaLangEnum || declaringClass.isEnum()) {
		this.aload_1(); // pass along name param as name arg
		this.iload_2(); // pass along ordinal param as ordinal arg
		resolvedPosition += 2;
	}	
	if (declaringClass.isNestedType()) {
		NestedTypeBinding nestedType = (NestedTypeBinding) declaringClass;
		SyntheticArgumentBinding[] syntheticArguments = nestedType.syntheticEnclosingInstances();
		for (int i = 0; i < (syntheticArguments == null ? 0 : syntheticArguments.length); i++) {
			TypeBinding type;
			load((type = syntheticArguments[i].type), resolvedPosition);
			if ((type == TypeBinding.DOUBLE) || (type == TypeBinding.LONG))
				resolvedPosition += 2;
			else
				resolvedPosition++;
		}
	}
	for (int i = 0; i < length; i++) {
		load(parameters[i], resolvedPosition);
		if ((parameters[i] == TypeBinding.DOUBLE) || (parameters[i] == TypeBinding.LONG))
			resolvedPosition += 2;
		else
			resolvedPosition++;
	}
	
	if (declaringClass.isNestedType()) {
		NestedTypeBinding nestedType = (NestedTypeBinding) declaringClass;
		SyntheticArgumentBinding[] syntheticArguments = nestedType.syntheticOuterLocalVariables();
		for (int i = 0; i < (syntheticArguments == null ? 0 : syntheticArguments.length); i++) {
			TypeBinding type;
			load((type = syntheticArguments[i].type), resolvedPosition);
			if ((type == TypeBinding.DOUBLE) || (type == TypeBinding.LONG))
				resolvedPosition += 2;
			else
				resolvedPosition++;
		}
	}
	this.invokespecial(constructorBinding);
	this.return_();
}
//static X valueOf(String name) {
// return (X) Enum.valueOf(X.class, name);
//}		
public void generateSyntheticBodyForEnumValueOf(SyntheticMethodBinding methodBinding) {
	initializeMaxLocals(methodBinding);
	final ReferenceBinding declaringClass = methodBinding.declaringClass;
	this.ldc(declaringClass);
	this.aload_0();
	this.invokeJavaLangEnumvalueOf(declaringClass);
	this.checkcast(declaringClass);
	this.areturn();
}
//static X[] values() {
// X[] values;
// int length;
// X[] result;
// System.arraycopy(values = $VALUES, 0, result = new X[length= values.length], 0, length)
// return result;
//}
public void generateSyntheticBodyForEnumValues(SyntheticMethodBinding methodBinding) {
	ClassScope scope = ((SourceTypeBinding)methodBinding.declaringClass).scope;
	FieldBinding enumValuesSyntheticfield = scope.referenceContext.enumValuesSyntheticfield;
	initializeMaxLocals(methodBinding);
	TypeBinding enumArray = methodBinding.returnType;
	
	this.getstatic(enumValuesSyntheticfield);
	this.dup();
	this.astore_0();
	this.iconst_0();
	this.aload_0();
	this.arraylength();
	this.dup();
	this.istore_1();
	this.newArray((ArrayBinding) enumArray);
	this.dup();
	this.astore_2();
	this.iconst_0();
	this.iload_1();
	this.invokeSystemArraycopy();
	this.aload_2();
	this.areturn();
}
public void generateSyntheticBodyForFieldReadAccess(SyntheticMethodBinding accessBinding) {
	initializeMaxLocals(accessBinding);
	FieldBinding fieldBinding = accessBinding.targetReadField;
	if (fieldBinding.isStatic())
		this.getstatic(fieldBinding);
	else {
		this.aload_0();
		this.getfield(fieldBinding);
	}
	switch (fieldBinding.type.id) {
//		case T_void :
//			this.return_();
//			break;
		case TypeIds.T_boolean :
		case TypeIds.T_byte :
		case TypeIds.T_char :
		case TypeIds.T_short :
		case TypeIds.T_int :
			this.ireturn();
			break;
		case TypeIds.T_long :
			this.lreturn();
			break;
		case TypeIds.T_float :
			this.freturn();
			break;
		case TypeIds.T_double :
			this.dreturn();
			break;
		default :
			this.areturn();
	}	
}
public void generateSyntheticBodyForFieldWriteAccess(SyntheticMethodBinding accessBinding) {
	initializeMaxLocals(accessBinding);
	FieldBinding fieldBinding = accessBinding.targetWriteField;
	if (fieldBinding.isStatic()) {
		load(fieldBinding.type, 0);
		this.putstatic(fieldBinding);
	} else {
		this.aload_0();
		load(fieldBinding.type, 1);
		this.putfield(fieldBinding);
	}
	this.return_();
}
public void generateSyntheticBodyForMethodAccess(SyntheticMethodBinding accessMethod) {

	initializeMaxLocals(accessMethod);
	MethodBinding targetMethod = accessMethod.targetMethod;
	TypeBinding[] parameters = targetMethod.parameters;
	int length = parameters.length;
	TypeBinding[] arguments = accessMethod.purpose == SyntheticMethodBinding.BridgeMethod 
													? accessMethod.parameters
													: null;
	int resolvedPosition;
	if (targetMethod.isStatic())
		resolvedPosition = 0;
	else {
		this.aload_0();
		resolvedPosition = 1;
	}
	for (int i = 0; i < length; i++) {
	    TypeBinding parameter = parameters[i];
	    if (arguments != null) { // for bridge methods
		    TypeBinding argument = arguments[i];
			load(argument, resolvedPosition);
			if (argument != parameter) 
			    checkcast(parameter);
	    } else {
			load(parameter, resolvedPosition);
		}
		if ((parameter == TypeBinding.DOUBLE) || (parameter == TypeBinding.LONG))
			resolvedPosition += 2;
		else
			resolvedPosition++;
	}
	if (targetMethod.isStatic())
		this.invokestatic(targetMethod);
	else {
		if (targetMethod.isConstructor()
			|| targetMethod.isPrivate()
			// qualified super "X.super.foo()" targets methods from superclass
			|| accessMethod.purpose == SyntheticMethodBinding.SuperMethodAccess){
			this.invokespecial(targetMethod);
		} else {
			if (targetMethod.declaringClass.isInterface()) { // interface or annotation type
				this.invokeinterface(targetMethod);
			} else {
				this.invokevirtual(targetMethod);
			}
		}
	}
	switch (targetMethod.returnType.id) {
		case TypeIds.T_void :
			this.return_();
			break;
		case TypeIds.T_boolean :
		case TypeIds.T_byte :
		case TypeIds.T_char :
		case TypeIds.T_short :
		case TypeIds.T_int :
			this.ireturn();
			break;
		case TypeIds.T_long :
			this.lreturn();
			break;
		case TypeIds.T_float :
			this.freturn();
			break;
		case TypeIds.T_double :
			this.dreturn();
			break;
		default :
			TypeBinding accessErasure = accessMethod.returnType.erasure();
			TypeBinding match = targetMethod.returnType.findSuperTypeOriginatingFrom(accessErasure);
			if (match == null) {
				this.checkcast(accessErasure); // for bridge methods
			}
			this.areturn();
	}
}
public void generateSyntheticBodyForSwitchTable(SyntheticMethodBinding methodBinding) {
	ClassScope scope = ((SourceTypeBinding)methodBinding.declaringClass).scope;
	initializeMaxLocals(methodBinding);
	final BranchLabel nullLabel = new BranchLabel(this);
	FieldBinding syntheticFieldBinding = methodBinding.targetReadField;

	this.getstatic(syntheticFieldBinding);
	this.dup();
	this.ifnull(nullLabel);
	this.areturn();
	this.pushOnStack(syntheticFieldBinding.type);
	nullLabel.place();
	this.pop();
	ReferenceBinding enumBinding = (ReferenceBinding) methodBinding.targetEnumType;
	ArrayBinding arrayBinding = scope.createArrayType(enumBinding, 1);
	this.invokeJavaLangEnumValues(enumBinding, arrayBinding);
	this.arraylength();
	this.newarray(ClassFileConstants.INT_ARRAY);
	this.astore_0();
	LocalVariableBinding localVariableBinding = new LocalVariableBinding(" tab".toCharArray(), scope.createArrayType(TypeBinding.INT, 1), 0, false); //$NON-NLS-1$
	this.addVariable(localVariableBinding);
	final FieldBinding[] fields = enumBinding.fields();
	if (fields != null) {
		for (int i = 0, max = fields.length; i < max; i++) {
			FieldBinding fieldBinding = fields[i];
			if ((fieldBinding.getAccessFlags() & ClassFileConstants.AccEnum) != 0) {
				final BranchLabel endLabel = new BranchLabel(this);
				final ExceptionLabel anyExceptionHandler = new ExceptionLabel(this, TypeBinding.LONG /* represents NoSuchFieldError*/);
				anyExceptionHandler.placeStart();
				this.aload_0();
				this.getstatic(fieldBinding);
				this.invokeEnumOrdinal(enumBinding.constantPoolName());
				this.generateInlinedValue(fieldBinding.id + 1); // zero should not be returned see bug 141810
				this.iastore();
				anyExceptionHandler.placeEnd();
				this.goto_(endLabel);
				// Generate the body of the exception handler
				this.pushExceptionOnStack(TypeBinding.LONG /*represents NoSuchFieldError*/);
				anyExceptionHandler.place();
				this.pop(); // we don't use it so we can pop it
				endLabel.place();
			}
		}
	}
	this.aload_0();
	this.dup();
	this.putstatic(syntheticFieldBinding);
	areturn();
	this.removeVariable(localVariableBinding);
}
/**
 * Code responsible to generate the suitable code to supply values for the synthetic enclosing
 * instance arguments of a constructor invocation of a nested type.
 */
public void generateSyntheticEnclosingInstanceValues(
		BlockScope currentScope, 
		ReferenceBinding targetType, 
		Expression enclosingInstance, 
		ASTNode invocationSite) {

	// supplying enclosing instance for the anonymous type's superclass
	ReferenceBinding checkedTargetType = targetType.isAnonymousType() ? (ReferenceBinding)targetType.superclass().erasure() : targetType;
	boolean hasExtraEnclosingInstance = enclosingInstance != null;
	if (hasExtraEnclosingInstance 
			&& (!checkedTargetType.isNestedType() || checkedTargetType.isStatic())) {
		currentScope.problemReporter().unnecessaryEnclosingInstanceSpecification(enclosingInstance, checkedTargetType);
		return;
	}

	// perform some emulation work in case there is some and we are inside a local type only
	ReferenceBinding[] syntheticArgumentTypes;
	if ((syntheticArgumentTypes = targetType.syntheticEnclosingInstanceTypes()) != null) {

		ReferenceBinding targetEnclosingType = checkedTargetType.enclosingType();
		long compliance = currentScope.compilerOptions().complianceLevel;

		// deny access to enclosing instance argument for allocation and super constructor call (if 1.4)
		// always consider it if complying to 1.5
		boolean denyEnclosingArgInConstructorCall;
		if (compliance <= ClassFileConstants.JDK1_3) {
			denyEnclosingArgInConstructorCall = invocationSite instanceof AllocationExpression;
		} else if (compliance == ClassFileConstants.JDK1_4){
			denyEnclosingArgInConstructorCall = invocationSite instanceof AllocationExpression
				|| invocationSite instanceof ExplicitConstructorCall && ((ExplicitConstructorCall)invocationSite).isSuperAccess();
		} else {
			//compliance >= JDK1_5
			denyEnclosingArgInConstructorCall = (invocationSite instanceof AllocationExpression
					|| invocationSite instanceof ExplicitConstructorCall && ((ExplicitConstructorCall)invocationSite).isSuperAccess()) 
				&& !targetType.isLocalType();
		}
		
		boolean complyTo14 = compliance >= ClassFileConstants.JDK1_4;
		for (int i = 0, max = syntheticArgumentTypes.length; i < max; i++) {
			ReferenceBinding syntheticArgType = syntheticArgumentTypes[i];
			if (hasExtraEnclosingInstance && syntheticArgType == targetEnclosingType) {
				hasExtraEnclosingInstance = false;
				enclosingInstance.generateCode(currentScope, this, true);
				if (complyTo14){
					dup();
					invokeObjectGetClass(); // will perform null check
					pop();
				}
			} else {
				Object[] emulationPath = currentScope.getEmulationPath(
						syntheticArgType, 
						false /*not only exact match (that is, allow compatible)*/,
						denyEnclosingArgInConstructorCall);
				this.generateOuterAccess(emulationPath, invocationSite, syntheticArgType, currentScope);
			}
		}
		if (hasExtraEnclosingInstance){
			currentScope.problemReporter().unnecessaryEnclosingInstanceSpecification(enclosingInstance, checkedTargetType);
		}
	}
}
/**
 * Code responsible to generate the suitable code to supply values for the synthetic outer local
 * variable arguments of a constructor invocation of a nested type.
 * (bug 26122) - synthetic values for outer locals must be passed after user arguments, e.g. new X(i = 1){}
 */
public void generateSyntheticOuterArgumentValues(BlockScope currentScope, ReferenceBinding targetType, ASTNode invocationSite) {

	// generate the synthetic outer arguments then
	SyntheticArgumentBinding syntheticArguments[];
	if ((syntheticArguments = targetType.syntheticOuterLocalVariables()) != null) {
		for (int i = 0, max = syntheticArguments.length; i < max; i++) {
			LocalVariableBinding targetVariable = syntheticArguments[i].actualOuterLocalVariable;
			VariableBinding[] emulationPath = currentScope.getEmulationPath(targetVariable);
			this.generateOuterAccess(emulationPath, invocationSite, targetVariable, currentScope);
		}
	}
}
public void generateUnboxingConversion(int unboxedTypeID) {
	switch (unboxedTypeID) {
		case TypeIds.T_byte :
			// invokevirtual: byteValue()
			this.invoke(
					Opcodes.OPC_invokevirtual,
					0, // argCount
					1, // return type size
					ConstantPool.JavaLangByteConstantPoolName,
					ConstantPool.BYTEVALUE_BYTE_METHOD_NAME,
					ConstantPool.BYTEVALUE_BYTE_METHOD_SIGNATURE);
			break;
		case TypeIds.T_short :
			// invokevirtual: shortValue()
			this.invoke(
					Opcodes.OPC_invokevirtual,
					0, // argCount
					1, // return type size
					ConstantPool.JavaLangShortConstantPoolName,
					ConstantPool.SHORTVALUE_SHORT_METHOD_NAME,
					ConstantPool.SHORTVALUE_SHORT_METHOD_SIGNATURE);
			break;
		case TypeIds.T_char :
			// invokevirtual: charValue()
			this.invoke(
					Opcodes.OPC_invokevirtual,
					0, // argCount
					1, // return type size
					ConstantPool.JavaLangCharacterConstantPoolName,
					ConstantPool.CHARVALUE_CHARACTER_METHOD_NAME,
					ConstantPool.CHARVALUE_CHARACTER_METHOD_SIGNATURE);
			break;
		case TypeIds.T_int :
			// invokevirtual: intValue()
			this.invoke(
					Opcodes.OPC_invokevirtual,
					0, // argCount
					1, // return type size
					ConstantPool.JavaLangIntegerConstantPoolName,
					ConstantPool.INTVALUE_INTEGER_METHOD_NAME,
					ConstantPool.INTVALUE_INTEGER_METHOD_SIGNATURE);
			break;
		case TypeIds.T_long :
			// invokevirtual: longValue()
			this.invoke(
					Opcodes.OPC_invokevirtual,
					0, // argCount
					2, // return type size
					ConstantPool.JavaLangLongConstantPoolName,
					ConstantPool.LONGVALUE_LONG_METHOD_NAME,
					ConstantPool.LONGVALUE_LONG_METHOD_SIGNATURE);
			break;
		case TypeIds.T_float :
			// invokevirtual: floatValue()
			this.invoke(
					Opcodes.OPC_invokevirtual,
					0, // argCount
					1, // return type size
					ConstantPool.JavaLangFloatConstantPoolName,
					ConstantPool.FLOATVALUE_FLOAT_METHOD_NAME,
					ConstantPool.FLOATVALUE_FLOAT_METHOD_SIGNATURE);
			break;
		case TypeIds.T_double :
			// invokevirtual: doubleValue()
			this.invoke(
					Opcodes.OPC_invokevirtual,
					0, // argCount
					2, // return type size
					ConstantPool.JavaLangDoubleConstantPoolName,
					ConstantPool.DOUBLEVALUE_DOUBLE_METHOD_NAME,
					ConstantPool.DOUBLEVALUE_DOUBLE_METHOD_SIGNATURE);
			break;
		case TypeIds.T_boolean :
			// invokevirtual: booleanValue()
			this.invoke(
					Opcodes.OPC_invokevirtual,
					0, // argCount
					1, // return type size
					ConstantPool.JavaLangBooleanConstantPoolName,
					ConstantPool.BOOLEANVALUE_BOOLEAN_METHOD_NAME,
					ConstantPool.BOOLEANVALUE_BOOLEAN_METHOD_SIGNATURE);
	}
}
/*
 * Wide conditional branch compare, improved by swapping comparison opcode
 *   ifeq WideTarget
 * becomes
 *    ifne Intermediate
 *    gotow WideTarget
 *    Intermediate:
 */
public void generateWideRevertedConditionalBranch(byte revertedOpcode, BranchLabel wideTarget) {
		BranchLabel intermediate = new BranchLabel(this);
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = revertedOpcode;
		intermediate.branch();
		this.goto_w(wideTarget);
		intermediate.place();
}
public void getBaseTypeValue(int baseTypeID) {
	switch (baseTypeID) {
		case TypeIds.T_byte :
			// invokevirtual: byteValue()
			this.invoke(
					Opcodes.OPC_invokevirtual,
					0, // argCount
					1, // return type size
					ConstantPool.JavaLangByteConstantPoolName,
					ConstantPool.BYTEVALUE_BYTE_METHOD_NAME,
					ConstantPool.BYTEVALUE_BYTE_METHOD_SIGNATURE);
			break;
		case TypeIds.T_short :
			// invokevirtual: shortValue()
			this.invoke(
					Opcodes.OPC_invokevirtual,
					0, // argCount
					1, // return type size
					ConstantPool.JavaLangShortConstantPoolName,
					ConstantPool.SHORTVALUE_SHORT_METHOD_NAME,
					ConstantPool.SHORTVALUE_SHORT_METHOD_SIGNATURE);
			break;
		case TypeIds.T_char :
			// invokevirtual: charValue()
			this.invoke(
					Opcodes.OPC_invokevirtual,
					0, // argCount
					1, // return type size
					ConstantPool.JavaLangCharacterConstantPoolName,
					ConstantPool.CHARVALUE_CHARACTER_METHOD_NAME,
					ConstantPool.CHARVALUE_CHARACTER_METHOD_SIGNATURE);
			break;
		case TypeIds.T_int :
			// invokevirtual: intValue()
			this.invoke(
					Opcodes.OPC_invokevirtual,
					0, // argCount
					1, // return type size
					ConstantPool.JavaLangIntegerConstantPoolName,
					ConstantPool.INTVALUE_INTEGER_METHOD_NAME,
					ConstantPool.INTVALUE_INTEGER_METHOD_SIGNATURE);
			break;
		case TypeIds.T_long :
			// invokevirtual: longValue()
			this.invoke(
					Opcodes.OPC_invokevirtual,
					0, // argCount
					2, // return type size
					ConstantPool.JavaLangLongConstantPoolName,
					ConstantPool.LONGVALUE_LONG_METHOD_NAME,
					ConstantPool.LONGVALUE_LONG_METHOD_SIGNATURE);
			break;
		case TypeIds.T_float :
			// invokevirtual: floatValue()
			this.invoke(
					Opcodes.OPC_invokevirtual,
					0, // argCount
					1, // return type size
					ConstantPool.JavaLangFloatConstantPoolName,
					ConstantPool.FLOATVALUE_FLOAT_METHOD_NAME,
					ConstantPool.FLOATVALUE_FLOAT_METHOD_SIGNATURE);
			break;
		case TypeIds.T_double :
			// invokevirtual: doubleValue()
			this.invoke(
					Opcodes.OPC_invokevirtual,
					0, // argCount
					2, // return type size
					ConstantPool.JavaLangDoubleConstantPoolName,
					ConstantPool.DOUBLEVALUE_DOUBLE_METHOD_NAME,
					ConstantPool.DOUBLEVALUE_DOUBLE_METHOD_SIGNATURE);
			break;
		case TypeIds.T_boolean :
			// invokevirtual: booleanValue()
			this.invoke(
					Opcodes.OPC_invokevirtual,
					0, // argCount
					1, // return type size
					ConstantPool.JavaLangBooleanConstantPoolName,
					ConstantPool.BOOLEANVALUE_BOOLEAN_METHOD_NAME,
					ConstantPool.BOOLEANVALUE_BOOLEAN_METHOD_SIGNATURE);
	}
}
final public byte[] getContents() {
	byte[] contents;
	System.arraycopy(bCodeStream, 0, contents = new byte[position], 0, position);
	return contents;
}
public void getfield(FieldBinding fieldBinding) {
	if (DEBUG) System.out.println(position + "\t\tgetfield:"+fieldBinding); //$NON-NLS-1$
	int returnTypeSize = 1;
	if ((fieldBinding.type.id == TypeIds.T_double) || (fieldBinding.type.id == TypeIds.T_long)) {
		returnTypeSize = 2;
	}
	generateFieldAccess(
			Opcodes.OPC_getfield,
			returnTypeSize,
			fieldBinding.declaringClass,
			fieldBinding.name,
			fieldBinding.type);
}
protected int getPosition() {
	return this.position;
}
public void getstatic(FieldBinding fieldBinding) {
	if (DEBUG) System.out.println(position + "\t\tgetstatic:"+fieldBinding); //$NON-NLS-1$
	int returnTypeSize = 1;
	if ((fieldBinding.type.id == TypeIds.T_double) || (fieldBinding.type.id == TypeIds.T_long)) {
		returnTypeSize = 2;
	}
	generateFieldAccess(
			Opcodes.OPC_getstatic,
			returnTypeSize,
			fieldBinding.declaringClass,
			fieldBinding.name,
			fieldBinding.type);
}
public void getTYPE(int baseTypeID) {
	countLabels = 0;
	switch (baseTypeID) {
		case TypeIds.T_byte :
			// getstatic: java.lang.Byte.TYPE			
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Byte.TYPE"); //$NON-NLS-1$
			generateFieldAccess(
					Opcodes.OPC_getstatic,
					1,
					ConstantPool.JavaLangByteConstantPoolName,
					ConstantPool.TYPE,
					ConstantPool.JavaLangClassSignature);
			break;
		case TypeIds.T_short :
			// getstatic: java.lang.Short.TYPE			
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Short.TYPE"); //$NON-NLS-1$
			generateFieldAccess(
					Opcodes.OPC_getstatic,
					1,
					ConstantPool.JavaLangShortConstantPoolName,
					ConstantPool.TYPE,
					ConstantPool.JavaLangClassSignature);
			break;
		case TypeIds.T_char :
			// getstatic: java.lang.Character.TYPE			
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Character.TYPE"); //$NON-NLS-1$
			generateFieldAccess(
					Opcodes.OPC_getstatic,
					1,
					ConstantPool.JavaLangCharacterConstantPoolName,
					ConstantPool.TYPE,
					ConstantPool.JavaLangClassSignature);
			break;
		case TypeIds.T_int :
			// getstatic: java.lang.Integer.TYPE			
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Integer.TYPE"); //$NON-NLS-1$
			generateFieldAccess(
					Opcodes.OPC_getstatic,
					1,
					ConstantPool.JavaLangIntegerConstantPoolName,
					ConstantPool.TYPE,
					ConstantPool.JavaLangClassSignature);
			break;
		case TypeIds.T_long :
			// getstatic: java.lang.Long.TYPE			
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Long.TYPE"); //$NON-NLS-1$
			generateFieldAccess(
					Opcodes.OPC_getstatic,
					1,
					ConstantPool.JavaLangLongConstantPoolName,
					ConstantPool.TYPE,
					ConstantPool.JavaLangClassSignature);
			break;
		case TypeIds.T_float :
			// getstatic: java.lang.Float.TYPE			
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Float.TYPE"); //$NON-NLS-1$
			generateFieldAccess(
					Opcodes.OPC_getstatic,
					1,
					ConstantPool.JavaLangFloatConstantPoolName,
					ConstantPool.TYPE,
					ConstantPool.JavaLangClassSignature);
			break;
		case TypeIds.T_double :
			// getstatic: java.lang.Double.TYPE			
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Double.TYPE"); //$NON-NLS-1$
			generateFieldAccess(
					Opcodes.OPC_getstatic,
					1,
					ConstantPool.JavaLangDoubleConstantPoolName,
					ConstantPool.TYPE,
					ConstantPool.JavaLangClassSignature);
			break;
		case TypeIds.T_boolean :
			// getstatic: java.lang.Boolean.TYPE			
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Boolean.TYPE"); //$NON-NLS-1$
			generateFieldAccess(
					Opcodes.OPC_getstatic,
					1,
					ConstantPool.JavaLangBooleanConstantPoolName,
					ConstantPool.TYPE,
					ConstantPool.JavaLangClassSignature);
			break;
		case TypeIds.T_void :
			// getstatic: java.lang.Void.TYPE
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Void.TYPE"); //$NON-NLS-1$
			generateFieldAccess(
					Opcodes.OPC_getstatic,
					1,
					ConstantPool.JavaLangVoidConstantPoolName,
					ConstantPool.TYPE,
					ConstantPool.JavaLangClassSignature);
			break;
	}
}
/**
 * We didn't call it goto, because there is a conflit with the goto keyword
 */
public void goto_(BranchLabel label) {
	if (this.wideMode) {
		this.goto_w(label);
		return;
	}
	if (DEBUG) System.out.println(position + "\t\tgoto:"+label); //$NON-NLS-1$
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	boolean chained = this.inlineForwardReferencesFromLabelsTargeting(label, position);
	if (DEBUG && chained) {
		if (DEBUG) {
			if (this.lastAbruptCompletion == this.position) {
				System.out.println("\t\t\t\t<branch chaining - goto eliminated : "+this.position+","+label+">");//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$				
			} else {
				System.out.println("\t\t\t\t<branch chaining - goto issued : "+this.position+","+label+">");//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			}
		}		
	}
	/*
	 Possible optimization for code such as:
	 public Object foo() {
		boolean b = true;
		if (b) {
			if (b)
				return null;
		} else {
			if (b) {
				return null;
			}
		}
		return null;
	}
	The goto around the else block for the first if will
	be unreachable, because the thenClause of the second if
	returns. Also see 114894
	}*/
	if (chained && this.lastAbruptCompletion == this.position) {
		if (label.position != Label.POS_NOT_SET) { // ensure existing forward references are updated
			int[] forwardRefs = label.forwardReferences();
			for (int i = 0, max = label.forwardReferenceCount(); i < max; i++) {
				this.writePosition(label, forwardRefs[i]);
			}
			this.countLabels = 0; // backward jump, no further chaining allowed
		}
		return;
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_goto;
	label.branch();
	this.lastAbruptCompletion = this.position;
}
public void goto_w(BranchLabel label) {
	if (DEBUG) System.out.println(position + "\t\tgotow:"+label); //$NON-NLS-1$
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_goto_w;
	label.branchWide();
	this.lastAbruptCompletion = this.position;	
}
public void i2b() {
	if (DEBUG) System.out.println(position + "\t\ti2b"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_i2b;
}
public void i2c() {
	if (DEBUG) System.out.println(position + "\t\ti2c"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_i2c;
}
public void i2d() {
	if (DEBUG) System.out.println(position + "\t\ti2d"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_i2d;
}
public void i2f() {
	if (DEBUG) System.out.println(position + "\t\ti2f"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_i2f;
}
public void i2l() {
	if (DEBUG) System.out.println(position + "\t\ti2l"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_i2l;
}
public void i2s() {
	if (DEBUG) System.out.println(position + "\t\ti2s"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_i2s;
}
public void iadd() {
	if (DEBUG) System.out.println(position + "\t\tiadd"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_iadd;
}
public void iaload() {
	if (DEBUG) System.out.println(position + "\t\tiaload"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_iaload;
}
public void iand() {
	if (DEBUG) System.out.println(position + "\t\tiand"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_iand;
}
public void iastore() {
	if (DEBUG) System.out.println(position + "\t\tiastore"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_iastore;
}
public void iconst_0() {
	if (DEBUG) System.out.println(position + "\t\ticonst_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_iconst_0;
}
public void iconst_1() {
	if (DEBUG) System.out.println(position + "\t\ticonst_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_iconst_1;
}
public void iconst_2() {
	if (DEBUG) System.out.println(position + "\t\ticonst_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_iconst_2;
}
public void iconst_3() {
	if (DEBUG) System.out.println(position + "\t\ticonst_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_iconst_3;
}
public void iconst_4() {
	if (DEBUG) System.out.println(position + "\t\ticonst_4"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_iconst_4;
}
public void iconst_5() {
	if (DEBUG) System.out.println(position + "\t\ticonst_5"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_iconst_5;
}
public void iconst_m1() {
	if (DEBUG) System.out.println(position + "\t\ticonst_m1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_iconst_m1;
}
public void idiv() {
	if (DEBUG) System.out.println(position + "\t\tidiv"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_idiv;
}
public void if_acmpeq(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tif_acmpeq:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth-=2;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(Opcodes.OPC_if_acmpne, lbl);
	} else {	
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_if_acmpeq;
		lbl.branch();
	}
}
public void if_acmpne(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tif_acmpne:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth-=2;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(Opcodes.OPC_if_acmpeq, lbl);
	} else {	
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_if_acmpne;
		lbl.branch();
	}
}
public void if_icmpeq(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tif_cmpeq:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(Opcodes.OPC_if_icmpne, lbl);
	} else {	
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_if_icmpeq;
		lbl.branch();
	}
}
public void if_icmpge(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tif_icmpge:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(Opcodes.OPC_if_icmplt, lbl);
	} else {	
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_if_icmpge;
		lbl.branch();
	}
}
public void if_icmpgt(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tif_icmpgt:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(Opcodes.OPC_if_icmple, lbl);
	} else {	
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_if_icmpgt;
		lbl.branch();
	}
}
public void if_icmple(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tif_icmple:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(Opcodes.OPC_if_icmpgt, lbl);
	} else {	
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_if_icmple;
		lbl.branch();
	}
}
public void if_icmplt(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tif_icmplt:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(Opcodes.OPC_if_icmpge, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_if_icmplt;
		lbl.branch();
	}
}
public void if_icmpne(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tif_icmpne:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(Opcodes.OPC_if_icmpeq, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_if_icmpne;
		lbl.branch();
	}
}
public void ifeq(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tifeq:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(Opcodes.OPC_ifne, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ifeq;
		lbl.branch();
	}
}
public void ifge(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tifge:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(Opcodes.OPC_iflt, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ifge;
		lbl.branch();
	}
}
public void ifgt(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tifgt:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(Opcodes.OPC_ifle, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ifgt;
		lbl.branch();
	}
}
public void ifle(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tifle:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(Opcodes.OPC_ifgt, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ifle;
		lbl.branch();
	}
}
public void iflt(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tiflt:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(Opcodes.OPC_ifge, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_iflt;
		lbl.branch();
	}
}
public void ifne(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tifne:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(Opcodes.OPC_ifeq, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ifne;
		lbl.branch();
	}
}
public void ifnonnull(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tifnonnull:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(Opcodes.OPC_ifnull, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ifnonnull;
		lbl.branch();
	}
}
public void ifnull(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tifnull:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(Opcodes.OPC_ifnonnull, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ifnull;
		lbl.branch();
	}
}
final public void iinc(int index, int value) {
	if (DEBUG) System.out.println(position + "\t\tiinc:"+index+","+value); //$NON-NLS-1$ //$NON-NLS-2$
	countLabels = 0;
	if ((index > 255) || (value < -128 || value > 127)) { // have to widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_wide;
		bCodeStream[classFileOffset++] = Opcodes.OPC_iinc;
		writeUnsignedShort(index);
		writeSignedShort(value);
	} else {
		if (classFileOffset + 2 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 3;
		bCodeStream[classFileOffset++] = Opcodes.OPC_iinc;
		bCodeStream[classFileOffset++] = (byte) index;
		bCodeStream[classFileOffset++] = (byte) value;
	}
}
public void iload(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tiload:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_wide;
		bCodeStream[classFileOffset++] = Opcodes.OPC_iload;
		writeUnsignedShort(iArg);
	} else {
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_iload;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
public void iload_0() {
	if (DEBUG) System.out.println(position + "\t\tiload_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 0) {
		maxLocals = 1;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_iload_0;
}
public void iload_1() {
	if (DEBUG) System.out.println(position + "\t\tiload_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_iload_1;
}
public void iload_2() {
	if (DEBUG) System.out.println(position + "\t\tiload_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_iload_2;
}
public void iload_3() {
	if (DEBUG) System.out.println(position + "\t\tiload_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_iload_3;
}
public void imul() {
	if (DEBUG) System.out.println(position + "\t\timul"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_imul;
}
public int indexOfSameLineEntrySincePC(int pc, int line) {
	for (int index = pc, max = pcToSourceMapSize; index < max; index+=2) {
		if (pcToSourceMap[index+1] == line)
			return index;
	}
	return -1;
}
public void ineg() {
	if (DEBUG) System.out.println(position + "\t\tineg"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_ineg;
}
/*
 * Some placed labels might be branching to a goto bytecode which we can optimize better.
 */
public boolean inlineForwardReferencesFromLabelsTargeting(BranchLabel targetLabel, int gotoLocation) {
	if (targetLabel.delegate != null) return false; // already inlined
	int chaining = L_UNKNOWN;
	for (int i = this.countLabels - 1; i >= 0; i--) {
		BranchLabel currentLabel = labels[i];
		if (currentLabel.position != gotoLocation) break;
		if (currentLabel == targetLabel) {
			chaining |= L_CANNOT_OPTIMIZE; // recursive
			continue;
		} 
		if (currentLabel.isStandardLabel()) {
			if (currentLabel.delegate != null) continue; // ignore since already inlined
			targetLabel.becomeDelegateFor(currentLabel);
			chaining |= L_OPTIMIZABLE; // optimizable, providing no vetoing
			continue;
		}
		// case label
		chaining |= L_CANNOT_OPTIMIZE;
	}
	return (chaining & (L_OPTIMIZABLE|L_CANNOT_OPTIMIZE)) == L_OPTIMIZABLE; // check was some standards, and no case/recursive
}
public void init(ClassFile targetClassFile) {
	this.classFile = targetClassFile;
	this.constantPool = targetClassFile.constantPool;
	this.bCodeStream = targetClassFile.contents;
	this.classFileOffset = targetClassFile.contentsOffset;
	this.startingClassFileOffset = this.classFileOffset;
	pcToSourceMapSize = 0;
	lastEntryPC = 0;
	int length = visibleLocals.length;
	if (noVisibleLocals.length < length) {
		noVisibleLocals = new LocalVariableBinding[length];
	}
	System.arraycopy(noVisibleLocals, 0, visibleLocals, 0, length);
	visibleLocalsCount = 0;
	
	length = locals.length;
	if (noLocals.length < length) {
		noLocals = new LocalVariableBinding[length];
	}
	System.arraycopy(noLocals, 0, locals, 0, length);
	allLocalsCounter = 0;

	length = exceptionLabels.length;
	if (noExceptionHandlers.length < length) {
		noExceptionHandlers = new ExceptionLabel[length];
	}
	System.arraycopy(noExceptionHandlers, 0, exceptionLabels, 0, length);
	exceptionLabelsCounter = 0;
	
	length = labels.length;
	if (noLabels.length < length) {
		noLabels = new BranchLabel[length];
	}
	System.arraycopy(noLabels, 0, labels, 0, length);
	countLabels = 0;
	this.lastAbruptCompletion = -1;

	stackMax = 0;
	stackDepth = 0;
	maxLocals = 0;
	position = 0;
}
/**
 * @param methodBinding the given method binding to initialize the max locals
 */
public void initializeMaxLocals(MethodBinding methodBinding) {

	if (methodBinding == null) {
		this.maxLocals = 0;
		return;
	}
	
	this.maxLocals = methodBinding.isStatic() ? 0 : 1;
	
	// take into account enum constructor synthetic name+ordinal
	if (methodBinding.isConstructor() && methodBinding.declaringClass.isEnum()) {
		this.maxLocals += 2; // String and int (enum constant name+ordinal)
	}
	
	// take into account the synthetic parameters
	if (methodBinding.isConstructor() && methodBinding.declaringClass.isNestedType()) {
		ReferenceBinding enclosingInstanceTypes[];
		if ((enclosingInstanceTypes = methodBinding.declaringClass.syntheticEnclosingInstanceTypes()) != null) {
			for (int i = 0, max = enclosingInstanceTypes.length; i < max; i++) {
				this.maxLocals++; // an enclosingInstanceType can only be a reference binding. It cannot be
				// LongBinding or DoubleBinding
			}
		}
		SyntheticArgumentBinding syntheticArguments[];
		if ((syntheticArguments = methodBinding.declaringClass.syntheticOuterLocalVariables()) != null) {
			for (int i = 0, max = syntheticArguments.length; i < max; i++) {
				TypeBinding argType;
				if (((argType = syntheticArguments[i].type) == TypeBinding.LONG) || (argType == TypeBinding.DOUBLE)) {
					this.maxLocals += 2;
				} else {
					this.maxLocals++;
				}
			}
		}
	}
	TypeBinding[] arguments;
	if ((arguments = methodBinding.parameters) != null) {
		for (int i = 0, max = arguments.length; i < max; i++) {
			TypeBinding argType;
			if (((argType = arguments[i]) == TypeBinding.LONG) || (argType == TypeBinding.DOUBLE)) {
				this.maxLocals += 2;
			} else {
				this.maxLocals++;
			}
		}
	}
}
/**
 * We didn't call it instanceof because there is a conflit with the
 * instanceof keyword
 */
public void instance_of(TypeBinding typeBinding) {
	if (DEBUG) System.out.println(position + "\t\tinstance_of:"+typeBinding); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_instanceof;
	writeUnsignedShort(constantPool.literalIndexForType(typeBinding));
}
protected void invoke(int opcode, int argsSize, int returnTypeSize, char[] declaringClass, char[] selector, char[] signature) {
	countLabels = 0;
	int argCount = argsSize;
	switch(opcode) {
		case Opcodes.OPC_invokeinterface :
			if (classFileOffset + 4 >= bCodeStream.length) {
				resizeByteArray();
			}
			position +=3;
			bCodeStream[classFileOffset++] = Opcodes.OPC_invokeinterface;
			writeUnsignedShort(constantPool.literalIndexForMethod(declaringClass, selector, signature, true));
			argCount++;
			bCodeStream[classFileOffset++] = (byte) argCount;
			bCodeStream[classFileOffset++] = 0;
			break;
		case Opcodes.OPC_invokevirtual :
		case Opcodes.OPC_invokespecial :
			if (classFileOffset + 2 >= bCodeStream.length) {
				resizeByteArray();
			}
			position++;
			bCodeStream[classFileOffset++] = (byte) opcode;
			writeUnsignedShort(constantPool.literalIndexForMethod(declaringClass, selector, signature, false));
			argCount++;
			break;
		case Opcodes.OPC_invokestatic :
			if (classFileOffset + 2 >= bCodeStream.length) {
				resizeByteArray();
			}
			position++;
			bCodeStream[classFileOffset++] = Opcodes.OPC_invokestatic;
			writeUnsignedShort(constantPool.literalIndexForMethod(declaringClass, selector, signature, false));
	}
	stackDepth += returnTypeSize - argCount;
	if (stackDepth > stackMax) {
		stackMax = stackDepth;
	}
}
protected void invokeAccessibleObjectSetAccessible() {
	// invokevirtual: java.lang.reflect.AccessibleObject.setAccessible(Z)V;
	this.invoke(
			Opcodes.OPC_invokevirtual,
			1, // argCount
			0, // return type size
			ConstantPool.JAVALANGREFLECTACCESSIBLEOBJECT_CONSTANTPOOLNAME,
			ConstantPool.SETACCESSIBLE_NAME,
			ConstantPool.SETACCESSIBLE_SIGNATURE);
}
protected void invokeArrayNewInstance() {
	// invokestatic: java.lang.reflect.Array.newInstance(Ljava.lang.Class;int[])Ljava.lang.Object;
	this.invoke(
			Opcodes.OPC_invokestatic,
			2, // argCount
			1, // return type size
			ConstantPool.JAVALANGREFLECTARRAY_CONSTANTPOOLNAME,
			ConstantPool.NewInstance,
			ConstantPool.NewInstanceSignature);
}
public void invokeClassForName() {
	// invokestatic: java.lang.Class.forName(Ljava.lang.String;)Ljava.lang.Class;
	if (DEBUG) System.out.println(position + "\t\tinvokestatic: java.lang.Class.forName(Ljava.lang.String;)Ljava.lang.Class;"); //$NON-NLS-1$
	this.invoke(
		Opcodes.OPC_invokestatic,
		1, // argCount
		1, // return type size
		ConstantPool.JavaLangClassConstantPoolName,
		ConstantPool.ForName,
		ConstantPool.ForNameSignature);
}
protected void invokeClassGetDeclaredConstructor() {
	// invokevirtual: java.lang.Class getDeclaredConstructor([Ljava.lang.Class)Ljava.lang.reflect.Constructor;
	this.invoke(
			Opcodes.OPC_invokevirtual,
			1, // argCount
			1, // return type size
			ConstantPool.JavaLangClassConstantPoolName,
			ConstantPool.GETDECLAREDCONSTRUCTOR_NAME,
			ConstantPool.GETDECLAREDCONSTRUCTOR_SIGNATURE);
}
protected void invokeClassGetDeclaredField() {
	// invokevirtual: java.lang.Class.getDeclaredField(Ljava.lang.String)Ljava.lang.reflect.Field;
	this.invoke(
			Opcodes.OPC_invokevirtual,
			1, // argCount
			1, // return type size
			ConstantPool.JavaLangClassConstantPoolName,
			ConstantPool.GETDECLAREDFIELD_NAME,
			ConstantPool.GETDECLAREDFIELD_SIGNATURE);
}
protected void invokeClassGetDeclaredMethod() {
	// invokevirtual: java.lang.Class getDeclaredMethod(Ljava.lang.String, [Ljava.lang.Class)Ljava.lang.reflect.Method;
	this.invoke(
			Opcodes.OPC_invokevirtual,
			2, // argCount
			1, // return type size
			ConstantPool.JavaLangClassConstantPoolName,
			ConstantPool.GETDECLAREDMETHOD_NAME,
			ConstantPool.GETDECLAREDMETHOD_SIGNATURE);
}
public void invokeEnumOrdinal(char[] enumTypeConstantPoolName) {
	// invokevirtual: <enumConstantPoolName>.ordinal()
	if (DEBUG) System.out.println(position + "\t\tinvokevirtual: "+new String(enumTypeConstantPoolName)+".ordinal()"); //$NON-NLS-1$ //$NON-NLS-2$
	this.invoke(
			Opcodes.OPC_invokevirtual,
			0, // argCount
			1, // return type size
			enumTypeConstantPoolName,
			ConstantPool.Ordinal,
			ConstantPool.OrdinalSignature);
}
public void invokeinterface(MethodBinding methodBinding) {
	if (DEBUG) System.out.println(position + "\t\tinvokeinterface: " + methodBinding); //$NON-NLS-1$
	countLabels = 0;
	// initialized to 1 to take into account this  immediately
	int argCount = 1;
	int id;
	if (classFileOffset + 4 >= bCodeStream.length) {
		resizeByteArray();
	}
	position += 3;
	bCodeStream[classFileOffset++] = Opcodes.OPC_invokeinterface;
	writeUnsignedShort(
		constantPool.literalIndexForMethod(
			methodBinding.constantPoolDeclaringClass(),
			methodBinding.selector,
			methodBinding.signature(classFile),
			true));
	for (int i = methodBinding.parameters.length - 1; i >= 0; i--)
		if (((id = methodBinding.parameters[i].id) == TypeIds.T_double) || (id == TypeIds.T_long))
			argCount += 2;
		else
			argCount += 1;
	bCodeStream[classFileOffset++] = (byte) argCount;
	// Generate a  0 into the byte array. Like the array is already fill with 0, we just need to increment
	// the number of bytes.
	bCodeStream[classFileOffset++] = 0;
	if (((id = methodBinding.returnType.id) == TypeIds.T_double) || (id == TypeIds.T_long)) {
		stackDepth += (2 - argCount);
	} else {
		if (id == TypeIds.T_void) {
			stackDepth -= argCount;
		} else {
			stackDepth += (1 - argCount);
		}
	}
	if (stackDepth > stackMax) {
		stackMax = stackDepth;
	}
}
public void invokeJavaLangAssertionErrorConstructor(int typeBindingID) {
	// invokespecial: java.lang.AssertionError.<init>(typeBindingID)V
	if (DEBUG) System.out.println(position + "\t\tinvokespecial: java.lang.AssertionError.<init>(typeBindingID)V"); //$NON-NLS-1$
	int argCount = 1;
	char[] signature = null;
	switch (typeBindingID) {
		case TypeIds.T_int :
		case TypeIds.T_byte :
		case TypeIds.T_short :
			signature = ConstantPool.IntConstrSignature;
			break;
		case TypeIds.T_long :
			signature = ConstantPool.LongConstrSignature;
			argCount = 2;
			break;
		case TypeIds.T_float :
			signature = ConstantPool.FloatConstrSignature;
			break;
		case TypeIds.T_double :
			signature = ConstantPool.DoubleConstrSignature;
			argCount = 2;
			break;
		case TypeIds.T_char :
			signature = ConstantPool.CharConstrSignature;
			break;
		case TypeIds.T_boolean :
			signature = ConstantPool.BooleanConstrSignature;
			break;
		case TypeIds.T_JavaLangObject :
		case TypeIds.T_JavaLangString :
		case TypeIds.T_null :
			signature = ConstantPool.ObjectConstrSignature;
			break;
	}
	this.invoke(
			Opcodes.OPC_invokespecial,
			argCount, // argCount
			0, // return type size
			ConstantPool.JavaLangAssertionErrorConstantPoolName,
			ConstantPool.Init,
			signature);
}
public void invokeJavaLangAssertionErrorDefaultConstructor() {
	// invokespecial: java.lang.AssertionError.<init>()V
	if (DEBUG) System.out.println(position + "\t\tinvokespecial: java.lang.AssertionError.<init>()V"); //$NON-NLS-1$
	this.invoke(
			Opcodes.OPC_invokespecial,
			0, // argCount
			0, // return type size
			ConstantPool.JavaLangAssertionErrorConstantPoolName,
			ConstantPool.Init,
			ConstantPool.DefaultConstructorSignature);
}
public void invokeJavaLangClassDesiredAssertionStatus() {
	// invokevirtual: java.lang.Class.desiredAssertionStatus()Z;
	if (DEBUG) System.out.println(position + "\t\tinvokevirtual: java.lang.Class.desiredAssertionStatus()Z;"); //$NON-NLS-1$
	this.invoke(
			Opcodes.OPC_invokevirtual,
			0, // argCount
			1, // return type size
			ConstantPool.JavaLangClassConstantPoolName,
			ConstantPool.DesiredAssertionStatus,
			ConstantPool.DesiredAssertionStatusSignature);
}
public void invokeJavaLangEnumvalueOf(ReferenceBinding binding) {
	// invokestatic: java.lang.Enum.valueOf(Class,String)
	if (DEBUG) System.out.println(position + "\t\tinvokestatic: java.lang.Enum.valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;"); //$NON-NLS-1$
	this.invoke(
			Opcodes.OPC_invokestatic,
			2, // argCount
			1, // return type size
			ConstantPool.JavaLangEnumConstantPoolName,
			ConstantPool.ValueOf,
			ConstantPool.ValueOfStringClassSignature);
}
public void invokeJavaLangEnumValues(TypeBinding enumBinding, ArrayBinding arrayBinding) {
	char[] signature = "()".toCharArray(); //$NON-NLS-1$
	signature = CharOperation.concat(signature, arrayBinding.constantPoolName());
	this.invoke(Opcodes.OPC_invokestatic, 0, 1, enumBinding.constantPoolName(), TypeConstants.VALUES, signature);
}
public void invokeJavaLangErrorConstructor() {
	// invokespecial: java.lang.Error<init>(Ljava.lang.String;)V
	if (DEBUG) System.out.println(position + "\t\tinvokespecial: java.lang.Error<init>(Ljava.lang.String;)V"); //$NON-NLS-1$
	this.invoke(
			Opcodes.OPC_invokespecial,
			1, // argCount
			0, // return type size
			ConstantPool.JavaLangErrorConstantPoolName,
			ConstantPool.Init,
			ConstantPool.StringConstructorSignature);
}
public void invokeJavaLangReflectConstructorNewInstance() {
	// invokevirtual: java.lang.reflect.Constructor.newInstance([Ljava.lang.Object;)Ljava.lang.Object;
	this.invoke(
			Opcodes.OPC_invokevirtual,
			1, // argCount
			1, // return type size
			ConstantPool.JavaLangReflectConstructorConstantPoolName,
			ConstantPool.NewInstance,
			ConstantPool.JavaLangReflectConstructorNewInstanceSignature);
}
protected void invokeJavaLangReflectFieldGetter(int typeID) {
	int returnTypeSize = 1;
	char[] signature = null;
	char[] selector = null;
	switch (typeID) {
		case TypeIds.T_int :
			selector = ConstantPool.GET_INT_METHOD_NAME;
			signature = ConstantPool.GET_INT_METHOD_SIGNATURE;
			break;
		case TypeIds.T_byte :
			selector = ConstantPool.GET_BYTE_METHOD_NAME;
			signature = ConstantPool.GET_BYTE_METHOD_SIGNATURE;
			break;
		case TypeIds.T_short :
			selector = ConstantPool.GET_SHORT_METHOD_NAME;
			signature = ConstantPool.GET_SHORT_METHOD_SIGNATURE;
			break;
		case TypeIds.T_long :
			selector = ConstantPool.GET_LONG_METHOD_NAME;
			signature = ConstantPool.GET_LONG_METHOD_SIGNATURE;
			returnTypeSize = 2;
			break;
		case TypeIds.T_float :
			selector = ConstantPool.GET_FLOAT_METHOD_NAME;
			signature = ConstantPool.GET_FLOAT_METHOD_SIGNATURE;
			break;
		case TypeIds.T_double :
			selector = ConstantPool.GET_DOUBLE_METHOD_NAME;
			signature = ConstantPool.GET_DOUBLE_METHOD_SIGNATURE;
			returnTypeSize = 2;
			break;
		case TypeIds.T_char :
			selector = ConstantPool.GET_CHAR_METHOD_NAME;
			signature = ConstantPool.GET_CHAR_METHOD_SIGNATURE;
			break;
		case TypeIds.T_boolean :
			selector = ConstantPool.GET_BOOLEAN_METHOD_NAME;
			signature = ConstantPool.GET_BOOLEAN_METHOD_SIGNATURE;
			break;
		default :
			selector = ConstantPool.GET_OBJECT_METHOD_NAME;
			signature = ConstantPool.GET_OBJECT_METHOD_SIGNATURE;
			break;
	}
	this.invoke(
			Opcodes.OPC_invokevirtual,
			1, // argCount
			returnTypeSize, // return type size
			ConstantPool.JAVALANGREFLECTFIELD_CONSTANTPOOLNAME,
			selector,
			signature);
}
protected void invokeJavaLangReflectFieldSetter(int typeID) {
	int argCount = 2;
	char[] signature = null;
	char[] selector = null;
	switch (typeID) {
		case TypeIds.T_int :
			selector = ConstantPool.SET_INT_METHOD_NAME;
			signature = ConstantPool.SET_INT_METHOD_SIGNATURE;
			break;
		case TypeIds.T_byte :
			selector = ConstantPool.SET_BYTE_METHOD_NAME;
			signature = ConstantPool.SET_BYTE_METHOD_SIGNATURE;
			break;
		case TypeIds.T_short :
			selector = ConstantPool.SET_SHORT_METHOD_NAME;
			signature = ConstantPool.SET_SHORT_METHOD_SIGNATURE;
			break;
		case TypeIds.T_long :
			selector = ConstantPool.SET_LONG_METHOD_NAME;
			signature = ConstantPool.SET_LONG_METHOD_SIGNATURE;
			argCount = 3;
			break;
		case TypeIds.T_float :
			selector = ConstantPool.SET_FLOAT_METHOD_NAME;
			signature = ConstantPool.SET_FLOAT_METHOD_SIGNATURE;
			break;
		case TypeIds.T_double :
			selector = ConstantPool.SET_DOUBLE_METHOD_NAME;
			signature = ConstantPool.SET_DOUBLE_METHOD_SIGNATURE;
			argCount = 3;
			break;
		case TypeIds.T_char :
			selector = ConstantPool.SET_CHAR_METHOD_NAME;
			signature = ConstantPool.SET_CHAR_METHOD_SIGNATURE;
			break;
		case TypeIds.T_boolean :
			selector = ConstantPool.SET_BOOLEAN_METHOD_NAME;
			signature = ConstantPool.SET_BOOLEAN_METHOD_SIGNATURE;
			break;
		default :
			selector = ConstantPool.SET_OBJECT_METHOD_NAME;
			signature = ConstantPool.SET_OBJECT_METHOD_SIGNATURE;
			break;
	}
	this.invoke(
			Opcodes.OPC_invokevirtual,
			argCount, // argCount
			0, // return type size
			ConstantPool.JAVALANGREFLECTFIELD_CONSTANTPOOLNAME,
			selector,
			signature);
}
public void invokeJavaLangReflectMethodInvoke() {
	// invokevirtual: java.lang.reflect.Method.invoke(Ljava.lang.Object;[Ljava.lang.Object;)Ljava.lang.Object;
	this.invoke(
			Opcodes.OPC_invokevirtual,
			2, // argCount
			1, // return type size
			ConstantPool.JAVALANGREFLECTMETHOD_CONSTANTPOOLNAME,
			ConstantPool.INVOKE_METHOD_METHOD_NAME,
			ConstantPool.INVOKE_METHOD_METHOD_SIGNATURE);
}
public void invokeJavaUtilIteratorHasNext() {
	// invokeinterface java.util.Iterator.hasNext()Z
	if (DEBUG) System.out.println(position + "\t\tinvokeinterface: java.util.Iterator.hasNext()Z"); //$NON-NLS-1$
	this.invoke(
			Opcodes.OPC_invokeinterface,
			0, // argCount
			1, // return type size
			ConstantPool.JavaUtilIteratorConstantPoolName,
			ConstantPool.HasNext,
			ConstantPool.HasNextSignature);
}
public void invokeJavaUtilIteratorNext() {
	// invokeinterface java.util.Iterator.next()java.lang.Object
	if (DEBUG) System.out.println(position + "\t\tinvokeinterface: java.util.Iterator.next()java.lang.Object"); //$NON-NLS-1$
	this.invoke(
			Opcodes.OPC_invokeinterface,
			0, // argCount
			1, // return type size
			ConstantPool.JavaUtilIteratorConstantPoolName,
			ConstantPool.Next,
			ConstantPool.NextSignature);
}
public void invokeNoClassDefFoundErrorStringConstructor() {
	// invokespecial: java.lang.NoClassDefFoundError.<init>(Ljava.lang.String;)V
	if (DEBUG) System.out.println(position + "\t\tinvokespecial: java.lang.NoClassDefFoundError.<init>(Ljava.lang.String;)V"); //$NON-NLS-1$
	this.invoke(
			Opcodes.OPC_invokespecial,
			1, // argCount
			0, // return type size
			ConstantPool.JavaLangNoClassDefFoundErrorConstantPoolName,
			ConstantPool.Init,
			ConstantPool.StringConstructorSignature);
}
public void invokeObjectGetClass() {
	// invokevirtual: java.lang.Object.getClass()Ljava.lang.Class;
	if (DEBUG) System.out.println(position + "\t\tinvokevirtual: java.lang.Object.getClass()Ljava.lang.Class;"); //$NON-NLS-1$
	this.invoke(
			Opcodes.OPC_invokevirtual,
			0, // argCount
			1, // return type size
			ConstantPool.JavaLangObjectConstantPoolName,
			ConstantPool.GetClass,
			ConstantPool.GetClassSignature);
}
public void invokespecial(MethodBinding methodBinding) {
	if (DEBUG) System.out.println(position + "\t\tinvokespecial:"+methodBinding); //$NON-NLS-1$
	countLabels = 0;
	// initialized to 1 to take into account this immediately
	int argCount = 1;
	int id;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_invokespecial;
	writeUnsignedShort(
		constantPool.literalIndexForMethod(
			methodBinding.constantPoolDeclaringClass(),
			methodBinding.selector,
			methodBinding.signature(classFile),
			false));
	if (methodBinding.isConstructor()) {
		final ReferenceBinding declaringClass = methodBinding.declaringClass;
		if (declaringClass.isNestedType()) {
			// enclosing instances
			TypeBinding[] syntheticArgumentTypes = declaringClass.syntheticEnclosingInstanceTypes();
			if (syntheticArgumentTypes != null) {
				for (int i = 0, max = syntheticArgumentTypes.length; i < max; i++) {
					if (((id = syntheticArgumentTypes[i].id) == TypeIds.T_double) || (id == TypeIds.T_long)) {
						argCount += 2;
					} else {
						argCount++;
					}
				}
			}
			// outer local variables
			SyntheticArgumentBinding[] syntheticArguments = declaringClass.syntheticOuterLocalVariables();
			if (syntheticArguments != null) {
				for (int i = 0, max = syntheticArguments.length; i < max; i++) {
					if (((id = syntheticArguments[i].type.id) == TypeIds.T_double) || (id == TypeIds.T_long)) {
						argCount += 2;
					} else {
						argCount++;
					}
				}
			}
		}
		if (declaringClass.isEnum()) {
			// adding String and int
			argCount += 2;
		}
	}
	for (int i = methodBinding.parameters.length - 1; i >= 0; i--)
		if (((id = methodBinding.parameters[i].id) == TypeIds.T_double) || (id == TypeIds.T_long))
			argCount += 2;
		else
			argCount++;
	if (((id = methodBinding.returnType.id) == TypeIds.T_double) || (id == TypeIds.T_long))
		stackDepth += (2 - argCount);
	else
		if (id == TypeIds.T_void)
			stackDepth -= argCount;
		else
			stackDepth += (1 - argCount);
	if (stackDepth > stackMax)
		stackMax = stackDepth;
}
public void invokestatic(MethodBinding methodBinding) {
	if (DEBUG) System.out.println(position + "\t\tinvokestatic:"+methodBinding); //$NON-NLS-1$
	// initialized to 0 to take into account that there is no this for
	// a static method
	countLabels = 0;
	int argCount = 0;
	int id;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_invokestatic;
	writeUnsignedShort(
		constantPool.literalIndexForMethod(
			methodBinding.constantPoolDeclaringClass(),
			methodBinding.selector,
			methodBinding.signature(classFile),
			false));
	for (int i = methodBinding.parameters.length - 1; i >= 0; i--)
		if (((id = methodBinding.parameters[i].id) == TypeIds.T_double) || (id == TypeIds.T_long))
			argCount += 2;
		else
			argCount += 1;
	if (((id = methodBinding.returnType.id) == TypeIds.T_double) || (id == TypeIds.T_long))
		stackDepth += (2 - argCount);
	else
		if (id == TypeIds.T_void)
			stackDepth -= argCount;
		else
			stackDepth += (1 - argCount);
	if (stackDepth > stackMax)
		stackMax = stackDepth;
}
/**
 * The equivalent code performs a string conversion of the TOS
 * @param typeID <CODE>int</CODE>
 */
public void invokeStringConcatenationAppendForType(int typeID) {
	if (DEBUG) {
		if (this.targetLevel >= ClassFileConstants.JDK1_5) {
			System.out.println(position + "\t\tinvokevirtual: java.lang.StringBuilder.append(...)"); //$NON-NLS-1$
		} else {
			System.out.println(position + "\t\tinvokevirtual: java.lang.StringBuffer.append(...)"); //$NON-NLS-1$
		}
	}
	int argCount = 1;
	int returnType = 1;
	char[] declaringClass = null;
	char[] selector = ConstantPool.Append;
	char[] signature = null;
	switch (typeID) {
		case TypeIds.T_int :
		case TypeIds.T_byte :
		case TypeIds.T_short :
			if (this.targetLevel >= ClassFileConstants.JDK1_5) {
				declaringClass = ConstantPool.JavaLangStringBuilderConstantPoolName;
				signature = ConstantPool.StringBuilderAppendIntSignature;
			} else {
				declaringClass = ConstantPool.JavaLangStringBufferConstantPoolName;
				signature = ConstantPool.StringBufferAppendIntSignature;
			}
			break;
		case TypeIds.T_long :
			if (this.targetLevel >= ClassFileConstants.JDK1_5) {
				declaringClass = ConstantPool.JavaLangStringBuilderConstantPoolName;
				signature = ConstantPool.StringBuilderAppendLongSignature;
			} else {
				declaringClass = ConstantPool.JavaLangStringBufferConstantPoolName;
				signature = ConstantPool.StringBufferAppendLongSignature;
			}
			argCount = 2;
			break;
		case TypeIds.T_float :
			if (this.targetLevel >= ClassFileConstants.JDK1_5) {
				declaringClass = ConstantPool.JavaLangStringBuilderConstantPoolName;
				signature = ConstantPool.StringBuilderAppendFloatSignature;
			} else {
				declaringClass = ConstantPool.JavaLangStringBufferConstantPoolName;
				signature = ConstantPool.StringBufferAppendFloatSignature;
			}
			break;
		case TypeIds.T_double :
			if (this.targetLevel >= ClassFileConstants.JDK1_5) {
				declaringClass = ConstantPool.JavaLangStringBuilderConstantPoolName;
				signature = ConstantPool.StringBuilderAppendDoubleSignature;
			} else {
				declaringClass = ConstantPool.JavaLangStringBufferConstantPoolName;
				signature = ConstantPool.StringBufferAppendDoubleSignature;
			}
			argCount = 2;
			break;
		case TypeIds.T_char :
			if (this.targetLevel >= ClassFileConstants.JDK1_5) {
				declaringClass = ConstantPool.JavaLangStringBuilderConstantPoolName;
				signature = ConstantPool.StringBuilderAppendCharSignature;
			} else {
				declaringClass = ConstantPool.JavaLangStringBufferConstantPoolName;
				signature = ConstantPool.StringBufferAppendCharSignature;
			}
			break;
		case TypeIds.T_boolean :
			if (this.targetLevel >= ClassFileConstants.JDK1_5) {
				declaringClass = ConstantPool.JavaLangStringBuilderConstantPoolName;
				signature = ConstantPool.StringBuilderAppendBooleanSignature;
			} else {
				declaringClass = ConstantPool.JavaLangStringBufferConstantPoolName;
				signature = ConstantPool.StringBufferAppendBooleanSignature;
			}
			break;
		case TypeIds.T_undefined :
		case TypeIds.T_JavaLangObject :
		case TypeIds.T_null :
			if (this.targetLevel >= ClassFileConstants.JDK1_5) {
				declaringClass = ConstantPool.JavaLangStringBuilderConstantPoolName;
				signature = ConstantPool.StringBuilderAppendObjectSignature;
			} else {
				declaringClass = ConstantPool.JavaLangStringBufferConstantPoolName;
				signature = ConstantPool.StringBufferAppendObjectSignature;
			}
			break;
		case TypeIds.T_JavaLangString :
			if (this.targetLevel >= ClassFileConstants.JDK1_5) {
				declaringClass = ConstantPool.JavaLangStringBuilderConstantPoolName;
				signature = ConstantPool.StringBuilderAppendStringSignature;
			} else {
				declaringClass = ConstantPool.JavaLangStringBufferConstantPoolName;
				signature = ConstantPool.StringBufferAppendStringSignature;
			}
			break;
	}
	this.invoke(
			Opcodes.OPC_invokevirtual,
			argCount, // argCount
			returnType, // return type size
			declaringClass,
			selector,
			signature);
}
public void invokeStringConcatenationDefaultConstructor() {
	// invokespecial: java.lang.StringBuffer.<init>()V
	if (DEBUG) {
		if (this.targetLevel >= ClassFileConstants.JDK1_5) {
			System.out.println(position + "\t\tinvokespecial: java.lang.StringBuilder.<init>()V"); //$NON-NLS-1$
		} else {
			System.out.println(position + "\t\tinvokespecial: java.lang.StringBuffer.<init>()V"); //$NON-NLS-1$
		}
	}
	char[] declaringClass = ConstantPool.JavaLangStringBufferConstantPoolName;
	if (this.targetLevel >= ClassFileConstants.JDK1_5) {
		declaringClass = ConstantPool.JavaLangStringBuilderConstantPoolName;
	}
	this.invoke(
			Opcodes.OPC_invokespecial,
			0, // argCount
			0, // return type size
			declaringClass,
			ConstantPool.Init,
			ConstantPool.DefaultConstructorSignature);
}
public void invokeStringConcatenationStringConstructor() {
	if (DEBUG) {
		if (this.targetLevel >= ClassFileConstants.JDK1_5) {
			System.out.println(position + "\t\tjava.lang.StringBuilder.<init>(Ljava.lang.String;)V"); //$NON-NLS-1$
		} else {
			System.out.println(position + "\t\tjava.lang.StringBuffer.<init>(Ljava.lang.String;)V"); //$NON-NLS-1$
		}
	}
	char[] declaringClass = ConstantPool.JavaLangStringBufferConstantPoolName;
	if (this.targetLevel >= ClassFileConstants.JDK1_5) {
		declaringClass = ConstantPool.JavaLangStringBuilderConstantPoolName;
	}
	this.invoke(
			Opcodes.OPC_invokespecial,
			1, // argCount
			0, // return type size
			declaringClass,
			ConstantPool.Init,
			ConstantPool.StringConstructorSignature);
}
public void invokeStringConcatenationToString() {
	if (DEBUG) {
		if (this.targetLevel >= ClassFileConstants.JDK1_5) {
			System.out.println(position + "\t\tinvokevirtual: StringBuilder.toString()Ljava.lang.String;"); //$NON-NLS-1$
		} else {
			System.out.println(position + "\t\tinvokevirtual: StringBuffer.toString()Ljava.lang.String;"); //$NON-NLS-1$
		}
	}
	char[] declaringClass = ConstantPool.JavaLangStringBufferConstantPoolName;
	if (this.targetLevel >= ClassFileConstants.JDK1_5) {
		declaringClass = ConstantPool.JavaLangStringBuilderConstantPoolName;
	}
	this.invoke(
			Opcodes.OPC_invokevirtual,
			0, // argCount
			1, // return type size
			declaringClass,
			ConstantPool.ToString,
			ConstantPool.ToStringSignature);
}
public void invokeStringIntern() {
	// invokevirtual: java.lang.String.intern()
	if (DEBUG) System.out.println(position + "\t\tinvokevirtual: java.lang.String.intern()"); //$NON-NLS-1$
	this.invoke(
			Opcodes.OPC_invokevirtual,
			0, // argCount
			1, // return type size
			ConstantPool.JavaLangStringConstantPoolName,
			ConstantPool.Intern,
			ConstantPool.InternSignature);
}
public void invokeStringValueOf(int typeID) {
	// invokestatic: java.lang.String.valueOf(argumentType)
	if (DEBUG) System.out.println(position + "\t\tinvokestatic: java.lang.String.valueOf(...)"); //$NON-NLS-1$
	int argCount = 1;
	char[] signature = null;
	switch (typeID) {
		case TypeIds.T_int :
		case TypeIds.T_byte :
		case TypeIds.T_short :
			signature = ConstantPool.ValueOfIntSignature;
			break;
		case TypeIds.T_long :
			signature = ConstantPool.ValueOfLongSignature;
			argCount = 2;
			break;
		case TypeIds.T_float :
			signature = ConstantPool.ValueOfFloatSignature;
			break;
		case TypeIds.T_double :
			signature = ConstantPool.ValueOfDoubleSignature;
			argCount = 2;
			break;
		case TypeIds.T_char :
			signature = ConstantPool.ValueOfCharSignature;
			break;
		case TypeIds.T_boolean :
			signature = ConstantPool.ValueOfBooleanSignature;
			break;
		case TypeIds.T_JavaLangObject :
		case TypeIds.T_JavaLangString :
		case TypeIds.T_null :
		case TypeIds.T_undefined :
			signature = ConstantPool.ValueOfObjectSignature;
			break;
	}
	this.invoke(
			Opcodes.OPC_invokestatic,
			argCount, // argCount
			1, // return type size
			ConstantPool.JavaLangStringConstantPoolName,
			ConstantPool.ValueOf,
			signature);
}
public void invokeSystemArraycopy() {
	// invokestatic #21 <Method java/lang/System.arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V>
	if (DEBUG) System.out.println(position + "\t\tinvokevirtual: java.lang.System.arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V"); //$NON-NLS-1$
	this.invoke(
			Opcodes.OPC_invokestatic,
			5, // argCount
			0, // return type size
			ConstantPool.JavaLangSystemConstantPoolName,
			ConstantPool.ArrayCopy,
			ConstantPool.ArrayCopySignature);
}
public void invokeThrowableGetMessage() {
	// invokevirtual: java.lang.Throwable.getMessage()Ljava.lang.String;
	if (DEBUG) System.out.println(position + "\t\tinvokevirtual: java.lang.Throwable.getMessage()Ljava.lang.String;"); //$NON-NLS-1$
	this.invoke(
			Opcodes.OPC_invokevirtual,
			0, // argCount
			1, // return type size
			ConstantPool.JavaLangThrowableConstantPoolName,
			ConstantPool.GetMessage,
			ConstantPool.GetMessageSignature);
}
public void invokevirtual(MethodBinding methodBinding) {
	if (DEBUG) System.out.println(position + "\t\tinvokevirtual:"+methodBinding); //$NON-NLS-1$
	countLabels = 0;
	// initialized to 1 to take into account this  immediately
	int argCount = 1;
	int id;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_invokevirtual;
	writeUnsignedShort(
		constantPool.literalIndexForMethod(
			methodBinding.constantPoolDeclaringClass(),
			methodBinding.selector,
			methodBinding.signature(classFile),
			false));
	for (int i = methodBinding.parameters.length - 1; i >= 0; i--)
		if (((id = methodBinding.parameters[i].id) == TypeIds.T_double) || (id == TypeIds.T_long))
			argCount += 2;
		else
			argCount++;
	if (((id = methodBinding.returnType.id) == TypeIds.T_double) || (id == TypeIds.T_long))
		stackDepth += (2 - argCount);
	else
		if (id == TypeIds.T_void)
			stackDepth -= argCount;
		else
			stackDepth += (1 - argCount);
	if (stackDepth > stackMax)
		stackMax = stackDepth;
}
public void ior() {
	if (DEBUG) System.out.println(position + "\t\tior"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_ior;
}
public void irem() {
	if (DEBUG) System.out.println(position + "\t\tirem"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_irem;
}
public void ireturn() {
	if (DEBUG) System.out.println(position + "\t\tireturn"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	// the stackDepth should be equal to 0 
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_ireturn;
	this.lastAbruptCompletion = this.position;		
}
public boolean isDefinitelyAssigned(Scope scope, int initStateIndex, LocalVariableBinding local) {
	// Mirror of UnconditionalFlowInfo.isDefinitelyAssigned(..)
	if ((local.tagBits & TagBits.IsArgument) != 0) {
		return true;
	}
	if (initStateIndex == -1)
		return false;
	int localPosition = local.id + maxFieldCount;
	MethodScope methodScope = scope.methodScope();
	// id is zero-based
	if (localPosition < UnconditionalFlowInfo.BitCacheSize) {
		return (methodScope.definiteInits[initStateIndex] & (1L << localPosition)) != 0; // use bits
	}
	// use extra vector
	long[] extraInits = methodScope.extraDefiniteInits[initStateIndex];
	if (extraInits == null)
		return false; // if vector not yet allocated, then not initialized
	int vectorIndex;
	if ((vectorIndex = (localPosition / UnconditionalFlowInfo.BitCacheSize) - 1) >= extraInits.length)
		return false; // if not enough room in vector, then not initialized 
	return ((extraInits[vectorIndex]) & (1L << (localPosition % UnconditionalFlowInfo.BitCacheSize))) != 0;
}
public void ishl() {
	if (DEBUG) System.out.println(position + "\t\tishl"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_ishl;
}
public void ishr() {
	if (DEBUG) System.out.println(position + "\t\tishr"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_ishr;
}
public void istore(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tistore:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_wide;
		bCodeStream[classFileOffset++] = Opcodes.OPC_istore;
		writeUnsignedShort(iArg);
	} else {
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_istore;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
public void istore_0() {
	if (DEBUG) System.out.println(position + "\t\tistore_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals == 0) {
		maxLocals = 1;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_istore_0;
}
public void istore_1() {
	if (DEBUG) System.out.println(position + "\t\tistore_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_istore_1;
}
public void istore_2() {
	if (DEBUG) System.out.println(position + "\t\tistore_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_istore_2;
}
public void istore_3() {
	if (DEBUG) System.out.println(position + "\t\tistore_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_istore_3;
}
public void isub() {
	if (DEBUG) System.out.println(position + "\t\tisub"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_isub;
}
public void iushr() {
	if (DEBUG) System.out.println(position + "\t\tiushr"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_iushr;
}
public void ixor() {
	if (DEBUG) System.out.println(position + "\t\tixor"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_ixor;
}
final public void jsr(BranchLabel lbl) {
	if (this.wideMode) {
		this.jsr_w(lbl);
		return;
	}
	if (DEBUG) System.out.println(position + "\t\tjsr"+lbl); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_jsr;
	lbl.branch();
}
final public void jsr_w(BranchLabel lbl) {
	if (DEBUG) System.out.println(position + "\t\tjsr_w"+lbl); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_jsr_w;
	lbl.branchWide();
}
public void l2d() {
	if (DEBUG) System.out.println(position + "\t\tl2d"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_l2d;
}
public void l2f() {
	if (DEBUG) System.out.println(position + "\t\tl2f"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_l2f;
}
public void l2i() {
	if (DEBUG) System.out.println(position + "\t\tl2i"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_l2i;
}
public void ladd() {
	if (DEBUG) System.out.println(position + "\t\tladd"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_ladd;
}
public void laload() {
	if (DEBUG) System.out.println(position + "\t\tlaload"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_laload;
}
public void land() {
	if (DEBUG) System.out.println(position + "\t\tland"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_land;
}
public void lastore() {
	if (DEBUG) System.out.println(position + "\t\tlastore"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 4;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lastore;
}
public void lcmp() {
	if (DEBUG) System.out.println(position + "\t\tlcmp"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lcmp;
}
public void lconst_0() {
	if (DEBUG) System.out.println(position + "\t\tlconst_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lconst_0;
}
public void lconst_1() {
	if (DEBUG) System.out.println(position + "\t\tlconst_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lconst_1;
}
public void ldc(float constant) {
	countLabels = 0;
	int index = constantPool.literalIndex(constant);
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (index > 255) {
		if (DEBUG) System.out.println(position + "\t\tldc_w:"+constant); //$NON-NLS-1$
		// Generate a ldc_w
		if (classFileOffset + 2 >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ldc_w;
		writeUnsignedShort(index);
	} else {
		if (DEBUG) System.out.println(position + "\t\tldc:"+constant); //$NON-NLS-1$
		// Generate a ldc
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ldc;
		bCodeStream[classFileOffset++] = (byte) index;
	}
}
public void ldc(int constant) {
	countLabels = 0;
	int index = constantPool.literalIndex(constant);
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (index > 255) {
		if (DEBUG) System.out.println(position + "\t\tldc_w:"+constant); //$NON-NLS-1$
		// Generate a ldc_w
		if (classFileOffset + 2 >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ldc_w;
		writeUnsignedShort(index);
	} else {
		if (DEBUG) System.out.println(position + "\t\tldc:"+constant); //$NON-NLS-1$
		// Generate a ldc
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ldc;
		bCodeStream[classFileOffset++] = (byte) index;
	}
}
public void ldc(String constant) {
	countLabels = 0;
	int currentCodeStreamPosition = position;
	char[] constantChars = constant.toCharArray();
	int index = constantPool.literalIndexForLdc(constantChars);
	if (index > 0) {
		// the string already exists inside the constant pool
		// we reuse the same index
		this.ldcForIndex(index, constantChars);
	} else {
		// the string is too big to be utf8-encoded in one pass.
		// we have to split it into different pieces.
		// first we clean all side-effects due to the code above
		// this case is very rare, so we can afford to lose time to handle it
		position = currentCodeStreamPosition;
		int i = 0;
		int length = 0;
		int constantLength = constant.length();
		byte[] utf8encoding = new byte[Math.min(constantLength + 100, 65535)];
		int utf8encodingLength = 0;
		while ((length < 65532) && (i < constantLength)) {
			char current = constantChars[i];
			// we resize the byte array immediately if necessary
			if (length + 3 > (utf8encodingLength = utf8encoding.length)) {
				System.arraycopy(utf8encoding, 0, utf8encoding = new byte[Math.min(utf8encodingLength + 100, 65535)], 0, length);
			}
			if ((current >= 0x0001) && (current <= 0x007F)) {
				// we only need one byte: ASCII table
				utf8encoding[length++] = (byte) current;
			} else {
				if (current > 0x07FF) {
					// we need 3 bytes
					utf8encoding[length++] = (byte) (0xE0 | ((current >> 12) & 0x0F)); // 0xE0 = 1110 0000
					utf8encoding[length++] = (byte) (0x80 | ((current >> 6) & 0x3F)); // 0x80 = 1000 0000
					utf8encoding[length++] = (byte) (0x80 | (current & 0x3F)); // 0x80 = 1000 0000
				} else {
					// we can be 0 or between 0x0080 and 0x07FF
					// In that case we only need 2 bytes
					utf8encoding[length++] = (byte) (0xC0 | ((current >> 6) & 0x1F)); // 0xC0 = 1100 0000
					utf8encoding[length++] = (byte) (0x80 | (current & 0x3F)); // 0x80 = 1000 0000
				}
			}
			i++;
		}
		// check if all the string is encoded (PR 1PR2DWJ)
		// the string is too big to be encoded in one pass
		newStringContatenation();
		dup();
		// write the first part
		char[] subChars = new char[i];
		System.arraycopy(constantChars, 0, subChars, 0, i);
		System.arraycopy(utf8encoding, 0, utf8encoding = new byte[length], 0, length);
		index = constantPool.literalIndex(subChars, utf8encoding);
		this.ldcForIndex(index, subChars);
		// write the remaining part
		invokeStringConcatenationStringConstructor();
		while (i < constantLength) {
			length = 0;
			utf8encoding = new byte[Math.min(constantLength - i + 100, 65535)];
			int startIndex = i;
			while ((length < 65532) && (i < constantLength)) {
				char current = constantChars[i];
				// we resize the byte array immediately if necessary
				if (length + 3 > (utf8encodingLength = utf8encoding.length)) {
					System.arraycopy(utf8encoding, 0, utf8encoding = new byte[Math.min(utf8encodingLength + 100, 65535)], 0, length);
				}
				if ((current >= 0x0001) && (current <= 0x007F)) {
					// we only need one byte: ASCII table
					utf8encoding[length++] = (byte) current;
				} else {
					if (current > 0x07FF) {
						// we need 3 bytes
						utf8encoding[length++] = (byte) (0xE0 | ((current >> 12) & 0x0F)); // 0xE0 = 1110 0000
						utf8encoding[length++] = (byte) (0x80 | ((current >> 6) & 0x3F)); // 0x80 = 1000 0000
						utf8encoding[length++] = (byte) (0x80 | (current & 0x3F)); // 0x80 = 1000 0000
					} else {
						// we can be 0 or between 0x0080 and 0x07FF
						// In that case we only need 2 bytes
						utf8encoding[length++] = (byte) (0xC0 | ((current >> 6) & 0x1F)); // 0xC0 = 1100 0000
						utf8encoding[length++] = (byte) (0x80 | (current & 0x3F)); // 0x80 = 1000 0000
					}
				}
				i++;
			}
			// the next part is done
			int newCharLength = i - startIndex;
			subChars = new char[newCharLength];
			System.arraycopy(constantChars, startIndex, subChars, 0, newCharLength);
			System.arraycopy(utf8encoding, 0, utf8encoding = new byte[length], 0, length);
			index = constantPool.literalIndex(subChars, utf8encoding);
			this.ldcForIndex(index, subChars);
			// now on the stack it should be a StringBuffer and a string.
			invokeStringConcatenationAppendForType(TypeIds.T_JavaLangString);
		}
		invokeStringConcatenationToString();
		invokeStringIntern();
	}
}
public void ldc(TypeBinding typeBinding) {
	countLabels = 0;
	int index = constantPool.literalIndexForType(typeBinding);
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (index > 255) {
		if (DEBUG) System.out.println(position + "\t\tldc_w:"+ typeBinding); //$NON-NLS-1$
		// Generate a ldc_w
		if (classFileOffset + 2 >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ldc_w;
		writeUnsignedShort(index);
	} else {
		if (DEBUG) System.out.println(position + "\t\tldw:"+ typeBinding); //$NON-NLS-1$
		// Generate a ldc
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ldc;
		bCodeStream[classFileOffset++] = (byte) index;
	}
}
public void ldc2_w(double constant) {
	if (DEBUG) System.out.println(position + "\t\tldc2_w:"+constant); //$NON-NLS-1$
	countLabels = 0;
	int index = constantPool.literalIndex(constant);
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	// Generate a ldc2_w
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_ldc2_w;
	writeUnsignedShort(index);
}
public void ldc2_w(long constant) {
	if (DEBUG) System.out.println(position + "\t\tldc2_w:"+constant); //$NON-NLS-1$
	countLabels = 0;
	int index = constantPool.literalIndex(constant);
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	// Generate a ldc2_w
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_ldc2_w;
	writeUnsignedShort(index);
}
public void ldcForIndex(int index, char[] constant) {
	stackDepth++;
	if (stackDepth > stackMax) {
		stackMax = stackDepth;
	}
	if (index > 255) {
		// Generate a ldc_w
		if (DEBUG) System.out.println(position + "\t\tldc_w:"+ new String(constant)); //$NON-NLS-1$
		if (classFileOffset + 2 >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ldc_w;
		writeUnsignedShort(index);
	} else {
		// Generate a ldc
		if (DEBUG) System.out.println(position + "\t\tldc:"+ new String(constant)); //$NON-NLS-1$
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ldc;
		bCodeStream[classFileOffset++] = (byte) index;
	}
}
public void ldiv() {
	if (DEBUG) System.out.println(position + "\t\tldiv"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_ldiv;
}
public void lload(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tlload:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (maxLocals <= iArg + 1) {
		maxLocals = iArg + 2;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_wide;
		bCodeStream[classFileOffset++] = Opcodes.OPC_lload;
		writeUnsignedShort(iArg);
	} else {
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_lload;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
public void lload_0() {
	if (DEBUG) System.out.println(position + "\t\tlload_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (maxLocals < 2) {
		maxLocals = 2;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lload_0;
}
public void lload_1() {
	if (DEBUG) System.out.println(position + "\t\tlload_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (maxLocals < 3) {
		maxLocals = 3;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lload_1;
}
public void lload_2() {
	if (DEBUG) System.out.println(position + "\t\tlload_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (maxLocals < 4) {
		maxLocals = 4;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lload_2;
}
public void lload_3() {
	if (DEBUG) System.out.println(position + "\t\tlload_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (maxLocals < 5) {
		maxLocals = 5;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lload_3;
}
public void lmul() {
	if (DEBUG) System.out.println(position + "\t\tlmul"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lmul;
}
public void lneg() {
	if (DEBUG) System.out.println(position + "\t\tlneg"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lneg;
}
public final void load(LocalVariableBinding localBinding) {
	load(localBinding.type, localBinding.resolvedPosition);
}
public final void load(TypeBinding typeBinding, int resolvedPosition) {
	countLabels = 0;
	// Using dedicated int bytecode
	switch(typeBinding.id) {
		case TypeIds.T_int :
		case TypeIds.T_byte :
		case TypeIds.T_char :
		case TypeIds.T_boolean :
		case TypeIds.T_short :
			switch (resolvedPosition) {
				case 0 :
					this.iload_0();
					break;
				case 1 :
					this.iload_1();
					break;
				case 2 :
					this.iload_2();
					break;
				case 3 :
					this.iload_3();
					break;
				//case -1 :
				// internal failure: trying to load variable not supposed to be generated
				//	break;
				default :
					this.iload(resolvedPosition);
			}
			break;
		case TypeIds.T_float :
			switch (resolvedPosition) {
				case 0 :
					this.fload_0();
					break;
				case 1 :
					this.fload_1();
					break;
				case 2 :
					this.fload_2();
					break;
				case 3 :
					this.fload_3();
					break;
				default :
					this.fload(resolvedPosition);
			}
			break;
		case TypeIds.T_long :
			switch (resolvedPosition) {
				case 0 :
					this.lload_0();
					break;
				case 1 :
					this.lload_1();
					break;
				case 2 :
					this.lload_2();
					break;
				case 3 :
					this.lload_3();
					break;
				default :
					this.lload(resolvedPosition);
			}
			break;
		case TypeIds.T_double :
			switch (resolvedPosition) {
				case 0 :
					this.dload_0();
					break;
				case 1 :
					this.dload_1();
					break;
				case 2 :
					this.dload_2();
					break;
				case 3 :
					this.dload_3();
					break;
				default :
					this.dload(resolvedPosition);
			}
			break;
		default :
			switch (resolvedPosition) {
				case 0 :
					this.aload_0();
					break;
				case 1 :
					this.aload_1();
					break;
				case 2 :
					this.aload_2();
					break;
				case 3 :
					this.aload_3();
					break;
				default :
					this.aload(resolvedPosition);
			}
	}
}
public void lookupswitch(CaseLabel defaultLabel, int[] keys, int[] sortedIndexes, CaseLabel[] casesLabel) {
	if (DEBUG) System.out.println(position + "\t\tlookupswitch"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	int length = keys.length;
	int pos = position;
	defaultLabel.placeInstruction();
	for (int i = 0; i < length; i++) {
		casesLabel[i].placeInstruction();
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lookupswitch;
	for (int i = (3 - (pos & 3)); i > 0; i--) { // faster than % 4
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = 0;
	}
	defaultLabel.branch();
	writeSignedWord(length);
	for (int i = 0; i < length; i++) {
		writeSignedWord(keys[sortedIndexes[i]]);
		casesLabel[sortedIndexes[i]].branch();
	}
}
public void lor() {
	if (DEBUG) System.out.println(position + "\t\tlor"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lor;
}
public void lrem() {
	if (DEBUG) System.out.println(position + "\t\tlrem"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lrem;
}
public void lreturn() {
	if (DEBUG) System.out.println(position + "\t\tlreturn"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	// the stackDepth should be equal to 0 
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lreturn;
	this.lastAbruptCompletion = this.position;		
}
public void lshl() {
	if (DEBUG) System.out.println(position + "\t\tlshl"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lshl;
}
public void lshr() {
	if (DEBUG) System.out.println(position + "\t\tlshr"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lshr;
}
public void lstore(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tlstore:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals <= iArg + 1) {
		maxLocals = iArg + 2;
	}
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_wide;
		bCodeStream[classFileOffset++] = Opcodes.OPC_lstore;
		writeUnsignedShort(iArg);
	} else {
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_lstore;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
public void lstore_0() {
	if (DEBUG) System.out.println(position + "\t\tlstore_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 2) {
		maxLocals = 2;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lstore_0;
}
public void lstore_1() {
	if (DEBUG) System.out.println(position + "\t\tlstore_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 3) {
		maxLocals = 3;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lstore_1;
}
public void lstore_2() {
	if (DEBUG) System.out.println(position + "\t\tlstore_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 4) {
		maxLocals = 4;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lstore_2;
}
public void lstore_3() {
	if (DEBUG) System.out.println(position + "\t\tlstore_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 5) {
		maxLocals = 5;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lstore_3;
}
public void lsub() {
	if (DEBUG) System.out.println(position + "\t\tlsub"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lsub;
}
public void lushr() {
	if (DEBUG) System.out.println(position + "\t\tlushr"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lushr;
}
public void lxor() {
	if (DEBUG) System.out.println(position + "\t\tlxor"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_lxor;
}
public void monitorenter() {
	if (DEBUG) System.out.println(position + "\t\tmonitorenter"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_monitorenter;
}
public void monitorexit() {
	if (DEBUG) System.out.println(position + "\t\tmonitorexit"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_monitorexit;
}
public void multianewarray(TypeBinding typeBinding, int dimensions) {
	if (DEBUG) System.out.println(position + "\t\tmultinewarray:"+typeBinding+","+dimensions); //$NON-NLS-1$ //$NON-NLS-2$
	countLabels = 0;
	stackDepth += (1 - dimensions);
	if (classFileOffset + 3 >= bCodeStream.length) {
		resizeByteArray();
	}
	position += 2;
	bCodeStream[classFileOffset++] = Opcodes.OPC_multianewarray;
	writeUnsignedShort(constantPool.literalIndexForType(typeBinding));
	bCodeStream[classFileOffset++] = (byte) dimensions;
}
// We didn't call it new, because there is a conflit with the new keyword
public void new_(TypeBinding typeBinding) {
	if (DEBUG) System.out.println(position + "\t\tnew:"+typeBinding.debugName()); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_new;
	writeUnsignedShort(constantPool.literalIndexForType(typeBinding));
}
public void newarray(int array_Type) {
	if (DEBUG) System.out.println(position + "\t\tnewarray:"+array_Type); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 1 >= bCodeStream.length) {
		resizeByteArray();
	}
	position += 2;
	bCodeStream[classFileOffset++] = Opcodes.OPC_newarray;
	bCodeStream[classFileOffset++] = (byte) array_Type;
}
public void newArray(ArrayBinding arrayBinding) {
	TypeBinding component = arrayBinding.elementsType();
	switch (component.id) {
		case TypeIds.T_int :
			this.newarray(ClassFileConstants.INT_ARRAY);
			break;
		case TypeIds.T_byte :
			this.newarray(ClassFileConstants.BYTE_ARRAY);
			break;
		case TypeIds.T_boolean :
			this.newarray(ClassFileConstants.BOOLEAN_ARRAY);
			break;
		case TypeIds.T_short :
			this.newarray(ClassFileConstants.SHORT_ARRAY);
			break;
		case TypeIds.T_char :
			this.newarray(ClassFileConstants.CHAR_ARRAY);
			break;
		case TypeIds.T_long :
			this.newarray(ClassFileConstants.LONG_ARRAY);
			break;
		case TypeIds.T_float :
			this.newarray(ClassFileConstants.FLOAT_ARRAY);
			break;
		case TypeIds.T_double :
			this.newarray(ClassFileConstants.DOUBLE_ARRAY);
			break;
		default :
			this.anewarray(component);
	}
}
public void newJavaLangAssertionError() {
	// new: java.lang.AssertionError
	if (DEBUG) System.out.println(position + "\t\tnew: java.lang.AssertionError"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_new;
	writeUnsignedShort(constantPool.literalIndexForType(ConstantPool.JavaLangAssertionErrorConstantPoolName));
}
public void newJavaLangError() {
	// new: java.lang.Error
	if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Error"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_new;
	writeUnsignedShort(constantPool.literalIndexForType(ConstantPool.JavaLangErrorConstantPoolName));
}
public void newNoClassDefFoundError() {
	// new: java.lang.NoClassDefFoundError
	if (DEBUG) System.out.println(position + "\t\tnew: java.lang.NoClassDefFoundError"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_new;
	writeUnsignedShort(constantPool.literalIndexForType(ConstantPool.JavaLangNoClassDefFoundErrorConstantPoolName));
}
public void newStringContatenation() {
	// new: java.lang.StringBuffer
	// new: java.lang.StringBuilder
	if (DEBUG) {
		if (this.targetLevel >= ClassFileConstants.JDK1_5) {
			System.out.println(position + "\t\tnew: java.lang.StringBuilder"); //$NON-NLS-1$
		} else {
			System.out.println(position + "\t\tnew: java.lang.StringBuffer"); //$NON-NLS-1$
		}
	}
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax) {
		stackMax = stackDepth;
	}
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_new;
	if (this.targetLevel >= ClassFileConstants.JDK1_5) {
		writeUnsignedShort(constantPool.literalIndexForType(ConstantPool.JavaLangStringBuilderConstantPoolName));
	} else {
		writeUnsignedShort(constantPool.literalIndexForType(ConstantPool.JavaLangStringBufferConstantPoolName));
	}
}
public void newWrapperFor(int typeID) {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_new;
	switch (typeID) {
		case TypeIds.T_int : // new: java.lang.Integer
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Integer"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForType(ConstantPool.JavaLangIntegerConstantPoolName));
			break;
		case TypeIds.T_boolean : // new: java.lang.Boolean
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Boolean"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForType(ConstantPool.JavaLangBooleanConstantPoolName));
			break;
		case TypeIds.T_byte : // new: java.lang.Byte
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Byte"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForType(ConstantPool.JavaLangByteConstantPoolName));
			break;
		case TypeIds.T_char : // new: java.lang.Character
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Character"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForType(ConstantPool.JavaLangCharacterConstantPoolName));
			break;
		case TypeIds.T_float : // new: java.lang.Float
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Float"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForType(ConstantPool.JavaLangFloatConstantPoolName));
			break;
		case TypeIds.T_double : // new: java.lang.Double
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Double"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForType(ConstantPool.JavaLangDoubleConstantPoolName));
			break;
		case TypeIds.T_short : // new: java.lang.Short
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Short"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForType(ConstantPool.JavaLangShortConstantPoolName));
			break;
		case TypeIds.T_long : // new: java.lang.Long
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Long"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForType(ConstantPool.JavaLangLongConstantPoolName));
			break;
		case TypeIds.T_void : // new: java.lang.Void
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Void"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForType(ConstantPool.JavaLangVoidConstantPoolName));
	}
}
public void nop() {
	if (DEBUG) System.out.println(position + "\t\tnop"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_nop;
}
public void optimizeBranch(int oldPosition, BranchLabel lbl) {
	for (int i = 0; i < this.countLabels; i++) {
		BranchLabel label = this.labels[i];
		if (oldPosition == label.position) {
			label.position = position;
			if (label instanceof CaseLabel) {
				int offset = position - ((CaseLabel) label).instructionPosition;
				int[] forwardRefs = label.forwardReferences();
				for (int j = 0, length = label.forwardReferenceCount(); j < length; j++) {
					int forwardRef = forwardRefs[j];
					this.writeSignedWord(forwardRef, offset);
				}
			} else {
				int[] forwardRefs = label.forwardReferences();
				for (int j = 0, length = label.forwardReferenceCount(); j < length; j++) {
					final int forwardRef = forwardRefs[j];
					this.writePosition(lbl, forwardRef);
				}
			}
		}
	}
}
public void pop() {
	if (DEBUG) System.out.println(position + "\t\tpop"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_pop;
}
public void pop2() {
	if (DEBUG) System.out.println(position + "\t\tpop2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_pop2;
}
public void pushOnStack(TypeBinding binding) {
	if (++stackDepth > stackMax)
		stackMax = stackDepth;
}
public void pushExceptionOnStack(TypeBinding binding) {
	this.stackDepth = 1;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
}
public void putfield(FieldBinding fieldBinding) {
	if (DEBUG) System.out.println(position + "\t\tputfield:"+fieldBinding); //$NON-NLS-1$
	int returnTypeSize = 1;
	if ((fieldBinding.type.id == TypeIds.T_double) || (fieldBinding.type.id == TypeIds.T_long)) {
		returnTypeSize = 2;
	}
	generateFieldAccess(
			Opcodes.OPC_putfield,
			returnTypeSize,
			fieldBinding.declaringClass,
			fieldBinding.name,
			fieldBinding.type);
}
public void putstatic(FieldBinding fieldBinding) {
	if (DEBUG) System.out.println(position + "\t\tputstatic:"+fieldBinding); //$NON-NLS-1$
	int returnTypeSize = 1;
	if ((fieldBinding.type.id == TypeIds.T_double) || (fieldBinding.type.id == TypeIds.T_long)) {
		returnTypeSize = 2;
	}
	generateFieldAccess(
			Opcodes.OPC_putstatic,
			returnTypeSize,
			fieldBinding.declaringClass,
			fieldBinding.name,
			fieldBinding.type);
}
public void record(LocalVariableBinding local) {
	if ((this.generateAttributes & (ClassFileConstants.ATTR_VARS
			| ClassFileConstants.ATTR_STACK_MAP_TABLE
			| ClassFileConstants.ATTR_STACK_MAP)) == 0)
		return;
	if (allLocalsCounter == locals.length) {
		// resize the collection
		System.arraycopy(locals, 0, locals = new LocalVariableBinding[allLocalsCounter + LOCALS_INCREMENT], 0, allLocalsCounter);
	}
	locals[allLocalsCounter++] = local;
	local.initializationPCs = new int[4];
	local.initializationCount = 0;
}

public void recordExpressionType(TypeBinding typeBinding) {
	// nothing to do
}
public void recordPositionsFrom(int startPC, int sourcePos) {
	this.recordPositionsFrom(startPC, sourcePos, false);
}
public void recordPositionsFrom(int startPC, int sourcePos, boolean widen) {

	/* Record positions in the table, only if nothing has 
	 * already been recorded. Since we output them on the way 
	 * up (children first for more specific info)
	 * The pcToSourceMap table is always sorted.
	 */

	if ((this.generateAttributes & ClassFileConstants.ATTR_LINES) == 0
			|| sourcePos == 0
			|| (startPC == position && !widen))
		return;

	// Widening an existing entry that already has the same source positions
	if (pcToSourceMapSize + 4 > pcToSourceMap.length) {
		// resize the array pcToSourceMap
		System.arraycopy(pcToSourceMap, 0, pcToSourceMap = new int[pcToSourceMapSize << 1], 0, pcToSourceMapSize);
	}
	// lastEntryPC represents the endPC of the lastEntry.
	if (pcToSourceMapSize > 0) {
		int lineNumber;
		int previousLineNumber = pcToSourceMap[pcToSourceMapSize - 1];
		if (this.lineNumberStart == this.lineNumberEnd) {
			// method on one line
			lineNumber = this.lineNumberStart;
		} else {
			// Check next line number if this is the one we are looking for
			int[] lineSeparatorPositions2 = this.lineSeparatorPositions;
			int length = lineSeparatorPositions2.length;
			if (previousLineNumber == 1) {
				if (sourcePos < lineSeparatorPositions2[0]) {
					lineNumber = 1;
					/* the last recorded entry is on the same line. But it could be relevant to widen this entry.
					   we want to extend this entry forward in case we generated some bytecode before the last entry that are not related to any statement
					*/	
					if (startPC < pcToSourceMap[pcToSourceMapSize - 2]) {
						int insertionIndex = insertionIndex(pcToSourceMap, pcToSourceMapSize, startPC);
						if (insertionIndex != -1) {
							// widen the existing entry
							// we have to figure out if we need to move the last entry at another location to keep a sorted table
							/* First we need to check if at the insertion position there is not an existing entry
							 * that includes the one we want to insert. This is the case if pcToSourceMap[insertionIndex - 1] == newLine.
							 * In this case we don't want to change the table. If not, we want to insert a new entry. Prior to insertion
							 * we want to check if it is worth doing an arraycopy. If not we simply update the recorded pc.
							 */
							if (!((insertionIndex > 1) && (pcToSourceMap[insertionIndex - 1] == lineNumber))) {
								if ((pcToSourceMapSize > 4) && (pcToSourceMap[pcToSourceMapSize - 4] > startPC)) {
									System.arraycopy(pcToSourceMap, insertionIndex, pcToSourceMap, insertionIndex + 2, pcToSourceMapSize - 2 - insertionIndex);
									pcToSourceMap[insertionIndex++] = startPC;
									pcToSourceMap[insertionIndex] = lineNumber;
								} else {
									pcToSourceMap[pcToSourceMapSize - 2] = startPC;
								}
							}
						}
					}
					lastEntryPC = position;
					return;
				} else if (length == 1 || sourcePos < lineSeparatorPositions2[1]) {
					lineNumber = 2;
					if (startPC <= lastEntryPC) {
						// we forgot to add an entry.
						// search if an existing entry exists for startPC
						int insertionIndex = insertionIndex(pcToSourceMap, pcToSourceMapSize, startPC);
						if (insertionIndex != -1) {
							// there is no existing entry starting with startPC.
							int existingEntryIndex = indexOfSameLineEntrySincePC(startPC, lineNumber); // index for PC
							/* the existingEntryIndex corresponds to an entry with the same line and a PC >= startPC.
								in this case it is relevant to widen this entry instead of creating a new one.
								line1: this(a,
								  b,
								  c);
								with this code we generate each argument. We generate a aload0 to invoke the constructor. There is no entry for this
								aload0 bytecode. The first entry is the one for the argument a.
								But we want the constructor call to start at the aload0 pc and not just at the pc of the first argument.
								So we widen the existing entry (if there is one) or we create a new entry with the startPC.
							*/
							if (existingEntryIndex != -1) {
								// widen existing entry
								pcToSourceMap[existingEntryIndex] = startPC;
							} else if (insertionIndex < 1 || pcToSourceMap[insertionIndex - 1] != lineNumber) {
								// we have to add an entry that won't be sorted. So we sort the pcToSourceMap.
								System.arraycopy(pcToSourceMap, insertionIndex, pcToSourceMap, insertionIndex + 2, pcToSourceMapSize - insertionIndex);
								pcToSourceMap[insertionIndex++] = startPC;
								pcToSourceMap[insertionIndex] = lineNumber;
								pcToSourceMapSize += 2;
							}
						} else if (position != lastEntryPC) { // no bytecode since last entry pc
							if (lastEntryPC == startPC || lastEntryPC == pcToSourceMap[pcToSourceMapSize - 2]) {
								pcToSourceMap[pcToSourceMapSize - 1] = lineNumber;
							} else {
								pcToSourceMap[pcToSourceMapSize++] = lastEntryPC;
								pcToSourceMap[pcToSourceMapSize++] = lineNumber;
							}
						} else if (pcToSourceMap[pcToSourceMapSize - 1] < lineNumber && widen) {
							// see if we can widen the existing entry
							pcToSourceMap[pcToSourceMapSize - 1] = lineNumber;
						}
					} else {
						// we can safely add the new entry. The endPC of the previous entry is not in conflit with the startPC of the new entry.
						pcToSourceMap[pcToSourceMapSize++] = startPC;
						pcToSourceMap[pcToSourceMapSize++] = lineNumber;
					}
					lastEntryPC = position;
					return;
				} else {
					// since lineSeparatorPositions is zero-based, we pass this.lineNumberStart - 1 and this.lineNumberEnd - 1
					lineNumber = Util.getLineNumber(sourcePos, lineSeparatorPositions, this.lineNumberStart - 1, this.lineNumberEnd - 1);
				}
			} else if (previousLineNumber < length) {
				if (lineSeparatorPositions2[previousLineNumber - 2] < sourcePos) {
					if (sourcePos < lineSeparatorPositions2[previousLineNumber - 1]) {
						lineNumber = previousLineNumber;
						/* the last recorded entry is on the same line. But it could be relevant to widen this entry.
						   we want to extend this entry forward in case we generated some bytecode before the last entry that are not related to any statement
						*/	
						if (startPC < pcToSourceMap[pcToSourceMapSize - 2]) {
							int insertionIndex = insertionIndex(pcToSourceMap, pcToSourceMapSize, startPC);
							if (insertionIndex != -1) {
								// widen the existing entry
								// we have to figure out if we need to move the last entry at another location to keep a sorted table
								/* First we need to check if at the insertion position there is not an existing entry
								 * that includes the one we want to insert. This is the case if pcToSourceMap[insertionIndex - 1] == newLine.
								 * In this case we don't want to change the table. If not, we want to insert a new entry. Prior to insertion
								 * we want to check if it is worth doing an arraycopy. If not we simply update the recorded pc.
								 */
								if (!((insertionIndex > 1) && (pcToSourceMap[insertionIndex - 1] == lineNumber))) {
									if ((pcToSourceMapSize > 4) && (pcToSourceMap[pcToSourceMapSize - 4] > startPC)) {
										System.arraycopy(pcToSourceMap, insertionIndex, pcToSourceMap, insertionIndex + 2, pcToSourceMapSize - 2 - insertionIndex);
										pcToSourceMap[insertionIndex++] = startPC;
										pcToSourceMap[insertionIndex] = lineNumber;
									} else {
										pcToSourceMap[pcToSourceMapSize - 2] = startPC;
									}
								}
							}
						}
						lastEntryPC = position;
						return;
					} else if (sourcePos < lineSeparatorPositions2[previousLineNumber]) {
						lineNumber = previousLineNumber + 1;
						if (startPC <= lastEntryPC) {
							// we forgot to add an entry.
							// search if an existing entry exists for startPC
							int insertionIndex = insertionIndex(pcToSourceMap, pcToSourceMapSize, startPC);
							if (insertionIndex != -1) {
								// there is no existing entry starting with startPC.
								int existingEntryIndex = indexOfSameLineEntrySincePC(startPC, lineNumber); // index for PC
								/* the existingEntryIndex corresponds to an entry with the same line and a PC >= startPC.
									in this case it is relevant to widen this entry instead of creating a new one.
									line1: this(a,
									  b,
									  c);
									with this code we generate each argument. We generate a aload0 to invoke the constructor. There is no entry for this
									aload0 bytecode. The first entry is the one for the argument a.
									But we want the constructor call to start at the aload0 pc and not just at the pc of the first argument.
									So we widen the existing entry (if there is one) or we create a new entry with the startPC.
								*/
								if (existingEntryIndex != -1) {
									// widen existing entry
									pcToSourceMap[existingEntryIndex] = startPC;
								} else if (insertionIndex < 1 || pcToSourceMap[insertionIndex - 1] != lineNumber) {
									// we have to add an entry that won't be sorted. So we sort the pcToSourceMap.
									System.arraycopy(pcToSourceMap, insertionIndex, pcToSourceMap, insertionIndex + 2, pcToSourceMapSize - insertionIndex);
									pcToSourceMap[insertionIndex++] = startPC;
									pcToSourceMap[insertionIndex] = lineNumber;
									pcToSourceMapSize += 2;
								}
							} else if (position != lastEntryPC) { // no bytecode since last entry pc
								if (lastEntryPC == startPC || lastEntryPC == pcToSourceMap[pcToSourceMapSize - 2]) {
									pcToSourceMap[pcToSourceMapSize - 1] = lineNumber;
								} else {
									pcToSourceMap[pcToSourceMapSize++] = lastEntryPC;
									pcToSourceMap[pcToSourceMapSize++] = lineNumber;
								}
							} else if (pcToSourceMap[pcToSourceMapSize - 1] < lineNumber && widen) {
								// see if we can widen the existing entry
								pcToSourceMap[pcToSourceMapSize - 1] = lineNumber;
							}
						} else {
							// we can safely add the new entry. The endPC of the previous entry is not in conflit with the startPC of the new entry.
							pcToSourceMap[pcToSourceMapSize++] = startPC;
							pcToSourceMap[pcToSourceMapSize++] = lineNumber;
						}
						lastEntryPC = position;
						return;
					} else {
						// since lineSeparatorPositions is zero-based, we pass this.lineNumberStart - 1 and this.lineNumberEnd - 1
						lineNumber = Util.getLineNumber(sourcePos, lineSeparatorPositions, this.lineNumberStart - 1, this.lineNumberEnd - 1);
					}
				} else {
					// since lineSeparatorPositions is zero-based, we pass this.lineNumberStart - 1 and this.lineNumberEnd - 1
					lineNumber = Util.getLineNumber(sourcePos, lineSeparatorPositions, this.lineNumberStart - 1, this.lineNumberEnd - 1);
				}
			} else if (lineSeparatorPositions2[length - 1] < sourcePos) {
				lineNumber = length + 1;
				if (startPC <= lastEntryPC) {
					// we forgot to add an entry.
					// search if an existing entry exists for startPC
					int insertionIndex = insertionIndex(pcToSourceMap, pcToSourceMapSize, startPC);
					if (insertionIndex != -1) {
						// there is no existing entry starting with startPC.
						int existingEntryIndex = indexOfSameLineEntrySincePC(startPC, lineNumber); // index for PC
						/* the existingEntryIndex corresponds to an entry with the same line and a PC >= startPC.
							in this case it is relevant to widen this entry instead of creating a new one.
							line1: this(a,
							  b,
							  c);
							with this code we generate each argument. We generate a aload0 to invoke the constructor. There is no entry for this
							aload0 bytecode. The first entry is the one for the argument a.
							But we want the constructor call to start at the aload0 pc and not just at the pc of the first argument.
							So we widen the existing entry (if there is one) or we create a new entry with the startPC.
						*/
						if (existingEntryIndex != -1) {
							// widen existing entry
							pcToSourceMap[existingEntryIndex] = startPC;
						} else if (insertionIndex < 1 || pcToSourceMap[insertionIndex - 1] != lineNumber) {
							// we have to add an entry that won't be sorted. So we sort the pcToSourceMap.
							System.arraycopy(pcToSourceMap, insertionIndex, pcToSourceMap, insertionIndex + 2, pcToSourceMapSize - insertionIndex);
							pcToSourceMap[insertionIndex++] = startPC;
							pcToSourceMap[insertionIndex] = lineNumber;
							pcToSourceMapSize += 2;
						}
					} else if (position != lastEntryPC) { // no bytecode since last entry pc
						if (lastEntryPC == startPC || lastEntryPC == pcToSourceMap[pcToSourceMapSize - 2]) {
							pcToSourceMap[pcToSourceMapSize - 1] = lineNumber;
						} else {
							pcToSourceMap[pcToSourceMapSize++] = lastEntryPC;
							pcToSourceMap[pcToSourceMapSize++] = lineNumber;
						}
					} else if (pcToSourceMap[pcToSourceMapSize - 1] < lineNumber && widen) {
						// see if we can widen the existing entry
						pcToSourceMap[pcToSourceMapSize - 1] = lineNumber;
					}
				} else {
					// we can safely add the new entry. The endPC of the previous entry is not in conflit with the startPC of the new entry.
					pcToSourceMap[pcToSourceMapSize++] = startPC;
					pcToSourceMap[pcToSourceMapSize++] = lineNumber;
				}
				lastEntryPC = position;
				return;
			} else {
				// since lineSeparatorPositions is zero-based, we pass this.lineNumberStart - 1 and this.lineNumberEnd - 1
				lineNumber = Util.getLineNumber(sourcePos, lineSeparatorPositions, this.lineNumberStart - 1, this.lineNumberEnd - 1);
			}
		}
		// in this case there is already an entry in the table
		if (previousLineNumber != lineNumber) {
			if (startPC <= lastEntryPC) {
				// we forgot to add an entry.
				// search if an existing entry exists for startPC
				int insertionIndex = insertionIndex(pcToSourceMap, pcToSourceMapSize, startPC);
				if (insertionIndex != -1) {
					// there is no existing entry starting with startPC.
					int existingEntryIndex = indexOfSameLineEntrySincePC(startPC, lineNumber); // index for PC
					/* the existingEntryIndex corresponds to an entry with the same line and a PC >= startPC.
						in this case it is relevant to widen this entry instead of creating a new one.
						line1: this(a,
						  b,
						  c);
						with this code we generate each argument. We generate a aload0 to invoke the constructor. There is no entry for this
						aload0 bytecode. The first entry is the one for the argument a.
						But we want the constructor call to start at the aload0 pc and not just at the pc of the first argument.
						So we widen the existing entry (if there is one) or we create a new entry with the startPC.
					*/
					if (existingEntryIndex != -1) {
						// widen existing entry
						pcToSourceMap[existingEntryIndex] = startPC;
					} else if (insertionIndex < 1 || pcToSourceMap[insertionIndex - 1] != lineNumber) {
						// we have to add an entry that won't be sorted. So we sort the pcToSourceMap.
						System.arraycopy(pcToSourceMap, insertionIndex, pcToSourceMap, insertionIndex + 2, pcToSourceMapSize - insertionIndex);
						pcToSourceMap[insertionIndex++] = startPC;
						pcToSourceMap[insertionIndex] = lineNumber;
						pcToSourceMapSize += 2;
					}
				} else if (position != lastEntryPC) { // no bytecode since last entry pc
					if (lastEntryPC == startPC || lastEntryPC == pcToSourceMap[pcToSourceMapSize - 2]) {
						pcToSourceMap[pcToSourceMapSize - 1] = lineNumber;
					} else {
						pcToSourceMap[pcToSourceMapSize++] = lastEntryPC;
						pcToSourceMap[pcToSourceMapSize++] = lineNumber;
					}
				} else if (pcToSourceMap[pcToSourceMapSize - 1] < lineNumber && widen) {
					// see if we can widen the existing entry
					pcToSourceMap[pcToSourceMapSize - 1] = lineNumber;
				}
			} else {
				// we can safely add the new entry. The endPC of the previous entry is not in conflit with the startPC of the new entry.
				pcToSourceMap[pcToSourceMapSize++] = startPC;
				pcToSourceMap[pcToSourceMapSize++] = lineNumber;
			}
		} else {
			/* the last recorded entry is on the same line. But it could be relevant to widen this entry.
			   we want to extend this entry forward in case we generated some bytecode before the last entry that are not related to any statement
			*/	
			if (startPC < pcToSourceMap[pcToSourceMapSize - 2]) {
				int insertionIndex = insertionIndex(pcToSourceMap, pcToSourceMapSize, startPC);
				if (insertionIndex != -1) {
					// widen the existing entry
					// we have to figure out if we need to move the last entry at another location to keep a sorted table
					/* First we need to check if at the insertion position there is not an existing entry
					 * that includes the one we want to insert. This is the case if pcToSourceMap[insertionIndex - 1] == newLine.
					 * In this case we don't want to change the table. If not, we want to insert a new entry. Prior to insertion
					 * we want to check if it is worth doing an arraycopy. If not we simply update the recorded pc.
					 */
					if (!((insertionIndex > 1) && (pcToSourceMap[insertionIndex - 1] == lineNumber))) {
						if ((pcToSourceMapSize > 4) && (pcToSourceMap[pcToSourceMapSize - 4] > startPC)) {
							System.arraycopy(pcToSourceMap, insertionIndex, pcToSourceMap, insertionIndex + 2, pcToSourceMapSize - 2 - insertionIndex);
							pcToSourceMap[insertionIndex++] = startPC;
							pcToSourceMap[insertionIndex] = lineNumber;
						} else {
							pcToSourceMap[pcToSourceMapSize - 2] = startPC;
						}
					}
				}
			}
		}
		lastEntryPC = position;
	} else {
		int lineNumber = 0;
		if (this.lineNumberStart == this.lineNumberEnd) {
			// method on one line
			lineNumber = this.lineNumberStart;
		} else {
			// since lineSeparatorPositions is zero-based, we pass this.lineNumberStart - 1 and this.lineNumberEnd - 1
			lineNumber = Util.getLineNumber(sourcePos, lineSeparatorPositions, this.lineNumberStart - 1, this.lineNumberEnd - 1);
		}
		// record the first entry
		pcToSourceMap[pcToSourceMapSize++] = startPC;
		pcToSourceMap[pcToSourceMapSize++] = lineNumber;
		lastEntryPC = position;
	}
}
/**
 * @param anExceptionLabel org.eclipse.jdt.internal.compiler.codegen.ExceptionLabel
 */
public void registerExceptionHandler(ExceptionLabel anExceptionLabel) {
	int length;
	if (exceptionLabelsCounter == (length = exceptionLabels.length)) {
		// resize the exception handlers table
		System.arraycopy(exceptionLabels, 0, exceptionLabels = new ExceptionLabel[length + LABELS_INCREMENT], 0, length);
	}
	// no need to resize. So just add the new exception label
	exceptionLabels[exceptionLabelsCounter++] = anExceptionLabel;
}
public void removeNotDefinitelyAssignedVariables(Scope scope, int initStateIndex) {
	// given some flow info, make sure we did not loose some variables initialization
	// if this happens, then we must update their pc entries to reflect it in debug attributes
	if ((this.generateAttributes & (ClassFileConstants.ATTR_VARS
			| ClassFileConstants.ATTR_STACK_MAP_TABLE
			| ClassFileConstants.ATTR_STACK_MAP)) == 0)
		return;
	for (int i = 0; i < visibleLocalsCount; i++) {
		LocalVariableBinding localBinding = visibleLocals[i];
		if (localBinding != null && !isDefinitelyAssigned(scope, initStateIndex, localBinding) && localBinding.initializationCount > 0) {
			localBinding.recordInitializationEndPC(position);
		}
	}
}
/**
 * Remove all entries in pcToSourceMap table that are beyond this.position
 */
public void removeUnusedPcToSourceMapEntries() {
	if (this.pcToSourceMapSize != 0) {
		while (this.pcToSourceMapSize >= 2 && this.pcToSourceMap[this.pcToSourceMapSize - 2] > this.position) {
			this.pcToSourceMapSize -= 2;
		}
	}
}
public void removeVariable(LocalVariableBinding localBinding) {
	if (localBinding == null) return;
	if (localBinding.initializationCount > 0) {
		localBinding.recordInitializationEndPC(position);
	}
	for (int i = visibleLocalsCount - 1; i >= 0; i--) {
		LocalVariableBinding visibleLocal = visibleLocals[i];
		if (visibleLocal == localBinding){
			visibleLocals[i] = null; // this variable is no longer visible afterwards
			return;
		}
	}
}
/**
 * @param referenceMethod org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
 * @param targetClassFile org.eclipse.jdt.internal.compiler.codegen.ClassFile
 */
public void reset(AbstractMethodDeclaration referenceMethod, ClassFile targetClassFile) {
	init(targetClassFile);
	this.methodDeclaration = referenceMethod;
	int[] lineSeparatorPositions2 = this.lineSeparatorPositions;
	if (lineSeparatorPositions2 != null) {
		int length = lineSeparatorPositions2.length;
		int lineSeparatorPositionsEnd = length - 1;
		if (referenceMethod.isClinit()
				|| referenceMethod.isConstructor()) {
			this.lineNumberStart = 1;
			this.lineNumberEnd = length == 0 ? 1 : length;
		} else {
			int start = Util.getLineNumber(referenceMethod.bodyStart, lineSeparatorPositions2, 0, lineSeparatorPositionsEnd);
			this.lineNumberStart = start;
			if (start > lineSeparatorPositionsEnd) {
				this.lineNumberEnd = start;
			} else {
				int end = Util.getLineNumber(referenceMethod.bodyEnd, lineSeparatorPositions2, start - 1, lineSeparatorPositionsEnd);
				if (end >= lineSeparatorPositionsEnd) {
					end = length;
				}
				this.lineNumberEnd = end == 0 ? 1 : end;
			}
		}
	}
	this.preserveUnusedLocals = referenceMethod.scope.compilerOptions().preserveAllLocalVariables;
	initializeMaxLocals(referenceMethod.binding);
}
public void reset(ClassFile givenClassFile) {
	this.targetLevel = givenClassFile.targetJDK;
	int produceAttributes = givenClassFile.produceAttributes;
	this.generateAttributes = produceAttributes;
	if ((produceAttributes & ClassFileConstants.ATTR_LINES) != 0) {
		this.lineSeparatorPositions = givenClassFile.referenceBinding.scope.referenceCompilationUnit().compilationResult.getLineSeparatorPositions();
	} else {
		this.lineSeparatorPositions = null;
	}
}
/**
 * @param targetClassFile The given classfile to reset the code stream
 */
public void resetForProblemClinit(ClassFile targetClassFile) {
	init(targetClassFile);
	initializeMaxLocals(null);
}
private final void resizeByteArray() {
	int length = bCodeStream.length;
	int requiredSize = length + length;
	if (classFileOffset >= requiredSize) {
		// must be sure to grow enough
		requiredSize = classFileOffset + length;
	}
	System.arraycopy(bCodeStream, 0, bCodeStream = new byte[requiredSize], 0, length);
}
final public void ret(int index) {
	if (DEBUG) System.out.println(position + "\t\tret:"+index); //$NON-NLS-1$
	countLabels = 0;
	if (index > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_wide;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ret;
		writeUnsignedShort(index);
	} else { // Don't Widen
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = Opcodes.OPC_ret;
		bCodeStream[classFileOffset++] = (byte) index;
	}
}
public void return_() {
	if (DEBUG) System.out.println(position + "\t\treturn"); //$NON-NLS-1$
	countLabels = 0;
	// the stackDepth should be equal to 0 
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_return;
	this.lastAbruptCompletion = this.position;	
}
public void saload() {
	if (DEBUG) System.out.println(position + "\t\tsaload"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_saload;
}
public void sastore() {
	if (DEBUG) System.out.println(position + "\t\tsastore"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_sastore;
}
/**
 * @param operatorConstant int
 * @param type_ID int
 */
public void sendOperator(int operatorConstant, int type_ID) {
	switch (type_ID) {
		case TypeIds.T_int :
		case TypeIds.T_boolean :
		case TypeIds.T_char :
		case TypeIds.T_byte :
		case TypeIds.T_short :
			switch (operatorConstant) {
				case OperatorIds.PLUS :
					this.iadd();
					break;
				case OperatorIds.MINUS :
					this.isub();
					break;
				case OperatorIds.MULTIPLY :
					this.imul();
					break;
				case OperatorIds.DIVIDE :
					this.idiv();
					break;
				case OperatorIds.REMAINDER :
					this.irem();
					break;
				case OperatorIds.LEFT_SHIFT :
					this.ishl();
					break;
				case OperatorIds.RIGHT_SHIFT :
					this.ishr();
					break;
				case OperatorIds.UNSIGNED_RIGHT_SHIFT :
					this.iushr();
					break;
				case OperatorIds.AND :
					this.iand();
					break;
				case OperatorIds.OR :
					this.ior();
					break;
				case OperatorIds.XOR :
					this.ixor();
					break;
			}
			break;
		case TypeIds.T_long :
			switch (operatorConstant) {
				case OperatorIds.PLUS :
					this.ladd();
					break;
				case OperatorIds.MINUS :
					this.lsub();
					break;
				case OperatorIds.MULTIPLY :
					this.lmul();
					break;
				case OperatorIds.DIVIDE :
					this.ldiv();
					break;
				case OperatorIds.REMAINDER :
					this.lrem();
					break;
				case OperatorIds.LEFT_SHIFT :
					this.lshl();
					break;
				case OperatorIds.RIGHT_SHIFT :
					this.lshr();
					break;
				case OperatorIds.UNSIGNED_RIGHT_SHIFT :
					this.lushr();
					break;
				case OperatorIds.AND :
					this.land();
					break;
				case OperatorIds.OR :
					this.lor();
					break;
				case OperatorIds.XOR :
					this.lxor();
					break;
			}
			break;
		case TypeIds.T_float :
			switch (operatorConstant) {
				case OperatorIds.PLUS :
					this.fadd();
					break;
				case OperatorIds.MINUS :
					this.fsub();
					break;
				case OperatorIds.MULTIPLY :
					this.fmul();
					break;
				case OperatorIds.DIVIDE :
					this.fdiv();
					break;
				case OperatorIds.REMAINDER :
					this.frem();
			}
			break;
		case TypeIds.T_double :
			switch (operatorConstant) {
				case OperatorIds.PLUS :
					this.dadd();
					break;
				case OperatorIds.MINUS :
					this.dsub();
					break;
				case OperatorIds.MULTIPLY :
					this.dmul();
					break;
				case OperatorIds.DIVIDE :
					this.ddiv();
					break;
				case OperatorIds.REMAINDER :
					this.drem();
			}
	}
}

public void sipush(int s) {
	if (DEBUG) System.out.println(position + "\t\tsipush:"+s); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_sipush;
	writeSignedShort(s);
}
public void store(LocalVariableBinding localBinding, boolean valueRequired) {
	int localPosition = localBinding.resolvedPosition;
	// Using dedicated int bytecode
	switch(localBinding.type.id) {
		case TypeIds.T_int :
		case TypeIds.T_char :
		case TypeIds.T_byte :
		case TypeIds.T_short :
		case TypeIds.T_boolean :
			if (valueRequired)
				this.dup();
			switch (localPosition) {
				case 0 :
					this.istore_0();
					break;
				case 1 :
					this.istore_1();
					break;
				case 2 :
					this.istore_2();
					break;
				case 3 :
					this.istore_3();
					break;
				//case -1 :
				// internal failure: trying to store into variable not supposed to be generated
				//	break;
				default :
					this.istore(localPosition);
			}
			break;
		case TypeIds.T_float :
			if (valueRequired)
				this.dup();
			switch (localPosition) {
				case 0 :
					this.fstore_0();
					break;
				case 1 :
					this.fstore_1();
					break;
				case 2 :
					this.fstore_2();
					break;
				case 3 :
					this.fstore_3();
					break;
				default :
					this.fstore(localPosition);
			}
			break;
		case TypeIds.T_double :
			if (valueRequired)
				this.dup2();
			switch (localPosition) {
				case 0 :
					this.dstore_0();
					break;
				case 1 :
					this.dstore_1();
					break;
				case 2 :
					this.dstore_2();
					break;
				case 3 :
					this.dstore_3();
					break;
				default :
					this.dstore(localPosition);
			}
			break;
		case TypeIds.T_long :
			if (valueRequired)
				this.dup2();
			switch (localPosition) {
				case 0 :
					this.lstore_0();
					break;
				case 1 :
					this.lstore_1();
					break;
				case 2 :
					this.lstore_2();
					break;
				case 3 :
					this.lstore_3();
					break;
				default :
					this.lstore(localPosition);
			}
			break;
		default:
			// Reference object
			if (valueRequired)
				this.dup();
			switch (localPosition) {
				case 0 :
					this.astore_0();
					break;
				case 1 :
					this.astore_1();
					break;
				case 2 :
					this.astore_2();
					break;
				case 3 :
					this.astore_3();
					break;
				default :
					this.astore(localPosition);
			}
	}
}
public void swap() {
	if (DEBUG) System.out.println(position + "\t\tswap"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_swap;
}
public void tableswitch(CaseLabel defaultLabel, int low, int high, int[] keys, int[] sortedIndexes, CaseLabel[] casesLabel) {
	if (DEBUG) System.out.println(position + "\t\ttableswitch"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	int length = casesLabel.length;
	int pos = position;
	defaultLabel.placeInstruction();
	for (int i = 0; i < length; i++)
		casesLabel[i].placeInstruction();
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = Opcodes.OPC_tableswitch;
	// padding
	for (int i = (3 - (pos & 3)); i > 0; i--) {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = 0;
	}
	defaultLabel.branch();
	writeSignedWord(low);
	writeSignedWord(high);
	int i = low, j = low;
	// the index j is used to know if the index i is one of the missing entries in case of an 
	// optimized tableswitch
	while (true) {
		int index;
		int key = keys[index = sortedIndexes[j - low]];
		if (key == i) {
			casesLabel[index].branch();
			j++;
			if (i == high) break; // if high is maxint, then avoids wrapping to minint.
		} else {
			defaultLabel.branch();
		}
		i++;
	}
}
public void throwAnyException(LocalVariableBinding anyExceptionVariable) {
	this.load(anyExceptionVariable);
	this.athrow();
}
public String toString() {
	StringBuffer buffer = new StringBuffer("( position:"); //$NON-NLS-1$
	buffer.append(position);
	buffer.append(",\nstackDepth:"); //$NON-NLS-1$
	buffer.append(stackDepth);
	buffer.append(",\nmaxStack:"); //$NON-NLS-1$
	buffer.append(stackMax);
	buffer.append(",\nmaxLocals:"); //$NON-NLS-1$
	buffer.append(maxLocals);
	buffer.append(")"); //$NON-NLS-1$
	return buffer.toString();
}
/**
 * Note: it will walk the locals table and extend the end range for all matching ones, no matter if
 * visible or not.
 * {  int i = 0;
 *    {  int j = 1; }
 * }   <== would process both 'i' and 'j'
 * Processing non-visible ones is mandated in some cases (include goto instruction after if-then block)
 */
public void updateLastRecordedEndPC(Scope scope, int pos) {

	/* Tune positions in the table, this is due to some 
	 * extra bytecodes being
	 * added to some user code (jumps). */
	/** OLD CODE
		if (!generateLineNumberAttributes)
			return;
		pcToSourceMap[pcToSourceMapSize - 1][1] = position;
		// need to update the initialization endPC in case of generation of local variable attributes.
		updateLocalVariablesAttribute(pos);	
	*/	

	if ((this.generateAttributes & ClassFileConstants.ATTR_LINES) != 0) {
		this.lastEntryPC = pos;
	}
	// need to update the initialization endPC in case of generation of local variable attributes.
	if ((this.generateAttributes & (ClassFileConstants.ATTR_VARS
			| ClassFileConstants.ATTR_STACK_MAP_TABLE
			| ClassFileConstants.ATTR_STACK_MAP)) != 0) {
		for (int i = 0, max = this.locals.length; i < max; i++) {
			LocalVariableBinding local = this.locals[i];
			if (local != null && local.declaringScope == scope && local.initializationCount > 0) {
				if (local.initializationPCs[((local.initializationCount - 1) << 1) + 1] == pos) {
					local.initializationPCs[((local.initializationCount - 1) << 1) + 1] = this.position;
				}
			}
		}
	}
}
protected void writePosition(BranchLabel label) {
	int offset = label.position - this.position + 1;
	if (Math.abs(offset) > 0x7FFF && !this.wideMode) {
		throw new AbortMethod(CodeStream.RESTART_IN_WIDE_MODE, null);
	}
	this.writeSignedShort(offset);
	int[] forwardRefs = label.forwardReferences();
	for (int i = 0, max = label.forwardReferenceCount(); i < max; i++) {
		this.writePosition(label, forwardRefs[i]);
	}	
}
protected void writePosition(BranchLabel label, int forwardReference) {
	final int offset = label.position - forwardReference + 1;
	if (Math.abs(offset) > 0x7FFF && !this.wideMode) {
		throw new AbortMethod(CodeStream.RESTART_IN_WIDE_MODE, null);
	}
	if (this.wideMode) {
		if ((label.tagBits & BranchLabel.WIDE) != 0) {
			this.writeSignedWord(forwardReference, offset);
		} else {
			this.writeSignedShort(forwardReference, offset);
		}
	} else {
		this.writeSignedShort(forwardReference, offset);
	}
}
/**
 * Write a signed 16 bits value into the byte array
 * @param value the signed short
 */
private final void writeSignedShort(int value) {
	// we keep the resize in here because it is used outside the code stream
	if (classFileOffset + 1 >= bCodeStream.length) {
		resizeByteArray();
	}
	position += 2;
	bCodeStream[classFileOffset++] = (byte) (value >> 8);
	bCodeStream[classFileOffset++] = (byte) value;
}
private final void writeSignedShort(int pos, int value) {
	int currentOffset = startingClassFileOffset + pos;
	if (currentOffset + 1 >= bCodeStream.length) {
		resizeByteArray();
	}
	bCodeStream[currentOffset] = (byte) (value >> 8);
	bCodeStream[currentOffset + 1] = (byte) value;
}
protected final void writeSignedWord(int value) {
	// we keep the resize in here because it is used outside the code stream
	if (classFileOffset + 3 >= bCodeStream.length) {
		resizeByteArray();
	}
	position += 4;
	bCodeStream[classFileOffset++] = (byte) ((value & 0xFF000000) >> 24);
	bCodeStream[classFileOffset++] = (byte) ((value & 0xFF0000) >> 16);
	bCodeStream[classFileOffset++] = (byte) ((value & 0xFF00) >> 8);
	bCodeStream[classFileOffset++] = (byte) (value & 0xFF);
}
protected void writeSignedWord(int pos, int value) {
	int currentOffset = startingClassFileOffset + pos;
	if (currentOffset + 3 >= bCodeStream.length) {
		resizeByteArray();
	}
	bCodeStream[currentOffset++] = (byte) ((value & 0xFF000000) >> 24);
	bCodeStream[currentOffset++] = (byte) ((value & 0xFF0000) >> 16);
	bCodeStream[currentOffset++] = (byte) ((value & 0xFF00) >> 8);
	bCodeStream[currentOffset++] = (byte) (value & 0xFF);
}
/**
 * Write a unsigned 16 bits value into the byte array
 * @param value the unsigned short
 */
private final void writeUnsignedShort(int value) {
	// no bound check since used only from within codestream where already checked
	position += 2;
	bCodeStream[classFileOffset++] = (byte) (value >>> 8);
	bCodeStream[classFileOffset++] = (byte) value;
}
protected void writeWidePosition(BranchLabel label) {
	int labelPos = label.position;
	int offset = labelPos - this.position + 1;
	this.writeSignedWord(offset);
	int[] forwardRefs = label.forwardReferences();
	for (int i = 0, max = label.forwardReferenceCount(); i < max; i++) {
		int forward = forwardRefs[i];
		offset = labelPos - forward + 1;
		this.writeSignedWord(forward, offset);
	}	
}
public void resetInWideMode() {
	this.wideMode = true;
}
}
