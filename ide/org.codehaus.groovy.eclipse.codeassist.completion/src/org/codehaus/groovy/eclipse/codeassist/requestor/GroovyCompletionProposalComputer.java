package org.codehaus.groovy.eclipse.codeassist.requestor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codeassist.factories.ExpressionCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.IGroovyCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.LocalVariableCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.ModifiersCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.NewFieldCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.NewMethodCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.PackageCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.TypeCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.processors.IGroovyCompletionProcessor;
import org.codehaus.groovy.eclipse.core.DocumentSourceBuffer;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder;
import org.codehaus.groovy.eclipse.core.util.ParseException;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

public class GroovyCompletionProposalComputer implements
        IJavaCompletionProposalComputer {
    
    private static Map<ContentAssistLocation, List<IGroovyCompletionProcessorFactory>> locationFactoryMap;
    static {
        locationFactoryMap = new HashMap<ContentAssistLocation, List<IGroovyCompletionProcessorFactory>>();

        List<IGroovyCompletionProcessorFactory> factories = new ArrayList<IGroovyCompletionProcessorFactory>(3);
        factories.add(new ModifiersCompletionProcessorFactory());
        factories.add(new NewMethodCompletionProcessorFactory());
        factories.add(new NewFieldCompletionProcessorFactory());
        factories.add(new TypeCompletionProcessorFactory());
        factories.add(new PackageCompletionProcessorFactory());
        locationFactoryMap.put(ContentAssistLocation.CLASS_BODY, factories);
        
        factories = new ArrayList<IGroovyCompletionProcessorFactory>(1);
        factories.add(new TypeCompletionProcessorFactory());
        factories.add(new PackageCompletionProcessorFactory());
        locationFactoryMap.put(ContentAssistLocation.EXCEPTIONS, factories);
        locationFactoryMap.put(ContentAssistLocation.EXTENDS, factories);
        locationFactoryMap.put(ContentAssistLocation.IMPLEMENTS, factories);
        locationFactoryMap.put(ContentAssistLocation.IMPORT, factories);
        locationFactoryMap.put(ContentAssistLocation.CONSTRUCTOR, factories);
        locationFactoryMap.put(ContentAssistLocation.PARAMETER, factories);

        factories = new ArrayList<IGroovyCompletionProcessorFactory>(1);
        factories.add(new ExpressionCompletionProcessorFactory());
        factories.add(new PackageCompletionProcessorFactory());
        locationFactoryMap.put(ContentAssistLocation.EXPRESSION, factories);

        factories = new ArrayList<IGroovyCompletionProcessorFactory>(1);
        factories.add(new TypeCompletionProcessorFactory());
        factories.add(new ExpressionCompletionProcessorFactory());
        factories.add(new LocalVariableCompletionProcessorFactory());
        factories.add(new PackageCompletionProcessorFactory());
        locationFactoryMap.put(ContentAssistLocation.STATEMENT, factories);

        factories = new ArrayList<IGroovyCompletionProcessorFactory>(1);
        factories.add(new ModifiersCompletionProcessorFactory());
        factories.add(new NewMethodCompletionProcessorFactory());
        factories.add(new NewFieldCompletionProcessorFactory());
        factories.add(new TypeCompletionProcessorFactory());
        factories.add(new ExpressionCompletionProcessorFactory());
        factories.add(new LocalVariableCompletionProcessorFactory());
        factories.add(new PackageCompletionProcessorFactory());
        locationFactoryMap.put(ContentAssistLocation.SCRIPT, factories);
    }
    
    
    public GroovyCompletionProposalComputer() {
    }

    public List<ICompletionProposal> computeCompletionProposals(
            ContentAssistInvocationContext context, IProgressMonitor monitor) {
        if (! (context instanceof JavaContentAssistInvocationContext)) {
            return Collections.EMPTY_LIST;
        }
        
        JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
        ICompilationUnit unit = javaContext.getCompilationUnit();
        if (! (unit instanceof GroovyCompilationUnit)) {
            return Collections.EMPTY_LIST;
        }
        
        GroovyCompilationUnit gunit = (GroovyCompilationUnit) unit;
        
        ModuleNode module = gunit.getModuleNode();
        if (module == null) {
            return Collections.EMPTY_LIST;
        }
        
        String fullCompletionText = findCompletionText(context.getDocument(), context.getInvocationOffset());
        String[] completionExpressions = findCompletionExpression(fullCompletionText);
        if (completionExpressions == null) {
            completionExpressions = new String[] { "", "" };
        }
        int supportingNodeEnd = completionExpressions[1] == null ? -1 : 
            context.getInvocationOffset() - fullCompletionText.length() + completionExpressions[0].length();
        CompletionNodeFinder finder = new CompletionNodeFinder(context.getInvocationOffset(), supportingNodeEnd, completionExpressions[1] == null ? completionExpressions[0] : completionExpressions[1], fullCompletionText);
        ContentAssistContext assistContext = finder.findContentAssistContext(gunit);
        List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
        if (assistContext != null) {
            List<IGroovyCompletionProcessorFactory> factories = locationFactoryMap.get(assistContext.location);
            if (factories != null) {
                SearchableEnvironment nameEnvironment = createSearchableEnvironment(javaContext);
                
                for (IGroovyCompletionProcessorFactory factory : factories) {
                    IGroovyCompletionProcessor processor = factory.createProcessor(assistContext, javaContext, nameEnvironment);
                    proposals.addAll(processor.generateProposals(monitor));
                }
            }        
            
            // filter or sort proposals???
        }
        return proposals;
    }

    
    /**
     * @param assistContext
     * @return
     */
    private SearchableEnvironment createSearchableEnvironment(
            JavaContentAssistInvocationContext javaContext) {
        try {
            return ((JavaProject) javaContext
                    .getProject()).newSearchableNameEnvironment(javaContext
                            .getCompilationUnit().getOwner());
        } catch (JavaModelException e) {
            GroovyCore.logException("Exception creating searchable environment for " + javaContext.getCompilationUnit(), e);
            return null;
        }
    }

    /**
     * @param completionText
     * @return
     */
    private String[] findCompletionExpression(String completionText) {
        return new ExpressionFinder().splitForCompletion(completionText);
    }

    protected String findCompletionText(IDocument doc, int offset) {
        try{
            ISourceBuffer buffer = new DocumentSourceBuffer(doc);
            return new ExpressionFinder().findForCompletions(buffer, offset - 1);
        } catch (ParseException e) {
            // can ignore.  probably just invalid code that is being completed at
            GroovyCore.trace("Cannot complete code:" + e.getMessage());
        }
        return "";
    }

    
    private static final List<IContextInformation> NO_CONTEXTS= Collections.emptyList();
    public List<IContextInformation> computeContextInformation(
            ContentAssistInvocationContext context, IProgressMonitor monitor) {
        return NO_CONTEXTS;
    }

    public String getErrorMessage() {
        return "";
    }

    public void sessionEnded() {

    }

    public void sessionStarted() {

    }

}
