/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.helper;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

/**
 * Resolver for type signatures from ITypes
 * 
 * @author Stefan Reinhard
 */
public class TypeResolver {
	
	/**
	 * Resolves the type name only
	 * @param signature acquired by type 
	 * @param declaring type of signature
	 */
	public static String getName(String signature, IType declaring) {
		String packageName = Signature.getSignatureQualifier(signature);
		String fullName = (packageName.trim().equals("") ? "" : packageName + ".")
		+ Signature.getElementType(Signature.getSignatureSimpleName(signature));
		return fullName;
	}

	/**
	 * Resolves the fully qualified name
	 * @param signature acquired by type 
	 * @param declaring type of signature
	 */
	public static String getFQN(String signature, IType declaring) {
		String fullName = getName(signature, declaring);
		
		if (fullName.indexOf("[") > 0) {
			fullName = fullName.substring(0, fullName.indexOf("["));
		}
		
		String resolvedType = resolveType(fullName, declaring);
		
		if (resolvedType != null && resolvedType.length() > 0) {
			return resolvedType;
		}
		
		return fullName;
	}
	
	private static String resolveType(String tName, IType declaring) {
		String[] firstLocation = null;
		String[][] resolvedTypeNames = null;
		StringBuilder name = new StringBuilder();
		
		try {
			if (tName != null) {
				resolvedTypeNames = declaring.resolveType(tName);

				if (resolvedTypeNames != null) {
					firstLocation = resolvedTypeNames[0];
					for (int i = 0; i < firstLocation.length; i++) {
						name.append(firstLocation[i]);
						if (i < firstLocation.length - 1)
							name.append(".");
					}
				}

				return name.toString();
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}

}
