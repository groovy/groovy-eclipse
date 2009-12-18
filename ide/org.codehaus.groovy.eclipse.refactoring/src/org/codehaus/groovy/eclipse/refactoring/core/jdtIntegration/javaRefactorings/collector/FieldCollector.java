/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.collector;

import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.FieldPattern;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Collector for fields with same FQN.
 * 
 * @author Stefan Reinhard
 */
public class FieldCollector extends SimpleNameCollector {
	
	private String className;
	private String fieldName;
	
	public FieldCollector(FieldPattern fieldPattern) {
		className = fieldPattern.getDeclaringClass().getName();
		fieldName = fieldPattern.getNameOfProperty();
	}
	
	@Override
    public boolean visit(SimpleName name) {
		IBinding binding = name.resolveBinding();
		if (binding instanceof IVariableBinding) {
			IVariableBinding fieldBinding = (IVariableBinding)binding;
			if (!fieldBinding.isField()) return true;
			ITypeBinding typeBinding = fieldBinding.getDeclaringClass();
			String simpleName = binding.getName();
			String declaringClass = typeBinding.getQualifiedName();
			if (declaringClass.equals(className) && simpleName.equals(fieldName)) {
				occurences.add(name);
			}
		}
		return true;
	}

}
