/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRefactorings.collector;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Abstract Visitor to collect specific symbols (SimpleNames) 
 * 
 * @author Stefan Reinhard
 */
public abstract class SimpleNameCollector extends ASTVisitor {
	
	protected LinkedList<SimpleName> occurences = new LinkedList<SimpleName>();
	
	public List<SimpleName> getOccurences() {
		return occurences;
	}

}
