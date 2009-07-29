/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;

/**
 * This class represents the base class of the ImportNodes which must
 * be handled differently in the refactorings
 * 
 * @author martin
 *
 */
public abstract class RefactoringImportNode extends ImportNode  {
	
	protected String newClassName;
	
	public RefactoringImportNode(ImportNode importNode) {
		super(importNode.getType(),importNode.getAlias());
		init();
	}
	
	public RefactoringImportNode(ClassNode type, String alias) {
		super(type, alias);
		init();
	}
	
	public void setNewClassName(String newName) {
		this.newClassName = getType().getPackageName() + "." + newName;
	}
	
    @Override
    public abstract String getText();
    
	private void init() {
		this.newClassName = getType().getName();
	}
}
