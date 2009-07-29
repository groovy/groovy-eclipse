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
package org.codehaus.groovy.eclipse.core.types;

import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.impl.SourceCodeContextFactory;
import org.codehaus.groovy.eclipse.core.types.internal.InferringEvaluationContext;
import org.codehaus.groovy.eclipse.core.types.internal.TypedEvaluationContext;
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
	
	/**
	 * Override the line/column in the current context. The new location must still be within the current context!
	 * @param region
	 * @return
	 */
	public TypeEvaluationContextBuilder location(IRegion region) {
		this.region = region;
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

		if (sourceCodeContext == null) {
			TypedEvaluationContext context = new TypedEvaluationContext();
			context.setClassLoader(classLoader);
			context.setImports(imports);
			context.setSymbolTable(symbolTable);
			context.setMemberLookup(memberLookup);
			return context;
		} else {
			sourceCodeContext = new SourceCodeContextFactory().createContext(sourceCodeContext, region);
			InferringEvaluationContext context = new InferringEvaluationContext();
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
