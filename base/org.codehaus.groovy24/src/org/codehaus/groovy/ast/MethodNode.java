/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.ast;

import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import groovyjarjarasm.asm.Opcodes;

import java.util.List;

/**
 * Represents a method declaration.
 */
public class MethodNode extends AnnotatedNode implements Opcodes {

    public static final String SCRIPT_BODY_METHOD_KEY = "org.codehaus.groovy.ast.MethodNode.isScriptBody";
    private final String name;
    private int modifiers;
    private boolean syntheticPublic;
    private ClassNode returnType;
    private Parameter[] parameters;
    private boolean hasDefaultValue = false;
    private Statement code;
    private boolean dynamicReturnType;
    private VariableScope variableScope;
    private final ClassNode[] exceptions;
    private final boolean staticConstructor;

    // type spec for generics
    private GenericsType[] genericsTypes = null;
    private boolean hasDefault;

    // cached data
    String typeDescriptor;

    public MethodNode(String name, int modifiers, ClassNode returnType, Parameter[] parameters, ClassNode[] exceptions, Statement code) {
        this.name = name;
        this.modifiers = modifiers;
        this.code = code;
        setReturnType(returnType);
        setParameters(parameters);
        this.hasDefault = false;
        this.exceptions = exceptions;
        this.staticConstructor = (name != null && name.equals("<clinit>"));
    }

    /**
     * The type descriptor for a method node is a string containing the name of the method, its return type,
     * and its parameter types in a canonical form. For simplicity, I'm using the format of a Java declaration
     * without parameter names.
     *
     * @return the type descriptor
     */
    public String getTypeDescriptor() {
        if (typeDescriptor == null) {
            String name = getName();
            Parameter[] parameters = getParameters();
            StringBuilder buf = new StringBuilder(64);
            buf.append(formatTypeName(getReturnType()));
            buf.append(' ');
            buf.append(name);
            buf.append('(');
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                Parameter param = parameters[i];
                buf.append(formatTypeName(param.getType()));
            }
            buf.append(')');
            typeDescriptor = buf.toString();
        }
        return typeDescriptor;
    }

    /**
     * Formats a type name in a readable version. For arrays, appends "[]" to the formatted
     * type name of the component. For unit class nodes, uses the class node name.
     * @param type the type to format
     * @return a human readable version of the type name (java.lang.String[] for example)
     */
    private static String formatTypeName(ClassNode type) {
        if (type.isArray()) {
            ClassNode it = type;
            int dim = 0;
            while (it.isArray()) {
                dim++;
                it = it.getComponentType();
            }
            StringBuilder sb = new StringBuilder(it.getName().length()+2*dim);
            sb.append(it.getName());
            for (int i=0;i<dim;i++) { sb.append("[]"); }
            return sb.toString();
        }
        return type.getName();
    }

    private void invalidateCachedData() {
        typeDescriptor = null;
    }

    public boolean isVoidMethod() {
        return ClassHelper.VOID_TYPE.equals(getReturnType());
    }

    public Statement getCode() {
        return code;
    }

    public void setCode(Statement code) {
        this.code = code;
    }

    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers(int modifiers) {
        invalidateCachedData();
        this.modifiers = modifiers;
    }

    public String getName() {
        return name;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public void setParameters(Parameter[] parameters) {
        invalidateCachedData();
        VariableScope scope = new VariableScope();
        // GRECLIPSE add
        this.hasDefaultValue = false;
        // GRECLIPSE end
        this.parameters = parameters;
        if (parameters != null && parameters.length > 0) {
            for (Parameter para : parameters) {
                if (para.hasInitialExpression()) {
                    this.hasDefaultValue = true;
                }
                para.setInStaticContext(isStatic());
                scope.putDeclaredVariable(para);
            }
        }
        setVariableScope(scope);
    }

    public ClassNode getReturnType() {
        return returnType;
    }

    public VariableScope getVariableScope() {
        return variableScope;
    }

    public void setVariableScope(VariableScope variableScope) {
        this.variableScope = variableScope;
        variableScope.setInStaticContext(isStatic());
    }

    public boolean isDynamicReturnType() {
        return dynamicReturnType;
    }

    public boolean isAbstract() {
        return (modifiers & ACC_ABSTRACT) != 0;
    }

    // GRECLIPSE add
    public boolean isDefault() {
        return (modifiers & (ACC_ABSTRACT | ACC_PUBLIC | ACC_STATIC)) == ACC_PUBLIC && getDeclaringClass() != null && getDeclaringClass().isInterface();
    }
    // GRECLIPSE end

    public boolean isStatic() {
        return (modifiers & ACC_STATIC) != 0;
    }

    public boolean isPublic() {
        return (modifiers & ACC_PUBLIC) != 0;
    }

    public boolean isPrivate() {
        return (modifiers & ACC_PRIVATE) != 0;
    }

    public boolean isFinal() {
        return (modifiers & ACC_FINAL) != 0;
    }

    public boolean isProtected() {
        return (modifiers & ACC_PROTECTED) != 0;
    }

    public boolean hasDefaultValue() {
        return this.hasDefaultValue;
    }

    /**
     * @return true if this method is the run method from a script
     */
    public boolean isScriptBody() {
        // GRECLIPSE edit
        //return getNodeMetaData(SCRIPT_BODY_METHOD_KEY) != null;
        return getDeclaringClass() != null &&
            getDeclaringClass().isScript() &&
            getName().equals("run") &&
            (parameters == null || parameters.length == 0) &&
            (returnType != null && returnType.getName().equals("java.lang.Object"));
        // GRECLIPSE: end
    }

    /**
     * Set the metadata flag for this method to indicate that it is a script body implementation.
     * @see ModuleNode createStatementsClass().
     */
    public void setIsScriptBody() {
        setNodeMetaData(SCRIPT_BODY_METHOD_KEY, Boolean.TRUE);
    }

    public String toString() {
        return super.toString() + "[" + getTypeDescriptor() + " from " + formatTypeName(getDeclaringClass()) + "]";
    }

    public void setReturnType(ClassNode returnType) {
        invalidateCachedData();
        dynamicReturnType |= ClassHelper.DYNAMIC_TYPE == returnType;
        this.returnType = returnType;
        if (returnType == null) this.returnType = ClassHelper.OBJECT_TYPE;
    }

    public ClassNode[] getExceptions() {
        return exceptions;
    }

    public Statement getFirstStatement() {
        if (code == null) return null;
        Statement first = code;
        while (first instanceof BlockStatement) {
            List<Statement> list = ((BlockStatement) first).getStatements();
            if (list.isEmpty()) {
                first = null;
            } else {
                first = list.get(0);
            }
        }
        return first;
    }

    public GenericsType[] getGenericsTypes() {
        return genericsTypes;
    }

    public void setGenericsTypes(GenericsType[] genericsTypes) {
        invalidateCachedData();
        this.genericsTypes = genericsTypes;
    }

    public void setAnnotationDefault(boolean b) {
        this.hasDefault = b;
    }

    public boolean hasAnnotationDefault() {
        return hasDefault;
    }

    public boolean isStaticConstructor() {
        return staticConstructor;
    }

    /**
     * Indicates that this method has been "promoted" to public by
     * Groovy when in fact there was no public modifier explicitly
     * in the source code. I.e. it remembers that it has applied
     * Groovy's "public methods by default" rule. This property is
     * typically only of interest to AST transform writers.
     *
     * @return true if this class is public but had no explicit public modifier
     */
    public boolean isSyntheticPublic() {
        return syntheticPublic;
    }

    public void setSyntheticPublic(boolean syntheticPublic) {
        this.syntheticPublic = syntheticPublic;
    }

    /**
     * Provides a nicely formatted string of the method definition. For simplicity, generic types on some of the elements
     * are not displayed. 
     * @return
     *      string form of node with some generic elements suppressed
     */
    @Override
    public String getText() {
        String retType = AstToTextHelper.getClassText(returnType);
        String exceptionTypes = AstToTextHelper.getThrowsClauseText(exceptions);
        String parms = AstToTextHelper.getParametersText(parameters);
        return AstToTextHelper.getModifiersText(modifiers) + " " + retType + " " + name + "(" + parms + ") " + exceptionTypes + " { ... }";
    }

    // GRECLIPSE add
    /**
     * When default parameters are involved, this field will be
     * the original method without any default parameters applied
     */
    private MethodNode original = this;

    /**
     * @return When default parameters are involved, this method returns the {@link MethodNode} 
     * where no default parameters have been applied.  Otherwise returns <code>this</code>.  Never
     * returns null.
     */
    public MethodNode getOriginal() {
        return original;
    }

    public void setOriginal(MethodNode original) {
        this.original = original;
    }
    // GRECLIPSE end
}
