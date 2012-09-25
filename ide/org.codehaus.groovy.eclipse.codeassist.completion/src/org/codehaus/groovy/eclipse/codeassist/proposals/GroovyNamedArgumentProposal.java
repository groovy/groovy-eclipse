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
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.completions.NamedParameterProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.codeassist.requestor.MethodInfoContentAssistContext;
import org.eclipse.jdt.core.Signature;
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

    private final String paramSignature;

    private final MethodNode ownerMethod;

    private final String contributor;

    private ProposalFormattingOptions options;

    public GroovyNamedArgumentProposal(String paramName, String paramSignature, MethodNode ownerMethod, String contributor) {
        this.paramName = paramName;
        this.paramSignature = paramSignature;
        ;
        this.ownerMethod = ownerMethod;
        this.contributor = contributor;
        setRelevanceMultiplier(100);
    }
    public GroovyNamedArgumentProposal(String paramName, ClassNode paramType, MethodNode ownerMethod, String contributor) {
        this.paramName = paramName;
        this.paramSignature = ProposalUtils.createTypeSignatureStr(unbox(paramType));
        ;
        this.ownerMethod = ownerMethod;
        this.contributor = contributor;
        setRelevanceMultiplier(100);
    }

    @Override
    public AnnotatedNode getAssociatedNode() {
        return ownerMethod;
    }

    public IJavaCompletionProposal createJavaProposal(ContentAssistContext context, JavaContentAssistInvocationContext javaContext) {
        if (context.location != ContentAssistLocation.METHOD_CONTEXT) {
            return null;
        }
        MethodInfoContentAssistContext methodContext = (MethodInfoContentAssistContext) context;
        int startIndex = methodContext.completionLocation - methodContext.completionExpression.length();
        int length = methodContext.completionEnd - startIndex;
        return new NamedParameterProposal(paramName, paramSignature, startIndex, length,
                ProposalUtils.getParameterImage(), createDisplayString(), computeRelevance(), false, javaContext,
                getGroovyProposalOptions().doParameterGuessing);
    }

    private ProposalFormattingOptions getGroovyProposalOptions() {
        if (options == null) {
            options = ProposalFormattingOptions.newFromOptions();
        }
        return options.newFromExisting(true, false, null);
    }

    protected StyledString createDisplayString() {
        StyledString ss = new StyledString();

        ss.append(paramName).append(" : ").append("__").append(" - ").append(Signature.toString(paramSignature))
                .append(" : named parameter : ", StyledString.QUALIFIER_STYLER)
                .append(" (" + contributor + ")", StyledString.DECORATIONS_STYLER);
        return ss;
    }

    /**
     * Can't use ClassHelper.getUnwrapper here since relies on == and this will
     * often be a JDTClassNode
     *
     * @param paramType2
     * @return
     */
    private ClassNode unbox(ClassNode maybeBoxed) {
        if (ClassHelper.isPrimitiveType(maybeBoxed)) {
            return maybeBoxed;
        }
        String name = maybeBoxed.getName();
        if (ClassHelper.Boolean_TYPE.getName().equals(name)) {
            return ClassHelper.boolean_TYPE;
        } else if (ClassHelper.Byte_TYPE.getName().equals(name)) {
            return ClassHelper.byte_TYPE;
        } else if (ClassHelper.Character_TYPE.getName().equals(name)) {
            return ClassHelper.char_TYPE;
        } else if (ClassHelper.Short_TYPE.getName().equals(name)) {
            return ClassHelper.short_TYPE;
        } else if (ClassHelper.Integer_TYPE.getName().equals(name)) {
            return ClassHelper.int_TYPE;
        } else if (ClassHelper.Long_TYPE.getName().equals(name)) {
            return ClassHelper.long_TYPE;
        } else if (ClassHelper.Float_TYPE.getName().equals(name)) {
            return ClassHelper.float_TYPE;
        } else if (ClassHelper.Double_TYPE.getName().equals(name)) {
            return ClassHelper.double_TYPE;
        } else {
            return maybeBoxed;
        }
    }
}
