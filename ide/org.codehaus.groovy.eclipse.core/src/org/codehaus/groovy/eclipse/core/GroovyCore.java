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
package org.codehaus.groovy.eclipse.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class GroovyCore {
    static boolean trace;

    static {
        String value = Platform.getDebugOption("org.codehaus.groovy.eclipse.core/trace"); //$NON-NLS-1$
        if (value != null && value.equalsIgnoreCase("true")) //$NON-NLS-1$
            trace = true;
    }

    /**
     * Logs an exception
     * 
     * @param message The message to save.
     * @param exception The exception to be logged.
     */
    public static void logException(String message, Throwable throwable) {
        log(IStatus.ERROR, message, throwable);
    }

    /**
     * Logs a warning.
     * 
     * @param message The warning to log.
     */
    public static void logWarning(final String message) {
        log(IStatus.WARNING, message, null);
    }

    /**
     * Logs a warning with an exception attached
     * 
     * @param message The warning to log.
     */
    public static void logWarning(final String message, final Throwable t) {
        log(IStatus.WARNING, message, t);
    }
    
    /**
     * Logs an information message.
     * 
     * @param message The message to log.
     */
    public static void logTraceMessage(String message) {
        log(IStatus.INFO, message, null);
    }

    private static void log(int severity, String message, Throwable throwable) {
        final IStatus status = new Status(severity, GroovyCoreActivator.getDefault().getBundle().getSymbolicName(), 0, message, throwable);
        GroovyCoreActivator.getDefault().getLog().log(status);
    }

    public static void trace(String message) {
        if (trace) {
            logTraceMessage("trace: " + message);
        }
    }

    public static void errorRunningGroovyFile(IFile file, Exception exception) {
        logException("Error running Groovy file: " + file.getName(), exception);
    }

    public static void errorRunningGroovy(Exception exception) {
        logException("Error running Groovy", exception);
    }
}