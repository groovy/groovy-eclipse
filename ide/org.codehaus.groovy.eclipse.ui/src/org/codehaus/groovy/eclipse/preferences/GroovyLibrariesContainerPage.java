/*
 * Copyright 2009-2020 the original author or authors.
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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.launching.JavaRuntime;
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

public class GroovyLibrariesContainerPage extends WizardPage implements IClasspathContainerPage, IClasspathContainerPageExtension {

    private IClasspathEntry containerEntry;
    private IJavaProject javaProject;
    private boolean isMinimal;
    private String userLibs;

    public GroovyLibrariesContainerPage() {
        super("GroovyLibrariesContainerPage", GroovyClasspathContainer.NAME, JavaPluginImages.DESC_WIZBAN_ADD_LIBRARY); //$NON-NLS-1$
        setDescription(MessageFormat.format(Messages.getString("GroovyLibrariesPreferencesPage.Description"), getTitle())); //$NON-NLS-1$
    }

    @Override
    public void initialize(final IJavaProject javaProject, final IClasspathEntry[] entries) {
        this.userLibs = GroovyClasspathContainer.getLegacyUserLibsPreference(this.javaProject = javaProject);
    }

    @Override
    public void setSelection(final IClasspathEntry containerEntry) {
        this.isMinimal = Optional.ofNullable(containerEntry).filter(GroovyClasspathContainer::hasMinimalAttribute).isPresent();
        if (!this.isMinimal && containerEntry != null) {
            if (containerEntry.getPath().lastSegment().equals("user-libs=true")) {
                this.userLibs = "true";
            } else if (containerEntry.getPath().lastSegment().equals("user-libs=false")) {
                this.userLibs = "false";
            }
        }
    }

    //--------------------------------------------------------------------------

    @Override
    public void createControl(final Composite parent) {
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

    private Group createControlGroup(final Composite parent) {
        Group group = new Group(parent, SWT.SHADOW_NONE);
        group.setFont(parent.getFont());
        group.setLayout(new GridLayout());

        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).indent(0, IDialogConstants.VERTICAL_MARGIN).applyTo(group);

        return group;
    }

    private SelectionListener createSelectionListener(final Consumer<Object> consumer) {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                if (((Button) event.widget).getSelection()) {
                    consumer.accept(event.widget.getData());
                }
            }
        };
    }

    @Override
    public boolean finish() {
        containerEntry = GroovyRuntime.newGroovyClasspathContainerEntry(isMinimal,
            JavaRuntime.isModularProject(javaProject), "default".equals(userLibs) ? null : Boolean.valueOf(userLibs));

        return true;
    }

    @Override
    public IClasspathEntry getSelection() {
        return containerEntry;
    }
}
