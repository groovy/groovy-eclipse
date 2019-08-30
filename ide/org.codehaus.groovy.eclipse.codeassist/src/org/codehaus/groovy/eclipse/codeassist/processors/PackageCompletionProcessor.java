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
package org.codehaus.groovy.eclipse.codeassist.processors;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.groovy.search.ITypeResolver;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class PackageCompletionProcessor extends AbstractGroovyCompletionProcessor implements ITypeResolver {

    protected ModuleNode module;
    protected JDTResolver resolver;

    public PackageCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
    }

    @Override
    public void setResolverInformation(ModuleNode module, JDTResolver resolver) {
        this.module = module;
        this.resolver = resolver;
    }

    @Override
    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        ContentAssistContext context = getContext();

        String expression = context.getQualifiedCompletionExpression();
        if (!doPackageCompletion(context, expression)) {
            return Collections.emptyList();
        }

        int replacementStart = getReplacementStartOffset();
        SearchableEnvironment environment = getNameEnvironment();
        GroovyProposalTypeSearchRequestor requestor = new GroovyProposalTypeSearchRequestor(
            context, getJavaContext(), replacementStart, -1, environment.nameLookup, monitor);
        environment.findPackages(expression.toCharArray(), requestor);

        return requestor.processAcceptedPackages();
    }

    protected boolean doPackageCompletion(ContentAssistContext context, String expression) {
        if (expression.isEmpty() || ILLEGAL_CHARS.matcher(expression).find()) {
            return false;
        }
        if (context.location == ContentAssistLocation.GENERICS &&
                context.completionNode instanceof GenericsType) {
            return false;
        }
        // check for parameter name completion
        if (context.location == ContentAssistLocation.PARAMETER &&
                context.completionNode instanceof AnnotatedNode) {
            AnnotatedNode completionNode = (AnnotatedNode) context.completionNode;
            if (completionNode.getStart() < completionNode.getNameStart() &&
                    context.completionLocation >= completionNode.getNameStart()) {
                return false;
            }
        }
        return true;
    }

    protected static final Pattern ILLEGAL_CHARS = Pattern.compile("[^\\p{javaJavaIdentifierPart}\\s\\.]");
}
