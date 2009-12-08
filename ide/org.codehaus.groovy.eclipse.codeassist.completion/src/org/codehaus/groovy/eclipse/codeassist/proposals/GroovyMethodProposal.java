 /*
 * Copyright 2003-2009 the original author or authors.
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

package org.codehaus.groovy.eclipse.codeassist.proposals;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.processors.GroovyCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.jdt.core.CompletionFlags;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 *
 */
public class GroovyMethodProposal extends AbstractGroovyProposal {
    
    
    private final MethodNode method;
    
    public GroovyMethodProposal(MethodNode method) {
        super();
        this.method = method;
    }

    public IJavaCompletionProposal createJavaProposal(
            ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext) {
        GroovyCompletionProposal proposal = new GroovyCompletionProposal(
                CompletionProposal.METHOD_REF, context.completionLocation);
        
        proposal.setCompletion(completionName());
        proposal.setDeclarationSignature(ProposalUtils.createTypeSignature(method.getDeclaringClass()));
        proposal.setName(method.getName().toCharArray());
        char[][] parameterTypeNames = createParameterTypeNames(method);
        proposal.setParameterTypeNames(parameterTypeNames);
        proposal.setParameterNames(createParameterNames(method, parameterTypeNames));
        proposal.setReplaceRange(context.completionLocation - context.completionExpression.length(), context.completionLocation - context.completionExpression.length());
        proposal.setFlags(method.getModifiers());
        proposal.setAdditionalFlags(CompletionFlags.Default);
        char[] methodSignature = ProposalUtils.createMethodSignature(method);
        proposal.setKey(methodSignature);
        proposal.setSignature(methodSignature);
        proposal.setRelevance(getRelevance(proposal.getName()));
        // FIXADE RC1 decide if we should support parameter guessing proposals
        // if (isGuessArguments) 
//        proposals.add(ParameterGuessingProposal.createProposal(
//                proposal, javaContext, isGuessArguments));

        return new GroovyJavaMethodProposal(proposal,
                    javaContext);

    }


    protected char[] completionName() {
        return (method.getName() + "()").toCharArray();
    }
    
    // FIXADE RC1 parameter names not in JDTClassNodes
    private char[][] createParameterNames(MethodNode method, char[][] parameterTypeNames) {
//        ClassNode clazz = method.getDeclaringClass();
//        if (clazz instanceof JDTClassNode) {
//            ReferenceBinding binding = ((JDTClassNode) clazz).getJdtBinding();
//            MethodBinding[] methodBindings = binding.methods();
//            for (MethodBinding methodBinding : methodBindings) {
//                if (parameterTypeNames.length == method.getParameters().length) {
//                    if (CharOperation.equals(methodBinding.selector, method.getName().toCharArray())) {
//                        boolean found = true;
//                        inner:
//                        for (int i = 0; i < parameterTypeNames.length; i++) {
//                            if (! CharOperation.equals(parameterTypeNames[i], methodBinding.parameters[i].signature())) {
//                                found = false;
//                                break inner;
//                            }
//                        }
//                        if (found) {
//                            methodBinding.parameters
//                        }
//                    }
//                }
//            }
//        }
        
        Parameter[] params = method.getParameters();
        char[][] paramNames = new char[params.length][];
        for (int i = 0; i < params.length; i++) {
            paramNames[i] = params[i].getName().toCharArray();
        }
        return paramNames;
    }

    private char[][] createParameterTypeNames(MethodNode method) {
        char[][] typeNames = new char[method.getParameters().length][];
        int i = 0;
        for (Parameter param : method.getParameters()) {
            typeNames[i] = ProposalUtils.createSimpleTypeName(param.getType());
            i++;
        }
        return typeNames;
    }

}
