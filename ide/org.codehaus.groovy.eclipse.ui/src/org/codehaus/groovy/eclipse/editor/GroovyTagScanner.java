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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.jdt.internal.ui.text.AbstractJavaScanner;
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
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class GroovyTagScanner extends AbstractJavaScanner {
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
		"def",
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
		"it",
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
		"use",
		"waitForOrKill",
		"withPrintWriter",
		"withReader",
		"withStream",
		"withWriter",
		"withWriterAppend",
		"write",
		"writeLine",
	};
	
	
	public static Set<String> gjdkSet = new HashSet<String>();
	static {
	    for (String gjdk : gjdkWords) {
            gjdkSet.add(gjdk);
        }
	}
	
	private final IColorManager manager;
	private final List<IRule> additionalRules;
	private final List<String> additionalGroovyKeywords;
	private final List<String> additionalGJDKKeywords;

	/**
	 * @deprecated
	 */
	public GroovyTagScanner(IColorManager manager) {
	    this(manager, null, null, null);
	}
	
	
	/**
	 * @param manager the color manager
	 * @param additionalRules Additional scanner rules for sub-types to add new kinds of partitioning
	 * @param additionalGroovyKeywords Additional keywords for sub-types to add new kinds of syntax highlighting
     * @deprecated use the syntaxHighlightingExtender extension point instead.  This gets all of the additional keyword 
     * highlighting into editors of files in a project with a particular nature.
	 */
	public GroovyTagScanner(IColorManager manager, List<IRule> additionalRules, List<String> additionalGroovyKeywords) {
	    this(manager, additionalRules, additionalGroovyKeywords, null);
	}
    /**
     * @param manager the color manager
     * @param additionalRules Additional scanner rules for sub-types to add new kinds of partitioning
     * @param additionalGroovyKeywords Additional keywords for sub-types to add new kinds of groovy keyword syntax highlighting
     * @param additionalGJDKKeywords Additional keywords for sub-types to add new kinds of gjdk syntax highlightin
     */
    public GroovyTagScanner(IColorManager manager, List<IRule> additionalRules, List<String> additionalGroovyKeywords, List<String> additionalGJDKKeywords) {
        super(manager, GroovyPlugin.getDefault().getPreferenceStore());
        this.manager = manager;
        this.additionalRules = additionalRules;
        this.additionalGroovyKeywords = additionalGroovyKeywords;
        this.additionalGJDKKeywords = additionalGJDKKeywords;
        initialize();
    }


    @Override
    protected List createRules() {
        return createGroovyRules(manager, additionalRules, additionalGJDKKeywords, additionalGroovyKeywords);
    }
    
    public static List<IRule> createGroovyRules(IColorManager manager, List<IRule> additionalRules, List<String> additionalGJDKKeywords, List<String> additionalGroovyKeywords) {
        
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
        IPreferenceStore store = GroovyPlugin.getDefault().getPreferenceStore();
        if (store.getBoolean(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_ENABLED)) {
            RGB gjdkRGB = PreferenceConverter.getColor(store,PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR);
            IToken gjdkToken = new Token(new TextAttribute(new Color(null,gjdkRGB), null, SWT.BOLD));
            for (int j = 0; j < gjdkWords.length; ++j) {
                keywordsRule.addWord(gjdkWords[j],gjdkToken);
            }
            // additional gjdk keywords
            if (additionalGJDKKeywords != null) {
                for (String additional : additionalGJDKKeywords) {
                    keywordsRule.addWord(additional,gjdkToken);
                }
            }
        }
        
        
        if (store.getBoolean(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_ENABLED)) {
            RGB rgb = PreferenceConverter.getColor(store,PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR);
            IToken token = new Token(new TextAttribute(new Color(null,rgb), null, SWT.BOLD));
            for (int j = 0; j < keywords.length; ++j) {
                keywordsRule.addWord(keywords[j],token);
            }
        }
        if (store.getBoolean(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_ENABLED)) {
            RGB rgb = PreferenceConverter.getColor(store,PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_COLOR);
            IToken token = new Token(new TextAttribute(new Color(null,rgb), null, SWT.BOLD));
            for (int j = 0; j < groovyKeywords.length; ++j) {
                keywordsRule.addWord(groovyKeywords[j],token);
            }
            // additional keywords
            if (additionalGroovyKeywords != null) {
                for (String additional : additionalGroovyKeywords) {
                    keywordsRule.addWord(additional,token);
                }
            }
        }
        if (store.getBoolean(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_ENABLED)) {
            RGB rgb = PreferenceConverter.getColor(store,PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR);
            IToken token = new Token(new TextAttribute(new Color(null,rgb), null, SWT.BOLD));
            for (int j = 0; j < types.length; ++j) {
                keywordsRule.addWord(types[j],token);
            }
        }
        if (store.getBoolean(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_ENABLED)) {
            RGB rgb = PreferenceConverter.getColor(store,PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR);
            IToken token = new Token(new TextAttribute(new Color(null,rgb), null, SWT.NONE));
            rules.add( new NumberRule(token));
        }
        if (store.getBoolean(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_ENABLED)) {
            RGB rgb = PreferenceConverter.getColor(store,PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_COLOR);
            IToken token = new Token(new TextAttribute(new Color(null,rgb), null, SWT.NONE));
            rules.add( new EndOfLineRule("//", token));
            rules.add( new EndOfLineRule("#!", token));
        }
        
        if (store.getBoolean(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_ENABLED)) {
            RGB rgb = PreferenceConverter.getColor(store,PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR);
            IToken token = new Token(new TextAttribute(new Color(null,rgb), null, SWT.ITALIC));
            rules.add( new SingleLineRule("/", "/", token, '\\'));
        }
        rules.add(keywordsRule); 
        
        // additional rules
        if (additionalRules != null) {
            rules.addAll(additionalRules);
        }
        
        return rules;
    }


    /**
     * Not used
     */
    @Override
    protected String[] getTokenProperties() {
        return new String[0];
    }
}
