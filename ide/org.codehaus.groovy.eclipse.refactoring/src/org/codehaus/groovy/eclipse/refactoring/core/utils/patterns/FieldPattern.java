/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.utils.patterns;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;

/**
 * Pattern that represents a Field of a Class as accurate as possible
 * @author reto kleeb
 *
 */
public class FieldPattern {
	
	private FieldNode originalFieldNode;
	private final ClassNode declaringClass;
	private final ClassNode typeOfProperty;
	private final String nameOfProperty;
	private final ASTNode selectedASTNode;
	
	public FieldPattern(ClassNode declaringClass, ClassNode typeOfProperty, String nameOfProperty, ASTNode selectedASTNode) {
		this.declaringClass = declaringClass;
		this.typeOfProperty = typeOfProperty;
		this.nameOfProperty = nameOfProperty;
		this.selectedASTNode = selectedASTNode;
	}
	
	public FieldPattern(ClassNode declaringClass, ClassNode typeOfProperty, String nameOfProperty) {
		this(declaringClass, typeOfProperty, nameOfProperty, null);
	}

	public FieldPattern(FieldNode original, ASTNode selectedASTNode) {
		this(original.getDeclaringClass(), original.getType(), original.getName(), selectedASTNode);
		this.originalFieldNode = original;
	}
	
	public FieldPattern(FieldNode original) {
		this(original.getDeclaringClass(), original.getType(), original.getName(), null);
		this.originalFieldNode = original;
	}

	public String getName(){
		return nameOfProperty;
	}
	
	@Override
    public boolean equals(Object obj) {
		if(obj instanceof FieldPattern){
			FieldPattern otherFieldPattern = (FieldPattern) obj;
			return this.declaringClass.equals(otherFieldPattern.getDeclaringClass()) &&
				this.typeOfProperty.equals(otherFieldPattern.getTypeOfProperty()) &&
				equalsName(obj);
		}
		return false;
	}

	public FieldNode getOriginalFieldNode() {
		return originalFieldNode;
	}

	public ClassNode getDeclaringClass() {
		return declaringClass;
	}

	public ClassNode getTypeOfProperty() {
		return typeOfProperty;
	}

	public String getNameOfProperty() {
		return nameOfProperty;
	}

	public boolean equalsName(Object obj) {
		if(obj instanceof FieldPattern){
			FieldPattern otherFieldPattern = (FieldPattern) obj;
			return this.nameOfProperty.equals(otherFieldPattern.getNameOfProperty());
		}
		return false;
	}

	public ASTNode getSelectedASTNode() {
		return selectedASTNode;
	}

}
