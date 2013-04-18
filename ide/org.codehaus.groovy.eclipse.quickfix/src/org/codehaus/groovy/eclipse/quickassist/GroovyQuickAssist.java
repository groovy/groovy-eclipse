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
import org.eclipse.jdt.internal.corext.template.java.CompilationUnitContextType;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.corext.template.java.JavaDocContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
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
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

public class GroovyQuickAssist implements IQuickAssistProcessor {
    private static final String $_LINE_SELECTION= "${" + GlobalTemplateVariables.LineSelection.NAME + "}"; //$NON-NLS-1$ //$NON-NLS-2$

	public boolean hasAssists(IInvocationContext context) throws CoreException {
		if (context != null
				&& isContentInGroovyProject(context.getCompilationUnit())) {
			return new AddSuggestionsQuickAssistProposal(context).hasProposals() || 
			        new ConvertToClosureCompletionProposal(context).hasProposals() ||
			        new ConvertToMethodCompletionProposal(context).hasProposals() ||
                    new ConvertToMultiLineStringCompletionProposal(context).hasProposals() ||
                    new ConvertToSingleLineStringCompletionProposal(context).hasProposals() ||
                    new RemoveUnnecessarySemicolonsCompletionProposal(context).hasProposals() ||
                    new SwapOperandsCompletionProposal(context).hasProposals() ||
                    new SplitAssigmentCompletionProposal(context).hasProposals() ||
                    new AssignStatementToNewLocalProposal(context).hasProposals();
		}
		return false;
	}

	public IJavaCompletionProposal[] getAssists(IInvocationContext context,
			IProblemLocation[] locations) throws CoreException {
	    if (!(context.getCompilationUnit() instanceof GroovyCompilationUnit)) {
	        return new IJavaCompletionProposal[0];
	    }
	    
		List<IJavaCompletionProposal> proposalList;
        if (context instanceof IQuickAssistInvocationContext) {
            proposalList = getTemplateAssists(
                    (IQuickAssistInvocationContext) context,
                    (GroovyCompilationUnit) context.getCompilationUnit());
        } else {
            proposalList = new ArrayList<IJavaCompletionProposal>();
        }
		
		AddSuggestionsQuickAssistProposal javaProposal = new AddSuggestionsQuickAssistProposal(
				context);
		if (javaProposal.hasProposals()) {
		    proposalList.add(javaProposal);
		}
		
		ConvertToClosureCompletionProposal convertToClosure = new ConvertToClosureCompletionProposal(context);
		if (convertToClosure.hasProposals()) {
            proposalList.add(convertToClosure);
        }
        
		ConvertToMethodCompletionProposal convertToMethod = new ConvertToMethodCompletionProposal(context);
		if (convertToMethod.hasProposals()) {
		    proposalList.add(convertToMethod);
		}
		
		ConvertToMultiLineStringCompletionProposal convertToMultiLineString = new ConvertToMultiLineStringCompletionProposal(context);
		if (convertToMultiLineString.hasProposals()) {
		    proposalList.add(convertToMultiLineString);
		}
		
		ConvertToSingleLineStringCompletionProposal convertToSingleLineString = new ConvertToSingleLineStringCompletionProposal(context);
		if (convertToSingleLineString.hasProposals()) {
		    proposalList.add(convertToSingleLineString);
		}
		
		RemoveUnnecessarySemicolonsCompletionProposal unnecessarySemicolons = new RemoveUnnecessarySemicolonsCompletionProposal(context);
		if (unnecessarySemicolons.hasProposals()) {
            proposalList.add(unnecessarySemicolons);
        }
		
		SplitAssigmentCompletionProposal splitAssignment = new SplitAssigmentCompletionProposal(context);
		if (splitAssignment.hasProposals()) {
		    proposalList.add(splitAssignment);
		}
		
		SwapOperandsCompletionProposal swapOperands = new SwapOperandsCompletionProposal(context);
		if (swapOperands.hasProposals()) {
		    proposalList.add(swapOperands);
		}
		
		AssignStatementToNewLocalProposal assignStatement = new AssignStatementToNewLocalProposal(context);
		if (assignStatement.hasProposals()) {
		    proposalList.add(assignStatement);
		}
		
		
		return proposalList.toArray(new IJavaCompletionProposal[0]);
	}
	
	public List<IJavaCompletionProposal> getTemplateAssists(IQuickAssistInvocationContext assistContext, GroovyCompilationUnit unit) {
        try {
            TemplateStore codeTemplates = GroovyQuickFixPlugin.getDefault().getTemplateStore();
            List<IJavaCompletionProposal> templates = new ArrayList<IJavaCompletionProposal>();
            Region region = new Region(assistContext.getOffset(), assistContext.getLength());
            ContextTypeRegistry templateContextRegistry= GroovyQuickFixPlugin.getDefault().getTemplateContextRegistry();
            TemplateContextType contextType= templateContextRegistry.getContextType(GroovyQuickFixPlugin.GROOVY_CONTEXT_TYPE);
            IDocument document = assistContext.getSourceViewer().getDocument();
            JavaContext templateContext = new GroovyContext(contextType, document, 
                    region.getOffset(), region.getLength(), unit);
            
            templateContext.setForceEvaluation(true);
            templateContext.setVariable("selection", document.get(region.getOffset(), region.getLength()));
            for (Template template : codeTemplates.getTemplates()) {
                if (isSurroundWith(template, templateContext)) {
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
	
	private boolean isSurroundWith(Template template, JavaContext templateContext) {
        String contextId= templateContext.getContextType().getId();
        return GroovyQuickFixPlugin.GROOVY_CONTEXT_TYPE.equals(contextId) && template.getPattern().indexOf($_LINE_SELECTION) != -1;
	}
	
	/**
     * True if the problem is contained in an accessible (open and existing)
     * Groovy project in the workspace. False otherwise.
     * 
     * @param unit
     *            compilation unit containing the resource with the problem
     * @return true if and only if the problem is contained in an accessible
     *         Groovy project. False otherwise
     */
    protected boolean isProblemInGroovyProject(IInvocationContext context) {
        if (context == null) {
            return false;
        }
        return isContentInGroovyProject(context.getCompilationUnit());
    }
    
    

    /**
     * True if the problem is contained in an accessible (open and existing)
     * Groovy project in the workspace. False otherwise.
     * 
     * @param unit
     *            compilation unit containing the resource with the problem
     * @return true if and only if the problem is contained in an accessible
     *         Groovy project. False otherwise
     */
    protected boolean isContentInGroovyProject(ICompilationUnit unit) {

        if (unit != null) {
            IResource resource = unit.getResource();
            if (resource != null) {
                IProject project = resource.getProject();
                if (project != null && project.isAccessible()
                        && GroovyNature.hasGroovyNature(project)) {
                    return true;
                }
            }
        }
        return false;
    }


}
