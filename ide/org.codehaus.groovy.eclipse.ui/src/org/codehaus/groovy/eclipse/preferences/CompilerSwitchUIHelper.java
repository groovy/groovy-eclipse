/*
 * Copyright 2009-2025 the original author or authors.
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
package org.codehaus.groovy.eclipse.preferences;

import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.frameworkadapter.util.SpecifiedVersion;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;

/**
 * Shared functionality to provide a UI for compiler switching.
 */
public class CompilerSwitchUIHelper {

    /**
     * Main entry point to generate UI for compiler switching.
     */
    public static Composite createCompilerSwitchBlock(final Composite parent) {
        Composite compilerPanel = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.marginBottom = 3;
        layout.marginHeight = 0;
        layout.marginWidth  = 0;
        layout.numColumns   = 1;
        compilerPanel.setLayout(layout);
        compilerPanel.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

        Label compilerVersion = new Label(compilerPanel, SWT.LEFT);
        compilerVersion.setText("You are currently using Groovy Compiler version " + CompilerUtils.getGroovyVersion());

        /**/ SpecifiedVersion activeGroovyVersion = CompilerUtils.getActiveGroovyVersion();
        for (SpecifiedVersion version : SpecifiedVersion.values()) {
            if (activeGroovyVersion != version) {
                var bundleVersion = CompilerUtils.getBundleVersion(version);
                if (bundleVersion != null) {
                    Button switchTo = new Button(compilerPanel, SWT.PUSH);
                    switchTo.addSelectionListener(switchTo(version));
                    switchTo.setText("Switch to " + bundleVersion);
                }
            }
        }

        Link moreInfoLink = new Link(compilerPanel, 0);
        moreInfoLink.addSelectionListener(SelectionListener.widgetSelectedAdapter((event) -> openUrl(event.text)));
        moreInfoLink.setText("<a href=\"https://github.com/groovy/groovy-eclipse/wiki\">See here</a> for more information (opens a browser window).");

        return compilerPanel;
    }

    private static SelectionListener switchTo(final SpecifiedVersion version) {
        return SelectionListener.widgetSelectedAdapter((event) -> {
            Shell shell = ((Button) event.getSource()).getShell();
            boolean result = MessageDialog.openQuestion(shell,
                "Change compiler and restart?",
                "Do you want to change the compiler?\n\n" +
                "If you select \"Yes\", the compiler will be changed and Eclipse will be restarted.\n\n" +
                "Make sure all your work is saved before clicking \"Yes\".");
            if (result) {
                IStatus status = CompilerUtils.switchVersions(CompilerUtils.getActiveGroovyVersion(), version);
                if (status == Status.OK_STATUS) {
                    restart(shell);
                } else {
                    String errorString = "Error occurred when trying to enable Groovy " + version.toReadableVersionString();
                    ErrorDialog dialog = new ErrorDialog(shell, "Error occurred", errorString, status, IStatus.ERROR);
                    dialog.open();
                }
            }
        });
    }

    private static void openUrl(final String location) {
        try {
            URL url = null;

            if (location != null) {
                url = new URL(location);
            }

            if (WebBrowserPreference.getBrowserChoice() == WebBrowserPreference.EXTERNAL) {
                try {
                    IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
                    support.getExternalBrowser().openURL(url);
                } catch (Exception e) {
                    GroovyCore.logException("Could not open browser", e);
                }
            } else {
                IWebBrowser browser = null;
                int flags = 0;
                if (WorkbenchBrowserSupport.getInstance().isInternalWebBrowserAvailable()) {
                    flags |= IWorkbenchBrowserSupport.AS_EDITOR | IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR;
                } else {
                    flags |= IWorkbenchBrowserSupport.AS_EXTERNAL | IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR;
                }

                browser = WorkbenchBrowserSupport.getInstance().createBrowser(flags, "org.eclipse.contribution.weaving.jdt", null, null);
                browser.openURL(url);
            }
        } catch (PartInitException e) {
            MessageDialog.openError(Display.getDefault().getActiveShell(), "Browser initialization error", "Browser could not be initiated");
        } catch (MalformedURLException e) {
            MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Malformed URL", location);
        }
    }

    //--------------------------------------------------------------------------

    private static final String PROP_VM = "eclipse.vm"; //$NON-NLS-1$

    private static final String PROP_VMARGS = "eclipse.vmargs"; //$NON-NLS-1$

    private static final String PROP_REFRESH_BUNDLES = "-Declipse.refreshBundles=true";

    private static final String PROP_CLEAN = "-Dosgi.clean=true"; //$NON-NLS-1$

    private static final String PROP_COMMANDS = "eclipse.commands"; //$NON-NLS-1$

    private static final String PROP_EXIT_CODE = "eclipse.exitcode"; //$NON-NLS-1$

    private static final String PROP_EXIT_DATA = "eclipse.exitdata"; //$NON-NLS-1$

    private static final String CMD_VMARGS = "-vmargs"; //$NON-NLS-1$

    private static final String NEW_LINE = "\n"; //$NON-NLS-1$

    /**
     * Borrowed from {@link OpenWorkspaceAction}.
     */
    private static void restart(final Shell shell) {
        String commandLine = buildCommandLine(shell);
        if (commandLine == null) {
            return;
        }

        System.out.println("Restart command line begin:\n " + commandLine);
        System.out.println("Restart command line end");
        System.setProperty(PROP_EXIT_DATA, commandLine);
        System.setProperty(PROP_EXIT_CODE, Integer.toString(24));
        Workbench.getInstance().restart();
    }

    /**
     * Creates and return a string with command line options for eclipse.exe that
     * will launch a new workbench that is the same as the currently running
     * one, but using the argument directory as its workspace.
     *
     * @param workspace
     *            the directory to use as the new workspace
     * @return a string of command line options or null on error
     */
    private static String buildCommandLine(final Shell shell) {
        String property = System.getProperty(PROP_VM);
        if (property == null) {
            MessageDialog.openError(shell, "Missing System Property",
                NLS.bind("Unable to relaunch the platform because the {0} property has not been set.", PROP_VM));
            return null;
        }

        StringBuffer result = new StringBuffer(512);
        result.append(property);
        result.append(NEW_LINE);

        // append the vmargs and commands. Assume that these already end in \n
        String vmargs = System.getProperty(PROP_VMARGS, "");
        vmargs = vmargs + NEW_LINE + PROP_REFRESH_BUNDLES + NEW_LINE + PROP_CLEAN + NEW_LINE;
        result.append(vmargs);

        // append the rest of the args, replacing or adding -data as required
        property = System.getProperty(PROP_COMMANDS);
        if (property != null) {
            result.append(property);
        }

        // put the vmargs back at the very end (the eclipse.commands property
        // already contains the -vm arg)
        if (vmargs != null) {
            result.append(CMD_VMARGS);
            result.append(NEW_LINE);
            result.append(vmargs);
        }

        return result.toString();
    }
}
