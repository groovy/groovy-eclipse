/*
 * Copyright 2009-2023 the original author or authors.
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
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Comment;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode;
import org.codehaus.groovy.transform.trait.Traits;
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
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Replacement for {@link DefaultJavaFoldingStructureProvider} that is intended
 * to provide Groovy-specific folding behavior for the {@link GroovyEditor}.
 */
public class GroovyAwareFoldingStructureProvider extends DefaultJavaFoldingStructureProvider {

    protected GroovyEditor editor;

    @Override
    public void install(final ITextEditor editor, final ProjectionViewer viewer) {
        super.install(editor, viewer);
        if (editor instanceof GroovyEditor)
            this.editor = (GroovyEditor) editor;
    }

    @Override
    public void uninstall() {
        this.editor = null;
        super.uninstall();
    }

    @Override
    protected void computeFoldingStructure(final IJavaElement element, final FoldingStructureComputationContext context) {
        // NOTE: be sure to call super.computeFoldingStructure when editor is null to preserve Java behavior
        try {
            if (editor != null && editor.getModuleNode() != null) {
                if (isMainType(element)) {
                    // add folding for multi-line closures
                    computeClosureFoldingStructure(element, context);

                    // TODO: add folding for multi-line strings or array/list/map literals?
                } else if (isScriptMethod(element)) {
                    // add folding for multi-line comments
                    computeCommentFoldingStructure(element, context);

                    // TODO: add folding for top-level types?

                    return; // prevent folding of entire script body
                }

                if (element instanceof IType) {
                    computeTraitMethodFoldingStructure((IType) element, context);
                }
            }
        } catch (Exception | LinkageError | AssertionError ignore) {
        }
        super.computeFoldingStructure(element, context);
    }

    protected void computeClosureFoldingStructure(final IJavaElement element, final FoldingStructureComputationContext context) {
        DepthFirstVisitor visitor = new DepthFirstVisitor() {
            @Override
            public void visitClosureExpression(final ClosureExpression expression) {
                if (expression.getEnd() > 0) {
                    IRegion normalized = alignRegion(new Region(expression.getStart(), expression.getLength()), context);
                    if (normalized != null) {
                        // TODO: any consequences to using the main type as the member?
                        Position position = createMemberPosition(normalized, (IMember) element);
                        if (position != null) {
                            boolean isCollapsed = false, isComment = false;
                            context.addProjectionRange(new JavaProjectionAnnotation(isCollapsed, element, isComment), position);
                        }
                    }
                }
                super.visitClosureExpression(expression);
            }
        };
        visitor.visitModule(editor.getModuleNode());
    }

    protected void computeCommentFoldingStructure(final IJavaElement element, final FoldingStructureComputationContext context) {
        for (Comment comment : editor.getModuleNode().getContext().getComments()) {
            if (!comment.isJavadoc() && comment.eline > comment.sline) {
                try {
                    // translate lines and columns to region
                    IDocument document = getDocument(context);
                    int offset = document.getLineOffset(comment.sline - 1) + (comment.scol - 1);
                    int length = (document.getLineOffset(comment.eline - 1) + (comment.ecol - 1)) - offset;

                    IRegion normalized = alignRegion(new Region(offset, length), context);
                    if (normalized != null && isScriptMethodElement(normalized)) {
                        Position position = createCommentPosition(normalized);
                        if (position != null) {
                            boolean isCollapsed = false, isComment = true;
                            context.addProjectionRange(new JavaProjectionAnnotation(isCollapsed, element, isComment), position);
                        }
                    }
                } catch (BadLocationException e) {
                    GroovyPlugin.getDefault().logError("Failed to compute region for comment", e);
                }
            }
        }
    }

    protected void computeTraitMethodFoldingStructure(final IType maybeTrait, final FoldingStructureComputationContext context) {
        findType(maybeTrait).filter(Traits::isTrait).ifPresent(traitType -> {
            List<MethodNode> traitMethods = traitType.getNodeMetaData("trait.methods");
            if (traitMethods != null && !traitMethods.isEmpty()) {
                for (MethodNode traitMethod : traitMethods) {
                    IRegion normalized = alignRegion(new Region(traitMethod.getStart(), traitMethod.getLength()), context);
                    if (normalized != null) {
                        IMember method = maybeTrait.getMethod(traitMethod.getName(), GroovyUtils.getParameterTypeSignatures(traitMethod, true));
                        Position position = createMemberPosition(normalized, method);
                        if (position != null) {
                            boolean isCollapsed = false, isComment = false;
                            context.addProjectionRange(new JavaProjectionAnnotation(isCollapsed, method, isComment), position);
                        }
                    }
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
        if (element instanceof IType) {
            String typeName = ((IType) element).getFullyQualifiedName();
            return typeName.equals(editor.getModuleNode().getMainClassName());
        }
        return false;
    }

    protected final boolean isScriptMethod(final IJavaElement element) {
        if (element instanceof IMethod && "run".equals(element.getElementName()) && ((IMethod) element).getNumberOfParameters() == 0) {
            return findType(((IMethod) element).getDeclaringType()).filter(GroovyUtils::isScript).isPresent();
        }
        return false;
    }

    protected final boolean isScriptMethodElement(final IRegion region) {
        FindSurroundingNode fsn = new FindSurroundingNode(
            new org.codehaus.groovy.eclipse.codebrowsing.requestor.Region(region.getOffset(), region.getLength()),
            FindSurroundingNode.VisitKind.SURROUNDING_NODE);
        IASTFragment astFragment = fsn.doVisitSurroundingNode(editor.getModuleNode());
        if (astFragment.getAssociatedNode() instanceof MethodNode) {
            return ((MethodNode) astFragment.getAssociatedNode()).isScriptBody();
        }
        if (astFragment.getAssociatedNode() instanceof ModuleNode) {
            return true;
        }
        return false;
    }

    protected static IDocument getDocument(final FoldingStructureComputationContext context) {
        return ReflectionUtils.executePrivateMethod(context.getClass(), "getDocument", context);
    }
}
