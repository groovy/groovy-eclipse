 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.editor.actions.EditorPartFacade;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IEditorPart;

public class TextUtils {
	/**
	 * Given some text, find the offset to the first match of some identifier in
	 * the text.
	 * 
	 * @param text
	 * @param identifier
	 * @return The offset, or -1 if there is no match.
	 */
	public static int findIdentifierOffset(String text, String identifier) {
		String notIdent = "[^a-zA-Z0-9_]";

//		Pattern pattern = Pattern.compile(notIdent + "(" + identifier + ")"
//				+ notIdent);
		Pattern pattern = Pattern.compile("(^|" + notIdent + ")(" + identifier + ")("
								+ notIdent + "|$)");
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			return matcher.start(2);
		}
		
		return -1;
	}
	
	/**
	 * Given an offset into some text, find the identifier that spans the
	 * offset. An identifier matches [_a-zA-Z][_a-zA-Z0-9]*
	 * 
	 * @param text
	 * @param offset
	 * @return A Region containing the offset and length of the identifier, or
	 *         null if an identifier was not found.
	 * @throws BadLocationException
	 */
	public static Region getIdentifier(String text, int off)
			throws BadLocationException {
	    int offset = off;
		// TODO: this is sleep deprived dailywtf material
		while (offset > 0 && Character.isJavaIdentifierPart(text.charAt(offset))) {
			--offset;
		}
		++offset;
		Pattern pattern = Pattern.compile("[_a-zA-Z][_a-zA-Z0-9]*");
		Matcher matcher = pattern.matcher(text);
		if (matcher.find(offset)) {
			do {
				int ix = matcher.start();
				String match = matcher.group();
				if (ix <= offset && offset < ix + match.length()) {
					return new Region(ix, match.length());
				}
				offset = ix;
			} while (matcher.find());
		}

		return null;
	}
	
	public static ASTNode getNodeOfInterest(IEditorPart editor){
		EditorPartFacade epf = new EditorPartFacade(editor);
		
		ClassNode[] classNodes = (ClassNode[]) epf.adaptEditorInput(ClassNode[].class);
		
		if (classNodes.length < 1) {
			return null;
		}
		
		ITextSelection sel = epf.getTextSelection();
		int line = sel.getStartLine();
		
		if (line < 0){
			return null;
		}
		
		ClassNode classNode = classNodes[0];
		List<ASTNode> astNodes = new ArrayList<ASTNode>();
		astNodes.addAll(classNode.getFields());
		astNodes.addAll(classNode.getMethods());
		for (int i = 0; i < astNodes.size(); i++) {
			ASTNode astNode = (ASTNode) astNodes.get(i);
			int startLine = astNode.getLineNumber()-1;
			int endLine = astNode.getLastLineNumber()-1;
			if (startLine <= line && line <= endLine){
				return astNode;
			}
		}
		
		return null;
		
	}
}
