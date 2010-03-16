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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.codeassist.proposals.CategoryProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.IProposalCreator;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
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
        Set<ClassNode> categories;
        public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result,
                IJavaElement enclosingElement) {
            
            if (doTest(node)) {
                resultingType = result.type;
                categories = result.scope.getCategoryNames();
                visitSuccessful = true;
                isStatic = node instanceof StaticMethodCallExpression ||
                    (node instanceof ClassExpression && 
                     // if we are completing on '.class' then never static context
                     resultingType != VariableScope.CLASS_CLASS_NODE);
                return VisitStatus.STOP_VISIT;
            }
            return VisitStatus.CONTINUE;
        }

        /**
         * @param node
         * @return
         */
        private boolean doTest(ASTNode node) {
            if (node instanceof ArgumentListExpression) {
                // we never complete on a list of arguments, but rather one of the arguments itself
                return false;
            }
            return isNotExpressionAndStatement(completionNode, node) && completionNode.getStart() == node.getStart() && completionNode.getEnd() == node.getEnd();
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
    
    final ASTNode completionNode;

    public StatementAndExpressionCompletionProcessor(ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext,
            SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
        this.completionNode = context.completionNode;
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
            for (IProposalCreator creator : creators) {
                groovyProposals.addAll(creator.findAllProposals(completionType, requestor.categories, 
                        context.completionExpression, isStatic));
            }
        } else {
            // we are at the statement location of a script
            // return the category proposals only
            AnnotatedNode node = context.containingDeclaration;
            ClassNode containingClass;
            if (node instanceof MethodNode) {
                containingClass = (ClassNode) node;
            } else if (node instanceof MethodNode) {
                containingClass = ((MethodNode) node).getDeclaringClass();
            } else {
                containingClass = null;
            }
            if (containingClass != null) {
                groovyProposals.addAll(new CategoryProposalCreator().findAllProposals(containingClass, 
                        Collections.singleton(VariableScope.DGM_CLASS_NODE), context.completionExpression, false));
            }
            completionType = null;
            isStatic = false;
        }
        
        // get proposals from providers
        try {
            List<IProposalProvider> providers = ProposalProviderRegistry.getRegistry().getProvidersFor(context.unit);
            for (IProposalProvider provider : providers) {
                List<IGroovyProposal> otherProposals = provider.getStatementAndExpressionProposals(context, completionType, isStatic, requestor.categories);
                if (otherProposals != null) {
                    groovyProposals.addAll(otherProposals);
                }
            }
        } catch (CoreException e) {
            GroovyCore.logException("Exception accessing proposal provider registry", e);
        }
        
        // filter??? sort???
        List<ICompletionProposal> javaProposals = new ArrayList<ICompletionProposal>(groovyProposals.size());
        JavaContentAssistInvocationContext javaContext = getJavaContext();
        for (IGroovyProposal groovyProposal : groovyProposals) {
            javaProposals.add(groovyProposal.createJavaProposal(context, javaContext));
        }

        return javaProposals;
    }

    /**
     * When completing an expression, use the completion type found by the requestor.
     * Otherwise, use the current type 
     * @param requestor
     * @return
     */
    private ClassNode getCompletionType(ExpressionCompletionRequestor requestor) {
         return getContext().location == ContentAssistLocation.EXPRESSION ? requestor.resultingType : 
             getContext().getEnclosingGroovyType();
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
