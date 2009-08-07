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

import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.impl.SourceCodeContextFactory;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.internal.InferringEvaluationContext;
import org.codehaus.groovy.eclipse.core.types.internal.TypedEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * A builder for type evaluation contexts.
 * 
 * @author empovazan
 */
public class TypeEvaluationContextBuilder {
	private static final String[] NO_IMPORTS = new String[0];
	private ClassLoader classLoader;
	private String[] imports = NO_IMPORTS;
	private ISourceCodeContext sourceCodeContext;
	private ISymbolTable symbolTable;
	private IMemberLookup memberLookup;
	private IRegion region = new Region(0, 0); // default if left unspecified
	private GroovyProjectFacade project;
	
	
	/**
	 * Override the line/column in the current context. The new location must still be within the current context!
	 * @param region
	 * @return
	 */
	public TypeEvaluationContextBuilder location(IRegion region) {
		this.region = region;
		return this;
	}

	public TypeEvaluationContextBuilder project(GroovyProjectFacade project) {
        this.project = project;
        return this;
    }

	
	
	public TypeEvaluationContextBuilder classLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
		return this;
	}
	
	public TypeEvaluationContextBuilder imports(String[] imports) {
		this.imports = imports;
		return this;
	}
	
	public TypeEvaluationContextBuilder sourceCodeContext(ISourceCodeContext sourceCodeContext) {
		this.sourceCodeContext = sourceCodeContext;
		return this;
	}
	
	public TypeEvaluationContextBuilder symbolTable(ISymbolTable symbolTable) {
		this.symbolTable = symbolTable;
		return this;
	}
	
	public TypeEvaluationContextBuilder memberLookup(IMemberLookup memberLookup) {
		this.memberLookup = memberLookup;
		return this;
	}
	
	public ITypeEvaluationContext done() {
		checkNonNull("ISymbolTable", symbolTable);
		checkNonNull("IMemberLookup", memberLookup);
		
		Assert.isNotNull(project, "Project must not be null");

		if (sourceCodeContext == null) {
			TypedEvaluationContext context = new TypedEvaluationContext(project);
			context.setClassLoader(classLoader);
			context.setImports(imports);
			context.setSymbolTable(symbolTable);
			context.setMemberLookup(memberLookup);
			return context;
		} else {
			sourceCodeContext = new SourceCodeContextFactory().createContext(sourceCodeContext, region);
			InferringEvaluationContext context = new InferringEvaluationContext(project);
			context.setClassLoader(classLoader);
			context.setImports(imports);
			context.setSourceCodeContext(sourceCodeContext);
			context.setSymbolTable(symbolTable);
			context.setMemberLookup(memberLookup);
			return context;
		}
	}

	private void checkNonNull(String message, Object object) {
		if (object == null) {
			throw new IllegalStateException("Object cannot be null: " + message);
		}
	}

	/**
	 * Build by example.
	 * @param evalContext
	 * @return
	 */
	public TypeEvaluationContextBuilder typeEvaluationContext(ITypeEvaluationContext evalContext) {
		TypedEvaluationContext typedContext = (TypedEvaluationContext) evalContext;
		if (evalContext instanceof InferringEvaluationContext) {
			InferringEvaluationContext inferringContext = (InferringEvaluationContext) evalContext;
			sourceCodeContext(inferringContext.getSourceCodeContext());
		}
		
		classLoader = typedContext.getClassLoader();
		imports = typedContext.getImports();
		symbolTable = typedContext.getSymbolTable();
		memberLookup = typedContext.getMemberLookup();
		
		return this;
	}
}
