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
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.helper.HierarchyBuilder;
import org.codehaus.groovy.eclipse.refactoring.core.rename.renameField.RenameFieldProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.FieldPattern;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;

/**
 * @author Stefan Reinhard
 */
public class FieldRenameConverter {
	
	/**
	 * Creates a <code>RenameFieldProvider</code> to rename a IField
	 * @param renamed
	 */
	public static RenameFieldProvider createProvider(IField renamed) {
		IGroovyFileProvider fileProvider = RenameRefactoringConverter.getFileProvider(renamed);
		FieldPattern pattern = createFieldPattern(renamed);
		RenameFieldProvider provider = new RenameFieldProvider(fileProvider, pattern);
		return provider;
	}

	private static FieldPattern createFieldPattern(IField renamed) {
		String fieldName = renamed.getElementName();
		IType declaringType = renamed.getDeclaringType();
		String declaringClassName = declaringType.getFullyQualifiedName();
		ClassNode declaringClass = ClassHelper.make(declaringClassName);
		HierarchyBuilder.addHierarchyToType(declaringType, declaringClass);
		return new FieldPattern(declaringClass, fieldName);
	}

}
