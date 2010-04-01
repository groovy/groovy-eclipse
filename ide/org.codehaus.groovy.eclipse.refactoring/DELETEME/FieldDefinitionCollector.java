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
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.RefactoringCodeVisitorSupport;
import org.codehaus.groovy.eclipse.refactoring.core.utils.patterns.FieldPattern;

/**
 * @author Stefan Reinhard
 */
public class FieldDefinitionCollector extends RefactoringCodeVisitorSupport {

	private FieldPattern fieldPattern;
	List<FieldNode> definitions = new LinkedList<FieldNode>();
	private boolean dynamicTyped;
	
	public FieldDefinitionCollector(ModuleNode rootNode, FieldPattern pattern) {
		super(rootNode);
		fieldPattern = pattern;
		if (pattern.getDeclaringClass().equals(ClassHelper.make(Object.class))) {
			dynamicTyped = true;
		} else {
			dynamicTyped = false;
		}
	}
	
	@Override
    public void visitField(FieldNode node) {
		super.visitField(node);
		FieldPattern candidatePattern = new FieldPattern(node);
		if (dynamicTyped) {
			if (fieldPattern.equalsName(candidatePattern)) {
				definitions.add(node);
			}
		} else {
			if (fieldPattern.equals(candidatePattern)) {
				definitions.add(node);
			}
		}
	}

	public List<FieldNode> getFieldDefinitions() {
		return definitions;
	}
}
