/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *******************************************************************************/
package org.codehaus.jdt.groovy.integration.internal;

import java.util.Collections;

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.SourceElementNotifier;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObjectToInt;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;

/**
 * The multiplexing parser can delegate file parsing to multiple parsers. In this scenario it subtypes 'Parser' (which is the Java
 * parser) but is also aware of a groovy parser. Depending on what kind of file is to be parsed, it will invoke the relevant parser.
 * 
 * @author Andrew Eisenberg
 */
@SuppressWarnings("restriction")
public class MultiplexingSourceElementRequestorParser extends SourceElementParser {

	ISourceElementRequestor groovyRequestor;

	SourceElementNotifier notifier;

	boolean groovyReportReferenceInfo;

	private GroovyParser parser;

	public MultiplexingSourceElementRequestorParser(ProblemReporter problemReporter, ISourceElementRequestor requestor,
			IProblemFactory problemFactory, CompilerOptions options, boolean reportLocalDeclarations, boolean optimizeStringLiterals) {
		super(requestor, problemFactory, options, reportLocalDeclarations, optimizeStringLiterals);
		// The superclass that is extended is in charge of parsing .java files
		this.groovyRequestor = requestor;
		this.notifier = new SourceElementNotifier(requestor, reportLocalDeclarations);
		this.parser = new GroovyParser(requestor, this.options, problemReporter, false, true);
	}

	@Override
	public CompilationUnitDeclaration parseCompilationUnit(ICompilationUnit unit, boolean fullParse, IProgressMonitor pm) {

		if (ContentTypeUtils.isGroovyLikeFileName(unit.getFileName())) {
			// ASSUMPTIONS:
			// 1) there is no difference between a diet and full parse in the groovy works, so can ignore the fullParse parameter
			// 2) parsing is for the entire CU (ie- from character 0, to unit.getContents().length)
			// 3) nodesToCategories map is not necessary. I think it has something to do with JavaDoc, but not sure

			CompilationResult compilationResult = new CompilationResult(unit, 0, 0, this.options.maxProblemsPerUnit);

			// FIXASC Is it ok to use a new parser here everytime? If we don't we sometimes recurse back into the first one
			// FIXASC ought to reuse to ensure types end up in same groovy CU
			CompilationUnitDeclaration cud = new GroovyParser(this.parser.requestor, this.options, problemReporter, false, true)
					.dietParse(unit, compilationResult);

			// CompilationUnitDeclaration cud = parser.dietParse(unit, compilationResult);

			HashtableOfObjectToInt sourceEnds = createSourceEnds(cud);

			notifier.notifySourceElementRequestor(cud, 0, unit.getContents().length, groovyReportReferenceInfo, sourceEnds,
			/* We don't care about the @category tag, so pass empty map */Collections.EMPTY_MAP);
			return cud;
		} else {
			return super.parseCompilationUnit(unit, fullParse, pm);
		}
	}

	@Override
	public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult) {
		if (ContentTypeUtils.isGroovyLikeFileName(sourceUnit.getFileName())) {
			return parser.dietParse(sourceUnit, compilationResult);
		} else {
			return super.dietParse(sourceUnit, compilationResult);
		}
	}

	// FIXASC This should be calculated in GroovyCompilationUnitDeclaration
	private HashtableOfObjectToInt createSourceEnds(CompilationUnitDeclaration cDecl) {
		HashtableOfObjectToInt table = new HashtableOfObjectToInt();
		if (cDecl.types != null) {
			for (TypeDeclaration tDecl : cDecl.types) {
				createSourceEndsForType(tDecl, table);
			}
		}
		return table;
	}

	@Override
	public void reset() {
		parser.reset();
	}

	// FIXASC This should be calculated in GroovyCompilationUnitDeclaration
	private void createSourceEndsForType(TypeDeclaration tDecl, HashtableOfObjectToInt table) {
		table.put(tDecl, tDecl.sourceEnd);
		if (tDecl.fields != null) {
			for (FieldDeclaration fDecl : tDecl.fields) {
				table.put(fDecl, fDecl.sourceEnd);
			}
		}
		if (tDecl.methods != null) {
			for (AbstractMethodDeclaration mDecl : tDecl.methods) {
				table.put(mDecl, mDecl.sourceEnd);
				if (mDecl.statements != null && mDecl.statements.length > 0) {
					for (Statement expr : mDecl.statements) {
						if (expr instanceof QualifiedAllocationExpression) {
							// assume anon inner type
							createSourceEndsForType(((QualifiedAllocationExpression) expr).anonymousType, table);
						}
					}
				}
			}
		}
		if (tDecl.memberTypes != null) {
			for (TypeDeclaration innerTDecl : tDecl.memberTypes) {
				createSourceEndsForType(innerTDecl, table);
			}
		}
	}

}
