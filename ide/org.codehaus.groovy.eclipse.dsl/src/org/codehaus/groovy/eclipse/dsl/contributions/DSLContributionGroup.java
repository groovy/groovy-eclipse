/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.contributions;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.jdt.groovy.search.GenericsMapper;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * A contribution group will determine the set of contribution elements (eg-
 * extra methods, properties, templates, etc) that are added to a particular type
 * when the attached pointcut matches.
 * 
 * @author andrew
 * @created Nov 17, 2010
 */
public class DSLContributionGroup extends ContributionGroup {
    private static final ParameterContribution[] NO_PARAMS = new ParameterContribution[0];

    private static final String NO_TYPE = "java.lang.Object";

    private static final String NO_NAME = "";

    /**
     * The closure that comes from the DSLD script.
     * It's delegate is set to <code>this</code>.
     */
    @SuppressWarnings("rawtypes")
    private final Closure contributionClosure;


    private VariableScope scope;
    
    // provider that is set for the entire contribution group
    // individual contributions can override
    private String provider = null;
    
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
    public List<IContributionElement> getContributions(GroovyDSLDContext pattern, BindingSet matches) {
        // uh oh...needs to be synchronized, or can we make this class stateless?
        synchronized (this) {
            List<IContributionElement> result;
            try {
                this.contributions = new ArrayList<IContributionElement>();
                this.scope = pattern.getCurrentScope();
                this.resolver = pattern.getResolverCache(); 
                this.bindings = matches.getBindings();
                this.currentType = pattern.getCurrentType();
                this.wormhole = scope.getWormhole();
                this.staticScope = pattern.isStatic();
                this.isPrimaryExpression = pattern.isPrimaryNode();
                contributionClosure.call();
            } catch (Exception e) {
                GroovyLogManager.manager.logException(TraceCategory.DSL, e);
            } finally {
                result = contributions;
                // must set targetType here in case someone changed the delegate on us
                pattern.setTargetType(currentType);
                this.contributions = null;
                this.scope = null;
                this.resolver = null;
                this.bindings = null;
                this.currentType = null;
                this.wormhole = null;
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
    
    void setDelegateType(Object arg) {
        ClassNode delegate = asClassNode(arg);
        if (delegate != null) {
            // also need to set targetType, but only if primary expression
            scope.addVariable("delegate", delegate, VariableScope.CLOSURE_CLASS);
            scope.addVariable("getDelegate", delegate, VariableScope.CLOSURE_CLASS);
            contributions.add(new EmptyContributionElement(currentType));
            
            if (isPrimaryExpression) {
                // must save for later
                currentType = delegate;
            }
        }
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

    /**
     * Called by closure to add a method
     * @param args
     */
    void method(Map<String, Object> args) {
        String name = asString(args.get("name"));
        
        Object value = args.get("type");
        String returnType = value == null ? "java.lang.Object" : asString(value);
        
        value = args.get("declaringType");
        String declaringType = value == null ? getTypeName(currentType) : asString(value);
        
        value = args.get("provider");
        String provider = value == null ? this.provider : asString(value); // might be null
        value = args.get("doc");
        String doc = value == null ? null : asString(value); // might be null

        boolean useNamedArgs = asBoolean(args.get("useNamedArgs"));
        boolean noParens = asBoolean(args.get("noParens"));
        
        ParameterContribution[] params = extractParams(args, "params");
        ParameterContribution[] namedParams = extractParams(args, "namedParams");
        ParameterContribution[] optionalParams = extractParams(args, "optionalParams");

        boolean isStatic = isStatic(args);
        boolean isDeprecated = isDeprecated(args);
        if (!staticScope || (staticScope && isStatic)) {
            contributions.add(new MethodContributionElement(name == null ? NO_NAME : name, params, namedParams, optionalParams, returnType == null ? NO_TYPE
                    : returnType, declaringType, isStatic, provider == null ? this.provider : provider, doc, useNamedArgs, noParens, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
        }
    }

    private ParameterContribution[] extractParams(Map<String, Object> args, String paramKind) {
        Object value;
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

    /**
     * @param object
     * @return
     */
    private boolean asBoolean(Object object) {
        if (object == null) {
            return false;
        } 
        if (object instanceof Boolean) {
            return (Boolean) object;
        }
        String str = object.toString();
        return str.equalsIgnoreCase("true") ||
               str.equalsIgnoreCase("yes");
    }

    
    
    /**
     * Called by closure to add a property
     * @param args
     */
    void property(Map<String, Object> args) {
        String name = asString(args.get("name"));
        
        Object value = args.get("type");
        String type = value == null ? NO_TYPE : asString(value);
        
        value = args.get("declaringType");
        String declaringType = value == null ? getTypeName(currentType) : asString(value);
        
        value = args.get("provider");
        String provider = value == null ? this.provider : asString(value); // might be null
        String doc = asString(args.get("doc")); // might be null
        
        boolean isStatic = isStatic(args);
        boolean isDeprecated = isDeprecated(args);
        if (!staticScope || (staticScope && isStatic)) {
            contributions.add(new PropertyContributionElement(name == null ? NO_NAME : name, type,
                    declaringType, isStatic, provider, doc, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
        }
    }

    /**
     * stub...will be used later to add templates
     * @param args
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
     * invoked by the closure
     * takes an expression and adds all members of its type to the augmented
     * class reference.
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
     * invoked by the closure
     * takes an expression and adds all members of its type to the augmented
     * class reference.
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
     * invoked by the closure
     * takes an expression and adds all members of its type to the augmented
     * class reference.
     */
    void delegatesToCategory(AnnotatedNode expr) {
        internalDelegatesTo(expr, false, false, true, false, null, false);
    }

    /**
     * Convert a {@link ClassNode} into a string that includes type parameters
     * @param clazz
     * @return
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
            sb.replace(sb.length()-1, sb.length(), ">");
        }
        return sb.toString();
    }

    private void internalDelegatesTo(AnnotatedNode expr, boolean useNamedArgs, boolean isStatic, boolean asCategory, boolean isDeprecated, List<String> exceptions, boolean noParens) {
        if (staticScope && !isStatic && !currentType.getName().equals(VariableScope.CLASS_CLASS_NODE)) {
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
                GroovyLogManager.manager.log(TraceCategory.DSL, 
                        "Cannot invoke delegatesTo() on an invalid object: " + expr);
            }
            return;
        }
        if (!type.getName().equals(Object.class.getName())) {
            // use this to resolve parameterized types
            GenericsMapper mapper = GenericsMapper.gatherGenerics(type, type.redirect());
            
            // naked variants of getter and setter methods must be added at the end
            // FIXADE why???
            List<IContributionElement> accessorContribs = new ArrayList<IContributionElement>(1);
            for (MethodNode method : type.getMethods()) {
                if ((exceptions == null || !exceptions.contains(method.getName())) && !(method instanceof ConstructorNode) && ! method.getName().contains("$")) {
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
    private void delegateToNonCategoryMethod(boolean useNamedArgs, boolean isStatic, ClassNode type, MethodNode method, ClassNode resolvedReturnType, boolean isDeprecated, 
            List<IContributionElement> accessorContribs, boolean noParens) {
        String name = method.getName();
        
        contributions.add(new MethodContributionElement(name, toParameterContribution(method.getParameters()), NO_PARAMS,
                NO_PARAMS, getTypeName(resolvedReturnType), getTypeName(type), (method.isStatic() || isStatic), provider, null,
                useNamedArgs, noParens, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
        
        // also add the associated property if applicable
        String prefix;
        if ((prefix = isAccessor(method, name, false)) != null) {
            accessorContribs.add(new PropertyContributionElement(Character.toLowerCase(name.charAt(prefix.length())) + name.substring(prefix.length()+1), getTypeName(resolvedReturnType), 
                    getTypeName(method.getDeclaringClass()), (method.isStatic() || isStatic), provider, null, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
        }
    }

    private void delegateToCategoryMethod(boolean useNamedArgs, boolean isStatic, ClassNode type, MethodNode method, ClassNode resolvedReturnType, boolean isDeprecated, 
            List<IContributionElement> accessorContribs, boolean noParens) {
        String name = method.getName();
        if (method.getParameters() != null && method.getParameters().length > 0) {
            ClassNode firstType = method.getParameters()[0].getType();
            if ((firstType.isInterface() && currentType.implementsInterface(firstType)) ||
                    currentType.isDerivedFrom(firstType)) {
                contributions.add(new MethodContributionElement(name, toParameterContributionRemoveFirst(method.getParameters()),
                        NO_PARAMS, NO_PARAMS, getTypeName(resolvedReturnType), getTypeName(type), isStatic, provider, null,
                        useNamedArgs, noParens, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
                // also add the associated property if applicable
                String prefix;
                if ((prefix = isAccessor(method, name, true)) != null) {
                    accessorContribs.add(new PropertyContributionElement(Character.toLowerCase(name.charAt(prefix.length())) + name.substring(prefix.length()+1), getTypeName(resolvedReturnType), 
                            getTypeName(method.getDeclaringClass()), (method.isStatic() || isStatic), provider, null, isDeprecated, DEFAULT_RELEVANCE_MULTIPLIER));
                }
            }
        }
    }

    private String isAccessor(MethodNode method, String name, boolean isCategory) {
        int paramCount = isCategory ? 1 : 0;
        if (method.getParameters() == null || method.getParameters().length == paramCount) {
            if (name.startsWith("get") && name.length() > 3) {
                return "get";
            } else if (name.startsWith("is") && name.length() > 2) {
                return "is";
            }
        }
        return null;
    }

    private ParameterContribution[] toParameterContribution(Parameter[] params) {
        if (params != null) {
            ParameterContribution[] contribs = new ParameterContribution[params.length];
            for (int i = 0; i < contribs.length; i++) {
                contribs[i] = new ParameterContribution(params[i]);
            }
            return contribs;
        } else {
            return new ParameterContribution[0];
        }
    }

    private ParameterContribution[] toParameterContributionRemoveFirst(Parameter[] params) {
        if (params != null) {
            ParameterContribution[] contribs = new ParameterContribution[params.length-1];
            for (int i = 1; i < params.length; i++) {
                contribs[i-1] = new ParameterContribution(params[i]);
            }
            return contribs;
        } else {
            return new ParameterContribution[0];
        }
    }
    
    
    void provider(Object args) {
        provider = args == null ? null : asString(args);
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
    
    private boolean getBoolean(String name, Map<?,?> args) {
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
     * @param value
     * @return
     */
    private String asString(Object value) {
        if (value == null) {
//            return "null";
            return null;
        } else if (value instanceof String) {
            return (String) value;
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
        } else if (value instanceof Class) {
            return ((Class<?>) value).getName();
        } else {
            return value.toString();
        }
    }
    
    /**
     * Logs a message to the groovy console log if open
     * @param msg
     */
    Object log(Object msg) {
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.DSL, "========== " + msg);
        }
        return msg;
    }
}
