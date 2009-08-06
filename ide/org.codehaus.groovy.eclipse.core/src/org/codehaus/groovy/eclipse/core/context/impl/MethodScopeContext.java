package org.codehaus.groovy.eclipse.core.context.impl;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.eclipse.jface.text.IRegion;

public class MethodScopeContext extends AbstractSourceCodeContext {
	private static final String ID = ISourceCodeContext.METHOD_SCOPE;
	
	public String getId() {
		return ID;
	}
	
	public MethodScopeContext(ISourceBuffer buffer, ASTNode[] astPath, IRegion region) {
		super(buffer, astPath, region);
	}
	
	public String toString() {
		return "Method Scope";
	}
}
