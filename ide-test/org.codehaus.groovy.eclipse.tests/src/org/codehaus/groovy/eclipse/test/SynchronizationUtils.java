/*
 * Copyright 2009-2017 the original author or authors.
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

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.groovy.tests.SimpleProgressMonitor;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

// Adapted from org.eclipse.jdt.ui.tests.performance.JdtPerformanceTestCase
public class SynchronizationUtils {

    public static void joinBackgroundActivities() {
        boolean interrupted;
        do {
            interrupted = false;
            try {
                Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
            } catch (OperationCanceledException e) {

            } catch (InterruptedException e) {
                interrupted = true;
            }
        } while (interrupted);

        do {
            interrupted = false;
            try {
                Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD, null);
            } catch (OperationCanceledException e) {

            } catch (InterruptedException e) {
                interrupted = true;
            }
        } while (interrupted);

        joinJobs(100, 500, 500);
    }

    private static boolean joinJobs(long minTime, long maxTime, long intervalTime) {
        long startTime = System.currentTimeMillis() + minTime;
        runEventQueue();
        while (System.currentTimeMillis() < startTime) {
            runEventQueue(intervalTime);
        }

        long endTime = maxTime > 0 && maxTime < Long.MAX_VALUE ? System.currentTimeMillis() + maxTime : Long.MAX_VALUE;
        boolean calm = allJobsQuiet();
        while (!calm && System.currentTimeMillis() < endTime) {
            runEventQueue(intervalTime);
          //printJobs();
            calm = allJobsQuiet();
        }
        return calm;
    }

    public static void sleep(int intervalTime) {
        try {
            Thread.sleep(intervalTime);
        } catch (InterruptedException e) {
        }
    }

    private static void runEventQueue() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null)
            runEventQueue(window.getShell());
    }

    private static void runEventQueue(Shell shell) {
        try {
            while (shell.getDisplay().readAndDispatch()) {
                // do nothing
            }
        } catch (SWTException e) {
            e.printStackTrace();
        }
    }

    private static void runEventQueue(long minTime) {
        long nextCheck = System.currentTimeMillis() + minTime;
        while (System.currentTimeMillis() < nextCheck) {
            runEventQueue();
            sleep(1);
        }
    }

    public static IViewPart showView(String id) throws Exception {
        try {
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(id);
        } finally {
            runEventQueue();
        }
    }

    private static boolean allJobsQuiet() {
        for (Job job : Job.getJobManager().find(null)) {
            switch (job.getState()) {
            case Job.RUNNING:
            case Job.WAITING:
                if (!SKIP_JOBS.contains(job.getName())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void printJobs() {
        System.out.println("=====Printing Jobs========");
        for (Job job : Job.getJobManager().find(null)) {
            switch (job.getState()) {
            case Job.RUNNING:
            case Job.WAITING:
                if (!SKIP_JOBS.contains(job.getName())) {
                    System.out.println(job.getName());
                }
            }
        }
        System.out.println("==========================");
    }

    public static void waitForIndexingToComplete(IJavaElement element) {
        try {
            JavaModelManager.getIndexManager().indexAll(element.getJavaProject().getProject());
            SimpleProgressMonitor monitor = new SimpleProgressMonitor("Search to trigger indexing");
            new SearchEngine().searchAllTypeNames(
                null,
                SearchPattern.R_EXACT_MATCH,
                "XXXXXXXXX".toCharArray(), // make sure we search a concrete name. This is faster according to Kent
                SearchPattern.R_EXACT_MATCH,
                IJavaSearchConstants.CLASS,
                SearchEngine.createJavaSearchScope(new IJavaElement[]{element}),
                new TypeNameRequestor() {},
                IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
                monitor);
            monitor.waitForCompletion();
        } catch (CoreException e) {
            e.printStackTrace();
        }

        SynchronizationUtils.joinBackgroundActivities();
        for (Job job : Job.getJobManager().find(null)) {
            switch (job.getState()) {
            case Job.RUNNING:
            case Job.WAITING:
                if (job.getName().contains("Java index")) {
                    joinUninterruptibly(job);
                }
            }
        }
    }

    public static void waitForDSLDProcessingToComplete() {
        SynchronizationUtils.joinBackgroundActivities();
        for (Job job : Job.getJobManager().find(null)) {
            switch (job.getState()) {
            case Job.RUNNING:
            case Job.WAITING:
                if (job.getName().startsWith("Refresh DSLD scripts")) {
                    joinUninterruptibly(job);
                }
            }
        }
    }

    private static void joinUninterruptibly(Job job) {
        boolean interrupted;
        do {
            interrupted = false;
            try {
                job.join();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        } while (interrupted);
    }

    private static final List<String> SKIP_JOBS = Arrays.asList("Animation start", "Decoration Calculation", "Flush Cache Job", "Open Blocked Dialog", "Sending problem marker updates...", "Update for Decoration Completion", "Update package explorer", "Usage Data Event consumer");
}
