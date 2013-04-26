/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.formatter;

import static org.codehaus.greclipse.GroovyTokenTypeBridge.EOF;
import static org.codehaus.greclipse.GroovyTokenTypeBridge.LBRACK;
import static org.codehaus.greclipse.GroovyTokenTypeBridge.LCURLY;
import static org.codehaus.greclipse.GroovyTokenTypeBridge.LPAREN;
import static org.codehaus.greclipse.GroovyTokenTypeBridge.NLS;
import static org.codehaus.greclipse.GroovyTokenTypeBridge.RBRACK;
import static org.codehaus.greclipse.GroovyTokenTypeBridge.RCURLY;
import static org.codehaus.greclipse.GroovyTokenTypeBridge.RPAREN;
import static org.codehaus.greclipse.GroovyTokenTypeBridge.STRING_CTOR_END;
import static org.codehaus.greclipse.GroovyTokenTypeBridge.STRING_CTOR_START;
import groovyjarjarantlr.Token;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.greclipse.GroovyTokenTypeBridge;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;

/**
 * This class is intended to be a place to group together code related to
 * indentation of Groovy Documents. It can be used by different "clients"
 * that require indentation services. At present there are three different
 * situations under which this logic is used:
 *<ul>
 * <li>when pressing newline to compute indentation for the next line
 *
 * <li>when pressing tab to do smart tab indentation.
 *
 * <li>when pasting text to do smart paste indentation
 * <ul>
 * <p>
 * Because this code is used under several different circumstances, it doesn't
 * provide a "simple intuitive" interface. Rather it provides a number of
 * helpful methods to compute indentation levels. A number of utility methods
 * for dealing with white space etc. are also included.
 * <p>
 * The GroovyIndentationService class makes use of a GroovyDocumentScanner to
 * tokenize documents (or sections thereof). This is not currently exposed
 * through public API, but it may seem reasonable to do so in the future (to
 * avoid having to create multiple DocumentScanners, which would be expensive).
 * <p>
 * At present the CTRL-I action is not handled by via this class. This still
 * uses Mike Klenk's implementation which works better when the region to indent
 * is large and we can get a good parse tree. Eventually, the logic in this
 * class should become smart enough to be able to replace Klenk's
 * implementation.
 *
 * @author kdvolder
 * @created 2010-06-08
 */
public class GroovyIndentationService {

    /**
     * Get an Indentation service that uses the right preferences for a given
     * IJavaProject.
     * The same object will be returned if the javaProject is the same as the
     * one from the
     * last request, but a new instance will be created if the project has
     * changed.
     *
     * @return
     */
    public static synchronized GroovyIndentationService get(IJavaProject javaProject) {
        if (lastIndentor == null || lastIndentor.getJavaProject() != javaProject) {
            lastIndentor = new GroovyIndentationService(javaProject);
        }
        return lastIndentor;
    }

    /**
     * Retrieves leading white space character from a String
     */
    public static String getLeadingWhiteSpace(String text) {
        int i = 0;
        while (i < text.length() && Character.isWhitespace(text.charAt(i)))
            i++;
        return text.substring(0, i);
    }

    /**
     * Retrieve the text of a line at a given line number in a document.
     *
     * @param d The document
     * @param lineNum The line number.
     * @return
     */
    public static String getLine(IDocument d, int lineNum) {
        try {
            String delim = d.getLineDelimiter(lineNum);
            int delimLen = delim == null ? 0 : delim.length();
            String string = d.get(d.getLineOffset(lineNum), d.getLineLength(lineNum) - delimLen);
            return string;
        } catch (BadLocationException e) {
            return "";
        }
    }
    /**
     * Gets the leading white space on a given line in the document.
     */
    public static String getLineLeadingWhiteSpace(IDocument d, int line) {
        return getLeadingWhiteSpace(getLine(d, line));
    }

    /**
     * Given an offset, retrieve the text from the beginning of the line this
     * offset is in, up to the offset.
     */
    public static String getLineTextUpto(IDocument d, int offset) throws BadLocationException {
        int line = d.getLineOfOffset(offset);
        int lineStart = d.getLineOffset(line);
        String lineStartText = d.get(lineStart, offset - lineStart);
        return lineStartText;
    }

    /**
     * Register an association between a pair of opening/closing braces or
     * parenthesis.
     * This is used for scanning backwards and determining the indentation level
     * after a closing brace, paren etc.
     * <p>
     * The arguments should be Token type constants from the
     * {@link GroovyTokenTypeBridge} class.
     */
    private static void openClosePair(int opener, int closer) {
        Assert.isTrue(!closer2opener.containsKey(closer));
        closer2opener.put(closer, opener);
        jumpIn.add(opener);
        jumpOut.add(closer);
    }
    private IJavaProject project;

    private IFormatterPreferences prefs;

    /**
     * A cached {@link GroovyDocumentScanner} instance. This will
     * be replaced when the document we are asked to work on changes
     * from the last time. Otherwise we will reuse the cached scanner.
     */
    private GroovyDocumentScanner cachedScanner;

    private static Set<Integer> jumpIn = new HashSet<Integer>();

    private static Set<Integer> jumpOut = new HashSet<Integer>();

    /**
     * A map relating the closer of a pair of braces/brackets/parens to its
     * corresponding opener.
     */
    private static Map<Integer, Integer> closer2opener = new HashMap<Integer, Integer>();


    /**
     * Caches a single instance for reuse, as long as we are working on the same
     * project.
     */
    private static GroovyIndentationService lastIndentor;

    static {
        openClosePair(LCURLY, RCURLY);
        openClosePair(LBRACK, RBRACK);
        openClosePair(LPAREN, RPAREN);
        openClosePair(STRING_CTOR_START, STRING_CTOR_END);
    }

    public GroovyIndentationService(IJavaProject project) {
        this.project = project;
    }

    /**
     * Compute indentation level for the next line after a newline is inserted
     * at a given offset.
     */
    public int computeIndentAfterNewline(IDocument d, int offset) throws BadLocationException {
        // To ensure we are basing this on a line that has actual tokens on
        // it...
        Token token = getTokenBefore(d, offset);
        int line = token == null ? 0 : getLine(token);
        int orgIndentLevel = token == null ? 0 : getLineIndentLevel(d, line);
        List<Token> tokens = getTokens(d, d.getLineOffset(line), offset);

        int indentLevel = simpleComputeNextLineIndentLevel(orgIndentLevel, tokens);
        if (indentLevel < orgIndentLevel) {
            // Jumping back from indentation is more complex.
            Token lastToken = tokens.get(tokens.size() - 1);
            if (lastToken.getType() == GroovyTokenTypeBridge.NLS)
                lastToken = getTokenBefore(d, lastToken);
            if (isCloserOfPair(lastToken)) {
                // A somewhat better strategy for newline after closing
                // brackets, parens or braces.
                indentLevel = getIndentLevelForCloserPair(d, lastToken);
            }
        }
        return indentLevel;
    }

    /**
     * Get the line number in the document for a given Token.
     *
     * @return 0-based index, as used by Eclipse IDocument interface.
     */
    private int getLine(Token token) {
        return token.getLine() - 1; // Antlr line numbers start at 1.
    }

    /**
     * Compute the proper indentation level for a given line. This may be
     * different from its actual indentation level.
     */
    public int computeIndentForLine(IDocument d, int line) {
        try {
            if (line == 0) {
                return 0;
            } else {
                Token nextToken = getTokenFrom(d, d.getLineOffset(line));
                if (isCloserOfPair(nextToken)) {
                    // Don't use the newline mechanism! Line up the with
                    // matching opening brace instead.
                    return getIndentLevelForCloserPair(d, nextToken);
                } else {
                    IRegion prevLine = d.getLineInformation(line - 1);
                    return computeIndentAfterNewline(d, prevLine.getOffset() + prevLine.getLength());
                }
            }
        } catch (BadLocationException e) {
            GroovyCore.logException("internal error", e);
            return 0;
        }
    }

    /**
     * Create indentation characters properly using tabs and spaces, equivalent
     * to a given number of spaces.
     */
    public String createIndentation(int spaces) {
        return createIndentation(getPrefs(), spaces);
    }

    public void dispose() {
        this.project = null;
        if (this.cachedScanner != null) {
            this.cachedScanner.dispose();
            this.cachedScanner = null;
        }
        this.prefs = null;
    }

    /**
     * When no longer requiring the services for a little while,
     * you can call this method to save a little memorty (assuming
     * you will be refreshing prefs next time anyway.
     */
    public void disposePrefs() {
        this.prefs = null;
    }

    @Override
    protected void finalize() throws Throwable {
        this.dispose();
        super.finalize();
    }

    public void fixIndentation(Document workCopy, int line, int newIndentLevel) {
        try {
            IRegion lineRegion = workCopy.getLineInformation(line);
            String text = workCopy.get(lineRegion.getOffset(), lineRegion.getLength()).trim();
            text = createIndentation(newIndentLevel) + text;
            workCopy.replace(lineRegion.getOffset(), lineRegion.getLength(), text);
        } catch (BadLocationException e) {
            GroovyCore.logException("internal error", e);
        }
    }

    private GroovyDocumentScanner getGroovyDocumentScanner(IDocument d) {
        if (cachedScanner != null && cachedScanner.getDocument() == d)
            return cachedScanner;
        // Either we have no scanner, or it is invalid (wrong document)
        if (cachedScanner != null) {
            cachedScanner.dispose();
            cachedScanner = null;
        }
        cachedScanner = new GroovyDocumentScanner(d);
        return cachedScanner;
    }

    /**
     * Get the current indentation level for line at given offset in document.
     */
    public int getIndentLevel(IDocument d, int offset) {
        try {
            return getLineIndentLevel(d, d.getLineOfOffset(offset));
        } catch (BadLocationException e) {
            // Presumably that line does not exist, so we use 0 as default
            // indentation level
            return 0;
        }
    }

    /**
     * Search backwards for a matching brace from position just after a closing
     * brace.
     *
     * @return the indentation level of the line at which the matching brace
     *         is found.
     */
    private int getIndentLevelForCloserPair(IDocument d, Token closer) {
        GroovyDocumentScanner scanner = getGroovyDocumentScanner(d);
        int closerType = closer.getType();
        int openerType = closer2opener.get(closerType);
        int closeCount = 1;
        Token token = closer;
        try {
            while (closeCount != 0 && (token = scanner.getLastTokenBefore(token)) != null) {
                if (token.getType() == openerType)
                    closeCount--;
                if (token.getType() == closerType)
                    closeCount++;
            }
            return getIndentLevel(d, scanner.getOffset(token));
        } catch (BadLocationException e) {
            // Something went wrong. Just use indent level of the line itself as
            // a "sensible" default.
            try {
                return getIndentLevel(d, scanner.getOffset(closer));
            } catch (BadLocationException e1) {
                return 0;
            }
        }
    }

    /**
     * Indentation service behavior is dependent on project preferences, so
     * there is
     * a javaProject associated with an {@link GroovyIndentationService}. This
     * method
     * returns the associated IJavaProject. This instance
     * may be null, in which case the core java preferences should be used.
     */
    public IJavaProject getJavaProject() {
        return project;
    }

    /**
     * This does the same as the getLineDelimeter method on Document, except it
     * returns "" instead of null when there is no line delimiter for this line.
     *
     * @throws BadLocationException
     */
    public String getLineDelimiter(IDocument d, int lineNum) throws BadLocationException {
        String result = d.getLineDelimiter(lineNum);
        if (result == null)
            return "";
        else
            return result;
    }

    /**
     * Determine the current indentation level of a given line to which a
     * command
     * applies.
     *
     * @return The number of spaces equivalent to the leading white space of the
     *         current line.
     */
    public int getLineIndentLevel(IDocument d, int lineNum) {
        return indentLevel(getLine(d, lineNum));
    }

    private List<Token> getLineTokensUpto(IDocument d, int offset) {
        return getGroovyDocumentScanner(d).getLineTokensUpto(offset);
    }

    public IFormatterPreferences getPrefs() {
        if (prefs == null)
            refreshPrefs();
        return prefs;
    }

    /**
     * @return A String equivalent to one tab, using either a tab,
     *         or a number of spaces, in accordance with the preferences.
     */
    public String getTabString() {
        return createIndentation(getPrefs().getTabSize());
    }

    private Token getTokenBefore(IDocument d, Token token) throws BadLocationException {
        return getGroovyDocumentScanner(d).getLastTokenBefore(token);
    }

    private Token getTokenBefore(IDocument d, int offset) throws BadLocationException {
        return getGroovyDocumentScanner(d).getLastTokenBefore(offset);
    }

    private Token getTokenFrom(IDocument d, int offset) {
        return getGroovyDocumentScanner(d).getTokenFrom(offset);
    }

    private List<Token> getTokens(IDocument d, int start, int end) {
        return getGroovyDocumentScanner(d).getTokens(start, end);
    }

    /**
     * Determine the current indentation level of a given line of text.
     *
     * @return The number of spaces equivalent to the leading white space of the
     *         lineText
     */
    public int indentLevel(String lineText) {
        int level = 0;
        for (int i = 0; i < lineText.length(); i++) {
            switch (lineText.charAt(i)) {
                case ' ':
                    level++;
                    break;
                case '\t':
                    level += getPrefs().getTabSize();
                    break;
                default:
                    return level;
            }
        }
        return level;
    }

    /**
     * Determine whether position is after a "{" (ignoring white space or
     * comments, but not NLS)
     */
    public boolean isAfterOpeningBrace(IDocument d, int pos) {
        Token token = getGroovyDocumentScanner(d).getLastTokenBefore(pos);
        return token != null && token.getType() == LCURLY;
    }

    /**
     * Determine whether a given token is close "opener" of a pair. That
     * is, whether is a closing parenthesis, brace or bracket.
     */
    private boolean isCloserOfPair(Token lastToken) {
        return closer2opener.containsKey(lastToken.getType());
    }

    /**
     * Determine whether position represent an end of line (ignoring white space
     * or comments)
     */
    public boolean isEndOfLine(IDocument d, int pos) {
        Token token = getGroovyDocumentScanner(d).getTokenFrom(pos);
        return token == null || token.getType() == NLS || token.getType() == EOF;
    }

    /**
     * @return true if the offset is inside an empty line in the document.
     */
    public boolean isInEmptyLine(IDocument d, int offset) {
        int line;
        try {
            line = d.getLineOfOffset(offset);
            String text = getLine(d, line);
            return text.trim().length() == 0;
        } catch (BadLocationException e) {
            GroovyCore.logException("Internal error", e);
            return false;
        }
    }

    /**
     * Get default line delimiter for a given document.
     */
    public String newline(IDocument d) {
        return TextUtilities.getDefaultLineDelimiter(d);
    }

    /**
     * This method can be called to ensure prefs are up to date.
     * Typically this gets called before performing a series of
     * indentation related stuff on a document.
     * <p>
     * It will also be called automatically when you ask for the preferences the
     * first time, or the first time after having called disposePrefs.
     */
    public void refreshPrefs() {
        this.prefs = new FormatterPreferences(project);
    }

    /**
     * Compute an adjusted indentation level for the next line, based on
     * some simple heuristics about the types of tokens seen only in the
     * previous
     * line.
     */
    private int simpleComputeNextLineIndentLevel(int indentLevel, List<Token> tokens) {
        int adjust = getOpenVersusCloseBalance(tokens);
        if (adjust > 0)
            indentLevel += getPrefs().getIndentationSize();
        else if (adjust < 0)
            indentLevel = indentLevel - getPrefs().getIndentationSize();
        return indentLevel;
    }

    public int getOpenVersusCloseBalance(List<Token> tokens) {
        int adjust = 0;
        for (Token tok : tokens) {
            if (jumpIn.contains(tok.getType()))
                adjust++;
            if (jumpOut.contains(tok.getType()))
                adjust--;
        }
        return adjust;
    }

    public static String createIndentation(IFormatterPreferences pref, int spaces) {
        StringBuilder gap = new StringBuilder();
        if (pref.useTabs()) {
            // Using tabs and possible some trailing space to make up the rest
            int tabSize = pref.getTabSize();
            while (spaces >= tabSize) {
                // Keep using tabs until the remaining spaces is less than a tab
                gap.append('\t');
                spaces -= tabSize;
            }
            while (spaces > 0) {
                gap.append(' ');
                spaces--;
            }
        } else {
            // Only use spaces
            for (int i = 0; i < spaces; i++) {
                gap.append(' ');
            }
        }
    	return gap.toString();
    }

    public boolean moreOpenThanCloseBefore(IDocument d, int offset) {
        return getOpenVersusCloseBalance(getLineTokensUpto(d, offset)) > 0;
    }

    /**
     *
     * @param d
     * @param offset offset into document
     * @return the length from the insert location to the start of the curly
     *         else 0 if the next token is not a close curly
     * @throws BadLocationException
     */
    public int lengthToNextCurly(IDocument d, int offset) throws BadLocationException {
        Token token = getTokenFrom(d, offset);
        // must make sure there is no newline
        if (!isEndOfLine(d, offset) && RCURLY == token.getType()) {
            return token.getColumn() - offset + d.getLineOffset(d.getLineOfOffset(offset));
        } else {
            return 0;
        }
    }
}
