/* 
 * Copyright (C) 2008, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.hierarchy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;

/**
 * Class to represent the Class hierarchy of a project
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class HierarchyTreeBuilder {
	
	private Map<String, HierarchyNode> container;

	public HierarchyTreeBuilder(IGroovyFileProvider docProvider) {
		container = new HashMap<String, HierarchyNode>();
		populateContainer(docProvider);
		linkContainerNodes();
	}
	
	/**
	 * Return all with the given class connected Nodes.
	 * A connection can bee an extension of a class or an implementation of an interface.
	 * @param startClass Class Node which is the start of the search
	 * @return Collection with all connected nodes.
	 */
	public Map<String, HierarchyNode> getInterconnectedClasses(ClassNode startClass) {
		
		Map<String, HierarchyNode>pool, candiatesA, candiatesB, result;
		
		pool = new HashMap<String, HierarchyNode>();
		candiatesA = new HashMap<String, HierarchyNode>();
		candiatesB = new HashMap<String, HierarchyNode>();
		result = new HashMap<String, HierarchyNode>();
		
		pool.putAll(container);
		HierarchyNode startNode = pool.get(startClass.getName());
		
		if (startNode == null) {
			// Set Java class as begin
			startNode = new HierarchyNode(startClass); 
			candiatesB.put(startNode.className, startNode);
		} else {
			candiatesB.put(startNode.className, pool.remove(startNode.className));
		}
		while(!candiatesB.isEmpty()) {

			for(HierarchyNode currentNode : candiatesB.values()) {
				// Adding Interfaces and super classes to candiates A
				if(currentNode.getExtendingClass() != null) {
					HierarchyNode poolNode = pool.remove(currentNode.getExtendingClass().getClassName());
					if(isValidInterconnectionType(poolNode))
						candiatesA.put(poolNode.getClassName(),poolNode);
				}
				for(HierarchyNode interf : currentNode.getImplementsClasses()) {
					HierarchyNode poolNode = pool.remove(interf.getClassName());
					if(isValidInterconnectionType(poolNode))
						candiatesA.put(interf.getClassName(),poolNode);
				}
				// Adding Children extending and implementing this class to candiates A
				for(HierarchyNode extChild : currentNode.getExtendingChildern()) {
					HierarchyNode poolNode = pool.remove(extChild.getClassName());
					if(isValidInterconnectionType(poolNode))
						candiatesA.put(extChild.getClassName(),poolNode);
				}
				for(HierarchyNode imlInterf : currentNode.getImplementingChildern()) {
					HierarchyNode poolNode = pool.remove(imlInterf.getClassName());
					if(isValidInterconnectionType(poolNode))
						candiatesA.put(imlInterf.getClassName(),poolNode);
				}
			}
			result.putAll(candiatesB);
			candiatesB.clear();
			candiatesB.putAll(candiatesA);
			candiatesA.clear();
		}
//		System.out.println(result);
		return result;
		
	}
	private boolean isValidInterconnectionType(HierarchyNode node) {
		if(node == null)
			return false;
		if(node.getClassName().equals("groovy.lang.Script"))
			return false;
		if(node.getClassName().equals("java.lang.Object"))
			return false;
		if(node.getClassName().equals("groovy.lang.GroovyObject"))
			return false;
		return true;		
	}

	private void linkContainerNodes() {
		for(HierarchyNode currrentNode : container.values()) {
			// Extension Linking
			if(currrentNode.getExtClass() != null) {
				container.get(currrentNode.getExtClass()).insertExtendingChild(currrentNode);
				currrentNode.setExtendingClass(container.get(currrentNode.getExtClass()));
			}
			// Interface Linking
			for(String interf : currrentNode.getImplInterfaces()) {
				container.get(interf).insertImplementingChild(currrentNode);
				currrentNode.insertImplementingInterface(container.get(interf));
			}
		}		
	}

	private void populateContainer(IGroovyFileProvider docProvider) {
		for(IGroovyDocumentProvider dp : docProvider.getAllSourceFiles()) {
			if(dp.getRootNode() != null) {
				for(ClassNode cn : (List<ClassNode>)dp.getRootNode().getClasses()) {
					createNamedHNode(cn,container);
				}
			}
		}
	}

	private HierarchyNode createNamedHNode(ClassNode classNode, Map<String, HierarchyNode> cnt) {
		HierarchyNode chn = new HierarchyNode(classNode);
		for(ClassNode interf : classNode.getInterfaces()) {
			createNamedHNode(interf, cnt);
		}
		cnt.put(chn.className, chn);
		if(classNode.getSuperClass() != null) {
			createNamedHNode(classNode.getSuperClass(),cnt);
		}
		return chn;
	}
	
	public HierarchyNode getNode(String className) {
		return container.get(className);
	}

	public HierarchyNode getHierarchyForClass(ClassNode currClass) {
		HierarchyNode root = null;
//		HierarchyNode newStart = null;
		
		if(container.containsKey(currClass.getName())) {
			HierarchyNode startPoint = container.get(currClass.getName());
			root = addSuperNode(startPoint);
		}
		return root;
	}

	private HierarchyNode addSuperNode(HierarchyNode startPoint) {
		if(startPoint.isInterface()) {
			if(!startPoint.getImplementsClasses().isEmpty()) {
				HierarchyNode superClass = new HierarchyNode(startPoint.getImplementsClasses().iterator().next().getOriginClass());
				startPoint.setExtendingClass(superClass);
				superClass.insertExtendingChild(startPoint);
				return addSuperNode(superClass);				
			}
		}
		if(startPoint.getExtendingClass() != null) {
			HierarchyNode superClass = new HierarchyNode(startPoint.getExtendingClass().getOriginClass());
			startPoint.setExtendingClass(superClass);
			superClass.insertExtendingChild(startPoint);
			return addSuperNode(superClass);
		}
	return startPoint;
	}
	public Map<String, HierarchyNode> getCompleteClassStructure() {
		return container;
	}

}
