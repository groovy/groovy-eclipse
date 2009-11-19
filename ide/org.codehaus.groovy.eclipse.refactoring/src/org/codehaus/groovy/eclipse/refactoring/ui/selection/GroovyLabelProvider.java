/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.ui.selection;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Stefan Reinhard
 */
public class GroovyLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof FieldNode) {
			FieldNode field = (FieldNode)element;
			ClassNode declaring = field.getDeclaringClass();
			return declaring.getNameWithoutPackage() + "." + super.getText(element);
		} else if (element instanceof MethodNode) {
			MethodNode method = (MethodNode)element;
			ClassNode declaring = method.getDeclaringClass();
			return declaring.getNameWithoutPackage() + "." + super.getText(element);
		} else if (element instanceof ClassNode) {
			ClassNode node = (ClassNode)element;
			return node.getName();
		} else {
			return super.getText(element);
		}
	}
}
