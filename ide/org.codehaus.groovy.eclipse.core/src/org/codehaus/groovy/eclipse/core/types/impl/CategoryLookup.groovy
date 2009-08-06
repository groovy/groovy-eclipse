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
package org.codehaus.groovy.eclipse.core.types.impl

import java.util.List;

import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.eclipse.core.IGroovyProjectAware;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContextAware;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.AbstractMemberLookup;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
/**
 * General category method lookup.
 */
class CategoryLookup extends AbstractMemberLookup implements ISourceCodeContextAware, IGroovyProjectAware {
	ISourceCodeContext context
	GroovyProjectFacade project 
	
	/**
	 * Ignore first parameter when doing content assist, but do not ignore when doing codeSelect
	 */
	boolean ignoreFirstParameter
	
	protected List collectAllFields(String type) { Collections.EMPTY_LIST }
	
	protected List collectAllProperties(String type) { Collections.EMPTY_LIST }
	
	protected List collectAllMethods(String type) {
		def results = []
		
		if (context == null) {
			return results
		}
		  
		def classNames = collectCategoryClassNames(context)
		def lookups = createLookupsForClassNames(classNames)
		lookups.each { lookup ->
			lookup.groovyProject = project
			results.addAll(lookup.collectAllMethods(type))
		}
		
		return results.unique()
	}
	
	private createLookupsForClassNames(classNames) {
		def lookups = classNames.collect { className ->
			def lookup = className
			def javaType = project.project.findType(className)
			if (javaType?.exists()) {
				lookup = new TypeCategoryLookup(javaType, ignoreFirstParameter)
			}
			lookup
		}

		// Remove those which were not converted to IMemberLookup instances.
		lookups = lookups.findAll { lookup -> !(lookup instanceof String) }
		return lookups
	}	
	
	private collectCategoryClassNames(context) {
		def classNames = []
		def path = context.astPath as List
		// Find all 'use' statements in this lookup.
		path.reverseEach { node ->
			if (node instanceof MethodCallExpression) {
				if (node.methodAsString?.equals('use')) {
					node.arguments.expressions.each { argument ->
						if (argument instanceof ClassExpression) {
							classNames << argument.type.name
						}
					}
				}
			}
		}
		// now add DefaultGroovyMethods since that is always available
		classNames << DefaultGroovyMethods.class.getName()
		return classNames.unique()
	}
	
	void setSourceCodeContext(ISourceCodeContext context) {
		this.context = context
	}

	void setGroovyProject(GroovyProjectFacade facade) {
		this.project = facade
	}
	
}