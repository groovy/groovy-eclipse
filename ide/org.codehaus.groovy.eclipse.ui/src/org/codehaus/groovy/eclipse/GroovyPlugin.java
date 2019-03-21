/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse;

import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.groovy.eclipse.debug.ui.EnsureJUnitFont;
import org.codehaus.groovy.eclipse.debug.ui.GroovyDebugOptionsEnforcer;
import org.codehaus.groovy.eclipse.debug.ui.GroovyJavaDebugElementAdapterFactory;
import org.codehaus.groovy.eclipse.editor.GroovyAwareFoldingStructureProvider;
import org.codehaus.groovy.eclipse.editor.GroovyOutlineTools;
import org.codehaus.groovy.eclipse.editor.GroovyTextTools;
import org.codehaus.groovy.eclipse.refactoring.actions.DelegatingCleanUpPostSaveListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.folding.JavaFoldingStructureProviderRegistry;
import org.eclipse.jdt.ui.text.folding.DefaultJavaFoldingStructureProvider;
import org.eclipse.jdt.ui.text.folding.IJavaFoldingStructureProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class GroovyPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.codehaus.groovy.eclipse.ui";
    public static final String GROOVY_TEMPLATE_CTX = "org.codehaus.groovy.eclipse.templates";
    public static final String COMPILER_MISMATCH_MARKER = "org.codehaus.groovy.eclipse.core.compilerMismatch";

    private static GroovyPlugin plugin;

    public static GroovyPlugin getDefault() {
        return plugin;
    }

    private static Boolean trace;

    public static void trace(String message) {
        if (trace == null) {
            String value = Platform.getDebugOption("org.codehaus.groovy.eclipse/trace");
            GroovyPlugin.trace = Boolean.valueOf(value);
        }
        if (trace == Boolean.TRUE) {
            plugin.logTraceMessage("trace: " + message);
        }
    }

    public static IWorkbenchPage getActiveWorkbenchPage() {
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        if (window != null) return window.getActivePage();
        return null;
    }

    public static Shell getActiveWorkbenchShell() {
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }
        Shell shell = window.getShell();
        if (shell == null) {
            shell = plugin.getWorkbench().getDisplay().getActiveShell();
        }
        return shell;
    }

    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        if (plugin == null) {
            return null;
        }
        IWorkbench workbench = plugin.getWorkbench();
        if (workbench == null) {
            return null;
        }
        return workbench.getActiveWorkbenchWindow();
    }

    //--------------------------------------------------------------------------

    private EnsureJUnitFont junitMono;
    private GroovyTextTools textTools;
    private GroovyOutlineTools outlineTools;

    public GroovyPlugin() {
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        textTools = new GroovyTextTools();
        outlineTools = new GroovyOutlineTools();

        addMonospaceFontListener();
        setStructureProviderRegistry();
        DelegatingCleanUpPostSaveListener.installCleanUp();

        // register our own stack frame label provider so that groovy stack frames are shown differently
        GroovyJavaDebugElementAdapterFactory.connect();

        if (getPreferenceStore().getBoolean(PreferenceConstants.GROOVY_DEBUG_FORCE_DEBUG_OPTIONS_ON_STARTUP)) {
            new GroovyDebugOptionsEnforcer().maybeForce(getPreferenceStore());
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        try {
            textTools.dispose();
            textTools = null;

            outlineTools.dispose();
            outlineTools = null;

            removeMonospaceFontListener();
            DelegatingCleanUpPostSaveListener.uninstallCleanUp();
        } finally {
            super.stop(context);
        }
    }

    private void addMonospaceFontListener() {
        junitMono = new EnsureJUnitFont();
        try {
            if (PlatformUI.isWorkbenchRunning() && getActiveWorkbenchPage() != null) {
                getActiveWorkbenchPage().addPartListener(junitMono);
            }
            getPreferenceStore().addPropertyChangeListener(junitMono);
            PrefUtil.getInternalPreferenceStore().addPropertyChangeListener(junitMono);
        } catch (Exception e) {
            logError("Error installing JUnit monospace font listener", e);
        }
    }

    private void removeMonospaceFontListener() {
        try {
            if (!getWorkbench().isClosing()) {
                getActiveWorkbenchPage().removePartListener(junitMono);
            }
        } catch (RuntimeException e) {
            // best-effort removal
        } finally {
            PrefUtil.getInternalPreferenceStore().removePropertyChangeListener(junitMono);
            getPreferenceStore().removePropertyChangeListener(junitMono);
            junitMono = null;
        }
    }

    private void setStructureProviderRegistry() {
        try {
            // JavaEditor.createSourceViewer is final so GroovyEditor cannot override
            // to extend the default folding structure provider, override the provider registry
            ReflectionUtils.setPrivateField(JavaPlugin.class, "fFoldingStructureProviderRegistry", JavaPlugin.getDefault(), new JavaFoldingStructureProviderRegistry() {
                @Override
                public IJavaFoldingStructureProvider getCurrentFoldingProvider() {
                    IJavaFoldingStructureProvider provider = super.getCurrentFoldingProvider();
                    if (provider.getClass().equals(DefaultJavaFoldingStructureProvider.class)) {
                        provider = new GroovyAwareFoldingStructureProvider();
                    }
                    return provider;
                }
            });
        } catch (RuntimeException e) {
            e.printStackTrace();
            // use Java defaults
        }
    }

    public GroovyTextTools getTextTools() {
        return textTools;
    }

    public GroovyOutlineTools getOutlineTools() {
        return outlineTools;
    }

    public void logError(String message, Throwable error) {
        log(IStatus.ERROR, message, error);
    }

    public void logWarning(String message) {
        log(IStatus.WARNING, message, null);
    }

    public void logTraceMessage(String message) {
        log(IStatus.INFO, message, null);
    }

    private void log(int severity, String message, Throwable cause) {
        final IStatus status = new Status(severity, PLUGIN_ID, message, cause);
        getLog().log(status);
    }
}
