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
package org.codehaus.groovy.eclipse.dsl.contributions;

import static java.beans.Introspector.decapitalize;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSetterName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import groovy.lang.Closure;
import groovy.lang.MissingPropertyException;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.groovy.search.GenericsMapper;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * A contribution group will determine the set of contribution elements (eg-
 * extra methods, properties, templates, etc) that are added to a particular type
 * when the attached pointcut matches.
 */
public class DSLContributionGroup extends ContributionGroup {

    private static final String NO_NAME = "";
    private static final String NO_TYPE = "java.lang.Object";
    private static final ParameterContribution[] NO_PARAMS = {};

    /**
     * The closure that comes from the DSLD script.
     * It's delegate is set to <code>this</code>.
     */
    private final Closure<?> contributionClosure;

    private VariableScope scope;

    // provider that is set for the entire contribution group; individual contributions can override
    private String provider;

    private IJavaProject project;

    private ResolverCache resolver;

    private Map<String, Collection<Object>> bindings;

    private ClassNode currentType;

    private Map<String, Object> wormhole;

    private boolean staticScope;

    private boolean isPrimaryExpression;

    public DSLContributionGroup(final Closure<?> contributionClosure) {
        this.contributionClosure = contributionClosure;
        if (contributionClosure != null) {
            contributionClosure.setDelegate(this);
            contributionClosure.setResolveStrategy(Closure.DELEGATE_FIRST);
        }
    }

    /**
     * The main entry point into the contribution.
     */
    @Override
    public List<IContributionElement> getContributions(final GroovyDSLDContext pattern, final BindingSet matches) {
        // uh oh...needs to be synchronized, or can we make this class stateless?
        synchronized (this) {
            List<IContributionElement> result;
            try {
                contributions = new ArrayList<>();
                scope = pattern.getCurrentScope();
                project = pattern.getCurrentProject();
                bindings = matches.getBindings();
                resolver = pattern.getResolverCache();
                wormhole = scope.getWormhole();
                currentType = pattern.getCurrentType();
                staticScope = pattern.isStatic();
                isPrimaryExpression = pattern.isPrimaryNode();

                contributionClosure.call();
            } catch (Exception e) {
                GroovyLogManager.manager.logException(TraceCategory.DSL, e);
            } finally {
                // if a contribution group changed the delegate, pass that along
                if (!currentType.equals(pattern.getCurrentType()))
                    pattern.setTargetType(currentType);
                result = contributions;
                contributions = null;
                scope = null;
                project = null;
                bindings = null;
                resolver = null;
                wormhole = null;
                currentType = null;
            }
            return result;
        }
    }

    @Override
    public Object getProperty(final String property) {
        switch (property) {
        case "resolver":
            return resolver;
        case "wormhole":
            return wormhole;
        case "currentType":
            return currentType;
        case "currentNode":
            return scope.getCurrentNode();
        case "enclosingNode":
            return scope.getEnclosingNode();
        default:
            if (bindings.containsKey(property)) {
                return bindings.get(property);
            }
            throw new MissingPropertyException(property, getClass());
        }
    }

    private ParameterContribution[] extractParams(final Map<String, Object> args, final String paramKind) {
        @SuppressWarnings("unchecked")
        Map<Object, Object> paramsMap = (Map<Object, Object>) args.get(paramKind);
        ParameterContribution[] params = NO_PARAMS;
        if (paramsMap != null && !paramsMap.isEmpty()) {
            params = new ParameterContribution[paramsMap.size()];
            int i = 0;
            for (Map.Entry<Object, Object> entry : paramsMap.entrySet()) {
                String name = asString(entry.getKey());
                String type = asString(entry.getValue());
                params[i++] = new ParameterContribution(name != null ? name : NO_NAME, type != null ? type : NO_TYPE);
            }
        }
        return params;
    }

    /**
     * Convert a {@link ClassNode} into a string that includes type parameters
     */
    static String getTypeName(final ClassNode clazz) {
        StringBuilder sb = new StringBuilder();
        sb.append(clazz.getName());
        if (clazz.getGenericsTypes() != null && clazz.getGenericsTypes().length > 0) {
            sb.append('<');
            for (GenericsType gt : clazz.getGenericsTypes()) {
                sb.append(getTypeName(gt.getType()));
                sb.append(',');
            }
            sb.replace(sb.length() - 1, sb.length(), ">");
        }
        return sb.toString();
    }

    private void internalDelegatesTo(final AnnotatedNode expr, final boolean useNamedArgs, final boolean isStatic,
            final boolean asCategory, final boolean isDeprecated, final List<String> exceptions, final boolean noParens) {
        if (staticScope && !isStatic && !VariableScope.CLASS_CLASS_NODE.equals(currentType)) {
            return;
        }
        ClassNode type;
        if (expr instanceof ClassNode) {
            type = (ClassNode) expr;
        } else if (expr instanceof FieldNode) {
            type = ((FieldNode) expr).getType();
        } else if (expr instanceof MethodNode) {
            type = ((MethodNode) expr).getReturnType();
        } else if (expr instanceof ClassExpression) {
            type = ((ClassExpression) expr).getType();
        } else {
            // invalid
            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.log(TraceCategory.DSL, "Cannot invoke delegatesTo() on an invalid object: " + expr);
            }
            return;
        }
        if (!VariableScope.OBJECT_CLASS_NODE.equals(type)) {
            // use this to resolve parameterized types
            GenericsMapper mapper = GenericsMapper.gatherGenerics(type);

            // naked variants of getter and setter methods must be added at the end
            // FIXADE why???
            List<IContributionElement> accessorContribs = new ArrayList<>(1);
            for (MethodNode method : type.getMethods()) {
                if ((exceptions == null || !exceptions.contains(method.getName())) && !(method instanceof ConstructorNode) && !method.getName().contains("$")) {
                    method = VariableScope.resolveTypeParameterization(mapper, method);
                    if (asCategory) {
                        delegateToCategoryMethod(useNamedArgs, isStatic, type, method, method.getReturnType(), isDeprecated, accessorContribs, noParens);
                    } else {
                        delegateToNonCategoryMethod(useNamedArgs, isStatic, type, method, method.getReturnType(), isDeprecated, accessorContribs, noParens);
                    }
                }
            }
            contributions.addAll(accessorContribs);
        }
    }

    // FIXADE TODO combine with #delegateToCategoryMethod
    private void delegateToNonCategoryMethod(final boolean useNamedArgs, final boolean isStatic, final ClassNode type, final MethodNode method,
            final ClassNode resolvedReturnType, final boolean isDeprecated, final List<IContributionElement> accessorContribs, final boolean noParens) {
        String name = method.getName();
        contributions.add(new MethodContributionElement(name, toParameterContribution(method.getParameters()), NO_PARAMS, NO_PARAMS, getTypeName(resolvedReturnType), getTypeName(type), (method.isStatic() || isStatic), provider, null, useNamedArgs, noParens, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
        // also add the associated property if applicable
        if ((name = isAccessor(method, name, false)) != null) {
            int modifiers = 0;
            if (method.isStatic() || isStatic) modifiers |= Flags.AccStatic;
            if (method.getDeclaringClass().getMethods(getSetterName(name)).isEmpty()) modifiers |= Flags.AccFinal;
            accessorContribs.add(new PropertyContributionElement(name, getTypeName(resolvedReturnType), getTypeName(method.getDeclaringClass()), modifiers, provider, null, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
        }
    }

    private void delegateToCategoryMethod(final boolean useNamedArgs, final boolean isStatic, final ClassNode type, final MethodNode method,
            final ClassNode resolvedReturnType, final boolean isDeprecated, final List<IContributionElement> accessorContribs, final boolean noParens) {
        String name = method.getName();
        if (method.getParameters() != null && method.getParameters().length > 0) {
            ClassNode firstType = method.getParameters()[0].getType();
            if ((firstType.isInterface() && currentType.implementsInterface(firstType)) || currentType.isDerivedFrom(firstType)) {
                contributions.add(new MethodContributionElement(name, toParameterContributionRemoveFirst(method.getParameters()), NO_PARAMS, NO_PARAMS, getTypeName(resolvedReturnType), getTypeName(type), isStatic, provider, null, useNamedArgs, noParens, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
                // also add the associated property if applicable
                if ((name = isAccessor(method, name, true)) != null) {
                    int modifiers = 0;
                    if (method.isStatic() || isStatic) modifiers |= Flags.AccStatic;
                    if (method.getDeclaringClass().getMethods(getSetterName(name)).isEmpty()) modifiers |= Flags.AccFinal;
                    accessorContribs.add(new PropertyContributionElement(name, getTypeName(resolvedReturnType), getTypeName(method.getDeclaringClass()), modifiers, provider, null, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
                }
            }
        }
    }

    private ParameterContribution[] toParameterContribution(final Parameter[] params) {
        if (params != null && params.length > 0) {
            ParameterContribution[] contribs = new ParameterContribution[params.length];
            for (int i = 0, n = params.length; i < n; i += 1) {
                contribs[i] = new ParameterContribution(params[i]);
            }
            return contribs;
        }
        return NO_PARAMS;
    }

    private ParameterContribution[] toParameterContributionRemoveFirst(final Parameter[] params) {
        if (params != null && params.length > 1) {
            ParameterContribution[] contribs = new ParameterContribution[params.length - 1];
            for (int i = 1, n = params.length; i < n; i += 1) {
                contribs[i - 1] = new ParameterContribution(params[i]);
            }
            return contribs;
        }
        return NO_PARAMS;
    }

    // TODO: Move to AccessorSupport or replace with something from it?
    private static String isAccessor(final MethodNode method, final String name, final boolean isCategory) {
        if (method.getParameters() == null || method.getParameters().length == (isCategory ? 1 : 0)) {
            if (name.length() > 3 && name.startsWith("get")) {
                return decapitalize(name.substring(3));
            } else if (name.length() > 2 && name.startsWith("is")) {
                return decapitalize(name.substring(2));
            }
        }
        return null;
    }

    private static boolean isDeprecated(final Map<?, ?> args) {
        return asBoolean(args.get("isDeprecated"));
    }

    private static boolean isFinal(final Map<?, ?> args) {
        Object modifier = args.get("readOnly");
        if (modifier == null)
            modifier = args.get("isFinal");
        return asBoolean(modifier);
    }

    private static boolean isStatic(final Map<?, ?> args) {
        return asBoolean(args.get("isStatic"));
    }

    private static boolean asBoolean(final Object value) {
        if (value == null) {
            return false;
        } else if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String string = value.toString();
        return ("true".equalsIgnoreCase(string) || "yes".equalsIgnoreCase(string));
    }

    private ClassNode asClassNode(final Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof ClassNode) {
            return (ClassNode) value;
        } else if (value instanceof String) {
            return resolver.resolve((String) value);
        } else if (value instanceof Class) {
            return resolver.resolve(((Class<?>) value).getCanonicalName());
        } else {
            return resolver.resolve(value.toString());
        }
    }

    @SuppressWarnings("rawtypes")
    private String asString(final Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Class) {
            return ((Class) value).getCanonicalName();
        } else if (value instanceof ClassNode) {
            return getTypeName(((ClassNode) value));
        } else if (value instanceof FieldNode) {
            return getTypeName(((FieldNode) value).getDeclaringClass()) + "." + ((FieldNode) value).getName();
        } else if (value instanceof MethodNode) {
            return getTypeName(((MethodNode) value).getDeclaringClass()) + "." + ((MethodNode) value).getName();
        } else if (value instanceof ConstantExpression) {
            return ((ConstantExpression) value).getText();
        } else if (value instanceof Variable) {
            return ((Variable) value).getName();
        } else if (value instanceof AnnotationNode) {
            return ((AnnotationNode) value).getClassNode().getName();
        } else if (value instanceof Collection && ((Collection) value).size() == 1) {
            return asString(((Collection) value).iterator().next());
        } // TODO: Handle array of length 1 same as Collection?
        return value.toString();
    }

    //--------------------------------------------------------------------------
    // methods available within contribution closure:

    void provider(final Object value) {
        provider = asString(value);
    }

    /**
     * Adds a property to the augmented class reference.
     */
    void property(final Map<String, Object> args) {
        boolean isStatic = isStatic(args);
        if (!staticScope || (staticScope && isStatic)) {
            String name = asString(args.get("name"));
            if (name == null) name = NO_NAME;

            String type = asString(args.get("type"));
            if (type == null) type = NO_TYPE;

            String declaringType = asString(args.get("declaringType"));
            if (declaringType == null) declaringType = getTypeName(currentType);

            int modifiers = 0;
            if (isStatic) modifiers |= Flags.AccStatic;
            if (isFinal(args)) modifiers |= Flags.AccFinal;

            String provider = asString(args.get("provider"));
            if (provider == null) provider = this.provider;

            String doc = asString(args.get("doc"));
            boolean isDeprecated = isDeprecated(args);

            contributions.add(new PropertyContributionElement(name, type, declaringType, modifiers, provider, doc, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
        }
    }

    /**
     * Adds a method to the augmented class reference.
     */
    void method(final Map<String, Object> args) {
        boolean isStatic = isStatic(args);
        if (!staticScope || (staticScope && isStatic)) {
            String name = asString(args.get("name"));
            if (name == null) name = NO_NAME;

            String returnType = asString(args.get("type"));
            if (returnType == null) returnType = NO_TYPE;

            String declaringType = asString(args.get("declaringType"));
            if (declaringType == null) declaringType = getTypeName(currentType);

            String provider = asString(args.get("provider"));
            if (provider == null) provider = this.provider;

            String doc = asString(args.get("doc"));
            boolean isDeprecated = isDeprecated(args);
            boolean noParens = asBoolean(args.get("noParens"));
            boolean useNamedArgs = asBoolean(args.get("useNamedArgs"));
            ParameterContribution[] params = extractParams(args, "params");
            ParameterContribution[] namedParams = extractParams(args, "namedParams");
            ParameterContribution[] optionalParams = extractParams(args, "optionalParams");

            contributions.add(new MethodContributionElement(name, params, namedParams, optionalParams, returnType,
                declaringType, isStatic, provider, doc, useNamedArgs, noParens, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
        }
    }

    /**
     * stub...will be used later to add templates
     */
    void template(final Map<String, String> args) {
    }

    @SuppressWarnings("unchecked")
    void delegatesTo(final Map<String, Object> args) {
        internalDelegatesTo(
            asClassNode(args.get("type")),
            asBoolean(args.get("useNamed")),
            isStatic(args),
            asBoolean(args.get("asCategory")),
            isDeprecated(args),
            (List<String>) args.get("except"),
            asBoolean(args.get("noParens")));
    }

    void delegatesTo(final String className) {
        delegatesTo(asClassNode(className));
    }

    void delegatesTo(final Class<?> clazz) {
        ClassNode resolved = asClassNode(clazz);
        if (resolved == VariableScope.OBJECT_CLASS_NODE && !clazz.getCanonicalName().equals(NO_TYPE)) {
            // likely that we are trying to resolve a class that is defined inside of a DSLD itself
            try {
                resolved = ClassHelper.make(clazz);
            } catch (Exception e) {
                GroovyDSLCoreActivator.logException(e);
            }
        }
        delegatesTo(resolved);
    }

    /**
     * Adds all members of {@code expr} type to the augmented class reference.
     */
    void delegatesTo(final AnnotatedNode expr) {
        internalDelegatesTo(expr, false, false, false, false, null, false);
    }

    void delegatesToUseNamedArgs(final String className) {
        delegatesToUseNamedArgs(asClassNode(className));
    }

    void delegatesToUseNamedArgs(final Class<?> clazz) {
        delegatesToUseNamedArgs(asClassNode(clazz));
    }

    /**
     * Adds all members of {@code expr} type to the augmented class reference.
     */
    void delegatesToUseNamedArgs(final AnnotatedNode expr) {
        internalDelegatesTo(expr, true, false, false, false, null, false);
    }

    void delegatesToCategory(final String className) {
        delegatesToCategory(asClassNode(className));
    }

    void delegatesToCategory(final Class<?> clazz) {
        delegatesToCategory(asClassNode(clazz));
    }

    /**
     * Adds all members of {@code expr} type to the augmented class reference.
     */
    void delegatesToCategory(final AnnotatedNode expr) {
        internalDelegatesTo(expr, false, false, true, false, null, false);
    }

    void setDelegateType(final Object arg) {
        ClassNode delegate = asClassNode(arg);
        if (delegate != null) {
            contributions.add(new EmptyContributionElement(currentType));
            scope.addVariable("delegate", delegate, VariableScope.CLOSURE_CLASS_NODE);
            scope.addVariable("getDelegate", delegate, VariableScope.CLOSURE_CLASS_NODE);

            // also need to set targetType, but only if primary expression
            if (isPrimaryExpression) {
                // must save for later
                currentType = delegate;
            }
        }
    }

    /**
     * Returns the parameter names and types of the given method node.
     */
    Map<String, ClassNode> params(final MethodNode node) {
        Parameter[] parameters = node.getParameters();
        if (parameters == null || parameters.length < 1) {
            return Collections.emptyMap();
        }

        String[] names = null;

        if ("arg0".equals(parameters[0].getName())) {
            ClassNode declaringClass = node.getDeclaringClass();
            if (declaringClass instanceof org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode) {
                try {
                    org.eclipse.jdt.core.IType declaringType = project.findType(declaringClass.getName());
                    if (declaringType != null && declaringType.exists()) {
                        String[] parameterTypeSignatures = new String[parameters.length];
                        for (int i = 0; i < parameters.length; i += 1) {
                            if (declaringType.isBinary()) {
                                parameterTypeSignatures[i] = String.valueOf(ProposalUtils.createTypeSignature(parameters[i].getType()));
                            } else {
                                parameterTypeSignatures[i] = String.valueOf(ProposalUtils.createUnresolvedTypeSignature(parameters[i].getType()));
                            }
                        }

                        String name = !node.getName().equals("<init>") ? node.getName() : declaringClass.getNameWithoutPackage();
                        org.eclipse.jdt.core.IMethod declaredMethod = declaringType.getMethod(name, parameterTypeSignatures);
                        if (declaredMethod != null && declaredMethod.exists()) {
                            names = declaredMethod.getParameterNames();
                        }
                    }
                } catch (Exception e) {
                    GroovyDSLCoreActivator.logException(e);
                }
            }
        }

        Map<String, ClassNode> params = new LinkedHashMap<>(parameters.length);
        for (int i = 0; i < parameters.length; i += 1) {
            params.put(names != null ? names[i] : parameters[i].getName(), parameters[i].getType());
        }
        return params;
    }

    /**
     * Logs a message to the Groovy Event Console log if it's open.
     */
    Object log(final Object msg) {
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "========== " + msg);
        }
        return msg;
    }
}
