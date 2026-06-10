/*
 * Copyright 2009-2026 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Comment;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedSourceMethod;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.groovy.core.util.DepthFirstVisitor;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.ui.text.folding.DefaultJavaFoldingStructureProvider;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Replacement for the {@link DefaultJavaFoldingStructureProvider} that provides
 * Groovy-specific folding regions for the {@link GroovyEditor}.
 */
public class GroovyAwareFoldingStructureProvider extends DefaultJavaFoldingStructureProvider {

    protected GroovyEditor editor;

    @Override
    public void install(final ITextEditor editor, final ProjectionViewer viewer) {
        if (editor instanceof GroovyEditor) { this.editor = (GroovyEditor) editor;
            ReflectionUtils.setPrivateField(DefaultJavaFoldingStructureProvider.class, "fPropertyChangeListener", this,
                (org.eclipse.jface.util.IPropertyChangeListener) (/*org.eclipse.jface.util.PropertyChangeEvent*/ event) -> {
                    if (event.getProperty().startsWith("editor_folding_") &&
                            !event.getProperty().startsWith("editor_folding_custom_")) {
                        this.editor.withoutSpecialFolding(this::initialize);
                    }
                }
            );
        }
        super.install(editor, viewer);
    }

    @Override
    protected void handleProjectionEnabled() {
        if (editor == null) super.handleProjectionEnabled();
        else editor.withoutSpecialFolding(super::handleProjectionEnabled);
    }

    @Override
    public void uninstall() {
        this.editor = null;
        super.uninstall();
    }

    //--------------------------------------------------------------------------

    @Override
    protected void computeFoldingStructure(final IJavaElement element, final FoldingStructureComputationContext context) {
        try {
            if (editor != null && editor.getModuleNode() != null) {
                if (isMainType(element)) {
                    // add folding for multi-line comments
                    computeCommentFoldingStructure(element, context);

                    // add folding for multi-line closures
                    computeClosureFoldingStructure(element, context);

                    // add folding for multi-line literals (list/map)
                } else if (isScriptMethod(element)) {
                    return; // prevent folding the entire script body
                }

                switch (element.getElementType()) {
                  case IJavaElement.TYPE:
                    computeTraitMethodFoldingStructure((IType) element, context);
                    if (!((IType) element).isMember()) break; //else fall through
                  case IJavaElement.FIELD:
                  case IJavaElement.METHOD:
                  case IJavaElement.INITIALIZER:
                    var range  = ((IMember) element).getSourceRange();
                    int offset = range.getOffset();
                    int length = range.getLength();
                    /**/range = ((IMember) element).getJavadocRange();
                    if (range != null) { // advance past the javadoc commnet
                        offset  = range.getOffset() + range.getLength() + 2;
                        length -= range.getLength() + 2;
                    }
                    IRegion normalized = alignRegion(offset, length, context);
                    if (normalized != null) {
                        boolean isCollapsed = element.getElementType()==IJavaElement.TYPE?context.collapseInnerTypes():context.collapseMembers(), isComment = false;
                        context.addProjectionRange(new JavaProjectionAnnotation(isCollapsed, element, isComment), createMemberPosition(normalized, (IMember) element));
                    }
                }
            }
        } catch (AssertionError | LinkageError | Exception ignore) {
        }
        // NOTE: call super.computeFoldingStructure for null editor to preserve Java behavior
        if (editor == null || element.getElementType() == IJavaElement.IMPORT_CONTAINER) {
            super.computeFoldingStructure(element, context);
        }
    }

    protected void computeClosureFoldingStructure(final IJavaElement element, final FoldingStructureComputationContext context) {
        DepthFirstVisitor visitor = new DepthFirstVisitor() {
            @Override
            public void visitClosureExpression(final ClosureExpression expression) {
                if (expression.getEnd() > 0) {
                    IRegion normalized = alignRegion(expression.getStart(), expression.getLength(), context);
                    if (normalized != null) {
                        boolean isCollapsed = false, isComment = false;
                        var lambda = org.eclipse.jdt.internal.core.LambdaFactory.createLambdaExpression(
                            (org.eclipse.jdt.internal.core.JavaElement) element, "Ljava.lang.Runnable;",
                            expression.getStart(), expression.getEnd() - 1, expression.getCode().getStart());
                        context.addProjectionRange(new JavaProjectionAnnotation(isCollapsed, element, isComment), createMemberPosition(normalized, lambda));
                    }
                }
                super.visitClosureExpression(expression);
            }
        };
        visitor.visitModule(editor.getModuleNode());
    }

    protected void computeCommentFoldingStructure(final IJavaElement element, final FoldingStructureComputationContext context) {
        for (Comment comment : editor.getModuleNode().getContext().getComments()) {
            if (comment.eline > comment.sline) {
                try {
                    // translate lines and columns to region
                    IDocument document = getDocument(context);
                    int offset = document.getLineOffset(comment.sline - 1) + (comment.scol - 1);
                    int length = (document.getLineOffset(comment.eline - 1) + (comment.ecol - 1)) - offset;

                    boolean head = comment.sline <= 2 && !comment.isJavadoc();
                    IRegion normalized = alignRegion(offset, length, context);
                    if (normalized != null && (head || comment.isJavadoc() || isScriptMethodElement(normalized))) {
                        boolean isCollapsed = (head ? context.collapseHeaderComments() : context.collapseJavadoc()), isComment = true;
                        context.addProjectionRange(new JavaProjectionAnnotation(isCollapsed, element, isComment), createCommentPosition(normalized));
                    }
                } catch (BadLocationException e) {
                    GroovyPlugin.getDefault().logError("Failed to compute region for comment", e);
                }
            }
        }
    }

    protected void computeTraitMethodFoldingStructure(final IType maybeTrait, final FoldingStructureComputationContext context) {
        findType(maybeTrait).map(t -> t.<Iterable<MethodNode>>getNodeMetaData("trait.methods")).ifPresent(traitMethods -> {
            for (MethodNode traitMethod : traitMethods) {
                IRegion normalized = alignRegion(traitMethod.getStart(), traitMethod.getLength(), context);
                if (normalized != null) {
                    boolean isCollapsed = context.collapseMembers(), isComment = false;
                    IMember method = new GroovyResolvedSourceMethod( // supplies name range
                        (org.eclipse.jdt.internal.core.JavaElement) maybeTrait, traitMethod.getName(),
                        GroovyUtils.getParameterTypeSignatures(traitMethod, true), "", "", traitMethod);
                    context.addProjectionRange(new JavaProjectionAnnotation(isCollapsed, method, isComment), createMemberPosition(normalized, method));
                }
            }
        });
    }

    //--------------------------------------------------------------------------

    protected final Optional<ClassNode> findType(final IType type) {
        String typeName = type.getFullyQualifiedName();
        Queue<ClassNode> classNodes = new LinkedList<>(editor.getModuleNode().getClasses());
        while (!classNodes.isEmpty()) {
            ClassNode classNode = classNodes.remove();
            if (classNode.getName().equals(typeName)) {
                return Optional.of(classNode);
            }
            for (Iterator<? extends ClassNode> it = classNode.getInnerClasses(); it.hasNext();) {
                classNodes.add(it.next());
            }
        }
        return Optional.empty();
    }

    protected final boolean isMainType(final IJavaElement element) {
        if (element instanceof IType type) {
            String typeName = type.getFullyQualifiedName();
            return typeName.equals(editor.getModuleNode().getMainClassName());
        }
        return false;
    }

    protected final boolean isScriptMethod(final IJavaElement element) {
        if (element instanceof IMethod method && "run".equals(method.getElementName()) && method.getNumberOfParameters() == 0) {
            return findType(method.getDeclaringType()).filter(GroovyUtils::isScript).isPresent();
        }
        return false;
    }

    protected final boolean isScriptMethodElement(final IRegion region) {
        FindSurroundingNode fsn = new FindSurroundingNode(
            new org.codehaus.groovy.eclipse.codebrowsing.requestor.Region(region.getOffset(), region.getLength()),
            FindSurroundingNode.VisitKind.SURROUNDING_NODE);
        IASTFragment astFragment = fsn.doVisitSurroundingNode(editor.getModuleNode());
        if (astFragment.getAssociatedNode() instanceof MethodNode methodNode) {
            return methodNode.isScriptBody();
        }
        if (astFragment.getAssociatedNode() instanceof ModuleNode) {
            return true;
        }
        return false;
    }

    protected static IDocument getDocument(final FoldingStructureComputationContext context) {
        return ReflectionUtils.executePrivateMethod(context.getClass(), "getDocument", context);
    }

    protected final IRegion alignRegion(final int offset, final int length, final FoldingStructureComputationContext context) {
        try {
            return ReflectionUtils.throwableExecutePrivateMethod(DefaultJavaFoldingStructureProvider.class, "alignRegion",
                new  Class[]{IRegion.class, FoldingStructureComputationContext.class, boolean.class}, this,
                new Object[]{new Region(offset, length), context, true});
        } catch (Throwable t) {
            ReflectionUtils.setPrivateField(DefaultJavaFoldingStructureProvider.class, "includelastLine", this, true);
            return alignRegion(new Region(offset, length), context);
        }
    }
}
