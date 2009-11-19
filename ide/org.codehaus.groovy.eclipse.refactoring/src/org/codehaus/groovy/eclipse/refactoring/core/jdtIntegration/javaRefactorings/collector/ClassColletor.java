/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.collector;

import org.codehaus.groovy.ast.ClassNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Collect's Classes with equal fully qualified names
 * 
 * @author Stefan Reinhard
 */
public class ClassColletor extends SimpleNameCollector {
	
	private ClassNode node;
	
	public ClassColletor(ClassNode node) {
		this.node = node;
	}
	
	public boolean visit(SimpleName name) {
		IBinding binding = name.resolveBinding();
		if (binding instanceof ITypeBinding) {
			ITypeBinding typeBinding = (ITypeBinding)binding;
			String qualifiedName = typeBinding.getQualifiedName();
			if (qualifiedName.equals(node.getName())) {
				occurences.add(name);
			}
		}
		return true;
	}

}
