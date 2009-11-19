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
package org.codehaus.groovy.eclipse.core.types

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.types.impl.CompositeLookup;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
/**
 * Registry for IMemberLookup instances.
 * This class is also a factory for creating an instance for a particular set of contexts.
 *
 * @author empovazan
 */ 
class MemberLookupRegistry {
	static mapSourceCodeContextIdToPrototypes = [:]
	
	static {
		// Load the extensions. 
		try {
			def registry = Platform.getExtensionRegistry()
			def point = registry.getExtensionPoint('org.codehaus.groovy.eclipse.core.types')
			def extensions = point.extensions
			for (i in 0..<extensions.length) {
				def extension = extensions[i]
				def elements = extension.configurationElements
				for (j in 0..<elements.length) {
					def element = elements[j]
					def requiredContexts = element.getAttribute('requiredContexts')
					// Contexts may be a comma separated list.
					requiredContexts = requiredContexts.replaceAll(',', ' ')
					requiredContexts = requiredContexts.tokenize()
					requiredContexts = requiredContexts.collect { ContextUtil.convertToQualifiedId(it) }
					def name = element.getAttribute('name')
					def cls = element.getAttribute('class')
					try {
						def lookup = (IMemberLookup) element.createExecutableExtension('class')
						registerMemberLookup(lookup, requiredContexts as String[])
					} catch (CoreException e) {
						GroovyCore.logException("Error installing member lookup: " + name, e)
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace()
		}
	}
	
	/**
	 * Register a member lookup for the given set of source code context ids.
	 */
	static void registerMemberLookup(IMemberLookup lookup, String[] contextIds) {
		for (id in contextIds) {
			def list
			list = mapSourceCodeContextIdToPrototypes[id]
			if (list == null) { list = []; mapSourceCodeContextIdToPrototypes[id] = list }
			list << lookup
		}
	}
	
	/**
	 * Create a member lookup for the given source code context. Any semantic context bounds to a source code context
	 * will be used to created semantic dependent lookups.
	 * @param context
	 */
	static IMemberLookup createMemberLookup(ISourceCodeContext[] contexts) {
		def createdPrototypes = [] as Set
		def lookups = []
		
		for (context in contexts) {
			def prototypes = mapSourceCodeContextIdToPrototypes[context.id]
			prototypes?.each { prototype ->
				if (!createdPrototypes.contains(prototype)) {
					createdPrototypes << prototype
					lookups << prototype.class.newInstance()
				}
			}
		}
		
		return new CompositeLookup(lookups as IMemberLookup[])
	}
}