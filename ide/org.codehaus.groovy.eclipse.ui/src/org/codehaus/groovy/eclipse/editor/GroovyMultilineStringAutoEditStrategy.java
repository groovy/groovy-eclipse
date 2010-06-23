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
package org.codehaus.groovy.eclipse.editor;

import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;

/**
 * This class is responsible for handling auto edits inside of Groovy multi line
 * strings.
 * <p>
 * The present implementation simply delegates all requests to a
 * DefaultIndentLineAutoEditStrategy. This should be reasonable since that
 * strategy is meant for editing text, which is mostly what should be inside a
 * multiline String.
 *
 * @author kdvolder
 * @created 2010-05-19
 */
public class GroovyMultilineStringAutoEditStrategy extends AbstractAutoEditStrategy {

    private static final boolean DEBUG_PASTE = true;

    IAutoEditStrategy wrappee = new DefaultIndentLineAutoEditStrategy();

    public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
        if (c.text.length() > 2) {
            if (DEBUG_PASTE) {
                System.out.println("MultiLineString paste");
            }
            return;
        }
        wrappee.customizeDocumentCommand(d, c);
    }

}
