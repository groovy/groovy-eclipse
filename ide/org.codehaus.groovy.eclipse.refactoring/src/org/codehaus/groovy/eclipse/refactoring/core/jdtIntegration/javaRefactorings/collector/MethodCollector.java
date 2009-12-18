/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.collector;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.MethodPattern;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Collect's inherited methods and invocations 
 * 
 * @author Stefan Reinhard
 */
public class MethodCollector extends SimpleNameCollector {
	
	private MethodPattern methodPattern;
	private ClassNode declaringClassNode;
	
	private IMethodBinding methodBinding;
	private ITypeBinding declaringType;
	
	public MethodCollector(MethodPattern methodPattern) {
		this.methodPattern = methodPattern;
		declaringClassNode = methodPattern.getClassType();
	}
	
	/**
	 * For all inherited methods from superclasses and interfaces
	 */
	@Override
    public boolean visit(MethodDeclaration node) {
		methodBinding = node.resolveBinding();
		declaringType = methodBinding.getDeclaringClass();
		checkInheritedBySuperclass(node); 
		checkInheritedByInterface(node);
		return true;
	}

	private void checkInheritedBySuperclass(MethodDeclaration node) {
		ITypeBinding superType = declaringType.getSuperclass();
		if (superType.getQualifiedName().equals(declaringClassNode.getName())) {
			checkSignature(methodBinding, node.getName());
		}
	}
	
	private void checkInheritedByInterface(MethodDeclaration node) {
		ITypeBinding[] interfaces = declaringType.getInterfaces();
		for (ITypeBinding implemented : interfaces) {
			if (implemented.getQualifiedName().equals(declaringClassNode.getName())) {
				checkSignature(methodBinding, node.getName());
			}
		}
	}
	
	/**
	 * For all method calls
	 */
	@Override
    public boolean visit(MethodInvocation invocation) {
		IMethodBinding methodBinding = invocation.resolveMethodBinding();
		String declaringClassName = methodBinding.getDeclaringClass().getQualifiedName();
		if (declaringClassName.equals(declaringClassNode.getName())) {
			checkSignature(methodBinding, invocation.getName());
		}
		return true;
	}
	
	private void checkSignature(IMethodBinding method, SimpleName occurence) {
		if (method.getName().equals(methodPattern.getMethodName()) &&
				method.getParameterTypes().length==methodPattern.getArgSize()) {
			occurences.add(occurence);
		}
	}
	
	@Override
    public boolean visit(MethodRef ref) {
		System.out.println(ref);
		return true;
	}

}
