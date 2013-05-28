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
package org.codehaus.groovy.eclipse.refactoring.core.rewriter;

import groovyjarjarasm.asm.Opcodes;

import java.util.List;

import org.codehaus.groovy.antlr.LineColumn;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.core.utils.FilePartReader;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class ASTWriterHelper implements Opcodes {

	public static final int MOD_FIELD = 1;
    public static final int MOD_CLASS = 2;
    public static final int MOD_METHOD = 3;

	/**
     * Converts the encripted modifier to the string representation
     *
     * @param modifiers encripted modifier
     * @param appearance where is the appearance of the modifier
     * @return modifiers as String
     */
    public static String getAccModifier(int modifiers, int appearance) {
    	StringBuilder accMod = new StringBuilder();
    	if ((modifiers & ACC_PRIVATE) != 0) {
    		accMod.append("private ");
    	}
    	else if ((modifiers & ACC_PUBLIC) != 0) {
    		if (appearance == MOD_METHOD)
    			accMod.append("def ");
    		else if (appearance == MOD_FIELD)
    			accMod.append("public ");
    		//for class, write nothing

    	}
    	else if ((modifiers & ACC_PROTECTED) != 0) {
    		accMod.append("protected ");
    	}
    	if ((modifiers & ACC_STATIC) != 0) {
    		accMod.append("static ");
    	}
    	if ((modifiers & ACC_TRANSIENT) != 0) {
    		accMod.append("transient ");
    	}
    	if ((modifiers & ACC_FINAL) != 0) {
    		accMod.append("final ");
    	}
    	if ((modifiers & ACC_SYNCHRONIZED) != 0) {
    		accMod.append("synchronized ");
    	}
    	if ((modifiers & ACC_VOLATILE) != 0) {
    		accMod.append("volatile ");
    	}
    	if ((modifiers & ACC_NATIVE) != 0) {
    		accMod.append("native ");
    	}
    	if ((modifiers & ACC_STRICT) != 0) {
    		accMod.append("strictfp ");
    	}
    	return accMod.toString();
    }
    /**
     * Evaluates if the FieldNode is a property, by searching the same FieldNode in the
     * property list. When the FieldNode is found in the property list the node is a property
     * @param node
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
		if(expressionInFile.length() >= 3){
			 firstThreeChars = expressionInFile.substring(0, 3);
			 firstThreeCharsAreSame = 	((firstThreeChars.charAt(0) == firstThreeChars.charAt(1)) &&
										(firstThreeChars.charAt(1) == firstThreeChars.charAt(2)));
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
