package org.codehaus.groovy.eclipse.codeassist.requestor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.codeassist.DocumentSourceBuffer;
import org.codehaus.groovy.eclipse.codeassist.factories.ConstructorCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.ExpressionCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.IGroovyCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.LocalVariableCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.ModifiersCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.NewFieldCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.NewMethodCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.PackageCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.TypeCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.processors.IGroovyCompletionProcessor;
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
import org.eclipse.jface.text.BadLocationException;
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

        factories = new ArrayList<IGroovyCompletionProcessorFactory>(2);
        factories.add(new TypeCompletionProcessorFactory());
        factories.add(new PackageCompletionProcessorFactory());
        factories.add(new ConstructorCompletionProcessorFactory());
        locationFactoryMap.put(ContentAssistLocation.EXCEPTIONS, factories);
        locationFactoryMap.put(ContentAssistLocation.EXTENDS, factories);
        locationFactoryMap.put(ContentAssistLocation.IMPLEMENTS, factories);
        locationFactoryMap.put(ContentAssistLocation.IMPORT, factories);
        locationFactoryMap.put(ContentAssistLocation.CONSTRUCTOR, factories);
        locationFactoryMap.put(ContentAssistLocation.PARAMETER, factories);

        factories = new ArrayList<IGroovyCompletionProcessorFactory>(2);
        factories.add(new ExpressionCompletionProcessorFactory());
        factories.add(new PackageCompletionProcessorFactory());
        locationFactoryMap.put(ContentAssistLocation.EXPRESSION, factories);

        factories = new ArrayList<IGroovyCompletionProcessorFactory>(4);
        factories.add(new TypeCompletionProcessorFactory());
        factories.add(new ExpressionCompletionProcessorFactory());
        factories.add(new LocalVariableCompletionProcessorFactory());
        factories.add(new PackageCompletionProcessorFactory());
        locationFactoryMap.put(ContentAssistLocation.STATEMENT, factories);

        factories = new ArrayList<IGroovyCompletionProcessorFactory>(5);
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
        GroovyLogManager.manager.log(TraceCategory.CONTENT_ASSIST,
                "Starting content assist for " + unit.getElementName());
        String event = "Content assist for " + unit.getElementName();
        GroovyLogManager.manager.logStart(event);

        GroovyCompilationUnit gunit = (GroovyCompilationUnit) unit;

        ModuleNode module = gunit.getModuleNode();
        if (module == null) {
            GroovyLogManager.manager.log(TraceCategory.CONTENT_ASSIST,
                    "Null module node for " + gunit.getElementName());
            return Collections.EMPTY_LIST;
        }

        String fullCompletionText = findCompletionText(context.getDocument(), context.getInvocationOffset());
        String[] completionExpressions = findCompletionExpression(fullCompletionText);
        if (completionExpressions == null) {
            completionExpressions = new String[] { "", "" };
        }
        String completionExpression = completionExpressions[1] == null ? completionExpressions[0]
                : completionExpressions[1];
        int supportingNodeEnd = findSupportingNodeEnd(context.getInvocationOffset(), fullCompletionText);
        CompletionNodeFinder finder = new CompletionNodeFinder(
                context.getInvocationOffset(), supportingNodeEnd,
                completionExpression, fullCompletionText);
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
        }

        GroovyLogManager.manager.logEnd(event, TraceCategory.CONTENT_ASSIST);
        return proposals;
    }

    /**
     * determine if the offset is the start of the first expression on the line.
     * note that the algorithm here only checks whitespace and will not return
     * true if there is a comment before the offset.
     *
     * @param invocationOffset
     * @param document
     * @return
     */
    private boolean isAtStartOfLine(int invocationOffset, IDocument document) {
        int offset = invocationOffset - 1;
        while (offset >= 0) {
            char c = '\0';
            try {
                c = document.getChar(offset--);
            } catch (BadLocationException e) {
                GroovyCore.logException("Exception during content assist", e);
                return false;
            }
            if (!Character.isWhitespace(c)) {
                // something's here
                return false;
            }
            switch (c) {
                case '\n':
                case '\r':
                    return true;
            }
        }
        // reached start of document...
        return true;
    }

    /**
     * @param context
     * @param fullCompletionText
     * @param completionExpressions
     * @return
     */
    private int findSupportingNodeEnd(int invocationOffset,
            String fullCompletionText) {
        String[] completionExpressions = new ExpressionFinder().splitForCompletionNoTrim(fullCompletionText);
        // if second part of completion expression is null, then there is no supporting node (ie- no '.')
        return completionExpressions[1] == null ? -1 :
            invocationOffset - fullCompletionText.length() + completionExpressions[0].length();
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
            if (offset > 0) {
                ISourceBuffer buffer = new DocumentSourceBuffer(doc);
                return new ExpressionFinder().findForCompletions(buffer, offset - 1);
            }
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
