/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								bug 395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
 *								Bug 417295 - [1.8[[null] Massage type annotated null analysis to gel well with deep encoded type bindings.
 *								Bug 438458 - [1.8][null] clean up handling of null type annotations wrt type variables
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

public class BaseTypeBinding extends TypeBinding {

	public static final int[] CONVERSIONS;
	public static final int IDENTITY = 1;
	public static final int WIDENING = 2;
	public static final int NARROWING = 4;
	public static final int MAX_CONVERSIONS = 16*16; // well-known x well-known

	static {
		CONVERSIONS	 = initializeConversions();
	}

	public static final int[] initializeConversions(){
		// fromType   destType --> conversion
		//  0000   0000       				0000

		int[] table  = new int[MAX_CONVERSIONS];

		table[TypeIds.Boolean2Boolean] = IDENTITY;

		table[TypeIds.Byte2Byte] 		= IDENTITY;
		table[TypeIds.Byte2Short] 		= WIDENING;
		table[TypeIds.Byte2Char] 		= NARROWING;
		table[TypeIds.Byte2Int] 			= WIDENING;
		table[TypeIds.Byte2Long] 		= WIDENING;
		table[TypeIds.Byte2Float] 		= WIDENING;
		table[TypeIds.Byte2Double] 	= WIDENING;

		table[TypeIds.Short2Byte] 		= NARROWING;
		table[TypeIds.Short2Short] 		= IDENTITY;
		table[TypeIds.Short2Char] 		= NARROWING;
		table[TypeIds.Short2Int] 			= WIDENING;
		table[TypeIds.Short2Long] 		= WIDENING;
		table[TypeIds.Short2Float]	 	= WIDENING;
		table[TypeIds.Short2Double] 	= WIDENING;

		table[TypeIds.Char2Byte] 		= NARROWING;
		table[TypeIds.Char2Short] 		= NARROWING;
		table[TypeIds.Char2Char] 		= IDENTITY;
		table[TypeIds.Char2Int] 			= WIDENING;
		table[TypeIds.Char2Long] 		= WIDENING;
		table[TypeIds.Char2Float] 		= WIDENING;
		table[TypeIds.Char2Double] 	= WIDENING;

		table[TypeIds.Int2Byte] 			= NARROWING;
		table[TypeIds.Int2Short] 			= NARROWING;
		table[TypeIds.Int2Char] 			= NARROWING;
		table[TypeIds.Int2Int] 				= IDENTITY;
		table[TypeIds.Int2Long] 			= WIDENING;
		table[TypeIds.Int2Float] 			= WIDENING;
		table[TypeIds.Int2Double] 		= WIDENING;

		table[TypeIds.Long2Byte] 		= NARROWING;
		table[TypeIds.Long2Short] 		= NARROWING;
		table[TypeIds.Long2Char] 		= NARROWING;
		table[TypeIds.Long2Int] 			= NARROWING;
		table[TypeIds.Long2Long] 		= IDENTITY;
		table[TypeIds.Long2Float] 		= WIDENING;
		table[TypeIds.Long2Double] 	= WIDENING;

		table[TypeIds.Float2Byte] 		= NARROWING;
		table[TypeIds.Float2Short] 		= NARROWING;
		table[TypeIds.Float2Char] 		= NARROWING;
		table[TypeIds.Float2Int] 			= NARROWING;
		table[TypeIds.Float2Long] 		= NARROWING;
		table[TypeIds.Float2Float] 		= IDENTITY;
		table[TypeIds.Float2Double] 	= WIDENING;

		table[TypeIds.Double2Byte] 	= NARROWING;
		table[TypeIds.Double2Short] 	= NARROWING;
		table[TypeIds.Double2Char] 	= NARROWING;
		table[TypeIds.Double2Int] 		= NARROWING;
		table[TypeIds.Double2Long] 	= NARROWING;
		table[TypeIds.Double2Float] 	= NARROWING;
		table[TypeIds.Double2Double]= IDENTITY;

		return table;
	}
	/**
	 * Predicate telling whether "left" can store a "right" using some widening conversion
	 *(is left bigger than right)
	 * @param left - the target type to convert to
	 * @param right - the actual type
	 * @return true if legal widening conversion
	 */
	public static final boolean isWidening(int left, int right) {
		int right2left = right + (left<<4);
		return right2left >= 0
						&& right2left < MAX_CONVERSIONS
						&& (CONVERSIONS[right2left] & (IDENTITY|WIDENING)) != 0;
	}

	/**
	 * Predicate telling whether "left" can store a "right" using some narrowing conversion
	 *(is left smaller than right)
	 * @param left - the target type to convert to
	 * @param right - the actual type
	 * @return true if legal narrowing conversion
	 */
	public static final boolean isNarrowing(int left, int right) {
		int right2left = right + (left<<4);
		return right2left >= 0
						&& right2left < MAX_CONVERSIONS
						&& (CONVERSIONS[right2left] & (IDENTITY|NARROWING)) != 0;
	}

	/**
	 * Predicate telling whether its a widening followed by narrowing conversion -
	 * as per section 5.1.4 applicable for byte to char
	 * @param left - the target type to convert to
	 * @param right - the actual type
	 * @return true if  widening and narrowing conversion
	 */
	public static final boolean isWideningAndNarrowing(int left, int right) {
		return TypeIds.Byte2Char ==  right + (left<<4);
	}

	/**
	 * Predicate telling whether "left" can store a "right" using some widening conversion and
	 * whether it is an exact widening conversion
	 * https://cr.openjdk.org/~abimpoudis/instanceof/jep455-20240424/specs/instanceof-jls.html#jls-5.1.2
	 *(is left bigger than right)
	 * @param left - the target type to convert to
	 * @param right - the actual type
	 * @return true if legal widening conversion
	 */
	public static final boolean isExactWidening(int left, int right) {
		if (isWidening(left, right)) {
			int right2left = right + (left<<4);
			switch (right2left) {
				case TypeIds.Byte2Short:
				case TypeIds.Byte2Int:
				case TypeIds.Byte2Long:
				case TypeIds.Byte2Float:
				case TypeIds.Byte2Double:
				case TypeIds.Short2Int:
				case TypeIds.Short2Long:
				case TypeIds.Short2Float:
				case TypeIds.Short2Double:
				case TypeIds.Char2Int:
				case TypeIds.Char2Long:
				case TypeIds.Char2Float:
				case TypeIds.Char2Double:
				case TypeIds.Int2Long:
				case TypeIds.Int2Double:
				case TypeIds.Float2Double:
					return true;
				default : return false;
			}

		}
		return false;
	}

	/**
	 *
	 * @param left - the target type to convert to
	 * @param right - the actual type
	 * @return return the conversion index
	 */
	public static final int getRightToLeft(int left, int right) {
		return right + (left<<4);
	}
	public final boolean isIntegralType(int tid) {
		return tid == TypeIds.T_byte || tid == TypeIds.T_short || tid == TypeIds.T_int || tid == TypeIds.T_long;
	}

	public char[] simpleName;

	private final char[] constantPoolName;

	BaseTypeBinding(int id, char[] name, char[] constantPoolName) {
		this.tagBits |= TagBits.IsBaseType;
		this.id = id;
		this.simpleName = name;
		this.constantPoolName = constantPoolName;
	}

	/**
	 * int -> I
	 */
	@Override
	public char[] computeUniqueKey(boolean isLeaf) {
		return constantPoolName();
	}

	/* Answer the receiver's constant pool name.
	*/
	@Override
	public char[] constantPoolName() {

		return this.constantPoolName;
	}

	@Override
	public TypeBinding clone(TypeBinding enclosingType) {
		return new BaseTypeBinding(this.id, this.simpleName, this.constantPoolName);
	}

	@Override
	public PackageBinding getPackage() {

		return null;
	}

	/* Answer true if the receiver type can be assigned to the argument type (right)
	*/
	@Override
	public final boolean isCompatibleWith(TypeBinding right, Scope captureScope) {
		if (equalsEquals(this, right))
			return true;
		int right2left = this.id + (right.id<<4);
		if (right2left >= 0
				&& right2left < MAX_CONVERSIONS
				&& (CONVERSIONS[right2left] & (IDENTITY|WIDENING)) != 0)
			return true;
		return this == TypeBinding.NULL && !right.isBaseType();
	}

	@Override
	public void setTypeAnnotations(AnnotationBinding[] annotations, boolean evalNullAnnotations) {
		super.setTypeAnnotations(annotations, false); // never set nullTagBits on base types
	}

	@Override
	public TypeBinding unannotated() {
		if (!this.hasTypeAnnotations())
			return this;
		switch (this.id) {
			case TypeIds.T_boolean:
				return TypeBinding.BOOLEAN;
			case TypeIds.T_byte:
				return TypeBinding.BYTE;
			case TypeIds.T_char:
				return TypeBinding.CHAR;
			case TypeIds.T_double:
				return TypeBinding.DOUBLE;
			case TypeIds.T_float:
				return TypeBinding.FLOAT;
			case TypeIds.T_int:
				return TypeBinding.INT;
			case TypeIds.T_long:
				return TypeBinding.LONG;
			case TypeIds.T_short:
				return TypeBinding.SHORT;
			default:
				throw new IllegalStateException();
			}
	}
	/**
	 * T_null is acting as an unchecked exception
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#isUncheckedException(boolean)
	 */
	@Override
	public boolean isUncheckedException(boolean includeSupertype) {
		return this == TypeBinding.NULL;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#kind()
	 */
	@Override
	public int kind() {
		return Binding.BASE_TYPE;
	}
	@Override
	public char[] qualifiedSourceName() {
		return this.simpleName;
	}

	@Override
	public char[] readableName() {
		return this.simpleName;
	}

	@Override
	public char[] shortReadableName() {
		return this.simpleName;
	}

	@Override
	public char[] sourceName() {
		return this.simpleName;
	}

	@Override
	public String toString() {
		return this.hasTypeAnnotations() ? annotatedDebugName() : new String(readableName());
	}
}
