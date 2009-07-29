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
package org.codehaus.groovy.eclipse.core.context

/**
 * Some little context utilities.
 *
 * @author empovazan
 */
class ContextUtil {
	static final mapSimpleIdToCoreContextId = [:]
	static final setOfCoreIds = [] as Set

	static {
		// Create a mapping from simple name core id's to full id's. Simple id's may be used in extensions.
		def coreIds = [
				ISourceCodeContext.CLASS, ISourceCodeContext.CLASS_SCOPE, ISourceCodeContext.CLOSURE_SCOPE, 
				ISourceCodeContext.CONSTRUCTOR_PARAMETERS, ISourceCodeContext.CONSTRUCTOR_SCOPE,
				ISourceCodeContext.METHOD_PARAMETERS, ISourceCodeContext.METHOD_SCOPE,
				ISourceCodeContext.MODULE, ISourceCodeContext.MODULE_SCOPE
			]
		coreIds.each { id ->
			mapSimpleIdToCoreContextId[id.tokenize('.')[-1]] = id
			setOfCoreIds << id
		}
	}
	
	/**
	 * Given a simple id that may be a GroovyEclipse specific id, convert it to the full qualified id.
	 * @return The qualified id or null if the simple id does not correspond to a core id.
	 */
	static String convertToQualifiedId(String simpleId) {
		return mapSimpleIdToCoreContextId[simpleId]
	}
	
	/**
	 * Given a simple id, expand it either into a GroovyEclipse specific id, or into an id with the given prefix.
	 * e.g. namespace = grails.types, ids = [domainClass, methodScope] 
	 * 		=> [grails.types.domainClass, org.codehaus.groovy.eclipse.core.context.methodScope]
	 * @param id The simple id - if it is qualified, the qualified id is simply returned.
	 * @param namespace Namespace which is a prefix to the id.
	 * @returns The expanded id, a core id, or the original id if it was already a qualified id. 
	 */
	static String expandId(String namespace, String id) {
		if (id.contains('.')) {
			return id
		}
		def expandedId = mapSimpleIdToCoreContextId[id]
		if (!expandedId) {
			expandedId = namespace + '.' + id
		}
		return expandedId
	}
}