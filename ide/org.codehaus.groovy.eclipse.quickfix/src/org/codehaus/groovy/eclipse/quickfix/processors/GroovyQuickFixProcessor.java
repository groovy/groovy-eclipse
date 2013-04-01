/*
 * Copyright 2010-2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix.processors;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.quickfix.proposals.GroovyQuickFixResolverRegistry;
import org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixResolver;
import org.codehaus.groovy.eclipse.quickfix.proposals.ProblemDescriptor;
import org.codehaus.groovy.eclipse.quickfix.proposals.ProblemType;
import org.codehaus.groovy.eclipse.quickfix.proposals.QuickFixProblemContext;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

/**
 * Integrates Groovy proposals into JDT quick fix framework.
 * 
 * @author Nieraj Singh
 * 
 */
public class GroovyQuickFixProcessor implements IQuickFixProcessor {
    public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		return isProblemInGroovyProject(unit) && ProblemType.isRecognizedProblemId(problemId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.ui.text.java.IQuickFixProcessor#getCorrections(org.eclipse
	 * .jdt.ui.text.java.IInvocationContext,
	 * org.eclipse.jdt.ui.text.java.IProblemLocation[])
	 */
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context,
			IProblemLocation[] locations) throws CoreException {

		// The problem must be in a Groovy project. Otherwise do not handle it
		// as the proposals should not appear if the problem is in any other
		// type of project
		if (isProblemInGroovyProject(context, locations)) {
		    QuickFixProblemContext problemContext = getQuickFixProblemContext(
					context, locations);

			if (problemContext != null) {
				List<IQuickFixResolver> resolvers = new GroovyQuickFixResolverRegistry(
						problemContext).getQuickFixResolvers();
				if (resolvers != null) {
					List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();
					for (IQuickFixResolver resolver : resolvers) {

						List<IJavaCompletionProposal> foundProposals = resolver
								.getQuickFixProposals();
						if (foundProposals != null) {
						    proposals.addAll(foundProposals);
						}
					}

					return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
				}

			}
		}
		return new IJavaCompletionProposal[0];
	    
//	    if (!(context instanceof IQuickAssistInvocationContext)) {
//	        return new IJavaCompletionProposal[0];
//	    }
//	    try {
//            IQuickAssistInvocationContext assistContext = (IQuickAssistInvocationContext) context;
//            TemplateStore codeTemplates = GroovyQuickFixPlugin.getDefault().getTemplateStore();
//            List<IJavaCompletionProposal> templates = new ArrayList<IJavaCompletionProposal>();
//            Region region = new Region(assistContext.getOffset(), assistContext.getLength());
//            ContextTypeRegistry templateContextRegistry= GroovyQuickFixPlugin.getDefault().getTemplateContextRegistry();
//            TemplateContextType contextType= templateContextRegistry.getContextType(GroovyQuickFixPlugin.GROOVY_CONTEXT_TYPE);
//            IDocument document = assistContext.getSourceViewer().getDocument();
//            JavaContext templateContext = new GroovyContext(contextType, document, 
//                    region.getOffset(), region.getLength(), context.getCompilationUnit());
//            
//            templateContext.setForceEvaluation(true);
//            templateContext.setVariable("selection", document.get(region.getOffset(), region.getLength()));
//            for (Template template : codeTemplates.getTemplates()) {
//                templates.add(new TemplateProposal(template, templateContext, region, null));
//            }
//            return templates.toArray(new IJavaCompletionProposal[0]);
//        } catch (BadLocationException e) {
//            GroovyQuickFixPlugin.log(e);
//            return new IJavaCompletionProposal[0];
//        }
	}

	/**
	 * Generates a representation of the Java problem context that the Groovy
	 * quick fix framework will understand.
	 * 
	 * @param context
	 *            Java context containing information about the problem
	 * @param locations
	 *            where the problem occurs
	 * @return model representing the Java problem context
	 */
	protected QuickFixProblemContext getQuickFixProblemContext(
			IInvocationContext context, IProblemLocation[] locations) {

		if (context == null || locations == null || locations.length == 0) {
			return null;
		}
		// FIX: for now return the first location. Add support to
		// return multiple locations if necessary
		IProblemLocation location = locations[0];

		ProblemDescriptor descriptor = getProblemDescriptor(location.getProblemId(),
						location.getMarkerType(),
						location.getProblemArguments());

		if (descriptor != null) {
			return new QuickFixProblemContext(descriptor, context, location);
		}

		return null;
	}

	/** not API.  Public for testing purposes */
	public ProblemDescriptor getProblemDescriptor(int problemID,
            String markerDescription, String[] messages) {
        ProblemType type = ProblemType.getProblemType(problemID, markerDescription,
                messages);
        if (type != null) {
            return new ProblemDescriptor(type, messages);
        }
        return null;
    }

	/**
	 * True if the problem is contained in an accessible (open and existing)
	 * Groovy project in the workspace. False otherwise.
	 * 
	 * @param context
	 *            containing Java/Groovy problem information
	 * @param locations
	 *            of the Java/Groovy problem
	 * @return true if and only if the problem is contained in an accessible
	 *         Groovy project. False otherwise
	 */
	protected boolean isProblemInGroovyProject(IInvocationContext context,
			IProblemLocation[] locations) {
		if (context != null && locations != null && locations.length > 0) {
			return isProblemInGroovyProject(context.getCompilationUnit());
		}

		return false;
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
	protected boolean isProblemInGroovyProject(ICompilationUnit unit) {
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
