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

package org.codehaus.groovy.eclipse.editor.highlighting;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.eclipse.editor.GroovyTextTools;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.jface.text.rules.IRule;

/**
 * Manages the HighlightingExtenderRegistry
 * This class is a singleton and is managed by {@link GroovyTextTools}
 * @author Andrew Eisenberg
 * @created Oct 23, 2009
 */
public class HighlightingExtenderRegistry {
	
	public static final String EXTENSION_POINT = 'org.codehaus.groovy.eclipse.ui.syntaxHighlightingExtension'
	public static final String NATURE_ID = 'natureID'
	public static final String EXTENDER = 'extender'
	
	def natureToExtenderMap
	
	def initialize() {
		natureToExtenderMap = [:]
		IExtensionRegistry registry = Platform.getExtensionRegistry()
		IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT)
		extensionPoint.extensions.each {
			it.configurationElements.each {
				def natureid = it.getAttribute(NATURE_ID)
				Object object = WorkbenchPlugin.createExtension(it, EXTENDER)
				if (object instanceof IHighlightingExtender) {
//					natureToExtenderMap += [ "${natureid}" : object ]
					natureToExtenderMap.put natureid, object
				}
			}
		}
	}
	
	IHighlightingExtender getExtender(String natureID) {
		natureToExtenderMap."${natureID}"
	}
	
	List<String> getExtraGroovyKeywordsForProject(IProject project) {
		if (!project) {
			return
		}
		def extraGroovyKeywords = []
		project.description.natureIds.each {
		    def extender = getExtender(it)
			if (extender) {
				def keywords = extender.additionalGroovyKeywords
				if (keywords != null) {
				    extraGroovyKeywords.addAll(keywords)
				}
			}
		}
		extraGroovyKeywords
	}
	List<String> getExtraGJDKKeywordsForProject(IProject project) {
		if (!project) {
			return
		}
		def extraGJDKKeywords = []
		project.description.natureIds.each {
			def extender = getExtender(it)
			if (extender) {
				def keywords = extender.additionalGJDKKeywords
				if (keywords != null) {
				    extraGJDKKeywords.addAll(keywords)
				}
			}
		}
		extraGJDKKeywords
	}
	List<IRule> getAdditionalRulesForProject(IProject project) {
		if (!project || !project.isAccessible()) {
		    return
		}
		def extraRules = []
		project.description.natureIds.each {
			def extender = getExtender(it)
			if (extender) {
				def rules = extender.additionalRules
				if (rules != null) {    
				    extraRules.addAll(rules)
				}
			}
		}
		extraRules
	}
}
