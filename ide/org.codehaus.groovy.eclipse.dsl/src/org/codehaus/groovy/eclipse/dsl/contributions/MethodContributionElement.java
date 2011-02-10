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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyMethodProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
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
    
    private final String provider;
    private final String doc;  // don't know how to use yet
    
    
    private ClassNode cachedDeclaringType;
    private ClassNode cachedReturnType;
    private Parameter[] cachedParameters;
    
    
    public MethodContributionElement(String methodName, ParameterContribution[] params, String returnType, String declaringType, boolean isStatic, String provider, String doc) {
        this.methodName = methodName;
        this.params = params;
        this.returnType = returnType;
        this.isStatic = isStatic;
        this.declaringType = declaringType;
        
        this.provider = provider == null ? GROOVY_DSL_PROVIDER : provider;
        this.doc = doc == null ? NO_DOC + provider : doc;
    }
    
    public TypeAndDeclaration lookupType(String name, ClassNode declaringType, ResolverCache resolver) {
        return name.equals(methodName) ? new TypeAndDeclaration(ensureReturnType(resolver), toMethod(declaringType, resolver),
                ensureDeclaringType(declaringType, resolver), doc) : null;
    }

    public IGroovyProposal toProposal(ClassNode declaringType, ResolverCache resolver) {
        return new GroovyMethodProposal(toMethod(declaringType.redirect(), resolver), provider);
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
}