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
package org.codehaus.groovy.eclipse.ui.cpcontainer;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.eclipse.core.ClasspathVariableInitializer;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.groovy.eclipse.core.util.ListUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
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
                if (StringUtils.isBlank(preference)) {
                    return true;
                }
                prefStore.setToDefault(
                        PreferenceConstants.GROOVY_RUNTIME_SOURCE);
                prefStore.save();
                return true;
            }
            if (StringUtils.equals(preference, "GROOVY_HOME"))
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
        final IPath embeddedJar = ClasspathVariableInitializer
                .getEmbeddedJar(GroovyCore.getEmbeddedGroovyRuntimeHome());
        options.add("Plugin Embedded Lib: " + embeddedJar.lastSegment());
        final IPath runtimePath = ClasspathVariableInitializer
                .getCPVarEmbeddablePath();
        if (!ObjectUtils.equals(GroovyCore.getEmbeddedGroovyRuntimeHome(),
                runtimePath)) {
            final IPath groovyHomeJar = ClasspathVariableInitializer
                    .getEmbeddedJar(runtimePath);
            if (groovyHomeJar != null && groovyHomeJar.toFile().exists())
                options.add("$GROOVY_HOME/" + groovyHomeJar.lastSegment());
        }
        versionCombo.setItems(options.toArray(new String[0]));
        versionCombo.setFont(composite.getFont());
        if (StringUtils.isNotBlank(getPreference()))
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


}
