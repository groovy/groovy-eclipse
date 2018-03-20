/*
 * Copyright 2009-2018 the original author or authors.
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.groovy.search.AccessorSupport;
import org.eclipse.jdt.groovy.search.VariableScope;

public abstract class AbstractProposalCreator implements IProposalCreator {

    protected VariableScope currentScope;

    public void setCurrentScope(VariableScope currentScope) {
        this.currentScope = currentScope;
    }

    protected Set<String> favoriteStaticMembers;

    public void setFavoriteStaticMembers(Set<String> favoriteStaticMembers) {
        this.favoriteStaticMembers = favoriteStaticMembers;
    }

    protected boolean checkName(String name) {
        return (name.charAt(0) != '<' && !name.contains("$"));
    }

    /**
     * Returns all fields, even those that are converted into properties.
     */
    protected Collection<FieldNode> getAllFields(ClassNode thisType, Set<ClassNode> exclude) {
        if (thisType.isArray()) {
            FieldNode length = new FieldNode("length", Flags.AccPublic | Flags.AccFinal, ClassHelper.int_TYPE, thisType, null);
            length.setDeclaringClass(thisType);
            return Collections.singleton(length);
        }

        Map<String, FieldNode> allFields = new HashMap<>();

        // use a LinkedHashSet to preserve order
        Set<ClassNode> types = new LinkedHashSet<>();
        getAllSupers(thisType, types, exclude);

        for (ClassNode type : types) {
            for (FieldNode field : type.getFields()) {
                if (checkName(field.getName())) {
                    // only add new field if the new field is more accessible than the existing one
                    FieldNode existing = allFields.get(field.getName());
                    if (existing == null || leftIsMoreAccessible(field, existing)) {
                        allFields.put(field.getName(), field);
                    }
                }
            }
        }

        // don't do anything with these types next time
        exclude.addAll(types);

        return allFields.values();
    }

    protected List<MethodNode> getAllMethods(ClassNode type, Set<ClassNode> exclude) {
        List<MethodNode> allMethods = type.getAllDeclaredMethods();
        if (!exclude.isEmpty()) {
            // remove all methods from classes that we have already visited
            for (Iterator<MethodNode> methodIter = allMethods.iterator(); methodIter.hasNext();) {
                if (exclude.contains(methodIter.next().getDeclaringClass())) {
                    methodIter.remove();
                }
            }
        }

        Set<ClassNode> types = new LinkedHashSet<>();
        getAllSupers(type, types, exclude);

        // keep track of the already seen types so that next time, we won't include them
        exclude.addAll(types);

        return allMethods;
    }

    protected void getAllSupers(ClassNode type, Set<ClassNode> set, Set<ClassNode> exclude) {
        if (type == null) {
            return;
        }
        if (!exclude.contains(type)) {
            set.add(type);
        }
        getAllSupers(type.getSuperClass(), set, exclude);
        for (ClassNode inter : (Iterable<ClassNode>) type.getAllInterfaces()) {
            if (!inter.getName().equals(type.getName())) {
                getAllSupers(inter, set, exclude);
            }
        }
    }

    /**
     * find the most accessible element
     */
    private static boolean leftIsMoreAccessible(FieldNode field, FieldNode existing) {
        int leftAcc;
        switch (field.getModifiers() & (Flags.AccPublic | Flags.AccPrivate | Flags.AccProtected)) {
        case Flags.AccPublic:
            leftAcc = 0;
            break;
        case Flags.AccProtected:
            leftAcc = 1;
            break;
        case Flags.AccPrivate:
            leftAcc = 3;
            break;
        default: // package-private
            leftAcc = 2;
            break;
        }

        int rightAcc;
        switch (existing.getModifiers() & (Flags.AccPublic | Flags.AccPrivate | Flags.AccProtected)) {
        case Flags.AccPublic:
            rightAcc = 0;
            break;
        case Flags.AccProtected:
            rightAcc = 1;
            break;
        case Flags.AccPrivate:
            rightAcc = 3;
            break;
        default: // package-private
            rightAcc = 2;
            break;
        }

        return (leftAcc < rightAcc);
    }

    protected void getAllSupersAsStrings(ClassNode type, Set<String> set) {
        if (type == null) {
            return;
        }
        set.add(type.getName());
        getAllSupersAsStrings(type.getSuperClass(), set);
        for (ClassNode inter : (Iterable<ClassNode>) type.getAllInterfaces()) {
            if (!inter.getName().equals(type.getName())) {
                getAllSupersAsStrings(inter, set);
            }
        }
    }

    /**
     * Checks that there is no field matching the bean property for a getter or setter.
     *
     * @param declaringClass declaring type of the method
     * @param methodName name of method to check
     */
    protected boolean hasNoField(ClassNode declaringClass, String methodName) {
        return (!"getClass".equals(methodName) &&
            declaringClass.getField(ProposalUtils.createMockFieldName(methodName)) == null &&
            declaringClass.getField(ProposalUtils.createCapitalMockFieldName(methodName)) == null);
    }

    protected FieldNode createMockField(MethodNode method) {
        FieldNode field = new FieldNode(ProposalUtils.createMockFieldName(method.getName()), method.getModifiers(), method.getReturnType(), method.getDeclaringClass(), null);
        field.setDeclaringClass(method.getDeclaringClass());
        field.setSourcePosition(method);
        return field;
    }

    /**
     * Determine the kind of accessor the prefix corresponds to, if any
     */
    protected AccessorSupport findLooselyMatchedAccessorKind(String prefix, String methodName, boolean isCategory) {
        AccessorSupport accessor = AccessorSupport.create(methodName, isCategory);
        if (accessor.isAccessor()) {
            String newName = ProposalUtils.createMockFieldName(methodName);
            return ProposalUtils.looselyMatches(prefix, newName) ? accessor : AccessorSupport.NONE;
        } else {
            return AccessorSupport.NONE;
        }
    }

    @Override
    public boolean redoForLoopClosure() {
        return true;
    }

    protected static ClassNode tryResolveClassNode(String typeName, ModuleNode module) {
        for (ClassNode t : module.getClasses()) {
            if (t.getName().equals(typeName)) {
                return t;
            }
        }
        try {
            //ClassNode type = ((EclipseSourceUnit) module.getContext()).resolver.resolve(typeName);
            Class<?> t = module.getContext().getClassLoader().loadClass(typeName, true, true, true);
            return ClassHelper.make(t);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
