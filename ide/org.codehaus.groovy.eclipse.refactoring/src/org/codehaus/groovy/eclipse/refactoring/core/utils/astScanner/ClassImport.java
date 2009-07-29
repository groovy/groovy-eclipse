/* 
 * Copyright (C) 2008, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ImportResolver;

/**
 * This class represents the imports like
 * import groovy.lang.Closure as MyClosure
 * 
 * @author martin
 *
 */
public class ClassImport extends RefactoringImportNode {
	
	private String newAlias;
	
	public ClassImport(ImportNode importNode) {
		super(importNode);
		newAlias = getAlias();
	}
    
    @Override
    public String getText() {
		if (ImportResolver.isExplizitAlias(this)) {
			return "import " + newClassName + " as " + newAlias;
		}
        return "import " + newClassName;
	}
    
    public void setNewAlias(String newAlias) {
    	this.newAlias = newAlias;
    }
    
    @Override
    public void visit(GroovyCodeVisitor visitor) {
    	if(visitor instanceof AbstractRefactoringCodeVisitor){
			((AbstractRefactoringCodeVisitor) visitor).visitClassImport(this);
		}
    }
}
