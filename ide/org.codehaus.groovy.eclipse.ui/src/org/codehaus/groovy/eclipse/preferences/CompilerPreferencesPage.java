package org.codehaus.groovy.eclipse.preferences;


import java.util.SortedSet;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainerInitializer;
import org.codehaus.groovy.eclipse.core.compiler.CompilerCheckerParticipant;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.groovy.frameworkadapter.util.SpecifiedVersion;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;

public class CompilerPreferencesPage extends PropertyAndPreferencePage implements
IWorkbenchPreferencePage, IWorkbenchPropertyPage {
    public static final String PROPERTY_ID = Activator.USING_PROJECT_PROPERTIES;

    public static final String PREFERENCES_ID = "org.codehaus.groovy.eclipse.preferences.compiler";

    protected final SpecifiedVersion activeGroovyVersion;

    protected SpecifiedVersion currentProjectVersion;

    private Button groovyLibButt;

    private ScriptFolderSelectorPreferences scriptFolderSelector;

    private IEclipsePreferences preferences;

    private ComboViewer compilerCombo;

    private Button doCheckForCompilerMismatch;

    public CompilerPreferencesPage() {
        super();
        activeGroovyVersion = CompilerUtils.getActiveGroovyVersion();
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        IProject project = getProject();
        ScopedPreferenceStore store;
        IScopeContext scope;
        if (project == null) {
            // workspace settings
            scope = InstanceScope.INSTANCE;
        } else {
            // project settings
            scope = new ProjectScope(project);
        }
        preferences = scope.getNode(Activator.PLUGIN_ID);
        store = new ScopedPreferenceStore(scope, Activator.PLUGIN_ID);
        return store;
    }

    public IEclipsePreferences getPreferences() {
        if (preferences == null) {
            doGetPreferenceStore();
        }
        return preferences;
    }


    @Override
    protected Label createDescriptionLabel(Composite parent) {
        if (isProjectPreferencePage()) {
            Composite body = new Composite(parent, SWT.NONE);
            GridLayout layout= new GridLayout();
            layout.marginHeight= 0;
            layout.marginWidth= 0;
            layout.numColumns = 2;
            body.setLayout(layout);
            body.setFont(parent.getFont());

            GridData data = new GridData(GridData.FILL, GridData.FILL, true, true);
            body.setLayoutData(data);
            createProjectCompilerSection(body);
        }
        return super.createDescriptionLabel(parent);
    }

    @Override
    protected Control createPreferenceContent(Composite parent) {
        final Composite page = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        page.setLayout(layout);
        page.setFont(parent.getFont());

        if (getElement() == null) {
            createWorkspaceCompilerSection(page);
        }

        // Groovy script folder
        scriptFolderSelector = new ScriptFolderSelectorPreferences(page, getPreferences(), getPreferenceStore(), getProject());
        scriptFolderSelector.createListContents();

        if (getElement() == null) {
            // Only for the workspace version
            // Groovy classpath container
            createClasspathContainerSection(page);
        }

        return page;
    }

    /**
     * @param parent
     * @param page
     */
    protected void createClasspathContainerSection(final Composite page) {
        Label gccLabel = new Label(page, SWT.WRAP);
        gccLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        gccLabel.setText("Groovy Classpath Container:");
        gccLabel.setFont(getBoldFont(page));

        Composite gccPage = new Composite(page, SWT.NONE | SWT.BORDER);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginHeight = 3;
        layout.marginWidth = 3;
        gccPage.setLayout(layout);
        gccPage.setFont(page.getFont());
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
    }

    protected void createProjectCompilerSection(final Composite page) {
        Label compilerLabel = new Label(page, SWT.WRAP);
        compilerLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        compilerLabel.setText("Groovy compiler level for project " + getProject().getName() + ":");
        compilerLabel.setFont(getBoldFont(page));
        compilerCombo = new ComboViewer(new Combo(page, SWT.DROP_DOWN | SWT.READ_ONLY));
        compilerCombo.setLabelProvider(new LabelProvider(){
            @Override
            public String getText(Object element) {
                return element instanceof SpecifiedVersion ? ((SpecifiedVersion) element).toReadableVersionString() : "";
            }
        });
        SortedSet<SpecifiedVersion> versions = CompilerUtils.getAllGroovyVersions();
        compilerCombo.add(SpecifiedVersion.DONT_CARE);
        for (SpecifiedVersion version : versions) {
            compilerCombo.add(version);
        }
        currentProjectVersion = CompilerUtils.getCompilerLevel(getProject());

        Label explainLabel = new Label(page, SWT.WRAP);
        explainLabel.setText("If the project compiler level does not match the workspace compiler level,\n" +
                "there will be a build error placed on the project.");
        GridData data = new GridData();
        data.horizontalSpan = 2;
        data.grabExcessHorizontalSpace = false;
        explainLabel.setLayoutData(data);
        setToProjectVersion();
    }

    private void setToProjectVersion() {
        compilerCombo.setSelection(new StructuredSelection(currentProjectVersion), true);
    }

    protected void createWorkspaceCompilerSection(final Composite page) {
        Label compilerLabel = new Label(page, SWT.WRAP);
        compilerLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        compilerLabel.setText("Groovy Compiler settings:");
        compilerLabel.setFont(getBoldFont(page));

        Composite compilerPage = new Composite(page, SWT.NONE | SWT.BORDER);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginHeight = 3;
        layout.marginWidth = 3;
        compilerPage.setLayout(layout);
        compilerPage.setFont(page.getFont());
        compilerPage.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        CompilerSwitchUIHelper.createCompilerSwitchBlock(compilerPage);

        doCheckForCompilerMismatch = new Button(compilerPage, SWT.CHECK);
        doCheckForCompilerMismatch.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        doCheckForCompilerMismatch.setText("Enable checking for mismatches between the project and workspace Groovy compiler levels");
        doCheckForCompilerMismatch.setSelection(getPreferences().getBoolean(Activator.GROOVY_CHECK_FOR_COMPILER_MISMATCH, true));
    }



    /**
     * @param page
     * @return
     */
    private Font getBoldFont(Composite page) {
        return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
    }

    @Override
    public void init(IWorkbench workbench) {}

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
        super.performApply();
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        if (getProject() == null) {
            GroovyCoreActivator.getDefault().setPreference(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, true);
        } else {
            enableProjectSpecificSettings(false);
        }
        scriptFolderSelector.restoreDefaultsPressed();
        if (compilerCombo != null) {
            setToProjectVersion();
        }

        if (doCheckForCompilerMismatch != null) {
            doCheckForCompilerMismatch.setSelection(true);
            getPreferences().putBoolean(Activator.GROOVY_CHECK_FOR_COMPILER_MISMATCH, true);
        }
    }

    private void applyPreferences() {
        if (getProject() == null) {
            GroovyCoreActivator.getDefault().setPreference(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, groovyLibButt.getSelection());
        } else {
            getPreferenceStore().setValue(PROPERTY_ID, useProjectSettings());
        }
        scriptFolderSelector.applyPreferences();

        if (doCheckForCompilerMismatch != null) {
            boolean isSelected = doCheckForCompilerMismatch.getSelection();
            boolean currentPref = getPreferences().getBoolean(Activator.GROOVY_CHECK_FOR_COMPILER_MISMATCH, true);
            if (!isSelected && currentPref) {
                // delete all markers in the workspace
                try {
                    ResourcesPlugin.getWorkspace().getRoot().deleteMarkers(CompilerCheckerParticipant.COMPILER_MISMATCH_PROBLEM, true, IResource.DEPTH_ONE);
                } catch (CoreException e) {
                    GroovyCore.logException("Error deleting markers", e);
                }
            }
            if (isSelected != currentPref) {
                getPreferences().putBoolean(Activator.GROOVY_CHECK_FOR_COMPILER_MISMATCH, isSelected);
                try {
                    getPreferences().flush();
                } catch (BackingStoreException e) {
                    GroovyCore.logException("Error saving compiler preferences", e);
                }
            }
        }

        if (compilerCombo != null) {
            StructuredSelection selection = (StructuredSelection) compilerCombo.getSelection();
            SpecifiedVersion selected = (SpecifiedVersion) selection.getFirstElement();
            if (selected == null) {
                selected = SpecifiedVersion.UNSPECIFIED;
            }
            if (selected != currentProjectVersion && selected != SpecifiedVersion.UNSPECIFIED) {
                CompilerUtils.setCompilerLevel(getProject(), selected, true);
            }
        }
    }

    @Override
    protected boolean hasProjectSpecificOptions(IProject project) {
        if (project != null && project.equals(getProject())) {
            return getPreferenceStore().getBoolean(PROPERTY_ID);
        }
        return false;
    }

    @Override
    protected String getPreferencePageID() {
        return PREFERENCES_ID;
    }

    @Override
    protected String getPropertyPageID() {
        return PROPERTY_ID;
    }

}
