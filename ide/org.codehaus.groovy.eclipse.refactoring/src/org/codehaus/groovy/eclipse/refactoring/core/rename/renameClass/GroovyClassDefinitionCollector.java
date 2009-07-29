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

/**
 * This class is used to collect all groovy class definitions. This is needed to decide, whether the
 * class can be renamed or not. Only the groovy classes found by this collector can be renamed
 * 
 * @author martin
 *
 */
public class GroovyClassDefinitionCollector extends RefactoringCodeVisitorSupport {

	private final List<String> groovyClasses = new ArrayList<String>();
	
	public GroovyClassDefinitionCollector(ModuleNode rootNode) {
		super(rootNode);
	}
	
	@Override
    public void visitClass(ClassNode node) {
		super.visitClass(node);
		groovyClasses.add(node.getName());
	}

	public List<String> getGroovyClasses() {
		return groovyClasses;
	}

}
