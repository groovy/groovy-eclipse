package org.codehaus.groovy.ast;

public class ClassNodeReference {

	public static final ClassNodeReference[] EMPTY_ARRAY = new ClassNodeReference[0];
	
	ClassNode classNode;
	
	public ClassNodeReference(ClassNode classNode) {
		this.classNode = classNode;
	}
	
	public ClassNode getClassNode() {
		return classNode;
	}

}
