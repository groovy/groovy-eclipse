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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.ProposalFormattingOptions;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup.TypeAndDeclaration;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.objectweb.asm.Opcodes;

/**
 * 
 * @author andrew
 * @created Nov 17, 2010
 */
public class MethodContributionElement implements IContributionElement {

    private static final BlockStatement EMPTY_BLOCK = new BlockStatement();
    private static final ClassNode[] NO_EXCEPTIONS = new ClassNode[0];
    private static final Parameter[] NO_PARAMETERS = new Parameter[0];
    private static final ClassNode UNKNOWN_TYPE = ClassHelper.DYNAMIC_TYPE;
    
    private final String methodName;
    private final ParameterContribution[] params;
    private final String returnType;
    private final String declaringType;
    private final boolean isStatic;
    private final boolean useNamedArgs;
    
    private final String provider;
    private final String doc;
    
    
    private ClassNode cachedDeclaringType;
    private ClassNode cachedReturnType;
    private Parameter[] cachedParameters;
    private ProposalFormattingOptions options = ProposalFormattingOptions.newFromOptions();

    
    
    public MethodContributionElement(String methodName, ParameterContribution[] params, String returnType, String declaringType, boolean isStatic, String provider, String doc, boolean useNamedArgs) {
        this.methodName = methodName;
        this.params = params;
        this.returnType = returnType;
        this.isStatic = isStatic;
        this.declaringType = declaringType;
        this.useNamedArgs = useNamedArgs;
        
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
        return groovyMethodProposal;
    }
    
    private MethodNode toMethod(ClassNode declaringType, ResolverCache resolver) {
        if (cachedParameters == null) {
            if (params == null) {
                cachedParameters = NO_PARAMETERS;
            } else {
                cachedParameters = new Parameter[params.length];
                for (int i = 0; i < params.length; i++) {
                    cachedParameters[i] = params[i].toParameter(resolver);
                }
            }
            if (cachedReturnType == null) {
                if (resolver != null) {
                    cachedReturnType = resolver.resolve(returnType);
                } else {
                    cachedReturnType = VariableScope.OBJECT_CLASS_NODE;
                }
            }
        }
        MethodNode meth = new MethodNode(methodName, opcode(), cachedReturnType, cachedParameters, NO_EXCEPTIONS, EMPTY_BLOCK);
        meth.setDeclaringClass(ensureDeclaringType(declaringType, resolver));
        return meth;
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
        return isStatic ? Opcodes.ACC_STATIC : Opcodes.ACC_PUBLIC;
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
}