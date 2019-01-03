/*
 * Copyright 2009-2019 the original author or authors.
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

import static org.codehaus.groovy.eclipse.dsl.contributions.ContributionElems.removeJavadocMarkup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.completions.NamedArgsMethodNode;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyNamedArgumentProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.ProposalFormattingOptions;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup.TypeAndDeclaration;

public class MethodContributionElement implements IContributionElement {

    private static final BlockStatement EMPTY_BLOCK = new BlockStatement();
    private static final ClassNode[] NO_EXCEPTIONS = ClassNode.EMPTY_ARRAY;
    private static final ParameterContribution[] NO_PARAMETER_CONTRIBUTION = {};

    private final String methodName;
    private final ParameterContribution[] params;
    private final ParameterContribution[] namedParams;
    private final ParameterContribution[] optionalParams;
    private final String returnType;
    private final String declaringType;
    private final boolean isStatic;
    private final boolean useNamedArgs;

    private final String provider;
    private final String doc;

    private ClassNode cachedDeclaringType;
    private ClassNode cachedReturnType;
    private Parameter[] cachedRegularParameters;
    private Parameter[] cachedNamedParameters;
    private Parameter[] cachedOptionalParameters;
    private final int relevanceMultiplier;
    private final boolean isDeprecated;
    private final boolean noParens;

    public MethodContributionElement(
            String methodName,
            ParameterContribution[] params,
            String returnType,
            String declaringType,
            boolean isStatic,
            String provider,
            String doc,
            boolean useNamedArgs,
            boolean isDeprecated,
            int relevanceMultiplier) {
        this(methodName, params, NO_PARAMETER_CONTRIBUTION, NO_PARAMETER_CONTRIBUTION, returnType, declaringType, isStatic, provider, doc, useNamedArgs, false, isDeprecated, relevanceMultiplier);
    }

    public MethodContributionElement(
            String methodName,
            ParameterContribution[] params,
            ParameterContribution[] namedParams,
            ParameterContribution[] optionalParams,
            String returnType,
            String declaringType,
            boolean isStatic,
            String provider,
            String doc,
            boolean useNamedArgs,
            boolean noParens,
            boolean isDeprecated,
            int relevanceMultiplier) {
        this.methodName = methodName;
        this.params = params;
        this.namedParams = namedParams;
        this.optionalParams = optionalParams;
        this.returnType = returnType;
        this.isStatic = isStatic;
        this.declaringType = declaringType;
        this.useNamedArgs = useNamedArgs;
        this.noParens = noParens;
        this.isDeprecated = isDeprecated;
        this.relevanceMultiplier = relevanceMultiplier;
        this.provider = (provider != null ? removeJavadocMarkup(provider) : GROOVY_DSL_PROVIDER);
        this.doc = (doc != null ? doc : NO_DOC + (provider != null ? provider : GROOVY_DSL_PROVIDER));
    }

    @Override
    public TypeAndDeclaration lookupType(String name, ClassNode declaringType, ResolverCache resolver) {
        if (name.equals(methodName)) {
            return new TypeAndDeclaration(returnType(resolver), toMethod(declaringType, resolver), declaringType(declaringType, resolver), doc);
        }
        return null;
    }

    @Override
    public IGroovyProposal toProposal(ClassNode declaringType, ResolverCache resolver) {
        GroovyMethodProposal proposal = new GroovyMethodProposal(toMethod(declaringType.redirect(), resolver), provider);
        proposal.setProposalFormattingOptions(ProposalFormattingOptions.newFromOptions().newFromExisting(useNamedArgs, noParens, proposal.getMethod()));
        proposal.setRelevanceMultiplier(relevanceMultiplier);
        return proposal;
    }

    @Override
    public List<IGroovyProposal> extraProposals(ClassNode declaringType, ResolverCache resolver, Expression expression) {
        // first find the arguments that are possible
        Map<String, ClassNode> availableParams = findAvailableParameters(resolver);

        if (availableParams.isEmpty()) {
            return ProposalUtils.NO_PROPOSALS;
        }

        removeUsedParameters(expression, availableParams);

        List<IGroovyProposal> extraProposals = new ArrayList<>(availableParams.size());
        for (Entry<String, ClassNode> available : availableParams.entrySet()) {
            extraProposals.add(new GroovyNamedArgumentProposal(available.getKey(), available.getValue(), toMethod(declaringType.redirect(), resolver), methodName));
        }
        return extraProposals;
    }

    private void removeUsedParameters(Expression expression, Map<String, ClassNode> availableParams) {
        if (expression instanceof MethodCallExpression) {
            // next find out if there are any existing named args
            MethodCallExpression call = (MethodCallExpression) expression;
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
                    availableParams.remove(paramName);
                }
            }
        }
    }

    private Map<String, ClassNode> findAvailableParameters(ResolverCache resolver) {
        Map<String, ClassNode> available = new HashMap<>(params.length);
        if (useNamedArgs) {
            for (ParameterContribution param : params) {
                available.put(param.name, param.toParameter(resolver).getType());
            }
        }

        for (ParameterContribution param : namedParams) {
            available.put(param.name, param.toParameter(resolver).getType());
        }

        for (ParameterContribution param : optionalParams) {
            available.put(param.name, param.toParameter(resolver).getType());
        }

        return available;
    }

    private MethodNode toMethod(ClassNode declaringType, ResolverCache resolver) {
        if (cachedRegularParameters == null) {
            cachedRegularParameters = initParams(params, resolver);
            cachedNamedParameters = initParams(namedParams, resolver);
            cachedOptionalParameters = initParams(optionalParams, resolver);
        }
        MethodNode meth = new NamedArgsMethodNode(methodName, modifiers(), returnType(resolver), cachedRegularParameters, cachedNamedParameters, cachedOptionalParameters, NO_EXCEPTIONS, EMPTY_BLOCK);
        meth.setDeclaringClass(declaringType(declaringType, resolver));
        return meth;
    }

    private Parameter[] initParams(ParameterContribution[] pcs, ResolverCache resolver) {
        Parameter[] ps;
        if (pcs == null) {
            ps = Parameter.EMPTY_ARRAY;
        } else {
            ps = new Parameter[pcs.length];
            for (int i = 0; i < pcs.length; i++) {
                ps[i] = pcs[i].toParameter(resolver);
            }
        }
        return ps;
    }

    protected ClassNode returnType(ResolverCache resolver) {
        if (cachedReturnType == null) {
            if (resolver != null) {
                cachedReturnType = resolver.resolve(returnType);
                if (returnType.indexOf('<') < 1) {
                    cachedReturnType = cachedReturnType.getPlainNodeReference();
                }
            } else {
                cachedReturnType = ClassHelper.DYNAMIC_TYPE;
            }
        }
        return cachedReturnType;
    }

    protected ClassNode declaringType(ClassNode lexicalDeclaringType, ResolverCache resolver) {
        if (declaringType != null && cachedDeclaringType == null) {
            cachedDeclaringType = resolver.resolve(declaringType);
        }
        return cachedDeclaringType == null ? lexicalDeclaringType : cachedDeclaringType;
    }

    protected int modifiers() {
        int modifiers = Flags.AccPublic;
        if (isStatic) modifiers |= Flags.AccStatic;
        if (isDeprecated) modifiers |= Flags.AccDeprecated;

        return modifiers;
    }

    @Override
    public String contributionName() {
        return methodName;
    }

    @Override
    public String description() {
        return "Method: " + declaringType + "." + methodName + "(..)";
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
        if (useNamedArgs) sb.append("useNamedArgs ");
        sb.append(returnType).append(' ');
        sb.append(declaringType);
        sb.append('.').append(methodName);
        sb.append('(').append(Arrays.toString(params)).append(')');
        sb.append(' ').append('(').append(provider).append(')');

        return sb.toString();
    }
}
