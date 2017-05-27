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
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.GlobalTemplateVariables.LineSelection;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;

public class GroovyQuickAssist implements IQuickAssistProcessor {

    public boolean hasAssists(IInvocationContext context) throws CoreException {
        return context != null && context.getCompilationUnit() instanceof GroovyCompilationUnit && isContentInGroovyProject(context.getCompilationUnit());
    }

    public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
        if (!hasAssists(context)) {
            return new IJavaCompletionProposal[0];
        }

        List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();

        if (context instanceof IQuickAssistInvocationContext) {
            proposals.addAll(getTemplateAssists((IQuickAssistInvocationContext) context, (GroovyCompilationUnit) context.getCompilationUnit()));
        }

        // check for 'Add inferencing suggestion'
        AddInferencingSuggestionQuickAssistProposal javaProposal = new AddInferencingSuggestionQuickAssistProposal(context);
        if (javaProposal.hasProposals()) {
            proposals.add(javaProposal);
        }

        // check for 'Assign statement to new local variable'
        AssignStatementToNewLocalProposal assignStatement = new AssignStatementToNewLocalProposal(context);
        if (assignStatement.hasProposals()) {
            proposals.add(assignStatement);
        }

        // check for 'Convert local variable to field'
        ConvertLocalToFieldProposal convertToField = new ConvertLocalToFieldProposal(context);
        if (convertToField.hasProposals()) {
            proposals.add(convertToField);
        }

        // check for 'Replace method call with property expression'
        ConvertMethodToPropertyProposal convertToProperty = new ConvertMethodToPropertyProposal(context);
        if (convertToProperty.hasProposals()) {
            proposals.add(convertToProperty);
        }

        // check for 'Convert method declaration to closure'
        ConvertToClosureCompletionProposal convertToClosure = new ConvertToClosureCompletionProposal(context);
        if (convertToClosure.hasProposals()) {
            proposals.add(convertToClosure);
        }

        // check for 'Convert closure declaration to method'
        ConvertToMethodCompletionProposal convertToMethod = new ConvertToMethodCompletionProposal(context);
        if (convertToMethod.hasProposals()) {
            proposals.add(convertToMethod);
        }

        // check for 'Convert to multi-line string'
        ConvertToMultiLineStringCompletionProposal convertToMultiLineString = new ConvertToMultiLineStringCompletionProposal(context);
        if (convertToMultiLineString.hasProposals()) {
            proposals.add(convertToMultiLineString);
        }

        // check for 'Convert to single-line string'
        ConvertToSingleLineStringCompletionProposal convertToSingleLineString = new ConvertToSingleLineStringCompletionProposal(context);
        if (convertToSingleLineString.hasProposals()) {
            proposals.add(convertToSingleLineString);
        }

        // check for 'Extract to constant (replace all occurrences)'
        ExtractToConstantProposal extractToConstantAllOccurrences = new ExtractToConstantProposal(context, true);
        if (extractToConstantAllOccurrences.hasProposals()) {
            proposals.add(extractToConstantAllOccurrences);
        }

        // check for 'Extract to constant'
        ExtractToConstantProposal extractToConstant = new ExtractToConstantProposal(context, false);
        if (extractToConstant.hasProposals()) {
            proposals.add(extractToConstant);
        }

        // check for 'Extract to local variable (replace all occurrences)'
        ExtractToLocalProposal extractToLocalAllOccurences = new ExtractToLocalProposal(context, true);
        if (extractToLocalAllOccurences.hasProposals()) {
            proposals.add(extractToLocalAllOccurences);
        }

        // check for 'Extract to local variable'
        ExtractToLocalProposal extractToLocal = new ExtractToLocalProposal(context, false);
        if (extractToLocal.hasProposals()) {
            proposals.add(extractToLocal);
        }

        // check for 'Remove unnecessary semicolons'
        RemoveUnnecessarySemicolonsCompletionProposal unnecessarySemicolons = new RemoveUnnecessarySemicolonsCompletionProposal(context);
        if (unnecessarySemicolons.hasProposals()) {
            proposals.add(unnecessarySemicolons);
        }

        // check for 'Split variable declaration'
        SplitAssigmentCompletionProposal splitAssignment = new SplitAssigmentCompletionProposal(context);
        if (splitAssignment.hasProposals()) {
            proposals.add(splitAssignment);
        }

        // check for 'Exchange left and right operands for infix expression'
        SwapOperandsCompletionProposal swapOperands = new SwapOperandsCompletionProposal(context);
        if (swapOperands.hasProposals()) {
            proposals.add(swapOperands);
        }

        return proposals.toArray(new IJavaCompletionProposal[0]);
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

    public List<IJavaCompletionProposal> getTemplateAssists(IQuickAssistInvocationContext assistContext, GroovyCompilationUnit unit) {
        try {
            IDocument document = assistContext.getSourceViewer().getDocument();
            Region region = new Region(assistContext.getOffset(), assistContext.getLength());
            if (region.getLength() > 1) { // must have selection to enable surround-with proposals
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
            }
        } catch (BadLocationException e) {
            GroovyQuickFixPlugin.log(e);
        }
        return Collections.emptyList();
    }

    private boolean isSurroundWith(Template template, JavaContext templateContext) {
        String contextId = templateContext.getContextType().getId();
        return GroovyQuickFixPlugin.GROOVY_CONTEXT_TYPE.equals(contextId) &&
            template.getPattern().indexOf(LINE_SELECTION_TEMPLATE) != -1;
    }

    private static final String LINE_SELECTION_TEMPLATE = "${" + LineSelection.NAME + "}";
}
