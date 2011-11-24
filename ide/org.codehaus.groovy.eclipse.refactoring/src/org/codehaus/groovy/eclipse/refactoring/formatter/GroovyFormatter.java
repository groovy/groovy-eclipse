/*
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package org.codehaus.groovy.eclipse.refactoring.formatter;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.TextEdit;

public abstract class GroovyFormatter {

    protected ITextSelection selection;
    protected IDocument document;

    public GroovyFormatter(ITextSelection sel, IDocument doc) {
        this.selection = sel;
        this.document = doc;
    }

    /**
     * Formats <code>source</code> and returns a text edit that corresponds to
     * the difference between the given string and the formatted string.
     *
     * @return the text edit
     */
    public abstract TextEdit format();
}
