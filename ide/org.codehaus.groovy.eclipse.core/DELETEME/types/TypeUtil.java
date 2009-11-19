 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.util;


import static org.codehaus.groovy.eclipse.core.util.ListUtil.newEmptyList;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.objectweb.asm.Opcodes;

public class TypeUtil {

	public static final String OBJECT_TYPE = "java.lang.Object";
	
	
	/**
	 * Match ignoring case and checking camel case.
	 * @param prefix
	 * @param target
	 * @return
	 */
	public static boolean looselyMatches(String prefix, String target) {
		// Zero length string matches everything.
		if (prefix.length() == 0) {
			return true;
		}
		
		// Exclude a bunch right away
		if (prefix.charAt(0) != target.charAt(0)) {
			return false;
		}
		
		if (target.startsWith(prefix)) {
			return true;
		}
		
		String lowerCase = target.toLowerCase();
		if (lowerCase.startsWith(prefix)) {
			return true;
		}
		
		// Test for camel characters in the prefix.
		if (prefix.equals(prefix.toLowerCase())) {
			return false;
		}
		
		String[] prefixParts = toCamelCaseParts(prefix);
		String[] targetParts = toCamelCaseParts(target);
		
		if (prefixParts.length > targetParts.length) {
			return false;
		}
		
		for (int i = 0; i < prefixParts.length; ++i) {
			if (!targetParts[i].startsWith(prefixParts[i])) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Convert an input string into parts delimited by upper case characters. Used for camel case matches.
	 * e.g. GroClaL = ['Gro','Cla','L'] to match say 'GroovyClassLoader'.
	 * e.g. mA = ['m','A']
	 * @param str
	 * @return
	 */
	private static String[] toCamelCaseParts(String str) {
		List< String > parts = newEmptyList();
		for (int i = str.length() - 1; i >= 0; --i) {
			if (Character.isUpperCase(str.charAt(i))) {
				parts.add(str.substring(i));
				str = str.substring(0, i);
			}
		}
		if (str.length() != 0) {
			parts.add(str);
		}
		Collections.reverse(parts);
		return parts.toArray(new String[parts.size()]);
	}
	
    private static String join(final String[] strings, final String delim) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strings.length; i++) {
        	if (strings[i].length() > 0) {
                sb.append(strings[i]);
                if (i < strings.length-1) {
                    sb.append(delim);
                }
        	}
        }
        return sb.toString();
    }

}
