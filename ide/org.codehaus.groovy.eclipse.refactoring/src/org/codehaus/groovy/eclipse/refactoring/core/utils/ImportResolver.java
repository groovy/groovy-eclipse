/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.utils;

import java.util.Collection;
import java.util.List;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.ResolveVisitor;

public class ImportResolver {

	/**
	 * Evaluates, if a Class must be written with or without package or even as alias
	 * 
	 * @param root ModuleNode to get the imports
	 * @param classType type to evaluate
	 * @param resolve considering default imports
	 * @return type name with or without package or as alias
	 */
	public static String getResolvedClassName(ModuleNode root, ClassNode classType, boolean defaultImports) {
		String alias = ImportResolver.asAlias(root, classType);
		if (alias.equals("")) {
			List<ImportNode> imports = root.getImports();
			//check if class is imported as class like foo.Bar
	    	for (ImportNode importNode :imports) {
	    		//if Class is imported write class without package
	    		if (importNode.getType().getName().equals(classType.getName())) {
	    			return classType.getNameWithoutPackage();
	    		}
	    	}
	    	//check if class is imported as package like foo.* and class is in package foo
	    	List<String> packageImports = root.getImportPackages();
	    	for (String packageName : packageImports) {
	    		//if Class is imported write class without package
	    		String classPackage = classType.getPackageName() + ".";
	    		if (packageName.equals(classPackage)) {
	    			return classType.getNameWithoutPackage();
	    		}
	    	}
	    	
	    	//check if it was a static import as package like java.lang.Math.PI
	    	if (root.getStaticImportAliases().values().contains(classType)) {
	    		return classType.getNameWithoutPackage();
	    	}
	    	
	    	//check if it was a static import as package like java.lang.Math.*
	    	if (root.getStaticImportClasses().values().contains(classType)) {
	    		return classType.getNameWithoutPackage();
	    	}
	    	
	    	//check if class is in own package
	    	if (root.hasPackageName()) {
	        	if (root.getPackageName().equals(classType.getPackageName() + ".")) {
	        		return classType.getNameWithoutPackage();
	        	}
	    	}
	    	
	    	if (defaultImports) {
	        	//check if class is imported with the default imports
	        	//DEFAULT_IMPORTS = {"java.lang.", "java.io.", "java.net.", "java.util.", "groovy.lang.", "groovy.util."}
	        	for (String packageName : ResolveVisitor.DEFAULT_IMPORTS ) {
	        		//if Class is imported write class without package
	        		if (packageName.equals(classType.getPackageName() + ".")) {
	        			return classType.getNameWithoutPackage();
	        		}
	        	}
	        	/*
	        	 * check if class is BigInteger or BigDecimal, these two classes are also imported
	        	 * as default
	        	 */
	        	if (classType.equals(ClassHelper.BigDecimal_TYPE)) {
	        		return classType.getNameWithoutPackage();
	        	} else if (classType.equals(ClassHelper.BigInteger_TYPE)) {
	        		return classType.getNameWithoutPackage();
	        	}
	    	}
	    	//class is not imported, write class with package
	    	return classType.getName();
		}
        return alias;
	}

	/**
	 * Evaluates if there is an alias for the type
	 * 
	 * @param root ModuleNode to get the imports
	 * @param classType type to evalueate
	 * @return the alias or an empty string if there is no alias
	 */
	public static String asAlias(ModuleNode root, ClassNode type) {
		//Test if class that is represented in ClassNode is imported with Alias
		List<ImportNode> imports = root.getImports();
		for (ImportNode importNode :imports) {
			if (ImportResolver.isExplizitAlias(importNode)) {
				if (importNode.getType().getName().equals(type.getName())) {
					return importNode.getAlias();
				}
			}
		}
		
		//Test if class that is represented in ClassNode is static imported with Alias
		Collection<String> staticImportKeys = root.getStaticImportAliases().keySet();
		for (String possibleAlias : staticImportKeys) {
			//if Class is imported write class without package
			if (root.getStaticImportAliases().values().contains(type)) {
				String fieldName = (String)root.getStaticImportFields().get(possibleAlias);
	    		if (!fieldName.equals(possibleAlias)) {
	    			return possibleAlias;
	    		} 
			}
		}
		return "";
	}

	/**
	 * In each ImportNode the alias is saved. But only if the type name and the alias saved
	 * in the ImportNode are different, an alias is used / makes sense
	 * @param importNode node to check
	 * @return true when an alias is used otherwise false
	 */
	public static boolean isExplizitAlias(ImportNode importNode) {
		return !importNode.getType().getNameWithoutPackage().equals(importNode.getAlias());
	}

	/**
	 * Findout if a PropertyExpression can be printed just as field that was imported
	 * 
	 * @param root ModuleNode to get the Imports
	 * @param type package that is imported
	 * @param possibleField  
	 * @return possibleField if it's imported as filed or empty string if not
	 */
	public static String asFieldName(ModuleNode root, ClassNode type, String possibleField) {
		//Test if class that is represented in ClassNode is static imported with Alias
		Collection<String> staticImportKeys = root.getStaticImportAliases().keySet();
		for (String key : staticImportKeys) {
			//if Class is imported write class without package
			if (root.getStaticImportAliases().values().contains(type)) {
				String fieldName = (String)root.getStaticImportFields().get(key);
	    		if (fieldName != null) {
	    			return fieldName;
	    		}
			}
		}
		
	   	if (root.getStaticImportClasses().values().contains(type)) {
	   		if (possibleField != null) {
	   			return possibleField;
	   		}
		}
		return "";
	}

}
