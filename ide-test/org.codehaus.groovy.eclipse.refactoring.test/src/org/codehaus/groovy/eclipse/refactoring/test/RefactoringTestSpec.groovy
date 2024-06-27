/*
 * Copyright 2009-2024 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.test

import java.util.regex.Matcher
import java.util.regex.Pattern

import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools
import org.codehaus.groovy.eclipse.refactoring.core.utils.FilePartReader
import org.eclipse.jface.text.BadLocationException
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.TextSelection

/**
 * Provides a data-driven test case by parsing a text file of the form:<pre>
 * ###prop
 * selectionType=points
 * startLine=3
 * startColumn=1
 * endLine=4
 * endColumn=0
 * newMethodName=myExtract
 * modifier=private
 * setPreferences=true
 * indentation=space
 * tabsize=4
 * ###src
 * def hh() {
 *     int[] i
 *     i
 * }
 * ###exp
 * def hh() {
 *     int[] i
 *     myExtract(i)
 * }
 *
 * private myExtract(int[] i) {
 *     i
 * }
 * ###end
 * </pre>
 */
class RefactoringTestSpec {

    boolean shouldFail
    final IDocument document
    final Map<String, String> properties

    private final File file
    private final String newLine
    private final Pattern origRegExp, expRegExp, propertiesRegExp

    RefactoringTestSpec(File file) {
        this.file = file
        newLine = FilePartReader.getLineDelimiter(new FileReader(file))
        origRegExp = Pattern.compile("###src${newLine}(.*)${newLine}###exp", Pattern.DOTALL)
        expRegExp = Pattern.compile("###exp${newLine}(.*)${newLine}###end", Pattern.DOTALL)
        propertiesRegExp = Pattern.compile("###prop${newLine}(.*)${newLine}###src", Pattern.DOTALL)

        document = getOrigin()
        properties = readProperties()
    }

    private Map<String, String> readProperties() {
        Map<String, String> properties = [:]
        def propertiesSection = propertiesRegExp.matcher(file.text)
        if (propertiesSection.find()) {
            String[] reults = propertiesSection.group(1).split(newLine)
            for (line in reults) {
                String[] prop = line.split('=')
                assert prop.length == 2 : "Initialisation of testproperties failed! (${prop})"
                properties.put(prop[0], prop[1])
            }
            if (properties.get('shouldFail') == 'true') {
                shouldFail = true
            }
        }
        properties.asUnmodifiable()
    }

    IDocument getOrigin() {
        getArea(origRegExp)
    }

    IDocument getExpected() {
        getArea(expRegExp)
    }

    private IDocument getArea(Pattern regExpression) {
        Matcher match = regExpression.matcher(file.text)
        if (match.find()) {
            return ASTTools.getDocumentWithSystemLineBreak(match.group(1))
        }
        return new Document()
    }

    TextSelection getUserSelection() {
        try {
            int startLine = getInt('startLine')
            int startColumn = getInt('startColumn')
            int endLine = getInt('endLine')
            int endColumn = getInt('endColumn')
            IRegion startRegion = document.getLineInformation(startLine - 1)
            int offset = startRegion.offset + startColumn - 1
            IRegion endRegion = document.getLineInformation(endLine - 1)
            int end = endRegion.offset + endColumn - 1
            return new TextSelection(offset, end - offset)
        } catch (BadLocationException e) {
            return new TextSelection(0, document.length)
        }
    }

    private int getInt(String key) {
        try {
            return Integer.parseInt(properties.get(key), 10)
        } catch (NumberFormatException e) {
            return -1
        }
    }
}
