/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - Initial API and implementation
 *     Andy Clement     - Additional work
 *******************************************************************************/
package org.codehaus.jdt.groovy.integration.internal;

import java.util.Collections;

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.SourceElementNotifier;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObjectToInt;
import org.eclipse.jdt.internal.core.search.indexing.IndexingParser;

/**
 * @author Andrew Eisenberg
 * @created Aug 27, 2009
 * 
 */
public class MultiplexingIndexingParser extends IndexingParser {
	SourceElementNotifier notifier;
	boolean groovyReportReferenceInfo;
	ISourceElementRequestor requestor;

	public MultiplexingIndexingParser(ISourceElementRequestor requestor, IProblemFactory problemFactory, CompilerOptions options,
			boolean reportLocalDeclarations, boolean optimizeStringLiterals, boolean useSourceJavadocParser) {
		super(requestor, problemFactory, options, reportLocalDeclarations, optimizeStringLiterals, useSourceJavadocParser);
		this.notifier = (SourceElementNotifier) ReflectionUtils.getPrivateField(SourceElementParser.class, "notifier", this);
		this.groovyReportReferenceInfo = reportLocalDeclarations;
		this.requestor = requestor;
	}

	@Override
	public void setRequestor(ISourceElementRequestor requestor) {
		super.setRequestor(requestor);
		this.requestor = requestor;
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
			GroovyCompilationUnitDeclaration cud = (GroovyCompilationUnitDeclaration) new GroovyParser(this.options,
					problemReporter, false, true).dietParse(unit, compilationResult);

			// CompilationUnitDeclaration cud groovyParser.dietParse(sourceUnit, compilationResult);
			HashtableOfObjectToInt sourceEnds = createSourceEnds(cud);
			GroovyIndexingVisitor visitor = new GroovyIndexingVisitor(requestor);
			visitor.doVisit(cud.getModuleNode(), cud.currentPackage);

			notifier.notifySourceElementRequestor(cud, 0, unit.getContents().length, groovyReportReferenceInfo, sourceEnds,
			/* We don't care about the @category tag, so pass empty map */Collections.EMPTY_MAP);
			return cud;
		} else {
			return super.parseCompilationUnit(unit, fullParse, pm);
		}
	}

	// FIXASC this code is copied from MultiplexingSourceElementParser. Should combine
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

	// FIXASC this code is copied from MultiplexingSourceElementParser. Should combine
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
			}
		}
		if (tDecl.memberTypes != null) {
			for (TypeDeclaration innerTDecl : tDecl.memberTypes) {
				createSourceEndsForType(innerTDecl, table);
			}
		}
	}

}
