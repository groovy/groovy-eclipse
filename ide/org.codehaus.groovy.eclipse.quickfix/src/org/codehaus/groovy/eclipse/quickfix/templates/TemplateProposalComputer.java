package org.codehaus.groovy.eclipse.quickfix.templates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixPlugin;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

public class TemplateProposalComputer implements
        IJavaCompletionProposalComputer {

    public void sessionStarted() {

    }

    public List<ICompletionProposal> computeCompletionProposals(
        ContentAssistInvocationContext context, IProgressMonitor monitor) {
        try {
            if (!(context instanceof JavaContentAssistInvocationContext)) {
                return Collections.emptyList();
            }
            
            JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
            if (!(javaContext.getCompilationUnit() instanceof GroovyCompilationUnit)) {
                return Collections.emptyList();
            }
            
            TemplateStore codeTemplates = GroovyQuickFixPlugin.getDefault().getTemplateStore();
            List<ICompletionProposal> templates = new ArrayList<ICompletionProposal>();
            Region region = new Region(javaContext.getInvocationOffset(), 0);
            ContextTypeRegistry templateContextRegistry= GroovyQuickFixPlugin.getDefault().getTemplateContextRegistry();
            TemplateContextType contextType= templateContextRegistry.getContextType(GroovyQuickFixPlugin.GROOVY_CONTEXT_TYPE);
            IDocument document = javaContext.getDocument();
            JavaContext templateContext = new GroovyContext(contextType, document, 
                    context.getInvocationOffset(), 0, javaContext.getCompilationUnit());
            String prefix = String.valueOf(context.computeIdentifierPrefix());
            templateContext.setForceEvaluation(true);
            templateContext.setVariable("selection", document.get(region.getOffset(), region.getLength()));
            for (Template template : codeTemplates.getTemplates()) {
                if (isApplicable(template, prefix)) {
                    templates.add(new TemplateProposal(template,
                            templateContext, region, null));
                }
            }
            return templates;
        } catch (BadLocationException e) {
            GroovyQuickFixPlugin.log(e);
            return Collections.emptyList();
        }
    }

    private boolean isApplicable(Template template, String prefix) {
        return template.getName().startsWith(prefix); 
    }

    public List<IContextInformation> computeContextInformation(
            ContentAssistInvocationContext context, IProgressMonitor monitor) {
        return Collections.emptyList();
    }

    public String getErrorMessage() {
        return null;
    }

    public void sessionEnded() {

    }

}
