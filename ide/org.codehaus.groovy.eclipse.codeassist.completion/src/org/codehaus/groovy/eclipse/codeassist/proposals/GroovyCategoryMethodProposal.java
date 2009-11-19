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
import org.objectweb.asm.Opcodes;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 *
 */
public class GroovyCategoryMethodProposal extends AbstractGroovyProposal {
    
    
    private final MethodNode method;
    
    public GroovyCategoryMethodProposal(MethodNode method) {
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
        proposal.setParameterNames(removeFirst(createParameterNames(method)));
        proposal.setParameterTypeNames(removeFirst(createParameterTypeNames(method)));
        proposal.setReplaceRange(context.completionLocation - context.completionExpression.length(), 
                context.completionLocation - context.completionExpression.length());
        proposal.setFlags(method.getModifiers() & ~Opcodes.ACC_STATIC);  // category methods are defined as static, but should not appear as such when a proposal
        proposal.setAdditionalFlags(CompletionFlags.Default);
        char[] methodSignature = ProposalUtils.createMethodSignature(method, 1);
        proposal.setKey(methodSignature);
        proposal.setSignature(methodSignature);
        proposal.setRelevance(getRelevance(proposal.getName()));
        // FIXADE M2 decide if we should support parameter guessing proposals
        // if (isGuessArguments) 
//        proposals.add(ParameterGuessingProposal.createProposal(
//                proposal, javaContext, isGuessArguments));

        return new GroovyJavaMethodProposal(proposal,
                    javaContext);

    }

    /**
     * @return
     */
    private char[][] removeFirst(char[][] array) {
        if (array.length > 0) {
            char[][] newArray = new char[array.length-1][];
            System.arraycopy(array, 1, newArray, 0, array.length-1);
            return newArray;
        } else {
            // shouldn't happen
            return array;
        }
    }


    protected char[] completionName() {
        return (method.getName() + "()").toCharArray();
    }
    
    private char[][] createParameterNames(MethodNode method) {
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
