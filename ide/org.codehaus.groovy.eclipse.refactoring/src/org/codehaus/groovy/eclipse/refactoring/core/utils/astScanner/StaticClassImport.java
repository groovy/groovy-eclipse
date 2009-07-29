/* 
 * Copyright (C) 2008, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * This class represents the imports like
 * import static java.lang.Math.*
 * 
 * @author martin
 *
 */
public class StaticClassImport extends RefactoringImportNode {
	

    public StaticClassImport(ClassNode type) {
        super(type, null);
    }
    
    @Override
    public String getText() {
		return "import static " + newClassName + ".*";
	}

	@Override
    public void visit(GroovyCodeVisitor visitor) {
		if(visitor instanceof AbstractRefactoringCodeVisitor){
			((AbstractRefactoringCodeVisitor) visitor).visitStaticClassImport(this);
		}
	}
}
