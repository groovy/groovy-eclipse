/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.Parameter;

public abstract class AbstractRefactoringCodeVisitor extends CodeVisitorSupport implements
		GroovyClassVisitor {
	
	public abstract void visitStaticFieldImport(StaticFieldImport staticAliasImport);
	
	public abstract void visitStaticClassImport(StaticClassImport staticClassImport);
	
	public abstract void visitClassImport(ClassImport classImport);
	
	public abstract void analyzeParameter(Parameter parameter);
	
	public abstract void analyzeType(ClassNode node);
	
	public abstract void scanAST();
}
