 /*
 * Copyright 2003-2009 the original author or authors.
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

package org.codehaus.groovy.eclipse.codeassist.processors;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.codeassist.completions.GroovyExtendedCompletionContext;
import org.codehaus.groovy.eclipse.codeassist.creators.AbstractProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.creators.CategoryProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.creators.IProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Nov 11, 2009
 *
 */
public class StatementAndExpressionCompletionProcessor extends
        AbstractGroovyCompletionProcessor {

    class ExpressionCompletionRequestor implements ITypeRequestor {

        boolean visitSuccessful = false;
        boolean isStatic = false;
        ClassNode resultingType;

        ClassNode lhsType;
        Set<ClassNode> categories;

        VariableScope currentScope;

        private Expression arrayAccessLHS;

        // keep track of the number of array accesses that must be dereferenced
        private int derefCount = 0;

        public ExpressionCompletionRequestor() {
            // remember the rightmost part of the LHS of a
            // binary expression
            ASTNode maybeLHS = getContext().getPerceivedCompletionNode();
            while (maybeLHS != null) {
                if (maybeLHS instanceof BinaryExpression) {
                    maybeLHS = arrayAccessLHS = ((BinaryExpression) maybeLHS)
                            .getLeftExpression();
                    derefCount++;
                } else if (maybeLHS instanceof PropertyExpression) {
                    arrayAccessLHS = ((PropertyExpression) maybeLHS)
                            .getObjectExpression();
                    maybeLHS = ((PropertyExpression) maybeLHS).getProperty();
                } else if (maybeLHS instanceof MethodCallExpression) {
                    arrayAccessLHS = ((MethodCallExpression) maybeLHS)
                            .getObjectExpression();
                    maybeLHS = ((MethodCallExpression) maybeLHS).getMethod();
                } else {
                    if (maybeLHS instanceof Expression) {
                        arrayAccessLHS = (Expression) maybeLHS;
                    }
                    maybeLHS = null;
                }
            }
        }

        public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result,
                IJavaElement enclosingElement) {

            // check to see if the enclosing element does not enclose the nodeToLookFor
            if (!interestingElement(enclosingElement)) {
                return VisitStatus.CANCEL_MEMBER;
            }

            if (node instanceof ClassNode) {
                ClassNode clazz = (ClassNode) node;
                if (clazz.redirect() == clazz && clazz.isScript()) {
                    return VisitStatus.CONTINUE;
                }
            } else if (node instanceof MethodNode) {
                MethodNode run = (MethodNode) node;
                if (run.getName().equals("run")
                        && run.getDeclaringClass().isScript()
                        && (run.getParameters() == null || run.getParameters().length == 0)) {
                    return VisitStatus.CONTINUE;
                }
            }

            boolean success = doTest(node);
            boolean derefList = false; // if true use the parameterized type of
                                       // the list
            if (!success) {
                // maybe this is content assist after array access
                // ie- foo[0].<_>
                derefList = success = doTestForAfterArrayAccess(node);
            }
            if (success) {
                maybeRememberLHSType(result);
                resultingType = findResultingType(result, derefList);

                categories = result.scope.getCategoryNames();
                visitSuccessful = true;
                isStatic = node instanceof StaticMethodCallExpression ||
                    (node instanceof ClassExpression &&
                     // if we are completing on '.class' then never static context
                     resultingType != VariableScope.CLASS_CLASS_NODE);
                currentScope = result.scope;
                return VisitStatus.STOP_VISIT;
            }
            return VisitStatus.CONTINUE;
        }

        private ClassNode findResultingType(TypeLookupResult result, boolean derefList) {
            // if completing on a method call with an implicit 'this'.
            ClassNode candidate = getContext().location == ContentAssistLocation.METHOD_CONTEXT ? result.declaringType
                    : result.type;
            if (derefList) {
                for (int i = 0; i < derefCount; i++) {

                    // GRECLIPSE-742: does the LHS type have a 'getAt' method?
                    boolean getAtFound = false;
                    List<MethodNode> getAts = candidate.getMethods("getAt");
                    for (MethodNode getAt : getAts) {
                        if (getAt.getParameters() != null && getAt.getParameters().length == 1) {
                            candidate = getAt.getReturnType();
                            getAtFound = true;
                        }
                    }

                    if (!getAtFound) {
                        if (VariableScope.MAP_CLASS_NODE.equals(candidate)) {
                            // for maps, always use the type of value
                            candidate = candidate.getGenericsTypes()[1].getType();
                        } else {
                            for (int j = 0; j < derefCount; j++) {
                                candidate = VariableScope.extractElementType(candidate);
                            }
                        }
                    }
                }
            }

            // now look at spread expressions
            // might be part of a spread operation
            boolean extractElementType = false; // for spread operations
            ASTNode enclosing = result.scope.getEnclosingNode();
            if (enclosing instanceof MethodCallExpression) {
                extractElementType = ((MethodCallExpression) enclosing).isSpreadSafe();
            } else if (enclosing instanceof PropertyExpression) {
                extractElementType = ((PropertyExpression) enclosing).isSpreadSafe();
            }
            if (extractElementType) {
                candidate = VariableScope.extractElementType(candidate);
            }
            if (ClassHelper.isPrimitiveType(candidate)) {
                candidate = ClassHelper.getWrapper(candidate);
            }
            return candidate;
        }

        /**
         * see if this is the lhs of an array access, eg the 'foo' of 'foo[0]'
         */
        private boolean doTestForAfterArrayAccess(ASTNode node) {
            return node == arrayAccessLHS;
        }

        private void maybeRememberLHSType(TypeLookupResult result) {
            if (isAssignmentOfLhs(result.getEnclosingAssignment())) {
                // check to see if this is the rhs of an assignment.
                // if so, then attempt to use the type of the lhs for
                // ordering of the proposals
                if (lhsNode instanceof Variable) {
                    Variable variable = (Variable) lhsNode;
                    VariableInfo info = result.scope
                            .lookupName(variable.getName());
                    ClassNode maybeType;
                    if (info != null) {
                        maybeType = info.type;
                    } else {
                        maybeType = variable.getType();
                    }
                    if (maybeType != null && !maybeType.equals(VariableScope.OBJECT_CLASS_NODE)) {
                        lhsType = ClassHelper.getUnwrapper(maybeType);
                    }
                }
            }
        }

        private boolean isAssignmentOfLhs(BinaryExpression node) {
            if (node != null && lhsNode != null) {
                Expression expression = node.getLeftExpression();
                return expression.getClass() == lhsNode.getClass() && expression.getStart() == lhsNode.getStart()
                        && expression.getEnd() == lhsNode.getEnd();
            }
            return false;
        }

        private boolean doTest(ASTNode node) {
            if (node instanceof ArgumentListExpression) {
                // || node instanceof PropertyExpression) {
                // we never complete on a list of arguments, but rather one of the arguments itself
                // also, never do a completion on the property expression, but rather on the
                // propertyExpression.getProperty() node
                return false;
            } else if (node instanceof BinaryExpression) {
                BinaryExpression bin = (BinaryExpression) node;
                if (bin.getLeftExpression() == arrayAccessLHS) {
                    // don't return true here, but rather wait for the LHS to
                    // come through
                    // this way we can use the derefed value as the completion
                    // type
                    return false;
                }
            }
            return isNotExpressionAndStatement(completionNode, node) && completionNode.getStart() == node.getStart() && completionNode.getEnd() == node.getEnd();
        }

        /**
         * @param enclosingElement
         * @return true iff enclosingElement's source location contains the source location of
         *         {@link #nodeToLookFor}
         */
        private boolean interestingElement(IJavaElement enclosingElement) {
            // the clinit is always interesting since the clinit contains static initializers
            if (enclosingElement.getElementName().equals("<clinit>")) {
                return true;
            }

            if (enclosingElement instanceof ISourceReference) {
                try {
                    ISourceRange range = ((ISourceReference) enclosingElement)
                            .getSourceRange();
                    return range.getOffset() <= completionNode.getStart()
                            && range.getOffset() + range.getLength() >= completionNode
                                    .getEnd();
                } catch (JavaModelException e) {
                    Util.log(e);
                }
            }
            return false;
        }

        private boolean isNotExpressionAndStatement(ASTNode thisNode, ASTNode otherNode) {
            if (thisNode instanceof Expression) {
                return !(otherNode instanceof Statement);
            } else if (thisNode instanceof Statement) {
                return !(otherNode instanceof Expression);
            } else {
                return true;
            }
        }

        public ClassNode getResultingType() {
            return resultingType;
        }
        public Set<ClassNode> getCategories() {
            return categories;
        }

        public boolean isVisitSuccessful() {
            return visitSuccessful;
        }
    }

    /**
     * the ASTNode being completed.
     */
    final ASTNode completionNode;

    /**
     * the LHS of the assignment statement associated with this content assist
     * invocation, or null if there is none.
     */
    final Expression lhsNode;

    public StatementAndExpressionCompletionProcessor(ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext,
            SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
        this.completionNode = context.getPerceivedCompletionNode();
        this.lhsNode = context.lhsNode;
    }

    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        TypeInferencingVisitorFactory factory = new TypeInferencingVisitorFactory();
        ContentAssistContext context = getContext();
        TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(context.unit);
        ExpressionCompletionRequestor requestor = new ExpressionCompletionRequestor();

        // if completion node is null, then it is likely because of a syntax error
        if (completionNode != null) {
            visitor.visitCompilationUnit(requestor);
        }
        ClassNode completionType;
        boolean isStatic;
        List<IGroovyProposal> groovyProposals = new LinkedList<IGroovyProposal>();
        if (requestor.isVisitSuccessful()) {
            // get all proposal creators
            isStatic = isStatic() || requestor.isStatic;
            IProposalCreator[] creators = getAllProposalCreators();
            completionType = getCompletionType(requestor);
            if (completionType == null) {
                completionType = context.containingDeclaration instanceof ClassNode ? (ClassNode) context.containingDeclaration
                        : context.unit.getModuleNode().getScriptClassDummy();
            }
            proposalCreatorLoop(context, requestor, completionType, isStatic, groovyProposals, creators, false);
            if (ContentAssistLocation.STATEMENT == context.location) {
                ClassNode closureThis = requestor.currentScope.getThis();
                if (closureThis != null && !closureThis.equals(completionType)) {
                    // inside of a closure
                    // must also add content assist for this (previously did the
                    // delegate)
                    proposalCreatorLoop(context, requestor, closureThis, isStatic, groovyProposals, creators, true);
                }
            }
        } else {
            // we are at the statement location of a script
            // return the category proposals only
            AnnotatedNode node = context.containingDeclaration;
            ClassNode containingClass;
            if (node instanceof ClassNode) {
                containingClass = (ClassNode) node;
            } else if (node instanceof MethodNode) {
                containingClass = ((MethodNode) node).getDeclaringClass();
            } else {
                containingClass = null;
            }
            if (containingClass != null) {
                groovyProposals.addAll(new CategoryProposalCreator().findAllProposals(containingClass,
                        VariableScope.ALL_DEFAULT_CATEGORIES, context.getPerceivedCompletionExpression(), false,
                        ContentAssistLocation.STATEMENT == context.location));
            }
            completionType = context.containingDeclaration instanceof ClassNode ? (ClassNode) context.containingDeclaration
                    : context.unit.getModuleNode().getScriptClassDummy();

            isStatic = false;
        }

        // get proposals from providers
        try {
            context.currentScope = requestor.currentScope != null ? requestor.currentScope
                    : createTopLevelScope(completionType);
            List<IProposalProvider> providers = ProposalProviderRegistry.getRegistry().getProvidersFor(context.unit);
            for (IProposalProvider provider : providers) {
                try {
                    List<IGroovyProposal> otherProposals = provider
                            .getStatementAndExpressionProposals(context,
                                    completionType, isStatic,
                                    requestor.categories);
                    if (otherProposals != null) {
                        groovyProposals.addAll(otherProposals);
                    }
                } catch (Exception e) {
                    GroovyCore
                            .logException(
                                    "Exception when using third party proposal provider: "
                                            + provider.getClass()
                                                    .getCanonicalName(), e);
                }
            }
        } catch (CoreException e) {
            GroovyCore.logException("Exception accessing proposal provider registry", e);
        }

        fillInExtendedContext(requestor);

        // extra filtering and sorting provided by third parties
        try {
            List<IProposalFilter> filters = ProposalProviderRegistry
                    .getRegistry().getFiltersFor(context.unit);
            for (IProposalFilter filter : filters) {
                try {
                    List<IGroovyProposal> newProposals = filter
                            .filterProposals(groovyProposals, context,
                                    getJavaContext());
                    groovyProposals = newProposals == null ? groovyProposals
                            : newProposals;
                } catch (Exception e) {
                    GroovyCore.logException(
                            "Exception when using third party proposal filter: "
                                    + filter.getClass().getCanonicalName(), e);
                }
            }
        } catch (CoreException e) {
            GroovyCore.logException(
                    "Exception accessing proposal provider registry", e);
        }

        List<ICompletionProposal> javaProposals = new ArrayList<ICompletionProposal>(groovyProposals.size());
        JavaContentAssistInvocationContext javaContext = getJavaContext();
        for (IGroovyProposal groovyProposal : groovyProposals) {
            try {
                IJavaCompletionProposal javaProposal = groovyProposal.createJavaProposal(context, javaContext);
                if (javaProposal != null) {
                    javaProposals.add(javaProposal);
                }
            } catch (Exception e) {
                GroovyCore.logException(
                                "Exception when creating groovy completion proposal",
                                e);
            }
        }

        return javaProposals;
    }

    private void proposalCreatorLoop(ContentAssistContext context, ExpressionCompletionRequestor requestor,
            ClassNode completionType, boolean isStatic, List<IGroovyProposal> groovyProposals, IProposalCreator[] creators,
            boolean isClosureThis) {
        for (IProposalCreator creator : creators) {
            if (isClosureThis && !creator.redoForLoopClosure()) {
                // avoid duplicate DGMs by not proposing category proposals
                // twice
                continue;
            }
            if (creator instanceof AbstractProposalCreator) {
                ((AbstractProposalCreator) creator).setLhsType(requestor.lhsType);
                ((AbstractProposalCreator) creator).setCurrentScope(requestor.currentScope);
            }
            groovyProposals.addAll(creator.findAllProposals(completionType, requestor.categories,
                    context.getPerceivedCompletionExpression(), isStatic, ContentAssistLocation.STATEMENT == context.location));
        }
    }

    /**
     * @param requestor
     */
    private void fillInExtendedContext(ExpressionCompletionRequestor requestor) {
        JavaContentAssistInvocationContext javaContext = getJavaContext();
        CompletionContext coreContext = javaContext.getCoreContext();
        if (coreContext != null && !coreContext.isExtended()) {
            // must use reflection to set the fields
            ReflectionUtils.setPrivateField(InternalCompletionContext.class, "isExtended", coreContext, true);
            ReflectionUtils.setPrivateField(InternalCompletionContext.class, "extendedContext", coreContext,
                    new GroovyExtendedCompletionContext(getContext(), requestor.currentScope));
        }
    }

    /**
     * @param completionType
     * @return
     */
    protected VariableScope createTopLevelScope(ClassNode completionType) {
        VariableScope scope = new VariableScope(null, completionType, false);
        return scope;
    }

    /**
     * When completing an expression, use the completion type found by the requestor. Otherwise, use
     * the current type
     * @param requestor
     * @return
     */
    private ClassNode getCompletionType(ExpressionCompletionRequestor requestor) {
        if (getContext().location == ContentAssistLocation.EXPRESSION) {
            return requestor.resultingType;
        } else if (getContext().location == ContentAssistLocation.METHOD_CONTEXT) {
            // if we are completing on a variable expression here, that means
            // we have something like this:
            // myMethodCall _
            // so, we want to look at the type of 'this' to complete on
            return completionNode instanceof VariableExpression ? requestor.currentScope.getDelegateOrThis()
                    : requestor.resultingType;
        } else {
            // use the current 'this' type so that closure types are correct
            ClassNode type = requestor.currentScope.getDelegateOrThis();
            if (type != null) {
                return type;
            } else {
                // will only happen if in top level scope
                return requestor.resultingType;
            }
        }
    }

    /**
     * When completing a expression, static context exists only if a ClassExpression
     * When completing a statement, static context exists if in a static method or field
     * @return true iff static
     */
    private boolean isStatic() {
        if (getContext().location == ContentAssistLocation.STATEMENT) {
            AnnotatedNode annotated = getContext().containingDeclaration;
            if (annotated instanceof FieldNode) {
                return ((FieldNode) annotated).isStatic();
            } else if (annotated instanceof MethodNode) {
                return ((MethodNode) annotated).isStatic();
            }
        }
        return false;
    }
}
