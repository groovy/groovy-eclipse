/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.eclipse.quickassist.proposals.AddInferencingSuggestionProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.AssignStatementToNewLocalProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertAccessorToPropertyProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertClosureDefToMethodProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertMethodDefToClosureProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertToMultiLineStringProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertToSingleLineStringProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertVariableToFieldProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ExtractToConstantProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ExtractToLocalProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.InlineLocalVariableProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.RemoveSpuriousSemicolonsProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ReplaceDefWithStaticTypeProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.SplitVariableDeclAndInitProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.SwapLeftAndRightOperandsProposal;
import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixPlugin;
import org.codehaus.groovy.eclipse.quickfix.templates.GroovyContext;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.GlobalTemplateVariables.LineSelection;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.text.templates.ContextTypeRegistry;

public class GroovyQuickAssist implements IQuickAssistProcessor {

    @Override
    public boolean hasAssists(final IInvocationContext context) throws CoreException {
        return getAssists(context, new IProblemLocation[0]).length > 0;
    }

    @Override
    public IJavaCompletionProposal[] getAssists(final IInvocationContext context, final IProblemLocation[] locations) throws CoreException {
        if (!(GroovyQuickFixPlugin.isGroovyProject(context) && context.getCompilationUnit() instanceof GroovyCompilationUnit)) {
            return new IJavaCompletionProposal[0];
        }

        List<IJavaCompletionProposal> proposals = new ArrayList<>();

        if (context instanceof IQuickAssistInvocationContext) {
            proposals.addAll(getTemplateAssists((IQuickAssistInvocationContext) context, (GroovyCompilationUnit) context.getCompilationUnit()));
        }

        GroovyQuickAssistContext gcontext = new GroovyQuickAssistContext(context);
        for (GroovyQuickAssistProposal proposal : new GroovyQuickAssistProposal[] {
            new AddInferencingSuggestionProposal(),
            new AssignStatementToNewLocalProposal(),
            new ConvertAccessorToPropertyProposal(),
            new ConvertClosureDefToMethodProposal(),
            new ConvertMethodDefToClosureProposal(),
            new ConvertToMultiLineStringProposal(),
            new ConvertToSingleLineStringProposal(),
            new ConvertVariableToFieldProposal(),
            new ExtractToConstantProposal(true),
            new ExtractToConstantProposal(false),
            new ExtractToLocalProposal(true),
            new ExtractToLocalProposal(false),
            new InlineLocalVariableProposal(),
            new RemoveSpuriousSemicolonsProposal(),
            new ReplaceDefWithStaticTypeProposal(),
            new SplitVariableDeclAndInitProposal(),
            new SwapLeftAndRightOperandsProposal(),
        }) {
            if (proposal.withContext(gcontext).getRelevance() > 0) {
                proposals.add(proposal);
            }
        }

        return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
    }

    public List<IJavaCompletionProposal> getTemplateAssists(final IQuickAssistInvocationContext context, final GroovyCompilationUnit unit) {
        IDocument document = null;
        ISourceViewer viewer = context.getSourceViewer();
        if (viewer != null) document = viewer.getDocument();
        Region region = new Region(context.getOffset(), context.getLength());
        if (document != null && region.getLength() > 1) { // must have selection for surround-with proposals
            try {
                ContextTypeRegistry templateContextRegistry = GroovyQuickFixPlugin.getDefault().getTemplateContextRegistry();
                TemplateContextType contextType = templateContextRegistry.getContextType(GroovyQuickFixPlugin.GROOVY_CONTEXT_TYPE);

                JavaContext templateContext = new GroovyContext(contextType, document, region.getOffset(), region.getLength(), unit);
                templateContext.setForceEvaluation(true);
                templateContext.setVariable(LineSelection.NAME, document.get(region.getOffset(), region.getLength()));

                List<IJavaCompletionProposal> templates = new ArrayList<>();
                for (Template template : GroovyQuickFixPlugin.getDefault().getTemplateStore().getTemplates()) {
                    if (isSurroundWith(template, templateContext)) {
                        templates.add(new TemplateProposal(template, templateContext, region, null));
                    }
                }
                return templates;
            } catch (BadLocationException e) {
                GroovyQuickFixPlugin.log(e);
            }
        }
        return Collections.emptyList();
    }

    //--------------------------------------------------------------------------

    private static boolean isSurroundWith(final Template template, final JavaContext templateContext) {
        String contextId = templateContext.getContextType().getId();
        return GroovyQuickFixPlugin.GROOVY_CONTEXT_TYPE.equals(contextId) &&
            template.getPattern().indexOf(LINE_SELECTION_TEMPLATE) != -1;
    }

    private static final String LINE_SELECTION_TEMPLATE = "${" + LineSelection.NAME + "}";
}
