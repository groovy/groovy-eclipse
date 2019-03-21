/*
 * Copyright 2009-2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.actions;

import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter;
import org.codehaus.groovy.eclipse.refactoring.formatter.FormatterPreferences;
import org.codehaus.groovy.eclipse.refactoring.formatter.IFormatterPreferences;
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

/**
 * @author Andrew Eisenberg
 * @created Aug 18, 2009
 */
public class GroovyCodeFormatCleanUp extends AbstractGroovyCleanUp {

    private final FormatKind kind;

    public GroovyCodeFormatCleanUp(FormatKind kind) {
        this.kind = kind;
    }

    @Override
    public ICleanUpFix createFix(CleanUpContext context) throws CoreException {
        ICompilationUnit unit = context.getCompilationUnit();

        if (!(unit instanceof GroovyCompilationUnit)) {
            return null;
        }

        GroovyCompilationUnit gunit = (GroovyCompilationUnit) unit;
        char[] contents = gunit.getContents();
        ITextSelection sel = new TextSelection(0, contents.length);
        IDocument doc = new Document(new String(contents));
        boolean isIndentOnly = kind == FormatKind.INDENT_ONLY;
        IFormatterPreferences preferences = new FormatterPreferences(gunit);

        DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(sel, doc, preferences, isIndentOnly);
        TextEdit edit = formatter.format();

        return new TextEditFix(edit, gunit, "Format groovy source code.");
    }

    @Override
    public String[] getStepDescriptions() {
        return new String[] { "Format groovy source code." };
    }
}
