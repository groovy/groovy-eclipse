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
package org.codehaus.groovy.eclipse.refactoring.ui.extract;

import org.codehaus.groovy.eclipse.refactoring.core.extract.ExtractGroovyMethodRefactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 *
 * @author andrew
 * @created May 17, 2010
 */
public class ExtractMethodRefactoringWizard extends RefactoringWizard {
    public ExtractMethodRefactoringWizard(ExtractGroovyMethodRefactoring refactoring) {
        super(refactoring, DIALOG_BASED_USER_INTERFACE);
    }

    @Override
    protected void addUserInputPages() {
        ExtractGroovyMethodRefactoring groovyRefactoring = (ExtractGroovyMethodRefactoring) getRefactoring();
        super.addPage(new ExtractMethodPage(groovyRefactoring.getName(), (ExtractGroovyMethodRefactoring) getRefactoring()));
    }
}
