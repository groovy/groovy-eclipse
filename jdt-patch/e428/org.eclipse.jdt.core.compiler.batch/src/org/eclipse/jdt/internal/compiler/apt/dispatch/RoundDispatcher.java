/*******************************************************************************
 * Copyright (c) 2006, 2015 BEA Systems, Inc.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Manages context during a single round of annotation processing.
 */
public class RoundDispatcher {

	private final Set<TypeElement> _unclaimedAnnotations;
	private final RoundEnvironment _roundEnv;
	private final IProcessorProvider _provider;
	private boolean _searchForStar = false;
	private final PrintWriter _traceProcessorInfo;
	private final PrintWriter _traceRounds;

	/**
	 * Processors discovered so far.  This list may grow during the
	 * course of a round, as additional processors are discovered.
	 */
	private final List<ProcessorInfo> _processors;

	/**
	 * @param rootAnnotations a possibly empty but non-null set of annotations on the
	 * root compilation units of this round.  A local copy of the set will be made, to
	 * avoid modifying the set passed in.
	 * @param traceProcessorInfo a PrintWriter that processor trace output will be sent
	 * to, or null if tracing is not desired.
	 * @param traceRounds
	 */
	public RoundDispatcher(
			IProcessorProvider provider,
			RoundEnvironment env,
			Set<TypeElement> rootAnnotations,
			PrintWriter traceProcessorInfo,
			PrintWriter traceRounds)
	{
		this._provider = provider;
		this._processors = provider.getDiscoveredProcessors();
		this._roundEnv = env;
		this._unclaimedAnnotations = new HashSet<>(rootAnnotations);
		this._traceProcessorInfo = traceProcessorInfo;
		this._traceRounds = traceRounds;
	}

	/**
	 * Handle a complete round, dispatching to all appropriate processors.
	 */
	public void round()
	{
		if (null != this._traceRounds) {
			StringBuilder sbElements = new StringBuilder();
			sbElements.append("\tinput files: {"); //$NON-NLS-1$
			Iterator<? extends Element> iElements = this._roundEnv.getRootElements().iterator();
			boolean hasNext = iElements.hasNext();
			while (hasNext) {
				sbElements.append(iElements.next());
				hasNext = iElements.hasNext();
				if (hasNext) {
					sbElements.append(',');
				}
			}
			sbElements.append('}');
			this._traceRounds.println(sbElements.toString());

			StringBuilder sbAnnots = new StringBuilder();
			sbAnnots.append("\tannotations: ["); //$NON-NLS-1$
			Iterator<TypeElement> iAnnots = this._unclaimedAnnotations.iterator();
			hasNext = iAnnots.hasNext();
			while (hasNext) {
				sbAnnots.append(iAnnots.next());
				hasNext = iAnnots.hasNext();
				if (hasNext) {
					sbAnnots.append(',');
				}
			}
			sbAnnots.append(']');
			this._traceRounds.println(sbAnnots.toString());

			this._traceRounds.println("\tlast round: " + this._roundEnv.processingOver()); //$NON-NLS-1$
		}

		// If there are no root annotations, try to find a processor that claims "*"
		this._searchForStar = this._unclaimedAnnotations.isEmpty();

		// Iterate over all the already-found processors, giving each one a chance at the unclaimed
		// annotations. If a processor is called at all, it is called on every subsequent round
		// including the final round, but it may be called with an empty set of annotations.
		for (ProcessorInfo pi : this._processors) {
			handleProcessor(pi);
		}

		// If there are any unclaimed annotations, or if there were no root annotations and
		// we have not yet run into a processor that claimed "*", continue discovery.
		while (this._searchForStar || !this._unclaimedAnnotations.isEmpty()) {
			ProcessorInfo pi = this._provider.discoverNextProcessor();
			if (null == pi) {
				// There are no more processors to be discovered.
				break;
			}
			handleProcessor(pi);
		}

		// TODO: If !unclaimedAnnos.isEmpty(), issue a warning.
	}

	/**
	 * Evaluate a single processor.  Depending on the unclaimed annotations,
	 * the annotations this processor supports, and whether it has already been
	 * called in a previous round, possibly call its process() method.
	 */
	private void handleProcessor(ProcessorInfo pi)
	{
		try {
			Set<TypeElement> annotationsToProcess = new HashSet<>();
			boolean shouldCall = pi.computeSupportedAnnotations(
					this._unclaimedAnnotations, annotationsToProcess);
			if (shouldCall) {
				boolean claimed = pi._processor.process(annotationsToProcess, this._roundEnv);
				if (null != this._traceProcessorInfo && !this._roundEnv.processingOver()) {
					StringBuilder sb = new StringBuilder();
					sb.append("Processor "); //$NON-NLS-1$
					sb.append(pi._processor.getClass().getName());
					sb.append(" matches ["); //$NON-NLS-1$
					Iterator<TypeElement> i = annotationsToProcess.iterator();
					boolean hasNext = i.hasNext();
					while (hasNext) {
						sb.append(i.next());
						hasNext = i.hasNext();
						if (hasNext) {
							sb.append(' ');
						}
					}
					sb.append("] and returns "); //$NON-NLS-1$
					sb.append(claimed);
					this._traceProcessorInfo.println(sb.toString());
				}
				if (claimed) {
					// The processor claimed its annotations.
					this._unclaimedAnnotations.removeAll(annotationsToProcess);
					if (pi.supportsStar()) {
						this._searchForStar = false;
					}
				}
			}
		} catch (Throwable e) {
			// If a processor throws an exception (as opposed to reporting an error),
			// report it and abort compilation by throwing AbortCompilation.
			this._provider.reportProcessorException(pi._processor, new Exception(e));
		}
	}

}
