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
package org.codehaus.groovy.eclipse.editor;


import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;


/**
 * A rule based JavaDoc scanner.
 */
public class GroovyDocScanner extends RuleBasedScanner {

	/**
	 * A key word detector.
	 */
	static class GroovyDocWordDetector implements IWordDetector {

	/* (non-Javadoc)
	 * Method declared on IWordDetector
	 */
		public boolean isWordStart(char c) {
			return (c == '@');
		}

		/* (non-Javadoc)
	 	* Method declared on IWordDetector
	 	*/
		public boolean isWordPart(char c) {
			return Character.isLetter(c);
		}
	}

	private static String[] fgKeywords= { "@author", "@deprecated", "@exception", "@param", "@return", "@see", "@serial", "@serialData", "@serialField", "@since", "@throws", "@version" }; //$NON-NLS-12$ //$NON-NLS-11$ //$NON-NLS-10$ //$NON-NLS-7$ //$NON-NLS-9$ //$NON-NLS-8$ //$NON-NLS-6$ //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$

	/**
	 * Create a new javadoc scanner for the given color provider.
	 * 
	 * @param provider the color provider
	 */
	 public GroovyDocScanner(IColorManager colorManager) {
		super();

		IToken keyword= new Token(new TextAttribute(colorManager.getColor(PreferenceConstants.GROOVY_EDITOR_GROOVYDOC_KEYWORD_COLOR)));
		IToken tag= new Token(new TextAttribute(colorManager.getColor(PreferenceConstants.GROOVY_EDITOR_GROOVYDOC_TAG_COLOR)));
		IToken link= new Token(new TextAttribute(colorManager.getColor(PreferenceConstants.GROOVY_EDITOR_GROOVYDOC_LINK_COLOR)));

		List<IRule> list= new ArrayList<IRule>();

		// Add rule for tags.
		list.add(new SingleLineRule("<", ">", tag)); //$NON-NLS-2$ //$NON-NLS-1$

		// Add rule for links.
		list.add(new SingleLineRule("{", "}", link)); //$NON-NLS-2$ //$NON-NLS-1$

		// Add generic whitespace rule.
		list.add(new WhitespaceRule(new GroovyWhitespaceDetector()));

		// Add word rule for keywords.
		WordRule wordRule= new WordRule(new GroovyDocWordDetector());
		for (int i= 0; i < fgKeywords.length; i++)
			wordRule.addWord(fgKeywords[i], keyword);
		list.add(wordRule);

		IRule[] result= new IRule[list.size()];
		list.toArray(result);
		setRules(result);
	}
}
