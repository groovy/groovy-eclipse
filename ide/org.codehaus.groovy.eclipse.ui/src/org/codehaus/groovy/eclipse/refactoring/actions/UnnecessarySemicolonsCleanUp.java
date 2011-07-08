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
package org.codehaus.groovy.eclipse.refactoring.actions;

import org.codehaus.groovy.eclipse.refactoring.formatter.GroovyFormatter;
import org.codehaus.groovy.eclipse.refactoring.formatter.SemicolonRemover;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.fix.TextEditFix;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.text.edits.TextEdit;

public class UnnecessarySemicolonsCleanUp extends AbstractGroovyCleanUp {

    @Override
    public ICleanUpFix createFix(CleanUpContext context) throws CoreException {
        ICompilationUnit unit = context.getCompilationUnit();

        if (!(unit instanceof GroovyCompilationUnit)) {
            return null;
        }

        GroovyCompilationUnit gunit = (GroovyCompilationUnit) unit;
        char[] contents = gunit.getContents();
        ITextSelection selection = new TextSelection(0, contents.length);
        IDocument document = new Document(new String(contents));
        GroovyFormatter formatter = new SemicolonRemover(selection, document);

        TextEdit edit = formatter.format();
        return new TextEditFix(edit, gunit, "Remove unnecessary semicolons.");
    }

    @Override
    public String[] getStepDescriptions() {
        return new String[] { "Remove unnecessary semicolons." };
    }
}
