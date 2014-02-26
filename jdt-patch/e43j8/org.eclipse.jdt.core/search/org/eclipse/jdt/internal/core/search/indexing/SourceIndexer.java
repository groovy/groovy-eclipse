/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;
import org.eclipse.jdt.internal.core.jdom.CompilationUnit;
import org.eclipse.jdt.internal.core.search.matching.JavaSearchNameEnvironment;
import org.eclipse.jdt.internal.core.search.matching.MethodPattern;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

/**
 * A SourceIndexer indexes java files using a java parser. The following items are indexed:
 * Declarations of:<br>
 * - Classes<br>
 * - Interfaces;<br>
 * - Methods;<br>
 * - Fields;<br>
 * - Lambda expressions;<br>
 * References to:<br>
 * - Methods (with number of arguments); <br>
 * - Fields;<br>
 * - Types;<br>
 * - Constructors.
 */
public class SourceIndexer extends AbstractIndexer implements ITypeRequestor, SuffixConstants {

	private LookupEnvironment lookupEnvironment;
	private CompilerOptions options;
	public ISourceElementRequestor requestor;
	private Parser basicParser;
	private CompilationUnit compilationUnit;
	private CompilationUnitDeclaration cud;
	private static final boolean DEBUG = false;
	
	public SourceIndexer(SearchDocument document) {
		super(document);
		this.requestor = new SourceIndexerRequestor(this);
	}
	public void indexDocument() {
		// Create a new Parser
		String documentPath = this.document.getPath();
		SourceElementParser parser = this.document.getParser();
		if (parser == null) {
			IPath path = new Path(documentPath);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));
			parser = JavaModelManager.getJavaModelManager().indexManager.getSourceElementParser(JavaCore.create(project), this.requestor);
		} else {
			parser.setRequestor(this.requestor);
		}

		// Launch the parser
		char[] source = null;
		char[] name = null;
		try {
			source = this.document.getCharContents();
			name = documentPath.toCharArray();
		} catch(Exception e){
			// ignore
		}
		if (source == null || name == null) return; // could not retrieve document info (e.g. resource was discarded)
		this.compilationUnit = new CompilationUnit(source, name);
		try {
			if (parser.parseCompilationUnit(this.compilationUnit, true, null).hasFunctionalTypes())
				this.document.requireIndexingResolvedDocument();
		} catch (Exception e) {
			if (JobManager.VERBOSE) {
				e.printStackTrace();
			}
		}
	}
	
	public void accept(IBinaryType binaryType, PackageBinding packageBinding, AccessRestriction accessRestriction) {
		this.lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding, accessRestriction);
	}

	public void accept(ICompilationUnit unit, AccessRestriction accessRestriction) {
		CompilationResult unitResult = new CompilationResult(unit, 1, 1, this.options.maxProblemsPerUnit);
		CompilationUnitDeclaration parsedUnit = this.basicParser.dietParse(unit, unitResult);
		this.lookupEnvironment.buildTypeBindings(parsedUnit, accessRestriction);
		this.lookupEnvironment.completeTypeBindings(parsedUnit, true);
	}

	public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding, AccessRestriction accessRestriction) {
		ISourceType sourceType = sourceTypes[0];
		while (sourceType.getEnclosingType() != null)
			sourceType = sourceType.getEnclosingType();
		SourceTypeElementInfo elementInfo = (SourceTypeElementInfo) sourceType;
		IType type = elementInfo.getHandle();
		ICompilationUnit sourceUnit = (ICompilationUnit) type.getCompilationUnit();
		accept(sourceUnit, accessRestriction);		
	}
	
	public void resolveDocument() {
		try {
			IPath path = new Path(this.document.getPath());
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));
			JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
			JavaProject javaProject = (JavaProject) model.getJavaProject(project);

			this.options = new CompilerOptions(javaProject.getOptions(true));
			ProblemReporter problemReporter =
					new ProblemReporter(
							DefaultErrorHandlingPolicies.proceedWithAllProblems(),
							this.options,
							new DefaultProblemFactory());

			// Re-parse using normal parser, IndexingParser swallows several nodes, see comment above class.
			this.basicParser = new Parser(problemReporter, false);
			this.basicParser.reportOnlyOneSyntaxError = true;
			this.cud = this.basicParser.parse(this.compilationUnit, new CompilationResult(this.compilationUnit, 0, 0, this.options.maxProblemsPerUnit));

			// Use a non model name environment to avoid locks, monitors and such.
			INameEnvironment nameEnvironment = new JavaSearchNameEnvironment(javaProject, JavaModelManager.getJavaModelManager().getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, true/*add primary WCs*/));
			this.lookupEnvironment = new LookupEnvironment(this, this.options, problemReporter, nameEnvironment);

			this.lookupEnvironment.buildTypeBindings(this.cud, null);
			this.lookupEnvironment.completeTypeBindings();
			this.cud.scope.faultInTypes();
			this.cud.resolve();
		} catch (Exception e) {
			if (JobManager.VERBOSE) {
				e.printStackTrace();
			}
		}
	}

	public void indexResolvedDocument() {
		try {
			if (DEBUG) {
				System.out.println(new String(this.cud.compilationResult.fileName) + ':');
			}
			final ASTVisitor visitor = new ASTVisitor() {
				public boolean visit(LambdaExpression lambdaExpression, BlockScope blockScope) {
					if (lambdaExpression.binding != null && lambdaExpression.binding.isValidBinding()) {
						if (DEBUG) {
							System.out.println('\t' + new String(lambdaExpression.descriptor.declaringClass.sourceName()) + '.' + 
									new String(lambdaExpression.descriptor.selector) + "-> {}"); //$NON-NLS-1$
						}
						SourceIndexer.this.addIndexEntry(IIndexConstants.METHOD_DECL, MethodPattern.createIndexKey(lambdaExpression.descriptor.selector, lambdaExpression.descriptor.parameters.length));
					} else {
						if (DEBUG) {
							System.out.println("\tnull/bad binding in lambda"); //$NON-NLS-1$
						}
					}
					return true;
				}
				public boolean visit(ReferenceExpression referenceExpression, BlockScope blockScope) {
					if (referenceExpression.isArrayConstructorReference())
						return true;
					MethodBinding binding = referenceExpression.getMethodBinding();
					if (binding != null && binding.isValidBinding()) {
						if (DEBUG) {
							System.out.println('\t' + new String(referenceExpression.descriptor.declaringClass.sourceName()) + "::"  //$NON-NLS-1$
									+ new String(referenceExpression.descriptor.selector) + " == " + new String(binding.declaringClass.sourceName()) + '.' + //$NON-NLS-1$
									new String(binding.selector));
						}
						if (referenceExpression.isMethodReference())
							SourceIndexer.this.addMethodReference(binding.selector, binding.parameters.length);
						else
							SourceIndexer.this.addConstructorReference(binding.declaringClass.sourceName(), binding.parameters.length);
					} else {
						if (DEBUG) {
							System.out.println("\tnull/bad binding in reference expression"); //$NON-NLS-1$
						}
					}
					return true;
				}
			};
			this.cud.traverse(visitor , this.cud.scope, false);
		} catch (Exception e) {
			if (JobManager.VERBOSE) {
				e.printStackTrace();
			}
		}
	}
}
