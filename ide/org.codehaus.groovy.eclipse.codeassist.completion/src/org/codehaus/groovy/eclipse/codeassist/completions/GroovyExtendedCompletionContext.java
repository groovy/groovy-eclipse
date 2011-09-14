/*
 * Copyright 2011 the original author or authors.
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;
import org.eclipse.jdt.internal.codeassist.InternalExtendedCompletionContext;

/**
 *
 * @author andrew
 * @created May 3, 2011
 */
public class GroovyExtendedCompletionContext extends InternalExtendedCompletionContext {

    private static final IJavaElement[] NO_ELEMENTS = new IJavaElement[0];

    private final ContentAssistContext context;

    private final VariableScope currentScope;

    // computed after initialization
    private IJavaElement enclosingElement;

    // computed after initialization
    private final Map<String, IJavaElement[]> visibleElements;

    public GroovyExtendedCompletionContext(ContentAssistContext context, VariableScope currentScope) {
        // we don't use any of the code in the super class
        super(null, null, null, null, null, null, null, null);
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
        ClassNode superType = toClassNode(typeSignature);
        boolean isInterface = superType.isInterface();

        // look at all local variables in scope
        Map<String, IJavaElement> nameElementMap = new LinkedHashMap<String, IJavaElement>();
        Iterator<Entry<String, VariableInfo>> variablesIter = currentScope.variablesIterator();
        while (variablesIter.hasNext()) {
            Entry<String, VariableInfo> entry = variablesIter.next();
            // don't put elements in a second time since we are moving from
            // inner scope to outer scope
            String varName = entry.getKey();
            if (!varName.equals("super") && !nameElementMap.containsKey(varName)) {
                ClassNode type = entry.getValue().type;
                if (isAssignableTo(type, superType, isInterface)) {
                    // note that parent, start location, and typeSignature are
                    // not important here
                    nameElementMap.put(varName,
                            ReflectionUtils.createLocalVariable(enclosingElement, varName, 0, typeSignature));
                }
            }
        }

        // now check fields
        IType enclosingType = (IType) enclosingElement.getAncestor(IJavaElement.TYPE);
        if (enclosingType != null) {
            try {
                ITypeHierarchy typeHierarchy = enclosingType.newSupertypeHierarchy(null);
                IType[] allTypes = typeHierarchy.getAllTypes();
                for (IType type : allTypes) {
                    IField[] fields = type.getFields();
                    for (IField field : fields) {
                        ClassNode fieldTypeClassNode = toClassNode(field.getTypeSignature());
                        if (isAssignableTo(fieldTypeClassNode, superType, isInterface)) {
                            nameElementMap.put(field.getElementName(), field);
                        }
                    }
                }
            } catch (JavaModelException e) {
                GroovyCore.logException("", e);
            }
        }
        return nameElementMap.values().toArray(NO_ELEMENTS);
    }

    /**
     * @param typeSignature
     * @return
     */
    private ClassNode toClassNode(String typeSignature) {
        int dims = Signature.getArrayCount(typeSignature);
        String noArray = Signature.getElementType(typeSignature);
        String qualifiedName = getQualifiedName(noArray);
        ClassNode resolved;
        if (typeSignature.length() == 1 + dims) {
            resolved = ClassHelper.getWrapper(ClassHelper.make(qualifiedName));
        } else {
            resolved = context.unit.getResolver().resolve(qualifiedName);
        }
        for (int i = 0; i < dims; i++) {
            resolved = resolved.makeArray();
        }
        return resolved;
    }

    /**
     * This handles checking super classes and interfaces, autoboxing, but not
     * generics or groovy-specific kinds of coercion
     */
    private boolean isAssignableTo(ClassNode type, ClassNode superType, boolean isInterface) {
        // must make sure that the array dimensions are the same
        while (type.isArray() && superType.isArray()) {
            type = type.getComponentType();
            superType = superType.getComponentType();
        }
        if (type.isArray() || superType.isArray()) {
            return false;
        }

        // now box the type
        type = ClassHelper.getWrapper(type);
        // no need to do this...already has been wrapped above
        // superType = ClassHelper.getWrapper(superType);

        if (type.equals(superType)) {
            return true;
        }

        if (isInterface) {
            return type.implementsInterface(superType);
        } else {
            return type.isDerivedFrom(superType);
        }
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
