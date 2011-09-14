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

package org.codehaus.groovy.eclipse.codeassist.creators;

import java.util.Set;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 *
 */
public abstract class AbstractProposalCreator implements IProposalCreator {

    /**
     * The type of the LHS of the assignment statement associated with this
     * invocation or null if there is none
     */
    protected ClassNode lhsType;

    protected VariableScope currentScope;

    protected boolean checkName(String name) {
        return name.charAt(0) != '<' && !name.contains("$");
    }

    protected void getAllSupers(ClassNode type, Set<ClassNode> set) {
        if (type == null) {
            return;
        }
        set.add(type);
        getAllSupers(type.getSuperClass(), set);
        for (ClassNode inter : (Iterable<ClassNode>) type.getAllInterfaces()) {
            if (! inter.getName().equals(type.getName())) {
                getAllSupers(inter, set);
            }
        }
    }

    protected boolean isInterestingType(ClassNode type) {
        return lhsType != null
                && ClassHelper.getUnwrapper(type).equals(lhsType);
    }

    protected void getAllSupersAsStrings(ClassNode type, Set<String> set) {
        if (type == null) {
            return;
        }
        set.add(type.getName());
        getAllSupersAsStrings(type.getSuperClass(), set);
        for (ClassNode inter : (Iterable<ClassNode>) type.getAllInterfaces()) {
            if (! inter.getName().equals(type.getName())) {
                getAllSupersAsStrings(inter, set);
            }
        }
    }

    public void setLhsType(ClassNode lhsType) {
        this.lhsType = lhsType;
    }

    public void setCurrentScope(VariableScope currentScope) {
        this.currentScope = currentScope;
    }

    /**
     * Check to ensure that there is no field with a getter or setter name before creating the mock
     * field
     * @param declaringClass declaring type of the method
     * @param methodName method to check for
     */
    protected boolean hasNoField(ClassNode declaringClass, String methodName) {
        return declaringClass.getField(createMockFieldName(methodName)) == null
                && declaringClass
                        .getField(createCapitalMockFieldName(methodName)) == null;
    }

    protected FieldNode createMockField(MethodNode method) {
        FieldNode field = new FieldNode(createMockFieldName(method.getName()),
                method.getModifiers(), method.getReturnType(),
                method.getDeclaringClass(), null);
        field.setDeclaringClass(method.getDeclaringClass());
        field.setSourcePosition(method);
        return field;
    }

    protected boolean looselyMatchesGetterName(String prefix, String methodName) {
        // first try 'is'
        boolean isGetterName = false;
        if (methodName.length() >= 2 && methodName.startsWith("is") && Character.isUpperCase(methodName.charAt(2))) {
            isGetterName = true;
        }

        if (!isGetterName) {
            if (methodName.length() > 3 && (methodName.startsWith("get") || methodName.startsWith("set"))
                    && Character.isUpperCase(methodName.charAt(3))) {
                isGetterName = true;
            }
        }
        if (isGetterName) {
            String newName = createMockFieldName(methodName);
            return ProposalUtils.looselyMatches(prefix, newName);
        } else {
            return false;
        }
    }

    /**
     * Create a name for a field if this is a getter or a setter method name
     * @param methodName
     * @return
     */
    protected String createMockFieldName(String methodName) {
        int prefix = methodName.startsWith("is") ? 2 : 3;

        return methodName.length() > prefix ? Character.toLowerCase(methodName.charAt(prefix)) + methodName.substring(prefix + 1)
                : "$$$$$";
    }

    /**
     * Create a name for a field if this is a getter or a setter method name The resulting name is
     * capitalized
     * @param methodName
     * @return
     */
    protected String createCapitalMockFieldName(String methodName) {
        return methodName.length() > 3 ? methodName.substring(3) : "$$$$$";
    }
}