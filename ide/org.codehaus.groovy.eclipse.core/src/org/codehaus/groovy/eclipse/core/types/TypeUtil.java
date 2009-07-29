/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.types;

import static org.codehaus.groovy.eclipse.core.util.ListUtil.newEmptyList;
import static org.codehaus.groovy.eclipse.core.util.MapUtil.newMap;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.objectweb.asm.Opcodes;

public class TypeUtil {
	private static final Map< String, String > mapPrimitiveToClass = newMap();
	
	static {
		mapPrimitiveToClass.put("byte", Byte.class.getName());
		mapPrimitiveToClass.put("char", Character.class.getName());
		mapPrimitiveToClass.put("short", Short.class.getName());
		mapPrimitiveToClass.put("int", Integer.class.getName());
		mapPrimitiveToClass.put("long", Long.class.getName());
		mapPrimitiveToClass.put("float", Float.class.getName());
		mapPrimitiveToClass.put("double", Double.class.getName());
		mapPrimitiveToClass.put("boolean", Boolean.class.getName());
	}

	public static final Field[] NO_FIELDS = new Field[0];

	public static final Property[] NO_PROPERTIES = new Property[0];

	public static final Method[] NO_METHODS = new Method[0];

	public static final Parameter[] NO_PARAMETERS = new Parameter[0];
	
	public static final String OBJECT_TYPE = "java.lang.Object";
	
	public static ClassType newClassType(ClassNode classNode) {
		String signature = Signature.createTypeSignature(classNode.getName(), true);
		int modifiers = convertFromASTModifiers(classNode.getModifiers());
		return new ClassType(signature, modifiers, classNode.getName());
	}
	
	public static ClassType newClassType(String signature, int modifiers) {
	    String qualifier = Signature.getSignatureQualifier(signature);
	    String name = Signature.getSignatureSimpleName(signature);
	    String qualName = ( qualifier.length() > 0 ? qualifier + "." : "") 
	        + name; 
		return new ClassType(signature, modifiers, qualName);
	}
	
	public static ClassType newClassType(Class cls) {
		String signature = cls.getName();
		int modifiers = convertFromJavaModifiers(cls.getModifiers());
		return new ClassType(signature, modifiers, cls.getCanonicalName());
	}

	public static ClassType newClassType(IType type) throws JavaModelException {
		String signature = Signature.createTypeSignature(type.getTypeQualifiedName(), true);
		int modifiers = convertFromIMemberModifiers(type);
		return new ClassType(signature, modifiers, type.getTypeQualifiedName());
	}

	public static Method newMethod(ClassType declaringClass, MethodNode methodNode) {
		int modifiers = TypeUtil.convertFromASTModifiers(methodNode.getModifiers());
		String returnType = methodNode.getReturnType().getName();
		Parameter[] parameters = createParameterList(methodNode.getParameters());
		return new Method(modifiers, methodNode.getName(), parameters, returnType, declaringClass, !returnType
				.equals(OBJECT_TYPE));
	}
	
	public static Method newMethod(java.lang.reflect.Method method) {
		int modifiers = TypeUtil.convertFromJavaModifiers(method.getModifiers());
		Parameter[] parameters = createParameterList(method.getParameterTypes());
		String returnType = method.getReturnType().getName();
		if (returnType.charAt(0) != '[') {
			returnType = method.getReturnType().getName();
		}
		ClassType declaringClass = TypeUtil.newClassType(method.getDeclaringClass());
		return new JavaMethod(modifiers, method.getName(), parameters, returnType, declaringClass);
	}

	public static Property newProperty(ClassType declaringClass, PropertyNode propertyNode) {
		String signature = propertyNode.getType().getName();
		int modifiers = TypeUtil.convertFromASTModifiers(propertyNode.getModifiers());
		// boolean readable = propertyNode.getGetterBlock() != null;
		// boolean writable = propertyNode.getSetterBlock() != null;
		// TODO: emp - how to determine read/write.
		return new Property(signature, modifiers, propertyNode.getName(), true, true, declaringClass, !propertyNode
				.getType().getText().equals(OBJECT_TYPE));
	}
	
	public static Type newField(FieldNode fieldNode) {
		return newField(newClassType(fieldNode.getDeclaringClass()), fieldNode);
	}
	

	public static Field newField(java.lang.reflect.Field field) {
		int modifiers = TypeUtil.convertFromJavaModifiers(field.getModifiers());
		ClassType declaringClass = TypeUtil.newClassType(field.getDeclaringClass());
		String signature = field.getType().getName();
		return new JavaField(signature, modifiers, field.getName(), declaringClass);
	}

	public static Field newField(ClassType declaringClass, FieldNode fieldNode) {
		int modifiers = TypeUtil.convertFromASTModifiers(fieldNode.getModifiers());
		String signature = fieldNode.getType() == null ? 
				"java.lang.Object": fieldNode.getType().getName();
		return new Field(signature, modifiers, fieldNode.getName(), declaringClass, !signature.equals(OBJECT_TYPE));
	}
	
	public static Type newField(IField field) throws IllegalArgumentException, JavaModelException {
		ClassType declaringClass = newClassType(field.getDeclaringType());
		String signature = field.getTypeSignature();
		int modifiers = TypeUtil.convertFromJavaCoreModifiers(field.getFlags());
		return new Field(signature, modifiers, field.getElementName(), declaringClass, !signature.equals(OBJECT_TYPE));
	}

	public static Type newLocalVariable(Variable var) {
		return new LocalVariable(var.getType().getName(), var.getName());
	}
	
	/**
	 * Convert a primitive to class wrapper.
	 * @param name
	 * @return
	 */
	public static String convertPrimitiveToWrapper(String name) {
		String newType = mapPrimitiveToClass.get(name);
		return newType != null ? newType : name;
	}
	
	/**
	 * Convert flags from an ast representation to a GroovyEclipse one.
	 * 
	 * @param astFlags
	 * @return
	 */
	public static int convertFromASTModifiers(int modifiers) {
		int flags = 0;
		flags |= ((modifiers & Opcodes.ACC_ABSTRACT) != 0) ? Modifiers.ACC_ABSTRACT : 0;
		flags |= ((modifiers & Opcodes.ACC_PUBLIC) != 0) ? Modifiers.ACC_PUBLIC : 0;
		flags |= ((modifiers & Opcodes.ACC_PRIVATE) != 0) ? Modifiers.ACC_PRIVATE : 0;
		flags |= ((modifiers & Opcodes.ACC_PROTECTED) != 0) ? Modifiers.ACC_PROTECTED : 0;
		flags |= ((modifiers & Opcodes.ACC_STATIC) != 0) ? Modifiers.ACC_STATIC : 0;
		flags |= ((modifiers & Opcodes.ACC_FINAL) != 0) ? Modifiers.ACC_FINAL : 0;
		return flags;
	}

	public static int convertFromJavaModifiers(int modifiers) {
		int flags = 0;
		flags |= ((modifiers & Modifier.ABSTRACT) != 0) ? Modifiers.ACC_ABSTRACT : 0;
		flags |= ((modifiers & Modifier.PUBLIC) != 0) ? Modifiers.ACC_PUBLIC : 0;
		flags |= ((modifiers & Modifier.PRIVATE) != 0) ? Modifiers.ACC_PRIVATE : 0;
		flags |= ((modifiers & Modifier.PROTECTED) != 0) ? Modifiers.ACC_PROTECTED : 0;
		flags |= ((modifiers & Modifier.STATIC) != 0) ? Modifiers.ACC_STATIC : 0;
		flags |= ((modifiers & Modifier.FINAL) != 0) ? Modifiers.ACC_FINAL : 0;
		return flags;
	}

	
	public static int convertFromIMemberModifiers(IMember member) throws JavaModelException {
		return convertFromJavaCoreModifiers(member.getFlags());
	}
	
	public static int convertFromJavaCoreModifiers(int modifiers) {
		int flags = 0;
		flags |= ((modifiers & Flags.AccAbstract) != 0) ? Modifiers.ACC_ABSTRACT : 0;
		flags |= ((modifiers & Flags.AccPublic) != 0) ? Modifiers.ACC_PUBLIC : 0;
		flags |= ((modifiers & Flags.AccPrivate) != 0) ? Modifiers.ACC_PRIVATE : 0;
		flags |= ((modifiers & Flags.AccProtected) != 0) ? Modifiers.ACC_PROTECTED : 0;
		flags |= ((modifiers & Flags.AccStatic) != 0) ? Modifiers.ACC_STATIC : 0;
		flags |= ((modifiers & Flags.AccFinal) != 0) ? Modifiers.ACC_FINAL : 0;
		return flags;
	}
	
	public static Parameter[] createParameterList(String[] types, String[] names) {
		if (types.length == 0) {
			return NO_PARAMETERS;
		}
		
		Parameter[] result = new Parameter[types.length];
		for (int i = 0; i < types.length; ++i) {
			result[i] = new Parameter(types[i], names[i]);
		}

		return result;
	}
	
	public static Parameter[] createParameterList(Class[] types) {
		if (types.length == 0) {
			return NO_PARAMETERS;
		}

		Parameter[] result = new Parameter[types.length];
		for (int i = 0; i < types.length; ++i) {
			result[i] = new Parameter(types[i].getName(), "arg" + i);
		}

		return result;
	}
	
	public static Parameter[] createParameterList(org.codehaus.groovy.ast.Parameter[] parameters) {
		if (parameters.length == 0) {
			return TypeUtil.NO_PARAMETERS;
		}

		Parameter[] result = new Parameter[parameters.length];
		for (int i = 0; i < parameters.length; ++i) {
			String typeName = parameters[i].getType().getName();
			String name = parameters[i].getName();
			result[i] = new Parameter(
			        typeName.charAt(0) == '[' ? typeName : Signature.createTypeSignature(typeName, true), 
			        name);
		}

		return result;
	}
	
	/**
	 * Match ignoring case and checking camel case.
	 * @param prefix
	 * @param target
	 * @return
	 */
	public static boolean looselyMatches(String prefix, String target) {
		// Zero length string matches everything.
		if (prefix.length() == 0) {
			return true;
		}
		
		// Exclude a bunch right away
		if (prefix.charAt(0) != target.charAt(0)) {
			return false;
		}
		
		if (target.startsWith(prefix)) {
			return true;
		}
		
		String lowerCase = target.toLowerCase();
		if (lowerCase.startsWith(prefix)) {
			return true;
		}
		
		// Test for camel characters in the prefix.
		if (prefix.equals(prefix.toLowerCase())) {
			return false;
		}
		
		String[] prefixParts = toCamelCaseParts(prefix);
		String[] targetParts = toCamelCaseParts(target);
		
		if (prefixParts.length > targetParts.length) {
			return false;
		}
		
		for (int i = 0; i < prefixParts.length; ++i) {
			if (!targetParts[i].startsWith(prefixParts[i])) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Convert an input string into parts delimited by upper case characters. Used for camel case matches.
	 * e.g. GroClaL = ['Gro','Cla','L'] to match say 'GroovyClassLoader'.
	 * e.g. mA = ['m','A']
	 * @param str
	 * @return
	 */
	private static String[] toCamelCaseParts(String str) {
		List< String > parts = newEmptyList();
		for (int i = str.length() - 1; i >= 0; --i) {
			if (Character.isUpperCase(str.charAt(i))) {
				parts.add(str.substring(i));
				str = str.substring(0, i);
			}
		}
		if (str.length() != 0) {
			parts.add(str);
		}
		Collections.reverse(parts);
		return parts.toArray(new String[parts.size()]);
	}
}
