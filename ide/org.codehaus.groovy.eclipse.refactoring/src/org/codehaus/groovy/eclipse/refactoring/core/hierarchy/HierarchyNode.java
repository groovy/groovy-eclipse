/* 
 * Copyright (C) 2008, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.hierarchy;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;

/**
 * Class representing a Node in a type hierarchy
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class HierarchyNode {
	
	public final String className;

	private final ClassNode originClass;
	
	private String extClass;
	private Set<String> implInterfaces;
		
	private HierarchyNode extendingClass;
	private Set<HierarchyNode> implementedInterfaces, implementingChildern, extendingChildern;

	
	/**
	 * Instancing a class with a given class node.
	 * This initializes only the Names of the parent-, implementing- and child Classes
	 * The references to other HierarchyNodes has to be set afterwards.
	 * @param classNode
	 */
	public HierarchyNode(ClassNode classNode) {
		this.originClass = classNode;
		this.className = originClass.getName();
		
		extClass = null;
		if(originClass.getSuperClass() != null)
			extClass = originClass.getSuperClass().getName();
		
		implInterfaces = new HashSet<String>();
		for(ClassNode interf : originClass.getInterfaces()) {
			implInterfaces.add(interf.getName());
		}
		
		implementedInterfaces = new HashSet<HierarchyNode>();
		implementingChildern = new HashSet<HierarchyNode>();
		extendingChildern = new HashSet<HierarchyNode>();

	}

	/**
	 * Adding a new child which implements this class
	 * @param node implementing child
	 */
	public void insertImplementingChild(HierarchyNode node) {
		if(originClass.isInterface() && node != null)
			implementingChildern.add(node);
	}
	/**
	 * Adding a new child which extends this class
	 * @param node extending child
	 */
	public void insertExtendingChild(HierarchyNode node) {
		if(node != null) {
			extendingChildern.add(node);
		}
	}
	/**
	 * Set the super class of this class
	 * @param node super class
	 */
	public void setExtendingClass(HierarchyNode node) {
		extendingClass = node;
	}
	/**
	 * Adding a new interfaces which is implemented by this class
	 * @param node the implemented interfaces
	 */
	public void insertImplementingInterface(HierarchyNode node) {
		implementedInterfaces.add(node);
	}
	
	/**
	 * Returns the full qualified class name of this node
	 * @return
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Return true if this node represents an interface
	 * @return
	 */
	public boolean isInterface() {
		return originClass.isInterface();
	}

	/**
	 * Return true if the corresponding class node is editable.
	 * The source code of this class is availlable
	 * @return
	 */
	public boolean isPrimaryClassNode() {
		return originClass.isPrimaryClassNode();
	}

	/**
	 * Returns the super class of this node
	 * @return
	 */
	public String getExtClass() {
		return extClass;
	}
	
	/**
	 * Return all names of the interfaces which are implemented by this node
	 * @return
	 */
	public Set<String> getImplInterfaces() {
		return implInterfaces;
	}

	/**
	 * Return the name of super class of this node
	 * @return
	 */
	public HierarchyNode getExtendingClass() {
		return extendingClass;
	}

	public Set<HierarchyNode> getImplementsClasses() {
		return implementedInterfaces;
	}

	public Set<HierarchyNode> getImplementingChildern() {
		return implementingChildern;
	}

	public Set<HierarchyNode> getExtendingChildern() {
		return extendingChildern;
	}
	
	public ClassNode getOriginClass() {
		return originClass;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("\n------\n" + className + "\n");
	
		if(extClass != null) {
			b.append("Ext: " + extClass);
		}
		if(implInterfaces.size() > 0) {
			b.append(" Impl: ");
			for(String n : implInterfaces) {
				b.append(n + ", ");
			}
		}
		
		b.append("\n");
		
		if(extendingClass != null) {
			b.append("Ext: " + extendingClass.className);
		}
		if(implementedInterfaces.size() > 0) {
			b.append(" Impl: ");
			for(HierarchyNode n : implementedInterfaces) {
				b.append(n.className + ", ");
			}
		}
		if(implementingChildern.size() > 0) {
			b.append(" ImplChild: ");
			for(HierarchyNode n : implementingChildern) {
				b.append(n.className + ", ");
			}
		}
		if(extendingChildern.size() > 0) {
			b.append(" ExtChild: ");
			for(HierarchyNode n : extendingChildern) {
				b.append(n.className + ", ");
			}
		}
		return b.toString() + "\n";
	}
}
