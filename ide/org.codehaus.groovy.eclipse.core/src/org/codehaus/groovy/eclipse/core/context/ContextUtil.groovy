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