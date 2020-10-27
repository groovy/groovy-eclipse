/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.processing.IJob;
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
            } catch (OperationCanceledException ignore) {

            } catch (InterruptedException e) {
                interrupted = true;
            }
        } while (interrupted);

        do {
            interrupted = false;
            try {
                Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD, null);
            } catch (OperationCanceledException ignore) {

            } catch (InterruptedException e) {
                interrupted = true;
            }
        } while (interrupted);

        runEventQueue();

        final long stopTime = (System.currentTimeMillis() + 1500);
        while (anyRunJobs() && System.currentTimeMillis() < stopTime) {
            runEventQueue();
            printJobs();
            sleep(100);
        }
    }

    public static void waitForIndexingToComplete() {
        if (JavaModelManager.getIndexManager().awaitingJobsCount() > 0) {
            // adapted from org.eclipse.jdt.internal.core.JavaModelManager#secondaryTypes and
            // org.eclipse.jdt.internal.core.SearchableEnvironment#findConstructorDeclarations
            JavaModelManager.getIndexManager().performConcurrentJob(new IJob() {
                @Override
                public boolean belongsTo(final String family) {
                    return true;
                }

                @Override
                public void cancel() {
                }

                @Override
                public void ensureReadyToRun() {
                }

                @Override
                public boolean execute(final IProgressMonitor progress) {
                    return progress == null || !progress.isCanceled();
                }

                @Override
                public String getJobFamily() {
                    return "";
                }
            }, IJob.WaitUntilReady, null);
        }

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
        for (Job job : Job.getJobManager().find(null)) {
            switch (job.getState()) {
            case Job.RUNNING:
            case Job.WAITING:
                if (job.getName().startsWith("Refresh DSLD")) {
                    joinUninterruptibly(job);
                }
            }
        }
    }

    public static void sleep(final int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignore) {
        }
    }

    public static IViewPart showView(final String id) {
        try {
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(id);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        } finally {
            runEventQueue();
        }
    }

    public static void joinUninterruptibly(final Job job) {
        boolean interrupted;
        do {
            interrupted = false;
            try {
                System.err.println("Waiting for: " + job.getName());
                job.join();
            } catch (OperationCanceledException ignore) {

            } catch (InterruptedException e) {
                interrupted = true;
            }
        } while (interrupted);
    }

    //--------------------------------------------------------------------------

    private static void runEventQueue() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            Shell shell = window.getShell();
            try {
                while (shell.getDisplay().readAndDispatch()) {
                    // do nothing
                }
            } catch (SWTException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean anyRunJobs() {
        for (Job job : Job.getJobManager().find(null)) {
            switch (job.getState()) {
            case Job.RUNNING:
            case Job.WAITING:
                if (!SKIP_JOBS.contains(job.getName().toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void printJobs() {
        /*
        System.out.println("=============Printing Jobs==============");
        for (Job job : Job.getJobManager().find(null)) {
            switch (job.getState()) {
            case Job.RUNNING:
            case Job.WAITING:
                if (!SKIP_JOBS.contains(job.getName().toLowerCase())) {
                    System.out.println(job.getName());
                }
            }
        }
        System.out.println("========================================");
        */
    }

    private static final List<String> SKIP_JOBS = Arrays.asList("animation start", "change cursor", "decoration calculation", "flush cache job", "open blocked dialog", "refreshing view", "sending problem marker updates...", "update for decoration completion", "update dynamic java sources working sets", "update package explorer", "update progress", "usage data event consumer");
}
