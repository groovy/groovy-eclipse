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
package org.codehaus.groovy.eclipse.codeassist.requestor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.codeassist.DocumentSourceBuffer;
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.factories.AnnotationCollectorTypeCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.AnnotationMemberValueCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.ConstructorCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.ExpressionCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.GetSetMethodCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.IGroovyCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.LocalVariableCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.ModifiersCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.NewFieldCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.NewMethodCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.NewVariableCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.PackageCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.factories.TypeCompletionProcessorFactory;
import org.codehaus.groovy.eclipse.codeassist.processors.IGroovyCompletionProcessor;
import org.codehaus.groovy.eclipse.codeassist.processors.IProposalFilter;
import org.codehaus.groovy.eclipse.codeassist.processors.IProposalFilterExtension;
import org.codehaus.groovy.eclipse.codeassist.processors.ProposalProviderRegistry;
import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder;
import org.codehaus.groovy.eclipse.core.util.ParseException;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.search.ITypeResolver;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

public class GroovyCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private static final Map<ContentAssistLocation, List<IGroovyCompletionProcessorFactory>> LOCATION_FACTORIES;
    static {
        Map<ContentAssistLocation, List<IGroovyCompletionProcessorFactory>> locationFactories = new EnumMap<>(ContentAssistLocation.class);

        locationFactories.put(ContentAssistLocation.ANNOTATION, Collections.unmodifiableList(Arrays.asList(
            new TypeCompletionProcessorFactory(),
            new PackageCompletionProcessorFactory(),
            new AnnotationCollectorTypeCompletionProcessorFactory()
        )));

        locationFactories.put(ContentAssistLocation.ANNOTATION_BODY, Collections.<IGroovyCompletionProcessorFactory>singletonList(
            new AnnotationMemberValueCompletionProcessorFactory()
        ));

        locationFactories.put(ContentAssistLocation.CLASS_BODY, Collections.unmodifiableList(Arrays.asList(
            new ModifiersCompletionProcessorFactory(),
            new NewMethodCompletionProcessorFactory(),
            new GetSetMethodCompletionProcessorFactory(),
            new NewFieldCompletionProcessorFactory(),
            new TypeCompletionProcessorFactory(),
            new PackageCompletionProcessorFactory(),
            new NewVariableCompletionProcessorFactory()
        )));

        locationFactories.put(ContentAssistLocation.CONSTRUCTOR, Collections.unmodifiableList(Arrays.asList(
            new TypeCompletionProcessorFactory(),
            new PackageCompletionProcessorFactory(),
            new ConstructorCompletionProcessorFactory()
        )));

        locationFactories.put(ContentAssistLocation.EXCEPTIONS, Collections.unmodifiableList(Arrays.asList(
            new TypeCompletionProcessorFactory(),
            new PackageCompletionProcessorFactory()
        )));

        locationFactories.put(ContentAssistLocation.EXPRESSION, Collections.unmodifiableList(Arrays.asList(
            new TypeCompletionProcessorFactory(),
            new PackageCompletionProcessorFactory(),
            new ExpressionCompletionProcessorFactory()
        )));

        locationFactories.put(ContentAssistLocation.EXTENDS, Collections.unmodifiableList(Arrays.asList(
            new TypeCompletionProcessorFactory(),
            new PackageCompletionProcessorFactory()
        )));

        locationFactories.put(ContentAssistLocation.GENERICS, Collections.unmodifiableList(Arrays.asList(
            new TypeCompletionProcessorFactory(),
            new PackageCompletionProcessorFactory()
        )));

        locationFactories.put(ContentAssistLocation.IMPLEMENTS, Collections.unmodifiableList(Arrays.asList(
            new TypeCompletionProcessorFactory(),
            new PackageCompletionProcessorFactory()
        )));

        locationFactories.put(ContentAssistLocation.IMPORT, Collections.unmodifiableList(Arrays.asList(
            new TypeCompletionProcessorFactory(),
            new PackageCompletionProcessorFactory(),
            new ExpressionCompletionProcessorFactory() // for static members
        )));

        locationFactories.put(ContentAssistLocation.METHOD_CONTEXT, Collections.unmodifiableList(Arrays.asList(
            new ExpressionCompletionProcessorFactory(), // for method contexts
            new ConstructorCompletionProcessorFactory(), // for constructor contexts
            new TypeCompletionProcessorFactory(),
            new PackageCompletionProcessorFactory(),
            new LocalVariableCompletionProcessorFactory()
        )));

        locationFactories.put(ContentAssistLocation.PACKAGE, Collections.<IGroovyCompletionProcessorFactory>singletonList(
            new PackageCompletionProcessorFactory()
        ));

        locationFactories.put(ContentAssistLocation.PARAMETER, Collections.unmodifiableList(Arrays.asList(
            new TypeCompletionProcessorFactory(),
            new PackageCompletionProcessorFactory()
        )));

        locationFactories.put(ContentAssistLocation.SCRIPT, Collections.unmodifiableList(Arrays.asList(
            new TypeCompletionProcessorFactory(),
            new PackageCompletionProcessorFactory(),
            new ExpressionCompletionProcessorFactory(),
            new LocalVariableCompletionProcessorFactory(),
            new ModifiersCompletionProcessorFactory(),
            new NewFieldCompletionProcessorFactory(),
            new NewMethodCompletionProcessorFactory(),
            new NewVariableCompletionProcessorFactory()
        )));

        locationFactories.put(ContentAssistLocation.STATEMENT, Collections.unmodifiableList(Arrays.asList(
            new TypeCompletionProcessorFactory(),
            new PackageCompletionProcessorFactory(),
            new ExpressionCompletionProcessorFactory(),
            new ConstructorCompletionProcessorFactory(),
            new LocalVariableCompletionProcessorFactory(),
            new NewVariableCompletionProcessorFactory()
        )));

        LOCATION_FACTORIES = Collections.unmodifiableMap(locationFactories);
    }

    @Override
    public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
        if (!(context instanceof JavaContentAssistInvocationContext)) {
            return Collections.EMPTY_LIST;
        }

        JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
        ICompilationUnit unit = javaContext.getCompilationUnit();
        if (!(unit instanceof GroovyCompilationUnit)) {
            return Collections.EMPTY_LIST;
        }

        String traceEvent = null;
        if (GroovyLogManager.manager.hasLoggers()) {
            GroovyLogManager.manager.log(TraceCategory.CONTENT_ASSIST, "Starting content assist for " + unit.getElementName());
            traceEvent = "Content assist for " + unit.getElementName();
            GroovyLogManager.manager.logStart(traceEvent);
        }

        GroovyCompilationUnit gunit = (GroovyCompilationUnit) unit;
        ModuleNodeInfo moduleInfo = gunit.getModuleInfo(true);
        if (moduleInfo == null) {
            if (traceEvent != null) {
                GroovyLogManager.manager.log(TraceCategory.CONTENT_ASSIST, "Null module node");
            }
            return Collections.EMPTY_LIST;
        }

        int offset = context.getInvocationOffset();
        IDocument document = context.getDocument();
        if (offset < 0 || offset > document.getLength()) {
            if (traceEvent != null) {
                GroovyLogManager.manager.log(TraceCategory.CONTENT_ASSIST, "Completion offset " + offset + " is out of bounds");
            }
            return Collections.EMPTY_LIST;
        }

        ContentAssistContext assistContext = createContentAssistContext(gunit, offset, document);

        // if content assist is invoked right after the @ sign this would lead to a timeout
        // in that case we just return an empty results
        boolean isEmptyAnnotationRequest = false;

        try {
            final int startIdx = assistContext.completionEnd - 1;

            if (startIdx >= 0) {
                final String previousChar = document.get(startIdx, 1);
                if (assistContext.completionExpression.length() == 0 && "@".equals(previousChar)) {
                    isEmptyAnnotationRequest = true;
                }
            }
        } catch (BadLocationException e) {
            GroovyContentAssist.logError("Exception while checking for empty content assist requests after an annotation", e);
        }

        List<ICompletionProposal> proposals = new ArrayList<>();
        if (assistContext != null) {
            List<IGroovyCompletionProcessorFactory> factories = LOCATION_FACTORIES.get(assistContext.location);
            if (factories != null) {
                SubMonitor submon = SubMonitor.convert(monitor, factories.size());
                SearchableEnvironment environment = createSearchableEnvironment(javaContext);
                try {
                    for (IGroovyCompletionProcessorFactory factory : factories) {

                        // this is the slowest factory, so we ignore it in this case, as it would lead to a timeout anyway
                        if (isEmptyAnnotationRequest && factory instanceof AnnotationCollectorTypeCompletionProcessorFactory) {
                            continue;
                        }

                        IGroovyCompletionProcessor processor = factory.createProcessor(assistContext, javaContext, environment);
                        if (processor != null) {
                            if (processor instanceof ITypeResolver) {
                                ((ITypeResolver) processor).setResolverInformation(moduleInfo.module, moduleInfo.resolver);
                            }
                            proposals.addAll(processor.generateProposals(submon.split(1)));
                        }
                    }
                } finally {
                    if (environment != null) {
                        environment.cleanup();
                    }
                }
            }

            // extra filtering and sorting provided by third parties
            try {
                List<IProposalFilter> filters = ProposalProviderRegistry.getRegistry().getFiltersFor(assistContext.unit);
                for (IProposalFilter filter : filters) {
                    try {
                        if (filter instanceof IProposalFilterExtension) {
                            List<ICompletionProposal> newProposals =
                                ((IProposalFilterExtension) filter).filterExtendedProposals(proposals, assistContext, javaContext);
                            if (newProposals != null) {
                                proposals = newProposals;
                            }
                        }
                    } catch (Exception e) {
                        GroovyContentAssist.logError("Exception when using third party proposal filter: " + filter.getClass().getCanonicalName(), e);
                    }
                }
            } catch (CoreException e) {
                GroovyContentAssist.logError("Exception accessing proposal provider registry", e);
            }
        }

        if (traceEvent != null) {
            GroovyLogManager.manager.logEnd(traceEvent, TraceCategory.CONTENT_ASSIST);
        }

        return proposals;
    }

    // visible for testing
    public ContentAssistContext createContentAssistContext(GroovyCompilationUnit gunit, int invocationOffset, IDocument document) {
        String fullCompletionText = findCompletionText(document, invocationOffset);
        String[] completionExpressions = findCompletionExpression(fullCompletionText);
        final String completionExpression;
        if (completionExpressions == null || "@".equals(fullCompletionText)) {
            completionExpression = "";
        } else if (completionExpressions[1] == null) {
            completionExpression = completionExpressions[0];
        } else {
            completionExpression = completionExpressions[1];
        }
        int completionEnd = findCompletionEnd(document, invocationOffset);
        int supportingNodeEnd = findSupportingNodeEnd(gunit, invocationOffset, fullCompletionText);

        CompletionNodeFinder finder = new CompletionNodeFinder(
            invocationOffset,
            completionEnd,
            supportingNodeEnd,
            completionExpression,
            fullCompletionText);
        ContentAssistContext context = finder.findContentAssistContext(gunit);
        return context;
    }

    private SearchableEnvironment createSearchableEnvironment(JavaContentAssistInvocationContext javaContext) {
        try {
            return ((JavaProject) javaContext.getProject()).newSearchableNameEnvironment(javaContext.getCompilationUnit().getOwner());
        } catch (JavaModelException e) {
            GroovyContentAssist.logError("Exception creating searchable environment for " + javaContext.getCompilationUnit(), e);
            return null;
        }
    }

    private String[] findCompletionExpression(String completionText) {
        return new ExpressionFinder().splitForCompletion(completionText);
    }

    protected String findCompletionText(IDocument doc, int offset) {
        String result = null;
        try {
            if (offset > 0) {
                ISourceBuffer buffer = new DocumentSourceBuffer(doc);
                result = new ExpressionFinder().findForCompletions(buffer, offset - 1);
            }
        } catch (ParseException e) {
            // can ignore; probably just invalid code that is being completed at
            if (GroovyLogManager.manager.hasLoggers()) {
                GroovyLogManager.manager.logException(TraceCategory.CONTENT_ASSIST, e);
            }
        }
        return (result != null ? result : "");
    }

    private int findCompletionEnd(IDocument doc, int offset) {
        ISourceBuffer buffer = new DocumentSourceBuffer(doc);
        return new ExpressionFinder().findTokenEnd(buffer, offset);
    }

    protected int findSupportingNodeEnd(GroovyCompilationUnit gunit, int invocationOffset, String fullCompletionText) {
        String[] completionExpressions = new ExpressionFinder().splitForCompletionNoTrim(fullCompletionText);
        // if second part of completion expression is null, then there is no supporting node (ie- no '.')
        if (completionExpressions[1] == null) {
            return -1;
        }
        return (invocationOffset - fullCompletionText.length() + completionExpressions[0].length());
    }

    @Override
    public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
        List<ICompletionProposal> proposals = computeCompletionProposals(context, monitor);
        List<IContextInformation> contexts = new ArrayList<>(proposals.size());
        for (ICompletionProposal proposal : proposals) {
            if (proposal.getContextInformation() != null) {
                contexts.add(proposal.getContextInformation());
            }
        }
        return contexts;
    }

    @Override
    public String getErrorMessage() {
        return "";
    }

    @Override
    public void sessionStarted() {
    }

    @Override
    public void sessionEnded() {
    }
}
