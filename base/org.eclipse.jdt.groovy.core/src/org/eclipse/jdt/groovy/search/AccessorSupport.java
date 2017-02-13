/*
 * Copyright 2009-2017 the original author or authors.
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
package org.eclipse.jdt.groovy.search;

import java.util.LinkedHashSet;
import java.util.List;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;

/**
 * Kind of accessor a method name may be and then does further processing on a method node if the name matches.
 *
 * @author andrew
 * @created Jan 23, 2012
 */
public enum AccessorSupport {
    GETTER("get"), SETTER("set"), ISSER("is"), NONE("");

    private final String prefix;

    private AccessorSupport(String prefix) {
        this.prefix = prefix;
    }

    public boolean isAccessor() {
        return (this != NONE);
    }

    public boolean isAccessorKind(MethodNode node, boolean isCategory) {
        int args = isCategory ? 1 : 0;
        ClassNode returnType = node.getReturnType();
        switch (this) {
        case GETTER:
            return (node.getParameters() == null || node.getParameters().length == args) &&
                !returnType.equals(VariableScope.VOID_CLASS_NODE);
        case SETTER:
            return node.getParameters() != null && node.getParameters().length == args + 1 &&
            (returnType.equals(VariableScope.VOID_CLASS_NODE) || returnType.equals(VariableScope.OBJECT_CLASS_NODE));
        case ISSER:
            return !isCategory && (node.getParameters() == null || node.getParameters().length == args) &&
                (returnType.equals(VariableScope.OBJECT_CLASS_NODE) || returnType.equals(VariableScope.BOOLEAN_CLASS_NODE) || returnType.equals(ClassHelper.boolean_TYPE));
        default:
            return false;
        }
    }

    public String createAccessorName(String name) {
        if (!name.startsWith(GETTER.prefix) && !name.startsWith(SETTER.prefix) && name.length() > 0) {
            return this.prefix + Character.toUpperCase(name.charAt(0)) + (name.length() > 1 ? name.substring(1) : "");
        }
        return null;
    }

    public static AccessorSupport findAccessorKind(MethodNode node, boolean isCategory) {
        AccessorSupport accessor = create(node.getName(), isCategory);
        return accessor.isAccessorKind(node, isCategory) ? accessor : NONE;
    }

    public static MethodNode findAccessorMethodForPropertyName(String name, ClassNode declaringType, boolean isCategory) {
        return findAccessorMethodForPropertyName(name, declaringType, isCategory, GETTER, ISSER, SETTER);
    }

    public static MethodNode findAccessorMethodForPropertyName(String name, ClassNode declaringType, boolean isCategory, AccessorSupport... kinds) {
        if (name != null && name.length() > 0 && kinds != null && kinds.length > 0) {
            String suffix = Character.toUpperCase(name.charAt(0)) + name.substring(1);

            for (AccessorSupport kind : kinds) {
                if (kind == NONE) continue;
                String methodName = kind.prefix + suffix;
                MethodNode meth = findAccessorMethodForMethodName(methodName, declaringType, isCategory, kind);
                if (meth != null) {
                    return meth;
                }
                // interfaces do not return super interface methods from getMethods(String)
                if (declaringType.isInterface()) {
                    LinkedHashSet<ClassNode> faces = new LinkedHashSet<ClassNode>();
                    VariableScope.findAllInterfaces(declaringType, faces, false);
                    for (ClassNode face : faces) {
                        meth = findAccessorMethodForMethodName(methodName, face, isCategory, kind);
                        if (meth != null) {
                            return meth;
                        }
                    }
                    // one implicit accessor exists in Object
                    if (!isCategory && kind == GETTER && methodName.equals("getClass")) {
                        return ClassHelper.OBJECT_TYPE.getMethod("getClass", new Parameter[0]);
                    }
                }
            }
        }
        return null;
    }

    private static MethodNode findAccessorMethodForMethodName(String name, ClassNode declaringType, boolean isCategory, AccessorSupport kind) {
        List<MethodNode> methods = declaringType.getMethods(name);
        for (MethodNode meth : methods) {
            if (kind == findAccessorKind(meth, isCategory)) {
                return meth;
            }
        }
        return null;
    }

    /**
     * @return true if the methodNode looks like a getter method for a property: method starting get<Something> with a non void return type and taking no parameters
     */
    public static boolean isGetter(MethodNode node) {
        return node.getReturnType() != VariableScope.VOID_CLASS_NODE && node.getParameters().length == 0 &&
            ((node.getName().startsWith("get") && node.getName().length() > 3) ||
                (node.getName().startsWith("is") && node.getName().length() > 2));
    }

    public static AccessorSupport create(String methodName, boolean isCategory) {
        AccessorSupport accessor = AccessorSupport.NONE;
        // is is allowed only for non-category methods
        if (!isCategory && methodName.length() > 2 && methodName.startsWith("is") &&
                Character.isUpperCase(methodName.charAt(2))) {
            accessor = AccessorSupport.ISSER;
        }

        if (!accessor.isAccessor()) {
            if (methodName.length() > 3 && (methodName.startsWith("get") || methodName.startsWith("set")) &&
                    Character.isUpperCase(methodName.charAt(3))) {
                accessor = methodName.charAt(0) == 'g' ? AccessorSupport.GETTER : AccessorSupport.SETTER;
            }
        }
        return accessor;
    }
}
