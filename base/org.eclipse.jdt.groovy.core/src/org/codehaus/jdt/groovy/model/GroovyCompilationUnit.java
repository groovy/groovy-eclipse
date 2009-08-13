/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.jdt.groovy.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.integration.internal.MultiplexingSourceElementRequestorParser;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObjectToInt;
import org.eclipse.jdt.internal.core.ASTHolderCUInfo;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.CompilationUnitElementInfo;
import org.eclipse.jdt.internal.core.CompilationUnitProblemFinder;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.OpenableElementInfo;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.ReconcileWorkingCopyOperation;
import org.eclipse.jdt.internal.core.JavaModelManager.PerWorkingCopyInfo;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.Region;
import org.eclipse.osgi.framework.adaptor.StatusException;
import org.eclipse.osgi.framework.internal.core.AbstractBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * @author Andrew Eisenberg
 * @created Jun 2, 2009
 * 
 */
@SuppressWarnings("restriction")
public class GroovyCompilationUnit extends CompilationUnit {

	private static ICodeSelectHelper selectHelper;

	// TODO This implicitly injects the CodeSelectHelper. Really, this should be
	// injected using an extension point.
	static {
		Bundle bundle = Platform.getBundle("org.codehaus.groovy.eclipse.codebrowsing"); //$NON-NLS-1$
		try {
			bundle.start(Bundle.START_TRANSIENT);
		} catch (Exception e) {
			// check to see if we really care
			// a bundle exception is thrown when org.ecliopse.jdt.groovy.core is started by
			// the org.codehaus.groovy.eclipse.codebrowsing bundle. This is because
			// of recursive starting
			boolean canIgnore = false;
			if (bundle instanceof AbstractBundle) {
				AbstractBundle aBundle = (AbstractBundle) bundle;
				if (e instanceof BundleException) {
					Throwable t = ((BundleException) e).getNestedException();
					if (t instanceof StatusException) {
						Object obj = ((StatusException) t).getStatus();
						if (aBundle.testStateChanging(obj)) {
							canIgnore = true;
						}
					}
				}
			} else {
				canIgnore = true;
			}
			if (!canIgnore) {
				Activator.getDefault().getLog().log(
						new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								"Error starting  org.codehaus.groovy.eclipse.codebrowsing bundle")); //$NON-NLS-1$
			}
		}
	}

	private class GroovyErrorHandlingPolicy implements IErrorHandlingPolicy {

		final boolean stopOnFirst;

		public GroovyErrorHandlingPolicy(boolean stopOnFirst) {
			this.stopOnFirst = stopOnFirst;
		}

		public boolean proceedOnErrors() {
			return !stopOnFirst;
		}

		public boolean stopOnFirstError() {
			return stopOnFirst;
		}

	}

	private class GroovyResolveRequestor implements ICompilerRequestor {
		private List<CompilationResult> results = new LinkedList<CompilationResult>();

		public void acceptResult(CompilationResult result) {
			results.add(result);
		}

		public List<CompilationResult> getResults() {
			return results;
		}

	}

	public GroovyCompilationUnit(PackageFragment parent, String name, WorkingCopyOwner owner) {
		super(parent, name, owner);
	}

	/**
	 * Returns the module node for this GroovyCompilationUnit creates one if one doesn't exist.
	 * 
	 * This is potentially a long running operation. This method ensures that this CompilationUnit is a working copy and that it is
	 * consistent (if not a reconcile operation is performed).
	 * 
	 * Probably should raise an exception if ModuleNode cannot be created
	 */
	public ModuleNode getModuleNode() {
		try {
			if (!isWorkingCopy()) {
				becomeWorkingCopy(null);
			}
			if (!isConsistent()) {
				reconcile(true, null);
			}
			PerWorkingCopyInfo info = getPerWorkingCopyInfo();
			if (info != null) {
				return ModuleNodeMapper.getInstance().get(info);
			}
		} catch (JavaModelException e) {
			// TODO error reporting?
			Activator.getDefault().getLog().log(e.getStatus());
		}
		// TODO I don't like returning null. Should we rethrow exception or
		// should we return dummy moduleNode?
		return null;
	}

	@Override
	public void discardWorkingCopy() throws JavaModelException {
		PerWorkingCopyInfo info = getPerWorkingCopyInfo();
		if (workingCopyInfoWillBeDiscarded(info)) {
			ModuleNodeMapper.getInstance().remove(info);
		}
		super.discardWorkingCopy();
	}

	/**
	 * working copy info is about to be discared if
	 */
	private boolean workingCopyInfoWillBeDiscarded(PerWorkingCopyInfo info) {
		return info != null
				&& ((Integer) ReflectionUtils.getPrivateField(PerWorkingCopyInfo.class, "useCount", info)).intValue() <= 1; //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean buildStructure(OpenableElementInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource)
			throws JavaModelException {

		CompilationUnitElementInfo unitInfo = (CompilationUnitElementInfo) info;

		// ensure buffer is opened
		IBuffer buffer = getBufferManager().getBuffer(this);
		if (buffer == null) {
			openBuffer(pm, unitInfo); // open buffer independently from the
			// info, since we are building the info
		}

		// generate structure and compute syntax problems if needed
		GroovyCompilationUnitStructureRequestor requestor = new GroovyCompilationUnitStructureRequestor(this, unitInfo, newElements);
		JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo = getPerWorkingCopyInfo();
		JavaProject project = (JavaProject) getJavaProject();

		// determine what kind of buildStructure we are doing
		boolean createAST;
		int reconcileFlags;
		boolean resolveBindings;
		HashMap problems;
		if (info instanceof ASTHolderCUInfo) {
			ASTHolderCUInfo astHolder = (ASTHolderCUInfo) info;
			createAST = ((Integer) ReflectionUtils.getPrivateField(ASTHolderCUInfo.class, "astLevel", astHolder)).intValue() != NO_AST; //$NON-NLS-1$
			resolveBindings = ((Boolean) ReflectionUtils.getPrivateField(ASTHolderCUInfo.class, "resolveBindings", astHolder))
					.booleanValue();
			reconcileFlags = ((Integer) ReflectionUtils.getPrivateField(ASTHolderCUInfo.class, "reconcileFlags", astHolder)) //$NON-NLS-1$
					.intValue();
			problems = (HashMap) ReflectionUtils.getPrivateField(ASTHolderCUInfo.class, "problems", astHolder);
		} else {
			createAST = false;
			resolveBindings = false;
			reconcileFlags = 0;
			problems = null;
		}

		boolean computeProblems = perWorkingCopyInfo != null && perWorkingCopyInfo.isActive() && project != null
				&& JavaProject.hasJavaNature(project.getProject());
		IProblemFactory problemFactory = new DefaultProblemFactory();

		// compiler options
		Map<String, String> options = (project == null ? JavaCore.getOptions() : project.getOptions(true));
		if (!computeProblems) {
			// disable task tags checking to speed up parsing
			options.put(JavaCore.COMPILER_TASK_TAGS, ""); //$NON-NLS-1$
		}

		// Required for Groovy, but not for Java
		options.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.ENABLED);

		CompilerOptions compilerOptions = new CompilerOptions(options);

		// Required for Groovy, but not for Java
		ProblemReporter reporter = new ProblemReporter(new GroovyErrorHandlingPolicy(!computeProblems), compilerOptions,
				new DefaultProblemFactory());

		SourceElementParser parser = new MultiplexingSourceElementRequestorParser(reporter, requestor, /*
																										 * not needed if computing
																										 * groovy only
																										 */
		problemFactory, compilerOptions, true/* report local declarations */, !createAST /*
																						 * optimize string literals only if not
																						 * creating a DOM AST
																						 */);
		parser.reportOnlyOneSyntaxError = !computeProblems;
		// FIXADE (M2) likely not needed for groovy
		parser.setMethodsFullRecovery(true);
		parser.setStatementsRecovery((reconcileFlags & ICompilationUnit.ENABLE_STATEMENTS_RECOVERY) != 0);

		if (!computeProblems && !resolveBindings && !createAST) // disable javadoc parsing if not computing problems, not resolving
			// and not creating ast
			parser.javadocParser.checkDocComment = false;
		requestor.setParser(parser);

		// update timestamp (might be IResource.NULL_STAMP if original does not
		// exist)
		if (underlyingResource == null) {
			underlyingResource = getResource();
		}
		// underlying resource is null in the case of a working copy on a class
		// file in a jar
		if (underlyingResource != null) {
			ReflectionUtils.setPrivateField(CompilationUnitElementInfo.class, "timestamp", unitInfo, ((IFile) underlyingResource) //$NON-NLS-1$
					.getModificationStamp());
		}

		GroovyCompilationUnitDeclaration compilationUnitDeclaration = null;
		CompilationUnit source = cloneCachingContents();
		try {
			// GROOVY
			// note that this is a slightly different approach than taken by super.buildStructure
			// in super.buildStructure, there is a test here to see if computeProblems is true.
			// if false, then parser.parserCompilationUnit is called.
			// this will not work for Groovy because we need to ensure bindings are resolved
			// for many operations (content assist and code select) to work.
			// So, for groovy, always use CompilationUnitProblemFinder.process and then process problems
			// separately only if necessary
			if (problems == null) {
				// report problems to the problem requestor
				problems = new HashMap();
				compilationUnitDeclaration = (GroovyCompilationUnitDeclaration) CompilationUnitProblemFinder.process(source,
						parser, this.owner, problems, createAST, reconcileFlags, pm);
				if (computeProblems) {
					try {
						perWorkingCopyInfo.beginReporting();
						for (Iterator iteraror = problems.values().iterator(); iteraror.hasNext();) {
							CategorizedProblem[] categorizedProblems = (CategorizedProblem[]) iteraror.next();
							if (categorizedProblems == null)
								continue;
							for (int i = 0, length = categorizedProblems.length; i < length; i++) {
								perWorkingCopyInfo.acceptProblem(categorizedProblems[i]);
							}
						}
					} finally {
						perWorkingCopyInfo.endReporting();
					}
				}
			} else {
				// collect problems
				compilationUnitDeclaration = (GroovyCompilationUnitDeclaration) CompilationUnitProblemFinder.process(source,
						parser, this.owner, problems, createAST, reconcileFlags, pm);
			}

			// GROOVY
			// if this is a working copy, then we have more work to do
			if (perWorkingCopyInfo != null && compilationUnitDeclaration != null) {
				ModuleNode module = compilationUnitDeclaration.getModuleNode();
				// finish off with the ModuleNode and complete its bindings

				// Store it for later
				if (module != null) {
					ModuleNodeMapper.getInstance().store(perWorkingCopyInfo, module);
				}
			}

			// create the DOM AST from the compiler AST
			if (createAST) {
				org.eclipse.jdt.core.dom.CompilationUnit ast;
				try {
					ast = AST.convertCompilationUnit(AST.JLS3, compilationUnitDeclaration, options, computeProblems, source,
							reconcileFlags, pm);
					ReflectionUtils.setPrivateField(ASTHolderCUInfo.class, "ast", info, ast); //$NON-NLS-1$
				} catch (OperationCanceledException e) {
					// catch this exception so as to not enter the catch(RuntimeException e) below
					// might need to do the same for AbortCompilation
					throw e;
				} catch (IllegalArgumentException e) {
					// if necessary, we can do some better reporting here.
					Util.log(e, "Problem with build structure: Offset for AST node is incorrect in " //$NON-NLS-1$
							+ this.getParent().getElementName() + "." + getElementName()); //$NON-NLS-1$
				} catch (Exception e) {
					Util.log(e, "Problem with build structure for " + this.getElementName()); //$NON-NLS-1$
				}
			}
		} catch (OperationCanceledException e) {
			// catch this exception so as to not enter the catch(RuntimeException e) below
			// might need to do the same for AbortCompilation
			throw e;
		} catch (Exception e) {
			// GROOVY: The groovy compiler does not handle broken code well in many situations
			// use this general catch clause so that exceptions thrown by broken code
			// do not bubble up the stack.
			Util.log(e, "Problem with build structure for " + this.getElementName()); //$NON-NLS-1$
		} finally {
			if (compilationUnitDeclaration != null) {
				compilationUnitDeclaration.cleanUp();
			}
		}
		return unitInfo.isStructureKnown();
	}

	/*
	 * Copied from super class, but changed so that a custom ReconcileWorkingCopyOperation can be run
	 */
	@Override
	public org.eclipse.jdt.core.dom.CompilationUnit reconcile(int astLevel, int reconcileFlags, WorkingCopyOwner workingCopyOwner,
			IProgressMonitor monitor) throws JavaModelException {
		if (!isWorkingCopy())
			return null; // Reconciling is not supported on non working copies
		if (workingCopyOwner == null)
			workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;

		PerformanceStats stats = null;
		if (ReconcileWorkingCopyOperation.PERF) {
			stats = PerformanceStats.getStats(JavaModelManager.RECONCILE_PERF, this);
			stats.startRun(new String(this.getFileName()));
		}
		ReconcileWorkingCopyOperation op = new GroovyReconcileWorkingCopyOperation(this, astLevel, reconcileFlags, workingCopyOwner);
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		try {
			manager.cacheZipFiles(); // cache zip files for performance (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=134172)
			op.runOperation(monitor);
		} finally {
			manager.flushZipFiles();
		}
		if (ReconcileWorkingCopyOperation.PERF) {
			stats.endRun();
		}
		return op.ast;
	}

	// TODO This should be calculated in GroovyCompilationUnitDeclaration
	private HashtableOfObjectToInt createSourceEnds(CompilationUnitDeclaration cDecl) {
		HashtableOfObjectToInt table = new HashtableOfObjectToInt();
		if (cDecl.types != null) {
			for (TypeDeclaration tDecl : cDecl.types) {
				createSourceEndsForType(tDecl, table);
			}
		}
		return table;
	}

	// TODO This should be calculated in GroovyCompilationUnitDeclaration
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

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == GroovyCompilationUnit.class) {
			return this;
		}
		if (adapter == ModuleNode.class) {
			return getModuleNode();
		}
		return super.getAdapter(adapter);
	}

	/*
	 * Clone this handle so that it caches its contents in memory. DO NOT PASS TO CLIENTS
	 */
	@Override
	public GroovyCompilationUnit cloneCachingContents() {
		return new GroovyCompilationUnit((PackageFragment) this.parent, this.name, this.owner) {
			private char[] cachedContents;

			@Override
			public char[] getContents() {
				if (this.cachedContents == null)
					this.cachedContents = GroovyCompilationUnit.this.getContents();
				return this.cachedContents;
			}

			@Override
			public CompilationUnit originalFromClone() {
				return GroovyCompilationUnit.this;
			}
		};
	}

	@Override
	public IJavaElement[] codeSelect(int offset, int length) throws JavaModelException {
		return codeSelect(offset, length, DefaultWorkingCopyOwner.PRIMARY);
	}

	@Override
	public IJavaElement[] codeSelect(int offset, int length, WorkingCopyOwner workingCopyOwner) throws JavaModelException {
		return codeSelect(this, offset, length, workingCopyOwner);
	}

	@Override
	protected IJavaElement[] codeSelect(org.eclipse.jdt.internal.compiler.env.ICompilationUnit cu, int offset, int length,
			WorkingCopyOwner o) throws JavaModelException {
		IJavaElement[] elts = super.codeSelect(cu, offset, length, o);

		// filter out ones we know are wrong
		Set<IJavaElement> realElts = new HashSet<IJavaElement>();
		for (IJavaElement elt : elts) {
			if (elt.getElementType() == IJavaElement.TYPE) {
				// filter out classes x, y, z, and all lower case single letter
				// classes
				if (elt.getElementName().length() == 1 && Character.isLowerCase(elt.getElementName().charAt(0))) {
					continue;
				}
			}
			realElts.add(elt);
		}
		if (selectHelper != null) {
			elts = selectHelper.select(this, new Region(offset, length));
			if (elts != null) {
				for (IJavaElement elt : elts) {
					realElts.add(elt);
				}
			}
		}

		return realElts.toArray(new IJavaElement[realElts.size()]);
	}

	/**
	 * There is no such thing as a primary type in Groovy. First look for a type of the same name as the CU, Else get the first type
	 * in getAllTypes()
	 */
	@Override
	public IType findPrimaryType() {
		IType type = super.findPrimaryType();
		if (type != null) {
			return type;
		}
		try {
			IType[] types = getTypes();
			if (types != null && types.length > 0) {
				return types[0];
			}
		} catch (JavaModelException e) {
			Util.log(e, "Error finding all types of " + this.getElementName());
		}
		return null;
	}

	public static void setSelectHelper(ICodeSelectHelper newSelectHelper) {
		selectHelper = newSelectHelper;
	}

}
