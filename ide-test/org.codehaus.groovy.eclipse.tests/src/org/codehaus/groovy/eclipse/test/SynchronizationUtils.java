/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.test;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

// Adapted from org.eclipse.jdt.ui.tests.performance.JdtPerformanceTestCase
public class SynchronizationUtils {

		
	public static void joinBackgroudActivities()  {
		// Join Building
		boolean interrupted= true;
		while (interrupted) {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
				interrupted= false;
			} catch (InterruptedException e) {
				interrupted= true;
			}
		}
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD, null);
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);	
		// Join jobs
		joinJobs(100, 500, 500);
	}


	private static boolean joinJobs(long minTime, long maxTime, long intervalTime) {
		long startTime= System.currentTimeMillis() + minTime;
		runEventQueue();
		while (System.currentTimeMillis() < startTime)
			runEventQueue(intervalTime);
		
		long endTime= maxTime > 0  && maxTime < Long.MAX_VALUE ? System.currentTimeMillis() + maxTime : Long.MAX_VALUE;
		boolean calm= allJobsQuiet();
		while (!calm && System.currentTimeMillis() < endTime) {
			runEventQueue(intervalTime);
			calm= allJobsQuiet();
		}
		return calm;
	}
	
	private static void sleep(int intervalTime) {
		try {
			Thread.sleep(intervalTime);
		} catch (InterruptedException e) {
		}
	}
	
	private static boolean allJobsQuiet() {
		IJobManager jobManager= Job.getJobManager();
		Job[] jobs= jobManager.find(null);
		for (int i= 0; i < jobs.length; i++) {
			Job job= jobs[i];
			int state= job.getState();
			//ignore jobs we don't care about
			if (!job.getName().equals("Flush Cache Job") &&  //$NON-NLS-1$
			        !job.getName().equals("Usage Data Event consumer") &&  //$NON-NLS-1$
					(state == Job.RUNNING || state == Job.WAITING)) {
				return false;
			}
		}
		return true;
	}
	
	private static void runEventQueue() {
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
			runEventQueue(window.getShell());
	}
	
	private static void runEventQueue(Shell shell) {
		try {
			while (shell.getDisplay().readAndDispatch()) {
				// do nothing
			}
		} catch (SWTException e) {
			System.err.println(e);
		}
	}
	
	private static void runEventQueue(long minTime) {
		long nextCheck= System.currentTimeMillis() + minTime;
		while (System.currentTimeMillis() < nextCheck) {
			runEventQueue();
			sleep(1);
		}
	}
	
	public static void printJobs() {
       IJobManager jobManager = Job.getJobManager();
        Job[] jobs = jobManager.find(null);
        System.out.println("=====Printing Jobs========");
        for (int i = 0; i < jobs.length; i++) {
            Job job = jobs[i];
            int state = job.getState();
            // ignore jobs we don't care about
            if (!job.getName().equals("Flush Cache Job") && //$NON-NLS-1$
                    !job.getName().equals("Usage Data Event consumer") && //$NON-NLS-1$
                    (state == Job.RUNNING || state == Job.WAITING)) {
                System.out.println(job.getName());
            }
        }
        System.out.println("==========================");
    }
}
