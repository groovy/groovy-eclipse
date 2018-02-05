/*
 * Copyright 2009-2018 the original author or authors.
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.creators.AbstractProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.creators.CategoryProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.creators.FieldProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.creators.IProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.creators.MethodProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyFieldProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.search.AccessorSupport;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class StatementAndExpressionCompletionProcessor extends AbstractGroovyCompletionProcessor {

    private class ExpressionCompletionRequestor implements ITypeRequestor {

        /** number of array accesses that must be dereferenced */
        private int derefCount;
        private boolean isStatic;
        private ClassNode lhsType;
        private ClassNode resultingType;
        private Set<ClassNode> categories;
        private Expression arrayAccessLHS;
        private VariableScope currentScope;

        private boolean visitSuccessful;

        private ExpressionCompletionRequestor() {
            // remember the rightmost part of the LHS of a binary expression
            ASTNode maybeLHS = getContext().getPerceivedCompletionNode();
            while (maybeLHS != null) {
                if (maybeLHS instanceof BinaryExpression) {
                    maybeLHS = arrayAccessLHS = ((BinaryExpression) maybeLHS).getLeftExpression();
                    derefCount += 1;
                } else if (maybeLHS instanceof PropertyExpression) {
                    arrayAccessLHS = ((PropertyExpression) maybeLHS).getObjectExpression();
                    maybeLHS = ((PropertyExpression) maybeLHS).getProperty();
                } else if (maybeLHS instanceof MethodCallExpression) {
                    arrayAccessLHS = ((MethodCallExpression) maybeLHS).getObjectExpression();
                    maybeLHS = ((MethodCallExpression) maybeLHS).getMethod();
                } else {
                    if (maybeLHS instanceof Expression) {
                        arrayAccessLHS = (Expression) maybeLHS;
                    }
                    maybeLHS = null;
                }
            }
        }

        @Override
        public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
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
                if (run.getName().equals("run") &&
                        run.getDeclaringClass().isScript() &&
                        (run.getParameters() == null || run.getParameters().length == 0)) {
                    return VisitStatus.CONTINUE;
                }
            } else if (node == lhsNode) {
                // NOTE: this should be mutually exclusive to maybeRememberTypeOfLHS()
                // save the inferred type of the LHS node for ranking of the proposals
                if (result.confidence != TypeConfidence.UNKNOWN) {
                    if (result.declaration instanceof MethodNode) {
                        MethodNode meth = (MethodNode) result.declaration;
                        if (AccessorSupport.SETTER.isAccessorKind(meth, false)) {
                            lhsType = meth.getParameters()[0].getType();
                        }
                    } else {
                        lhsType = result.type;
                    }
                    if (VariableScope.OBJECT_CLASS_NODE.equals(lhsType)) {
                        lhsType = null;
                    }
                }
            }

            boolean derefList = false; // if true use the parameterized type of the list
            boolean success = doTest(node);
            if (!success) {
                // maybe this is content assist after array access, i.e. foo[0]._
                derefList = success = doTestForAfterArrayAccess(node);
            }
            if (success) {
                maybeRememberTypeOfLHS(result);
                categories = result.scope.getCategoryNames();
                resultingType = findResultingType(result, derefList);
                visitSuccessful = true;
                isStatic = node instanceof StaticMethodCallExpression ||
                    // if we are completing on '.class' then never static context
                    (node instanceof ClassExpression && !resultingType.equals(VariableScope.CLASS_CLASS_NODE));
                currentScope = result.scope;
                return VisitStatus.STOP_VISIT;
            }
            return VisitStatus.CONTINUE;
        }

        private ClassNode findResultingType(TypeLookupResult result, boolean derefList) {
            // if completing on a method call with an implicit 'this'.
            ClassNode candidate = getContext().location == ContentAssistLocation.METHOD_CONTEXT ? result.declaringType : result.type;
            if (derefList) {
                for (int i = 0; i < derefCount; i += 1) {
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
                            for (int j = 0; j < derefCount; j += 1) {
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
         * Determines if this is the lhs of an array access -- the 'foo' of 'foo[0]'.
         */
        private boolean doTestForAfterArrayAccess(ASTNode node) {
            return node == arrayAccessLHS;
        }

        private void maybeRememberTypeOfLHS(TypeLookupResult result) {
            VariableScope.CallAndType cat;
            if (isAssignmentOfLHS(result.enclosingAssignment)) {
                // check to see if this is the rhs of an assignment.
                // if so, then attempt to use the type of the lhs for
                // ordering of the proposals
                if (lhsNode instanceof Variable) {
                    Variable variable = (Variable) lhsNode;
                    VariableInfo info = result.scope.lookupName(variable.getName());
                    lhsType = (info != null ? info.type : variable.getType());
                }/* else if (lhsNode instanceof FieldExpression) {
                    lhsType = lhsNode.getType();
                }*/ else if (lhsNode instanceof PropertyExpression) {
                    lhsType = ((PropertyExpression) lhsNode).getProperty().getType();
                }
            } else if ((cat = result.scope.getEnclosingMethodCallExpression()) != null) {
                // is a method parameter the receiver of the completion expression?
                int paramIndex = getParameterPosition(result.declaration, cat.call);
                if (paramIndex >= 0 && cat.declaration instanceof MethodNode) {
                    Parameter[] params = ((MethodNode) cat.declaration).getParameters();
                    lhsType = params[Math.min(paramIndex, params.length - 1)].getType();
                    if (lhsType.isArray() && paramIndex >= params.length - 1) {
                        lhsType = lhsType.getComponentType();
                    }
                }
            }
            if (VariableScope.OBJECT_CLASS_NODE.equals(lhsType)) {
                lhsType = null;
            }
        }

        private boolean isAssignmentOfLHS(BinaryExpression node) {
            if (node != null && lhsNode != null) {
                Expression expression = node.getLeftExpression();
                return expression == lhsNode || (expression.getClass() == lhsNode.getClass() &&
                    expression.getStart() == lhsNode.getStart() && expression.getEnd() == lhsNode.getEnd());
            }
            return false;
        }

        private int getParameterPosition(ASTNode argumentCandidate, MethodCallExpression callExpression) {
            if (callExpression != null && callExpression.getArguments() instanceof TupleExpression) {
                int paramIndex = -1;
                for (Expression argument : ((TupleExpression) callExpression.getArguments()).getExpressions()) {
                    paramIndex += 1;
                    if (argument == argumentCandidate) {
                        return paramIndex;
                    }
                }
            }
            return -1;
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
         * @return {@code true} iff enclosingElement's source location contains the source location of {@link #nodeToLookFor}
         */
        private boolean interestingElement(IJavaElement enclosingElement) {
            // the clinit is always interesting since the clinit contains static initializers
            if (enclosingElement.getElementName().equals("<clinit>")) {
                return true;
            }

            if (enclosingElement instanceof ISourceReference) {
                try {
                    ISourceRange range = ((ISourceReference) enclosingElement).getSourceRange();
                    return range.getOffset() <= completionNode.getStart() &&
                        range.getOffset() + range.getLength() >= completionNode.getEnd();
                } catch (JavaModelException e) {
                    GroovyContentAssist.logError(e);
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
    }

    /**
     * The ASTNode being completed.
     */
    private final ASTNode completionNode;

    /**
     * The LHS of the assignment associated with this content assist invocation, or {@code null} if there is none.
     */
    private final ASTNode lhsNode;

    public StatementAndExpressionCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
        this.completionNode = context.getPerceivedCompletionNode();
        this.lhsNode = context.lhsNode;
    }

    @Override
    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        ContentAssistContext context = getContext();
        TypeInferencingVisitorWithRequestor visitor =
            new TypeInferencingVisitorFactory().createVisitor(context.unit);
        ExpressionCompletionRequestor requestor = new ExpressionCompletionRequestor();

        // if completion node is null, then it is likely because of a syntax error
        if (completionNode != null) {
            visitor.visitCompilationUnit(requestor);
        }

        List<IGroovyProposal> groovyProposals = new ArrayList<>();
        boolean isPrimary = (context.location == ContentAssistLocation.STATEMENT);
        boolean isStatic; ClassNode completionType;

        if (requestor.visitSuccessful) {
            isStatic = requestor.isStatic;
            context.lhsType = requestor.lhsType;
            completionType = getCompletionType(requestor);

            // TODO: if (isPrimary && requestor.currentScope.getEnclosingClosure() != null)
            //       check closure's resolve strategy; delegate and/or owner may be excluded and Closure may be included

            ClassNode closureCompletionType = null;
            if (isPrimary) {
                ClassNode thisType = requestor.currentScope.getThis();
                if (thisType != null && !thisType.equals(completionType)) {
                    closureCompletionType = thisType; // aka the "owner" type
                }
            }

            IProposalCreator[] creators = chooseProposalCreators();
            boolean isStatic1 = (isStatic && closureCompletionType == null);
            // if completionType refers to the closure delegate, use instance (non-static) semantics
            proposalCreatorLoop(context, requestor, completionType, isStatic1, groovyProposals, creators, false);

            if (completionType.equals(VariableScope.CLASS_CLASS_NODE) &&
                    !completionType.getGenericsTypes()[0].getType().equals(VariableScope.CLASS_CLASS_NODE) &&
                    !completionType.getGenericsTypes()[0].getType().equals(VariableScope.OBJECT_CLASS_NODE)) {
                // "Foo.bar" and "Foo.@bar" are static; "Foo.&bar" is not static
                boolean isStatic2 = !METHOD_POINTER_COMPLETION.matcher(context.fullCompletionExpression).matches();
                proposalCreatorLoop(context, requestor, completionType.getGenericsTypes()[0].getType(), isStatic2, groovyProposals, creators, false);
            }

            if (closureCompletionType != null) {
                // inside of a closure; must also add content assist for this (previously did the delegate)
                proposalCreatorLoop(context, requestor, closureCompletionType, isStatic, groovyProposals, creators, true);
            }

            if (isPrimary) {
                // if receiver type is an enum, propose its constants directly
                if (context.lhsType != null && context.lhsType.isEnum() && !context.lhsType.equals(completionType)) {
                    List<IGroovyProposal> enumFields = new FieldProposalCreator().findAllProposals(
                        context.lhsType, Collections.EMPTY_SET, context.getPerceivedCompletionExpression(), true, true);
                    for (Iterator<IGroovyProposal> it = enumFields.iterator(); it.hasNext();) {
                        GroovyFieldProposal proposal = (GroovyFieldProposal) it.next();
                        if (proposal.getField().isEnum()) {
                            proposal.setRequiredStaticImport(
                                context.lhsType.getName() + '.' + proposal.getField().getName());
                        } else {
                            it.remove();
                        }
                    }
                    groovyProposals.addAll(enumFields);
                }
            }
        } else {
            isStatic = false; // why?
            completionType = (context.containingDeclaration instanceof ClassNode
                ? (ClassNode) context.containingDeclaration : context.unit.getModuleNode().getScriptClassDummy());

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
                groovyProposals.addAll(new CategoryProposalCreator().findAllProposals(containingClass, VariableScope.ALL_DEFAULT_CATEGORIES, context.getPerceivedCompletionExpression(), false, isPrimary));
            } else if (node instanceof ImportNode) {
                ImportNode importNode = (ImportNode) node;
                if (importNode.isStatic()) {
                    containingClass = importNode.getType();
                    groovyProposals.addAll(new FieldProposalCreator().findAllProposals(containingClass, VariableScope.ALL_DEFAULT_CATEGORIES, context.getPerceivedCompletionExpression(), true, isPrimary));
                    groovyProposals.addAll(new MethodProposalCreator().findAllProposals(containingClass, VariableScope.ALL_DEFAULT_CATEGORIES, context.getPerceivedCompletionExpression(), true, isPrimary));
                }
            }
        }

        // get proposals from providers (TODO: Should this move into proposalCreatorLoop?)
        try {
            context.currentScope = requestor.currentScope != null ? requestor.currentScope : createTopLevelScope(completionType);
            List<IProposalProvider> providers = ProposalProviderRegistry.getRegistry().getProvidersFor(context.unit);
            for (IProposalProvider provider : providers) {
                try {
                    List<IGroovyProposal> otherProposals = provider.getStatementAndExpressionProposals(context, completionType, isStatic, requestor.categories);
                    if (otherProposals != null) {
                        groovyProposals.addAll(otherProposals);
                    }
                } catch (Exception e) {
                    GroovyContentAssist.logError("Exception when using third party proposal provider: " + provider.getClass().getCanonicalName(), e);
                }
            }
        } catch (CoreException e) {
            GroovyContentAssist.logError("Exception accessing proposal provider registry", e);
        }

        getContext().extend(getJavaContext().getCoreContext(), requestor.currentScope);

        // extra filtering and sorting provided by third parties
        try {
            List<IProposalFilter> filters = ProposalProviderRegistry.getRegistry().getFiltersFor(context.unit);
            for (IProposalFilter filter : filters) {
                try {
                    List<IGroovyProposal> newProposals = filter.filterProposals(groovyProposals, context, getJavaContext());
                    if (newProposals != null) {
                        groovyProposals = newProposals;
                    }
                } catch (Exception e) {
                    GroovyContentAssist.logError("Exception when using third party proposal filter: " + filter.getClass().getCanonicalName(), e);
                }
            }
        } catch (CoreException e) {
            GroovyContentAssist.logError("Exception accessing proposal provider registry", e);
        }

        List<ICompletionProposal> javaProposals = new ArrayList<>(groovyProposals.size());
        JavaContentAssistInvocationContext javaContext = getJavaContext();
        for (IGroovyProposal groovyProposal : groovyProposals) {
            try {
                IJavaCompletionProposal javaProposal = groovyProposal.createJavaProposal(context, javaContext);
                if (javaProposal != null) {
                    javaProposals.add(javaProposal);
                }
            } catch (Exception e) {
                GroovyContentAssist.logError("Exception when creating groovy completion proposal", e);
            }
        }

        return javaProposals;
    }

    private void proposalCreatorLoop(ContentAssistContext context, ExpressionCompletionRequestor requestor, ClassNode completionType, boolean isStatic, List<IGroovyProposal> groovyProposals, IProposalCreator[] creators, boolean isClosureThis) {
        for (IProposalCreator creator : creators) {
            if (isClosureThis && !creator.redoForLoopClosure()) {
                // avoid duplicate DGMs by not proposing category proposals twice
                continue;
            }
            if (creator instanceof AbstractProposalCreator) {
                ((AbstractProposalCreator) creator).setCurrentScope(requestor.currentScope);
                ((AbstractProposalCreator) creator).setFavoriteStaticMembers(context.getFavoriteStaticMembers());
            }
            Set<ClassNode> categories = requestor.categories;
            String expression = context.getPerceivedCompletionExpression();
            boolean isPrimary = (context.location == ContentAssistLocation.STATEMENT);
            groovyProposals.addAll(
                creator.findAllProposals(completionType, categories, expression, isStatic, isPrimary));
        }
    }

    protected VariableScope createTopLevelScope(ClassNode completionType) {
        VariableScope scope = new VariableScope(null, completionType, false);
        return scope;
    }

    private ClassNode getCompletionType(ExpressionCompletionRequestor requestor) {
        ClassNode completionType;
        ContentAssistContext context = getContext();

        switch (context.location) {
        case EXPRESSION:
            completionType = requestor.resultingType;
            break;
        case METHOD_CONTEXT:
            // completing on a variable expression here, that means we have something like: myMethodCall _
            // so, we want to look at the type of 'this' to complete on
            completionType = (completionNode instanceof VariableExpression
                ? requestor.currentScope.getDelegateOrThis() : requestor.resultingType);
            break;
        default:
            // use the current 'this' type so that closure types are correct
            completionType = requestor.currentScope.getDelegateOrThis();
            if (completionType == null) {
                // will only happen if in top level scope
                completionType = requestor.resultingType;
            }
        }

        if (completionType == null) {
            if (context.containingDeclaration instanceof ClassNode) {
                completionType = (ClassNode) context.containingDeclaration;
            } else {
                completionType = context.unit.getModuleNode().getScriptClassDummy();
            }
        }

        return completionType;
    }

    private IProposalCreator[] chooseProposalCreators() {
        String completionExpression = getContext().fullCompletionExpression;
        if (completionExpression == null) completionExpression = "";
        if (FIELD_ACCESS_COMPLETION.matcher(completionExpression).matches()) {
            return new IProposalCreator[] {new FieldProposalCreator()};
        }
        if (METHOD_POINTER_COMPLETION.matcher(completionExpression).matches()) {
            return new IProposalCreator[] {new MethodProposalCreator()};
        }
        return getAllProposalCreators();
    }

    public static final Pattern FIELD_ACCESS_COMPLETION =  Pattern.compile(".+\\.@\\s*(?:\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)?", Pattern.DOTALL);
    public static final Pattern METHOD_POINTER_COMPLETION = Pattern.compile(".+\\.&\\s*(?:\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)?", Pattern.DOTALL);
}
