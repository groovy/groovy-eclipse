/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.editor;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.jdt.ui.text.IJavaPartitions.*;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

public class GroovyPartitionScanner extends RuleBasedPartitionScanner {

	public final static String GROOVY_MULTILINE_STRINGS= "__groovy_multiline_string"; //$NON-NLS-1$
    /**
     * Array with legal content types.
     * @since 3.0
     */
    public final static String[] LEGAL_CONTENT_TYPES= new String[] {
        JAVA_DOC,
        JAVA_MULTI_LINE_COMMENT,
        JAVA_SINGLE_LINE_COMMENT,
        JAVA_STRING,
        JAVA_CHARACTER,
        GROOVY_MULTILINE_STRINGS
    };

	/**
	 * Detector for empty comments.
	 */
	static class EmptyCommentDetector implements IWordDetector {

		/* (non-Javadoc)
		* Method declared on IWordDetector
	 	*/
		public boolean isWordStart(char c) {
			return (c == '/');
		}

		/* (non-Javadoc)
		* Method declared on IWordDetector
	 	*/
		public boolean isWordPart(char c) {
			return (c == '*' || c == '/');
		}
	}
	
	/**
	 * 
	 */
	static class WordPredicateRule extends WordRule implements IPredicateRule {
		
		private final IToken fSuccessToken;
		
		public WordPredicateRule(IToken successToken) {
			super(new EmptyCommentDetector());
			fSuccessToken= successToken;
			addWord("/**/", fSuccessToken); //$NON-NLS-1$
		}
		
		/*
		 * @see org.eclipse.jface.text.rules.IPredicateRule#evaluate(ICharacterScanner, boolean)
		 */
		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
			return super.evaluate(scanner);
		}

		/*
		 * @see org.eclipse.jface.text.rules.IPredicateRule#getSuccessToken()
		 */
		public IToken getSuccessToken() {
			return fSuccessToken;
		}
	}

	/**
	 * Creates the partitioner and sets up the appropriate rules.
	 */
	public GroovyPartitionScanner() {
		super();

		IToken comment= new Token(JAVA_MULTI_LINE_COMMENT);
		IToken mString = new Token(GROOVY_MULTILINE_STRINGS);
        IToken sString = new Token(JAVA_STRING);
        IToken sComment= new Token(JAVA_SINGLE_LINE_COMMENT);

		List<IRule> rules= new ArrayList<IRule>();

		// Add rule for single line comments.
		rules.add(new EndOfLineRule("//", sComment)); //$NON-NLS-1$


		// Add rule for strings and character constants.
//		rules.add(new SingleLineRule("\"", "\"", Token.UNDEFINED, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
        rules.add(new MultiLineRule("'''", "'''", mString, '\\'));
        rules.add(new MultiLineRule("\"\"\"", "\"\"\"", mString, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
        rules.add(new SingleLineRule("\"", "\"", sString, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
		rules.add(new SingleLineRule("'", "'", sString, '\\')); //$NON-NLS-2$ //$NON-NLS-1$


		// Add special case word rule.
		rules.add(new WordPredicateRule(comment));

		// Add rules for multi-line comments 
		rules.add(new MultiLineRule("/*", "*/", comment, (char) 0, true)); //$NON-NLS-1$ //$NON-NLS-2$
		
		
		IPredicateRule[] result= new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}
}
