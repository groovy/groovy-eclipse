/*
 * Copyright 2009-2019 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A basic progress monitor that can be waited upon (via waitForCompletion()).
 */
public class SimpleProgressMonitor implements IProgressMonitor {

    public String description;
    private static boolean debug;
    public volatile int incomplete;

    public SimpleProgressMonitor(String description) {
        this.description = description;
    }

    @Override
    public void beginTask(String name, int totalWork) {
        incomplete += 1;
    }

    @Override
    public void done() {
        incomplete -= 1;
    }

    @Override
    public void internalWorked(double work) {
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void setCanceled(boolean value) {
    }

    @Override
    public void setTaskName(String name) {
    }

    @Override
    public void subTask(String name) {
    }

    @Override
    public void worked(int work) {
    }

    /**
     * Wait up to 5seconds for this progress monitor to be called with 'done()'.
     * If it times out then an IllegalStateException is thrown.
     */
    public void waitForCompletion() {
        waitForCompletion(5);
    }

    /**
     * Wait up to the specified number of seconds for this progress monitor to be called with 'done()'.
     * If it times out then an IllegalStateException is thrown.
     */
    public void waitForCompletion(int timeoutSeconds) {
        int count = 0;
        while (incomplete > 0) {
            try {
                Thread.sleep(250);
            } catch (Exception ignore) {
            }
            count += 1;
            if (count > (timeoutSeconds * 4)) {
                throw new IllegalStateException(description + " timed out after " + timeoutSeconds + " seconds");
            }
        }
        if (debug && incomplete < 1) {
            System.err.println(description + " completed");
        }
    }
}
