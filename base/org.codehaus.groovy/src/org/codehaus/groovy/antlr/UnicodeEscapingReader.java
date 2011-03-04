/*
 * Copyright 2003-2011 the original author or authors.
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


package org.codehaus.groovy.antlr;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import antlr.CharScanner;

/**
 * Translates GLS-defined unicode escapes into characters. Throws an exception
 * in the event of an invalid unicode escape being detected.
 *
 * <p>No attempt has been made to optimise this class for speed or
 * space.</p>
 *
 * @version $Revision: 7922 $
 */

// GRECLIPSE: start
/* old { 
public class UnicodeEscapingReader extends Reader implements UnicodeUnescaper {
 */
// add interface
public class UnicodeEscapingReader extends Reader implements UnicodeUnescaper {
// end
    private final Reader reader;
    private CharScanner lexer;
    private boolean hasNextChar = false;
    private int nextChar;
    private final SourceBuffer sourceBuffer;
   
    // GRECLIPSE: start
    // GRECLIPSE-805 keep track of unicode escape sequences
    int previousLine;
    int numUnicodeEscapesFound = 0;
    int numUnicodeEscapesFoundOnCurrentLine = 0;
    // end
    
    /**
     * Constructor.
     * @param reader The reader that this reader will filter over.
     */
    public UnicodeEscapingReader(Reader reader,SourceBuffer sourceBuffer) {
        this.reader = reader;
        this.sourceBuffer = sourceBuffer;
        if (sourceBuffer != null) {
            sourceBuffer.setUnescaper(this);
        }
    }

    /**
     * Sets the lexer that is using this reader. Must be called before the
     * lexer is used.
     */
    public void setLexer(CharScanner lexer) {
        this.lexer = lexer;
    }

    /**
     * Reads characters from the underlying reader.
     * @see java.io.Reader#read(char[],int,int)
     */
    public int read(char cbuf[], int off, int len) throws IOException {
        int c = 0;
        int count = 0;
        while (count < len && (c = read())!= -1) {
            cbuf[off + count] = (char) c;
            count++;
        }
        return (count == 0 && c == -1) ? -1 : count;
    }

    /**
     * Gets the next character from the underlying reader,
     * translating escapes as required.
     * @see java.io.Reader#close()
     */
    public int read() throws IOException {
        if (hasNextChar) {
            hasNextChar = false;
            write(nextChar);
            return nextChar;
        }
        
        // GRECLIPSE: start
        if (previousLine != lexer.getLine()) {
            // new line, so reset unicode escapes
            numUnicodeEscapesFoundOnCurrentLine = 0;
            previousLine = lexer.getLine();
        }
        // end
        
        int c = reader.read();
        if (c != '\\') {
            write(c);
            return c;
        }

        // Have one backslash, continue if next char is 'u'
        c = reader.read();
        if (c != 'u') {
            hasNextChar = true;
            nextChar = c;
            write('\\');
            return '\\';
        }

        // Swallow multiple 'u's
        do {
            c = reader.read();
        } while (c == 'u');

        // Get first hex digit
        checkHexDigit(c);
        StringBuffer charNum = new StringBuffer();
        charNum.append((char) c);

        // Must now be three more hex digits
        for (int i = 0; i < 3; i++) {
            c = reader.read();
            checkHexDigit(c);
            charNum.append((char) c);
        }
        int rv = Integer.parseInt(charNum.toString(), 16);
        write(rv);
        
        // GRECLIPSE: start
        // remember location of this escape char
        numUnicodeEscapesFound += 5;
        numUnicodeEscapesFoundOnCurrentLine += 5;
        // end
        
        return rv;
    }
    private void write(int c) {
        if (sourceBuffer != null) {sourceBuffer.write(c);}
    }
    /**
     * Checks that the given character is indeed a hex digit.
     */
    private void checkHexDigit(int c) throws IOException {
        if (c >= '0' && c <= '9') {
            return;
        }
        if (c >= 'a' && c <= 'f') {
            return;
        }
        if (c >= 'A' && c <= 'F') {
            return;
        }
        // Causes the invalid escape to be skipped
        hasNextChar = true;
        nextChar = c;
        throw new IOException("Did not find four digit hex character code."
                + " line: " + lexer.getLine() + " col:" + lexer.getColumn());
    }

    // GRECLIPSE: start new methods
    public int getUnescapedUnicodeColumnCount() {
        return numUnicodeEscapesFoundOnCurrentLine;
    }
    
    public int getUnescapedUnicodeOffsetCount() {
        return numUnicodeEscapesFound /*- numUnicodeEscapesFoundOnCurrentLine*/;
    }
    // end
    
    /**
     * Closes this reader by calling close on the underlying reader.
     * @see java.io.Reader#close()
     */
    public void close() throws IOException {
        reader.close();
    }
}
