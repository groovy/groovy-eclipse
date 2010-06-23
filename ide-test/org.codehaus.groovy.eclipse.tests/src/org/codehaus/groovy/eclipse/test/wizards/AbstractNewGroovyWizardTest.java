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
package org.codehaus.groovy.eclipse.test.wizards;

import java.util.Hashtable;

import junit.framework.TestCase;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.test.TestProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 *
 * @author ns
 * @created May 18, 2010
 */
public abstract class AbstractNewGroovyWizardTest extends TestCase {

    protected TestProject fProject;

    protected IJavaProject fJProject;

    protected IPackageFragmentRoot fSourceFolder;

    protected AbstractNewGroovyWizardTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        Hashtable<String, String> options = JavaCore.getDefaultOptions();
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
        JavaCore.setOptions(options);

        IPreferenceStore store = JavaPlugin.getDefault().getPreferenceStore();
        store.setValue(PreferenceConstants.CODEGEN_ADD_COMMENTS, false);
        store.setValue(PreferenceConstants.CODEGEN_USE_OVERRIDE_ANNOTATION, true);

        fProject = new TestProject("testWizards");
        fJProject = fProject.getJavaProject();
        fSourceFolder = fProject.getSourceFolder();

        GroovyRuntime.addGroovyRuntime(fJProject.getProject());
    }

    @Override
    protected void tearDown() throws Exception {
        fProject.dispose();
    }

}
