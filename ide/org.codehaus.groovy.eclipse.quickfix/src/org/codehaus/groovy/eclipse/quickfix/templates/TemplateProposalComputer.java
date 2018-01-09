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
package org.codehaus.groovy.eclipse.quickfix.templates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixPlugin;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;

public class TemplateProposalComputer implements IJavaCompletionProposalComputer {

    private static final int EMPTY_PREFIX_TEMPLATE_RELEVANCE = 20;

    @Override
    public void sessionStarted() {
    }

    @Override
    public void sessionEnded() {
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
        return Collections.emptyList();
    }

    @Override
    public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
        try {
            if (context instanceof JavaContentAssistInvocationContext) {
                JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
                if (javaContext.getCompilationUnit() instanceof GroovyCompilationUnit) {
                    TemplateContextType contextType = GroovyQuickFixPlugin.getDefault().getTemplateContextRegistry().getContextType(GroovyQuickFixPlugin.GROOVY_CONTEXT_TYPE);
                    GroovyContext templateContext = new GroovyContext(contextType, context.getDocument(), context.getInvocationOffset(), 0, javaContext.getCompilationUnit());
                    templateContext.setForceEvaluation(true);
                    templateContext.setVariable("selection", "");

                    String templatePrefix = context.computeIdentifierPrefix().toString();
                    int offset = context.getInvocationOffset() - templatePrefix.length();
                    while (--offset >= 0) {
                        if (!Character.isWhitespace(context.getDocument().getChar(offset))) {
                            break;
                        }
                    }
                    if (offset > 0 && (context.getDocument().getChar(offset) == '@' || context.getDocument().getChar(offset) == '&')) {
                        offset -= 1;
                    }

                    if (offset == -1 || context.getDocument().getChar(offset) != '.') {
                        return computeCompletionProposals(templateContext, templatePrefix);
                    }
                }
            }
        } catch (BadLocationException e) {
            GroovyQuickFixPlugin.log(e);
        }
        return Collections.emptyList();
    }

    private List<ICompletionProposal> computeCompletionProposals(GroovyContext context, String prefix) throws BadLocationException {
        List<ICompletionProposal> templates = new ArrayList<>();
        Region region = new Region(context.getCompletionOffset(), context.getCompletionLength());
        for (Template template : GroovyQuickFixPlugin.getDefault().getTemplateStore().getTemplates()) {
            if (template.getName().startsWith(prefix)) {
                TemplateProposal templateProposal = new TemplateProposal(template, context, region, null);
                if (prefix.length() == 0) {
                    templateProposal.setRelevance(EMPTY_PREFIX_TEMPLATE_RELEVANCE);
                }
                templates.add(templateProposal);
            }
        }
        return templates;
    }
}
