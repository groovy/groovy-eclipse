/* 
 * Copyright (C) 2008, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.popup.actions;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.editor.actions.EditingAction;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.WorkspaceDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.WorkspaceFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.hierarchy.HierarchyTreeBuilder;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameDispatcher;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameClass.RenameClassProvider;
import org.eclipse.jface.action.IAction;

/**
 * @author Michael Klenk mklenk@hsr.ch
 * 
 */
public class TypeHierarchyAction extends EditingAction {

	@Override
    public void run(IAction action) {
		
		try {
			wait(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		WorkspaceDocumentProvider docProv = new WorkspaceDocumentProvider(getFile());
		UserSelection sel = new UserSelection(getTextSelection().getOffset(),getTextSelection().getLength());
		RenameDispatcher disp = new RenameDispatcher(docProv, sel);
		ASTNode node = disp.getSelectedNode();
		ClassNode classNode = RenameClassProvider.giveClassNodeToRename(node);
		if(classNode == null) {
			classNode = (ClassNode)docProv.getRootNode().getClasses().get(0);
		}
		
		
		HierarchyTreeBuilder htree = new HierarchyTreeBuilder(new WorkspaceFileProvider(docProv));
		htree.getInterconnectedClasses(classNode);
		
//		HierarchyView hview = new HierarchyView(htree,classNode);
//		hview.createPartControl(getShell());
//		hview.setFocus();
	}

}
