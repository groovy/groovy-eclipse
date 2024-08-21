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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.util.Messages;

public class ProcessTaskManager {

	private final Compiler compiler;
	private final int startingIndex;
	private volatile Future<?> processingTask; // synchronized write, volatile read
	CompilationUnitDeclaration unitToProcess;
	private Throwable caughtException;

	// queue
	volatile int currentIndex, availableIndex, size, sleepCount;
	private final CompilationUnitDeclaration[] units;

	public static final int PROCESSED_QUEUE_SIZE = 100;

	/** Normally a single thread is created an reused on subsequent builds **/
	private static final ExecutorService executor = Executors.newCachedThreadPool(r -> {
		Thread t = new Thread(r, "Compiler Processing Task"); //$NON-NLS-1$
		t.setDaemon(true);
		return t;
	});

public ProcessTaskManager(Compiler compiler, int startingIndex) {
	this.compiler = compiler;
	this.startingIndex = startingIndex;

	this.currentIndex = 0;
	this.availableIndex = 0;
	this.size = PROCESSED_QUEUE_SIZE;
	this.sleepCount = 0; // 0 is no one, +1 is the processing thread & -1 is the writing/main thread
	this.units = new CompilationUnitDeclaration[this.size];

	synchronized (this) {
		this.processingTask = executor.submit(this::compile);
	}
}

// add unit to the queue - wait if no space is available
private synchronized void addNextUnit(CompilationUnitDeclaration newElement) {
	while (this.units[this.availableIndex] != null) {
		//System.out.print('a');
		//if (this.sleepCount < 0) throw new IllegalStateException(Integer.valueOf(this.sleepCount).toString());
		this.sleepCount = 1;
		try {
			wait(250);
		} catch (InterruptedException ignore) {
			// ignore
		}
		this.sleepCount = 0;
	}

	this.units[this.availableIndex++] = newElement;
	if (this.availableIndex >= this.size)
		this.availableIndex = 0;
	if (this.sleepCount <= -1)
		notify(); // wake up writing thread to accept next unit - could be the last one - must avoid deadlock
}

public CompilationUnitDeclaration removeNextUnit() throws Error {
	CompilationUnitDeclaration next = null;
	boolean yield = false;
	synchronized (this) {
		next = this.units[this.currentIndex];
		if (next == null || this.caughtException != null) {
			do {
				if (this.processingTask == null) {
					if (this.caughtException != null) {
						// rethrow the caught exception from the processingThread in the main compiler thread
						if (this.caughtException instanceof Error)
							throw (Error) this.caughtException;
						throw (RuntimeException) this.caughtException;
					}
					return null;
				}
				//System.out.print('r');
				//if (this.sleepCount > 0) throw new IllegalStateException(Integer.valueOf(this.sleepCount).toString());
				this.sleepCount = -1;
				try {
					wait(100);
				} catch (InterruptedException ignore) {
					// ignore
				}
				this.sleepCount = 0;
				next = this.units[this.currentIndex];
			} while (next == null);
		}

		this.units[this.currentIndex++] = null;
		if (this.currentIndex >= this.size)
			this.currentIndex = 0;
		if (this.sleepCount >= 1 && ++this.sleepCount > 4) {
			notify(); // wake up processing thread to add next unit but only after removing some elements first
			yield = this.sleepCount > 8;
		}
	}
	if (yield)
		Thread.yield();
	return next;
}

private void compile() {
	int unitIndex = this.startingIndex;
	synchronized (this) { // wait until processingTask is assigned
		@SuppressWarnings("unused")
		Future<?> p = this.processingTask;
	}
	boolean noAnnotations = this.compiler.annotationProcessorManager == null;
	while (this.processingTask != null) {
		this.unitToProcess = null;
		int index = -1;
		boolean cleanup = noAnnotations || this.compiler.shouldCleanup(unitIndex);
		try {
			synchronized (this) {
				if (this.processingTask == null) return;

				this.unitToProcess = this.compiler.getUnitToProcess(unitIndex);
				if (this.unitToProcess == null) {
					this.processingTask = null;
					return;
				}
				index = unitIndex++;
				if (this.unitToProcess.compilationResult.hasBeenAccepted)
					continue;
			}

			try {
				this.compiler.reportProgress(Messages.bind(Messages.compilation_processing, new String(this.unitToProcess.getFileName())));
				if (this.compiler.options.verbose)
					this.compiler.out.println(
						Messages.bind(Messages.compilation_process,
						new String[] {
							String.valueOf(index + 1),
							String.valueOf(this.compiler.totalUnits),
							new String(this.unitToProcess.getFileName())
						}));
				try {
					this.compiler.process(this.unitToProcess, index);
				} catch (AbortCompilation keptCancelation) {
					throw keptCancelation;
				} catch (Error | RuntimeException e) {
					throw new RuntimeException("Internal Error compiling " + new String(this.unitToProcess.getFileName()), e); //$NON-NLS-1$
				}
			} finally {
				// cleanup compilation unit result, but only if not annotation processed.
				if (this.unitToProcess != null && cleanup)
					this.unitToProcess.cleanUp();
			}

			addNextUnit(this.unitToProcess);
		} catch (Error | RuntimeException e) {
			synchronized (this) {
				this.processingTask = null;
				this.caughtException = e;
			}
			return;
		}
	}
}

public void shutdown() {
	try {
		Future<?> t = null;
		synchronized (this) {
			t = this.processingTask;
			if (t != null) {
				// stop processing on error:
				this.processingTask = null;
				notifyAll();
			}
		}
		if (t != null) {
			t.get(250, TimeUnit.MILLISECONDS); // do not wait forever
		}
	} catch (InterruptedException | ExecutionException | TimeoutException ignored) {
		// ignore
	}
}
}
