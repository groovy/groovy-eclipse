/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.astviews

import static org.eclipse.jdt.core.JavaCore.addElementChangedListener
import static org.eclipse.jdt.core.JavaCore.removeElementChangedListener

import groovy.transform.*

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.eclipse.editor.GroovyEditor
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.core.runtime.Adapters
import org.eclipse.jdt.core.ElementChangedEvent
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IElementChangedListener
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IJavaElementDelta
import org.eclipse.jface.text.TextSelection
import org.eclipse.jface.viewers.IStructuredContentProvider
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.jface.viewers.ITreeContentProvider
import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.ui.IEditorPart
import org.eclipse.ui.IPartListener
import org.eclipse.ui.IWorkbenchPart
import org.eclipse.ui.part.DrillDownAdapter
import org.eclipse.ui.part.ViewPart
import org.eclipse.ui.texteditor.ITextEditor

/**
 * A view into the Groovy AST. Anyone who needs to manipulate the AST will find this useful for exploring various nodes.
 */
@AutoFinal @CompileStatic
class ASTView extends ViewPart {

    private TreeViewer viewer

    private IEditorPart editor

    private IPartListener partListener

    private IElementChangedListener listener

    @Override
    void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL)
        DrillDownAdapter drillDownAdapter = new DrillDownAdapter(viewer)
        viewer.contentProvider = new ViewContentProvider()
        viewer.labelProvider = new ViewLabelProvider()
        viewer.comparator = null
        viewer.input = null

        viewer.addDoubleClickListener { event ->
            def obj = ((IStructuredSelection) viewer.selection).firstElement
            def val = ((ITreeNode) obj)?.value
            if (val instanceof ASTNode) {
                ASTNode node = (ASTNode) val
                if (node.lineNumber > 0 && editor instanceof ITextEditor) {
                    ((ITextEditor) editor).selectionProvider.selection = new TextSelection(node.start, node.length)
                }
            }
        }

        listener = { ElementChangedEvent event ->
            if (editor != null) {
                def unit = Adapters.adapt(editor.editorInput, GroovyCompilationUnit)
                if (isUnitInDelta(event.delta, unit)) {
                    Display.default.asyncExec { ->
                        def treePaths =
                            viewer.expandedElements
                        viewer.input = unit.moduleNode
                        viewer.expandedElements = treePaths
                    }
                }
            }
        }

        addElementChangedListener(listener, ElementChangedEvent.POST_RECONCILE)

        partListener = new IPartListener() {
            @Override
            void partActivated(IWorkbenchPart part) {
            }

            @Override
            void partBroughtToTop(IWorkbenchPart part) {
                try {
                    if (part instanceof IEditorPart) {
                        def unit = Adapters.adapt(part.editorInput, GroovyCompilationUnit)
                        if (unit != null) {
                            if (editor != part) {
                                editor = part
                                def treePaths =
                                    viewer.expandedElements
                                viewer.input = unit.moduleNode
                                viewer.expandedElements = treePaths
                            }
                            return
                        }
                    }
                } catch (err) {
                    Activator.warn('Error updating AST Viewer', err)
                }
                partClosed(part)
            }

            @Override
            void partClosed(IWorkbenchPart part) {
                // This is a guard - the content provider should not be null, but sometimes this happens when the
                // part is disposed of for various reasons (unhandled exceptions AFAIK). Without this guard,
                // error message popups continue until Eclipse if forcefully killed.
                if (viewer.contentProvider != null) {
                    viewer.input = null
                }
                editor = null
            }

            @Override
            void partDeactivated(IWorkbenchPart part) {
            }

            @Override
            void partOpened(IWorkbenchPart part) {
            }
        }

        site.page.with {
            addPartListener(partListener)
            if (activeEditor instanceof GroovyEditor) {
                Display.default.asyncExec { ->
                    partListener.partBroughtToTop(activeEditor)
                }
            }
        }
    }

    @Override
    void dispose() {
        try {
            site.page.removePartListener(partListener)
            removeElementChangedListener(listener)
        } finally {
            partListener = null
            listener = null
            super.dispose()
        }
    }

    @Override
    void setFocus() {
        viewer.control.setFocus()
    }

    //--------------------------------------------------------------------------

    private static boolean isUnitInDelta(IJavaElementDelta delta, ICompilationUnit unit) {
        ICompilationUnit icu = (ICompilationUnit) delta.element.getAncestor(IJavaElement.COMPILATION_UNIT)
        if (icu != null) {
            return (icu.elementName == unit.elementName)
        }

        // delta is a potential ancestor of this compilationUnit
        return delta.affectedChildren?.any { IJavaElementDelta d ->
            isUnitInDelta(d, unit)
        }
    }

    @AutoImplement
    private static class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
        private ITreeNode root

        @Override
        Object getParent(Object child) {
            if (child instanceof ITreeNode) {
                child.parent
            }
        }

        @Override
        Object[] getElements(Object inputElement) {
            if (inputElement instanceof ModuleNode) {
                root = TreeNodeFactory.createTreeNode(null, inputElement, 'Module Nodes')
                return root.children
            }
            return new Object[0]
        }

        @Override
        Object[] getChildren(Object parent) {
            if (parent instanceof ITreeNode) {
                parent.children
            }
        }

        @Override
        boolean hasChildren(Object parent) {
            if (parent instanceof ITreeNode) {
                !parent.isLeaf()
            }
        }
    }

    private static class ViewLabelProvider extends LabelProvider {
        @Override
        String getText(Object obj) {
            if (obj instanceof ITreeNode) {
                obj.displayName
            }
        }
    }
}
