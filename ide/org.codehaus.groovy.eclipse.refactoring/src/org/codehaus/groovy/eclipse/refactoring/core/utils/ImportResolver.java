/*
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

            // check if class is imported as class like foo.Bar
            ImportNode found = findImportNodeForClass(classType, root.getImports());
            if (found != null) {
                return found.getType().getNameWithoutPackage();
            }

            // check if it was a static import as package like java.lang.Math.*
            found = findImportNodeForClass(classType, root.getStaticStarImports().values());
            if (found != null) {
                return found.getType().getNameWithoutPackage();
            }

            // check if it was a static import as package like java.lang.Math.PI
            found = findImportNodeForClass(classType, root.getStaticImports().values());
            if (found != null) {
                return found.getType().getNameWithoutPackage();
            }

	    	//check if class is in own package
            String packageNameWithDot = classType.getPackageName() + ".";
	    	if (root.hasPackageName()) {
                if (root.getPackageName().equals(packageNameWithDot)) {
	        		return classType.getNameWithoutPackage();
	        	}
	    	}

            // check if class is imported as package like foo.* and class is in
            // package foo
            List<ImportNode> packageImports = root.getStarImports();
            for (ImportNode imp : packageImports) {
                // if Class is imported write class without package
                if (imp.getPackageName().equals(packageNameWithDot)) {
                    return classType.getNameWithoutPackage();
                }
            }

	    	if (defaultImports) {
	        	//check if class is imported with the default imports
	        	//DEFAULT_IMPORTS = {"java.lang.", "java.io.", "java.net.", "java.util.", "groovy.lang.", "groovy.util."}
	        	for (String packageName : ResolveVisitor.DEFAULT_IMPORTS ) {
	        		//if Class is imported write class without package
                    if (packageName.equals(packageNameWithDot)) {
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

    private static ImportNode findImportNodeForClass(ClassNode toFind, Collection<ImportNode> imports) {
        for (ImportNode imp : imports) {
            if (imp.getType() != null && imp.getType().getName().equals(toFind.getName())) {
                return imp;
            }
        }
        return null;
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
        ImportNode imp = findImportNodeForClass(type, root.getImports());
        if (imp != null && isExplicitAlias(imp)) {
            return imp.getAlias();
        }

        return "";
	}

	/**
	 * In each ImportNode the alias is saved. But only if the type name and the alias saved
	 * in the ImportNode are different, an alias is used / makes sense
	 * @param importNode node to check
	 * @return true when an alias is used otherwise false
	 */
    public static boolean isExplicitAlias(ImportNode importNode) {
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
        ImportNode imp = root.getStaticImports().get(possibleField);
        if (imp != null && imp.getType() != null && imp.getType().getName().equals(type.getName())) {
            return possibleField;
        }

		// check to see if class is imported as 'import static
        // com.foo.MyClass.*'
        imp = findImportNodeForClass(type, root.getStaticStarImports().values());
        if (imp != null) {
            return possibleField;
		}
		return "";
	}

}
