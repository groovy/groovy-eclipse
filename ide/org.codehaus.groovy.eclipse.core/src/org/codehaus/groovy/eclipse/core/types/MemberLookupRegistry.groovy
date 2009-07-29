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