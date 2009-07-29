/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

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

    public static Preferences getPreferenceStore() {
        return GroovyCoreActivator.getDefault().getPluginPreferences();
    }

    public static ManifestElement[] getGroovyBundleClasspath() throws BundleException {
        final String header = "Groovy-Runtime-Jars";
        return getBundleClasspath(header, "" + GroovyCoreActivator.bundle("org.codehaus.groovy").getHeaders().get(header));
    }

    public static ManifestElement[] getBundleClasspath(final long id) throws BundleException {
        return getBundleClasspath("" + GroovyCoreActivator.getBundle(id).getHeaders().get(Constants.BUNDLE_CLASSPATH));
    }

    public static ManifestElement[] getBundleClasspath() throws BundleException {
        return getBundleClasspath("" + GroovyCoreActivator.getDefault().getBundle().getHeaders().get(Constants.BUNDLE_CLASSPATH));
    }

    private static ManifestElement[] getBundleClasspath(final String requires) throws BundleException {
        if (StringUtils.isBlank(requires))
            return new ManifestElement[0];
        return getBundleClasspath(Constants.BUNDLE_CLASSPATH, requires);
    }

    private static ManifestElement[] getBundleClasspath(final String header, final String requires) throws BundleException {
        if (StringUtils.isBlank(requires))
            return new ManifestElement[0];
        return ManifestElement.parseHeader(header, requires);
    }

    public static void errorRunningGroovyFile(IFile file, Exception exception) {
        logException("Error running Groovy file: " + file.getName(), exception);
    }

    public static void errorRunningGroovy(Exception exception) {
        logException("Error running Groovy", exception);
    }

    public static IPath getEmbeddedGroovyRuntimeHome() {
        try {
            return new Path(FileLocator.toFileURL(
                    GroovyCoreActivator.bundle("org.codehaus.groovy").getEntry(
                            "/")).getPath());
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    public static final String GROOVY_ERROR_MARKER = "org.codehaus.groovy.eclipse.groovyFailure";
}