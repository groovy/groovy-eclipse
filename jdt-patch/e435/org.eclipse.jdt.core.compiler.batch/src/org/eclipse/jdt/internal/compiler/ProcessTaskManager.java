/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.util.Messages;

public class ProcessTaskManager implements AutoCloseable {

	private final Compiler compiler;
	private final int startingIndex;
	private volatile boolean processing;
	private final Future<?> processingTask; // synchronized write, volatile read
	/** synchronized access **/
	private CompilationUnitDeclaration unitWithError;

	/** contains CompilationUnitDeclaration or something else as stop signal **/
	private final BlockingQueue<Object> units;

	private static final int PROCESSED_QUEUE_SIZE = 100;
	private static final Object STOP_SIGNAL = new Object();

	/** Normally a single thread is created an reused on subsequent builds **/
	private static final ExecutorService executor = Executors.newCachedThreadPool(r -> {
		Thread t = new Thread(r, "Compiler Processing Task"); //$NON-NLS-1$
		t.setDaemon(true);
		return t;
	});

	public ProcessTaskManager(Compiler compiler, int startingIndex) {
		this.compiler = compiler;
		this.startingIndex = startingIndex;

		this.units = new ArrayBlockingQueue<>(PROCESSED_QUEUE_SIZE);
		this.processing = true;
		this.processingTask = executor.submit(this::processing);
	}

// add unit to the queue - wait if no space is available
	private void addNextUnit(Object newElement) {
		try {
			this.units.put(newElement);
		} catch (InterruptedException interrupt) {
			throw new RuntimeException(interrupt);
		}
	}

	private Object lastSignal;

	/** blocks until at least one CompilationUnitDeclaration can be returned or empty when no more elements can be expected **/
	public Collection<CompilationUnitDeclaration> removeNextUnits() throws Error, AbortCompilation {
		List<CompilationUnitDeclaration> elements = new ArrayList<>();
		do {
			Object next = this.lastSignal;
			this.lastSignal = null;
			try {
				// wait until at least 1 element is available:
				if (next == null) {
					next = this.units.take();
				}
				while (next instanceof CompilationUnitDeclaration cu) {
					elements.add(cu);
					// optionally read more elements if already available:
					next = this.units.poll();
				}
				if (!elements.isEmpty()) {
					if (next != null) {
						// defer any stop signal until all CompilationUnitDeclaration read
						this.lastSignal = next;
					}
					return elements;
				}
			} catch (InterruptedException interrupt) {
				throw new AbortCompilation(true/* silent */, new RuntimeException(interrupt));
			}
			if (next instanceof Error error) {
				throw error;
			}
			if (next instanceof RuntimeException runtimeException) {
				throw runtimeException;
			}
			if (next == STOP_SIGNAL) {
				return Collections.emptyList();
			}
			throw new IllegalStateException("Received unexpected element to process: " + String.valueOf(next)); //$NON-NLS-1$
		} while (true);
	}

	private void processing() {
		try {
			int unitIndex = this.startingIndex;
			boolean noAnnotations = this.compiler.annotationProcessorManager == null;
			while (this.processing) {
				int index = unitIndex++;
				boolean cleanup = noAnnotations || this.compiler.shouldCleanup(index);
				CompilationUnitDeclaration unitToProcess = this.compiler.getUnitToProcess(index);
				try {
					if (unitToProcess == null) {
						break;
					}
					if (unitToProcess.compilationResult.hasBeenAccepted) {
						continue;
					}

					try {
						this.compiler.reportProgress(Messages.bind(Messages.compilation_processing,
								new String(unitToProcess.getFileName())));
						if (this.compiler.options.verbose)
							this.compiler.out.println(Messages.bind(Messages.compilation_process,
									new String[] { String.valueOf(index + 1), String.valueOf(this.compiler.totalUnits),
											new String(unitToProcess.getFileName()) }));
						try {
							this.compiler.process(unitToProcess, index);
						} catch (AbortCompilation abortCompilation) {
							throw abortCompilation;
						} catch (Error | RuntimeException uncheckedThrowable) {
							throw new RuntimeException(
									"Internal Error compiling " + new String(unitToProcess.getFileName()), //$NON-NLS-1$
									uncheckedThrowable);
						}
					} finally {
						// cleanup compilation unit result, but only if not annotation processed.
						if (cleanup) {
							unitToProcess.cleanUp();
						}
					}

					addNextUnit(unitToProcess);
				} catch (Error | RuntimeException uncheckedThrowable) {
					this.units.clear(); // make sure there is room for a premature stop signal
					synchronized (this) {
						this.unitWithError = unitToProcess;
					}
					addNextUnit(uncheckedThrowable);
					return;
				}
			}
		} finally {
			addNextUnit(STOP_SIGNAL);
		}
	}
	synchronized CompilationUnitDeclaration getUnitWithError(){
		return this.unitWithError;
	}

	@Override
	public void close() {
		// On exceptional handling (error/cancel) the processingTask could be still running.
		// stop it:
		this.processing = false;
		this.units.clear(); // no longer needed and allows addNextUnit() to progress if blocked
		this.processingTask.cancel(true); // interrupt whatever else the task is doing
	}
}