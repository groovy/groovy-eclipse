/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.formatter.lineWrap;

import groovyjarjarantlr.Token;
import org.codehaus.greclipse.GroovyTokenTypeBridge;
import org.codehaus.groovy.eclipse.refactoring.formatter.GroovyBeautifier;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.ReplaceEdit;

public class SameLine extends CorrectLineWrap {

    public SameLine(GroovyBeautifier beautifier) {
        super(beautifier);
    }

    @Override
    public ReplaceEdit correctLineWrap(int pos, Token token) throws BadLocationException {
        ReplaceEdit correctEdit = null;
        if (beautifier.formatter.getPreviousTokenIncludingNLS(pos).getType() == GroovyTokenTypeBridge.NLS) {
            Token lastNotNLSToken = beautifier.formatter.getPreviousToken(pos);
            int replaceStart = beautifier.formatter.getOffsetOfTokenEnd(lastNotNLSToken) ;
            int replaceEnd = beautifier.formatter.getOffsetOfToken(token);
            correctEdit = new ReplaceEdit(replaceStart,replaceEnd-replaceStart," ");
        }
        return correctEdit;
    }
}
