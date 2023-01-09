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
package org.codehaus.groovy.eclipse.astviews

import static java.beans.Introspector.decapitalize

import static org.eclipse.jdt.core.JavaCore.addElementChangedListener
import static org.eclipse.jdt.core.JavaCore.removeElementChangedListener
import static org.eclipse.swt.widgets.Display.getDefault as getDisplay

import groovy.transform.*

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.eclipse.editor.GroovyEditor
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.core.runtime.Adapters
import org.eclipse.jdt.core.ElementChangedEvent
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IElementChangedListener
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IJavaElementDelta
import org.eclipse.jdt.groovy.core.util.GroovyUtils
import org.eclipse.jface.action.MenuManager
import org.eclipse.jface.text.TextSelection
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.jface.viewers.ITreeContentProvider
import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.jface.viewers.Viewer
import org.eclipse.jface.viewers.ViewerFilter
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Composite
import org.eclipse.ui.IEditorPart
import org.eclipse.ui.IPartListener
import org.eclipse.ui.IWorkbenchPart
import org.eclipse.ui.part.DrillDownAdapter
import org.eclipse.ui.part.ViewPart
import org.eclipse.ui.texteditor.ITextEditor

/**
 * A view into the Groovy AST. Anyone who needs to manipulate the AST will find
 * this useful for exploring various nodes.
 */
@AutoFinal @CompileStatic
class ASTView extends ViewPart {

    private TreeViewer viewer

    private IEditorPart editor

    private IPartListener partListener

    private IElementChangedListener reconcileListener

    @Override
    void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL)
        viewer.contentProvider = new TreeContentProvider()
        viewer.labelProvider = new TreeLabelProvider()
        viewer.addFilter(new TreeNodeFilter())

        viewer.addDoubleClickListener { event ->
            def element = ((IStructuredSelection) viewer.selection).firstElement
            if (((TreeNode) element).value instanceof ASTNode) {
                def astNode = (ASTNode) ((TreeNode) element).value
                if (astNode.lineNumber > 0 && editor instanceof ITextEditor) {
                    ((ITextEditor) editor).selectionProvider.selection = new TextSelection(astNode.start, astNode.length)
                }
            }
        }

        // add tree navigation actions to toolbar
        def downer = new DrillDownAdapter(viewer)
        viewSite.actionBars.toolBarManager.with {
            downer.addNavigationActions(it)
            update(true)
        }

        // add tree navigation actions to context menu
        viewer.tree.menu = new MenuManager('#PopupMenu').with {
            viewSite.registerContextMenu(it, viewer)
            downer.addNavigationActions(it)
            createContextMenu(viewer.tree)
        }

        def resetTreeView = { root ->
            viewer.input = root
            downer.reset()
        }

        //

        reconcileListener = { ElementChangedEvent event ->
            def unit = Adapters.adapt(editor, GroovyCompilationUnit)
            if (unit != null && isUnitInDelta(event.delta, unit)) {
                display.asyncExec { ->
                    def ee = viewer.expandedElements
                    resetTreeView(unit.moduleNode)
                    viewer.setExpandedElements(ee)
                }
            }
        }

        addElementChangedListener(reconcileListener, ElementChangedEvent.POST_RECONCILE)

        //

        partListener = new IPartListener() {
            @Override
            void partActivated(IWorkbenchPart part) {
            }

            @Override
            void partBroughtToTop(IWorkbenchPart part) {
                if (part != editor && part instanceof IEditorPart) {
                    try {
                        def node = Adapters.adapt(part, ModuleNode)
                        if (node != null) {
                            resetTreeView(node)
                            editor = part
                            return
                        }
                    } catch (Throwable t) {
                        Activator.warn('Error updating AST Viewer', t)
                    }
                    resetTreeView(null)
                    editor = null
                }
            }

            @Override
            void partClosed(IWorkbenchPart part) {
                if (part != null && part == editor) {
                    resetTreeView(null)
                    editor = null
                }
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
                display.asyncExec { ->
                    partListener.partBroughtToTop(activeEditor)
                }
            }
        }
    }

    @Override
    void dispose() {
        try {
            removeElementChangedListener(reconcileListener)
            site.page.removePartListener(partListener)
        } finally {
            reconcileListener = null
            partListener = null
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

    private static class TreeContentProvider implements ITreeContentProvider {
        @Override
        Object[] getElements(Object input) {
            getChildren(input instanceof ModuleNode ? new TreeNode(value: input) : input)
        }

        @Override
        Object[] getChildren(Object node) {
            def treeNode = (TreeNode) node
            def nodeValue = treeNode.value

            if (nodeValue instanceof ASTNode || nodeValue instanceof DynamicVariable || nodeValue instanceof VariableScope ||
                    nodeValue instanceof SourceUnit || nodeValue instanceof CompileUnit || nodeValue instanceof CompilerConfiguration) {
                def methods = nodeValue.class.methods.findAll { method ->
                    method.parameterCount == 0 && method.name =~ /^(is|has(?!hCode$)|get(?!(Type)?Class$|(Static)?(Star)?Imports$)|redirect$)/
                }
                def results = methods.findResults { method ->
                    String name = method.name
                    if (name.startsWith('get')) {
                        name = decapitalize(name.substring(3))
                    }
                    try {
                        def value
                        if (nodeValue instanceof ClassNode && (name == 'unresolvedInterfaces' || name == 'unresolvedSuperClass'))
                            value = ClassNode.getMethod(method.name, boolean).invoke(nodeValue, false) // do not follow redirect!
                        else
                            value = method.invoke(nodeValue)

                        if ((name != 'text' && !nodeValue.is(value)) ||
                                (name == 'text' && !(value =~ /^<not implemented /))) {
                            return new TreeNode(label: name, value: value, parent: treeNode)
                        }
                    } catch (AssertionError | ClassCastException | NullPointerException | ReflectiveOperationException ignore) {
                    }
                    return null
                }
                if (nodeValue instanceof ModuleNode) {
                    results << new TreeNode(label: 'imports', value: GroovyUtils.getAllImportNodes(nodeValue), parent: treeNode)
                }
                return results.toArray().sort(true) { ((TreeNode) it).label }
            }

            if (nodeValue instanceof Iterable || nodeValue instanceof Object[]) {
                def list = []
                nodeValue.eachWithIndex { e, i ->
                    list << new TreeNode(label: "[$i]", value: e, parent: treeNode)
                }
                return list.toArray()
            }

            if (nodeValue instanceof Map) {
                def list = ((Map) nodeValue).collect { k, v ->
                    new TreeNode(label: k instanceof String ? /"$k"/ : "[$k]", value: v, parent: treeNode)
                }
                return list.toArray()
            }
        }

        @Override
        boolean hasChildren(Object node) {
            def value = ((TreeNode) node).value

            if (value instanceof Map || value instanceof Iterable || value instanceof Object[]) {
                return value // false if empty
            }

            (value instanceof ASTNode || value instanceof DynamicVariable || value instanceof VariableScope ||
                value instanceof SourceUnit || value instanceof CompileUnit || value instanceof CompilerConfiguration)
        }

        @Override
        Object getParent(Object node) {
            ((TreeNode) node).parent
        }
    }

    private static class TreeLabelProvider extends LabelProvider {
        @Override
        String getText(Object node) {
            def label = ((TreeNode) node).label
            def value = ((TreeNode) node).value

            switch (value) {
            case stmt.Statement:
            case expr.Expression:
                def valueClass = value.class
                if (valueClass.isAnonymousClass()) valueClass = valueClass.superclass
                return "$label : ${valueClass.simpleName - ~/(Expression|Statement)$/}"
            case MethodNode:
                def descriptor = ((MethodNode) value).typeDescriptor.replace(
                    '<init>', ((MethodNode) value).declaringClass.nameWithoutPackage)
                return "$label : ${descriptor.substring(descriptor.indexOf(' ') + 1)}"
            case ClassNode:
                return "$label : ${value['unresolvedName']}"
            case Variable:
                return "$label : ${value['name']}"
            case ImportNode:
                return "$label : ${value['text']}"
            case ASTNode:
            case SourceUnit:
            case CompileUnit:
            case VariableScope:
            case CompilerConfiguration:
                if (label.charAt(0) != '[') return label
            case Map:
            case Iterable:
            case Object[]:
                return label
            case Character:
                return "$label : '$value'"
            case CharSequence:
                return "$label : \"$value\""
            default:
                return "$label : $value"
            }
        }
    }

    private static class TreeNodeFilter extends ViewerFilter {
        @Override
        boolean select(Viewer viewer, Object parent, Object node) {
            def label = ((TreeNode) node).label
            def value = ((TreeNode) node).value
            def outer = parent instanceof TreeNode ? parent.value : parent

            // filter redundant properties
            if (outer instanceof ClassNode) {
                if (label ==~ /abstractMethods|allDeclaredMethods|allInterfaces|declaredMethodsMap|fieldIndex|hasMultiRedirect|(hasP|p)ackageName|/ +
                        /is(Array|DerivedFromGroovyObject|RedirectNode)|length|module|name(WithoutPackage)?|outer(Most)?Class|plainNodeReference|text/) {
                    return false
                }
                if (outer.redirectNode && label ==~ /annotations|compileUnit|declaredConstructors|enclosingMethod|fields|hasInconsistentHierarchy|/ +
                        /innerClasses|interfaces|is(?!GenericsPlaceHolder|Synthetic(Public)?|UsingGenerics)\w+|methods|mixins|modifiers|/ +
                        /outerClasses|package|permittedSubclasses|properties|superClass|typeAnnotations/) {
                    return false
                }
                if (!outer.redirectNode && label ==~ /unresolved(Name|Interfaces|SuperClass)/) {
                    return false
                }
            } else if (outer instanceof Variable) { // FieldNode, PropertyNode, etc.
                if (label ==~ /name|hasInitialExpression|initialValueExpression|is(Enum|Final|Private|Protected|Public|Static|Volatile)/) {
                    return false
                }
            } else if (outer instanceof MethodNode) {
                if (label ==~ /firstStatement|is(Abstract|Default|Final|PackageScope|Private|Protected|Public|Static|VoidMethod)|name|typeDescriptor/) {
                    return false
                }
            } else if (outer instanceof ModuleNode) {
                if (label ==~ /isEmpty|packageName|hasPackage(Name)?|scriptClassDummy/) {
                    return false
                }
            } else if (outer instanceof CompileUnit) {
                if (label ==~ /(c|generatedInnerC|sortedC)lasses|hasClassNodeToCompile/) {
                    return false
                }
            } else if (outer instanceof ProcessingUnit) {
                if (label ==~ /AST|CST|source/) {
                    return false
                }
            } else if (outer instanceof stmt.Statement) {
                if (label ==~ /isEmpty|statementLabel/) {
                    return false
                }
            } else if (outer instanceof ImportNode || outer instanceof PackageNode) {
                if (label == 'text') {
                    return false
                }
            }
            if (outer instanceof ASTNode) {
                if (label ==~ /instance|metaDataMap/) {
                    return false
                }
                if (label == 'groovydoc') {
                    return value['present']
                }
            }

            if (value instanceof ClassNode && value['outerClass'] != null &&
                    parent['label'] == 'classes' && parent['parent']['value'] instanceof ModuleNode) {
                return false
            }

            if (value instanceof Map || value instanceof Iterable || value instanceof Object[]) {
                return value // false if empty
            }

            return true
        }
    }

    @EqualsAndHashCode(includes='label,parent')
    private static class TreeNode {
        String label
        Object value
        TreeNode parent

        void setValue(value) {
            this.value = value instanceof Iterator ? value.collect() : value
        }
    }
}
