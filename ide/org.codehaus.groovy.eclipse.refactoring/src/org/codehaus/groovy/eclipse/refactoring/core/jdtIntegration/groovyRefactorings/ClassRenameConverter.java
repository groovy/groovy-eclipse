/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.groovyRefactorings;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameClass.RenameClassProvider;
import org.eclipse.jdt.core.IType;

/**
 * @author Stefan Reinhard
 */
public class ClassRenameConverter {
	
	/**
	 * Creates a <code>RenameClassProvider</code> to rename a IType
	 * @param renamed
	 */
	public static RenameClassProvider createProvider(IType renamed) {
		IGroovyFileProvider fileProvider = RenameRefactoringConverter.getFileProvider(renamed);
		String fullName = renamed.getFullyQualifiedName();
		ClassNode node = ClassHelper.makeWithoutCaching(fullName);
		RenameClassProvider provider = new RenameClassProvider(fileProvider, node, renamed.getCompilationUnit());
		return provider;
	}

}
