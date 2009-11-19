/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.ui.selection;

import org.codehaus.groovy.ast.ASTNode;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.SharedImages;

/**
 * @author Stefan Reinhard
 */
@SuppressWarnings("restriction")
public class LabelDispatcher extends LabelProvider {
	
	JavaLabelProvider javaLabel = new JavaLabelProvider();
	GroovyLabelProvider groovyLabel = new GroovyLabelProvider();
	
	public Image getImage(Object element) {
		if (element instanceof IJavaElement) {
			return javaLabel.getImage(element);
		} else if (element instanceof ASTNode) {
			return groovyLabel.getImage(element);
		} else if (element instanceof String) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_FOLDER);
		} else {
			return null;
		}
	}
	
	public String getText(Object element) {
		if (element instanceof IJavaElement) {
			return javaLabel.getText(element);
		} else if (element instanceof ASTNode) {
			return groovyLabel.getText(element);
		} else {
			return element.toString();
		}
	}

}
