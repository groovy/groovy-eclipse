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
package org.codehaus.groovy.eclipse.codeassist.requestor;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.search.ITypeRequestor.VisitStatus;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.ui.PreferenceConstants;

public class ContentAssistContext {

    /**
     * the caret location where completion occurs
     */
    public final int completionLocation;

    /**
     * The location of the end of the token being completed on.
     *
     * If the completion token is someTh^. then the completionEnd is the char offset of the 'h'. If
     * the completion token is someTh^ing (ie- content assist after the 'h', but there is still more
     * to the completion token) then the completionEnd is the location of the 'g'.
     */
    public final int completionEnd;

    /**
     * the phrase that is being completed. not null, but might be empty. if the full phrase is
     * foo.bar.baz.someTh^, the completion expression will be someTh
     */
    public final String completionExpression;

    /**
     * the full phrase of the entire statement being completed. if the full phrase is
     * foo.bar.baz.someTh^, then that will be the fullCompletionExpression
     */
    public final String fullCompletionExpression;

    /**
     * the ast node that provides the type being completed. might be null if
     * there is none
     */
    public final ASTNode completionNode;

    /**
     * the import, method, field, class, or code block that contains this
     * completion request
     */
    public final ASTNode containingCodeBlock;

    /**
     * the left hand side of the assignment statement containing the completion
     * node, or null if there is none
     */
    public final Expression lhsNode;
    public ClassNode lhsType;

    /**
     * the location kind of this content assist invocation
     */
    public final ContentAssistLocation location;

    /**
     * The JDT compilation unit that contains this assist invocation
     */
    public final GroovyCompilationUnit unit;

    /**
     * the class, method or field containing the completion location
     */
    public final AnnotatedNode containingDeclaration;

    /**
     * The {@link VariableScope} at the requested location. Might be null if a
     * {@link TypeInferencingVisitorWithRequestor} has not been sent down the AST yet.
     */
    public VariableScope currentScope;

    private Set<String> favoriteStaticMembers;

    public ContentAssistContext(
            int completionLocation,
            String completionExpression,
            String fullCompletionExpression,
            ASTNode completionNode,
            ASTNode containingCodeBlock,
            Expression lhsNode,
            ContentAssistLocation location,
            GroovyCompilationUnit unit,
            AnnotatedNode containingDeclaration,
            int completionEnd) {
        this.completionLocation = completionLocation;
        this.completionExpression = completionExpression;
        this.fullCompletionExpression = fullCompletionExpression;
        this.completionNode = completionNode;
        this.containingCodeBlock = containingCodeBlock;
        this.lhsNode = lhsNode;
        this.location = location;
        this.unit = unit;
        this.containingDeclaration = containingDeclaration;
        this.completionEnd = completionEnd;
    }

    public IType getEnclosingType() {
        try {
            IJavaElement element = unit.getElementAt(completionLocation);
            if (element != null) {
                return (IType) element.getAncestor(IJavaElement.TYPE);
            }
        } catch (JavaModelException e) {
            GroovyContentAssist.logError("Exception finding completion for " + unit, e);
        }
        return null;
    }

    public ClassNode getEnclosingGroovyType() {
        if (containingDeclaration instanceof ClassNode) {
            return (ClassNode) containingDeclaration;
        } else {
            return containingDeclaration.getDeclaringClass();
        }
    }

    /**
     * @see org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer.getFavoriteStaticMembers()
     */
    public Set<String> getFavoriteStaticMembers() {
        if (favoriteStaticMembers == null) {
            String serializedFavorites = PreferenceConstants.getPreferenceStore().getString(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS);
            if (serializedFavorites != null && serializedFavorites.length() > 0) {
                favoriteStaticMembers = new TreeSet<>();
                Collections.addAll(favoriteStaticMembers,
                    serializedFavorites.split(";"));
            } else {
                favoriteStaticMembers = Collections.emptySet();
            }
        }
        return favoriteStaticMembers;
    }

    /**
     * The completion node that is being used for completion
     * (may be different than the default if doing a method context completon
     */
    public ASTNode getPerceivedCompletionNode() {
        return completionNode;
    }

    /**
     * The completion text that is being used for completion
     * (may be different than the default if doing a method context completon
     */
    public String getPerceivedCompletionExpression() {
        return completionExpression;
    }

    public VariableScope getPerceivedCompletionScope() {
        if (currentScope == null && completionNode != null) {
            new TypeInferencingVisitorFactory().createVisitor(unit).visitCompilationUnit(
                (ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) -> {
                    if (node == completionNode) {
                        currentScope = result.scope;
                        return VisitStatus.STOP_VISIT;
                    }
                    return VisitStatus.CONTINUE;
                });
        }
        return currentScope;
    }
}
