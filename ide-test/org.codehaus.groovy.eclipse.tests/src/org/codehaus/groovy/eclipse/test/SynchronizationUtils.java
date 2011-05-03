 /*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.test;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.internal.core.JavaModelManager;
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
//			printJobs();
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
	
	public static void waitForIndexingToComplete() {
	    try {
            performDummySearch(JavaModelManager.getJavaModelManager().getJavaModel());
        } catch (CoreException e1) {
            e1.printStackTrace();
        }
	    SynchronizationUtils.joinBackgroudActivities();
        Job[] jobs = Job.getJobManager().find(null);
        for (int i = 0; i < jobs.length; i++) {
            if (jobs[i].getName().startsWith("Java indexing")) {
                boolean wasInterrupted = true;
                while (wasInterrupted) {
                    try {
                        wasInterrupted = false;
                        jobs[i].join();
                    } catch (InterruptedException e) {
                        wasInterrupted = true;
                    }
                }
            }
        }
	}
	
    protected static class Requestor extends TypeNameRequestor { }

    private static void performDummySearch(IJavaElement element) throws CoreException {
        new SearchEngine().searchAllTypeNames(
            null,
            SearchPattern.R_EXACT_MATCH,
            "XXXXXXXXX".toCharArray(), // make sure we search a concrete name. This is faster according to Kent
            SearchPattern.R_EXACT_MATCH,
            IJavaSearchConstants.CLASS,
            SearchEngine.createJavaSearchScope(new IJavaElement[]{element}),
            new Requestor(),
            IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
            null);
    }


	public static void waitForRefactoringToComplete() {
	    SynchronizationUtils.joinBackgroudActivities();
	    Job[] jobs = Job.getJobManager().find(null);
	    for (int i = 0; i < jobs.length; i++) {
	        if (jobs[i].getName().startsWith("Java indexing")) {
	            boolean wasInterrupted = true;
	            while (wasInterrupted) {
	                try {
	                    wasInterrupted = false;
	                    jobs[i].join();
	                } catch (InterruptedException e) {
	                    wasInterrupted = true;
	                }
	            }
	        }
	    }
	}

    public static void waitForDSLDProcessingToComplete() {
        SynchronizationUtils.joinBackgroudActivities();
        Job[] jobs = Job.getJobManager().find(null);
        for (int i = 0; i < jobs.length; i++) {
            if (jobs[i].getName().startsWith("Refresh DSLD scripts")) {
                boolean wasInterrupted = true;
                while (wasInterrupted) {
                    try {
                        wasInterrupted = false;
                        jobs[i].join();
                    } catch (InterruptedException e) {
                        wasInterrupted = true;
                    }
                }
            }
        }
    }

}
