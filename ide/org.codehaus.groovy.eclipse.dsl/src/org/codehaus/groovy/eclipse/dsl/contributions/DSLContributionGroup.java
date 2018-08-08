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
package org.codehaus.groovy.eclipse.dsl.contributions;

import static java.beans.Introspector.decapitalize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import groovy.lang.Closure;

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
    @SuppressWarnings("rawtypes")
    private final Closure contributionClosure;

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

    public DSLContributionGroup(@SuppressWarnings("rawtypes") Closure contributionClosure) {
        this.contributionClosure = contributionClosure;
        if (contributionClosure != null) {
            contributionClosure.setDelegate(this);
            contributionClosure.setResolveStrategy(Closure.DELEGATE_FIRST);
        }
    }

    /**
     * This is the main entry point into the contribution
     */
    @Override
    public List<IContributionElement> getContributions(GroovyDSLDContext pattern, BindingSet matches) {
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
    public Object getProperty(String property) {
        if ("wormhole".equals(property)) {
            return wormhole;
        } else if ("currentNode".equals(property)) {
            return scope.getCurrentNode();
        } else if ("enclosingNode".equals(property)) {
            return scope.getEnclosingNode();
        } else if ("currentType".equals(property)) {
            return currentType;
        } else if ("resolver".equals(property)) {
            return resolver;
        }
        return bindings.get(property);
    }

    private ClassNode asClassNode(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return resolver.resolve((String) value);
        } else if (value instanceof ClassNode) {
            return (ClassNode) value;
        } else if (value instanceof Class) {
            return resolver.resolve(((Class<?>) value).getName());
        } else {
            return resolver.resolve(value.toString());
        }
    }

    private ParameterContribution[] extractParams(Map<String, Object> args, String paramKind) {
        Object value;
        @SuppressWarnings("unchecked")
        Map<Object, Object> paramsMap = (Map<Object, Object>) args.get(paramKind);
        ParameterContribution[] params;
        if (paramsMap != null) {
            params = new ParameterContribution[paramsMap.size()];
            int i = 0;
            for (Entry<Object, Object> entry : paramsMap.entrySet()) {
                value = entry.getValue();
                String type = value == null ? "java.lang.Object" : asString(value);
                params[i++] = new ParameterContribution(asString(entry.getKey()), type);
            }
        } else {
            params = NO_PARAMS;
        }
        return params;
    }

    private boolean asBoolean(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (Boolean) object;
        }
        String str = object.toString();
        return ("true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str));
    }

    /**
     * Convert a {@link ClassNode} into a string that includes type parameters
     */
    static String getTypeName(ClassNode clazz) {
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

    private void internalDelegatesTo(AnnotatedNode expr, boolean useNamedArgs, boolean isStatic, boolean asCategory, boolean isDeprecated, List<String> exceptions, boolean noParens) {
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
            GenericsMapper mapper = GenericsMapper.gatherGenerics(type, type.redirect());

            // naked variants of getter and setter methods must be added at the end
            // FIXADE why???
            List<IContributionElement> accessorContribs = new ArrayList<>(1);
            for (MethodNode method : type.getMethods()) {
                if ((exceptions == null || !exceptions.contains(method.getName())) && !(method instanceof ConstructorNode) && !method.getName().contains("$")) {
                    ClassNode resolvedReturnType = VariableScope.resolveTypeParameterization(mapper, VariableScope.clone(method.getReturnType()));
                    if (asCategory) {
                        delegateToCategoryMethod(useNamedArgs, isStatic, type, method, resolvedReturnType, isDeprecated, accessorContribs, noParens);
                    } else {
                        delegateToNonCategoryMethod(useNamedArgs, isStatic, type, method, resolvedReturnType, isDeprecated, accessorContribs, noParens);
                    }
                }
            }
            contributions.addAll(accessorContribs);
        }
    }

    // FIXADE TODO combine with #delegateToCategoryMethod
    private void delegateToNonCategoryMethod(boolean useNamedArgs, boolean isStatic, ClassNode type, MethodNode method,
            ClassNode resolvedReturnType, boolean isDeprecated, List<IContributionElement> accessorContribs, boolean noParens) {
        String name = method.getName();
        contributions.add(new MethodContributionElement(name, toParameterContribution(method.getParameters()), NO_PARAMS, NO_PARAMS, getTypeName(resolvedReturnType), getTypeName(type), (method.isStatic() || isStatic), provider, null, useNamedArgs, noParens, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
        // also add the associated property if applicable
        if ((name = isAccessor(method, name, false)) != null) {
            accessorContribs.add(new PropertyContributionElement(name, getTypeName(resolvedReturnType), getTypeName(method.getDeclaringClass()), (method.isStatic() || isStatic), provider, null, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
        }
    }

    private void delegateToCategoryMethod(boolean useNamedArgs, boolean isStatic, ClassNode type, MethodNode method,
            ClassNode resolvedReturnType, boolean isDeprecated, List<IContributionElement> accessorContribs, boolean noParens) {
        String name = method.getName();
        if (method.getParameters() != null && method.getParameters().length > 0) {
            ClassNode firstType = method.getParameters()[0].getType();
            if ((firstType.isInterface() && currentType.implementsInterface(firstType)) || currentType.isDerivedFrom(firstType)) {
                contributions.add(new MethodContributionElement(name, toParameterContributionRemoveFirst(method.getParameters()), NO_PARAMS, NO_PARAMS, getTypeName(resolvedReturnType), getTypeName(type), isStatic, provider, null, useNamedArgs, noParens, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
                // also add the associated property if applicable
                if ((name = isAccessor(method, name, true)) != null) {
                    accessorContribs.add(new PropertyContributionElement(name, getTypeName(resolvedReturnType), getTypeName(method.getDeclaringClass()), (method.isStatic() || isStatic), provider, null, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
                }
            }
        }
    }

    // TODO: Move to AccessorSupport or replace with something from it?
    private String isAccessor(MethodNode method, String name, boolean isCategory) {
        if (method.getParameters() == null || method.getParameters().length == (isCategory ? 1 : 0)) {
            if (name.length() > 3 && name.startsWith("get")) {
                return decapitalize(name.substring(3));
            } else if (name.length() > 2 && name.startsWith("is")) {
                return decapitalize(name.substring(2));
            }
        }
        return null;
    }

    private ParameterContribution[] toParameterContribution(Parameter[] params) {
        if (params != null) {
            ParameterContribution[] contribs = new ParameterContribution[params.length];
            for (int i = 0; i < contribs.length; i += 1) {
                contribs[i] = new ParameterContribution(params[i]);
            }
            return contribs;
        } else {
            return new ParameterContribution[0];
        }
    }

    private ParameterContribution[] toParameterContributionRemoveFirst(Parameter[] params) {
        if (params != null) {
            ParameterContribution[] contribs = new ParameterContribution[params.length - 1];
            for (int i = 1; i < params.length; i += 1) {
                contribs[i - 1] = new ParameterContribution(params[i]);
            }
            return contribs;
        } else {
            return new ParameterContribution[0];
        }
    }

    /**
     * @param args map passed in from the call to method or property
     * @return true iff the static argument is passed in.
     */
    private boolean isStatic(Map<?, ?> args) {
        return getBoolean("isStatic", args);
    }

    private boolean isDeprecated(Map<?, ?> args) {
        return getBoolean("isDeprecated", args);
    }

    private boolean getBoolean(String name, Map<?, ?> args) {
        Object maybeStatic = args.get(name);
        if (maybeStatic == null) {
            return false;
        } else if (maybeStatic instanceof Boolean) {
            return (Boolean) maybeStatic;
        } else {
            return Boolean.getBoolean(maybeStatic.toString());
        }
    }

    /**
     * Converts an object into a string
     */
    @SuppressWarnings("rawtypes")
    private String asString(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Class) {
            return ((Class) value).getName();
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

    void provider(Object args) {
        provider = args == null ? null : asString(args);
    }

    /**
     * Adds a method to the augmented class reference.
     */
    void method(Map<String, Object> args) {
        String name = asString(args.get("name"));

        Object value = args.get("type");
        String returnType = (value == null ? "java.lang.Object" : asString(value));

        value = args.get("declaringType");
        String declaringType = (value == null ? getTypeName(currentType) : asString(value));

        value = args.get("provider");
        String provider = (value == null ? this.provider : asString(value));
        value = args.get("doc");
        String doc = (value == null ? null : asString(value));

        boolean useNamedArgs = asBoolean(args.get("useNamedArgs"));
        boolean noParens = asBoolean(args.get("noParens"));

        ParameterContribution[] params = extractParams(args, "params");
        ParameterContribution[] namedParams = extractParams(args, "namedParams");
        ParameterContribution[] optionalParams = extractParams(args, "optionalParams");

        boolean isStatic = isStatic(args);
        boolean isDeprecated = isDeprecated(args);
        if (!staticScope || (staticScope && isStatic)) {
            contributions.add(new MethodContributionElement(name == null ? NO_NAME : name, params, namedParams, optionalParams,
                returnType == null ? NO_TYPE : returnType, declaringType, isStatic, provider == null ? this.provider : provider,
                    doc, useNamedArgs, noParens, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
        }
    }

    /**
     * Adds a property to the augmented class reference.
     */
    void property(Map<String, Object> args) {
        String name = asString(args.get("name"));

        Object value = args.get("type");
        String type = (value == null ? NO_TYPE : asString(value));

        value = args.get("declaringType");
        String declaringType = (value == null ? getTypeName(currentType) : asString(value));

        value = args.get("provider");
        String provider = (value == null ? this.provider : asString(value));
        String doc = asString(args.get("doc"));

        boolean isStatic = isStatic(args);
        boolean isDeprecated = isDeprecated(args);
        if (!staticScope || (staticScope && isStatic)) {
            contributions.add(new PropertyContributionElement(name == null ? NO_NAME : name, type,
                declaringType, isStatic, provider, doc, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
        }
    }

    /**
     * stub...will be used later to add templates
     */
    void template(Map<String, String> args) {
    }

    void delegatesTo(Map<String, Object> args) {
        String name = asString(args.get("type"));
        boolean isStatic = isStatic(args);
        boolean isDeprecated = isDeprecated(args);
        boolean asCategory = getBoolean("asCategory", args);
        boolean useNamed = getBoolean("useNamed", args);
        boolean noParens = getBoolean("noParens", args);
        @SuppressWarnings("unchecked")
        List<String> except = (List<String>) args.get("except");
        ClassNode type = this.resolver.resolve(name);
        internalDelegatesTo(type, useNamed, isStatic, asCategory, isDeprecated, except, noParens);
    }

    void delegatesTo(String className) {
        delegatesTo(this.resolver.resolve(className));
    }

    void delegatesTo(Class<?> clazz) {
        ClassNode resolved = this.resolver.resolve(clazz.getCanonicalName());
        if (resolved == VariableScope.OBJECT_CLASS_NODE && !clazz.getName().equals(Object.class.getName())) {
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
    void delegatesTo(AnnotatedNode expr) {
        internalDelegatesTo(expr, false, false, false, false, null, false);
    }

    void delegatesToUseNamedArgs(String className) {
        delegatesToUseNamedArgs(this.resolver.resolve(className));
    }

    void delegatesToUseNamedArgs(Class<?> clazz) {
        delegatesToUseNamedArgs(this.resolver.resolve(clazz.getCanonicalName()));
    }

    /**
     * Adds all members of {@code expr} type to the augmented class reference.
     */
    void delegatesToUseNamedArgs(AnnotatedNode expr) {
        internalDelegatesTo(expr, true, false, false, false, null, false);
    }

    void delegatesToCategory(String className) {
        delegatesToCategory(this.resolver.resolve(className));
    }

    void delegatesToCategory(Class<?> clazz) {
        delegatesToCategory(this.resolver.resolve(clazz.getCanonicalName()));
    }

    /**
     * Adds all members of {@code expr} type to the augmented class reference.
     */
    void delegatesToCategory(AnnotatedNode expr) {
        internalDelegatesTo(expr, false, false, true, false, null, false);
    }

    void setDelegateType(Object arg) {
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
    Map<String, ClassNode> params(MethodNode node) {
        Parameter[] parameters = node.getParameters();
        if (parameters == null || parameters.length < 1) {
            return Collections.emptyMap();
        }

        String[] names = null;

        // adapted from GroovyMethodProposal.createAllParameterNames(ICompilationUnit)
        if (parameters[0].getName().equals("arg0") || parameters[0].getName().equals("param0")) {
            ClassNode declaringClass = node.getDeclaringClass();
            if (declaringClass instanceof org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode) {
                try {
                    org.eclipse.jdt.core.IType declaringType = project.findType(declaringClass.getName());
                    if (declaringType != null && declaringType.exists()) {
                        String[] parameterTypeSignatures = new String[parameters.length];
                        for (int i = 0; i < parameters.length; i += 1) {
                            if (declaringType.isBinary()) {
                                parameterTypeSignatures[i] = ProposalUtils.createTypeSignatureStr(parameters[i].getType());
                            } else {
                                parameterTypeSignatures[i] = ProposalUtils.createUnresolvedTypeSignatureStr(parameters[i].getType());
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
    Object log(Object msg) {
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "========== " + msg);
        }
        return msg;
    }
}
