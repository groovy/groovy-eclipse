/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.codeassist.completions;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.jdt.groovy.model.JavaCoreUtil;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;
import org.eclipse.jdt.internal.codeassist.InternalExtendedCompletionContext;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.SourceField;

public class GroovyExtendedCompletionContext extends InternalExtendedCompletionContext {

    private final ContentAssistContext context;

    private final VariableScope currentScope;

    // computed after initialization
    private IJavaElement enclosingElement;

    // computed after initialization
    private final Map<String, IJavaElement[]> visibleElements;

    public GroovyExtendedCompletionContext(ContentAssistContext context, VariableScope currentScope) {
        super(null, null, null, null, null, null, null, null, null);

        this.context = context;
        this.currentScope = currentScope;
        this.visibleElements = new HashMap<>();
    }

    @Override
    public boolean canUseDiamond(String[] parameterTypes, char[] fullyQualifiedTypeName) {
        return true;
    }

    @Override
    public IJavaElement getEnclosingElement() {
        if (enclosingElement == null) {
            try {
                enclosingElement = context.unit.getElementAt(context.completionLocation);
            } catch (JavaModelException e) {
                GroovyContentAssist.logError("Exception computing content assist proposals", e);
            }
            if (enclosingElement == null) {
                enclosingElement = context.unit;
            }
        }
        return enclosingElement;
    }

    @Override
    public IJavaElement[] getVisibleElements(String typeSignature) {
        return visibleElements.computeIfAbsent(Signature.getTypeErasure(typeSignature), this::computeVisibleElements);
    }

    private IJavaElement[] computeVisibleElements(String typeSignature) {
        Map<String, IJavaElement> visibleElements = new LinkedHashMap<>();
        ClassNode targetType = toClassNode(typeSignature);

        // add qualifying local variables of the enclosing scope
        if (currentScope != null) {
            for (VariableInfo varInfo : currentScope) {
                // GRECLIPSE-1268 currently, no good way to get to the actual declaration of the variable.
                // This can cause ordering problems for the guessed parameters.
                // don't put elements in a second time since we are moving from inner scope to outer scope
                String varName = varInfo.name;
                // ignore synthetic getters and setters that are put in the scope; looking at prefix is an approximation
                if (!varName.startsWith("get") &&
                    !varName.startsWith("set") &&
                    !varName.equals("this" ) &&
                    !varName.equals("super") &&
                    !varName.startsWith("<") &&
                    !visibleElements.containsKey(varName)) {

                    ClassNode type = varInfo.type;
                    if (type == null) type = VariableScope.NULL_TYPE;
                    if (GroovyUtils.isAssignable(type, targetType)) {
                        // NOTE: parent, source location, typeSignature, etc. are not important here
                        int start = 0, until = varName.length() - 1;
                        visibleElements.putIfAbsent(varName, new LocalVariable(
                            (JavaElement) getEnclosingElement(), varName, start, until, start, until, typeSignature, null, 0, false));
                    }
                }
            }
        }

        // add qualifying fields of the enclosing type(s)
        IType enclosingType = (IType) getEnclosingElement().getAncestor(IJavaElement.TYPE);
        if (enclosingType != null) {
            try {
                addFields(targetType, visibleElements, enclosingType);
                ITypeHierarchy typeHierarchy = enclosingType.newSupertypeHierarchy(null);
                for (IType superType : typeHierarchy.getAllSupertypes(enclosingType)) {
                    addFields(targetType, visibleElements, superType);
                }
            } catch (JavaModelException e) {
                GroovyContentAssist.logError(e);
            }
        }

        // add enum constants if target type is an enum
        if (targetType.isEnum()) {
            try {
                IType enumType = JavaCoreUtil.findType(targetType.getName(), enclosingElement);
                for (IField enumField : enumType.getFields()) {
                    if (enumField.isEnumConstant()) {
                        visibleElements.putIfAbsent(enumField.getElementName(), enumField);
                    }
                }
            } catch (JavaModelException e) {
                GroovyContentAssist.logError(e);
            }
        }
        return visibleElements.values().toArray(new IJavaElement[0]);
    }

    protected void addFields(ClassNode targetType, Map<String, IJavaElement> visibleElements, IType type)
            throws JavaModelException {
        for (IField field : type.getFields()) {
            ClassNode fieldTypeClassNode = toClassNode(field.getTypeSignature());
            if (GroovyUtils.isAssignable(fieldTypeClassNode, targetType)) {
                visibleElements.putIfAbsent(field.getElementName(), field);
            }
        }
        for (IMethod method : type.getMethods()) {
            ClassNode methodReturnTypeClassNode = toClassNode(method.getReturnType());
            if (!VariableScope.VOID_CLASS_NODE.equals(methodReturnTypeClassNode) &&
                    GroovyUtils.isAssignable(methodReturnTypeClassNode, targetType)) {
                if ((method.getParameterTypes() == null || method.getParameterTypes().length == 0) &&
                        (method.getElementName().startsWith("get") || method.getElementName().startsWith("is"))) {
                    visibleElements.putIfAbsent(method.getElementName(), method);
                    IField field = new PropertyVariant(method);
                    visibleElements.putIfAbsent(field.getElementName(), field);
                }
            }
        }
    }

    protected ClassNode toClassNode(char[] typeSignature) {
        int dims = Signature.getArrayCount(typeSignature);
        char[] base = Signature.getElementType(typeSignature);
        char[] name = CharOperation.concat(Signature.getSignatureQualifier(base), Signature.getSignatureSimpleName(base), '.');

        ClassNode node;
        if (typeSignature.length == (1 + dims)) { // primitive type
            node = ClassHelper.make(String.valueOf(name));
        } else {
            node = resolve(String.valueOf(name));
        }
        while (dims-- > 0) {
            node = node.makeArray();
        }
        return node;
    }

    protected ClassNode toClassNode(String typeSignature) {
        int dims = Signature.getArrayCount(typeSignature);
        String base = Signature.getElementType(typeSignature);
        String name = Signature.getSignatureSimpleName(base);
        String qual = Signature.getSignatureQualifier(base);
        if (!qual.isEmpty()) {
            name = qual + '.' + name;
        }

        ClassNode node;
        if (typeSignature.length() == (1 + dims)) { // primitive type
            node = ClassHelper.make(name);
        } else {
            node = resolve(name);
        }
        while (dims-- > 0) {
            node = node.makeArray();
        }
        return node;
    }

    protected ClassNode resolve(String fullyQualifiedTypeName) {
        ModuleNodeInfo info = context.unit.getModuleInfo(false);
        return info.resolver.resolve(fullyQualifiedTypeName);
    }

    private static String toFieldName(IMethod method) {
        return ProposalUtils.createMockFieldName(method.getElementName());
    }

    private static class PropertyVariant extends SourceField implements IField {
        private final IMethod baseMethod;

        PropertyVariant(IMethod method) {
            super((JavaElement) method.getParent(), toFieldName(method));
            baseMethod = method;
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public String getTypeSignature() throws JavaModelException {
            return baseMethod.getReturnType();
        }

        @Override
        public int getFlags() throws JavaModelException {
            return baseMethod.getFlags();
        }
    }
}
