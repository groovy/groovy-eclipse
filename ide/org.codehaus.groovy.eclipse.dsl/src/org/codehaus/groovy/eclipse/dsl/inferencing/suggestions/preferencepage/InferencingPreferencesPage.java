/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.preferencepage;

import java.util.List;

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.InferencingSuggestionsManager;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-04-20
 */
public class InferencingPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {
    private IWorkbenchPage page;

    private GroovySuggestionsTable table;

    public static final String PAGE_DESCRIPTION = "Select a project to manage the Groovy inferencing suggestions.";

    public InferencingPreferencesPage() {
        setDescription(PAGE_DESCRIPTION);
    }

    public void init(IWorkbench workbench) {
        if (workbench != null && workbench.getActiveWorkbenchWindow() != null) {
            page = workbench.getActiveWorkbenchWindow().getActivePage();
        }
    }

    protected IWorkbenchPage getPage() {
        return page;
    }

    protected Control createContents(Composite parent) {
        List<IProject> projects = GroovyNature.getAllAccessibleGroovyProjects();
        table = new GroovySuggestionsTable(projects);
        return table.createTable(parent);
    }

    public boolean performOk() {

        if (super.performOk()) {
            IProject project = table.getSelectedProject();
            InferencingSuggestionsManager.getInstance().commitChanges(project);
            return true;
        }
        return false;
    }

    public boolean performCancel() {
        IProject project = table.getSelectedProject();
        InferencingSuggestionsManager.getInstance().restoreSuggestions(project);

        return super.performCancel();
    }

}
