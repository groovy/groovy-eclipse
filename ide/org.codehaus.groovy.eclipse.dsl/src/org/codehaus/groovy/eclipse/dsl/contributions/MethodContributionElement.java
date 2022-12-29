/*
 * Copyright 2009-2022 the original author or authors.
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

import static org.codehaus.groovy.eclipse.dsl.contributions.ContributionElems.removeJavadocMarkup;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.plus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyNamedArgumentProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.ProposalFormattingOptions;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.codehaus.jdt.groovy.ast.MethodNodeWithNamedParams;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup.TypeAndDeclaration;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope;

public class MethodContributionElement implements IContributionElement {

    private static final ParameterContribution[] NO_PARAMETER_CONTRIBUTIONS = {};

    private final String methodName;
    private final String returnType;
    private final String declaringType;
    private final String provider, doc;
    private final ParameterContribution[] namedParams;
    private final ParameterContribution[] optionalParams;
    private final ParameterContribution[] positionalParams;
    private final boolean isDeprecated, isStatic, noParens;

    private final int relevanceMultiplier;

    private ClassNode cachedReturnType;
    private ClassNode cachedDeclaringType;
    private Parameter[] cachedNamedParameters;
    private Parameter[] cachedOptionalParameters;
    private Parameter[] cachedPositionalParameters;

    public MethodContributionElement(
            final String methodName,
            final ParameterContribution[] params,
            final String returnType,
            final String declaringType,
            final boolean isStatic,
            final String provider,
            final String doc,
            final boolean useNamedArgs,
            final boolean isDeprecated,
            final int relevanceMultiplier) {
        this(methodName, params, NO_PARAMETER_CONTRIBUTIONS, NO_PARAMETER_CONTRIBUTIONS, returnType,
            declaringType, isStatic, provider, doc, useNamedArgs, false, isDeprecated, relevanceMultiplier);
    }

    public MethodContributionElement(
            final String methodName,
            final ParameterContribution[] params,
            final ParameterContribution[] namedParams,
            final ParameterContribution[] optionalParams,
            final String returnType,
            final String declaringType,
            final boolean isStatic,
            final String provider,
            final String doc,
            final boolean useNamedArgs,
            final boolean noParens,
            final boolean isDeprecated,
            final int relevanceMultiplier) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.declaringType = declaringType;

        this.namedParams = !useNamedArgs ? namedParams : (namedParams == null ? params : plus(params, namedParams));
        this.optionalParams = optionalParams;
        this.positionalParams = !useNamedArgs ? params : NO_PARAMETER_CONTRIBUTIONS;

        this.noParens = noParens;
        this.isStatic = isStatic;
        this.isDeprecated = isDeprecated;
        this.relevanceMultiplier = relevanceMultiplier;
        this.provider = (provider != null ? removeJavadocMarkup(provider) : GROOVY_DSL_PROVIDER);
        this.doc = (doc != null ? doc : NO_DOC + (provider != null ? provider : GROOVY_DSL_PROVIDER));
    }

    @Override
    public TypeAndDeclaration resolve(final String name, final ClassNode declaringType, final ResolverCache resolver, final VariableScope scope) {
        if (name.equals(methodName)) {
            MethodNode decl = toMethod(declaringType, resolver);
            ClassNode  type = (decl instanceof ConstructorNode ? decl.getDeclaringClass() : decl.getReturnType());
            if (!scope.isMethodCall()) {
                return new TypeAndDeclaration(type, decl, decl.getDeclaringClass(), doc, TypeConfidence.LOOSELY_INFERRED);
            }

            // @see org.eclipse.jdt.groovy.search.SimpleTypeLookup#isTypeCompatible(ClassNode,ClassNode)
            java.util.function.BiFunction<ClassNode, ClassNode, Boolean> checker = (source, target) -> {
                Boolean result = Boolean.TRUE;
                if (!target.equals(source) && !(VariableScope.NULL_TYPE == source && !ClassHelper.isPrimitiveType(target))) {
                    // NOTE: Exact match of Closure to SAM Type creates tie for m(Closure) and m(Comparator)
                    result = !GroovyUtils.isAssignable(source, target) && !(VariableScope.CLOSURE_CLASS_NODE.equals(source) && ClassHelper.isSAMType(target))
                            ? Boolean.FALSE : null; // not an exact match
                }
                return result;
            };

            List<ClassNode> argumentTypes = scope.getMethodCallArgumentTypes();
            Parameter[] parameters = ((MethodNodeWithNamedParams) decl).getPositionalParams();
            if (!argumentTypes.isEmpty() && argumentTypes.get(0).equals(VariableScope.MAP_CLASS_NODE) &&
                (parameters.length == 0 || !parameters[0].getType().equals(VariableScope.MAP_CLASS_NODE)))
                argumentTypes = argumentTypes.subList(1, argumentTypes.size()); // TODO: has named params?

            int nArguments = argumentTypes.size(), nthParameter = parameters.length - 1;
            boolean variadic = (nthParameter != -1 && parameters[nthParameter].getType().isArray());
            Boolean compat = (variadic ? nArguments >= nthParameter : nArguments == parameters.length);
            for (int i = 0; i < nArguments && !Boolean.FALSE.equals(compat); i += 1) {
                ClassNode at = GroovyUtils.getWrapperTypeIfPrimitive(argumentTypes.get(i));
                ClassNode pt = GroovyUtils.getWrapperTypeIfPrimitive(parameters[Math.min(i, nthParameter)].getType());
                if (variadic && (i > nthParameter || (i == nthParameter && !at.isArray()))) pt = pt.getComponentType();

                Boolean partialResult = checker.apply(at, pt);
                if (partialResult == null) {
                    compat = null; // loose
                } else if (!partialResult) {
                    compat = Boolean.FALSE;
                }
            }
            if (!Boolean.FALSE.equals(compat)) {
                return new TypeAndDeclaration(type, decl, decl.getDeclaringClass(), doc);
            }
        }
        return null;
    }

    @Override
    public IGroovyProposal toProposal(final ClassNode declaringType, final ResolverCache resolver) {
        GroovyMethodProposal proposal = new GroovyMethodProposal(toMethod(declaringType.redirect(), resolver), provider);
        proposal.setProposalFormattingOptions(ProposalFormattingOptions.newFromOptions().newFromExisting(false, noParens, proposal.getMethod()));
        proposal.setRelevanceMultiplier(relevanceMultiplier);
        return proposal;
    }

    @Override
    public List<IGroovyProposal> extraProposals(final ClassNode declaringType, final ResolverCache resolver, final Expression expression) {
        // first find the arguments that are possible
        Map<String, ClassNode> availableNamedParams = collectAvailableParameters(resolver);
        removeUsedParameters(availableNamedParams, expression);
        if (availableNamedParams.isEmpty()) {
            return ProposalUtils.NO_PROPOSALS;
        }

        List<IGroovyProposal> extraProposals = new ArrayList<>(availableNamedParams.size());
        for (Map.Entry<String, ClassNode> available : availableNamedParams.entrySet()) {
            extraProposals.add(new GroovyNamedArgumentProposal(available.getKey(), available.getValue(), toMethod(declaringType.redirect(), resolver), getContributionName()));
        }
        return extraProposals;
    }

    private Map<String, ClassNode> collectAvailableParameters(final ResolverCache resolver) {
        Map<String, ClassNode> available = new HashMap<>(namedParams.length + optionalParams.length);

        for (ParameterContribution param : namedParams) {
            available.put(param.name, param.toParameter(resolver).getType());
        }

        for (ParameterContribution param : optionalParams) {
            available.put(param.name, param.toParameter(resolver).getType());
        }

        return available;
    }

    private void removeUsedParameters(final Map<String, ClassNode> available, final Expression expression) {
        if (expression instanceof MethodCall) {
            // next find out if there are any existing named args
            MethodCall call = (MethodCall) expression;
            Expression arguments = call.getArguments();
            if (arguments instanceof TupleExpression) {
                for (Expression maybeArg : ((TupleExpression) arguments).getExpressions()) {
                    if (maybeArg instanceof MapExpression) {
                        arguments = maybeArg;
                        break;
                    }
                }
            }

            // now remove the arguments that are already written
            if (arguments instanceof MapExpression) {
                // Do extra filtering to determine what parameters are still available
                MapExpression enclosingCallArgs = (MapExpression) arguments;
                for (MapEntryExpression entry : enclosingCallArgs.getMapEntryExpressions()) {
                    String paramName = entry.getKeyExpression().getText();
                    available.remove(paramName);
                }
            }
        }
    }

    private MethodNode toMethod(ClassNode declaringType, ResolverCache resolver) {
        if (cachedNamedParameters == null) {
            cachedNamedParameters = toParameters(namedParams, resolver);
            cachedOptionalParameters = toParameters(optionalParams, resolver);
            cachedPositionalParameters = toParameters(positionalParams, resolver);
        }
        MethodNode method;
        if ("<init>".equals(methodName)) {
            method = new ConstructorContribution(modifiers(), cachedNamedParameters, cachedOptionalParameters, cachedPositionalParameters);
            ClassNode type = declaringType(declaringType, resolver);
            if (type == declaringType) type = returnType(resolver);
            method.setDeclaringClass(type);
        } else {
            method = new MethodContribution(methodName, modifiers(), returnType(resolver), cachedNamedParameters, cachedOptionalParameters, cachedPositionalParameters);
            method.setDeclaringClass(declaringType(declaringType, resolver));
        }
        return method;
    }

    private static Parameter[] toParameters(ParameterContribution[] pcs, ResolverCache resolver) {
        Parameter[] ps;
        if (pcs == null) {
            ps = Parameter.EMPTY_ARRAY;
        } else {
            int n = pcs.length;
            ps = new Parameter[n];
            for (int i = 0; i < n; i += 1) {
                ps[i] = pcs[i].toParameter(resolver);
            }
        }
        return ps;
    }

    private ClassNode declaringType(ClassNode lexicalDeclaringType, ResolverCache resolver) {
        if (declaringType != null && cachedDeclaringType == null) {
            cachedDeclaringType = resolver.resolve(declaringType);
        }
        return cachedDeclaringType == null ? lexicalDeclaringType : cachedDeclaringType;
    }

    private ClassNode returnType(ResolverCache resolver) {
        if (cachedReturnType == null) {
            if (resolver != null) {
                cachedReturnType = resolver.resolve(returnType);
                if (returnType.indexOf('<') < 1) {
                    cachedReturnType = cachedReturnType.getPlainNodeReference();
                }
            } else {
                cachedReturnType = ClassHelper.dynamicType();
            }
        }
        return cachedReturnType;
    }

    private int modifiers() {
        int modifiers = Flags.AccPublic;
        if (isStatic) modifiers |= Flags.AccStatic;
        if (isDeprecated) modifiers |= Flags.AccDeprecated;

        return modifiers;
    }

    @Override
    public String description() {
        return "Method: " + declaringType + "." + methodName + "(..)";
    }

    @Override
    public String getContributionName() {
        if ("<init>".equals(methodName)) {
            return getDeclaringTypeName();
        }
        return methodName;
    }

    @Override
    public String getDeclaringTypeName() {
        return declaringType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("public ");
        if (isStatic) sb.append("static ");
        if (isDeprecated) sb.append("deprecated ");
        sb.append(returnType).append(' ');
        sb.append(declaringType);
        sb.append('.').append(methodName);
        sb.append('(').append(Arrays.toString(positionalParams)).append(')');
        sb.append(' ').append('(').append(provider).append(')');

        return sb.toString();
    }

    //--------------------------------------------------------------------------

    public static class MethodContribution extends MethodNode implements MethodNodeWithNamedParams {

        public MethodContribution(String name, int modifiers, ClassNode returnType, Parameter[] namedParams, Parameter[] optionalParams, Parameter[] positionalParams) {
            super(name, modifiers, returnType, MethodNodeWithNamedParams.concatParams(positionalParams, namedParams, optionalParams), null, null);

            this.namedParams = namedParams;
            this.optionalParams = optionalParams;
            this.positionalParams = positionalParams;
        }

        private final Parameter[] namedParams, optionalParams, positionalParams;

        @Override
        public Parameter[] getNamedParams() {
            return namedParams;
        }

        @Override
        public Parameter[] getOptionalParams() {
            return optionalParams;
        }

        @Override
        public Parameter[] getPositionalParams() {
            return positionalParams;
        }
    }

    public static class ConstructorContribution extends ConstructorNode implements MethodNodeWithNamedParams {

        public ConstructorContribution(int modifiers, Parameter[] namedParams, Parameter[] optionalParams, Parameter[] positionalParams) {
            super(modifiers, MethodNodeWithNamedParams.concatParams(positionalParams, namedParams, optionalParams), null, null);

            this.namedParams = namedParams;
            this.optionalParams = optionalParams;
            this.positionalParams = positionalParams;
        }

        private final Parameter[] namedParams, optionalParams, positionalParams;

        @Override
        public Parameter[] getNamedParams() {
            return namedParams;
        }

        @Override
        public Parameter[] getOptionalParams() {
            return optionalParams;
        }

        @Override
        public Parameter[] getPositionalParams() {
            return positionalParams;
        }
    }
}
