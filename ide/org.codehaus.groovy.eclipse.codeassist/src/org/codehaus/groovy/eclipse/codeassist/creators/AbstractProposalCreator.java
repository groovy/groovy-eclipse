/*
 * Copyright 2009-2021 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.creators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.proposals.AbstractGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.AccessorSupport;
import org.eclipse.jdt.groovy.search.VariableScope;

public abstract class AbstractProposalCreator implements IProposalCreator {

    public void setCurrentScope(VariableScope currentScope) {
        this.currentScope = currentScope;
    }
    protected VariableScope currentScope;

    public void setFavoriteStaticMembers(Set<String> favoriteStaticMembers) {
        this.favoriteStaticMembers = favoriteStaticMembers;
    }
    protected Set<String> favoriteStaticMembers;

    public void setNameMatchingStrategy(BiPredicate<String, String> strategy) {
        this.matcher = strategy;
    }
    protected BiPredicate<String, String> matcher = ProposalUtils::looselyMatches;

    //--------------------------------------------------------------------------

    protected static FieldNode createMockField(final MethodNode method) {
        String fieldName = ProposalUtils.createMockFieldName(method.getName());
        int fieldModifiers = (method.getModifiers() & 0xF) | (GroovyUtils.isDeprecated(method) ? Flags.AccDeprecated : 0);
        ClassNode fieldType = AccessorSupport.isSetter(method) ? DefaultGroovyMethods.last(method.getParameters()).getType() : method.getReturnType();

        FieldNode fieldNode = new FieldNode(fieldName, fieldModifiers, fieldType, method.getDeclaringClass(), null);
        fieldNode.setDeclaringClass(method.getDeclaringClass());
        fieldNode.setSourcePosition(method);
        return fieldNode;
    }

    protected static void demoteLowVisibilityProposals(List<IGroovyProposal> proposals, ClassNode completionType) {
        for (IGroovyProposal proposal : proposals) {
            if (proposal instanceof AbstractGroovyProposal) {
                AbstractGroovyProposal groovyProposal = (AbstractGroovyProposal) proposal;

                int flags = groovyProposal.getAssociatedNodeFlags();
                ClassNode declaringType = groovyProposal.getAssociatedNode().getDeclaringClass();

                if (!Flags.isPublic(flags) && !declaringType.equals(completionType) &&
                    !(Flags.isProtected(flags) && completionType.isDerivedFrom(declaringType))) {

                    groovyProposal.setRelevanceMultiplier(groovyProposal.getRelevanceMultiplier() * 0.05f);
                }
            }
        }
    }

    /**
     * Determine the kind of accessor the prefix corresponds to, if any
     */
    protected AccessorSupport findLooselyMatchedAccessorKind(String prefix, String methodName, boolean isCategory) {
        AccessorSupport accessor = AccessorSupport.create(methodName, isCategory);
        if (accessor.isAccessor()) {
            String newName = ProposalUtils.createMockFieldName(methodName);
            return matcher.test(prefix, newName) ? accessor : AccessorSupport.NONE;
        } else {
            return AccessorSupport.NONE;
        }
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

        Set<ClassNode> types = new LinkedHashSet<>();
        VariableScope.createTypeHierarchy(thisType, types, false);
        types.removeAll(exclude);

        for (ClassNode type : types) {
            List<FieldNode> fields = type.getFields();
            if (type == thisType) {
                List<FieldNode> traitFields = type.redirect().getNodeMetaData("trait.fields");
                if (traitFields != null) {
                    fields = new ArrayList<>(fields);
                    fields.addAll(traitFields);
                }
            }

            for (FieldNode field : fields) {
                // only add new field if the new field is more accessible than the existing one
                FieldNode existing = allFields.get(field.getName());
                if (existing == null || leftIsMoreAccessible(field, existing)) {
                    allFields.put(field.getName(), field);
                }
            }
        }

        // don't do anything with these types next time
        exclude.addAll(types);

        return allFields.values();
    }

    protected Collection<MethodNode> getAllMethods(ClassNode type, Set<ClassNode> exclude) {
        Map<String, MethodNode> methods = new HashMap<>();

        BiConsumer<MethodNode, Map<String, ClassNode>> mapper = (mn, spec) -> {
            StringBuilder sb = new StringBuilder(mn.getName());
            sb.append('(');
            for (org.codehaus.groovy.ast.Parameter p : mn.getParameters()) {
                ClassNode pt = p.getOriginType();
                if (!pt.isGenericsPlaceHolder()) {
                    sb.append(pt.getName());
                } else {
                    sb.append(spec.getOrDefault(pt.getUnresolvedName(), pt).getName());
                }
                sb.append(';');
            }

            methods.merge(sb.toString(), mn, (m1, m2) ->
                // keep override method unless it's synthetic
                !Flags.isSynthetic(m1.getModifiers()) ? m1 : m2);
        };

        if (exclude == null || !exclude.contains(type)) {
            List<MethodNode> traitMethods = type.redirect().getNodeMetaData("trait.methods");
            if (traitMethods != null && !traitMethods.isEmpty()) {
                for (MethodNode mn : traitMethods) {
                    mapper.accept(mn, Collections.emptyMap());
                }
            }
        }

        Set<ClassNode> types = new LinkedHashSet<>();
        VariableScope.createTypeHierarchy(type, types, true);
        for (ClassNode cn : types) {
            if ((exclude != null && exclude.contains(cn)) ||
                Flags.isSynthetic(cn.getModifiers())) continue;

            Map<String, ClassNode> spec = GenericsUtils.createGenericsSpec(cn);
            for (MethodNode mn : cn.getMethods()) {
                mapper.accept(mn, spec);
            }
        }

        if (exclude != null) exclude.addAll(types); // exclude types next time

        return methods.values();
    }

    protected static boolean leftIsMoreAccessible(FieldNode field, FieldNode existing) {
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

    /**
     * Checks that there is no field matching the bean property for a getter or setter.
     *
     * @param declaringClass declaring type of the method
     * @param methodName name of method to check
     */
    protected static boolean hasNoField(ClassNode declaringClass, String methodName) {
        return (!"getClass".equals(methodName) &&
            declaringClass.getField(ProposalUtils.createMockFieldName(methodName)) == null &&
            declaringClass.getField(ProposalUtils.createCapitalMockFieldName(methodName)) == null);
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
            ClassNode typeNode = ClassHelper.make(t);
            typeNode.lazyClassInit();
            return typeNode;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return null;
        }
    }
}
