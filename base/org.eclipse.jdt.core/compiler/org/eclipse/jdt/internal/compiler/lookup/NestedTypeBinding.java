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
package org.eclipse.jdt.internal.compiler.lookup;

public class NestedTypeBinding extends SourceTypeBinding {

	public SourceTypeBinding enclosingType;

	public SyntheticArgumentBinding[] enclosingInstances;
	public SyntheticArgumentBinding[] outerLocalVariables;
	public int enclosingInstancesSlotSize; // amount of slots used by synthetic enclosing instances
	public int outerLocalVariablesSlotSize; // amount of slots used by synthetic outer local variables
	
	public NestedTypeBinding(char[][] typeName, ClassScope scope, SourceTypeBinding enclosingType) {
		super(typeName, enclosingType.fPackage, scope);
		this.tagBits |= TagBits.IsNestedType;
		this.enclosingType = enclosingType;
	}
	
	/* Add a new synthetic argument for <actualOuterLocalVariable>.
	* Answer the new argument or the existing argument if one already existed.
	*/
	public SyntheticArgumentBinding addSyntheticArgument(LocalVariableBinding actualOuterLocalVariable) {
		SyntheticArgumentBinding synthLocal = null;
	
		if (outerLocalVariables == null) {
			synthLocal = new SyntheticArgumentBinding(actualOuterLocalVariable);
			outerLocalVariables = new SyntheticArgumentBinding[] {synthLocal};
		} else {
			int size = outerLocalVariables.length;
			int newArgIndex = size;
			for (int i = size; --i >= 0;) {		// must search backwards
				if (outerLocalVariables[i].actualOuterLocalVariable == actualOuterLocalVariable)
					return outerLocalVariables[i];	// already exists
				if (outerLocalVariables[i].id > actualOuterLocalVariable.id)
					newArgIndex = i;
			}
			SyntheticArgumentBinding[] synthLocals = new SyntheticArgumentBinding[size + 1];
			System.arraycopy(outerLocalVariables, 0, synthLocals, 0, newArgIndex);
			synthLocals[newArgIndex] = synthLocal = new SyntheticArgumentBinding(actualOuterLocalVariable);
			System.arraycopy(outerLocalVariables, newArgIndex, synthLocals, newArgIndex + 1, size - newArgIndex);
			outerLocalVariables = synthLocals;
		}
		//System.out.println("Adding synth arg for local var: " + new String(actualOuterLocalVariable.name) + " to: " + new String(this.readableName()));
		if (scope.referenceCompilationUnit().isPropagatingInnerClassEmulation)
			this.updateInnerEmulationDependents();
		return synthLocal;
	}

	/* Add a new synthetic argument for <enclosingType>.
	* Answer the new argument or the existing argument if one already existed.
	*/
	public SyntheticArgumentBinding addSyntheticArgument(ReferenceBinding targetEnclosingType) {
		SyntheticArgumentBinding synthLocal = null;
		if (enclosingInstances == null) {
			synthLocal = new SyntheticArgumentBinding(targetEnclosingType);
			enclosingInstances = new SyntheticArgumentBinding[] {synthLocal};
		} else {
			int size = enclosingInstances.length;
			int newArgIndex = size;
			for (int i = size; --i >= 0;) {
				if (enclosingInstances[i].type == targetEnclosingType)
					return enclosingInstances[i]; // already exists
				if (this.enclosingType() == targetEnclosingType)
					newArgIndex = 0;
			}
			SyntheticArgumentBinding[] newInstances = new SyntheticArgumentBinding[size + 1];
			System.arraycopy(enclosingInstances, 0, newInstances, newArgIndex == 0 ? 1 : 0, size);
			newInstances[newArgIndex] = synthLocal = new SyntheticArgumentBinding(targetEnclosingType);
			enclosingInstances = newInstances;
		}
		//System.out.println("Adding synth arg for enclosing type: " + new String(enclosingType.readableName()) + " to: " + new String(this.readableName()));
		if (scope.referenceCompilationUnit().isPropagatingInnerClassEmulation)
			this.updateInnerEmulationDependents();
		return synthLocal;
	}

	/* Add a new synthetic argument and field for <actualOuterLocalVariable>.
	* Answer the new argument or the existing argument if one already existed.
	*/
	public SyntheticArgumentBinding addSyntheticArgumentAndField(LocalVariableBinding actualOuterLocalVariable) {
		SyntheticArgumentBinding synthLocal = addSyntheticArgument(actualOuterLocalVariable);
		if (synthLocal == null) return null;
	
		if (synthLocal.matchingField == null)
			synthLocal.matchingField = addSyntheticFieldForInnerclass(actualOuterLocalVariable);
		return synthLocal;
	}

	/* Add a new synthetic argument and field for <enclosingType>.
	* Answer the new argument or the existing argument if one already existed.
	*/
	public SyntheticArgumentBinding addSyntheticArgumentAndField(ReferenceBinding targetEnclosingType) {
		SyntheticArgumentBinding synthLocal = addSyntheticArgument(targetEnclosingType);
		if (synthLocal == null) return null;
	
		if (synthLocal.matchingField == null)
			synthLocal.matchingField = addSyntheticFieldForInnerclass(targetEnclosingType);
		return synthLocal;
	}

	/**
	 * Compute the resolved positions for all the synthetic arguments
	 */
	final public void computeSyntheticArgumentSlotSizes() {
	
		int slotSize = 0; 
		// insert enclosing instances first, followed by the outerLocals
		int enclosingInstancesCount = this.enclosingInstances == null ? 0 : this.enclosingInstances.length;
		for (int i = 0; i < enclosingInstancesCount; i++){
			SyntheticArgumentBinding argument = this.enclosingInstances[i];
			// position the enclosing instance synthetic arg
			argument.resolvedPosition = slotSize + 1; // shift by 1 to leave room for aload0==this
			if (slotSize + 1 > 0xFF) { // no more than 255 words of arguments
				this.scope.problemReporter().noMoreAvailableSpaceForArgument(argument, this.scope.referenceType()); 
			}
			if ((argument.type == TypeBinding.LONG) || (argument.type == TypeBinding.DOUBLE)){
				slotSize += 2;
			} else {
				slotSize ++;
			}
		}
		this.enclosingInstancesSlotSize = slotSize; 
		
		slotSize = 0; // reset, outer local are not positionned yet, since will be appended to user arguments
		int outerLocalsCount = this.outerLocalVariables == null ? 0 : this.outerLocalVariables.length;
			for (int i = 0; i < outerLocalsCount; i++){
			SyntheticArgumentBinding argument = this.outerLocalVariables[i];
			// do NOT position the outerlocal synthetic arg yet,  since will be appended to user arguments
			if ((argument.type == TypeBinding.LONG) || (argument.type == TypeBinding.DOUBLE)){
				slotSize += 2;
			} else {
				slotSize ++;
			}
		}
		this.outerLocalVariablesSlotSize = slotSize;
	}
	
	/* Answer the receiver's enclosing type... null if the receiver is a top level type.
	*/
	public ReferenceBinding enclosingType() {

		return enclosingType;
	}

	/* Answer the synthetic argument for <actualOuterLocalVariable> or null if one does not exist.
	*/
	public SyntheticArgumentBinding getSyntheticArgument(LocalVariableBinding actualOuterLocalVariable) {

		if (outerLocalVariables == null) return null;		// is null if no outer local variables are known
	
		for (int i = outerLocalVariables.length; --i >= 0;)
			if (outerLocalVariables[i].actualOuterLocalVariable == actualOuterLocalVariable)
				return outerLocalVariables[i];
		return null;
	}

	public SyntheticArgumentBinding[] syntheticEnclosingInstances() {
		return enclosingInstances;		// is null if no enclosing instances are required
	}

	public ReferenceBinding[] syntheticEnclosingInstanceTypes() {
		if (enclosingInstances == null)
			return null;
	
		int length = enclosingInstances.length;
		ReferenceBinding types[] = new ReferenceBinding[length];
		for (int i = 0; i < length; i++)
			types[i] = (ReferenceBinding) enclosingInstances[i].type;
		return types;
	}

	public SyntheticArgumentBinding[] syntheticOuterLocalVariables() {

		return outerLocalVariables;		// is null if no outer locals are required
	}

	/*
	 * Trigger the dependency mechanism forcing the innerclass emulation
	 * to be propagated to all dependent source types.
	 */
	public void updateInnerEmulationDependents() {
		// nothing to do in general, only local types are doing anything
	}
	
	/* Answer the synthetic argument for <targetEnclosingType> or null if one does not exist.
	*/
	public SyntheticArgumentBinding getSyntheticArgument(ReferenceBinding targetEnclosingType, boolean onlyExactMatch) {

		if (enclosingInstances == null) return null;		// is null if no enclosing instances are known
	
		// exact match
		for (int i = enclosingInstances.length; --i >= 0;)
			if (enclosingInstances[i].type == targetEnclosingType)
				if (enclosingInstances[i].actualOuterLocalVariable == null)
					return enclosingInstances[i];
	
		// type compatibility : to handle cases such as
		// class T { class M{}}
		// class S extends T { class N extends M {}} --> need to use S as a default enclosing instance for the super constructor call in N().
		if (!onlyExactMatch){
			for (int i = enclosingInstances.length; --i >= 0;)
				if (enclosingInstances[i].actualOuterLocalVariable == null)
					if (enclosingInstances[i].type.findSuperTypeOriginatingFrom(targetEnclosingType) != null)
						return enclosingInstances[i];
		}
		return null;
	}
}
