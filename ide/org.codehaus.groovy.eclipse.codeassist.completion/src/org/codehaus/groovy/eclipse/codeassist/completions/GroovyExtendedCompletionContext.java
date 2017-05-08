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
package org.codehaus.groovy.eclipse.codeassist.completions;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.jdt.groovy.internal.SimplifiedExtendedCompletionContext;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.SourceField;

public class GroovyExtendedCompletionContext extends SimplifiedExtendedCompletionContext {

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

    private static String toFieldName(IMethod method) {
        return ProposalUtils.createMockFieldName(method.getElementName());
    }

    private final ContentAssistContext context;

    private final VariableScope currentScope;

    // computed after initialization
    private IJavaElement enclosingElement;

    // computed after initialization
    private final Map<String, IJavaElement[]> visibleElements;

    public GroovyExtendedCompletionContext(ContentAssistContext context, VariableScope currentScope) {
        this.context = context;
        this.currentScope = currentScope;
        this.visibleElements = new HashMap<String, IJavaElement[]>();
    }

    @Override
    public IJavaElement getEnclosingElement() {
        if (enclosingElement == null) {
            try {
                enclosingElement = context.unit.getElementAt(context.completionLocation);
            } catch (JavaModelException e) {
                GroovyCore.logException("Exception computing content assist proposals", e);
            }
            if (enclosingElement == null) {
                enclosingElement = context.unit;
            }
        }

        return enclosingElement;
    }

    @Override
    public IJavaElement[] getVisibleElements(String typeSignature) {
        // let's not work with parameterized sigs
        typeSignature = Signature.getTypeErasure(typeSignature);

        IJavaElement[] elements = visibleElements.get(typeSignature);
        if (elements == null) {
            elements = computeVisibleElements(typeSignature);
            visibleElements.put(typeSignature, elements);
        }
        return elements;
    }

    private IJavaElement[] computeVisibleElements(String typeSignature) {
        ClassNode targetType = toClassNode(typeSignature);
        boolean isEnum = targetType.isEnum();

        // look at all local variables in scope
        Map<String, IJavaElement> nameElementMap = new LinkedHashMap<String, IJavaElement>();
        if (currentScope != null) {
            for (VariableInfo varInfo : currentScope) {
                // GRECLIPSE-1268 currently, no good way to get to the actual declaration of the variable.
                // This can cause ordering problems for the guessed parameters.
                // don't put elements in a second time since we are moving from inner scope to outer scope
                String varName = varInfo.name;
                // ignore synthetic getters and setters that are put in the scope
                // looking at prefix is a good approximation
                if (!varName.startsWith("get") &&
                    !varName.startsWith("set") &&
                    !varName.equals("super") &&
                    !varName.startsWith("<") &&
                    !nameElementMap.containsKey(varName)) {

                    ClassNode type = varInfo.type;
                    if (GroovyUtils.isAssignable(type, targetType)) {
                        // NOTE: parent, source location, typeSignature, etc. are not important here
                        int start = 0, until = varName.length() - 1;
                        nameElementMap.put(varName, new LocalVariable(
                            (JavaElement) getEnclosingElement(), varName, start, until, start, until, typeSignature, null, 0, false));
                    }
                }
            }
        }

        // now check fields
        IType enclosingType = (IType) getEnclosingElement().getAncestor(IJavaElement.TYPE);
        if (enclosingType != null) {
            try {
                addFields(targetType, nameElementMap, enclosingType);
                ITypeHierarchy typeHierarchy = enclosingType.newSupertypeHierarchy(null);
                IType[] allTypes = typeHierarchy.getAllSupertypes(enclosingType);
                for (IType superType : allTypes) {
                    addFields(targetType, nameElementMap, superType);
                }
            } catch (JavaModelException e) {
                GroovyCore.logException("", e);
            }
        }

        if (isEnum) {
            IType targetIType = new GroovyProjectFacade(enclosingElement).groovyClassToJavaType(targetType);
            List<FieldNode> fields = targetType.getFields();
            for (FieldNode enumVal : fields) {
                String name = enumVal.getName();
                if (name.equals("MIN_VALUE") || name.equals("MAX_VALUE")) {
                    continue;
                }
                if (!enumVal.getType().equals(targetType)) {
                    continue;
                }
                nameElementMap.put(targetIType.getElementName() + "." + name, targetIType.getField(name));
                nameElementMap.put(name, targetIType.getField(name));
            }
        }
        return nameElementMap.values().toArray(new IJavaElement[0]);
    }

    public void addFields(ClassNode targetType, Map<String, IJavaElement> nameElementMap, IType type)
            throws JavaModelException {
        IField[] fields = type.getFields();
        for (IField field : fields) {
            ClassNode fieldTypeClassNode = toClassNode(field.getTypeSignature());
            if (GroovyUtils.isAssignable(fieldTypeClassNode, targetType)) {
                nameElementMap.put(field.getElementName(), field);
            }
        }
        // also add methods
        IMethod[] methods = type.getMethods();
        for (IMethod method : methods) {
            ClassNode methodReturnTypeClassNode = toClassNode(method.getReturnType());
            if (GroovyUtils.isAssignable(methodReturnTypeClassNode, targetType)) {
                if ((method.getParameterTypes() == null || method.getParameterTypes().length == 0) &&
                    (method.getElementName().startsWith("get") || method.getElementName().startsWith("is"))) {

                    nameElementMap.put(method.getElementName(), method);
                    IField field = new PropertyVariant(method);
                    nameElementMap.put(field.getElementName(), field);
                }
            }
        }
    }

    private ClassNode toClassNode(String typeSignature) {
        int dims = Signature.getArrayCount(typeSignature);
        String noArray = Signature.getElementType(typeSignature);
        String qualifiedName = getQualifiedName(noArray);
        ClassNode resolved;
        if (typeSignature.length() == 1 + dims) { // is primitive type
            resolved = /*ClassHelper.getWrapper(*/ClassHelper.make(qualifiedName)/*)*/;
        } else {
            try {
                resolved = context.unit.getModuleInfo(false).resolver.resolve(qualifiedName);
            } catch (NullPointerException e) {
                // ignore; likely DSL support not available
                resolved = VariableScope.OBJECT_CLASS_NODE;
            }
        }
        for (int i = 0; i < dims; i += 1) {
            resolved = resolved.makeArray();
        }
        return resolved;
    }

    private String getQualifiedName(String typeSignature) {
        String qualifier = Signature.getSignatureQualifier(typeSignature);
        String qualifiedName = Signature.getSignatureSimpleName(typeSignature);
        if (qualifier.length() > 0) {
            qualifiedName = qualifier + "." + qualifiedName;
        }
        return qualifiedName;
    }
}
