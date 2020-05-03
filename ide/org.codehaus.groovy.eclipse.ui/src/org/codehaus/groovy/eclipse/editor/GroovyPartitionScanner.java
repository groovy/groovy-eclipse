/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor;

import static org.eclipse.jdt.ui.text.IJavaPartitions.JAVA_CHARACTER;
import static org.eclipse.jdt.ui.text.IJavaPartitions.JAVA_DOC;
import static org.eclipse.jdt.ui.text.IJavaPartitions.JAVA_MULTI_LINE_COMMENT;
import static org.eclipse.jdt.ui.text.IJavaPartitions.JAVA_SINGLE_LINE_COMMENT;
import static org.eclipse.jdt.ui.text.IJavaPartitions.JAVA_STRING;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.preferences.PreferenceConstants;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class GroovyPartitionScanner extends RuleBasedPartitionScanner {

    public static final String GROOVY_MULTILINE_STRINGS = "__groovy_multiline_string";

    /**
     * @since 3.0
     */
    public static final String[] LEGAL_CONTENT_TYPES = new String[] {
        JAVA_CHARACTER,
        JAVA_STRING,
        JAVA_DOC,
        JAVA_MULTI_LINE_COMMENT,
        JAVA_SINGLE_LINE_COMMENT,
        GROOVY_MULTILINE_STRINGS,
    };

    /**
     * Creates the partitioner and sets up the appropriate rules.
     */
    public GroovyPartitionScanner() {
        super();
        List<IRule> rules = createRules(false);
        setPredicateRules(rules.toArray(new IPredicateRule[rules.size()]));
    }

    public static List<IRule> createRules(final boolean withColor) {
        List<IRule> rules = new ArrayList<>(8);

        IToken javadocComment = new Token(JAVA_DOC);
        IToken multilnComment = new Token(JAVA_MULTI_LINE_COMMENT);
        IToken endlineComment = new Token(JAVA_SINGLE_LINE_COMMENT);

        // special case for empty comments
        rules.add(new WordPredicateRule(multilnComment));

        // javadoc comments
        rules.add(new MultiLineRule("/**", "*/", javadocComment, (char) 0, true));

        // multi-line comments
        rules.add(new MultiLineRule("/*", "*/", multilnComment, (char) 0, true));

        // single-line comments
        rules.add(new EndOfLineRule("//", endlineComment));

        Object textAttr = null;
        if (withColor) {
            RGB rgb = PreferenceConverter.getColor(PreferenceConstants.getPreferenceStore(), PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR);
            textAttr = new TextAttribute(new Color(null, rgb), null, SWT.ITALIC);
        }
        IToken groovyString = new Token(textAttr != null ? textAttr : GROOVY_MULTILINE_STRINGS);
        IToken javaString = new Token(textAttr != null ? textAttr : JAVA_STRING);

        // multi-line strings
        rules.add(new MultiLineRule("'''", "'''", groovyString));
        rules.add(new MultiLineRule("\"\"\"", "\"\"\"", groovyString));

        // single-line strings
        rules.add(new SingleLineRule("'", "'", javaString, '\\'));
        rules.add(new SingleLineRule("\"", "\"", javaString, '\\'));

        // slashy and dollar-slashy strings are identified by semantic highlighting

        return rules;
    }

    //--------------------------------------------------------------------------

    static class EmptyCommentDetector implements IWordDetector {
        @Override
        public boolean isWordStart(final char c) {
            return (c == '/');
        }

        @Override
        public boolean isWordPart(final char c) {
            return (c == '*' || c == '/');
        }
    }

    static class WordPredicateRule extends WordRule implements IPredicateRule {

        private final IToken fSuccessToken;

        WordPredicateRule(final IToken successToken) {
            super(new EmptyCommentDetector());
            fSuccessToken = successToken;
            addWord("/**/", fSuccessToken); //$NON-NLS-1$
        }

        @Override
        public IToken evaluate(final ICharacterScanner scanner, final boolean resume) {
            return super.evaluate(scanner);
        }

        @Override
        public IToken getSuccessToken() {
            return fSuccessToken;
        }
    }
}
