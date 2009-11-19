/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.rename;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.RefactoringCodeVisitorSupport;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.MethodPattern;

/**
 * @author Stefan Reinhard
 */
public class MethodDefinitionCollector extends RefactoringCodeVisitorSupport {
	
	private MethodPattern methodPattern;
	List<MethodNode> definitons = new LinkedList<MethodNode>();
	private boolean dynamicTyped;
	
	public MethodDefinitionCollector(ModuleNode rootNode, MethodPattern pattern) {
		super(rootNode);
		methodPattern = pattern;
		if(pattern.getClassType().equals(ClassHelper.make(Object.class))) {
			dynamicTyped = true;
		} else {
			dynamicTyped = false;
		}
	}
	
	@Override
	public void visitMethod(MethodNode node) {
		super.visitMethod(node);
		if (dynamicTyped) { 
			MethodPattern pattern = new MethodPattern(node); 
			if (methodPattern.equalSignature(pattern)) { 
				definitons.add(node); 
			}
		} else { 
			MethodPattern pattern = new MethodPattern(node, node.getDeclaringClass());
			if (methodPattern.equalsExactly(pattern)) { 
				definitons.add(node); 
			}
		}
	}

	
	public List<MethodNode> getMethodDefinitions() {
		return definitons;
	}
	
}
