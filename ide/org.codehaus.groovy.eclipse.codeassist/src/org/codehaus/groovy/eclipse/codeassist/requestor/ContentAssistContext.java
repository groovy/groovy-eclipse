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

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.completions.GroovyExtendedCompletionContext;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.ITypeRequestor.VisitStatus;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

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
    public final ASTNode lhsNode;
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
            ASTNode lhsNode,
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

    public final void extend(CompletionContext that, VariableScope scope) {
        if (that != null && !that.isExtended()) {
            if (scope == null) scope = getPerceivedCompletionScope();
            ReflectionUtils.setPrivateField(InternalCompletionContext.class, "isExtended", that, Boolean.TRUE);
            ReflectionUtils.setPrivateField(InternalCompletionContext.class, "extendedContext", that, new GroovyExtendedCompletionContext(this, scope));
        }
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
        ClassNode containingTypeDecl;
        if (!(containingDeclaration instanceof ClassNode)) {
            containingTypeDecl = containingDeclaration.getDeclaringClass();
        } else {
            containingTypeDecl = (ClassNode) containingDeclaration;
            // check for type annotation (sits outside of declaration)
            if (containingCodeBlock instanceof AnnotationNode &&
                    containingCodeBlock.getEnd() < containingTypeDecl.getNameStart()) {
                containingTypeDecl = containingTypeDecl.getOuterClass();
            }
        }
        return containingTypeDecl;
    }

    /**
     * @see org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer.getFavoriteStaticMembers()
     */
    public Set<String> getFavoriteStaticMembers() {
        if (favoriteStaticMembers == null) {
            String serializedFavorites = PreferenceConstants.getPreferenceStore().getString(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS);
            if (serializedFavorites != null && serializedFavorites.length() > 0) {
                favoriteStaticMembers = Arrays.stream(serializedFavorites.split(";"))
                    .filter(it -> it.indexOf('.') != -1).collect(Collectors.toCollection(TreeSet::new));
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
     * The expression text that is being used for completion. It may be different
     * than the default if doing a method context completion.
     */
    public String getPerceivedCompletionExpression() {
        if (completionExpression == null) return null;
        return completionExpression.replaceAll("^(?:@|new\\b)|\\s+", "");
    }

    public String getQualifiedCompletionExpression() {
        if (fullCompletionExpression == null) return null;
        return fullCompletionExpression.replaceAll("^(?:@|new\\b)|\\s+", "");
    }

    public VariableScope getPerceivedCompletionScope() {
        if (currentScope == null && completionNode != null) {
            TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit);
            visitor.visitCompilationUnit((ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) -> {
                if (node == completionNode) {
                    currentScope = result.scope;
                    return VisitStatus.STOP_VISIT;
                }
                return VisitStatus.CONTINUE;
            });
        }
        return currentScope;
    }

    /**
     * Determines if right paren comes after completion range.
     */
    public boolean isParenAfter(IDocument document) {
        if (document != null && document.getLength() > completionEnd) {
            try {
                return ('(' == document.getChar(completionEnd));
            } catch (BadLocationException e) {
                GroovyContentAssist.logError("Exception during content assist", e);
            }
        }
        return false;
    }
}
