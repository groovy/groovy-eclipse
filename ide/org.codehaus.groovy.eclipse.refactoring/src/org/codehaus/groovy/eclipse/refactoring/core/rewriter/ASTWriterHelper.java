/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.core.rewriter;

import java.util.List;

import org.codehaus.groovy.antlr.LineColumn;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.core.utils.FilePartReader;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class ASTWriterHelper {

    public static final int MOD_FIELD = 1;
    public static final int MOD_CLASS = 2;
    public static final int MOD_METHOD = 3;

    /**
     * Converts the encripted modifier to the string representation.
     *
     * @param modifiers encripted modifier
     * @param appearance where is the appearance of the modifier
     * @return modifiers as String
     */
    public static String getAccModifier(int modifiers, int appearance) {
        StringBuilder accMod = new StringBuilder();
        if (Flags.isPrivate(modifiers)) {
            accMod.append("private ");
        } else if (Flags.isPublic(modifiers)) {
            if (appearance == MOD_METHOD)
                accMod.append("def ");
            else if (appearance == MOD_FIELD)
                accMod.append("public ");
            // for class, write nothing
        } else if (Flags.isProtected(modifiers)) {
            accMod.append("protected ");
        }
        if (Flags.isStatic(modifiers)) {
            accMod.append("static ");
        }
        if (Flags.isTransient(modifiers)) {
            accMod.append("transient ");
        }
        if (Flags.isFinal(modifiers)) {
            accMod.append("final ");
        }
        if (Flags.isSynchronized(modifiers)) {
            accMod.append("synchronized ");
        }
        if (Flags.isVolatile(modifiers)) {
            accMod.append("volatile ");
        }
        if (Flags.isNative(modifiers)) {
            accMod.append("native ");
        }
        if (Flags.isStrictfp(modifiers)) {
            accMod.append("strictfp ");
        }
        return accMod.toString();
    }

    /**
     * Evaluates if the FieldNode is a property, by searching the same FieldNode in the
     * property list. When the FieldNode is found in the property list the node is a property
     *
     * @return true when FieldNode is a property, otherwise false
     */
    public static boolean isProperty(FieldNode node) {
        List<PropertyNode> properties = node.getOwner().getProperties();
        for (PropertyNode property : properties) {
            if (property.getField().equals(node)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Evaluates the different quotes of groovy string or returns an empty string
     * for the case the first character in the expression was not a groovy string quote
     *
     * @param currentDocument document to read
     * @param coords position to read
     * @return different quote versions of groovy strings
     */
    public static String getStringMarker(IDocument currentDocument, LineColumn coords) {
        if (currentDocument == null) {
            return "\"";
        }
        String expressionInFile;
        try {
            expressionInFile = FilePartReader.readForwardFromCoordinate(currentDocument, coords);
        } catch (BadLocationException e) {
            GroovyCore.logException("Error during refactoring...trying to recover", e);
            expressionInFile = "";
        }
        char charBefore = expressionInFile.charAt(0);
        String firstThreeChars = "";
        boolean firstThreeCharsAreSame = false;
        if (expressionInFile.length() >= 3) {
            firstThreeChars = expressionInFile.substring(0, 3);
            firstThreeCharsAreSame =
                firstThreeChars.charAt(0) == firstThreeChars.charAt(1) &&
                firstThreeChars.charAt(1) == firstThreeChars.charAt(2);
        }
        if (charBefore == '\'' || charBefore == '\"' || charBefore == '/') {
            if (firstThreeCharsAreSame) {
                return firstThreeChars;
            }
            return String.valueOf(charBefore);
        }
        return "\"";
    }
}
