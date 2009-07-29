/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.rename.renameClass;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.RefactoringCodeVisitorSupport;

public class ClassNamesInSamePackageCollector extends RefactoringCodeVisitorSupport {

	private final List<String> classNamesInSamePackage = new ArrayList<String>();
	private final ClassNode oldClass;

	public ClassNamesInSamePackageCollector(ModuleNode rootNode, ClassNode oldClass) {
		super(rootNode);
		this.oldClass = oldClass;
	}
	
	@Override
    public void visitClass(ClassNode node) {
		super.visitClass(node);
		if (node.getPackageName() == null && oldClass.getPackageName() == null) {
			classNamesInSamePackage .add(node.getNameWithoutPackage());
		} else if (node.getPackageName() != null &&
				(node.getPackageName().equals(oldClass.getPackageName()))) {
			classNamesInSamePackage.add(node.getNameWithoutPackage());
		}
	}

	public List<String> getClassNamesInSamePackage() {
		return classNamesInSamePackage;
	}

}
