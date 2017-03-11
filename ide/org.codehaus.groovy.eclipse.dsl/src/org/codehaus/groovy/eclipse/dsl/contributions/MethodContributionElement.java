/*
 * Copyright 2009-2017 the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import groovyjarjarasm.asm.Opcodes;
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
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup.TypeAndDeclaration;
import org.eclipse.jdt.groovy.search.VariableScope;

public class MethodContributionElement implements IContributionElement {

    private static final BlockStatement EMPTY_BLOCK = new BlockStatement();
    private static final ClassNode[] NO_EXCEPTIONS = ClassNode.EMPTY_ARRAY;
    private static final ParameterContribution[] NO_PARAMETER_CONTRIBUTION = new ParameterContribution[0];
    private static final ClassNode UNKNOWN_TYPE = ClassHelper.DYNAMIC_TYPE;

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
    private ProposalFormattingOptions options = ProposalFormattingOptions.newFromOptions();
    private final int relevanceMultiplier;
    private final boolean isDeprecated;
    private final boolean noParens;

    public MethodContributionElement(String methodName, ParameterContribution[] params, String returnType, String declaringType, boolean isStatic, String provider, String doc, boolean useNamedArgs, boolean isDeprecated, int relevanceMultiplier) {
        this(methodName, params, NO_PARAMETER_CONTRIBUTION, NO_PARAMETER_CONTRIBUTION, returnType, declaringType, isStatic, provider, doc, useNamedArgs, false, isDeprecated, relevanceMultiplier);
    }

    public MethodContributionElement(String methodName, ParameterContribution[] params, ParameterContribution[] namedParams,
            ParameterContribution[] optionalParams, String returnType, String declaringType, boolean isStatic, String provider,
            String doc, boolean useNamedArgs, boolean noParens, boolean isDeprecated, int relevanceMultiplier) {
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

        this.provider = provider == null ? GROOVY_DSL_PROVIDER : provider;
        this.doc = doc == null ? NO_DOC + this.provider : doc;

    }

    public TypeAndDeclaration lookupType(String name, ClassNode declaringType, ResolverCache resolver) {
        if (name.equals(methodName))
            return new TypeAndDeclaration(ensureReturnType(resolver), toMethod(declaringType, resolver),
                    ensureDeclaringType(declaringType, resolver), doc);
        else
            return null;
    }

    public IGroovyProposal toProposal(ClassNode declaringType, ResolverCache resolver) {
        GroovyMethodProposal groovyMethodProposal = new GroovyMethodProposal(toMethod(declaringType.redirect(), resolver), provider, options);
        groovyMethodProposal.setUseNamedArguments(useNamedArgs);
        groovyMethodProposal.setNoParens(noParens);
        groovyMethodProposal.setRelevanceMultiplier(relevanceMultiplier);
        return groovyMethodProposal;
    }

    public List<IGroovyProposal> extraProposals(ClassNode declaringType, ResolverCache resolver, Expression expression) {
        // first find the arguments that are possible
        Map<String, ClassNode> availableParams = findAvailableParameters(resolver);

        if (availableParams.isEmpty()) {
            return ProposalUtils.NO_PROPOSALS;
        }

        removeUsedParameters(expression, availableParams);

        List<IGroovyProposal> extraProposals = new ArrayList<IGroovyProposal>(availableParams.size());
        for (Entry<String, ClassNode> available : availableParams.entrySet()) {
            extraProposals.add(new GroovyNamedArgumentProposal(available.getKey(), available.getValue(), toMethod(declaringType.redirect(), resolver), provider));
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
                for (MapEntryExpression entry : enclosingCallArgs .getMapEntryExpressions()) {
                    String paramName = entry.getKeyExpression().getText();
                    availableParams.remove(paramName);
                }
            }
        }
    }

    /**
     * @param resolver
     * @return
     */
    private Map<String, ClassNode> findAvailableParameters(ResolverCache resolver) {
        Map<String, ClassNode> available = new HashMap<String, ClassNode>(params.length);
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
            cachedOptionalParameters = initParams(optionalParams, resolver);
            cachedNamedParameters = initParams(namedParams, resolver);
            if (cachedReturnType == null) {
                if (resolver != null) {
                    cachedReturnType = resolver.resolve(returnType);
                } else {
                    cachedReturnType = VariableScope.OBJECT_CLASS_NODE;
                }
            }
        }
        MethodNode meth = new NamedArgsMethodNode(methodName, opcode(), cachedReturnType, cachedRegularParameters, cachedNamedParameters, cachedOptionalParameters, NO_EXCEPTIONS, EMPTY_BLOCK);
        meth.setDeclaringClass(ensureDeclaringType(declaringType, resolver));
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

    protected ClassNode ensureReturnType(ResolverCache resolver) {
        if (cachedReturnType == null) {
            cachedReturnType = resolver.resolve(returnType);
        }
        return cachedReturnType == null ? UNKNOWN_TYPE : cachedReturnType;
    }

    protected ClassNode ensureDeclaringType(ClassNode lexicalDeclaringType, ResolverCache resolver) {
        if (declaringType != null && cachedDeclaringType == null) {
            cachedDeclaringType = resolver.resolve(declaringType);
        }
        return cachedDeclaringType == null ? lexicalDeclaringType : cachedDeclaringType;
    }

    protected int opcode() {
        int modifiers = isStatic ? Opcodes.ACC_STATIC : Opcodes.ACC_PUBLIC;
        modifiers |= isDeprecated ? Opcodes.ACC_DEPRECATED : 0;
        return modifiers;
    }

    public String contributionName() {
        return methodName;
    }

    public String description() {
        return "Method: " + declaringType + "." + methodName + "(..)";
    }

    public String getDeclaringTypeName() {
        return declaringType;
    }

    @Override
    public String toString() {
        return "public " + (isStatic ? "static " : "") + (isDeprecated ? "deprecated " : "")
                + (useNamedArgs ? "useNamedArgs " : "") + returnType + " " + declaringType + "." + methodName + "("
                + Arrays.toString(params) + ") (" + provider + ")";
    }
}