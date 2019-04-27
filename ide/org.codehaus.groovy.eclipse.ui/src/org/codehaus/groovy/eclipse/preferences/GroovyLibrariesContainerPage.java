/*
 * Copyright 2009-2019 the original author or authors.
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

import java.text.MessageFormat;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainerInitializer;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.osgi.service.prefs.BackingStoreException;

public class GroovyLibrariesContainerPage extends WizardPage implements IClasspathContainerPage, IClasspathContainerPageExtension {

    private IClasspathEntry containerEntry;
    private IEclipsePreferences prefs;
    private IJavaProject project;
    private boolean isMinimal;
    private String userLibs;

    public GroovyLibrariesContainerPage() {
        super("GroovyLibrariesContainerPage", GroovyClasspathContainer.DESC, JavaPluginImages.DESC_WIZBAN_ADD_LIBRARY); //$NON-NLS-1$
        setDescription(MessageFormat.format(Messages.getString("GroovyLibrariesPreferencesPage.Description"), getTitle())); //$NON-NLS-1$
    }

    @Override
    public void initialize(IJavaProject project, IClasspathEntry[] entries) {
        this.project = project;
        this.prefs = new ProjectScope(project.getProject()).getNode(GroovyCoreActivator.PLUGIN_ID);
        this.userLibs = this.prefs.get(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB, "default"); //$NON-NLS-1$
    }

    @Override
    public void setSelection(IClasspathEntry containerEntry) {
        this.containerEntry = (containerEntry != null ? containerEntry
            : JavaCore.newContainerEntry(GroovyClasspathContainer.CONTAINER_ID));
        this.isMinimal = GroovyClasspathContainer.hasMinimalAttribute(this.containerEntry);
    }

    //--------------------------------------------------------------------------

    @Override
    public void createControl(Composite parent) {
        SelectionListener minimalListener = createSelectionListener(data -> isMinimal = (boolean) data);
        SelectionListener userLibListener = createSelectionListener(data -> userLibs  = (String)  data);

        Composite panel = new Composite(parent, SWT.NONE);
        panel.setFont(parent.getFont());
        panel.setLayout(new GridLayout());
        setControl(panel);

        //
        Group group = createControlGroup(panel);
        group.setText(Messages.getString("GroovyLibrariesPreferencesPage.OsgiLibsGroup")); //$NON-NLS-1$

        Label label = new Label(group, SWT.WRAP);
        label.setText(Messages.getString("GroovyLibrariesPreferencesPage.OsgiLibsLabel")); //$NON-NLS-1$

        Button button = new Button(group, SWT.RADIO);
        button.setData(Boolean.FALSE);
        button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        button.setSelection(!isMinimal);
        button.addSelectionListener(minimalListener);
        button.setText(MessageFormat.format(Messages.getString("GroovyLibrariesPreferencesPage.OsgiLibsChoiceY"), //$NON-NLS-1$
            CompilerUtils.getExtraJarsForClasspath().stream().map(jar -> jar.toFile().getName()).collect(Collectors.joining(", ")))); //$NON-NLS-1$

        button = new Button(group, SWT.RADIO);
        button.setData(Boolean.TRUE);
        button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        button.setSelection(isMinimal);
        button.addSelectionListener(minimalListener);
        button.setText(Messages.getString("GroovyLibrariesPreferencesPage.OsgiLibsChoiceN")); //$NON-NLS-1$

        //
        group = createControlGroup(panel);
        group.setText(Messages.getString("GroovyLibrariesPreferencesPage.UserLibsGroup")); //$NON-NLS-1$

        label = new Label(group, SWT.WRAP);
        label.setText(Messages.getString("GroovyLibrariesPreferencesPage.UserLibsLabel")); //$NON-NLS-1$

        button = new Button(group, SWT.RADIO);
        button.setData("default"); //$NON-NLS-1$
        button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        button.setSelection(button.getData().equals(userLibs));
        button.addSelectionListener(userLibListener);
        button.setText(Messages.getString("GroovyLibrariesPreferencesPage.UserLibsChoiceX")); //$NON-NLS-1$

        button = new Button(group, SWT.RADIO);
        button.setData("true"); //$NON-NLS-1$
        button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        button.setSelection(button.getData().equals(userLibs));
        button.addSelectionListener(userLibListener);
        button.setText(Messages.getString("GroovyLibrariesPreferencesPage.UserLibsChoiceY")); //$NON-NLS-1$

        button = new Button(group, SWT.RADIO);
        button.setData("false"); //$NON-NLS-1$
        button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        button.setSelection(button.getData().equals(userLibs));
        button.addSelectionListener(userLibListener);
        button.setText(Messages.getString("GroovyLibrariesPreferencesPage.UserLibsChoiceN")); //$NON-NLS-1$
    }

    private Group createControlGroup(Composite parent) {
        Group group = new Group(parent, SWT.SHADOW_NONE);
        group.setFont(parent.getFont());
        group.setLayout(new GridLayout());

        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).indent(0, IDialogConstants.VERTICAL_MARGIN).applyTo(group);

        return group;
    }

    private SelectionListener createSelectionListener(Consumer<Object> consumer) {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (((Button) e.widget).getSelection()) {
                    consumer.accept(e.widget.getData());
                }
            }
        };
    }

    @Override
    public boolean finish() {
        try {
            if (!prefs.get(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB, "default").equals(userLibs)) { //$NON-NLS-1$
                prefs.put(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB, userLibs);
                prefs.flush();
            }

            GroovyRuntime.ensureGroovyClasspathContainer(project, isMinimal);
            GroovyClasspathContainerInitializer.updateGroovyClasspathContainer(project);
        } catch (BackingStoreException | JavaModelException e) {
            GroovyPlugin.getDefault().logError(Messages.getString("GroovyLibrariesPreferencesPage.FinishError"), e); //$NON-NLS-1$
        }

        return true;
    }

    @Override
    public IClasspathEntry getSelection() {
        return containerEntry;
    }
}
