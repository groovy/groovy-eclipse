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

import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;

/**
 * This is a common superclass for different Auto Edit strategies we may
 * implement. This class provides a convenient place to put useful helper
 * methods.
 *
 * @author kdvolder
 * @created 2010-05-19
 */
public abstract class AbstractAutoEditStrategy implements IAutoEditStrategy {

    protected boolean isNewline(IDocument document, String text) {
        String[] delimiters = document.getLegalLineDelimiters();
        if (delimiters != null)
            for (String nl : delimiters) {
                if (nl.equals(text))
                    return true;
            }
        return false;
    }

}
