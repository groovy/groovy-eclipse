/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.proposals;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.completions.NamedParameterProposal;
import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.codeassist.requestor.MethodInfoContentAssistContext;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

public class GroovyNamedArgumentProposal implements IGroovyProposal {

    private final String paramName, paramSignature;
    private final MethodNode ownerMethod;
    private final String contributor;

    public GroovyNamedArgumentProposal(String paramName, String paramSignature, MethodNode ownerMethod, String contributor) {
        this.paramName = paramName;
        this.paramSignature = paramSignature;
        this.ownerMethod = ownerMethod;
        this.contributor = contributor;
    }

    public GroovyNamedArgumentProposal(String paramName, ClassNode paramType, MethodNode ownerMethod, String contributor) {
        this(paramName, String.valueOf(ProposalUtils.createTypeSignature(paramType)), ownerMethod, contributor);
    }

    @Override
    public IJavaCompletionProposal createJavaProposal(ContentAssistContext context, JavaContentAssistInvocationContext javaContext) {
        if (context.location != ContentAssistLocation.METHOD_CONTEXT) {
            return null;
        }
        MethodInfoContentAssistContext methodContext = (MethodInfoContentAssistContext) context;
        int offset = methodContext.completionLocation - methodContext.completionExpression.length();
        int length = methodContext.completionEnd - offset;
        Image image = ProposalUtils.getParameterImage();
        StyledString displayString = createDisplayString();
        int relevance = Relevance.VERY_HIGH.getRelevance();
        boolean inJavadoc = false;

        return new NamedParameterProposal(paramName, paramSignature, offset, length, image, displayString, relevance, inJavadoc, javaContext);
    }

    protected StyledString createDisplayString() {
        return new StyledString()
            .append(paramName).append(" : __ - ")
            .append(Signature.toString(paramSignature))
            .append(" : named parameter of ", StyledString.QUALIFIER_STYLER)
            .append(contributor, StyledString.DECORATIONS_STYLER);
    }
}
