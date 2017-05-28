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
package org.codehaus.groovy.eclipse.quickassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.eclipse.quickassist.proposals.AddInferencingSuggestionProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.AssignStatementToNewLocalProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertAccessorToPropertyProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertClosureDefToMethodProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertVariableToFieldProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertMethodDefToClosureProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertToMultiLineStringProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ConvertToSingleLineStringProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ExtractToConstantProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.ExtractToLocalProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.RemoveSpuriousSemicolonsProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.SplitVariableDeclAndInitProposal;
import org.codehaus.groovy.eclipse.quickassist.proposals.SwapLeftAndRightOperandsProposal;
import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixPlugin;
import org.codehaus.groovy.eclipse.quickfix.templates.GroovyContext;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
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
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.GlobalTemplateVariables.LineSelection;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;

public class GroovyQuickAssist implements IQuickAssistProcessor {

    public boolean hasAssists(IInvocationContext context) throws CoreException {
        return getAssists(context, null).length > 0;
    }

    public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
        if (context == null || !(context.getCompilationUnit() instanceof GroovyCompilationUnit) ||
                !isContentInGroovyProject(context.getCompilationUnit())) {
            return new IJavaCompletionProposal[0];
        }

        List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();

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
            new RemoveSpuriousSemicolonsProposal(),
            new SplitVariableDeclAndInitProposal(),
            new SwapLeftAndRightOperandsProposal(),
        }) {
            if (proposal.withContext(gcontext).getRelevance() > 0) {
                proposals.add(proposal);
            }
        }

        return proposals.toArray(new IJavaCompletionProposal[0]);
    }

    public List<IJavaCompletionProposal> getTemplateAssists(IQuickAssistInvocationContext context, GroovyCompilationUnit unit) {
        IDocument document = null;
        ISourceViewer viewer = context.getSourceViewer();
        if (viewer != null) document = viewer.getDocument();
        Region region = new Region(context.getOffset(), context.getLength());
        if (document != null && region.getLength() > 1) // must have selection for surround-with proposals
        try {
            ContextTypeRegistry templateContextRegistry = GroovyQuickFixPlugin.getDefault().getTemplateContextRegistry();
            TemplateContextType contextType = templateContextRegistry.getContextType(GroovyQuickFixPlugin.GROOVY_CONTEXT_TYPE);

            JavaContext templateContext = new GroovyContext(contextType, document, region.getOffset(), region.getLength(), unit);
            templateContext.setForceEvaluation(true);
            templateContext.setVariable(LineSelection.NAME, document.get(region.getOffset(), region.getLength()));

            List<IJavaCompletionProposal> templates = new ArrayList<IJavaCompletionProposal>();
            for (Template template : GroovyQuickFixPlugin.getDefault().getTemplateStore().getTemplates()) {
                if (isSurroundWith(template, templateContext)) {
                    templates.add(new TemplateProposal(template, templateContext, region, null));
                }
            }
            return templates;
        } catch (BadLocationException e) {
            GroovyQuickFixPlugin.log(e);
        }
        return Collections.emptyList();
    }

    //--------------------------------------------------------------------------

    /**
     * Determines if the problem is contained in an accessible (open and existing)
     * Groovy project in the workspace.
     *
     * @param unit compilation unit containing the resource with the problem
     * @return {@code true} iff the problem is contained in an accessible Groovy project
     */
    private boolean isContentInGroovyProject(ICompilationUnit unit) {
        if (unit != null) {
            IResource resource = unit.getResource();
            if (resource != null) {
                IProject project = resource.getProject();
                if (project != null && project.isAccessible() && GroovyNature.hasGroovyNature(project)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSurroundWith(Template template, JavaContext templateContext) {
        String contextId = templateContext.getContextType().getId();
        return GroovyQuickFixPlugin.GROOVY_CONTEXT_TYPE.equals(contextId) &&
            template.getPattern().indexOf(LINE_SELECTION_TEMPLATE) != -1;
    }

    private static final String LINE_SELECTION_TEMPLATE = "${" + LineSelection.NAME + "}";
}
