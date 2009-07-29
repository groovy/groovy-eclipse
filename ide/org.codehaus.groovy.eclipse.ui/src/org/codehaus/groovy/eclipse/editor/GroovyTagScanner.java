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

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class GroovyTagScanner extends RuleBasedScanner {
	private static String[] types =
	{
		"boolean",
		"byte",
		"char",
		"class",
		"double",
		"float",
		"int",
		"interface",
		"long",
		"short",
		"void"
	};
	private static String[] keywords = 
	{   
		"abstract",
		"break",
		"case",
		"catch",
		"const",
		"continue",
		"def",
		"default",
		"do",
		"else",
		"extends",
		"final",
		"finally",
		"for",
		"goto",
		"if",
		"implements",
		"import",
		"instanceof",
		"interface",
		"native",
		"new",
		"package",
		"private",
		"protected",
		"public",
		"return",
		"static",
		"super",
		"switch",
		"synchronized",
		"this",
		"throw",
		"throws",
		"transient",
		"try",
		"volatile",
		"while",
		"true",
		"false",
		"null",
		"void"
	};
	private static String[] groovyKeywords = {
		"as",
		"assert",
		"def",
		"mixin",
		"property",
		"test",
		"using",
		"in",
		
		// for EasyB, probably shouldn't keep
		"after",
		"before",
		"scenario",
		"then",
		"given",
		"and",
		"when"
	};
	
	private static String[] gjdkWords = {
		"abs",
		"any",
		"append",
		"asList",
		"asWritable",
		"call",
		"collect",
		"compareTo",
		"count",
		"div",
		"dump",
		"each",
		"eachByte",
		"eachFile",
		"eachLine",
		"every",
		"find",
		"findAll",
		"flatten",
		"getAt",
		"getErr",
		"getIn",
		"getOut",
		"getText",
		"grep",
		"immutable",
		"inject",
		"inspect",
		"intersect",
		"invokeMethods",
		"isCase",
		"join",
		"leftShift",
		"minus",
		"multiply",
		"newInputStream",
		"newOutputStream",
		"newPrintWriter",
		"newReader",
		"newWriter",
		"next",
		"plus",
		"pop",
		"power",
		"previous",
		"print",
		"println",
		"push",
		"putAt",
		"read",
		"readBytes",
		"readLines",
		"reverse",
		"reverseEach",
		"round",
		"size",
		"sort",
		"splitEachLine",
		"step",
		"subMap",
		"times",
		"toInteger",
		"toList",
		"tokenize",
		"upto",
		"waitForOrKill",
		"withPrintWriter",
		"withReader",
		"withStream",
		"withWriter",
		"withWriterAppend",
		"write",
		"writeLine"
	};

	public GroovyTagScanner(IColorManager manager) {
		List<IRule> rules = new ArrayList<IRule>();

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new GroovyWhitespaceDetector()));
		
		// add keywords rule
		IToken plainCode =
			new Token(
					new TextAttribute(manager.getColor(IJavaColorConstants.JAVA_DEFAULT)));
		
		WordRule keywordsRule = new WordRule(new IWordDetector(){
			
			public boolean isWordStart(char c) {
				return c == '@' || Character.isJavaIdentifierStart(c);
			}

			public boolean isWordPart(char c) {
				return Character.isJavaIdentifierPart(c);
			}
		
		},plainCode); 
		// add gjdk to the java keyword rule
		Preferences prefs = GroovyPlugin.getDefault().getPluginPreferences();
		IPreferenceStore store = GroovyPlugin.getDefault().getPreferenceStore();
		if (prefs.getBoolean(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_ENABLED)) {
			RGB gjdkRGB = PreferenceConverter.getColor(store,PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR);
			IToken gjdkToken = new Token(new TextAttribute(new Color(null,gjdkRGB), null, SWT.BOLD));
			for (int j = 0; j < gjdkWords.length; ++j) {
				keywordsRule.addWord(gjdkWords[j],gjdkToken);
			}
		}
		if (prefs.getBoolean(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_ENABLED)) {
			RGB rgb = PreferenceConverter.getColor(store,PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR);
			IToken token = new Token(new TextAttribute(new Color(null,rgb), null, SWT.BOLD));
			for (int j = 0; j < keywords.length; ++j) {
				keywordsRule.addWord(keywords[j],token);
			}
		}
		if (prefs.getBoolean(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_ENABLED)) {
			RGB rgb = PreferenceConverter.getColor(store,PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_COLOR);
			IToken token = new Token(new TextAttribute(new Color(null,rgb), null, SWT.BOLD));
			for (int j = 0; j < groovyKeywords.length; ++j) {
				keywordsRule.addWord(groovyKeywords[j],token);
			}
		}
		if (prefs.getBoolean(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_ENABLED)) {
			RGB rgb = PreferenceConverter.getColor(store,PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR);
			IToken token = new Token(new TextAttribute(new Color(null,rgb), null, SWT.BOLD));
			for (int j = 0; j < types.length; ++j) {
				keywordsRule.addWord(types[j],token);
			}
		}
		if (prefs.getBoolean(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_ENABLED)) {
			RGB rgb = PreferenceConverter.getColor(store,PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR);
			IToken token = new Token(new TextAttribute(new Color(null,rgb), null, SWT.NONE));
			rules.add( new NumberRule(token));
		}
        if (prefs.getBoolean(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_ENABLED)) {
            RGB rgb = PreferenceConverter.getColor(store,PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_COLOR);
            IToken token = new Token(new TextAttribute(new Color(null,rgb), null, SWT.NONE));
            rules.add( new EndOfLineRule("//", token));
            rules.add( new EndOfLineRule("#!", token));
        }
        
        if (prefs.getBoolean(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_ENABLED)) {
            RGB rgb = PreferenceConverter.getColor(store,PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR);
            IToken token = new Token(new TextAttribute(new Color(null,rgb), null, SWT.ITALIC));
            rules.add( new SingleLineRule("/", "/", token, '\\'));
        }
        
		

		rules.add(keywordsRule); 
		// convert rules list to array and return
		IRule[] ruleArray = new IRule[rules.size()];
		rules.toArray(ruleArray);
		setRules(ruleArray);
	}
}
