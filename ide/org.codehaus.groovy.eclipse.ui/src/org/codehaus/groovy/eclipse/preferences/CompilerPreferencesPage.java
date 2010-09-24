package org.codehaus.groovy.eclipse.preferences;

import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainerInitializer;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.actions.OpenWorkspaceAction;

public class CompilerPreferencesPage extends PreferencePage implements
        IWorkbenchPreferencePage {
    private static final String PROP_VM = "eclipse.vm"; //$NON-NLS-1$

    private static final String PROP_VMARGS = "eclipse.vmargs"; //$NON-NLS-1$

    private static final String PROP_REFRESH_BUNDLES = "-Declipse.refreshBundles=true";

    private static final String PROP_COMMANDS = "eclipse.commands"; //$NON-NLS-1$

    private static final String PROP_EXIT_CODE = "eclipse.exitcode"; //$NON-NLS-1$

    private static final String PROP_EXIT_DATA = "eclipse.exitdata"; //$NON-NLS-1$

    private static final String CMD_VMARGS = "-vmargs"; //$NON-NLS-1$

    private static final String NEW_LINE = "\n"; //$NON-NLS-1$

    protected final boolean isGroovy17Disabled;

    private Button groovyLibButt;

    // FIXADE: uncomment usage when script folders are handled by the compiler
    // private ScriptFolderSelector scriptFolderSelector;

    public CompilerPreferencesPage() {
        super("Compiler");
        setPreferenceStore(GroovyPlugin.getDefault().getPreferenceStore());
        isGroovy17Disabled = CompilerUtils.isGroovy17DisabledOrMissing();
    }


    @Override
    protected Control createContents(Composite parent) {
        Composite page = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        page.setLayout(layout);
        page.setFont(parent.getFont());


        // section on Groovy classpath container
        Label gccLabel = new Label(page, SWT.WRAP);
        gccLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        gccLabel.setText("Groovy Classpath Container:");

        Composite gccPage = new Composite(page, SWT.NONE | SWT.BORDER);
        layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginHeight = 3;
        layout.marginWidth = 3;
        gccPage.setLayout(layout);
        gccPage.setFont(parent.getFont());
        gccPage.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        groovyLibButt = new Button(gccPage, SWT.CHECK);
        groovyLibButt.setText("Include all jars in ~/.groovy/lib on the classpath.");
        groovyLibButt.setSelection(GroovyCoreActivator.getDefault().getPreference(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, true));

        GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        gd.widthHint = 500;

        Label groovyLibLabel = new Label(gccPage, SWT.WRAP);
        groovyLibLabel.setText("This is the default setting and individual projects can be configured "
                + "by clicking on the properties page of the Groovy Support classpath container.");
        groovyLibLabel.setLayoutData(gd);

        Label classpathLabel = new Label(gccPage, SWT.WRAP);
        classpathLabel.setText("\nReset the Groovy Classpath Containers.");
        Button updateGCC = new Button(gccPage, SWT.PUSH);
        updateGCC.setText("Update all Groovy Classpath Containers");
        updateGCC.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                updateClasspathContainers();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                updateClasspathContainers();
            }
        });
        Label classpathLabel2 = new Label(gccPage, SWT.WRAP);
        classpathLabel2.setText("Perform this action if there are changes to ~/.groovy/lib "
                + "that should be reflected in your projects' classpaths.");
        classpathLabel2.setLayoutData(gd);

        // scriptFolderSelector = new ScriptFolderSelector(page);
        // scriptFolderSelector.createListContents();

        Label compilerVersion = new Label(page, SWT.LEFT | SWT.WRAP);
        compilerVersion.setText("You are currently using Groovy Compiler version " + CompilerUtils.getGroovyVersion() + ".");

        Link moreInfoLink = new Link(page, SWT.BORDER);
        moreInfoLink.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false));
        moreInfoLink
                .setText("<a href=\"http://docs.codehaus.org/display/GROOVY/Compiler+Switching+within+Groovy-Eclipse\">Information on how to switch to "
                        + CompilerUtils.getOtherVersion() + "...</a>");
        moreInfoLink.addListener (SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                openUrl(event.text);
            }
        });
        return page;
    }

    /**
     * borrowed from {@link OpenWorkspaceAction}
     */
    protected void restart(Shell shell) {
        String command_line = buildCommandLine(shell);
        if (command_line == null) {
            return;
        }

        System.setProperty(PROP_EXIT_CODE, Integer.toString(24));
        System.setProperty(PROP_EXIT_DATA, command_line);
        System.out.println("Restart command line begin:\n " + command_line);
        System.out.println("Restart command line end");
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
    private String buildCommandLine(Shell shell) {
        String property = System.getProperty(PROP_VM);
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
        String vmargs = System.getProperty(PROP_VMARGS);
        vmargs = vmargs == null ?
                PROP_REFRESH_BUNDLES + NEW_LINE :
                    vmargs + NEW_LINE + PROP_REFRESH_BUNDLES + NEW_LINE;
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


    public void init(IWorkbench workbench) {

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

    private void updateClasspathContainers() {
        try {
            GroovyClasspathContainerInitializer.updateAllGroovyClasspathContainers();
        } catch (JavaModelException e) {
            GroovyCore.logException("Problem updating Groovy classpath contianers", e);
        }

    }

    @Override
    public boolean performOk() {
        applyPreferences();
        return super.performOk();
    }
    @Override
    public void performApply() {
        applyPreferences();
        super.performApply();
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        GroovyCoreActivator.getDefault().setPreference(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, true);
        // scriptFolderSelector.restoreDefaultsPressed();
    }

    private void applyPreferences() {
        GroovyCoreActivator.getDefault().setPreference(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, groovyLibButt.getSelection());
        // scriptFolderSelector.applyPreferences();
    }


}
