/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.astviews;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A view into the Groovy AST. Anyone who needs to manipulate the AST will find this useful for exploring various nodes.
 */
public class ASTView extends ViewPart {

    private TreeViewer viewer;

    private Action doubleClickAction;

    private IEditorPart editor;

    private IPartListener partListener;

    private IElementChangedListener listener;

    /**
     * This is a callback that will allow us to create the viewer and initialize it.
     */
    @Override
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        @SuppressWarnings("unused")
        DrillDownAdapter drillDownAdapter = new DrillDownAdapter(viewer);
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setComparator(null);
        viewer.setInput(null);
        makeActions();
        //hookContextMenu();
        hookDoubleClickAction();
        hookGroovy();
        //contributeToActionBars();
    }

    @Override
    public void dispose() {
        unhookGroovy();
        super.dispose();
    }

    private void hookGroovy() {
        partListener = new IPartListener() {
            @Override
            public void partActivated(IWorkbenchPart part) {
            }

            @Override
            public void partBroughtToTop(IWorkbenchPart part) {
                try {
                    if (part instanceof IEditorPart) {
                        IFile file = Adapters.adapt(((IEditorPart) part).getEditorInput(), IFile.class);
                        if (file != null && ContentTypeUtils.isGroovyLikeFileName(file.getName())) {
                            ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
                            if (unit instanceof GroovyCompilationUnit) {
                                if (editor != part) {
                                    editor = (IEditorPart) part;
                                    Object[] treePaths = viewer.getExpandedElements();
                                    viewer.setInput(((GroovyCompilationUnit) unit).getModuleNode());
                                    viewer.setExpandedElements(treePaths);
                                } else {
                                    // nothing to do!
                                }
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    GroovyCore.logException("Error updating AST Viewer", e);
                }
                editor = null;
                // This is a guard - the content provider should not be null, but sometimes this happens when the
                // part is disposed of for various reasons (unhandled exceptions AFAIK). Without this guard,
                // error message popups continue until Eclipse if forcefully killed.
                if (viewer.getContentProvider() != null) {
                    viewer.setInput(null);
                }
            }

            @Override
            public void partClosed(IWorkbenchPart part) {
            }

            @Override
            public void partDeactivated(IWorkbenchPart part) {
            }

            @Override
            public void partOpened(IWorkbenchPart part) {
            }
        };
        getSite().getPage().addPartListener(partListener);

        // Warm the listener up.
        if (getSite().getPage().getActiveEditor() instanceof GroovyEditor) {
            partListener.partBroughtToTop(getSite().getPage().getActiveEditor());
        }

        listener = new IElementChangedListener() {
            @Override
            public void elementChanged(ElementChangedEvent event) {
                // The editor is currently not a GroovyEditor, so there is not ASTView to refresh.
                if (editor == null) {
                    return;
                }
                IJavaElementDelta delta = event.getDelta();
                IFile file = Adapters.adapt(editor.getEditorInput(), IFile.class);
                final GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);

                // determine if the delta contains the ICompUnit under question
                if (isUnitInDelta(delta, unit)) {
                    Display.getDefault().asyncExec(() -> {
                        Object[] treePaths = viewer.getExpandedElements();
                        viewer.setInput(unit.getModuleNode());
                        viewer.setExpandedElements(treePaths);
                    });
                }
            }

            private boolean isUnitInDelta(IJavaElementDelta delta, GroovyCompilationUnit unit) {
                IJavaElement elt = delta.getElement();
                if (elt.getElementType() == IJavaElement.COMPILATION_UNIT) {
                    // comparing with a compilation unit
                    // if test fails, no need to go further
                    return elt.getElementName().equals(unit.getElementName());
                }

                ICompilationUnit candidateUnit = (ICompilationUnit) elt.getAncestor(IJavaElement.COMPILATION_UNIT);
                if (candidateUnit != null) {
                    // now if test fails, no need to go further
                    return candidateUnit.getElementName().equals(unit.getElementName());
                }

                // delta is a potential ancestor of this compilationUnit
                IJavaElementDelta[] deltas = delta.getAffectedChildren();
                if (deltas != null) {
                    for (IJavaElementDelta delta2 : deltas) {
                        if (isUnitInDelta(delta2, unit)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };

        JavaCore.addElementChangedListener(listener, ElementChangedEvent.POST_RECONCILE);
    }

    private void unhookGroovy() {
        JavaCore.removeElementChangedListener(listener);
        getSite().getPage().removePartListener(partListener);
    }

    private void makeActions() {
        doubleClickAction = new Action() {
            @Override
            public void run() {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                if (obj == null) {
                    return;
                }
                if (((ITreeNode) obj).getValue() instanceof ASTNode) {
                    Object value = ((ITreeNode) obj).getValue();
                    if (!(value instanceof ASTNode)) {
                        return;
                    }

                    ASTNode node = (ASTNode) value;
                    if (node.getLineNumber() != -1) {
                        int offset0 = node.getStart();
                        int offset1 = node.getEnd();
                        if (editor instanceof ITextEditor) {
                            ((ITextEditor) editor).getSelectionProvider().setSelection(new TextSelection(offset0, offset1 - offset0));
                        }
                    }
                }
            }
        };
    }

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(event -> doubleClickAction.run());
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    private static class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
        ITreeNode root;

        @Override
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (!(inputElement instanceof ModuleNode)) {
                return new Object[0];
            }
            root = TreeNodeFactory.createTreeNode(null, inputElement, "Module Nodes");
            Object[] children = root.getChildren();
            return children;
        }

        @Override
        public Object getParent(Object child) {
            Object parent = ((ITreeNode) child).getParent();
            return parent;
        }

        @Override
        public Object[] getChildren(Object parent) {
            ITreeNode[] children = ((ITreeNode) parent).getChildren();
            return children;
        }

        @Override
        public boolean hasChildren(Object parent) {
            boolean has = !((ITreeNode) parent).isLeaf();
            return has;
        }
    }

    private static class ViewLabelProvider extends LabelProvider {
        @Override
        public String getText(Object obj) {
            return ((ITreeNode) obj).getDisplayName();
        }

        @Override
        public Image getImage(Object obj) {
            return null;
        }
    }
}
