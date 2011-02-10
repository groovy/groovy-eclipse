/*
 * Copyright 2003-2010 the original author or authors.
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

import groovy.lang.Closure;
import groovy.lang.MissingPropertyException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.dsl.contexts.ContextPattern;
import org.codehaus.groovy.eclipse.dsl.script.IContextQueryResult;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.groovy.search.VariableScope.CallAndType;

/**
 * A contribution group will determine the set of contribution elements (ie-
 * extra methods
 * and properties) that are added to a particular depending on the context.
 * 
 * @author andrew
 * @created Nov 17, 2010
 */
public class ContributionGroup implements IContributionGroup {
    private static final ParameterContribution[] NO_PARAMS = new ParameterContribution[0];

    private static final String NO_TYPE = "java.lang.Object";

    private static final String NO_NAME = "";

    /**
     * The closure that comes from the GDSL script.
     * It's delegate is set to <code>this</code>.
     */
    private final Closure contributionClosure;

    private List<IContributionElement> contributions;

    private VariableScope scope;

    /**
     * this is a property that is available in the closure
     */
    private ClassNode classType;

    /**
     * access to the result of the context query
     */
    private PsiClassWrapper psiClass;

    public ContributionGroup(Closure contributionClosure) {
        this.contributionClosure = contributionClosure;
        
        if (contributionClosure != null) {
            contributionClosure.setDelegate(this);
            contributionClosure.setResolveStrategy(Closure.DELEGATE_FIRST);
        }
    }

    /**
     * This is the main entry point into the contribution
     */
    public List<IContributionElement> getContributions(ContextPattern pattern, ClassNode declaringType, VariableScope scope,
            IContextQueryResult<?> matches) {
        
        // uh oh...needs to be synchronized, or can we make this class stateless?
        // add the fields to the binding?
        synchronized (this) {
            this.contributions = new ArrayList<IContributionElement>();
            this.scope = scope;
            this.classType = declaringType;
            this.psiClass = new PsiClassWrapper(matches);
            contributionClosure.call(Arrays.asList(pattern, declaringType));
            List<IContributionElement> result = contributions;
            this.contributions = null;
            this.scope = null;
            this.classType = null;
            this.psiClass = null;
            return result;
        }
    }

    // called by the closure
    void method(Map<Object, Object> args) {
        String name = (String) args.get("name");
        String returnType = (String) args.get("type");
        String declaringType = (String) args.get("declaringType"); // might be
                                                                   // null
        String provider = (String) args.get("provider"); // might be null
        String doc = (String) args.get("doc"); // might be null

        Map<String, String> paramsMap = (Map<String, String>) args.get("params");
        ParameterContribution[] params;
        if (paramsMap != null) {
            params = new ParameterContribution[paramsMap.size()];
            int i = 0;
            for (Entry<String, String> entry : paramsMap.entrySet()) {
                params[i++] = new ParameterContribution(entry.getKey(), entry.getValue());
            }
        } else {
            params = NO_PARAMS;
        }

        boolean isStatic = isStatic(args);

        if (!scope.isStatic() || (scope.isStatic() && isStatic)) {
            contributions.add(new MethodContributionElement(name == null ? NO_NAME : name, params, returnType == null ? NO_TYPE
                    : returnType, declaringType, isStatic, provider == null ? getProvider() : provider, doc));
        }
    }

    // called by the closure
    void property(Map<String, String> args) {
        String name = args.get("name");
        String type = args.get("type");
        String declaringType = args.get("declaringType"); // might be null
        String provider = args.get("provider"); // might be null
        String doc = args.get("doc"); // might be null
        boolean isStatic = isStatic(args);
        if (!scope.isStatic() || (scope.isStatic() && isStatic)) {
            contributions.add(new PropertyContributionElement(name == null ? NO_NAME : name, type == null ? NO_TYPE : type,
                    declaringType, isStatic, provider == null ? getProvider() : provider, doc));
        }
    }

    /**
     * returns a method invocation expression of a given context with a name,
     * matching methodName or null otherwise.
     * called by the closure
     */
    CallAndType enclosingCall(String methodNamePattern) {
        Assert.isNotNull(scope, "Scope should not be null");
        List<CallAndType> exprs = scope.getAllEnclosingMethodCallExpressions();
        for (CallAndType expr : exprs) {
            if (expr.call.getMethodAsString().matches(methodNamePattern)) {
                return expr;
            }
        }
        return null;
    }


    CallAndType enclosingCallType(String typeNamePattern) {
        Assert.isNotNull(scope, "Scope should not be null");
        List<CallAndType> exprs = scope.getAllEnclosingMethodCallExpressions();
        for (CallAndType expr : exprs) {
            if (expr.declaringType.getName().matches(typeNamePattern)) {
                return expr;
            }
        }
        return null;
    }


    /**
     * invoked by the closure
     * takes an expression and adds all members of its type to the augmented
     * class reference.
     */
    void delegatesTo(AnnotatedNode expr) {
        ClassNode type;
        if (expr instanceof Expression) {
            type = queryType((Expression) expr);
        } else if (expr instanceof ClassNode) {
            type = (ClassNode) expr;
        } else if (expr instanceof FieldNode) {
            type = ((FieldNode) expr).getType();
        } else if (expr instanceof MethodNode) {
            type = ((MethodNode) expr).getReturnType();
        } else {
            // invalid
            return;
        }
        if (!type.getName().equals(Object.class.getName())) {
            for (MethodNode method : type.getMethods()) {
                if (!(method instanceof ConstructorNode)) {
                    contributions.add(new MethodContributionElement(method.getName(), toParameterContribution(method
                            .getParameters()), method.getReturnType().getName(), type.getName(), method.isStatic(), getProvider(),
                            null));
                }
            }
        }
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

    /**
     * Invoked by the closure to determing the type of the expression if know.
     * Only Expressions that have already been visited will have a type. Returns
     * {@link ClassHelper#DYNAMIC_TYPE} if nothing is found.
     */
    ClassNode queryType(Expression expr) {
        return scope.queryExpressionType(expr);
    }

    /**
     * Required getter used by the closure to access the classType field
     * as a property
     * 
     * @return
     */
    public ClassNode getClassType() {
        return classType;
    }

    /**
     * required getter used by the closure for access to the psiClass field
     * as a property
     * 
     * @return
     */
    public PsiClassWrapper getPsiClass() {
        return psiClass;
    }

    public String getProvider() {
        String result = null;
        try {
            // prevent recursion
            contributionClosure.setResolveStrategy(Closure.OWNER_ONLY);
            result = (String) contributionClosure.getProperty("provider");
            return result;
        } catch (MissingPropertyException e) {
            // ignore
            return "Inferencing DSL";
        } finally {
            contributionClosure.setResolveStrategy(Closure.DELEGATE_FIRST);
        }
    }

    // alternative way to add a method contribution
    public void addMethodContribution(String name, ParameterContribution[] params, String returnType, String declaringType,
            boolean isStatic) {
        contributions.add(new MethodContributionElement(name, params, returnType, declaringType, isStatic, getProvider(), null));
    }

    // alternative way to add a property contribution
    public void addPropertyContribution(String name, String type, String declaringType, boolean isStatic) {
        contributions.add(new PropertyContributionElement(name, type, declaringType, isStatic, getProvider(), null));
    }

    /**
     * @param args map passed in from the call to method or property
     * @return true iff the static argument is passed in.
     */
    private boolean isStatic(Map<?, ?> args) {
        Object maybeStatic = args.get("isStatic");
        if (maybeStatic == null) {
            return false;
        } else {
            return Boolean.getBoolean(maybeStatic.toString());
        }
    }
}
