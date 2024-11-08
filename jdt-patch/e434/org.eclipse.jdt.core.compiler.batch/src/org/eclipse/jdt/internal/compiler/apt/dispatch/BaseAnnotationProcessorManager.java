/*******************************************************************************
 * Copyright (c) 2007, 2018 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.dispatch;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.internal.compiler.AbstractAnnotationProcessorManager;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * This class is the central dispatch point for Java 6 annotation processing.
 * This is created and configured by the JDT core; specifics depend on how
 * compilation is being performed, ie from the command line, via the Tool
 * interface, or within the IDE.  This class manages the discovery of annotation
 * processors and other information spanning multiple rounds of processing;
 * context that is valid only within a single round is managed by
 * {@link RoundDispatcher}.  There may be multiple instances of this class;
 * there is in general one of these for every Compiler that has annotation
 * processing enabled.  Within the IDE there will typically be one for every
 * Java project, because each project potentially has a separate processor path.
 *
 * TODO: do something useful with _supportedOptions and _supportedAnnotationTypes.
 */
public abstract class BaseAnnotationProcessorManager extends AbstractAnnotationProcessorManager
		implements IProcessorProvider
{

	protected PrintWriter _out;
	protected PrintWriter _err;
	protected BaseProcessingEnvImpl _processingEnv;
	public boolean _isFirstRound = true;

	/**
	 * The list of processors that have been loaded so far.  A processor on this
	 * list has been initialized, but may not yet have been called to process().
	 */
	protected List<ProcessorInfo> _processors = new ArrayList<>();

	// Tracing
	protected boolean _printProcessorInfo = false;
	protected boolean _printRounds = false;
	protected int _round;

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.AbstractAnnotationProcessorManager#configure(org.eclipse.jdt.internal.compiler.batch.Main, java.lang.String[])
	 */
	@Override
	public void configure(Object batchCompiler, String[] options) {
		// Implemented by BatchAnnotationProcessorManager.
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.AbstractAnnotationProcessorManager#configureFromPlatform(org.eclipse.jdt.internal.compiler.Compiler, java.lang.Object)
	 */
	@Override
	public void configureFromPlatform(Compiler compiler, Object compilationUnitLocator, Object javaProject, boolean isTestCode) {
		// Implemented by IdeAnnotationProcessorManager.
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ProcessorInfo> getDiscoveredProcessors() {
		return this._processors;
	}

	@Override
	public ICompilationUnit[] getDeletedUnits() {
		return this._processingEnv.getDeletedUnits();
	}

	@Override
	public ICompilationUnit[] getNewUnits() {
		return this._processingEnv.getNewUnits();
	}

	@Override
	public ReferenceBinding[] getNewClassFiles() {
		return this._processingEnv.getNewClassFiles();
	}

	@Override
	public void reset() {
		this._processingEnv.reset();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.AbstractAnnotationProcessorManager#setErr(java.io.PrintWriter)
	 */
	@Override
	public void setErr(PrintWriter err) {
		this._err = err;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.AbstractAnnotationProcessorManager#setOut(java.io.PrintWriter)
	 */
	@Override
	public void setOut(PrintWriter out) {
		this._out = out;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.AbstractAnnotationProcessorManager#setProcessors(java.lang.Object[])
	 */
	@Override
	public void setProcessors(Object[] processors) {
		// Only meaningful in batch mode.
		throw new UnsupportedOperationException();
	}

	/**
	 * A single "round" of processing, in the sense implied in
	 * {@link javax.annotation.processing.Processor}.
	 * <p>
	 * The Java 6 Processor spec contains ambiguities about how processors that support "*" are
	 * handled. Eclipse tries to match Sun's implementation in javac. What that actually does is
	 * analogous to inspecting the set of annotions found in the root units and adding an
	 * "imaginary" annotation if the set is empty. Processors are then called in order of discovery;
	 * for each processor, the intersection between the set of root annotations and the set of
	 * annotations the processor supports is calculated, and if it is non-empty, the processor is
	 * called. If the processor returns <code>true</code> then the intersection (including the
	 * imaginary annotation if one exists) is removed from the set of root annotations and the loop
	 * continues, until the set is empty. Of course, the imaginary annotation is not actually
	 * included in the set of annotations passed in to the processor. A processor's process() method
	 * is not called until its intersection set is non-empty, but thereafter it is called on every
	 * round. Note that even if a processor is not called in the first round, if it is called in
	 * subsequent rounds, it will be called in the order in which the processors were discovered,
	 * rather than being added to the end of the list.
	 */
	@Override
	public void processAnnotations(CompilationUnitDeclaration[] units, ReferenceBinding[] referenceBindings, boolean isLastRound) {
		if (units != null) {
			for (CompilationUnitDeclaration declaration : units) {
				if (declaration != null && declaration.scope != null) {
					ModuleBinding m = declaration.scope.module();
					if (m != null) {
						this._processingEnv._current_module = m;
						break;
					}
				}
			}
		}
		RoundEnvImpl roundEnv = new RoundEnvImpl(units, referenceBindings, isLastRound, this._processingEnv);
		PrintWriter out = this._out; // closable resource not manages in this class
		PrintWriter traceProcessorInfo = this._printProcessorInfo ? out : null;
		PrintWriter traceRounds = this._printRounds ? out : null;
		if (traceRounds != null) {
			traceRounds.println("Round " + ++this._round + ':'); //$NON-NLS-1$
		}
		RoundDispatcher dispatcher = new RoundDispatcher(
				this, roundEnv, roundEnv.getRootAnnotations(), traceProcessorInfo, traceRounds);
		dispatcher.round();
		if (this._isFirstRound) {
			this._isFirstRound = false;
		}
	}
}
