/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
package org.eclipse.jdt.internal.core.search.processing;

import static org.eclipse.jdt.internal.core.JavaModelManager.trace;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.Util;

public abstract class JobManager {

	/**
	 * queue of jobs to execute
	 * <br>
	 * synchronized by JobManager.this
	 */
	private final List<IJob> awaitingJobs = new LinkedList<>();

	private volatile boolean executing;

	/**
	 * background processing
	 * <br>
	 * synchronized by JobManager.this
	 */
	private Thread processingThread;

	private volatile Job progressJob;

	/**
	 * counter indicating whether job execution is enabled or not, disabled if <= 0
	 * it cannot go beyond 1
	 * <br>
	 * synchronized by JobManager.this
	 */
	private int enableCount = 1;

	public static boolean VERBOSE = false;

	/**
	 * flag indicating that the activation has completed
	 * <br>
	 * synchronized by JobManager.this
	 */
	private boolean activated;

	private final AtomicInteger awaitingClients = new AtomicInteger();

	private final Object idleMonitor = new Object();

	private synchronized Thread getProcessingThread() {
		return this.processingThread;
	}
	/**
	 * Invoked exactly once, in background, before starting processing any job
	 */
	synchronized void activateProcessing() {
		this.activated = true;
		// someone may wait for awaitingJobsCount() that returned dummy job count
		// before indexer was started
		notifyAll();
	}
	/**
	 * Answer the amount of awaiting jobs.
	 */
	public synchronized int awaitingJobsCount() {
		// pretend busy in case concurrent job attempts performing before activated
		return this.activated ? this.awaitingJobs.size() : 1;
	}
	/**
	 * Answers the first job in the queue, or null if there is no job available or
	 * index manager is disabled
	 *
	 * Until the job has completed, the job manager will keep answering the same job.
	 */
	public synchronized IJob currentJob() {
		if (this.enableCount > 0 && !this.awaitingJobs.isEmpty()) {
			return this.awaitingJobs.get(0);
		}
		return null;
	}

	/**
	 * Answers the first job in the queue, or null if there is no job available,
	 * independently on job manager enablement state.
	 * Until the job has completed, the job manager will keep answering the same job.
	 */
	public synchronized IJob currentJobForced() {
		if (!this.awaitingJobs.isEmpty()) {
			return this.awaitingJobs.get(0);
		}
		return null;
	}

	public synchronized void disable() {
		this.enableCount--;
		if (VERBOSE) {
			trace("DISABLING background indexing"); //$NON-NLS-1$
		}
	}

	/**
	 * @return {@code true} if the job manager is enabled
	 */
	public synchronized boolean isEnabled() {
		return this.enableCount > 0;
	}

	/**
	 * Remove the index from cache for a given project.
	 * Passing null as a job family discards them all.
	 */
	public void discardJobs(String jobFamily) {

		if (VERBOSE) {
			trace("DISCARD   background job family - " + jobFamily); //$NON-NLS-1$
		}

		try {
			IJob currentJob;
			// cancel current job if it belongs to the given family
			synchronized(this){
				currentJob = currentJob();
				disable();
			}
			if (currentJob != null && (jobFamily == null || currentJob.belongsTo(jobFamily))) {
				currentJob.cancel();

				synchronized (this) {
					// wait until current active job has finished
					while (getProcessingThread() != null && this.executing){
						try {
							if (VERBOSE) {
								trace("-> waiting end of current background job - " + currentJob); //$NON-NLS-1$
							}
							this.wait(50);
						} catch(InterruptedException e){
							// ignore
						}
					}
				}
			}

			synchronized(this) {
				Iterator<IJob> it = this.awaitingJobs.iterator();
				boolean notify = false;
				while (it.hasNext()) {
					currentJob = it.next();
					if (jobFamily == null || currentJob.belongsTo(jobFamily)) {
						if (VERBOSE) {
							trace("-> discarding background job  - " + currentJob); //$NON-NLS-1$
						}
						currentJob.cancel();
						it.remove();
						notify = true;
					}
				}
				if (notify){
					notifyAll(); // notify waiters for awaitingJobsCount()
				}
			}
		} finally {
			enable();
		}
		if (VERBOSE) {
			trace("DISCARD   DONE with background job family - " + jobFamily); //$NON-NLS-1$
		}
	}
	public synchronized void enable() {
		this.enableCount++;
		if (VERBOSE) {
			trace("ENABLING  background indexing"); //$NON-NLS-1$
		}
		notifyAll(); // wake up the background thread if it is waiting (context must be synchronized)
	}
	protected synchronized boolean isJobWaiting(IJob request) {
		if(this.awaitingJobs.size() <= 1) {
			return false;
		}
		return hasPendingJobMatching(request::equals);
	}

	protected synchronized boolean hasPendingJobMatching(Predicate<IJob> request) {
		int awaitingJobsCount = awaitingJobsCount();
		if (awaitingJobsCount <= 1) {
			return false;
		}
		// Start at the end and go backwards
		ListIterator<IJob> iterator = this.awaitingJobs.listIterator(awaitingJobsCount);
		IJob first = this.awaitingJobs.get(0);
		while (iterator.hasPrevious()) {
			IJob job = iterator.previous();
			// don't check first job, as it may have already started
			if(job == first) {
				break;
			}
			if (request.test(job)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Advance to the next available job, once the current one has been completed.
	 * Note: clients awaiting until the job count is zero are still waiting at this point.
	 */
	protected synchronized void moveToNextJob() {
		//if (!enabled) return;

		if (!this.awaitingJobs.isEmpty()) {
			this.awaitingJobs.remove(0);
			if (awaitingJobsCount() == 0) {
				synchronized (this) {
					this.notifyAll();
				}
			}
		}
	}
	/**
	 * When idle, give chance to do something
	 */
	protected abstract void notifyIdle(long idlingMilliSeconds);

	/**
	 * This API is allowing to run one job in concurrence with background processing.
	 * Indeed since other jobs are performed in background, resource sharing might be
	 * an issue.Therefore, this functionality allows a given job to be run without
	 * colliding with background ones.
	 * Note: multiple thread might attempt to perform concurrent jobs at the same time,
	 *            and should synchronize (it is deliberately left to clients to decide whether
	 *            concurrent jobs might interfere or not. In general, multiple read jobs are ok).
	 *
	 * Waiting policy can be:
	 * 		IJobConstants.ForceImmediateSearch
	 * 		IJobConstants.CancelIfNotReadyToSearch
	 * 		IJobConstants.WaitUntilReadyToSearch
	 */
	public boolean performConcurrentJob(IJob searchJob, int waitingPolicy, IProgressMonitor monitor) {
		if (VERBOSE) {
			trace("STARTING  concurrent job - " + searchJob); //$NON-NLS-1$
		}

		searchJob.ensureReadyToRun();

		boolean status = IJob.FAILED;
		try {
			SubMonitor subMonitor = SubMonitor.convert(monitor);
			if (awaitingJobsCount() > 0) {
				if (VERBOSE) {
					trace("-> NOT READY - " + awaitingJobsCount() + " awaiting jobs - " + searchJob);//$NON-NLS-1$ //$NON-NLS-2$
				}

				switch (waitingPolicy) {

					case IJob.ForceImmediate :
						if (VERBOSE) {
							trace("-> NOT READY - forcing immediate - " + searchJob);//$NON-NLS-1$
						}
						try {
							disable(); // pause indexing
							status = searchJob.execute(subMonitor);
						} finally {
							enable();
						}
						if (VERBOSE) {
							trace("FINISHED  concurrent job - " + searchJob); //$NON-NLS-1$
						}
						return status;

					case IJob.CancelIfNotReady :
						if (VERBOSE) {
							trace("-> NOT READY - cancelling - " + searchJob); //$NON-NLS-1$
							trace("CANCELED concurrent job - " + searchJob); //$NON-NLS-1$
						}
						throw new OperationCanceledException();

					case IJob.WaitUntilReady :
						int totalWork = 1000;
						SubMonitor waitMonitor = subMonitor.setWorkRemaining(10).split(8).setWorkRemaining(totalWork);
						// use local variable to avoid potential NPE (see bug 20435 NPE when searching java method
						// and bug 42760 NullPointerException in JobManager when searching)
						Thread t = getProcessingThread();
						int originalPriority = t == null ? -1 : t.getPriority();
						try {
							if (t != null)
								t.setPriority(Thread.currentThread().getPriority());
							this.awaitingClients.incrementAndGet();
							IJob previousJob = null;
							int awaitingJobsCount;
							int lastJobsCount = totalWork;
							float lastWorked = 0;
							float totalWorked = 0;
							while ((awaitingJobsCount = awaitingJobsCount()) > 0) {
								if (waitMonitor.isCanceled() || getProcessingThread() == null)
									throw new OperationCanceledException();

								boolean shouldDisable = false;
								IJob currentJob = currentJobForced();
								if (currentJob != null) {
									if (!isEnabled()) {
										if (VERBOSE) {
											trace("-> NOT READY (" + this.enableCount //$NON-NLS-1$
													+ ") - enabling indexer to process " //$NON-NLS-1$
													+ awaitingJobsCount + " jobs - " + searchJob);//$NON-NLS-1$
										}
										enable();
										shouldDisable = true;
									}
									synchronized (this.idleMonitor) {
										this.idleMonitor.notifyAll(); // wake up idle sleepers
									}
								}
								if (currentJob != null && currentJob != previousJob) {
									if (VERBOSE) {
										trace("-> NOT READY - waiting until ready  to process " + awaitingJobsCount + " awaiting jobs - " + searchJob);//$NON-NLS-1$ //$NON-NLS-2$
									}
									String indexing = Messages.bind(Messages.jobmanager_filesToIndex, currentJob.getJobFamily(), Integer.toString(awaitingJobsCount));
									waitMonitor.subTask(indexing);
									// ratio of the amount of work relative to the total work
									float ratio = awaitingJobsCount < totalWork ? 1 : ((float) totalWork) / awaitingJobsCount;
									if (lastJobsCount > awaitingJobsCount) {
										totalWorked += (lastJobsCount - awaitingJobsCount) * ratio;
									} else {
										// more jobs were added, just increment by the ratio
										totalWorked += ratio;
									}
									if (totalWorked - lastWorked >= 1) {
										waitMonitor.worked((int) (totalWorked - lastWorked));
										lastWorked = totalWorked;
									}
									lastJobsCount = awaitingJobsCount;
									previousJob = currentJob;
								}
								synchronized (this) {
									if (awaitingJobsCount() > 0) {
										try {
											this.wait(50); // avoid Thread.sleep! wait is informed by notifyAll
										} catch (InterruptedException e) {
											// ignore
										}
									}
								}
								if (shouldDisable) {
									if (VERBOSE) {
										trace("-> NOT READY (" + this.enableCount + ") - disabling indexer again, still awaiting jobs: " //$NON-NLS-1$ //$NON-NLS-2$
												+ awaitingJobsCount + " - " + searchJob);//$NON-NLS-1$
									}
									disable();
								}
							}
						} finally {
							this.awaitingClients.decrementAndGet();
							if (t != null && originalPriority > -1 && t.isAlive())
								t.setPriority(originalPriority);
						}
				}
			}
			status = searchJob.execute(subMonitor);
		} finally {
			SubMonitor.done(monitor);
			if (VERBOSE) {
				trace("FINISHED  concurrent job - " + searchJob); //$NON-NLS-1$
			}
		}
		return status;
	}
	public abstract String processName();

	/**
	 * Schedules given job for execution is there is no equal jobs waiting in the queue already
	 *
	 * @see JobManager#isJobWaiting(IJob)
	 * @param job
	 *            a job to schedule (or not)
	 */
	public synchronized void requestIfNotWaiting(IJob job) {
		if(!isJobWaiting(job)) {
			request(job);
		}
	}

	public synchronized void request(IJob job) {

		job.ensureReadyToRun();
		// append the job to the list of ones to process later on
		this.awaitingJobs.add(job);
		if (VERBOSE) {
			trace("REQUEST   background job - " + job); //$NON-NLS-1$
			trace("AWAITING JOBS count: " + awaitingJobsCount()); //$NON-NLS-1$
		}
		notifyAll(); // wake up the background thread if it is waiting
	}
	/**
	 * Flush current state
	 */
	public void reset() {
		if (VERBOSE) {
			trace("Reset"); //$NON-NLS-1$
		}

		Thread thread = getProcessingThread();

		if (thread != null) {
			discardJobs(null); // discard all jobs
		} else {
			synchronized (this) {
				/* initiate background processing */
				Thread t = new Thread(this::indexerLoop, processName());
				t.setDaemon(true);
				// less prioritary by default, priority is raised if clients are actively waiting on it
				t.setPriority(Thread.NORM_PRIORITY-1);
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296343
				// set the context loader to avoid leaking the current context loader
				t.setContextClassLoader(this.getClass().getClassLoader());
				t.start();
				this.processingThread = t;
			}
		}
	}

	/**
	 * Infinite loop performing resource indexing
	 */
	void indexerLoop() {

		boolean cacheZipFiles = false;
		Long idlingStart = null;
		activateProcessing();
		try {
			class ProgressJob extends Job {
				ProgressJob(String name) {
					super(name);
				}
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					IJob job = currentJob();
					while (!monitor.isCanceled() && job != null) {
						 String taskName = new StringBuffer(Messages.jobmanager_indexing)
							.append(Messages.bind(Messages.jobmanager_filesToIndex, job.getJobFamily(), Integer.toString(awaitingJobsCount())))
							.toString();
						monitor.subTask(taskName);
						setName(taskName);
						synchronized (JobManager.this) {
							if (currentJob() != null) {
								try {
									JobManager.this.wait(500);
								} catch (InterruptedException e) {
									// ignore
								}
							}
						}
						job = currentJob();
					}
					//make sure next index job will schedule new ProgressJob:
					JobManager.this.progressJob = null;
					return Status.OK_STATUS;
				}
			}
			this.progressJob = null;
			while (getProcessingThread() != null) {
				try {
					IJob job;
					synchronized (this) {
						// handle shutdown case when notifyAll came before the wait but after the while loop was entered
						if (getProcessingThread() == null) continue;

						// must check for new job inside this sync block to avoid timing hole
						if ((job = currentJob()) == null) {
							Job pJob = this.progressJob;
							if (pJob != null) {
								pJob.cancel();
								this.progressJob = null;
							}
							if (idlingStart == null) {
								idlingStart = System.nanoTime();
							} else {
								this.wait(); // wait until a new job is posted or disabled indexer is enabled again
							}
						}
					}
					if (job == null) {
						// don't call notifyIdle() within synchronized block or it may deadlock:
						notifyIdle((System.nanoTime() - idlingStart) / 1_000_000);
						if (currentJob() != null) {
							// notifyIdle() may have requested new job
							continue;
						}
						if (cacheZipFiles) {
							JavaModelManager.getJavaModelManager().flushZipFiles(this);
							cacheZipFiles = false;
						}
						// just woke up, delay before processing any new jobs, allow some time for the active thread to finish
						synchronized (this.idleMonitor) {
							this.idleMonitor.wait(500); // avoid sleep fixed time
						}
						continue;
					}
					idlingStart = null;
					if (VERBOSE) {
						trace(awaitingJobsCount() + " awaiting jobs"); //$NON-NLS-1$
						trace("STARTING background job - " + job); //$NON-NLS-1$
					}
					try {
						this.executing = true;
						if (this.progressJob == null) {
							ProgressJob pJob = new ProgressJob(Messages.bind(Messages.jobmanager_indexing, "", "")); //$NON-NLS-1$ //$NON-NLS-2$
							pJob.setPriority(Job.LONG);
							pJob.setSystem(true);
							pJob.schedule();
							this.progressJob = pJob;
						}
						if (!cacheZipFiles) {
							JavaModelManager.getJavaModelManager().cacheZipFiles(this);
							cacheZipFiles = true;
						}
						job.execute(null); // may enqueue a new job
					} finally {
						this.executing = false;
						if (VERBOSE) {
							trace("FINISHED background job - " + job); //$NON-NLS-1$
						}
						moveToNextJob();
						if (this.awaitingClients.get() == 0 && job.waitNeeded()) {
							if (VERBOSE) {
								trace("WAITING after job - " + job); //$NON-NLS-1$
							}
							synchronized (this.idleMonitor) {
								this.idleMonitor.wait(5); // avoid sleep fixed time
							}
						}
					}
				} catch (InterruptedException e) { // background indexing was interrupted
				}
			}
		} catch (ThreadDeath e) {
			// do not restart
			throw e;
		} catch (RuntimeException|Error e) {
			if (getProcessingThread() != null) { // if not shutting down
				// log exception
				Util.log(e, "Background Indexer Crash Recovery"); //$NON-NLS-1$

				// keep job manager alive
				discardJobs(null);
				synchronized (this) {
					this.processingThread = null;
				}
				reset(); // this will fork a new thread with no waiting jobs, some indexes will be inconsistent
			}
			throw e;
		} finally {
			if (cacheZipFiles) {
				JavaModelManager.getJavaModelManager().flushZipFiles(this);
				cacheZipFiles = false;
			}
		}
	}
	/**
	 * Stop background processing, and wait until the current job is completed before returning
	 */
	public void shutdown() {

		if (VERBOSE) {
			trace("Shutdown"); //$NON-NLS-1$
		}

		disable();
		discardJobs(null); // will wait until current executing job has completed
		Thread thread = getProcessingThread();
		try {
			if (thread != null) { // see http://bugs.eclipse.org/bugs/show_bug.cgi?id=31858
				synchronized (this) {
					this.processingThread = null; // mark the job manager as shutting down so that the thread will stop by itself
					notifyAll(); // ensure its awake so it can be shutdown
				}
				// in case processing thread is handling a job
				thread.join();
			}
			Job job = this.progressJob;
			if (job != null) {
				job.cancel();
				job.join();
			}
		} catch (InterruptedException e) {
			// ignore
		}
	}
	@Override
	public synchronized String toString() {
		StringBuilder buffer = new StringBuilder(10);
		buffer.append("Enable count:").append(this.enableCount).append('\n'); //$NON-NLS-1$
		int numJobs = this.awaitingJobs.size();
		buffer.append("Jobs in queue:").append(numJobs).append('\n'); //$NON-NLS-1$
		for (int i = 0; i < numJobs && i < 15; i++) {
			buffer.append(i).append(" - job["+i+"]: ").append(this.awaitingJobs.get(i)).append('\n'); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return buffer.toString();
	}
}
