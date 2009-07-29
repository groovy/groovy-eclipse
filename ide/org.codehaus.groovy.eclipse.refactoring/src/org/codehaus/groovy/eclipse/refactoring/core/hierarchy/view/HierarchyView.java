/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.hierarchy.view;

import java.util.HashSet;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.WorkspaceDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.WorkspaceFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.hierarchy.HierarchyNode;
import org.codehaus.groovy.eclipse.refactoring.core.hierarchy.HierarchyTreeBuilder;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

@SuppressWarnings("restriction")
public class HierarchyView extends ViewPart {
	private TreeViewer viewer;
	//private DrillDownAdapter drillDownAdapter;
	private IPartListener partListener;
	private GroovyEditor editor;

	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {
		private HierarchyNode invisibleRoot;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {

		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {

				try {
				editor = (GroovyEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				IFile sourceFile = ((IFileEditorInput) editor.getEditorInput()).getFile();
				WorkspaceDocumentProvider docProv = new WorkspaceDocumentProvider(sourceFile);
				HierarchyTreeBuilder treeBuilder = new HierarchyTreeBuilder(new WorkspaceFileProvider(docProv));
				
				ClassNode classNode = (ClassNode)docProv.getRootNode().getClasses().get(0);
		
				invisibleRoot = new HierarchyNode(new ClassNode("Invisible",0,null));
				invisibleRoot.insertExtendingChild(treeBuilder.getHierarchyForClass(classNode));
				} catch (Exception e) {
					invisibleRoot = new HierarchyNode(new ClassNode("Invisible",0,null));
				}
				return getChildren(invisibleRoot);

		}
		public Object getParent(Object child) {
			if (child instanceof HierarchyNode) {
				HierarchyNode hn = (HierarchyNode) child;
				if(hn.isInterface()) {
					if(!hn.getImplementsClasses().isEmpty())
						return hn.getImplementsClasses().iterator().next();
				}
				return hn.getExtendingClass();	
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof HierarchyNode) {
				HashSet<HierarchyNode> children = new HashSet<HierarchyNode>();
				children.addAll(((HierarchyNode)parent).getExtendingChildern());
				children.addAll(((HierarchyNode)parent).getImplementingChildern());
				return children.toArray();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof HierarchyNode)
				return (!((HierarchyNode)parent).getExtendingChildern().isEmpty() ||
						!((HierarchyNode)parent).getImplementingChildern().isEmpty());
			return false;
		}
	}
	class ViewLabelProvider extends LabelProvider {

		@Override
        public String getText(Object obj) {
			return ((HierarchyNode)obj).getOriginClass().getNameWithoutPackage();
		}
		@Override
        public Image getImage(Object obj) {
			HierarchyNode node = (HierarchyNode) obj;
			if(node.isInterface())
				return JavaPluginImages.get( JavaPluginImages.IMG_OBJS_INTERFACE );
			return JavaPluginImages.get( JavaPluginImages.IMG_OBJS_CLASS );
		}
	}

	/**
	 * The constructor.
	 */
	public HierarchyView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	@Override
    public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		// drillDownAdapter = new DrillDownAdapter(viewer);
		new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
		hookGroovy();

		
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
    public void setFocus() {
		viewer.getControl().setFocus();
		viewer.getContentProvider().inputChanged(viewer, null, null);
	}
	private void hookGroovy() {
		partListener = new IPartListener() {
			public void partActivated(IWorkbenchPart part) {
			}

			public void partBroughtToTop(IWorkbenchPart part) {
				if (part instanceof GroovyEditor && editor != part) {
					editor = (GroovyEditor) part;
					viewer.setInput(editor.getEditorInput().getAdapter(IFile.class));
					viewer.expandAll();
					return;
				}
				editor = null;
				if (viewer.getContentProvider() != null) {
					viewer.setInput(null);
				}
			}

			public void partClosed(IWorkbenchPart part) {
			}

			public void partDeactivated(IWorkbenchPart part) {
			}

			public void partOpened(IWorkbenchPart part) {
			}
		};
		getSite().getPage().addPartListener(partListener);

		// Warm the listener up.
		if (getSite().getPage().getActiveEditor() instanceof GroovyEditor) {
			partListener.partBroughtToTop(getSite().getPage().getActiveEditor());
		}
	}
}