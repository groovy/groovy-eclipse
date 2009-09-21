 /*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.core.types;


import static org.codehaus.groovy.eclipse.core.util.ListUtil.newEmptyList;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.objectweb.asm.Opcodes;

public class TypeUtil {

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

    public static ClassType newObjectClassType() {
        String signature = Signature.createTypeSignature("java.lang.Object",
                true);
        int modifiers = Opcodes.ACC_PUBLIC;
        return new ClassType(signature, modifiers, "java.lang.Object");
    }
	
    public static ClassType newClassType(ClassNode node, int modifiers) {
        return new ClassType(Signature.createTypeSignature(node.getName(), true), node.getModifiers(), node.getName());
    }
	
	public static ClassType newClassType(IType type) throws JavaModelException {
		String signature = Signature.createTypeSignature(type.getFullyQualifiedName(), true);
		int modifiers = convertFromIMemberModifiers(type);
		return new ClassType(signature, modifiers, type.getFullyQualifiedName());
	}

	public static Method newMethod(ClassType declaringClass, MethodNode methodNode) {
		int modifiers = TypeUtil.convertFromASTModifiers(methodNode.getModifiers());
		String returnType = methodNode.getReturnType().getName();
		Parameter[] parameters = createParameterList(methodNode.getParameters());
		return new Method(modifiers, methodNode.getName(), parameters, returnType, declaringClass, !returnType
				.equals(OBJECT_TYPE));
	}
	
	public static Method newMethod(IMethod method, IType jdtDeclaringClass) throws JavaModelException {
	    int modifiers = TypeUtil.convertFromIMemberModifiers(method);
	    String[] typeSigs = getParameterTypes(method);
	    // convert to fully qualified name and must resolve if dealing with a source type
	    boolean doResolve = !jdtDeclaringClass.isReadOnly();
	    for (int i = 0; i < typeSigs.length; i++) {
	        // if an array, then use the type signature, not the name
	        if (typeSigs[i].charAt(0) != '[') {
                String resolvedType = Signature.toString(Signature.getTypeErasure(typeSigs[i]));
                if (doResolve) {
                    try {
                        resolvedType = join(jdtDeclaringClass.resolveType(resolvedType)[0], ".");
                    } catch (NullPointerException e) {
                        // ignore
                    }
                }
                typeSigs[i] = resolvedType;
	        }
        }
	    
        Parameter[] parameters = createParameterList(typeSigs, getParameterNames(method));
        String returnType = getReturnType(method);
        // if an array, then use the type signature, not the name
        ClassType declaringClass = TypeUtil.newClassType(jdtDeclaringClass);
        return new JavaMethod(modifiers, method.getElementName(), parameters, returnType, declaringClass);
	}

    /**
     * @param method
     * @return
     */
    private static String[] getParameterTypes(IMethod method) {
        String[] origParamTypes = method.getParameterTypes();
        String[] copy = new String[origParamTypes.length];
        System.arraycopy(origParamTypes, 0, copy, 0, origParamTypes.length);
        return copy;
    }

    /**
     * @param method
     * @return
     * @throws JavaModelException
     */
    private static String getReturnType(IMethod method)
            throws JavaModelException {
        try {
            String returnType = method.getReturnType();
            if (returnType.charAt(0) != '[') {
                returnType = Signature.toString(Signature.getTypeErasure(returnType));
                if (!method.isBinary()) {
                    try {
                        returnType = join(method.getDeclaringType().resolveType(returnType)[0], ".");
                    } catch (NullPointerException e) {
                        // ignore
                    }
                }
            }
            return returnType;
        } catch (StringIndexOutOfBoundsException e) {
            return "java.lang.Object";
        }
    }

    /**
     * @param method
     * @return
     * @throws JavaModelException
     */
    private static String[] getParameterNames(IMethod method)
            throws JavaModelException {
        try {
            return method.getParameterNames();
        } catch (StringIndexOutOfBoundsException e) {
            // sometimes this happens for class files without debug info
            int count = getParameterTypes(method).length;
            String[] names = new String[count];
            for (int i = 0; i < count; i++) {
                names[i] = "arg" + i;
            }
            return names;
        }
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
	
	public static Field newField(FieldNode fieldNode) {
		return newField(newClassType(fieldNode.getDeclaringClass()), fieldNode);
	}
	
	public static Field newField(ClassType declaringClass, FieldNode fieldNode) {
		int modifiers = TypeUtil.convertFromASTModifiers(fieldNode.getModifiers());
		String signature = fieldNode.getType() == null ? 
				"java.lang.Object": fieldNode.getType().getName();
		return new Field(signature, modifiers, fieldNode.getName(), declaringClass, !signature.equals(OBJECT_TYPE));
	}
	
	public static Field newField(IField field) throws IllegalArgumentException, JavaModelException {
		ClassType declaringClass = newClassType(field.getDeclaringType());
		String signature = field.getTypeSignature();
		int modifiers = TypeUtil.convertFromJavaCoreModifiers(field.getFlags());
		return new Field(signature, modifiers, field.getElementName(), declaringClass, !signature.equals(OBJECT_TYPE));
	}
	
	public static Field newField(IField field, IType declaringType) throws IllegalArgumentException, JavaModelException {
	    ClassType declaringClass = newClassType(declaringType);
	    String signature = field.getTypeSignature();
	    int modifiers = TypeUtil.convertFromJavaCoreModifiers(field.getFlags());
	    return new Field(signature, modifiers, field.getElementName(), declaringClass, !signature.equals(OBJECT_TYPE));
	}

	public static GroovyDeclaration newLocalVariable(Variable var) {
		return new LocalVariable(var.getType().getName(), var.getName());
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
	
    private static String join(final String[] strings, final String delim) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strings.length; i++) {
            sb.append(strings[i]);
            if (i < strings.length-1) {
                sb.append(delim);
            }
        }
        return sb.toString();
    }

}
