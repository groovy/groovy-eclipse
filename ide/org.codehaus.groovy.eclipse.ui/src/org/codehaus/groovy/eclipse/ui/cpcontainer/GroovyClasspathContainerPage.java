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

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.groovy.eclipse.core.util.ListUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class GroovyClasspathContainerPage extends NewElementWizardPage
        implements IClasspathContainerPage, IClasspathContainerPageExtension {
    private IJavaProject jProject;
    private IPersistentPreferenceStore prefStore;

    private IClasspathEntry containerEntryResult;

    private Combo versionCombo;

    public GroovyClasspathContainerPage() {
        this("Groovy Classpath Container");
    }

    public GroovyClasspathContainerPage(final String pageName) {
        super(pageName);
    }

    private String getPreference() {
        return prefStore != null ? 
                prefStore.getString(PreferenceConstants.GROOVY_RUNTIME_SOURCE) : 
                    null;
    }

    public boolean finish() {
        try {
            if (prefStore == null)
                return false;
            final String preference = getPreference();
            if (versionCombo.getSelectionIndex() == 0) {
                if (preference == null || preference.trim().length() == 0) {
                    return true;
                }
                prefStore.setToDefault(
                        PreferenceConstants.GROOVY_RUNTIME_SOURCE);
                prefStore.save();
                return true;
            }
            if (nullEquals(preference, "GROOVY_HOME"))
                return true;
            prefStore.setValue(PreferenceConstants.GROOVY_RUNTIME_SOURCE,
                    "GROOVY_HOME");
            prefStore.save();
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            GroovyRuntime.addGroovyClasspathContainer(jProject);
        }
        return true;
    }

    public IClasspathEntry getSelection() {
        return this.containerEntryResult;
    }

    public void setSelection(final IClasspathEntry containerEntry) {
        this.containerEntryResult = containerEntry;
    }

    protected void doSelectionChanged() {}

    public void createControl(final Composite parent) {
        final PixelConverter converter = new PixelConverter(parent);
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());

        composite.setLayout(new GridLayout(2, false));

        Label label = new Label(composite, SWT.NONE);
        label.setFont(composite.getFont());
        label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false,
                false, 1, 1));
        label.setText("Select Groovy Runtime:");
        versionCombo = new Combo(composite, SWT.READ_ONLY);
        final List<String> options = ListUtil.newEmptyList();
        URL groovyURL = CompilerUtils.getExportedGroovyAllJar();
        IPath runtimeJarPath = new Path(groovyURL.getPath());
        options.add("Plugin Embedded Lib: " + runtimeJarPath.lastSegment());
        versionCombo.setItems(options.toArray(new String[0]));
        versionCombo.setFont(composite.getFont());
        String pref = getPreference();
        if (pref != null || pref.trim().length() > 0)
            versionCombo.select(1);
        final GridData data = new GridData(GridData.BEGINNING, GridData.CENTER,
                false, false, 1, 1);
        data.widthHint = converter.convertWidthInCharsToPixels(45);
        versionCombo.setLayoutData(data);
        versionCombo.select(0);
        versionCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                doSelectionChanged();
            }
        });

        doSelectionChanged();

        setControl(composite);
    }

    public void initialize(final IJavaProject project,
            final IClasspathEntry[] currentEntries) {
        jProject = project;
        prefStore = preferenceStore(jProject.getProject());
    }

    private IPersistentPreferenceStore preferenceStore(IProject p) {
        return new ScopedPreferenceStore( 
                new ProjectScope(p), 
                "org.codehaus.groovy.eclipse.preferences" );
    }

    public boolean nullEquals(Object object1, Object object2) {
        if (object1 == object2) {
            return true;
        }
        if ((object1 == null) || (object2 == null)) {
            return false;
        }
        return object1.equals(object2);
    }

}
