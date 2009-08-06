/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
/**
 * 
 */
package org.codehaus.groovy.eclipse.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;

/**
 * @author thorsten
 *
 */
public class ImportManager extends ArrayList<String> {
	private static final long serialVersionUID = -7805550495730173835L;
	private final IJavaProject project;
	private String basePackage = "";
	private final String[] notAllowed = new String[]{"<", "["};
	private static Set<String> defaultImports = new HashSet<String>();
	private static Map<String, String> primitives = new HashMap<String, String>();
	private final List<String> genericTypes = new ArrayList<String>();
	
	static {
		defaultImports.add("java.lang");
		defaultImports.add("java.io");
		defaultImports.add("java.net");
		defaultImports.add("groovy.lang");
		defaultImports.add("groovy.util");
	}

	static {
		primitives.put("boolean", "false");
		primitives.put("byte", "0");
		primitives.put("char", "''");
		primitives.put("short", "0");
		primitives.put("int", "0");
		primitives.put("long", "0");
		primitives.put("foat", "0");
		primitives.put("double", "0");
		primitives.put("void", null);
	}
	/**
	 * @param project
	 */
	public ImportManager(IJavaProject project) {
		super();
		this.project = project;		
	}
	
	public void setBasePackage(String basePackage){
		this.basePackage = basePackage;
	}
	

	/* (non-Javadoc)
	 * @see java.util.ArrayList#clear()
	 */
	@Override
    public void clear() {
		basePackage = null;
		super.clear();
	}

	/* (non-Javadoc)
	 * @see java.util.TreeSet#add(java.lang.Object)
	 */
	public boolean addImport(String typeName) {
		typeName = removeGenericTypeInformations(typeName);
		
		if (basePackage.equals(typeName)){
			return false;
		}
		
		if (contains(typeName)){
			return false;
		}

		if (typeName.indexOf(".")>0){
			String baseName = typeName.substring(0, typeName.lastIndexOf("."));
			if (basePackage.equals(baseName) || contains(baseName+".*")
					|| defaultImports.contains(baseName)){
				return false;
			}
		}else{
			if (!isPrimitive(typeName) && !genericTypes.contains(typeName)){
				genericTypes.add(typeName);
			}
			return false;
		}
		return super.add(typeName);
	}
	
	public void sortImports(){
		Collections.sort(this);
	}
	
	public void sortGenericTypes(){
		Collections.sort(genericTypes);
	}
	
	public String toSimpleName(String typeName){
		
		if (typeName.indexOf(".")>0){
			return typeName.substring(typeName.lastIndexOf(".")+1);
		}else{
			return typeName;
		}
	}
	
	public boolean isPrimitive(String typeName){
		return primitives.containsKey(typeName);
	}
	
	public String getPrimitiveDefaultValue(String typeName){
		return primitives.get(typeName);
	}

	@Override
    public String toString(){
		String imports = "";
		
		sortImports();
		for (int i=0;i<size();i++){
			imports += "import "+get(i)+CodeGeneration.getLineDelimiter(project);
		}
		return imports;
	}

	public String getGenericTypesAsString() {
		String generics = "";
		
		if (genericTypes.isEmpty()){
			return generics;
		}
		
		sortGenericTypes();
		for (int i=0;i<genericTypes.size();i++){
			generics += genericTypes.get(i);
			if (i < genericTypes.size()-1){
				generics += ", ";
			}
		}		
		return "<"+generics+">";
	}
	
	private String removeGenericTypeInformations(String typeName){
		for (int i = 0; i < notAllowed.length; i++) {
			if (typeName.contains(notAllowed[i])){
				typeName = typeName.substring(0, typeName.indexOf(notAllowed[i]));
			}
		}
		return typeName;
	}
}
