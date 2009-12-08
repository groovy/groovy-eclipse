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

import org.codehaus.groovy.ast.ASTNode;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Holds all potential candidates when a refactoring has due to dynamic
 * typing multiple possible candidates. These candidates can be either
 * defined in Groovy or in Java.
 * 
 * @author Stefan Reinhard
 */
public class RenameCandidates {
	
	private class CandidateList<T> {
		public String name;
		public T[] candidates;
		public CandidateList(String name, T[] candidates) {
			this.name = name;
			this.candidates = candidates;
		}
	};
	
	public static final String GROOVY = "Groovy (no files will be renamed)";
	public static final String JAVA = "Java (files renamed if selected element is a primary type)";
	
	private List<CandidateList<? extends Object>> candidateMap;

	public RenameCandidates() {
		candidateMap = new LinkedList<CandidateList<? extends Object>>();
	}
	
	public RenameCandidates(ASTNode[] groovy, IJavaElement[] java) {
		this();
		candidateMap.add(new CandidateList<ASTNode>(GROOVY,groovy));
		candidateMap.add(new CandidateList<IJavaElement>(JAVA,java));
	}
	
	public ASTNode[] getGroovyCandidates() {
		return (ASTNode[])getCandidateList(GROOVY);
	}

	public IJavaElement[] getJavaCandidates() {
		return (IJavaElement[])getCandidateList(JAVA);
	}
	
	public Object[] getCandidateList(Object listName) {
		for (CandidateList<? extends Object> c : candidateMap) {
			if (c.name.equals(listName)) {
				return c.candidates;
			}
		}
		return new Object[0];
	}
	
	public Object[] getAllCandidates() {
		LinkedList<Object> allCandidates = new LinkedList<Object>();
		for (CandidateList<? extends Object> c : candidateMap) {
			for (Object candidate : c.candidates) {
				allCandidates.add(candidate);
			}
		}
		return allCandidates.toArray();
	}
	
	public String getListNameFor(Object element) {
		for (CandidateList<? extends Object> c : candidateMap) {
			for(Object o : c.candidates) {
				if (o.equals(element)) {
					return c.name;
				}
			}
		}
		return null;
	}
	
	public String[] getListNames() {
		LinkedList<String> keys = new LinkedList<String>();
		for (CandidateList<? extends Object> c : candidateMap) {
			keys.add(c.name);
		}
		return keys.toArray(new String[keys.size()]);
	}
	
	public void addCandidateList(String name, Object[] candidates) {
		candidateMap.add(new CandidateList<Object>(name, candidates));
	}
	
	public void removeCandidateList(String listName) {
		for (CandidateList<? extends Object> c : candidateMap) {
			if (c.name.equals(listName)) {
				candidateMap.remove(c);
			}
		}
	}
	
	

}
