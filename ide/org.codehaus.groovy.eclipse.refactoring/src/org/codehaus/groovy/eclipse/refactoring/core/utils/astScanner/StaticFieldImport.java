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
 * import static java.lang.Math.PI as myPI
 * 
 * @author martin
 *
 */
public class StaticFieldImport extends RefactoringImportNode {
	
	private final String field;
	private String newField;
	private String newAlias;

    public StaticFieldImport(ClassNode type, String alias, String field) {
        super(type,alias);
        this.field = field;
        this.newClassName = type.getName();
        this.newField = field;
        this.newAlias = getAlias();
    }
    
    @Override
    public String getText() {
		if (field.equals(newAlias)) {
			return "import static " + newClassName + "." + newField;
		}
        //has alias
        return "import static " + newClassName + "." + newField + " as " + newAlias;
	}

	public String getField() {
		return field;
	}

	public void setNewField(String field) {
		this.newField = field;
	}
	
    public void setNewAlias(String newAlias) {
    	this.newAlias = newAlias;
    }

	@Override
    public boolean equals(Object obj) {
		if(obj instanceof StaticFieldImport){
			StaticFieldImport other = (StaticFieldImport) obj;
			return (this.getType().equals(other.getType()) && 
					this.field.equals(other.getField()) && 
					this.getAlias().equals(other.getAlias()));
		}
		return false;
	}

	@Override
    public void visit(GroovyCodeVisitor visitor) {
		if(visitor instanceof AbstractRefactoringCodeVisitor){
			((AbstractRefactoringCodeVisitor) visitor).visitStaticFieldImport(this);
		}
	}
	
}
