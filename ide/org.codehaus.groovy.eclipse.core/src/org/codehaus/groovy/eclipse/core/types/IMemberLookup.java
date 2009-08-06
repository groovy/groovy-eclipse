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
package org.codehaus.groovy.eclipse.core.types;

/**
 * Low level interface for looking up members of some type. This is used for code completion, type evaluation, browsing,
 * etc. This interface may be implemented to provide dynamic member information for domain specific lookup such as for
 * Grails.
 * 
 * @author empovazan
 */
public interface IMemberLookup extends Modifiers {
	/**
	 * Lookup fields for a type.
	 * 
	 * @param type
	 * @param name The name to match.
	 * @param accessible If true, return only accessible fields. If false, return all fields.
	 * @param staticAccess If true then the type signature is a class. Else it represents an instance.
	 * @param exact
	 *            If true, then perform exact matches only. If false, then the match should ignore case and perform
	 *            camel case lookups. An implementation is available in {@link TypeUtil#looselyMatches(String, String)}
	 * @return
	 */
	public Field[] lookupFields(String type, String name, boolean accessible, boolean staticAccess, boolean exact);

	/**
	 * Lookup getters and setters, converted to Groovy property names. This list may overlap with the fields list.
	 * 
	 * @param type
	 * @param name The name to match.
	 * @param accessible If true, return only accessible properties. If false, return all properties.
	 * @param staticAccess If true then the type signature is a class. Else it represents an instance.
	 * @param exact
	 *            If true, then perform exact matches only. If false, then the match should ignore case and perform
	 *            camel case lookups. An implementation is available in {@link TypeUtil#looselyMatches(String, String)}
	 * @return
	 */
	public Property[] lookupProperties(String type, String name, boolean accessible, boolean staticAccess, boolean exact);

	/**
	 * Lookup all methods with the given name.
	 * 
	 * @param type
	 * @param name
	 *            The name to match.
	 * @param accessible
	 *            If true, return only accessible methods. If false, return all methods.
	 * @param staticAccess
	 *            If true then the type signature is a class. Else it represents an instance.
	 * @param exact
	 *            If true, then perform exact matches only. If false, then the match should ignore case and perform
	 *            camel case lookups. An implementation is available in {@link TypeUtil#looselyMatches(String, String)}
	 *            The number and type of parameters is undefined. Use
	 *            {@link #lookupMethods(String, String, String[], boolean, boolean, boolean)} to include parameter
	 *            matching.
	 * @return
	 */
	public Method[] lookupMethods(String type, String name, boolean accessible, boolean staticAccess, boolean exact);

	/**
	 * Lookup member
	 * 
	 * @param type
	 * @param name
	 *            The name to match.
	 * @param paramTypes
	 *            The parameters to match. All methods which have compatible fields, ie. same or super type, are
	 *            matches.
	 * @param accessible
	 *            If true, return only accessible methods. If false, return all methods.
	 * @param staticAccess
	 *            If true then the type signature is a class. Else it represents an instance.
	 * @param exact
	 *            If true, then perform exact matches only. If false, then the match should ignore case and perform
	 *            camel case lookups. An implementation is available in {@link TypeUtil#looselyMatches(String, String)}
	 * @return
	 */
	public Method[] lookupMethods(String type, String name, String[] paramTypes, boolean accessible,
			boolean staticAccess, boolean exact);
}
