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
package org.codehaus.groovy.eclipse.core.types.impl

import java.util.List;

import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.eclipse.core.IGroovyProjectAware;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContextAware;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.AbstractMemberLookup;
/**
 * General category method lookup.
 */
class CategoryLookup extends AbstractMemberLookup implements ISourceCodeContextAware, IGroovyProjectAware {
	ISourceCodeContext context
	GroovyProjectFacade project 
	
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
				lookup = new TypeCategoryLookup(javaType)
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
		return classNames.unique()
	}
	
	void setSourceCodeContext(ISourceCodeContext context) {
		this.context = context
	}

	void setGroovyProject(GroovyProjectFacade facade) {
		this.project = facade
	}
	
}