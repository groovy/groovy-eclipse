/*
 * Copyright 2003-2010 the original author or authors.
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
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.actions.OpenWorkspaceAction;
import org.osgi.framework.Version;

/**
 * Shared functionality to provide
 * a UI for compiler switching
 * @author andrew
 * @created 2013-01-04
 */
public class CompilerSwitchUIHelper {
    static final String PROP_VM = "eclipse.vm"; //$NON-NLS-1$

    static final String PROP_VMARGS = "eclipse.vmargs"; //$NON-NLS-1$

    static final String PROP_REFRESH_BUNDLES = "-Declipse.refreshBundles=true";

    static final String PROP_CLEAN = "-Dosgi.clean=true"; //$NON-NLS-1$

    static final String PROP_COMMANDS = "eclipse.commands"; //$NON-NLS-1$

    private static final String PROP_EXIT_CODE = "eclipse.exitcode"; //$NON-NLS-1$

    private static final String PROP_EXIT_DATA = "eclipse.exitdata"; //$NON-NLS-1$

    static final String CMD_VMARGS = "-vmargs"; //$NON-NLS-1$


    static final String NEW_LINE = "\n"; //$NON-NLS-1$


    /**
     * Main entry point to generate UI for compiler switching
     * @param compilerPage
     */
    public static Composite createCompilerSwitchBlock(Composite parent) {
        Composite compilerPage = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginHeight = 3;
        layout.marginWidth = 3;
        compilerPage.setLayout(layout);

        SpecifiedVersion activeGroovyVersion = CompilerUtils.getActiveGroovyVersion();
        Label compilerVersion = new Label(compilerPage, SWT.LEFT | SWT.WRAP);
        compilerVersion.setText("You are currently using Groovy Compiler version " + CompilerUtils.getGroovyVersion() + ".");

        for (SpecifiedVersion version : SpecifiedVersion.values()) {
            if (activeGroovyVersion != version) {
                switchVersion(version, compilerPage);
            }
        }

        Link moreInfoLink = new Link(compilerPage, 0);
        moreInfoLink.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false));
        moreInfoLink.setText("<a href=\"http://docs.codehaus.org/display/GROOVY/Compiler+Switching+within+Groovy-Eclipse\">See here</a> for more information "
                + "on compiler switching (opens a browser window).");
        moreInfoLink.addListener (SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                openUrl(event.text);
            }
        });

        return compilerPage;
    }

    /**
     * Provides UI for switching compiler between versions
     * @param toVersion
     */
    private static void switchVersion(final SpecifiedVersion toSpecifiedVersion, final Composite compilerPage) {
        final Version toVersion = CompilerUtils.getBundleVersion(toSpecifiedVersion);
        if (toVersion == null) {
            // this version is not installed
            return;
        }

        Button switchTo = new Button(compilerPage, SWT.PUSH);
        switchTo.setText("Switch to " + toVersion);
        switchTo.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                Shell shell = compilerPage.getShell();
                boolean result = MessageDialog.openQuestion(shell, "Change compiler and restart?",
                        "Do you want to change the compiler?\n\nIf you select \"Yes\"," +
                                " the compiler will be changed and Eclipse will be restarted.\n\n" +
                        "Make sure all your work is saved before clicking \"Yes\".");

                if (result) {
                    // change compiler
                    SpecifiedVersion activeGroovyVersion = CompilerUtils.getActiveGroovyVersion();
                    IStatus status = CompilerUtils.switchVersions(activeGroovyVersion, toSpecifiedVersion);
                    if (status == Status.OK_STATUS) {
                        restart(shell);
                    } else {
                        ErrorDialog error = new ErrorDialog(shell,
                                "Error occurred", "Error occurred when trying to enable Groovy " +
                                        toVersion,
                                        status, IStatus.ERROR);
                        error.open();
                    }
                }
            }

            public void widgetDefaultSelected(SelectionEvent e) {}
        });
    }

    /**
     * borrowed from {@link OpenWorkspaceAction}
     */
    protected static void restart(Shell shell) {
        String command_line = buildCommandLine(shell);
        if (command_line == null) {
            return;
        }

        System.out.println("Restart command line begin:\n " + command_line);
        System.out.println("Restart command line end");
        System.setProperty(PROP_EXIT_DATA, command_line);
        System.setProperty(PROP_EXIT_CODE, Integer.toString(24));
        Workbench.getInstance().restart();

    }

    /**
     * Create and return a string with command line options for eclipse.exe that
     * will launch a new workbench that is the same as the currently running
     * one, but using the argument directory as its workspace.
     *
     * @param workspace
     *            the directory to use as the new workspace
     * @return a string of command line options or null on error
     */
    private static String buildCommandLine(Shell shell) {
        String property = FrameworkProperties.getProperty(PROP_VM);
        if (property == null) {
            MessageDialog
            .openError(
                    shell,
                    IDEWorkbenchMessages.OpenWorkspaceAction_errorTitle,
                    NLS.bind(IDEWorkbenchMessages.OpenWorkspaceAction_errorMessage,
                            PROP_VM));
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

    public static void openUrl(String location) {
        try {
            URL url = null;

            if (location != null) {
                url = new URL(location);
            }

            if (WebBrowserPreference.getBrowserChoice() == WebBrowserPreference.EXTERNAL) {
                try {
                    IWorkbenchBrowserSupport support = PlatformUI
                            .getWorkbench().getBrowserSupport();
                    support.getExternalBrowser().openURL(url);
                } catch (Exception e) {
                    GroovyCore.logException("Could not open browser", e);
                }
            } else {
                IWebBrowser browser = null;
                int flags = 0;
                if (WorkbenchBrowserSupport.getInstance()
                        .isInternalWebBrowserAvailable()) {
                    flags |= IWorkbenchBrowserSupport.AS_EDITOR
                            | IWorkbenchBrowserSupport.LOCATION_BAR
                            | IWorkbenchBrowserSupport.NAVIGATION_BAR;
                } else {
                    flags |= IWorkbenchBrowserSupport.AS_EXTERNAL
                            | IWorkbenchBrowserSupport.LOCATION_BAR
                            | IWorkbenchBrowserSupport.NAVIGATION_BAR;
                }

                String id = "org.eclipse.contribution.weaving.jdt";
                browser = WorkbenchBrowserSupport.getInstance().createBrowser(
                        flags, id, null, null);
                browser.openURL(url);
            }
        } catch (PartInitException e) {
            MessageDialog.openError(Display.getDefault().getActiveShell(),
                    "Browser initialization error",
                    "Browser could not be initiated");
        } catch (MalformedURLException e) {
            MessageDialog.openInformation(Display.getDefault()
                    .getActiveShell(), "Malformed URL",
                    location);
        }
    }


}
