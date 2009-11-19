/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.collector;

import org.codehaus.groovy.eclipse.refactoring.core.utils.StringUtils;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Collect Getter and Setter for a Field
 * 
 * @author Stefan Reinhard
 */
public class MutatorCollector extends SimpleNameCollector {

	private String setter;
	private String getter;
	private String className;
	
	public MutatorCollector(String className, String fieldName) {
		this.className = className;
		setter = "set" + StringUtils.capitalize(fieldName);
		getter = "get" + StringUtils.capitalize(fieldName);
	}
	
	public boolean visit(MethodInvocation invocation) {
		IMethodBinding methodBinding = invocation.resolveMethodBinding();
		String declaringClassName = methodBinding.getDeclaringClass().getQualifiedName();
		if (declaringClassName.equals(className) &&
				methodBinding.getName().equals(setter) ||
					methodBinding.getName().equals(getter)) {
			occurences.add(invocation.getName());
		}
		return true;
	}
}
