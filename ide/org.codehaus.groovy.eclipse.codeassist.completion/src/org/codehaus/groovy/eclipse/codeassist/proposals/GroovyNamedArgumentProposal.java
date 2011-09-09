/*
 * Copyright 2011 the original author or authors.
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


import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.codeassist.requestor.MethodInfoContentAssistContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;

/**
 * A proposal that creates a named argument
 *
 * @author andrew
 * @created Sep 2, 2011
 */
public class GroovyNamedArgumentProposal extends AbstractGroovyProposal {

    private final String paramName;

    private final ClassNode paramType;

    private final MethodNode ownerMethod;

    private final String contributor;

    private ProposalFormattingOptions options;

    @Override
    public AnnotatedNode getAssociatedNode() {
        return ownerMethod;
    }

    public GroovyNamedArgumentProposal(String paramName, ClassNode paramType, MethodNode ownerMethod, String contributor) {
        this.paramName = paramName;
        this.paramType = paramType;
        this.ownerMethod = ownerMethod;
        this.contributor = contributor;
        setRelevanceMultiplier(100);
    }

    public IJavaCompletionProposal createJavaProposal(ContentAssistContext context, JavaContentAssistInvocationContext javaContext) {
        if (context.location != ContentAssistLocation.METHOD_CONTEXT) {
            return null;
        }
        MethodInfoContentAssistContext methodContext = (MethodInfoContentAssistContext) context;
        int startIndex = methodContext.completionLocation - methodContext.completionExpression.length();
        int length = methodContext.completionEnd - startIndex;
        return new NamedParameterProposal(paramName, paramType, startIndex, length,
                ProposalUtils.getParameterImage(), createDisplayString(), computeRelevance(), false, javaContext,
                getGroovyProposalOptions().doParameterGuessing);
    }

    private ProposalFormattingOptions getGroovyProposalOptions() {
        if (options == null) {
            options = ProposalFormattingOptions.newFromOptions();
        }
        return options.newFromExisting(true, null);
    }

    protected StyledString createDisplayString() {
        StyledString ss = new StyledString();

        ss.append(paramName).append(" : ").append("__").append(" - ")
.append(ProposalUtils.createSimpleTypeName(paramType))
                .append(" : named parameter : ", StyledString.QUALIFIER_STYLER)
                .append(" (" + contributor + ")", StyledString.DECORATIONS_STYLER);
        return ss;
    }

}
