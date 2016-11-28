/*
 * Copyright 2009-2016 the original author or authors.
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

import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.ui.PreferenceConstants;

/**
 * @author ns
 * @created May 18, 2010
 */
public abstract class AbstractNewGroovyWizardTest extends EclipseTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
        setJavaPreference(PreferenceConstants.CODEGEN_ADD_COMMENTS, "false");
        setJavaPreference(PreferenceConstants.CODEGEN_USE_OVERRIDE_ANNOTATION, "true");
    }
}
