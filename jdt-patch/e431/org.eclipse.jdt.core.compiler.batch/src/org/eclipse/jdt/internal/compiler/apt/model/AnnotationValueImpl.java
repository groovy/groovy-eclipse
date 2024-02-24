/*******************************************************************************
 * Copyright (c) 2005, 2023 IBM Corporation and others.
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
 *     het@google.com - Bug 441790
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.DoubleConstant;
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.compiler.impl.LongConstant;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.problem.ShouldNotImplement;
import org.eclipse.jdt.internal.compiler.util.Util;

public class AnnotationValueImpl implements AnnotationValue, TypeIds {

	/*
	 * Additions to T_* constants in TypeIds.
	 */
	private static final int T_AnnotationMirror = -1;
	private static final int T_EnumConstant = -2;
	private static final int T_ClassObject = -3;
	private static final int T_ArrayType = -4;

	private final BaseProcessingEnvImpl _env;

	/**
	 * The annotation value, as it would be returned by
	 * {@link #getValue()}.  For instance, an Integer (for an int
	 * constant), a VariableElement (for an enum constant), or
	 * a List<AnnotationValueImpl> containing multiple such (for an array type).
	 */
	private final Object _value;

	/**
	 * The type stored in _value, represented as a T_* value from {@link TypeIds}
	 * or one of the additional T_* values defined in this class.
	 */
	private final int _kind;

	/**
	 * @param value
	 *            The JDT representation of a compile-time constant. See
	 *            {@link org.eclipse.jdt.internal.compiler.lookup.ElementValuePair#getValue()} for possible object types:
	 *            <ul>
	 *            <li>{@link org.eclipse.jdt.internal.compiler.impl.Constant} for member
	 *            of primitive type or String</li>
	 *            <li>{@link TypeBinding} for a member value of type
	 *            {@link java.lang.Class}</li>
	 *            <li>{@link FieldBinding} for an enum constant</li>
	 *            <li>{@link AnnotationBinding} for an annotation instance</li>
	 *            <li><code>Object[]</code> for a member value of array type, where the
	 *            array entries are one of the above</li>
	 *            </ul>
	 * @param type
	 *            The JDT representation of the type of the constant, as determined
	 *            by the return type of the element.  This is needed because the type
	 *            of the value may have been widened (e.g., byte to int) by the compiler
	 *            and we need to call the proper visitor.  This is used only for base types.
	 *            If it is null or not a BaseTypeBinding, it is ignored and the type is
	 *            determined from the type of the value.
	 */
	public AnnotationValueImpl(BaseProcessingEnvImpl env, Object value, TypeBinding type) {
		this._env = env;
		int kind[] = new int[1];
		if (type == null) {
			this._value = convertToMirrorType(value, type, kind);
			this._kind = kind[0];
		} else if (type.isArrayType()) {
			List<AnnotationValue> convertedValues = null;
			TypeBinding valueType = ((ArrayBinding)type).elementsType();
			if (value instanceof Object[]) {
				Object[] values = (Object[])value;
				convertedValues = new ArrayList<>(values.length);
				for (Object oneValue : values) {
					convertedValues.add(new AnnotationValueImpl(this._env, oneValue, valueType));
				}
			} else {
				convertedValues = new ArrayList<>(1);
				convertedValues.add(new AnnotationValueImpl(this._env, value, valueType));
			}
			this._value = Collections.unmodifiableList(convertedValues);
			this._kind = T_ArrayType;
		} else {
			this._value = convertToMirrorType(value, type, kind);
			this._kind = kind[0];
		}
	}

	/**
	 * Convert the JDT representation of a single constant into its javax.lang.model
	 * representation.  For instance, convert a StringConstant into a String, or
	 * a FieldBinding into a VariableElement.  This does not handle the case where
	 * value is an Object[].
	 * @param value the JDT object
	 * @param type the return type of the annotation member.  If null or not a
	 * BaseTypeBinding, this is ignored and the value is inspected to determine type.
	 * @param kind an int array whose first element will be set to the type of the
	 * converted object, represented with T_* values from TypeIds or from this class.
	 * @return converted mirror type
	 */
	private Object convertToMirrorType(Object value, TypeBinding type, int kind[]) {
		if (type == null) {
			kind[0] = TypeIds.T_JavaLangString;
			return "<error>"; //$NON-NLS-1$
		} else if (type instanceof BaseTypeBinding || type.id == TypeIds.T_JavaLangString) {
			if (value == null) {
				if (type instanceof BaseTypeBinding
						|| type.id == TypeIds.T_JavaLangString) {
					// return a string with error in it to reflect a value that could not be resolved
					kind[0] = TypeIds.T_JavaLangString;
					return "<error>"; //$NON-NLS-1$
				} else if (type.isAnnotationType()) {
					kind[0] = T_AnnotationMirror;
					return this._env.getFactory().newAnnotationMirror(null);
				}
			} else if (value instanceof Constant) {
				if (type instanceof BaseTypeBinding) {
					kind[0] = ((BaseTypeBinding)type).id;
				}
				else if (type.id == TypeIds.T_JavaLangString) {
					kind[0] = ((Constant)value).typeID();
				} else {
					// error case
					kind[0] = TypeIds.T_JavaLangString;
					return "<error>"; //$NON-NLS-1$
				}
				switch (kind[0]) {
				case T_boolean:
					return ((Constant)value).booleanValue();
				case T_byte:
					return ((Constant)value).byteValue();
				case T_char:
					return ((Constant)value).charValue();
				case T_double:
					return ((Constant)value).doubleValue();
				case T_float:
					return ((Constant)value).floatValue();
				case T_int:
					try {
						if (value instanceof LongConstant
								|| value instanceof DoubleConstant
								|| value instanceof FloatConstant) {
							// error case
							kind[0] = TypeIds.T_JavaLangString;
							return "<error>"; //$NON-NLS-1$
						}
						return ((Constant)value).intValue();
					} catch (ShouldNotImplement e) {
						kind[0] = TypeIds.T_JavaLangString;
						return "<error>"; //$NON-NLS-1$
					}
				case T_JavaLangString:
					return ((Constant)value).stringValue();
				case T_long:
					return ((Constant)value).longValue();
				case T_short:
					return ((Constant)value).shortValue();
				}
			}
		} else if (type.isEnum()) {
			if (value instanceof FieldBinding) {
				kind[0] = T_EnumConstant;
				return this._env.getFactory().newElement((FieldBinding) value);
			} else {
				kind[0] = TypeIds.T_JavaLangString;
				return "<error>"; //$NON-NLS-1$
			}
		} else if (type.isAnnotationType()) {
			if (value instanceof AnnotationBinding) {
				kind[0] = T_AnnotationMirror;
				return this._env.getFactory().newAnnotationMirror((AnnotationBinding) value);
			}
		} else if (value instanceof TypeBinding) {
			kind[0] = T_ClassObject;
			return this._env.getFactory().newTypeMirror((TypeBinding) value);
		}
		// error case
		kind[0] = TypeIds.T_JavaLangString;
		return "<error>"; //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked") // Need to cast Object _value to a List<AnnotationValue>
	@Override
	public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
		switch (this._kind) {
		case TypeIds.T_boolean:
			return v.visitBoolean((Boolean)this._value, p);
		case TypeIds.T_byte:
			return v.visitByte((Byte)this._value, p);
		case TypeIds.T_char:
			return v.visitChar((Character)this._value, p);
		case TypeIds.T_double:
			return v.visitDouble((Double)this._value, p);
		case TypeIds.T_float:
			return v.visitFloat((Float)this._value, p);
		case TypeIds.T_int:
			return v.visitInt((Integer)this._value, p);
		case TypeIds.T_JavaLangString:
			return v.visitString((String)this._value, p);
		case TypeIds.T_long:
			return v.visitLong((Long)this._value, p);
		case TypeIds.T_short:
			return v.visitShort((Short)this._value, p);
		case T_EnumConstant:
			return v.visitEnumConstant((VariableElement)this._value, p);
		case T_ClassObject:
			return v.visitType((TypeMirror)this._value, p);
		case T_AnnotationMirror:
			return v.visitAnnotation((AnnotationMirror)this._value, p);
		case T_ArrayType:
			return v.visitArray((List<AnnotationValue>)this._value, p);
		default:
			return null;
		}
	}

	@Override
	public Object getValue() {
		return this._value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AnnotationValueImpl) {
			return this._value.equals(((AnnotationValueImpl) obj)._value);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this._value.hashCode() + this._kind;
	}

	@Override
	public String toString() {
		if (this._value == null) {
			return "null"; //$NON-NLS-1$
		} else if (this._value instanceof String) {
			String value = (String) this._value;
			StringBuilder sb = new StringBuilder();
			sb.append('"');
			for (int i = 0; i < value.length(); i++) {
				Util.appendEscapedChar(sb, value.charAt(i), true);
			}
			sb.append('"');
			return sb.toString();
		} else if (this._value instanceof Character) {
			StringBuilder sb = new StringBuilder();
			sb.append('\'');
			Util.appendEscapedChar(sb, ((Character) this._value).charValue(), false);
			sb.append('\'');
			return sb.toString();
		} else if (this._value instanceof VariableElement) {
			VariableElement enumDecl = (VariableElement) this._value;
			return enumDecl.asType().toString() + "." + enumDecl.getSimpleName(); //$NON-NLS-1$
		} else if (this._value instanceof Collection) {
			// It must be Collection<AnnotationValue>
			@SuppressWarnings("unchecked")
			Collection<AnnotationValue> values = (Collection<AnnotationValue>) this._value;
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			boolean first = true;
			for (AnnotationValue annoValue : values) {
				if (!first) {
					sb.append(", "); //$NON-NLS-1$
				}
				first = false;
				sb.append(annoValue.toString());
			}
			sb.append('}');
			return sb.toString();
		} else if (this._value instanceof TypeMirror) {
			return this._value.toString() + ".class"; //$NON-NLS-1$
		} else {
			return this._value.toString();
		}
	}
}
