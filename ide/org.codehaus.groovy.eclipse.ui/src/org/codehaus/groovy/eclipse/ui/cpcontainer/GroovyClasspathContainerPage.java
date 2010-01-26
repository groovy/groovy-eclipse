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
package org.codehaus.groovy.eclipse.ui.cpcontainer;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainerInitializer;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.osgi.service.prefs.BackingStoreException;

public class GroovyClasspathContainerPage extends NewElementWizardPage
        implements IClasspathContainerPage, IClasspathContainerPageExtension {
    private IJavaProject jProject;
    private IEclipsePreferences prefStore;
    private IClasspathEntry containerEntryResult;
    private Button[] useGroovyLib;
    
    private enum UseGroovyLib { TRUE("true", "Yes"), FALSE("false", "No"), DEFAULT("default", "Use workspace default");
        private final String val;
        private final String label;
        private UseGroovyLib(String val, String label) {
            this.val = val;
            this.label = label;
        }
        String val() {
            return val;
        }
        
        String label() {
            return label;
        }
        
        
        /**
         * @param string
         * @return
         */
        static UseGroovyLib fromString(String val) {
            if (val.equals(TRUE.val)) {
                return TRUE;
            } else if (val.equals(FALSE.val)) {
                return FALSE;
            }
            return DEFAULT;
        }
    }


    public GroovyClasspathContainerPage() {
        super("Groovy Classpath Container");
        setTitle("Groovy Libraries");
        setDescription("Configure the Groovy Library classpath container");
        setImageDescriptor(JavaPluginImages.DESC_WIZBAN_ADD_LIBRARY);
    }

    private UseGroovyLib getPreference() {
        return prefStore != null ? 
                UseGroovyLib.fromString(prefStore.get(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB, "default")) : 
                UseGroovyLib.DEFAULT;
    }

    public boolean finish() {
        try {
            if (prefStore == null) {
                return true;
            }
            
            UseGroovyLib storedPreference = getPreference();
            UseGroovyLib currentPreference = getPreferenceSelection();
            if (storedPreference != currentPreference) {
                prefStore.put(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB,
                        currentPreference.val());
                prefStore.flush();
            }
            // always refresh on finish
            refreshNow();
        } catch (BackingStoreException e) {
            GroovyCore.logException("Exception trying to store Groovy classpath container changes", e);
        } finally {
            GroovyRuntime.addGroovyClasspathContainer(jProject);
        }
        return true;
    }

    private UseGroovyLib getPreferenceSelection() {
        if (useGroovyLib[0].getSelection()) {
            return (UseGroovyLib) useGroovyLib[0].getData();
        } else if (useGroovyLib[1].getSelection()) {
            return (UseGroovyLib) useGroovyLib[1].getData();
        } else if (useGroovyLib[2].getSelection()) {
            return (UseGroovyLib) useGroovyLib[2].getData();
        }
        return UseGroovyLib.DEFAULT;
    }

    /**
     * will be a no-op if continaer not already in project
     */
    private void refreshNow() {
        try {
            if (jProject != null) {
                GroovyClasspathContainerInitializer.updateGroovyClasspathContainer(jProject);
            }
        } catch (JavaModelException e) {
            GroovyCore.logException("Exception trying to store Groovy classpath container changes", e);
        }
    }

    public IClasspathEntry getSelection() {
        return this.containerEntryResult != null ? 
                this.containerEntryResult : 
                    JavaCore.newContainerEntry(GroovyClasspathContainer.CONTAINER_ID);
    }

    public void setSelection(final IClasspathEntry containerEntry) {
        this.containerEntryResult = containerEntry;
    }

    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);

        composite.setLayout(new GridLayout(1, false));
        
        Label should = new Label(composite, SWT.WRAP);
        should.setText("Should jars in the ~/.groovy/lib directory be included on the classpath?");
        
        useGroovyLib = new Button[3];
        for (int i = 0; i < useGroovyLib.length; i++) {
            useGroovyLib[i] = new Button(composite, SWT.RADIO);
            useGroovyLib[i].setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                    false));
            useGroovyLib[i].setSelection(getPreference() == UseGroovyLib.values()[i]);
            useGroovyLib[i].setData(UseGroovyLib.values()[i]);
            useGroovyLib[i].setText(UseGroovyLib.values()[i].label());
            
            if (prefStore == null) {
                // if container not associated with project, then can't change this
                useGroovyLib[i].setEnabled(false);
            }
        }
        
        Label l = new Label(composite, SWT.NONE);
        l.setText("(Affects this project only)\n\n");
        
        l = new Label(composite, SWT.NONE);
        l.setText("Groovy Libraries will automatically be refreshed for this project when 'Finish' is clicked.");

        setControl(composite);
    }

    public void initialize(final IJavaProject project,
            final IClasspathEntry[] currentEntries) {
        if (project == null) {
            return;
        }
        jProject = project;
        prefStore = preferenceStore(jProject.getProject());
    }

    private IEclipsePreferences preferenceStore(IProject p) {
        IScopeContext projectScope = new ProjectScope(p);
        return projectScope
                .getNode(GroovyCoreActivator.PLUGIN_ID);
    }
}
